package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.util.AutismLiteVariant;
import autismclient.util.PerTickPlacementLock;
import autismclient.util.RemoteViewController;
import autismclient.util.RotationMath;
import autismclient.util.ServerRotationTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.SilentMovementCoordinator;
import autismclient.util.RegistryIdValidator;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.MacroExecutor;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.client.player.ClientInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.BedBlock;
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

public final class BedDefenderModule extends Module implements SilentMovementCoordinator.Participant {
   static final String WOOL_BLOCKS = Arrays.stream(DyeColor.values()).map(color -> "minecraft:" + color.getName() + "_wool").collect(Collectors.joining("|"));
   static final String DEFAULT_BLOCKS = "minecraft:end_stone|" + WOOL_BLOCKS + "|minecraft:oak_planks|minecraft:obsidian";
   private List<BedDefenderModule.Inner_CachePool> ghosts = List.of();
   private BedDefenderModule.Inner_ConfigStore layoutKey;
   private List<BedDefenderModule.Inner_TaskQueue> steps = List.of();
   private Set<BlockPos> visited = Set.of();
   private int currentLayer;
   private final ExternalBreakTracker externalBreaks = new ExternalBreakTracker();
   private final Map<BlockPos, Integer> init_Map = new ConcurrentHashMap<>();
   private final Set<BlockPos> load_Set = new HashSet<>();
   private Set<BlockPos> stepCells = Set.of();
   private final ConcurrentLinkedQueue<BedDefenderModule.Inner_NetHelper> blockEdits = new ConcurrentLinkedQueue<>();
   private DyeColor teamColor;
   private final int[] layerSlots = new int[6];
   private final Map<String, Set<Block>> layerBlockCache = new HashMap<>();
   private final Map<BlockPos, Set<BlockPos>> read_Map = new HashMap<>();
   private final Map<BlockPos, List<Vec3>> surveyRays = new HashMap<>();
   private final autismclient.util.BlockPlacementPlanner.RayBudget surveyRayBudget = new autismclient.util.BlockPlacementPlanner.RayBudget(48);
   private static final float init_F = 55.0F;
   private static final double init_D = 1.0;
   private static final int load_I = 6;
   private static final int read_I = 5;
   private static final int write_I = 16;
   private static final int update_I = 64;
   private static final int sync_I = 96;
   private static final int flush_I = 16;
   private static final int close_I = 4096;
   private static final int open_I = 20;
   private static final int poll = 8;
   private static final int push = 60;
   private static final int pop = 16;
   private static final int tick = 12;
   private static final double load_D = 0.5;
   private static final int run = 16;
   private final List<BlockPos> planCells = new ArrayList<>();
   private final Set<BlockPos> recentlyPlaced = new LinkedHashSet<>();
   private BlockPos cachedBed;
   private int ticksSinceRescan;
   private final Set<BlockPos> bedCells = new LinkedHashSet<>();
   private BlockPos candidateBed;
   private BlockPos candidatePartner;
   private int candidateConfirmCount;
   private final Map<BlockPos, BedDefenderModule.Inner_DataUtil> stalls = new LinkedHashMap<>();
   private int lastPlaceTick = -2147483648;
   private int unusedLastRotationTick = -2147483648;
   private boolean needsSneak;
   private int sneakDecisionTick = -2147483648;
   private int switchBackSlot = -1;
   private int switchedToSlot = -1;
   private int switchBackDelay;
   private final KillAuraModule.TickCachedFlag init_aa_JsonUtil = new KillAuraModule.TickCachedFlag();
   private final List<autismclient.util.BlockPlacementPlanner.SupportCandidate> supportCandidates = new ArrayList<>();
   private int[] candidateRanks = new int[0];
   private int optionCursor;
   private long init_J;
   private long load_J;
   private boolean load_Z;
   private final Map<autismclient.util.BlockPlacementPlanner.SupportCandidate, List<Vec3>> candidateRays = new HashMap<>();
   private final Map<autismclient.util.BlockPlacementPlanner.SupportCandidate, Integer> flush_Map = new HashMap<>();
   private final Map<BlockPos, List<autismclient.util.BlockPlacementPlanner.PlacementPlan>> confirmedPlans = new HashMap<>();
   private int raysThisTick;
   private BedDefenderModule.Inner_StreamUtil[] candidatePlans = new BedDefenderModule.Inner_StreamUtil[0];
   private int[] read_IArr = new int[0];
   private final autismclient.util.BlockPlacementPlanner.RayBudget tickRayBudget = new autismclient.util.BlockPlacementPlanner.RayBudget(16);
   private int lastCacheTick = -2147483648;
   private boolean rayBudgetExhausted;
   private final Map<BlockPos, Boolean> blockedCache = new LinkedHashMap<>();
   private String rejectTrace;
   private String blockListRaw;
   private Set<Block> blockListCache = Set.of();
   private final ColorSetting ghostTintSetting;
   private BlockPos lastPlacedCell;

   @Override
   public boolean onPacketReceive(Packet<?> packet) {
      if (packet instanceof ClientboundBlockUpdatePacket edit) {
         this.blockEdits.add(new BedDefenderModule.Inner_NetHelper(edit.getPos().immutable(), !edit.getBlockState().canBeReplaced()));
      } else if (packet instanceof ClientboundSectionBlocksUpdatePacket edits) {
         edits.runUpdates((pos, state) -> this.blockEdits.add(new BedDefenderModule.Inner_NetHelper(pos.immutable(), !state.canBeReplaced())));
      }

      while (this.blockEdits.size() > 8192) {
         this.blockEdits.poll();
      }

      return false;
   }

   @Override
   public boolean onPacketSend(Packet<?> packet) {
      if (packet instanceof ServerboundPlayerActionPacket action) {
         int now = AutismSessionState.getInstance().getClientTick();
         switch (action.getAction()) {
            case START_DESTROY_BLOCK:
               this.init_Map.put(action.getPos().immutable(), now + 2);
               break;
            case STOP_DESTROY_BLOCK:
               this.init_Map.put(action.getPos().immutable(), now + 10);
               break;
            case ABORT_DESTROY_BLOCK:
               this.init_Map.remove(action.getPos());
         }
      }

      return false;
   }

   public List<BedDefenderModule.Inner_CachePool> ghostCells() {
      return this.ghosts;
   }

   public int ghostTint() {
      return this.ghostTintSetting.get();
   }

   public BedDefenderModule() {
      super("bed-defender", "BedDefender", ae.read, "Walls in a bed with your hardest blocks.");
      this.add(new IntSetting("max-layers", "Max Layers", 1, 1, 5, 1).group("Bed").description("Shell depth around the bed"));
      this.add(new BoolSetting("auto-bed", "Auto Bed", true).group("Bed").description("Matches beds to name colour."));
      this.add(new BoolSetting("visualize", "Visualize", true).group("Render").description("Preview the remaining defence as ghost blocks"));
      this.add(new IntSetting("ghost-opacity", "Ghost Opacity", 20, 5, 80, 1).group("Render").visibleWhen(() -> this.bool("visualize")).unit("%"));
      this.ghostTintSetting = this.add(new ColorSetting("ghost-tint", "Tint", -18248).group("Render").visibleWhen(() -> this.bool("visualize")));
      this.add(new BoolSetting("prefer-sides", "Prefer Sides", true).group("Bed").description("Sides before the top"));
      this.add(new BoolSetting("defend-under", "Defend Under", true).group("Bed").description("Also fill under the bed"));
      this.add(
         new BoolSetting("top-riser", "Top Riser", true).group("Bed").visibleWhen(() -> this.integer("max-layers") == 1).description("Riser to reach the top")
      );
      this.add(new BoolSetting("require-sneak", "Require Sneak", false).group("Bed").description("Only while holding shift"));
      this.add(new IntSetting("rescan-interval", "Rescan Interval", 10, 1, 40, 1).group("Bed").unit("ticks").description("Ticks between full bed sweeps"));
      this.add(
         new ChoiceSetting("filter-mode", "Filter", "Whitelist", "Whitelist", "Blacklist")
            .group("Blocks")
            .visibleWhen(() -> !this.bool("layer-materials"))
            .description("How the list is applied")
      );
      this.add(
         RegistryListSetting.blocks("blocks", "Blocks", DEFAULT_BLOCKS)
            .group("Blocks")
            .visibleWhen(() -> !this.bool("layer-materials"))
            .description("Defence material")
      );
      this.add(
         new BoolSetting("layer-materials", "Layer Materials", false)
            .group("Blocks")
            .description("Choose an allowed block list for each layer instead of the shared filter")
      );

      for (int layer = 1; layer <= 5; layer++) {
         int number = layer;
         String defaults = layer == 1 ? "minecraft:obsidian" : (layer == 2 ? "minecraft:end_stone" : WOOL_BLOCKS);
         this.add(
            RegistryListSetting.blocks("layer-" + layer + "-blocks", "Layer " + layer + " Blocks", defaults)
               .group("Blocks")
               .visibleWhen(() -> this.bool("layer-materials") && this.integer("max-layers") >= number)
               .description("Only these blocks in layer " + layer + "; waits if none are available")
         );
      }

      this.add(new BoolSetting("allow-chests", "Allow Chests", false).group("Blocks").description("Also allow chests as material"));
      this.add(new BoolSetting("sneak-for-bed", "Sneak For Bed", true).group("Placing").description("Crouch to place on bed"));
      this.add(new BoolSetting("switch-back", "Switch Back", true).group("Placing").description("Return to your old slot"));
   }

   @Override
   public void onEnable() {
      if ("minecraft:obsidian|minecraft:end_stone|minecraft:ender_chest".equals(this.value("blocks"))) {
         this.setValue("blocks", DEFAULT_BLOCKS);
      }

      this.resetState();
   }

   @Override
   public void onOptionValueChanged(String settingId) {
      this.layerBlockCache.clear();
      this.blockListRaw = null;
      this.clearPlan();
      this.bedCells.clear();
      this.clearBedCandidate();
      this.cachedBed = null;
      this.ticksSinceRescan = 2147483647;
   }

   @Override
   public void onDisable() {
      this.resetState();
      autismclient.util.SilentRotationManager.releaseOwner(this.id());
   }

   @Override
   public void onGameLeft() {
      this.resetState();
      if (this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         autismclient.util.SilentRotationManager.resetAll();
      }
   }

   private void resetState() {
      this.clearPlan();
      this.recentlyPlaced.clear();
      this.externalBreaks.clear();
      this.init_Map.clear();
      this.load_Set.clear();
      this.stepCells = Set.of();
      this.blockEdits.clear();
      this.cachedBed = null;
      this.bedCells.clear();
      this.clearBedCandidate();
      this.clearTickCaches();
      this.stalls.clear();
      this.ticksSinceRescan = 2147483647;
      this.lastPlaceTick = -2147483648;
      this.needsSneak = false;
      this.switchBackSlot = -1;
      this.switchedToSlot = -1;
      this.switchBackDelay = 0;
      autismclient.util.InventorySlotArbiter.releaseAllClaims(this.id());
   }

   public static Input overrideInput(ClientInput source, Input original) {
      if (original != null && MC != null && MC.player != null && MC.player.input == source) {
         if (al.init_Module_Str("bed-defender") instanceof BedDefenderModule bed && bed.isEnabled()) {
            return bed.read_Z_5() && !original.shift()
               ? new Input(original.forward(), original.backward(), original.left(), original.right(), original.jump(), true, original.sprint())
               : original;
         } else {
            return original;
         }
      } else {
         return original;
      }
   }

   private boolean read_Z_5() {
      if (!this.needsSneak) {
         return false;
      } else if (this.sneakDecisionTick != AutismSessionState.getInstance().getClientTick()) {
         this.needsSneak = false;
         return false;
      } else {
         return this.sync_Z_2();
      }
   }

   private boolean sync_Z_2() {
      if (MC != null && MC.player != null) {
         return !MC.player.isPassenger() && !MC.player.getAbilities().flying
            ? AutismLiteVariant.enabled() || !PkgCls_a.init_Z()
            : false;
      } else {
         return false;
      }
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return autismclient.util.SilentRotationManager.hasActiveRotation() && this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId());
   }

   @Override
   public void tick() {
      if (MC != null && MC.player != null && !this.isEnabled() && this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         autismclient.util.SilentRotationManager.tick(this.id(), MC.player);
      }
   }

   @Override
   public String info() {
      if (this.bool("auto-bed") && this.teamColor == null) {
         return "No team colour";
      } else if (this.currentLayer > 0 && this.layerSlots[this.currentLayer] < 0) {
         return "L" + this.currentLayer + ": no blocks";
      } else if (!this.planCells.isEmpty()) {
         return Integer.toString(this.planCells.size());
      } else {
         return this.cachedBed != null ? "bed" : "";
      }
   }

   public static boolean init_Z_10() {
      if (!(al.init_Module_Str("bed-defender") instanceof BedDefenderModule bed)) {
         return false;
      } else {
         int var2 = AutismSessionState.getInstance().getClientTick() - bed.unusedLastRotationTick;
         return bed.isEnabled() && !autismclient.util.SilentRotationManager.isReturningToPlayer() && var2 >= 0 && var2 <= 5 && "bed-defender".equals(autismclient.util.SilentRotationManager.getActiveOwnerId());
      }
   }

   @Override
   public boolean isSilentMovementActive() {
      boolean enabled = this.isEnabled();
      return !SilentMovementCoordinator.isSafeWalkActive() && (autismclient.util.SilentRotationManager.isReturningToPlayer() || enabled && this.canAct());
   }

   private boolean canAct() {
      return this.canOperate() && !this.open_Z();
   }

   private boolean canOperate() {
      return MC != null
         && MC.player != null
         && MC.level != null
         && MC.getConnection() != null
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && !aD.init_Z()
         && !FreecamController.isActive()
         && !RemoteViewController.isActive()
         && (AutismLiteVariant.enabled() || !PkgCls_a.init_Z())
         && !MacroExecutor.isRunning()
         && !MC.player.isDeadOrDying()
         && !MC.player.isSpectator()
         && (!MC.player.isUsingItem() || KillAuraModule.holdsBlockHit())
         && (AutismLiteVariant.enabled() || !ClutchModule.isClutchEngaged() && !DangerModule.sync_Z_12())
         && !MC.player.isHandsBusy()
         && !SilentMovementCoordinator.isSafeWalkActive()
         && !ScaffoldModule.update_Z_5()
         && !AutoTotemModule.init_Z_6()
         && !AutoArmorModule.isClickPending();
   }

   private boolean open_Z() {
      return this.init_aa_JsonUtil.valueFor(AutismSessionState.getInstance().getClientTick(), this::holdsWeapon);
   }

   private boolean holdsWeapon() {
      if (MC != null && MC.player != null) {
         ItemStack mainHand = MC.player.getMainHandItem();
         return KillAuraModule.isThrowable(mainHand) ? true : mainHand.isEmpty() && KillAuraModule.isThrowable(MC.player.getOffhandItem());
      } else {
         return false;
      }
   }

   private void releaseRotation() {
      this.unusedLastRotationTick = -2147483648;
      if (MC != null && MC.player != null && this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         autismclient.util.SilentRotationManager.releaseOwner(this.id());
         autismclient.util.SilentRotationManager.tick(this.id(), MC.player);
      }
   }

   @Override
   public void preMovementTick() {
      this.needsSneak = false;
      if (MC != null && MC.player != null && MC.level != null && MC.getConnection() != null) {
         this.open_V();
         if (this.bool("require-sneak") && !MC.player.isShiftKeyDown()) {
            this.clearPlan();
            this.releaseRotation();
         } else {
            this.teamColor = this.resolveTeamColor();
            int slot = this.materialSlot(0);
            ItemStack material = slot < 0 ? ItemStack.EMPTY : MC.player.getInventory().getItem(slot);
            ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
            RotationMath.Rotation wireRotation = wire.initialized() ? new RotationMath.Rotation(wire.currentYaw(), wire.currentPitch()) : null;
            RotationMath.Rotation from = wireRotation != null ? wireRotation : RotationMath.ofPlayer(MC.player);
            this.resetPerTickCaches();
            this.buildPlan(material);
            slot = this.currentLayer == 0 ? -1 : this.layerSlots[this.currentLayer];
            material = slot < 0 ? ItemStack.EMPTY : MC.player.getInventory().getItem(slot);
            this.refreshPlanIfChanged(from, material);
            this.init_V_ce_DataUtil_ItemStack(from, material);
            this.init_V_I(slot, material, wireRotation, from);
         }
      } else {
         this.clearPlan();
         this.releaseRotation();
      }
   }

   private void init_V_ce_DataUtil_ItemStack(RotationMath.Rotation from, ItemStack material) {
      this.read_Map.clear();
      this.surveyRays.clear();
      this.confirmedPlans.clear();
      if (this.planCells.size() >= 2 && !material.isEmpty()) {
         ArrayList<BlockPos> surveyOrder = new ArrayList<>(this.planCells);
         surveyOrder.sort(Comparator.<BlockPos>comparingDouble(p -> p.distToCenterSqr(MC.player.getEyePosition())).reversed());
         Set<BlockPos> considered = new LinkedHashSet<>(surveyOrder.subList(0, Math.min(64, surveyOrder.size())));
         Map<BlockPos, List<Vec3>> rays = this.surveyRays;
         Map<BlockPos, List<autismclient.util.BlockPlacementPlanner.SupportCandidate>> buckets = new LinkedHashMap<>();

         for (BlockPos cell : considered) {
            buckets.put(cell, new ArrayList<>());
         }

         for (autismclient.util.BlockPlacementPlanner.SupportCandidate option : this.supportCandidates) {
            if (considered.contains(option.targetCell())) {
               buckets.get(option.targetCell()).add(option);
            }
         }

         this.surveyRayBudget.reset(48);
         autismclient.util.BlockPlacementPlanner.Query request = this.init_U_StateCache_ce_DataUtil(from, material, this.tick_Z_2() || this.shouldSneakForBed(), null)
            .withRayBudget(this.surveyRayBudget);
         boolean more = true;

         for (int offset = 0; more && this.surveyRayBudget.remaining() > 0; offset++) {
            more = false;

            for (Entry<BlockPos, List<autismclient.util.BlockPlacementPlanner.SupportCandidate>> entry : buckets.entrySet()) {
               if (offset < entry.getValue().size()) {
                  more = true;
                  List<Vec3> points = rays.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>());
                  if (points.size() < 4) {
                     autismclient.util.BlockPlacementPlanner.SupportCandidate option = entry.getValue().get(offset);
                     autismclient.util.BlockPlacementPlanner.AimSample aim = autismclient.util.BlockPlacementPlanner.computeAimSample(option, request.withTargetCell(option.targetCell()), null);
                     autismclient.util.BlockPlacementPlanner.PlacementPlan confirmed = this.init_U_CachePool_U_TaskQueue(option, aim, request, null);
                     if (confirmed != null) {
                        points.add(confirmed.hit().getLocation());
                        this.confirmedPlans.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>()).add(confirmed);
                     }

                     if (this.surveyRayBudget.remaining() == 0) {
                        break;
                     }
                  }
               }
            }
         }

         Map<BlockPos, Set<BlockPos>> postureBefore = new HashMap<>();
         Vec3 eye = MC.player.getEyePosition();
         Vec3 crouchEye = eye.add(0.0, Math.min(0.0F, MC.player.getEyeHeight(Pose.CROUCHING) - MC.player.getEyeHeight()), 0.0);
         int postureRays = 16;
         if (crouchEye.y < eye.y - 0.02) {
            for (BlockPos victim : considered) {
               List<Vec3> points = rays.get(victim);
               if (points != null && !points.isEmpty()) {
                  boolean stillVisible = false;
                  boolean examined = true;

                  for (Vec3 point : points) {
                     if (postureRays-- <= 0) {
                        examined = false;
                        break;
                     }

                     Vec3 end = point.add(point.subtract(crouchEye).normalize().scale(0.01));
                     BlockHitResult hit = MC.level
                        .clip(new ClipContext(crouchEye, end, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, MC.player));
                     if (hit.getType() == Type.BLOCK
                        && request.hitValidator().accepts(MC.level.getBlockState(hit.getBlockPos()), hit.getBlockPos(), true)
                        && request.hitValidator().lands(hit, victim)) {
                        stillVisible = true;
                        break;
                     }
                  }

                  if (!stillVisible && examined) {
                     for (Entry<BlockPos, List<autismclient.util.BlockPlacementPlanner.SupportCandidate>> entry : buckets.entrySet()) {
                        if (!entry.getKey().equals(victim) && entry.getValue().stream().anyMatch(autismclient.util.BlockPlacementPlanner.SupportCandidate::requiresSneak)) {
                           postureBefore.computeIfAbsent(entry.getKey(), ignored -> new HashSet<>()).add(victim);
                        }
                     }
                  }
               }
            }
         }

         List<BlockPos> ordered = BlockPlacementOrderSolver.order(eye, this.planCells, rays, postureBefore);
         this.planCells.clear();
         this.planCells.addAll(ordered);
         this.close_V_2();
         this.load_J = this.planKey();
         Map<BlockPos, Integer> rank = new HashMap<>();

         for (int i = 0; i < this.planCells.size(); i++) {
            rank.put(this.planCells.get(i), i);
         }

         for (BlockPos blocker : considered) {
            for (Entry<BlockPos, List<Vec3>> entry : rays.entrySet()) {
               if (rank.get(entry.getKey()) < rank.get(blocker)
                  && (BlockPlacementOrderSolver.init(eye, blocker, entry.getValue()) || postureBefore.getOrDefault(blocker, Set.of()).contains(entry.getKey()))) {
                  this.read_Map.computeIfAbsent(blocker, ignored -> new HashSet<>()).add(entry.getKey());
               }
            }
         }
      }
   }

   private void resetPerTickCaches() {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick != this.lastCacheTick) {
         this.lastCacheTick = tick;
         this.tickRayBudget.reset(16);
         this.clearTickCaches();
      }
   }

   private void clearTickCaches() {
      this.blockedCache.clear();
      this.candidateRays.clear();
      this.raysThisTick = 0;
      this.rejectTrace = null;
   }

   private void clearPlan() {
      this.planCells.clear();
      this.ghosts = List.of();
      this.currentLayer = 0;
      this.supportCandidates.clear();
      this.flush_Map.clear();
      this.candidateRays.clear();
      this.candidateRanks = new int[0];
      this.candidatePlans = new BedDefenderModule.Inner_StreamUtil[0];
      this.read_IArr = new int[0];
      this.load_Z = false;
      this.optionCursor = 0;
   }

   private void buildPlan(ItemStack material) {
      this.planCells.clear();
      this.ghosts = List.of();
      this.currentLayer = 0;
      ItemStack[] materials = new ItemStack[6];

      for (int layer = 1; layer <= 5; layer++) {
         this.layerSlots[layer] = this.materialSlot(layer);
         materials[layer] = this.layerSlots[layer] < 0 ? ItemStack.EMPTY : MC.player.getInventory().getItem(this.layerSlots[layer]);
      }

      Vec3 eye = MC.player.getEyePosition();
      double reach = MC.player.blockInteractionRange();
      int layers = this.integer("max-layers");
      BlockPos bedPos = this.findBed(eye, reach + layers + 1.0);
      if (bedPos == null) {
         this.clearBedCandidate();
      } else {
         BlockState bedState = MC.level.getBlockState(bedPos);
         if (!(bedState.getBlock() instanceof BedBlock)) {
            this.cachedBed = null;
            this.clearBedCandidate();
         } else {
            Direction other = BedBlock.getConnectedDirection(bedState);
            Direction outward = other.getOpposite();
            Direction[] perps = other.getAxis() == Axis.X ? new Direction[]{Direction.NORTH, Direction.SOUTH} : new Direction[]{Direction.WEST, Direction.EAST};
            BlockPos partner = bedPos.relative(other);
            BedDefenderModule.Inner_ConfigStore key = new BedDefenderModule.Inner_ConfigStore(bedPos, other, layers, this.bool("defend-under"));
            if (!key.equals(this.layoutKey)) {
               Set<BlockPos> visited = new HashSet<>();
               List<BedDefenderModule.Inner_TaskQueue> steps = new ArrayList<>();
               floodLayers(bedPos, layers, new Direction[]{outward, Direction.UP, perps[0], perps[1]}, visited, steps);
               floodLayers(partner, layers, new Direction[]{other, Direction.UP, perps[0], perps[1]}, visited, steps);
               if (this.bool("defend-under")) {
                  if (visited.add(bedPos.below())) {
                     steps.add(new BedDefenderModule.Inner_TaskQueue(bedPos.below(), 1));
                  }

                  if (visited.add(partner.below())) {
                     steps.add(new BedDefenderModule.Inner_TaskQueue(partner.below(), 1));
                  }
               }

               this.layoutKey = key;
               this.steps = List.copyOf(steps);
               this.visited = Set.copyOf(visited);
            }

            Set<BlockPos> visited = new HashSet<>(this.visited);
            List<BedDefenderModule.Inner_TaskQueue> steps = this.steps;
            int now = AutismSessionState.getInstance().getClientTick();
            this.init_Map.entrySet().removeIf(entry -> entry.getValue() < now);
            BlockPos mining = MC.gameMode instanceof AutismMultiPlayerGameModeAccessor accessor && MC.gameMode.isDestroying()
               ? accessor.autism$getDestroyBlockPos()
               : null;
            if (mining != null) {
               this.init_Map.put(mining.immutable(), now + 10);
            }

            BedDefenderModule.Inner_NetHelper edit;
            while ((edit = this.blockEdits.poll()) != null) {
               this.load_Set.remove(edit.pos());
               if (this.stepCells.contains(edit.pos())) {
                  this.externalBreaks.onBlockEdit(edit.pos(), edit.solid(), this.init_Map.containsKey(edit.pos()));
               }
            }

            this.stepCells = steps.stream().map(BedDefenderModule.Inner_TaskQueue::cell).collect(Collectors.toUnmodifiableSet());

            for (BedDefenderModule.Inner_TaskQueue step : steps) {
               if (MC.level.isLoaded(step.cell()) && !this.load_Set.contains(step.cell())) {
                  BlockState existing = MC.level.getBlockState(step.cell());
                  this.externalBreaks
                     .onBlockEdit(
                        step.cell(), !existing.canBeReplaced(), this.init_Map.containsKey(step.cell()) || step.cell().equals(mining)
                     );
               }
            }

            int repairSlot = this.materialSlot(0);
            boolean repairing = repairSlot >= 0 && steps.stream().anyMatch(step -> this.externalBreaks.wasExternallyBroken(step.cell()));
            ItemStack repairMaterial = repairing ? MC.player.getInventory().getItem(repairSlot) : ItemStack.EMPTY;
            double limit = reach + 1.0;
            double limitSq = limit * limit;
            int bedY = bedPos.getY();
            boolean sneaking = this.tick_Z_2() || this.shouldSneakForBed();
            List<BedDefenderModule.Inner_BufferHelper> pool = new ArrayList<>(steps.size());
            List<BedDefenderModule.Inner_CachePool> ghosts = new ArrayList<>();

            for (BedDefenderModule.Inner_TaskQueue step : steps) {
               ItemStack layerMaterial = repairing && this.externalBreaks.wasExternallyBroken(step.cell()) ? repairMaterial : materials[step.layer()];
               if (MC.level.isLoaded(step.cell())
                  && !MC.level.isOutsideBuildHeight(step.cell())
                  && this.canPlaceAt(step.cell(), layerMaterial)) {
                  ghosts.add(this.init_ListPool_CachePool_BlockPos(step.cell(), step.layer(), layerMaterial));
               }

               BedDefenderModule.Inner_BufferHelper candidate = this.candidateAt(
                  step.cell(), step.layer(), bedPos, partner, bedY, eye, limitSq, layerMaterial, sneaking
               );
               if (candidate != null) {
                  pool.add(candidate);
               } else {
                  BlockPos capped = this.init_BlockPos_BlockPos(step.cell(), bedY, layerMaterial, visited);
                  if (capped != null) {
                     if (this.canPlaceAt(capped, layerMaterial)) {
                        ghosts.add(this.init_ListPool_CachePool_BlockPos(capped, step.layer(), layerMaterial));
                     }

                     BedDefenderModule.Inner_BufferHelper above = this.candidateAt(capped, step.layer(), bedPos, partner, bedY, eye, limitSq, layerMaterial, sneaking);
                     if (above != null) {
                        pool.add(above);
                     }
                  }
               }
            }

            this.addTopCandidates(pool, visited, bedPos, partner, outward, other, layers, bedY, eye, limitSq, materials[1], sneaking);

            for (BedDefenderModule.Inner_BufferHelper cell : pool) {
               if (cell.layer() > layers) {
                  ghosts.add(this.init_ListPool_CachePool_BlockPos(cell.cell(), 1, materials[1]));
               }
            }

            this.ghosts = List.copyOf(ghosts);
            this.currentLayer = ghosts.stream()
               .filter(cell -> !repairing || this.externalBreaks.wasExternallyBroken(cell.pos()))
               .mapToInt(BedDefenderModule.Inner_CachePool::layer)
               .min()
               .orElse(0);
            if (repairing && this.currentLayer > 0) {
               this.layerSlots[this.currentLayer] = repairSlot;
            }

            Set<BlockPos> requiredRisers = new HashSet<>();
            if (!repairing && this.currentLayer == 1 && layers > 1 && !sneaking) {
               List<BedDefenderModule.Inner_BufferHelper> inner = pool.stream().filter(cell -> cell.layer() == 1).toList();
               if (!inner.isEmpty()
                  && inner.stream().noneMatch(BedDefenderModule.Inner_BufferHelper::load_Z)
                  && inner.stream().allMatch(cell -> cell.cell().equals(bedPos.above()) || cell.cell().equals(partner.above()))) {
                  for (BedDefenderModule.Inner_BufferHelper cell : pool) {
                     if (cell.layer() == 2 && cell.load_Z() && inner.stream().anyMatch(top -> top.cell().distManhattan(cell.cell()) == 1)) {
                        requiredRisers.add(cell.cell());
                     }
                  }

                  if (!requiredRisers.isEmpty()) {
                     this.currentLayer = 2;
                  }
               }
            }

            int selectedLayer = this.currentLayer;
            pool.removeIf(cell -> repairing ? !this.externalBreaks.wasExternallyBroken(cell.cell()) : cell.layer() != selectedLayer && (layers != 1 || cell.layer() != 2));
            if (!requiredRisers.isEmpty()) {
               pool.removeIf(cell -> !requiredRisers.contains(cell.cell()));
            }

            Set<BlockPos> blocked = new HashSet<>();

            for (BedDefenderModule.Inner_BufferHelper candidate : pool) {
               if (!candidate.load_Z()) {
                  blocked.add(candidate.cell());
               }
            }

            Axis bedAxis = other.getAxis();
            List<BedDefenderModule.Inner_BufferHelper> candidates = new ArrayList<>(pool.size());

            for (BedDefenderModule.Inner_BufferHelper candidate : pool) {
               int unlocks = 0;

               for (Direction direction : Direction.values()) {
                  if (blocked.contains(candidate.cell().relative(direction))) {
                     unlocks++;
                  }
               }

               candidates.add(
                  new BedDefenderModule.Inner_BufferHelper(
                     candidate.cell(),
                     candidate.layer(),
                     candidate.init_Z(),
                     candidate.load_Z(),
                     unlocks,
                     isInBedRow(candidate.cell(), bedPos, bedAxis, bedY),
                     candidate.init_D()
                  )
               );
            }

            boolean preferSides = this.bool("prefer-sides");
            boolean topFirst = sneaking && selectedLayer == 1;
            candidates.sort((left, right) -> {
               if (left.layer() != right.layer()) {
                  return Integer.compare(left.layer(), right.layer());
               }

               boolean leftRefill = this.recentlyPlaced.contains(left.cell());
               if (leftRefill != this.recentlyPlaced.contains(right.cell())) {
                  return leftRefill ? -1 : 1;
               }

               if (left.load_Z() != right.load_Z()) {
                  return left.load_Z() ? -1 : 1;
               }

               if (left.load_I() != right.load_I()) {
                  return Integer.compare(right.load_I(), left.load_I());
               }

               if (left.init_Z() != right.init_Z()) {
                  if (topFirst) {
                     return left.init_Z() ? -1 : 1;
                  }

                  if (preferSides) {
                     return left.init_Z() ? 1 : -1;
                  }
               }

               if (topFirst && left.read() != right.read()) {
                  return left.read() ? -1 : 1;
               } else {
                  return Double.compare(left.init_D(), right.init_D());
               }
            });

            for (BedDefenderModule.Inner_BufferHelper candidate : candidates) {
               this.planCells.add(candidate.cell());
            }

            if (!bedPos.equals(this.candidateBed)) {
               this.candidateBed = bedPos.immutable();
               this.candidateConfirmCount = 0;
               this.bedCells.remove(bedPos);
               this.bedCells.remove(partner);
            }

            this.candidatePartner = partner.immutable();
            if (this.ghosts.isEmpty()) {
               if (++this.candidateConfirmCount >= 20) {
                  this.lockBed(bedPos, partner);
               }
            } else if (this.planCells.isEmpty() && this.canOperate() && this.currentLayer > 0 && this.layerSlots[this.currentLayer] >= 0) {
               this.confirmBedCandidate();
            }
         }
      }
   }

   private void clearBedCandidate() {
      this.candidateBed = null;
      this.candidatePartner = null;
      this.candidateConfirmCount = 0;
   }

   private boolean confirmBedCandidate() {
      if (this.candidateBed != null && this.candidatePartner != null) {
         if (++this.candidateConfirmCount < this.bedConfirmTicks()) {
            return false;
         }

         this.lockBed(this.candidateBed, this.candidatePartner);
         return true;
      } else {
         return false;
      }
   }

   private int bedConfirmTicks() {
      return 20;
   }

   private static boolean isInBedRow(BlockPos pos, BlockPos bedPos, Axis axis, int bedY) {
      if (pos.getY() != bedY) {
         return false;
      } else {
         return axis == Axis.X ? pos.getZ() == bedPos.getZ() : pos.getX() == bedPos.getX();
      }
   }

   private BedDefenderModule.Inner_BufferHelper candidateAt(
      BlockPos pos, int layer, BlockPos bedPos, BlockPos partner, int bedY, Vec3 eye, double limitSq, ItemStack material, boolean sneaking
   ) {
      if (pos.equals(bedPos) || pos.equals(partner)) {
         return null;
      }

      if (MC.level.isOutsideBuildHeight(pos) || !MC.level.isLoaded(pos)) {
         return null;
      }

      if (!this.canPlaceAt(pos, material)) {
         return null;
      }

      double distSq = pos.distToCenterSqr(eye);
      return distSq > limitSq
         ? null
         : new BedDefenderModule.Inner_BufferHelper(pos.immutable(), layer, pos.getY() > bedY, this.init_Z_BlockPos_3(pos, eye, material, sneaking), 0, false, distSq);
   }

   private BlockPos init_BlockPos_BlockPos(BlockPos pos, int bedY, ItemStack material, Set<BlockPos> visited) {
      if (pos.getY() != bedY) {
         return null;
      }

      BlockState state = MC.level.getBlockState(pos);
      if (!state.isAir() && !this.canPlaceInto(state, pos, material)) {
         AABB[] tops = autismclient.util.BlockPlacementPlanner.extractFaceRects(state, pos, Direction.UP, 2147483647);
         if (tops.length == 0) {
            return null;
         }

         for (AABB top : tops) {
            if (top.maxY - pos.getY() >= 0.5) {
               return null;
            }
         }

         BlockPos above = pos.above();
         return visited.add(above) ? above : null;
      } else {
         return null;
      }
   }

   private void addTopCandidates(
      List<BedDefenderModule.Inner_BufferHelper> pool,
      Set<BlockPos> visited,
      BlockPos bedPos,
      BlockPos partner,
      Direction outward,
      Direction other,
      int layers,
      int bedY,
      Vec3 eye,
      double limitSq,
      ItemStack material,
      boolean sneaking
   ) {
      if (layers == 1 && this.bool("top-riser")) {
         this.addBedTopCell(pool, visited, bedPos.above(), bedPos.relative(outward).above(), bedPos, partner, bedY, eye, limitSq, material, sneaking);
         this.addBedTopCell(pool, visited, partner.above(), partner.relative(other).above(), bedPos, partner, bedY, eye, limitSq, material, sneaking);
      }
   }

   private void addBedTopCell(
      List<BedDefenderModule.Inner_BufferHelper> pool,
      Set<BlockPos> visited,
      BlockPos top,
      BlockPos riser,
      BlockPos bedPos,
      BlockPos partner,
      int bedY,
      Vec3 eye,
      double limitSq,
      ItemStack material,
      boolean sneaking
   ) {
      for (BedDefenderModule.Inner_BufferHelper candidate : pool) {
         if (candidate.cell().equals(top)) {
            if (!candidate.load_Z() && visited.add(riser)) {
               BedDefenderModule.Inner_BufferHelper promoted = this.candidateAt(riser, 2, bedPos, partner, bedY, eye, limitSq, material, sneaking);
               if (promoted != null) {
                  pool.add(promoted);
               }

               return;
            }

            return;
         }
      }
   }

   private boolean canPlaceAt(BlockPos pos, ItemStack material) {
      return this.canPlaceInto(MC.level.getBlockState(pos), pos, material);
   }

   private boolean canPlaceInto(BlockState state, BlockPos pos, ItemStack material) {
      if (!state.canBeReplaced()) {
         return false;
      } else if (!state.isAir() && material.getItem() instanceof BlockItem) {
         BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
         return new BlockPlaceContext(MC.player, InteractionHand.MAIN_HAND, material, hit).replacingClickedOnBlock();
      } else {
         return true;
      }
   }

   private boolean init_Z_BlockPos_3(BlockPos cell, Vec3 eye, ItemStack material, boolean sneaking) {
      if (autismclient.util.BlockPlacementPlanner.canPlaceInReplaceableCell(cell, MC.level)) {
         return true;
      }

      for (Direction face : autismclient.util.BlockPlacementPlanner.DEFAULT_FACE_ORDER) {
         BlockPos support = cell.relative(face.getOpposite());
         BlockState state = MC.level.getBlockState(support);
         if (this.init_Z_BlockState_2(state, support, material, sneaking)
            && !autismclient.util.BlockPlacementPlanner.isSelfOccludedByLid(eye, support, face, MC.level)
            && this.init_Z_BlockState(state, support, face, eye)) {
            return true;
         }
      }

      return false;
   }

   private boolean init_Z_BlockState(BlockState state, BlockPos support, Direction face, Vec3 eye) {
      for (AABB rect : autismclient.util.BlockPlacementPlanner.extractFaceRects(state, support, face, 2147483647)) {
         if (autismclient.util.BlockPlacementPlanner.signedDistancePastFace(eye, rect, face) > 1.0E-4) {
            return true;
         }
      }

      return false;
   }

   private void lockBed(BlockPos bedPos, BlockPos partner) {
      this.clearPlan();
      this.clearBedCandidate();
      this.cachedBed = null;
      this.ticksSinceRescan = this.integer("rescan-interval");
      this.bedCells.add(bedPos.immutable());
      this.bedCells.add(partner.immutable());

      while (this.bedCells.size() > 16) {
         this.bedCells.remove(this.bedCells.iterator().next());
      }
   }

   private static void floodLayers(BlockPos seed, int layers, Direction[] directions, Set<BlockPos> visited, List<BedDefenderModule.Inner_TaskQueue> out) {
      ArrayDeque<BedDefenderModule.Inner_TaskQueue> queue = new ArrayDeque<>();
      queue.add(new BedDefenderModule.Inner_TaskQueue(seed, 0));
      visited.add(seed);

      while (!queue.isEmpty()) {
         BedDefenderModule.Inner_TaskQueue step = queue.poll();
         if (step.layer() > 0) {
            out.add(step);
         }

         if (step.layer() < layers) {
            for (Direction direction : directions) {
               BlockPos next = step.cell().relative(direction);
               if (visited.add(next)) {
                  queue.add(new BedDefenderModule.Inner_TaskQueue(next, step.layer() + 1));
               }
            }
         }
      }
   }

   private BlockPos findBed(Vec3 eye, double radius) {
      if (this.ticksSinceRescan < 2147483647) {
         this.ticksSinceRescan++;
      }

      BlockPos cached = this.cachedBed;
      if (cached != null && this.isBedInRange(cached, eye, radius)) {
         return cached;
      }

      boolean lost = cached != null;
      this.cachedBed = null;
      if (!lost && this.ticksSinceRescan < this.integer("rescan-interval")) {
         return null;
      }

      this.ticksSinceRescan = 0;
      this.cachedBed = this.scanForBed(eye, radius, true);
      if (this.cachedBed == null && !this.bedCells.isEmpty()) {
         this.bedCells.clear();
         this.cachedBed = this.scanForBed(eye, radius, false);
      }

      return this.cachedBed;
   }

   private boolean isBedInRange(BlockPos pos, Vec3 eye, double radius) {
      return pos.distToCenterSqr(eye) > radius * radius ? false : this.isOwnBed(MC.level.getBlockState(pos));
   }

   private boolean isOwnBed(BlockState state) {
      return state.getBlock() instanceof BedBlock bed && (!this.bool("auto-bed") || this.teamColor != null && bed.getColor() == this.teamColor);
   }

   private DyeColor resolveTeamColor() {
      return TeamDyeColorResolver.ownTeamColor();
   }

   private BlockPos scanForBed(Vec3 eye, double radius, boolean skipCompleted) {
      int minX = Mth.floor(eye.x - radius);
      int maxX = Mth.ceil(eye.x + radius);
      int minZ = Mth.floor(eye.z - radius);
      int maxZ = Mth.ceil(eye.z + radius);
      int minY = Math.max(Mth.floor(eye.y - radius), MC.level.getMinY());
      int maxY = Math.min(Mth.ceil(eye.y + radius), MC.level.getMaxY());
      if (minY > maxY) {
         return null;
      }

      BlockPos best = null;
      double bestSq = radius * radius;

      for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
         double distSq = pos.distToCenterSqr(eye);
         if (!(distSq > bestSq) && (!skipCompleted || !this.bedCells.contains(pos)) && this.isOwnBed(MC.level.getBlockState(pos))) {
            best = pos.immutable();
            bestSq = distSq;
         }
      }

      return best;
   }

   private void init_V_I(int slot, ItemStack material, RotationMath.Rotation wireRotation, RotationMath.Rotation from) {
      if (!this.canOperate()) {
         this.releaseRotation();
      } else if (this.open_Z()) {
         this.releaseRotation();
         if (slot >= 0 && slot != MC.player.getInventory().getSelectedSlot()) {
            this.switchToSlot(slot);
         }
      } else if (BlinkPacketManager.isHoldingActionsOnly()) {
         this.releaseRotation();
      } else if (this.planCells.isEmpty() || slot < 0) {
         this.releaseRotation();
         this.tickSwitchBack();
      } else if (this.confirmBedCandidate()) {
         this.releaseRotation();
         this.tickSwitchBack();
      } else {
         boolean sneaking = this.tick_Z_2();
         this.flush_V_2();
         BedDefenderModule.Inner_StreamUtil plan = this.init_ListPool_StreamUti_ce_DataUtil(from, material, null, sneaking);
         if (plan == null) {
            this.releaseRotation();
            this.tickSwitchBack();
         } else {
            KillAuraModule.releaseBlockHitNow();
            if (MC.player.isUsingItem()) {
               this.releaseRotation();
            } else {
               boolean slotReady = slot == MC.player.getInventory().getSelectedSlot();
               BlockHitResult hit = null;
               if (wireRotation != null) {
                  hit = this.init_BlockHitResult_ListPool_StreamUti(plan, wireRotation, sneaking);
                  boolean sneakPending = plan.requiresSneak() && !sneaking;
                  if (hit == null && slotReady && !sneakPending && this.init_Z_ListPool_StreamUti_ce_DataUtil(plan, wireRotation)) {
                     plan = this.init_ListPool_StreamUti_ce_DataUtil(from, material, null, sneaking);
                     hit = plan == null ? null : this.init_BlockHitResult_ListPool_StreamUti(plan, wireRotation, sneaking);
                  }
               }

               if (plan == null) {
                  this.releaseRotation();
                  this.tickSwitchBack();
               } else {
                  this.switchBackDelay = 0;
                  boolean fire = hit != null && slotReady && this.notPlacedThisTick();
                  BedDefenderModule.Inner_StreamUtil next = null;
                  boolean aimed;
                  if (fire) {
                     next = this.init_ListPool_StreamUti_ce_DataUtil(from, material, plan.cell(), sneaking);
                     aimed = this.requestRotation(next != null ? next.goalRotation() : plan.goalRotation(), true);
                  } else {
                     aimed = this.requestRotation(plan.goalRotation(), false);
                  }

                  this.needsSneak = this.shouldSneakForBed() && (plan.requiresSneak() || next != null && next.requiresSneak());
                  this.sneakDecisionTick = AutismSessionState.getInstance().getClientTick();
                  if (fire && aimed) {
                     this.init_V_ListPool_StreamUti_BlockHitResult(plan, hit);
                  } else if (aimed && !slotReady) {
                     this.switchToSlot(slot);
                  }
               }
            }
         }
      }
   }

   private boolean requestRotation(RotationMath.Rotation goal, boolean holdPitch) {
      autismclient.util.SilentRotationManager.requestRotation(this.id(), 30, goal);
      this.unusedLastRotationTick = AutismSessionState.getInstance().getClientTick();
      if (!this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         return false;
      }

      autismclient.util.SilentRotationManager.tick(this.id(), MC.player, 55.0F, holdPitch ? 0.0F : 55.0F, autismclient.util.RotationStreamPlanner.MotionProfile.BED_SHELL);
      return true;
   }

   private boolean init_Z_ListPool_StreamUti_ce_DataUtil(BedDefenderModule.Inner_StreamUtil plan, RotationMath.Rotation wire) {
      double band = autismclient.util.RotationStreamPlanner.pitchMargin(RotationMath.gcdStep());
      if (RotationMath.angularDistance(wire, plan.goalRotation()) > band * 2.0) {
         return false;
      }

      BlockPos key = plan.cell().immutable();

      while (!this.stalls.containsKey(key) && this.stalls.size() >= 16) {
         this.stalls.remove(this.stalls.keySet().iterator().next());
      }

      BedDefenderModule.Inner_DataUtil stall = this.stalls.computeIfAbsent(key, k -> new BedDefenderModule.Inner_DataUtil());
      if (stall.stallAttempts > 1) {
         stall.init_I = 0;
      }

      stall.stallAttempts = 0;
      if (++stall.init_I < 8) {
         return false;
      }

      stall.init_I = 0;
      stall.refusedCooldown = 60;
      autismclient.util.BlockPlacementPlanner.SupportFace refusal = plan.supportFace();
      if (!stall.refusedFaces.contains(refusal)) {
         stall.refusedFaces.add(refusal);
      }

      while (stall.refusedFaces.size() > 12) {
         stall.refusedFaces.remove(0);
      }

      return true;
   }

   private void flush_V_2() {
      Iterator<Entry<BlockPos, BedDefenderModule.Inner_DataUtil>> entries = this.stalls.entrySet().iterator();

      while (entries.hasNext()) {
         BedDefenderModule.Inner_DataUtil stall = entries.next().getValue();
         if (stall.refusedCooldown > 0) {
            stall.refusedCooldown--;
         }

         stall.stallAttempts++;
         if (stall.stallAttempts > 1) {
            stall.init_I = 0;
         }

         if (stall.refusedCooldown == 0) {
            stall.refusedFaces.clear();
         }

         if (stall.init_I == 0 && stall.refusedCooldown == 0) {
            entries.remove();
         }
      }
   }

   private void refreshPlanIfChanged(RotationMath.Rotation from, ItemStack material) {
      long key = autismclient.util.BlockPlacementPlanner.hashCellsNeighborhood(this.planCells, MC.level);
      if (this.load_Z && key == this.init_J) {
         long ranks = this.planKey();
         if (ranks != this.load_J) {
            this.load_J = ranks;
            this.close_V_2();
         }
      } else {
         this.init_J = key;
         this.load_J = this.planKey();
         this.load_Z = true;
         this.supportCandidates.clear();
         this.flush_Map.clear();
         this.candidateRays.clear();
         this.optionCursor = 0;
         autismclient.util.BlockPlacementPlanner.Query template = this.init_U_StateCache_ce_DataUtil(from, material, this.tick_Z_2(), null);
         int[] ranks = new int[Math.min(4096, Math.max(16, this.planCells.size() * 8))];
         int count = 0;

         for (int rank = 0; rank < this.planCells.size() && this.supportCandidates.size() < 4096; rank++) {
            autismclient.util.BlockPlacementPlanner.collectCandidates(template.withTargetCell(this.planCells.get(rank)), this.supportCandidates);
            if (this.supportCandidates.size() > 4096) {
               this.supportCandidates.subList(4096, this.supportCandidates.size()).clear();
            }

            if (ranks.length < this.supportCandidates.size()) {
               ranks = Arrays.copyOf(ranks, Math.max(this.supportCandidates.size(), ranks.length * 2));
            }

            while (count < this.supportCandidates.size()) {
               ranks[count++] = rank;
            }
         }

         this.candidateRanks = Arrays.copyOf(ranks, count);
         this.candidatePlans = new BedDefenderModule.Inner_StreamUtil[this.supportCandidates.size()];
         this.read_IArr = new int[this.supportCandidates.size()];
         Arrays.fill(this.read_IArr, -2147483648);
      }
   }

   private long planKey() {
      long key = 1125899906842597L;

      for (BlockPos cell : this.planCells) {
         key = key * 31L + cell.asLong();
      }

      return key;
   }

   private void close_V_2() {
      Map<BlockPos, Integer> rank = new HashMap<>(this.planCells.size() * 2);

      for (int i = 0; i < this.planCells.size(); i++) {
         rank.put(this.planCells.get(i), i);
      }

      for (int i = 0; i < this.supportCandidates.size() && i < this.candidateRanks.length; i++) {
         Integer now = rank.get(this.supportCandidates.get(i).targetCell());
         if (now != null) {
            this.candidateRanks[i] = now;
         }
      }
   }

   private BedDefenderModule.Inner_StreamUtil init_ListPool_StreamUti_ce_DataUtil(RotationMath.Rotation from, ItemStack material, BlockPos skip, boolean sneaking) {
      int size = this.supportCandidates.size();
      if (size == 0) {
         return null;
      }

      if (this.optionCursor >= size) {
         this.optionCursor = 0;
      }

      AABB playerBox = MC.player.getBoundingBox();
      Vec3 delta = MC.player.getDeltaMovement();
      BlockState placed = material.getItem() instanceof BlockItem blockItem ? blockItem.getBlock().defaultBlockState() : null;
      int tick = AutismSessionState.getInstance().getClientTick();
      StringBuilder trace = null;
      autismclient.util.BlockPlacementPlanner.Query request = this.init_U_StateCache_ce_DataUtil(from, material, sneaking, trace);
      autismclient.util.BlockPlacementPlanner.RejectReason[] refusal = new autismclient.util.BlockPlacementPlanner.RejectReason[1];
      BedDefenderModule.Inner_StreamUtil best = null;
      float bestAngle = 3.4028235E38F;
      int bestRank = 2147483647;
      int start = this.optionCursor;

      for (int step = 0; step < size; step++) {
         int index = (start + step) % size;
         autismclient.util.BlockPlacementPlanner.SupportCandidate option = this.supportCandidates.get(index);
         BlockPos cell = option.targetCell();
         int rank = index < this.candidateRanks.length ? this.candidateRanks[index] : 2147483647;
         if (!cell.equals(skip) && rank <= bestRank && !this.init_Z_BlockPos_2(cell, placed, playerBox, delta)) {
            BedDefenderModule.Inner_DataUtil stall = this.stalls.get(cell);
            if (stall != null && stall.refusedCooldown > 0 && stall.refusedFaces.contains(option.supportFace())) {
               appendRejection(trace, option.face(), autismclient.util.BlockPlacementPlanner.RejectReason.BANNED);
               this.optionCursor = index + 1 == size ? 0 : index + 1;
            } else {
               BedDefenderModule.Inner_StreamUtil plan;
               if (this.read_IArr[index] == tick) {
                  plan = this.candidatePlans[index];
               } else {
                  plan = this.init_ListPool_StreamUti_U_TaskQueue(option, request, refusal);
                  if (this.rayBudgetExhausted) {
                     this.optionCursor = index;
                     break;
                  }

                  this.candidatePlans[index] = plan;
                  this.read_IArr[index] = tick;
               }

               this.optionCursor = index + 1 == size ? 0 : index + 1;
               if (plan != null && (stall == null || stall.refusedCooldown <= 0 || !stall.refusedFaces.contains(plan.supportFace()))) {
                  float angle = RotationMath.angularDistance(from, plan.goalRotation());
                  if (best == null || rank != bestRank || isBetterCandidate(plan.face() == Direction.UP, angle, best, bestAngle)) {
                     best = plan;
                     bestAngle = angle;
                     bestRank = rank;
                  }
               }
            }
         } else {
            this.optionCursor = index + 1 == size ? 0 : index + 1;
         }
      }

      if (trace != null && this.rejectTrace == null && trace.length() > 0) {
         this.rejectTrace = trace.toString();
      }

      for (int pass = 0; best != null && pass < this.confirmedPlans.size(); pass++) {
         BedDefenderModule.Inner_StreamUtil earlier = null;

         for (BlockPos cell : this.read_Map.getOrDefault(best.cell(), Set.of())) {
            if (!cell.equals(skip) && !this.init_Z_BlockPos_2(cell, placed, playerBox, delta)) {
               BedDefenderModule.Inner_DataUtil stall = this.stalls.get(cell);

               for (autismclient.util.BlockPlacementPlanner.PlacementPlan candidate : this.confirmedPlans.getOrDefault(cell, List.of())) {
                  if (stall == null || stall.refusedCooldown <= 0 || !stall.refusedFaces.contains(candidate.support().supportFace())) {
                     BedDefenderModule.Inner_StreamUtil alternative = new BedDefenderModule.Inner_StreamUtil(cell, candidate.support().supportFace(), candidate);
                     if (earlier == null || RotationMath.angularDistance(from, alternative.goalRotation()) < RotationMath.angularDistance(from, earlier.goalRotation())) {
                        earlier = alternative;
                     }
                  }
               }
            }
         }

         if (earlier == null) {
            break;
         }

         best = earlier;
      }

      return best;
   }

   private BedDefenderModule.Inner_StreamUtil init_ListPool_StreamUti_U_TaskQueue(
      autismclient.util.BlockPlacementPlanner.SupportCandidate option, autismclient.util.BlockPlacementPlanner.Query request, autismclient.util.BlockPlacementPlanner.RejectReason[] refusal
   ) {
      this.rayBudgetExhausted = false;
      autismclient.util.BlockPlacementPlanner.AimSample aim = autismclient.util.BlockPlacementPlanner.computeAimSample(option, request.withTargetCell(option.targetCell()), refusal);
      if (aim != null
         || refusal[0] != autismclient.util.BlockPlacementPlanner.RejectReason.NOT_A_SUPPORT
            && refusal[0] != autismclient.util.BlockPlacementPlanner.RejectReason.SELF_OCCLUDED
            && refusal[0] != autismclient.util.BlockPlacementPlanner.RejectReason.BEHIND_PLANE) {
         autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = this.init_U_CachePool_U_TaskQueue(option, aim, request, refusal);
         if (candidate == null) {
            this.rayBudgetExhausted = refusal[0] == autismclient.util.BlockPlacementPlanner.RejectReason.NO_BUDGET;
            return null;
         } else {
            return new BedDefenderModule.Inner_StreamUtil(option.targetCell(), candidate.support().supportFace(), candidate);
         }
      } else {
         return null;
      }
   }

   private autismclient.util.BlockPlacementPlanner.PlacementPlan init_U_CachePool_U_TaskQueue(
      autismclient.util.BlockPlacementPlanner.SupportCandidate option,
      autismclient.util.BlockPlacementPlanner.AimSample aim,
      autismclient.util.BlockPlacementPlanner.Query request,
      autismclient.util.BlockPlacementPlanner.RejectReason[] outcome
   ) {
      if (AutismLiteVariant.enabled()) {
         return autismclient.util.BlockPlacementPlanner.verifyPlacement(option, aim, request, outcome);
      }

      autismclient.util.BlockPlacementPlanner.RejectReason[] refusal = outcome == null ? new autismclient.util.BlockPlacementPlanner.RejectReason[1] : outcome;
      autismclient.util.BlockPlacementPlanner.PlacementPlan found = aim == null ? null : autismclient.util.BlockPlacementPlanner.verifyPlacement(option, aim, request, refusal);
      if (found == null && refusal[0] != autismclient.util.BlockPlacementPlanner.RejectReason.NO_BUDGET) {
         List<Vec3> points;
         if (request.rayBudget() == this.surveyRayBudget) {
            ArrayList<Vec3> samples = new ArrayList<>();
            Axis u = autismclient.util.BlockPlacementPlanner.tangentAxis(option.face().getAxis(), true);
            Axis v = autismclient.util.BlockPlacementPlanner.tangentAxis(option.face().getAxis(), false);

            for (double a : autismclient.util.BlockPlacementPlanner.axisSampleRange(option.faceRect(), u, 0.02, false)) {
               for (double b : autismclient.util.BlockPlacementPlanner.axisSampleRange(option.faceRect(), v, 0.02, false)) {
                  double[] xyz = new double[]{option.faceRect().getCenter().x, option.faceRect().getCenter().y, option.faceRect().getCenter().z};
                  xyz[u.ordinal()] = a;
                  xyz[v.ordinal()] = b;
                  samples.add(new Vec3(xyz[0], xyz[1], xyz[2]));
               }
            }

            points = samples;
         } else {
            points = this.candidateRays.get(option);
            if (points == null) {
               if (this.raysThisTick >= 4) {
                  refusal[0] = autismclient.util.BlockPlacementPlanner.RejectReason.NO_BUDGET;
                  return null;
               }

               this.raysThisTick++;
               points = this.init_List_U_TaskQueue(option, request, aim == null ? option.faceRect().getCenter() : aim.point());
               this.candidateRays.put(option, points);
            }
         }

         int start = Math.floorMod(this.flush_Map.getOrDefault(option, 0), Math.max(1, points.size()));

         for (int i = 0; i < Math.min(4, points.size()); i++) {
            int index = (start + i) % points.size();
            Vec3 point = points.get(index);
            if (request.rayBudget() != this.surveyRayBudget) {
               this.flush_Map.put(option, index + 1);
            }

            if (!(request.eyePos().distanceToSqr(point) > request.reach() * request.reach())) {
               RotationMath.Rotation goal = RotationMath.lookAt(point, request.eyePos());
               if (!(Math.abs(goal.pitch()) > request.maxPitchDegrees())) {
                  if (request.rayBudget() != null && !request.rayBudget().consume()) {
                     if (request.rayBudget() != this.surveyRayBudget) {
                        this.flush_Map.put(option, index);
                     }

                     refusal[0] = autismclient.util.BlockPlacementPlanner.RejectReason.NO_BUDGET;
                     return null;
                  }

                  RotationMath.Rotation emitted = RotationMath.applyGcd(goal, request.fromRotation() == null ? goal : request.fromRotation());
                  if (!(Math.abs(emitted.pitch()) > request.maxPitchDegrees())) {
                     BlockHitResult hit = autismclient.util.BlockPlacementPlanner.raycastBlock(request.eyePos(), emitted, request.reach(), MC.level, MC.player);
                     if (hit != null) {
                        BlockState state = MC.level.getBlockState(hit.getBlockPos());
                        boolean sneak = request.isSneaking() || option.requiresSneak() && request.allowAutoSneak();
                        if (request.hitValidator().accepts(state, hit.getBlockPos(), sneak) && request.hitValidator().lands(hit, option.targetCell())) {
                           autismclient.util.BlockPlacementPlanner.FaceBounds landing = autismclient.util.BlockPlacementPlanner.findHitFaceBounds(state, hit.getBlockPos(), hit);
                           if (landing != null) {
                              autismclient.util.BlockPlacementPlanner.SupportCandidate actual = new autismclient.util.BlockPlacementPlanner.SupportCandidate(
                                 option.targetCell(),
                                 hit.getBlockPos(),
                                 landing.face(),
                                 option.placementForm(),
                                 state,
                                 landing.box(),
                                 option.faceArea(),
                                 option.edgeMarginRatio(),
                                 autismclient.util.BlockPlacementPlanner.requiresSneakToPlace(state, hit.getBlockPos()),
                                 new autismclient.util.BlockPlacementPlanner.SupportFace(hit.getBlockPos(), landing.face())
                              );
                              autismclient.util.BlockPlacementPlanner.AimSample landed = new autismclient.util.BlockPlacementPlanner.AimSample(
                                 hit.getLocation(),
                                 emitted,
                                 emitted,
                                 request.eyePos().distanceTo(hit.getLocation()),
                                 request.fromRotation() == null ? 0.0F : RotationMath.angularDistance(request.fromRotation(), emitted),
                                 autismclient.util.BlockPlacementPlanner.SamplePoint.ESCAPE
                              );
                              refusal[0] = autismclient.util.BlockPlacementPlanner.RejectReason.NONE;
                              return new autismclient.util.BlockPlacementPlanner.PlacementPlan(
                                 actual, landed, hit, !hit.getBlockPos().equals(option.supportPos()), landed.turnDegrees()
                              );
                           }
                        }
                     }
                  }
               }
            }
         }

         refusal[0] = autismclient.util.BlockPlacementPlanner.RejectReason.RAY_MISSED;
         return null;
      } else {
         return found;
      }
   }

   private List<Vec3> init_List_U_TaskQueue(autismclient.util.BlockPlacementPlanner.SupportCandidate option, autismclient.util.BlockPlacementPlanner.Query request, Vec3 preferred) {
      if (AutismLiteVariant.enabled()) {
         return List.of();
      }

      ArrayList<AABB> blockers = new ArrayList<>();
      AABB bounds = option.faceRect().minmax(new AABB(request.eyePos(), request.eyePos())).inflate(1.0);
      CollisionContext context = CollisionContext.of(MC.player);

      for (BlockPos pos : BlockPos.betweenClosed(
         Mth.floor(bounds.minX), Mth.floor(bounds.minY), Mth.floor(bounds.minZ), Mth.floor(bounds.maxX), Mth.floor(bounds.maxY), Mth.floor(bounds.maxZ)
      )) {
         if (MC.level.hasChunkAt(pos)) {
            BlockState state = MC.level.getBlockState(pos);
            if (!state.isAir()) {
               for (AABB box : state.getShape(MC.level, pos, context).toAabbs()) {
                  blockers.add(box.move(pos));
               }
            }
         }
      }

      return FaceVisibilitySolver.visiblePoints(request.eyePos(), option.faceRect(), option.face(), blockers, preferred);
   }

   private autismclient.util.BlockPlacementPlanner.Query init_U_StateCache_ce_DataUtil(RotationMath.Rotation from, ItemStack material, boolean sneaking, StringBuilder trace) {
      Vec3 eye = MC.player.getEyePosition();
      return new autismclient.util.BlockPlacementPlanner.Query(
            null, eye, MC.player.blockInteractionRange(), autismclient.util.BlockPlacementPlanner.blockItemValidator(material, MC.player, InteractionHand.MAIN_HAND)
         )
         .withFromRotation(from)
         .withSneaking(sneaking)
         .withAutoSneak(this.shouldSneakForBed())
         .withLeadEye(eye.add(MC.player.getDeltaMovement()))
         .withRayBudget(this.tickRayBudget)
         .withTrace(trace);
   }

   private boolean shouldSneakForBed() {
      return this.bool("sneak-for-bed") && this.sync_Z_2();
   }

   private boolean init_Z_BlockPos_2(BlockPos cell, BlockState placed, AABB playerBox, Vec3 delta) {
      Boolean known = this.blockedCache.get(cell);
      if (known != null) {
         return known;
      }

      boolean blocked = this.isCellBlocked(cell, placed, playerBox, delta);
      this.blockedCache.put(cell, blocked);
      return blocked;
   }

   private boolean isCellBlocked(BlockPos cell, BlockState placed, AABB playerBox, Vec3 delta) {
      AABB box = new AABB(cell);
      if (!box.intersects(playerBox) && !box.intersects(playerBox.move(delta)) && !box.intersects(playerBox.move(delta.scale(-1.0)))) {
         return placed != null
            ? !MC.level.isUnobstructed(placed, cell, CollisionContext.empty())
            : !MC.level.getEntities(MC.player, box, EntitySelector.NO_SPECTATORS).isEmpty();
      } else {
         return true;
      }
   }

   private static boolean isBetterCandidate(boolean up, float angle, BedDefenderModule.Inner_StreamUtil best, float bestAngle) {
      boolean bestUp = best.face() == Direction.UP;
      return up != bestUp ? up : angle < bestAngle;
   }

   private static void appendRejection(StringBuilder trace, Direction face, autismclient.util.BlockPlacementPlanner.RejectReason reason) {
      if (trace != null && trace.length() < 96) {
         if (trace.length() > 0) {
            trace.append(' ');
         }

         trace.append(face.getSerializedName().charAt(0)).append(':').append(reason.shortCode());
      }
   }

   private boolean init_Z_BlockState_2(BlockState state, BlockPos support, ItemStack material, boolean sneaking) {
      return !state.isAir() && !this.canPlaceInto(state, support, material) ? autismclient.util.BlockPlacementPlanner.isValidSupportBlock(state, support, sneaking) : false;
   }

   private boolean tick_Z_2() {
      return MC.player.isShiftKeyDown() && MC.player.getLastSentInput().shift()
         ? !MC.player.getMainHandItem().isEmpty() || !MC.player.getOffhandItem().isEmpty()
         : false;
   }

   private BlockHitResult init_BlockHitResult_ListPool_StreamUti(BedDefenderModule.Inner_StreamUtil plan, RotationMath.Rotation rotation, boolean sneaking) {
      ItemStack held = MC.player.getItemInHand(InteractionHand.MAIN_HAND);
      autismclient.util.BlockPlacementPlanner.Query gate = this.init_U_StateCache_ce_DataUtil(rotation, held, sneaking, null)
         .withTargetCell(plan.cell());
      return autismclient.util.BlockPlacementPlanner.verifyWithRotation(
         plan.plan(), rotation, MC.player.getEyePosition(), MC.player.blockInteractionRange(), gate
      );
   }

   private boolean notPlacedThisTick() {
      return AutismSessionState.getInstance().getClientTick() != this.lastPlaceTick;
   }

   private void init_V_ListPool_StreamUti_BlockHitResult(BedDefenderModule.Inner_StreamUtil plan, BlockHitResult hit) {
      InteractionHand hand = InteractionHand.MAIN_HAND;
      if (!al.init_Z_HitResult(hit, hand, this.id()) && autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
         try {
            if (!autismclient.util.ClientInteractionOverride.queueUseBlock(hit, hand)) {
               return;
            }

            if (!PerTickPlacementLock.init(this.id())) {
               autismclient.util.ClientInteractionOverride.clearAll();
               return;
            }

            this.poll_V();
            this.lastPlacedCell = plan.cell();
            this.load_Set.add(plan.cell().immutable());
         } finally {
            autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
         }
      }
   }

   private void open_V() {
      if (this.lastPlacedCell != null) {
         BlockPos cell = this.lastPlacedCell;
         this.lastPlacedCell = null;
         if (!MC.level.getBlockState(cell).canBeReplaced()) {
            this.markCellDone(cell);
         }
      }
   }

   private void poll_V() {
      this.lastPlaceTick = AutismSessionState.getInstance().getClientTick();
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
      return this.lastPlaceTick == AutismSessionState.getInstance().getClientTick();
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return hitResult instanceof EntityHitResult && this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && autismclient.util.SilentRotationManager.hasActiveRotation();
   }

   private void markCellDone(BlockPos cell) {
      this.planCells.remove(cell);
      this.candidateConfirmCount = 0;
      BlockPos immutable = cell.immutable();
      this.recentlyPlaced.remove(immutable);
      this.recentlyPlaced.add(immutable);

      while (this.recentlyPlaced.size() > 64) {
         this.recentlyPlaced.remove(this.recentlyPlaced.iterator().next());
      }
   }

   private int materialSlot(int layer) {
      Inventory inventory = MC.player.getInventory();
      int selected = inventory.getSelectedSlot();
      int best = -1;

      for (int slot = 0; slot < 9; slot++) {
         if (!autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, this.id())) {
            ItemStack stack = inventory.getItem(slot);
            if (this.isLayerMaterial(stack, layer) && (best < 0 || isBetterMaterial(stack, inventory.getItem(best), slot == selected, best == selected))) {
               best = slot;
            }
         }
      }

      return best;
   }

   private static boolean isBetterMaterial(ItemStack candidate, ItemStack incumbent, boolean candidateHeld, boolean incumbentHeld) {
      float candidateHardness = blockHardness(candidate);
      float incumbentHardness = blockHardness(incumbent);
      boolean candidateUnbreakable = candidateHardness < 0.0F;
      boolean incumbentUnbreakable = incumbentHardness < 0.0F;
      if (candidateUnbreakable != incumbentUnbreakable) {
         return candidateUnbreakable;
      } else if (candidateHardness != incumbentHardness) {
         return candidateHardness > incumbentHardness;
      } else if (incumbentHeld) {
         return candidate.getCount() >= incumbent.getCount() + 16;
      } else {
         return candidateHeld ? candidate.getCount() + 16 > incumbent.getCount() : candidate.getCount() > incumbent.getCount();
      }
   }

   private static float blockHardness(ItemStack stack) {
      return stack.getItem() instanceof BlockItem blockItem ? blockItem.getBlock().defaultDestroyTime() : 0.0F;
   }

   private void switchToSlot(int slot) {
      if (autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
         try {
            int selected = MC.player.getInventory().getSelectedSlot();
            if (this.switchBackSlot < 0 && this.bool("switch-back")) {
               this.switchBackSlot = selected;
            }

            autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, slot);
            this.switchedToSlot = slot;
         } finally {
            autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
         }
      }
   }

   private void tickSwitchBack() {
      if (this.switchBackSlot >= 0) {
         if (!this.bool("switch-back")) {
            this.switchBackSlot = -1;
            this.switchedToSlot = -1;
         } else if (this.switchedToSlot >= 0 && MC.player.getInventory().getSelectedSlot() != this.switchedToSlot) {
            this.switchBackSlot = -1;
            this.switchedToSlot = -1;
         } else {
            boolean fighting = al.init_Module_Str("kill-aura") instanceof KillAuraModule aura && aura.isEnabled();
            if ((++this.switchBackDelay >= 6 || fighting) && autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
               try {
                  autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, this.switchBackSlot);
                  this.switchBackSlot = -1;
                  this.switchedToSlot = -1;
                  this.switchBackDelay = 0;
               } finally {
                  autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
               }
            }
         }
      }
   }

   private boolean isLayerMaterial(ItemStack stack, int layer) {
      if (!(stack != null && !stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem)) {
         return false;
      }

      if (!stack.isItemEnabled(MC.level.enabledFeatures())) {
         return false;
      }

      Block block = blockItem.getBlock();
      if (this.bool("layer-materials")) {
         boolean allowed = layer > 0
            ? this.layerBlocks(layer).contains(block)
            : this.configuredBlocks().contains(block)
               || IntStream.rangeClosed(1, this.integer("max-layers")).anyMatch(index -> this.layerBlocks(index).contains(block));
         if (!allowed) {
            return false;
         }
      } else {
         boolean listed = this.configuredBlocks().contains(block);
         if ("Whitelist".equals(this.choice("filter-mode")) != listed) {
            return false;
         }
      }

      return block.defaultBlockState().isCollisionShapeFullBlock(MC.level, BlockPos.ZERO)
         ? true
         : this.bool("allow-chests") && block instanceof AbstractChestBlock;
   }

   private Set<Block> configuredBlocks() {
      String raw = this.value("blocks");
      if (raw.equals(this.blockListRaw)) {
         return this.blockListCache;
      }

      Set<Block> blocks = new LinkedHashSet<>();

      for (String entry : this.list("blocks")) {
         Identifier identifier = Identifier.tryParse(RegistryIdValidator.normalize(entry));
         if (identifier != null) {
            BuiltInRegistries.BLOCK.getOptional(identifier).ifPresent(blocks::add);
         }
      }

      this.blockListRaw = raw;
      this.blockListCache = Collections.unmodifiableSet(blocks);
      return this.blockListCache;
   }

   private Set<Block> layerBlocks(int layer) {
      String raw = this.value("layer-" + layer + "-blocks");
      return this.layerBlockCache.computeIfAbsent(raw, key -> {
         Set<Block> blocks = new LinkedHashSet<>();

         for (String entry : key.split("\\|")) {
            Identifier id = Identifier.tryParse(RegistryIdValidator.normalize(entry));
            if (id != null) {
               BuiltInRegistries.BLOCK.getOptional(id).ifPresent(blocks::add);
            }
         }

         return Collections.unmodifiableSet(blocks);
      });
   }

   private BedDefenderModule.Inner_CachePool init_ListPool_CachePool_BlockPos(BlockPos pos, int layer, ItemStack material) {
      BlockState state = material.getItem() instanceof BlockItem item ? item.getBlock().defaultBlockState() : null;
      if (state == null && (this.bool("layer-materials") || "Whitelist".equals(this.choice("filter-mode")))) {
         for (Block block : this.bool("layer-materials") ? this.layerBlocks(layer) : this.configuredBlocks()) {
            if (block.defaultBlockState().isCollisionShapeFullBlock(MC.level, pos)) {
               state = block.defaultBlockState();
               break;
            }
         }
      }

      if (state == null) {
         state = Blocks.GLASS.defaultBlockState();
      }

      return new BedDefenderModule.Inner_CachePool(pos.immutable(), state, layer, !material.isEmpty());
   }

   private record Inner_BufferHelper(BlockPos cell, int layer, boolean init_Z, boolean load_Z, int load_I, boolean read, double init_D) {
   }

   public record Inner_CachePool(BlockPos pos, BlockState state, int layer, boolean init_Z) {
   }

   private record Inner_ConfigStore(BlockPos init_BlockPos, Direction init_Direction, int init_I, boolean init_Z) {
   }

   private static final class Inner_DataUtil {
      private int init_I;
      private int refusedCooldown;
      private int stallAttempts;
      private final List<autismclient.util.BlockPlacementPlanner.SupportFace> refusedFaces = new ArrayList<>();
   }

   private record Inner_NetHelper(BlockPos pos, boolean solid) {
   }

   private record Inner_StreamUtil(
      BlockPos cell, autismclient.util.BlockPlacementPlanner.SupportFace supportFace, autismclient.util.BlockPlacementPlanner.PlacementPlan plan
   ) {
      Direction face() {
         return this.plan.support().face();
      }

      RotationMath.Rotation goalRotation() {
         return this.plan.aim().goalRotation();
      }

      boolean requiresSneak() {
         return this.plan.support().requiresSneak();
      }
   }

   private record Inner_TaskQueue(BlockPos cell, int layer) {
   }
}
