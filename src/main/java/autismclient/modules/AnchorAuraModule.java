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
   private static final float init_F = 5.0F;
   private static final int init_I = 4;
   private static final int load_I = 4;
   private static final int read_I = 32;
   private static final int write_I = 6;
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
   private static int get = -1;
   private final Random random = new Random();
   private long init_J;
   private long load_J;
   private int set;
   private final Map<BlockPos, Integer> bannedCells = new HashMap<>();
   private LivingEntity currentTarget;
   private int put = -2147483648;
   private int add = -2147483648;
   private final autismclient.util.ActionCooldownScheduler stepCooldowns = new autismclient.util.ActionCooldownScheduler();
   private final autismclient.util.CombatAimSpeedController aimSpeed = new autismclient.util.CombatAimSpeedController();
   private final autismclient.util.SessionContextWatcher sessionWatcher = new autismclient.util.SessionContextWatcher();
   private final autismclient.util.StickyPlacementSelector stickySelector = new autismclient.util.StickyPlacementSelector();
   private final autismclient.util.TargetLeadDamageEstimator leadEstimator = new autismclient.util.TargetLeadDamageEstimator();
   private final autismclient.util.ActivationModeLatch activationLatch = new autismclient.util.ActivationModeLatch();
   private BlockPos init_BlockPos;
   private long read_J;
   private long write_J;
   private final Set<BlockPos> init_Set = new LinkedHashSet<>();
   private String entityFilterSource;
   private Set<String> entityFilterIds = Set.of();
   private int clear = -1;
   private int reset = -1;
   private int start;
   private int stop = -2147483648;
   private BlockPos activeAnchor;
   private LivingEntity activeTarget;
   private final autismclient.util.BlockSequenceSyncTracker sequenceSync = new autismclient.util.BlockSequenceSyncTracker();
   private int next;
   private int prev;
   private boolean init_Z;
   private boolean load_Z;
   private int valid = -2147483648;
   private boolean read_Z;
   private int size = -2147483648;
   private int data;
   private BlockPos read_BlockPos;
   private BlockPos write_BlockPos;
   private int value;
   private BlockPos plannedAnchor;
   private int cache = -1;
   private LivingEntity read_LivingEntity;
   private int state;
   private int queue;
   private BlockPos plannedShield;
   private boolean plannedShieldObsidian;
   private BlockPos flush_BlockPos;
   private BlockPos close_BlockPos;
   private BlockPos open_BlockPos;
   private int buffer = -2147483648;
   private BlockPos activeShield;
   private boolean update_Z;
   private BlockPos plannedSupport;
   private BlockPos activeSupport;
   private int entry;
   private int node;
   private int task;
   private autismclient.util.ExplosionDamageCalculator.SimOptions simOptions;
   private final Map<BlockPos, autismclient.util.ExplosionDamageCalculator.SimOptions> simOptionsByCell;
   private String read_Str;

   private static int latencyTicks() {
      return autismclient.util.LatencyTickWindow.windowTicks(autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs());
   }

   public AnchorAuraModule() {
      super("anchor-aura", "AnchorAura", ae.init_ae, "Blows targets up with respawn anchors.");
      this.simOptions = autismclient.util.ExplosionDamageCalculator.SimOptions.DEFAULT;
      this.simOptionsByCell = new HashMap<>();
      this.read_Str = "";
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
      this.init_V_2();
   }

   @Override
   public void onDisable() {
      this.init_V_2();
      autismclient.util.SilentRotationManager.releaseOwner("anchor-aura");
   }

   @Override
   public void onGameLeft() {
      this.init_V_2();
      if ("anchor-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         autismclient.util.SilentRotationManager.resetAll();
      }
   }

   private void init_V_2() {
      this.aimSpeed.reset();
      this.sessionWatcher.init();
      this.stickySelector.init();
      autismclient.util.QueuedCombatAction.init("anchor-aura");
      this.init_BlockPos = null;
      this.init_J = 0L;
      this.load_J = 0L;
      this.set = 0;
      this.currentTarget = null;
      this.put = -2147483648;
      this.add = -2147483648;
      this.stepCooldowns.init();
      this.leadEstimator.init();
      this.activationLatch.init();
      this.read_J = 0L;
      this.write_J = 0L;
      this.init_Set.clear();
      this.clear = -1;
      this.reset = -1;
      this.start = 0;
      this.stop = -2147483648;
      this.activeAnchor = null;
      this.activeTarget = null;
      this.sequenceSync.init();
      this.init_Z = false;
      this.load_Z = false;
      this.valid = -2147483648;
      this.read_Z = false;
      this.size = -2147483648;
      this.read_BlockPos = null;
      this.write_BlockPos = null;
      this.value = 0;
      this.activeShield = null;
      this.update_Z = false;
      this.plannedSupport = null;
      this.activeSupport = null;
      this.entry = 0;
      this.node = 0;
      this.flush_BlockPos = null;
      this.close_BlockPos = null;
      this.open_BlockPos = null;
      this.buffer = -2147483648;
      this.task = 0;
      this.clearPlan();
      this.bannedCells.clear();
      this.simOptionsByCell.clear();
      this.read_Str = "";
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
      if (!this.read_Str.isEmpty()) {
         return this.read_Str;
      }

      LivingEntity current = this.currentTarget;
      return current == null ? "" : current.getName().getString();
   }

   public LivingEntity getTarget() {
      return this.currentTarget;
   }

   LivingEntity currentTarget() {
      int age = AutismSessionState.getInstance().getClientTick() - this.add;
      return this.isEnabled() && age >= 0 && age <= 2 && this.flush() ? this.currentTarget : null;
   }

   public static double init_D() {
      return al.init_Module_Str("anchor-aura") instanceof AnchorAuraModule aura && aura.set > 0 ? aura.init_J / aura.set / 1000.0 : 0.0;
   }

   public static double load_D() {
      return al.init_Module_Str("anchor-aura") instanceof AnchorAuraModule aura ? aura.load_J / 1000.0 : 0.0;
   }

   public static List<EspHighlightBox> highlightBoxes() {
      if (al.init_Module_Str("anchor-aura") instanceof AnchorAuraModule aura && aura.isEnabled() && aura.bool("preview")) {
         int color = am.init_I_Module(aura, "preview-color", 1728031283);
         List<EspHighlightBox> boxes = new ArrayList<>(3);
         BlockPos anchor = aura.plannedAnchor != null ? aura.plannedAnchor : aura.activeAnchor;
         BlockPos support = aura.plannedSupport != null ? aura.plannedSupport : aura.activeSupport;
         BlockPos shield = aura.plannedShield != null ? aura.plannedShield : aura.activeShield;
         int tick = AutismSessionState.getInstance().getClientTick();
         if (anchor == null && support == null && shield == null && tick - aura.buffer < 0) {
            anchor = aura.flush_BlockPos;
            support = aura.close_BlockPos;
            shield = aura.open_BlockPos;
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
         int age = AutismSessionState.getInstance().getClientTick() - aura.put;
         return age >= 0 && age <= 5;
      } else {
         return false;
      }
   }

   public static boolean read_Z() {
      return al.init_Module_Str("anchor-aura") instanceof AnchorAuraModule aura && aura.isEnabled() ? aura.add == AutismSessionState.getInstance().getClientTick() : false;
   }

   public static boolean init_Z_I(int slot) {
      return al.init_Module_Str("anchor-aura") instanceof AnchorAuraModule aura && aura.isEnabled() ? aura.clear >= 0 && aura.reset == slot : false;
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
      return !SilentMovementCoordinator.isSafeWalkActive() && (autismclient.util.SilentRotationManager.isReturningToPlayer() || enabled && this.flush());
   }

   private boolean sync() {
      if (System.nanoTime() < this.write_J) {
         return false;
      }

      int bind = this.integer("activation-key");
      boolean down = bind != -1 && autismclient.util.ValueSink.init(MC, bind);
      return this.activationLatch.init(this.choice("activation"), down);
   }

   private void init_V_LivingEntity(LivingEntity previous) {
      if (previous != null
         && (!previous.isAlive() || previous.isRemoved() || !(autismclient.util.ExplosionDamageCalculator.effectiveHealth(previous) > 0.0))
         && this.bool("stop-on-kill")) {
         this.write_J = System.nanoTime() + this.integer("stop-delay") * 1000000L;
         this.activationLatch.init("Press");
      }
   }

   private boolean flush() {
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

   private static boolean close() {
      return BedDefenderModule.init_Z_10() || SurroundModule.init_Z_27() || CrystalAuraModule.flush() && CrystalAuraModule.init_Z_51();
   }

   private boolean init_Z_BufferHelper_Cache(AnchorAuraModule.Inner_CachePool plan) {
      return plan.step() == AnchorAuraModule.Inner_ConfigStore.DETONATE
         ? true
         : plan.step() == AnchorAuraModule.Inner_ConfigStore.CHARGE && this.init_Z && plan.cell().equals(this.activeAnchor);
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
            this.init_V_2();
            if ("anchor-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
               autismclient.util.SilentRotationManager.resetAll();
            }
         });
         int tick = AutismSessionState.getInstance().getClientTick();
         this.pruneBannedCells(tick);
         this.write_V_2();
         this.load_V_I(tick);
         if (!this.flush()) {
            this.currentTarget = null;
            this.clearPlan();
            this.load_V_2();
            return;
         }

         this.init_V_LivingEntity(this.currentTarget);
         if (!this.sync()) {
            this.currentTarget = null;
            this.clearPlan();
            this.load_V_2();
            return;
         }

         LivingEntity enemy = this.findTarget();
         if (enemy != null) {
            this.leadEstimator.init(enemy, tick);
         }

         if (enemy == null || enemy != this.currentTarget) {
            this.task = 0;
         }

         this.currentTarget = enemy;
         if (enemy == null) {
            this.clearPlan();
         }

         double range = MC.player.blockInteractionRange();
         this.init_BlockPos = null;
         if ("Aimed".equals(this.choice("aim-mode")) && autismclient.util.WeaponDamageCalculator.init() instanceof BlockHitResult hit) {
            BlockState pointed = MC.level.getBlockState(hit.getBlockPos());
            this.init_BlockPos = !(pointed.getBlock() instanceof RespawnAnchorBlock) && !pointed.canBeReplaced()
               ? hit.getBlockPos().relative(hit.getDirection())
               : hit.getBlockPos();
         }

         autismclient.util.BlockPlacementPlanner.RayBudget budget = new autismclient.util.BlockPlacementPlanner.RayBudget(48);
         this.refreshSimOptions();
         boolean higherRung = close();
         boolean caPrime = CrystalAuraModule.flush();
         boolean caCommitted = caPrime && CrystalAuraModule.sync_Z_11();
         if (caPrime) {
            this.clearPlan();
         }

         AnchorAuraModule.Inner_CachePool plan;
         try (autismclient.util.ExplosionDamageCalculator.SimScope pass = autismclient.util.ExplosionDamageCalculator.beginSimScope()) {
            plan = this.init_BufferHelper_Cache_I(tick, range, budget);
            if (plan == null && this.activeAnchor == null && enemy != null && !caPrime && this.sync()) {
               plan = this.init(enemy, tick, range, budget, higherRung || caCommitted);
            }
         }

         if (plan == null) {
            if (!higherRung
               && !caPrime
               && !CrystalAuraModule.close()
               && (this.activeAnchor != null || this.bool("repeat"))
               && this.aimSpeed.init("anchor-aura", 20, MC.player, this.aimSubject(), this.wireRotation())) {
               this.read_Str = "waiting";
               return;
            }

            this.load_V_2();
            return;
         }

         this.read_Str = plan.step().name().toLowerCase(Locale.ROOT);
         boolean finishing = this.init_Z_BufferHelper_Cache(plan);
         boolean mayAct = !higherRung && !caCommitted || finishing;
         if (mayAct) {
            this.put = tick;
         }

         boolean releasedToDefend = mayAct && !finishing && !this.stepReady(plan.step()) && CrystalAuraModule.close();
         if (mayAct && !releasedToDefend) {
            this.add = tick;
         }

         autismclient.util.InventorySlotArbiter.claimInventoryClick("anchor-aura");
         RotationMath.Rotation wire = this.wireRotation();
         boolean allowed = wire != null && this.stepReady(plan.step()) && mayAct;
         boolean slotReady = allowed && this.init_Z_I_Z(plan.slot(), finishing);
         boolean finishSwitchRefused = finishing && allowed && !slotReady && MC.player.getInventory().getSelectedSlot() != plan.slot();
         boolean useStep = plan.step() == AnchorAuraModule.Inner_ConfigStore.CHARGE
            || plan.step() == AnchorAuraModule.Inner_ConfigStore.DETONATE;
         BlockHitResult alignedUse = wire != null && useStep ? this.init_BlockHitResult_BufferHelper_Cache_ce_DataUtil(plan, wire) : null;
         BlockHitResult hit = slotReady ? (alignedUse != null ? alignedUse : this.init_BlockHitResult_BufferHelper_Cache_ce_DataUtil(plan, wire)) : null;
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

         this.init_V_BufferHelper_Cache(plan, hit, tick);
      } finally {
         long elapsed = System.nanoTime() - profileStarted;
         this.init_J += elapsed;
         this.load_J = Math.max(this.load_J, elapsed);
         this.set++;
      }
   }

   private static boolean rotationsMatch(RotationMath.Rotation first, RotationMath.Rotation second) {
      return Math.abs(RotationMath.angleDelta(first.yaw(), second.yaw())) <= 0.05F && Math.abs(first.pitch() - second.pitch()) <= 0.05F;
   }

   private void load_V_2() {
      this.aimSpeed.reset();
      this.read_Str = "";
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

   private boolean stepReady(AnchorAuraModule.Inner_ConfigStore step) {
      int tick = AutismSessionState.getInstance().getClientTick();
      return this.stepCooldowns.isReady(step.name(), tick, System.nanoTime());
   }

   private void markStep(AnchorAuraModule.Inner_ConfigStore step) {
      int tick = AutismSessionState.getInstance().getClientTick();
      this.stepCooldowns
         .schedule(
            step.name(),
            AutismSessionState.getInstance().getClientTick(),
            System.nanoTime(),
            autismclient.util.ActionCooldownScheduler.jitteredDelayNanos(this.stepDelay(step), this.integer("step-jitter"), this.random)
         );
      this.start = this.integer("switch-back-delay");
   }

   private int stepDelay(AnchorAuraModule.Inner_ConfigStore step) {
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

      if (this.activeAnchor != null && this.flush() && hitResult instanceof BlockHitResult hit) {
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

   private AnchorAuraModule.Inner_CachePool init_BufferHelper_Cache_I(int tick, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget) {
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
            if (!this.init_Z && this.entry < 3) {
               return this.init_BufferHelper_Cache_BlockPos_2(this.activeAnchor, this.activeSupport, range, budget);
            }

            this.init_V_Z(true);
            return null;
         }

         if (autismclient.util.ServerVerifiedBlockState.init(MC.level, this.activeSupport).canBeReplaced()) {
            return null;
         }

         this.activeSupport = null;
      }

      if (!anchor) {
         if (this.load_Z) {
            this.init_V_Z(false);
            return null;
         } else if (this.bool("place") && !this.init_Z && this.data < 3) {
            this.read_Z = false;
            this.size = -2147483648;
            return this.init_BufferHelper_Cache_BlockPos(cell, range, budget);
         } else {
            this.init_V_Z(true);
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
            if (!this.init_Z && this.node < 3 && this.hasShieldMaterial(this.update_Z)) {
               AnchorAuraModule.Inner_CachePool shield = this.load(this.activeShield, range, budget);
               if (charge <= 0 || shield != null && !this.sync(cell)) {
                  return shield;
               }

               this.activeShield = null;
               this.update_Z = false;
            } else {
               if (charge <= 0 || autismclient.util.ServerVerifiedBlockState.init(MC.level, cell).getValue(RespawnAnchorBlock.CHARGE) <= 0) {
                  this.init_V_Z(true);
                  return null;
               }

               this.activeShield = null;
               this.update_Z = false;
            }
         }

         if (charge <= 0) {
            if (!this.poll(cell)) {
               if (this.activeShield != null && MC.level.getBlockState(this.activeShield) == autismclient.util.ServerVerifiedBlockState.init(MC.level, this.activeShield)) {
                  this.init_V_Z(true);
               }

               return null;
            } else if (!this.bool("charge")) {
               return null;
            } else {
               return this.read_Z && tick - this.size < latencyTicks()
                  ? null
                  : this.init_BufferHelper_Cache_BlockPos(cell, AnchorAuraModule.Inner_ConfigStore.CHARGE, range, budget);
            }
         } else if (this.bool("detonate") && this.init_Z_BlockPos_BlockState(cell, state)) {
            return this.load_Z && tick - this.valid < latencyTicks()
               ? null
               : this.init_BufferHelper_Cache_BlockPos(cell, AnchorAuraModule.Inner_ConfigStore.DETONATE, range, budget);
         } else {
            return null;
         }
      }
   }

   private AnchorAuraModule.Inner_CachePool init_BufferHelper_Cache_LivingEntity(
      LivingEntity enemy, int tick, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget
   ) {
      BlockPos adopted = this.init_BlockPos_LivingEntity_D(enemy, range);
      if (adopted != null) {
         BlockState state = MC.level.getBlockState(adopted);
         AnchorAuraModule.Inner_ConfigStore step = state.getValue(RespawnAnchorBlock.CHARGE) > 0
            ? AnchorAuraModule.Inner_ConfigStore.DETONATE
            : AnchorAuraModule.Inner_ConfigStore.CHARGE;
         boolean usable = step == AnchorAuraModule.Inner_ConfigStore.DETONATE
            ? this.bool("detonate") && this.init_Z_BlockPos_BlockState(adopted, state)
            : this.open();
         boolean refused = true;
         if (usable) {
            autismclient.util.BlockPlacementPlanner.RejectReason[] outcome = new autismclient.util.BlockPlacementPlanner.RejectReason[1];
            AnchorAuraModule.Inner_CachePool plan = this.init(adopted, step, range, budget, outcome);
            if (plan != null) {
               this.adoptAnchor(adopted, tick, null, false);
               return plan;
            }

            refused = outcome[0] != autismclient.util.BlockPlacementPlanner.RejectReason.NO_BUDGET || this.load_Z_BlockPos(adopted);
         }

         if (refused) {
            this.bannedCells.put(adopted, tick + 40);
         }
      }

      if (!this.bool("place")) {
         return null;
      }

      if (!this.open()) {
         return null;
      }

      List<BlockPos> cells = this.candidateCells(enemy, range);
      if (cells.isEmpty()) {
         if (this.bool("glowstone-shield")) {
            AnchorAuraModule.Inner_CachePool shielded = this.load(enemy, range, budget);
            if (shielded != null) {
               return shielded;
            }
         }

         if (this.task < 3) {
            this.task++;
         }

         if (this.bool("force-engage") && this.task >= 3) {
            cells = this.load(enemy, range);
         }
      } else {
         this.task = 0;
      }

      if (cells.isEmpty() && this.bool("support-blocks")) {
         AnchorAuraModule.Inner_CachePool supported = this.init_BufferHelper_Cache_LivingEntity(enemy, range, budget);
         if (supported != null) {
            return supported;
         }
      }

      if (cells.isEmpty()) {
         return null;
      }

      autismclient.util.BlockPlacementPlanner.RejectReason[] outcome = new autismclient.util.BlockPlacementPlanner.RejectReason[1];
      int start = this.init_I_List(cells);

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
         AnchorAuraModule.Inner_CachePool plan = this.init(cell, cover, cover != null && this.init_I_BlockPos_BlockPos(cover, cell) == 1, range, budget, outcome);
         if (plan != null) {
            this.read_BlockPos = cell;
            return plan;
         }

         if (outcome[0] == autismclient.util.BlockPlacementPlanner.RejectReason.NO_BUDGET) {
            this.read_BlockPos = cell;
            return null;
         }

         this.read_BlockPos = cells.get(index + 1 == cells.size() ? 0 : index + 1);
      }

      return this.bool("support-blocks") ? this.init_BufferHelper_Cache_LivingEntity(enemy, range, budget) : null;
   }

   private int init_I_List(List<BlockPos> cells) {
      if (this.read_BlockPos == null) {
         return 0;
      }

      int index = cells.indexOf(this.read_BlockPos);
      return index < 0 ? 0 : index;
   }

   private boolean load_Z_BlockPos(BlockPos cell) {
      if (cell.equals(this.write_BlockPos)) {
         this.value++;
      } else {
         this.write_BlockPos = cell.immutable();
         this.value = 1;
      }

      if (this.value < 3) {
         return false;
      }

      this.write_BlockPos = null;
      this.value = 0;
      return true;
   }

   private AnchorAuraModule.Inner_CachePool init(LivingEntity enemy, int tick, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget, boolean hotbarOwnedAbove) {
      if (this.plannedAnchor != null) {
         boolean valid = this.bool("place")
            && enemy == this.read_LivingEntity
            && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(this.cache, "anchor-aura")
            && (
               this.plannedSupport != null
                  ? isObsidian(MC.player.getInventory().getItem(this.cache))
                     && this.init_Z_BlockPos_AABB(this.plannedAnchor, this.motionEnvelope())
                     && this.init_Z_BlockState(Blocks.OBSIDIAN.defaultBlockState(), this.plannedSupport, this.motionEnvelope())
                  : isRespawnAnchor(MC.player.getInventory().getItem(this.cache))
                     && this.init_Z_BlockState(Blocks.RESPAWN_ANCHOR.defaultBlockState(), this.plannedAnchor, this.motionEnvelope())
            )
            && (
               this.plannedShield == null
                  || this.hasShieldMaterial(this.plannedShieldObsidian) && this.init_Z_BlockState(shieldState(this.plannedShieldObsidian), this.plannedShield, this.motionEnvelope())
            );
         valid = valid && this.init_Z_LivingEntity(enemy, this.plannedAnchor, this.plannedSupport, this.plannedShield);
         if (valid) {
            if (hotbarOwnedAbove) {
               this.state = tick + this.switchDelay();
            }

            if (tick - this.state < 0) {
               AnchorAuraModule.Inner_CachePool held = this.plannedSupport != null
                  ? this.init_BufferHelper_Cache_BlockPos_2(this.plannedAnchor, this.plannedSupport, range, budget)
                  : this.init(this.plannedAnchor, this.plannedShield, this.plannedShieldObsidian, range, budget, null);
               if (held != null) {
                  this.queue = 0;
                  return held;
               }

               if (++this.queue < 3) {
                  return null;
               }
            }

            this.bannedCells.put(this.plannedAnchor, tick + 10);
         }

         this.clearPlan();
      }

      AnchorAuraModule.Inner_CachePool plan = this.init_BufferHelper_Cache_LivingEntity(enemy, tick, range, budget);
      if (plan != null
         && (plan.step() == AnchorAuraModule.Inner_ConfigStore.PLACE || plan.step() == AnchorAuraModule.Inner_ConfigStore.SUPPORT)) {
         this.plannedAnchor = plan.step() == AnchorAuraModule.Inner_ConfigStore.SUPPORT ? plan.read() : plan.cell();
         this.plannedSupport = plan.step() == AnchorAuraModule.Inner_ConfigStore.SUPPORT ? plan.cell() : null;
         this.plannedShield = plan.load();
         this.plannedShieldObsidian = plan.shieldObsidian();
         this.cache = plan.slot();
         this.read_LivingEntity = enemy;
         this.state = tick + this.switchDelay();
      }

      return plan;
   }

   private boolean init_Z_LivingEntity(LivingEntity enemy, BlockPos cell, BlockPos support, BlockPos shield) {
      if (enemy != null && enemy.isAlive() && this.anchorExplodes(cell)) {
         autismclient.util.ExplosionDamageCalculator.SimOptions verify = this.simOptionsFor(cell);
         if (shield != null || support != null) {
            verify = verify.withPlacedObsidian(shield != null ? shield : support);
         }

         autismclient.util.ExplosionDamageCalculator.DamagePair rank = this.init_S_BufferHelper_LivingEntity(
            enemy, center(cell), verify, this.leadBox(enemy)
         );
         return this.init_Z_S_BufferHelper(rank)
            ? true
            : shield == null
               && this.bool("force-engage")
               && this.task >= 3
               && this.init_Z_S_BufferHelper(
                  autismclient.util.TargetLeadDamageEstimator.init(enemy, center(cell), 5.0F, verify, autismclient.util.TargetLeadDamageEstimator.init(enemy))
               )
               && (!this.bool("protect-friends") || autismclient.util.WeaponDamageCalculator.init(center(cell), 5.0F, verify, this.decimal("max-friend-damage")));
      } else {
         return false;
      }
   }

   private void clearPlan() {
      this.plannedAnchor = null;
      this.cache = -1;
      this.read_LivingEntity = null;
      this.state = 0;
      this.queue = 0;
      this.plannedShield = null;
      this.plannedShieldObsidian = false;
      this.plannedSupport = null;
   }

   private boolean open() {
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
         this.stepDelay(AnchorAuraModule.Inner_ConfigStore.PLACE), this.integer("switch-delay"), autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs()
      );
   }

   private int init_I_Z(boolean shielded) {
      return autismclient.util.ActionCooldownScheduler.init(
         this.stepDelay(AnchorAuraModule.Inner_ConfigStore.PLACE),
         this.stepDelay(AnchorAuraModule.Inner_ConfigStore.CHARGE),
         this.stepDelay(AnchorAuraModule.Inner_ConfigStore.DETONATE),
         this.integer("switch-delay"),
         autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs(),
         shielded
      );
   }

   private void adoptAnchor(BlockPos anchorCell, BlockPos supportCell, int tick) {
      this.adoptAnchor(anchorCell, tick, null, false);
      this.activeSupport = supportCell.immutable();
      this.entry = 0;
      this.next = tick + this.init_I_Z(true);
   }

   private void adoptAnchor(BlockPos cell, int tick, BlockPos shield, boolean shieldObsidian) {
      this.clearPlan();
      this.activeAnchor = cell.immutable();
      this.activeTarget = this.currentTarget;
      this.sequenceSync.init();
      this.activeShield = shield;
      this.update_Z = shieldObsidian;
      this.node = 0;
      this.next = tick + this.init_I_Z(shield != null);
      this.init_Z = false;
      this.load_Z = false;
      this.valid = -2147483648;
      this.read_Z = false;
      this.size = -2147483648;
      this.data = 0;
      this.task = 0;
      this.read_BlockPos = null;
   }

   private void load_V_I(int tick) {
      if (this.activeAnchor != null) {
         if (!this.init_Z) {
            if (tick - this.next > 0) {
               this.init_Z = true;
               this.prev = tick + this.init_I_Z(this.activeShield != null);
            }
         } else if (tick - this.prev > 0) {
            this.init_V_Z(true);
         }
      }
   }

   private void init_V_Z(boolean ban) {
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
      this.update_Z = false;
      this.plannedSupport = null;
      this.activeSupport = null;
      this.entry = 0;
      this.node = 0;
      this.init_Z = false;
      this.load_Z = false;
      this.valid = -2147483648;
      this.read_Z = false;
      this.size = -2147483648;
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

   private AnchorAuraModule.Inner_CachePool init_BufferHelper_Cache_BlockPos(BlockPos cell, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget) {
      return this.init(cell, null, false, range, budget, null);
   }

   private AnchorAuraModule.Inner_CachePool init(
      BlockPos cell,
      BlockPos shield,
      boolean shieldObsidian,
      double range,
      autismclient.util.BlockPlacementPlanner.RayBudget budget,
      autismclient.util.BlockPlacementPlanner.RejectReason[] outcome
   ) {
      if (!this.init_Z_BlockState(Blocks.RESPAWN_ANCHOR.defaultBlockState(), cell, this.motionEnvelope())) {
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
         : new AnchorAuraModule.Inner_CachePool(cell, AnchorAuraModule.Inner_ConfigStore.PLACE, slot, candidate, range, shield, shieldObsidian, null);
   }

   private AnchorAuraModule.Inner_CachePool init_BufferHelper_Cache_BlockPos_2(
      BlockPos anchorCell, BlockPos supportCell, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget
   ) {
      if (this.init_Z_BlockPos_AABB(anchorCell, this.motionEnvelope()) && this.init_Z_BlockState(Blocks.OBSIDIAN.defaultBlockState(), supportCell, this.motionEnvelope())
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
            : new AnchorAuraModule.Inner_CachePool(supportCell, AnchorAuraModule.Inner_ConfigStore.SUPPORT, slot, candidate, range, null, false, anchorCell);
      } else {
         return null;
      }
   }

   private AnchorAuraModule.Inner_CachePool init_BufferHelper_Cache_BlockPos(
      BlockPos cell, AnchorAuraModule.Inner_ConfigStore step, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget
   ) {
      return this.init(cell, step, range, budget, null);
   }

   private AnchorAuraModule.Inner_CachePool init(
      BlockPos cell,
      AnchorAuraModule.Inner_ConfigStore step,
      double range,
      autismclient.util.BlockPlacementPlanner.RayBudget budget,
      autismclient.util.BlockPlacementPlanner.RejectReason[] outcome
   ) {
      int slot = step == AnchorAuraModule.Inner_ConfigStore.CHARGE ? this.findHotbarSlot(AnchorAuraModule::isGlowstone) : this.detonateSlot();
      if (slot < 0) {
         return null;
      }

      autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = this.init_U_CachePool_BlockPos(cell, range, budget, outcome);
      return candidate == null ? null : new AnchorAuraModule.Inner_CachePool(cell, step, slot, candidate, range, null, false, null);
   }

   private AnchorAuraModule.Inner_CachePool load(BlockPos cell, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget) {
      boolean obsidian = this.update_Z;
      if (!this.init_Z_BlockState(shieldState(obsidian), cell, this.motionEnvelope())) {
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
         : new AnchorAuraModule.Inner_CachePool(cell, AnchorAuraModule.Inner_ConfigStore.SHIELD, slot, candidate, range, null, obsidian, null);
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

   private autismclient.util.BlockPlacementPlanner.Query init_U_StateCache_BlockPos_D(BlockPos anchor, double range) {
      LocalPlayer player = MC.player;
      autismclient.util.BlockPlacementPlanner.Query request = new autismclient.util.BlockPlacementPlanner.Query(
            anchor, player.getEyePosition(), range, new AnchorAuraModule.Inner_DataUtil(anchor)
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

   private autismclient.util.BlockPlacementPlanner.PlacementPlan init_U_CachePool_BlockPos(
      BlockPos anchor, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget, autismclient.util.BlockPlacementPlanner.RejectReason[] outcome
   ) {
      BlockState state = MC.level.getBlockState(anchor);
      if (!(state.getBlock() instanceof RespawnAnchorBlock)) {
         return null;
      }

      if (!(autismclient.util.ServerVerifiedBlockState.init(MC.level, anchor).getBlock() instanceof RespawnAnchorBlock)) {
         return null;
      }

      autismclient.util.BlockPlacementPlanner.Query request = this.init_U_StateCache_BlockPos_D(anchor, range).withRayBudget(budget);
      Vec3 eye = MC.player.getEyePosition();
      autismclient.util.BlockPlacementPlanner.RejectReason[] refusal = outcome != null ? outcome : new autismclient.util.BlockPlacementPlanner.RejectReason[1];
      List<AnchorAuraModule.Inner_NetHelper> aims = new ArrayList<>();

      for (Direction face : autismclient.util.BlockPlacementPlanner.DEFAULT_FACE_ORDER) {
         for (AABB rect : autismclient.util.BlockPlacementPlanner.extractFaceRects(state, anchor, face, 3)) {
            autismclient.util.BlockPlacementPlanner.SupportCandidate option = new autismclient.util.BlockPlacementPlanner.SupportCandidate(
               anchor,
               anchor,
               face,
               1,
               state,
               rect,
               init_D_AABB_Direction(rect, face),
               autismclient.util.BlockPlacementPlanner.edgeMarginRatio(rect, face, eye),
               false,
               new autismclient.util.BlockPlacementPlanner.SupportFace(anchor, face)
            );
            autismclient.util.BlockPlacementPlanner.AimSample aim = autismclient.util.BlockPlacementPlanner.computeAimSample(option, request, refusal);
            if (aim != null) {
               aims.add(new AnchorAuraModule.Inner_NetHelper(option, aim));
            }
         }
      }

      aims.sort(Comparator.comparingDouble(candidatex -> candidatex.aim().turnDegrees()));
      int start = this.stickySelector.init(aims, AnchorAuraModule.Inner_NetHelper::support);

      for (int step = 0; step < aims.size(); step++) {
         AnchorAuraModule.Inner_NetHelper aim = aims.get((start + step) % aims.size());
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

   private static double init_D_AABB_Direction(AABB rect, Direction face) {
      Axis normal = face.getAxis();
      Axis first = autismclient.util.BlockPlacementPlanner.tangentAxis(normal, true);
      Axis second = autismclient.util.BlockPlacementPlanner.tangentAxis(normal, false);
      return (rect.max(first) - rect.min(first)) * (rect.max(second) - rect.min(second));
   }

   private BlockHitResult init_BlockHitResult_BufferHelper_Cache_ce_DataUtil(AnchorAuraModule.Inner_CachePool plan, RotationMath.Rotation wire) {
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
            var10000 = this.init_U_StateCache_BlockPos_D(plan.cell(), plan.range());
            break;
         default:
            throw new MatchException(null, null);
      }

      autismclient.util.BlockPlacementPlanner.Query gate = var10000;
      return autismclient.util.BlockPlacementPlanner.verifyWithRotation(plan.placement(), wire, player.getEyePosition(), plan.range(), gate);
   }

   private void init_V_BufferHelper_Cache(AnchorAuraModule.Inner_CachePool plan, BlockHitResult hit, int tick) {
      if (this.load_Z_BufferHelper_Cache(plan)
         && !al.init_Z_HitResult(hit, InteractionHand.MAIN_HAND, "anchor-aura")
         && autismclient.util.QueuedCombatAction.init(
            "anchor-aura",
            hit,
            InteractionHand.MAIN_HAND,
            () -> this.flush()
               && this.load_Z_BufferHelper_Cache(plan)
               && this.wireRotation() != null
               && this.init_BlockHitResult_BufferHelper_Cache_ce_DataUtil(plan, this.wireRotation()) != null,
            () -> this.init_V_BufferHelper_Cache_I(plan, AutismSessionState.getInstance().getClientTick())
         )
         && !PerTickPlacementLock.init("anchor-aura")) {
         autismclient.util.ClientInteractionOverride.clearAll();
      }
   }

   private void init_V_BufferHelper_Cache_I(AnchorAuraModule.Inner_CachePool plan, int tick) {
      if (this.plannedAnchor != null
         || this.activeAnchor != null
         || this.plannedSupport != null
         || this.activeSupport != null
         || this.plannedShield != null
         || this.activeShield != null) {
         this.flush_BlockPos = this.plannedAnchor != null ? this.plannedAnchor : this.activeAnchor;
         this.close_BlockPos = this.plannedSupport != null ? this.plannedSupport : this.activeSupport;
         this.open_BlockPos = this.plannedShield != null ? this.plannedShield : this.activeShield;
         this.buffer = tick + 10;
      }

      this.markStep(plan.step());
      if (plan.step() == AnchorAuraModule.Inner_ConfigStore.SUPPORT) {
         this.adoptAnchor(plan.read(), plan.cell(), tick);
         this.entry++;
      }

      if (plan.step() == AnchorAuraModule.Inner_ConfigStore.PLACE) {
         if (this.activeAnchor == null || !this.activeAnchor.equals(plan.cell())) {
            this.adoptAnchor(plan.cell(), tick, plan.load(), plan.shieldObsidian());
         }

         this.data++;
         this.sequenceSync.init(MC.level, ((AutismClientLevelPredictionAccessor)MC.level).autism$getBlockPredictionHandler().currentSequence());
         this.init_Set.add(plan.cell().immutable());

         while (this.init_Set.size() > 64) {
            this.init_Set.remove(this.init_Set.iterator().next());
         }
      }

      if (plan.step() == AnchorAuraModule.Inner_ConfigStore.SHIELD) {
         this.node++;
      }

      if (plan.step() == AnchorAuraModule.Inner_ConfigStore.CHARGE) {
         this.read_Z = true;
         this.size = tick;
      }

      if (plan.step() == AnchorAuraModule.Inner_ConfigStore.DETONATE) {
         this.load_Z = true;
         this.valid = tick;
      }

      this.aimSpeed.load(this.aimSubject(), tick, autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs());
   }

   private Object aimSubject() {
      return this.activeAnchor != null ? this.activeAnchor : this.currentTarget;
   }

   private boolean load_Z_BufferHelper_Cache(AnchorAuraModule.Inner_CachePool plan) {
      if (!AutismLiteVariant.enabled()
         && (
            plan.step() == AnchorAuraModule.Inner_ConfigStore.SUPPORT
               || plan.step() == AnchorAuraModule.Inner_ConfigStore.PLACE
               || plan.step() == AnchorAuraModule.Inner_ConfigStore.SHIELD
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
               && this.init_Z_BlockState(Blocks.OBSIDIAN.defaultBlockState(), plan.cell(), this.motionEnvelope())
               && this.init_Z_BlockPos_AABB(plan.read(), this.motionEnvelope())
               && this.init_Z_LivingEntity(this.currentTarget, plan.read(), plan.cell(), null);
            break;
         case PLACE:
            var10000 = !anchor
               && isRespawnAnchor(MC.player.getMainHandItem())
               && this.init_Z_BlockState(Blocks.RESPAWN_ANCHOR.defaultBlockState(), plan.cell(), this.motionEnvelope())
               && this.init_Z_LivingEntity(this.currentTarget, plan.cell(), null, plan.load());
            break;
         case SHIELD:
            var10000 = state.canBeReplaced()
               && this.init_Z_BlockState(shieldState(plan.shieldObsidian()), plan.cell(), this.motionEnvelope())
               && (plan.shieldObsidian() ? isObsidian(MC.player.getMainHandItem()) : isGlowstone(MC.player.getMainHandItem()));
            break;
         case CHARGE:
            var10000 = !this.sequenceSync.init(MC.level)
               && anchor
               && state.getValue(RespawnAnchorBlock.CHARGE) < 4
               && isGlowstone(MC.player.getMainHandItem())
               && this.poll(plan.cell());
            break;
         case DETONATE:
            var10000 = !this.sequenceSync.init(MC.level)
               && anchor
               && !isGlowstone(MC.player.getMainHandItem())
               && this.init_Z_BlockPos_BlockState(plan.cell(), state);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private boolean init_Z_BlockPos_BlockState(BlockPos cell, BlockState state) {
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
            return !this.anchorExplodes(cell) ? false : this.sync(cell) && this.close(cell);
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
      List<AnchorAuraModule.Inner_StreamUtil> candidates = new ArrayList<>();
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         if (!this.bannedCells.containsKey(cursor) && this.flush(cursor) && this.anchorExplodes(cursor) && this.init_Z_BlockState(anchor, cursor, nextBox)) {
            Vec3 centre = center(cursor);
            if (!this.bool("only-above") || !(centre.y < enemy.getY())) {
               double distanceSq = eye.distanceToSqr(centre);
               if (!(distanceSq > rangeSq)) {
                  double bound = autismclient.util.ExplosionDamageCalculator.calculateUpperBoundDamage(enemy, centre, 5.0F, this.simOptionsFor(cursor).withTargetBox(lead));
                  if (!(bound < damageFloor)) {
                     candidates.add(new AnchorAuraModule.Inner_StreamUtil(cursor.immutable(), centre, bound, distanceSq));
                  }
               }
            }
         }
      }

      if (candidates.isEmpty()) {
         return List.of();
      }

      candidates.sort(Comparator.comparingDouble(AnchorAuraModule.Inner_StreamUtil::damageBound).reversed());
      List<AnchorAuraModule.Inner_BufferHelper> accepted = new ArrayList<>();
      double bestValue = -1.0;
      int evaluated = 0;

      for (AnchorAuraModule.Inner_StreamUtil candidate : candidates) {
         if (accepted.size() >= 4 && candidate.damageBound() <= bestValue || evaluated >= 32) {
            break;
         }

         evaluated++;
         autismclient.util.ExplosionDamageCalculator.DamagePair rank = this.init_S_BufferHelper_LivingEntity(
            enemy, candidate.center(), this.simOptionsFor(candidate.cell()), lead
         );
         if (this.init_Z_S_BufferHelper(rank)) {
            accepted.add(new AnchorAuraModule.Inner_BufferHelper(candidate.cell(), rank, candidate.load()));
            bestValue = Math.max(bestValue, init_D_S_BufferHelper_Z(rank, efficient));
         }
      }

      if (accepted.isEmpty()) {
         return List.of();
      }

      accepted.sort(
         (first, second) -> {
            int byValue = Double.compare(
               init_D_S_BufferHelper_Z(second.rank(), efficient), init_D_S_BufferHelper_Z(first.rank(), efficient)
            );
            if (byValue != 0) {
               return byValue;
            }

            int byPreference = compareRanks(first.rank(), second.rank());
            return byPreference != 0 ? byPreference : Double.compare(second.init_D(), first.init_D());
         }
      );
      List<BlockPos> result = new ArrayList<>(Math.min(4, accepted.size()));

      for (AnchorAuraModule.Inner_BufferHelper scored : accepted) {
         if (result.size() >= 4) {
            break;
         }

         result.add(scored.cell());
      }

      return result;
   }

   private static double init_D_S_BufferHelper_Z(autismclient.util.ExplosionDamageCalculator.DamagePair rank, boolean efficient) {
      return efficient ? rank.load() - 2.5 * rank.selfDamage() : rank.load();
   }

   private AnchorAuraModule.Inner_CachePool init_BufferHelper_Cache_LivingEntity(LivingEntity enemy, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget) {
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
         List<AnchorAuraModule.Inner_TaskQueue> accepted = new ArrayList<>();
         MutableBlockPos cursor = new MutableBlockPos();
         int evaluated = 0;

         for (int i = 0; i + 2 < offsets.length; i += 3) {
            cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
            BlockPos anchorCell = cursor.above();
            if (this.flush(anchorCell)
               && !this.bannedCells.containsKey(cursor)
               && !this.bannedCells.containsKey(anchorCell)
               && this.anchorExplodes(anchorCell)
               && this.init_Z_BlockPos_AABB(anchorCell, nextBox)
               && this.init_Z_BlockState(obsidian, cursor, nextBox)) {
               Vec3 centre = center(anchorCell);
               if (!(this.bool("only-above") && centre.y < enemy.getY())) {
                  double distanceSq = eye.distanceToSqr(centre);
                  if (!(distanceSq > rangeSq)) {
                     autismclient.util.ExplosionDamageCalculator.SimOptions supported = this.simOptionsFor(anchorCell).withPlacedObsidian(cursor.immutable());
                     if (!(autismclient.util.ExplosionDamageCalculator.calculateUpperBoundDamage(enemy, centre, 5.0F, supported.withTargetBox(lead)) < floor)) {
                        if (++evaluated > 32) {
                           break;
                        }

                        autismclient.util.ExplosionDamageCalculator.DamagePair rank = this.init_S_BufferHelper_LivingEntity(enemy, centre, supported, lead);
                        if (this.init_Z_S_BufferHelper(rank)) {
                           accepted.add(new AnchorAuraModule.Inner_TaskQueue(anchorCell.immutable(), cursor.immutable(), rank, distanceSq));
                        }
                     }
                  }
               }
            }
         }

         accepted.sort((first, second) -> {
            int preference = compareRanks(first.rank(), second.rank());
            return preference != 0 ? preference : Double.compare(second.init_D(), first.init_D());
         });
         int limit = Math.min(4, accepted.size());

         for (int i = 0; i < limit; i++) {
            AnchorAuraModule.Inner_TaskQueue pair = accepted.get(i);
            AnchorAuraModule.Inner_CachePool plan = this.init_BufferHelper_Cache_BlockPos_2(pair.cell(), pair.load(), range, budget);
            if (plan != null) {
               return plan;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private List<BlockPos> load(LivingEntity enemy, double range) {
      LocalPlayer player = MC.player;
      Vec3 eye = player.getEyePosition();
      BlockPos origin = player.blockPosition();
      int[] offsets = offsetsForRadius(Mth.ceil(range));
      BlockState anchor = Blocks.RESPAWN_ANCHOR.defaultBlockState();
      double rangeSq = range * range;
      AABB nextBox = this.motionEnvelope();
      AABB enemyRing = enemy.getBoundingBox().inflate(1.0);
      int feetY = Mth.floor(enemy.getY());
      List<AnchorAuraModule.Inner_BufferHelper> accepted = new ArrayList<>();
      int evaluated = 0;
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         if (!this.bannedCells.containsKey(cursor) && this.flush(cursor) && this.anchorExplodes(cursor) && this.init_Z_BlockState(anchor, cursor, nextBox)) {
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
               if (this.init_Z_S_BufferHelper(rank) && this.close(cursor)) {
                  accepted.add(new AnchorAuraModule.Inner_BufferHelper(cursor.immutable(), rank, distanceSq));
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
         return byPreference != 0 ? byPreference : Double.compare(second.init_D(), first.init_D());
      });
      List<BlockPos> result = new ArrayList<>(Math.min(4, accepted.size()));

      for (AnchorAuraModule.Inner_BufferHelper scored : accepted) {
         if (result.size() >= 4) {
            break;
         }

         result.add(scored.cell());
      }

      return result;
   }

   private AnchorAuraModule.Inner_CachePool load(LivingEntity enemy, double range, autismclient.util.BlockPlacementPlanner.RayBudget budget) {
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
      List<AnchorAuraModule.Inner_BufferHelper> needsShield = new ArrayList<>();
      int evaluated = 0;
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         if (!this.bannedCells.containsKey(cursor) && this.flush(cursor) && this.anchorExplodes(cursor) && this.init_Z_BlockState(anchor, cursor, nextBox)) {
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
                     needsShield.add(new AnchorAuraModule.Inner_BufferHelper(cursor.immutable(), rank, distanceSq));
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
         return byPreference != 0 ? byPreference : Double.compare(second.init_D(), first.init_D());
      });
      int tried = 0;

      for (AnchorAuraModule.Inner_BufferHelper scored : needsShield) {
         if (tried >= 4) {
            break;
         }

         tried++;
         BlockPos shield = this.findShieldCell(enemy, scored.cell(), damageFloor, selfCeiling);
         if (shield != null) {
            int material = this.init_I_BlockPos_BlockPos(shield, scored.cell());
            if (material >= 0) {
               boolean obsidian = material == 1;
               return this.init(scored.cell(), shield, obsidian, range, budget, null);
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
         if (obsidianAvailable && this.init_Z_BlockPos_BlockPos(shieldCell, anchorCell) || glowstoneAvailable && this.update(shieldCell)) {
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
         if (!cell.equals(anchorCell) && this.init_Z_BlockState(Blocks.GLOWSTONE.defaultBlockState(), cell, nextBox)) {
            cells.add(cell);
         }
      }

      cells.sort(
         (first, second) -> {
            boolean firstGround = this.write(first);
            boolean secondGround = this.write(second);
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

   private boolean write(BlockPos cell) {
      BlockPos below = cell.below();
      return autismclient.util.BlockPlacementPlanner.isValidSupportBlock(MC.level.getBlockState(below), below, false);
   }

   private boolean update(BlockPos cell) {
      for (Direction face : Direction.values()) {
         BlockPos support = cell.relative(face);
         if (autismclient.util.BlockPlacementPlanner.isValidSupportBlock(MC.level.getBlockState(support), support, false)) {
            return true;
         }
      }

      return false;
   }

   private boolean init_Z_BlockPos_BlockPos(BlockPos cell, BlockPos anchorCell) {
      return this.update(cell) || cell.distManhattan(anchorCell) == 1;
   }

   private int init_I_BlockPos_BlockPos(BlockPos cell, BlockPos anchor) {
      boolean durable = this.hasShieldMaterial(true) && this.init_Z_BlockPos_BlockPos(cell, anchor);
      boolean fuel = this.hasShieldMaterial(false) && this.update(cell);
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

   private boolean init_Z_BlockPos_AABB(BlockPos cell, AABB nextBox) {
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

   private boolean init_Z_BlockState(BlockState anchor, BlockPos cell, AABB nextBox) {
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

   private BlockPos init_BlockPos_LivingEntity_D(LivingEntity enemy, double range) {
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
         if (!this.bannedCells.containsKey(cursor) && this.flush(cursor) && (!this.bool("own-only") || this.init_Set.contains(cursor))) {
            BlockState state = MC.level.getBlockState(cursor);
            if (state.getBlock() instanceof RespawnAnchorBlock && this.anchorExplodes(cursor)) {
               Vec3 centre = center(cursor);
               double distanceSq = eye.distanceToSqr(centre);
               if (!(distanceSq > rangeSq)) {
                  autismclient.util.ExplosionDamageCalculator.DamagePair rank = this.init_S_BufferHelper_LivingEntity(
                     enemy, centre, this.simOptionsFor(cursor), lead
                  );
                  if (this.init_Z_S_BufferHelper(rank)) {
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
         ? this.read_I() >= 2
         : this.findHotbarSlot(AnchorAuraModule::isGlowstone) >= 0 && this.findHotbarSlot(AnchorAuraModule::isObsidian) >= 0;
   }

   private int read_I() {
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

   private boolean init_Z_I_Z(int slot, boolean finishingWork) {
      if (slot < 0) {
         return true;
      }

      LocalPlayer player = MC.player;
      if (!autismclient.util.InventorySlotArbiter.claimSlot("anchor-aura", slot)) {
         return false;
      }

      int held = player.getInventory().getSelectedSlot();
      if (held == slot) {
         if (System.nanoTime() < this.read_J) {
            return false;
         }

         this.start = this.integer("switch-back-delay");
         return true;
      } else {
         if (this.load(slot, finishingWork)) {
            if (this.clear < 0) {
               this.clear = held;
            }

            this.reset = slot;
         }

         this.start = this.integer("switch-back-delay");
         return player.getInventory().getSelectedSlot() == slot && System.nanoTime() >= this.read_J;
      }
   }

   private boolean load(int slot, boolean finishingWork) {
      if (!BedDefenderModule.init_Z_10() && !SurroundModule.init_Z_27()) {
         if (finishingWork || !CrystalAuraModule.flush() || !CrystalAuraModule.init_Z_51() && !CrystalAuraModule.sync_Z_11()) {
            int tick = AutismSessionState.getInstance().getClientTick();
            if (tick == this.stop) {
               return false;
            }

            if (!autismclient.util.InventorySlotArbiter.beginActionGroup("anchor-aura")) {
               return false;
            }

            try {
               autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, slot);
               this.read_J = System.nanoTime() + this.integer("switch-delay") * 1000000L;
            } finally {
               autismclient.util.InventorySlotArbiter.endActionGroup("anchor-aura");
            }

            this.stop = tick;
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

   private void write_V_2() {
      if (this.clear >= 0 && MC.gui.screen() == null && MC.gui.overlay() == null) {
         if (this.reset >= 0 && MC.player.getInventory().getSelectedSlot() != this.reset) {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (!CrystalAuraModule.init_Z_I_10(selected) && !KillAuraModule.load_Z_I_3(selected)) {
               this.clear = -1;
               this.reset = -1;
               this.start = 0;
            }
         } else if (this.start > 0) {
            this.start--;
         } else if (!autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(this.clear, "anchor-aura") && this.load(this.clear, false)) {
            this.clear = -1;
            this.reset = -1;
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

   private autismclient.util.ExplosionDamageCalculator.DamagePair init_S_BufferHelper_LivingEntity(
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

   private boolean init_Z_S_BufferHelper(autismclient.util.ExplosionDamageCalculator.DamagePair rank) {
      return autismclient.util.ExplosionDamageCalculator.wouldSelfDamagePopOrKill(rank.selfDamage())
         ? false
         : autismclient.util.WeaponDamageCalculator.init(rank.load(), rank.selfDamage(), this.decimal("min-target-damage"), this.decimal("max-self-damage"), this.bool("efficient"));
   }

   private boolean sync(BlockPos cell) {
      return !autismclient.util.ExplosionDamageCalculator.wouldSelfDamagePopOrKill(this.selfDamage(cell));
   }

   private boolean flush(BlockPos cell) {
      return !"Aimed".equals(this.choice("aim-mode")) || cell.equals(this.init_BlockPos);
   }

   private boolean close(BlockPos cell) {
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
      } else if (this.open(cell)) {
         return options.withPlacedObsidian(this.activeShield);
      } else {
         return MC.level.getBlockState(this.activeShield) != autismclient.util.ServerVerifiedBlockState.init(MC.level, this.activeShield)
            ? options.withExcludedBlocks(List.of(cell, this.activeShield))
            : options;
      }
   }

   private boolean open(BlockPos cell) {
      if (this.activeShield != null && cell.equals(this.activeAnchor)) {
         net.minecraft.world.level.block.Block material = shieldState(this.update_Z).getBlock();
         return MC.level.getBlockState(this.activeShield).is(material) && autismclient.util.ServerVerifiedBlockState.init(MC.level, this.activeShield).is(material);
      } else {
         return false;
      }
   }

   private boolean poll(BlockPos cell) {
      if (this.activeShield == null || !cell.equals(this.activeAnchor)) {
         return true;
      }

      if (!this.open(cell)) {
         return false;
      }

      double self = this.selfDamage(cell);
      return self <= this.decimal("max-self-damage") && !autismclient.util.ExplosionDamageCalculator.wouldSelfDamagePopOrKill(self) && this.close(cell);
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
      if (clamped == get) {
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
      get = clamped;
      return flat;
   }

   private record Inner_BufferHelper(BlockPos cell, autismclient.util.ExplosionDamageCalculator.DamagePair rank, double init_D) {
   }

   private record Inner_CachePool(
      BlockPos cell,
      AnchorAuraModule.Inner_ConfigStore step,
      int slot,
      autismclient.util.BlockPlacementPlanner.PlacementPlan placement,
      double range,
      BlockPos load,
      boolean shieldObsidian,
      BlockPos read
   ) {
   }

   private enum Inner_ConfigStore {
      SUPPORT,
      PLACE,
      SHIELD,
      CHARGE,
      DETONATE;
   }

   private record Inner_DataUtil(BlockPos init) implements autismclient.util.BlockPlacementPlanner.HitValidator {
      @Override
      public boolean lands(BlockHitResult hit, BlockPos cell) {
         return hit != null && this.init.equals(hit.getBlockPos());
      }

      @Override
      public boolean init(BlockState state, BlockPos pos, boolean sneaking) {
         return this.init.equals(pos) && state.getBlock() instanceof RespawnAnchorBlock;
      }
   }

   private record Inner_NetHelper(autismclient.util.BlockPlacementPlanner.SupportCandidate support, autismclient.util.BlockPlacementPlanner.AimSample aim) {
   }

   private record Inner_StreamUtil(BlockPos cell, Vec3 center, double damageBound, double load) {
   }

   private record Inner_TaskQueue(BlockPos cell, BlockPos load, autismclient.util.ExplosionDamageCalculator.DamagePair rank, double init_D) {
   }
}
