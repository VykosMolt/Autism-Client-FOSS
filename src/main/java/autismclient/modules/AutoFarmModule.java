package autismclient.modules;

import autismclient.ai.ca;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismMinecraftAccessor;
import autismclient.util.AutismLiteVariant;
import autismclient.util.bA;
import autismclient.util.ce;
import autismclient.util.ch;
import autismclient.util.ck;
import autismclient.util.cn;
import autismclient.util.di;
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

public final class AutoFarmModule extends Module implements cn.Inner_DataUtil {
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
   private static final Set<Block> init_Set = Set.of(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.DIRT_PATH, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT);
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
   private final Random init_Random = new Random();
   private final autismclient.util.ac<AutoFarmModule.Inner_StreamUtil> init_ac = new autismclient.util.ac<>(4096);
   private int start = -2147483648;
   private int stop = -2147483648;
   private boolean init_Z;
   private int next = -2147483648;
   private int prev = -1;
   private int valid = -1;
   private int size;
   private int data = -2147483648;
   private int value;
   private BlockPos init_BlockPos;
   private int cache = -2147483648;
   private BlockPos load_BlockPos;
   private int state = -2147483648;
   private BlockPos read_BlockPos;
   private int queue = -2147483648;
   private BlockPos write_BlockPos;
   private autismclient.util.aa.Inner_DataUtil init_aa_DataUtil;
   private int buffer = -2147483648;
   private final Long2IntOpenHashMap init_Long2IntOpenHashMa = new Long2IntOpenHashMap();
   private final Long2ObjectOpenHashMap<Block> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   private BlockPos update_BlockPos;
   private autismclient.util.aa.Inner_DataUtil load_aa_DataUtil;
   private int entry;
   private BlockPos sync_BlockPos;
   private autismclient.util.aa.Inner_DataUtil read_aa_DataUtil;
   private ce.Inner_DataUtil init_ce_DataUtil;
   private Vec3 init_Vec3;
   private static final int node = 20;
   private static final int task = 10;
   private BlockPos flush_BlockPos;
   private autismclient.util.aa.Inner_DataUtil write_aa_DataUtil;
   private float write_F = 3.4028235E38F;
   private int pool;
   private final Long2IntOpenHashMap load_Long2IntOpenHashMa = new Long2IntOpenHashMap();
   private static final int index = 3;
   private ce.Inner_DataUtil load_ce_DataUtil;
   private int count = -2147483648;
   private int flags = -2147483648;
   private Axis init_Direction_Axis;
   private int level = 1;
   private final int[] init_IArr = new int[1];
   private int frame;
   private ClientLevel init_ClientLevel;
   private String init_Str = "";
   private Set<Block> write_Set = Set.of();
   private String load_Str = "";
   private static int[] load_IArr = new int[0];
   private static int block = -1;
   private String read_Str;
   private int chunk = -2147483648;
   private int table;
   private boolean load_Z;
   private static final int store = 3;
   private BlockPos close_BlockPos;
   private int drain;
   private int fill = -2147483648;
   private boolean read_Z;
   private final Map<IntegerProperty, Integer> init_Map = new IdentityHashMap<>();
   private final Map<BlockState, Boolean> load_Map = new IdentityHashMap<>();
   private final autismclient.util.X<AutoFarmModule.Inner_DataUtil> init_X = new autismclient.util.X<>();
   private int mark = -2147483648;
   private int scan = -2147483648;
   private ce.Inner_DataUtil read_ce_DataUtil;
   private int parse = -1;
   private int bind = -1;
   private Item init_Item;
   private final Map<Item, Integer> read_Map = new HashMap<>();
   private Holder<Enchantment> init_Holder;
   private ClientLevel load_ClientLevel;
   private static final int f_a_I = 8000;
   private boolean write_Z;
   private double load_D;
   private float update_F;
   private int f_b_I;
   private int f_c_I;
   private long init_J;
   private int f_d_I;
   private int f_e_I;
   private int f_f_I;
   private double read_D;
   private double write_D;
   private Vec3 load_Vec3;
   private float sync_F;
   private int f_g_I;
   private int f_h_I;
   private int f_i_I;
   private int f_j_I;
   private final List<AutoFarmModule.Inner_ConfigStore> init_List = new ArrayList<>();

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
      this.init_V_15();
   }

   @Override
   public void onDisable() {
      this.init_V_15();
      autismclient.util.aD.init(this.id());
      autismclient.util.ah.update_V(this.id());
      bA.init_V();
   }

   @Override
   public void onGameLeft() {
      this.init_V_15();
      if (this.sync_Z_3()) {
         autismclient.util.aD.load_V();
      }

      autismclient.util.ah.update_V(this.id());
      bA.init_V();
   }

   private void init_V_15() {
      this.start = -2147483648;
      this.mark = -2147483648;
      this.scan = -2147483648;
      this.read_ce_DataUtil = null;
      this.fill = -2147483648;
      this.chunk = -2147483648;
      this.read_Str = null;
      this.init_ClientLevel = null;
      this.prev = -1;
      this.valid = -1;
      this.size = 0;
      this.data = -2147483648;
      this.init_ac.init_V();
      this.init_X.init();
      this.init_BlockPos = null;
      this.cache = -2147483648;
      this.load_BlockPos = null;
      this.state = -2147483648;
      this.read_BlockPos = null;
      this.queue = -2147483648;
      this.write_BlockPos = null;
      this.init_aa_DataUtil = null;
      this.buffer = -2147483648;
      this.init_Long2IntOpenHashMa.clear();
      this.init_Long2ObjectOpenHas.clear();
      this.update_BlockPos = null;
      this.load_aa_DataUtil = null;
      this.entry = 0;
      this.close_BlockPos = null;
      this.drain = 0;
      this.sync_BlockPos = null;
      this.read_aa_DataUtil = null;
      this.init_ce_DataUtil = null;
      this.init_Vec3 = null;
      this.flush_BlockPos = null;
      this.write_aa_DataUtil = null;
      this.write_F = 3.4028235E38F;
      this.pool = 0;
      this.load_Long2IntOpenHashMa.clear();
      this.load_ce_DataUtil = null;
      this.count = -2147483648;
      this.flags = -2147483648;
      this.init_Direction_Axis = null;
      this.level = 1;
      this.next = -2147483648;
      this.write_Z = false;
      this.write_V_8();
      this.load_Str = "";
   }

   @Override
   public String info() {
      return this.load_Str;
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && MC != null && MC.player != null && this.sync_Z_3()) {
         autismclient.util.aD.init_V_Str_LocalPlayer(this.id(), MC.player);
      }
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return autismclient.util.aD.read_Z() && this.sync_Z_3();
   }

   @Override
   public boolean load_Z_23() {
      return cn.init_Z() ? false : autismclient.util.aD.init_Z() || this.isEnabled() && this.read_Z_8();
   }

   @Override
   public void preMovementTick() {
      if (!AutismLiteVariant.enabled()) {
         if (MC != null && MC.player != null && MC.level != null && MC.gameMode != null) {
            if (MC.level != this.init_ClientLevel) {
               this.init_ClientLevel = MC.level;
               this.mark = -2147483648;
               this.scan = -2147483648;
               this.read_ce_DataUtil = null;
               this.read_Str = null;
               this.init_ac.init_V();
               this.init_X.init();
               this.init_BlockPos = null;
               this.load_BlockPos = null;
               this.state = -2147483648;
               this.read_BlockPos = null;
               this.queue = -2147483648;
               this.cache = -2147483648;
               this.write_Z = false;
               this.next = -2147483648;
               this.write_BlockPos = null;
               this.init_Direction_Axis = null;
               this.init_Long2IntOpenHashMa.clear();
               this.init_Long2ObjectOpenHas.clear();
               this.update_BlockPos = null;
               this.load_aa_DataUtil = null;
               this.entry = 0;
               this.close_BlockPos = null;
               this.drain = 0;
               this.sync_BlockPos = null;
               this.read_aa_DataUtil = null;
               this.init_ce_DataUtil = null;
               this.init_Vec3 = null;
               this.flush_BlockPos = null;
               this.write_aa_DataUtil = null;
               this.write_F = 3.4028235E38F;
               this.pool = 0;
               this.load_Long2IntOpenHashMa.clear();
               this.load_ce_DataUtil = null;
               this.count = -2147483648;
               bA.init_V();
               this.write_V_8();
            }

            this.load_V_12();
            this.sync_V_3();
            if (!this.read_Z_8()) {
               this.read_V_8();
               this.open_V_2();
            } else {
               AutoFarmModule.Inner_BufferHelper target = this.init_RingBuffer_BufferH();
               if (target == null) {
                  this.load_Str = this.init_Str_4();
                  this.update_V_7();
                  this.update_BlockPos = null;
                  this.entry = 0;
                  ce.Inner_DataUtil graceGoal = this.bool("movement") ? null : this.init_ce_DataUtil_2();
                  if (this.bool("movement")) {
                     this.poll_V_2();
                  } else {
                     bA.load();
                     this.init_BlockPos = null;
                     this.init_V_ce_DataUtil_2(graceGoal);
                  }

                  if (graceGoal == null) {
                     this.close_V_3();
                     this.open_V_2();
                  }
               } else {
                  this.size = 0;
                  boolean walkingThrough = this.bool("movement") && !this.load_Z && this.init_Z_RingBuffer_BufferH(target);
                  if (!walkingThrough) {
                     bA.load();
                  }

                  this.init_BlockPos = null;
                  ce.Inner_DataUtil acted = this.init_ce_DataUtil_RingBuffer_BufferH(target);
                  if (acted != null) {
                     this.flags = ck.init_ck().state_I();
                     this.update_BlockPos = null;
                     this.entry = 0;
                     this.flush_BlockPos = null;
                     this.write_aa_DataUtil = null;
                     this.write_F = 3.4028235E38F;
                     this.pool = 0;
                     String var10001;
                     switch (target.init_aa_DataUtil()) {
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

                     this.load_Str = var10001;
                     this.load_ce_DataUtil = acted;
                     this.count = ck.init_ck().state_I();
                     this.init_V_ce_DataUtil_Z_2(acted, true);
                  } else {
                     this.load_Str = "";
                     this.update_V_7();
                     int tickNow = ck.init_ck().state_I();
                     int settleAge = tickNow - this.data;
                     boolean deliberatePause = tickNow == this.start || settleAge >= 0 && settleAge <= this.value + 1 || tickNow < this.next;
                     if (!deliberatePause) {
                        this.init_V_RingBuffer_BufferH_I(target, tickNow);
                        this.load(target, tickNow);
                     }

                     this.load_ce_DataUtil = target.init_ce_DataUtil();
                     this.count = tickNow;
                     this.init_V_ce_DataUtil_2(target.init_ce_DataUtil());
                  }
               }
            }
         } else {
            this.init_V_15();
            bA.init_V();
         }
      }
   }

   private void load_V_12() {
      String settings = this.bool("movement")
         + ":"
         + this.init_I_9()
         + ":"
         + this.init_Set_5()
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
      if (!settings.equals(this.read_Str)) {
         this.read_Str = settings;
         this.write_V_8();
         bA.init_V();
         this.init_BlockPos = null;
         this.read_BlockPos = null;
         this.load_BlockPos = null;
         this.state = -2147483648;
         this.cache = -2147483648;
         this.queue = -2147483648;
         this.write_Z = false;
         this.write_BlockPos = null;
         this.sync_BlockPos = null;
         this.load_ce_DataUtil = null;
         this.init_Long2IntOpenHashMa.clear();
         this.init_Long2ObjectOpenHas.clear();
         this.load_Long2IntOpenHashMa.clear();
         this.fill = -2147483648;
         this.load_Map.clear();
      }
   }

   private ce.Inner_DataUtil init_ce_DataUtil_2() {
      if (this.load_ce_DataUtil == null) {
         return null;
      }

      int age = ck.init_ck().state_I() - this.count;
      return age >= 0 && age <= 3 ? this.load_ce_DataUtil : null;
   }

   private String init_Str_4() {
      if (this.bool("till") && !this.pop()) {
         return "No hoe";
      }

      if (this.bool("replant") || this.bool("plant")) {
         for (Block crop : this.init_Set_5()) {
            if (!autismclient.util.Y.read(crop) && autismclient.util.Y.init_Item_Block(crop) == null) {
               return "No seed";
            }
         }
      }

      return this.bool("bonemeal") && !this.tick_Z_3() && this.init_Z_16() ? "No bonemeal" : "";
   }

   private boolean init_Z_16() {
      Set<Block> crops = this.init_Set_5();
      if (crops.isEmpty()) {
         return false;
      }

      Vec3 eye = MC.player.getEyePosition();
      double cullSqr = this.init_D_4() * this.init_D_4();
      int[] offsets = init_IArr_I_2(Mth.ceil(this.init_D_4()) + 3);
      BlockPos origin = MC.player.blockPosition();
      int ox = origin.getX();
      int oy = origin.getY();
      int oz = origin.getZ();
      MutableBlockPos cursor = new MutableBlockPos();

      for (int i = 0; i < offsets.length; i += 3) {
         int cx = ox + offsets[i];
         int cy = oy + offsets[i + 1];
         int cz = oz + offsets[i + 2];
         if (!(init_D_Vec3(eye, cx, cy, cz) > cullSqr)) {
            BlockState state = MC.level.getBlockState(cursor.set(cx, cy, cz));
            if (crops.contains(state.getBlock()) && this.init_Z_BlockPos_BlockState_2(cursor, state)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean read_Z_8() {
      if (MC == null || MC.player == null || MC.level == null || MC.gameMode == null) {
         return false;
      } else if (aD.init_Z() || FreecamController.init_Z()) {
         return false;
      } else if (!MC.player.isAlive() || MC.player.isSpectator()) {
         return false;
      } else if (cn.init_Z() || ScaffoldModule.update_Z_5()) {
         return false;
      } else if (MC.player.isUsingItem() || MC.player.isHandsBusy()) {
         return false;
      } else if (this.flush_Z_3()) {
         return false;
      } else {
         return MC.gui != null && MC.gui.screen() == null && MC.gui.overlay() == null ? !AutoTotemModule.init_Z_6() && !AutoArmorModule.init_Z_20() : false;
      }
   }

   private void read_V_8() {
      bA.load();
      this.write_V_8();
      this.init_BlockPos = null;
      this.load_Str = "";
      this.close_V_3();
      if (this.sync_Z_3()) {
         autismclient.util.aD.init(this.id());
         if (MC != null && MC.player != null) {
            autismclient.util.aD.init_V_Str_LocalPlayer(this.id(), MC.player);
         }
      }
   }

   private boolean sync_Z_3() {
      return this.id().equals(autismclient.util.aD.init_Str());
   }

   private boolean flush_Z_3() {
      String owner = autismclient.util.aD.init_Str();
      return owner != null && !this.id().equals(owner);
   }

   private void init_V_ce_DataUtil_2(ce.Inner_DataUtil goal) {
      this.init_V_ce_DataUtil_Z_2(goal, false);
   }

   private void init_V_ce_DataUtil_Z_2(ce.Inner_DataUtil goal, boolean pinQuiet) {
      if (goal != null) {
         if (!this.flush_Z_3()) {
            autismclient.util.aD.init_V_Str(this.id(), 5, goal);
         }
      } else if (this.sync_Z_3()) {
         autismclient.util.aD.init(this.id());
      }

      if (this.sync_Z_3()) {
         autismclient.util.aD.init_V_Str_5(this.id(), MC.player, pinQuiet);
      }
   }

   private double init_D_4() {
      return MC.player.blockInteractionRange();
   }

   private int init_I_9() {
      int tick = ck.init_ck().state_I();
      if (tick != this.chunk) {
         this.chunk = tick;
         this.table = this.integer("search-range");
      }

      return this.table;
   }

   private AutoFarmModule.Inner_BufferHelper init_RingBuffer_BufferH() {
      Set<Block> crops = this.init_Set_5();
      if (crops.isEmpty()) {
         return null;
      }

      double reach = this.init_D_4();
      Vec3 eye = MC.player.getEyePosition();
      int[] offsets = init_IArr_I_2(Mth.ceil(reach) + 3);
      double cullSqr = reach * reach;
      int tick = ck.init_ck().state_I();
      boolean wantReplant = this.bool("replant");
      boolean wantPlant = this.bool("plant");
      int hoeSlot = this.bool("till") ? this.load_I_6() : -1;
      boolean bonemealOffer = this.bool("bonemeal") && this.tick_Z_3();
      BlockPos origin = MC.player.blockPosition();
      int ox = origin.getX();
      int oy = origin.getY();
      int oz = origin.getZ();
      MutableBlockPos cursor = new MutableBlockPos();
      ce.Inner_DataUtil sweep = this.load_ce_DataUtil_2();
      BlockPos[][] candidateCells = new BlockPos[autismclient.util.aa.Inner_DataUtil.values().length][3];
      double[][] candidateKeys = new double[autismclient.util.aa.Inner_DataUtil.values().length][3];
      double[][] candidateDistances = new double[autismclient.util.aa.Inner_DataUtil.values().length][3];

      for (double[] keys : candidateKeys) {
         Arrays.fill(keys, 1.0 / 0.0);
      }

      this.load_Z = false;

      for (int i = 0; i < offsets.length; i += 3) {
         int cx = ox + offsets[i];
         int cy = oy + offsets[i + 1];
         int cz = oz + offsets[i + 2];
         double distanceSqr = init_D_Vec3(eye, cx, cy, cz);
         if (!(distanceSqr > cullSqr)) {
            BlockState state = MC.level.getBlockState(cursor.set(cx, cy, cz));
            Block block = state.getBlock();
            if (crops.contains(block)) {
               if (this.init_Z_BlockState_3(state)
                  && !this.init_Z_I(cx, cy, cz, tick)
                  && !this.load(cx, cy, cz, tick)
                  && this.init_Z_BlockPos_MutableBl_2(cursor, cx, cy, cz, block)) {
                  this.init(autismclient.util.aa.Inner_DataUtil.HARVEST, cx, cy, cz, distanceSqr, sweep, eye, candidateCells, candidateKeys, candidateDistances);
               } else if (bonemealOffer
                  && !this.init_Z_I(cx, cy, cz, tick)
                  && !this.load(cx, cy, cz, tick)
                  && this.init_Z_BlockPos_BlockState_2(cursor.set(cx, cy, cz), state)) {
                  this.init(autismclient.util.aa.Inner_DataUtil.BONEMEAL, cx, cy, cz, distanceSqr, sweep, eye, candidateCells, candidateKeys, candidateDistances);
               }
            } else if ((wantReplant || wantPlant) && state.canBeReplaced()) {
               if (!this.init_Z_I(cx, cy, cz, tick) && !this.load(cx, cy, cz, tick)) {
                  BlockState below = MC.level.getBlockState(cursor.set(cx, cy - 1, cz));
                  if (wantReplant && this.init(cx, cy, cz, below, crops, tick) != null) {
                     this.init(autismclient.util.aa.Inner_DataUtil.REPLANT_RECENT, cx, cy, cz, distanceSqr, sweep, eye, candidateCells, candidateKeys, candidateDistances);
                  }

                  if (wantPlant && this.init_RingBuffer_CachePo_I(cx, cy, cz, below, crops) != null) {
                     this.init(
                        autismclient.util.aa.Inner_DataUtil.REPLANT, cx, cy, cz, distanceSqr, sweep, eye, candidateCells, candidateKeys, candidateDistances
                     );
                  }
               }
            } else if (this.bool("till")
               && this.pop()
               && init_Set.contains(block)
               && (block == Blocks.ROOTED_DIRT || MC.level.getBlockState(cursor.set(cx, cy + 1, cz)).isAir())
               && !this.init_Z_I(cx, cy, cz, tick)
               && !this.load(cx, cy, cz, tick)) {
               this.init(autismclient.util.aa.Inner_DataUtil.TILL, cx, cy, cz, distanceSqr, sweep, eye, candidateCells, candidateKeys, candidateDistances);
            }
         }
      }

      List<autismclient.util.aa.Inner_StreamUtil<AutoFarmModule.Inner_BufferHelper>> options = new ArrayList<>();

      for (autismclient.util.aa.Inner_DataUtil kind : autismclient.util.aa.Inner_DataUtil.values()) {
         for (BlockPos cell : candidateCells[kind.ordinal()]) {
            if (cell == null) {
               break;
            }

            AutoFarmModule.Inner_BufferHelper candidate = this.init_RingBuffer_BufferH_BlockPos(cell, kind, crops, hoeSlot, tick);
            if (candidate != null) {
               options.add(this.init_aa_StreamUtil_RingBuffer_BufferH_Vec3(candidate, eye));
               break;
            }

            this.read(cell, tick);
         }
      }

      AutoFarmModule.Inner_BufferHelper previous = null;
      int stickyAge = tick - this.buffer;
      if (this.write_BlockPos != null
         && stickyAge >= 0
         && stickyAge <= 8
         && init_D_Vec3(eye, this.write_BlockPos.getX(), this.write_BlockPos.getY(), this.write_BlockPos.getZ()) <= cullSqr
         && !this.init_Z_I(this.write_BlockPos.getX(), this.write_BlockPos.getY(), this.write_BlockPos.getZ(), tick)
         && !this.load(this.write_BlockPos.getX(), this.write_BlockPos.getY(), this.write_BlockPos.getZ(), tick)) {
         previous = this.init_RingBuffer_BufferH_BlockPos(this.write_BlockPos, this.init_aa_DataUtil, crops, hoeSlot, tick);
         if (previous != null) {
            AutoFarmModule.Inner_BufferHelper rebuilt = previous;
            AutoFarmModule.Inner_BufferHelper existing = options.stream()
               .map(autismclient.util.aa.Inner_StreamUtil::init_Obj)
               .filter(t -> t.init_aa_DataUtil() == rebuilt.init_aa_DataUtil() && t.init_BlockPos().equals(rebuilt.init_BlockPos()))
               .findFirst()
               .orElse(null);
            if (existing != null) {
               previous = existing;
            } else {
               options.add(this.init_aa_StreamUtil_RingBuffer_BufferH_Vec3(previous, eye));
            }
         }
      }

      AutoFarmModule.Inner_BufferHelper chosen = autismclient.util.aa.init(
         options, sweep.init(), sweep.load(), MC.player.getInventory().getSelectedSlot(), previous
      );
      if (chosen == null) {
         this.write_BlockPos = null;
         return null;
      }

      if (!chosen.init_BlockPos().equals(this.write_BlockPos) || chosen.init_aa_DataUtil() != this.init_aa_DataUtil) {
         this.buffer = tick;
      }

      this.write_BlockPos = chosen.init_BlockPos();
      this.init_aa_DataUtil = chosen.init_aa_DataUtil();
      this.state = -2147483648;
      if (chosen.init_aa_DataUtil() == autismclient.util.aa.Inner_DataUtil.HARVEST) {
         AutoFarmModule.Inner_BufferHelper solved = this.init_RingBuffer_BufferH_BlockPos(chosen.init_BlockPos());
         if (solved != null) {
            chosen = solved;
         }
      }

      return chosen;
   }

   private autismclient.util.aa.Inner_StreamUtil<AutoFarmModule.Inner_BufferHelper> init_aa_StreamUtil_RingBuffer_BufferH_Vec3(
      AutoFarmModule.Inner_BufferHelper target, Vec3 eye
   ) {
      int slot = target.init_I();
      if (target.init_Item() != null) {
         BlockHitResult hit = ScaffoldModule.init_BlockHitResult_Vec3(eye, target.init_ce_DataUtil(), this.init_D_4(), MC.level, MC.player);
         if (hit != null && this.init_Z_Item_BlockHitResult(target.init_Item(), hit)) {
            slot = -1;
         }
      }

      boolean urgent = target.init_aa_DataUtil() == autismclient.util.aa.Inner_DataUtil.REPLANT_RECENT
         && this.bool("movement")
         && this.init_Z_BlockPos_Vec3(target.init_BlockPos(), eye);
      this.load_Z |= urgent;
      return new autismclient.util.aa.Inner_StreamUtil<>(
         target, target.init_aa_DataUtil(), target.init_ce_DataUtil().init(), target.init_ce_DataUtil().load(), slot, urgent
      );
   }

   private boolean init_Z_BlockPos_Vec3(BlockPos cell, Vec3 eye) {
      Vec3 motion = MC.player.getDeltaMovement();
      double dx = motion.x * 8.0;
      double dz = motion.z * 8.0;
      if (dx * dx + dz * dz < 0.04) {
         double yaw = Math.toRadians(this.load_ce_DataUtil_2().init());
         dx = -Math.sin(yaw) * 1.6;
         dz = Math.cos(yaw) * 1.6;
      }

      return init_D_Vec3(eye.add(dx, 0.0, dz), cell.getX(), cell.getY(), cell.getZ()) > this.init_D_4() * this.init_D_4() - 0.5;
   }

   private AutoFarmModule.Inner_BufferHelper init_RingBuffer_BufferH_BlockPos(
      BlockPos pos, autismclient.util.aa.Inner_DataUtil kind, Set<Block> crops, int hoeSlot, int tick
   ) {
      switch (kind) {
         case HARVEST:
            BlockState state = MC.level.getBlockState(pos);
            if (crops.contains(state.getBlock()) && this.init_Z_BlockState_3(state)) {
               if (autismclient.util.Y.load(state.getBlock()) && !MC.level.getBlockState(pos.below()).is(state.getBlock())) {
                  return null;
               }

               return this.init_RingBuffer_BufferH_BlockPos(pos);
            }

            return null;
         case BONEMEAL:
            if (!this.bool("bonemeal")) {
               return null;
            }

            return this.init_Z_BlockPos_3(pos) ? this.load_RingBuffer_BufferH(pos) : null;
         case TILL:
            return this.load_Z(pos) ? this.init_RingBuffer_BufferH_BlockPos_I(pos, hoeSlot) : null;
         case REPLANT_RECENT:
         case REPLANT:
            if (kind == autismclient.util.aa.Inner_DataUtil.REPLANT_RECENT ? this.bool("replant") : this.bool("plant")) {
               return !MC.level.getBlockState(pos).canBeReplaced()
                  ? null
                  : this.init_RingBuffer_BufferH_BlockPos(pos, kind == autismclient.util.aa.Inner_DataUtil.REPLANT_RECENT, tick);
            } else {
               return null;
            }
         default:
            return null;
      }
   }

   private boolean init_Z_I(int x, int y, int z, int tick) {
      long key = BlockPos.asLong(x, y, z);
      if (!this.init_Long2IntOpenHashMa.containsKey(key)) {
         return false;
      }

      int stamp = this.init_Long2IntOpenHashMa.get(key);
      int age = tick - stamp;
      if (age >= 0 && age <= 80) {
         Block base = this.init_Long2ObjectOpenHas.get(key);
         if (base != null && MC.level.getBlockState(new BlockPos(x, y - 1, z)).getBlock() != base) {
            this.init_Long2IntOpenHashMa.remove(key);
            this.init_Long2ObjectOpenHas.remove(key);
            return false;
         } else {
            return true;
         }
      } else {
         this.init_Long2IntOpenHashMa.remove(key);
         this.init_Long2ObjectOpenHas.remove(key);
         return false;
      }
   }

   private void init_V_BlockPos_I(BlockPos pos, int tick) {
      if (pos.equals(this.close_BlockPos)) {
         this.drain++;
      } else {
         this.close_BlockPos = pos;
         this.drain = 1;
      }

      if (this.drain >= 3) {
         this.load(pos, tick);
         this.close_BlockPos = null;
         this.drain = 0;
      }
   }

   private void load(BlockPos pos, int tick) {
      if (this.init_Long2IntOpenHashMa.size() >= 64) {
         this.init_Long2IntOpenHashMa.entrySet().removeIf(entry -> {
            int age = tick - entry.getValue();
            if (age >= 0 && age <= 80) {
               return false;
            }

            this.init_Long2ObjectOpenHas.remove(entry.getKey());
            return true;
         });
      }

      this.init_Long2IntOpenHashMa.put(pos.asLong(), tick);
      this.init_Long2ObjectOpenHas.put(pos.asLong(), MC.level.getBlockState(pos.below()).getBlock());
   }

   private void init_V_RingBuffer_BufferH_I(AutoFarmModule.Inner_BufferHelper target, int tick) {
      if (!this.flush_Z_3()
         && !BedDefenderModule.init_Z_10()
         && !SurroundModule.init_Z_27()
         && !CrystalAuraModule.init_Z_51()
         && !AnchorAuraModule.init_Z_2()
         && !BlinkPacketManager.load_Z()
         && ch.init_ch_CachePool().init_Z()) {
         ce.Inner_DataUtil goal = target.init_ce_DataUtil();
         ce.Inner_DataUtil wire = this.load_ce_DataUtil_2();
         boolean arrived = Math.abs(ce.init(wire.init(), goal.init())) <= 3.0F && Math.abs(wire.load() - goal.load()) <= 3.0F;
         if (!arrived) {
            this.update_BlockPos = null;
            this.entry = 0;
         } else {
            if (target.init_BlockPos().equals(this.update_BlockPos) && target.init_aa_DataUtil() == this.load_aa_DataUtil) {
               this.entry++;
            } else {
               this.update_BlockPos = target.init_BlockPos();
               this.load_aa_DataUtil = target.init_aa_DataUtil();
               this.entry = 1;
            }

            if (this.entry >= this.init_I_I_2(tick)) {
               this.load(target.init_BlockPos(), tick);
               this.update_BlockPos = null;
               this.entry = 0;
            }
         }
      }
   }

   private int init_I_I_2(int tick) {
      int actionAge = tick - this.flags;
      return actionAge >= 0 && actionAge <= 2 ? 2 : 6;
   }

   private void load(AutoFarmModule.Inner_BufferHelper target, int tick) {
      if (!this.flush_Z_3()
         && !BedDefenderModule.init_Z_10()
         && !SurroundModule.init_Z_27()
         && !CrystalAuraModule.init_Z_51()
         && !AnchorAuraModule.init_Z_2()
         && !BlinkPacketManager.load_Z()
         && ch.init_ch_CachePool().init_Z()) {
         ce.Inner_DataUtil goal = target.init_ce_DataUtil();
         ce.Inner_DataUtil wire = this.load_ce_DataUtil_2();
         float distance = Math.abs(ce.init(wire.init(), goal.init())) + Math.abs(wire.load() - goal.load());
         if (!target.init_BlockPos().equals(this.flush_BlockPos) || target.init_aa_DataUtil() != this.write_aa_DataUtil) {
            this.flush_BlockPos = target.init_BlockPos();
            this.write_aa_DataUtil = target.init_aa_DataUtil();
            this.write_F = distance;
            this.pool = 0;
         } else if (distance < this.write_F - 0.5F) {
            this.write_F = distance;
            this.pool = 0;
         } else {
            this.pool++;
         }

         if (this.pool >= 20) {
            this.read(target.init_BlockPos(), tick);
            this.flush_BlockPos = null;
            this.write_aa_DataUtil = null;
            this.write_F = 3.4028235E38F;
            this.pool = 0;
         }
      }
   }

   private void read(BlockPos pos, int tick) {
      this.load_Long2IntOpenHashMa.put(pos.asLong(), tick);
   }

   private boolean load(int x, int y, int z, int tick) {
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
      return !autismclient.util.Y.load(block) || MC.level.getBlockState(cursor.set(x, y - 1, z)).is(block);
   }

   private void init(
      autismclient.util.aa.Inner_DataUtil kind,
      int x,
      int y,
      int z,
      double distanceSqr,
      ce.Inner_DataUtil sweep,
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
      double key = Math.hypot(ce.init(sweep.init(), yaw), pitch - sweep.load());
      if (kind == autismclient.util.aa.Inner_DataUtil.REPLANT_RECENT && this.bool("movement") && this.init_Z_BlockPos_Vec3(new BlockPos(x, y, z), eye)) {
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

   private ce.Inner_DataUtil load_ce_DataUtil_2() {
      int tick = ck.init_ck().state_I();
      if (tick == this.scan && this.read_ce_DataUtil != null) {
         return this.read_ce_DataUtil;
      }

      this.scan = tick;
      ch.Inner_CachePool wire = ch.init_ch_CachePool();
      this.read_ce_DataUtil = wire.init_Z() ? new ce.Inner_DataUtil(wire.read(), wire.write()) : ce.init(MC.player);
      return this.read_ce_DataUtil;
   }

   private boolean init_Z_BlockState_3(BlockState state) {
      return !this.close_Z_3() ? true : this.load(state);
   }

   private boolean load(BlockState state) {
      Boolean cached = this.load_Map.get(state);
      if (cached != null) {
         return cached;
      }

      boolean verdict = true;

      for (Property<?> property : state.getProperties()) {
         if (property instanceof IntegerProperty age && "age".equals(age.getName())) {
            verdict = state.getValue(age) >= this.init_I_IntegerProperty(age);
            break;
         }
      }

      this.load_Map.put(state, verdict);
      return verdict;
   }

   private boolean close_Z_3() {
      int tick = ck.init_ck().state_I();
      if (tick != this.fill) {
         this.fill = tick;
         this.read_Z = this.bool("grown-only");
      }

      return this.read_Z;
   }

   private int init_I_IntegerProperty(IntegerProperty age) {
      return this.init_Map.computeIfAbsent(age, key -> Collections.max(key.getPossibleValues()));
   }

   private Set<Block> init_Set_5() {
      String raw = this.value("crops");
      if (raw.equals(this.init_Str)) {
         return this.write_Set;
      }

      Set<Block> blocks = new LinkedHashSet<>();

      for (String entry : this.list("crops")) {
         Identifier identifier = Identifier.tryParse(di.init(entry));
         if (identifier != null) {
            BuiltInRegistries.BLOCK.getOptional(identifier).filter(autismclient.util.Y::init_Z_Block).ifPresent(blocks::add);
         }
      }

      this.init_Str = raw;
      this.write_Set = Collections.unmodifiableSet(blocks);
      return this.write_Set;
   }

   private AutoFarmModule.Inner_CachePool init_RingBuffer_CachePo_I(int x, int y, int z, BlockState below, Set<Block> crops) {
      AutoFarmModule.Inner_StreamUtil ownCell = this.init_ac.init_Obj(BlockPos.asLong(x, y, z));
      if (ownCell != null) {
         return this.init_Z_RingBuffer_StreamU(ownCell) ? null : this.init(ownCell.init_Block(), below, crops, x, y, z);
      }

      Block preferred = null;
      int bestDistance = 2147483647;

      for (int distance = 1; distance <= 4 && preferred == null; distance++) {
         for (Direction direction : Plane.HORIZONTAL) {
            int nx = x + direction.getStepX() * distance;
            int nz = z + direction.getStepZ() * distance;
            AutoFarmModule.Inner_StreamUtil entry = this.init_ac.init_Obj(BlockPos.asLong(nx, y, nz));
            Block neighbor = entry != null ? entry.init_Block() : MC.level.getBlockState(new BlockPos(nx, y, nz)).getBlock();
            if (crops.contains(neighbor) && this.init_Z_Block(neighbor, below, x, y, z)) {
               bestDistance = distance;
               preferred = neighbor;
               break;
            }
         }
      }

      if (preferred != null && bestDistance <= 4) {
         return this.init(preferred, below, crops, x, y, z);
      }

      for (Block crop : crops) {
         AutoFarmModule.Inner_CachePool choice = this.init(crop, below, crops, x, y, z);
         if (choice != null) {
            return choice;
         }
      }

      return null;
   }

   private AutoFarmModule.Inner_CachePool init(Block crop, BlockState below, Set<Block> crops, int x, int y, int z) {
      if (crops.contains(crop) && this.init_Z_Block(crop, below, x, y, z)) {
         if (crop instanceof CropBlock && MC.level.getRawBrightness(new BlockPos(x, y, z), 0) < 8) {
            return null;
         }

         Item seed = autismclient.util.Y.init_Item_Block(crop);
         return seed == null ? null : this.init_RingBuffer_CachePo_Block_Item(crop, seed);
      } else {
         return null;
      }
   }

   private boolean init_Z_Block(Block crop, BlockState below, int x, int y, int z) {
      if (crop != Blocks.COCOA) {
         return autismclient.util.Y.init_Z_Block_BlockState(crop, below);
      }

      BlockPos cell = new BlockPos(x, y, z);

      for (Direction direction : Plane.HORIZONTAL) {
         if (autismclient.util.Y.init_Z_Block_BlockState(crop, MC.level.getBlockState(cell.relative(direction)))) {
            return true;
         }
      }

      return false;
   }

   private AutoFarmModule.Inner_CachePool init_RingBuffer_CachePo_Block_Item(Block crop, Item seed) {
      int slot = this.init_I_Item(seed);
      if (slot >= 0) {
         return new AutoFarmModule.Inner_CachePool(crop, seed, slot);
      } else {
         return this.init_Z_Item(seed) ? new AutoFarmModule.Inner_CachePool(crop, seed, -1) : null;
      }
   }

   private AutoFarmModule.Inner_CachePool init(int x, int y, int z, BlockState below, Set<Block> crops, int tick) {
      AutoFarmModule.Inner_StreamUtil entry = this.init_ac.init_Obj(BlockPos.asLong(x, y, z));
      return entry != null && !this.init_Z_RingBuffer_StreamU(entry) ? this.init(entry.init_Block(), below, crops, x, y, z) : null;
   }

   private AutoFarmModule.Inner_BufferHelper init_RingBuffer_BufferH_BlockPos(BlockPos cell) {
      int slot = this.bool("fortune") ? this.write_I_4() : -1;
      ce.Inner_DataUtil rotation = this.load_ce_DataUtil(cell);
      return rotation == null
         ? null
         : new AutoFarmModule.Inner_BufferHelper(autismclient.util.aa.Inner_DataUtil.HARVEST, cell, MC.level.getBlockState(cell).getBlock(), null, slot, rotation);
   }

   private boolean init_Z_ce_DataUtil_Vec3(ce.Inner_DataUtil goal, Vec3 eye) {
      BlockHitResult ray = ScaffoldModule.init_BlockHitResult_Vec3(eye, goal, this.init_D_4(), MC.level, MC.player);
      if (ray == null) {
         return false;
      }

      BlockPos cell = ray.getBlockPos();
      BlockState state = MC.level.getBlockState(cell);
      return this.init_Set_5().contains(state.getBlock()) && this.init_Z_BlockState_3(state)
         ? !autismclient.util.Y.load(state.getBlock()) || MC.level.getBlockState(cell.below()).is(state.getBlock())
         : false;
   }

   private AutoFarmModule.Inner_BufferHelper init_RingBuffer_BufferH_BlockPos(BlockPos cell, boolean recent, int tick) {
      Set<Block> crops = this.init_Set_5();
      BlockState below = MC.level.getBlockState(cell.below());
      AutoFarmModule.Inner_CachePool choice = recent
         ? this.init(cell.getX(), cell.getY(), cell.getZ(), below, crops, tick)
         : this.init_RingBuffer_CachePo_I(cell.getX(), cell.getY(), cell.getZ(), below, crops);
      if (choice == null) {
         return null;
      }

      ItemStack stack = choice.init_I() >= 0 ? MC.player.getInventory().getItem(choice.init_I()) : MC.player.getOffhandItem();
      autismclient.util.aa.Inner_DataUtil kind = recent ? autismclient.util.aa.Inner_DataUtil.REPLANT_RECENT : autismclient.util.aa.Inner_DataUtil.REPLANT;
      Vec3 eye = MC.player.getEyePosition();
      ce.Inner_DataUtil rotation;
      if (this.sync_BlockPos != null && this.sync_BlockPos.equals(cell) && this.read_aa_DataUtil == kind && this.init_Vec3.distanceTo(eye) <= 1.0) {
         rotation = this.init_ce_DataUtil;
      } else {
         rotation = this.init_ce_DataUtil_BlockPos_ItemStack(cell, stack);
         if (rotation != null) {
            this.sync_BlockPos = cell.immutable();
            this.read_aa_DataUtil = kind;
            this.init_ce_DataUtil = rotation;
            this.init_Vec3 = eye;
         }
      }

      return rotation == null ? null : new AutoFarmModule.Inner_BufferHelper(kind, cell, choice.init_Block(), choice.init_Item(), choice.init_I(), rotation);
   }

   private AutoFarmModule.Inner_BufferHelper init_RingBuffer_BufferH_BlockPos_I(BlockPos cell, int hoeSlot) {
      Item hoe = hoeSlot >= 0 ? MC.player.getInventory().getItem(hoeSlot).getItem() : (this.push() ? MC.player.getOffhandItem().getItem() : null);
      if (hoe == null) {
         return null;
      }

      ce.Inner_DataUtil rotation = this.read_ce_DataUtil(cell);
      return rotation == null ? null : new AutoFarmModule.Inner_BufferHelper(autismclient.util.aa.Inner_DataUtil.TILL, cell, null, hoe, hoeSlot, rotation);
   }

   private AutoFarmModule.Inner_BufferHelper load_RingBuffer_BufferH(BlockPos cell) {
      int slot = this.read_I_4();
      if (slot < 0 && !this.init_Z_Item(Items.BONE_MEAL)) {
         return null;
      }

      ce.Inner_DataUtil rotation = this.init_ce_DataUtil_BlockPos(cell);
      return rotation == null
         ? null
         : new AutoFarmModule.Inner_BufferHelper(
            autismclient.util.aa.Inner_DataUtil.BONEMEAL, cell, MC.level.getBlockState(cell).getBlock(), Items.BONE_MEAL, slot, rotation
         );
   }

   private ce.Inner_DataUtil init_ce_DataUtil_BlockPos(BlockPos cell) {
      Vec3 eye = MC.player.getEyePosition();
      VoxelShape shape = MC.level.getBlockState(cell).getShape(MC.level, cell);
      AABB box = shape.isEmpty() ? new AABB(cell) : shape.bounds().move(cell);
      ce.Inner_DataUtil goal = ce.init(box.getCenter(), eye);
      if (!init_Z_F_2(goal.load())) {
         return null;
      } else {
         return this.load(goal, eye) ? goal : null;
      }
   }

   private boolean load(ce.Inner_DataUtil goal, Vec3 eye) {
      BlockHitResult ray = ScaffoldModule.init_BlockHitResult_Vec3(eye, goal, this.init_D_4(), MC.level, MC.player);
      return ray != null && this.init_Z_BlockPos_3(ray.getBlockPos());
   }

   private boolean init_Z_BlockPos_3(BlockPos pos) {
      BlockState state = MC.level.getBlockState(pos);
      return this.init_Set_5().contains(state.getBlock()) && this.init_Z_BlockPos_BlockState_2(pos, state);
   }

   private boolean init_Z_BlockPos_BlockState_2(BlockPos pos, BlockState state) {
      Block block = state.getBlock();
      return !autismclient.util.Y.load(block) && !this.load(state)
         ? block instanceof BonemealableBlock b && Compat263.isValidBonemealTarget(b, MC.level, pos, state)
         : false;
   }

   private ce.Inner_DataUtil load_ce_DataUtil(BlockPos cell) {
      Vec3 eye = MC.player.getEyePosition();
      ce.Inner_DataUtil row = this.init_ce_DataUtil_BlockPos_Vec3(cell, eye);
      if (row != null) {
         return row;
      }

      Set<Block> crops = this.init_Set_5();
      double cullSqr = this.init_D_4() * this.init_D_4();
      List<BlockPos> members = new ArrayList<>();
      MutableBlockPos cursor = new MutableBlockPos();

      for (int dx = -2; dx <= 2; dx++) {
         for (int dy = -2; dy <= 2; dy++) {
            for (int dz = -2; dz <= 2; dz++) {
               int x = cell.getX() + dx;
               int y = cell.getY() + dy;
               int z = cell.getZ() + dz;
               if (!(init_D_Vec3(eye, x, y, z) > cullSqr)) {
                  BlockState state = MC.level.getBlockState(cursor.set(x, y, z));
                  if (crops.contains(state.getBlock())
                     && this.init_Z_BlockState_3(state)
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
         ce.Inner_DataUtil goal = ce.init(centroid, eye);
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
            ce.Inner_DataUtil goal = ce.init(centroid, eye);
            if (this.init_Z_ce_DataUtil_Vec3(goal, eye)) {
               return goal;
            }
         }
      }

      VoxelShape shape = MC.level.getBlockState(cell).getShape(MC.level, cell);
      AABB box = shape.isEmpty() ? new AABB(cell) : shape.bounds().move(cell);
      ce.Inner_DataUtil boxGoal = ce.init(box.getCenter(), eye);
      return !this.init_Z_ce_DataUtil_Vec3(boxGoal, eye) ? null : boxGoal;
   }

   private void init_V_BlockPos(BlockPos cell, Vec3 eye, Set<Block> crops, double cullSqr, List<BlockPos> members) {
      ce.Inner_DataUtil sweep = this.load_ce_DataUtil_2();
      int[] offsets = init_IArr_I_2(Mth.ceil(this.init_D_4()) + 3);
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
         if ((Math.abs(x - cell.getX()) > 2 || Math.abs(y - cell.getY()) > 2 || Math.abs(z - cell.getZ()) > 2) && !(init_D_Vec3(eye, x, y, z) > cullSqr)) {
            BlockState state = MC.level.getBlockState(cursor.set(x, y, z));
            if (crops.contains(state.getBlock()) && this.init_Z_BlockState_3(state) && this.init_Z_BlockPos_MutableBl_2(cursor, x, y, z, state.getBlock())) {
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
         return Math.abs(ce.init(sweep.init(), toYaw)) + Math.abs(toPitch - sweep.load());
      }));

      for (int i = 0; i < Math.min(4, beyond.size()); i++) {
         members.add(beyond.get(i));
      }
   }

   private ce.Inner_DataUtil init_ce_DataUtil_BlockPos_Vec3(BlockPos pick, Vec3 eye) {
      Set<Block> crops = this.init_Set_5();
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
         axis = this.init_Direction_Axis == Axis.Z ? Axis.Z : Axis.X;
      } else {
         axis = countX > countZ ? Axis.X : Axis.Z;
      }

      int along = axis == Axis.X ? px : pz;
      int perp = axis == Axis.X ? pz : px;
      int sign;
      if (this.init_Direction_Axis == axis) {
         sign = this.level;
      } else {
         sign = this.init_I_Direction_Axis(axis);
         if (sign == 0) {
            sign = 1;
         }
      }

      int end = this.init(cursor, along, perp, py, axis, sign, crops, this.init_IArr);
      if (end == -2147483648 && this.init_Direction_Axis != axis) {
         sign = -sign;
         end = this.init(cursor, along, perp, py, axis, sign, crops, this.init_IArr);
      }

      if (end == -2147483648) {
         return this.init(cursor, along, perp, py, axis, sign, crops, eye);
      }

      if ((end - along) * sign <= 3) {
         ce.Inner_DataUtil pivot = this.init(cursor, along, perp, py, axis, sign, crops, eye);
         if (pivot != null) {
            return pivot;
         }

         this.init(cursor, along, perp, py, axis, sign, crops, this.init_IArr);
      }

      this.init_Direction_Axis = axis;
      this.level = sign;
      ce.Inner_DataUtil goal = this.init(along, perp, this.init_IArr[0], axis, sign, end, eye);
      if (goal != null && !this.init_Z_ce_DataUtil_Vec3(goal, eye)) {
         goal = null;
      }

      if (goal != null) {
         this.init_V_I(end, perp, axis);
      }

      return goal;
   }

   private ce.Inner_DataUtil init(MutableBlockPos cursor, int along, int perp, int py, Axis axis, int sign, Set<Block> crops, Vec3 eye) {
      for (int offset = 1; offset <= 3; offset++) {
         for (int side = -1; side <= 1; side += 2) {
            int rowPerp = perp + side * offset;
            int anchor = -2147483648;
            int anchorDist = 2147483647;

            for (int da = -2; da <= 2; da++) {
               if (this.init(cursor, along + da, rowPerp, py, axis, crops) != -2147483648 && Math.abs(da) < anchorDist) {
                  anchorDist = Math.abs(da);
                  anchor = along + da;
               }
            }

            if (anchor != -2147483648) {
               int newSign = -sign;
               int end = this.init(cursor, anchor, rowPerp, py, axis, newSign, crops, this.init_IArr);
               if (end != -2147483648) {
                  ce.Inner_DataUtil goal = this.init(anchor, rowPerp, this.init_IArr[0], axis, newSign, end, eye);
                  if (goal != null && !this.init_Z_ce_DataUtil_Vec3(goal, eye)) {
                     goal = null;
                  }

                  if (goal == null) {
                     return null;
                  }

                  this.init_Direction_Axis = axis;
                  this.level = newSign;
                  this.init_V_I(end, rowPerp, axis);
                  return goal;
               }
            }
         }
      }

      return null;
   }

   private void init_V_I(int end, int perp, Axis axis) {
      this.load_BlockPos = axis == Axis.X ? new BlockPos(end, this.frame, perp) : new BlockPos(perp, this.frame, end);
      this.state = ck.init_ck().state_I();
   }

   private int init(MutableBlockPos cursor, int along, int perp, int py, Axis axis, int sign, Set<Block> crops, int[] nextY) {
      int end = -2147483648;
      int gap = 0;
      boolean gapAllWater = true;

      for (int step = 1; step <= 8; step++) {
         int a = along + sign * step;
         int cropY = this.init(cursor, a, perp, py, axis, crops);
         if (cropY != -2147483648) {
            if (end == -2147483648) {
               nextY[0] = cropY;
            }

            end = a;
            this.frame = cropY;
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

   private int init(MutableBlockPos cursor, int a, int perp, int py, Axis axis, Set<Block> crops) {
      for (int dy = -1; dy <= 1; dy++) {
         if (crops.contains(MC.level.getBlockState(init_BlockPos_MutableBl_BlockPos_MutableBl(cursor, a, perp, py + dy, axis)).getBlock())) {
            return py + dy;
         }
      }

      return -2147483648;
   }

   private boolean init_Z_BlockPos_MutableBl(MutableBlockPos cursor, int a, int perp, int py, Axis axis) {
      for (int dy = -1; dy <= 1; dy++) {
         Block block = MC.level.getBlockState(init_BlockPos_MutableBl_BlockPos_MutableBl(cursor, a, perp, py + dy, axis)).getBlock();
         if (block == Blocks.WATER || block == Blocks.LILY_PAD) {
            return true;
         }
      }

      return false;
   }

   private static MutableBlockPos init_BlockPos_MutableBl_BlockPos_MutableBl(MutableBlockPos cursor, int a, int perp, int y, Axis axis) {
      return axis == Axis.X ? cursor.set(a, y, perp) : cursor.set(perp, y, a);
   }

   private ce.Inner_DataUtil init(int along, int perp, int y, Axis axis, int sign, int end, Vec3 eye) {
      double goal = along + sign * 1.75;
      goal = sign > 0 ? Math.min(goal, end + 1.0) : Math.max(goal, end - 1.0);
      Vec3 point = axis == Axis.X ? new Vec3(goal + 0.5, y + 0.5, perp + 0.5) : new Vec3(perp + 0.5, y + 0.5, goal + 0.5);
      return eye.distanceToSqr(point) > this.init_D_4() * this.init_D_4() ? null : ce.init(point, eye);
   }

   private int init_I_Direction_Axis(Axis axis) {
      float yaw = this.load_ce_DataUtil_2().init();
      double component = axis == Axis.X ? -Math.sin(Math.toRadians(yaw)) : Math.cos(Math.toRadians(yaw));
      return component > 0.05 ? 1 : (component < -0.05 ? -1 : 0);
   }

   private ce.Inner_DataUtil init_ce_DataUtil_BlockPos_ItemStack(BlockPos cell, ItemStack seedStack) {
      autismclient.util.U.Inner_CachePool candidate = autismclient.util.U.init_U_CachePool(
         new autismclient.util.U.Inner_StateCache(
               cell, MC.player.getEyePosition(), this.init_D_4(), autismclient.util.U.init(seedStack, MC.player, InteractionHand.MAIN_HAND)
            )
            .init_U_StateCache_ce_DataUtil(this.load_ce_DataUtil_2())
            .init_U_StateCache_D(autismclient.util.U.init_D())
            .init_U_StateCache_Z(MC.player.isSecondaryUseActive())
            .load(false)
            .init_U_StateCache_U_StreamUtil(new autismclient.util.U.Inner_StreamUtil(48))
      );
      return candidate == null ? null : candidate.init_U_DataUtil().init_ce_DataUtil();
   }

   private ce.Inner_DataUtil read_ce_DataUtil(BlockPos dirt) {
      ce.Inner_DataUtil rotation = this.init_ce_DataUtil_BlockPos_BlockPos(dirt, dirt.above());
      if (rotation != null) {
         return rotation;
      }

      Direction[] sides = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
      this.init_V_DirectionArr_BlockPos(sides, dirt);

      for (Direction side : sides) {
         rotation = this.init_ce_DataUtil_BlockPos_BlockPos(dirt, dirt.relative(side));
         if (rotation != null) {
            return rotation;
         }
      }

      return null;
   }

   private ce.Inner_DataUtil init_ce_DataUtil_BlockPos_BlockPos(BlockPos dirt, BlockPos cell) {
      autismclient.util.U.Inner_CachePool candidate = autismclient.util.U.init_U_CachePool(
         new autismclient.util.U.Inner_StateCache(
               cell, MC.player.getEyePosition(), this.init_D_4(), (hit, target) -> hit.getBlockPos().equals(dirt) && hit.getDirection() != Direction.DOWN
            )
            .init_U_StateCache_ce_DataUtil(this.load_ce_DataUtil_2())
            .init_U_StateCache_D(autismclient.util.U.init_D())
            .init_U_StateCache_Z(MC.player.isSecondaryUseActive())
            .load(false)
            .init_U_StateCache_U_StreamUtil(new autismclient.util.U.Inner_StreamUtil(48))
      );
      return candidate == null ? null : candidate.init_U_DataUtil().init_ce_DataUtil();
   }

   private void init_V_DirectionArr_BlockPos(Direction[] sides, BlockPos dirt) {
      Vec3 eye = MC.player.getEyePosition();
      float wire = this.load_ce_DataUtil_2().init();
      float[] keys = new float[sides.length];

      for (int i = 0; i < sides.length; i++) {
         keys[i] = Math.abs(ce.init(wire, ce.init(Vec3.atCenterOf(dirt.relative(sides[i])), eye).init()));
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

   private boolean load_Z(BlockPos pos) {
      Block block = MC.level.getBlockState(pos).getBlock();
      return !init_Set.contains(block) ? false : block == Blocks.ROOTED_DIRT || MC.level.getBlockState(pos.above()).isAir();
   }

   private ce.Inner_DataUtil init_ce_DataUtil_RingBuffer_BufferH(AutoFarmModule.Inner_BufferHelper target) {
      int tick = ck.init_ck().state_I();
      if (tick == this.start) {
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

      if (this.flush_Z_3()) {
         return null;
      }

      int switchAge = tick - this.data;
      if (switchAge >= 0 && switchAge <= this.value) {
         return null;
      }

      if (tick < this.next) {
         return null;
      }

      if (BlinkPacketManager.load_Z()) {
         return null;
      }

      ch.Inner_CachePool wire = ch.init_ch_CachePool();
      if (!wire.init_Z()) {
         return null;
      }

      ce.Inner_DataUtil wireRotation = new ce.Inner_DataUtil(wire.read(), wire.write());
      if (target.init_aa_DataUtil() != autismclient.util.aa.Inner_DataUtil.HARVEST && !init_Z_F_2(wireRotation.load())) {
         return null;
      }

      ce.Inner_DataUtil silent = cn.init_ce_DataUtil(MC.player);
      if (silent != null && !init_Z_ce_DataUtil_ce_DataUtil_3(silent, wireRotation)) {
         return null;
      }

      BlockHitResult ray = ScaffoldModule.init_BlockHitResult_Vec3(MC.player.getEyePosition(), wireRotation, this.init_D_4(), MC.level, MC.player);
      if (ray == null) {
         return null;
      }

      ce.Inner_DataUtil var10000;
      switch (target.init_aa_DataUtil()) {
         case HARVEST:
            var10000 = this.init_ce_DataUtil_RingBuffer_BufferH(target, ray, wireRotation, tick);
            break;
         case BONEMEAL:
            var10000 = this.write(target, ray, wireRotation, tick);
            break;
         case TILL:
            var10000 = this.read(target, ray, wireRotation, tick);
            break;
         case REPLANT_RECENT:
         case REPLANT:
            var10000 = this.load(target, ray, wireRotation, tick);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private ce.Inner_DataUtil init_ce_DataUtil_RingBuffer_BufferH(
      AutoFarmModule.Inner_BufferHelper target, BlockHitResult ray, ce.Inner_DataUtil wireRotation, int tick
   ) {
      BlockPos cell = ray.getBlockPos();
      BlockState state = MC.level.getBlockState(cell);
      if (this.init_Set_5().contains(state.getBlock()) && this.init_Z_BlockState_3(state)) {
         if (autismclient.util.Y.load(state.getBlock()) && !MC.level.getBlockState(cell.below()).is(state.getBlock())) {
            return null;
         }

         if (target.init_I() >= 0) {
            if (!this.init_Z_I_4(target.init_I())) {
               return null;
            }

            if (this.init_I_ItemStack(MC.player.getMainHandItem()) <= 0) {
               return null;
            }
         }

         if (!this.init_Z_RingBuffer_BufferH(target, cell, state, tick)) {
            return null;
         }

         this.init_V_Z_3(true);
         if (!autismclient.util.aY.init(this.id(), ray)) {
            return null;
         }

         this.init_V_I_6(tick);
         this.stop = tick;
         this.init_Z = true;
         this.init_V_BlockPos_2(cell, state.getBlock(), tick);
         return wireRotation;
      } else {
         return null;
      }
   }

   private ce.Inner_DataUtil load(AutoFarmModule.Inner_BufferHelper target, BlockHitResult ray, ce.Inner_DataUtil wireRotation, int tick) {
      boolean offhand = this.init_Z_Item_BlockHitResult(target.init_Item(), ray);
      InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
      if (!offhand && target.init_I() < 0) {
         if (MC.player.getOffhandItem().is(target.init_Item()) && !autismclient.util.ah.load_Z(this.id())) {
            this.write(ray);
         }

         return null;
      } else {
         ItemStack planStack = offhand
            ? MC.player.getOffhandItem()
            : (target.init_I() >= 0 ? MC.player.getInventory().getItem(target.init_I()) : ItemStack.EMPTY);
         if (!planStack.is(target.init_Item())) {
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
         AutoFarmModule.Inner_CachePool actualChoice = this.bool("replant")
            ? this.init(cell.getX(), cell.getY(), cell.getZ(), below, this.init_Set_5(), tick)
            : null;
         if (actualChoice == null && this.bool("plant")) {
            actualChoice = this.init_RingBuffer_CachePo_I(cell.getX(), cell.getY(), cell.getZ(), below, this.init_Set_5());
         }

         if (actualChoice != null && actualChoice.init_Block() == target.init_Block() && actualChoice.init_Item() == target.init_Item()) {
            BlockState placed = target.init_Block().getStateForPlacement(context);
            if (placed != null && placed.canSurvive(MC.level, cell) && MC.level.isUnobstructed(placed, cell, CollisionContext.placementContext(MC.player))) {
               BlockPos clicked = ray.getBlockPos();
               if (!autismclient.util.U.load(MC.level.getBlockState(clicked), clicked, MC.player.isSecondaryUseActive())) {
                  return null;
               }

               if (!offhand) {
                  if (!this.init_Z_I_4(target.init_I())) {
                     return null;
                  }

                  if (!MC.player.getMainHandItem().is(target.init_Item())) {
                     return null;
                  }
               }

               if (al.init_Z_HitResult(ray, hand, this.id())) {
                  return null;
               }

               if (!this.open_Z_2()) {
                  return null;
               }

               this.init_V_Z_3(false);
               if (offhand && !this.init_Z_Item_BlockHitResult(target.init_Item(), ray)) {
                  return null;
               }

               if (!this.init_Z_RingBuffer_BufferH(target, cell, MC.level.getBlockState(cell), tick)) {
                  return null;
               }

               if (!autismclient.util.aY.init(this.id(), ray, hand)) {
                  return null;
               }

               this.init_V_I_6(tick);
               this.stop = tick;
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
            return this.load(ray);
         } else if (Compat263.isAxe(item)) {
            return this.read_Z(ray.getBlockPos());
         } else if (Compat263.isShovel(item)) {
            return this.read(ray);
         } else {
            return item instanceof ShearsItem ? this.write(ray.getBlockPos()) : item.getClass() != Item.class && !(item instanceof MaceItem);
         }
      }
   }

   private boolean load(BlockHitResult ray) {
      return !this.load_Z(ray.getBlockPos()) ? false : MC.level.getBlockState(ray.getBlockPos()).is(Blocks.ROOTED_DIRT) || ray.getDirection() != Direction.DOWN;
   }

   private boolean read_Z(BlockPos pos) {
      BlockState state = MC.level.getBlockState(pos);
      return read_Set.contains(state.getBlock())
         || WeatheringCopper.getPrevious(state).isPresent()
         || HoneycombItem.WAX_OFF_BY_BLOCK.get().containsKey(state.getBlock());
   }

   private boolean read(BlockHitResult ray) {
      if (ray.getDirection() == Direction.DOWN) {
         return false;
      }

      BlockPos pos = ray.getBlockPos();
      BlockState state = MC.level.getBlockState(pos);
      return state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)
         ? true
         : load_Set.contains(state.getBlock()) && MC.level.getBlockState(pos.above()).isAir();
   }

   private boolean write(BlockPos pos) {
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

   private boolean write(BlockHitResult ray) {
      int selected = MC.player.getInventory().getSelectedSlot();
      int fortune = this.bool("fortune") ? this.write_I_4() : -1;
      if (fortune >= 0 && fortune != selected && !this.init_Z_ItemStack_BlockHitResult(MC.player.getInventory().getItem(fortune), ray)) {
         if (!this.init_Z_I_I_2(fortune, selected)) {
            return false;
         }

         this.valid = fortune;
         return true;
      } else {
         for (int slot = 0; slot < 9; slot++) {
            if (slot != selected
               && !autismclient.util.ah.init(slot, this.id())
               && !this.init_Z_ItemStack_BlockHitResult(MC.player.getInventory().getItem(slot), ray)) {
               if (!this.init_Z_I_I_2(slot, selected)) {
                  return false;
               }

               this.valid = slot;
               return true;
            }
         }

         return false;
      }
   }

   private ce.Inner_DataUtil read(AutoFarmModule.Inner_BufferHelper target, BlockHitResult ray, ce.Inner_DataUtil wireRotation, int tick) {
      if (!this.load(ray)) {
         return null;
      }

      ItemStack offStack = MC.player.getOffhandItem();
      boolean offhand = init_Z_ItemStack_3(offStack) && this.init_Z_Item_BlockHitResult(offStack.getItem(), ray);
      InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
      if (!offhand) {
         if (target.init_I() < 0) {
            if (this.push()) {
               this.write(ray);
            }

            return null;
         }

         if (!this.init_Z_I_4(target.init_I())) {
            return null;
         }

         if (!init_Z_ItemStack_3(MC.player.getMainHandItem())) {
            return null;
         }
      }

      if (MC.player.getCooldowns().isOnCooldown(MC.player.getItemInHand(hand))) {
         return null;
      }

      if (al.init_Z_HitResult(ray, hand, this.id())) {
         return null;
      }

      if (!this.open_Z_2()) {
         return null;
      }

      this.init_V_Z_3(false);
      if (offhand && !this.init_Z_Item_BlockHitResult(offStack.getItem(), ray)) {
         return null;
      }

      if (!this.init_Z_RingBuffer_BufferH(target, ray.getBlockPos(), MC.level.getBlockState(ray.getBlockPos()), tick)) {
         return null;
      }

      if (!autismclient.util.aY.init(this.id(), ray, hand)) {
         return null;
      }

      this.init_V_I_6(tick);
      this.stop = tick;
      this.init_Z = false;
      return wireRotation;
   }

   private ce.Inner_DataUtil write(AutoFarmModule.Inner_BufferHelper target, BlockHitResult ray, ce.Inner_DataUtil wireRotation, int tick) {
      if (!this.init_Z_BlockPos_3(ray.getBlockPos())) {
         return null;
      }

      boolean offhand = this.init_Z_Item_BlockHitResult(Items.BONE_MEAL, ray);
      InteractionHand hand = offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
      if (!offhand) {
         if (target.init_I() < 0) {
            if (MC.player.getOffhandItem().is(Items.BONE_MEAL) && !autismclient.util.ah.load_Z(this.id())) {
               this.write(ray);
            }

            return null;
         }

         if (!this.init_Z_I_4(target.init_I())) {
            return null;
         }

         if (!MC.player.getMainHandItem().is(Items.BONE_MEAL)) {
            return null;
         }
      }

      if (al.init_Z_HitResult(ray, hand, this.id())) {
         return null;
      }

      if (!this.open_Z_2()) {
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

      if (!autismclient.util.aY.init(this.id(), ray, hand)) {
         return null;
      }

      this.init_V_I_6(tick);
      this.stop = tick;
      this.init_Z = false;
      return wireRotation;
   }

   private boolean open_Z_2() {
      boolean useKeyDown = autismclient.util.aY.init_Z(this.id()) && !this.init_Z;
      return useKeyDown || ((AutismMinecraftAccessor)MC).autism$getRightClickDelay() <= 0;
   }

   private void init_V_I_6(int tick) {
      this.start = tick;
   }

   private void init_V_Z_3(boolean attack) {
      if (autismclient.util.aY.init_Z(this.id()) && this.init_Z != attack) {
         autismclient.util.aY.init_V(this.id());
      }
   }

   private void write_V_8() {
      autismclient.util.aY.init_V(this.id());
      this.stop = -2147483648;
   }

   private void update_V_7() {
      if (this.stop != ck.init_ck().state_I()) {
         this.write_V_8();
      }
   }

   private void init_V_BlockPos_2(BlockPos pos, Block crop, int tick) {
      this.init_V_BlockPos_2(pos);
      if ((this.bool("replant") || this.bool("plant")) && autismclient.util.Y.init_Item_Block(crop) != null && this.init_ac.init_Obj(pos.asLong()) == null) {
         this.init_ac.init(pos.asLong(), new AutoFarmModule.Inner_StreamUtil(pos.immutable(), crop, tick, ca.init_J_BlockPos(pos), false));
      }
   }

   private boolean init_Z_RingBuffer_StreamU(AutoFarmModule.Inner_StreamUtil entry) {
      int now = ck.init_ck().state_I();
      ClientPacketListener connection = MC.getConnection();
      PlayerInfo info = connection == null ? null : connection.getPlayerInfo(MC.player.getUUID());
      if (entry.init_Z() && !autismclient.util.ab.init(now, entry.init_I(), info == null ? 0 : info.getLatency())) {
         ca.Inner_DataUtil update = ca.init_ca_DataUtil_BlockPos(entry.init_BlockPos());
         return update == null || update.revision() <= entry.init_J() || !update.state().isAir();
      } else {
         return true;
      }
   }

   private void sync_V_3() {
      int tick = ck.init_ck().state_I();
      this.init_ac
         .init_Collection()
         .removeIf(
            entry -> {
               BlockPos pos = entry.init_BlockPos();
               if (MC.level.hasChunkAt(pos) && tick != entry.init_I()) {
                  BlockState state = MC.level.getBlockState(pos);
                  if (state.is(entry.init_Block())) {
                     ca.Inner_DataUtil update = ca.init_ca_DataUtil_BlockPos(pos);
                     return !this.load(state)
                        && update != null
                        && update.revision() > entry.init_J()
                        && update.state().is(entry.init_Block())
                        && !this.load(update.state());
                  }

                  if (!state.canBeReplaced()) {
                     return true;
                  }

                  ca.Inner_DataUtil update = ca.init_ca_DataUtil_BlockPos(pos);
                  if (!entry.init_Z() && update != null && update.revision() > entry.init_J() && update.state().isAir()) {
                     this.init_ac.init(pos.asLong(), new AutoFarmModule.Inner_StreamUtil(pos, entry.init_Block(), tick, entry.init_J(), true));
                  }

                  return !this.init_Z_Block(entry.init_Block(), MC.level.getBlockState(pos.below()), pos.getX(), pos.getY(), pos.getZ());
               } else {
                  return false;
               }
            }
         );
   }

   private boolean init_Z_RingBuffer_BufferH(AutoFarmModule.Inner_BufferHelper target, BlockPos cell, BlockState state, int tick) {
      int limit = 40;
      if (target.init_aa_DataUtil() == autismclient.util.aa.Inner_DataUtil.HARVEST) {
         float progress = state.getDestroyProgress(MC.player, MC.level, cell);
         if (progress > 0.0F && Float.isFinite(progress)) {
            limit += (int)Math.min(1200.0, Math.ceil(1.0 / progress));
         }
      }

      if (this.init_X.init(cell.asLong(), new AutoFarmModule.Inner_DataUtil(target.init_aa_DataUtil(), state), tick, limit)) {
         return true;
      }

      this.load(cell, tick);
      this.load(target.init_BlockPos(), tick);
      this.write_V_8();
      return false;
   }

   private void init_V_BlockPos_2(BlockPos pos) {
      if (!this.init_Long2IntOpenHashMa.isEmpty()) {
         int px = pos.getX();
         int py = pos.getY();
         int pz = pos.getZ();
         this.init_Long2IntOpenHashMa.keySet().removeIf(key -> {
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
         : hitResult instanceof EntityHitResult && this.id().equals(autismclient.util.aD.init_Str()) && autismclient.util.aD.read_Z();
   }

   private boolean poll_Z_2() {
      if (this.stop != -2147483648 && autismclient.util.aY.init_Z(this.id())) {
         int tick = ck.init_ck().state_I();
         int age = tick - this.stop;
         return age >= 0 && age <= 1;
      } else {
         return false;
      }
   }

   private static boolean init_Z_F_2(float pitch) {
      return ScaffoldModule.read_Z_F(Math.abs(pitch));
   }

   private static boolean init_Z_ce_DataUtil_ce_DataUtil_3(ce.Inner_DataUtil first, ce.Inner_DataUtil second) {
      float epsilon = (float)Math.max(0.05000000074505806, ce.init() + 0.05000000074505806);
      return Math.abs(ce.init(first.init(), second.init())) <= epsilon && Math.abs(first.load() - second.load()) <= epsilon;
   }

   private boolean init_Z_I_4(int slot) {
      int selected = MC.player.getInventory().getSelectedSlot();
      if (selected == slot) {
         return true;
      }

      if (!this.init_Z_I_I_2(slot, selected)) {
         return false;
      }

      this.valid = slot;
      return false;
   }

   private boolean init_Z_I_I_2(int slot, int selected) {
      if (BedDefenderModule.init_Z_10() || SurroundModule.init_Z_27() || CrystalAuraModule.init_Z_51() || AnchorAuraModule.init_Z_2()) {
         return false;
      }

      if (this.flush_Z_3()) {
         return false;
      }

      int tick = ck.init_ck().state_I();
      if (tick == this.data) {
         return false;
      }

      if (!autismclient.util.ah.write_Z(this.id())) {
         return false;
      }

      try {
         if (this.prev < 0) {
            this.prev = selected;
         }

         if (slot < 0 || slot >= MC.options.keyHotbarSlots.length || autismclient.util.ah.init(slot, this.id())) {
            return false;
         }

         autismclient.util.aB key = autismclient.util.aB.init(MC.options.keyHotbarSlots[slot]);
         key.autism$simulatePress(true);
         key.autism$simulatePress(false);
         key.autism$resetPressedState();
      } finally {
         autismclient.util.ah.read_V(this.id());
      }

      this.data = tick;
      this.value = this.init_Random.nextInt(3);
      return true;
   }

   private int init_I_Item(Item item) {
      this.flush_V_3();
      return this.read_Map.getOrDefault(item, -1);
   }

   private void flush_V_3() {
      int tick = ck.init_ck().state_I();
      if (tick != this.mark) {
         this.mark = tick;
         this.parse = -1;
         this.bind = -1;
         this.read_Map.clear();
         int selected = MC.player.getInventory().getSelectedSlot();
         int bestFortuneDistance = 2147483647;
         boolean bestFortuneHoe = false;

         for (int slot = 0; slot < 9; slot++) {
            if (!autismclient.util.ah.init(slot, this.id())) {
               ItemStack stack = MC.player.getInventory().getItem(slot);
               if (!stack.isEmpty()) {
                  int distance = Math.abs(slot - selected);
                  this.read_Map
                     .merge(
                        stack.getItem(), slot, (oldSlot, newSlot) -> (Integer)(Math.abs(newSlot - selected) < Math.abs(oldSlot - selected) ? newSlot : oldSlot)
                     );
                  boolean hoe = init_Z_ItemStack_3(stack);
                  if (hoe && (this.parse < 0 || distance < Math.abs(this.parse - selected))) {
                     this.parse = slot;
                  }

                  if (this.init_I_ItemStack(stack) > 0 && (this.bind < 0 || hoe && !bestFortuneHoe || hoe == bestFortuneHoe && distance < bestFortuneDistance)) {
                     this.bind = slot;
                     bestFortuneHoe = hoe;
                     bestFortuneDistance = distance;
                  }
               }
            }
         }

         ItemStack offhand = MC.player.getOffhandItem();
         this.init_Item = !offhand.isEmpty() && !autismclient.util.ah.load_Z(this.id()) ? offhand.getItem() : null;
      }
   }

   private boolean init_Z_Item(Item item) {
      this.flush_V_3();
      return this.init_Item == item;
   }

   private boolean init_Z_Item_BlockHitResult(Item item, BlockHitResult ray) {
      return MC.player.getOffhandItem().is(item) && !autismclient.util.ah.load_Z(this.id()) && !this.init_Z_BlockHitResult(ray);
   }

   private boolean push() {
      return !autismclient.util.ah.load_Z(this.id()) && init_Z_ItemStack_3(MC.player.getOffhandItem());
   }

   private boolean pop() {
      return this.load_I_6() >= 0 || this.push();
   }

   private int load_I_6() {
      this.flush_V_3();
      return this.parse;
   }

   private int read_I_4() {
      return this.init_I_Item(Items.BONE_MEAL);
   }

   private boolean tick_Z_3() {
      return this.init_Z_Item(Items.BONE_MEAL) || this.read_I_4() >= 0;
   }

   private int write_I_4() {
      this.flush_V_3();
      return this.bind;
   }

   private static boolean init_Z_ItemStack_3(ItemStack stack) {
      return Compat263.isHoe(stack.getItem());
   }

   private int init_I_ItemStack(ItemStack stack) {
      try {
         if (MC != null && MC.level != null && stack != null && !stack.isEmpty()) {
            if (this.load_ClientLevel != MC.level) {
               this.load_ClientLevel = MC.level;
               this.init_Holder = MC.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
            }

            return EnchantmentHelper.getItemEnchantmentLevel(this.init_Holder, stack);
         } else {
            return 0;
         }
      } catch (Throwable t) {
         return 0;
      }
   }

   private void close_V_3() {
      if (this.prev >= 0 && this.size <= 0) {
         this.size = 2 + this.init_Random.nextInt(3);
      }
   }

   private void open_V_2() {
      if (this.prev >= 0) {
         if (!this.bool("switch-back")) {
            this.prev = -1;
            this.valid = -1;
            this.size = 0;
         } else if (MC.gui != null && MC.gui.screen() == null && MC.gui.overlay() == null) {
            if (this.valid >= 0 && MC.player.getInventory().getSelectedSlot() != this.valid) {
               this.prev = -1;
               this.valid = -1;
               this.size = 0;
            } else if (this.size > 0 && --this.size <= 0) {
               if (!autismclient.util.ah.init(this.prev, this.id()) && this.init_Z_I_I_2(this.prev, MC.player.getInventory().getSelectedSlot())) {
                  this.value = 0;
                  this.prev = -1;
                  this.valid = -1;
               } else {
                  this.size = 1;
               }
            }
         }
      }
   }

   private void poll_V_2() {
      int tick = ck.init_ck().state_I();
      BlockPos goal = this.init_BlockPos;
      if (goal != null && (bA.write(goal) || this.init_aa_DataUtil_BlockPos_I(goal, tick) == null)) {
         goal = null;
         this.init_BlockPos = null;
         this.cache = tick - 20;
      }

      if (goal == null && (this.cache == -2147483648 || tick - this.cache >= 20)) {
         goal = this.init_BlockPos_2();
         if (!this.run()) {
            this.cache = tick;
         }

         this.init_BlockPos = goal;
      }

      if (goal == null) {
         bA.load();
         this.init_V_ce_DataUtil_2(null);
      } else if (!bA.read(goal)) {
         if (bA.load(goal)) {
            this.init_BlockPos = null;
            bA.load();
            this.next = tick + 2 + this.init_Random.nextInt(3);
            this.cache = tick - 20;
            this.init_V_ce_DataUtil_2(null);
         } else {
            bA.load();
            bA.init_V_BlockPos_I(goal, 200);
            this.init_BlockPos = null;
            this.cache = tick - 20;
            this.init_V_ce_DataUtil_2(null);
         }
      } else {
         this.load_Str = "Walk";
         BlockPos walkNode = bA.init_BlockPos();
         this.init_V_ce_DataUtil_2(ce.init(Vec3.atCenterOf(walkNode != null ? walkNode : goal), MC.player.getEyePosition()));
      }
   }

   private boolean init_Z_RingBuffer_BufferH(AutoFarmModule.Inner_BufferHelper target) {
      int tick = ck.init_ck().state_I();
      Vec3 eye = MC.player.getEyePosition();
      double reachSqr = this.init_D_4() * this.init_D_4();
      BlockPos goal = this.init_BlockPos_RingBuffer_BufferH(target, eye, reachSqr, tick);
      if (goal == null) {
         return false;
      } else if (bA.read(goal)) {
         this.cache = tick - 20;
         return true;
      } else {
         this.read_BlockPos = null;
         if (bA.load(goal)) {
            bA.load();
            return false;
         } else {
            bA.load();
            bA.init_V_BlockPos_I(goal, 200);
            this.queue = tick - 20;
            return false;
         }
      }
   }

   private BlockPos init_BlockPos_RingBuffer_BufferH(AutoFarmModule.Inner_BufferHelper target, Vec3 eye, double reachSqr, int tick) {
      if (target.init_aa_DataUtil() == autismclient.util.aa.Inner_DataUtil.HARVEST && this.state == tick && this.load_BlockPos != null) {
         return init_D_Vec3(eye, this.load_BlockPos.getX(), this.load_BlockPos.getY(), this.load_BlockPos.getZ()) > reachSqr
               && !bA.write(this.load_BlockPos)
               && this.init_aa_DataUtil_BlockPos_I(this.load_BlockPos, tick) != null
            ? this.load_BlockPos
            : null;
      }

      BlockPos goal = this.read_BlockPos;
      if (goal != null
         && (bA.write(goal) || this.init_aa_DataUtil_BlockPos_I(goal, tick) == null || init_D_Vec3(eye, goal.getX(), goal.getY(), goal.getZ()) <= reachSqr)) {
         goal = null;
         this.read_BlockPos = null;
         this.queue = tick - 20;
      }

      if (goal == null && (this.queue == -2147483648 || tick - this.queue >= 20)) {
         goal = this.init_BlockPos_D(reachSqr, 90.0F, Math.min(12, this.init_I_9()));
         if (!this.run()) {
            this.queue = tick;
         }

         this.read_BlockPos = goal;
      }

      return goal;
   }

   private autismclient.util.aa.Inner_DataUtil init_aa_DataUtil_BlockPos_I(BlockPos goal, int tick) {
      Set<Block> crops = this.init_Set_5();
      if (crops.isEmpty()) {
         return null;
      }

      boolean wantReplant = this.bool("replant");
      boolean wantPlant = this.bool("plant");
      boolean wantTill = this.bool("till") && this.pop();
      boolean wantBonemeal = this.bool("bonemeal") && this.tick_Z_3();
      return this.init(
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

   private BlockPos init_BlockPos_2() {
      return this.init_BlockPos_D(0.0, 360.0F, this.init_I_9());
   }

   private boolean run() {
      return this.write_Z;
   }

   private BlockPos init_BlockPos_D(double minDistSqr, float maxCorrection, int maxRing) {
      Set<Block> crops = this.init_Set_5();
      if (crops.isEmpty()) {
         this.write_Z = false;
         return null;
      }

      BlockPos origin = MC.player.blockPosition();
      int manhattan = this.init_I_9();
      if (this.write_Z
         && (
            this.load_D != minDistSqr
               || this.update_F != maxCorrection
               || this.f_b_I != maxRing
               || this.f_c_I != manhattan
               || origin.getX() != this.f_d_I
               || origin.getY() != this.f_e_I
               || origin.getZ() != this.f_f_I
         )) {
         this.write_Z = false;
      }

      if (!this.write_Z) {
         this.write_Z = true;
         this.load_D = minDistSqr;
         this.update_F = maxCorrection;
         this.f_b_I = maxRing;
         this.f_c_I = manhattan;
         this.init_J = (long)maxRing * maxRing;
         this.f_d_I = origin.getX();
         this.f_e_I = origin.getY();
         this.f_f_I = origin.getZ();
         this.read_D = MC.player.getX();
         this.write_D = MC.player.getZ();
         this.load_Vec3 = MC.player.getEyePosition();
         this.sync_F = this.load_ce_DataUtil_2().init();
         this.f_g_I = 0;
         this.f_h_I = 0;
         this.f_i_I = 0;
         this.f_j_I = 0;
         this.init_List.clear();
      }

      int tick = ck.init_ck().state_I();
      boolean wantReplant = this.bool("replant");
      boolean wantPlant = this.bool("plant");
      boolean wantTill = this.bool("till") && this.pop();
      boolean wantBonemeal = this.bool("bonemeal") && this.tick_Z_3();
      MutableBlockPos cursor = new MutableBlockPos();
      int budget = 8000;

      while (this.write_Z) {
         if (budget <= 0) {
            return null;
         }

         if (this.f_g_I > this.f_b_I) {
            this.write_Z = false;
            break;
         }

         int edgeCount = this.f_g_I == 0 ? 1 : this.f_g_I * 8;
         if (this.f_h_I >= edgeCount) {
            this.f_g_I++;
            this.f_h_I = 0;
            this.f_i_I = 0;
            this.f_j_I = 0;
         } else {
            int dx;
            int dz;
            if (this.f_g_I == 0) {
               dx = 0;
               dz = 0;
            } else {
               int side = this.f_h_I / (this.f_g_I * 2);
               int offset = this.f_h_I % (this.f_g_I * 2) - this.f_g_I;
               switch (side) {
                  case 0:
                     dx = offset;
                     dz = -this.f_g_I;
                     break;
                  case 1:
                     dx = offset + 1;
                     dz = this.f_g_I;
                     break;
                  case 2:
                     dx = -this.f_g_I;
                     dz = offset + 1;
                     break;
                  default:
                     dx = this.f_g_I;
                     dz = offset;
               }
            }

            long horizontalSqr = (long)dx * dx + (long)dz * dz;
            if (Math.abs(dx) + Math.abs(dz) > this.f_c_I) {
               this.f_h_I++;
            } else if (this.f_i_I > 12) {
               this.f_h_I++;
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
                  if (horizontalSqr + (long)dy * dy <= this.init_J) {
                     budget--;
                     int cx = this.f_d_I + dx;
                     int cy = this.f_e_I + dy;
                     int cz = this.f_f_I + dz;
                     cursor.set(cx, cy, cz);
                     if (MC.level.hasChunkAt(cursor)
                        && !bA.write(cursor)
                        && this.init(cursor, cx, cy, cz, crops, tick, wantReplant, wantPlant, wantTill, wantBonemeal) != null
                        && (!(this.load_D > 0.0) || !(init_D_Vec3(this.load_Vec3, cx, cy, cz) <= this.load_D))) {
                        float correction = Math.abs(ce.init(this.sync_F, init_F_D(this.read_D, this.write_D, cx, cz)));
                        if (!(correction > this.update_F)) {
                           this.init_V_BlockPos_D(new BlockPos(cx, cy, cz), Math.sqrt(init_D_Vec3(this.load_Vec3, cx, cy, cz)) * 10.0 + correction / 18.0);
                        }
                     }
                  }
               }
            }
         }
      }

      return this.init_BlockPos_I(tick);
   }

   private void init_V_BlockPos_D(BlockPos cell, double estimate) {
      for (int i = 0; i < this.init_List.size(); i++) {
         AutoFarmModule.Inner_ConfigStore old = this.init_List.get(i);
         if (old.init_BlockPos().distSqr(cell) <= 9.0) {
            if (old.init_D() > estimate) {
               this.init_List.set(i, new AutoFarmModule.Inner_ConfigStore(cell, estimate));
            }

            return;
         }
      }

      this.init_List.add(new AutoFarmModule.Inner_ConfigStore(cell, estimate));
      this.init_List.sort(Comparator.comparingDouble(AutoFarmModule.Inner_ConfigStore::init_D));
      if (this.init_List.size() > 6) {
         this.init_List.removeLast();
      }
   }

   private BlockPos init_BlockPos_I(int tick) {
      BlockPos best = null;
      double bestCost = 1.0 / 0.0;

      for (AutoFarmModule.Inner_ConfigStore choice : this.init_List) {
         BlockPos cell = choice.init_BlockPos();
         if (this.init_aa_DataUtil_BlockPos_I(cell, tick) != null && !bA.write(cell)) {
            double cost = bA.init_D_BlockPos(cell);
            if (Double.isFinite(cost)) {
               int neighbours = 0;

               for (Direction direction : Plane.HORIZONTAL) {
                  if (this.init_aa_DataUtil_BlockPos_I(cell.relative(direction), tick) != null) {
                     neighbours++;
                  }
               }

               float correction = Math.abs(ce.init(this.load_ce_DataUtil_2().init(), init_F_D(MC.player.getX(), MC.player.getZ(), cell.getX(), cell.getZ())));
               cost += correction / 18.0 - neighbours;
               if (cost < bestCost) {
                  bestCost = cost;
                  best = cell;
               }
            }
         }
      }

      this.init_List.clear();
      return best;
   }

   private static float init_F_D(double px, double pz, int x, int z) {
      return (float)(Math.toDegrees(Math.atan2(z + 0.5 - pz, x + 0.5 - px)) - 90.0);
   }

   private autismclient.util.aa.Inner_DataUtil init(
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
         if (this.init_Z_BlockState_3(state) && this.init_Z_BlockPos_MutableBl_2(cursor, cx, cy, cz, block)) {
            return autismclient.util.aa.Inner_DataUtil.HARVEST;
         } else {
            return wantBonemeal && this.init_Z_BlockPos_BlockState_2(cursor.set(cx, cy, cz), state) ? autismclient.util.aa.Inner_DataUtil.BONEMEAL : null;
         }
      } else if ((wantReplant || wantPlant) && state.canBeReplaced()) {
         BlockState below = MC.level.getBlockState(cursor.set(cx, cy - 1, cz));
         if (wantReplant && this.init(cx, cy, cz, below, crops, tick) != null) {
            return autismclient.util.aa.Inner_DataUtil.REPLANT_RECENT;
         } else {
            return wantPlant && this.init_RingBuffer_CachePo_I(cx, cy, cz, below, crops) != null ? autismclient.util.aa.Inner_DataUtil.REPLANT : null;
         }
      } else {
         return wantTill && init_Set.contains(block) && (block == Blocks.ROOTED_DIRT || MC.level.getBlockState(cursor.set(cx, cy + 1, cz)).isAir())
            ? autismclient.util.aa.Inner_DataUtil.TILL
            : null;
      }
   }

   private static double init_D_Vec3(Vec3 eye, int x, int y, int z) {
      double dx = eye.x < x ? x - eye.x : (eye.x > x + 1 ? eye.x - (x + 1) : 0.0);
      double dy = eye.y < y ? y - eye.y : (eye.y > y + 1 ? eye.y - (y + 1) : 0.0);
      double dz = eye.z < z ? z - eye.z : (eye.z > z + 1 ? eye.z - (z + 1) : 0.0);
      return dx * dx + dy * dy + dz * dz;
   }

   private static int[] init_IArr_I_2(int radius) {
      int clamped = Mth.clamp(radius, 0, 8);
      if (clamped == block) {
         return load_IArr;
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

      block = clamped;
      load_IArr = flat;
      return flat;
   }

   private record Inner_BufferHelper(
      autismclient.util.aa.Inner_DataUtil init_aa_DataUtil,
      BlockPos init_BlockPos,
      Block init_Block,
      Item init_Item,
      int init_I,
      ce.Inner_DataUtil init_ce_DataUtil
   ) {
   }

   private record Inner_CachePool(Block init_Block, Item init_Item, int init_I) {
   }

   private record Inner_ConfigStore(BlockPos init_BlockPos, double init_D) {
   }

   private record Inner_DataUtil(autismclient.util.aa.Inner_DataUtil init_aa_DataUtil, BlockState init_BlockState) {
   }

   private record Inner_StreamUtil(BlockPos init_BlockPos, Block init_Block, int init_I, long init_J, boolean init_Z) {
   }
}
