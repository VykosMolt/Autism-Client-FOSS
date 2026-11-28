package autismclient.modules;

import autismclient.ai.AiServerEvidence;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismMinecraftAccessor;
import autismclient.util.AutismLiteVariant;
import autismclient.util.GridWalkNavigator;
import autismclient.util.RotationMath;
import autismclient.util.ServerRotationTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.SilentMovementCoordinator;
import autismclient.util.RegistryIdValidator;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ArmorStandItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.DebugStickItem;
import net.minecraft.world.item.EmptyMapItem;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.FoodOnAStickItem;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.KnowledgeBookItem;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AutoFarmModule extends Module implements SilentMovementCoordinator.Participant {
   private static final int init_I = 2;
   private static final int load_I = 2;
   private static final float init_F = 0.05F;
   private static final int read_I = 4096;
   private static final int write_I = 8;
   private static final int update_I = 80;
   private static final int sync_I = 6;
   private static final int flush_I = 2;
   private static final float load_F = 3.0F;
   private static final int close_I = 3;
   private static final int open = 2;
   private static final int poll = 8;
   private static final int push = 4;
   private static final int pop = 8;
   private static final int tick = 3;
   private static final double init_D = 1.75;
   private static final int run = 2;
   private static final int get = 2;
   private static final int set = 3;
   private static final int put = 12;
   private static final int add = 20;
   private static final int clear = 200;
   private static final int reset = 12;
   private static final float read_F = 90.0F;
   private static final Set<Block> TILLABLE = Set.of(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.DIRT_PATH, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT);
   private static final Set<Block> load_Set = Set.of(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.PODZOL, Blocks.MYCELIUM);
   private static final Set<Block> read_Set = Set.of(
      Blocks.OAK_WOOD,
      Blocks.OAK_LOG,
      Blocks.DARK_OAK_WOOD,
      Blocks.DARK_OAK_LOG,
      Blocks.PALE_OAK_WOOD,
      Blocks.PALE_OAK_LOG,
      Blocks.ACACIA_WOOD,
      Blocks.ACACIA_LOG,
      Blocks.CHERRY_WOOD,
      Blocks.CHERRY_LOG,
      Blocks.BIRCH_WOOD,
      Blocks.BIRCH_LOG,
      Blocks.JUNGLE_WOOD,
      Blocks.JUNGLE_LOG,
      Blocks.SPRUCE_WOOD,
      Blocks.SPRUCE_LOG,
      Blocks.WARPED_STEM,
      Blocks.WARPED_HYPHAE,
      Blocks.CRIMSON_STEM,
      Blocks.CRIMSON_HYPHAE,
      Blocks.MANGROVE_WOOD,
      Blocks.MANGROVE_LOG,
      Blocks.BAMBOO_BLOCK
   );
   private final Random random = new Random();
   private final autismclient.util.BoundedPositionCache<AutoFarmModule.HarvestedCell> harvestedCells = new autismclient.util.BoundedPositionCache<>(4096);
   private int lastInteractTick = -2147483648;
   private int miningStartTick = -2147483648;
   private boolean init_Z;
   private int nextActionTick = -2147483648;
   private int switchBackSlot = -1;
   private int switchedToSlot = -1;
   private int switchBackDelay;
   private int lastSlotSwitchTick = -2147483648;
   private int slotSwitchSettleTicks;
   private BlockPos walkGoal;
   private int lastGoalSearchTick = -2147483648;
   private BlockPos rowEnd;
   private int rowEndTick = -2147483648;
   private BlockPos read_BlockPos;
   private int queue = -2147483648;
   private BlockPos targetCell;
   private autismclient.util.FarmActionScheduler.ActionKind targetKind;
   private int targetChangeTick = -2147483648;
   private final Long2IntOpenHashMap actedCells = new Long2IntOpenHashMap();
   private final Long2ObjectOpenHashMap<Block> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   private BlockPos update_BlockPos;
   private autismclient.util.FarmActionScheduler.ActionKind load_aa_DataUtil;
   private int sameTargetTicks;
   private BlockPos solvedCell;
   private autismclient.util.FarmActionScheduler.ActionKind solvedKind;
   private RotationMath.Rotation solvedRotation;
   private Vec3 solvedEye;
   private static final int node = 20;
   private static final int task = 10;
   private BlockPos flush_BlockPos;
   private autismclient.util.FarmActionScheduler.ActionKind write_aa_DataUtil;
   private float write_F = 3.4028235E38F;
   private int noProgressTicks;
   private final Long2IntOpenHashMap load_Long2IntOpenHashMa = new Long2IntOpenHashMap();
   private static final int index = 3;
   private RotationMath.Rotation lastRotation;
   private int lastRotationTick = -2147483648;
   private int lastActionTick = -2147483648;
   private Axis rowAxis;
   private int sweepSign = 1;
   private final int[] init_IArr = new int[1];
   private int rowCropY;
   private ClientLevel trackedLevel;
   private String cropListRaw = "";
   private Set<Block> cropListCache = Set.of();
   private String status = "";
   private static int[] sphereOffsetCache = new int[0];
   private static int sphereOffsetRadius = -1;
   private String settingsKey;
   private int searchRangeTick = -2147483648;
   private int cachedSearchRange;
   private boolean load_Z;
   private static final int store = 3;
   private BlockPos repeatedCell;
   private int repeatedCellCount;
   private int grownOnlyCacheTick = -2147483648;
   private boolean grownOnlyCache;
   private final Map<IntegerProperty, Integer> maxAgeCache = new IdentityHashMap<>();
   private final Map<BlockState, Boolean> load_Map = new IdentityHashMap<>();
   private final autismclient.util.CellRetryLimiter<AutoFarmModule.RetryKey> retryLimiter = new autismclient.util.CellRetryLimiter<>();
   private int toolScanTick = -2147483648;
   private int wireRotationCacheTick = -2147483648;
   private RotationMath.Rotation wireRotationCache;
   private int nearestHoeSlot = -1;
   private int bestFortuneSlot = -1;
   private Item offhandItem;
   private final Map<Item, Integer> hotbarSlots = new HashMap<>();
   private Holder<Enchantment> fortune;
   private ClientLevel fortuneLookupLevel;
   private static final int f_a_I = 8000;
   private boolean searchActive;
   private double searchMinDistSqr;
   private float searchMaxCorrection;
   private int searchMaxRing;
   private int searchManhattan;
   private long searchMaxRingSqr;
   private int searchOriginX;
   private int searchOriginY;
   private int searchOriginZ;
   private double searchPlayerX;
   private double searchPlayerZ;
   private Vec3 searchEye;
   private float searchYaw;
   private int searchRing;
   private int searchEdgeIndex;
   private int f_i_I;
   private int f_j_I;
   private final List<AutoFarmModule.CellCandidate> candidates = new ArrayList<>();

   AutoFarmModule() {
      super("auto-farm", "AutoFarm", ae.read, "Harvests and replants crops.");
      this.add(new BoolSetting("movement", "Movement", false).description("Walk to distant crops").build());
      this.add(new IntSetting("search-range", "Search Range", 16, 4, 16, 1).description("Pathfinding search radius").build());
      this.add(
         RegistryListSetting.crops(
               "crops",
               "Crops",
               "minecraft:wheat|minecraft:carrots|minecraft:potatoes|minecraft:beetroots|minecraft:nether_wart|minecraft:pumpkin|minecraft:melon"
            )
            .description("Crop blocks to farm")
            .build()
      );
      this.add(new BoolSetting("grown-only", "Fully Grown Only", true).description("Harvest mature crops only").build());
      this.add(new BoolSetting("replant", "Replant", true).description("Replant after harvesting").build());
      this.add(new BoolSetting("plant", "Plant", true).description("Plant bare farmland").build());
      this.add(new BoolSetting("bonemeal", "Bonemeal", true).description("Fertilize crops to grow").build());
      this.add(new BoolSetting("fortune", "Prefer Fortune", true).description("Harvest with fortune tool").build());
      this.add(new BoolSetting("till", "Till Dirt", false).description("Hoe dirt into farmland").build());
      this.add(new BoolSetting("switch-back", "Switch Back", true).description("Return to previous slot").build());
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
      GridWalkNavigator.reset();
   }

   @Override
   public void onGameLeft() {
      this.resetState();
      if (this.ownsRotation()) {
         autismclient.util.SilentRotationManager.resetAll();
      }

      autismclient.util.InventorySlotArbiter.releaseAllClaims(this.id());
      GridWalkNavigator.reset();
   }

   private void resetState() {
      this.lastInteractTick = -2147483648;
      this.toolScanTick = -2147483648;
      this.wireRotationCacheTick = -2147483648;
      this.wireRotationCache = null;
      this.grownOnlyCacheTick = -2147483648;
      this.searchRangeTick = -2147483648;
      this.settingsKey = null;
      this.trackedLevel = null;
      this.switchBackSlot = -1;
      this.switchedToSlot = -1;
      this.switchBackDelay = 0;
      this.lastSlotSwitchTick = -2147483648;
      this.harvestedCells.clear();
      this.retryLimiter.init();
      this.walkGoal = null;
      this.lastGoalSearchTick = -2147483648;
      this.rowEnd = null;
      this.rowEndTick = -2147483648;
      this.read_BlockPos = null;
      this.queue = -2147483648;
      this.targetCell = null;
      this.targetKind = null;
      this.targetChangeTick = -2147483648;
      this.actedCells.clear();
      this.init_Long2ObjectOpenHas.clear();
      this.update_BlockPos = null;
      this.load_aa_DataUtil = null;
      this.sameTargetTicks = 0;
      this.repeatedCell = null;
      this.repeatedCellCount = 0;
      this.solvedCell = null;
      this.solvedKind = null;
      this.solvedRotation = null;
      this.solvedEye = null;
      this.flush_BlockPos = null;
      this.write_aa_DataUtil = null;
      this.write_F = 3.4028235E38F;
      this.noProgressTicks = 0;
      this.load_Long2IntOpenHashMa.clear();
      this.lastRotation = null;
      this.lastRotationTick = -2147483648;
      this.lastActionTick = -2147483648;
      this.rowAxis = null;
      this.sweepSign = 1;
      this.nextActionTick = -2147483648;
      this.searchActive = false;
      this.releaseMining();
      this.status = "";
   }

   @Override
   public String info() {
      return this.status;
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
      return SilentMovementCoordinator.isSafeWalkActive() ? false : autismclient.util.SilentRotationManager.isReturningToPlayer() || this.isEnabled() && this.canRun();
   }

   @Override
   public void preMovementTick() {
      if (!AutismLiteVariant.enabled()) {
         if (MC != null && MC.player != null && MC.level != null && MC.gameMode != null) {
            if (MC.level != this.trackedLevel) {
               this.trackedLevel = MC.level;
               this.toolScanTick = -2147483648;
               this.wireRotationCacheTick = -2147483648;
               this.wireRotationCache = null;
               this.settingsKey = null;
               this.harvestedCells.clear();
               this.retryLimiter.init();
               this.walkGoal = null;
               this.rowEnd = null;
               this.rowEndTick = -2147483648;
               this.read_BlockPos = null;
               this.queue = -2147483648;
               this.lastGoalSearchTick = -2147483648;
               this.searchActive = false;
               this.nextActionTick = -2147483648;
               this.targetCell = null;
               this.rowAxis = null;
               this.actedCells.clear();
               this.init_Long2ObjectOpenHas.clear();
               this.update_BlockPos = null;
               this.load_aa_DataUtil = null;
               this.sameTargetTicks = 0;
               this.repeatedCell = null;
               this.repeatedCellCount = 0;
               this.solvedCell = null;
               this.solvedKind = null;
               this.solvedRotation = null;
               this.solvedEye = null;
               this.flush_BlockPos = null;
               this.write_aa_DataUtil = null;
               this.write_F = 3.4028235E38F;
               this.noProgressTicks = 0;
               this.load_Long2IntOpenHashMa.clear();
               this.lastRotation = null;
               this.lastRotationTick = -2147483648;
               GridWalkNavigator.reset();
               this.releaseMining();
            }

            this.resetOnSettingsChange();
            this.sync_V_3();
            if (!this.canRun()) {
               this.releaseControl();
               this.tickSwitchBack();
            } else {
               AutoFarmModule.FarmTarget target = this.findTarget();
               if (target == null) {
                  this.status = this.missingItemReason();
                  this.releaseStaleMining();
                  this.update_BlockPos = null;
                  this.sameTargetTicks = 0;
                  RotationMath.Rotation graceGoal = this.bool("movement") ? null : this.recentRotation();
                  if (this.bool("movement")) {
                     this.poll_V_2();
                  } else {
                     GridWalkNavigator.load();
                     this.walkGoal = null;
                     this.requestRotation(graceGoal);
                  }

                  if (graceGoal == null) {
                     this.scheduleSwitchBack();
                     this.tickSwitchBack();
                  }
               } else {
                  this.switchBackDelay = 0;
                  boolean walkingThrough = this.bool("movement") && !this.load_Z && this.init_Z_RingBuffer_BufferH(target);
                  if (!walkingThrough) {
                     GridWalkNavigator.load();
                  }

                  this.walkGoal = null;
                  RotationMath.Rotation acted = this.init_ce_DataUtil_RingBuffer_BufferH(target);
                  if (acted != null) {
                     this.lastActionTick = AutismSessionState.getInstance().getClientTick();
                     this.update_BlockPos = null;
                     this.sameTargetTicks = 0;
                     this.flush_BlockPos = null;
                     this.write_aa_DataUtil = null;
                     this.write_F = 3.4028235E38F;
                     this.noProgressTicks = 0;
                     String var10001;
                     switch (target.kind()) {
                        case HARVEST:
                           var10001 = "Harvest";
                           break;
                        case BONEMEAL:
                           var10001 = "Bonemeal";
                           break;
                        case TILL:
                           var10001 = "Till";
                           break;
                        default:
                           var10001 = "Replant";
                     }

                     this.status = var10001;
                     this.lastRotation = acted;
                     this.lastRotationTick = AutismSessionState.getInstance().getClientTick();
                     this.requestRotation(acted, true);
                  } else {
                     this.status = "";
                     this.releaseStaleMining();
                     int tickNow = AutismSessionState.getInstance().getClientTick();
                     int settleAge = tickNow - this.lastSlotSwitchTick;
                     boolean deliberatePause = tickNow == this.lastInteractTick || settleAge >= 0 && settleAge <= this.slotSwitchSettleTicks + 1 || tickNow < this.nextActionTick;
                     if (!deliberatePause) {
                        this.init_V_RingBuffer_BufferH_I(target, tickNow);
                        this.rotateTowardTarget(target, tickNow);
                     }

                     this.lastRotation = target.rotation();
                     this.lastRotationTick = tickNow;
                     this.requestRotation(target.rotation());
                  }
               }
            }
         } else {
            this.resetState();
            GridWalkNavigator.reset();
         }
      }
   }

   private void resetOnSettingsChange() {
      String settings = this.bool("movement")
         + ":"
         + this.searchRange()
         + ":"
         + this.cropBlocks()
         + ":"
         + this.bool("grown-only")
         + ":"
         + this.bool("replant")
         + ":"
         + this.bool("plant")
         + ":"
         + this.bool("bonemeal")
         + ":"
         + this.bool("fortune")
         + ":"
         + this.bool("till");
      if (!settings.equals(this.settingsKey)) {
         this.settingsKey = settings;
         this.releaseMining();
         GridWalkNavigator.reset();
         this.walkGoal = null;
         this.read_BlockPos = null;
         this.rowEnd = null;
         this.rowEndTick = -2147483648;
         this.lastGoalSearchTick = -2147483648;
         this.queue = -2147483648;
         this.searchActive = false;
         this.targetCell = null;
         this.solvedCell = null;
         this.lastRotation = null;
         this.actedCells.clear();
         this.init_Long2ObjectOpenHas.clear();
         this.load_Long2IntOpenHashMa.clear();
         this.grownOnlyCacheTick = -2147483648;
         this.load_Map.clear();
      }
   }

   private RotationMath.Rotation recentRotation() {
      if (this.lastRotation == null) {
         return null;
      }

      int age = AutismSessionState.getInstance().getClientTick() - this.lastRotationTick;
      return age >= 0 && age <= 3 ? this.lastRotation : null;
   }

   private String missingItemReason() {
      if (this.bool("till") && !this.hasUsableHoe()) {
         return "No hoe";
      }

      if (this.bool("replant") || this.bool("plant")) {
         for (Block crop : this.cropBlocks()) {
            if (!autismclient.util.FarmCropCatalog.read(crop) && autismclient.util.FarmCropCatalog.seedFor(crop) == null) {
               return "No seed";
            }
         }
      }

      return this.bool("bonemeal") && !this.hasBoneMeal() && this.init_Z_16() ? "No bonemeal" : "";
   }

   private boolean init_Z_16() {
      Set<Block> crops = this.cropBlocks();
      if (crops.isEmpty()) {
         return false;
      }

      Vec3 eye = MC.player.getEyePosition();
      double cullSqr = this.reach() * this.reach();
      int[] offsets = sphereOffsets(Mth.ceil(this.reach()) + 3);
      BlockPos origin = MC.player.blockPosition();
      int ox = origin.getX();
      int oy = origin.getY();
      int oz = origin.getZ();
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i < offsets.length; i += 3) {
         int cx = ox + offsets[i];
         int cy = oy + offsets[i + 1];
         int cz = oz + offsets[i + 2];
         if (!(distanceSqToCell(eye, cx, cy, cz) > cullSqr)) {
            BlockState state = MC.level.getBlockState(cursor.set(cx, cy, cz));
            if (crops.contains(state.getBlock()) && this.isBonemealTarget(cursor, state)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean canRun() {
      if (MC == null || MC.player == null || MC.level == null || MC.gameMode == null) {
         return false;
      } else if (aD.init_Z() || FreecamController.isActive()) {
         return false;
      } else if (!MC.player.isAlive() || MC.player.isSpectator()) {
         return false;
      } else if (SilentMovementCoordinator.isSafeWalkActive() || ScaffoldModule.update_Z_5()) {
         return false;
      } else if (MC.player.isUsingItem() || MC.player.isHandsBusy()) {
         return false;
      } else if (this.rotationOwnedByOther()) {
         return false;
      } else {
         return MC.gui != null && MC.gui.screen() == null && MC.gui.overlay() == null ? !AutoTotemModule.init_Z_6() && !AutoArmorModule.init_Z_20() : false;
      }
   }

   private void releaseControl() {
      GridWalkNavigator.load();
      this.releaseMining();
      this.walkGoal = null;
      this.status = "";
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

   private boolean rotationOwnedByOther() {
      String owner = autismclient.util.SilentRotationManager.getActiveOwnerId();
      return owner != null && !this.id().equals(owner);
   }

   private void requestRotation(RotationMath.Rotation goal) {
      this.requestRotation(goal, false);
   }

   private void requestRotation(RotationMath.Rotation goal, boolean pinQuiet) {
      if (goal != null) {
         if (!this.rotationOwnedByOther()) {
            autismclient.util.SilentRotationManager.requestRotation(this.id(), 5, goal);
         }
      } else if (this.ownsRotation()) {
         autismclient.util.SilentRotationManager.releaseOwner(this.id());
      }

      if (this.ownsRotation()) {
         autismclient.util.SilentRotationManager.tickPinQuiet(this.id(), MC.player, pinQuiet);
      }
   }

   private double reach() {
      return MC.player.blockInteractionRange();
   }

   private int searchRange() {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick != this.searchRangeTick) {
         this.searchRangeTick = tick;
         this.cachedSearchRange = this.integer("search-range");
      }

      return this.cachedSearchRange;
   }

   private AutoFarmModule.FarmTarget findTarget() {
      Set<Block> crops = this.cropBlocks();
      if (crops.isEmpty()) {
         return null;
      }

      double reach = this.reach();
      Vec3 eye = MC.player.getEyePosition();
      int[] offsets = sphereOffsets(Mth.ceil(reach) + 3);
      double cullSqr = reach * reach;
      int tick = AutismSessionState.getInstance().getClientTick();
      boolean wantReplant = this.bool("replant");
      boolean wantPlant = this.bool("plant");
      int hoeSlot = this.bool("till") ? this.load_I_6() : -1;
      boolean bonemealOffer = this.bool("bonemeal") && this.hasBoneMeal();
      BlockPos origin = MC.player.blockPosition();
      int ox = origin.getX();
      int oy = origin.getY();
      int oz = origin.getZ();
      MutableBlockPos cursor = new MutableBlockPos();
      RotationMath.Rotation sweep = this.wireRotation();
      BlockPos[][] candidateCells = new BlockPos[autismclient.util.FarmActionScheduler.ActionKind.values().length][3];
      double[][] candidateKeys = new double[autismclient.util.FarmActionScheduler.ActionKind.values().length][3];
      double[][] candidateDistances = new double[autismclient.util.FarmActionScheduler.ActionKind.values().length][3];

      for (double[] keys : candidateKeys) {
         Arrays.fill(keys, 1.0 / 0.0);
      }

      this.load_Z = false;

      for (int i = 0; i < offsets.length; i += 3) {
         int cx = ox + offsets[i];
         int cy = oy + offsets[i + 1];
         int cz = oz + offsets[i + 2];
         double distanceSqr = distanceSqToCell(eye, cx, cy, cz);
         if (!(distanceSqr > cullSqr)) {
            BlockState state = MC.level.getBlockState(cursor.set(cx, cy, cz));
            Block block = state.getBlock();
            if (crops.contains(block)) {
               if (this.passesGrownFilter(state)
                  && !this.init_Z_I(cx, cy, cz, tick)
                  && !this.isRecentlySkipped(cx, cy, cz, tick)
                  && this.init_Z_BlockPos_MutableBl_2(cursor, cx, cy, cz, block)) {
                  this.considerCandidate(autismclient.util.FarmActionScheduler.ActionKind.HARVEST, cx, cy, cz, distanceSqr, sweep, eye, candidateCells, candidateKeys, candidateDistances);
               } else if (bonemealOffer
                  && !this.init_Z_I(cx, cy, cz, tick)
                  && !this.isRecentlySkipped(cx, cy, cz, tick)
                  && this.isBonemealTarget(cursor.set(cx, cy, cz), state)) {
                  this.considerCandidate(autismclient.util.FarmActionScheduler.ActionKind.BONEMEAL, cx, cy, cz, distanceSqr, sweep, eye, candidateCells, candidateKeys, candidateDistances);
               }
            } else if ((wantReplant || wantPlant) && state.canBeReplaced()) {
               if (!this.init_Z_I(cx, cy, cz, tick) && !this.isRecentlySkipped(cx, cy, cz, tick)) {
                  BlockState below = MC.level.getBlockState(cursor.set(cx, cy - 1, cz));
                  if (wantReplant && this.replantSeedAt(cx, cy, cz, below, crops, tick) != null) {
                     this.considerCandidate(autismclient.util.FarmActionScheduler.ActionKind.REPLANT_RECENT, cx, cy, cz, distanceSqr, sweep, eye, candidateCells, candidateKeys, candidateDistances);
                  }

                  if (wantPlant && this.init_RingBuffer_CachePo_I(cx, cy, cz, below, crops) != null) {
                     this.considerCandidate(
                        autismclient.util.FarmActionScheduler.ActionKind.REPLANT, cx, cy, cz, distanceSqr, sweep, eye, candidateCells, candidateKeys, candidateDistances
                     );
                  }
               }
            } else if (this.bool("till")
               && this.hasUsableHoe()
               && TILLABLE.contains(block)
               && (block == Blocks.ROOTED_DIRT || MC.level.getBlockState(cursor.set(cx, cy + 1, cz)).isAir())
               && !this.init_Z_I(cx, cy, cz, tick)
               && !this.isRecentlySkipped(cx, cy, cz, tick)) {
               this.considerCandidate(autismclient.util.FarmActionScheduler.ActionKind.TILL, cx, cy, cz, distanceSqr, sweep, eye, candidateCells, candidateKeys, candidateDistances);
            }
         }
      }

      List<autismclient.util.FarmActionScheduler.ActionOption<AutoFarmModule.FarmTarget>> options = new ArrayList<>();

      for (autismclient.util.FarmActionScheduler.ActionKind kind : autismclient.util.FarmActionScheduler.ActionKind.values()) {
         for (BlockPos cell : candidateCells[kind.ordinal()]) {
            if (cell == null) {
               break;
            }

            AutoFarmModule.FarmTarget candidate = this.targetForKind(cell, kind, crops, hoeSlot, tick);
            if (candidate != null) {
               options.add(this.init_aa_StreamUtil_RingBuffer_BufferH_Vec3(candidate, eye));
               break;
            }

            this.markSkipped(cell, tick);
         }
      }

      AutoFarmModule.FarmTarget previous = null;
      int stickyAge = tick - this.targetChangeTick;
      if (this.targetCell != null
         && stickyAge >= 0
         && stickyAge <= 8
         && distanceSqToCell(eye, this.targetCell.getX(), this.targetCell.getY(), this.targetCell.getZ()) <= cullSqr
         && !this.init_Z_I(this.targetCell.getX(), this.targetCell.getY(), this.targetCell.getZ(), tick)
         && !this.isRecentlySkipped(this.targetCell.getX(), this.targetCell.getY(), this.targetCell.getZ(), tick)) {
         previous = this.targetForKind(this.targetCell, this.targetKind, crops, hoeSlot, tick);
         if (previous != null) {
            AutoFarmModule.FarmTarget rebuilt = previous;
            AutoFarmModule.FarmTarget existing = options.stream()
               .map(autismclient.util.FarmActionScheduler.ActionOption::target)
               .filter(t -> t.kind() == rebuilt.kind() && t.cell().equals(rebuilt.cell()))
               .findFirst()
               .orElse(null);
            if (existing != null) {
               previous = existing;
            } else {
               options.add(this.init_aa_StreamUtil_RingBuffer_BufferH_Vec3(previous, eye));
            }
         }
      }

      AutoFarmModule.FarmTarget chosen = autismclient.util.FarmActionScheduler.init(
         options, sweep.yaw(), sweep.pitch(), MC.player.getInventory().getSelectedSlot(), previous
      );
      if (chosen == null) {
         this.targetCell = null;
         return null;
      }

      if (!chosen.cell().equals(this.targetCell) || chosen.kind() != this.targetKind) {
         this.targetChangeTick = tick;
      }

      this.targetCell = chosen.cell();
      this.targetKind = chosen.kind();
      this.rowEndTick = -2147483648;
      if (chosen.kind() == autismclient.util.FarmActionScheduler.ActionKind.HARVEST) {
         AutoFarmModule.FarmTarget solved = this.harvestTarget(chosen.cell());
         if (solved != null) {
            chosen = solved;
         }
      }

      return chosen;
   }

   private autismclient.util.FarmActionScheduler.ActionOption<AutoFarmModule.FarmTarget> init_aa_StreamUtil_RingBuffer_BufferH_Vec3(
      AutoFarmModule.FarmTarget target, Vec3 eye
   ) {
      int slot = target.slot();
      if (target.item() != null) {
         BlockHitResult hit = ScaffoldModule.raycast(eye, target.rotation(), this.reach(), MC.level, MC.player);
         if (hit != null && this.init_Z_Item_BlockHitResult(target.item(), hit)) {
            slot = -1;
         }
      }

      boolean urgent = target.kind() == autismclient.util.FarmActionScheduler.ActionKind.REPLANT_RECENT
         && this.bool("movement")
         && this.init_Z_BlockPos_Vec3(target.cell(), eye);
      this.load_Z |= urgent;
      return new autismclient.util.FarmActionScheduler.ActionOption<>(
         target, target.kind(), target.rotation().yaw(), target.rotation().pitch(), slot, urgent
      );
   }

   private boolean init_Z_BlockPos_Vec3(BlockPos cell, Vec3 eye) {
      Vec3 motion = MC.player.getDeltaMovement();
      double dx = motion.x * 8.0;
      double dz = motion.z * 8.0;
      if (dx * dx + dz * dz < 0.04) {
         double yaw = Math.toRadians(this.wireRotation().yaw());
         dx = -Math.sin(yaw) * 1.6;
         dz = Math.cos(yaw) * 1.6;
      }

      return distanceSqToCell(eye.add(dx, 0.0, dz), cell.getX(), cell.getY(), cell.getZ()) > this.reach() * this.reach() - 0.5;
   }

   private AutoFarmModule.FarmTarget targetForKind(
      BlockPos pos, autismclient.util.FarmActionScheduler.ActionKind kind, Set<Block> crops, int hoeSlot, int tick
   ) {
      switch (kind) {
         case HARVEST:
            BlockState state = MC.level.getBlockState(pos);
            if (crops.contains(state.getBlock()) && this.passesGrownFilter(state)) {
               if (autismclient.util.FarmCropCatalog.load(state.getBlock()) && !MC.level.getBlockState(pos.below()).is(state.getBlock())) {
                  return null;
               }

               return this.harvestTarget(pos);
            }

            return null;
         case BONEMEAL:
            if (!this.bool("bonemeal")) {
               return null;
            }

            return this.isBonemealableCrop(pos) ? this.bonemealTarget(pos) : null;
         case TILL:
            return this.isTillable(pos) ? this.tillTarget(pos, hoeSlot) : null;
         case REPLANT_RECENT:
         case REPLANT:
            if (kind == autismclient.util.FarmActionScheduler.ActionKind.REPLANT_RECENT ? this.bool("replant") : this.bool("plant")) {
               return !MC.level.getBlockState(pos).canBeReplaced()
                  ? null
                  : this.plantTarget(pos, kind == autismclient.util.FarmActionScheduler.ActionKind.REPLANT_RECENT, tick);
            } else {
               return null;
            }
         default:
            return null;
      }
   }

   private boolean init_Z_I(int x, int y, int z, int tick) {
      long key = BlockPos.asLong(x, y, z);
      if (!this.actedCells.containsKey(key)) {
         return false;
      }

      int stamp = this.actedCells.get(key);
      int age = tick - stamp;
      if (age >= 0 && age <= 80) {
         Block base = this.init_Long2ObjectOpenHas.get(key);
         if (base != null && MC.level.getBlockState(new BlockPos(x, y - 1, z)).getBlock() != base) {
            this.actedCells.remove(key);
            this.init_Long2ObjectOpenHas.remove(key);
            return false;
         } else {
            return true;
         }
      } else {
         this.actedCells.remove(key);
         this.init_Long2ObjectOpenHas.remove(key);
         return false;
      }
   }

   private void init_V_BlockPos_I(BlockPos pos, int tick) {
      if (pos.equals(this.repeatedCell)) {
         this.repeatedCellCount++;
      } else {
         this.repeatedCell = pos;
         this.repeatedCellCount = 1;
      }

      if (this.repeatedCellCount >= 3) {
         this.markActed(pos, tick);
         this.repeatedCell = null;
         this.repeatedCellCount = 0;
      }
   }

   private void markActed(BlockPos pos, int tick) {
      if (this.actedCells.size() >= 64) {
         this.actedCells.entrySet().removeIf(entry -> {
            int age = tick - entry.getValue();
            if (age >= 0 && age <= 80) {
               return false;
            }

            this.init_Long2ObjectOpenHas.remove(entry.getKey());
            return true;
         });
      }

      this.actedCells.put(pos.asLong(), tick);
      this.init_Long2ObjectOpenHas.put(pos.asLong(), MC.level.getBlockState(pos.below()).getBlock());
   }

   private void init_V_RingBuffer_BufferH_I(AutoFarmModule.FarmTarget target, int tick) {
      if (!this.rotationOwnedByOther()
         && !BedDefenderModule.init_Z_10()
         && !SurroundModule.init_Z_27()
         && !CrystalAuraModule.init_Z_51()
         && !AnchorAuraModule.init_Z_2()
         && !BlinkPacketManager.isHoldingActionsOnly()
         && ServerRotationTracker.getSnapshot().initialized()) {
         RotationMath.Rotation goal = target.rotation();
         RotationMath.Rotation wire = this.wireRotation();
         boolean arrived = Math.abs(RotationMath.angleDelta(wire.yaw(), goal.yaw())) <= 3.0F && Math.abs(wire.pitch() - goal.pitch()) <= 3.0F;
         if (!arrived) {
            this.update_BlockPos = null;
            this.sameTargetTicks = 0;
         } else {
            if (target.cell().equals(this.update_BlockPos) && target.kind() == this.load_aa_DataUtil) {
               this.sameTargetTicks++;
            } else {
               this.update_BlockPos = target.cell();
               this.load_aa_DataUtil = target.kind();
               this.sameTargetTicks = 1;
            }

            if (this.sameTargetTicks >= this.actionCooldown(tick)) {
               this.markActed(target.cell(), tick);
               this.update_BlockPos = null;
               this.sameTargetTicks = 0;
            }
         }
      }
   }

   private int actionCooldown(int tick) {
      int actionAge = tick - this.lastActionTick;
      return actionAge >= 0 && actionAge <= 2 ? 2 : 6;
   }

   private void rotateTowardTarget(AutoFarmModule.FarmTarget target, int tick) {
      if (!this.rotationOwnedByOther()
         && !BedDefenderModule.init_Z_10()
         && !SurroundModule.init_Z_27()
         && !CrystalAuraModule.init_Z_51()
         && !AnchorAuraModule.init_Z_2()
         && !BlinkPacketManager.isHoldingActionsOnly()
         && ServerRotationTracker.getSnapshot().initialized()) {
         RotationMath.Rotation goal = target.rotation();
         RotationMath.Rotation wire = this.wireRotation();
         float distance = Math.abs(RotationMath.angleDelta(wire.yaw(), goal.yaw())) + Math.abs(wire.pitch() - goal.pitch());
         if (!target.cell().equals(this.flush_BlockPos) || target.kind() != this.write_aa_DataUtil) {
            this.flush_BlockPos = target.cell();
            this.write_aa_DataUtil = target.kind();
            this.write_F = distance;
            this.noProgressTicks = 0;
         } else if (distance < this.write_F - 0.5F) {
            this.write_F = distance;
            this.noProgressTicks = 0;
         } else {
            this.noProgressTicks++;
         }

         if (this.noProgressTicks >= 20) {
            this.markSkipped(target.cell(), tick);
            this.flush_BlockPos = null;
            this.write_aa_DataUtil = null;
            this.write_F = 3.4028235E38F;
            this.noProgressTicks = 0;
         }
      }
   }

   private void markSkipped(BlockPos pos, int tick) {
      this.load_Long2IntOpenHashMa.put(pos.asLong(), tick);
   }

   private boolean isRecentlySkipped(int x, int y, int z, int tick) {
      long key = BlockPos.asLong(x, y, z);
      int stamp = this.load_Long2IntOpenHashMa.getOrDefault(key, -2147483648);
      if (stamp == -2147483648) {
         return false;
      }

      int age = tick - stamp;
      if (age >= 0 && age <= 10) {
         return true;
      }

      this.load_Long2IntOpenHashMa.remove(key);
      return false;
   }

   private boolean init_Z_BlockPos_MutableBl_2(MutableBlockPos cursor, int x, int y, int z, Block block) {
      return !autismclient.util.FarmCropCatalog.load(block) || MC.level.getBlockState(cursor.set(x, y - 1, z)).is(block);
   }

   private void considerCandidate(
      autismclient.util.FarmActionScheduler.ActionKind kind,
      int x,
      int y,
      int z,
      double distanceSqr,
      RotationMath.Rotation sweep,
      Vec3 eye,
      BlockPos[][] cells,
      double[][] keys,
      double[][] distances
   ) {
      int group = kind.ordinal();
      double dx = x + 0.5 - eye.x;
      double dy = y + 0.5 - eye.y;
      double dz = z + 0.5 - eye.z;
      float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz))));
      double key = Math.hypot(RotationMath.angleDelta(sweep.yaw(), yaw), pitch - sweep.pitch());
      if (kind == autismclient.util.FarmActionScheduler.ActionKind.REPLANT_RECENT && this.bool("movement") && this.init_Z_BlockPos_Vec3(new BlockPos(x, y, z), eye)) {
         key -= 360.0;
      }

      for (int i = 0; i < cells[group].length; i++) {
         if (!(key > keys[group][i]) && (key != keys[group][i] || !(distanceSqr >= distances[group][i]))) {
            for (int j = cells[group].length - 1; j > i; j--) {
               cells[group][j] = cells[group][j - 1];
               keys[group][j] = keys[group][j - 1];
               distances[group][j] = distances[group][j - 1];
            }

            cells[group][i] = new BlockPos(x, y, z);
            keys[group][i] = key;
            distances[group][i] = distanceSqr;
            break;
         }
      }
   }

   private RotationMath.Rotation wireRotation() {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick == this.wireRotationCacheTick && this.wireRotationCache != null) {
         return this.wireRotationCache;
      }

      this.wireRotationCacheTick = tick;
      ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
      this.wireRotationCache = wire.initialized() ? new RotationMath.Rotation(wire.currentYaw(), wire.currentPitch()) : RotationMath.ofPlayer(MC.player);
      return this.wireRotationCache;
   }

   private boolean passesGrownFilter(BlockState state) {
      return !this.grownOnly() ? true : this.isFullyGrown(state);
   }

   private boolean isFullyGrown(BlockState state) {
      Boolean cached = this.load_Map.get(state);
      if (cached != null) {
         return cached;
      }

      boolean verdict = true;

      for (Property<?> property : state.getProperties()) {
         if (property instanceof IntegerProperty age && "age".equals(age.getName())) {
            verdict = state.getValue(age) >= this.maxAge(age);
            break;
         }
      }

      this.load_Map.put(state, verdict);
      return verdict;
   }

   private boolean grownOnly() {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick != this.grownOnlyCacheTick) {
         this.grownOnlyCacheTick = tick;
         this.grownOnlyCache = this.bool("grown-only");
      }

      return this.grownOnlyCache;
   }

   private int maxAge(IntegerProperty age) {
      return this.maxAgeCache.computeIfAbsent(age, key -> Collections.max(key.getPossibleValues()));
   }

   private Set<Block> cropBlocks() {
      String raw = this.value("crops");
      if (raw.equals(this.cropListRaw)) {
         return this.cropListCache;
      }

      Set<Block> blocks = new LinkedHashSet<>();

      for (String entry : this.list("crops")) {
         Identifier identifier = Identifier.tryParse(RegistryIdValidator.init(entry));
         if (identifier != null) {
            BuiltInRegistries.BLOCK.getOptional(identifier).filter(autismclient.util.FarmCropCatalog::isCrop).ifPresent(blocks::add);
         }
      }

      this.cropListRaw = raw;
      this.cropListCache = Collections.unmodifiableSet(blocks);
      return this.cropListCache;
   }

   private AutoFarmModule.SeedChoice init_RingBuffer_CachePo_I(int x, int y, int z, BlockState below, Set<Block> crops) {
      AutoFarmModule.HarvestedCell ownCell = this.harvestedCells.get(BlockPos.asLong(x, y, z));
      if (ownCell != null) {
         return this.init_Z_RingBuffer_StreamU(ownCell) ? null : this.replantSeedFor(ownCell.block(), below, crops, x, y, z);
      }

      Block preferred = null;
      int bestDistance = 2147483647;

      for (int distance = 1; distance <= 4 && preferred == null; distance++) {
         for (Direction direction : Plane.HORIZONTAL) {
            int nx = x + direction.getStepX() * distance;
            int nz = z + direction.getStepZ() * distance;
            AutoFarmModule.HarvestedCell entry = this.harvestedCells.get(BlockPos.asLong(nx, y, nz));
            Block neighbor = entry != null ? entry.block() : MC.level.getBlockState(new BlockPos(nx, y, nz)).getBlock();
            if (crops.contains(neighbor) && this.canPlantOn(neighbor, below, x, y, z)) {
               bestDistance = distance;
               preferred = neighbor;
               break;
            }
         }
      }

      if (preferred != null && bestDistance <= 4) {
         return this.replantSeedFor(preferred, below, crops, x, y, z);
      }

      for (Block crop : crops) {
         AutoFarmModule.SeedChoice choice = this.replantSeedFor(crop, below, crops, x, y, z);
         if (choice != null) {
            return choice;
         }
      }

      return null;
   }

   private AutoFarmModule.SeedChoice replantSeedFor(Block crop, BlockState below, Set<Block> crops, int x, int y, int z) {
      if (crops.contains(crop) && this.canPlantOn(crop, below, x, y, z)) {
         if (crop instanceof CropBlock && MC.level.getRawBrightness(new BlockPos(x, y, z), 0) < 8) {
            return null;
         }

         Item seed = autismclient.util.FarmCropCatalog.seedFor(crop);
         return seed == null ? null : this.seedChoice(crop, seed);
      } else {
         return null;
      }
   }

   private boolean canPlantOn(Block crop, BlockState below, int x, int y, int z) {
      if (crop != Blocks.COCOA) {
         return autismclient.util.FarmCropCatalog.canGrowOn(crop, below);
      }

      BlockPos cell = new BlockPos(x, y, z);

      for (Direction direction : Plane.HORIZONTAL) {
         if (autismclient.util.FarmCropCatalog.canGrowOn(crop, MC.level.getBlockState(cell.relative(direction)))) {
            return true;
         }
      }

      return false;
   }

   private AutoFarmModule.SeedChoice seedChoice(Block crop, Item seed) {
      int slot = this.hotbarSlotOf(seed);
      if (slot >= 0) {
         return new AutoFarmModule.SeedChoice(crop, seed, slot);
      } else {
         return this.offhandHolds(seed) ? new AutoFarmModule.SeedChoice(crop, seed, -1) : null;
      }
   }

   private AutoFarmModule.SeedChoice replantSeedAt(int x, int y, int z, BlockState below, Set<Block> crops, int tick) {
      AutoFarmModule.HarvestedCell entry = this.harvestedCells.get(BlockPos.asLong(x, y, z));
      return entry != null && !this.init_Z_RingBuffer_StreamU(entry) ? this.replantSeedFor(entry.block(), below, crops, x, y, z) : null;
   }

   private AutoFarmModule.FarmTarget harvestTarget(BlockPos cell) {
      int slot = this.bool("fortune") ? this.fortuneSlot() : -1;
      RotationMath.Rotation rotation = this.load_ce_DataUtil(cell);
      return rotation == null
         ? null
         : new AutoFarmModule.FarmTarget(autismclient.util.FarmActionScheduler.ActionKind.HARVEST, cell, MC.level.getBlockState(cell).getBlock(), null, slot, rotation);
   }

   private boolean init_Z_ce_DataUtil_Vec3(RotationMath.Rotation goal, Vec3 eye) {
      BlockHitResult ray = ScaffoldModule.raycast(eye, goal, this.reach(), MC.level, MC.player);
      if (ray == null) {
         return false;
      }

      BlockPos cell = ray.getBlockPos();
      BlockState state = MC.level.getBlockState(cell);
      return this.cropBlocks().contains(state.getBlock()) && this.passesGrownFilter(state)
         ? !autismclient.util.FarmCropCatalog.load(state.getBlock()) || MC.level.getBlockState(cell.below()).is(state.getBlock())
         : false;
   }

   private AutoFarmModule.FarmTarget plantTarget(BlockPos cell, boolean recent, int tick) {
      Set<Block> crops = this.cropBlocks();
      BlockState below = MC.level.getBlockState(cell.below());
      AutoFarmModule.SeedChoice choice = recent
         ? this.replantSeedAt(cell.getX(), cell.getY(), cell.getZ(), below, crops, tick)
         : this.init_RingBuffer_CachePo_I(cell.getX(), cell.getY(), cell.getZ(), below, crops);
      if (choice == null) {
         return null;
      }

      ItemStack stack = choice.slot() >= 0 ? MC.player.getInventory().getItem(choice.slot()) : MC.player.getOffhandItem();
      autismclient.util.FarmActionScheduler.ActionKind kind = recent ? autismclient.util.FarmActionScheduler.ActionKind.REPLANT_RECENT : autismclient.util.FarmActionScheduler.ActionKind.REPLANT;
      Vec3 eye = MC.player.getEyePosition();
      RotationMath.Rotation rotation;
      if (this.solvedCell != null && this.solvedCell.equals(cell) && this.solvedKind == kind && this.solvedEye.distanceTo(eye) <= 1.0) {
         rotation = this.solvedRotation;
      } else {
         rotation = this.plantRotation(cell, stack);
         if (rotation != null) {
            this.solvedCell = cell.immutable();
            this.solvedKind = kind;
            this.solvedRotation = rotation;
            this.solvedEye = eye;
         }
      }

      return rotation == null ? null : new AutoFarmModule.FarmTarget(kind, cell, choice.crop(), choice.seed(), choice.slot(), rotation);
   }

   private AutoFarmModule.FarmTarget tillTarget(BlockPos cell, int hoeSlot) {
      Item hoe = hoeSlot >= 0 ? MC.player.getInventory().getItem(hoeSlot).getItem() : (this.offhandHoeUsable() ? MC.player.getOffhandItem().getItem() : null);
      if (hoe == null) {
         return null;
      }

      RotationMath.Rotation rotation = this.tillRotation(cell);
      return rotation == null ? null : new AutoFarmModule.FarmTarget(autismclient.util.FarmActionScheduler.ActionKind.TILL, cell, null, hoe, hoeSlot, rotation);
   }

   private AutoFarmModule.FarmTarget bonemealTarget(BlockPos cell) {
      int slot = this.boneMealSlot();
      if (slot < 0 && !this.offhandHolds(Items.BONE_MEAL)) {
         return null;
      }

      RotationMath.Rotation rotation = this.aimAtCell(cell);
      return rotation == null
         ? null
         : new AutoFarmModule.FarmTarget(
            autismclient.util.FarmActionScheduler.ActionKind.BONEMEAL, cell, MC.level.getBlockState(cell).getBlock(), Items.BONE_MEAL, slot, rotation
         );
   }

   private RotationMath.Rotation aimAtCell(BlockPos cell) {
      Vec3 eye = MC.player.getEyePosition();
      VoxelShape shape = MC.level.getBlockState(cell).getShape(MC.level, cell);
      AABB box = shape.isEmpty() ? new AABB(cell) : shape.bounds().move(cell);
      RotationMath.Rotation goal = RotationMath.lookAt(box.getCenter(), eye);
      if (!isPitchAllowed(goal.pitch())) {
         return null;
      } else {
         return this.rayHitsBonemealable(goal, eye) ? goal : null;
      }
   }

   private boolean rayHitsBonemealable(RotationMath.Rotation goal, Vec3 eye) {
      BlockHitResult ray = ScaffoldModule.raycast(eye, goal, this.reach(), MC.level, MC.player);
      return ray != null && this.isBonemealableCrop(ray.getBlockPos());
   }

   private boolean isBonemealableCrop(BlockPos pos) {
      BlockState state = MC.level.getBlockState(pos);
      return this.cropBlocks().contains(state.getBlock()) && this.isBonemealTarget(pos, state);
   }

   private boolean isBonemealTarget(BlockPos pos, BlockState state) {
      Block block = state.getBlock();
      return !autismclient.util.FarmCropCatalog.load(block) && !this.isFullyGrown(state)
         ? block instanceof BonemealableBlock b && Compat263.isValidBonemealTarget(b, MC.level, pos, state)
         : false;
   }

   private RotationMath.Rotation load_ce_DataUtil(BlockPos cell) {
      Vec3 eye = MC.player.getEyePosition();
      RotationMath.Rotation row = this.init_ce_DataUtil_BlockPos_Vec3(cell, eye);
      if (row != null) {
         return row;
      }

      Set<Block> crops = this.cropBlocks();
      double cullSqr = this.reach() * this.reach();
      List<BlockPos> members = new ArrayList<>();
      MutableBlockPos cursor = new MutableBlockPos();

      for (int dx = -2; dx <= 2; dx++) {
         for (int dy = -2; dy <= 2; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
               int x = cell.getX() + dx;
               int y = cell.getY() + dy;
               int z = cell.getZ() + dz;
               if (!(distanceSqToCell(eye, x, y, z) > cullSqr)) {
                  BlockState state = MC.level.getBlockState(cursor.set(x, y, z));
                  if (crops.contains(state.getBlock())
                     && this.passesGrownFilter(state)
                     && this.init_Z_BlockPos_MutableBl_2(cursor, x, y, z, state.getBlock())) {
                     members.add(new BlockPos(x, y, z));
                  }
               }
            }
         }
      }

      members.sort(Comparator.comparingInt(member -> {
         int dx = member.getX() - cell.getX();
         int dy = member.getY() - cell.getY();
         int dz = member.getZ() - cell.getZ();
         return dx * dx + dy * dy + dz * dz;
      }));
      int bandCount = members.size();
      if (bandCount <= 3) {
         this.init_V_BlockPos(cell, eye, crops, cullSqr, members);
      }

      int count = Math.min(members.size(), 8);
      double sumX = 0.0;
      double sumY = 0.0;
      double sumZ = 0.0;

      for (int i = 0; i < count; i++) {
         BlockPos member = members.get(i);
         sumX += member.getX() + 0.5;
         sumY += member.getY() + 0.5;
         sumZ += member.getZ() + 0.5;
      }

      Vec3 centroid = new Vec3(sumX / count, sumY / count, sumZ / count);
      if (eye.distanceToSqr(centroid) <= cullSqr) {
         RotationMath.Rotation goal = RotationMath.lookAt(centroid, eye);
         if (this.init_Z_ce_DataUtil_Vec3(goal, eye)) {
            return goal;
         }
      }

      if (count > bandCount) {
         count = bandCount;
         sumX = 0.0;
         sumY = 0.0;
         sumZ = 0.0;

         for (int i = 0; i < count; i++) {
            BlockPos member = members.get(i);
            sumX += member.getX() + 0.5;
            sumY += member.getY() + 0.5;
            sumZ += member.getZ() + 0.5;
         }

         centroid = new Vec3(sumX / count, sumY / count, sumZ / count);
         if (eye.distanceToSqr(centroid) <= cullSqr) {
            RotationMath.Rotation goal = RotationMath.lookAt(centroid, eye);
            if (this.init_Z_ce_DataUtil_Vec3(goal, eye)) {
               return goal;
            }
         }
      }

      VoxelShape shape = MC.level.getBlockState(cell).getShape(MC.level, cell);
      AABB box = shape.isEmpty() ? new AABB(cell) : shape.bounds().move(cell);
      RotationMath.Rotation boxGoal = RotationMath.lookAt(box.getCenter(), eye);
      return !this.init_Z_ce_DataUtil_Vec3(boxGoal, eye) ? null : boxGoal;
   }

   private void init_V_BlockPos(BlockPos cell, Vec3 eye, Set<Block> crops, double cullSqr, List<BlockPos> members) {
      RotationMath.Rotation sweep = this.wireRotation();
      int[] offsets = sphereOffsets(Mth.ceil(this.reach()) + 3);
      BlockPos origin = MC.player.blockPosition();
      int ox = origin.getX();
      int oy = origin.getY();
      int oz = origin.getZ();
      MutableBlockPos cursor = new MutableBlockPos();
      List<BlockPos> beyond = new ArrayList<>();

      for (int i = 0; i < offsets.length; i += 3) {
         int x = ox + offsets[i];
         int y = oy + offsets[i + 1];
         int z = oz + offsets[i + 2];
         if ((Math.abs(x - cell.getX()) > 2 || Math.abs(y - cell.getY()) > 2 || Math.abs(z - cell.getZ()) > 2) && !(distanceSqToCell(eye, x, y, z) > cullSqr)) {
            BlockState state = MC.level.getBlockState(cursor.set(x, y, z));
            if (crops.contains(state.getBlock()) && this.passesGrownFilter(state) && this.init_Z_BlockPos_MutableBl_2(cursor, x, y, z, state.getBlock())) {
               beyond.add(new BlockPos(x, y, z));
            }
         }
      }

      beyond.sort(Comparator.comparingDouble(pos -> {
         double dx = pos.getX() + 0.5 - eye.x;
         double dy = pos.getY() + 0.5 - eye.y;
         double dz = pos.getZ() + 0.5 - eye.z;
         float toYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
         float toPitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
         return Math.abs(RotationMath.angleDelta(sweep.yaw(), toYaw)) + Math.abs(toPitch - sweep.pitch());
      }));

      for (int i = 0; i < Math.min(4, beyond.size()); i++) {
         members.add(beyond.get(i));
      }
   }

   private RotationMath.Rotation init_ce_DataUtil_BlockPos_Vec3(BlockPos pick, Vec3 eye) {
      Set<Block> crops = this.cropBlocks();
      int px = pick.getX();
      int py = pick.getY();
      int pz = pick.getZ();
      MutableBlockPos cursor = new MutableBlockPos();
      int countX = 0;
      int countZ = 0;

      for (int d = -8; d <= 8; d++) {
         for (int dy = -1; dy <= 1; dy++) {
            if (crops.contains(MC.level.getBlockState(cursor.set(px + d, py + dy, pz)).getBlock())) {
               countX++;
            }

            if (crops.contains(MC.level.getBlockState(cursor.set(px, py + dy, pz + d)).getBlock())) {
               countZ++;
            }
         }
      }

      if (Math.max(countX, countZ) < 3) {
         return null;
      }

      Axis axis;
      if (countX == countZ) {
         axis = this.rowAxis == Axis.Z ? Axis.Z : Axis.X;
      } else {
         axis = countX > countZ ? Axis.X : Axis.Z;
      }

      int along = axis == Axis.X ? px : pz;
      int perp = axis == Axis.X ? pz : px;
      int sign;
      if (this.rowAxis == axis) {
         sign = this.sweepSign;
      } else {
         sign = this.facingSign(axis);
         if (sign == 0) {
            sign = 1;
         }
      }

      int end = this.findRowEnd(cursor, along, perp, py, axis, sign, crops, this.init_IArr);
      if (end == -2147483648 && this.rowAxis != axis) {
         sign = -sign;
         end = this.findRowEnd(cursor, along, perp, py, axis, sign, crops, this.init_IArr);
      }

      if (end == -2147483648) {
         return this.rowSweepRotation(cursor, along, perp, py, axis, sign, crops, eye);
      }

      if ((end - along) * sign <= 3) {
         RotationMath.Rotation pivot = this.rowSweepRotation(cursor, along, perp, py, axis, sign, crops, eye);
         if (pivot != null) {
            return pivot;
         }

         this.findRowEnd(cursor, along, perp, py, axis, sign, crops, this.init_IArr);
      }

      this.rowAxis = axis;
      this.sweepSign = sign;
      RotationMath.Rotation goal = this.rowAimRotation(along, perp, this.init_IArr[0], axis, sign, end, eye);
      if (goal != null && !this.init_Z_ce_DataUtil_Vec3(goal, eye)) {
         goal = null;
      }

      if (goal != null) {
         this.markRowEnd(end, perp, axis);
      }

      return goal;
   }

   private RotationMath.Rotation rowSweepRotation(MutableBlockPos cursor, int along, int perp, int py, Axis axis, int sign, Set<Block> crops, Vec3 eye) {
      for (int offset = 1; offset <= 3; offset++) {
         for (int side = -1; side <= 1; side += 2) {
            int rowPerp = perp + side * offset;
            int anchor = -2147483648;
            int anchorDist = 2147483647;

            for (int da = -2; da <= 2; da++) {
               if (this.cropYAt(cursor, along + da, rowPerp, py, axis, crops) != -2147483648 && Math.abs(da) < anchorDist) {
                  anchorDist = Math.abs(da);
                  anchor = along + da;
               }
            }

            if (anchor != -2147483648) {
               int newSign = -sign;
               int end = this.findRowEnd(cursor, anchor, rowPerp, py, axis, newSign, crops, this.init_IArr);
               if (end != -2147483648) {
                  RotationMath.Rotation goal = this.rowAimRotation(anchor, rowPerp, this.init_IArr[0], axis, newSign, end, eye);
                  if (goal != null && !this.init_Z_ce_DataUtil_Vec3(goal, eye)) {
                     goal = null;
                  }

                  if (goal == null) {
                     return null;
                  }

                  this.rowAxis = axis;
                  this.sweepSign = newSign;
                  this.markRowEnd(end, rowPerp, axis);
                  return goal;
               }
            }
         }
      }

      return null;
   }

   private void markRowEnd(int end, int perp, Axis axis) {
      this.rowEnd = axis == Axis.X ? new BlockPos(end, this.rowCropY, perp) : new BlockPos(perp, this.rowCropY, end);
      this.rowEndTick = AutismSessionState.getInstance().getClientTick();
   }

   private int findRowEnd(MutableBlockPos cursor, int along, int perp, int py, Axis axis, int sign, Set<Block> crops, int[] nextY) {
      int end = -2147483648;
      int gap = 0;
      boolean gapAllWater = true;

      for (int step = 1; step <= 8; step++) {
         int a = along + sign * step;
         int cropY = this.cropYAt(cursor, a, perp, py, axis, crops);
         if (cropY != -2147483648) {
            if (end == -2147483648) {
               nextY[0] = cropY;
            }

            end = a;
            this.rowCropY = cropY;
            gap = 0;
            gapAllWater = true;
         } else {
            gapAllWater = gapAllWater && this.init_Z_BlockPos_MutableBl(cursor, a, perp, py, axis);
            if (++gap > (gapAllWater ? 2 : 1)) {
               break;
            }
         }
      }

      return end;
   }

   private int cropYAt(MutableBlockPos cursor, int a, int perp, int py, Axis axis, Set<Block> crops) {
      for (int dy = -1; dy <= 1; dy++) {
         if (crops.contains(MC.level.getBlockState(setAlongAxis(cursor, a, perp, py + dy, axis)).getBlock())) {
            return py + dy;
         }
      }

      return -2147483648;
   }

   private boolean init_Z_BlockPos_MutableBl(MutableBlockPos cursor, int a, int perp, int py, Axis axis) {
      for (int dy = -1; dy <= 1; dy++) {
         Block block = MC.level.getBlockState(setAlongAxis(cursor, a, perp, py + dy, axis)).getBlock();
         if (block == Blocks.WATER || block == Blocks.LILY_PAD) {
            return true;
         }
      }

      return false;
   }

   private static MutableBlockPos setAlongAxis(MutableBlockPos cursor, int a, int perp, int y, Axis axis) {
      return axis == Axis.X ? cursor.set(a, y, perp) : cursor.set(perp, y, a);
   }

   private RotationMath.Rotation rowAimRotation(int along, int perp, int y, Axis axis, int sign, int end, Vec3 eye) {
      double goal = along + sign * 1.75;
      goal = sign > 0 ? Math.min(goal, end + 1.0) : Math.max(goal, end - 1.0);
      Vec3 point = axis == Axis.X ? new Vec3(goal + 0.5, y + 0.5, perp + 0.5) : new Vec3(perp + 0.5, y + 0.5, goal + 0.5);
      return eye.distanceToSqr(point) > this.reach() * this.reach() ? null : RotationMath.lookAt(point, eye);
   }

   private int facingSign(Axis axis) {
      float yaw = this.wireRotation().yaw();
      double component = axis == Axis.X ? -Math.sin(Math.toRadians(yaw)) : Math.cos(Math.toRadians(yaw));
      return component > 0.05 ? 1 : (component < -0.05 ? -1 : 0);
   }

   private RotationMath.Rotation plantRotation(BlockPos cell, ItemStack seedStack) {
      autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.plan(
         new autismclient.util.BlockPlacementPlanner.Query(
               cell, MC.player.getEyePosition(), this.reach(), autismclient.util.BlockPlacementPlanner.blockItemValidator(seedStack, MC.player, InteractionHand.MAIN_HAND)
            )
            .withFromRotation(this.wireRotation())
            .withMaxPitch(autismclient.util.BlockPlacementPlanner.defaultMaxPitchDegrees())
            .withSneaking(MC.player.isSecondaryUseActive())
            .withAutoSneak(false)
            .withRayBudget(new autismclient.util.BlockPlacementPlanner.RayBudget(48))
      );
      return candidate == null ? null : candidate.aim().goalRotation();
   }

   private RotationMath.Rotation tillRotation(BlockPos dirt) {
      RotationMath.Rotation rotation = this.init_ce_DataUtil_BlockPos_BlockPos(dirt, dirt.above());
      if (rotation != null) {
         return rotation;
      }

      Direction[] sides = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
      this.sortSidesByYaw(sides, dirt);

      for (Direction side : sides) {
         rotation = this.init_ce_DataUtil_BlockPos_BlockPos(dirt, dirt.relative(side));
         if (rotation != null) {
            return rotation;
         }
      }

      return null;
   }

   private RotationMath.Rotation init_ce_DataUtil_BlockPos_BlockPos(BlockPos dirt, BlockPos cell) {
      autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.plan(
         new autismclient.util.BlockPlacementPlanner.Query(
               cell, MC.player.getEyePosition(), this.reach(), (hit, target) -> hit.getBlockPos().equals(dirt) && hit.getDirection() != Direction.DOWN
            )
            .withFromRotation(this.wireRotation())
            .withMaxPitch(autismclient.util.BlockPlacementPlanner.defaultMaxPitchDegrees())
            .withSneaking(MC.player.isSecondaryUseActive())
            .withAutoSneak(false)
            .withRayBudget(new autismclient.util.BlockPlacementPlanner.RayBudget(48))
      );
      return candidate == null ? null : candidate.aim().goalRotation();
   }

   private void sortSidesByYaw(Direction[] sides, BlockPos dirt) {
      Vec3 eye = MC.player.getEyePosition();
      float wire = this.wireRotation().yaw();
      float[] keys = new float[sides.length];

      for (int i = 0; i < sides.length; i++) {
         keys[i] = Math.abs(RotationMath.angleDelta(wire, RotationMath.lookAt(Vec3.atCenterOf(dirt.relative(sides[i])), eye).yaw()));
      }

      for (int i = 1; i < sides.length; i++) {
         Direction side = sides[i];
         float key = keys[i];

         int j;
         for (j = i - 1; j >= 0 && keys[j] > key; j--) {
            sides[j + 1] = sides[j];
            keys[j + 1] = keys[j];
         }

         sides[j + 1] = side;
         keys[j + 1] = key;
      }
   }

   private boolean isTillable(BlockPos pos) {
      Block block = MC.level.getBlockState(pos).getBlock();
      return !TILLABLE.contains(block) ? false : block == Blocks.ROOTED_DIRT || MC.level.getBlockState(pos.above()).isAir();
   }

   private RotationMath.Rotation init_ce_DataUtil_RingBuffer_BufferH(AutoFarmModule.FarmTarget target) {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick == this.lastInteractTick) {
         return null;
      }

      if (BedDefenderModule.init_Z_10()) {
         return null;
      }

      if (SurroundModule.init_Z_27()) {
         return null;
      }

      if (CrystalAuraModule.init_Z_51()) {
         return null;
      }

      if (AnchorAuraModule.init_Z_2()) {
         return null;
      }

      if (this.rotationOwnedByOther()) {
         return null;
      }

      int switchAge = tick - this.lastSlotSwitchTick;
      if (switchAge >= 0 && switchAge <= this.slotSwitchSettleTicks) {
         return null;
      }

      if (tick < this.nextActionTick) {
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
      if (target.kind() != autismclient.util.FarmActionScheduler.ActionKind.HARVEST && !isPitchAllowed(wireRotation.pitch())) {
         return null;
      }

      RotationMath.Rotation silent = SilentMovementCoordinator.getRenderRotation(MC.player);
      if (silent != null && !rotationsMatch(silent, wireRotation)) {
         return null;
      }

      BlockHitResult ray = ScaffoldModule.raycast(MC.player.getEyePosition(), wireRotation, this.reach(), MC.level, MC.player);
      if (ray == null) {
         return null;
      }

      RotationMath.Rotation var10000;
      switch (target.kind()) {
         case HARVEST:
            var10000 = this.init_ce_DataUtil_RingBuffer_BufferH(target, ray, wireRotation, tick);
            break;
         case BONEMEAL:
            var10000 = this.bonemealRotation(target, ray, wireRotation, tick);
            break;
         case TILL:
            var10000 = this.tillRotation(target, ray, wireRotation, tick);
            break;
         case REPLANT_RECENT:
         case REPLANT:
            var10000 = this.replantRotation(target, ray, wireRotation, tick);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private RotationMath.Rotation init_ce_DataUtil_RingBuffer_BufferH(
      AutoFarmModule.FarmTarget target, BlockHitResult ray, RotationMath.Rotation wireRotation, int tick
   ) {
      BlockPos cell = ray.getBlockPos();
      BlockState state = MC.level.getBlockState(cell);
      if (this.cropBlocks().contains(state.getBlock()) && this.passesGrownFilter(state)) {
         if (autismclient.util.FarmCropCatalog.load(state.getBlock()) && !MC.level.getBlockState(cell.below()).is(state.getBlock())) {
            return null;
         }

         if (target.slot() >= 0) {
            if (!this.selectSlot(target.slot())) {
               return null;
            }

            if (this.fortuneLevel(MC.player.getMainHandItem()) <= 0) {
               return null;
            }
         }

         if (!this.init_Z_RingBuffer_BufferH(target, cell, state, tick)) {
            return null;
         }

         this.init_V_Z_3(true);
         if (!autismclient.util.SilentMiningCoordinator.init(this.id(), ray)) {
            return null;
         }

         this.init_V_I_6(tick);
         this.miningStartTick = tick;
         this.init_Z = true;
         this.init_V_BlockPos_2(cell, state.getBlock(), tick);
         return wireRotation;
      } else {
         return null;
      }
   }

   private RotationMath.Rotation replantRotation(AutoFarmModule.FarmTarget target, BlockHitResult ray, RotationMath.Rotation wireRotation, int tick) {
      boolean offhand = this.init_Z_Item_BlockHitResult(target.item(), ray);
      InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
      if (!offhand && target.slot() < 0) {
         if (MC.player.getOffhandItem().is(target.item()) && !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther(this.id())) {
            this.switchToHarvestTool(ray);
         }

         return null;
      } else {
         ItemStack planStack = offhand
            ? MC.player.getOffhandItem()
            : (target.slot() >= 0 ? MC.player.getInventory().getItem(target.slot()) : ItemStack.EMPTY);
         if (!planStack.is(target.item())) {
            return null;
         }

         BlockPlaceContext context = new BlockPlaceContext(MC.player, hand, planStack, ray);
         if (!context.canPlace()) {
            return null;
         }

         BlockPos cell = context.getClickedPos();
         if (!MC.level.getBlockState(cell).canBeReplaced()) {
            return null;
         }

         BlockState below = MC.level.getBlockState(cell.below());
         AutoFarmModule.SeedChoice actualChoice = this.bool("replant")
            ? this.replantSeedAt(cell.getX(), cell.getY(), cell.getZ(), below, this.cropBlocks(), tick)
            : null;
         if (actualChoice == null && this.bool("plant")) {
            actualChoice = this.init_RingBuffer_CachePo_I(cell.getX(), cell.getY(), cell.getZ(), below, this.cropBlocks());
         }

         if (actualChoice != null && actualChoice.crop() == target.crop() && actualChoice.seed() == target.item()) {
            BlockState placed = target.crop().getStateForPlacement(context);
            if (placed != null && placed.canSurvive(MC.level, cell) && MC.level.isUnobstructed(placed, cell, CollisionContext.placementContext(MC.player))) {
               BlockPos clicked = ray.getBlockPos();
               if (!autismclient.util.BlockPlacementPlanner.isValidSupportBlock(MC.level.getBlockState(clicked), clicked, MC.player.isSecondaryUseActive())) {
                  return null;
               }

               if (!offhand) {
                  if (!this.selectSlot(target.slot())) {
                     return null;
                  }

                  if (!MC.player.getMainHandItem().is(target.item())) {
                     return null;
                  }
               }

               if (al.init_Z_HitResult(ray, hand, this.id())) {
                  return null;
               }

               if (!this.canUseItem()) {
                  return null;
               }

               this.init_V_Z_3(false);
               if (offhand && !this.init_Z_Item_BlockHitResult(target.item(), ray)) {
                  return null;
               }

               if (!this.init_Z_RingBuffer_BufferH(target, cell, MC.level.getBlockState(cell), tick)) {
                  return null;
               }

               if (!autismclient.util.SilentMiningCoordinator.init(this.id(), ray, hand)) {
                  return null;
               }

               this.init_V_I_6(tick);
               this.miningStartTick = tick;
               this.init_Z = false;
               return wireRotation;
            } else {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   private boolean init_Z_BlockHitResult(BlockHitResult ray) {
      return this.init_Z_ItemStack_BlockHitResult(MC.player.getMainHandItem(), ray);
   }

   private boolean init_Z_ItemStack_BlockHitResult(ItemStack main, BlockHitResult ray) {
      BlockState clickedState = MC.level.getBlockState(ray.getBlockPos());
      if (!MC.player.isSecondaryUseActive()
         && clickedState.is(Blocks.SWEET_BERRY_BUSH)
         && clickedState.getValue(SweetBerryBushBlock.AGE) > 1
         && !main.is(Items.BONE_MEAL)) {
         return true;
      } else if (main.isEmpty()) {
         return false;
      } else if (MC.player.getCooldowns().isOnCooldown(main)) {
         return false;
      } else {
         Item item = main.getItem();
         if (item instanceof BlockItem) {
            return true;
         } else if (main.getUseAnimation() != ItemUseAnimation.NONE) {
            return true;
         } else if (main.has(DataComponents.CONSUMABLE)
            || main.has(DataComponents.EQUIPPABLE)
            || main.has(DataComponents.BLOCKS_ATTACKS)
            || main.has(DataComponents.KINETIC_WEAPON)) {
            return true;
         } else if (main.is(Items.EXPERIENCE_BOTTLE)
            || main.is(Items.ENDER_PEARL)
            || main.is(Items.SNOWBALL)
            || main.is(Items.EGG)
            || main.is(Items.FIREWORK_ROCKET)
            || main.is(Items.FISHING_ROD)
            || item instanceof BucketItem
            || main.is(Items.WIND_CHARGE)
            || main.is(Items.FIRE_CHARGE)
            || main.is(Items.ENDER_EYE)
            || main.is(Items.SPLASH_POTION)
            || main.is(Items.LINGERING_POTION)
            || main.is(Items.FLINT_AND_STEEL)
            || main.is(Items.BONE_MEAL)
            || main.is(Items.GLASS_BOTTLE)
            || main.is(Items.CARROT_ON_A_STICK)) {
            return true;
         } else if (item instanceof SpawnEggItem
            || item instanceof BoatItem
            || item instanceof MinecartItem
            || item instanceof HangingEntityItem
            || item instanceof ArmorStandItem
            || item instanceof EndCrystalItem
            || item instanceof PotionItem
            || item instanceof MapItem
            || item instanceof EmptyMapItem
            || item instanceof CompassItem
            || item instanceof HoneycombItem
            || item instanceof LeadItem
            || item instanceof BundleItem
            || item instanceof KnowledgeBookItem
            || item instanceof WritableBookItem
            || item instanceof WrittenBookItem
            || item instanceof DebugStickItem
            || item instanceof FoodOnAStickItem) {
            return true;
         } else if (Compat263.isHoe(item)) {
            return this.hoeCanUse(ray);
         } else if (Compat263.isAxe(item)) {
            return this.read_Z(ray.getBlockPos());
         } else if (Compat263.isShovel(item)) {
            return this.shovelCanUse(ray);
         } else {
            return item instanceof ShearsItem ? this.shearsCanUse(ray.getBlockPos()) : item.getClass() != Item.class && !(item instanceof MaceItem);
         }
      }
   }

   private boolean hoeCanUse(BlockHitResult ray) {
      return !this.isTillable(ray.getBlockPos()) ? false : MC.level.getBlockState(ray.getBlockPos()).is(Blocks.ROOTED_DIRT) || ray.getDirection() != Direction.DOWN;
   }

   private boolean read_Z(BlockPos pos) {
      BlockState state = MC.level.getBlockState(pos);
      return read_Set.contains(state.getBlock())
         || WeatheringCopper.getPrevious(state).isPresent()
         || HoneycombItem.WAX_OFF_BY_BLOCK.get().containsKey(state.getBlock());
   }

   private boolean shovelCanUse(BlockHitResult ray) {
      if (ray.getDirection() == Direction.DOWN) {
         return false;
      }

      BlockPos pos = ray.getBlockPos();
      BlockState state = MC.level.getBlockState(pos);
      return state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)
         ? true
         : load_Set.contains(state.getBlock()) && MC.level.getBlockState(pos.above()).isAir();
   }

   private boolean shearsCanUse(BlockPos pos) {
      BlockState state = MC.level.getBlockState(pos);
      Block block = state.getBlock();
      if (block instanceof PumpkinBlock) {
         return true;
      } else {
         return block instanceof BeehiveBlock && state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5
            ? true
            : block instanceof GrowingPlantHeadBlock plant && !plant.isMaxAge(state);
      }
   }

   private boolean switchToHarvestTool(BlockHitResult ray) {
      int selected = MC.player.getInventory().getSelectedSlot();
      int fortune = this.bool("fortune") ? this.fortuneSlot() : -1;
      if (fortune >= 0 && fortune != selected && !this.init_Z_ItemStack_BlockHitResult(MC.player.getInventory().getItem(fortune), ray)) {
         if (!this.init_Z_I_I_2(fortune, selected)) {
            return false;
         }

         this.switchedToSlot = fortune;
         return true;
      } else {
         for (int slot = 0; slot < 9; slot++) {
            if (slot != selected
               && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, this.id())
               && !this.init_Z_ItemStack_BlockHitResult(MC.player.getInventory().getItem(slot), ray)) {
               if (!this.init_Z_I_I_2(slot, selected)) {
                  return false;
               }

               this.switchedToSlot = slot;
               return true;
            }
         }

         return false;
      }
   }

   private RotationMath.Rotation tillRotation(AutoFarmModule.FarmTarget target, BlockHitResult ray, RotationMath.Rotation wireRotation, int tick) {
      if (!this.hoeCanUse(ray)) {
         return null;
      }

      ItemStack offStack = MC.player.getOffhandItem();
      boolean offhand = isHoe(offStack) && this.init_Z_Item_BlockHitResult(offStack.getItem(), ray);
      InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
      if (!offhand) {
         if (target.slot() < 0) {
            if (this.offhandHoeUsable()) {
               this.switchToHarvestTool(ray);
            }

            return null;
         }

         if (!this.selectSlot(target.slot())) {
            return null;
         }

         if (!isHoe(MC.player.getMainHandItem())) {
            return null;
         }
      }

      if (MC.player.getCooldowns().isOnCooldown(MC.player.getItemInHand(hand))) {
         return null;
      }

      if (al.init_Z_HitResult(ray, hand, this.id())) {
         return null;
      }

      if (!this.canUseItem()) {
         return null;
      }

      this.init_V_Z_3(false);
      if (offhand && !this.init_Z_Item_BlockHitResult(offStack.getItem(), ray)) {
         return null;
      }

      if (!this.init_Z_RingBuffer_BufferH(target, ray.getBlockPos(), MC.level.getBlockState(ray.getBlockPos()), tick)) {
         return null;
      }

      if (!autismclient.util.SilentMiningCoordinator.init(this.id(), ray, hand)) {
         return null;
      }

      this.init_V_I_6(tick);
      this.miningStartTick = tick;
      this.init_Z = false;
      return wireRotation;
   }

   private RotationMath.Rotation bonemealRotation(AutoFarmModule.FarmTarget target, BlockHitResult ray, RotationMath.Rotation wireRotation, int tick) {
      if (!this.isBonemealableCrop(ray.getBlockPos())) {
         return null;
      }

      boolean offhand = this.init_Z_Item_BlockHitResult(Items.BONE_MEAL, ray);
      InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
      if (!offhand) {
         if (target.slot() < 0) {
            if (MC.player.getOffhandItem().is(Items.BONE_MEAL) && !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther(this.id())) {
               this.switchToHarvestTool(ray);
            }

            return null;
         }

         if (!this.selectSlot(target.slot())) {
            return null;
         }

         if (!MC.player.getMainHandItem().is(Items.BONE_MEAL)) {
            return null;
         }
      }

      if (al.init_Z_HitResult(ray, hand, this.id())) {
         return null;
      }

      if (!this.canUseItem()) {
         return null;
      }

      this.init_V_Z_3(false);
      if (offhand && !this.init_Z_Item_BlockHitResult(Items.BONE_MEAL, ray)) {
         return null;
      }

      if (MC.player.getCooldowns().isOnCooldown(MC.player.getItemInHand(hand))) {
         return null;
      }

      if (!this.init_Z_RingBuffer_BufferH(target, ray.getBlockPos(), MC.level.getBlockState(ray.getBlockPos()), tick)) {
         return null;
      }

      if (!autismclient.util.SilentMiningCoordinator.init(this.id(), ray, hand)) {
         return null;
      }

      this.init_V_I_6(tick);
      this.miningStartTick = tick;
      this.init_Z = false;
      return wireRotation;
   }

   private boolean canUseItem() {
      boolean useKeyDown = autismclient.util.SilentMiningCoordinator.isOwner(this.id()) && !this.init_Z;
      return useKeyDown || ((AutismMinecraftAccessor)MC).autism$getRightClickDelay() <= 0;
   }

   private void init_V_I_6(int tick) {
      this.lastInteractTick = tick;
   }

   private void init_V_Z_3(boolean attack) {
      if (autismclient.util.SilentMiningCoordinator.isOwner(this.id()) && this.init_Z != attack) {
         autismclient.util.SilentMiningCoordinator.release(this.id());
      }
   }

   private void releaseMining() {
      autismclient.util.SilentMiningCoordinator.release(this.id());
      this.miningStartTick = -2147483648;
   }

   private void releaseStaleMining() {
      if (this.miningStartTick != AutismSessionState.getInstance().getClientTick()) {
         this.releaseMining();
      }
   }

   private void init_V_BlockPos_2(BlockPos pos, Block crop, int tick) {
      this.forgetNearbyActions(pos);
      if ((this.bool("replant") || this.bool("plant")) && autismclient.util.FarmCropCatalog.seedFor(crop) != null && this.harvestedCells.get(pos.asLong()) == null) {
         this.harvestedCells.init(pos.asLong(), new AutoFarmModule.HarvestedCell(pos.immutable(), crop, tick, AiServerEvidence.blockRevision(pos), false));
      }
   }

   private boolean init_Z_RingBuffer_StreamU(AutoFarmModule.HarvestedCell entry) {
      int now = AutismSessionState.getInstance().getClientTick();
      ClientPacketListener connection = MC.getConnection();
      PlayerInfo info = connection == null ? null : connection.getPlayerInfo(MC.player.getUUID());
      if (entry.init_Z() && !autismclient.util.BlockBreakGraceCalc.init(now, entry.tick(), info == null ? 0 : info.getLatency())) {
         AiServerEvidence.BlockUpdate update = AiServerEvidence.lastBlockUpdate(entry.pos());
         return update == null || update.revision() <= entry.revision() || !update.state().isAir();
      } else {
         return true;
      }
   }

   private void sync_V_3() {
      int tick = AutismSessionState.getInstance().getClientTick();
      this.harvestedCells
         .entries()
         .removeIf(
            entry -> {
               BlockPos pos = entry.pos();
               if (MC.level.hasChunkAt(pos) && tick != entry.tick()) {
                  BlockState state = MC.level.getBlockState(pos);
                  if (state.is(entry.block())) {
                     AiServerEvidence.BlockUpdate update = AiServerEvidence.lastBlockUpdate(pos);
                     return !this.isFullyGrown(state)
                        && update != null
                        && update.revision() > entry.revision()
                        && update.state().is(entry.block())
                        && !this.isFullyGrown(update.state());
                  }

                  if (!state.canBeReplaced()) {
                     return true;
                  }

                  AiServerEvidence.BlockUpdate update = AiServerEvidence.lastBlockUpdate(pos);
                  if (!entry.init_Z() && update != null && update.revision() > entry.revision() && update.state().isAir()) {
                     this.harvestedCells.init(pos.asLong(), new AutoFarmModule.HarvestedCell(pos, entry.block(), tick, entry.revision(), true));
                  }

                  return !this.canPlantOn(entry.block(), MC.level.getBlockState(pos.below()), pos.getX(), pos.getY(), pos.getZ());
               } else {
                  return false;
               }
            }
         );
   }

   private boolean init_Z_RingBuffer_BufferH(AutoFarmModule.FarmTarget target, BlockPos cell, BlockState state, int tick) {
      int limit = 40;
      if (target.kind() == autismclient.util.FarmActionScheduler.ActionKind.HARVEST) {
         float progress = state.getDestroyProgress(MC.player, MC.level, cell);
         if (progress > 0.0F && Float.isFinite(progress)) {
            limit += (int)Math.min(1200.0, Math.ceil(1.0 / progress));
         }
      }

      if (this.retryLimiter.init(cell.asLong(), new AutoFarmModule.RetryKey(target.kind(), state), tick, limit)) {
         return true;
      }

      this.markActed(cell, tick);
      this.markActed(target.cell(), tick);
      this.releaseMining();
      return false;
   }

   private void forgetNearbyActions(BlockPos pos) {
      if (!this.actedCells.isEmpty()) {
         int px = pos.getX();
         int py = pos.getY();
         int pz = pos.getZ();
         this.actedCells.keySet().removeIf(key -> {
            if (Math.abs(BlockPos.getX(key) - px) <= 2 && Math.abs(BlockPos.getY(key) - py) <= 2 && Math.abs(BlockPos.getZ(key) - pz) <= 2) {
               this.init_Long2ObjectOpenHas.remove(key);
               return true;
            } else {
               return false;
            }
         });
      }
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
      return this.poll_Z_2();
   }

   @Override
   public boolean shouldCancelAttack(HitResult hitResult) {
      return this.poll_Z_2()
         ? true
         : hitResult instanceof EntityHitResult && this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && autismclient.util.SilentRotationManager.hasActiveRotation();
   }

   private boolean poll_Z_2() {
      if (this.miningStartTick != -2147483648 && autismclient.util.SilentMiningCoordinator.isOwner(this.id())) {
         int tick = AutismSessionState.getInstance().getClientTick();
         int age = tick - this.miningStartTick;
         return age >= 0 && age <= 1;
      } else {
         return false;
      }
   }

   private static boolean isPitchAllowed(float pitch) {
      return ScaffoldModule.isBelowPitchLimit(Math.abs(pitch));
   }

   private static boolean rotationsMatch(RotationMath.Rotation first, RotationMath.Rotation second) {
      float epsilon = (float)Math.max(0.05000000074505806, RotationMath.gcdStep() + 0.05000000074505806);
      return Math.abs(RotationMath.angleDelta(first.yaw(), second.yaw())) <= epsilon && Math.abs(first.pitch() - second.pitch()) <= epsilon;
   }

   private boolean selectSlot(int slot) {
      int selected = MC.player.getInventory().getSelectedSlot();
      if (selected == slot) {
         return true;
      }

      if (!this.init_Z_I_I_2(slot, selected)) {
         return false;
      }

      this.switchedToSlot = slot;
      return false;
   }

   private boolean init_Z_I_I_2(int slot, int selected) {
      if (BedDefenderModule.init_Z_10() || SurroundModule.init_Z_27() || CrystalAuraModule.init_Z_51() || AnchorAuraModule.init_Z_2()) {
         return false;
      }

      if (this.rotationOwnedByOther()) {
         return false;
      }

      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick == this.lastSlotSwitchTick) {
         return false;
      }

      if (!autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
         return false;
      }

      try {
         if (this.switchBackSlot < 0) {
            this.switchBackSlot = selected;
         }

         if (slot < 0 || slot >= MC.options.keyHotbarSlots.length || autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, this.id())) {
            return false;
         }

         autismclient.util.KeyMappingControl key = autismclient.util.KeyMappingControl.of(MC.options.keyHotbarSlots[slot]);
         key.autism$simulatePress(true);
         key.autism$simulatePress(false);
         key.autism$resetPressedState();
      } finally {
         autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
      }

      this.lastSlotSwitchTick = tick;
      this.slotSwitchSettleTicks = this.random.nextInt(3);
      return true;
   }

   private int hotbarSlotOf(Item item) {
      this.scanHotbar();
      return this.hotbarSlots.getOrDefault(item, -1);
   }

   private void scanHotbar() {
      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick != this.toolScanTick) {
         this.toolScanTick = tick;
         this.nearestHoeSlot = -1;
         this.bestFortuneSlot = -1;
         this.hotbarSlots.clear();
         int selected = MC.player.getInventory().getSelectedSlot();
         int bestFortuneDistance = 2147483647;
         boolean bestFortuneHoe = false;

         for (int slot = 0; slot < 9; slot++) {
            if (!autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, this.id())) {
               ItemStack stack = MC.player.getInventory().getItem(slot);
               if (!stack.isEmpty()) {
                  int distance = Math.abs(slot - selected);
                  this.hotbarSlots
                     .merge(
                        stack.getItem(), slot, (oldSlot, newSlot) -> (Integer)(Math.abs(newSlot - selected) < Math.abs(oldSlot - selected) ? newSlot : oldSlot)
                     );
                  boolean hoe = isHoe(stack);
                  if (hoe && (this.nearestHoeSlot < 0 || distance < Math.abs(this.nearestHoeSlot - selected))) {
                     this.nearestHoeSlot = slot;
                  }

                  if (this.fortuneLevel(stack) > 0 && (this.bestFortuneSlot < 0 || hoe && !bestFortuneHoe || hoe == bestFortuneHoe && distance < bestFortuneDistance)) {
                     this.bestFortuneSlot = slot;
                     bestFortuneHoe = hoe;
                     bestFortuneDistance = distance;
                  }
               }
            }
         }

         ItemStack offhand = MC.player.getOffhandItem();
         this.offhandItem = !offhand.isEmpty() && !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther(this.id()) ? offhand.getItem() : null;
      }
   }

   private boolean offhandHolds(Item item) {
      this.scanHotbar();
      return this.offhandItem == item;
   }

   private boolean init_Z_Item_BlockHitResult(Item item, BlockHitResult ray) {
      return MC.player.getOffhandItem().is(item) && !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther(this.id()) && !this.init_Z_BlockHitResult(ray);
   }

   private boolean offhandHoeUsable() {
      return !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther(this.id()) && isHoe(MC.player.getOffhandItem());
   }

   private boolean hasUsableHoe() {
      return this.load_I_6() >= 0 || this.offhandHoeUsable();
   }

   private int load_I_6() {
      this.scanHotbar();
      return this.nearestHoeSlot;
   }

   private int boneMealSlot() {
      return this.hotbarSlotOf(Items.BONE_MEAL);
   }

   private boolean hasBoneMeal() {
      return this.offhandHolds(Items.BONE_MEAL) || this.boneMealSlot() >= 0;
   }

   private int fortuneSlot() {
      this.scanHotbar();
      return this.bestFortuneSlot;
   }

   private static boolean isHoe(ItemStack stack) {
      return Compat263.isHoe(stack.getItem());
   }

   private int fortuneLevel(ItemStack stack) {
      try {
         if (MC != null && MC.level != null && stack != null && !stack.isEmpty()) {
            if (this.fortuneLookupLevel != MC.level) {
               this.fortuneLookupLevel = MC.level;
               this.fortune = MC.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
            }

            return EnchantmentHelper.getItemEnchantmentLevel(this.fortune, stack);
         } else {
            return 0;
         }
      } catch (Throwable t) {
         return 0;
      }
   }

   private void scheduleSwitchBack() {
      if (this.switchBackSlot >= 0 && this.switchBackDelay <= 0) {
         this.switchBackDelay = 2 + this.random.nextInt(3);
      }
   }

   private void tickSwitchBack() {
      if (this.switchBackSlot >= 0) {
         if (!this.bool("switch-back")) {
            this.switchBackSlot = -1;
            this.switchedToSlot = -1;
            this.switchBackDelay = 0;
         } else if (MC.gui != null && MC.gui.screen() == null && MC.gui.overlay() == null) {
            if (this.switchedToSlot >= 0 && MC.player.getInventory().getSelectedSlot() != this.switchedToSlot) {
               this.switchBackSlot = -1;
               this.switchedToSlot = -1;
               this.switchBackDelay = 0;
            } else if (this.switchBackDelay > 0 && --this.switchBackDelay <= 0) {
               if (!autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(this.switchBackSlot, this.id()) && this.init_Z_I_I_2(this.switchBackSlot, MC.player.getInventory().getSelectedSlot())) {
                  this.slotSwitchSettleTicks = 0;
                  this.switchBackSlot = -1;
                  this.switchedToSlot = -1;
               } else {
                  this.switchBackDelay = 1;
               }
            }
         }
      }
   }

   private void poll_V_2() {
      int tick = AutismSessionState.getInstance().getClientTick();
      BlockPos goal = this.walkGoal;
      if (goal != null && (GridWalkNavigator.write(goal) || this.actionFor(goal, tick) == null)) {
         goal = null;
         this.walkGoal = null;
         this.lastGoalSearchTick = tick - 20;
      }

      if (goal == null && (this.lastGoalSearchTick == -2147483648 || tick - this.lastGoalSearchTick >= 20)) {
         goal = this.searchGoal();
         if (!this.isSearchActive()) {
            this.lastGoalSearchTick = tick;
         }

         this.walkGoal = goal;
      }

      if (goal == null) {
         GridWalkNavigator.load();
         this.requestRotation(null);
      } else if (!GridWalkNavigator.read(goal)) {
         if (GridWalkNavigator.load(goal)) {
            this.walkGoal = null;
            GridWalkNavigator.load();
            this.nextActionTick = tick + 2 + this.random.nextInt(3);
            this.lastGoalSearchTick = tick - 20;
            this.requestRotation(null);
         } else {
            GridWalkNavigator.load();
            GridWalkNavigator.init_V_BlockPos_I(goal, 200);
            this.walkGoal = null;
            this.lastGoalSearchTick = tick - 20;
            this.requestRotation(null);
         }
      } else {
         this.status = "Walk";
         BlockPos walkNode = GridWalkNavigator.nextNode();
         this.requestRotation(RotationMath.lookAt(Vec3.atCenterOf(walkNode != null ? walkNode : goal), MC.player.getEyePosition()));
      }
   }

   private boolean init_Z_RingBuffer_BufferH(AutoFarmModule.FarmTarget target) {
      int tick = AutismSessionState.getInstance().getClientTick();
      Vec3 eye = MC.player.getEyePosition();
      double reachSqr = this.reach() * this.reach();
      BlockPos goal = this.init_BlockPos_RingBuffer_BufferH(target, eye, reachSqr, tick);
      if (goal == null) {
         return false;
      } else if (GridWalkNavigator.read(goal)) {
         this.lastGoalSearchTick = tick - 20;
         return true;
      } else {
         this.read_BlockPos = null;
         if (GridWalkNavigator.load(goal)) {
            GridWalkNavigator.load();
            return false;
         } else {
            GridWalkNavigator.load();
            GridWalkNavigator.init_V_BlockPos_I(goal, 200);
            this.queue = tick - 20;
            return false;
         }
      }
   }

   private BlockPos init_BlockPos_RingBuffer_BufferH(AutoFarmModule.FarmTarget target, Vec3 eye, double reachSqr, int tick) {
      if (target.kind() == autismclient.util.FarmActionScheduler.ActionKind.HARVEST && this.rowEndTick == tick && this.rowEnd != null) {
         return distanceSqToCell(eye, this.rowEnd.getX(), this.rowEnd.getY(), this.rowEnd.getZ()) > reachSqr
               && !GridWalkNavigator.write(this.rowEnd)
               && this.actionFor(this.rowEnd, tick) != null
            ? this.rowEnd
            : null;
      }

      BlockPos goal = this.read_BlockPos;
      if (goal != null
         && (GridWalkNavigator.write(goal) || this.actionFor(goal, tick) == null || distanceSqToCell(eye, goal.getX(), goal.getY(), goal.getZ()) <= reachSqr)) {
         goal = null;
         this.read_BlockPos = null;
         this.queue = tick - 20;
      }

      if (goal == null && (this.queue == -2147483648 || tick - this.queue >= 20)) {
         goal = this.searchGoal(reachSqr, 90.0F, Math.min(12, this.searchRange()));
         if (!this.isSearchActive()) {
            this.queue = tick;
         }

         this.read_BlockPos = goal;
      }

      return goal;
   }

   private autismclient.util.FarmActionScheduler.ActionKind actionFor(BlockPos goal, int tick) {
      Set<Block> crops = this.cropBlocks();
      if (crops.isEmpty()) {
         return null;
      }

      boolean wantReplant = this.bool("replant");
      boolean wantPlant = this.bool("plant");
      boolean wantTill = this.bool("till") && this.hasUsableHoe();
      boolean wantBonemeal = this.bool("bonemeal") && this.hasBoneMeal();
      return this.actionAt(
         new MutableBlockPos(goal.getX(), goal.getY(), goal.getZ()),
         goal.getX(),
         goal.getY(),
         goal.getZ(),
         crops,
         tick,
         wantReplant,
         wantPlant,
         wantTill,
         wantBonemeal
      );
   }

   private BlockPos searchGoal() {
      return this.searchGoal(0.0, 360.0F, this.searchRange());
   }

   private boolean isSearchActive() {
      return this.searchActive;
   }

   private BlockPos searchGoal(double minDistSqr, float maxCorrection, int maxRing) {
      Set<Block> crops = this.cropBlocks();
      if (crops.isEmpty()) {
         this.searchActive = false;
         return null;
      }

      BlockPos origin = MC.player.blockPosition();
      int manhattan = this.searchRange();
      if (this.searchActive
         && (
            this.searchMinDistSqr != minDistSqr
               || this.searchMaxCorrection != maxCorrection
               || this.searchMaxRing != maxRing
               || this.searchManhattan != manhattan
               || origin.getX() != this.searchOriginX
               || origin.getY() != this.searchOriginY
               || origin.getZ() != this.searchOriginZ
         )) {
         this.searchActive = false;
      }

      if (!this.searchActive) {
         this.searchActive = true;
         this.searchMinDistSqr = minDistSqr;
         this.searchMaxCorrection = maxCorrection;
         this.searchMaxRing = maxRing;
         this.searchManhattan = manhattan;
         this.searchMaxRingSqr = (long)maxRing * maxRing;
         this.searchOriginX = origin.getX();
         this.searchOriginY = origin.getY();
         this.searchOriginZ = origin.getZ();
         this.searchPlayerX = MC.player.getX();
         this.searchPlayerZ = MC.player.getZ();
         this.searchEye = MC.player.getEyePosition();
         this.searchYaw = this.wireRotation().yaw();
         this.searchRing = 0;
         this.searchEdgeIndex = 0;
         this.f_i_I = 0;
         this.f_j_I = 0;
         this.candidates.clear();
      }

      int tick = AutismSessionState.getInstance().getClientTick();
      boolean wantReplant = this.bool("replant");
      boolean wantPlant = this.bool("plant");
      boolean wantTill = this.bool("till") && this.hasUsableHoe();
      boolean wantBonemeal = this.bool("bonemeal") && this.hasBoneMeal();
      MutableBlockPos cursor = new MutableBlockPos();
      int budget = 8000;

      while (this.searchActive) {
         if (budget <= 0) {
            return null;
         }

         if (this.searchRing > this.searchMaxRing) {
            this.searchActive = false;
            break;
         }

         int edgeCount = this.searchRing == 0 ? 1 : this.searchRing * 8;
         if (this.searchEdgeIndex >= edgeCount) {
            this.searchRing++;
            this.searchEdgeIndex = 0;
            this.f_i_I = 0;
            this.f_j_I = 0;
         } else {
            int dx;
            int dz;
            if (this.searchRing == 0) {
               dx = 0;
               dz = 0;
            } else {
               int side = this.searchEdgeIndex / (this.searchRing * 2);
               int offset = this.searchEdgeIndex % (this.searchRing * 2) - this.searchRing;
               switch (side) {
                  case 0:
                     dx = offset;
                     dz = -this.searchRing;
                     break;
                  case 1:
                     dx = offset + 1;
                     dz = this.searchRing;
                     break;
                  case 2:
                     dx = -this.searchRing;
                     dz = offset + 1;
                     break;
                  default:
                     dx = this.searchRing;
                     dz = offset;
               }
            }

            long horizontalSqr = (long)dx * dx + (long)dz * dz;
            if (Math.abs(dx) + Math.abs(dz) > this.searchManhattan) {
               this.searchEdgeIndex++;
            } else if (this.f_i_I > 12) {
               this.searchEdgeIndex++;
               this.f_i_I = 0;
               this.f_j_I = 0;
            } else {
               int syLimit = this.f_i_I == 0 ? 1 : 2;
               if (this.f_j_I >= syLimit) {
                  this.f_i_I++;
                  this.f_j_I = 0;
               } else {
                  int dy = this.f_j_I == 0 ? this.f_i_I : -this.f_i_I;
                  this.f_j_I++;
                  if (horizontalSqr + (long)dy * dy <= this.searchMaxRingSqr) {
                     budget--;
                     int cx = this.searchOriginX + dx;
                     int cy = this.searchOriginY + dy;
                     int cz = this.searchOriginZ + dz;
                     cursor.set(cx, cy, cz);
                     if (MC.level.hasChunkAt(cursor)
                        && !GridWalkNavigator.write(cursor)
                        && this.actionAt(cursor, cx, cy, cz, crops, tick, wantReplant, wantPlant, wantTill, wantBonemeal) != null
                        && (!(this.searchMinDistSqr > 0.0) || !(distanceSqToCell(this.searchEye, cx, cy, cz) <= this.searchMinDistSqr))) {
                        float correction = Math.abs(RotationMath.angleDelta(this.searchYaw, yawToCell(this.searchPlayerX, this.searchPlayerZ, cx, cz)));
                        if (!(correction > this.searchMaxCorrection)) {
                           this.offerCandidate(new BlockPos(cx, cy, cz), Math.sqrt(distanceSqToCell(this.searchEye, cx, cy, cz)) * 10.0 + correction / 18.0);
                        }
                     }
                  }
               }
            }
         }
      }

      return this.bestCandidate(tick);
   }

   private void offerCandidate(BlockPos cell, double estimate) {
      for (int i = 0; i < this.candidates.size(); i++) {
         AutoFarmModule.CellCandidate old = this.candidates.get(i);
         if (old.cell().distSqr(cell) <= 9.0) {
            if (old.cost() > estimate) {
               this.candidates.set(i, new AutoFarmModule.CellCandidate(cell, estimate));
            }

            return;
         }
      }

      this.candidates.add(new AutoFarmModule.CellCandidate(cell, estimate));
      this.candidates.sort(Comparator.comparingDouble(AutoFarmModule.CellCandidate::cost));
      if (this.candidates.size() > 6) {
         this.candidates.removeLast();
      }
   }

   private BlockPos bestCandidate(int tick) {
      BlockPos best = null;
      double bestCost = 1.0 / 0.0;

      for (AutoFarmModule.CellCandidate choice : this.candidates) {
         BlockPos cell = choice.cell();
         if (this.actionFor(cell, tick) != null && !GridWalkNavigator.write(cell)) {
            double cost = GridWalkNavigator.pathCost(cell);
            if (Double.isFinite(cost)) {
               int neighbours = 0;

               for (Direction direction : Plane.HORIZONTAL) {
                  if (this.actionFor(cell.relative(direction), tick) != null) {
                     neighbours++;
                  }
               }

               float correction = Math.abs(RotationMath.angleDelta(this.wireRotation().yaw(), yawToCell(MC.player.getX(), MC.player.getZ(), cell.getX(), cell.getZ())));
               cost += correction / 18.0 - neighbours;
               if (cost < bestCost) {
                  bestCost = cost;
                  best = cell;
               }
            }
         }
      }

      this.candidates.clear();
      return best;
   }

   private static float yawToCell(double px, double pz, int x, int z) {
      return (float)(Math.toDegrees(Math.atan2(z + 0.5 - pz, x + 0.5 - px)) - 90.0);
   }

   private autismclient.util.FarmActionScheduler.ActionKind actionAt(
      MutableBlockPos cursor,
      int cx,
      int cy,
      int cz,
      Set<Block> crops,
      int tick,
      boolean wantReplant,
      boolean wantPlant,
      boolean wantTill,
      boolean wantBonemeal
   ) {
      BlockState state = MC.level.getBlockState(cursor);
      Block block = state.getBlock();
      if (crops.contains(block)) {
         if (this.passesGrownFilter(state) && this.init_Z_BlockPos_MutableBl_2(cursor, cx, cy, cz, block)) {
            return autismclient.util.FarmActionScheduler.ActionKind.HARVEST;
         } else {
            return wantBonemeal && this.isBonemealTarget(cursor.set(cx, cy, cz), state) ? autismclient.util.FarmActionScheduler.ActionKind.BONEMEAL : null;
         }
      } else if ((wantReplant || wantPlant) && state.canBeReplaced()) {
         BlockState below = MC.level.getBlockState(cursor.set(cx, cy - 1, cz));
         if (wantReplant && this.replantSeedAt(cx, cy, cz, below, crops, tick) != null) {
            return autismclient.util.FarmActionScheduler.ActionKind.REPLANT_RECENT;
         } else {
            return wantPlant && this.init_RingBuffer_CachePo_I(cx, cy, cz, below, crops) != null ? autismclient.util.FarmActionScheduler.ActionKind.REPLANT : null;
         }
      } else {
         return wantTill && TILLABLE.contains(block) && (block == Blocks.ROOTED_DIRT || MC.level.getBlockState(cursor.set(cx, cy + 1, cz)).isAir())
            ? autismclient.util.FarmActionScheduler.ActionKind.TILL
            : null;
      }
   }

   private static double distanceSqToCell(Vec3 eye, int x, int y, int z) {
      double dx = eye.x < x ? x - eye.x : (eye.x > x + 1 ? eye.x - (x + 1) : 0.0);
      double dy = eye.y < y ? y - eye.y : (eye.y > y + 1 ? eye.y - (y + 1) : 0.0);
      double dz = eye.z < z ? z - eye.z : (eye.z > z + 1 ? eye.z - (z + 1) : 0.0);
      return dx * dx + dy * dy + dz * dz;
   }

   private static int[] sphereOffsets(int radius) {
      int clamped = Mth.clamp(radius, 0, 8);
      if (clamped == sphereOffsetRadius) {
         return sphereOffsetCache;
      }

      int limit = clamped * clamped;
      List<int[]> cells = new ArrayList<>();

      for (int x = -clamped; x <= clamped; x++) {
         for (int y = -clamped; y <= clamped; y++) {
            for (int z = -clamped; z <= clamped; z++) {
               if (x * x + y * y + z * z <= limit) {
                  cells.add(new int[]{x, y, z});
               }
            }
         }
      }

      cells.sort(Comparator.comparingInt(cellx -> cellx[0] * cellx[0] + cellx[1] * cellx[1] + cellx[2] * cellx[2]));
      int[] flat = new int[cells.size() * 3];

      for (int i = 0; i < cells.size(); i++) {
         int[] cell = cells.get(i);
         flat[i * 3] = cell[0];
         flat[i * 3 + 1] = cell[1];
         flat[i * 3 + 2] = cell[2];
      }

      sphereOffsetRadius = clamped;
      sphereOffsetCache = flat;
      return flat;
   }

   private record FarmTarget(
      autismclient.util.FarmActionScheduler.ActionKind kind,
      BlockPos cell,
      Block crop,
      Item item,
      int slot,
      RotationMath.Rotation rotation
   ) {
   }

   private record SeedChoice(Block crop, Item seed, int slot) {
   }

   private record CellCandidate(BlockPos cell, double cost) {
   }

   private record RetryKey(autismclient.util.FarmActionScheduler.ActionKind kind, BlockState state) {
   }

   private record HarvestedCell(BlockPos pos, Block block, int tick, long revision, boolean init_Z) {
   }
}
