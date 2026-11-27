package autismclient.modules;

import autismclient.ai.AiServerEvidence;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.KeybindSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.mixin.accessor.AutismMinecraftAccessor;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
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
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
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

public final class CrystalAuraModule extends Module implements SilentMovementCoordinator.Participant {
   public static final String ID = "crystal-aura";
   private static final float init_F = 6.0F;
   private static final double init_D = 2.0;
   private static final int init_I = 48;
   private static final double load_D = 8.0;
   private static final double read_D = 5.0;
   private static final double write_D = 2.0;
   private static final int load_I = 12;
   private static final int read_I = 6;
   private static final int write_I = 4;
   private static final int update_I = 2;
   private static final int sync_I = 1;
   private static final double update_D = 0.0625;
   private static final double sync_D = 2.0;
   private static final int flush_I = 1;
   private static final double flush_D = 1.0;
   private static final double close_D = 5.0;
   private static final int close_I = 2;
   private static final double open_D = 5.0;
   private static final double poll_D = 2.0;
   private static final int open_I = 3;
   private static final int poll_I = 20;
   private static final int push_I = 2;
   private static final int pop_I = 4;
   private static final int tick = 2;
   private static final int run = 40;
   private static final int get = 16;
   private autismclient.util.ExplosionDamageCalculator.SimOptions simOptions;
   private static final EnumSet<Direction> ALL_DIRECTIONS = EnumSet.allOf(Direction.class);
   private static final double[] HITBOX_SAMPLE_FRACTIONS = new double[]{0.3, 0.5, 0.7};
   private static final int set = 4;
   private static final int put = 32;
   private static int[] cachedSphereOffsets = new int[0];
   private static int add = -1;
   private static volatile boolean crystalViewEnabled;
   private static volatile float viewSize = 0.3F;
   private static volatile float viewY = -0.5F;
   private static volatile float viewSpin;
   private static volatile float viewBounce = 0.25F;
   private final Random jitterRandom;
   private long totalTickNanos;
   private long maxTickNanos;
   private int clear;
   private LivingEntity target;
   private String attackStage;
   private int reset;
   private BlockPos reservedCrystalCell;
   private final autismclient.util.ActionCooldownScheduler cooldowns;
   private final autismclient.util.CombatAimSpeedController aimSpeed;
   private final autismclient.util.SessionContextWatcher sessionWatcher;
   private final autismclient.util.TargetLeadDamageEstimator leadEstimator;
   private final autismclient.util.ActivationModeLatch activationLatch;
   private int start;
   private long burstResetDeadline;
   private long stopDeadline;
   private long switchDeadline;
   private boolean auditHasReadDeadline;
   private boolean auditHasWriteDeadline;
   private boolean auditHasUpdateDeadline;
   private HitResult aimedHit;
   private String entityFilterSource;
   private Set<String> entityFilter;
   private int stop;
   private int next;
   private int prev;
   private int valid;
   private int size;
   private BlockPos plannedObsidianPos;
   private int data;
   private LivingEntity obsidianPlanTarget;
   private int value;
   private double plannedObsidianDamage;
   private BlockPos plannedSupportPos;
   private LivingEntity supportPlanTarget;
   private final autismclient.util.StickyPlacementSelector stickySelector;
   private int cache;
   private BlockPos previewSupportPos;
   private BlockPos previewCrystalPos;
   private int state;
   private BlockPos lastPlacedSupport;
   private int queue;
   private int buffer;
   private int entry;
   private int node;
   private int task;
   private int pool;
   private BlockPos unverifiedObsidianPos;
   private int index;
   private int count;
   private final autismclient.util.LatencyTickWindow<BlockPos> recentPlacements;
   private final autismclient.util.LatencyTickWindow<EndCrystal> recentAttacks;
   private final autismclient.util.PendingCrystalAttackCache pendingAttacks;
   private int flags;
   private BlockPos bestBuilderSupport;
   private int level;
   private int frame;
   private BlockPos builderPlayerPos;
   private BlockPos builderEnemyPos;
   private BlockPos cachedSupportsOrigin;
   private BlockPos cachedSupportsEnemyPos;
   private long cachedSupportsWorldKey;
   private long cachedSupportsOptionsKey;
   private int block;
   private List<BlockPos> cachedSupports;
   private final List<CrystalAuraModule.Inner_StateCache> scoredCandidates;
   private int chunk;
   private final Map<BlockPos, Integer> init_Map;
   private static volatile int table = -2147483648;

   public CrystalAuraModule() {
      super("crystal-aura", "CrystalAura", ae.init_ae, "Places and detonates end crystals.");
      this.simOptions = autismclient.util.ExplosionDamageCalculator.SimOptions.DEFAULT.withIgnoreTerrain(true);
      this.jitterRandom = new Random();
      this.attackStage = "";
      this.reset = -2147483648;
      this.cooldowns = new autismclient.util.ActionCooldownScheduler();
      this.aimSpeed = new autismclient.util.CombatAimSpeedController();
      this.sessionWatcher = new autismclient.util.SessionContextWatcher();
      this.leadEstimator = new autismclient.util.TargetLeadDamageEstimator();
      this.activationLatch = new autismclient.util.ActivationModeLatch();
      this.entityFilter = Set.of();
      this.stop = -1;
      this.next = -1;
      this.valid = -2147483648;
      this.data = -1;
      this.value = -2147483648;
      this.stickySelector = new autismclient.util.StickyPlacementSelector();
      this.cache = -2147483648;
      this.state = -2147483648;
      this.node = -1;
      this.pool = -2147483648;
      this.index = -2147483648;
      this.recentPlacements = new autismclient.util.LatencyTickWindow<>(16);
      this.recentAttacks = new autismclient.util.LatencyTickWindow<>(16);
      this.pendingAttacks = new autismclient.util.PendingCrystalAttackCache();
      this.flags = -2147483648;
      this.level = -1;
      this.block = -2147483648;
      this.cachedSupports = List.of();
      this.scoredCandidates = new ArrayList<>(4);
      this.chunk = -2147483648;
      this.init_Map = new LinkedHashMap<>();
      this.add(new BoolSetting("place", "Place", true).description("Place on obsidian and bedrock.").build());
      this.add(new BoolSetting("destroy", "Destroy", true).description("Attack crystals in range.").build());
      this.add(
         new BoolSetting("fast-replace", "Fast Replace", true)
            .description("Replace a crystal after our dispatched break without waiting for its removal update.")
            .group("Timing")
            .build()
      );
      this.add(
         new ChoiceSetting("aim-mode", "Aim Mode", "Auto", "Auto", "Aimed")
            .description("Auto chooses blast positions; Aimed starts combos at your crosshair.")
            .build()
      );
      this.add(autismclient.util.CombatAimSpeedController.speedSetting().build());
      this.add(new BoolSetting("silent", "Silent", true).group("Aim").build());
      this.add(new BoolSetting("move-fix", "Move Fix", true).group("Aim").description("Walk where you look").visibleWhen(() -> this.bool("silent")).build());
      this.add(
         new BoolSetting("anti-weakness", "Anti Weakness", true)
            .description("Borrow a damaging hotbar weapon when weakness prevents crystal breaks.")
            .group("Damage")
            .build()
      );
      this.add(
         new BoolSetting("protect-friends", "Protect Friends", true)
            .description("Check collateral blast damage to friends and teammates.")
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
      this.add(new BoolSetting("obsidian", "Place Obsidian", true).description("Build a support when none exists.").build());
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
         new ChoiceSetting("burst-limit", "Burst Limit", "Unlimited", "Unlimited", "1", "2")
            .description("Crystals to break before a short burst reset.")
            .group("Engagement")
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
      this.add(
         new IntSetting("switch-back-delay", "Switch Back Delay", 20, 1, 100, 1)
            .description("Idle ticks before switching back.")
            .group("Place")
            .visibleWhen(() -> this.bool("place"))
            .build()
      );
      this.add(
         new BoolSetting("only-above", "Only Above", false).description("Place above target.").group("Place").visibleWhen(() -> this.bool("place")).build()
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
      this.add(new IntSetting("place-delay", "Place Delay", 0, 0, 1000, 10).description("Floor between places.").unit("ms").group("Timing").build());
      this.add(new IntSetting("destroy-delay", "Destroy Delay", 0, 0, 1000, 10).description("Floor between breaks.").unit("ms").group("Timing").build());
      this.add(new IntSetting("jitter", "Delay Jitter", 0, 0, 200, 5).description("Random extra delay.").unit("ms").group("Timing").build());
      this.add(
         new IntSetting("switch-delay", "Switch Delay", 0, 0, 1000, 10)
            .description("Extra settling time after an automatic hotbar switch.")
            .unit("ms")
            .group("Timing")
            .build()
      );
      this.add(
         new IntSetting("burst-reset", "Burst Reset", 500, 0, 2000, 50)
            .description("Pause after the selected crystal burst limit.")
            .unit("ms")
            .group("Timing")
            .visibleWhen(() -> !"Unlimited".equals(this.choice("burst-limit")))
            .build()
      );
      this.add(new BoolSetting("spam", "Spam", true).description("Run at the tick floor in a prime position.").group("Timing").build());
      this.add(
         new IntSetting("spam-delay", "Spam Delay", 0, 0, 200, 10)
            .description("Floor between actions while spamming.")
            .unit("ms")
            .group("Timing")
            .visibleWhen(() -> this.bool("spam"))
            .build()
      );
      this.add(
         new BoolSetting("vulnerable-spam", "Vulnerable Spam", true)
            .description("Prime fast with the enemy trapped above; gates cadence and anchor priority.")
            .group("Timing")
            .build()
      );
      this.add(
         new BoolSetting("defend", "Blast Protection", true)
            .description("Build damage-ranked obsidian cover against crystals and charged anchors, including level fights.")
            .group("Defend")
            .build()
      );
      this.add(new BoolSetting("preview", "Placement Preview", false).description("Show committed crystal and obsidian cells.").group("Preview").build());
      this.add(
         new ColorSetting("preview-color", "Preview Color", 1728002875)
            .description("Fill and outline for placement previews.")
            .group("Preview")
            .visibleWhen(() -> this.bool("preview"))
            .build()
      );
      this.add(new BoolSetting("crystal-view", "CrystalView", false).description("Shrink and slow crystal models.").group("CrystalView").build());
      this.add(
         new DoubleSetting("view-size", "Size", 0.3, 0.1, 1.5, 0.05)
            .description("Crystal model scale.")
            .group("CrystalView")
            .visibleWhen(() -> this.bool("crystal-view"))
            .build()
      );
      this.add(
         new DoubleSetting("view-y", "Y Translate", -0.5, -2.0, 2.0, 0.05)
            .description("Vertical offset in blocks.")
            .group("CrystalView")
            .visibleWhen(() -> this.bool("crystal-view"))
            .build()
      );
      this.add(
         new DoubleSetting("view-spin", "Spin Speed", 0.0, 0.0, 5.0, 0.05)
            .description("Rotation speed. 0 freezes it.")
            .group("CrystalView")
            .visibleWhen(() -> this.bool("crystal-view"))
            .build()
      );
      this.add(
         new DoubleSetting("view-bounce", "Bounce", 0.25, -1.0, 1.0, 0.05)
            .description("Bob height. Negative inverts it.")
            .group("CrystalView")
            .visibleWhen(() -> this.bool("crystal-view"))
            .build()
      );
      autismclient.aggressivepvp.Versions.crystalAuraCreated(this);
   }

   @Override
   public String settingsHint() {
      String hint = autismclient.aggressivepvp.Versions.hint(this);
      return hint != null ? hint : super.settingsHint();
   }

   @Override
   public void onEnable() {
      if (autismclient.aggressivepvp.Versions.onEnable(autismclient.aggressivepvp.Versions.crystal)) {
         this.stop = -1;
         this.next = -1;
         this.prev = 0;
         this.refreshViewSettings();
         return;
      }

      this.resetState();
      this.refreshViewSettings();
   }

   @Override
   public void onDisable() {
      if (autismclient.aggressivepvp.Versions.onDisable(autismclient.aggressivepvp.Versions.crystal)) {
         this.stop = -1;
         this.next = -1;
         this.prev = 0;
         this.refreshViewSettings();
         return;
      }

      int back = this.stop;
      int borrowed = this.next;
      int delay = this.prev;
      this.resetState();
      this.refreshViewSettings();
      // Do not preserve Default's deferred switch-back when handing over to Aggressive.
      boolean handingOverToAggressive = autismclient.aggressivepvp.Versions.crystal != null
         && autismclient.aggressivepvp.Versions.crystal.aggressive()
         && !autismclient.aggressivepvp.Versions.crystal.failed;
      if (back >= 0 && !handingOverToAggressive) {
         // The switch-back still runs after its delay, from tick().
         this.stop = back;
         this.next = borrowed;
         this.prev = delay;
      }

      autismclient.util.SilentRotationManager.releaseOwner("crystal-aura");
   }

   @Override
   public void onGameLeft() {
      if (autismclient.aggressivepvp.Versions.onGameLeft(autismclient.aggressivepvp.Versions.crystal)) {
         this.stop = -1;
         this.next = -1;
         this.prev = 0;
         return;
      }

      this.resetState();
      if ("crystal-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         autismclient.util.SilentRotationManager.resetAll();
      }
   }

   private void resetState() {
      this.aimSpeed.reset();
      autismclient.util.QueuedCombatAction.init("crystal-aura");
      this.sessionWatcher.init();
      this.switchDeadline = 0L;
      this.auditHasUpdateDeadline = false;
      this.totalTickNanos = 0L;
      this.maxTickNanos = 0L;
      this.clear = 0;
      this.target = null;
      this.reset = -2147483648;
      this.reservedCrystalCell = null;
      this.cooldowns.init();
      this.leadEstimator.init();
      this.activationLatch.init();
      this.start = 0;
      this.burstResetDeadline = 0L;
      this.stopDeadline = 0L;
      this.auditHasReadDeadline = false;
      this.auditHasWriteDeadline = false;
      this.stop = -1;
      this.next = -1;
      this.prev = 0;
      this.valid = -2147483648;
      this.size = 0;
      this.lastPlacedSupport = null;
      this.queue = 0;
      this.buffer = 0;
      this.entry = 0;
      this.node = -1;
      this.task = 0;
      this.pool = -2147483648;
      this.unverifiedObsidianPos = null;
      this.index = -2147483648;
      this.count = 0;
      this.recentPlacements.clear();
      this.recentAttacks.clear();
      this.pendingAttacks.init();
      this.flags = -2147483648;
      this.bestBuilderSupport = null;
      this.level = -1;
      this.frame = 0;
      table = -2147483648;
      this.builderPlayerPos = null;
      this.builderEnemyPos = null;
      this.cachedSupportsOrigin = null;
      this.cachedSupportsEnemyPos = null;
      this.block = -2147483648;
      this.cachedSupports = List.of();
      this.scoredCandidates.clear();
      this.chunk = -2147483648;
      this.previewSupportPos = null;
      this.previewCrystalPos = null;
      this.state = -2147483648;
      this.init_Map.clear();
      this.clearPlans();
      autismclient.util.InventorySlotArbiter.releaseAllClaims("crystal-aura");
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return crystalViewEnabled || this.bool("crystal-view") || "crystal-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && autismclient.util.SilentRotationManager.hasActiveRotation() || this.stop >= 0;
   }

   @Override
   public void tick() {
      this.refreshViewSettings();
      if (!this.isEnabled() && MC != null && MC.player != null) {
         if ("crystal-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
            autismclient.util.SilentRotationManager.tick("crystal-aura", MC.player);
         }

         if (MC.level != null) {
            this.update_V_17();
         }
      }
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      this.refreshViewSettings();
      this.block = -2147483648;
      if ("vulnerable-spam".equals(settingId)) {
         this.lastPlacedSupport = null;
         this.queue = 0;
         this.buffer = 0;
         this.entry = 0;
         this.node = -1;
         this.task = 0;
      }
   }

   @Override
   public String info() {
      if (autismclient.aggressivepvp.Versions.running(autismclient.aggressivepvp.Versions.crystal)) {
         return autismclient.aggressivepvp.Versions.info(autismclient.aggressivepvp.Versions.crystal);
      }

      LivingEntity current = this.target;
      return current == null ? "" : current.getName().getString();
   }

   public LivingEntity getTarget() {
      return this.target;
   }

   LivingEntity currentTarget() {
      int age = AutismSessionState.getInstance().getClientTick() - this.reset;
      return this.isEnabled() && age >= 0 && age <= 2 && this.push() ? this.target : null;
   }

   public static double averageTickMicros() {
      return al.init_Module_Str("crystal-aura") instanceof CrystalAuraModule aura && aura.clear > 0 ? aura.totalTickNanos / aura.clear / 1000.0 : 0.0;
   }

   public static double maxTickMicros() {
      return al.init_Module_Str("crystal-aura") instanceof CrystalAuraModule aura ? aura.maxTickNanos / 1000.0 : 0.0;
   }

   public static List<EspHighlightBox> highlightBoxes() {
      if (al.init_Module_Str("crystal-aura") instanceof CrystalAuraModule aura && aura.isEnabled() && aura.bool("preview")) {
         int color = am.init_I_Module(aura, "preview-color", 1728002875);
         List<EspHighlightBox> boxes = new ArrayList<>(2);
         int tick = AutismSessionState.getInstance().getClientTick();
         if (aura.plannedSupportPos == null && aura.plannedObsidianPos == null && tick - aura.state < 0) {
            if (aura.previewSupportPos != null) {
               boxes.add(new EspHighlightBox(new AABB(aura.previewSupportPos), color));
            }

            if (aura.previewCrystalPos != null) {
               boxes.add(new EspHighlightBox(new AABB(aura.previewCrystalPos), color));
            }
         }

         if (aura.plannedSupportPos != null) {
            boxes.add(new EspHighlightBox(new AABB(aura.plannedSupportPos), color));
         }

         if (aura.plannedObsidianPos != null) {
            boxes.add(new EspHighlightBox(new AABB(aura.plannedObsidianPos), color));
         }

         return boxes;
      } else {
         return List.of();
      }
   }

   public static boolean init_Z_51() {
      if (al.init_Module_Str("crystal-aura") instanceof CrystalAuraModule aura && aura.isEnabled()) {
         int age = AutismSessionState.getInstance().getClientTick() - aura.reset;
         return age >= 0 && age <= 5;
      } else {
         return false;
      }
   }

   public static boolean ownsActiveRotation() {
      return init_Z_51() && "crystal-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId());
   }

   public static boolean init_Z_BlockPos_6(BlockPos cell) {
      if (cell == null) {
         return false;
      } else if (!(al.init_Module_Str("crystal-aura") instanceof CrystalAuraModule aura && aura.isEnabled())) {
         return false;
      } else {
         if (aura.reset != AutismSessionState.getInstance().getClientTick()) {
            return false;
         }

         BlockPos reserved = aura.reservedCrystalCell;
         return reserved == null
            ? false
            : cell.getX() == reserved.getX() && cell.getZ() == reserved.getZ() && cell.getY() >= reserved.getY() && cell.getY() < reserved.getY() + 2;
      }
   }

   public static boolean init_Z_I_10(int slot) {
      if (autismclient.aggressivepvp.Versions.running(autismclient.aggressivepvp.Versions.crystal)) {
         return false;
      }
      return al.init_Module_Str("crystal-aura") instanceof CrystalAuraModule aura && aura.isEnabled() ? aura.stop >= 0 && aura.next == slot : false;
   }

   public static boolean sync_Z_11() {
      return al.init_Module_Str("crystal-aura") instanceof CrystalAuraModule aura && aura.isEnabled() ? aura.plannedObsidianPos != null || aura.plannedSupportPos != null : false;
   }

   public static boolean flush() {
      return al.init_Module_Str("crystal-aura") instanceof CrystalAuraModule aura && aura.isEnabled() ? aura.pop() : false;
   }

   public static boolean close() {
      int age = AutismSessionState.getInstance().getClientTick() - table;
      return age >= 0 && age <= 1;
   }

   public static boolean load_Z_BlockPos_3(BlockPos cell) {
      if (cell == null) {
         return false;
      }

      if (al.init_Module_Str("crystal-aura") instanceof CrystalAuraModule aura && aura.isEnabled()) {
         BlockPos obsidian = aura.plannedObsidianPos;
         if (obsidian != null
            && cell.getX() == obsidian.getX()
            && cell.getZ() == obsidian.getZ()
            && cell.getY() >= obsidian.getY()
            && cell.getY() < obsidian.getY() + 2) {
            return true;
         }

         BlockPos support = aura.plannedSupportPos;
         return support != null && cell.getX() == support.getX() && cell.getZ() == support.getZ() && cell.getY() == support.getY() + 1;
      } else {
         return false;
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
      if (autismclient.aggressivepvp.Versions.running(autismclient.aggressivepvp.Versions.crystal)) {
         return false;
      }

      boolean enabled = this.isEnabled();
      return !SilentMovementCoordinator.isSafeWalkActive() && (autismclient.util.SilentRotationManager.isReturningToPlayer() || enabled && this.push());
   }

   private static boolean auditBeforeDeadline(boolean active, long deadline) {
      return active && System.nanoTime() - deadline < 0L;
   }

   private boolean poll() {
      if (auditBeforeDeadline(this.auditHasWriteDeadline, this.stopDeadline)) {
         return false;
      }

      int bind = this.integer("activation-key");
      boolean down = bind != -1 && autismclient.util.ValueSink.init(MC, bind);
      if (!this.activationLatch.init(this.choice("activation"), down)) {
         this.start = 0;
         return false;
      }

      byte var10000;
      switch (this.choice("burst-limit")) {
         case "1":
            var10000 = 1;
            break;
         case "2":
            var10000 = 2;
            break;
         default:
            var10000 = 0;
      }

      int limit = var10000;
      if (limit != 0 && this.start >= limit) {
         if (auditBeforeDeadline(this.auditHasReadDeadline, this.burstResetDeadline)) {
            return false;
         }

         this.start = 0;
         return true;
      } else {
         return true;
      }
   }

   private void checkTargetKilled(LivingEntity previous) {
      if (previous != null && (!previous.isAlive() || previous.isRemoved() || !(autismclient.util.ExplosionDamageCalculator.effectiveHealth(previous) > 0.0))) {
         this.start = 0;
         if (this.bool("stop-on-kill")) {
            this.stopDeadline = System.nanoTime() + this.integer("stop-delay") * 1000000L;
            this.auditHasWriteDeadline = true;
            this.activationLatch.init("Press");
         }
      }
   }

   private boolean push() {
      return MC != null
         && MC.player != null
         && MC.level != null
         && MC.gameMode != null
         && MC.getConnection() != null
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && !MC.player.isDeadOrDying()
         && !MC.player.isSpectator()
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

   @Override
   public void preMovementTick() {
      if (autismclient.aggressivepvp.Versions.preMovementTick(autismclient.aggressivepvp.Versions.crystal)) {
         return;
      }

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
            if ("crystal-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
               autismclient.util.SilentRotationManager.resetAll();
            }
         });
         if (MC.gui.screen() == null && MC.missTime > 10) {
            MC.missTime = 0;
         }

         this.update_V_17();
         if (!this.push()) {
            this.target = null;
            this.clearPlans();
            this.lastPlacedSupport = null;
            this.queue = 0;
            this.buffer = 0;
            this.entry = 0;
            this.node = -1;
            this.task = 0;
            this.releaseClaims();
            return;
         }

         LivingEntity previousTarget = this.target;
         this.checkTargetKilled(previousTarget);
         if (!this.poll()) {
            this.target = null;
            this.clearPlans();
            this.releaseClaims();
            return;
         }

         LivingEntity enemy = this.findTarget();
         if (enemy != previousTarget) {
            this.start = 0;
         }

         this.target = enemy;
         if (enemy != null) {
            this.leadEstimator.init(enemy, AutismSessionState.getInstance().getClientTick());
         }

         if (enemy == null) {
            this.clearPlans();
            this.lastPlacedSupport = null;
            this.queue = 0;
            this.buffer = 0;
            this.entry = 0;
            this.node = -1;
            this.task = 0;
            this.releaseClaims();
            return;
         }

         this.simOptions = autismclient.util.ExplosionDamageCalculator.SimOptions.DEFAULT.withIgnoreTerrain(this.bool("terrain"));
         if (this.unverifiedObsidianPos != null) {
            if (MC.level.getBlockState(this.unverifiedObsidianPos) != autismclient.util.ServerVerifiedBlockState.init(MC.level, this.unverifiedObsidianPos)) {
               if (!this.init_Z_LivingEntity_4(enemy)) {
                  this.releaseClaims();
               }

               return;
            }

            this.unverifiedObsidianPos = null;
         }

         this.aimedHit = "Aimed".equals(this.choice("aim-mode")) ? autismclient.util.WeaponDamageCalculator.init() : null;
         CrystalAuraModule.Inner_NetHelper place = null;
         CrystalAuraModule.Inner_TaskQueue obsidian = null;
         CrystalAuraModule.Inner_TaskQueue defend = null;

         CrystalAuraModule.Inner_ConfigStore destroy;
         try (autismclient.util.ExplosionDamageCalculator.SimScope pass = autismclient.util.ExplosionDamageCalculator.beginSimScope()) {
            defend = this.planDefense(new autismclient.util.BlockPlacementPlanner.RayBudget(48), true);
            if (defend != null) {
               this.clearPlans();
               table = AutismSessionState.getInstance().getClientTick();
            }

            destroy = defend == null ? this.planDestroy(enemy) : null;
            CrystalAuraModule.Inner_ConfigStore waitingDestroy = null;
            if (destroy != null && !this.cooldowns.isReadyOrOtherCooling("destroy", "place", AutismSessionState.getInstance().getClientTick(), System.nanoTime())) {
               waitingDestroy = destroy;
               destroy = null;
            }

            if (destroy == null && defend == null) {
               autismclient.util.BlockPlacementPlanner.RayBudget budget = new autismclient.util.BlockPlacementPlanner.RayBudget(48);
               if (this.read(enemy)) {
                  obsidian = this.init_t_TaskQueue_U_StreamUtil(budget);
               } else if (this.update(enemy)) {
                  place = this.load(enemy, budget);
               }

               if (obsidian == null && place == null && this.plannedObsidianPos == null && this.plannedSupportPos == null) {
                  place = this.planPlace(enemy, budget);
                  boolean builderSuppressed = false;
                  if (this.bool("place") && this.bool("obsidian") && this.put() && (place == null || place.load() < 8.0)) {
                     if (this.write(enemy)) {
                        builderSuppressed = true;
                     } else if (!this.run()) {
                        obsidian = this.planBuilder(enemy, budget, place == null ? -1.0 : place.load());
                        if (obsidian != null) {
                           place = null;
                           this.setBuilderPlan(enemy, obsidian);
                        }
                     }
                  }

                  if (obsidian == null && place != null) {
                     this.setSupportPlan(enemy, place);
                  }

                  if (builderSuppressed && place == null) {
                     if (++this.frame >= 4) {
                        this.bestBuilderSupport = null;
                        this.frame = 0;
                     }
                  } else {
                     this.frame = 0;
                  }
               }

               if (place == null && obsidian == null && this.plannedObsidianPos == null && this.plannedSupportPos == null) {
                  defend = this.init_t_TaskQueue_LivingEntity_U_StreamUtil(enemy, budget);
                  if (defend == null) {
                     defend = this.planDefense(budget, false);
                  }

                  if (defend != null) {
                     table = AutismSessionState.getInstance().getClientTick();
                  }
               }

               if (place == null && obsidian == null && defend == null) {
                  destroy = waitingDestroy;
               }
            }
         }

         this.init(enemy, destroy, place, obsidian);
         if (destroy != null || place != null || obsidian != null || defend != null) {
            if (defend != null ? AnchorAuraModule.read_Z() : !this.pop() && AnchorAuraModule.init_Z_2()) {
               this.releaseClaims();
               return;
            }

            this.reset = AutismSessionState.getInstance().getClientTick();
            this.reservedCrystalCell = null;
            boolean borrowsHand = place != null && place.hand() != InteractionHand.OFF_HAND
               || obsidian != null && obsidian.hand() != InteractionHand.OFF_HAND
               || defend != null && defend.hand() != InteractionHand.OFF_HAND
               || this.stop >= 0;
            if (borrowsHand) {
               autismclient.util.InventorySlotArbiter.claimInventoryClick("crystal-aura");
            } else {
               autismclient.util.InventorySlotArbiter.releaseInventoryClick("crystal-aura");
            }

            if (destroy != null) {
               this.applyAntiWeakness();
            }

            RotationMath.Rotation wire = this.serverRotation();
            RotationMath.Rotation goal = destroy != null
               ? destroy.goal()
               : (
                  place != null
                     ? place.placement().aim().goalRotation()
                     : (
                        obsidian != null
                           ? obsidian.placement().aim().goalRotation()
                           : defend.placement().aim().goalRotation()
                     )
               );
            boolean ready = wire != null && this.actionReady(destroy != null) && (destroy == null || this.set());
            Vec3 destroyHit = ready && destroy != null ? this.destroyHitPoint(destroy, wire) : null;
            BlockHitResult placeHit = ready && destroy == null && place != null ? this.placeHit(place, wire) : null;
            BlockHitResult obsidianHit = ready && destroy == null && place == null && obsidian != null
               ? this.builderHit(obsidian, wire)
               : null;
            BlockHitResult defendHit = ready && destroy == null && place == null && obsidian == null && defend != null
               ? this.builderHit(defend, wire)
               : null;
            boolean fire = ready
               && (destroy != null ? destroyHit != null : (place != null ? placeHit != null : (obsidian != null ? obsidianHit != null : defendHit != null)));
            fire = fire && !BedDefenderModule.init_Z_10() && !SurroundModule.init_Z_27();
            this.aimSpeed.init("crystal-aura", 18, MC.player, enemy, this.decimal("rotation-speed-multiplier"), fire ? wire : goal, fire);
            if (!fire) {
               return;
            }

            RotationMath.Rotation outgoing = SilentMovementCoordinator.getRenderRotation(MC.player);
            if (outgoing == null || !sameRotation(outgoing, wire)) {
               if (destroy != null) {
                  this.attackStage = "outgoing";
               }

               return;
            }

            if (destroy != null) {
               this.executeDestroy(destroy, destroyHit);
               return;
            }

            if (place != null) {
               this.executePlace(place, placeHit);
               return;
            }

            if (obsidian != null) {
               this.executeBuilder(obsidian, obsidianHit);
            } else {
               this.load(defend, defendHit);
            }

            return;
         }

         if (!this.init_Z_LivingEntity_4(enemy)) {
            this.releaseClaims();
         }
      } finally {
         long elapsed = System.nanoTime() - profileStarted;
         this.totalTickNanos += elapsed;
         this.maxTickNanos = Math.max(this.maxTickNanos, elapsed);
         this.clear++;
      }
   }

   private static boolean sameRotation(RotationMath.Rotation first, RotationMath.Rotation second) {
      return Math.abs(RotationMath.angleDelta(first.yaw(), second.yaw())) <= 0.05F && Math.abs(first.pitch() - second.pitch()) <= 0.05F;
   }

   private boolean init_Z_LivingEntity_4(LivingEntity enemy) {
      return !SurroundModule.init_Z_27()
         && !BedDefenderModule.init_Z_10()
         && (this.pop() || !AnchorAuraModule.init_Z_2())
         && this.aimSpeed.init("crystal-aura", 18, MC.player, enemy, this.serverRotation());
   }

   private void releaseClaims() {
      this.aimSpeed.reset();
      autismclient.util.InventorySlotArbiter.releaseInventoryClick("crystal-aura");
      if ("crystal-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         autismclient.util.SilentRotationManager.releaseOwner("crystal-aura");
         autismclient.util.SilentRotationManager.tick("crystal-aura", MC.player);
      }
   }

   private RotationMath.Rotation serverRotation() {
      ServerRotationTracker.RotationSnapshot snapshot = ServerRotationTracker.getSnapshot();
      return !snapshot.initialized() ? null : new RotationMath.Rotation(snapshot.currentYaw(), snapshot.currentPitch());
   }

   private RotationMath.Rotation wireRotation() {
      RotationMath.Rotation wire = this.serverRotation();
      return wire != null ? wire : RotationMath.ofPlayer(MC.player);
   }

   private boolean actionReady(boolean destroy) {
      int tick = AutismSessionState.getInstance().getClientTick();
      return this.cooldowns.isReady(destroy ? "destroy" : "place", tick, System.nanoTime());
   }

   private void recordAction(boolean destroy) {
      int tick = AutismSessionState.getInstance().getClientTick();
      this.aimSpeed.load(this.target, tick, autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs());
      long now = System.nanoTime();
      if (this.spamEnabled()) {
         this.cooldowns.schedule(destroy ? "destroy" : "place", tick, now, this.integer("spam-delay") * 1000000L);
      } else {
         this.cooldowns
            .schedule(
               destroy ? "destroy" : "place",
               tick,
               now,
               autismclient.util.ActionCooldownScheduler.jitteredDelayNanos(this.integer(destroy ? "destroy-delay" : "place-delay"), this.integer("jitter"), this.jitterRandom)
            );
      }

      this.prev = this.integer("switch-back-delay");
   }

   private void init(LivingEntity enemy, CrystalAuraModule.Inner_ConfigStore destroy, CrystalAuraModule.Inner_NetHelper place, CrystalAuraModule.Inner_TaskQueue obsidian) {
      if (place == null) {
         boolean held = destroy != null || obsidian != null || this.plannedObsidianPos != null || this.plannedSupportPos != null;
         if (held) {
            this.buffer = 0;
            this.task = 0;
         } else {
            if (++this.buffer > 1) {
               this.lastPlacedSupport = null;
               this.queue = 0;
            }

            if (++this.task > 1) {
               this.entry = 0;
               this.node = -1;
            }
         }
      } else {
         this.buffer = 0;
         BlockPos support = place.support();
         boolean strong = place.load() >= this.decimal("min-target-damage") + 2.0;
         boolean sameArea = this.lastPlacedSupport != null
            && Math.abs(support.getX() - this.lastPlacedSupport.getX()) <= 1
            && Math.abs(support.getY() - this.lastPlacedSupport.getY()) <= 1
            && Math.abs(support.getZ() - this.lastPlacedSupport.getZ()) <= 1;
         this.lastPlacedSupport = support;
         if (!strong) {
            this.queue = 0;
         } else {
            this.queue = sameArea ? this.queue + 1 : 1;
         }

         if (this.load_Z_LivingEntity_2(enemy)) {
            this.task = 0;
            this.entry = enemy.getId() == this.node ? this.entry + 1 : 1;
            this.node = enemy.getId();
         } else if (++this.task > 1) {
            this.entry = 0;
            this.node = -1;
         }
      }
   }

   private boolean pop() {
      LivingEntity enemy = this.target;
      if (enemy != null && MC.player != null) {
         int age = AutismSessionState.getInstance().getClientTick() - this.flags;
         boolean blastGrace = age >= 0 && age <= 2;
         if (!this.bool("vulnerable-spam") || this.entry < 2 || !this.load_Z_LivingEntity_2(enemy) && this.task > 1) {
            if (this.queue < 2) {
               return false;
            } else {
               return blastGrace ? true : MC.player.getDeltaMovement().lengthSqr() <= 0.0625 && enemy.getDeltaMovement().lengthSqr() <= 0.0625;
            }
         } else {
            return blastGrace ? true : horizontalSpeedSq(MC.player) <= 0.0625 && horizontalSpeedSq(enemy) <= 0.0625;
         }
      } else {
         return false;
      }
   }

   private boolean load_Z_LivingEntity_2(LivingEntity enemy) {
      double drop = enemy.getY() - MC.player.getY();
      if (!(drop < 1.0) && !(drop > 5.0)) {
         double dx = enemy.getX() - MC.player.getX();
         double dz = enemy.getZ() - MC.player.getZ();
         double range = MC.player.blockInteractionRange();
         return dx * dx + dz * dz <= range * range;
      } else {
         return false;
      }
   }

   private static double horizontalSpeedSq(Entity entity) {
      Vec3 delta = entity.getDeltaMovement();
      return delta.x * delta.x + delta.z * delta.z;
   }

   private boolean spamEnabled() {
      return this.bool("spam") && this.pop();
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
               && entity.hurtTime <= this.integer("hurt-time")
               && autismclient.util.ExplosionDamageCalculator.effectiveHealth(entity) > 0.0
         );
      if (found.isEmpty()) {
         return null;
      }

      Vec3 eye = player.getEyePosition();
      double rangeSq = range * range;
      String mode = this.choice("targeting");
      LivingEntity best = null;
      double bestScore = 1.7976931348623157E308;

      for (LivingEntity entity : found) {
         double distanceSq = distanceSqToBox(entity.getBoundingBox(), eye);
         if (!(distanceSq > rangeSq)) {
            double score = this.init(mode, entity, distanceSq, eye);
            if (score < bestScore) {
               bestScore = score;
               best = entity;
            }
         }
      }

      LivingEntity incumbent = this.target;
      if (incumbent != null && incumbent != best && found.contains(incumbent)) {
         double grace = range + 0.5;
         double incumbentDistanceSq = distanceSqToBox(incumbent.getBoundingBox(), eye);
         if (incumbentDistanceSq <= grace * grace
            && (best == null || !autismclient.util.TargetSwitchHysteresis.init(mode, bestScore, this.init(mode, incumbent, incumbentDistanceSq, eye)))) {
            return incumbent;
         }
      }

      return best;
   }

   private double init(String mode, LivingEntity entity, double distanceSq, Vec3 eye) {
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
      Set<String> ids = this.entityFilterIds();
      int separator = id.indexOf(58);
      return ids.contains(id) || separator >= 0 && ids.contains(id.substring(separator + 1));
   }

   private Set<String> entityFilterIds() {
      List<String> entries = this.list("entities");
      String source = String.join("|", entries);
      if (source.equals(this.entityFilterSource)) {
         return this.entityFilter;
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
      this.entityFilter = Set.copyOf(normalized);
      return this.entityFilter;
   }

   private CrystalAuraModule.Inner_ConfigStore planDestroy(LivingEntity enemy) {
      if (!this.bool("destroy")) {
         return null;
      }

      LocalPlayer player = MC.player;
      double range = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
      if (range <= 0.0) {
         return null;
      }

      Vec3 eye = player.getEyePosition();
      AABB search = player.getBoundingBox().inflate(range + 2.0);
      List<EndCrystal> crystals = MC.level.getEntitiesOfClass(EndCrystal.class, search, Entity::isAlive);
      if (crystals.isEmpty()) {
         return null;
      }

      double rangeSq = range * range;
      int tick = AutismSessionState.getInstance().getClientTick();

      for (EndCrystal tracked : new ArrayList<>(this.recentAttacks.activeKeys(tick))) {
         if (MC.level.getEntity(tracked.getId()) != tracked || !tracked.isAlive()) {
            this.recentAttacks.remove(tracked);
         }
      }

      if (this.recentPlacements.size() > 0) {
         for (EndCrystal crystal : crystals) {
            if (!this.pendingAttacks.init(crystal, tick)) {
               this.recentPlacements.remove(BlockPos.containing(crystal.position()));
            }
         }
      }

      List<CrystalAuraModule.Inner_BufferHelper> choices = new ArrayList<>();
      AABB blastTarget = this.predictedHitbox(enemy, 0, 0.5);

      for (EndCrystal crystal : crystals) {
         if (!this.recentAttacks.init(crystal, tick)
            && (!"Aimed".equals(this.choice("aim-mode")) || this.aimedHit instanceof EntityHitResult aimed && aimed.getEntity() == crystal)
            && !(distanceSqToBox(crystal.getBoundingBox(), eye) > rangeSq)) {
            autismclient.util.ExplosionDamageCalculator.DamagePair rank = this.rankDamage(enemy, crystal.position(), blastTarget);
            if (this.meetsDamageThresholds(enemy, rank) && this.isSafeForFriends(crystal.position())) {
               choices.add(new CrystalAuraModule.Inner_BufferHelper(crystal, rank));
            }
         }
      }

      choices.sort(
         (first, second) -> this.isBetterRank(first.rank(), second.rank())
            ? -1
            : (this.isBetterRank(second.rank(), first.rank()) ? 1 : 0)
      );
      int tried = 0;

      for (CrystalAuraModule.Inner_BufferHelper choice : choices) {
         if (tried++ >= 4) {
            break;
         }

         RotationMath.Rotation goal = this.crystalAimRotation(choice.crystal(), range);
         if (goal != null) {
            return new CrystalAuraModule.Inner_ConfigStore(choice.crystal(), goal, range);
         }
      }

      return null;
   }

   private RotationMath.Rotation crystalAimRotation(EndCrystal crystal, double range) {
      Vec3 eye = MC.player.getEyePosition();
      RotationMath.Rotation wire = this.wireRotation();
      AABB box = crystal.getBoundingBox();
      if (autismclient.util.HitboxRayHelper.init(
            box, eye, Vec3.directionFromRotation(wire.pitch(), wire.yaw()), range, pointx -> this.isRayBlocked(eye, pointx)
         )
         != null) {
         return wire;
      }

      RotationMath.Rotation best = null;
      float bestDelta = 3.4028235E38F;
      Vec3 nearest = autismclient.util.HitboxRayHelper.init(box, eye);
      RotationMath.Rotation nearAim = RotationMath.lookAt(nearest, eye);
      if (autismclient.util.HitboxRayHelper.init(
            box, eye, Vec3.directionFromRotation(nearAim.pitch(), nearAim.yaw()), range, pointx -> this.isRayBlocked(eye, pointx)
         )
         != null) {
         best = nearAim;
         bestDelta = RotationMath.vectorAngleDegrees(nearAim, wire);
      }

      for (double fx : HITBOX_SAMPLE_FRACTIONS) {
         for (double fy : HITBOX_SAMPLE_FRACTIONS) {
            for (double fz : HITBOX_SAMPLE_FRACTIONS) {
               Vec3 point = new Vec3(Mth.lerp(fx, box.minX, box.maxX), Mth.lerp(fy, box.minY, box.maxY), Mth.lerp(fz, box.minZ, box.maxZ));
               double distance = eye.distanceTo(point);
               if (!(distance > range) && !this.isRayBlocked(eye, point)) {
                  RotationMath.Rotation rotation = RotationMath.lookAt(point, eye);
                  float delta = RotationMath.vectorAngleDegrees(rotation, wire);
                  if (delta < bestDelta) {
                     bestDelta = delta;
                     best = rotation;
                  }
               }
            }
         }
      }

      return best;
   }

   private Vec3 destroyHitPoint(CrystalAuraModule.Inner_ConfigStore plan, RotationMath.Rotation wire) {
      if (!plan.crystal().isAlive()) {
         return null;
      }

      if (((AutismMinecraftAccessor)MC).autism$getMissTime() > 0) {
         return null;
      }

      Vec3 eye = MC.player.getEyePosition();
      Vec3 look = Vec3.directionFromRotation(wire.pitch(), wire.yaw());
      Vec3 hit = autismclient.util.HitboxRayHelper.init(
         plan.crystal().getBoundingBox(), eye, look, plan.range(), point -> this.isRayBlocked(eye, point)
      );
      if (hit == null) {
         this.attackStage = "ray";
      }

      return hit;
   }

   private boolean executeDestroy(CrystalAuraModule.Inner_ConfigStore plan, Vec3 hitPoint) {
      EndCrystal crystal = plan.crystal();
      if (!crystal.isAlive()) {
         return false;
      }

      LivingEntity enemy = this.target;
      if (!autismclient.util.InventorySlotArbiter.beginActionGroup("crystal-aura")) {
         this.attackStage = "hand-group";
         return false;
      }

      try {
         ((AutismMultiPlayerGameModeAccessor)MC.gameMode).autism$ensureHasSentCarriedItem();
      } finally {
         autismclient.util.InventorySlotArbiter.endActionGroup("crystal-aura");
      }

      if (!autismclient.util.QueuedCombatAction.init(
         "crystal-aura",
         new EntityHitResult(crystal, hitPoint),
         () -> this.push()
            && crystal.isAlive()
            && this.set()
            && this.serverRotation() != null
            && this.destroyHitPoint(plan, this.serverRotation()) != null
            && this.init_Z_LivingEntity_3(enemy, crystal.position(), false),
         () -> this.onAttackDispatched(crystal)
      )) {
         this.attackStage = "queue";
         return false;
      } else {
         this.attackStage = "queued";
         return true;
      }
   }

   private void onAttackDispatched(EndCrystal crystal) {
      this.attackStage = "dispatched";
      this.clearPlans();
      this.recordAction(true);
      this.recentAttacks.init(crystal, AutismSessionState.getInstance().getClientTick(), autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs());
      this.pendingAttacks.init(crystal, BlockPos.containing(crystal.position()), AutismSessionState.getInstance().getClientTick(), autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs());
      this.flags = AutismSessionState.getInstance().getClientTick();
      this.start++;
      if (!"Unlimited".equals(this.choice("burst-limit")) && this.start >= Integer.parseInt(this.choice("burst-limit"))) {
         this.burstResetDeadline = System.nanoTime() + this.integer("burst-reset") * 1000000L;
         this.auditHasReadDeadline = true;
         this.activationLatch.init("Press");
      }
   }

   private CrystalAuraModule.Inner_NetHelper planPlace(LivingEntity enemy, autismclient.util.BlockPlacementPlanner.RayBudget budget) {
      if (!this.bool("place")) {
         return null;
      }

      LocalPlayer player = MC.player;
      double range = player.blockInteractionRange();
      if (range <= 0.0) {
         return null;
      }

      if (!this.put()) {
         return null;
      }

      List<BlockPos> supports = this.findSupports(enemy, range);
      if (supports.isEmpty()) {
         return null;
      }

      Vec3 eye = player.getEyePosition();
      RotationMath.Rotation wire = this.wireRotation();
      autismclient.util.BlockPlacementPlanner.RejectReason[] refusal = new autismclient.util.BlockPlacementPlanner.RejectReason[1];
      List<CrystalAuraModule.Inner_JsonUtil> entries = new ArrayList<>();
      List<autismclient.util.BlockPlacementPlanner.SupportCandidate> scan = new ArrayList<>();

      for (BlockPos support : supports) {
         BlockPos cell = support.above();
         BlockState state = MC.level.getBlockState(support);
         autismclient.util.BlockPlacementPlanner.Query request = new autismclient.util.BlockPlacementPlanner.Query(
               cell, eye, range, autismclient.util.BlockPlacementPlanner.anchorFaceValidator(support, ALL_DIRECTIONS)
            )
            .withFromRotation(wire)
            .withLeadEye(eye.add(player.getDeltaMovement()))
            .withRayBudget(budget);
         scan.clear();
         this.init(support, state, cell, eye, wire, scan);

         for (autismclient.util.BlockPlacementPlanner.SupportCandidate option : scan) {
            entries.add(new CrystalAuraModule.Inner_JsonUtil(support, request, option));
         }
      }

      if (entries.isEmpty()) {
         return null;
      }

      int size = entries.size();
      int start = this.size >= 0 && this.size < size ? this.size : 0;

      for (int step = 0; step < size; step++) {
         int index = (start + step) % size;
         CrystalAuraModule.Inner_JsonUtil entry = entries.get(index);
         autismclient.util.BlockPlacementPlanner.AimSample aim = autismclient.util.BlockPlacementPlanner.computeAimSample(entry.option(), entry.request(), refusal);
         if (aim != null) {
            autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.verifyPlacement(
               entry.option(), aim, entry.request(), refusal
            );
            if (candidate != null) {
               InteractionHand hand = this.crystalHand();
               this.size = 0;
               BlockPos chosen = entry.support();
               Vec3 chosenSource = new Vec3(chosen.getX() + 0.5, chosen.getY() + 1.0, chosen.getZ() + 0.5);
               double chosenDamage = this.rankDamage(enemy, chosenSource, this.predictedHitbox(enemy, this.leadHorizonTicks(), 1.5))
                  .load();
               return new CrystalAuraModule.Inner_NetHelper(chosen, hand, range, candidate, entry.request(), chosenDamage);
            }

            if (refusal[0] == autismclient.util.BlockPlacementPlanner.RejectReason.NO_BUDGET) {
               this.size = index;
               return null;
            }
         }
      }

      this.size = 0;
      return null;
   }

   private void init(BlockPos support, BlockState state, BlockPos cell, Vec3 eye, RotationMath.Rotation wire, List<autismclient.util.BlockPlacementPlanner.SupportCandidate> out) {
      boolean requiresSneak = autismclient.util.BlockPlacementPlanner.requiresSneakToPlace(state, support);
      List<CrystalAuraModule.Inner_SyncManager> keyed = new ArrayList<>();

      for (Direction face : autismclient.util.BlockPlacementPlanner.DEFAULT_FACE_ORDER) {
         autismclient.util.BlockPlacementPlanner.SupportFace intent = new autismclient.util.BlockPlacementPlanner.SupportFace(support, face);

         for (AABB rect : autismclient.util.BlockPlacementPlanner.extractFaceRects(state, support, face, 3)) {
            autismclient.util.BlockPlacementPlanner.SupportCandidate option = new autismclient.util.BlockPlacementPlanner.SupportCandidate(
               cell, support, face, 1, state, rect, init_D_AABB_Direction_2(rect, face), autismclient.util.BlockPlacementPlanner.edgeMarginRatio(rect, face, eye), requiresSneak, intent
            );
            double turn = RotationMath.vectorAngleDegrees(RotationMath.lookAt(autismclient.util.BlockPlacementPlanner.faceCenter(rect, face), eye), wire);
            keyed.add(new CrystalAuraModule.Inner_SyncManager(option, turn));
         }
      }

      keyed.sort(Comparator.comparingDouble(CrystalAuraModule.Inner_SyncManager::turn));

      for (CrystalAuraModule.Inner_SyncManager entry : keyed) {
         out.add(entry.option());
      }
   }

   private static double init_D_AABB_Direction_2(AABB rect, Direction face) {
      Axis normal = face.getAxis();
      Axis first = autismclient.util.BlockPlacementPlanner.tangentAxis(normal, true);
      Axis second = autismclient.util.BlockPlacementPlanner.tangentAxis(normal, false);
      return (rect.max(first) - rect.min(first)) * (rect.max(second) - rect.min(second));
   }

   private List<BlockPos> findSupports(LivingEntity enemy, double range) {
      LocalPlayer player = MC.player;
      Vec3 eye = player.getEyePosition();
      BlockPos origin = player.blockPosition();
      int tick = AutismSessionState.getInstance().getClientTick();
      long worldKey = AiServerEvidence.blockRevision(origin) ^ AiServerEvidence.blockRevision(enemy.blockPosition());
      long optionsKey = Objects.hash(
         this.decimal("min-target-damage"), this.decimal("max-self-damage"), this.bool("efficient"), this.bool("only-above"), this.bool("terrain")
      );
      if (tick == this.block
         && origin.equals(this.cachedSupportsOrigin)
         && enemy.blockPosition().equals(this.cachedSupportsEnemyPos)
         && worldKey == this.cachedSupportsWorldKey
         && optionsKey == this.cachedSupportsOptionsKey) {
         return this.cachedSupports;
      }

      this.scoredCandidates.clear();
      this.chunk = tick;
      int[] offsets = sphereOffsets(Mth.ceil(range));
      double floor = this.decimal("min-target-damage");
      double rangeSq = range * range;
      AABB nextBox = player.getBoundingBox().move(player.getDeltaMovement());
      AABB blastTarget = this.predictedHitbox(enemy, this.leadHorizonTicks(), 1.5);
      autismclient.util.ExplosionDamageCalculator.SimOptions targetOptions = this.simOptions.withTargetBox(blastTarget);
      List<CrystalAuraModule.Inner_DataUtil> candidates = new ArrayList<>();
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         if (this.read(cursor)
            && (!"Aimed".equals(this.choice("aim-mode")) || this.aimedHit instanceof BlockHitResult aimed && aimed.getBlockPos().equals(cursor))
            && MC.level.isEmptyBlock(cursor.above())) {
            Vec3 source = new Vec3(cursor.getX() + 0.5, cursor.getY() + 1.0, cursor.getZ() + 0.5);
            if (!this.bool("only-above") || !(source.y < enemy.getY())) {
               double distanceSq = eye.distanceToSqr(source);
               if (!(distanceSq > rangeSq)) {
                  double bound = autismclient.util.ExplosionDamageCalculator.calculateUpperBoundDamage(enemy, source, 6.0F, targetOptions);
                  if (!(bound < floor) && this.init_Z_BlockPos_AABB_3(cursor, nextBox) && !this.write(cursor)) {
                     candidates.add(new CrystalAuraModule.Inner_DataUtil(cursor.immutable(), source, bound, distanceSq));
                  }
               }
            }
         }
      }

      if (candidates.isEmpty()) {
         return List.of();
      }

      candidates.sort(Comparator.comparingDouble(CrystalAuraModule.Inner_DataUtil::bound).reversed());
      List<CrystalAuraModule.Inner_StateCache> accepted = new ArrayList<>();
      double bestDamage = -1.0;
      int evaluated = 0;

      for (CrystalAuraModule.Inner_DataUtil candidate : candidates) {
         if (accepted.size() >= 4 && candidate.bound() <= bestDamage || evaluated >= 32) {
            break;
         }

         if (this.init_Z_Vec3_9(eye, candidate.support(), candidate.source())) {
            evaluated++;
            autismclient.util.ExplosionDamageCalculator.DamagePair rank = this.rankDamage(enemy, candidate.source(), blastTarget);
            if (!this.meetsDamageThresholds(enemy, rank)) {
               if (this.bool("defend") && this.scoredCandidates.size() < 4 && rank.load() >= floor && rank.selfDamage() > this.decimal("max-self-damage")) {
                  this.scoredCandidates.add(new CrystalAuraModule.Inner_StateCache(candidate.support(), rank, candidate.load()));
               }
            } else if (this.isSafeForFriends(candidate.source())) {
               accepted.add(new CrystalAuraModule.Inner_StateCache(candidate.support(), rank, candidate.load()));
               bestDamage = Math.max(bestDamage, rank.load());
            }
         }
      }

      if (accepted.isEmpty()) {
         this.cachedSupports = List.of();
         return List.of();
      }

      accepted.sort((first, second) -> {
         int byBucket = Double.compare(damageBucket(second.rank().load()), damageBucket(first.rank().load()));
         if (byBucket != 0) {
            return byBucket;
         }

         int bySelf = Double.compare(first.rank().selfDamage(), second.rank().selfDamage());
         return bySelf != 0 ? bySelf : Double.compare(second.init_D(), first.init_D());
      });
      List<BlockPos> result = new ArrayList<>(Math.min(4, accepted.size()));

      for (CrystalAuraModule.Inner_StateCache scored : accepted) {
         if (result.size() >= 4) {
            break;
         }

         result.add(scored.cell());
      }

      this.cachedSupports = result;
      this.cachedSupportsOrigin = origin.immutable();
      this.cachedSupportsEnemyPos = enemy.blockPosition().immutable();
      this.cachedSupportsWorldKey = worldKey;
      this.cachedSupportsOptionsKey = optionsKey;
      this.block = tick;
      return result;
   }

   private static double damageBucket(double targetDamage) {
      return Math.floor(targetDamage * 2.0);
   }

   private boolean read(BlockPos pos) {
      BlockState state = MC.level.getBlockState(pos);
      return state.getBlock() == Blocks.OBSIDIAN || state.getBlock() == Blocks.BEDROCK;
   }

   private boolean init_Z_BlockPos_AABB_3(BlockPos support, AABB nextBox) {
      BlockPos cell = support.above();
      if (!MC.level.isEmptyBlock(cell)) {
         return false;
      }

      AABB box = new AABB(cell.getX(), cell.getY(), cell.getZ(), cell.getX() + 1.0, cell.getY() + 2.0, cell.getZ() + 1.0);
      if (nextBox.intersects(box)) {
         return false;
      }

      if (!AutismLiteVariant.enabled() && BacktrackModule.init_Z_AABB_2(box)) {
         return false;
      }

      List<Entity> occupants = MC.level.getEntities((Entity)null, box);
      if (!occupants.isEmpty()) {
         this.init_V_BlockPos_List(cell, occupants);
      }

      for (Entity occupant : occupants) {
         if (!this.init_Z_Entity_BlockPos(occupant, cell)) {
            return false;
         }
      }

      return true;
   }

   private boolean write(BlockPos support) {
      return this.recentPlacements.init(support.above(), AutismSessionState.getInstance().getClientTick());
   }

   private void init_V_BlockPos_List(BlockPos cell, List<Entity> occupants) {
      if (this.recentPlacements.size() != 0) {
         for (Entity occupant : occupants) {
            if (occupant instanceof EndCrystal && !this.pendingAttacks.init(occupant, AutismSessionState.getInstance().getClientTick())) {
               this.recentPlacements.remove(cell);
               return;
            }
         }
      }
   }

   private void recordPlacement(BlockPos cell) {
      int tick = AutismSessionState.getInstance().getClientTick();
      this.pendingAttacks.init(cell, tick);
      this.recentPlacements.init(cell, tick, autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs());
   }

   private boolean init_Z_Entity_BlockPos(Entity occupant, BlockPos cell) {
      return this.bool("fast-replace") && occupant instanceof EndCrystal && this.pendingAttacks.init(occupant, cell, AutismSessionState.getInstance().getClientTick());
   }

   public static List<Entity> filterPlacementOccupants(UseOnContext context, List<Entity> occupants) {
      if (AutismLiteVariant.enabled()) {
         return occupants;
      }

      if (!occupants.isEmpty()
         && context.getLevel() == MC.level
         && context.getPlayer() == MC.player
         && autismclient.util.QueuedCombatAction.init("crystal-aura", context.getClickedPos())) {
         if (al.init_Module_Str("crystal-aura") instanceof CrystalAuraModule aura && aura.isEnabled() && aura.bool("fast-replace")) {
            BlockPos cell = context.getClickedPos().above();
            return occupants.stream().filter(entity -> !aura.init_Z_Entity_BlockPos(entity, cell)).toList();
         } else {
            return occupants;
         }
      } else {
         return occupants;
      }
   }

   private boolean run() {
      int tick = AutismSessionState.getInstance().getClientTick();
      int radius = Mth.clamp(Mth.ceil(MC.player.blockInteractionRange()), 0, 8);
      BlockPos origin = MC.player.blockPosition();

      for (EndCrystal dying : this.recentAttacks.activeKeys(tick)) {
         if (MC.level.getEntity(dying.getId()) == dying && withinRadius(origin, BlockPos.containing(dying.position()).below(), radius)) {
            return true;
         }
      }

      for (BlockPos cell : this.recentPlacements.activeKeys(tick)) {
         if (withinRadius(origin, cell.below(), radius)) {
            return true;
         }
      }

      return false;
   }

   private static boolean withinRadius(BlockPos origin, BlockPos support, int radius) {
      int dx = support.getX() - origin.getX();
      int dy = support.getY() - origin.getY();
      int dz = support.getZ() - origin.getZ();
      return dx * dx + dy * dy + dz * dz <= radius * radius;
   }

   private BlockHitResult placeHit(CrystalAuraModule.Inner_NetHelper plan, RotationMath.Rotation wire) {
      if (plan.hand() == null) {
         return null;
      } else if (plan.hand() == InteractionHand.OFF_HAND && autismclient.util.ClientInteractionOverride.isMainHandUseableItem()) {
         return null;
      } else {
         BlockPos support = plan.support();
         if (!this.read(support)) {
            return null;
         } else {
            AABB nextBox = MC.player.getBoundingBox().move(MC.player.getDeltaMovement());
            if (this.init_Z_BlockPos_AABB_3(support, nextBox) && !this.write(support)) {
               return !isEndCrystal(MC.player.getItemInHand(plan.hand()))
                  ? null
                  : autismclient.util.BlockPlacementPlanner.verifyWithRotation(
                     plan.placement(), wire, MC.player.getEyePosition(), plan.range(), plan.request()
                  );
            } else {
               return null;
            }
         }
      }
   }

   private boolean executePlace(CrystalAuraModule.Inner_NetHelper plan, BlockHitResult hit) {
      if (hit == null) {
         return false;
      } else {
         InteractionHand hand = plan.hand();
         LivingEntity enemy = this.target;
         if (al.init_Z_HitResult(hit, hand, "crystal-aura")) {
            return false;
         } else if (!autismclient.util.QueuedCombatAction.init(
            "crystal-aura",
            hit,
            hand,
            () -> this.push()
               && this.serverRotation() != null
               && this.placeHit(plan, this.serverRotation()) != null
               && this.init_Z_LivingEntity_3(
                  enemy, new Vec3(plan.support().getX() + 0.5, plan.support().getY() + 1.0, plan.support().getZ() + 0.5), true
               ),
            () -> this.onPlaceDispatched(plan)
         )) {
            return false;
         } else if (!PerTickPlacementLock.init("crystal-aura")) {
            autismclient.util.ClientInteractionOverride.clearAll();
            return false;
         } else {
            return true;
         }
      }
   }

   private void onPlaceDispatched(CrystalAuraModule.Inner_NetHelper plan) {
      this.showPreview(plan.support());
      this.clearPlans();
      this.reservedCrystalCell = plan.support().above();
      this.recordPlacement(this.reservedCrystalCell);
      this.recordAction(false);
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
      return autismclient.aggressivepvp.Versions.running(autismclient.aggressivepvp.Versions.crystal)
         ? false
         : this.cooldowns.init(AutismSessionState.getInstance().getClientTick());
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return autismclient.aggressivepvp.Versions.running(autismclient.aggressivepvp.Versions.crystal)
         ? false
         : hitResult instanceof EntityHitResult && "crystal-aura".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && autismclient.util.SilentRotationManager.hasActiveRotation();
   }

   private CrystalAuraModule.Inner_TaskQueue planBuilder(LivingEntity enemy, autismclient.util.BlockPlacementPlanner.RayBudget budget, double rivalDamage) {
      LocalPlayer player = MC.player;
      double range = player.blockInteractionRange();
      if (range <= 0.0) {
         return null;
      }

      boolean offhand = isObsidian(player.getOffhandItem()) && !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther("crystal-aura") && !autismclient.util.ClientInteractionOverride.isMainHandUseableItem();
      int slot = offhand ? -1 : this.init_I_30();
      if (!offhand && slot < 0) {
         return null;
      }

      InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
      ItemStack material = offhand ? player.getOffhandItem() : player.getInventory().getItem(slot);
      Vec3 eye = player.getEyePosition();
      BlockPos origin = player.blockPosition();
      int[] offsets = sphereOffsets(Mth.ceil(range));
      double floor = this.decimal("min-target-damage");
      double rangeSq = range * range;
      AABB nextBox = player.getBoundingBox().move(player.getDeltaMovement());
      AABB blastTarget = this.predictedHitbox(enemy, this.leadHorizonTicks(), 1.5);
      autismclient.util.ExplosionDamageCalculator.SimOptions targetOptions = this.simOptions.withTargetBox(blastTarget);
      BlockState obsidianState = Blocks.OBSIDIAN.defaultBlockState();
      List<autismclient.util.BlockPlacementPlanner.SupportCandidate> supportFaces = new ArrayList<>();
      AABB solidNextBox = autismclient.util.PlayerMotionEnvelope.init(player);
      List<CrystalAuraModule.Inner_DataUtil> candidates = new ArrayList<>();
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         Vec3 source = new Vec3(cursor.getX() + 0.5, cursor.getY() + 1.0, cursor.getZ() + 0.5);
         if ((!"Aimed".equals(this.choice("aim-mode")) || cursor.equals(this.init_BlockPos_8())) && (!this.bool("only-above") || !(source.y < enemy.getY()))) {
            if (this.read(cursor)) {
               double supportBound = autismclient.util.ExplosionDamageCalculator.calculateUpperBoundDamage(enemy, source, 6.0F, targetOptions);
               if (supportBound >= floor && this.init_Z_BlockPos_AABB_3(cursor, nextBox) && this.init_Z_Vec3_9(eye, cursor, source)) {
                  autismclient.util.ExplosionDamageCalculator.DamagePair supportRank = this.rankDamage(enemy, source, blastTarget);
                  if (this.meetsDamageThresholds(enemy, supportRank)) {
                     rivalDamage = Math.max(rivalDamage, supportRank.load());
                  }
               }
            } else if (MC.level.getBlockState(cursor).canBeReplaced()
               && !AnchorAuraModule.init_Z_BlockPos(cursor)
               && MC.level.isUnobstructed(obsidianState, cursor, CollisionContext.empty())
               && !solidNextBox.intersects(new AABB(cursor))
               && (AutismLiteVariant.enabled() || !BacktrackModule.init_Z_AABB_2(new AABB(cursor)))) {
               double distanceSq = eye.distanceToSqr(source);
               if (!(distanceSq > rangeSq)) {
                  double bound = autismclient.util.ExplosionDamageCalculator.calculateUpperBoundDamage(enemy, source, 6.0F, targetOptions);
                  if (!(bound < floor) && this.init_Z_BlockPos_AABB_3(cursor, nextBox) && !this.write(cursor)) {
                     supportFaces.clear();
                     autismclient.util.BlockPlacementPlanner.collectCandidates(this.init(cursor.immutable(), material, hand, range, eye, this.wireRotation()), supportFaces);
                     if (!supportFaces.isEmpty()) {
                        candidates.add(new CrystalAuraModule.Inner_DataUtil(cursor.immutable(), source, bound, distanceSq));
                     }
                  }
               }
            }
         }
      }

      if (candidates.isEmpty()) {
         return null;
      }

      candidates.sort(Comparator.comparingDouble(CrystalAuraModule.Inner_DataUtil::bound).reversed());
      List<CrystalAuraModule.Inner_StateCache> accepted = new ArrayList<>();
      double bestDamage = -1.0;
      int evaluated = 0;

      for (CrystalAuraModule.Inner_DataUtil candidate : candidates) {
         if (accepted.size() >= 4 && candidate.bound() <= bestDamage || evaluated >= 32) {
            break;
         }

         evaluated++;
         autismclient.util.ExplosionDamageCalculator.DamagePair rank = autismclient.util.TargetLeadDamageEstimator.init(
            enemy, candidate.source(), 6.0F, this.simOptions.withPlacedObsidian(candidate.support()), blastTarget
         );
         if (this.meetsDamageThresholds(enemy, rank) && this.isSafeForFriends(candidate.source())) {
            accepted.add(new CrystalAuraModule.Inner_StateCache(candidate.support(), rank, candidate.load()));
            bestDamage = Math.max(bestDamage, rank.load());
         }
      }

      if (accepted.isEmpty()) {
         return null;
      }

      accepted.sort((first, second) -> {
         int byBucket = Double.compare(damageBucket(second.rank().load()), damageBucket(first.rank().load()));
         if (byBucket != 0) {
            return byBucket;
         }

         int bySelf = Double.compare(first.rank().selfDamage(), second.rank().selfDamage());
         return bySelf != 0 ? bySelf : Double.compare(second.init_D(), first.init_D());
      });
      double builderBest = accepted.get(0).rank().load();
      if (rivalDamage >= 0.0 && !builderBeatsRival(rivalDamage, builderBest)) {
         return null;
      }

      RotationMath.Rotation wire = this.wireRotation();
      autismclient.util.BlockPlacementPlanner.RejectReason[] refusal = new autismclient.util.BlockPlacementPlanner.RejectReason[1];
      int limit = Math.min(4, accepted.size());

      for (int i = 0; i < limit; i++) {
         BlockPos cell = accepted.get(i).cell();
         autismclient.util.BlockPlacementPlanner.Query request = this.init(cell, material, hand, range, eye, wire).withRayBudget(budget);
         autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.plan(request, refusal);
         if (candidate != null) {
            InteractionHand resolved = offhand ? InteractionHand.OFF_HAND : this.handForSlot(slot);
            return new CrystalAuraModule.Inner_TaskQueue(cell, resolved, range, candidate, accepted.get(i).rank().load(), slot);
         }

         if (refusal[0] == autismclient.util.BlockPlacementPlanner.RejectReason.NO_BUDGET) {
            return null;
         }

         refusal[0] = null;
      }

      return null;
   }

   private static boolean builderBeatsRival(double rivalDamage, double builderDamage) {
      return builderDamage >= rivalDamage + 5.0 && builderDamage >= rivalDamage * 2.0;
   }

   private autismclient.util.BlockPlacementPlanner.Query init(BlockPos cell, ItemStack stack, InteractionHand hand, double range, Vec3 eye, RotationMath.Rotation wire) {
      return new autismclient.util.BlockPlacementPlanner.Query(cell, eye, range, autismclient.util.BlockPlacementPlanner.blockItemValidator(stack, MC.player, hand))
         .withFromRotation(wire)
         .withMaxPitch(autismclient.util.BlockPlacementPlanner.defaultMaxPitchDegrees())
         .withLeadEye(eye.add(MC.player.getDeltaMovement()))
         .withSneaking(MC.player.isSecondaryUseActive())
         .withAutoSneak(false);
   }

   private BlockHitResult builderHit(CrystalAuraModule.Inner_TaskQueue plan, RotationMath.Rotation wire) {
      if (!AutismLiteVariant.enabled() && BacktrackModule.init_Z_AABB_2(new AABB(plan.cell()))) {
         return null;
      }

      if (plan.hand() == null) {
         return null;
      }

      if (plan.hand() == InteractionHand.OFF_HAND && autismclient.util.ClientInteractionOverride.isMainHandUseableItem()) {
         return null;
      }

      BlockPos cell = plan.cell();
      if (autismclient.util.PlayerMotionEnvelope.init(MC.player).intersects(new AABB(cell))) {
         return null;
      }

      if (!MC.level.getBlockState(cell).canBeReplaced()) {
         return null;
      }

      if (!MC.level.isUnobstructed(Blocks.OBSIDIAN.defaultBlockState(), cell, CollisionContext.empty())) {
         return null;
      }

      ItemStack held = MC.player.getItemInHand(plan.hand());
      if (!isObsidian(held)) {
         return null;
      }

      Vec3 eye = MC.player.getEyePosition();
      return autismclient.util.BlockPlacementPlanner.verifyWithRotation(
         plan.placement(), wire, eye, plan.range(), this.init(cell, held, plan.hand(), plan.range(), eye, this.wireRotation())
      );
   }

   private boolean executeBuilder(CrystalAuraModule.Inner_TaskQueue plan, BlockHitResult hit) {
      return this.executeBuilder(plan, hit, () -> {});
   }

   private boolean executeBuilder(CrystalAuraModule.Inner_TaskQueue plan, BlockHitResult hit, Runnable dispatched) {
      if (hit == null) {
         return false;
      } else {
         InteractionHand hand = plan.hand();
         if (al.init_Z_HitResult(hit, hand, "crystal-aura")) {
            return false;
         } else if (!autismclient.util.QueuedCombatAction.init(
            "crystal-aura",
            hit,
            hand,
            () -> this.push() && this.serverRotation() != null && this.builderHit(plan, this.serverRotation()) != null,
            () -> {
               this.onBuilderDispatched(plan);
               dispatched.run();
            }
         )) {
            return false;
         } else if (!PerTickPlacementLock.init("crystal-aura")) {
            autismclient.util.ClientInteractionOverride.clearAll();
            return false;
         } else {
            return true;
         }
      }
   }

   private void onBuilderDispatched(CrystalAuraModule.Inner_TaskQueue plan) {
      this.unverifiedObsidianPos = plan.cell().immutable();
      this.showPreview(plan.cell());
      this.clearPlans();
      this.reservedCrystalCell = plan.cell().above();
      this.init_Map.put(plan.cell().immutable(), AutismSessionState.getInstance().getClientTick() + 40);

      while (this.init_Map.size() > 16) {
         this.init_Map.remove(this.init_Map.keySet().iterator().next());
      }

      this.recordAction(false);
   }

   private boolean update(BlockPos cell) {
      Integer until = this.init_Map.get(cell);
      if (until == null) {
         return false;
      } else if (AutismSessionState.getInstance().getClientTick() - until >= 0) {
         this.init_Map.remove(cell);
         return false;
      } else {
         return true;
      }
   }

   private static boolean isObsidian(ItemStack stack) {
      return !stack.isEmpty() && stack.is(Items.OBSIDIAN);
   }

   private int init_I_30() {
      LocalPlayer player = MC.player;
      int selected = player.getInventory().getSelectedSlot();
      int best = -1;
      int bestSteps = 2147483647;

      for (int slot = 0; slot < 9; slot++) {
         if (isObsidian(player.getInventory().getItem(slot)) && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, "crystal-aura")) {
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

   private InteractionHand handForSlot(int slot) {
      return this.handForSlot(slot, false);
   }

   private InteractionHand handForSlot(int slot, boolean defensive) {
      LocalPlayer player = MC.player;
      if (player.getInventory().getSelectedSlot() == slot) {
         if (auditBeforeDeadline(this.auditHasUpdateDeadline, this.switchDeadline)) {
            return null;
         }

         this.prev = this.integer("switch-back-delay");
         return InteractionHand.MAIN_HAND;
      } else {
         int held = player.getInventory().getSelectedSlot();
         if (this.init_Z_I_Z_3(slot, defensive)) {
            if (this.stop < 0) {
               this.stop = held;
            }

            this.next = slot;
         }

         this.prev = this.integer("switch-back-delay");
         return player.getInventory().getSelectedSlot() == slot && !auditBeforeDeadline(this.auditHasUpdateDeadline, this.switchDeadline) ? InteractionHand.MAIN_HAND : null;
      }
   }

   private CrystalAuraModule.Inner_TaskQueue planDefense(autismclient.util.BlockPlacementPlanner.RayBudget budget, boolean imminentOnly) {
      if (!this.bool("defend")) {
         return null;
      }

      LocalPlayer player = MC.player;
      if (!SurroundModule.init_Z_27() && !BedDefenderModule.init_Z_10()) {
         if (!imminentOnly && this.pop()) {
            return null;
         }

         int tick = AutismSessionState.getInstance().getClientTick();
         int cooldownAge = tick - this.pool;
         if (cooldownAge >= 0 && cooldownAge < 2) {
            return null;
         }

         int windowAge = tick - this.index;
         if (windowAge < 0 || windowAge >= 20) {
            this.index = tick;
            this.count = 0;
         }

         if (this.count >= 3) {
            return null;
         }

         Player enemy = this.findPlayerTarget();
         if (enemy == null) {
            return null;
         }

         double range = player.blockInteractionRange();
         if (range <= 0.0) {
            return null;
         }

         List<CrystalAuraModule.Inner_CachePool> threats = this.crystalThreats(player, enemy, imminentOnly);
         if (threats.isEmpty()) {
            return null;
         }

         boolean offhand = isObsidian(player.getOffhandItem()) && !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther("crystal-aura") && !autismclient.util.ClientInteractionOverride.isMainHandUseableItem();
         int slot = offhand ? -1 : this.init_I_30();
         if (!offhand && slot < 0) {
            return null;
         }

         InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
         ItemStack material = offhand ? player.getOffhandItem() : player.getInventory().getItem(slot);
         Vec3 eye = player.getEyePosition();
         double rangeSq = range * range;
         AABB playerBox = player.getBoundingBox();
         Vec3 delta = player.getDeltaMovement();
         AABB nextBox = autismclient.util.PlayerMotionEnvelope.init(player);
         double worst = threats.get(0).damageBound();
         LinkedHashSet<BlockPos> candidates = new LinkedHashSet<>();
         List<List<BlockPos>> rays = threats.stream().map(threatx -> autismclient.util.BlastShieldPosFinder.init(playerBox, nextBox, threatx.source())).toList();

         for (int i = 0; i < 24 && candidates.size() < 24; i++) {
            for (List<BlockPos> ray : rays) {
               if (i < ray.size() && candidates.size() < 24) {
                  candidates.add(ray.get(i));
               }
            }
         }

         List<CrystalAuraModule.Inner_StreamUtil> cells = new ArrayList<>();

         for (BlockPos cell : candidates) {
            if (!new AABB(cell).intersects(playerBox.move(delta.scale(-1.0))) && this.canPlaceObsidian(cell, eye, rangeSq)) {
               double after = 0.0;

               for (CrystalAuraModule.Inner_CachePool threat : threats) {
                  after = Math.max(after, autismclient.util.ExplosionDamageCalculator.calculateDamageCached(player, threat.source(), threat.power(), threat.simOptions().withPlacedObsidian(cell)));
               }

               double reduction = worst - after;
               if (reduction >= 2.0) {
                  cells.add(new CrystalAuraModule.Inner_StreamUtil(cell, reduction));
               }
            }
         }

         if (cells.isEmpty()) {
            return null;
         }

         cells.sort(Comparator.comparingDouble(CrystalAuraModule.Inner_StreamUtil::reduction).reversed());
         return this.init(cells, material, hand, slot, range, eye, budget);
      } else {
         return null;
      }
   }

   private CrystalAuraModule.Inner_TaskQueue init_t_TaskQueue_LivingEntity_U_StreamUtil(LivingEntity enemy, autismclient.util.BlockPlacementPlanner.RayBudget budget) {
      if (this.bool("defend")
         && this.bool("place")
         && this.put()
         && !SurroundModule.init_Z_27()
         && !BedDefenderModule.init_Z_10()
         && !AnchorAuraModule.read_Z()
         && !this.scoredCandidates.isEmpty()) {
         int tick = AutismSessionState.getInstance().getClientTick();
         if (this.chunk != tick) {
            return null;
         }

         if (tick - this.pool >= 0 && tick - this.pool < 2) {
            return null;
         }

         if (tick - this.index < 0 || tick - this.index >= 20) {
            this.index = tick;
            this.count = 0;
         }

         if (this.count >= 3) {
            return null;
         }

         LocalPlayer player = MC.player;
         boolean offhand = isObsidian(player.getOffhandItem()) && !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther("crystal-aura") && !autismclient.util.ClientInteractionOverride.isMainHandUseableItem();
         int slot = offhand ? -1 : this.init_I_30();
         if (!offhand && slot < 0) {
            return null;
         }

         InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
         ItemStack stack = offhand ? player.getOffhandItem() : player.getInventory().getItem(slot);
         Vec3 eye = player.getEyePosition();
         double range = player.blockInteractionRange();
         AABB next = autismclient.util.PlayerMotionEnvelope.init(player);
         AABB targetBox = this.predictedHitbox(enemy, this.leadHorizonTicks(), 1.5);
         ArrayList<CrystalAuraModule.Inner_StreamUtil> viable = new ArrayList<>();

         for (CrystalAuraModule.Inner_StateCache support : this.scoredCandidates) {
            Vec3 source = Vec3.atBottomCenterOf(support.cell().above());
            int tested = 0;

            for (BlockPos cell : autismclient.util.BlastShieldPosFinder.init(player.getBoundingBox(), next, source)) {
               if (!cell.equals(support.cell())
                  && !cell.equals(support.cell().above())
                  && !new AABB(cell).clip(eye, source).isPresent()
                  && this.canPlaceObsidian(cell, eye, range * range)) {
                  if (++tested > 6) {
                     break;
                  }

                  autismclient.util.ExplosionDamageCalculator.SimOptions covered = this.simOptions.withPlacedObsidian(cell);
                  autismclient.util.ExplosionDamageCalculator.DamagePair rank = autismclient.util.TargetLeadDamageEstimator.init(enemy, source, 6.0F, covered, targetBox);
                  if (this.meetsDamageThresholds(enemy, rank)
                     && autismclient.util.BlastShieldPosFinder.init(
                        support.rank().load(),
                        support.rank().selfDamage(),
                        rank.load(),
                        rank.selfDamage(),
                        this.decimal("min-target-damage"),
                        this.decimal("max-self-damage"),
                        this.bool("efficient")
                     )
                     && (!this.bool("protect-friends") || autismclient.util.WeaponDamageCalculator.init(source, 6.0F, covered, this.decimal("max-friend-damage")))) {
                     viable.add(new CrystalAuraModule.Inner_StreamUtil(cell, rank.load() - rank.selfDamage()));
                  }
               }
            }
         }

         viable.sort(Comparator.comparingDouble(CrystalAuraModule.Inner_StreamUtil::reduction).reversed());
         return this.init(viable, stack, hand, slot, range, eye, budget);
      } else {
         return null;
      }
   }

   private boolean canPlaceObsidian(BlockPos cell, Vec3 eye, double rangeSq) {
      if (MC.level.getBlockState(cell).canBeReplaced()
         && !AnchorAuraModule.init_Z_BlockPos(cell)
         && MC.level.isUnobstructed(Blocks.OBSIDIAN.defaultBlockState(), cell, CollisionContext.empty())
         && !(eye.distanceToSqr(Vec3.atCenterOf(cell)) > rangeSq)) {
         if (!AutismLiteVariant.enabled() && BacktrackModule.init_Z_AABB_2(new AABB(cell))) {
            return false;
         }

         for (Direction face : Direction.values()) {
            BlockPos support = cell.relative(face);
            if (autismclient.util.BlockPlacementPlanner.isValidSupportBlock(MC.level.getBlockState(support), support, false)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private CrystalAuraModule.Inner_TaskQueue init(
      List<CrystalAuraModule.Inner_StreamUtil> cells, ItemStack material, InteractionHand hand, int slot, double range, Vec3 eye, autismclient.util.BlockPlacementPlanner.RayBudget budget
   ) {
      autismclient.util.BlockPlacementPlanner.RejectReason[] refusal = new autismclient.util.BlockPlacementPlanner.RejectReason[1];
      int limit = Math.min(4, cells.size());

      for (int i = 0; i < limit; i++) {
         CrystalAuraModule.Inner_StreamUtil cell = cells.get(i);
         autismclient.util.BlockPlacementPlanner.Query request = this.init(cell.cell(), material, hand, range, eye, this.wireRotation())
            .withRayBudget(budget);
         autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.plan(request, refusal);
         if (candidate != null) {
            InteractionHand resolved = hand == InteractionHand.OFF_HAND ? hand : this.handForSlot(slot, true);
            return new CrystalAuraModule.Inner_TaskQueue(cell.cell(), resolved, range, candidate, cell.reduction(), slot);
         }

         if (refusal[0] == autismclient.util.BlockPlacementPlanner.RejectReason.NO_BUDGET) {
            return null;
         }

         refusal[0] = null;
      }

      return null;
   }

   private Player findPlayerTarget() {
      LocalPlayer player = MC.player;
      double range = this.decimal("target-range");
      AABB search = player.getBoundingBox().inflate(range);
      List<Player> found = MC.level
         .getEntitiesOfClass(
            Player.class,
            search,
            entityx -> entityx != player && entityx.isAlive() && !entityx.isSpectator() && !AntiBotTracker.shouldIgnoreEntity(entityx) && !TeamsModule.init(entityx, "killaura")
         );
      Vec3 eye = player.getEyePosition();
      double rangeSq = range * range;
      Player best = null;
      double bestDistanceSq = 1.7976931348623157E308;

      for (Player entity : found) {
         double distanceSq = distanceSqToBox(entity.getBoundingBox(), eye);
         if (!(distanceSq > rangeSq) && distanceSq < bestDistanceSq) {
            bestDistanceSq = distanceSq;
            best = entity;
         }
      }

      return best;
   }

   private List<CrystalAuraModule.Inner_CachePool> crystalThreats(LocalPlayer player, Player enemy, boolean imminentOnly) {
      double maximum = this.decimal("max-self-damage");
      ArrayList<CrystalAuraModule.Inner_CachePool> sources = new ArrayList<>();
      AABB near = player.getBoundingBox().inflate(5.0);

      for (EndCrystal crystal : MC.level.getEntitiesOfClass(EndCrystal.class, near, Entity::isAlive)) {
         if (!this.pendingAttacks.init(crystal, AutismSessionState.getInstance().getClientTick())) {
            double bound = autismclient.util.ExplosionDamageCalculator.calculateUpperBoundSelfDamage(crystal.position(), 6.0F, this.simOptions);
            if (bound > maximum) {
               sources.add(new CrystalAuraModule.Inner_CachePool(crystal.position(), 6.0F, this.simOptions, bound));
            }
         }
      }

      BlockPos origin = player.blockPosition();
      int[] offsets = sphereOffsets(Mth.ceil(5.0));
      AABB nextBox = player.getBoundingBox().move(player.getDeltaMovement());
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i + 2 < offsets.length; i += 3) {
         cursor.set(origin.getX() + offsets[i], origin.getY() + offsets[i + 1], origin.getZ() + offsets[i + 2]);
         BlockState state = MC.level.getBlockState(cursor);
         if (state.getBlock() instanceof RespawnAnchorBlock && state.getValue(RespawnAnchorBlock.CHARGE) > 0 && !AnchorAuraModule.init_Z_BlockPos(cursor)) {
            Boolean works = MC.level.environmentAttributes().getValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, cursor);
            if (!Boolean.TRUE.equals(works)) {
               BlockPos cell = cursor.immutable();
               autismclient.util.ExplosionDamageCalculator.SimOptions blast = this.simOptions.withExcludedBlocks(List.of(cell));
               Vec3 source = Vec3.atCenterOf(cell);
               double bound = autismclient.util.ExplosionDamageCalculator.calculateUpperBoundSelfDamage(source, 5.0F, blast);
               if (bound > maximum) {
                  sources.add(new CrystalAuraModule.Inner_CachePool(source, 5.0F, blast, bound));
               }
            }
         } else if (!imminentOnly
            && this.read(cursor)
            && MC.level.isEmptyBlock(cursor.above())
            && !this.update(cursor)
            && this.init_Z_BlockPos_AABB_3(cursor, nextBox)) {
            Vec3 source = new Vec3(cursor.getX() + 0.5, cursor.getY() + 1, cursor.getZ() + 0.5);
            Vec3 direction = enemy.position().subtract(player.position());
            Vec3 offset = source.subtract(player.position());
            if ((!(direction.horizontalDistanceSqr() > 0.25) || !(offset.x * direction.x + offset.z * direction.z <= 0.0))
               && !(enemy.getEyePosition().distanceToSqr(Vec3.atCenterOf(cursor)) > enemy.blockInteractionRange() * enemy.blockInteractionRange())) {
               double bound = autismclient.util.ExplosionDamageCalculator.calculateUpperBoundSelfDamage(source, 6.0F, this.simOptions);
               if (bound > maximum) {
                  sources.add(new CrystalAuraModule.Inner_CachePool(source, 6.0F, this.simOptions, bound));
               }
            }
         }
      }

      sources.sort(Comparator.comparingDouble(CrystalAuraModule.Inner_CachePool::damageBound).reversed());
      ArrayList<CrystalAuraModule.Inner_CachePool> threats = new ArrayList<>();

      for (int i = 0; i < Math.min(12, sources.size()); i++) {
         CrystalAuraModule.Inner_CachePool source = sources.get(i);
         double damage = autismclient.util.ExplosionDamageCalculator.calculateDamageCached(player, source.source(), source.power(), source.simOptions());
         if (damage > maximum) {
            threats.add(new CrystalAuraModule.Inner_CachePool(source.source(), source.power(), source.simOptions(), damage));
         }
      }

      threats.sort(Comparator.comparingDouble(CrystalAuraModule.Inner_CachePool::damageBound).reversed());
      return List.copyOf(threats.subList(0, Math.min(4, threats.size())));
   }

   private boolean load(CrystalAuraModule.Inner_TaskQueue plan, BlockHitResult hit) {
      return this.executeBuilder(plan, hit, () -> {
         this.pool = AutismSessionState.getInstance().getClientTick();
         this.count++;
      });
   }

   private void setBuilderPlan(LivingEntity enemy, CrystalAuraModule.Inner_TaskQueue plan) {
      this.plannedObsidianPos = plan.cell();
      this.data = plan.slot();
      this.obsidianPlanTarget = enemy;
      this.plannedObsidianDamage = plan.load();
      this.value = AutismSessionState.getInstance().getClientTick() + 12;
      this.plannedSupportPos = null;
   }

   private void setSupportPlan(LivingEntity enemy, CrystalAuraModule.Inner_NetHelper plan) {
      this.plannedSupportPos = plan.support();
      this.supportPlanTarget = enemy;
      this.stickySelector.init();
      this.cache = AutismSessionState.getInstance().getClientTick() + 6;
      this.plannedObsidianPos = null;
   }

   private void showPreview(BlockPos support) {
      this.previewSupportPos = support.immutable();
      this.previewCrystalPos = support.above().immutable();
      this.state = AutismSessionState.getInstance().getClientTick() + 10;
   }

   private void clearPlans() {
      this.plannedObsidianPos = null;
      this.plannedSupportPos = null;
      this.obsidianPlanTarget = null;
      this.supportPlanTarget = null;
      this.stickySelector.init();
   }

   private boolean read(LivingEntity enemy) {
      if (this.plannedObsidianPos == null) {
         return false;
      }

      if (AutismSessionState.getInstance().getClientTick() - this.value <= 0
         && enemy == this.obsidianPlanTarget
         && this.bool("place")
         && this.bool("obsidian")
         && this.put()
         && MC.level.getBlockState(this.plannedObsidianPos).canBeReplaced()
         && MC.level.isUnobstructed(Blocks.OBSIDIAN.defaultBlockState(), this.plannedObsidianPos, CollisionContext.empty())
         && this.init_Z_BlockPos_AABB_3(this.plannedObsidianPos, MC.player.getBoundingBox().move(MC.player.getDeltaMovement()))
         && this.get()) {
         Vec3 heldSource = new Vec3(this.plannedObsidianPos.getX() + 0.5, this.plannedObsidianPos.getY() + 1.0, this.plannedObsidianPos.getZ() + 0.5);
         if (this.meetsDamageThresholds(
               enemy,
               autismclient.util.TargetLeadDamageEstimator.init(
                  enemy, heldSource, 6.0F, this.simOptions.withPlacedObsidian(this.plannedObsidianPos), this.predictedHitbox(enemy, this.leadHorizonTicks(), 1.5)
               )
            )
            && this.isSafeForFriends(heldSource)) {
            BlockPos playerPos = MC.player.blockPosition();
            BlockPos enemyPos = enemy.blockPosition();
            if (!playerPos.equals(this.builderPlayerPos) || !enemyPos.equals(this.builderEnemyPos)) {
               this.builderPlayerPos = playerPos.immutable();
               this.builderEnemyPos = enemyPos.immutable();
               List<BlockPos> supports = this.findSupports(enemy, MC.player.blockInteractionRange());
               if (!supports.isEmpty()) {
                  this.level = enemy.getId();
                  this.bestBuilderSupport = supports.get(0);
                  this.frame = 0;
                  this.plannedObsidianPos = null;
                  return false;
               }
            }

            return true;
         } else {
            this.plannedObsidianPos = null;
            return false;
         }
      } else {
         this.plannedObsidianPos = null;
         return false;
      }
   }

   private boolean write(LivingEntity enemy) {
      if (this.bestBuilderSupport != null && enemy.getId() == this.level) {
         if (this.read(this.bestBuilderSupport)
            && this.init_Z_BlockPos_AABB_3(this.bestBuilderSupport, MC.player.getBoundingBox().move(MC.player.getDeltaMovement()))
            && this.meetsDamageThresholds(
               enemy,
               this.rankDamage(
                  enemy,
                  new Vec3(this.bestBuilderSupport.getX() + 0.5, this.bestBuilderSupport.getY() + 1.0, this.bestBuilderSupport.getZ() + 0.5),
                  this.predictedHitbox(enemy, this.leadHorizonTicks(), 1.5)
               )
            )) {
            return true;
         }

         this.bestBuilderSupport = null;
         return false;
      } else {
         return false;
      }
   }

   private boolean get() {
      LocalPlayer player = MC.player;
      return this.data < 0
         ? isObsidian(player.getOffhandItem()) && !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther("crystal-aura")
         : !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(this.data, "crystal-aura") && isObsidian(player.getInventory().getItem(this.data));
   }

   private CrystalAuraModule.Inner_TaskQueue init_t_TaskQueue_U_StreamUtil(autismclient.util.BlockPlacementPlanner.RayBudget budget) {
      LocalPlayer player = MC.player;
      double range = player.blockInteractionRange();
      if (range <= 0.0) {
         return null;
      }

      Vec3 eye = player.getEyePosition();
      boolean offhand = this.data < 0;
      InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
      ItemStack material = offhand ? player.getOffhandItem() : player.getInventory().getItem(this.data);
      autismclient.util.BlockPlacementPlanner.Query request = this.init(this.plannedObsidianPos, material, hand, range, eye, this.wireRotation())
         .withRayBudget(budget);
      autismclient.util.BlockPlacementPlanner.RejectReason[] refusal = new autismclient.util.BlockPlacementPlanner.RejectReason[1];
      autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.plan(request, refusal);
      if (candidate == null) {
         if (refusal[0] != autismclient.util.BlockPlacementPlanner.RejectReason.NO_BUDGET) {
            this.plannedObsidianPos = null;
         }

         return null;
      } else {
         InteractionHand resolved = offhand ? InteractionHand.OFF_HAND : this.handForSlot(this.data);
         return new CrystalAuraModule.Inner_TaskQueue(this.plannedObsidianPos, resolved, range, candidate, this.plannedObsidianDamage, this.data);
      }
   }

   private boolean update(LivingEntity enemy) {
      if (this.plannedSupportPos == null) {
         return false;
      }

      if (AutismSessionState.getInstance().getClientTick() - this.cache <= 0
         && enemy == this.supportPlanTarget
         && this.bool("place")
         && this.put()
         && this.read(this.plannedSupportPos)
         && this.init_Z_BlockPos_AABB_3(this.plannedSupportPos, MC.player.getBoundingBox().move(MC.player.getDeltaMovement()))) {
         autismclient.util.ExplosionDamageCalculator.DamagePair rank = this.rankDamage(
            enemy,
            new Vec3(this.plannedSupportPos.getX() + 0.5, this.plannedSupportPos.getY() + 1.0, this.plannedSupportPos.getZ() + 0.5),
            this.predictedHitbox(enemy, this.leadHorizonTicks(), 1.5)
         );
         if (this.meetsDamageThresholds(enemy, rank)
            && this.isSafeForFriends(new Vec3(this.plannedSupportPos.getX() + 0.5, this.plannedSupportPos.getY() + 1.0, this.plannedSupportPos.getZ() + 0.5))) {
            return true;
         }

         this.plannedSupportPos = null;
         return false;
      } else {
         this.plannedSupportPos = null;
         return false;
      }
   }

   private CrystalAuraModule.Inner_NetHelper load(LivingEntity enemy, autismclient.util.BlockPlacementPlanner.RayBudget budget) {
      LocalPlayer player = MC.player;
      double range = player.blockInteractionRange();
      if (range <= 0.0) {
         return null;
      }

      Vec3 eye = player.getEyePosition();
      RotationMath.Rotation wire = this.wireRotation();
      BlockPos support = this.plannedSupportPos;
      BlockPos cell = support.above();
      BlockState state = MC.level.getBlockState(support);
      autismclient.util.BlockPlacementPlanner.Query request = new autismclient.util.BlockPlacementPlanner.Query(cell, eye, range, autismclient.util.BlockPlacementPlanner.anchorFaceValidator(support, ALL_DIRECTIONS))
         .withFromRotation(wire)
         .withLeadEye(eye.add(player.getDeltaMovement()))
         .withRayBudget(budget);
      List<autismclient.util.BlockPlacementPlanner.SupportCandidate> scan = new ArrayList<>();
      this.init(support, state, cell, eye, wire, scan);
      autismclient.util.BlockPlacementPlanner.RejectReason[] refusal = new autismclient.util.BlockPlacementPlanner.RejectReason[1];
      int start = this.stickySelector.init(scan, optionx -> optionx);

      for (int step = 0; step < scan.size(); step++) {
         autismclient.util.BlockPlacementPlanner.SupportCandidate option = scan.get((start + step) % scan.size());
         autismclient.util.BlockPlacementPlanner.AimSample aim = autismclient.util.BlockPlacementPlanner.computeAimSample(option, request, refusal);
         if (aim != null) {
            autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.verifyPlacement(option, aim, request, refusal);
            if (candidate != null) {
               this.stickySelector.init();
               InteractionHand hand = this.crystalHand();
               Vec3 source = new Vec3(support.getX() + 0.5, support.getY() + 1.0, support.getZ() + 0.5);
               double damage = this.rankDamage(enemy, source, this.predictedHitbox(enemy, this.leadHorizonTicks(), 1.5)).load();
               return new CrystalAuraModule.Inner_NetHelper(support, hand, range, candidate, request, damage);
            }

            if (refusal[0] == autismclient.util.BlockPlacementPlanner.RejectReason.NO_BUDGET) {
               this.stickySelector.init(option);
               return null;
            }
         }
      }

      this.plannedSupportPos = null;
      this.stickySelector.init();
      return null;
   }

   private static boolean isEndCrystal(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() == Items.END_CRYSTAL;
   }

   private BlockPos init_BlockPos_8() {
      if (this.aimedHit instanceof BlockHitResult hit && hit.getType() == Type.BLOCK) {
         return MC.level.getBlockState(hit.getBlockPos()).canBeReplaced() ? hit.getBlockPos() : hit.getBlockPos().relative(hit.getDirection());
      } else {
         return null;
      }
   }

   private boolean set() {
      return this.valid != AutismSessionState.getInstance().getClientTick()
         && !auditBeforeDeadline(this.auditHasUpdateDeadline, this.switchDeadline)
         && autismclient.util.WeaponDamageCalculator.init(MC.player, MC.player.getMainHandItem()) > 0.0;
   }

   private void applyAntiWeakness() {
      if (!this.set() && this.bool("anti-weakness") && this.valid != AutismSessionState.getInstance().getClientTick()) {
         int best = -1;
         double bestDamage = 0.0;

         for (int slot = 0; slot < 9; slot++) {
            if (!autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, "crystal-aura")) {
               double damage = autismclient.util.WeaponDamageCalculator.init(MC.player, MC.player.getInventory().getItem(slot));
               if (damage > bestDamage) {
                  best = slot;
                  bestDamage = damage;
               }
            }
         }

         if (best < 0) {
            this.attackStage = "weakness";
         } else {
            int held = MC.player.getInventory().getSelectedSlot();
            if (best != held && this.load_Z_I_6(best)) {
               if (this.stop < 0) {
                  this.stop = held;
               }

               this.next = best;
            }

            this.prev = this.integer("switch-back-delay");
         }
      }
   }

   private boolean isSafeForFriends(Vec3 source) {
      return !this.bool("protect-friends") || autismclient.util.WeaponDamageCalculator.init(source, 6.0F, this.simOptions, this.decimal("max-friend-damage"));
   }

   private boolean put() {
      LocalPlayer player = MC.player;
      return isEndCrystal(player.getOffhandItem()) && !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther("crystal-aura") ? true : this.load_I_16() >= 0;
   }

   private InteractionHand crystalHand() {
      LocalPlayer player = MC.player;
      if (isEndCrystal(player.getOffhandItem()) && !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther("crystal-aura") && !autismclient.util.ClientInteractionOverride.isMainHandUseableItem()) {
         return InteractionHand.OFF_HAND;
      }

      if (isEndCrystal(player.getMainHandItem())) {
         if (auditBeforeDeadline(this.auditHasUpdateDeadline, this.switchDeadline)) {
            return null;
         }

         this.prev = this.integer("switch-back-delay");
         return InteractionHand.MAIN_HAND;
      } else {
         int slot = this.load_I_16();
         if (slot < 0) {
            return null;
         }

         int held = player.getInventory().getSelectedSlot();
         if (this.load_Z_I_6(slot)) {
            if (this.stop < 0) {
               this.stop = held;
            }

            this.next = slot;
         }

         this.prev = this.integer("switch-back-delay");
         return player.getInventory().getSelectedSlot() == slot && !auditBeforeDeadline(this.auditHasUpdateDeadline, this.switchDeadline) ? InteractionHand.MAIN_HAND : null;
      }
   }

   private boolean load_Z_I_6(int slot) {
      return this.init_Z_I_Z_3(slot, false);
   }

   private boolean init_Z_I_Z_3(int slot, boolean defensive) {
      if (!BedDefenderModule.init_Z_10() && !SurroundModule.init_Z_27() && (this.pop() || (defensive ? !AnchorAuraModule.read_Z() : !AnchorAuraModule.init_Z_2()))) {
         int tick = AutismSessionState.getInstance().getClientTick();
         if (tick == this.valid) {
            return false;
         }

         if (!autismclient.util.InventorySlotArbiter.beginActionGroup("crystal-aura")) {
            return false;
         }

         try {
            autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, slot);
            this.switchDeadline = System.nanoTime() + this.integer("switch-delay") * 1000000L;
            this.auditHasUpdateDeadline = true;
         } finally {
            autismclient.util.InventorySlotArbiter.endActionGroup("crystal-aura");
         }

         this.valid = tick;
         return true;
      } else {
         return false;
      }
   }

   private int load_I_16() {
      LocalPlayer player = MC.player;
      int selected = player.getInventory().getSelectedSlot();
      int best = -1;
      int bestSteps = 2147483647;

      for (int slot = 0; slot < 9; slot++) {
         if (isEndCrystal(player.getInventory().getItem(slot)) && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, "crystal-aura")) {
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

   private void update_V_17() {
      if (this.stop >= 0 && MC.gui.screen() == null && MC.gui.overlay() == null) {
         if (this.next >= 0 && MC.player.getInventory().getSelectedSlot() != this.next) {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (!AnchorAuraModule.init_Z_I(selected) && !KillAuraModule.load_Z_I_3(selected)) {
               this.stop = -1;
               this.next = -1;
               this.prev = 0;
            }
         } else if (this.prev > 0) {
            this.prev--;
         } else if (!autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(this.stop, "crystal-aura") && this.load_Z_I_6(this.stop)) {
            this.stop = -1;
            this.next = -1;
         }
      }
   }

   private int leadHorizonTicks() {
      return Math.min(12, 2 + (int)Math.ceil(autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs() / 50.0));
   }

   private AABB predictedHitbox(LivingEntity enemy, int horizonTicks, double maxLead) {
      return this.leadEstimator.predictedBox(enemy, AutismSessionState.getInstance().getClientTick(), horizonTicks, maxLead);
   }

   private autismclient.util.ExplosionDamageCalculator.DamagePair rankDamage(LivingEntity enemy, Vec3 source, AABB targetBox) {
      return autismclient.util.TargetLeadDamageEstimator.init(enemy, source, 6.0F, this.simOptions, targetBox);
   }

   private boolean meetsDamageThresholds(LivingEntity enemy, autismclient.util.ExplosionDamageCalculator.DamagePair rank) {
      return autismclient.util.ExplosionDamageCalculator.wouldSelfDamagePopOrKill(rank.selfDamage())
         ? false
         : autismclient.util.WeaponDamageCalculator.init(rank.load(), rank.selfDamage(), this.decimal("min-target-damage"), this.decimal("max-self-damage"), this.bool("efficient"));
   }

   private boolean init_Z_LivingEntity_3(LivingEntity enemy, Vec3 source, boolean placing) {
      return enemy != null
         && enemy.isAlive()
         && MC.level.getEntity(enemy.getId()) == enemy
         && !TeamsModule.init(enemy, "killaura")
         && !AntiBotTracker.shouldIgnoreEntity(enemy)
         && this.meetsDamageThresholds(
            enemy,
            this.rankDamage(enemy, source, this.predictedHitbox(enemy, placing ? this.leadHorizonTicks() : 0, placing ? 1.5 : 0.5))
         )
         && this.isSafeForFriends(source);
   }

   private boolean isBetterRank(autismclient.util.ExplosionDamageCalculator.DamagePair candidate, autismclient.util.ExplosionDamageCalculator.DamagePair incumbent) {
      int byTarget = Double.compare(candidate.load(), incumbent.load());
      return byTarget != 0 ? byTarget > 0 : candidate.selfDamage() < incumbent.selfDamage();
   }

   private boolean init_Z_Vec3_9(Vec3 eye, BlockPos support, Vec3 source) {
      HitResult hit = MC.level.clip(new ClipContext(eye, source, Block.COLLIDER, Fluid.NONE, MC.player));
      return hit.getType() == Type.MISS ? true : hit instanceof BlockHitResult block && block.getBlockPos().equals(support);
   }

   private boolean isRayBlocked(Vec3 eye, Vec3 point) {
      HitResult hit = MC.level.clip(new ClipContext(eye, point, Block.COLLIDER, Fluid.NONE, MC.player));
      return hit.getType() != Type.MISS;
   }

   private static double distanceSqToBox(AABB box, Vec3 point) {
      double dx = Math.max(Math.max(box.minX - point.x, point.x - box.maxX), 0.0);
      double dy = Math.max(Math.max(box.minY - point.y, point.y - box.maxY), 0.0);
      double dz = Math.max(Math.max(box.minZ - point.z, point.z - box.maxZ), 0.0);
      return dx * dx + dy * dy + dz * dz;
   }

   private static int[] sphereOffsets(int radius) {
      int clamped = Mth.clamp(radius, 0, 8);
      if (clamped == add) {
         return cachedSphereOffsets;
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

      cachedSphereOffsets = flat;
      add = clamped;
      return flat;
   }

   private void refreshViewSettings() {
      if (!this.bool("crystal-view")) {
         crystalViewEnabled = false;
      } else {
         viewSize = (float)this.decimal("view-size");
         viewY = (float)this.decimal("view-y");
         viewSpin = (float)this.decimal("view-spin");
         viewBounce = (float)this.decimal("view-bounce");
         crystalViewEnabled = true;
      }
   }

   public static boolean open() {
      return crystalViewEnabled;
   }

   public static float init_F_9() {
      return viewSize;
   }

   public static float load_F_5() {
      return viewY;
   }

   public static float read_F_3() {
      return viewSpin;
   }

   public static float write_F_2() {
      return viewBounce;
   }

   private record Inner_BufferHelper(EndCrystal crystal, autismclient.util.ExplosionDamageCalculator.DamagePair rank) {
   }

   private record Inner_CachePool(Vec3 source, float power, autismclient.util.ExplosionDamageCalculator.SimOptions simOptions, double damageBound) {
   }

   private record Inner_ConfigStore(EndCrystal crystal, RotationMath.Rotation goal, double range) {
   }

   private record Inner_DataUtil(BlockPos support, Vec3 source, double bound, double load) {
   }

   private record Inner_JsonUtil(
      BlockPos support, autismclient.util.BlockPlacementPlanner.Query request, autismclient.util.BlockPlacementPlanner.SupportCandidate option
   ) {
   }

   private record Inner_NetHelper(
      BlockPos support,
      InteractionHand hand,
      double range,
      autismclient.util.BlockPlacementPlanner.PlacementPlan placement,
      autismclient.util.BlockPlacementPlanner.Query request,
      double load
   ) {
   }

   private record Inner_StateCache(BlockPos cell, autismclient.util.ExplosionDamageCalculator.DamagePair rank, double init_D) {
   }

   private record Inner_StreamUtil(BlockPos cell, double reduction) {
   }

   private record Inner_SyncManager(autismclient.util.BlockPlacementPlanner.SupportCandidate option, double turn) {
   }

   private record Inner_TaskQueue(
      BlockPos cell,
      InteractionHand hand,
      double range,
      autismclient.util.BlockPlacementPlanner.PlacementPlan placement,
      double load,
      int slot
   ) {
   }
}
