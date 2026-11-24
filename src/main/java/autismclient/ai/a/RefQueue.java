package autismclient.ai.a;

import autismclient.AutismClientAddon;
import autismclient.ai.aV;
import autismclient.ai.af;
import autismclient.ai.cG;
import autismclient.ai.ca;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.util.AutismLiteVariant;
import autismclient.util.U;
import autismclient.util.aB;
import autismclient.util.ai;
import autismclient.util.ce;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RefQueue implements ClassCache.Inner_TaskQueue {
   private static final double run_D = 0.42250000000000004;
   private static final int open_I = 16;
   private static final int poll_I = 6;
   private static final int push_I = 6;
   private static final int pop_I = 24;
   static final int init_I = 3;
   static final double init_D = 6.0;
   static final double load_D = 30.0;
   private static final ItemStack[] init_ItemStackArr = new ItemStack[9];
   private static final boolean init_Z = Boolean.getBoolean("autism.navigation.debug");
   private static final boolean load_Z = Boolean.getBoolean("autism.navigation.trace");
   static final int load_I = 5;
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final DataUtil init_DataUtil = new DataUtil();
   private final RefQueue.Inner_DataUtil init_RefQueue_DataUtil = new RefQueue.Inner_DataUtil(
      this.init_Minecraft, this::load_V_I, this::init_V_Vec3, false, false, this.init_DataUtil::load_V, () -> !this.init_DataUtil.init_Z()
   );
   private List<ClassCache.UiHelper> init_List = List.of();
   private int tick_I;
   private boolean read_Z = true;
   private RefQueue.Inner_TaskQueue init_RefQueue_TaskQueue = RefQueue.Inner_TaskQueue.init;
   private ClassCache.UiHelper init_ClassCache_UiHelpe;
   private int run_I;
   private int get_I;
   private int set_I;
   private int put_I;
   private boolean write_Z;
   private BlockPos init_BlockPos;
   private long read_J;
   private Block init_Block;
   private boolean update_Z;
   private boolean sync_Z;
   private boolean flush_Z;
   private int add_I;
   private BlockPos load_BlockPos;
   private int clear_I;
   private int reset_I;
   private boolean close_Z;
   private boolean open_Z;
   private boolean poll_Z;
   private BitSetUtil init_BitSetUtil;
   private ClassCache.Inner_NetHelper init_ClassCache_NetHelp;
   private int start_I;
   private float close_F;
   private int stop_I;
   private int next_I;
   private boolean push_Z;
   private TableIndex.Inner_StateCache init_TableIndex_StateCa;
   private PoolEntry init_PoolEntry;
   private int prev_I;
   private boolean pop_Z;
   private boolean tick_Z;
   private boolean run_Z;
   private boolean get_Z;
   private int valid_I;
   private int size_I;
   private int data_I;
   private int value_I;
   private int cache_I;
   private boolean set_Z;
   private UtilCore init_UtilCore;
   private ItemStack[] load_ItemStackArr;
   private String init_Str;
   private int state_I;
   private boolean put_Z;
   private AiOperation init_AiOperation;
   private boolean add_Z;
   private boolean clear_Z;
   private StateCache init_StateCache;
   private int queue_I;
   private int buffer_I;
   private int entry;
   private double get_D;
   private BlockPos read_BlockPos;
   private ClassCache.UiHelper load_ClassCache_UiHelpe;
   private final RefQueue.Inner_CachePool init_RefQueue_CachePool;
   private boolean reset_Z;
   static final int read_I = 3;
   static final long init_J = 45000000000L;
   static final long load_J = 500000000L;
   private boolean start_Z;
   private boolean stop_Z;
   private Predicate<BlockPos> init_Predicate;
   static final int write_I = 20;
   private int node;
   private boolean next_Z;
   private Predicate<BlockPos> load_Predicate;
   private Function<BlockPos, Vec3> init_Function;
   private Function<Predicate<AABB>, Vec3> load_Function;
   private Runnable init_Runnable;
   private boolean prev_Z;
   private int task;
   static final double read_D = 10.0;
   private ClassCache.UiHelper read_ClassCache_UiHelpe;
   private boolean valid_Z;
   static final int update_I = 80;
   static final float init_F = 60.0F;
   static final float load_F = 60.0F;
   private boolean size_Z;
   private int pool;
   private int index;
   private boolean data_Z;
   private ClassCache.UiHelper write_ClassCache_UiHelpe;
   private ClassCache.UiHelper update_ClassCache_UiHelpe;
   static final int sync_I = 20;
   static final double write_D = 0.12;
   static final double update_D = 4.5;
   static final float read_F = 22.5F;
   static final double sync_D = 0.5;
   private static LocalPlayer init_LocalPlayer;
   private static int count;
   private Vec3 init_Vec3;
   private boolean value_Z;
   private int flags;
   private RefQueue.Inner_StreamUtil init_RefQueue_StreamUti;
   static final float write_F = 28.0F;
   private BlockPos write_BlockPos;
   private BlockPos update_BlockPos;
   private boolean cache_Z;
   private int level;
   private int frame;
   private int block;
   private int chunk;
   private ClassCache.UiHelper sync_ClassCache_UiHelpe;
   private boolean state_Z;
   private ClassCache.UiHelper flush_ClassCache_UiHelpe;
   private static volatile LongPredicate init_LongPredicate = key -> false;
   private static volatile long write_J;
   private static volatile RefQueue.Inner_ConfigStore init_RefQueue_ConfigSto;
   private EventBus.Inner_DataUtil init_EventBus_DataUtil;
   private Set<BlockPos> init_Set;
   static final double flush_D = 0.2;
   private EventBus.Inner_DataUtil load_EventBus_DataUtil;
   private ClassCache.UiHelper close_ClassCache_UiHelpe;
   private Set<BlockPos> load_Set;
   private final Set<BlockPos> read_Set;
   private NetHelper init_NetHelper;
   private int table;
   private int store;
   private int drain;
   private int fill;
   private int mark;
   private static volatile Set<Long> write_Set = Set.of();
   private static volatile long update_J;
   static final double close_D = 50.0;
   static final double open_D = 4.0;
   private int scan;
   static final double poll_D = 1.25;
   static final double push_D = 1.6;
   private boolean queue_Z;
   private Vec3 load_Vec3;
   private float open_F;
   private float poll_F;
   private int parse;
   private boolean buffer_Z;
   private static volatile float push_F;
   private static volatile long sync_J;
   static final float update_F = 5.0F;
   static final float sync_F = 4.0F;
   static final double pop_D = 0.36;
   static final double tick_D = 0.55;
   private static final long flush_J = 6510516211317473281L;
   private String load_Str;
   private BlockPos sync_BlockPos;
   static final float flush_F = 15.0F;
   static final int flush_I = 3;
   static final int close_I = 20;
   private float pop_F;
   private float tick_F;
   private int bind;
   private int f_a_I;
   private final ArrayDeque<Integer> init_ArrayDeque;
   private final ArrayDeque<String> load_ArrayDeque;
   private int f_b_I;
   private Vec3 read_Vec3;

   public RefQueue() {
      this.init_ClassCache_NetHelp = ClassCache.Inner_NetHelper.init_ClassCache_NetHelp;
      this.close_F = 0.0F / 0.0F;
      this.stop_I = 2147483647;
      this.value_I = -1;
      this.cache_I = -1;
      this.init_RefQueue_CachePool = new RefQueue.Inner_CachePool();
      this.start_Z = true;
      this.init_Predicate = cell -> true;
      this.frame = -2147483648;
      this.init_Set = Set.of();
      this.load_Set = Set.of();
      this.read_Set = new HashSet<>();
      this.mark = -1073741824;
      this.scan = -1;
      this.open_F = 0.0F / 0.0F;
      this.load_Str = "";
      this.pop_F = 0.0F / 0.0F;
      this.init_ArrayDeque = new ArrayDeque<>();
      this.load_ArrayDeque = new ArrayDeque<>();
      this.f_b_I = -2147483648;
   }

   void init_V_ClassCache_NetHelp(ClassCache.Inner_NetHelper policy) {
      this.init_ClassCache_NetHelp = policy;
   }

   public void init_V_List(List<ClassCache.UiHelper> movements) {
      this.init_V_List_Z(movements, true);
   }

   public void init_V_List_Z(List<ClassCache.UiHelper> movements, boolean reachesGoal) {
      this.read_ClassCache_UiHelpe = this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.load ? this.init_ClassCache_UiHelpe : null;
      this.update_V_Z(false);
      this.init_List = List.copyOf(movements);
      this.tick_I = 0;
      this.init_ClassCache_UiHelpe = null;
      this.read_Z = reachesGoal;
      this.init_RefQueue_TaskQueue = RefQueue.Inner_TaskQueue.load;
   }

   public int init_I() {
      return this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.load && this.read_Z && this.init_List != null ? this.init_List.size() - this.tick_I : -1;
   }

   public int load_I() {
      return this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.load && this.init_List != null && !this.init_List.isEmpty()
         ? this.init_List.size() - this.tick_I
         : -1;
   }

   public Set<BlockPos> init_Set() {
      if (this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.load && this.init_List != null && !this.init_List.isEmpty()) {
         Set<BlockPos> out = new HashSet<>();

         for (int i = Math.max(0, this.tick_I); i < this.init_List.size(); i++) {
            ClassCache.UiHelper m = this.init_List.get(i);
            out.add(m.from().below());
            out.add(m.to().below());
            int dx = m.to().getX() - m.from().getX();
            int dz = m.to().getZ() - m.from().getZ();
            if (dx != 0 && dz != 0) {
               out.add(m.from().offset(dx, -1, 0));
               out.add(m.from().offset(0, -1, dz));
            }
         }

         return out;
      } else {
         return Set.of();
      }
   }

   public BlockPos init_BlockPos() {
      return this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.load && this.init_List != null && !this.init_List.isEmpty()
         ? this.init_List.get(this.init_List.size() - 1).to()
         : null;
   }

   public boolean init_Z_List_Z(List<ClassCache.UiHelper> movements, boolean reachesGoal) {
      if (this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.load && !this.init_List.isEmpty() && !movements.isEmpty()) {
         if (!movements.get(0).from().equals(this.init_List.get(this.init_List.size() - 1).to())) {
            return false;
         }

         List<ClassCache.UiHelper> joined = new ArrayList<>(this.init_List.size() - this.tick_I + movements.size());
         joined.addAll(this.init_List.subList(this.tick_I, this.init_List.size()));
         joined.addAll(movements);
         this.init_List = List.copyOf(joined);
         this.table = this.table - this.tick_I;
         this.store = this.store - this.tick_I;
         this.tick_I = 0;
         this.read_Z = reachesGoal;
         return true;
      } else {
         return false;
      }
   }

   public RefQueue.Inner_TaskQueue init_RefQueue_TaskQueue() {
      return this.init_RefQueue_TaskQueue;
   }

   public BlockPos load_BlockPos() {
      return this.read_BlockPos;
   }

   public ClassCache.UiHelper init_ClassCache_UiHelpe() {
      return this.load_ClassCache_UiHelpe;
   }

   public boolean init_Z() {
      return this.reset_Z;
   }

   public void init_V() {
      this.init_RefQueue_CachePool.init();
   }

   static long init_J_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      return (m.from().asLong() * 31L + m.to().asLong()) * 31L + m.type().ordinal();
   }

   public int read_I() {
      return this.queue_I;
   }

   public int write_I() {
      return this.buffer_I;
   }

   public int update_I() {
      return this.entry;
   }

   public int sync_I() {
      return this.state_I;
   }

   public void init_V_Z(boolean allow) {
      this.set_Z = allow;
      if (allow && this.init_UtilCore == null) {
         this.init_UtilCore = new UtilCore(this.init_Minecraft);
      }
   }

   public void init_V_StateCache(StateCache memory) {
      this.init_StateCache = memory;
   }

   public void load_V_Z(boolean allow) {
      this.start_Z = allow;
   }

   public void read_V_Z(boolean enabled) {
      this.stop_Z = enabled;
   }

   public void init_V_Predicate(Predicate<BlockPos> allowed) {
      this.init_Predicate = allowed;
   }

   public void init_V_BiPredicate(BiPredicate<BlockState, ItemStack> accepted) {
      this.init_RefQueue_DataUtil.init_RefQueue_DataUtil_BiPredicate(accepted);
   }

   public double init_D() {
      return this.get_D;
   }

   public RefQueue.Inner_TaskQueue load_RefQueue_TaskQueue() {
      if (this.init_RefQueue_TaskQueue != RefQueue.Inner_TaskQueue.load) {
         return this.init_RefQueue_TaskQueue;
      }

      if (this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
         if (!AutismLiteVariant.enabled()) {
            cG.init_V_Obj(this, this.init_List, this.tick_I, this::init_BlockState);
         }

         if (!AutismLiteVariant.enabled()) {
            this.prev_Z = false;
         }

         this.push_V();
         this.init_DataUtil.load_V();
         this.poll_V();
         if (!AutismLiteVariant.enabled()
            && this.prev_Z
            && this.init_Runnable != null
            && this.init_Minecraft.player.onGround()
            && !this.init_Minecraft.options.keyJump.isDown()) {
            this.init_Runnable.run();
         }

         return this.init_RefQueue_TaskQueue;
      } else {
         this.write_V();
         return this.init_RefQueue_TaskQueue;
      }
   }

   private void poll_V() {
      if ((this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.load || this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.read)
         && this.init_Minecraft.player != null
         && !this.init_Minecraft.player.isInWater()
         && (
            this.init_RefQueue_TaskQueue != RefQueue.Inner_TaskQueue.load
               || this.init_ClassCache_UiHelpe == null
               || this.init_ClassCache_UiHelpe.type() != ClassCache.Inner_SyncManager.start
               || !this.close_Z && !this.init_Minecraft.player.onGround()
         )) {
         LocalPlayer player = this.init_Minecraft.player;
         double forward = (this.init_Minecraft.options.keyUp.isDown() ? 1 : 0) - (this.init_Minecraft.options.keyDown.isDown() ? 1 : 0);
         double strafe = (this.init_Minecraft.options.keyLeft.isDown() ? 1 : 0) - (this.init_Minecraft.options.keyRight.isDown() ? 1 : 0);
         boolean jumping = player.onGround() && this.init_Minecraft.options.keyJump.isDown();
         if (forward != 0.0 || strafe != 0.0 || jumping || !player.onGround() || !(player.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4)) {
            NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(
               forward, strafe, jumping, player.isSprinting(), this.init_Minecraft.options.keyShift.isDown(), player.getYRot()
            );
            boolean descending = this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.load
               && this.init_ClassCache_UiHelpe != null
               && player.onGround()
               && !jumping
               && (
                  this.init_ClassCache_UiHelpe.type() == ClassCache.Inner_SyncManager.flush
                     || this.init_ClassCache_UiHelpe.type() == ClassCache.Inner_SyncManager.close
                     || this.init_ClassCache_UiHelpe.type() == ClassCache.Inner_SyncManager.open
                     || this.init_ClassCache_UiHelpe.type() == ClassCache.Inner_SyncManager.next
               );
            boolean safe = descending
               ? init_Z_NodeWalker_CachePo(
                  this.init_BitSetUtil(),
                  init_NodeWalker_StreamU_LocalPlayer(player),
                  input,
                  init_F_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe),
                  this.init_Set_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe)::contains
               )
               : init_Z_NodeWalker_CachePo_2(
                  this.init_BitSetUtil(),
                  init_NodeWalker_StreamU_LocalPlayer(player),
                  input,
                  this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.read ? 64 : (jumping ? 30 : 2)
               );
            if (!safe) {
               if (descending) {
                  NodeWalker.Inner_DataUtil brake = init_NodeWalker_DataUti_NodeWalker_CachePo_2(
                     this.init_BitSetUtil(),
                     init_NodeWalker_StreamU_LocalPlayer(player),
                     input,
                     init_F_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe),
                     this.init_Set_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe)::contains
                  );
                  if (brake != null) {
                     this.init_Minecraft.options.keyUp.setDown(brake.forward() > 0.0);
                     this.init_Minecraft.options.keyDown.setDown(brake.forward() < 0.0);
                     this.init_Minecraft.options.keyLeft.setDown(brake.strafe() > 0.0);
                     this.init_Minecraft.options.keyRight.setDown(brake.strafe() < 0.0);
                     this.init_Minecraft.options.keyJump.setDown(false);
                     this.init_Minecraft.options.keySprint.setDown(false);
                     this.init_Minecraft.options.keyShift.setDown(brake.sneak());
                     player.setSprinting(false);
                     return;
                  }
               }

               if (!descending && this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.load && player.onGround() && this.node < 20) {
                  NodeWalker.Inner_DataUtil centre = init_NodeWalker_DataUti_LocalPlayer(player);
                  if (centre != null && init_Z_NodeWalker_CachePo_2(this.init_BitSetUtil(), init_NodeWalker_StreamU_LocalPlayer(player), centre, 2)) {
                     this.node++;
                     this.init_Minecraft.options.keyUp.setDown(centre.forward() > 0.0);
                     this.init_Minecraft.options.keyDown.setDown(centre.forward() < 0.0);
                     this.init_Minecraft.options.keyLeft.setDown(centre.strafe() > 0.0);
                     this.init_Minecraft.options.keyRight.setDown(centre.strafe() < 0.0);
                     this.init_Minecraft.options.keyJump.setDown(false);
                     this.init_Minecraft.options.keySprint.setDown(false);
                     this.init_Minecraft.options.keyShift.setDown(true);
                     player.setSprinting(false);
                     return;
                  }
               }

               this.init_V_Str("live movement would touch lava" + this.init_Str_LocalPlayer_NodeWalker_DataUti(player, input));
               load_V();
            }
         }
      }
   }

   static NodeWalker.Inner_DataUtil init_NodeWalker_DataUti_NodeWalker_CachePo_2(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, NodeWalker.Inner_DataUtil input, float heading, LongPredicate allowed
   ) {
      double yaw = Math.toRadians(input.yaw());
      double forward = -Math.sin(yaw) * state.vx() + Math.cos(yaw) * state.vz();
      double strafe = Math.cos(yaw) * state.vx() + Math.sin(yaw) * state.vz();
      NodeWalker.Inner_DataUtil counter = new NodeWalker.Inner_DataUtil(
         forward > 0.005 ? -1.0 : (forward < -0.005 ? 1.0 : 0.0),
         strafe > 0.005 ? -1.0 : (strafe < -0.005 ? 1.0 : 0.0),
         false,
         false,
         input.sneak(),
         input.yaw()
      );

      for (NodeWalker.Inner_DataUtil candidate : new NodeWalker.Inner_DataUtil[]{
         new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, input.sneak(), input.yaw()), counter
      }) {
         if (init_Z_NodeWalker_CachePo(world, state, candidate, heading, allowed)) {
            return candidate;
         }
      }

      return null;
   }

   static boolean init_Z_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, NodeWalker.Inner_DataUtil input, float heading, LongPredicate allowed
   ) {
      for (int i = 0; i < 2; i++) {
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, input, world);
         if (NodeWalker.init_Z(world, state, next)) {
            return false;
         }

         state = next;
         if (!state.onGround()) {
            for (NodeWalker.Inner_DataUtil air : new NodeWalker.Inner_DataUtil[]{
               NodeWalker.Inner_DataUtil.run(heading, input.sprint()), NodeWalker.Inner_DataUtil.NONE, NodeWalker.Inner_DataUtil.run(heading + 180.0F, false)
            }) {
               l.Inner_StreamUtil landing = l.init(state, air, false, world);
               if (landing.down() && allowed.test(l.init_J_D_D(landing.at().x(), landing.at().z())) && l.init(landing.at(), allowed, world)) {
                  return true;
               }
            }

            return false;
         }
      }

      return true;
   }

   private String init_Str_LocalPlayer_NodeWalker_DataUti(LocalPlayer player, NodeWalker.Inner_DataUtil input) {
      NodeWalker.Inner_StreamUtil state = init_NodeWalker_StreamU_LocalPlayer(player);
      BitSetUtil world = this.init_BitSetUtil();

      for (int i = 0; i < 3; i++) {
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, i == 0 ? input : input.withJump(false), world);
         int[] cell = NodeWalker.init_IArr(world, state, next);
         if (cell != null) {
            BlockState block = this.init_Minecraft.level.getBlockState(new BlockPos(cell[0], cell[1], cell[2]));
            return " at "
               + cell[0]
               + ", "
               + cell[1]
               + ", "
               + cell[2]
               + " ("
               + BuiltInRegistries.BLOCK.getKey(block.getBlock()).getPath()
               + (cell[3] == 1 ? ", contact hazard" : "")
               + ")";
         }

         state = next;
      }

      return "";
   }

   private static NodeWalker.Inner_DataUtil init_NodeWalker_DataUti_LocalPlayer(LocalPlayer player) {
      double cx = Math.floor(player.getX()) + 0.5;
      double cz = Math.floor(player.getZ()) + 0.5;
      double dx = cx - player.getX();
      double dz = cz - player.getZ();
      if (dx * dx + dz * dz < 0.0064) {
         return null;
      }

      int[] keys = init_IArr_F_F(player.getYRot(), NodeWalker.init_F_D(player.getX(), player.getZ(), cx, cz));
      return new NodeWalker.Inner_DataUtil(keys[0], -keys[1], false, false, true, player.getYRot());
   }

   static boolean init_Z_NodeWalker_CachePo_2(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, NodeWalker.Inner_DataUtil input, int ticks) {
      boolean airborne = !state.onGround();
      int limit = airborne ? Math.max(ticks, 64) : ticks;

      for (int i = 0; i < limit; i++) {
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, i == 0 ? input : input.withJump(false), world);
         if (NodeWalker.init_Z(world, state, next)) {
            return false;
         }

         if (airborne && next.onGround() && load_Z_NodeWalker_CachePo_NodeWalker_StreamU(world, next)) {
            return true;
         }

         airborne |= !next.onGround();
         if (airborne) {
            limit = Math.max(limit, 64);
         }

         state = next;
      }

      return !airborne;
   }

   private static boolean load_Z_NodeWalker_CachePo_NodeWalker_StreamU(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state) {
      AABB body = state.box(false);
      AABB under = new AABB(body.minX, body.minY - 0.001, body.minZ, body.maxX, body.minY, body.maxZ);

      for (AABB box : world.init_List(under)) {
         if (box.intersects(under)) {
            return true;
         }
      }

      return false;
   }

   public static boolean init_Z_D_D(double forward, double strafe) {
      return init_Z_D_2(forward, strafe, false, false);
   }

   public static boolean init_Z_D_2(double forward, double strafe, boolean jump, boolean sprint) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         LocalPlayer player = client.player;
         return init_Z_NodeWalker_CachePo_2(
            new BitSetUtil(client.level, player),
            NodeWalker.Inner_StreamUtil.of(player),
            new NodeWalker.Inner_DataUtil(forward, strafe, jump, sprint, player.isShiftKeyDown(), player.getYRot()),
            jump ? 30 : 2
         );
      } else {
         return false;
      }
   }

   static boolean init_Z_NodeWalker_CachePo_NodeWalker_StreamU(NodeWalker.Inner_CachePool after, NodeWalker.Inner_StreamUtil state) {
      return init_Z_NodeWalker_CachePo(after, state, 0.0);
   }

   static boolean init_Z_NodeWalker_CachePo_2(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, NodeWalker.Inner_DataUtil run) {
      double floor = state.y();
      NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, run, world);
      if (NodeWalker.init_Z(world, state, next)) {
         return false;
      }

      for (int tick = 0; tick < 64; tick++) {
         if (!next.onGround() || Math.abs(next.y() - floor) > 0.01) {
            return false;
         }

         if (next.speed() < 0.01) {
            return true;
         }

         state = next;
         next = NodeWalker.init(state, NodeWalker.Inner_DataUtil.NONE, world);
         if (NodeWalker.init_Z(world, state, next)) {
            return false;
         }
      }

      return false;
   }

   static boolean init_Z_NodeWalker_CachePo(NodeWalker.Inner_CachePool after, NodeWalker.Inner_StreamUtil state, double alreadyFallen) {
      double fallTop = state.y() + Math.max(0.0, alreadyFallen);
      int settledTicks = 0;

      for (int i = 0; i < 64; i++) {
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, NodeWalker.Inner_DataUtil.NONE, after);
         if (NodeWalker.init_Z(after, state, next)) {
            return false;
         }

         if (next.onGround()) {
            if (fallTop - next.y() > 3.000001) {
               return false;
            }

            fallTop = next.y();
            if (++settledTicks >= 8 && next.speed() < 0.01) {
               return true;
            }
         } else {
            settledTicks = 0;
            fallTop = Math.max(fallTop, next.y());
         }

         state = next;
      }

      return false;
   }

   static boolean init_Z_NodeWalker_CachePo_3(NodeWalker.Inner_CachePool before, NodeWalker.Inner_StreamUtil state, List<AABB> boxes) {
      for (int i = 0; i < 64; i++) {
         AABB body = state.box(false);

         for (AABB box : boxes) {
            if (Math.abs(box.maxY - body.minY) <= 0.08 && box.maxX > body.minX && box.minX < body.maxX && box.maxZ > body.minZ && box.minZ < body.maxZ) {
               return true;
            }
         }

         if (state.onGround() && state.speed() < 0.01 && i > 0) {
            return false;
         }

         state = NodeWalker.init(state, NodeWalker.Inner_DataUtil.NONE, before);
      }

      return false;
   }

   public static boolean init_Z_BlockPos(BlockPos target) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         List<AABB> boxes = client.level
            .getBlockState(target)
            .getCollisionShape(client.level, target)
            .toAabbs()
            .stream()
            .map(box -> box.move(target.getX(), target.getY(), target.getZ()))
            .filter(box -> box.maxY <= client.player.getY() + 0.08)
            .toList();
         if (boxes.isEmpty()) {
            return true;
         }

         NodeWalker.Inner_StreamUtil state = init_NodeWalker_StreamU_LocalPlayer(client.player);
         if (!init_Z_NodeWalker_CachePo_3(new BitSetUtil(client.level, client.player), state, boxes)) {
            return true;
         }

         boolean safe = init_Z_NodeWalker_CachePo(
            new BitSetUtil(client.level, client.player, target), state, client.player.onGround() ? 0.0 : client.player.fallDistance
         );
         if (init_Z && !safe) {
            ListPool.init("unsafe support removal target=" + target + " state=" + state + " fallen=" + client.player.fallDistance);
         }

         return safe;
      } else {
         return false;
      }
   }

   public static boolean init_Z_Vec3_Vec3(Vec3 from, Vec3 to) {
      Minecraft client = Minecraft.getInstance();
      if (client.level == null) {
         return false;
      }

      BitSetUtil world = new BitSetUtil(client.level, client.player);
      int steps = Math.max(1, (int)Math.ceil(from.distanceTo(to) * 4.0));
      NodeWalker.Inner_StreamUtil previous = new NodeWalker.Inner_StreamUtil(from.x, from.y, from.z, 0.0, 0.0, 0.0, true, 0, false, false);

      for (int i = 1; i <= steps; i++) {
         Vec3 point = from.lerp(to, (double)i / steps);
         NodeWalker.Inner_StreamUtil next = new NodeWalker.Inner_StreamUtil(point.x, point.y, point.z, 0.0, 0.0, 0.0, true, 0, false, false);
         if (NodeWalker.init_Z(world, previous, next)) {
            return false;
         }

         previous = next;
      }

      return true;
   }

   public boolean load_Z() {
      if (this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
         LocalPlayer player = this.init_Minecraft.player;
         if (!player.isInWater() && (!player.onGround() || !(player.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4))) {
            if (init_Z_NodeWalker_CachePo_2(this.init_BitSetUtil(), init_NodeWalker_StreamU_LocalPlayer(player), NodeWalker.Inner_DataUtil.NONE, 64)) {
               return true;
            }

            load_V();
            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public static void load_V() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         LocalPlayer player = mc.player;
         open_V();
         player.setSprinting(false);
         mc.options.keyShift.setDown(true);
         double yaw = Math.toRadians(player.getYRot());
         Vec3 velocity = player.getDeltaMovement();
         double forward = -Math.sin(yaw) * velocity.x + Math.cos(yaw) * velocity.z;
         double strafe = Math.cos(yaw) * velocity.x + Math.sin(yaw) * velocity.z;
         mc.options.keyDown.setDown(forward > 0.005);
         mc.options.keyUp.setDown(forward < -0.005);
         mc.options.keyRight.setDown(strafe > 0.005);
         mc.options.keyLeft.setDown(strafe < -0.005);
      }
   }

   private void push_V() {
      this.load_ItemStackArr = null;
      LocalPlayer player = this.init_Minecraft.player;
      int chained = 0;
      if (!this.flush(player)) {
         while (this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.load) {
            if (this.tick_I >= this.init_List.size()) {
               if (this.load_BlockPos != null) {
                  this.run_V();
                  return;
               }

               this.init_RefQueue_TaskQueue = RefQueue.Inner_TaskQueue.read;
               this.update_V_Z(false);
               return;
            }

            if (this.init_ClassCache_UiHelpe == null) {
               this.init_ClassCache_UiHelpe = this.init_List.get(this.tick_I);
               this.init_V_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe);
               if (this.init_RefQueue_TaskQueue != RefQueue.Inner_TaskQueue.load) {
                  return;
               }
            }

            if (push(this.init_ClassCache_UiHelpe) && player.onGround()) {
               int reach = this.init_I_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe);
               int pulled = reach > this.tick_I ? this.init_I_LocalPlayer_I(player, reach) : -1;
               if (pulled > this.tick_I) {
                  double skipped = 0.0;

                  for (int k = this.tick_I; k < pulled; k++) {
                     skipped += this.init_List.get(k).costTicks();
                  }

                  this.get_D += skipped;
                  this.tick_I = pulled;
                  this.init_ClassCache_UiHelpe = this.init_List.get(this.tick_I);
                  this.init_V_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe);
                  if (this.init_RefQueue_TaskQueue != RefQueue.Inner_TaskQueue.load) {
                     return;
                  }

                  this.run_I = this.run_I - (int)Math.ceil(skipped * 3.0);
               }
            }

            if (this.init_ClassCache_UiHelpe.type() != ClassCache.Inner_SyncManager.run || this.init_NetHelper == null && !this.push_Z()) {
               if (this.sync(player)) {
                  this.put();
                  if (++chained >= 16) {
                     return;
                  }
               } else {
                  if (!this.close(this.init_ClassCache_UiHelpe)) {
                     if ((
                           read_Z_ClassCache_SyncMan(this.init_ClassCache_UiHelpe.type())
                              || this.init_ClassCache_UiHelpe.type() == ClassCache.Inner_SyncManager.tick
                        )
                        && player.onGround()
                        && !player.isInWater()
                        && init_Z_D(
                           player.getY(), this.init_D_BlockPos(this.init_ClassCache_UiHelpe.from()), this.init_D_BlockPos(this.init_ClassCache_UiHelpe.to())
                        )) {
                        if (init_Z) {
                           ListPool.init("off path: below jump takeoff " + this.init_ClassCache_UiHelpe.from() + " at " + player.position());
                        }

                        this.init_V_Str("below the takeoff");
                        return;
                     }

                     if (load_Z_ClassCache_UiHelpe_2(this.init_ClassCache_UiHelpe, player.getY(), player.onGround() && !player.isInWater())) {
                        if (init_Z) {
                           ListPool.init(
                              "off path: below " + this.init_ClassCache_UiHelpe.type() + " " + this.init_ClassCache_UiHelpe.to() + " at " + player.position()
                           );
                        }

                        this.init_V_Str("slid below a flat step");
                        return;
                     }

                     if (init_Z_ClassCache_UiHelpe(
                        this.init_ClassCache_UiHelpe, player.getY(), player.isUnderWater(), player.isUnderWater() && this.update_Z_LocalPlayer(player)
                     )) {
                        if (init_Z) {
                           ListPool.init(
                              "off path: under a ceiling below "
                                 + this.init_ClassCache_UiHelpe.type()
                                 + " "
                                 + this.init_ClassCache_UiHelpe.to()
                                 + " at "
                                 + player.position()
                           );
                        }

                        this.init_V_Str("under water beneath a ceiling, below the planned level");
                        return;
                     }

                     if (init_Z_ClassCache_UiHelpe_2(this.init_ClassCache_UiHelpe, player.getY(), player.isInWater())) {
                        if (init_Z) {
                           ListPool.init(
                              "off path: "
                                 + this.init_ClassCache_UiHelpe.type()
                                 + " fell into water short of "
                                 + this.init_ClassCache_UiHelpe.to()
                                 + " at "
                                 + player.position()
                           );
                        }

                        this.init_V_Str("fell short of the jump into water");
                        return;
                     }

                     if (read(this.init_ClassCache_UiHelpe, player.getY(), !player.isInWater() && player.onGround())) {
                        BlockPos under = BlockPos.containing(player.getX(), player.getY() - 0.01, player.getZ());
                        this.init_V_Str(
                           "above the planned walking level, on " + this.init_Minecraft.level.getBlockState(under) + " at " + under.toShortString()
                        );
                        return;
                     }

                     if (player.onGround()
                        && this.value_Z
                        && player.position()
                              .distanceToSqr(this.init_ClassCache_UiHelpe.from().getX() + 0.5, player.getY(), this.init_ClassCache_UiHelpe.from().getZ() + 0.5)
                           <= 0.25) {
                        this.value_Z = false;
                     }

                     if (!player.onGround()
                        || player.isInWater()
                        || this.update_Z_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe)
                        || this.init_ClassCache_UiHelpe.jump() != null && this.init_ClassCache_UiHelpe.jump().maneuver() != null
                        || (push(this.init_ClassCache_UiHelpe) || !init_Z_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe, player.getX(), player.getZ()))
                           && (
                              !this.value_Z && !load_Z_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe, player.getX(), player.getZ())
                                 || push(this.init_ClassCache_UiHelpe)
                                    && (
                                       this.init_Z_D(player.getX(), player.getZ(), this.init_ClassCache_UiHelpe.to(), new HashMap<>())
                                          || this.load_Vec3_LocalPlayer_ClassCache_UiHelpe(player, this.init_ClassCache_UiHelpe) != null
                                    )
                           )) {
                        boolean midAir = this.update_Z_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe);
                        if (!midAir) {
                           int bad = init_I_List(this.init_List, this.tick_I, 5, this.init_Function(), this.stop_Z);
                           if (bad < 0 && this.pop_Z() && !this.open(this.init_ClassCache_UiHelpe)) {
                              bad = this.tick_I;
                           }

                           if (bad < 0) {
                              bad = init_I_List_2(this.init_List, this.tick_I, 5, this.close(player), this.close_Z);
                           }

                           if (bad >= 0) {
                              this.load_Str = "a move ahead turned impossible";
                              this.init_V_BlockPos_ClassCache_UiHelpe(this.init_List.get(bad).to(), this.init_List.get(bad));
                              return;
                           }
                        }

                        if (this.init_AiOperation != null) {
                           this.set();
                           this.pop_V();
                           return;
                        }

                        if (this.init_UtilCore != null && this.init_UtilCore.init_Z()) {
                           this.set();
                           this.tick_V();
                           return;
                        }

                        if (this.next_Z && !midAir && player.onGround() && !this.init_Z_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe)) {
                           this.init_DataUtil.init_V_DataUtil_DataUtil(DataUtil.Inner_DataUtil.write);
                           if (!this.init_ClassCache_UiHelpe.cellsToBreak().isEmpty() && this.init_Z_LocalPlayer_3(player, this.init_ClassCache_UiHelpe, false)
                              )
                            {
                              return;
                           }

                           this.set();
                           return;
                        }

                        boolean softSettling = this.init_ClassCache_UiHelpe.type() == ClassCache.Inner_SyncManager.poll
                           && !player.onGround()
                           && !player.isInWater()
                           && !player.onClimbable();
                        boolean rimWork = this.init_ClassCache_UiHelpe.type() == ClassCache.Inner_SyncManager.run
                           && !this.update_Z
                           && (this.sync_Z || this.init_BlockPos != null)
                           && this.run_I < ClassCache.Inner_CachePool.init_I_D(this.init_ClassCache_UiHelpe.costTicks()) + 80;
                        boolean counted = !player.isInWater() || ++this.put_I % 3 == 0;
                        if (rimWork) {
                           this.run_I++;
                        } else if (!softSettling
                           && counted
                           && ++this.run_I > ClassCache.Inner_CachePool.init_I_D(this.init_ClassCache_UiHelpe.costTicks())
                           && !midAir) {
                           this.init_V_Str("over its time budget" + this.init_Str_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe));
                           return;
                        }

                        this.init_V_LocalPlayer_ClassCache_UiHelpe(player, this.init_ClassCache_UiHelpe);
                        PacketUtil.init(
                           this.init_ClassCache_UiHelpe.type(),
                           this.init_NetHelper == null || this.init_NetHelper.init_JsonUtil().init_JsonUtil_ConfigSto() == JsonUtil.Inner_ConfigStore.init,
                           this.init_Z_LocalPlayer_ClassCache_UiHelpe(player, this.init_ClassCache_UiHelpe)
                        );
                        this.run_V();
                        return;
                     }

                     if (this.init_ClassCache_UiHelpe.jump() != null
                        && !init_Z_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe, player.getX(), player.getZ())
                        && this.update_Z_LocalPlayer_ClassCache_UiHelpe(player, this.init_ClassCache_UiHelpe)) {
                        return;
                     }

                     AutismClientAddon.LOG
                        .info(
                           "[AI] off path: past {} {} -> {} at {}; replanning from here",
                           this.init_ClassCache_UiHelpe.type(),
                           this.init_ClassCache_UiHelpe.from().toShortString(),
                           this.init_ClassCache_UiHelpe.to().toShortString(),
                           player.blockPosition().toShortString()
                        );
                     this.init_V_Str("past its target");
                     return;
                  }

                  if (this.init_ClassCache_UiHelpe.type() == ClassCache.Inner_SyncManager.set && !this.flush(this.init_ClassCache_UiHelpe)) {
                     if (++this.run_I > ClassCache.Inner_CachePool.init_I_D(this.init_ClassCache_UiHelpe.costTicks())) {
                        this.init_V_Str("over its time budget" + this.init_Str_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe));
                     }

                     return;
                  }

                  this.get_D = this.get_D + this.init_ClassCache_UiHelpe.costTicks();
                  this.tick_I++;
                  this.init_ClassCache_UiHelpe = null;
                  this.put();
                  if (++chained >= 16) {
                     return;
                  }
               }
            } else {
               if (this.write_Z_LocalPlayer(player)) {
                  return;
               }

               this.put();
               if (++chained >= 16) {
                  return;
               }
            }
         }
      }
   }

   private boolean init_Z_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper m) {
      if (player != null
         && player.onGround()
         && !player.isInWater()
         && !player.onClimbable()
         && !this.init_Minecraft.options.keyShift.isDown()
         && !this.init_Minecraft.options.keyJump.isDown()
         && this.tick_I + 1 < this.init_List.size()
         && m.cellsToBreak().isEmpty()
         && m.cellsToPlace().isEmpty()) {
         switch (m.type()) {
            case init:
            case load:
            case read:
            case write:
            case reset:
            case update:
            case sync:
               ClassCache.UiHelper next = this.init_List.get(this.tick_I + 1);
               if (!next.from().equals(m.to())) {
                  return false;
               } else {
                  boolean nextJump = read_Z_ClassCache_SyncMan(next.type());
                  boolean nextDescend = next.type() == ClassCache.Inner_SyncManager.flush
                     || next.type() == ClassCache.Inner_SyncManager.close
                     || next.type() == ClassCache.Inner_SyncManager.open
                     || next.type() == ClassCache.Inner_SyncManager.poll;
                  if (!nextJump && !nextDescend) {
                     return false;
                  } else {
                     double surf = this.init_D_ClassCache_UiHelpe(m);
                     if (Math.abs(player.getY() - surf) > 0.05) {
                        return false;
                     } else {
                        double tx = m.to().getX() + 0.5;
                        double tz = m.to().getZ() + 0.5;
                        if (Math.hypot(player.getX() - tx, player.getZ() - tz) > 1.35) {
                           return false;
                        } else {
                           int mdx = m.to().getX() - m.from().getX();
                           int mdz = m.to().getZ() - m.from().getZ();
                           int ndx = next.to().getX() - next.from().getX();
                           int ndz = next.to().getZ() - next.from().getZ();
                           if (mdx * ndx + mdz * ndz < 0) {
                              return false;
                           } else {
                              double forward = (this.init_Minecraft.options.keyUp.isDown() ? 1 : 0) - (this.init_Minecraft.options.keyDown.isDown() ? 1 : 0);
                              double strafe = (this.init_Minecraft.options.keyLeft.isDown() ? 1 : 0) - (this.init_Minecraft.options.keyRight.isDown() ? 1 : 0);
                              if (forward == 0.0 && strafe == 0.0) {
                                 return false;
                              } else {
                                 BitSetUtil world = this.init_BitSetUtil();
                                 NodeWalker.Inner_StreamUtil state = init_NodeWalker_StreamU_LocalPlayer(player);
                                 NodeWalker.Inner_DataUtil stepIn = new NodeWalker.Inner_DataUtil(
                                    forward, strafe, false, player.isSprinting() && forward > 0.0, false, this.init_DataUtil.init_F()
                                 );
                                 NodeWalker.Inner_StreamUtil one = NodeWalker.init(state, stepIn, world);
                                 if (one.onGround() && !(Math.abs(one.y() - surf) > 0.02) && !NodeWalker.init_Z(world, state, one)) {
                                    if (!nextDescend && !init_Z_NodeWalker_CachePo_2(world, state, stepIn)) {
                                       float jumpYaw = NodeWalker.init_F_D(one.x(), one.z(), next.to().getX() + 0.5, next.to().getZ() + 0.5);
                                       boolean jumpSprint = (next.jump() == null || next.jump().sprint()) && this.close(player);
                                       return TableIndex.init_TableIndex_UiHelpe_NodeWalker_CachePo(
                                                world, one, jumpYaw, jumpSprint, next.to(), this.init_D_ClassCache_UiHelpe(next)
                                             )
                                             .margin()
                                          >= -0.12;
                                    }

                                    return true;
                                 }

                                 return false;
                              }
                           }
                        }
                     }
                  }
               }
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public void read_V() {
      this.init_RefQueue_CachePool.init();
   }

   public void write_V_Z(boolean hold) {
      this.init_V_Z_Predicate(hold, null);
   }

   public void init_V_Z_Predicate(boolean hold, Predicate<BlockPos> keeps) {
      this.next_Z = hold;
      this.load_Predicate = hold ? keeps : null;
   }

   private boolean init_Z_ClassCache_UiHelpe(ClassCache.UiHelper move) {
      return this.load_Predicate != null
         && move.cellsToBreak().isEmpty()
         && move.cellsToPlace().isEmpty()
         && move.jump() == null
         && this.load_Predicate.test(move.to());
   }

   public void init_V_Function(Function<BlockPos, Vec3> aim) {
      this.init_Function = aim;
   }

   public void init_V_Function_Runnable(Function<Predicate<AABB>, Vec3> look, Runnable click) {
      if (!AutismLiteVariant.enabled()) {
         this.load_Function = look;
         this.init_Runnable = click;
      }
   }

   static boolean init_Z_AABB(AABB placed, List<ClassCache.UiHelper> route, int at, AABB body, Vec3 velocity) {
      if (placed.intersects(body.expandTowards(velocity.scale(3.0)).inflate(0.15))) {
         return false;
      }

      for (int i = at; i < route.size(); i++) {
         ClassCache.UiHelper movement = route.get(i);
         BlockPos from = movement.from();
         BlockPos to = movement.to();
         AABB sweep = new AABB(
            Math.min(from.getX(), to.getX()) + 0.19,
            Math.min(from.getY(), to.getY()) - 1,
            Math.min(from.getZ(), to.getZ()) + 0.19,
            Math.max(from.getX(), to.getX()) + 0.81,
            Math.max(from.getY(), to.getY()) + 1.8 + 1.4,
            Math.max(from.getZ(), to.getZ()) + 0.81
         );
         if (placed.intersects(sweep)) {
            return false;
         }
      }

      return true;
   }

   public void init_V_I(int blocks) {
      this.task = Math.max(0, blocks);
   }

   public void write_V() {
      if (!AutismLiteVariant.enabled()) {
         cG.init_V(this);
      }

      if (this.init_RefQueue_TaskQueue != RefQueue.Inner_TaskQueue.update) {
         this.init_RefQueue_TaskQueue = RefQueue.Inner_TaskQueue.update;
         this.add();
      }
   }

   public boolean read_Z() {
      if (!this.update_Z()) {
         return false;
      }

      if (this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.update) {
         return true;
      }

      this.update_V_Z(false);
      this.init_DataUtil.init_V();
      this.init_List = List.of();
      this.tick_I = 0;
      this.init_ClassCache_UiHelpe = null;
      this.read_BlockPos = null;
      this.load_ClassCache_UiHelpe = null;
      this.init_RefQueue_TaskQueue = RefQueue.Inner_TaskQueue.init;
      return true;
   }

   private void pop_V() {
      AiOperation.Result result = this.init_AiOperation.tick();
      if (result.terminal()) {
         if (init_Z) {
            ListPool.init("plank craft " + result.status() + ": " + result.message());
         }

         if (result.status() == AiOperation.Status.SUCCEEDED) {
            this.clear_Z = true;
         } else {
            this.add_Z = true;
         }

         this.init_AiOperation = null;
      }
   }

   private void tick_V() {
      switch (this.init_UtilCore.load_UtilCore_BufferHel()) {
         case read:
            this.state_I++;
            this.init_UtilCore.init_V();
            break;
         case write:
            this.put_Z = true;
            this.init_UtilCore.init_V();
      }
   }

   public BlockPos read_BlockPos() {
      return !this.init_List.isEmpty() && this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.load
         ? this.init_List.get(this.init_List.size() - 1).to()
         : null;
   }

   public void update_V() {
      if (this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.write) {
         this.init_RefQueue_TaskQueue = RefQueue.Inner_TaskQueue.init;
         this.read_BlockPos = null;
         this.load_ClassCache_UiHelpe = null;
         this.init_List = List.of();
         this.init_ClassCache_UiHelpe = null;
      }
   }

   @Override
   public boolean write_Z() {
      return this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.load;
   }

   public Set<BlockPos> init_Set_I(int moves) {
      if (this.init_RefQueue_TaskQueue != RefQueue.Inner_TaskQueue.load) {
         return Set.of();
      }

      Set<BlockPos> out = new HashSet<>();

      for (int i = this.tick_I; i < Math.min(this.init_List.size(), this.tick_I + moves); i++) {
         for (BlockPos c : this.init_List.get(i).cellsToBreak()) {
            if (!init_Z_ClassCache_FileCac(this.load_ClassCache_FileCac_BlockPos(c))) {
               out.add(c);
            }
         }
      }

      return out;
   }

   public boolean update_Z() {
      if (this.init_Minecraft.player != null && !flush_Z()) {
         return false;
      } else if (this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.load && this.init_ClassCache_UiHelpe != null) {
         LocalPlayer p = this.init_Minecraft.player;
         boolean steady = p == null || flush_Z();
         boolean landed = p != null
            && p.onGround()
            && read_Z_ClassCache_SyncMan(this.init_ClassCache_UiHelpe.type())
            && this.write_BlockPos().equals(this.init_ClassCache_UiHelpe.to());
         return steady && (this.pop_Z() || landed) && !this.update_Z_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe);
      } else {
         return true;
      }
   }

   public boolean sync_Z() {
      return this.init_BlockPos != null && (this.init_Minecraft.level == null || !this.close(this.init_BlockPos))
         || this.init_UtilCore != null && this.init_UtilCore.init_Z()
         || this.init_AiOperation != null
         || this.valid_I > 0;
   }

   public static boolean flush_Z() {
      Minecraft client = Minecraft.getInstance();
      LocalPlayer player = client.player;
      return player != null
         && (
            player.isInWater()
               || player.onClimbable()
               || player.onGround()
                  && client.level != null
                  && init_Z_AABB_Predicate(player.getBoundingBox(), sole -> client.level.getBlockCollisions(player, sole).iterator().hasNext())
         );
   }

   static boolean init_Z_AABB_Predicate(AABB body, Predicate<AABB> blockCollision) {
      return blockCollision.test(new AABB(body.minX, body.minY - 1.0E-6, body.minZ, body.maxX, body.minY, body.maxZ));
   }

   public boolean close_Z() {
      return this.load_BlockPos != null;
   }

   @Override
   public int flush_I() {
      int count = 0;

      for (int i = this.tick_I; i < this.init_List.size(); i++) {
         if (i != this.tick_I || !this.update_Z) {
            count += this.init_List.get(i).cellsToPlace().size();
         }
      }

      return count;
   }

   @Override
   public List<ClassCache.UiHelper> init_List() {
      return this.init_RefQueue_TaskQueue == RefQueue.Inner_TaskQueue.load
         ? List.copyOf(this.init_List.subList(this.tick_I, this.init_List.size()))
         : List.of();
   }

   @Override
   public int close_I() {
      double sum = 0.0;

      for (int i = this.tick_I + (this.init_ClassCache_UiHelpe == null ? 0 : 1); i < this.init_List.size(); i++) {
         sum += this.init_List.get(i).costTicks();
      }

      return (int)Math.ceil(sum);
   }

   @Override
   public BlockPos write_BlockPos() {
      return i.init(this.init_Minecraft);
   }

   static boolean init_Z_ClassCache_UiHelpe_Function(ClassCache.UiHelper m, Function<BlockPos, ClassCache.Inner_FileCache> probe) {
      if (m.plannedBreakTicks() < 0.0) {
         return false;
      }

      double live = 0.0;

      for (BlockPos cell : m.cellsToBreak()) {
         ClassCache.Inner_FileCache p = probe.apply(cell);
         if (!p.passable() && p.diggable()) {
            live += p.breakTicks();
         }
      }

      return live - m.plannedBreakTicks() > 10.0;
   }

   private void init_V_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      ClassCache.UiHelper was = this.read_ClassCache_UiHelpe;
      this.read_ClassCache_UiHelpe = null;
      boolean resumed = was != null && was.type() == m.type() && was.from().equals(m.from()) && was.to().equals(m.to());
      if (!resumed && this.init_RefQueue_CachePool.init(m, System.nanoTime())) {
         AutismClientAddon.LOG
            .info(
               "[AI] LOOP: {} {} -> {} entered {} times in {} s at {}",
               m.type(),
               m.from().toShortString(),
               m.to().toShortString(),
               3,
               45L,
               this.init_Minecraft.player == null ? "-" : this.init_Minecraft.player.blockPosition().toShortString()
            );
         if (this.init_Minecraft.level != null && this.init_Minecraft.player != null) {
            AutismClientAddon.LOG.info("[AI] {}", n.init(this.init_Minecraft.level, this.init_Minecraft.player.blockPosition(), m.to(), 6, 4));
         }

         this.init_RefQueue_CachePool.init();
         this.reset_Z = true;
         this.load_Str = "loop";
         this.init_V_BlockPos_ClassCache_UiHelpe(m.to(), m);
      } else {
         this.init_DataUtil
            .init_V_DataUtil_DataUtil(
               m.cellsToBreak().isEmpty() && m.cellsToPlace().isEmpty() ? DataUtil.Inner_DataUtil.load_DataUtil_DataUtil : DataUtil.Inner_DataUtil.write
            );
         this.run_I = 0;
         this.get_I = 0;
         this.sync_Z = false;
         this.set_I = 0;
         this.write_Z = false;
         this.init_BlockPos = null;
         this.read_J = 0L;
         this.init_Block = null;
         this.update_Z = false;
         this.flush_Z = false;
         this.add_I = 0;
         this.close_Z = false;
         this.open_Z = false;
         this.poll_Z = false;
         this.close_F = 0.0F / 0.0F;
         this.stop_I = 2147483647;
         this.next_I = 0;
         this.flags = 0;
         this.start_I = 0;
         this.push_Z = false;
         this.value_Z = false;
         this.init_RefQueue_StreamUti = null;
         this.init_TableIndex_StateCa = null;
         this.init_PoolEntry = null;
         this.prev_I = 0;
         this.pop_Z = false;
         this.tick_Z = false;
         this.run_Z = false;
         this.put_Z = false;
         this.add_Z = false;
         this.valid_Z = false;
         this.size_Z = false;
         this.pool = 0;
         this.index = 0;
         this.data_Z = false;
         this.node = 0;
         this.valid_I = 0;
         this.size_I = 0;
         this.init_RefQueue_DataUtil.init_V();
         this.write_BlockPos = null;
         if (m.type() != ClassCache.Inner_SyncManager.push
            && m.type() != ClassCache.Inner_SyncManager.next
            && m.type() != ClassCache.Inner_SyncManager.pop
            && m.type() != ClassCache.Inner_SyncManager.stop) {
            this.cache_Z = false;
         }

         if (init_Z) {
            ListPool.init("move enter type=" + m.type() + " from=" + m.from() + " to=" + m.to() + " cost=" + m.costTicks());
         }

         boolean openDrop = m.type() == ClassCache.Inner_SyncManager.push
            && m.cellsToBreak().stream().allMatch(cell -> this.load_ClassCache_FileCac_BlockPos(cell).passable());
         Vec3 line = this.sync(m) ? this.init_Vec3_ClassCache_UiHelpe(m) : null;
         this.state_Z = line != null;
         if (!openDrop) {
            this.init_V_Vec3(
               line != null
                  ? line
                  : (
                     this.init_Minecraft.player == null
                        ? this.load_Vec3_ClassCache_UiHelpe(m)
                        : this.init_Vec3_LocalPlayer_2(this.init_Minecraft.player, m, this.load_Vec3_ClassCache_UiHelpe(m))
                  )
            );
         }

         this.state_Z = false;
         this.get_Z = !openDrop && this.init_DataUtil.init_D() > 30.0;
         if (!this.open(m) || init_Z_ClassCache_UiHelpe_Function(m, this::init_ClassCache_FileCac_BlockPos)) {
            this.init_V_Str("no longer valid against the live world");
         }
      }
   }

   private void init_V_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper m) {
      if (TaskTimer.init_Z()) {
         TaskTimer.init_V(m.type() + " " + m.to().toShortString() + (player.onGround() ? "" : " airborne"));
      }

      this.init_DataUtil
         .init_V_DataUtil_DataUtil(
            init_Z_ClassCache_SyncMan(m.type())
               ? DataUtil.Inner_DataUtil.update
               : (
                  !m.cellsToPlace().isEmpty()
                     ? DataUtil.Inner_DataUtil.read_DataUtil_DataUtil
                     : (!m.cellsToBreak().isEmpty() ? DataUtil.Inner_DataUtil.write : DataUtil.Inner_DataUtil.load_DataUtil_DataUtil)
               )
         );
      if (!this.sync(m) && init_Z_ClassCache_SyncMan(m.type(), this.close_Z, this.get_Z, this.init_DataUtil.init_D(), this.init_DataUtil.init_Z())) {
         this.set();
      } else {
         switch (m.type()) {
            case init:
            case load:
            case update:
               this.init_V_LocalPlayer_3(player, m, false, false);
               break;
            case read:
            case write:
               boolean sprint = init_Z_ClassCache_SyncMan_Z(m.type(), this.close(player));
               this.init_V_LocalPlayer_3(player, m, sprint, false);
               break;
            case reset:
               this.load_V_LocalPlayer_ClassCache_UiHelpe(player, m);
               break;
            case sync:
               this.update_V_LocalPlayer_ClassCache_UiHelpe(player, m);
               break;
            case get:
               this.sync_V(player, m);
               break;
            case put:
            case add:
            case clear:
            case start:
               this.flush_V(player, m);
               break;
            case flush:
               this.init_V_LocalPlayer_3(
                  player,
                  m,
                  init_Z_ClassCache_SyncMan_Z(m.type(), this.close(player))
                     && (this.tick_I < this.init_List.size() - 1 && this.load_Z_ClassCache_UiHelpe(m) || this.load_Z_LocalPlayer_ClassCache_UiHelpe(player, m))
               );
               break;
            case close:
            case open:
               this.init_V_LocalPlayer_3(player, m, this.close(player) && this.load_Z_LocalPlayer_ClassCache_UiHelpe(player, m));
               break;
            case poll:
               boolean waterDrop = this.load_ClassCache_FileCac_BlockPos(m.to()).water();
               if (!player.onGround() && !player.isInWater() && !player.onClimbable() && waterDrop) {
                  this.read_V_LocalPlayer_ClassCache_UiHelpe(player, m);
               } else {
                  if (waterDrop
                     && player.onGround()
                     && !player.isInWater()
                     && !this.init_Z_NodeWalker_StreamU_ClassCache_UiHelpe(init_NodeWalker_StreamU_LocalPlayer(player), m)) {
                     this.write_V_LocalPlayer_ClassCache_UiHelpe(player, m);
                     break;
                  }

                  this.init_V_LocalPlayer_3(player, m, false, false);
               }
               break;
            case push:
               this.close(player, m);
               break;
            case pop:
               this.open(player, m);
               break;
            case stop:
               this.push(player, m);
               break;
            case next:
               this.poll(player, m);
               break;
            case tick:
               this.pop(player, m);
               break;
            case run:
               this.tick(player, m);
               break;
            case set:
               this.run(player, m);
         }

         if (load_Z) {
            ListPool.init(
               String.format(
                     Locale.ROOT,
                     "tick %s t%d to=%s pos=(%.2f,%.2f,%.2f) v=(%.3f,%.3f) yaw=%.1f pitch=%.1f goal=%.1f keys %s%s%s%s%s%s%s ground=%s",
                     m.type(),
                     this.run_I,
                     m.to().toShortString(),
                     player.getX(),
                     player.getY(),
                     player.getZ(),
                     player.getDeltaMovement().x,
                     player.getDeltaMovement().z,
                     player.getYRot(),
                     player.getXRot(),
                     this.init_DataUtil.init_F(),
                     this.init_Minecraft.options.keyUp.isDown() ? "W" : "",
                     this.init_Minecraft.options.keyLeft.isDown() ? "A" : "",
                     this.init_Minecraft.options.keyDown.isDown() ? "S" : "",
                     this.init_Minecraft.options.keyRight.isDown() ? "D" : "",
                     this.init_Minecraft.options.keyJump.isDown() ? "J" : "",
                     this.init_Minecraft.options.keyShift.isDown() ? "C" : "",
                     player.isSprinting() ? "R" : "",
                     player.onGround()
                  )
                  + (
                     this.init_Minecraft.gameMode instanceof AutismMultiPlayerGameModeAccessor dig
                        ? String.format(
                           Locale.ROOT, " dig gap=%d on=%s p=%.2f", dig.autism$getDestroyDelay(), dig.autism$isDestroying(), dig.autism$getDestroyProgress()
                        )
                        : ""
                  )
            );
         }
      }
   }

   private void load_V_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper m) {
      if (this.load_Vec3_LocalPlayer_ClassCache_UiHelpe(player, m) != null) {
         this.init_V_LocalPlayer_3(player, m, this.close(player), false);
      } else {
         Vec3 gaze = this.load_Vec3_ClassCache_UiHelpe(m);
         BlockPos door = this.update_BlockPos();
         if (door != null) {
            this.init_V_Vec3(this.init_Vec3_BlockPos(door));
         } else {
            this.init_V_LocalPlayer_Vec3(player, this.init_Vec3_LocalPlayer_2(player, m, gaze));
         }

         if (this.write_Z_LocalPlayer_ClassCache_UiHelpe(player, m)) {
            this.set();
         } else {
            float want = this.init_F_LocalPlayer(player, m, gaze, false);
            if (!this.init_DataUtil.init_Z() && !init_Z_F_F(this.init_DataUtil.init_F(), want)) {
               this.init_V_LocalPlayer_Z(player, this.init_Z_F(want) && init_Z_ClassCache_SyncMan_Z(m.type(), this.close(player)));
               this.init_Minecraft.options.keyJump.setDown(false);
               this.init_Minecraft.options.keyShift.setDown(false);
            } else {
               if (this.init_DataUtil.init_Z()) {
                  this.init_Minecraft.options.keyDown.setDown(false);
                  this.init_Minecraft.options.keyUp.setDown(true);
                  this.load_V_LocalPlayer(player, m, this.init_DataUtil.init_F());
               } else {
                  this.init_Z_F(want);
                  this.init_V_LocalPlayer(player, m, this.init_DataUtil.init_F());
               }

               this.init_V_LocalPlayer_Z(player, init_Z_ClassCache_SyncMan_Z(m.type(), this.close(player)));
               this.init_Minecraft
                  .options
                  .keyJump
                  .setDown(
                     init_Z_Z(player.onGround())
                        && (init_Z_List(this.init_List, this.tick_I, player.getX(), player.getZ()) || this.read_Z_LocalPlayer_ClassCache_UiHelpe(player, m))
                  );
               this.init_Minecraft.options.keyShift.setDown(false);
               if (door != null) {
                  this.init_V_BlockPos(door);
               }
            }
         }
      }
   }

   static boolean init_Z_Z(boolean onGround) {
      return onGround;
   }

   private void init_V_LocalPlayer_3(LocalPlayer player, ClassCache.UiHelper m, boolean sprint) {
      if (player.onClimbable() && !player.onGround() && init_Z_ClassCache_UiHelpe_5(m, player.blockPosition(), player.getY())) {
         this.init_DataUtil.init_V();
         this.set();
         this.init_V_LocalPlayer_Z(player, false);
      } else if (!player.onGround() && !player.isInWater() && !player.onClimbable()) {
         if (init_Z && !this.valid_Z) {
            this.valid_Z = true;
            ListPool.init("descend airborne to=" + m.to() + " position=" + player.position() + " motion=" + player.getDeltaMovement());
         }

         this.init_V_LocalPlayer_Vec3(player, this.init_Vec3_LocalPlayer_2(player, m, this.load_Vec3_ClassCache_UiHelpe(m)));
         float heading = init_F_ClassCache_UiHelpe(m);
         l.Inner_DataUtil air = this.init_l_DataUtil_LocalPlayer(player, m, heading);
         this.init_Minecraft.options.keyJump.setDown(false);
         this.init_Minecraft.options.keyShift.setDown(false);
         switch (air) {
            case init:
               this.init_Z_F(heading);
               break;
            case read:
               this.init_Z_F(heading + 180.0F);
               this.init_V_LocalPlayer_Z(player, false);
               break;
            case load:
               this.set();
               this.init_V_LocalPlayer_Z(player, false);
         }
      } else {
         this.init_V_LocalPlayer_3(player, m, sprint, false);
      }
   }

   static boolean init_Z_ClassCache_UiHelpe_5(ClassCache.UiHelper m, BlockPos feet, double y) {
      return m.to().getY() < m.from().getY() && y > m.to().getY() + 0.6 && feet.getX() == m.to().getX() && feet.getZ() == m.to().getZ();
   }

   private void read_V_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper m) {
      this.init_V_LocalPlayer_Vec3(player, this.init_Vec3_LocalPlayer_2(player, m, this.load_Vec3_ClassCache_UiHelpe(m)));
      double tx = m.to().getX() + 0.5;
      double tz = m.to().getZ() + 0.5;
      float heading = NodeWalker.init_F_D(player.getX(), player.getZ(), tx, tz);
      NodeWalker.Inner_StreamUtil state = init_NodeWalker_StreamU_LocalPlayer(player);
      BitSetUtil world = this.init_BitSetUtil();
      NodeWalker.Inner_DataUtil[] choices = new NodeWalker.Inner_DataUtil[]{
         this.init_NodeWalker_DataUti_F_Z(heading, false),
         new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, false, this.init_DataUtil.init_F()),
         this.init_NodeWalker_DataUti_F_Z(heading + 180.0F, false)
      };
      int best = -1;
      int nearest = 1;
      double bestDistance = 1.7976931348623157E308;
      double nearestDistance = 1.7976931348623157E308;

      for (int i = 0; i < choices.length; i++) {
         l.Inner_StreamUtil landing = l.init(state, choices[i], false, world);
         if (landing.down()) {
            double distance = Math.hypot(landing.at().x() - tx, landing.at().z() - tz);
            if (distance < nearestDistance) {
               nearestDistance = distance;
               nearest = i;
            }

            if (!(landing.at().y() > m.to().getY() + 0.5)
               && this.load_ClassCache_FileCac_BlockPos(BlockPos.containing(landing.at().x(), m.to().getY(), landing.at().z())).water()
               && distance < bestDistance) {
               bestDistance = distance;
               best = i;
            }
         }
      }

      this.init_Minecraft.options.keyJump.setDown(false);
      this.init_Minecraft.options.keyShift.setDown(false);
      this.init_V_LocalPlayer_Z(player, false);
      switch (best >= 0 ? best : nearest) {
         case 0:
            this.init_Z_F(heading);
            break;
         case 2:
            this.init_Z_F(heading + 180.0F);
            break;
         default:
            this.set();
      }
   }

   private boolean init_Z_NodeWalker_StreamU_ClassCache_UiHelpe(NodeWalker.Inner_StreamUtil s, ClassCache.UiHelper m) {
      BitSetUtil world = this.init_BitSetUtil();
      double tx = m.to().getX() + 0.5;
      double tz = m.to().getZ() + 0.5;

      for (int policy = 0; policy < 3; policy++) {
         NodeWalker.Inner_StreamUtil cur = s;
         boolean airborne = false;
         boolean landed = false;
         boolean tight = false;
         boolean entered = false;
         ArrayList<NodeWalker.Inner_StreamUtil> flight = new ArrayList<>();

         for (int t = 0; t < 60 && !landed; t++) {
            float heading = NodeWalker.init_F_D(cur.x(), cur.z(), tx, tz);
            NodeWalker.Inner_DataUtil keys = airborne && policy != 0
               ? (
                  policy == 1
                     ? new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, false, heading)
                     : new NodeWalker.Inner_DataUtil(-1.0, 0.0, false, false, false, heading)
               )
               : new NodeWalker.Inner_DataUtil(1.0, 0.0, false, false, false, heading);
            NodeWalker.Inner_StreamUtil next = NodeWalker.init(cur, keys, world);
            if (NodeWalker.init_Z(world, cur, next)) {
               break;
            }

            cur = next;
            if (!cur.onGround()) {
               airborne = true;
            } else if (airborne) {
               landed = true;
            } else if (t > 20) {
               break;
            }

            if (airborne) {
               flight.add(cur);
            }

            BlockPos feet = BlockPos.containing(cur.x(), cur.y() + 0.01, cur.z());
            if (airborne && !entered && this.load_ClassCache_FileCac_BlockPos(feet).water()) {
               entered = true;
               tight = !ClassCache.Inner_RenderState.init_Z_NodeWalker_CachePo(world, cur.x(), cur.z(), feet)
                  || !ClassCache.Inner_RenderState.init_Z_NodeWalker_CachePo(world, flight, feet.getY());
            }
         }

         if (landed
            && !tight
            && !(cur.y() > m.to().getY() + 0.5)
            && this.load_ClassCache_FileCac_BlockPos(BlockPos.containing(cur.x(), m.to().getY(), cur.z())).water()) {
            return true;
         }
      }

      return false;
   }

   private void write_V_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper m) {
      double cx = m.from().getX() + 0.5;
      double cz = m.from().getZ() + 0.5;
      double dx = cx - player.getX();
      double dz = cz - player.getZ();
      NodeWalker.Inner_StreamUtil s = init_NodeWalker_StreamU_LocalPlayer(player);
      if (Math.hypot(dx, dz) > 1.5) {
         this.init_V_LocalPlayer_3(player, m, false, false);
      } else if (Math.hypot(dx, dz) <= 0.1 && Math.hypot(s.vx(), s.vz()) < 0.03) {
         this.set();
         this.init_V_Str("no way into the water from the drop's takeoff");
      } else {
         float yaw = player.getYRot();
         double r = Math.toRadians(yaw);
         double forward = dx * -Math.sin(r) + dz * Math.cos(r);
         double right = dx * -Math.cos(r) + dz * -Math.sin(r);
         this.init_Minecraft.options.keyUp.setDown(forward > 0.06);
         this.init_Minecraft.options.keyDown.setDown(forward < -0.06);
         this.init_Minecraft.options.keyRight.setDown(right > 0.06);
         this.init_Minecraft.options.keyLeft.setDown(right < -0.06);
         this.init_Minecraft.options.keyJump.setDown(false);
         this.init_Minecraft.options.keyShift.setDown(true);
         this.init_V_LocalPlayer_Z(player, false);
      }
   }

   private static float init_F_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      return NodeWalker.init_F_D(0.0, 0.0, m.to().getX() - m.from().getX(), m.to().getZ() - m.from().getZ());
   }

   private Set<Long> init_Set_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      Set<Long> cells = new HashSet<>();
      cells.add(l.init_J_I_I(m.to().getX(), m.to().getZ()));
      if (this.tick_I < this.init_List.size() - 1) {
         int dx = Integer.signum(m.to().getX() - m.from().getX());
         int dz = Integer.signum(m.to().getZ() - m.from().getZ());
         BlockPos past = m.to().offset(dx, 0, dz);
         if (init_Z_BlockPos_Function(past, this::load_ClassCache_FileCac_BlockPos)) {
            cells.add(l.init_J_I_I(past.getX(), past.getZ()));
         }
      }

      return cells;
   }

   private l.Inner_DataUtil init_l_DataUtil_LocalPlayer(LocalPlayer player, ClassCache.UiHelper m, float heading) {
      int dx = Integer.signum(m.to().getX() - m.from().getX());
      int dz = Integer.signum(m.to().getZ() - m.from().getZ());
      boolean far = false;
      if (this.tick_I + 1 < this.init_List.size()) {
         ClassCache.UiHelper next = this.init_List.get(this.tick_I + 1);
         far = Integer.signum(next.to().getX() - next.from().getX()) == dx
            && Integer.signum(next.to().getZ() - next.from().getZ()) == dz
            && next.to().getY() <= next.from().getY()
            && next.cellsToBreak().isEmpty()
            && next.cellsToPlace().isEmpty();
      }

      Set<Long> cells = this.init_Set_ClassCache_UiHelpe(m);
      NodeWalker.Inner_DataUtil go = this.init_NodeWalker_DataUti_F_Z(heading, player.isSprinting());
      NodeWalker.Inner_DataUtil none = new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, false, this.init_DataUtil.init_F());
      NodeWalker.Inner_DataUtil brake = this.init_NodeWalker_DataUti_F_Z(heading + 180.0F, false);
      return l.init(
         init_NodeWalker_StreamU_LocalPlayer(player), go, none, brake, cells::contains, far, m.to().getX() + 0.5, m.to().getZ() + 0.5, this.init_BitSetUtil()
      );
   }

   private boolean load_Z_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper m) {
      if (player.onGround() && !player.isInWater()) {
         BitSetUtil world = this.init_BitSetUtil();
         float heading = this.init_F_LocalPlayer(player, m, this.load_Vec3_ClassCache_UiHelpe(m), false);
         NodeWalker.Inner_DataUtil run = this.init_NodeWalker_DataUti_F_Z(heading, true);
         NodeWalker.Inner_StreamUtil state = init_NodeWalker_StreamU_LocalPlayer(player);

         for (int t = 0; t < 20 && state.onGround(); t++) {
            state = NodeWalker.init(state, run, world);
         }

         if (state.onGround()) {
            return false;
         }

         l.Inner_StreamUtil landing = l.init(state, this.init_NodeWalker_DataUti_F_Z(init_F_ClassCache_UiHelpe(m) + 180.0F, false), false, world);
         return landing.down() && this.init_Set_ClassCache_UiHelpe(m).contains(l.init_J_D_D(landing.at().x(), landing.at().z()));
      } else {
         return false;
      }
   }

   private Set<Long> load_Set_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      Set<Long> cells = new HashSet<>();
      cells.add(l.init_J_I_I(m.to().getX(), m.to().getZ()));

      for (int i = this.tick_I + 1; i < Math.min(this.init_List.size(), this.tick_I + 3); i++) {
         ClassCache.UiHelper next = this.init_List.get(i);
         if (!push(next)
            || next.to().getY() != m.to().getY()
            || i + 1 >= this.init_List.size()
            || !push(this.init_List.get(i + 1)) && this.init_List.get(i + 1).to().getY() < next.to().getY()) {
            break;
         }

         cells.add(l.init_J_I_I(next.to().getX(), next.to().getZ()));
      }

      return cells;
   }

   private NodeWalker.Inner_DataUtil init_NodeWalker_DataUti_F_Z(float wantYaw, boolean sprint) {
      float faced = this.init_DataUtil.init_F();
      int[] keys = init_IArr_F_F(faced, wantYaw);
      return new NodeWalker.Inner_DataUtil(keys[0], -keys[1], false, sprint && keys[0] > 0, false, faced);
   }

   private boolean read_Z_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper m) {
      if (this.start_Z && player.onGround() && player.isSprinting() && !player.isInWater() && !player.isShiftKeyDown()) {
         int y = m.to().getY();
         if (Math.abs(player.getY() - y) > 0.01) {
            return false;
         }

         int last = -1;

         for (int i = this.tick_I; i < Math.min(this.init_List.size(), this.tick_I + 8); last = i++) {
            ClassCache.UiHelper run = this.init_List.get(i);
            if (!push(run) || run.to().getY() != y || run.type() == ClassCache.Inner_SyncManager.init || run.type() == ClassCache.Inner_SyncManager.load) {
               break;
            }
         }

         if (last <= this.tick_I) {
            return false;
         }

         for (int i = this.tick_I; i <= last; i++) {
            BlockPos at = this.init_List.get(i).to();

            for (int dx = -1; dx <= 1; dx++) {
               for (int dz = -1; dz <= 1; dz++) {
                  if (player.level().getBlockState(at.offset(dx, -1, dz)).getBlock() instanceof FarmlandBlock) {
                     return false;
                  }
               }
            }
         }

         Set<Long> cells = new HashSet<>();
         Set<Long> run = new HashSet<>();

         for (int i = this.tick_I; i <= last; i++) {
            long c = l.init_J_I_I(this.init_List.get(i).to().getX(), this.init_List.get(i).to().getZ());
            run.add(c);
            if (i < last) {
               cells.add(c);
            }
         }

         Map<Long, Boolean> floorRoom = new HashMap<>();

         for (int i = this.tick_I; i < last; i++) {
            BlockPos at = this.init_List.get(i).to();
            boolean room = true;

            for (int dx = -1; dx <= 1 && room; dx++) {
               for (int dz = -1; dz <= 1 && room; dz++) {
                  BlockPos beside = at.offset(dx, 0, dz);
                  room = this.init_Z_D(at.getX() + 0.5, at.getZ() + 0.5, beside, floorRoom);
               }
            }

            if (room) {
               for (int dx = -1; dx <= 1; dx++) {
                  for (int dz = -1; dz <= 1; dz++) {
                     long beside = l.init_J_I_I(at.getX() + dx, at.getZ() + dz);
                     cells.add(beside);
                     run.add(beside);
                  }
               }
            }
         }

         run.add(l.init_J_I_I(m.from().getX(), m.from().getZ()));
         float heading = this.init_F_LocalPlayer(player, m, this.load_Vec3_ClassCache_UiHelpe(m), false);
         BitSetUtil world = this.init_BitSetUtil();
         l.Inner_StreamUtil landing = l.init(init_NodeWalker_StreamU_LocalPlayer(player), this.init_NodeWalker_DataUti_F_Z(heading, true), true, world);
         if (landing.down() && !landing.struck() && !(Math.abs(landing.at().y() - player.getY()) > 0.01)) {
            BlockPos land = BlockPos.containing(landing.at().x(), y, landing.at().z());
            if (!cells.contains(l.init_J_I_I(land.getX(), land.getZ()))) {
               return false;
            } else {
               return !l.init(landing.at(), run::contains, world) ? false : this.init_Z_D(player.getX(), player.getZ(), land, new HashMap<>());
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void update_V_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper m) {
      boolean sprint = this.close(player) && this.tick_I < this.init_List.size() - 1;
      boolean press = false;
      Vec3 gaze = this.init_Vec3_LocalPlayer_ClassCache_UiHelpe(player, m);
      if (this.data_Z && player.onGround() && load_Z_D_D(player.getY(), this.init_D_ClassCache_UiHelpe(m))) {
         float yaw = this.init_F_LocalPlayer(player, m, gaze, false);
         this.init_V_LocalPlayer_F(player, yaw);
         if (this.init_Z_LocalPlayer_F(player, yaw)) {
            return;
         }

         gaze = this.read_Vec3;
      } else if (m.type() == ClassCache.Inner_SyncManager.sync && !this.init_DataUtil.init_Z()) {
         Vec3 walk = this.init_Vec3_LocalPlayer_2(player, m, gaze);
         int dx = m.to().getX() - m.from().getX();
         int dz = m.to().getZ() - m.from().getZ();
         Vec3 d = walk.subtract(player.getEyePosition());
         boolean ahead = (dx != 0 || dz != 0) && d.horizontalDistanceSqr() >= 2.25;
         float walkYaw = ahead ? EventBus.init(d) : 0.0F / 0.0F;
         boolean near = ahead && Math.abs(Mth.wrapDegrees(walkYaw - NodeWalker.init_F_D(0.0, 0.0, dx, dz))) <= 60.0F;
         if (ahead && !near && this.update_ClassCache_UiHelpe != m) {
            if (this.write_ClassCache_UiHelpe == m) {
               near = true;
            } else if (!player.onGround() || !load_Z_D_D(player.getY(), this.init_D_ClassCache_UiHelpe(m))) {
               near = true;
            } else if (this.init_Z_LocalPlayer_2(player, m, walkYaw)) {
               this.write_ClassCache_UiHelpe = m;
               near = true;
            } else {
               this.update_ClassCache_UiHelpe = m;
            }
         }

         if (near) {
            this.init_V_LocalPlayer_Vec3(player, walk);
            gaze = this.read_Vec3;
         } else {
            this.init_V_Vec3(gaze);
         }
      } else {
         this.init_V_Vec3(gaze);
      }

      if (player.onGround() && load_Z_D_D(player.getY(), this.init_D_ClassCache_UiHelpe(m))) {
         NodeWalker.Inner_DataUtil run = this.init_NodeWalker_DataUti_F_Z(this.init_F_LocalPlayer(player, m, gaze, false), sprint || player.isSprinting());
         Set<Long> cells = this.load_Set_ClassCache_UiHelpe(m);
         NodeWalker.Inner_StreamUtil state = init_NodeWalker_StreamU_LocalPlayer(player);
         BitSetUtil world = this.init_BitSetUtil();
         int now = l.init(state, run, 0, this.init_D_ClassCache_UiHelpe(m), cells::contains, world);
         int later = l.init(state, run, 1, this.init_D_ClassCache_UiHelpe(m), cells::contains, world);
         if (now < 0 && later < 0 && sprint) {
            NodeWalker.Inner_DataUtil walk = this.init_NodeWalker_DataUti_F_Z(this.init_F_LocalPlayer(player, m, gaze, false), false);
            now = l.init(state, walk, 0, this.init_D_ClassCache_UiHelpe(m), cells::contains, world);
            later = l.init(state, walk, 1, this.init_D_ClassCache_UiHelpe(m), cells::contains, world);
            sprint = false;
         }

         if (now < 0 && later < 0 && load_Z_D(init_D_LocalPlayer_ClassCache_UiHelpe(player, m))) {
            double[] line = this.init_DArr_ClassCache_UiHelpe(m);
            int dx = m.to().getX() - m.from().getX();
            int dz = m.to().getZ() - m.from().getZ();
            double length = Math.hypot(dx, dz);
            double ux = dx / length;
            double uz = dz / length;
            double lateral = length > 0.0 ? Math.abs((player.getX() - line[0]) * -uz + (player.getZ() - line[1]) * ux) : 0.0;
            if (init_Z_D(lateral) && this.index < 20) {
               this.index++;
               this.init_V_LocalPlayer_Z(player, false);
               this.init_V_LocalPlayer(player, m, false, false, gaze);
               return;
            }

            float faced = this.init_DataUtil.init_F();
            boolean runSprint = sprint;
            if (length > 0.0 && this.pool < 8 && l.init(state, this.init_D_ClassCache_UiHelpe(m), cells::contains, world, future -> {
               double along = (future.x() - line[0]) * ux + (future.z() - line[1]) * uz;
               double ahead = along + 0.5;
               if (this.tick_I == this.init_List.size() - 1) {
                  ahead = Math.min(ahead, 0.0);
               }

               float heading = NodeWalker.init_F_D(future.x(), future.z(), line[0] + ux * ahead, line[1] + uz * ahead);
               int[] keys = init_IArr_F_F(faced, heading);
               return new NodeWalker.Inner_DataUtil(keys[0], -keys[1], false, runSprint && keys[0] > 0, false, faced);
            })) {
               this.pool++;
               this.set();
               this.init_V_LocalPlayer_Z(player, false);
               return;
            }

            if (m.type() == ClassCache.Inner_SyncManager.sync && !this.data_Z && !this.init_DataUtil.init_Z()) {
               this.data_Z = true;
               this.set();
               this.init_Minecraft.options.keyShift.setDown(true);
               return;
            }

            if (init_Z) {
               ListPool.init(
                  "jump-up rejected state="
                     + state
                     + " input="
                     + this.init_NodeWalker_DataUti_F_Z(this.init_F_LocalPlayer(player, m, gaze, false), sprint)
                     + " now="
                     + now
                     + " later="
                     + later
                     + " surface="
                     + this.init_D_ClassCache_UiHelpe(m)
                     + " cells="
                     + cells
               );
            }

            this.init_V_Str("no safe jump-up landing from the live approach");
            this.init_Minecraft.options.keyShift.setDown(true);
            return;
         }

         press = l.init_Z_I_I(now, later);
         if (init_Z && press && !this.size_Z) {
            this.size_Z = true;
            ListPool.init(
               "jump-up press to="
                  + m.to()
                  + " edge="
                  + String.format("%.2f", init_D_LocalPlayer_ClassCache_UiHelpe(player, m))
                  + " ticks now="
                  + now
                  + " later="
                  + later
                  + " position="
                  + player.position()
            );
         }
      }

      this.init_V_LocalPlayer(player, m, sprint, press, gaze);
   }

   private Vec3 init_Vec3_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper m) {
      int dx = m.to().getX() - m.from().getX();
      int dz = m.to().getZ() - m.from().getZ();
      if (dx == 0 && dz == 0) {
         return this.init_Vec3_Vec3(new Vec3(m.to().getX() + 0.5, player.getEyePosition().y, m.to().getZ() + 0.5));
      }

      float yaw = NodeWalker.init_F_D(0.0, 0.0, dx, dz);
      float zig = m.type() == ClassCache.Inner_SyncManager.sync ? this.init_F_I_F(this.tick_I, yaw) : 0.0F / 0.0F;
      boolean dug = m.type() == ClassCache.Inner_SyncManager.stop || m.type() == ClassCache.Inner_SyncManager.next;
      if (!Float.isNaN(zig)) {
         yaw = zig;
      } else if (dug && Math.abs(Mth.wrapDegrees(player.getYRot() - yaw)) <= 60.0F) {
         yaw = player.getYRot();
      }

      float pitch = Math.max(-60.0F, Math.min(60.0F, player.getXRot()));
      return player.getEyePosition().add(Vec3.directionFromRotation(pitch, yaw).scale(3.0));
   }

   private float init_F_I_F(int at, float yaw) {
      for (int other : new int[]{at + 1, at - 1}) {
         if (other >= 0 && other < this.init_List.size()) {
            float turn = init_F_ClassCache_UiHelpe_F(this.init_List.get(other), yaw);
            if (!Float.isNaN(turn)) {
               return yaw + turn / 2.0F;
            }
         }
      }

      return 0.0F / 0.0F;
   }

   static float init_F_ClassCache_UiHelpe_F(ClassCache.UiHelper step, float yaw) {
      if (step.type() != ClassCache.Inner_SyncManager.sync) {
         return 0.0F / 0.0F;
      }

      int dx = step.to().getX() - step.from().getX();
      int dz = step.to().getZ() - step.from().getZ();
      if (dx != 0 == (dz != 0)) {
         return 0.0F / 0.0F;
      }

      float turn = Mth.wrapDegrees(NodeWalker.init_F_D(0.0, 0.0, dx, dz) - yaw);
      return Math.abs(Math.abs(turn) - 90.0F) < 1.0F ? turn : 0.0F / 0.0F;
   }

   private boolean init_Z_LocalPlayer_2(LocalPlayer player, ClassCache.UiHelper m, float walkYaw) {
      int dx = m.to().getX() - m.from().getX();
      int dz = m.to().getZ() - m.from().getZ();
      int[] keys = init_IArr_F_F(walkYaw, NodeWalker.init_F_D(0.0, 0.0, dx, dz));
      NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(keys[0], -keys[1], false, false, false, walkYaw);
      Set<Long> cells = this.load_Set_ClassCache_UiHelpe(m);
      NodeWalker.Inner_StreamUtil state = init_NodeWalker_StreamU_LocalPlayer(player);
      BitSetUtil world = this.init_BitSetUtil();
      return l.init(state, input, 0, this.init_D_ClassCache_UiHelpe(m), cells::contains, world) >= 0
         || l.init(state, input, 1, this.init_D_ClassCache_UiHelpe(m), cells::contains, world) >= 0;
   }

   static boolean init_Z_D(double lateral) {
      return lateral > 0.12;
   }

   static boolean load_Z_D_D(double feetY, double stepSurface) {
      return stepSurface - feetY > 0.6;
   }

   static boolean load_Z_D(double edgeDistance) {
      return edgeDistance <= 0.6;
   }

   static boolean init_Z_ClassCache_UiHelpe_2(ClassCache.UiHelper m, int cellX, int cellZ, double playerY, boolean supported) {
      if (!supported) {
         return false;
      } else {
         double dy = playerY - m.to().getY();
         if (!(dy < -1.0) && !(dy > 0.6)) {
            int dx = Integer.signum(m.to().getX() - m.from().getX());
            int dz = Integer.signum(m.to().getZ() - m.from().getZ());
            BlockPos to = m.to();
            return cellX == to.getX() && cellZ == to.getZ() || cellX == to.getX() + dx && cellZ == to.getZ() + dz;
         } else {
            return false;
         }
      }
   }

   static boolean init_Z_List(List<ClassCache.UiHelper> path, int index, double x, double z) {
      if (init_Z_List_I(path, index)) {
         return true;
      }

      if (index >= path.size()) {
         return false;
      }

      ClassCache.UiHelper first = path.get(index);
      BlockPos delta = first.to().subtract(first.from());
      BlockPos end = first.to();
      double len = Math.hypot(delta.getX(), delta.getZ());
      if (len != 0.0 && delta.getY() == 0) {
         for (int i = index; i < path.size(); i++) {
            ClassCache.UiHelper m = path.get(i);
            if (m.type() != ClassCache.Inner_SyncManager.reset
                  && m.type() != ClassCache.Inner_SyncManager.read
                  && m.type() != ClassCache.Inner_SyncManager.init
               || !m.to().subtract(m.from()).equals(delta)) {
               if (i == index) {
                  return false;
               }
               break;
            }

            end = m.to();
         }

         double ahead = ((end.getX() + 0.5 - x) * delta.getX() + (end.getZ() + 0.5 - z) * delta.getZ()) / len;
         return ahead >= 4.5;
      } else {
         return false;
      }
   }

   static boolean init_Z_List_I(List<ClassCache.UiHelper> path, int index) {
      if (index + 4 >= path.size()) {
         return false;
      }

      ClassCache.UiHelper first = path.get(index);
      BlockPos delta = first.to().subtract(first.from());

      for (int i = index; i <= index + 4; i++) {
         ClassCache.UiHelper m = path.get(i);
         if (m.type() != ClassCache.Inner_SyncManager.reset && m.type() != ClassCache.Inner_SyncManager.read && m.type() != ClassCache.Inner_SyncManager.init) {
            return false;
         }

         if (!m.to().subtract(m.from()).equals(delta)) {
            return false;
         }
      }

      return true;
   }

   private void sync_V(LocalPlayer player, ClassCache.UiHelper m) {
      boolean up = m.to().getY() > m.from().getY();
      boolean water = player.isInWater();
      if (!player.onClimbable() && !water) {
         this.init_V_LocalPlayer_3(player, m, false, up);
      } else {
         Direction wall = water ? null : this.init_Direction_BlockPos(player.blockPosition());
         double columnX = m.to().getX() + 0.5;
         double columnZ = m.to().getZ() + 0.5;
         this.init_Minecraft.options.keyDown.setDown(false);
         if (wall != null && up) {
            Vec3 eye = player.getEyePosition();
            this.init_V_Vec3(new Vec3(player.getX() + wall.getStepX(), eye.y, player.getZ() + wall.getStepZ()));
            this.init_Minecraft.options.keyUp.setDown(init_Z_F_F(this.init_DataUtil.init_F(), NodeWalker.init_F_D(0.0, 0.0, wall.getStepX(), wall.getStepZ())));
         } else {
            if (water) {
               this.init_DataUtil.init_V();
               this.init_V_LocalPlayer(player, columnX, columnZ, 0.15);
               this.init_V_LocalPlayer_Z(player, false);
               this.init_Minecraft.options.keyJump.setDown(up);
               this.init_Minecraft.options.keyShift.setDown(!up);
               return;
            }

            this.init_DataUtil.init_V();
            this.init_Minecraft.options.keyUp.setDown(false);
         }

         this.init_Minecraft.options.keyLeft.setDown(false);
         this.init_Minecraft.options.keyRight.setDown(false);
         this.init_V_LocalPlayer_Z(player, false);
         this.init_Minecraft.options.keyJump.setDown(up);
         this.init_Minecraft.options.keyShift.setDown(!up && water);
      }
   }

   private void init_V_LocalPlayer(LocalPlayer player, double x, double z, double deadBand) {
      double yaw = Math.toRadians(player.getYRot());
      double dx = x - player.getX();
      double dz = z - player.getZ();
      double forward = dx * -Math.sin(yaw) + dz * Math.cos(yaw);
      double right = dx * -Math.cos(yaw) + dz * -Math.sin(yaw);
      this.init_Minecraft.options.keyUp.setDown(forward > deadBand);
      this.init_Minecraft.options.keyDown.setDown(forward < -deadBand);
      this.init_Minecraft.options.keyRight.setDown(right > deadBand);
      this.init_Minecraft.options.keyLeft.setDown(right < -deadBand);
   }

   static boolean init_Z_ClassCache_SyncMan(ClassCache.Inner_SyncManager type) {
      boolean var10000;
      switch (type) {
         case put:
         case add:
         case clear:
         case start:
         case tick:
         case run:
            var10000 = true;
            break;
         case flush:
         case close:
         case open:
         case poll:
         case push:
         case pop:
         case stop:
         case next:
         default:
            var10000 = false;
      }

      return var10000;
   }

   static boolean init_Z_F_F(float facedYaw, float wantYaw) {
      return Math.abs(Mth.wrapDegrees(wantYaw - facedYaw)) <= 22.5F;
   }

   public static int[] init_IArr_F_F(float facedYaw, float wantYaw) {
      double off = Math.toRadians(Mth.wrapDegrees(wantYaw - facedYaw));
      double forward = Math.cos(off);
      double right = Math.sin(off);
      double band = Math.cos(Math.toRadians(67.5));
      return new int[]{forward > band ? 1 : (forward < -band ? -1 : 0), right > band ? 1 : (right < -band ? -1 : 0)};
   }

   private boolean init_Z_F(float wantYaw) {
      int[] keys = init_IArr_F_F(this.init_DataUtil.init_F(), wantYaw);
      this.init_Minecraft.options.keyUp.setDown(keys[0] > 0);
      this.init_Minecraft.options.keyDown.setDown(keys[0] < 0);
      this.init_Minecraft.options.keyRight.setDown(keys[1] > 0);
      this.init_Minecraft.options.keyLeft.setDown(keys[1] < 0);
      return keys[0] > 0;
   }

   private Direction init_Direction_BlockPos(BlockPos cell) {
      BlockState state = this.init_Minecraft.level.getBlockState(cell);
      if (state.getBlock() instanceof LadderBlock) {
         return state.getValue(LadderBlock.FACING).getOpposite();
      }

      BlockState above = this.init_Minecraft.level.getBlockState(cell.above());
      return above.getBlock() instanceof LadderBlock ? above.getValue(LadderBlock.FACING).getOpposite() : null;
   }

   public void sync_V() {
      this.set();
      if (this.init_Minecraft.player != null) {
         this.init_V_LocalPlayer_Z(this.init_Minecraft.player, false);
      }

      RefQueue.Inner_DataUtil.init_V_Minecraft(this.init_Minecraft);
   }

   public boolean open_Z() {
      return this.init_ClassCache_UiHelpe != null && !this.init_ClassCache_UiHelpe.cellsToPlace().isEmpty() && !this.update_Z;
   }

   private boolean load_Z_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      int dx = Integer.signum(m.to().getX() - m.from().getX());
      int dz = Integer.signum(m.to().getZ() - m.from().getZ());
      BlockPos after = m.to().offset(dx, 0, dz);
      ClassCache.Inner_FileCache cell = this.load_ClassCache_FileCac_BlockPos(after);
      if (!cell.loaded()) {
         return false;
      }

      if (!cell.passable()) {
         return true;
      }

      ClassCache.Inner_FileCache floor = this.load_ClassCache_FileCac_BlockPos(after.below());
      return floor.loaded() && !floor.passable();
   }

   private void init_V_LocalPlayer_3(LocalPlayer player, ClassCache.UiHelper m, boolean sprint, boolean jump) {
      this.init_V_LocalPlayer(player, m, sprint, jump, null);
   }

   private void init_V_LocalPlayer(LocalPlayer player, ClassCache.UiHelper m, boolean sprint, boolean jump, Vec3 aimOverride) {
      if (AutismLiteVariant.enabled() || this.load_Function == null || !this.init_Z_LocalPlayer(player, m, sprint, aimOverride)) {
         if (player.onClimbable() && !player.onGround() && load_Z_ClassCache_UiHelpe_D(m, player.getY())) {
            this.init_DataUtil.init_V();
            this.set();
            this.init_V_LocalPlayer_Z(player, false);
         } else {
            Vec3 gaze = aimOverride != null ? aimOverride : this.load_Vec3_ClassCache_UiHelpe(m);
            BlockPos door = aimOverride == null ? this.update_BlockPos() : null;
            if (door != null) {
               this.init_V_Vec3(this.init_Vec3_BlockPos(door));
            } else if (aimOverride != null) {
               this.init_V_Vec3(gaze);
            } else {
               this.init_V_LocalPlayer_Vec3(player, this.init_Vec3_LocalPlayer_2(player, m, gaze));
            }

            if (sprint && aimOverride == null && (m.type() == ClassCache.Inner_SyncManager.read || m.type() == ClassCache.Inner_SyncManager.write)) {
               jump = this.read_Z_LocalPlayer_ClassCache_UiHelpe(player, m);
            }

            if (this.write_Z_LocalPlayer_ClassCache_UiHelpe(player, m)) {
               this.init_Minecraft.options.keyUp.setDown(false);
               this.init_Minecraft.options.keyLeft.setDown(false);
               this.init_Minecraft.options.keyRight.setDown(false);
               this.init_V_LocalPlayer_Z(player, false);
               this.init_Minecraft.options.keyJump.setDown(false);
               this.init_Minecraft.options.keyShift.setDown(false);
            } else {
               float want = this.init_F_LocalPlayer(player, m, gaze, false);
               float faced = this.init_DataUtil.init_F();
               if (init_Z && this.run_I % 10 == 0 && NodeWalker.Inner_StreamUtil.stuckAt(player) != 0) {
                  ListPool.init(
                     "web tick move="
                        + m.type()
                        + " to="
                        + m.to()
                        + " pos="
                        + String.format("%.3f,%.3f,%.3f", player.getX(), player.getY(), player.getZ())
                        + " want="
                        + String.format("%.1f", want)
                        + " faced="
                        + String.format("%.1f", faced)
                        + " yRot="
                        + String.format("%.1f", player.getYRot())
                        + " aligned="
                        + init_Z_F_F(faced, want)
                        + " ground="
                        + player.onGround()
                        + " sprinting="
                        + player.isSprinting()
                  );
               }

               if (this.init_DataUtil.init_Z()) {
                  this.init_Minecraft.options.keyDown.setDown(false);
                  this.init_Minecraft.options.keyUp.setDown(true);
                  this.load_V_LocalPlayer(player, m, faced);
                  this.init_V_LocalPlayer_Z(player, sprint);
               } else {
                  boolean forward = this.init_Z_F(want);
                  if (m.type() != ClassCache.Inner_SyncManager.sync && m.type() != ClassCache.Inner_SyncManager.stop) {
                     this.init_V_LocalPlayer(player, m, faced);
                  }

                  this.init_V_LocalPlayer_Z(player, forward && sprint);
               }

               double waterY = m.to().getY();
               if (player.isInWater()
                  && this.tick_I < this.init_List.size()
                  && this.load_ClassCache_FileCac_BlockPos(player.blockPosition().above(2)).passable()) {
                  waterY = init_D_List_I(this.init_List, this.tick_I);
               }

               boolean waterLift = player.isInWater() && load(player.getY(), waterY, player.onGround(), player.isUnderWater());
               this.init_Minecraft.options.keyJump.setDown(jump || waterLift || player.onClimbable() && init_Z_ClassCache_UiHelpe_D(m, player.getY()));
               this.init_Minecraft.options.keyShift.setDown(false);
               if (door != null) {
                  this.init_V_BlockPos(door);
               }
            }
         }
      }
   }

   private boolean init_Z_LocalPlayer(LocalPlayer player, ClassCache.UiHelper movement, boolean sprint, Vec3 override) {
      if (override == null
         && player.onGround()
         && !player.isInWater()
         && !player.onClimbable()
         && !player.isShiftKeyDown()
         && movement.from().getY() == movement.to().getY()
         && movement.cellsToBreak().isEmpty()
         && movement.cellsToPlace().isEmpty()
         && this.update_BlockPos() == null
         && (
            movement.type() == ClassCache.Inner_SyncManager.init
               || movement.type() == ClassCache.Inner_SyncManager.load
               || movement.type() == ClassCache.Inner_SyncManager.read
               || movement.type() == ClassCache.Inner_SyncManager.write
         )) {
         Vec3 point = this.load_Function.apply(shape -> init_Z_AABB(shape, this.init_List, this.tick_I, player.getBoundingBox(), player.getDeltaMovement()));
         if (point == null) {
            return false;
         }

         this.prev_Z = true;
         this.init_V_LocalPlayer(player, movement, sprint, false, point);
         return true;
      } else {
         return false;
      }
   }

   static boolean load(double feetY, double targetY, boolean grounded, boolean submerged) {
      return submerged || !grounded && feetY < targetY + 0.05 || targetY - feetY > 0.6;
   }

   static double init_D_List_I(List<ClassCache.UiHelper> route, int at) {
      double y = route.get(at).to().getY();

      for (int i = at + 1; i < Math.min(route.size(), at + 5); i++) {
         ClassCache.UiHelper next = route.get(i);
         if (next.type() == ClassCache.Inner_SyncManager.sync || next.type() == ClassCache.Inner_SyncManager.update) {
            return Math.max(y, next.to().getY());
         }

         if (!push(next) || !next.cellsToBreak().isEmpty() || !next.cellsToPlace().isEmpty() || next.to().getY() != y) {
            break;
         }
      }

      return y;
   }

   private float init_F_LocalPlayer(LocalPlayer player, ClassCache.UiHelper m, Vec3 gaze, boolean pastTarget) {
      Vec3 recovery = pastTarget ? null : this.load_Vec3_LocalPlayer_ClassCache_UiHelpe(player, m);
      if (recovery != null) {
         return NodeWalker.init_F_D(player.getX(), player.getZ(), recovery.x, recovery.z);
      }

      int dx = m.to().getX() - m.from().getX();
      int dz = m.to().getZ() - m.from().getZ();
      if (dx == 0 && dz == 0) {
         return NodeWalker.init_F_D(player.getX(), player.getZ(), gaze.x, gaze.z);
      }

      BlockPos pulled = pastTarget ? null : this.init_BlockPos_ClassCache_UiHelpe(m);
      if (pulled != null) {
         return NodeWalker.init_F_D(player.getX(), player.getZ(), pulled.getX() + 0.5, pulled.getZ() + 0.5);
      }

      double[] line = this.init_DArr_ClassCache_UiHelpe(m);
      double len = Math.hypot(dx, dz);
      double ux = dx / len;
      double uz = dz / len;
      double along = (player.getX() - line[0]) * ux + (player.getZ() - line[1]) * uz;
      double across = Math.abs((player.getX() - line[0]) * uz - (player.getZ() - line[1]) * ux);
      if (across > 0.6 && this.init_Z_D(player.getX(), player.getZ(), m.to(), new HashMap<>())) {
         return NodeWalker.init_F_D(player.getX(), player.getZ(), line[0], line[1]);
      }

      double ahead = along + 0.5;
      if (!pastTarget
         && (
            this.tick_I >= this.init_List.size() - 1
               || player.getY() > m.to().getY() + 0.6 && load_Z_ClassCache_UiHelpe_Function(m, this::load_ClassCache_FileCac_BlockPos)
         )) {
         ahead = Math.min(ahead, 0.0);
      }

      return NodeWalker.init_F_D(player.getX(), player.getZ(), line[0] + ux * ahead, line[1] + uz * ahead);
   }

   static boolean load_Z_ClassCache_UiHelpe_Function(ClassCache.UiHelper m, Function<BlockPos, ClassCache.Inner_FileCache> probe) {
      if (m.to().getY() < m.from().getY()
         && (
            m.type() == ClassCache.Inner_SyncManager.flush
               || m.type() == ClassCache.Inner_SyncManager.close
               || m.type() == ClassCache.Inner_SyncManager.open
               || m.type() == ClassCache.Inner_SyncManager.next
         )) {
         BlockPos past = m.to().offset(Integer.signum(m.to().getX() - m.from().getX()), 0, Integer.signum(m.to().getZ() - m.from().getZ()));
         ClassCache.Inner_FileCache floor = probe.apply(past);
         return !floor.passable() && floor.top16() >= 16 && probe.apply(past.above()).passable();
      } else {
         return false;
      }
   }

   private Vec3 load_Vec3_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper m) {
      if (this.tick_I > 0
         && this.init_List.get(this.tick_I - 1).type() == ClassCache.Inner_SyncManager.start
         && push(m)
         && player.onGround()
         && !(Math.abs(player.getY() - m.to().getY()) > 0.02)
         && !(Math.abs(player.getX() - (m.from().getX() + 0.5)) > 2.0)
         && !(Math.abs(player.getZ() - (m.from().getZ() + 0.5)) > 2.0)) {
         Map<Long, Boolean> seen = new HashMap<>();
         if (this.init_Z_D(player.getX(), player.getZ(), m.to(), seen)) {
            return null;
         }

         BlockPos here = player.blockPosition();
         Vec3 best = null;
         double cost = 1.0 / 0.0;

         for (BlockPos via : new BlockPos[]{
            m.from(), new BlockPos(here.getX(), m.to().getY(), m.from().getZ()), new BlockPos(m.from().getX(), m.to().getY(), here.getZ())
         }) {
            double x = via.getX() + 0.5;
            double z = via.getZ() + 0.5;
            if (!(Math.hypot(player.getX() - x, player.getZ() - z) < 0.1)
               && this.init_Z_D(player.getX(), player.getZ(), via, seen)
               && this.init_Z_D(x, z, m.to(), seen)) {
               double length = Math.hypot(player.getX() - x, player.getZ() - z) + Math.hypot(m.to().getX() + 0.5 - x, m.to().getZ() + 0.5 - z);
               if (length < cost) {
                  cost = length;
                  best = new Vec3(x, player.getEyeY(), z);
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   private double[] init_DArr_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      double x = m.to().getX() + 0.5;
      double z = m.to().getZ() + 0.5;
      int dx = Integer.signum(m.to().getX() - m.from().getX());
      int dz = Integer.signum(m.to().getZ() - m.from().getZ());
      if (dx != 0 != (dz != 0) && this.init_Minecraft.level != null) {
         for (BlockPos cell : new BlockPos[]{m.to(), m.to().above(), m.from(), m.from().above()}) {
            double gap = this.init_D_BlockPos_Z(cell, dx != 0);
            if (!Double.isNaN(gap)) {
               if (dx != 0) {
                  z = Math.floor(z) + gap;
               } else {
                  x = Math.floor(x) + gap;
               }
               break;
            }
         }

         return new double[]{x, z};
      } else {
         return new double[]{x, z};
      }
   }

   private double init_D_BlockPos_Z(BlockPos cell, boolean alongX) {
      BlockState state = this.init_Minecraft.level.getBlockState(cell);
      if (!(state.getBlock() instanceof DoorBlock) && !(state.getBlock() instanceof LadderBlock)) {
         return 0.0 / 0.0;
      }

      VoxelShape shape = state.getCollisionShape(this.init_Minecraft.level, cell);
      if (shape.isEmpty()) {
         return 0.0 / 0.0;
      }

      AABB b = shape.bounds();
      if (alongX) {
         if (b.maxZ - b.minZ > 0.5) {
            return 0.0 / 0.0;
         } else {
            return b.minZ < 0.01 ? (b.maxZ + 1.0) / 2.0 : b.minZ / 2.0;
         }
      } else if (b.maxX - b.minX > 0.5) {
         return 0.0 / 0.0;
      } else {
         return b.minX < 0.01 ? (b.maxX + 1.0) / 2.0 : b.minX / 2.0;
      }
   }

   private boolean write_Z_BlockPos(BlockPos cell) {
      BlockState state = this.init_Minecraft.level.getBlockState(cell);
      return state.getBlock() instanceof DoorBlock && !state.getCollisionShape(this.init_Minecraft.level, cell).isEmpty();
   }

   private void init_V_LocalPlayer(LocalPlayer player, ClassCache.UiHelper m, float yaw) {
      Vec3 motion = player.getDeltaMovement();
      boolean blocked = k.init(player.horizontalCollision, motion.x, motion.z);
      this.size_I = blocked ? this.size_I + 1 : 0;
      double currentLeft = 0.0;
      if (player.isInWater()) {
         BlockPos at = player.blockPosition();
         Vec3 flow = this.init_Minecraft.level.getFluidState(at).getFlow(this.init_Minecraft.level, at);
         currentLeft = k.init(yaw, flow.x * 0.014, flow.z * 0.014);
      }

      if (blocked || !(Math.abs(currentLeft) <= 0.003)) {
         double[] line = this.init_DArr_ClassCache_UiHelpe(m);
         double offset = k.init(yaw, player.getX(), player.getZ(), line[0], line[1], m.to().getX() - m.from().getX(), m.to().getZ() - m.from().getZ());
         int strafe = k.init(offset, false, blocked, currentLeft, this.size_I);
         if (strafe != 0) {
            if (init_Z && (this.size_I == 1 || this.run_I % 20 == 0)) {
               ListPool.init(
                  "unstick "
                     + (strafe > 0 ? "left" : "right")
                     + " to="
                     + m.to()
                     + " offset="
                     + String.format("%.2f", offset)
                     + " blocked="
                     + this.size_I
                     + " current="
                     + String.format("%.4f", currentLeft)
               );
            }

            this.init_Minecraft.options.keyLeft.setDown(strafe > 0);
            this.init_Minecraft.options.keyRight.setDown(strafe < 0);
         }
      }
   }

   private void load_V_LocalPlayer(LocalPlayer player, ClassCache.UiHelper m, float yaw) {
      Vec3 motion = player.getDeltaMovement();
      boolean blocked = k.init(player.horizontalCollision, motion.x, motion.z);
      this.size_I = blocked ? this.size_I + 1 : 0;
      double offset = k.init(
         yaw, player.getX(), player.getZ(), m.to().getX() + 0.5, m.to().getZ() + 0.5, m.to().getX() - m.from().getX(), m.to().getZ() - m.from().getZ()
      );
      double currentLeft = 0.0;
      if (player.isInWater()) {
         BlockPos at = player.blockPosition();
         Vec3 flow = this.init_Minecraft.level.getFluidState(at).getFlow(this.init_Minecraft.level, at);
         currentLeft = k.init(yaw, flow.x * 0.014, flow.z * 0.014);
      }

      int strafe = k.init(offset, this.read_Z_ClassCache_UiHelpe(m), blocked, currentLeft, this.size_I);
      if (init_Z && strafe != 0 && (this.size_I == 1 || this.run_I % 20 == 0)) {
         ListPool.init(
            "strafe "
               + (strafe > 0 ? "left" : "right")
               + " to="
               + m.to()
               + " offset="
               + String.format("%.2f", offset)
               + " blocked="
               + this.size_I
               + " current="
               + String.format("%.4f", currentLeft)
         );
      }

      this.init_Minecraft.options.keyLeft.setDown(strafe > 0);
      this.init_Minecraft.options.keyRight.setDown(strafe < 0);
   }

   private boolean read_Z_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      int dx = Integer.signum(m.to().getX() - m.from().getX());
      int dz = Integer.signum(m.to().getZ() - m.from().getZ());
      if (dx != 0 == (dz != 0)) {
         return false;
      }

      BlockPos to = m.to();

      for (int side = -1; side <= 1; side += 2) {
         BlockPos beside = dx != 0 ? to.offset(0, 0, side) : to.offset(side, 0, 0);
         if (this.update_Z_BlockPos(beside) || this.update_Z_BlockPos(beside.above())) {
            return true;
         }
      }

      return false;
   }

   private boolean update_Z_BlockPos(BlockPos p) {
      return !this.init_Minecraft.level.getBlockState(p).getCollisionShape(this.init_Minecraft.level, p).isEmpty();
   }

   private boolean write_Z_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper m) {
      if (!this.read_Z || this.tick_I != this.init_List.size() - 1 || m.to().getY() > m.from().getY()) {
         return false;
      }

      if (player.blockPosition().getX() != m.to().getX() || player.blockPosition().getZ() != m.to().getZ()) {
         return false;
      }

      if (player.isInWater()) {
         return false;
      }

      Vec3 motion = player.getDeltaMovement();
      double speed = Math.hypot(motion.x, motion.z);
      if (speed < 1.0E-4) {
         return false;
      }

      double friction = this.init_Minecraft.level.getBlockState(player.blockPosition().below()).getBlock().getFriction();
      if (friction >= 0.97) {
         return false;
      }

      double f = player.onGround() ? 0.91 * friction : 0.91;
      double glide = speed * f / (1.0 - f);
      double stopX = player.getX() + motion.x / speed * glide;
      double stopZ = player.getZ() + motion.z / speed * glide;
      return stopX >= m.to().getX() + 0.1 && stopX <= m.to().getX() + 0.9 && stopZ >= m.to().getZ() + 0.1 && stopZ <= m.to().getZ() + 0.9;
   }

   private void init_V_LocalPlayer_Z(LocalPlayer player, boolean wanted) {
      wanted &= player.getPose() != Pose.CROUCHING;
      init_LocalPlayer = wanted ? null : player;
      count = player.tickCount;
      if (wanted) {
         player.setSprinting(true);
         if (!this.init_Minecraft.options.toggleSprint().get()) {
            this.init_Minecraft.options.keySprint.setDown(true);
         }
      } else {
         player.setSprinting(false);
         this.init_Minecraft.options.keySprint.setDown(false);
      }
   }

   public static boolean poll_Z() {
      LocalPlayer player = Minecraft.getInstance().player;
      if (player != null && player == init_LocalPlayer) {
         int age = player.tickCount - count;
         return age >= 0 && age <= 1;
      } else {
         return false;
      }
   }

   static boolean init_Z_ClassCache_SyncMan_Z(ClassCache.Inner_SyncManager type, boolean foodOk) {
      boolean var10000;
      switch (type) {
         case read:
         case write:
         case reset:
         case put:
         case start:
         case flush:
            var10000 = foodOk;
            break;
         case update:
         case sync:
         case get:
         default:
            var10000 = false;
            break;
         case add:
         case clear:
            var10000 = true;
      }

      return var10000;
   }

   private static double init_D_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper m) {
      int dx = Integer.signum(m.to().getX() - m.from().getX());
      int dz = Integer.signum(m.to().getZ() - m.from().getZ());
      if (dx != 0) {
         double boundary = dx > 0 ? m.from().getX() + 1.0 : m.from().getX();
         return dx > 0 ? boundary - player.getX() : player.getX() - boundary;
      } else {
         double boundary = dz > 0 ? m.from().getZ() + 1.0 : m.from().getZ();
         return dz > 0 ? boundary - player.getZ() : player.getZ() - boundary;
      }
   }

   private boolean init_Z_LocalPlayer(LocalPlayer player) {
      Vec3 now = player.position();
      boolean moved = this.init_Vec3 == null || now.distanceToSqr(this.init_Vec3) > 1.0E-5;
      this.init_Vec3 = now;
      return moved;
   }

   private void flush_V(LocalPlayer player, ClassCache.UiHelper m) {
      TableIndex.Inner_StateCache plan = this.init_TableIndex_StateCa == null ? m.jump() : this.init_TableIndex_StateCa;
      if (plan != null && plan.maneuver() != null) {
         this.init_V_LocalPlayer_2(player, m, plan);
      } else {
         float heading = this.init_F_LocalPlayer_ClassCache_UiHelpe(player, m);
         boolean flying = this.close_Z && (!player.onGround() || player.getDeltaMovement().y > 0.001);
         if (!flying) {
            this.init_V_LocalPlayer_F(player, heading);
         }

         this.init_Minecraft.options.keyShift.setDown(false);
         this.init_Minecraft.options.keyDown.setDown(false);
         if (this.start_I > 0) {
            this.set();
            this.init_Minecraft.options.keyShift.setDown(true);
            if (--this.start_I == 0) {
               this.init_V_Str("jump braked to a stop");
            }
         } else {
            boolean sprint = (plan == null || plan.sprint()) && this.close(player);
            BitSetUtil world = this.init_BitSetUtil();
            NodeWalker.Inner_StreamUtil s = init_NodeWalker_StreamU_LocalPlayer(player);
            double surfTo = this.init_D_ClassCache_UiHelpe(m);
            if (this.close_Z) {
               if (!player.onGround() || player.getDeltaMovement().y > 0.001) {
                  this.poll_Z = true;
                  TableIndex.Inner_BufferHelper cont = this.init_TableIndex_BufferH_ClassCache_UiHelpe(m);
                  int remainingForward = this.stop_I == 2147483647 ? 2147483647 : this.stop_I - this.next_I;
                  TableIndex.Inner_DataUtil air = null;
                  float yaw = player.getYRot();
                  if (s.vy() < 0.0 && s.y() - surfTo <= 0.75 && cont != null && this.tick_I + 1 < this.init_List.size()) {
                     ClassCache.UiHelper next = this.init_List.get(this.tick_I + 1);
                     if (next.from().equals(m.to()) && (next.to().getX() != next.from().getX() || next.to().getZ() != next.from().getZ())) {
                        float nextYaw = init_F_ClassCache_UiHelpe(next);
                        float nextTurn = Mth.wrapDegrees(nextYaw - yaw);
                        if (Math.abs(Mth.wrapDegrees(nextYaw - heading)) <= 95.0F && Math.abs(nextTurn) > 1.0F) {
                           float stepYaw = yaw + Math.clamp(nextTurn, -28.0F, 28.0F);
                           TableIndex.Inner_DataUtil proof = TableIndex.init_TableIndex_DataUti_NodeWalker_CachePo_2(
                              world, s, stepYaw, sprint, m.to(), surfTo, cont
                           );
                           if (proof.margin() >= 0.14) {
                              this.init_V_LocalPlayer_F(player, stepYaw);
                              air = proof;
                           }
                        }
                     }
                  }

                  if (air == null) {
                     air = cont != null
                        ? TableIndex.init_TableIndex_DataUti_NodeWalker_CachePo_2(world, s, yaw, sprint, m.to(), surfTo, cont)
                        : TableIndex.init_TableIndex_DataUti_NodeWalker_CachePo(world, s, yaw, sprint, m.to(), surfTo, remainingForward);
                     if (air == null) {
                        air = TableIndex.init_TableIndex_DataUti_NodeWalker_CachePo_2(world, s, yaw, sprint, m.to(), surfTo, cont);
                     }

                     if (air.margin() < -0.12 && Math.abs(Mth.wrapDegrees(heading - yaw)) > 1.0F) {
                        this.init_V_LocalPlayer_F(player, heading);
                        air = TableIndex.init_TableIndex_DataUti_NodeWalker_CachePo_2(world, s, this.init_DataUtil.init_F(), sprint, m.to(), surfTo, cont);
                     }
                  }

                  if (init_Z) {
                     ListPool.init(
                        "jump air state="
                           + s
                           + " forwardLeft="
                           + remainingForward
                           + " surface="
                           + surfTo
                           + " control="
                           + air
                           + " water="
                           + player.isInWater()
                           + " lava="
                           + player.isInLava()
                     );
                  }

                  this.next_I++;
                  this.init_Minecraft.options.keyUp.setDown(air.forward() > 0.0);
                  this.init_Minecraft.options.keyDown.setDown(air.forward() < 0.0);
                  this.init_Minecraft.options.keyLeft.setDown(air.strafe() > 0.0);
                  this.init_Minecraft.options.keyRight.setDown(air.strafe() < 0.0);
                  this.init_V_LocalPlayer_Z(player, sprint && air.forward() > 0.0);
                  this.init_Minecraft.options.keyJump.setDown(false);
               } else if (!this.poll_Z) {
                  this.init_Minecraft.options.keyJump.setDown(true);
                  this.init_Minecraft.options.keyUp.setDown(true);
                  this.init_V_LocalPlayer_Z(player, sprint);
               } else if (this.write_Z_ClassCache_UiHelpe(m) || !this.init_Z_ClassCache_UiHelpe_2(m, world, s, surfTo)) {
                  if (this.flags == 0
                     && Math.abs(player.getY() - surfTo) <= 0.02
                     && TableIndex.init_D_NodeWalker_CachePo_2(world, s, m.to(), surfTo, player.getYRot()) >= -0.12
                     && init_Z_NodeWalker_CachePo_2(world, s, NodeWalker.Inner_DataUtil.run(this.init_DataUtil.init_F(), sprint))) {
                     this.flags++;
                     this.init_Minecraft.options.keyUp.setDown(true);
                     this.init_Minecraft.options.keyJump.setDown(false);
                     this.init_Minecraft.options.keyLeft.setDown(false);
                     this.init_Minecraft.options.keyRight.setDown(false);
                     this.init_V_LocalPlayer_Z(player, sprint);
                  } else if (!this.init_Z_ClassCache_UiHelpe_2(m, world, s, surfTo)) {
                     if (init_Z) {
                        ListPool.init(
                           "jump land-fail to="
                              + m.to()
                              + " position="
                              + player.position()
                              + " margin="
                              + TableIndex.init(player.getX(), player.getZ(), m.to())
                        );
                     }

                     this.init_V_Str("landed short or wide");
                  }
               }
            } else {
               this.init_Minecraft.options.keyLeft.setDown(false);
               this.init_Minecraft.options.keyRight.setDown(false);
               if (!player.onGround()) {
                  double surfFrom = m.from().getY() - 1 + this.load_ClassCache_FileCac_BlockPos(m.from().below()).top16() / 16.0;
                  TableIndex.Inner_DataUtil settle = TableIndex.init_TableIndex_DataUti_NodeWalker_CachePo(world, s, heading, false, m.from(), surfFrom);
                  this.init_Minecraft.options.keyUp.setDown(settle.margin() >= -0.12 && settle.forward() > 0.0);
                  this.init_Minecraft.options.keyDown.setDown(settle.margin() >= -0.12 && settle.forward() < 0.0);
                  this.init_Minecraft.options.keyLeft.setDown(settle.margin() >= -0.12 && settle.strafe() > 0.0);
                  this.init_Minecraft.options.keyRight.setDown(settle.margin() >= -0.12 && settle.strafe() < 0.0);
                  this.init_Minecraft.options.keyJump.setDown(false);
                  this.init_V_LocalPlayer_Z(player, false);
                  if (init_Z && this.run_I % 10 == 0) {
                     ListPool.init(
                        "jump settling on takeoff " + m.from() + " position=" + player.position() + " motion=" + player.getDeltaMovement() + " proof=" + settle
                     );
                  }
               } else {
                  float yaw = heading;
                  float activeYaw = this.init_DataUtil.init_F();
                  boolean aligned = Math.abs(Mth.wrapDegrees(yaw - activeYaw)) <= 1.0F;
                  if (plan != null && !plan.running()) {
                     if (!this.push_Z) {
                        TableIndex.UiHelper liveOnActive = !aligned && Math.abs(Mth.wrapDegrees(yaw - activeYaw)) <= 20.0F
                           ? TableIndex.init_TableIndex_UiHelpe_NodeWalker_CachePo(world, s, activeYaw, sprint, m.to(), surfTo)
                           : new TableIndex.UiHelper(0, 0.0, 2147483647, -1.0, sprint);
                        if (liveOnActive.runTicks() == 0 && liveOnActive.margin() >= 0.14) {
                           yaw = activeYaw;
                           this.run_Z = true;
                           this.push_Z = true;
                        } else if (TableIndex.init_TableIndex_UiHelpe_NodeWalker_CachePo(world, s, yaw, sprint, m.to(), surfTo).margin()
                           >= init_D_TableIndex_StateCa(plan)) {
                           if (init_Z && !aligned) {
                              ListPool.init("jump settle lands, turning yaw=" + yaw + " camera=" + activeYaw + " position=" + player.position());
                           }

                           if (!aligned && this.init_Z_LocalPlayer(player, yaw, sprint)) {
                              return;
                           }

                           this.run_Z = true;
                           this.push_Z = true;
                        } else {
                           if (!this.run_Z && Math.hypot(s.vx(), s.vz()) > 0.03 && this.init_Z_LocalPlayer(player)) {
                              if (init_Z) {
                                 ListPool.init("jump settle brake to=" + m.to() + " position=" + player.position() + " motion=" + player.getDeltaMovement());
                              }

                              this.init_V_LocalPlayer_2(player, yaw, world, s);
                              return;
                           }

                           if (!aligned) {
                              if (init_Z) {
                                 ListPool.init("jump settle turn yaw=" + yaw + " camera=" + activeYaw + " position=" + player.position());
                              }

                              this.init_Z_LocalPlayer_F(player, yaw);
                              return;
                           }

                           this.run_Z = true;
                           if (init_Z) {
                              ListPool.init(
                                 String.format(
                                    Locale.ROOT,
                                    "jump settle to start (%.2f, %.2f) from (%.2f, %.2f) speed %.3f",
                                    plan.startX(),
                                    plan.startZ(),
                                    player.getX(),
                                    player.getZ(),
                                    Math.hypot(s.vx(), s.vz())
                                 )
                              );
                           }

                           if (this.init(player, plan, yaw, world, s, m.to(), surfTo, sprint)) {
                              return;
                           }

                           this.push_Z = true;
                           s = init_NodeWalker_StreamU_LocalPlayer(player);
                        }
                     }
                  } else if (!aligned) {
                     TableIndex.UiHelper onTurn = Math.abs(Mth.wrapDegrees(yaw - activeYaw)) <= 24.0F
                        ? TableIndex.init_TableIndex_UiHelpe_NodeWalker_CachePo(world, s, activeYaw, sprint, m.to(), surfTo)
                        : new TableIndex.UiHelper(0, 0.0, 2147483647, -1.0, sprint);
                     if (onTurn.runTicks() == 0 && onTurn.margin() >= 0.14) {
                        yaw = activeYaw;
                     } else if (this.init_Z_LocalPlayer(player, yaw, sprint)) {
                        return;
                     }
                  }

                  TableIndex.UiHelper t = TableIndex.init_TableIndex_UiHelpe_NodeWalker_CachePo(world, s, yaw, sprint, m.to(), surfTo);
                  if (t.margin() >= -0.12) {
                     this.init_Minecraft.options.keyUp.setDown(true);
                     this.init_Minecraft.options.keyLeft.setDown(t.strafe() > 0.0);
                     this.init_Minecraft.options.keyRight.setDown(t.strafe() < 0.0);
                     this.init_V_LocalPlayer_Z(player, sprint);
                     if (t.runTicks() == 0) {
                        this.init_Minecraft.options.keyJump.setDown(true);
                        this.close_Z = true;
                        this.close_F = yaw;
                        this.stop_I = t.coastAfter();
                        this.next_I = 0;
                        if (init_Z) {
                           ListPool.init(
                              "jump takeoff from="
                                 + m.from()
                                 + " to="
                                 + m.to()
                                 + " position="
                                 + player.position()
                                 + " motion="
                                 + player.getDeltaMovement()
                                 + " margin="
                                 + t.margin()
                                 + " strafe="
                                 + t.strafe()
                                 + " sprint="
                                 + sprint
                                 + " water="
                                 + player.isInWater()
                                 + " pose="
                                 + player.getPose()
                                 + " decision="
                                 + t
                                 + " plan="
                                 + plan
                                 + "\n"
                                 + n.init(this.init_Minecraft.level, player.blockPosition(), m.to(), 3, 3)
                           );
                        }
                     } else {
                        this.init_Minecraft.options.keyJump.setDown(false);
                     }
                  } else {
                     if (aligned && plan != null && plan.runTicks() > 0 && !this.tick_Z) {
                        NodeWalker.Inner_DataUtil correction = init_NodeWalker_DataUti_NodeWalker_CachePo_2(world, s, yaw, sprint, m.to(), surfTo);
                        if (correction != null) {
                           this.init_V_LocalPlayer_NodeWalker_DataUti(player, correction);
                           return;
                        }
                     }

                     if (!this.tick_Z
                        && init_Z_NodeWalker_CachePo_2(world, s, NodeWalker.Inner_DataUtil.run(yaw, sprint))
                        && this.run_I < ClassCache.Inner_CachePool.init_I_D(m.costTicks()) / 2) {
                        this.init_Minecraft.options.keyUp.setDown(true);
                        this.init_V_LocalPlayer_Z(player, sprint);
                        this.init_Minecraft.options.keyJump.setDown(false);
                     } else if (this.init_TableIndex_StateCa == null && s.onGround()) {
                        if (Math.hypot(s.vx(), s.vz()) > 0.03 && this.init_Z_LocalPlayer(player)) {
                           this.tick_Z = true;
                           this.init_V_LocalPlayer_2(player, yaw, world, s);
                        } else {
                           this.tick_Z = false;
                           double surfFrom = m.from().getY() - 1 + this.load_ClassCache_FileCac_BlockPos(m.from().below()).top16() / 16.0;
                           this.init_TableIndex_StateCa = TableIndex.init_TableIndex_StateCa_NodeWalker_CachePo(
                              world, m.from(), surfFrom, m.to(), surfTo, 0.0, this.close(player)
                           );
                           if (this.init_TableIndex_StateCa == null && plan != null && plan.running() && this.tick_I > 0) {
                              ClassCache.UiHelper previous = this.init_List.get(this.tick_I - 1);
                              BlockPos runway = previous.from();
                              if (push(previous)
                                 && previous.to().equals(m.from())
                                 && init_Z_NodeWalker_CachePo(world, s, runway.getX() + 0.5, runway.getZ() + 0.5)) {
                                 TableIndex.Inner_StateCache backed = TableIndex.init_TableIndex_StateCa_NodeWalker_CachePo(
                                    world, runway, this.init_D_BlockPos(runway), m.to(), surfTo, 0.0, this.close(player), false
                                 );
                                 if (backed != null && backed.margin() >= -0.04) {
                                    this.init_TableIndex_StateCa = backed;
                                 }
                              }
                           }

                           if (init_Z) {
                              ListPool.init("jump re-solved from a standstill to=" + m.to() + " plan=" + this.init_TableIndex_StateCa);
                           }

                           if (this.init_TableIndex_StateCa != null) {
                              this.push_Z = false;
                              this.close_F = this.init_RefQueue_StreamUti == null ? 0.0F / 0.0F : this.init_RefQueue_StreamUti.yaw();
                              this.set();
                           } else {
                              this.init_V_Str("no safe jump landing from the current footing");
                              this.init_Minecraft.options.keyShift.setDown(true);
                           }
                        }
                     } else {
                        if (init_Z) {
                           ListPool.init(
                              "jump no takeoff to="
                                 + m.to()
                                 + " position="
                                 + player.position()
                                 + " motion="
                                 + player.getDeltaMovement()
                                 + " margin="
                                 + t.margin()
                           );
                        }

                        this.start_I = 3;
                        this.set();
                        this.init_Minecraft.options.keyShift.setDown(true);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean init_Z_ClassCache_UiHelpe_2(ClassCache.UiHelper move, BitSetUtil world, NodeWalker.Inner_StreamUtil state, double surface) {
      if (this.flags < 16 && !(Math.abs(state.y() - surface) > 0.02)) {
         NodeWalker.Inner_DataUtil input = init_NodeWalker_DataUti_NodeWalker_CachePo(world, state, this.init_DataUtil.init_F(), move.to());
         if (input == null) {
            return false;
         }

         this.flags++;
         this.set();
         this.init_Minecraft.options.keyUp.setDown(input.forward() > 0.0);
         this.init_Minecraft.options.keyDown.setDown(input.forward() < 0.0);
         this.init_Minecraft.options.keyLeft.setDown(input.strafe() > 0.0);
         this.init_Minecraft.options.keyRight.setDown(input.strafe() < 0.0);
         return true;
      } else {
         return false;
      }
   }

   static NodeWalker.Inner_DataUtil init_NodeWalker_DataUti_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, float yaw, BlockPos target
   ) {
      double x = target.getX() + 0.5;
      double z = target.getZ() + 0.5;
      double bestDistance = Math.hypot(state.x() - x, state.z() - z);
      if (bestDistance > 2.4) {
         return null;
      }

      NodeWalker.Inner_DataUtil best = null;
      NodeWalker.Inner_DataUtil braking = null;
      boolean bestHasFloor = false;
      double startDistance = bestDistance;
      double brakingSpeed = 1.7976931348623157E308;

      for (int forward = -1; forward <= 1; forward++) {
         for (int strafe = -1; strafe <= 1; strafe++) {
            NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(forward, strafe, false, false, false, yaw);
            NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, input, world);
            double distance = Math.hypot(next.x() - x, next.z() - z);
            if (!NodeWalker.load(world, state, next) && init_Z_NodeWalker_CachePo_2(world, state, input)) {
               if (next.speed() < brakingSpeed) {
                  brakingSpeed = next.speed();
                  braking = input;
               }

               if (!(distance >= startDistance)) {
                  boolean hasFloor = TableIndex.init_Z_NodeWalker_CachePo_2(world, (int)Math.floor(next.x()), (int)Math.floor(next.z()), next.y());
                  if (best == null || (bestHasFloor == hasFloor ? !(distance >= bestDistance) : !bestHasFloor)) {
                     bestHasFloor = hasFloor;
                     bestDistance = distance;
                     best = input;
                  }
               }
            }
         }
      }

      if (best != null) {
         return best;
      } else {
         return brakingSpeed < state.speed() - 0.005 ? braking : null;
      }
   }

   private boolean update_Z_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper move) {
      BlockPos from = move.from();
      double x = from.getX() + 0.5;
      double z = from.getZ() + 0.5;
      if (++this.run_I < 20
         && !(Math.abs(player.getY() - this.init_D_BlockPos(from)) > 0.05)
         && !(player.position().distanceToSqr(x, player.getY(), z) > 6.25)
         && this.init_Z_D(player.getX(), player.getZ(), from, new HashMap<>())) {
         this.value_Z = true;
         float heading = this.init_F_LocalPlayer_ClassCache_UiHelpe(player, move);
         this.init_V_LocalPlayer_F(player, heading);
         float toStartYaw = NodeWalker.init_F_D(player.getX(), player.getZ(), x, z);
         int[] keys = init_IArr_F_F(this.init_DataUtil.init_F(), toStartYaw);
         NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(keys[0], -keys[1], false, false, false, this.init_DataUtil.init_F());
         this.set();
         this.init_Z_F(toStartYaw);
         if (!init_Z_NodeWalker_CachePo_2(this.init_BitSetUtil(), init_NodeWalker_StreamU_LocalPlayer(player), input)) {
            this.init_Minecraft.options.keyShift.setDown(true);
         }

         return true;
      } else {
         return false;
      }
   }

   private void init_V_LocalPlayer_2(LocalPlayer player, ClassCache.UiHelper m, TableIndex.Inner_StateCache plan) {
      BitSetUtil world = this.init_BitSetUtil();
      NodeWalker.Inner_StreamUtil state = init_NodeWalker_StreamU_LocalPlayer(player);
      double surface = this.init_D_ClassCache_UiHelpe(m);
      if (this.pop_Z) {
         if (player.onGround()) {
            if (!this.init_Z_ClassCache_UiHelpe_2(m, world, state, surface)) {
               this.init_V_Str("staged jump landed short or wide");
            }
         } else {
            TableIndex.Inner_DataUtil air = TableIndex.init_TableIndex_DataUti_NodeWalker_CachePo(
               world, state, player.getYRot(), this.close(player), m.to(), surface
            );
            this.init_V_LocalPlayer_NodeWalker_DataUti(
               player, new NodeWalker.Inner_DataUtil(air.forward(), air.strafe(), false, this.close(player) && air.forward() > 0.0, false, player.getYRot())
            );
         }
      } else {
         if (this.init_PoolEntry == null) {
            this.init_V_LocalPlayer_F(player, plan.yaw());
            this.init_Minecraft.options.keyShift.setDown(false);
            if (!player.onGround()) {
               double fromSurface = m.from().getY() - 1 + this.load_ClassCache_FileCac_BlockPos(m.from().below()).top16() / 16.0;
               TableIndex.Inner_DataUtil air = TableIndex.init_TableIndex_DataUti_NodeWalker_CachePo(world, state, plan.yaw(), false, m.from(), fromSurface);
               this.init_V_LocalPlayer_NodeWalker_DataUti(player, new NodeWalker.Inner_DataUtil(air.forward(), air.strafe(), false, false, false, plan.yaw()));
               return;
            }

            boolean aligned = Math.abs(Mth.wrapDegrees(this.init_DataUtil.init_F() - plan.yaw())) <= 1.0F;
            double startDistance = Math.hypot(plan.startX() - state.x(), plan.startZ() - state.z());
            if (aligned && startDistance <= 0.25 && plan.maneuver().replay(world, state, 0, m.to(), surface) >= -0.04) {
               this.run_Z = true;
               this.init_PoolEntry = plan.maneuver();
               this.prev_I = 0;
            } else {
               if (!this.run_Z && Math.hypot(state.vx(), state.vz()) > 0.03 && this.init_Z_LocalPlayer(player)) {
                  this.init_V_LocalPlayer_NodeWalker_DataUti(player, PoolEntry.settleInput(world, state, this.init_DataUtil.init_F()));
                  return;
               }

               this.run_Z = true;
               if (!aligned) {
                  this.init_Z_LocalPlayer_F(player, plan.yaw());
                  return;
               }

               if (this.init(player, plan, plan.yaw(), world, state, m.to(), surface, plan.sprint() && this.close(player))) {
                  return;
               }

               state = init_NodeWalker_StreamU_LocalPlayer(player);
               double initialProof = plan.maneuver().replay(world, state, 0, m.to(), surface);
               if (initialProof >= -0.04) {
                  this.init_PoolEntry = plan.maneuver();
               } else {
                  this.init_PoolEntry = plan.maneuver().fitApproach(world, state, m.to(), surface, this.init_DataUtil.init_F());
                  if (this.init_PoolEntry == null) {
                     this.init_PoolEntry = PoolEntry.live(world, state, m.from(), m.to(), surface, plan.yaw(), plan.sprint() && this.close(player));
                  }
               }

               if (this.init_PoolEntry == null) {
                  if (init_Z) {
                     ListPool.init(
                        "staged initial approach failed from="
                           + m.from()
                           + " to="
                           + m.to()
                           + " state="
                           + state
                           + " proof="
                           + initialProof
                           + " solverSprint="
                           + (plan.sprint() && this.close(player))
                           + " movementSpeed="
                           + world.load()
                           + " plan="
                           + plan
                           + "\n"
                           + n.init(this.init_Minecraft.level, m.from(), m.to(), 3, 3)
                     );
                  }

                  this.init_V_Str("staged jump has no safe live approach");
                  return;
               }

               this.prev_I = 0;
            }
         }

         int remaining = this.init_PoolEntry.inputs().size() - this.prev_I;
         if (remaining <= 4
            && this.poll_Z
            && TableIndex.init(state)
            && Math.abs(state.y() - surface) <= 0.02
            && Math.hypot(m.to().getX() + 0.5 - state.x(), m.to().getZ() + 0.5 - state.z()) <= 1.1) {
            if (PoolEntry.coastsIntoLanding(world, state, m.to(), surface, player.getYRot())) {
               this.set();
               return;
            }

            if ((remaining <= 0 || !init_Z_NodeWalker_CachePo_2(world, state, this.init_PoolEntry.inputs().get(this.prev_I)))
               && this.init_Z_ClassCache_UiHelpe_2(m, world, state, surface)) {
               return;
            }
         }

         if (remaining <= 0) {
            this.set();
            if (player.onGround()) {
               if (!PoolEntry.coastsIntoLanding(world, state, m.to(), surface, player.getYRot())) {
                  this.init_V_Str("staged jump landed outside its landing");
               }
            } else {
               this.pop_Z = true;
            }
         } else {
            double proof = this.init_PoolEntry.replay(world, state, this.prev_I, m.to(), surface);
            if (proof < -0.12) {
               if (init_Z) {
                  ListPool.init(
                     "staged replay failed from="
                        + m.from()
                        + " to="
                        + m.to()
                        + " tick="
                        + this.prev_I
                        + " state="
                        + state
                        + " proof="
                        + proof
                        + " maneuver="
                        + this.init_PoolEntry
                  );
               }

               boolean airborne = !player.onGround();
               PoolEntry revised = airborne
                  ? this.init_PoolEntry.fitFlight(world, state, this.prev_I, m.to(), surface)
                  : PoolEntry.live(world, state, m.from(), m.to(), surface, this.init_PoolEntry.inputs().getFirst().yaw(), plan.sprint() && this.close(player));
               if (revised == null) {
                  if (airborne) {
                     this.pop_Z = true;
                     this.init_V_LocalPlayer_2(player, m, plan);
                     return;
                  }

                  this.init_V_LocalPlayer_NodeWalker_DataUti(player, PoolEntry.settleInput(world, state, this.init_DataUtil.init_F()));
                  this.init_PoolEntry = null;
                  this.run_Z = false;
                  this.prev_I = 0;
                  return;
               }

               this.init_PoolEntry = revised;
               this.prev_I = 0;
            }

            NodeWalker.Inner_DataUtil input = this.init_PoolEntry.inputs().get(this.prev_I);
            if (init_Z) {
               ListPool.init(
                  "staged dispatch from="
                     + m.from()
                     + " to="
                     + m.to()
                     + " tick="
                     + this.prev_I
                     + " input="
                     + input
                     + " camera="
                     + player.getYRot()
                     + " planned="
                     + this.init_DataUtil.init_F()
                     + " state="
                     + state
               );
            }

            this.prev_I++;
            if (input.jump()) {
               this.close_Z = true;
               this.close_F = input.yaw();
            }

            if (!player.onGround()) {
               this.poll_Z = true;
            }

            this.init_V_LocalPlayer_4(player, input, true);
         }
      }
   }

   private void init_V_LocalPlayer_NodeWalker_DataUti(LocalPlayer player, NodeWalker.Inner_DataUtil input) {
      this.init_V_LocalPlayer_4(player, input, false);
   }

   private void init_V_LocalPlayer_4(LocalPlayer player, NodeWalker.Inner_DataUtil input, boolean proved) {
      this.init_V_LocalPlayer_F(player, input.yaw());
      if (proved) {
         this.init_DataUtil.init_V_DataUtil_DataUtil(DataUtil.Inner_DataUtil.flush);
      }

      this.init_Minecraft.options.keyUp.setDown(input.forward() > 0.0);
      this.init_Minecraft.options.keyDown.setDown(input.forward() < 0.0);
      this.init_Minecraft.options.keyLeft.setDown(input.strafe() > 0.0);
      this.init_Minecraft.options.keyRight.setDown(input.strafe() < 0.0);
      this.init_Minecraft.options.keyJump.setDown(input.jump());
      this.init_Minecraft.options.keyShift.setDown(input.sneak());
      this.init_V_LocalPlayer_Z(player, input.sprint() && this.close(player));
   }

   private boolean init(
      LocalPlayer player,
      TableIndex.Inner_StateCache plan,
      float yaw,
      BitSetUtil world,
      NodeWalker.Inner_StreamUtil s,
      BlockPos to,
      double surfTo,
      boolean sprint
   ) {
      if (plan.maneuver() == null && this.init_RefQueue_StreamUti == null) {
         this.init_RefQueue_StreamUti = init_RefQueue_StreamUti_NodeWalker_CachePo(world, s, plan, to, surfTo, sprint);
         if (this.init_RefQueue_StreamUti != null) {
            this.close_F = this.init_RefQueue_StreamUti.yaw();
            yaw = this.close_F;
            this.init_V_LocalPlayer_F(player, yaw);
            if (this.init_Z_LocalPlayer_F(player, yaw)) {
               return true;
            }
         }
      }

      double startX = this.init_RefQueue_StreamUti == null ? plan.startX() : this.init_RefQueue_StreamUti.x();
      double startZ = this.init_RefQueue_StreamUti == null ? plan.startZ() : this.init_RefQueue_StreamUti.z();
      double dx = startX - player.getX();
      double dz = startZ - player.getZ();
      double dist = Math.hypot(dx, dz);
      double speed = Math.hypot(s.vx(), s.vz());
      boolean exact = init_Z_TableIndex_StateCa(plan);
      boolean precise = plan.maneuver() != null || this.init_RefQueue_StreamUti != null || exact;
      double tolerance = this.init_RefQueue_StreamUti != null ? 0.012 : (exact ? 0.025 : (precise ? 0.05 : 0.1));
      double speedTolerance = precise ? 0.02 : 0.03;
      if (sprint && speed < 0.05 && (!precise || dist <= tolerance) && player.getPose() == Pose.CROUCHING) {
         NodeWalker.Inner_StreamUtil stood = NodeWalker.init(s, NodeWalker.Inner_DataUtil.NONE, world);
         if (stood.onGround() && Math.abs(stood.y() - s.y()) <= 0.02 && stood.pose() != s.pose()) {
            this.set();
            this.init_Minecraft.options.keyShift.setDown(false);
            return true;
         }
      }

      if (plan.maneuver() == null && speed < 0.05 && player.getPose() != Pose.CROUCHING) {
         double wantMargin = load_D_TableIndex_StateCa(plan);
         double margin = TableIndex.init_TableIndex_UiHelpe_NodeWalker_CachePo(world, s, yaw, sprint, to, surfTo).margin();
         if (margin >= wantMargin || dist <= tolerance && margin >= -0.12) {
            this.set();
            return false;
         }

         if (exact && this.init_RefQueue_StreamUti == null && dist <= 0.12 && speed < 0.02) {
            for (float offset : new float[]{-1.0F, 1.0F, -2.0F, 2.0F, -4.0F, 4.0F}) {
               float adjusted = plan.yaw() + offset;
               if (!(TableIndex.init_TableIndex_UiHelpe_NodeWalker_CachePo(world, s, adjusted, sprint, to, surfTo, 8).margin() < wantMargin)) {
                  this.close_F = adjusted;
                  this.init_V_LocalPlayer_F(player, this.close_F);
                  this.set();
                  return true;
               }
            }
         }

         if (!precise && dist < 0.24 && plan.runTicks() > 0) {
            NodeWalker.Inner_DataUtil correction = init_NodeWalker_DataUti_NodeWalker_CachePo_2(world, s, yaw, sprint, to, surfTo);
            if (correction != null) {
               this.init_V_LocalPlayer_NodeWalker_DataUti(player, correction);
               return true;
            }
         }
      }

      if (this.init_RefQueue_StreamUti == null && dist <= tolerance && speed < speedTolerance) {
         this.set();
         return false;
      }

      if (precise && dist < (this.init_RefQueue_StreamUti == null ? 0.42 : 0.8)) {
         NodeWalker.Inner_DataUtil input = init_NodeWalker_DataUti_NodeWalker_CachePo(
            world, s, yaw, startX, startZ, !exact && this.init_RefQueue_StreamUti == null ? 0.038 : tolerance
         );
         if (this.init_RefQueue_StreamUti != null && input != null && !input.sneak() && input.forward() == 0.0 && input.strafe() == 0.0 && speed < 0.004) {
            input = null;
         }

         if (input == null && this.init_RefQueue_StreamUti != null && speed < 0.02) {
            input = init(world, s, yaw, sprint, to, surfTo, load_D_TableIndex_StateCa(plan));
         }

         if (input == null && this.init_RefQueue_StreamUti != null && speed < 0.02) {
            for (float turn : new float[]{-10.0F, 10.0F, -20.0F, 20.0F, -25.0F, 25.0F}) {
               input = init_NodeWalker_DataUti_NodeWalker_CachePo(world, s, yaw + turn, startX, startZ, tolerance);
               if (input != null) {
                  if (input.forward() == 0.0 && input.strafe() == 0.0) {
                     input = input.withYaw(yaw);
                  }
                  break;
               }
            }
         }

         if (input != null) {
            this.init_V_LocalPlayer_NodeWalker_DataUti(player, input);
            return true;
         }

         if (speed < 0.02) {
            this.set();
            return false;
         }
      }

      NodeWalker.Inner_StreamUtil coast = s;
      boolean coastHolds = true;

      for (int t = 0; t < 6; t++) {
         coast = NodeWalker.init(coast, NodeWalker.Inner_DataUtil.NONE, world);
         if (!coast.onGround() || Math.abs(coast.y() - s.y()) > 0.02) {
            coastHolds = false;
            break;
         }

         if (coast.speed() < 0.02) {
            break;
         }
      }

      if (coastHolds && Math.hypot(startX - coast.x(), startZ - coast.z()) <= tolerance) {
         this.set();
         return true;
      }

      double r = Math.toRadians(yaw);
      double forward = dx * -Math.sin(r) + dz * Math.cos(r);
      double right = dx * -Math.cos(r) + dz * -Math.sin(r);
      this.init_Minecraft.options.keyUp.setDown(forward > 0.06);
      this.init_Minecraft.options.keyDown.setDown(forward < -0.06);
      this.init_Minecraft.options.keyRight.setDown(right > 0.06);
      this.init_Minecraft.options.keyLeft.setDown(right < -0.06);
      this.init_Minecraft.options.keyJump.setDown(false);
      this.init_V_LocalPlayer_Z(player, false);
      double f = (forward > 0.06 ? 1 : 0) - (forward < -0.06 ? 1 : 0);
      double st = (right < -0.06 ? 1 : 0) - (right > 0.06 ? 1 : 0);
      NodeWalker.Inner_StreamUtil next = NodeWalker.init(s, new NodeWalker.Inner_DataUtil(f, st, false, false, false, yaw), world);

      for (int t = 0; t < 3 && next.onGround(); t++) {
         next = NodeWalker.init(next, new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, false, yaw), world);
      }

      boolean keeps = next.onGround() && Math.abs(next.y() - s.y()) <= 0.02;
      this.init_Minecraft.options.keyShift.setDown(!keeps);
      return true;
   }

   private static NodeWalker.Inner_DataUtil init(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, float yaw, boolean sprint, BlockPos target, double surface, double margin
   ) {
      NodeWalker.Inner_DataUtil crouch = new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, true, yaw);
      NodeWalker.Inner_StreamUtil primed = NodeWalker.init(state, crouch, world);
      if (primed.onGround() && !(Math.abs(primed.y() - state.y()) > 0.02) && !NodeWalker.load(world, state, primed)) {
         for (int forward : new int[]{0, -1, 1}) {
            for (int side : new int[]{0, -1, 1}) {
               if (forward != 0 || side != 0) {
                  NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(forward, side, false, false, false, yaw);
                  NodeWalker.Inner_StreamUtil stopped = init_NodeWalker_StreamU_NodeWalker_CachePo(world, state, input);
                  if (stopped != null
                     && TableIndex.init_TableIndex_UiHelpe_NodeWalker_CachePo(world, stopped, yaw, sprint, target, surface, 8).margin() >= margin) {
                     return input;
                  }

                  if (state.pose() == 0) {
                     stopped = init_NodeWalker_StreamU_NodeWalker_CachePo(world, primed, input);
                     if (stopped != null
                        && TableIndex.init_TableIndex_UiHelpe_NodeWalker_CachePo(world, stopped, yaw, sprint, target, surface, 8).margin() >= margin) {
                        return crouch;
                     }
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static RefQueue.Inner_StreamUtil init_RefQueue_StreamUti_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, TableIndex.Inner_StateCache plan, BlockPos target, double surface, boolean sprint
   ) {
      double dx = plan.startX() - state.x();
      double dz = plan.startZ() - state.z();
      double gap = Math.hypot(dx, dz);
      if (!(gap < 0.25) && !(gap > 1.1)) {
         AABB body = state.box(false);
         List<AABB> path = world.init_List(body.expandTowards(dx, 0.0, dz));
         if (NodeWalker.init(new Vec3(dx, 0.0, dz), body, path).horizontalDistanceSqr() >= (gap - 0.02) * (gap - 0.02)) {
            return null;
         }

         RefQueue.Inner_StreamUtil result = null;
         double best = 1.0 / 0.0;

         for (AABB obstacle : path) {
            for (double padding : new double[]{0.012, 0.02}) {
               double clearance = 0.3 + padding;
               double[] xs = new double[]{obstacle.minX - clearance, obstacle.maxX + clearance, state.x(), state.x()};
               double[] zs = new double[]{state.z(), state.z(), obstacle.minZ - clearance, obstacle.maxZ + clearance};

               for (int i = 0; i < xs.length; i++) {
                  double x = xs[i];
                  double z = zs[i];
                  double distance = Math.hypot(x - state.x(), z - state.z());
                  if (!(distance >= best) && !(distance > 0.8) && init_Z_NodeWalker_CachePo(world, state, x, z)) {
                     NodeWalker.Inner_StreamUtil stand = NodeWalker.Inner_StreamUtil.standing(x, state.y(), z);
                     float heading = NodeWalker.init_F_D(x, z, plan.landX(), plan.landZ());

                     for (int turn : new int[]{0, -10, 10, -20, 20}) {
                        float yaw = heading + turn;
                        if (!(TableIndex.init_TableIndex_UiHelpe_NodeWalker_CachePo(world, stand, yaw, sprint, target, surface, 8).margin() < -0.04)) {
                           result = new RefQueue.Inner_StreamUtil(x, z, yaw);
                           best = distance;
                           break;
                        }
                     }
                  }
               }
            }
         }

         return result;
      } else {
         return null;
      }
   }

   private static boolean init_Z_NodeWalker_CachePo(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, double x, double z) {
      int steps = Math.max(1, (int)Math.ceil(Math.hypot(x - state.x(), z - state.z()) / 0.05));
      NodeWalker.Inner_StreamUtil last = state;

      for (int step = 1; step <= steps; step++) {
         double fraction = (double)step / steps;
         NodeWalker.Inner_StreamUtil next = NodeWalker.Inner_StreamUtil.standing(
            state.x() + (x - state.x()) * fraction, state.y(), state.z() + (z - state.z()) * fraction
         );
         if (!TableIndex.init_Z_NodeWalker_CachePo(world, next.x(), next.y(), next.z()) || NodeWalker.load(world, last, next)) {
            return false;
         }

         last = next;
      }

      return true;
   }

   static NodeWalker.Inner_DataUtil init_NodeWalker_DataUti_NodeWalker_CachePo_2(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, float yaw, boolean sprint, BlockPos target, double surface
   ) {
      for (int forward : new int[]{0, -1, 1}) {
         for (int strafe : new int[]{0, -1, 1}) {
            if (forward != 0 || strafe != 0 || !(state.speed() < 0.01)) {
               NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(forward, strafe, false, false, false, yaw);
               NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, input, world);
               if (next.onGround()
                  && !(Math.abs(next.y() - state.y()) > 0.02)
                  && TableIndex.init_Z_NodeWalker_CachePo(world, next.x(), next.y(), next.z())
                  && !NodeWalker.load(world, state, next)
                  && TableIndex.init_TableIndex_UiHelpe_NodeWalker_CachePo(world, next, yaw, sprint, target, surface, 8).margin() >= -0.04) {
                  return input;
               }
            }
         }
      }

      return null;
   }

   static NodeWalker.Inner_DataUtil init_NodeWalker_DataUti_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, float yaw, double x, double z
   ) {
      return init_NodeWalker_DataUti_NodeWalker_CachePo(world, state, yaw, x, z, 0.038);
   }

   private static NodeWalker.Inner_DataUtil init_NodeWalker_DataUti_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil state, float yaw, double x, double z, double tolerance
   ) {
      NodeWalker.Inner_DataUtil neutral = NodeWalker.Inner_DataUtil.NONE.withYaw(yaw);
      NodeWalker.Inner_StreamUtil coast = init_NodeWalker_StreamU_NodeWalker_CachePo(world, state, neutral);
      double closest = coast == null ? 1.0 / 0.0 : Math.hypot(x - coast.x(), z - coast.z());
      if (closest <= tolerance) {
         return neutral;
      }

      NodeWalker.Inner_DataUtil best = null;

      for (boolean sneak : new boolean[]{false, true}) {
         for (int forward = -1; forward <= 1; forward++) {
            for (int strafe = -1; strafe <= 1; strafe++) {
               if (forward != 0 || strafe != 0) {
                  NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(forward, strafe, false, false, sneak, yaw);
                  NodeWalker.Inner_StreamUtil stopped = init_NodeWalker_StreamU_NodeWalker_CachePo(world, state, input);
                  if (stopped != null) {
                     double distance = Math.hypot(x - stopped.x(), z - stopped.z());
                     if (!(distance >= closest - 0.005)) {
                        best = input;
                        closest = distance;
                     }
                  }
               }
            }
         }

         if (best != null && (!sneak && closest <= 0.042 || sneak)) {
            return best;
         }
      }

      if (coast != null && state.speed() >= 0.02) {
         return neutral;
      }

      if (state.pose() == 0 && coast != null) {
         NodeWalker.Inner_DataUtil crouch = new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, true, yaw);
         NodeWalker.Inner_StreamUtil primed = NodeWalker.init(state, crouch, world);
         if (primed.onGround() && !(Math.abs(primed.y() - state.y()) > 0.02) && !NodeWalker.load(world, state, primed)) {
            for (int forward = -1; forward <= 1; forward++) {
               for (int strafe = -1; strafe <= 1; strafe++) {
                  if (forward != 0 || strafe != 0) {
                     NodeWalker.Inner_StreamUtil stopped = init_NodeWalker_StreamU_NodeWalker_CachePo(
                        world, primed, new NodeWalker.Inner_DataUtil(forward, strafe, false, false, true, yaw)
                     );
                     if (stopped != null && Math.hypot(x - stopped.x(), z - stopped.z()) < closest - 0.005) {
                        return crouch;
                     }
                  }
               }
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static NodeWalker.Inner_StreamUtil init_NodeWalker_StreamU_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil start, NodeWalker.Inner_DataUtil first
   ) {
      NodeWalker.Inner_StreamUtil state = start;

      for (int tick = 0; tick < 16; tick++) {
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, tick == 0 ? first : NodeWalker.Inner_DataUtil.NONE, world);
         if (!next.onGround() || Math.abs(next.y() - start.y()) > 0.02 || NodeWalker.load(world, state, next)) {
            return null;
         }

         if (next.speed() <= 0.004) {
            return next;
         }

         state = next;
      }

      return null;
   }

   private TableIndex.Inner_BufferHelper init_TableIndex_BufferH_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      if (this.tick_I + 1 >= this.init_List.size()) {
         return null;
      }

      ClassCache.UiHelper next = this.init_List.get(this.tick_I + 1);
      if (next.from().equals(m.to()) && next.cellsToBreak().isEmpty() && next.cellsToPlace().isEmpty()) {
         int ndx = next.to().getX() - next.from().getX();
         int ndz = next.to().getZ() - next.from().getZ();
         int jdx = m.to().getX() - m.from().getX();
         int jdz = m.to().getZ() - m.from().getZ();
         double denom = Math.hypot(jdx, jdz) * Math.hypot(ndx, ndz);
         if (denom < 1.0E-6 || (jdx * ndx + jdz * ndz) / denom < Math.cos(0.7853981633974483)) {
            return null;
         }

         if (next.type() == ClassCache.Inner_SyncManager.start) {
            if (!this.open(next)) {
               return null;
            }

            boolean sprint = next.jump() == null || next.jump().sprint();
            return TableIndex.Inner_BufferHelper.jump(
               next.from(),
               this.init_D_ClassCache_UiHelpe(m),
               next.to(),
               this.init_D_ClassCache_UiHelpe(next),
               init_F_ClassCache_UiHelpe(next),
               sprint && this.close(this.init_Minecraft.player)
            );
         } else {
            if (push(next)) {
               if (Math.max(Math.abs(ndx), Math.abs(ndz)) != 1 || !this.init_Z_D(m.to().getX() + 0.5, m.to().getZ() + 0.5, next.to(), new HashMap<>())) {
                  return null;
               }
            } else {
               switch (next.type()) {
                  case update:
                  case sync:
                  case flush:
                  case close:
                  case open:
                     if (!this.open(next)) {
                        return null;
                     }
                     break;
                  case get:
                  case put:
                  case add:
                  case clear:
                  case start:
                  default:
                     return null;
               }
            }

            return TableIndex.Inner_BufferHelper.walk(next.to(), this.init_D_ClassCache_UiHelpe(next));
         }
      } else {
         return null;
      }
   }

   static boolean init_Z_ClassCache_UiHelpe_D(ClassCache.UiHelper movement, double feetY) {
      return movement.to().getY() >= movement.from().getY() && feetY < movement.to().getY() - 0.05;
   }

   private boolean read_Z_ClassCache_UiHelpe_D(ClassCache.UiHelper m, double surfTo) {
      BlockPos cell = BlockPos.containing(this.init_Minecraft.player.getX(), m.to().getY(), this.init_Minecraft.player.getZ());
      return Math.abs(cell.getX() - m.to().getX()) <= 1 && Math.abs(cell.getZ() - m.to().getZ()) <= 1
         ? cell.equals(m.to()) || TableIndex.init_Z_NodeWalker_CachePo_2(this.init_BitSetUtil(), cell.getX(), cell.getZ(), surfTo)
         : false;
   }

   private boolean write_Z_ClassCache_UiHelpe(ClassCache.UiHelper move) {
      if (this.tick_I + 1 >= this.init_List.size()) {
         return true;
      }

      LocalPlayer player = this.init_Minecraft.player;
      BlockPos landing = move.to();
      BlockPos actual = BlockPos.containing(player.getX(), landing.getY(), player.getZ());
      if (actual.equals(landing)) {
         return true;
      }

      ClassCache.UiHelper next = this.init_List.get(this.tick_I + 1);
      return push(next) && next.from().equals(landing) && (actual.equals(next.to()) || this.init_Z_D(player.getX(), player.getZ(), next.to(), new HashMap<>()));
   }

   private void init_V_LocalPlayer_2(LocalPlayer player, float yaw, BitSetUtil world, NodeWalker.Inner_StreamUtil s) {
      NodeWalker.Inner_DataUtil settle = PoolEntry.settleInput(world, s, this.init_DataUtil.init_F());
      this.init_Minecraft.options.keyUp.setDown(settle.forward() > 0.0);
      this.init_Minecraft.options.keyDown.setDown(settle.forward() < 0.0);
      this.init_Minecraft.options.keyLeft.setDown(settle.strafe() > 0.0);
      this.init_Minecraft.options.keyRight.setDown(settle.strafe() < 0.0);
      this.init_Minecraft.options.keyJump.setDown(false);
      this.init_Minecraft.options.keyShift.setDown(settle.sneak());
      this.init_V_LocalPlayer_Z(player, false);
   }

   private float init_F_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper m) {
      if (!Float.isNaN(this.close_F)) {
         return this.close_F;
      }

      TableIndex.Inner_StateCache plan = this.init_TableIndex_StateCa == null ? m.jump() : this.init_TableIndex_StateCa;
      if (this.init_TableIndex_StateCa == null && !init_Z_TableIndex_StateCa(plan) && (plan == null || !plan.running() || plan.flightTicks() > 6)) {
         double tx = m.to().getX() + 0.5;
         double tz = m.to().getZ() + 0.5;
         double dx = tx - player.getX();
         double dz = tz - player.getZ();
         if (dx * dx + dz * dz >= 1.5625) {
            return NodeWalker.init_F_D(player.getX(), player.getZ(), tx, tz);
         }

         int mx = m.to().getX() - m.from().getX();
         int mz = m.to().getZ() - m.from().getZ();
         this.close_F = mx == 0 && mz == 0 ? player.getYRot() : NodeWalker.init_F_D(0.0, 0.0, mx, mz);
         return this.close_F;
      } else {
         return plan.yaw();
      }
   }

   private static boolean init_Z_TableIndex_StateCa(TableIndex.Inner_StateCache plan) {
      return plan != null && plan.maneuver() == null && !plan.running() && (plan.backup() > 0.0 || plan.runTicks() > 0 && plan.margin() < 0.045);
   }

   private static double init_D_TableIndex_StateCa(TableIndex.Inner_StateCache plan) {
      return init_Z_TableIndex_StateCa(plan) ? load_D_TableIndex_StateCa(plan) : -0.12;
   }

   private static double load_D_TableIndex_StateCa(TableIndex.Inner_StateCache plan) {
      return Math.max(-0.04, Math.min(0.14, plan.margin() - 0.02));
   }

   private void init_V_LocalPlayer_F(LocalPlayer player, float yaw) {
      float turn = Math.abs(Mth.wrapDegrees(yaw - player.getYRot()));
      this.init_DataUtil.init_V_DataUtil_DataUtil(turn <= 28.0F ? DataUtil.Inner_DataUtil.update : DataUtil.Inner_DataUtil.load_DataUtil_DataUtil);
      double r = Math.toRadians(yaw);
      this.init_V_Vec3(new Vec3(player.getX() - Math.sin(r) * 4.0, player.getEyePosition().y, player.getZ() + Math.cos(r) * 4.0));
   }

   private boolean init_Z_LocalPlayer_F(LocalPlayer player, float yaw) {
      if (Math.abs(Mth.wrapDegrees(yaw - this.init_DataUtil.init_F())) <= 1.0F) {
         return false;
      }

      this.set();
      this.init_V_LocalPlayer_Z(player, false);
      NodeWalker.Inner_StreamUtil s = init_NodeWalker_StreamU_LocalPlayer(player);
      NodeWalker.Inner_StreamUtil next = s;
      BitSetUtil world = this.init_BitSetUtil();

      for (int t = 0; t < 4 && next.onGround(); t++) {
         next = NodeWalker.init(next, NodeWalker.Inner_DataUtil.NONE, world);
      }

      this.init_Minecraft.options.keyShift.setDown(!player.onGround() || !next.onGround() || !(Math.abs(next.y() - s.y()) <= 0.02));
      return true;
   }

   private boolean init_Z_LocalPlayer(LocalPlayer player, float yaw, boolean sprint) {
      if (Math.abs(Mth.wrapDegrees(yaw - this.init_DataUtil.init_F())) <= 1.0F) {
         return false;
      }

      this.init_Minecraft.options.keyJump.setDown(false);
      BitSetUtil world = this.init_BitSetUtil();
      NodeWalker.Inner_StreamUtil state = init_NodeWalker_StreamU_LocalPlayer(player);
      int[] keys = init_IArr_F_F(this.init_DataUtil.init_F(), yaw);
      NodeWalker.Inner_DataUtil input = new NodeWalker.Inner_DataUtil(keys[0], -keys[1], false, sprint && keys[0] > 0, false, this.init_DataUtil.init_F());
      if (player.onGround() && init_Z_NodeWalker_CachePo_2(world, state, input)) {
         this.init_Minecraft.options.keyShift.setDown(false);
         this.init_V_LocalPlayer_Z(player, this.init_Z_F(yaw) && sprint);
      } else if (player.onGround() && init_Z_NodeWalker_CachePo_2(world, state, NodeWalker.Inner_DataUtil.NONE)) {
         this.set();
         this.init_V_LocalPlayer_Z(player, false);
      } else {
         this.init_V_LocalPlayer_2(player, yaw, world, state);
      }

      return true;
   }

   private BitSetUtil init_BitSetUtil() {
      if (this.init_BitSetUtil == null || this.init_BitSetUtil.init_ClientLevel() != this.init_Minecraft.level) {
         this.init_BitSetUtil = new BitSetUtil(this.init_Minecraft.level, this.init_Minecraft.player);
      }

      this.init_BitSetUtil.init_V();
      this.init_BitSetUtil.init(this.init_ClassCache_NetHelp.init_D);
      return this.init_BitSetUtil;
   }

   private static NodeWalker.Inner_StreamUtil init_NodeWalker_StreamU_LocalPlayer(LocalPlayer player) {
      return NodeWalker.Inner_StreamUtil.of(player);
   }

   private double init_D_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      return ClassCache.Inner_RenderState.init(m.to().getY(), this.load_ClassCache_FileCac_BlockPos(m.to().below()));
   }

   private double init_D_BlockPos(BlockPos stance) {
      ClassCache.Inner_FileCache floor = this.load_ClassCache_FileCac_BlockPos(stance.below());
      return floor.passable() ? stance.getY() : ClassCache.Inner_RenderState.init(stance.getY(), floor);
   }

   private static boolean read_Z_ClassCache_SyncMan(ClassCache.Inner_SyncManager type) {
      return type == ClassCache.Inner_SyncManager.put
         || type == ClassCache.Inner_SyncManager.add
         || type == ClassCache.Inner_SyncManager.clear
         || type == ClassCache.Inner_SyncManager.start;
   }

   private boolean update_Z_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      if (m != null && this.close_Z && read_Z_ClassCache_SyncMan(m.type())) {
         LocalPlayer p = this.init_Minecraft.player;
         return p != null && !p.onGround() && !p.isInWater() && !p.onClimbable();
      } else {
         return false;
      }
   }

   private void close(LocalPlayer player, ClassCache.UiHelper m) {
      if (!this.sync_Z(player, m)) {
         this.set();
         BlockPos cell = m.cellsToBreak().get(0);

         for (int i = 1; i < m.cellsToBreak().size(); i++) {
            BlockPos obstruction = m.cellsToBreak().get(i);
            if (!this.init_Minecraft.level.getBlockState(obstruction).isAir()) {
               this.init_V_Vec3(Vec3.atCenterOf(obstruction));
               switch (this.init_RefQueue_DataUtil.init_RefQueue_DataUtil__BlockPos(obstruction)) {
                  case init:
                  default:
                     break;
                  case load:
                     this.queue_I++;
                     this.init_RefQueue_DataUtil.init_V();
                     break;
                  case read:
                     this.init_V_Str("dig obstruction failed: " + this.init_RefQueue_DataUtil.init_Str());
               }

               return;
            }
         }

         double centreX = cell.getX() + 0.5;
         double centreZ = cell.getZ() + 0.5;
         this.init_V_List_List(m.cellsToBreak(), this.load_ClassCache_FileCac_BlockPos(cell).passable() ? List.of() : List.of(cell));
         if (this.load_ClassCache_FileCac_BlockPos(cell).passable()) {
            this.init_V_LocalPlayer(player, centreX, centreZ, 0.1);
            RefQueue.Inner_DataUtil.init_V_Minecraft(this.init_Minecraft);
         } else if (!this.open(cell)) {
            BlockPos standing = m.from();
            BlockState standingState = this.init_Minecraft.level.getBlockState(standing);
            if (!standingState.isAir()
               && standingState.getFluidState().isEmpty()
               && this.sync_ClassCache_UiHelpe != m
               && standingState.getCollisionShape(this.init_Minecraft.level, standing).isEmpty()
               && !standingState.getShape(this.init_Minecraft.level, standing).isEmpty()
               && standingState.getDestroySpeed(this.init_Minecraft.level, standing) >= 0.0F) {
               this.init_V_Vec3(Vec3.atCenterOf(standing));
               switch (this.init_RefQueue_DataUtil.init_RefQueue_DataUtil__BlockPos(standing)) {
                  case init:
                  default:
                     break;
                  case load:
                     this.init_RefQueue_DataUtil.init_V();
                     break;
                  case read:
                     this.init_RefQueue_DataUtil.init_V();
                     this.sync_ClassCache_UiHelpe = m;
               }
            } else {
               this.init_V_Vec3(Vec3.atCenterOf(cell));
               this.init_V_LocalPlayer(player, centreX, centreZ, 0.2);
               switch (this.init_RefQueue_DataUtil.init_RefQueue_DataUtil__BlockPos(cell)) {
                  case init:
                  default:
                     break;
                  case load:
                     this.queue_I++;
                     this.init_RefQueue_DataUtil.init_V();
                     break;
                  case read:
                     this.init_V_Str("dig failed: " + this.init_RefQueue_DataUtil.init_Str());
               }
            }
         }
      }
   }

   static boolean init_Z_BlockState(BlockState state) {
      return state.is(Blocks.DIRT)
         || state.is(Blocks.GRASS_BLOCK)
         || state.is(Blocks.MYCELIUM)
         || state.is(Blocks.PODZOL)
         || state.is(Blocks.DIRT_PATH)
         || state.is(Blocks.COARSE_DIRT)
         || state.is(Blocks.ROOTED_DIRT);
   }

   private boolean sync_Z(LocalPlayer player, ClassCache.UiHelper m) {
      if (m.from().getX() != this.frame || m.from().getZ() != this.block || m.from().getY() > this.chunk) {
         this.frame = m.from().getX();
         this.block = m.from().getZ();
         this.chunk = m.from().getY();
      }

      if (this.write_BlockPos != null) {
         if (!this.load_ClassCache_FileCac_BlockPos(this.write_BlockPos).passable()) {
            this.update_BlockPos = this.write_BlockPos;
            this.cache_Z = true;
            AutismClientAddon.LOG.info("[AI] capped the shaft at {}", this.write_BlockPos.toShortString());
            this.write_BlockPos = null;
            return false;
         }

         if (++this.level > 40) {
            this.write_BlockPos = null;
            return false;
         }

         this.set();
         RefQueue.Inner_DataUtil.init_V_Minecraft(this.init_Minecraft);
         if (TaskTimer.init_Z()) {
            TaskTimer.init_V("cap " + this.write_BlockPos.toShortString());
         }

         if (this.load_RefQueue_BufferHel_BlockPos(this.write_BlockPos) == RefQueue.Inner_BufferHelper.read) {
            this.write_BlockPos = null;
            return false;
         } else {
            return true;
         }
      } else if (player.onGround() && !player.isInWater() && this.poll_I() >= 0) {
         BlockPos feet = player.blockPosition();
         BlockPos cap = feet.above(2);
         if (!this.init_Z_BlockPos_BlockPos(feet, cap)) {
            return false;
         }

         int depth = this.init_I_BlockPos(feet);
         boolean hunted = depth >= 4 && this.load_Z_LocalPlayer(player);
         if (depth <= 8 && !hunted) {
            return false;
         }

         AutismClientAddon.LOG.info("[AI] capping the shaft at {}: {} deep{}", cap.toShortString(), depth, hunted ? ", a hostile after the player" : "");
         this.write_BlockPos = cap;
         this.level = 0;
         FileCache.read_V(cap);
         return this.sync_Z(player, m);
      } else {
         return false;
      }
   }

   private int init_I_BlockPos(BlockPos feet) {
      int dug = this.chunk - feet.getY();
      int ahead = 0;

      for (int i = this.tick_I; i < this.init_List.size(); i++) {
         ClassCache.UiHelper next = this.init_List.get(i);
         if (next.type() != ClassCache.Inner_SyncManager.push || next.to().getX() != this.frame || next.to().getZ() != this.block) {
            break;
         }

         ahead++;
      }

      return dug + Math.max(ahead, this.task);
   }

   private boolean load_Z_LocalPlayer(LocalPlayer player) {
      for (Mob mob : this.init_Minecraft.level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(24.0))) {
         if (mob.isAlive() && mob instanceof Enemy && mob.isAggressive()) {
            return true;
         }
      }

      return false;
   }

   private boolean init_Z_BlockPos_BlockPos(BlockPos feet, BlockPos cap) {
      BlockPos head = feet.above();
      if (this.load_ClassCache_FileCac_BlockPos(head).passable()
         && this.load_ClassCache_FileCac_BlockPos(cap).passable()
         && this.load_ClassCache_FileCac_BlockPos(cap.above()).passable()) {
         for (Direction side : Plane.HORIZONTAL) {
            if (this.load_ClassCache_FileCac_BlockPos(head.relative(side)).passable() || this.load_ClassCache_FileCac_BlockPos(cap.relative(side)).passable()) {
               return false;
            }
         }

         if (this.cache_Z
            && this.update_BlockPos != null
            && this.update_BlockPos.getY() > feet.getY()
            && Math.max(Math.abs(this.update_BlockPos.getX() - feet.getX()), Math.abs(this.update_BlockPos.getZ() - feet.getZ())) <= 6) {
            return false;
         }

         for (int i = this.tick_I; i < this.init_List.size(); i++) {
            BlockPos to = this.init_List.get(i).to();
            if (to.getX() == cap.getX() && to.getZ() == cap.getZ() && to.getY() >= cap.getY() - 1) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private void open(LocalPlayer player, ClassCache.UiHelper m) {
      if (!this.init_Z_LocalPlayer_3(player, m, true)) {
         Vec3 ahead = this.load_Vec3_LocalPlayer(player);
         if (ahead == null && this.init_Function != null && this.tick_I == this.init_List.size() - 1) {
            ahead = this.init_Function.apply(m.to());
         }

         RefQueue.Inner_DataUtil.init_V_Minecraft(this.init_Minecraft);
         this.init_V_LocalPlayer(
            player, m, false, false, ahead != null ? ahead : this.init_Vec3_Vec3(new Vec3(m.to().getX() + 0.5, player.getEyePosition().y, m.to().getZ() + 0.5))
         );
      }
   }

   private void poll(LocalPlayer player, ClassCache.UiHelper m) {
      if (!this.init_Z_LocalPlayer_3(player, m, true)) {
         RefQueue.Inner_DataUtil.init_V_Minecraft(this.init_Minecraft);
         Vec3 look = this.load_Vec3_LocalPlayer(player);
         if (look == null) {
            look = this.init_Vec3_LocalPlayer_ClassCache_UiHelpe(player, m);
         }

         if (!player.onGround() && !player.isInWater() && !player.onClimbable()) {
            this.init_V_Vec3(look);
            this.set();
            this.init_V_LocalPlayer_Z(player, false);
         } else {
            this.init_V_LocalPlayer(player, m, false, false, look);
         }
      }
   }

   private void push(LocalPlayer player, ClassCache.UiHelper m) {
      boolean headRoom = false;

      for (BlockPos c : m.cellsToBreak()) {
         if (c.getX() == m.from().getX() && c.getZ() == m.from().getZ() && !init_Z_ClassCache_FileCac(this.load_ClassCache_FileCac_BlockPos(c))) {
            headRoom = true;
         }
      }

      if (!this.init_Z_LocalPlayer_3(player, m, !headRoom)) {
         RefQueue.Inner_DataUtil.init_V_Minecraft(this.init_Minecraft);
         this.update_V_LocalPlayer_ClassCache_UiHelpe(player, m);
      }
   }

   private boolean init_Z_LocalPlayer_3(LocalPlayer player, ClassCache.UiHelper m, boolean walk) {
      List<BlockPos> cells = m.cellsToBreak();
      List<BlockPos> remaining = new ArrayList<>(cells.size());

      for (BlockPos c : cells) {
         if (!init_Z_ClassCache_FileCac(this.load_ClassCache_FileCac_BlockPos(c))) {
            remaining.add(c);
         }
      }

      this.init_V_List_List(cells, remaining);
      if (remaining.isEmpty()) {
         if (!this.init_Z_List(cells)) {
            return false;
         }

         this.set();
         return true;
      } else {
         BlockPos cell = null;
         Vec3 point = null;
         if (!this.init_DataUtil.init_Z()) {
            Vec3 eye = player.getEyePosition();
            double reach = player.blockInteractionRange();
            EventBus.Inner_CachePool shapes = this.init_EventBus_CachePool();
            Vec3 lineEye = walk && m.type() == ClassCache.Inner_SyncManager.stop ? init_Vec3_Vec3_ClassCache_UiHelpe(eye, m) : eye;
            this.init_EventBus_DataUtil = this.init_EventBus_DataUtil_ClassCache_UiHelpe(m, remaining, lineEye, player, shapes, reach);
            BlockPos led = this.init_EventBus_DataUtil == null ? null : EventBus.init(this.init_EventBus_DataUtil, eye, remaining, shapes, reach);
            if (led != null) {
               cell = led;
               point = EventBus.init(this.init_EventBus_DataUtil, eye, shapes, reach);
            }

            if (load_Z) {
               ListPool.init(
                  "dig line remaining="
                     + remaining
                     + " chain="
                     + (this.init_EventBus_DataUtil == null ? "-" : this.init_EventBus_DataUtil.chain())
                     + " pitch="
                     + (this.init_EventBus_DataUtil == null ? "-" : String.format(Locale.ROOT, "%.1f", EventBus.load(this.init_EventBus_DataUtil.direction())))
                     + " led="
                     + led
                     + " eye="
                     + String.format(Locale.ROOT, "%.2f,%.2f,%.2f", eye.x, eye.y, eye.z)
               );
            }
         }

         if (cell == null) {
            cell = remaining.get(0);
         }

         boolean alongLine = point != null;
         if (this.init_Minecraft.gameMode instanceof AutismMultiPlayerGameModeAccessor dig && dig.autism$getDestroyProgress() > 0.0F) {
            BlockPos busy = dig.autism$getDestroyBlockPos();
            if (busy != null && !busy.equals(cell) && remaining.contains(busy)) {
               BlockHitResult ray = DataUtil.init_BlockHitResult();
               BlockHitResult at = ray != null && ray.getBlockPos().equals(busy) ? ray : CachePool.init(this.init_Minecraft, busy);
               if (at != null) {
                  cell = busy.immutable();
                  point = at.getLocation();
                  alongLine = false;
               }
            }
         }

         BlockPos soft = this.init_BlockPos_LocalPlayer_BlockPos(player, cell);
         if (soft != null && this.flush_ClassCache_UiHelpe != m) {
            this.set();
            if (!this.init_RefQueue_DataUtil.init_Z()) {
               this.init_V_Vec3(Vec3.atCenterOf(soft));
            }

            switch (this.init_RefQueue_DataUtil.init_RefQueue_DataUtil__BlockPos(soft)) {
               case init:
               default:
                  break;
               case load:
                  this.init_RefQueue_DataUtil.init_V();
                  break;
               case read:
                  this.init_RefQueue_DataUtil.init_V();
                  this.flush_ClassCache_UiHelpe = m;
            }

            return true;
         } else {
            if (this.open(cell)) {
               this.set();
               return true;
            }

            this.state_Z = alongLine;
            double dx = m.to().getX() + 0.5 - player.getX();
            double dz = m.to().getZ() + 0.5 - player.getZ();
            boolean press = walk
               && dx * dx + dz * dz > 0.6889
               && (m.to().getY() >= m.from().getY() || !init_Z_ClassCache_FileCac(this.load_ClassCache_FileCac_BlockPos(m.to().above())));
            if (press) {
               this.init_Z_F(NodeWalker.init_F_D(player.getX(), player.getZ(), m.to().getX() + 0.5, m.to().getZ() + 0.5));
            } else {
               this.init_Minecraft.options.keyUp.setDown(false);
               this.init_Minecraft.options.keyDown.setDown(false);
               this.init_Minecraft.options.keyLeft.setDown(false);
               this.init_Minecraft.options.keyRight.setDown(false);
            }

            this.init_Minecraft.options.keyJump.setDown(false);
            this.init_Minecraft.options.keyShift.setDown(player.onClimbable() && !player.onGround());
            this.init_V_LocalPlayer_Z(player, false);

            try {
               switch (this.init_RefQueue_DataUtil.init_RefQueue_DataUtil__BlockPos_Vec3(cell, point)) {
                  case init:
                  default:
                     break;
                  case load:
                     this.init_RefQueue_DataUtil.init_V();
                     boolean solid = false;

                     for (BlockPos c : cells) {
                        if (!init_Z_ClassCache_FileCac(this.load_ClassCache_FileCac_BlockPos(c))) {
                           solid = true;
                           break;
                        }
                     }

                     if (!solid && !this.init_Z_List(cells)) {
                        return false;
                     }
                     break;
                  case read:
                     this.init_V_Str("dig failed: " + this.init_RefQueue_DataUtil.init_Str());
               }
            } finally {
               this.state_Z = false;
            }

            return true;
         }
      }
   }

   private BlockPos init_BlockPos_LocalPlayer_BlockPos(LocalPlayer player, BlockPos cell) {
      if (CachePool.init(this.init_Minecraft, cell) != null) {
         return null;
      }

      Vec3 eye = player.getEyePosition();
      BlockHitResult hit = this.init_Minecraft
         .level
         .clip(new ClipContext(eye, Vec3.atCenterOf(cell), net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, player));
      if (hit.getType() != Type.BLOCK) {
         return null;
      }

      BlockPos in = hit.getBlockPos().immutable();
      if (!in.equals(cell) && !(eye.distanceToSqr(hit.getLocation()) > player.blockInteractionRange() * player.blockInteractionRange())) {
         BlockState state = this.init_Minecraft.level.getBlockState(in);
         if (!state.isAir() && state.getFluidState().isEmpty() && !state.hasBlockEntity() && !(state.getDestroySpeed(this.init_Minecraft.level, in) < 0.0F)) {
            Block block = state.getBlock();
            if (!load_Z_BlockPos(in)
               && (state.canBeReplaced() || block instanceof VegetationBlock || block instanceof LeafLitterBlock || block instanceof SnowLayerBlock)) {
               return state.getCollisionShape(this.init_Minecraft.level, in).isEmpty() ? in : null;
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static void init_V_LongPredicate(LongPredicate cells) {
      init_LongPredicate = cells;
      write_J = System.nanoTime() + 3000000000L;
   }

   public static void flush_V() {
      init_LongPredicate = key -> false;
      write_J = 0L;
   }

   static boolean load_Z_BlockPos(BlockPos cell) {
      return System.nanoTime() < write_J && init_LongPredicate.test(cell.asLong());
   }

   public static void init_V_RefQueue_ConfigSto(RefQueue.Inner_ConfigStore source) {
      init_RefQueue_ConfigSto = source;
   }

   private static void init_V_BlockPos_Block(BlockPos cell, Block block) {
      RefQueue.Inner_ConfigStore source = init_RefQueue_ConfigSto;
      if (source == null || source.init_Z(cell)) {
         a.init(cell, block);
      }
   }

   private int load_I_BlockPos(BlockPos cell) {
      RefQueue.Inner_ConfigStore source = init_RefQueue_ConfigSto;
      if (source != null && cell != null && this.init_Minecraft.player != null) {
         if (!source.load(cell)) {
            return -1;
         }

         Item wanted = source.init_Item(cell);
         if (wanted != null) {
            for (int i = 0; i < 9; i++) {
               if (this.init_Minecraft.player.getInventory().getItem(i).is(wanted)) {
                  return i;
               }
            }

            return -1;
         }
      }

      return this.poll_I();
   }

   private Item init_Item() {
      RefQueue.Inner_ConfigStore source = init_RefQueue_ConfigSto;
      return source != null && this.init_ClassCache_UiHelpe != null && !this.init_ClassCache_UiHelpe.cellsToPlace().isEmpty()
         ? source.init_Item(this.init_ClassCache_UiHelpe.cellsToPlace().get(0))
         : null;
   }

   private boolean sync(ClassCache.UiHelper m) {
      return !this.init_DataUtil.init_Z()
         && !m.cellsToBreak().isEmpty()
         && (m.type() == ClassCache.Inner_SyncManager.pop || m.type() == ClassCache.Inner_SyncManager.stop || m.type() == ClassCache.Inner_SyncManager.next);
   }

   private Vec3 init_Vec3_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      LocalPlayer player = this.init_Minecraft.player;
      if (player == null) {
         return null;
      }

      List<BlockPos> remaining = new ArrayList<>();

      for (BlockPos c : m.cellsToBreak()) {
         if (!init_Z_ClassCache_FileCac(this.load_ClassCache_FileCac_BlockPos(c))) {
            remaining.add(c);
         }
      }

      if (remaining.isEmpty()) {
         return null;
      }

      Vec3 eye = player.getEyePosition();
      double reach = player.blockInteractionRange();
      EventBus.Inner_CachePool shapes = this.init_EventBus_CachePool();
      this.init_EventBus_DataUtil = this.init_EventBus_DataUtil_ClassCache_UiHelpe(m, remaining, eye, player, shapes, reach);
      return this.init_EventBus_DataUtil == null ? null : EventBus.init(this.init_EventBus_DataUtil, eye, shapes, reach);
   }

   private EventBus.Inner_DataUtil init_EventBus_DataUtil_ClassCache_UiHelpe(
      ClassCache.UiHelper m, List<BlockPos> remaining, Vec3 eye, LocalPlayer player, EventBus.Inner_CachePool shapes, double reach
   ) {
      List<BlockPos> targets = new ArrayList<>(remaining);
      targets.addAll(this.init_List_List(remaining));
      Set<BlockPos> cells = Set.copyOf(targets);
      boolean changed = !cells.equals(this.init_Set);
      this.init_Set = cells;
      if (changed
         && m.equals(this.close_ClassCache_UiHelpe)
         && this.load_EventBus_DataUtil != null
         && cells.equals(this.load_Set)
         && EventBus.init(this.load_EventBus_DataUtil, eye, remaining, shapes, reach) != null) {
         this.close_ClassCache_UiHelpe = null;
         return this.load_EventBus_DataUtil;
      } else {
         return EventBus.init_EventBus_DataUtil_EventBus_DataUtil(
            this.init_EventBus_DataUtil, changed, eye, player.getYRot(), player.getXRot(), remaining, targets, shapes, reach, this.init_F()
         );
      }
   }

   static Vec3 init_Vec3_Vec3_ClassCache_UiHelpe(Vec3 eye, ClassCache.UiHelper m) {
      int dx = Integer.signum(m.to().getX() - m.from().getX());
      int dz = Integer.signum(m.to().getZ() - m.from().getZ());
      if (dx != 0 == (dz != 0)) {
         return eye;
      }

      double cx = m.from().getX() + 0.5;
      double cz = m.from().getZ() + 0.5;
      double along = (eye.x - cx) * dx + (eye.z - cz) * dz;
      return along >= 0.199 ? eye : new Vec3(dx != 0 ? cx + dx * 0.2 : eye.x, eye.y, dz != 0 ? cz + dz * 0.2 : eye.z);
   }

   private float init_F() {
      if (this.tick_I >= this.init_List.size()) {
         return 0.0F / 0.0F;
      }

      BlockPos a = this.init_List.get(this.tick_I).from();
      BlockPos b = this.init_List.get(Math.min(this.init_List.size() - 1, this.tick_I + 3)).to();
      return Math.hypot(b.getX() - a.getX(), b.getZ() - a.getZ()) < 1.5 ? 0.0F / 0.0F : NodeWalker.init_F_D(a.getX(), a.getZ(), b.getX(), b.getZ());
   }

   private List<BlockPos> init_List_List(List<BlockPos> remaining) {
      int at = -1;

      for (int i = this.tick_I; i < Math.min(this.init_List.size(), this.tick_I + 3) && at < 0; i++) {
         if (!Collections.disjoint(this.init_List.get(i).cellsToBreak(), remaining)) {
            at = i;
         }
      }

      if (at >= 0 && at + 1 < this.init_List.size()) {
         ClassCache.UiHelper next = this.init_List.get(at + 1);
         if (next.type() != ClassCache.Inner_SyncManager.pop
            && next.type() != ClassCache.Inner_SyncManager.stop
            && next.type() != ClassCache.Inner_SyncManager.next) {
            return List.of();
         }

         List<BlockPos> out = new ArrayList<>();

         for (BlockPos c : next.cellsToBreak()) {
            if (!init_Z_ClassCache_FileCac(this.load_ClassCache_FileCac_BlockPos(c)) && !remaining.contains(c)) {
               out.add(c);
            }
         }

         return out;
      } else {
         return List.of();
      }
   }

   private void init_V_List_List(List<BlockPos> cells, List<BlockPos> remaining) {
      for (BlockPos c : cells) {
         if (remaining.contains(c)) {
            this.read_Set.add(c);
         } else if (this.read_Set.remove(c)) {
            this.queue_I++;
         }
      }
   }

   private EventBus.Inner_CachePool init_EventBus_CachePool() {
      return pos -> this.init_Minecraft.level.getBlockState(pos).getShape(this.init_Minecraft.level, pos);
   }

   private Vec3 init_Vec3_LocalPlayer(LocalPlayer player) {
      if (!this.init_DataUtil.init_Z() && this.tick_I + 1 < this.init_List.size()) {
         ClassCache.UiHelper next = this.init_List.get(this.tick_I + 1);
         if (next.cellsToBreak().isEmpty()) {
            return null;
         }

         List<BlockPos> remaining = new ArrayList<>();

         for (BlockPos c : next.cellsToBreak()) {
            if (!init_Z_ClassCache_FileCac(this.load_ClassCache_FileCac_BlockPos(c))) {
               remaining.add(c);
            }
         }

         return remaining.isEmpty() ? null : this.init_Vec3_LocalPlayer(player, next, remaining);
      } else {
         return null;
      }
   }

   private Vec3 load_Vec3_LocalPlayer(LocalPlayer player) {
      Vec3 ahead = this.init_Vec3_LocalPlayer(player);
      if (ahead == null) {
         return null;
      }

      float pitch = EventBus.load(ahead.subtract(player.getEyePosition()));
      if (Math.abs(pitch) > 70.0F) {
         ahead = player.getEyePosition().add(Vec3.directionFromRotation(pitch, player.getYRot()).scale(3.0));
      }

      return ahead;
   }

   private Vec3 init_Vec3_LocalPlayer(LocalPlayer player, ClassCache.UiHelper dig, List<BlockPos> remaining) {
      Vec3 eye = read_Vec3_LocalPlayer_ClassCache_UiHelpe(player, dig);
      double reach = player.blockInteractionRange();
      EventBus.Inner_CachePool shapes = this.init_EventBus_CachePool();
      List<BlockPos> targets = new ArrayList<>(remaining);
      targets.addAll(this.init_List_List(remaining));
      Set<BlockPos> cells = Set.copyOf(targets);
      if (!dig.equals(this.close_ClassCache_UiHelpe)
         || !cells.equals(this.load_Set)
         || this.load_EventBus_DataUtil == null
         || EventBus.init(this.load_EventBus_DataUtil, eye, remaining, shapes, reach) == null) {
         this.load_EventBus_DataUtil = EventBus.init_EventBus_DataUtil_Vec3(
            eye, player.getYRot(), player.getXRot(), remaining, targets, shapes, reach, this.init_F()
         );
         this.close_ClassCache_UiHelpe = dig;
         this.load_Set = cells;
      }

      return this.load_EventBus_DataUtil == null ? null : init_Vec3_LocalPlayer_3(player, eye, EventBus.init(this.load_EventBus_DataUtil, eye, shapes, reach));
   }

   private static Vec3 read_Vec3_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper dig) {
      return new Vec3(dig.from().getX() + 0.5, dig.from().getY() + player.getEyeHeight(), dig.from().getZ() + 0.5);
   }

   private static Vec3 init_Vec3_LocalPlayer_3(LocalPlayer player, Vec3 stance, Vec3 point) {
      if (point == null) {
         return null;
      }

      ce.Inner_DataUtil look = ce.init(point, stance);
      float yaw = Math.abs(look.load()) > 70.0F ? player.getYRot() : look.init();
      return player.getEyePosition().add(Vec3.directionFromRotation(look.load(), yaw).scale(Math.max(2.0, point.distanceTo(stance))));
   }

   private static boolean init_Z_ClassCache_FileCac(ClassCache.Inner_FileCache p) {
      return p.passable() && !p.web();
   }

   private boolean init_Z_List(List<BlockPos> cells) {
      for (Entity entity : this.init_Minecraft.level.entitiesForRendering()) {
         if (entity instanceof FallingBlockEntity) {
            BlockPos at = entity.blockPosition();

            for (BlockPos c : cells) {
               if (at.getX() == c.getX() && at.getZ() == c.getZ() && at.getY() >= c.getY() - 1 && at.getY() <= c.getY() + 8) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private void pop(LocalPlayer player, ClassCache.UiHelper m) {
      BlockPos cell = m.cellsToPlace().get(0);
      if (this.open_Z || m.cellsToBreak().isEmpty() || !player.onGround() || !this.init_Z_LocalPlayer_3(player, m, false)) {
         if (!this.open_Z) {
            double dx = cell.getX() + 0.5 - player.getX();
            double dz = cell.getZ() + 0.5 - player.getZ();
            this.set();
            this.init_Minecraft.options.keyShift.setDown(true);
            if (!player.onGround()) {
               return;
            }

            if (dx * dx + dz * dz > 0.04) {
               this.init_DataUtil.init_V_ce_DataUtil(init_ce_DataUtil_LocalPlayer(player));
               this.init_V_LocalPlayer(player, cell.getX() + 0.5, cell.getZ() + 0.5, 0.08);
               return;
            }

            if (!init_Z_D_2(player.getY(), cell.getY(), m.repairFarmland())
               && (!this.read_Z_LocalPlayer(player) || !init_Z_D_2(player.getY() + 0.125, cell.getY(), false))) {
               this.init_V_Str("pillar takeoff is below the new support");
               return;
            }

            Vec3 velocity = player.getDeltaMovement();
            if (velocity.x * velocity.x + velocity.z * velocity.z > 0.0064) {
               return;
            }

            if (this.load_I_BlockPos(cell) < 0 && (this.tick_Z() || this.run_Z())) {
               return;
            }

            this.init_DataUtil.init_V_ce_DataUtil(init_ce_DataUtil_LocalPlayer(player));
            this.init_DataUtil.load_V();
            if (this.init_DataUtil.init_D() > 6.0) {
               return;
            }

            this.open_Z = true;
         }

         this.init_DataUtil.init_V_ce_DataUtil(init_ce_DataUtil_LocalPlayer(player));
         this.init_Minecraft.options.keyJump.setDown(true);
         this.init_Minecraft.options.keyUp.setDown(false);
         this.init_V_LocalPlayer_Z(player, false);
         this.init_Minecraft.options.keyShift.setDown(false);
         if (this.update_Z) {
            this.init_Minecraft.options.keyJump.setDown(false);
         } else if (this.close(cell)) {
            this.update_Z = true;
            this.init_Minecraft.options.keyJump.setDown(false);
            if (this.init_BlockPos != null) {
               this.buffer_I++;
               init_V_BlockPos_Block(cell, this.init_Block);
               FileCache.write_V(cell);
               AutismClientAddon.LOG
                  .info(
                     "[AI] placed {} at {} (pillar, {} -> {})",
                     this.init_Block,
                     cell.toShortString(),
                     this.init_ClassCache_UiHelpe == null ? "-" : this.init_ClassCache_UiHelpe.from().toShortString(),
                     this.init_ClassCache_UiHelpe == null ? "-" : this.init_ClassCache_UiHelpe.to().toShortString()
                  );
            }
         } else if (this.init_BlockPos != null) {
            if (++this.set_I > 6) {
               this.init_V_Str("pillar block never confirmed");
            }
         } else if (player.getY() > cell.getY() + 1.01) {
            if (this.get_I >= 6) {
               this.init_V_Str("pillar place attempts used up");
               return;
            }

            RefQueue.Inner_BufferHelper attempt = this.init_RefQueue_BufferHel_BlockPos(cell);
            if (attempt == RefQueue.Inner_BufferHelper.init) {
               this.init_BlockPos = cell;
            }

            if (attempt != RefQueue.Inner_BufferHelper.load) {
               this.get_I++;
            }
         }
      }
   }

   private static ce.Inner_DataUtil init_ce_DataUtil_LocalPlayer(LocalPlayer player) {
      return new ce.Inner_DataUtil(player.getYRot(), 90.0F);
   }

   private boolean read_Z_LocalPlayer(LocalPlayer player) {
      BlockPos below = BlockPos.containing(player.getX(), player.getY() - 0.01, player.getZ());
      return this.init_Minecraft.level.getBlockState(below).is(Blocks.SOUL_SAND);
   }

   static boolean init_Z_D_2(double feetY, int placementY, boolean repairFarmland) {
      return feetY >= placementY - (repairFarmland ? 0.0625 : 0.0) - 0.01;
   }

   private RefQueue.Inner_BufferHelper init_RefQueue_BufferHel_BlockPos(BlockPos cell) {
      int slot = this.load_I_BlockPos(cell);
      if (slot < 0) {
         return this.load_RefQueue_BufferHel_BlockPos(cell);
      }

      if (this.init_DataUtil.init_D() > 1.0) {
         return RefQueue.Inner_BufferHelper.load;
      }

      HitResult ray = this.init_Minecraft.player.pick(this.init_Minecraft.player.blockInteractionRange(), 1.0F, false);
      if (ray instanceof BlockHitResult hit && ray.getType() == Type.BLOCK && hit.getBlockPos().equals(cell.below()) && hit.getDirection() == Direction.UP) {
         if (U.init_Z(this.init_Minecraft.level.getBlockState(hit.getBlockPos()), hit.getBlockPos())) {
            this.init_Minecraft.options.keyShift.setDown(true);
            if (!this.init_Minecraft.player.isShiftKeyDown() || !this.init_Minecraft.player.getLastSentInput().shift()) {
               return RefQueue.Inner_BufferHelper.load;
            }
         }

         this.load_V_I(slot);
         ItemStack stack = this.init_Minecraft.player.getInventory().getItem(slot);
         this.init_Str = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
         this.read_J = ca.init_J_BlockPos(cell);
         this.init_Block = ((BlockItem)stack.getItem()).getBlock();
         if (init_Z) {
            ListPool.init("place click cell=" + cell + " via=" + hit.getBlockPos() + "/UP (straight down) rev=" + this.read_J);
         }

         InteractionResult placementResult = this.init_Minecraft.gameMode.useItemOn(this.init_Minecraft.player, InteractionHand.MAIN_HAND, hit);
         if (placementResult.consumesAction() && !this.init_Minecraft.level.getBlockState(cell).isAir()) {
            init_V_BlockPos_Block(cell, this.init_Block);
         }

         if (init_Z) {
            ListPool.init(
               "pillar use "
                  + placementResult
                  + " secondary="
                  + this.init_Minecraft.player.isSecondaryUseActive()
                  + " shift="
                  + this.init_Minecraft.player.isShiftKeyDown()
                  + " key="
                  + this.init_Minecraft.options.keyShift.isDown()
            );
         }

         return RefQueue.Inner_BufferHelper.init;
      } else {
         return this.load_RefQueue_BufferHel_BlockPos(cell);
      }
   }

   private boolean push_Z() {
      if (!this.init_DataUtil.init_Z() && this.init_ClassCache_UiHelpe != null) {
         int dx = this.init_ClassCache_UiHelpe.to().getX() - this.init_ClassCache_UiHelpe.from().getX();
         int dz = this.init_ClassCache_UiHelpe.to().getZ() - this.init_ClassCache_UiHelpe.from().getZ();
         return this.init_ClassCache_UiHelpe.to().getY() == this.init_ClassCache_UiHelpe.from().getY()
            && Math.abs(dx) + Math.abs(dz) == 1
            && this.init_ClassCache_UiHelpe.cellsToPlace().size() == 1
            && this.init_ClassCache_UiHelpe.cellsToPlace().get(0).equals(this.init_ClassCache_UiHelpe.to().below());
      } else {
         return false;
      }
   }

   private void init_V_LocalPlayer(LocalPlayer player) {
      ClassCache.UiHelper first = this.init_ClassCache_UiHelpe;
      int dx = first.to().getX() - first.from().getX();
      int dz = first.to().getZ() - first.from().getZ();

      int end;
      for (end = this.tick_I; end + 1 < this.init_List.size(); end++) {
         ClassCache.UiHelper next = this.init_List.get(end + 1);
         if (next.type() != ClassCache.Inner_SyncManager.run
            || !next.from().equals(this.init_List.get(end).to())
            || next.to().getX() - next.from().getX() != dx
            || next.to().getZ() - next.from().getZ() != dz
            || next.to().getY() != first.from().getY()) {
            break;
         }
      }

      int floorY = first.from().getY() - 1;
      int length = end - this.tick_I + 1;
      this.fill = end;
      int farSide = end + 1 < this.init_List.size() ? this.init_I_ClassCache_UiHelpe(this.init_List.get(end + 1), first.from(), dx, dz, length) : -1;
      if (farSide > length) {
         length = farSide;
         end++;
      }

      JsonUtil.Inner_CachePool lane = new JsonUtil.Inner_CachePool(first.from().getX(), first.from().getZ(), floorY, dx, dz, length);
      JsonUtil.Inner_ConfigStore style = NetHelper.init_JsonUtil_ConfigSto_Z(!this.stop_Z, lane, player, this.open_I());
      boolean turning = end + 1 < this.init_List.size() && init_Z_ClassCache_UiHelpe_3(this.init_List.get(end + 1), dx, dz);
      if (lane.length() <= 2) {
         style = JsonUtil.Inner_ConfigStore.load;
      }

      this.init_NetHelper = new NetHelper(lane, style, this.init_DataUtil, turning);
      this.init_ArrayDeque.clear();
      this.table = this.tick_I;
      this.store = end;
      double budget = 0.0;

      for (int i = this.tick_I; i <= end; i++) {
         budget += ClassCache.Inner_CachePool.init_I_D(this.init_List.get(i).costTicks());
      }

      this.drain = (int)Math.ceil(budget) + 60;
      AutismClientAddon.LOG
         .info("[AI] bridge {} cells {} from {} toward {},{}", lane.length(), style.name().toLowerCase(Locale.ROOT), first.from().toShortString(), dx, dz);
   }

   private int init_I_ClassCache_UiHelpe(ClassCache.UiHelper jump, BlockPos rim, int dx, int dz, int length) {
      if (read_Z_ClassCache_SyncMan(jump.type()) && jump.to().getY() == rim.getY()) {
         int ax = jump.to().getX() - rim.getX();
         int az = jump.to().getZ() - rim.getZ();
         int ahead = ax * dx + az * dz;
         int across = Math.abs(ax * dz - az * dx);
         if (ahead > length && across <= 1) {
            for (int i = length + 1; i <= ahead; i++) {
               BlockPos floor = new BlockPos(rim.getX() + dx * i, rim.getY() - 1, rim.getZ() + dz * i);
               ClassCache.Inner_FileCache cell = this.load_ClassCache_FileCac_BlockPos(floor);
               if (!cell.loaded()) {
                  return -1;
               }

               if (!cell.passable()) {
                  return cell.top16() >= 16 ? i - 1 : -1;
               }

               if (cell.water()
                  || cell.lava()
                  || !this.load_ClassCache_FileCac_BlockPos(floor.above()).passable()
                  || !this.load_ClassCache_FileCac_BlockPos(floor.above(2)).passable()) {
                  return -1;
               }
            }

            return -1;
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   private boolean write_Z_LocalPlayer(LocalPlayer player) {
      if (this.init_NetHelper == null) {
         this.init_V_LocalPlayer(player);
      }

      JsonUtil.Inner_CachePool lane = this.init_NetHelper.init_JsonUtil_CachePool();
      if (this.init_NetHelper.init_I() > this.drain) {
         this.init_V_Str("bridge over its time budget");
         return true;
      }

      int slot = this.load_I_BlockPos(this.init_NetHelper.init_BlockPos());
      if (slot < 0 && player.onGround() && this.init_NetHelper.init_JsonUtil().init_I_NodeWalker_CachePo(this.init_BitSetUtil()) <= lane.length()) {
         this.set();
         this.init_Minecraft.options.keyShift.setDown(true);
         if (this.init_AiOperation != null) {
            this.pop_V();
            return true;
         } else if (this.init_UtilCore != null && this.init_UtilCore.init_Z()) {
            this.tick_V();
            return true;
         } else if (!this.tick_Z() && !this.run_Z()) {
            this.init_V_Str("out of blocks mid-bridge");
            return true;
         } else {
            return true;
         }
      } else {
         NetHelper.Inner_DataUtil step = this.init_NetHelper
            .init(
               slot,
               this::load_V_I,
               cell -> {
                  this.entry++;
                  ItemStack stack = player.getInventory().getItem(slot);
                  this.init_Str = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                  Block block = this.init_Minecraft.level.getBlockState(cell).getBlock();
                  init_V_BlockPos_Block(cell, block);
                  FileCache.write_V(cell);
                  AutismClientAddon.LOG
                     .info(
                        "[AI] placed {} at {} (bridge {})",
                        block,
                        cell.toShortString(),
                        this.init_NetHelper.init_JsonUtil().init_JsonUtil_ConfigSto().name().toLowerCase(Locale.ROOT)
                     );
               }
            );
         if (step == NetHelper.Inner_DataUtil.write) {
            this.init_V_Str("bridge footing below the planned support level");
            return true;
         }

         if (step == NetHelper.Inner_DataUtil.read) {
            if (init_Z) {
               ListPool.init("bridge fell at " + player.position());
            }

            this.init_V_Str("fell off the bridge");
            return true;
         } else {
            double u = lane.along(player.getX(), player.getZ());

            while (
               this.tick_I < this.fill
                  && u >= this.tick_I - this.table + 1
                  && !this.load_ClassCache_FileCac_BlockPos(this.init_List.get(this.tick_I).cellsToPlace().get(0)).passable()
            ) {
               this.tick_I++;
            }

            this.init_ClassCache_UiHelpe = this.init_List.get(this.tick_I);
            if (step != NetHelper.Inner_DataUtil.load) {
               return true;
            }

            if (init_Z) {
               ListPool.init("bridge done in " + this.init_NetHelper.init_I() + " ticks at " + player.position());
            }

            this.get_D = this.get_D + this.init_List.get(this.store).costTicks();
            this.tick_I = this.store + 1;
            this.init_ClassCache_UiHelpe = null;
            this.init_NetHelper = null;
            this.init_ArrayDeque.clear();

            while (this.tick_I < this.init_List.size() && init_Z_ClassCache_UiHelpe_LocalPlayer(this.init_List.get(this.tick_I), player)) {
               this.get_D = this.get_D + this.init_List.get(this.tick_I++).costTicks();
            }

            return false;
         }
      }
   }

   private static boolean init_Z_ClassCache_UiHelpe_LocalPlayer(ClassCache.UiHelper m, LocalPlayer player) {
      if (push(m) && !(Math.abs(player.getY() - m.to().getY()) > 0.6)) {
         double dx = m.to().getX() - m.from().getX();
         double dz = m.to().getZ() - m.from().getZ();
         double length = Math.hypot(dx, dz);
         if (length < 1.0E-6) {
            return false;
         }

         double rx = player.getX() - (m.to().getX() + 0.5);
         double rz = player.getZ() - (m.to().getZ() + 0.5);
         return (rx * dx + rz * dz) / length > -0.3 && Math.abs(rx * dz - rz * dx) / length < 1.5;
      } else {
         return false;
      }
   }

   private int open_I() {
      int count = 0;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = this.init_Minecraft.player.getInventory().getItem(i);
         if (stack.getItem() instanceof BlockItem block
            && !(block.getBlock() instanceof FallingBlock)
            && block.getBlock().defaultBlockState().isCollisionShapeFullBlock(this.init_Minecraft.level, this.init_Minecraft.player.blockPosition())
            && !block.getBlock().defaultBlockState().hasBlockEntity()) {
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            if (UtilCore.init_I(id) >= 0 && UtilCore.init(stack, this.init_Minecraft.player.getInventory())) {
               count += stack.getCount();
            }
         }
      }

      return count;
   }

   private void tick(LocalPlayer player, ClassCache.UiHelper m) {
      BlockPos support = m.cellsToPlace().get(0);
      if (!this.load_ClassCache_FileCac_BlockPos(support).passable()) {
         if (!this.update_Z) {
            this.update_Z = true;
            if (this.init_BlockPos != null) {
               this.entry++;
               init_V_BlockPos_Block(support, this.init_Block);
               FileCache.write_V(support);
               AutismClientAddon.LOG
                  .info(
                     "[AI] placed {} at {} ({}, {} -> {})",
                     this.init_Block,
                     support.toShortString(),
                     this.init_ClassCache_UiHelpe == null ? "-" : this.init_ClassCache_UiHelpe.type(),
                     this.init_ClassCache_UiHelpe == null ? "-" : this.init_ClassCache_UiHelpe.from().toShortString(),
                     this.init_ClassCache_UiHelpe == null ? "-" : this.init_ClassCache_UiHelpe.to().toShortString()
                  );
               this.init_BlockPos = null;
            }
         }
      } else if (this.update_Z) {
         this.update_Z = false;
      }

      if (this.close(player)) {
         if (this.update_Z) {
            this.init_V_Vec3(this.init_Vec3_Vec3(new Vec3(m.to().getX() + 0.5, player.getEyePosition().y, m.to().getZ() + 0.5)));
         } else if (!this.sync_Z) {
            this.init_V_Vec3(Vec3.atCenterOf(support));
         }

         if (!this.update_Z && player.getY() < m.from().getY() - 0.5 && !player.onGround()) {
            if (init_Z) {
               ListPool.init("bridge slipped cell=" + support + " creep " + this.sync_Z + " pending " + this.init_BlockPos);
            }

            this.init_V_Str("slipped into the gap before the support landed");
         } else {
            if (!this.update_Z && this.init_BlockPos != null && ++this.set_I > 6) {
               if (this.write_Z) {
                  this.init_V_Str("bridge click never applied");
                  return;
               }

               this.set_I = 0;
               RefQueue.Inner_BufferHelper retry = this.load_RefQueue_BufferHel_BlockPos(support);
               if (retry == RefQueue.Inner_BufferHelper.init) {
                  this.write_Z = true;
               } else if (retry == RefQueue.Inner_BufferHelper.read) {
                  this.init_V_Str("bridge place impossible");
                  return;
               }
            }

            if (!this.update_Z && this.init_BlockPos == null) {
               RefQueue.Inner_BufferHelper attempt = this.load_RefQueue_BufferHel_BlockPos(support);
               if (init_Z && this.sync_Z) {
                  ListPool.init(
                     String.format(
                        Locale.ROOT,
                        "bridge creep tick %d attempt %s eye %.3f edge %.3f ground %s",
                        this.run_I,
                        attempt,
                        player.getEyePosition().x,
                        init_D_LocalPlayer_ClassCache_UiHelpe(player, m),
                        player.onGround()
                     )
                  );
               }

               if (attempt == RefQueue.Inner_BufferHelper.init) {
                  this.init_BlockPos = support;
                  this.sync_Z = false;
               } else if (attempt == RefQueue.Inner_BufferHelper.read) {
                  if (player.onGround() && init_D_LocalPlayer_ClassCache_UiHelpe(player, m) > -0.45) {
                     if (!this.sync_Z && init_Z) {
                        ListPool.init("place creep engage cell=" + support + " edge=" + init_D_LocalPlayer_ClassCache_UiHelpe(player, m));
                     }

                     this.sync_Z = true;
                  } else {
                     this.get_I++;
                  }
               }

               if (this.get_I >= 6) {
                  if (init_Z) {
                     ListPool.init("telly place attempts-exhausted cell=" + support + " attempts=" + this.get_I);
                  }

                  this.init_V_Str("bridge place attempts used up");
                  return;
               }
            }

            boolean rimHold = !this.update_Z
               && this.init_BlockPos == null
               && (player.onGround() || player.isInWater())
               && init_D_LocalPlayer_ClassCache_UiHelpe(player, m) <= 0.55
               && (!this.sync_Z || !player.onGround());
            this.init_Minecraft.options.keyUp.setDown(!rimHold);
            this.init_V_LocalPlayer_Z(player, !rimHold && !this.sync_Z);
            this.init_Minecraft.options.keyJump.setDown(false);
            this.init_Minecraft
               .options
               .keyShift
               .setDown(
                  rimHold
                     || this.sync_Z
                     || TaskQueue.init(
                        player.onGround(),
                        this.init_BlockPos != null && player.blockPosition().equals(m.to()),
                        !this.load_ClassCache_FileCac_BlockPos(support).passable()
                     )
               );
         }
      } else if (this.update_Z) {
         this.init_Minecraft.options.keyShift.setDown(false);
         this.init_V_LocalPlayer(player, m, false, false, this.init_Vec3_Vec3(new Vec3(m.to().getX() + 0.5, player.getEyePosition().y, m.to().getZ() + 0.5)));
      } else {
         this.init_V_Vec3(Vec3.atCenterOf(support));
         this.init_Minecraft.options.keyShift.setDown(true);
         this.init_Minecraft.options.keyJump.setDown(false);
         this.init_V_LocalPlayer_Z(player, false);
         if (!player.onGround() && player.getY() < m.from().getY() - 0.5) {
            this.init_V_Str("slipped into the gap before the support landed");
         } else if (this.init_BlockPos != null) {
            if (++this.set_I > 6) {
               this.init_V_Str("bridge block never confirmed");
            }
         } else {
            double distance = player.getEyePosition().distanceTo(Vec3.atCenterOf(support));
            boolean rimHold = !this.update_Z && this.init_BlockPos == null && player.onGround() && init_D_LocalPlayer_ClassCache_UiHelpe(player, m) <= 0.55;
            this.init_Minecraft.options.keyUp.setDown(!rimHold && distance > 1.8);
            if (rimHold) {
               this.init_Minecraft.options.keyUp.setDown(false);
               this.init_Minecraft.options.keyShift.setDown(true);
            }

            if (!(distance > 4.4)) {
               if (this.get_I >= 6) {
                  if (init_Z) {
                     ListPool.init("place attempts-exhausted cell=" + support + " attempts=" + this.get_I);
                  }

                  this.init_V_Str("no face to place the bridge block on");
               } else {
                  RefQueue.Inner_BufferHelper attempt = this.load_RefQueue_BufferHel_BlockPos(support);
                  if (attempt == RefQueue.Inner_BufferHelper.init) {
                     this.init_BlockPos = support;
                  }

                  if (attempt != RefQueue.Inner_BufferHelper.load) {
                     this.get_I++;
                  }
               }
            }
         }
      }
   }

   private void run(LocalPlayer player, ClassCache.UiHelper m) {
      BlockPos cell = m.to();
      boolean closed = this.sync(cell);
      int pdx = Integer.signum(m.to().getX() - m.from().getX());
      int pdz = Integer.signum(m.to().getZ() - m.from().getZ());
      Vec3 past = new Vec3(m.to().getX() + 0.5 + pdx * 1.2, player.getEyePosition().y, m.to().getZ() + 0.5 + pdz * 1.2);
      if (closed) {
         this.init_V_Vec3(this.init_Vec3_BlockPos(cell));
         this.init_V_BlockPos(cell);
      } else {
         this.init_V_LocalPlayer_Vec3(player, this.init_Vec3_LocalPlayer_2(player, m, past));
      }

      boolean forward = this.init_Z_F(this.init_F_LocalPlayer(player, m, past, true));
      this.init_V_LocalPlayer_Z(player, forward && this.close(player));
      this.init_Minecraft.options.keyJump.setDown(false);
      this.init_Minecraft.options.keyShift.setDown(false);
      this.flush_Z = false;
   }

   private BlockPos update_BlockPos() {
      for (int i = this.tick_I + 1; i < Math.min(this.init_List.size(), this.tick_I + 3); i++) {
         ClassCache.UiHelper ahead = this.init_List.get(i);
         if (ahead.type() == ClassCache.Inner_SyncManager.set) {
            return this.sync(ahead.to()) ? ahead.to() : null;
         }

         if (!push(ahead)) {
            return null;
         }
      }

      return null;
   }

   private Vec3 init_Vec3_BlockPos(BlockPos door) {
      double eye = this.init_Minecraft.player.getEyeY();
      boolean tall = this.init_Minecraft.level.getBlockState(door).getBlock() instanceof DoorBlock;
      double top = door.getY() + (tall ? 1.9 : 0.8);
      double bottom = door.getY() + 0.1;
      return new Vec3(door.getX() + 0.5, Math.max(bottom, Math.min(top, eye)), door.getZ() + 0.5);
   }

   private void init_V_BlockPos(BlockPos door) {
      LocalPlayer player = this.init_Minecraft.player;
      if (player != null && this.sync(door) && player.tickCount - this.mark >= 3) {
         this.init_DataUtil.load_V();
         BlockHitResult hit = DataUtil.init_BlockHitResult();
         if (hit != null && (hit.getBlockPos().equals(door) || hit.getBlockPos().equals(door.above()))) {
            this.init_Minecraft.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
            Compat263.swing(player, InteractionHand.MAIN_HAND);
            this.mark = player.tickCount;
            this.flush_Z = true;
            if (init_Z) {
               ListPool.init("door click " + door.toShortString() + " from " + String.format("%.2f", player.position().distanceTo(Vec3.atCenterOf(door))));
            }
         }
      }
   }

   private boolean flush(ClassCache.UiHelper m) {
      if (this.sync(m.to())) {
         this.run(this.init_Minecraft.player, m);
         return false;
      }

      this.flush_Z = false;
      if (read_Z_BlockPos(m.to())) {
         this.load_BlockPos = m.to().immutable();
         this.add_I = 0;
         this.clear_I = Integer.signum(m.to().getX() - m.from().getX());
         this.reset_I = Integer.signum(m.to().getZ() - m.from().getZ());
      }

      return true;
   }

   public static void init_V_Set(Set<Long> cells) {
      write_Set = Set.copyOf(cells);
      update_J = cells.isEmpty() ? 0L : System.nanoTime() + 3000000000L;
   }

   static boolean read_Z_BlockPos(BlockPos cell) {
      return System.nanoTime() < update_J && (write_Set.contains(cell.asLong()) || write_Set.contains(cell.below().asLong()));
   }

   private void run_V() {
      if (this.load_BlockPos != null) {
         BlockPos cell = this.load_BlockPos;
         if (this.init_Minecraft.player != null
            && this.init_Minecraft.level != null
            && this.flush(cell)
            && ++this.add_I <= 24
            && !(this.init_Minecraft.player.blockPosition().distSqr(cell) > 36.0)) {
            if (init_Z_List(
               this.init_List_BlockPos(cell), Vec3.atLowerCornerOf(cell), this.init_Minecraft.player.getBoundingBox(), this.clear_I, this.reset_I, 0.05
            )) {
               AABB doorway = new AABB(cell);
               if (this.init_Minecraft.level.getBlockState(cell).getBlock() instanceof DoorBlock) {
                  doorway = doorway.expandTowards(0.0, 1.0, 0.0).expandTowards(0.0, -1.0, 0.0);
               }

               if (!doorway.intersects(this.init_Minecraft.player.getBoundingBox())) {
                  this.set();
                  this.init_V_Vec3(this.load_Vec3_BlockPos(cell));
                  if (DataUtil.init_Z_BlockPos(cell)) {
                     BlockHitResult hit = DataUtil.init_BlockHitResult();
                     if (hit != null) {
                        this.init_Minecraft.gameMode.useItemOn(this.init_Minecraft.player, InteractionHand.MAIN_HAND, hit);
                        Compat263.swing(this.init_Minecraft.player, InteractionHand.MAIN_HAND);
                     }

                     this.load_BlockPos = null;
                  }
               }
            }
         } else {
            this.load_BlockPos = null;
         }
      }
   }

   private List<AABB> init_List_BlockPos(BlockPos cell) {
      BlockState state = this.init_Minecraft.level.getBlockState(cell);
      return state.hasProperty(BlockStateProperties.OPEN)
         ? state.setValue(BlockStateProperties.OPEN, false).getShape(this.init_Minecraft.level, cell).toAabbs()
         : state.getShape(this.init_Minecraft.level, cell).toAabbs();
   }

   static boolean init_Z_List(List<AABB> shapeBoxes, Vec3 base, AABB playerBox, int dx, int dz, double margin) {
      for (AABB box : shapeBoxes) {
         AABB pane = box.move(base.x, base.y, base.z);
         boolean passed = dx > 0
            ? playerBox.minX > pane.maxX + margin
            : (dx < 0 ? playerBox.maxX < pane.minX - margin : (dz > 0 ? playerBox.minZ > pane.maxZ + margin : playerBox.maxZ < pane.minZ - margin));
         if (!passed) {
            return false;
         }
      }

      return true;
   }

   private Vec3 load_Vec3_BlockPos(BlockPos cell) {
      return this.init_Minecraft.player != null && this.init_Minecraft.level != null
         ? init_Vec3_List(
            this.init_Minecraft.level.getBlockState(cell).getShape(this.init_Minecraft.level, cell).toAabbs(),
            Vec3.atLowerCornerOf(cell),
            this.init_Minecraft.player.getEyePosition()
         )
         : Vec3.atCenterOf(cell);
   }

   static Vec3 init_Vec3_List(List<AABB> boxes, Vec3 base, Vec3 eye) {
      AABB best = null;
      double bestSqr = 1.7976931348623157E308;

      for (AABB box : boxes) {
         double d = eye.distanceToSqr(box.getCenter().add(base));
         if (d < bestSqr) {
            bestSqr = d;
            best = box;
         }
      }

      return best != null ? best.getCenter().add(base) : base.add(0.5, 0.5, 0.5);
   }

   private static boolean load_Z_BlockState(BlockState state) {
      return state.getBlock() instanceof DoorBlock || state.getBlock() instanceof FenceGateBlock;
   }

   private boolean sync(BlockPos cell) {
      BlockState state = this.init_Minecraft.level.getBlockState(cell);
      return load_Z_BlockState(state) && state.hasProperty(BlockStateProperties.OPEN) && !state.getValue(BlockStateProperties.OPEN);
   }

   private boolean flush(BlockPos cell) {
      BlockState state = this.init_Minecraft.level.getBlockState(cell);
      return load_Z_BlockState(state) && state.hasProperty(BlockStateProperties.OPEN) && state.getValue(BlockStateProperties.OPEN);
   }

   private boolean close(ClassCache.UiHelper m) {
      if (!m.cellsToPlace().isEmpty() && !this.update_Z && !this.flush_Z(this.init_Minecraft.player, m)) {
         return false;
      }

      if (push(m)
         && init_Z_ClassCache_UiHelpe(
            m,
            this.init_Minecraft.player.getX(),
            this.init_Minecraft.player.getY(),
            this.init_Minecraft.player.getZ(),
            this.init_Minecraft.player.onGround() || this.init_Minecraft.player.isInWater()
         )
         && init_Z_BlockPos_Function(this.init_Minecraft.player.blockPosition(), this::load_ClassCache_FileCac_BlockPos)) {
         return true;
      }

      if (m.type() == ClassCache.Inner_SyncManager.set) {
         int ddx = Integer.signum(m.to().getX() - m.from().getX());
         int ddz = Integer.signum(m.to().getZ() - m.from().getZ());
         return !this.sync(m.to())
            && init_Z_List(this.init_List_BlockPos(m.to()), Vec3.atLowerCornerOf(m.to()), this.init_Minecraft.player.getBoundingBox(), ddx, ddz, 0.05);
      }

      if (m.type() == ClassCache.Inner_SyncManager.get && m.to().getY() > m.from().getY()) {
         return this.init_Minecraft.player.getY() >= m.to().getY() + 0.1
            && Math.abs(this.init_Minecraft.player.getX() - (m.to().getX() + 0.5)) < 1.4
            && Math.abs(this.init_Minecraft.player.getZ() - (m.to().getZ() + 0.5)) < 1.4
            && (this.init_Minecraft.player.isInWater() || this.init_Minecraft.player.onClimbable() || this.init_Minecraft.player.onGround());
      }

      if (m.type() == ClassCache.Inner_SyncManager.start) {
         if (this.init_Minecraft.player.onGround() && !(this.init_Minecraft.player.getDeltaMovement().y > 0.001)) {
            double surfTo = this.init_D_ClassCache_UiHelpe(m);
            if (!this.write_Z_ClassCache_UiHelpe(m)) {
               return false;
            }

            if (!this.pop_Z && this.init_PoolEntry != null && this.prev_I < this.init_PoolEntry.inputs().size()) {
               NodeWalker.Inner_StreamUtil state = init_NodeWalker_StreamU_LocalPlayer(this.init_Minecraft.player);
               return Math.abs(state.y() - surfTo) <= 0.02
                  && this.read_Z_ClassCache_UiHelpe_D(m, surfTo)
                  && TableIndex.init_D_NodeWalker_CachePo_2(this.init_BitSetUtil(), state, m.to(), surfTo, this.init_Minecraft.player.getYRot()) >= -0.04;
            }

            if (TableIndex.init_Z_D(this.init_Minecraft.player.getX(), this.init_Minecraft.player.getY(), this.init_Minecraft.player.getZ(), m.to(), surfTo)) {
               return true;
            }

            if (Math.abs(this.init_Minecraft.player.getY() - surfTo) <= 0.02
               && this.read_Z_ClassCache_UiHelpe_D(m, surfTo)
               && TableIndex.init_D_NodeWalker_CachePo_2(
                     this.init_BitSetUtil(),
                     init_NodeWalker_StreamU_LocalPlayer(this.init_Minecraft.player),
                     m.to(),
                     surfTo,
                     this.init_Minecraft.player.getYRot()
                  )
                  >= -0.12) {
               return true;
            }

            BlockPos cell = this.init_Minecraft.player.blockPosition();
            return init_Z_BlockPos(m.to(), cell, this.init_Minecraft.player.getY(), surfTo, this.close_Z, this.poll_Z)
               && init_Z_BlockPos_Function(cell, this::load_ClassCache_FileCac_BlockPos);
         } else {
            return false;
         }
      } else if (read_Z_ClassCache_SyncMan(m.type())) {
         if (!this.init_Minecraft.player.onGround()) {
            return false;
         } else {
            return this.init_Minecraft.player.position().distanceToSqr(Vec3.atBottomCenterOf(m.to())) < 0.42250000000000004
               ? true
               : init_Z_ClassCache_UiHelpe_BlockPos(m, this.init_Minecraft.player.blockPosition())
                  && ClassCache.Inner_RenderState.load(this::load_ClassCache_FileCac_BlockPos, this.init_Minecraft.player.blockPosition());
         }
      } else {
         if (m.type() == ClassCache.Inner_SyncManager.reset) {
            return init_Z_ClassCache_UiHelpe_6(
               m,
               this.init_Minecraft.player.position(),
               this.init_Minecraft.player.onGround() || this.init_Minecraft.player.isInWater() || this.init_Minecraft.player.onClimbable()
            );
         }

         if (m.type() == ClassCache.Inner_SyncManager.flush
            || m.type() == ClassCache.Inner_SyncManager.close
            || m.type() == ClassCache.Inner_SyncManager.open
            || m.type() == ClassCache.Inner_SyncManager.next) {
            return init_Z_ClassCache_UiHelpe_2(
               m,
               this.init_Minecraft.player.blockPosition().getX(),
               this.init_Minecraft.player.blockPosition().getZ(),
               this.init_Minecraft.player.getY(),
               this.init_Minecraft.player.onGround() || this.init_Minecraft.player.isInWater() || this.init_Minecraft.player.onClimbable()
            );
         }

         if (m.type() == ClassCache.Inner_SyncManager.push) {
            return !this.load_ClassCache_FileCac_BlockPos(m.cellsToBreak().get(0)).passable()
               ? false
               : this.init_Minecraft.player.getY() < m.from().getY() - 0.5
                  && (this.init_Minecraft.player.onGround() || this.init_Minecraft.player.isInWater() || this.init_Minecraft.player.onClimbable());
         }

         if (this.init_Minecraft.player.isInWater()
            && this.load_ClassCache_FileCac_BlockPos(m.to()).water()
            && !this.init_List.isEmpty()
            && m != this.init_List.get(this.init_List.size() - 1)
            && Math.hypot(this.init_Minecraft.player.getX() - (m.to().getX() + 0.5), this.init_Minecraft.player.getZ() - (m.to().getZ() + 0.5)) <= 1.2
            && Math.abs(this.init_Minecraft.player.getY() - m.to().getY()) <= 1.0) {
            return true;
         }

         if (this.init_Minecraft.player.blockPosition().getX() == m.to().getX() && this.init_Minecraft.player.blockPosition().getZ() == m.to().getZ()) {
            double arrivalDy = this.init_Minecraft.player.getY() - m.to().getY();
            if (arrivalDy < -1.0 || arrivalDy > 1.35) {
               return false;
            } else if (m.to().getY() <= m.from().getY()) {
               return m.type() == ClassCache.Inner_SyncManager.poll
                  ? init_Z_D(
                     this.init_Minecraft.player.getY(),
                     m.to().getY(),
                     this.init_Minecraft.player.onGround(),
                     this.init_Minecraft.player.isInWater(),
                     this.init_Minecraft.player.onClimbable()
                  )
                  : true;
            } else {
               return this.init_Minecraft.player.onGround() || this.init_Minecraft.player.isInWater() || this.init_Minecraft.player.onClimbable();
            }
         } else {
            return false;
         }
      }
   }

   static boolean init_Z_D(double feetY, double targetY, boolean grounded, boolean water, boolean climbing) {
      double dy = feetY - targetY;
      return dy >= -1.0 && dy <= 1.35 && (water || climbing || grounded && dy <= 0.6);
   }

   static boolean init(int playerCellX, double playerY, int playerCellZ, boolean supported, int fromX, int toX, int fromZ, int toZ, double toY) {
      if (supported && !(playerY < toY - 0.001)) {
         int dx = Integer.signum(toX - fromX);
         int dz = Integer.signum(toZ - fromZ);
         if (dx != 0) {
            return dx > 0 ? playerCellX >= toX : playerCellX <= toX;
         } else if (dz != 0) {
            return dz > 0 ? playerCellZ >= toZ : playerCellZ <= toZ;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private boolean flush_Z(LocalPlayer player, ClassCache.UiHelper m) {
      boolean supported = player.onGround() || player.isInWater() || player.onClimbable();
      if (supported && m.type() == ClassCache.Inner_SyncManager.run && player.onGround() && !player.isInWater()) {
         BlockPos on = player.getOnPos();
         if (on.getX() == m.from().getX() && on.getZ() == m.from().getZ()) {
            supported = false;
         }
      }

      return init(
         player.blockPosition().getX(),
         player.getY(),
         player.blockPosition().getZ(),
         supported,
         m.from().getX(),
         m.to().getX(),
         m.from().getZ(),
         m.to().getZ(),
         m.to().getY()
      );
   }

   static boolean init_Z_ClassCache_UiHelpe_6(ClassCache.UiHelper m, Vec3 player, boolean onGroundish) {
      BlockPos to = m.to();
      return BlockPos.containing(player.x, to.getY(), player.z).equals(to) && Math.abs(player.y - to.getY()) <= (onGroundish ? 0.65 : 2.0);
   }

   private boolean open(ClassCache.UiHelper m) {
      return init_Z_ClassCache_UiHelpe_4(m, this.init_Function(), this.stop_Z);
   }

   static boolean read_Z_ClassCache_UiHelpe_Function(ClassCache.UiHelper m, Function<BlockPos, ClassCache.Inner_FileCache> probe) {
      return init_Z_ClassCache_UiHelpe_4(m, probe, false);
   }

   static boolean init_Z_ClassCache_UiHelpe_4(ClassCache.UiHelper m, Function<BlockPos, ClassCache.Inner_FileCache> probe, boolean supportedBridges) {
      if (supportedBridges && m.type() == ClassCache.Inner_SyncManager.run) {
         ClassCache.Inner_FileCache support = probe.apply(m.to().below());
         ClassCache.Inner_FileCache feet = probe.apply(m.to());
         ClassCache.Inner_FileCache head = probe.apply(m.to().above());
         return support.loaded()
            && support.passable()
            && feet.loaded()
            && feet.passable()
            && !feet.lava()
            && !feet.hazardFeet()
            && head.loaded()
            && head.passable()
            && !head.lava()
            && !head.hazardFeet()
            && update_Z_ClassCache_UiHelpe_Function(m, probe);
      } else {
         return write_Z_ClassCache_UiHelpe_Function(m, probe) && update_Z_ClassCache_UiHelpe_Function(m, probe);
      }
   }

   private static boolean write_Z_ClassCache_UiHelpe_Function(ClassCache.UiHelper m, Function<BlockPos, ClassCache.Inner_FileCache> probe) {
      boolean var10000;
      switch (m.type()) {
         case init:
         case load:
         case read:
         case write:
            var10000 = init_Z_BlockPos_Function(m.to(), probe);
            break;
         case reset:
            var10000 = ClassCache.Inner_RenderState.write(probe, m.from(), m.to());
            break;
         case update:
            var10000 = (!probe.apply(m.to().below()).passable() || probe.apply(m.from()).water() && probe.apply(m.to()).water())
               && init_Z_BlockPos_Function(m.to(), probe);
            break;
         case sync:
            var10000 = !probe.apply(m.to().below()).passable()
               && init_Z_BlockPos_Function(m.to(), probe)
               && (!probe.apply(m.from().below()).farmland() || probe.apply(m.from()).water());
            break;
         case get:
            if (m.to().getY() > m.from().getY()) {
               ClassCache.Inner_FileCache tx = probe.apply(m.to());
               var10000 = (tx.passable() || tx.climbable())
                  && !tx.lava()
                  && !tx.hazardFeet()
                  && (probe.apply(m.to().above()).passable() || probe.apply(m.to().above()).climbable());
            } else {
               var10000 = init_Z_BlockPos_Function(m.to(), probe);
            }
            break;
         case put:
         case add:
         case clear:
            int dx = Integer.signum(m.to().getX() - m.from().getX());
            int dz = Integer.signum(m.to().getZ() - m.from().getZ());
            int gap = Math.max(Math.abs(m.to().getX() - m.from().getX()), Math.abs(m.to().getZ() - m.from().getZ())) - 1;
            var10000 = ClassCache.Inner_RenderState.init_Z_Function(probe, m.from(), dx, dz, gap, m.type() == ClassCache.Inner_SyncManager.clear);
            break;
         case start:
            var10000 = (init_Z_BlockPos_Function(m.to(), probe) || ClassCache.Inner_RenderState.init_Z_Function_2(probe, m.to(), m.jump()))
               && !probe.apply(m.to().below()).farmland()
               && ClassCache.Inner_RenderState.load(probe.apply(m.from()), probe.apply(m.from().below()));
            break;
         case flush:
         case close:
         case open:
            var10000 = init_Z_BlockPos_Function(m.to(), probe)
               && !probe.apply(m.to().below()).farmland()
               && (
                  ClassCache.Inner_RenderState.read(probe, m.from(), m.to())
                     || ClassCache.Inner_RenderState.init_Z_Function(
                        probe,
                        m.to(),
                        Integer.signum(m.to().getX() - m.from().getX()),
                        Integer.signum(m.to().getZ() - m.from().getZ()),
                        m.from().getY() - m.to().getY()
                     )
               );
            break;
         case poll:
            ClassCache.Inner_FileCache t = probe.apply(m.to());
            var10000 = t.passable()
               && !t.lava()
               && !t.hazardFeet()
               && !probe.apply(m.to().below()).farmland()
               && Math.min(t.fallMult10(), probe.apply(m.to().below()).fallMult10()) <= 2;
            break;
         case push:
            BlockPos cell = m.cellsToBreak().get(0);
            ClassCache.Inner_FileCache c = probe.apply(cell);
            ClassCache.Inner_FileCache landing = probe.apply(cell.below());
            ClassCache.Inner_FileCache fromFeet = probe.apply(m.from());
            ClassCache.Inner_FileCache fromHead = probe.apply(m.from().above());
            var10000 = (c.passable() || c.diggable())
               && !landing.passable()
               && !landing.hazardFloor()
               && !landing.lava()
               && !landing.water()
               && !landing.farmland()
               && landing.top16() <= 16
               && (!fromFeet.water() || !fromHead.water());
            break;
         case pop:
         case stop:
         case next: {
            ClassCache.Inner_FileCache floor = probe.apply(m.to().below());
            boolean ok = !floor.passable() && !floor.hazardFloor() && !floor.lava();

            for (BlockPos cell2 : m.cellsToBreak()) {
               ClassCache.Inner_FileCache c2 = probe.apply(cell2);
               if (!c2.passable() && !c2.diggable()) {
                  ok = false;
               }
            }

            var10000 = ok;
            break;
         }
         case tick: {
            ClassCache.Inner_FileCache footing = probe.apply(m.from().below());
            boolean ok = (
                  !footing.farmland() && footing.top16() >= 16 || m.repairFarmland() && footing.farmland() && footing.top16() == 15 || footing.soulSand()
               )
               && !footing.door();

            for (int up = 2; up <= 3; up++) {
               BlockPos c3 = m.from().above(up);
               ClassCache.Inner_FileCache p = probe.apply(c3);
               if (p.lava()) {
                  ok = false;
               } else if (!p.passable() && (!m.cellsToBreak().contains(c3) || !p.diggable()) && up == 2) {
                  ok = false;
               }
            }

            var10000 = ok;
            break;
         }
         case run:
            ClassCache.Inner_FileCache support = probe.apply(m.cellsToPlace().get(0));
            var10000 = support.passable()
               && !support.water()
               && !support.lava()
               && probe.apply(m.to()).passable()
               && probe.apply(m.to().above()).passable()
               && ClassCache.Inner_RenderState.init_I(probe, m.cellsToPlace().get(0)) >= 0;
            break;
         case set: {
            ClassCache.Inner_FileCache feet = probe.apply(m.to());
            ClassCache.Inner_FileCache head = probe.apply(m.to().above());
            ClassCache.Inner_FileCache floor = probe.apply(m.to().below());
            var10000 = (feet.passable() || feet.door())
               && !feet.lava()
               && !feet.hazardFeet()
               && (head.passable() || head.door())
               && !head.lava()
               && !head.hazardFeet()
               && !floor.passable()
               && !floor.hazardFloor()
               && !floor.lava();
            break;
         }
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static boolean update_Z_ClassCache_UiHelpe_Function(ClassCache.UiHelper m, Function<BlockPos, ClassCache.Inner_FileCache> probe) {
      boolean var10000;
      switch (m.type()) {
         case put:
         case add:
         case clear:
            if (!probe.apply(m.from()).cached() && !probe.apply(m.to()).cached() && !probe.apply(m.to().below()).cached()) {
               int dx = Integer.signum(m.to().getX() - m.from().getX());
               int dz = Integer.signum(m.to().getZ() - m.from().getZ());
               int gap = Math.max(Math.abs(m.to().getX() - m.from().getX()), Math.abs(m.to().getZ() - m.from().getZ())) - 1;
               boolean clear = true;

               for (int i = 1; i <= gap; i++) {
                  if (probe.apply(m.from().offset(dx * i, 0, dz * i)).cached()) {
                     clear = false;
                  }
               }

               var10000 = clear;
            } else {
               var10000 = false;
            }
            break;
         case start:
            var10000 = !probe.apply(m.from()).cached() && !probe.apply(m.to()).cached() && !probe.apply(m.to().below()).cached();
            break;
         case flush:
         default:
            var10000 = true;
            break;
         case close:
         case open:
         case poll:
            for (int y = m.to().getY() - 1; y <= m.from().getY() + 1; y++) {
               if (probe.apply(new BlockPos(m.to().getX(), y, m.to().getZ())).cached()) {
                  return false;
               }
            }

            var10000 = true;
            break;
         case push:
            var10000 = init_Z_List_Function(m.cellsToBreak(), probe) && !probe.apply(m.cellsToBreak().get(0).below()).cached();
            break;
         case pop:
         case stop:
         case next:
            var10000 = init_Z_List_Function(m.cellsToBreak(), probe) && !probe.apply(m.to().below()).cached();
            break;
         case tick:
            var10000 = init_Z_List_Function(m.cellsToPlace(), probe) && !probe.apply(m.from().above(2)).cached() && !probe.apply(m.from().above(3)).cached();
            break;
         case run:
            var10000 = init_Z_List_Function(m.cellsToPlace(), probe) && !probe.apply(m.to()).cached();
      }

      return var10000;
   }

   private static boolean init_Z_List_Function(List<BlockPos> cells, Function<BlockPos, ClassCache.Inner_FileCache> probe) {
      for (BlockPos cell : cells) {
         if (probe.apply(cell).cached()) {
            return false;
         }
      }

      return true;
   }

   static int init_I_List_2(List<ClassCache.UiHelper> path, int current, int lookahead, Function<BlockPos, ClassCache.Inner_FileCache> probe) {
      return init_I_List(path, current, lookahead, probe, false);
   }

   static int init_I_List(
      List<ClassCache.UiHelper> path, int current, int lookahead, Function<BlockPos, ClassCache.Inner_FileCache> probe, boolean supportedBridges
   ) {
      int end = Math.min(path.size(), current + 1 + lookahead);
      Map<Long, ClassCache.Inner_FileCache> edits = new HashMap<>();
      Function<BlockPos, ClassCache.Inner_FileCache> after = cell -> {
         ClassCache.Inner_FileCache edited = edits.get(cell.asLong());
         return edited != null ? edited : probe.apply(cell);
      };

      for (int i = Math.max(0, current); i < end; i++) {
         ClassCache.UiHelper m = path.get(i);
         if (i > current && !init_Z_ClassCache_UiHelpe_4(m, edits.isEmpty() ? probe : after, supportedBridges)) {
            return i;
         }

         for (BlockPos c : m.cellsToBreak()) {
            edits.put(c.asLong(), ClassCache.Inner_FileCache.air());
         }

         for (BlockPos c : m.cellsToPlace()) {
            edits.put(c.asLong(), ClassCache.Inner_FileCache.fullBlock(1.0 / 0.0));
         }
      }

      return -1;
   }

   static int init_I_List_2(List<ClassCache.UiHelper> path, int current, int lookahead, boolean sprintAvailable, boolean currentCommitted) {
      if (sprintAvailable) {
         return -1;
      }

      int start = currentCommitted ? current + 1 : current;
      int end = Math.min(path.size(), current + 1 + lookahead);

      for (int i = Math.max(start, 0); i < end; i++) {
         if (load_Z_ClassCache_SyncMan(path.get(i).type())) {
            return i;
         }
      }

      return -1;
   }

   static boolean load_Z_ClassCache_SyncMan(ClassCache.Inner_SyncManager type) {
      return type == ClassCache.Inner_SyncManager.add || type == ClassCache.Inner_SyncManager.clear;
   }

   static boolean init_Z_ClassCache_SyncMan(ClassCache.Inner_SyncManager type, boolean committed, boolean placementInFlight) {
      if (read_Z_ClassCache_SyncMan(type)) {
         return !committed;
      }

      boolean var10000;
      switch (type) {
         case tick:
         case run:
            var10000 = !placementInFlight;
            break;
         default:
            var10000 = true;
      }

      return var10000;
   }

   private boolean pop_Z() {
      return this.init_ClassCache_UiHelpe != null
         && init_Z_ClassCache_SyncMan(this.init_ClassCache_UiHelpe.type(), this.close_Z, this.update_Z || this.init_BlockPos != null);
   }

   static boolean init_Z_ClassCache_SyncMan(ClassCache.Inner_SyncManager type, boolean committed, boolean headingGated, double aimErrorDegrees) {
      return init_Z_ClassCache_SyncMan(type, committed, headingGated, aimErrorDegrees, true);
   }

   static boolean init_Z_ClassCache_SyncMan(
      ClassCache.Inner_SyncManager type, boolean committed, boolean headingGated, double aimErrorDegrees, boolean sweepLane
   ) {
      if (!sweepLane && !write_Z_ClassCache_SyncMan(type)) {
         return false;
      }

      boolean mustConverge = read_Z_ClassCache_SyncMan(type) ? !committed : headingGated;
      return mustConverge && aimErrorDegrees > 6.0;
   }

   private static boolean write_Z_ClassCache_SyncMan(ClassCache.Inner_SyncManager type) {
      return type == ClassCache.Inner_SyncManager.push
         || type == ClassCache.Inner_SyncManager.pop
         || type == ClassCache.Inner_SyncManager.stop
         || type == ClassCache.Inner_SyncManager.next
         || type == ClassCache.Inner_SyncManager.tick;
   }

   static boolean init_Z_ClassCache_UiHelpe_BlockPos(ClassCache.UiHelper m, BlockPos cell) {
      int dx = Integer.signum(m.to().getX() - m.from().getX());
      int dz = Integer.signum(m.to().getZ() - m.from().getZ());
      return cell.equals(m.to()) || cell.equals(m.to().offset(dx, 0, dz)) || cell.equals(m.to().offset(2 * dx, 0, 2 * dz));
   }

   static boolean init_Z_Z_I(boolean observedClosed, int waitTicks) {
      return observedClosed && waitTicks <= 0;
   }

   private static boolean init_Z_BlockPos_Function(BlockPos cell, Function<BlockPos, ClassCache.Inner_FileCache> probe) {
      ClassCache.Inner_FileCache feet = probe.apply(cell);
      ClassCache.Inner_FileCache head = probe.apply(cell.above());
      return ClassCache.Inner_RenderState.init_Z_ClassCache_FileCac(feet, head, probe.apply(cell.below()))
         ? true
         : feet.loaded() && feet.water() && !feet.lava() && !feet.hazardFeet() && head.loaded() && (head.water() || head.passable()) && !head.lava();
   }

   private ClassCache.Inner_FileCache init_ClassCache_FileCac_BlockPos(BlockPos cell) {
      if (this.load_ItemStackArr == null) {
         int slots = 36;
         this.load_ItemStackArr = new ItemStack[slots];

         for (int i = 0; i < slots; i++) {
            this.load_ItemStackArr[i] = this.init_Minecraft.player.getInventory().getItem(i).copy();
         }
      }

      return ClassCache.Inner_StreamUtil.init_ClassCache_FileCac_Minecraft(this.init_Minecraft, this.init_Minecraft.level, cell, this.load_ItemStackArr);
   }

   private Function<BlockPos, ClassCache.Inner_FileCache> init_Function() {
      return cell -> {
         ClassCache.Inner_FileCache live = this.load_ClassCache_FileCac_BlockPos(cell);
         return !live.passable() && !live.diggable() && live.loaded() && !live.cached() ? this.init_ClassCache_FileCac_BlockPos(cell) : live;
      };
   }

   private ClassCache.Inner_FileCache load_ClassCache_FileCac_BlockPos(BlockPos cell) {
      ClassCache.Inner_FileCache live = ClassCache.Inner_StreamUtil.init_ClassCache_FileCac_Minecraft(
         this.init_Minecraft, this.init_Minecraft.level, cell, init_ItemStackArr
      );
      return !live.loaded() && this.init_StateCache != null ? this.init_StateCache.init_ClassCache_FileCac_I(cell.getX(), cell.getY(), cell.getZ()) : live;
   }

   private Vec3 load_Vec3_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      double eyeY = this.init_Minecraft.player == null ? m.to().getY() : this.init_Minecraft.player.getEyePosition().y;
      if (m.cellsToBreak().isEmpty() && m.cellsToPlace().isEmpty()) {
         Vec3 var10000;
         switch (m.type()) {
            case init:
            case load:
            case read:
            case write:
            case reset:
            case update:
            case sync:
            case get:
            case flush:
            case close:
            case open:
            case poll:
               var10000 = this.init_Vec3_Vec3(this.load_Vec3_ClassCache_UiHelpe_D(m, eyeY));
               break;
            case put:
            case add:
            case clear:
            case start:
            default:
               var10000 = init_Vec3_ClassCache_UiHelpe_D(m, eyeY);
         }

         return var10000;
      } else {
         return init_Vec3_ClassCache_UiHelpe_D(m, eyeY);
      }
   }

   private static boolean poll(ClassCache.UiHelper m) {
      if (m.cellsToBreak().isEmpty() && m.cellsToPlace().isEmpty()) {
         boolean var10000;
         switch (m.type()) {
            case init:
            case load:
            case read:
            case write:
            case reset:
            case update:
            case sync:
            case put:
            case add:
            case clear:
            case start:
            case flush:
            case close:
            case open:
            case poll:
            case set:
               var10000 = true;
               break;
            case get:
            case push:
            case pop:
            case stop:
            case next:
            case tick:
            case run:
            default:
               var10000 = false;
         }

         return var10000;
      } else {
         return false;
      }
   }

   private Vec3 init_Vec3_LocalPlayer_2(LocalPlayer player, ClassCache.UiHelper m, Vec3 stepGaze) {
      this.buffer_Z = false;
      if (poll(m) && !read_Z_ClassCache_SyncMan(m.type()) && !this.init_DataUtil.init_Z()) {
         for (int i = this.tick_I + 1; i < Math.min(this.init_List.size(), this.tick_I + 8); i++) {
            ClassCache.UiHelper ahead = this.init_List.get(i);
            if (!ahead.cellsToBreak().isEmpty() || !ahead.cellsToPlace().isEmpty()) {
               Vec3 line = ahead.cellsToPlace().isEmpty() ? this.write_Vec3_LocalPlayer_ClassCache_UiHelpe(player, ahead) : null;
               if (line != null) {
                  return line;
               }
               break;
            }

            if (!poll(ahead)) {
               break;
            }
         }

         if (this.init_Function != null && this.init_List.size() - this.tick_I <= 2) {
            boolean walks = true;

            for (int i = this.tick_I + 1; i < this.init_List.size() && walks; i++) {
               walks = poll(this.init_List.get(i));
            }

            Vec3 there = walks ? this.init_Function.apply(this.init_List.get(this.init_List.size() - 1).to()) : null;
            if (there != null) {
               this.buffer_Z = true;
               return there;
            }
         }

         double[] carrot = this.init_DArr_LocalPlayer_ClassCache_UiHelpe(player, m);
         if (carrot == null) {
            return stepGaze;
         }

         if (this.scan >= 0) {
            ClassCache.UiHelper stop = this.init_List.get(this.scan);
            Vec3 line = stop.cellsToPlace().isEmpty() && !stop.cellsToBreak().isEmpty() ? this.write_Vec3_LocalPlayer_ClassCache_UiHelpe(player, stop) : null;
            if (line != null) {
               return line;
            }
         }

         double eyeY = player.getEyePosition().y + (carrot[2] - carrot[3]);
         return this.init_Vec3_Vec3(new Vec3(carrot[0], eyeY, carrot[1]));
      } else {
         return stepGaze;
      }
   }

   private double[] init_DArr_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper m) {
      this.scan = -1;
      int gazeIndex = !player.onGround() && push(m) ? init_I_List(this.init_List, this.tick_I, player.getX(), player.getZ()) : this.tick_I;
      m = this.init_List.get(gazeIndex);
      double ax = m.from().getX() + 0.5;
      double az = m.from().getZ() + 0.5;
      double bx = m.to().getX() + 0.5;
      double bz = m.to().getZ() + 0.5;
      double sx = bx - ax;
      double sz = bz - az;
      double length = Math.hypot(sx, sz);
      double t = length < 1.0E-6 ? 1.0 : Math.max(0.0, Math.min(1.0, ((player.getX() - ax) * sx + (player.getZ() - az) * sz) / (length * length)));
      double rise = m.to().getY() - m.from().getY();
      double body = m.from().getY() + rise * t;
      double left = 4.0;
      double x = ax + sx * t;
      double z = az + sz * t;
      double y = m.to().getY();
      double rest = length * (1.0 - t);
      if (rest >= left) {
         double u = length < 1.0E-6 ? 1.0 : t + left / length;
         return new double[]{x + sx / length * left, z + sz / length * left, m.from().getY() + rise * Math.min(1.0, u), body};
      }

      left -= rest;
      x = bx;
      z = bz;
      double hx = length < 1.0E-6 ? 0.0 : sx / length;
      double hz = length < 1.0E-6 ? 0.0 : sz / length;
      double turned = 0.0;

      for (int i = gazeIndex + 1; i < Math.min(this.init_List.size(), gazeIndex + 10); i++) {
         ClassCache.UiHelper ahead = this.init_List.get(i);
         if (!poll(ahead)) {
            this.scan = i;
            break;
         }

         double fx = ahead.from().getX() + 0.5;
         double fz = ahead.from().getZ() + 0.5;
         double tx = ahead.to().getX() + 0.5;
         double tz = ahead.to().getZ() + 0.5;
         double l = Math.hypot(tx - fx, tz - fz);
         if (!(l < 1.0E-6)) {
            double nx = (tx - fx) / l;
            double nz = (tz - fz) / l;
            if (hx != 0.0 || hz != 0.0) {
               double cross = hx * nz - hz * nx;
               double dot = hx * nx + hz * nz;
               if (dot < -0.5) {
                  break;
               }

               turned += Math.atan2(cross, dot);
               if (Math.abs(turned) > Math.toRadians(100.0)) {
                  break;
               }
            }

            hx = nx;
            hz = nz;
            if (l >= left) {
               double u = left / l;
               return new double[]{
                  fx + (tx - fx) / l * left, fz + (tz - fz) / l * left, ahead.from().getY() + (ahead.to().getY() - ahead.from().getY()) * u, body
               };
            }

            left -= l;
            x = tx;
            z = tz;
            y = ahead.to().getY();
         }
      }

      return new double[]{x, z, y, body};
   }

   static int init_I_List(List<ClassCache.UiHelper> route, int at, double x, double z) {
      ClassCache.UiHelper first = route.get(at);
      if (!push(first)) {
         return at;
      }

      int best = at;
      double dx = x - first.from().getX() - 0.5;
      double dz = z - first.from().getZ() - 0.5;
      double distance = dx * dx + dz * dz;

      for (int i = at + 1; i < Math.min(route.size(), at + 8); i++) {
         ClassCache.UiHelper next = route.get(i);
         if (!push(next) || next.from().getY() != first.from().getY() || next.to().getY() != first.to().getY()) {
            break;
         }

         dx = x - next.from().getX() - 0.5;
         dz = z - next.from().getZ() - 0.5;
         double candidate = dx * dx + dz * dz;
         if (candidate < distance) {
            distance = candidate;
            best = i;
         }
      }

      return best;
   }

   private Vec3 write_Vec3_LocalPlayer_ClassCache_UiHelpe(LocalPlayer player, ClassCache.UiHelper dig) {
      List<BlockPos> remaining = new ArrayList<>();

      for (BlockPos c : dig.cellsToBreak()) {
         if (!init_Z_ClassCache_FileCac(this.load_ClassCache_FileCac_BlockPos(c))) {
            remaining.add(c);
         }
      }

      return remaining.isEmpty() ? null : this.init_Vec3_LocalPlayer(player, dig, remaining);
   }

   private boolean init_Z_LocalPlayer_Vec3(LocalPlayer p, Vec3 held) {
      if (this.init_ClassCache_UiHelpe == null) {
         return false;
      }

      int dx = this.init_ClassCache_UiHelpe.to().getX() - this.init_ClassCache_UiHelpe.from().getX();
      int dz = this.init_ClassCache_UiHelpe.to().getZ() - this.init_ClassCache_UiHelpe.from().getZ();
      if (dx == 0 && dz == 0) {
         return true;
      }

      float stepYaw = NodeWalker.init_F_D(0.0, 0.0, dx, dz);
      float heldYaw = NodeWalker.init_F_D(p.getX(), p.getZ(), held.x, held.z);
      return Math.abs(Mth.wrapDegrees(heldYaw - stepYaw)) <= 60.0F;
   }

   private void init_V_LocalPlayer_Vec3(LocalPlayer p, Vec3 point) {
      if (!this.buffer_Z
         && this.init_ClassCache_UiHelpe != null
         && this.tick_I >= this.init_List.size() - 1
         && Math.hypot(this.init_ClassCache_UiHelpe.to().getX() + 0.5 - p.getX(), this.init_ClassCache_UiHelpe.to().getZ() + 0.5 - p.getZ()) < 2.0) {
         this.init_DataUtil.init_V_ce_DataUtil(new ce.Inner_DataUtil(p.getYRot(), p.getXRot()));
      } else {
         float yaw = NodeWalker.init_F_D(p.getX(), p.getZ(), point.x, point.z);
         if (!Float.isNaN(this.open_F) && this.load_Vec3 != null) {
            float turn = Mth.wrapDegrees(yaw - this.open_F);
            if (Math.abs(turn) > 20.0F) {
               if (this.poll_F != 0.0F
                  && Math.signum(turn) != Math.signum(this.poll_F)
                  && this.bind - this.parse <= 8
                  && this.init_Z_LocalPlayer_Vec3(p, this.load_Vec3)) {
                  this.init_V_Vec3(new Vec3(this.load_Vec3.x, point.y, this.load_Vec3.z));
                  return;
               }

               this.poll_F = turn;
               this.parse = this.bind;
            }
         }

         this.open_F = yaw;
         this.load_Vec3 = point;
         this.init_V_Vec3(init_Vec3_LocalPlayer_Vec3(p, this.load_Vec3_LocalPlayer_Vec3(p, point)));
      }
   }

   public static void init_V_F_J(float degrees, long nanos) {
      push_F = degrees;
      sync_J = nanos <= 0L ? 0L : System.nanoTime() + nanos;
   }

   private static Vec3 init_Vec3_LocalPlayer_Vec3(LocalPlayer p, Vec3 point) {
      if (System.nanoTime() >= sync_J) {
         return point;
      }

      Vec3 eye = p.getEyePosition();
      Vec3 d = point.subtract(eye);
      double distance = d.length();
      return !(distance < 0.001) && !(EventBus.load(d) >= push_F) ? eye.add(Vec3.directionFromRotation(push_F, EventBus.init(d)).scale(distance)) : point;
   }

   private Vec3 load_Vec3_LocalPlayer_Vec3(LocalPlayer p, Vec3 point) {
      Vec3 eye = p.getEyePosition();
      Vec3 d = point.subtract(eye);
      double distance = d.length();
      if (distance < 0.001) {
         return point;
      }

      float yaw = EventBus.init(d);
      float pitch = EventBus.load(d);
      float dy = Mth.wrapDegrees(yaw - p.getYRot());
      float dp = pitch - p.getXRot();
      float wantYaw = Math.abs(dy) <= 5.0F ? p.getYRot() : yaw - Math.signum(dy) * 5.0F;
      float wantPitch = Math.abs(dp) <= 4.0F ? p.getXRot() : pitch - Math.signum(dp) * 4.0F;
      return eye.add(Vec3.directionFromRotation(wantPitch, wantYaw).scale(distance));
   }

   private Vec3 init_Vec3_Vec3(Vec3 gaze) {
      LocalPlayer p = this.init_Minecraft.player;
      if (p == null) {
         return gaze;
      } else {
         double dx = gaze.x - p.getX();
         double dz = gaze.z - p.getZ();
         double limit = this.queue_Z ? 1.6 : 1.25;
         this.queue_Z = dx * dx + dz * dz < limit * limit;
         if (!this.queue_Z) {
            return gaze;
         } else if (p.isInWater()) {
            Vec3 ahead = p.position().add(Vec3.directionFromRotation(0.0F, p.getYRot()).scale(4.0));
            return new Vec3(ahead.x, gaze.y, ahead.z);
         } else {
            return init_Vec3_Vec3(p.getEyePosition(), p.getYRot(), p.getXRot());
         }
      }
   }

   static Vec3 init_Vec3_Vec3(Vec3 eye, float yaw, float pitch) {
      double y = Math.toRadians(yaw);
      double p = Math.toRadians(pitch);
      double horizontal = 4.0 * Math.cos(p);
      return eye.add(-Math.sin(y) * horizontal, -Math.sin(p) * 4.0, Math.cos(y) * horizontal);
   }

   static boolean init_Z_ClassCache_UiHelpe(ClassCache.UiHelper m, double x, double y, double z, boolean supported) {
      if (supported && !(Math.abs(y - m.to().getY()) > 0.6)) {
         double dx = m.to().getX() - m.from().getX();
         double dz = m.to().getZ() - m.from().getZ();
         double length = Math.hypot(dx, dz);
         if (length < 1.0E-6) {
            return false;
         }

         dx /= length;
         dz /= length;
         double rx = x - (m.to().getX() + 0.5);
         double rz = z - (m.to().getZ() + 0.5);
         double along = rx * dx + rz * dz;
         double lateral = Math.abs(rx * dz - rz * dx);
         return along > 0.0 && along < 1.6 && lateral < 0.7;
      } else {
         return false;
      }
   }

   static boolean init_Z_ClassCache_UiHelpe(ClassCache.UiHelper m, double x, double z) {
      double dx = m.to().getX() - m.from().getX();
      double dz = m.to().getZ() - m.from().getZ();
      double length = Math.hypot(dx, dz);
      if (length < 1.0E-6) {
         return false;
      }

      double rx = x - (m.to().getX() + 0.5);
      double rz = z - (m.to().getZ() + 0.5);
      return (rx * dx + rz * dz) / length > 0.8;
   }

   static boolean load_Z_ClassCache_UiHelpe(ClassCache.UiHelper m, double x, double z) {
      double ax = m.from().getX() + 0.5;
      double az = m.from().getZ() + 0.5;
      double bx = m.to().getX() + 0.5;
      double bz = m.to().getZ() + 0.5;
      double dx = bx - ax;
      double dz = bz - az;
      double lengthSq = dx * dx + dz * dz;
      double t = lengthSq < 1.0E-9 ? 0.0 : Math.max(0.0, Math.min(1.0, ((x - ax) * dx + (z - az) * dz) / lengthSq));
      double px = ax + t * dx - x;
      double pz = az + t * dz - z;
      return px * px + pz * pz > 2.25;
   }

   static boolean init_Z_ClassCache_UiHelpe_2(ClassCache.UiHelper m, double feetY, boolean inWater) {
      return inWater && read_Z_ClassCache_SyncMan(m.type()) && feetY < m.to().getY() - 0.6;
   }

   static boolean init_Z_ClassCache_UiHelpe(ClassCache.UiHelper m, double feetY, boolean underWater, boolean ceiling) {
      return underWater && ceiling && feetY < Math.min(m.from().getY(), m.to().getY()) - 0.9;
   }

   private boolean update_Z_LocalPlayer(LocalPlayer player) {
      BlockPos cell = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());

      for (int dy = 0; dy <= 3; cell = cell.above()) {
         if (!this.init_Minecraft.level.getFluidState(cell).is(FluidTags.WATER)) {
            return !this.init_Minecraft.level.getBlockState(cell).getCollisionShape(this.init_Minecraft.level, cell).isEmpty();
         }

         dy++;
      }

      return false;
   }

   static boolean load_Z_ClassCache_UiHelpe_2(ClassCache.UiHelper m, double feetY, boolean grounded) {
      return grounded && push(m) && feetY < Math.min(m.from().getY(), m.to().getY()) - 0.97;
   }

   static boolean init_Z_ClassCache_UiHelpe_3(ClassCache.UiHelper next, int dx, int dz) {
      int nx = next.to().getX() - next.from().getX();
      int nz = next.to().getZ() - next.from().getZ();
      return nx == 0 && nz == 0 ? true : nx * dz != nz * dx || nx * dx + nz * dz <= 0;
   }

   static boolean init_Z_BlockPos(BlockPos target, BlockPos cell, double feetY, double surface, boolean committed, boolean airborne) {
      return committed
         && airborne
         && !cell.equals(target)
         && cell.getY() == target.getY()
         && Math.abs(cell.getX() - target.getX()) <= 1
         && Math.abs(cell.getZ() - target.getZ()) <= 1
         && Math.abs(feetY - surface) <= 0.6;
   }

   static boolean init_Z_D(double feetY, double takeoffSurface, double landingSurface) {
      return feetY < Math.min(takeoffSurface, landingSurface) - 0.55;
   }

   private static boolean update_Z_ClassCache_SyncMan(ClassCache.Inner_SyncManager type) {
      return type == ClassCache.Inner_SyncManager.sync
         || type == ClassCache.Inner_SyncManager.flush
         || type == ClassCache.Inner_SyncManager.close
         || type == ClassCache.Inner_SyncManager.open;
   }

   private static boolean push(ClassCache.UiHelper m) {
      if (m.to().getY() == m.from().getY() && m.cellsToBreak().isEmpty() && m.cellsToPlace().isEmpty()) {
         boolean var10000;
         switch (m.type()) {
            case init:
            case load:
            case read:
            case write:
            case reset:
               var10000 = true;
               break;
            default:
               var10000 = false;
         }

         return var10000;
      } else {
         return false;
      }
   }

   static boolean read(ClassCache.UiHelper movement, double feetY, boolean supported) {
      return supported && push(movement) && feetY > movement.to().getY() + 0.65;
   }

   static boolean load_Z_ClassCache_UiHelpe_D(ClassCache.UiHelper movement, double feetY) {
      return push(movement) && feetY > movement.to().getY() + 0.6;
   }

   private boolean sync(LocalPlayer player) {
      if (this.init_ClassCache_UiHelpe != null && (player.onGround() || player.isInWater())) {
         boolean landed = update_Z_ClassCache_SyncMan(this.init_ClassCache_UiHelpe.type())
            && player.onGround()
            && Math.abs(player.getY() - this.init_ClassCache_UiHelpe.to().getY()) < 0.6;
         if (!push(this.init_ClassCache_UiHelpe) && !landed) {
            return false;
         }

         BlockPos at = player.blockPosition();
         int j = this.tick_I + 1;

         for (int end = Math.min(this.init_List.size(), this.tick_I + 6); j < end; j++) {
            ClassCache.UiHelper later = this.init_List.get(j);
            if (!push(later)) {
               return false;
            }

            if (j >= this.tick_I + 2 && at.equals(later.from())) {
               for (int k = this.tick_I; k < j; k++) {
                  this.get_D = this.get_D + this.init_List.get(k).costTicks();
               }

               this.tick_I = j;
               this.init_ClassCache_UiHelpe = null;
               return true;
            }

            if (at.equals(later.to()) && at.getY() == later.to().getY()) {
               for (int k = this.tick_I; k <= j; k++) {
                  this.get_D = this.get_D + this.init_List.get(k).costTicks();
               }

               this.tick_I = j + 1;
               this.init_ClassCache_UiHelpe = null;
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private BlockPos init_BlockPos_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      int i = this.init_I_ClassCache_UiHelpe(m);
      return i < 0 ? null : this.init_List.get(i).to();
   }

   private int init_I_LocalPlayer_I(LocalPlayer player, int upTo) {
      int best = this.tick_I;
      double bestDistance = 1.7976931348623157E308;

      for (int j = this.tick_I; j <= upTo && j < this.init_List.size(); j++) {
         BlockPos from = this.init_List.get(j).from();
         double d = Math.hypot(player.getX() - (from.getX() + 0.5), player.getZ() - (from.getZ() + 0.5));
         if (d < bestDistance) {
            bestDistance = d;
            best = j;
         }
      }

      return best;
   }

   private int init_I_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      LocalPlayer p = this.init_Minecraft.player;
      if (p != null && push(m)) {
         int best = -1;
         Map<Long, Boolean> seen = new HashMap<>();
         int i = this.tick_I + 1;

         for (int end = Math.min(this.init_List.size(), this.tick_I + 6); i < end; best = i++) {
            ClassCache.UiHelper ahead = this.init_List.get(i);
            if (!push(ahead) || ahead.to().getY() != m.to().getY() || !this.init_Z_D(p.getX(), p.getZ(), ahead.to(), seen)) {
               break;
            }
         }

         return best;
      } else {
         return -1;
      }
   }

   private boolean init_Z_D(double fromX, double fromZ, BlockPos to, Map<Long, Boolean> seen) {
      double tx = to.getX() + 0.5;
      double tz = to.getZ() + 0.5;
      double dx = tx - fromX;
      double dz = tz - fromZ;
      double length = Math.hypot(dx, dz);
      if (length < 1.0E-6) {
         return true;
      }

      int steps = (int)Math.ceil(length / 0.25);

      for (int s = 0; s <= steps; s++) {
         double x = fromX + dx * s / steps;
         double z = fromZ + dz * s / steps;

         for (int cx = -1; cx <= 1; cx += 2) {
            for (int cz = -1; cz <= 1; cz += 2) {
               BlockPos cell = new BlockPos((int)Math.floor(x + cx * 0.36), to.getY(), (int)Math.floor(z + cz * 0.36));
               if (!seen.computeIfAbsent(
                  cell.asLong(),
                  c -> this.init_Predicate.test(cell)
                     && init_Z_BlockPos_Function(cell, this::load_ClassCache_FileCac_BlockPos)
                     && !this.write_Z_BlockPos(cell)
                     && !this.write_Z_BlockPos(cell.above())
               )) {
                  return false;
               }
            }
         }

         for (int ix = (int)Math.floor(x - 0.55); ix <= (int)Math.floor(x + 0.55); ix++) {
            for (int iz = (int)Math.floor(z - 0.55); iz <= (int)Math.floor(z + 0.55); iz++) {
               long key = BlockPos.asLong(ix, to.getY(), iz) ^ 6510516211317473281L;
               BlockPos near = new BlockPos(ix, to.getY(), iz);
               if (!seen.computeIfAbsent(
                  key,
                  c -> !this.load_ClassCache_FileCac_BlockPos(near).contactHazard() && !this.load_ClassCache_FileCac_BlockPos(near.above()).contactHazard()
               )) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private Vec3 load_Vec3_ClassCache_UiHelpe_D(ClassCache.UiHelper m, double eyeY) {
      BlockPos pulled = this.init_BlockPos_ClassCache_UiHelpe(m);
      if (pulled != null) {
         return new Vec3(pulled.getX() + 0.5, eyeY, pulled.getZ() + 0.5);
      }

      int dx = Integer.signum(m.to().getX() - m.from().getX());
      int dz = Integer.signum(m.to().getZ() - m.from().getZ());
      BlockPos focus = m.to();
      int i = this.tick_I + 1;

      for (int end = Math.min(this.init_List.size(), this.tick_I + 4); i < end; i++) {
         ClassCache.UiHelper ahead = this.init_List.get(i);
         if (!ahead.cellsToBreak().isEmpty()
            || !ahead.cellsToPlace().isEmpty()
            || ahead.type() == ClassCache.Inner_SyncManager.set
            || Integer.signum(ahead.to().getX() - ahead.from().getX()) != dx
            || Integer.signum(ahead.to().getZ() - ahead.from().getZ()) != dz) {
            break;
         }

         focus = ahead.to();
      }

      return new Vec3(focus.getX() + 0.5, eyeY, focus.getZ() + 0.5);
   }

   static Vec3 init_Vec3_ClassCache_UiHelpe_D(ClassCache.UiHelper m, double eyeY) {
      if (!m.cellsToBreak().isEmpty()) {
         return Vec3.atCenterOf(m.cellsToBreak().get(0));
      } else {
         return !m.cellsToPlace().isEmpty() ? Vec3.atCenterOf(m.cellsToPlace().get(0)) : new Vec3(m.to().getX() + 0.5, eyeY, m.to().getZ() + 0.5);
      }
   }

   private void get() {
      this.init_V_BlockPos_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe == null ? null : this.init_ClassCache_UiHelpe.to(), this.init_ClassCache_UiHelpe);
   }

   private void init_V_Str(String why) {
      this.load_Str = why;
      this.get();
   }

   public String init_Str() {
      return this.load_Str;
   }

   private String init_Str_ClassCache_UiHelpe(ClassCache.UiHelper m) {
      if (m != null && this.init_Minecraft.level != null) {
         BlockPos t = m.to();
         Function<BlockPos, String> name = p -> BuiltInRegistries.BLOCK.getKey(this.init_Minecraft.level.getBlockState(p).getBlock()).getPath();
         return " (target feet " + name.apply(t) + ", head " + name.apply(t.above()) + ", floor " + name.apply(t.below()) + ")";
      } else {
         return "";
      }
   }

   private void init_V_BlockPos_ClassCache_UiHelpe(BlockPos target, ClassCache.UiHelper m) {
      if (Boolean.getBoolean("autism.navigation.debug") && this.init_Minecraft.player != null) {
         ListPool.init(
            "stall move="
               + m
               + " position="
               + this.init_Minecraft.player.position()
               + " motion="
               + this.init_Minecraft.player.getDeltaMovement()
               + " grounded="
               + this.init_Minecraft.player.onGround()
               + " committed="
               + this.close_Z
               + " airborne="
               + this.poll_Z
               + " ticks="
               + this.run_I
         );
      }

      this.read_BlockPos = target;
      this.load_ClassCache_UiHelpe = m;
      this.init_RefQueue_TaskQueue = RefQueue.Inner_TaskQueue.write;
      this.add();
   }

   public void close_V() {
      this.reset_Z = false;
   }

   private boolean close(BlockPos cell) {
      return this.load_ClassCache_FileCac_BlockPos(cell).passable()
         ? false
         : this.init_BlockPos == null || this.init_Minecraft.level.getBlockState(cell).getBlock() == this.init_Block && ca.init_J_BlockPos(cell) > this.read_J;
   }

   private RefQueue.Inner_BufferHelper load_RefQueue_BufferHel_BlockPos(BlockPos cell) {
      int slot = this.load_I_BlockPos(cell);
      if (slot < 0) {
         if (this.tick_Z()) {
            return RefQueue.Inner_BufferHelper.load;
         } else {
            return this.run_Z() ? RefQueue.Inner_BufferHelper.load : RefQueue.Inner_BufferHelper.read;
         }
      } else {
         this.load_V_I(slot);
         ItemStack stack = this.init_Minecraft.player.getInventory().getItem(slot);
         this.init_Str = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
         U.Inner_StateCache request = new U.Inner_StateCache(
               cell,
               this.init_Minecraft.player.getEyePosition(),
               this.init_Minecraft.player.blockInteractionRange(),
               U.init(stack, this.init_Minecraft.player, InteractionHand.MAIN_HAND)
            )
            .init_U_StateCache_Z(this.init_Minecraft.player.isShiftKeyDown() && this.init_Minecraft.player.getLastSentInput().shift())
            .init_U_StateCache_U_StreamUtil(new U.Inner_StreamUtil(6));
         U.Inner_JsonUtil[] refusal = new U.Inner_JsonUtil[1];
         U.Inner_CachePool candidate = U.init(request, refusal);
         if (candidate == null) {
            if (init_Z) {
               ListPool.init(
                  "place no-face cell="
                     + cell
                     + " attempts="
                     + this.get_I
                     + " refusal="
                     + refusal[0]
                     + String.format(
                        Locale.ROOT,
                        " eye %.3f %.3f %.3f ground %s shift %s ticks %d",
                        this.init_Minecraft.player.getEyePosition().x,
                        this.init_Minecraft.player.getEyePosition().y,
                        this.init_Minecraft.player.getEyePosition().z,
                        this.init_Minecraft.player.onGround(),
                        this.init_Minecraft.options.keyShift.isDown(),
                        this.run_I
                     )
               );
            }

            return RefQueue.Inner_BufferHelper.read;
         } else {
            this.init_DataUtil.init_V_DataUtil_DataUtil(DataUtil.Inner_DataUtil.write);
            ce.Inner_DataUtil goal = candidate.init_U_DataUtil().init_ce_DataUtil();
            Vec3 eye = this.init_Minecraft.player.getEyePosition();
            Vec3 point = candidate.init_U_DataUtil().init_Vec3();
            boolean pillarSupport = this.init_ClassCache_UiHelpe != null && this.init_ClassCache_UiHelpe.type() == ClassCache.Inner_SyncManager.tick;
            if (pillarSupport && goal != null && point != null && Math.hypot(point.x - eye.x, point.z - eye.z) < 0.4) {
               this.init_DataUtil.init_V_Vec3(point);
            } else if (goal != null) {
               this.init_DataUtil.init_V_ce_DataUtil(goal);
            } else {
               this.init_DataUtil.init_V_Vec3(candidate.init_U_DataUtil().init_Vec3());
            }

            this.init_DataUtil.load_V();
            if (!DataUtil.init_Z_BlockPos(candidate.init_BlockHitResult().getBlockPos())) {
               return RefQueue.Inner_BufferHelper.load;
            }

            this.read_J = ca.init_J_BlockPos(cell);
            this.init_Block = ((BlockItem)stack.getItem()).getBlock();
            if (init_Z) {
               ListPool.init(
                  "place click cell="
                     + cell
                     + " via="
                     + candidate.init_BlockHitResult().getBlockPos()
                     + "/"
                     + candidate.init_BlockHitResult().getDirection()
                     + " rev="
                     + this.read_J
               );
            }

            InteractionResult result = this.init_Minecraft
               .gameMode
               .useItemOn(this.init_Minecraft.player, InteractionHand.MAIN_HAND, candidate.init_BlockHitResult());
            if (result.consumesAction() && !this.init_Minecraft.level.getBlockState(cell).isAir()) {
               init_V_BlockPos_Block(cell, this.init_Block);
            }

            return RefQueue.Inner_BufferHelper.init;
         }
      }
   }

   private int poll_I() {
      int best = -1;
      int bestRank = 2147483647;
      int bestCount = -1;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = this.init_Minecraft.player.getInventory().getItem(i);
         if (stack.getItem() instanceof BlockItem block
            && !(block.getBlock() instanceof FallingBlock)
            && block.getBlock().defaultBlockState().isCollisionShapeFullBlock(this.init_Minecraft.level, this.init_Minecraft.player.blockPosition())
            && !block.getBlock().defaultBlockState().hasBlockEntity()) {
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            int rank = UtilCore.init_I(id);
            if (rank >= 0 && UtilCore.init(stack, this.init_Minecraft.player.getInventory())) {
               if (id.equals(this.init_Str)) {
                  rank = -1;
               }

               if (rank < bestRank || rank == bestRank && stack.getCount() > bestCount) {
                  best = i;
                  bestRank = rank;
                  bestCount = stack.getCount();
               }
            }
         }
      }

      return best;
   }

   private BlockState init_BlockState() {
      int slot = this.poll_I();
      return slot >= 0 && this.init_Minecraft.player.getInventory().getItem(slot).getItem() instanceof BlockItem b ? b.getBlock().defaultBlockState() : null;
   }

   private boolean tick_Z() {
      Item wanted = this.init_Item();
      if (!this.set_Z && !this.clear_Z && wanted == null) {
         return false;
      }

      if (this.init_UtilCore == null) {
         this.init_UtilCore = new UtilCore(this.init_Minecraft);
      }

      if (!this.init_UtilCore.init_Z() && !this.put_Z) {
         List<UtilCore.Inner_CachePool> backpack = new ArrayList<>(27);

         for (int slot = 9; slot < 36; slot++) {
            backpack.add(this.init_UtilCore_CachePool_ItemStack(this.init_Minecraft.player.getInventory().getItem(slot)));
         }

         int index = -1;
         if (wanted != null) {
            for (int slot = 9; slot < 36 && index < 0; slot++) {
               if (this.init_Minecraft.player.getInventory().getItem(slot).is(wanted)) {
                  index = slot - 9;
               }
            }

            if (index < 0) {
               return false;
            }
         } else {
            index = UtilCore.init(backpack, this.init_Str);
         }

         if (index < 0) {
            return false;
         }

         List<UtilCore.Inner_DataUtil> hotbar = new ArrayList<>(9);

         for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = this.init_Minecraft.player.getInventory().getItem(slot);
            hotbar.add(
               new UtilCore.Inner_DataUtil(stack.isEmpty(), stack.getItem() instanceof BlockItem, stack.has(DataComponents.FOOD), stack.isDamageableItem())
            );
         }

         int destination = UtilCore.init(hotbar);
         if (destination < 0) {
            return false;
         }

         String id = wanted != null ? BuiltInRegistries.ITEM.getKey(wanted).toString() : backpack.get(index).itemId();
         this.init_UtilCore.init_V_I(9 + index, destination, id);
         return true;
      } else {
         return false;
      }
   }

   private boolean run_Z() {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      if (this.init_AiOperation != null || this.add_Z || this.init_Minecraft.player == null) {
         return false;
      }

      if (UtilCore.init(this.init_Minecraft.player.getInventory()) <= 0) {
         return false;
      }

      int wanted = 0;

      for (int i = this.tick_I; i < this.init_List.size(); i++) {
         wanted += this.init_List.get(i).cellsToPlace().size();
      }

      this.init_AiOperation = aV.init(this.init_Minecraft.player.getInventory(), Math.max(1, wanted));
      if (this.init_AiOperation == null) {
         this.add_Z = true;
         return false;
      }

      if (init_Z) {
         ListPool.init("plank craft start for " + wanted + " placements");
      }

      return true;
   }

   private UtilCore.Inner_CachePool init_UtilCore_CachePool_ItemStack(ItemStack stack) {
      return stack.getItem() instanceof BlockItem block && UtilCore.init(stack, this.init_Minecraft.player.getInventory())
         ? new UtilCore.Inner_CachePool(
            BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),
            stack.getCount(),
            block.getBlock() instanceof FallingBlock,
            block.getBlock().defaultBlockState().isCollisionShapeFullBlock(this.init_Minecraft.level, this.init_Minecraft.player.blockPosition()),
            block.getBlock().defaultBlockState().hasBlockEntity()
         )
         : new UtilCore.Inner_CachePool(null, 0, false, false, false);
   }

   private boolean open(BlockPos cell) {
      if (this.set_Z && this.init_UtilCore != null && !this.init_UtilCore.init_Z() && !this.put_Z) {
         BlockState state = this.init_Minecraft.level.getBlockState(cell);
         int hotbarTool = autismclient.util.ArrayUtil.init(this.init_Minecraft, state, 9, false);
         if (hotbarTool < 0
            || state.requiresCorrectToolForDrops()
               && ClassCache.Inner_StreamUtil.update(state)
               && !this.init_Minecraft.player.getInventory().getItem(hotbarTool).isCorrectToolForDrops(state)) {
            if (v.init(this.init_List_BlockState(state)) instanceof v.Inner_DataUtil.Inner_CachePool swap) {
               int var6 = swap.backpackSlot();
               this.init_UtilCore
                  .init_V_I(
                     var6,
                     this.init_Minecraft.player.getInventory().getSelectedSlot(),
                     BuiltInRegistries.ITEM.getKey(this.init_Minecraft.player.getInventory().getItem(var6).getItem()).toString()
                  );
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private List<v.Inner_StreamUtil> init_List_BlockState(BlockState state) {
      boolean handsOk = !state.requiresCorrectToolForDrops();
      List<v.Inner_StreamUtil> out = new ArrayList<>(36);

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = this.init_Minecraft.player.getInventory().getItem(slot);
         out.add(
            new v.Inner_StreamUtil(
               slot,
               stack.isDamageableItem() ? stack.getMaxDamage() - stack.getDamageValue() : -1,
               handsOk || stack.isCorrectToolForDrops(state),
               af.load_Z(stack)
            )
         );
      }

      return out;
   }

   private void load_V_I(int slot) {
      if (this.value_I == -1) {
         this.cache_I = this.init_Minecraft.player.getInventory().getSelectedSlot();
      }

      if (this.init_Minecraft.player.getInventory().getSelectedSlot() != slot) {
         this.init_Minecraft.player.getInventory().setSelectedSlot(slot);
         this.value_I = slot;
      }
   }

   private void init_V_Vec3(Vec3 target) {
      this.read_Vec3 = target;
      LocalPlayer p = this.init_Minecraft.player;
      if (this.sync_BlockPos != null && !this.init_RefQueue_DataUtil.init_Z_BlockPos(this.sync_BlockPos)) {
         this.sync_BlockPos = null;
      }

      if (this.init_RefQueue_DataUtil.load() && this.init_RefQueue_DataUtil.init_BlockPos != null) {
         this.sync_BlockPos = this.init_RefQueue_DataUtil.init_BlockPos;
      }

      if (p != null && !this.init_RefQueue_DataUtil.load()) {
         Vec3 eye = p.getEyePosition();
         if (Mth.floor(target.x) == Mth.floor(eye.x)
            && Mth.floor(target.z) == Mth.floor(eye.z)
            && Math.abs(target.y - eye.y) > 1.0
            && (
               this.init_ClassCache_UiHelpe == null
                  || this.init_ClassCache_UiHelpe.cellsToBreak().isEmpty()
                  || this.init_ClassCache_UiHelpe.cellsToBreak().contains(BlockPos.containing(target))
            )) {
            BlockPos block = BlockPos.containing(target);
            VoxelShape outline = this.init_Minecraft.level == null
               ? null
               : this.init_Minecraft.level.getBlockState(block).getShape(this.init_Minecraft.level, block);
            boolean below = target.y < eye.y;
            double face = outline != null && !outline.isEmpty()
               ? block.getY() + (below ? outline.max(Axis.Y) : outline.min(Axis.Y))
               : block.getY() + (below ? 1.0 : 0.0);
            this.init_DataUtil.init_V_ce_DataUtil(new ce.Inner_DataUtil(p.getYRot(), init_F_Vec3(eye, p.getYRot(), p.getXRot(), below, face)));
            return;
         }

         if (this.init_ClassCache_UiHelpe != null && !this.init_ClassCache_UiHelpe.cellsToBreak().isEmpty() && !this.state_Z && this.sync_BlockPos == null) {
            int dx = this.init_ClassCache_UiHelpe.to().getX() - this.init_ClassCache_UiHelpe.from().getX();
            int dz = this.init_ClassCache_UiHelpe.to().getZ() - this.init_ClassCache_UiHelpe.from().getZ();
            if (dx == 0 != (dz == 0)
               && Mth.floor(target.x) == this.init_ClassCache_UiHelpe.to().getX()
               && Mth.floor(target.z) == this.init_ClassCache_UiHelpe.to().getZ()) {
               double lateral = dx != 0
                  ? Math.abs(p.getZ() - (this.init_ClassCache_UiHelpe.from().getZ() + 0.5))
                  : Math.abs(p.getX() - (this.init_ClassCache_UiHelpe.from().getX() + 0.5));
               if (lateral < 0.3) {
                  double h = Math.hypot(target.x - eye.x, target.z - eye.z);
                  float pitch = (float)Math.toDegrees(Math.atan2(eye.y - target.y, Math.max(0.3, h)));
                  float yaw = NodeWalker.init_F_D(0.0, 0.0, dx, dz);
                  if (this.init_Z_Vec3(eye, yaw, pitch, BlockPos.containing(target))) {
                     this.init_DataUtil.init_V_ce_DataUtil(new ce.Inner_DataUtil(yaw, pitch));
                     return;
                  }
               }
            }
         }
      }

      this.init_DataUtil.init_V_Vec3(target);
   }

   private boolean init_Z_Vec3(Vec3 eye, float yaw, float pitch, BlockPos cell) {
      Vec3 end = eye.add(Vec3.directionFromRotation(pitch, yaw).scale(Math.max(4.5, this.init_Minecraft.player.blockInteractionRange())));
      BlockHitResult hit = this.init_Minecraft
         .level
         .clip(new ClipContext(eye, end, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, this.init_Minecraft.player));
      return hit.getType() == Type.BLOCK && hit.getBlockPos().equals(cell);
   }

   static float init_F_Vec3(Vec3 eye, float yaw, float pitch, Vec3 target) {
      boolean below = target.y < eye.y;
      int by = Mth.floor(target.y);
      return init_F_Vec3(eye, yaw, pitch, below, below ? by + 1 : by);
   }

   static float init_F_Vec3(Vec3 eye, float yaw, float pitch, boolean below, double face) {
      double h = below ? eye.y - face : face - eye.y;
      if (h <= 0.0) {
         return below ? 90.0F : -90.0F;
      } else {
         double rad = Math.toRadians(yaw);
         double dx = -Math.sin(rad);
         double dz = Math.cos(rad);
         int cx = Mth.floor(eye.x);
         int cz = Mth.floor(eye.z);
         double tx = dx > 1.0E-9 ? (cx + 1 - eye.x) / dx : (dx < -1.0E-9 ? (cx - eye.x) / dx : 1.7976931348623157E308);
         double tz = dz > 1.0E-9 ? (cz + 1 - eye.z) / dz : (dz < -1.0E-9 ? (cz - eye.z) / dz : 1.7976931348623157E308);
         double edge = Math.min(tx, tz);
         float keep = (float)Math.toDegrees(Math.atan2(h, Math.max(0.02, edge - 0.1)));
         float need = (float)Math.min(90.0, Math.toDegrees(Math.atan2(h, Math.max(0.02, edge - 0.3))));
         if (below) {
            return pitch >= keep ? pitch : need;
         } else {
            return -pitch >= keep ? pitch : -need;
         }
      }
   }

   static boolean init_Z_F(float lastTurn, int ticksSince, float turn) {
      return Math.abs(turn) > 15.0F && lastTurn != 0.0F && Math.signum(turn) != Math.signum(lastTurn) && ticksSince <= 6;
   }

   private boolean flush(LocalPlayer player) {
      this.bind++;
      float yaw = player.getYRot();
      boolean consecutive = player.tickCount == this.f_b_I + 1;
      this.f_b_I = player.tickCount;
      if ((this.init_PoolEntry == null || this.prev_I <= 0 || this.pop_Z) && (!this.close_Z || player.onGround())) {
         if (!consecutive) {
            this.load_ArrayDeque.addLast("|");
         }

         if (!Float.isNaN(this.pop_F) && consecutive) {
            float turn = Mth.wrapDegrees(yaw - this.pop_F);
            this.load_ArrayDeque.addLast(String.format(Locale.ROOT, "%.0f", turn) + (player.onGround() ? "" : "a"));

            while (this.load_ArrayDeque.size() > 12) {
               this.load_ArrayDeque.pollFirst();
            }

            if (Math.abs(turn) > 15.0F) {
               if (init_Z_F(this.tick_F, this.bind - this.f_a_I, turn)) {
                  this.init_ArrayDeque.addLast(this.bind);
               }

               this.tick_F = turn;
               this.f_a_I = this.bind;
            }
         }

         this.pop_F = yaw;

         while (!this.init_ArrayDeque.isEmpty() && this.bind - this.init_ArrayDeque.peekFirst() > 20) {
            this.init_ArrayDeque.pollFirst();
         }

         if (this.init_ArrayDeque.size() < 3
            || this.init_ClassCache_UiHelpe == null
            || this.update_Z_ClassCache_UiHelpe(this.init_ClassCache_UiHelpe)
            || this.init_NetHelper != null) {
            return false;
         }

         if (!player.onGround() && !player.isInWater() && !player.onClimbable()) {
            return false;
         }

         this.init_ArrayDeque.clear();
         AutismClientAddon.LOG
            .info(
               "[AI] JITTER: camera reversed {} times in a second on {} {} -> {} at {} (aim {}, breaks {}, places {}, turns {}); replanning from here",
               3,
               this.init_ClassCache_UiHelpe.type(),
               this.init_ClassCache_UiHelpe.from().toShortString(),
               this.init_ClassCache_UiHelpe.to().toShortString(),
               player.position(),
               this.read_Vec3,
               this.init_ClassCache_UiHelpe.cellsToBreak(),
               this.init_ClassCache_UiHelpe.cellsToPlace(),
               this.load_ArrayDeque
            );
         this.init_V_Str("camera jitter");
         return true;
      } else {
         this.pop_F = yaw;
         this.tick_F = 0.0F;
         this.init_ArrayDeque.clear();
         this.load_ArrayDeque.clear();
         return false;
      }
   }

   private boolean close(LocalPlayer player) {
      return player.getFoodData().getFoodLevel() > 6;
   }

   private void set() {
      this.init_Minecraft.options.keyUp.setDown(false);
      this.init_Minecraft.options.keyDown.setDown(false);
      this.init_Minecraft.options.keyLeft.setDown(false);
      this.init_Minecraft.options.keyRight.setDown(false);
      this.init_Minecraft.options.keySprint.setDown(false);
      this.init_Minecraft.options.keyJump.setDown(false);
      this.init_Minecraft.options.keyShift.setDown(false);
      if (this.init_Minecraft.player != null) {
         this.init_Minecraft.player.setSprinting(false);
      }
   }

   private void put() {
      ClassCache.UiHelper next = this.tick_I < this.init_List.size() ? this.init_List.get(this.tick_I) : null;
      boolean nextSprints = next != null
         && this.init_Minecraft.player != null
         && this.close(this.init_Minecraft.player)
         && (
            init_Z_ClassCache_SyncMan_Z(next.type(), true) && (next.jump() == null || next.jump().sprint())
               || next.type() == ClassCache.Inner_SyncManager.sync && this.tick_I < this.init_List.size() - 1
         );
      boolean keepSprint = this.init_Minecraft.player != null && this.init_Minecraft.player.isSprinting() && nextSprints;
      this.set();
      if (keepSprint) {
         this.init_V_LocalPlayer_Z(this.init_Minecraft.player, true);
      }
   }

   private void add() {
      this.update_V_Z(true);
   }

   private void update_V_Z(boolean restoreSlot) {
      this.init_NetHelper = null;
      this.read_Set.clear();
      this.init_RefQueue_DataUtil.init_V();
      this.clear();
      this.load_BlockPos = null;
      if (this.init_UtilCore != null) {
         this.init_UtilCore.init_V();
      }

      if (this.init_AiOperation != null) {
         this.init_AiOperation.cancel();
         this.init_AiOperation = null;
      }

      if (restoreSlot) {
         if (this.init_Minecraft.player != null && this.value_I >= 0 && this.init_Minecraft.player.getInventory().getSelectedSlot() == this.value_I) {
            this.init_Minecraft.player.getInventory().setSelectedSlot(this.cache_I);
         }

         this.value_I = -1;
      }
   }

   private void clear() {
      open_V();
   }

   public static int init_I_List(List<ClassCache.UiHelper> path) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return -1;
      }

      Function<BlockPos, ClassCache.Inner_FileCache> probe = cell -> ClassCache.Inner_StreamUtil.init_ClassCache_FileCac_Minecraft(
         mc, mc.level, cell, init_ItemStackArr
      );
      return !path.isEmpty() && !init_Z_ClassCache_UiHelpe_4(path.getFirst(), probe, true) ? 0 : init_I_List(path, 0, path.size(), probe, true);
   }

   public static void open_V() {
      init_LocalPlayer = null;
      Minecraft mc = Minecraft.getInstance();
      if (mc.options != null) {
         for (KeyMapping key : List.of(
            mc.options.keyUp, mc.options.keyDown, mc.options.keyLeft, mc.options.keyRight, mc.options.keyJump, mc.options.keySprint, mc.options.keyShift
         )) {
            key.setDown(aB.init(key).autism$physicalForBody());
         }
      }
   }

   enum Inner_BufferHelper {
      init,
      load,
      read;
   }

   static final class Inner_CachePool {
      private final ArrayDeque<long[]> init = new ArrayDeque<>();

      boolean init(ClassCache.UiHelper m, long now) {
         while (!this.init.isEmpty() && now - ((long[])this.init.peekFirst())[1] > 45000000000L) {
            this.init.pollFirst();
         }

         long sig = RefQueue.init_J_ClassCache_UiHelpe(m);
         Iterator<long[]> it = this.init.descendingIterator();

         while (it.hasNext()) {
            long[] e = it.next();
            if (e[0] == sig) {
               if (now - e[1] < 500000000L) {
                  e[1] = now;
                  return false;
               }
               break;
            }
         }

         int seen = 1;

         for (long[] e : this.init) {
            if (e[0] == sig) {
               seen++;
            }
         }

         this.init.addLast(new long[]{sig, now});

         while (this.init.size() > 256) {
            this.init.pollFirst();
         }

         return seen >= 3;
      }

      void init() {
         this.init.clear();
      }
   }

   public interface Inner_ConfigStore {
      Item init_Item(BlockPos var1);

      boolean init_Z(BlockPos var1);

      default boolean load(BlockPos cell) {
         return true;
      }
   }

   public static final class Inner_DataUtil {
      private static final double read_D = 9.0;
      private final Minecraft init_Minecraft;
      private final UtilCore init_UtilCore;
      private final boolean init_Z;
      private final boolean load_Z;
      private final IntConsumer init_IntConsumer;
      private final Consumer<Vec3> init_Consumer;
      private BlockPos init_BlockPos;
      private BlockPos load_BlockPos;
      private BlockState init_BlockState;
      private int update;
      private int sync;
      private int flush;
      private float init_F;
      private float load_F;
      static final int init_I = 10;
      private boolean read_Z;
      private final Runnable init_Runnable;
      private final BooleanSupplier init_BooleanSupplier;
      private String load_Str = "";
      private BiPredicate<BlockState, ItemStack> init_BiPredicate = (state, stack) -> true;
      private Set<BlockPos> init_Set = Set.of();
      private Consumer<ce.Inner_DataUtil> load_Consumer;
      private Vec3 init_Vec3;
      private float read_F;
      static final double init_D = 0.3;
      static final double load_D = 6.0;
      private static int close = -2147483648;
      public static final String init_Str = "too little air to break it under water";
      public static final int load_I = 6;
      public static final int read_I = 40;
      public static final int write_I = 24;
      private Vec3 load_Vec3;
      private BlockPos read_BlockPos;
      private Vec3 read_Vec3;
      private Vec3 write_Vec3;
      private BlockPos write_BlockPos;
      private BlockState load_BlockState;

      public String init_Str() {
         return this.load_Str;
      }

      private RefQueue.Inner_DataUtil.Inner_DataUtil_2 init_RefQueue_DataUtil__Str(String why) {
         this.load_Str = why;
         return RefQueue.Inner_DataUtil.Inner_DataUtil_2.read;
      }

      public Inner_DataUtil(Minecraft mc, IntConsumer slotSelector) {
         this(mc, slotSelector, point -> {});
      }

      public Inner_DataUtil(Minecraft mc, IntConsumer slotSelector, Consumer<Vec3> aimAt) {
         this(mc, slotSelector, aimAt, false);
      }

      public Inner_DataUtil(Minecraft mc, IntConsumer slotSelector, Consumer<Vec3> aimAt, boolean intentionalTarget) {
         this(mc, slotSelector, aimAt, intentionalTarget, intentionalTarget);
      }

      public Inner_DataUtil(Minecraft mc, IntConsumer slotSelector, Consumer<Vec3> aimAt, boolean intentionalTarget, boolean requireDrops) {
         this(mc, slotSelector, aimAt, intentionalTarget, requireDrops, () -> {});
      }

      public Inner_DataUtil(Minecraft mc, IntConsumer slotSelector, Consumer<Vec3> aimAt, boolean intentionalTarget, boolean requireDrops, Runnable aimTick) {
         this(mc, slotSelector, aimAt, intentionalTarget, requireDrops, aimTick, () -> false);
      }

      public Inner_DataUtil(
         Minecraft mc,
         IntConsumer slotSelector,
         Consumer<Vec3> aimAt,
         boolean intentionalTarget,
         boolean requireDrops,
         Runnable aimTick,
         BooleanSupplier instantAim
      ) {
         this.init_BooleanSupplier = instantAim;
         this.init_Runnable = aimTick;
         this.load_Z = requireDrops;
         this.init_Minecraft = mc;
         this.init_UtilCore = new UtilCore(mc);
         this.init_Z = intentionalTarget;
         this.init_IntConsumer = slotSelector;
         this.init_Consumer = aimAt;
      }

      public RefQueue.Inner_DataUtil init_RefQueue_DataUtil_Consumer(Consumer<ce.Inner_DataUtil> turn) {
         this.load_Consumer = turn;
         return this;
      }

      public RefQueue.Inner_DataUtil init_RefQueue_DataUtil_BiPredicate(BiPredicate<BlockState, ItemStack> accepted) {
         this.init_BiPredicate = accepted;
         return this;
      }

      public RefQueue.Inner_DataUtil init_RefQueue_DataUtil_Set(Set<BlockPos> cells) {
         this.init_Set = Set.copyOf(cells);
         return this;
      }

      public RefQueue.Inner_DataUtil.Inner_DataUtil_2 init_RefQueue_DataUtil__BlockPos(BlockPos target) {
         return this.init_RefQueue_DataUtil__BlockPos_Vec3(target, null);
      }

      public RefQueue.Inner_DataUtil.Inner_DataUtil_2 init_RefQueue_DataUtil__BlockPos_Vec3(BlockPos target, Vec3 aimOverride) {
         return this.init_RefQueue_DataUtil__BlockPos(target, aimOverride, null);
      }

      public RefQueue.Inner_DataUtil.Inner_DataUtil_2 init_RefQueue_DataUtil__BlockPos(BlockPos target, Vec3 aimOverride, Vec3 glide) {
         if (this.read_Z && !target.equals(this.init_BlockPos)) {
            this.init_V();
         }

         if (!AutismLiteVariant.enabled()) {
            cG.init(this, target);
         }

         this.init_Vec3 = glide;
         this.write_Vec3 = aimOverride;
         if (this.init_Minecraft.player != null
            && this.init_Minecraft.level != null
            && this.init_Minecraft.gameMode != null
            && this.init_Minecraft.level.getChunkSource().getChunk(target.getX() >> 4, target.getZ() >> 4, ChunkStatus.FULL, false) != null) {
            if (this.init_UtilCore.init_Z()) {
               UtilCore.Inner_BufferHelper result = this.init_UtilCore.load_UtilCore_BufferHel();
               if (result == UtilCore.Inner_BufferHelper.write) {
                  this.init_V();
                  return this.init_RefQueue_DataUtil__Str("tool swap failed");
               }

               if (result != UtilCore.Inner_BufferHelper.read) {
                  return RefQueue.Inner_DataUtil.Inner_DataUtil_2.init;
               }

               this.init_UtilCore.init_V();
            }

            if (this.read_Z && target.equals(this.init_BlockPos)) {
               if (++this.update > this.sync) {
                  this.init_V();
                  return this.init_RefQueue_DataUtil__Str("took longer than " + this.sync + " ticks");
               }

               BlockState now = this.init_Minecraft.level.getBlockState(this.init_BlockPos);
               if (now.getBlock() != this.init_BlockState.getBlock() && RefQueue.init_Z_BlockState(now) && RefQueue.init_Z_BlockState(this.init_BlockState)) {
                  this.init_BlockState = now;
               }

               if (now.getBlock() != this.init_BlockState.getBlock()) {
                  if (!now.isAir() && now.getFluidState().isEmpty()) {
                     this.init_V();
                     return this.init_RefQueue_DataUtil__Str("turned into " + now.getBlock().getName().getString());
                  } else {
                     FileCache.init(this.init_BlockPos, this.init_BlockState);
                     this.init_V();
                     return RefQueue.Inner_DataUtil.Inner_DataUtil_2.load;
                  }
               } else {
                  if (now.getDestroySpeed(this.init_Minecraft.level, this.init_BlockPos) < 0.0F
                     || now.hasBlockEntity() && !this.init_Z
                     || !now.getFluidState().isEmpty()) {
                     this.init_V();
                     return this.init_RefQueue_DataUtil__Str("became unbreakable or fluid");
                  }

                  if (!ClassCache.Inner_StreamUtil.init_Z_ClientLevel(this.init_Minecraft.level, this.init_BlockPos, this.init_Set)) {
                     this.init_V();
                     return this.init_RefQueue_DataUtil__Str("fluid appeared next to it");
                  }

                  if (!this.load(this.init_BlockPos)) {
                     this.init_V();
                     return this.init_RefQueue_DataUtil__Str("removing this floor would cause an unsafe fall");
                  }

                  if (this.read(this.init_BlockPos)) {
                     return RefQueue.Inner_DataUtil.Inner_DataUtil_2.init;
                  }

                  BlockHitResult hit = CachePool.init(this.init_Minecraft, this.init_BlockPos);
                  if (hit == null) {
                     if (RefQueue.init_Z) {
                        ListPool.init(
                           "digger lost "
                              + this.init_BlockPos
                              + " eye="
                              + this.init_Minecraft.player.getEyePosition()
                              + " body="
                              + this.init_Minecraft.player.position()
                              + " motion="
                              + this.init_Minecraft.player.getDeltaMovement()
                              + " pose="
                              + this.init_Minecraft.player.getPose()
                              + " climb="
                              + this.init_Minecraft.player.onClimbable()
                              + " shift="
                              + this.init_Minecraft.options.keyShift.isDown()
                        );
                     }

                     this.init_V();
                     return this.init_RefQueue_DataUtil__Str("lost sight of it while breaking");
                  } else {
                     int slot = r.init_I_Minecraft_2(this.init_Minecraft, now, this.load_Z, this.init_BiPredicate);
                     if (slot < 0) {
                        this.init_V();
                        return this.init_RefQueue_DataUtil__Str("no tool left that gets its drops");
                     }

                     if (slot >= 9) {
                        String item = BuiltInRegistries.ITEM.getKey(this.init_Minecraft.player.getInventory().getItem(slot).getItem()).toString();
                        this.init_V();
                        this.init_UtilCore.init_V_I(slot, this.init_Minecraft.player.getInventory().getSelectedSlot(), item);
                        return RefQueue.Inner_DataUtil.Inner_DataUtil_2.init;
                     }

                     this.init_IntConsumer.accept(slot);
                     Vec3 point = this.init_Vec3_BlockPos_BlockHitResult(this.init_BlockPos, hit);
                     ce.Inner_DataUtil across = this.init_ce_DataUtil();
                     if (across != null) {
                        this.load_Consumer.accept(across);
                     } else {
                        this.init_Consumer.accept(point);
                     }

                     this.init_Runnable.run();
                     TaskTimer.init(this.init_Minecraft);
                     if (!DataUtil.init_Z_BlockPos(this.init_BlockPos)) {
                        init_V_Minecraft(this.init_Minecraft);
                        float yaw = this.init_Minecraft.player.getYRot();
                        float pitch = this.init_Minecraft.player.getXRot();
                        boolean turning = Math.abs(Mth.wrapDegrees(yaw - this.init_F)) + Math.abs(pitch - this.load_F) > 0.5F;
                        this.init_F = yaw;
                        this.load_F = pitch;
                        if (RefQueue.load_Z && !turning) {
                           BlockHitResult ray = DataUtil.init_BlockHitResult();
                           ListPool.init(
                              "digger off "
                                 + this.init_BlockPos
                                 + " ray "
                                 + (ray == null ? "-" : ray.getBlockPos() + " " + ray.getDirection())
                                 + " yaw="
                                 + yaw
                                 + " pitch="
                                 + pitch
                                 + " eye="
                                 + this.init_Minecraft.player.getEyePosition()
                           );
                        }

                        if (!turning && ++this.flush > 10) {
                           this.init_V();
                           return this.init_RefQueue_DataUtil__Str("crosshair left it mid-break");
                        } else {
                           return RefQueue.Inner_DataUtil.Inner_DataUtil_2.init;
                        }
                     } else {
                        this.flush = 0;
                        this.init_V_BlockPos_Direction(this.init_BlockPos, hit.getDirection());
                        return this.init_RefQueue_DataUtil_();
                     }
                  }
               }
            } else {
               if (this.read_Z) {
                  this.init_V();
               }

               if (!target.equals(this.load_BlockPos)) {
                  this.load_BlockPos = target.immutable();
                  this.flush = 0;
               }

               BlockState initial = this.init_Minecraft.level.getBlockState(target);
               if (initial.isAir()) {
                  if (target.equals(this.write_BlockPos) && this.load_BlockState != null) {
                     FileCache.init(target, this.load_BlockState);
                  }

                  this.write_BlockPos = null;
                  return RefQueue.Inner_DataUtil.Inner_DataUtil_2.load;
               } else if (!(initial.getDestroySpeed(this.init_Minecraft.level, target) < 0.0F)
                  && (!initial.hasBlockEntity() || this.init_Z)
                  && initial.getFluidState().isEmpty()) {
                  if (!ClassCache.Inner_StreamUtil.init_Z_ClientLevel(this.init_Minecraft.level, target, this.init_Set)) {
                     if (RefQueue.init_Z) {
                        ListPool.init("digger fail digSafe " + target);
                     }

                     return this.init_RefQueue_DataUtil__Str("unsafe to dig (fluid next to it)");
                  } else {
                     if (ClassCache.Inner_StreamUtil.init_Z_BlockGetter_BlockPos(this.init_Minecraft.level, target)) {
                        return this.init_RefQueue_DataUtil__Str("a lit portal's frame");
                     }

                     if (!this.load(target)) {
                        return this.init_RefQueue_DataUtil__Str("removing this floor would cause an unsafe fall");
                     }

                     if (target.equals(this.init_Minecraft.player.blockPosition().above(2))
                        && this.init_Minecraft.level.getBlockState(target.above()).getBlock() instanceof FallingBlock) {
                        if (RefQueue.init_Z) {
                           ListPool.init("digger fail falling over head " + target);
                        }

                        return this.init_RefQueue_DataUtil__Str("sand or gravel over it");
                     } else {
                        int best = r.init_I_Minecraft_2(this.init_Minecraft, initial, this.load_Z, this.init_BiPredicate);
                        if (best < 0) {
                           if (RefQueue.init_Z) {
                              ListPool.init("digger fail no-tool " + target);
                           }

                           AutismClientAddon.LOG
                              .info(
                                 "[AI] digger: no tool that gets the drops of {} at {}",
                                 BuiltInRegistries.BLOCK.getKey(initial.getBlock()).getPath(),
                                 target.toShortString()
                              );
                           return this.init_RefQueue_DataUtil__Str("no tool that gets its drops");
                        } else {
                           if (best >= 9) {
                              String item = BuiltInRegistries.ITEM.getKey(this.init_Minecraft.player.getInventory().getItem(best).getItem()).toString();
                              this.init_UtilCore.init_V_I(best, this.init_Minecraft.player.getInventory().getSelectedSlot(), item);
                              return RefQueue.Inner_DataUtil.Inner_DataUtil_2.init;
                           }

                           if (init_Z_Minecraft_D(
                              this.init_Minecraft,
                              r.init_D_Minecraft(
                                 this.init_Minecraft,
                                 this.init_Minecraft.player.getInventory().getItem(best),
                                 initial,
                                 initial.getDestroySpeed(this.init_Minecraft.level, target)
                              )
                           )) {
                              return this.init_RefQueue_DataUtil__Str("too little air to break it under water");
                           }

                           BlockHitResult hit = CachePool.init(this.init_Minecraft, target);
                           if (hit == null) {
                              if (RefQueue.init_Z) {
                                 ListPool.init("digger fail no-hit " + target + " eye=" + this.init_Minecraft.player.getEyePosition());
                              }

                              return this.init_RefQueue_DataUtil__Str("no clickable face in reach");
                           } else {
                              if (this.init_Z()) {
                                 this.init_V_BlockPos_BlockHitResult(target, hit);
                                 init_V_Minecraft(this.init_Minecraft);
                                 return RefQueue.Inner_DataUtil.Inner_DataUtil_2.init;
                              }

                              this.init_Consumer.accept(this.init_Vec3_BlockPos_BlockHitResult(target, hit));
                              this.init_Runnable.run();
                              TaskTimer.init(this.init_Minecraft);
                              if (!DataUtil.init_Z_BlockPos(target)) {
                                 init_V_Minecraft(this.init_Minecraft);
                                 float yaw = this.init_Minecraft.player.getYRot();
                                 float pitch = this.init_Minecraft.player.getXRot();
                                 boolean turning = Math.abs(Mth.wrapDegrees(yaw - this.init_F)) + Math.abs(pitch - this.load_F) > 0.5F;
                                 this.init_F = yaw;
                                 this.load_F = pitch;
                                 if (turning) {
                                    return RefQueue.Inner_DataUtil.Inner_DataUtil_2.init;
                                 }

                                 if (this.init_Minecraft.player.getDeltaMovement().lengthSqr() > 0.008
                                    && !this.init_Minecraft.player.isInWater()
                                    && !this.init_Minecraft.player.onClimbable()) {
                                    return RefQueue.Inner_DataUtil.Inner_DataUtil_2.init;
                                 }

                                 if (++this.flush > 10) {
                                    this.flush = 0;
                                    if (RefQueue.init_Z) {
                                       ListPool.init("digger fail aim " + target);
                                    }

                                    return this.init_RefQueue_DataUtil__Str("crosshair never landed on it");
                                 } else {
                                    if (this.flush > 2) {
                                       this.init_Consumer.accept(hit.getLocation());
                                    }

                                    return RefQueue.Inner_DataUtil.Inner_DataUtil_2.init;
                                 }
                              } else {
                                 this.flush = 0;
                                 this.init_V_BlockPos(target, hit, best);
                                 return this.init_RefQueue_DataUtil_();
                              }
                           }
                        }
                     }
                  }
               } else {
                  if (RefQueue.init_Z) {
                     ListPool.init("digger fail unbreakable/fluid " + target);
                  }

                  return this.init_RefQueue_DataUtil__Str("unbreakable or fluid");
               }
            }
         } else {
            this.init_V();
            return this.init_RefQueue_DataUtil__Str("chunk not loaded");
         }
      }

      private boolean load(BlockPos target) {
         return RefQueue.init_Z_BlockPos(target);
      }

      private RefQueue.Inner_DataUtil.Inner_DataUtil_2 init_RefQueue_DataUtil_() {
         BlockState now = this.init_Minecraft.level.getBlockState(this.init_BlockPos);
         if (now.getBlock() != this.init_BlockState.getBlock() && (now.isAir() || !now.getFluidState().isEmpty())) {
            FileCache.init(this.init_BlockPos, this.init_BlockState);
            this.init_V();
            return RefQueue.Inner_DataUtil.Inner_DataUtil_2.load;
         } else {
            return RefQueue.Inner_DataUtil.Inner_DataUtil_2.init;
         }
      }

      public static boolean init_Z_Minecraft_D(Minecraft mc, double breakTicks) {
         LocalPlayer p = mc.player;
         return p != null
               && mc.level != null
               && p.isEyeInFluid(FluidTags.WATER)
               && !p.hasEffect(MobEffects.WATER_BREATHING)
               && !p.hasEffect(MobEffects.CONDUIT_POWER)
            ? p.getAirSupply() < breakTicks + init_I_Minecraft(mc) + 40.0
            : false;
      }

      public static int init_I_Minecraft(Minecraft mc) {
         BlockPos cell = BlockPos.containing(mc.player.getEyePosition());

         for (int i = 1; i <= 24; i++) {
            cell = cell.above();
            BlockState state = mc.level.getBlockState(cell);
            if (!state.getFluidState().is(FluidTags.WATER)) {
               return state.getCollisionShape(mc.level, cell).isEmpty() ? i * 6 : 144;
            }
         }

         return 144;
      }

      static boolean init_Z_I(int gapTicks, boolean instant, boolean currentRayHitsBlock) {
         return gapTicks > 0 && instant && currentRayHitsBlock;
      }

      boolean init_Z() {
         if (!this.read_Z
            && this.init_Minecraft.player != null
            && this.init_Minecraft.level != null
            && this.init_BooleanSupplier.getAsBoolean()
            && this.init_Minecraft.gameMode instanceof AutismMultiPlayerGameModeAccessor gap) {
            BlockHitResult var3 = DataUtil.init_BlockHitResult();
            return init_Z_I(gap.autism$getDestroyDelay(), true, var3 != null && !this.init_Minecraft.level.getBlockState(var3.getBlockPos()).isAir());
         } else {
            return false;
         }
      }

      private static boolean init_Z_Minecraft(Minecraft mc) {
         if (close == mc.player.tickCount) {
            return true;
         }

         close = mc.player.tickCount;
         ai.init(mc.player);
         return false;
      }

      private void init_V_BlockPos_Direction(BlockPos pos, Direction fallback) {
         if (!init_Z_Minecraft(this.init_Minecraft)) {
            BlockHitResult ray = DataUtil.init_BlockHitResult();
            Direction face = ray != null && ray.getBlockPos().equals(pos) ? ray.getDirection() : fallback;
            if (this.init_Minecraft.gameMode.continueDestroyBlock(pos, face)) {
               Compat263.addBreakingBlockEffect(this.init_Minecraft.level, pos, face);
               Compat263.swing(this.init_Minecraft.player, InteractionHand.MAIN_HAND);
            }
         }
      }

      public static boolean init_Z_Minecraft(Minecraft mc, BlockPos pos, Direction fallback) {
         if (mc.player != null && mc.level != null && mc.gameMode != null && !init_Z_Minecraft(mc)) {
            BlockHitResult ray = DataUtil.init_BlockHitResult();
            Direction face = ray != null && ray.getBlockPos().equals(pos) ? ray.getDirection() : fallback;
            if (!mc.gameMode.continueDestroyBlock(pos, face)) {
               return false;
            }

            Compat263.addBreakingBlockEffect(mc.level, pos, face);
            Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
            return true;
         } else {
            return false;
         }
      }

      public static void init_V_Minecraft(Minecraft mc) {
         if (mc.player != null && mc.level != null && mc.gameMode instanceof AutismMultiPlayerGameModeAccessor gap && gap.autism$getDestroyDelay() > 0) {
            BlockHitResult ray = DataUtil.init_BlockHitResult();
            if (ray != null
               && !mc.level.getBlockState(ray.getBlockPos()).isAir()
               && !init_Z_Minecraft(mc)
               && mc.gameMode.continueDestroyBlock(ray.getBlockPos(), ray.getDirection())) {
               Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
            }
         }
      }

      static boolean init_Z_BlockPos_List(BlockPos target, List<Vec3> fallingPositions) {
         Vec3 center = Vec3.atCenterOf(target);

         for (Vec3 p : fallingPositions) {
            if (p.distanceToSqr(center) <= 9.0) {
               return true;
            }
         }

         return false;
      }

      private boolean read(BlockPos target) {
         List<Vec3> falling = null;

         for (Entity entity : this.init_Minecraft.level.entitiesForRendering()) {
            if (entity instanceof FallingBlockEntity) {
               if (falling == null) {
                  falling = new ArrayList<>();
               }

               falling.add(entity.position());
            }
         }

         return falling != null && init_Z_BlockPos_List(target, falling);
      }

      private void init_V_BlockPos(BlockPos target, BlockHitResult hit, int slot) {
         this.init_BlockPos = target.immutable();
         this.init_BlockState = this.init_Minecraft.level.getBlockState(target);
         if (slot >= 0) {
            this.init_IntConsumer.accept(slot);
         }

         this.init_Consumer.accept(this.init_Vec3_BlockPos_BlockHitResult(target, hit));
         this.init_V_BlockPos_Direction(this.init_BlockPos, hit.getDirection());
         this.update = 0;
         this.read_F = (float)r.init_D_Minecraft(
            this.init_Minecraft,
            this.init_Minecraft.player.getMainHandItem(),
            this.init_BlockState,
            this.init_BlockState.getDestroySpeed(this.init_Minecraft.level, this.init_BlockPos)
         );
         this.sync = (int)Math.min(72000.0, Math.ceil(this.read_F * 4.0F) + 40.0);
         this.read_Z = true;
      }

      private Vec3 init_Vec3_BlockPos_BlockHitResult(BlockPos target, BlockHitResult hit) {
         Vec3 eye = this.init_Minecraft.player.getEyePosition();
         if (this.read_Z && target.equals(this.init_BlockPos)) {
            BlockHitResult current = DataUtil.init_BlockHitResult();
            if (current != null && current.getBlockPos().equals(target)) {
               this.read_BlockPos = target.immutable();
               this.load_Vec3 = this.init_Minecraft.player.getLookAngle();
               this.read_Vec3 = null;
               return current.getLocation().add(this.load_Vec3.scale(0.02));
            }
         }

         if (!this.read_Z && this.load_Consumer != null && this.write_Vec3 != null && this.init_Z_BlockPos_Vec3(target, this.write_Vec3)) {
            this.read_BlockPos = target.immutable();
            this.load_Vec3 = this.write_Vec3.subtract(eye).normalize();
            this.read_Vec3 = null;
            return this.write_Vec3;
         }

         if (target.equals(this.read_BlockPos) && this.load_Vec3 != null) {
            Vec3 along = this.init_Vec3_BlockPos_Vec3(target, eye);
            if (along != null) {
               return along;
            }
         }

         if (target.equals(this.read_BlockPos) && this.read_Vec3 != null && this.init_Z_BlockPos_Vec3(target, this.read_Vec3)) {
            return this.read_Vec3;
         } else if (this.write_Vec3 != null && this.init_Z_BlockPos_Vec3(target, this.write_Vec3)) {
            this.read_BlockPos = target.immutable();
            this.load_Vec3 = this.write_Vec3.subtract(eye).normalize();
            this.read_Vec3 = null;
            return this.write_Vec3;
         } else {
            Vec3 middle = load(target, hit);
            Vec3 point = CachePool.init(eye, hit.getLocation(), middle, candidate -> this.init_Z_BlockPos_Vec3(target, candidate));
            this.read_BlockPos = target.immutable();
            this.load_Vec3 = null;
            this.read_Vec3 = point;
            return point;
         }
      }

      private Vec3 init_Vec3_BlockPos_Vec3(BlockPos target, Vec3 eye) {
         double reach = this.init_Minecraft.player.blockInteractionRange();
         BlockHitResult seen = this.init_Minecraft
            .level
            .clip(
               new ClipContext(
                  eye, eye.add(this.load_Vec3.scale(reach)), net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, this.init_Minecraft.player
               )
            );
         return seen.getType() == Type.BLOCK && seen.getBlockPos().equals(target) ? seen.getLocation().add(this.load_Vec3.scale(0.02)) : null;
      }

      private void init_V_BlockPos_BlockHitResult(BlockPos target, BlockHitResult hit) {
         if (this.load_Consumer != null
            && this.init_Minecraft.player != null
            && this.init_BooleanSupplier.getAsBoolean()
            && this.init_Minecraft.gameMode instanceof AutismMultiPlayerGameModeAccessor gap) {
            int left = gap.autism$getDestroyDelay();
            if (left > 0) {
               Vec3 eye = this.init_Minecraft.player.getEyePosition();
               Vec3 goal = this.init_Vec3_BlockPos_BlockHitResult(target, hit);
               ce.Inner_DataUtil now = ce.init(this.init_Minecraft.player);
               ce.Inner_DataUtil want = ce.init(goal, eye);
               float dy = Mth.wrapDegrees(want.init() - now.init());
               float dp = want.load() - now.load();
               if (!(Math.abs(dy) + Math.abs(dp) < 0.5F)) {
                  float share = 1.0F / (left + 1);
                  ce.Inner_DataUtil step = new ce.Inner_DataUtil(now.init() + dy * share, now.load() + dp * share);
                  Vec3 dir = Vec3.directionFromRotation(step.load(), step.init());
                  BlockHitResult seen = this.init_Minecraft
                     .level
                     .clip(
                        new ClipContext(
                           eye,
                           eye.add(dir.scale(this.init_Minecraft.player.blockInteractionRange())),
                           net.minecraft.world.level.ClipContext.Block.OUTLINE,
                           Fluid.NONE,
                           this.init_Minecraft.player
                        )
                     );
                  if (seen.getType() == Type.BLOCK && !this.init_Minecraft.level.getBlockState(seen.getBlockPos()).isAir()) {
                     this.load_Consumer.accept(step);
                     this.init_Runnable.run();
                  }
               }
            }
         }
      }

      private ce.Inner_DataUtil init_ce_DataUtil() {
         if (this.load_Consumer != null
            && this.init_Vec3 != null
            && this.read_Z
            && this.init_BlockPos != null
            && this.init_Minecraft.player != null
            && this.init_BooleanSupplier.getAsBoolean()) {
            BlockHitResult current = DataUtil.init_BlockHitResult();
            if (current != null && current.getBlockPos().equals(this.init_BlockPos)) {
               Vec3 eye = this.init_Minecraft.player.getEyePosition();
               Vec3 look = this.init_Minecraft.player.getLookAngle().normalize();
               Vec3 toward = this.init_Vec3.normalize();
               double angle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, look.dot(toward)))));
               if (angle < 0.5) {
                  return null;
               }

               double left = Math.max(1.0F, this.read_F - this.update - 1.0F);
               double rate = Math.min(6.0, Math.max(0.3, angle / left));
               VoxelShape box = this.init_BlockState == null ? null : this.init_BlockState.getShape(this.init_Minecraft.level, this.init_BlockPos);
               if (box != null && !box.isEmpty()) {
                  AABB bounds = box.bounds().move(this.init_BlockPos);

                  for (double step = Math.min(rate, angle); step >= 0.15; step /= 2.0) {
                     Vec3 dir = init_Vec3_Vec3(look, toward, step / angle);
                     BlockHitResult seen = this.init_Minecraft
                        .level
                        .clip(
                           new ClipContext(
                              eye,
                              eye.add(dir.scale(this.init_Minecraft.player.blockInteractionRange())),
                              net.minecraft.world.level.ClipContext.Block.OUTLINE,
                              Fluid.NONE,
                              this.init_Minecraft.player
                           )
                        );
                     if (seen.getType() == Type.BLOCK && seen.getBlockPos().equals(this.init_BlockPos) && EventBus.init(seen, bounds) >= 0.06) {
                        return new ce.Inner_DataUtil(EventBus.init(dir), EventBus.load(dir));
                     }
                  }

                  return null;
               } else {
                  return null;
               }
            } else {
               return null;
            }
         } else {
            return null;
         }
      }

      static Vec3 init_Vec3_Vec3(Vec3 a, Vec3 b, double t) {
         if (t <= 0.0) {
            return a;
         }

         if (t >= 1.0) {
            return b;
         }

         double dot = Math.max(-1.0, Math.min(1.0, a.dot(b)));
         double theta = Math.acos(dot);
         if (theta < 1.0E-6) {
            return b;
         }

         Vec3 tangent = b.subtract(a.scale(dot));
         if (tangent.lengthSqr() < 1.0E-12) {
            Vec3 axis = Math.abs(a.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
            tangent = axis.subtract(a.scale(a.dot(axis)));
         }

         return a.scale(Math.cos(theta * t)).add(tangent.normalize().scale(Math.sin(theta * t))).normalize();
      }

      private boolean init_Z_BlockPos_Vec3(BlockPos target, Vec3 point) {
         Vec3 eye = this.init_Minecraft.player.getEyePosition();
         BlockHitResult seen = this.init_Minecraft
            .level
            .clip(new ClipContext(eye, point, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, this.init_Minecraft.player));
         return CachePool.init(eye, target, seen, this.init_Minecraft.player.blockInteractionRange());
      }

      private static Vec3 load(BlockPos target, BlockHitResult hit) {
         Direction face = hit.getDirection();
         return Vec3.atCenterOf(target).add(face.getStepX() * 0.45, face.getStepY() * 0.45, face.getStepZ() * 0.45);
      }

      public void init_V() {
         if (!AutismLiteVariant.enabled()) {
            cG.init_V(this);
         }

         this.flush = 0;
         this.load_BlockPos = null;
         this.init_UtilCore.init_V();
         if (this.read_Z && this.init_BlockPos != null && this.init_BlockState != null && this.init_Minecraft.level != null) {
            if (this.init_Minecraft.level.getBlockState(this.init_BlockPos).isAir()) {
               FileCache.init(this.init_BlockPos, this.init_BlockState);
            } else {
               this.write_BlockPos = this.init_BlockPos;
               this.load_BlockState = this.init_BlockState;
            }
         }

         if (this.read_Z && this.init_Minecraft.gameMode != null && this.write(this.init_BlockPos)) {
            this.init_Minecraft.gameMode.stopDestroyBlock();
            ai.init();
         }

         this.read_Z = false;
         this.init_BlockPos = null;
         this.init_BlockState = null;
      }

      private boolean write(BlockPos cell) {
         return this.init_Minecraft.gameMode instanceof AutismMultiPlayerGameModeAccessor dig
            ? cell != null && cell.equals(dig.autism$getDestroyBlockPos())
            : true;
      }

      public boolean load() {
         return this.flush > 0;
      }

      public boolean init_Z_BlockPos(BlockPos target) {
         return this.read_Z && target.equals(this.init_BlockPos);
      }

      public enum Inner_DataUtil_2 {
         init,
         load,
         read;
      }
   }

   private record Inner_StreamUtil(double x, double z, float yaw) {
   }

   public enum Inner_TaskQueue {
      init,
      load,
      read,
      write,
      update;
   }
}
