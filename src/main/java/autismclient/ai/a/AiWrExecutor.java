package autismclient.ai.a;

import autismclient.compat263.Compat263;

import autismclient.AutismClientAddon;
import autismclient.ai.cG;
import autismclient.ai.ca;
import autismclient.ai.aV;
import autismclient.ai.af;
import autismclient.api.ai.AiOperation;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.util.AutismLiteVariant;
import autismclient.util.RotationMath;
import autismclient.util.BlockPlacementPlanner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeafLitterBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AiWrExecutor implements ClassCache.Inner_TaskQueue {
   private static final int flowState = 16;
   private static final int flowTemp = 6;
   private static final int readEx_I = 6;
   private static final int value_I = 24;
   private static final int prevEx_I = 60;
   private static final int prev_I = 3;
   private static final int chunkAt_I = 2;
   private static final int state_I = 60;
   private static final int closeOf_I = 6;
   private static final long putAt_J = 6000000L;
   private static final long fill_J = 8000000L;
   private static final ItemStack[] putAt_ItemStackArr = new ItemStack[9];
   private static final boolean putAt_Z = Boolean.getBoolean("autism.navigation.debug");
   private final Minecraft putAt_Minecraft = Minecraft.getInstance();
   private final DataUtil putAt_CacheHelper = new DataUtil();
   private final AiBlockDigger putAt_JsonCache = new AiBlockDigger(
      this.putAt_Minecraft, this::fill, this::putAt_V_Vec3, false, false, this.putAt_CacheHelper::load_V, () -> !this.putAt_CacheHelper.init_Z()
   );
   private List<ClassCache.UiHelper> putAt_List = List.of();
   private int pollAt_I;
   private boolean fill_Z = true;
   private AiWrExecutor.Inner_TableCache putAt_RingCache_TableCac;
   private AiWrExecutor.Inner_TaskState putAt_RingCache_TaskStat;
   private AiWrExecutor.Inner_JsonCache putAt_RingCache_JsonCach;
   private ClassCache.UiHelper putAt_FileStore_PacketHe;
   private ClassCache.UiHelper fill_FileStore_PacketHe;
   private int pollOf_I;
   private int tick_I;
   private int flushOf_I;
   private int stateOf_I;
   private boolean readEx_Z;
   private boolean value_Z;
   private boolean prevEx_Z;
   private boolean prev_Z;
   private BlockPos putAt_BlockPos;
   private long readEx_J;
   private Block putAt_Block;
   private int store_I;
   private int nextBy_I;
   private BlockPos fill_BlockPos;
   private int dataEx_I;
   private int frameOf_I;
   private int syncAt_I;
   private NetHelper putAt_TokenCodec;
   private int stopBy;
   private int dataTo;
   private int initAt;
   private int setOf;
   private final Set<BlockPos> putAt_Set;
   private EventBus.Inner_DataUtil putAt_EventBus_CacheHelp;
   private EventBus.Inner_DataUtil fill_EventBus_CacheHelp;
   private ClassCache.UiHelper readEx_FileStore_PacketHe;
   private ClassCache.UiHelper value_FileStore_PacketHe;
   private ClassCache.UiHelper prevEx_FileStore_PacketHe;
   private Set<BlockPos> fill_Set;
   private Set<BlockPos> readEx_Set;
   private boolean chunkAt_Z;
   private BlockPos readEx_BlockPos;
   private BlockPos value_BlockPos;
   private BlockPos prevEx_BlockPos;
   private boolean state_Z;
   private int count;
   private int getTo;
   private int tableEx;
   private int levelOf;
   private int addTo;
   private int task;
   private int initEx;
   private boolean closeOf_Z;
   private UtilCore putAt_SyncCore;
   private ItemStack[] fill_ItemStackArr;
   private String putAt_Str;
   private int poolEx;
   private boolean pollAt_Z;
   private boolean pollOf_Z;
   private boolean tick_Z;
   private AiOperation putAt_AiOperation;
   private StateCache putAt_PacketHelper;
   private ClassCache.Inner_NetHelper putAt_FileStore_RingUtil;
   private boolean flushOf_Z;
   private boolean stateOf_Z;
   private boolean store_Z;
   private boolean nextBy_Z;
   private double putAt_D;
   private boolean dataEx_Z;
   private static final Predicate<BlockPos> putAt_Predicate = var0 -> true;
   private Predicate<BlockPos> fill_Predicate;
   private ClassCache.UiHelper prev_FileStore_PacketHe;
   private boolean frameOf_Z;
   private Predicate<BlockPos> readEx_Predicate;
   private Function<BlockPos, Vec3> putAt_Function;
   private Function<Predicate<AABB>, Vec3> fill_Function;
   private Runnable putAt_Runnable;
   private BitSetUtil putAt_NetEntry;
   private RingBuffer putAt_PoolStore;
   private StateCache fill_PacketHelper;
   private final AiRoutePolicy.Inner_CacheHelper putAt_ValueSink_CacheHel;
   private boolean syncAt_Z;
   private BlockPos prev_BlockPos;
   private ClassCache.UiHelper chunkAt_FileStore_PacketHe;
   private String fill_Str;
   private int writeAt;
   private int reset;
   private int readBy;
   private double fill_D;

   public AiWrExecutor() {
      this.putAt_RingCache_TableCac = AiWrExecutor.Inner_TableCache.putAt_RingCache_TableCac;
      this.syncAt_I = -1073741824;
      this.putAt_Set = new HashSet<>();
      this.fill_Set = Set.of();
      this.readEx_Set = Set.of();
      this.getTo = -2147483648;
      this.task = -1;
      this.initEx = -1;
      this.putAt_FileStore_RingUtil = ClassCache.Inner_NetHelper.init;
      this.flushOf_Z = true;
      this.stateOf_Z = true;
      this.nextBy_Z = true;
      this.putAt_D = 0.0 / 0.0;
      this.fill_Predicate = putAt_Predicate;
      this.putAt_ValueSink_CacheHel = new AiRoutePolicy.Inner_CacheHelper();
      this.fill_Str = "";
   }

   public static boolean fill_Z() {
      return AiMotionSafety.fill_Z();
   }

   public static void putAt_V() {
      AiMotionSafety.putAt_V();
   }

   public static void fill_V() {
      AiMotionSafety.fill_V();
   }

   public static int putAt_I_List(List<ClassCache.UiHelper> var0) {
      return AiRoutePolicy.putAt(var0);
   }

   static boolean putAt_Z_FileStore_PacketHe(
      ClassCache.UiHelper var0, Function<BlockPos, ClassCache.Inner_FileCache> var1, Predicate<BlockPos> var2, Predicate<BlockPos> var3
   ) {
      if (var0 != null && var0.cellsToBreak().isEmpty() && var0.cellsToPlace().isEmpty()) {
         ClassCache.Inner_FileCache var4 = (ClassCache.Inner_FileCache)var1.apply(var0.from());
         ClassCache.Inner_FileCache var5 = (ClassCache.Inner_FileCache)var1.apply(var0.to());
         if (!var4.water() && !var5.water()) {
            boolean var10000;
            switch (var0.type()) {
               case init:
               case load:
               case read:
               case write:
               case reset:
               case update:
               case sync:
               case start:
               case put:
               case add:
               case clear:
               case SLIME_BOUNCE:
               case LADDER_JUMP:
               case get:
               case flush:
               case close:
               case open:
               case poll:
                  var10000 = true;
                  break;
               case set:
                  var10000 = !var2.test(var0.to()) && !var3.test(var0.to());
                  break;
               default:
                  var10000 = false;
            }

            return var10000;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   static int putAt_I_List(
      List<ClassCache.UiHelper> var0, int var1, Function<BlockPos, ClassCache.Inner_FileCache> var2, Predicate<BlockPos> var3, Predicate<BlockPos> var4
   ) {
      int var5 = var1;

      while (var5 < var0.size() && putAt_Z_FileStore_PacketHe((ClassCache.UiHelper)var0.get(var5), var2, var3, var4)) {
         var5++;
      }

      return var5;
   }

   static boolean putAt_Z_List_Function(List<ClassCache.UiHelper> var0, Function<BlockPos, ClassCache.Inner_FileCache> var1) {
      for (ClassCache.UiHelper var3 : var0) {
         if (((ClassCache.Inner_FileCache)var1.apply(var3.from().below())).farmland() || ((ClassCache.Inner_FileCache)var1.apply(var3.to().below())).farmland()) {
            return true;
         }
      }

      return false;
   }

   static boolean putAt_Z_Z(boolean var0, boolean var1, boolean var2, boolean var3, double var4, double var6) {
      return var0 && var1 && var2 && !var3 && var4 * var4 + var6 * var6 > 4.0E-4;
   }

   public void putAt_V_List(List<ClassCache.UiHelper> var1) {
      this.putAt_V_List_Z(var1, true);
   }

   public void putAt_V_List_Z(List<ClassCache.UiHelper> var1, boolean var2) {
      this.fill_FileStore_PacketHe = this.putAt_RingCache_TableCac == AiWrExecutor.Inner_TableCache.fill
         ? (
            this.putAt_FileStore_PacketHe != null
               ? this.putAt_FileStore_PacketHe
               : (this.pollAt_I < this.putAt_List.size() ? this.putAt_List.get(this.pollAt_I) : null)
         )
         : null;
      this.state(false);
      this.putAt_List = List.copyOf(var1);
      this.pollAt_I = 0;
      this.putAt_FileStore_PacketHe = null;
      this.fill_Z = var2;
      this.prev_FileStore_PacketHe = null;
      this.putAt_RingCache_TableCac = AiWrExecutor.Inner_TableCache.fill;
   }

   public boolean putAt_Z_List_Z(List<ClassCache.UiHelper> var1, boolean var2) {
      if (this.putAt_RingCache_TableCac == AiWrExecutor.Inner_TableCache.fill && !this.putAt_List.isEmpty() && !var1.isEmpty()) {
         if (!((ClassCache.UiHelper)var1.get(0)).from().equals(this.putAt_List.get(this.putAt_List.size() - 1).to())) {
            return false;
         }

         int var3 = this.pollAt_I;
         ArrayList var4 = new ArrayList(this.putAt_List.size() - var3 + var1.size());
         var4.addAll(this.putAt_List.subList(var3, this.putAt_List.size()));
         var4.addAll(var1);
         this.putAt_List = List.copyOf(var4);
         if (this.putAt_RingCache_TaskStat != null) {
            this.putAt_RingCache_TaskStat.flowState = Math.max(0, this.putAt_RingCache_TaskStat.flowState - var3);
            this.putAt_RingCache_TaskStat.flowTemp = Math.max(0, this.putAt_RingCache_TaskStat.flowTemp - var3);
         }

         this.stopBy -= var3;
         this.dataTo -= var3;
         this.initAt -= var3;
         this.pollAt_I = 0;
         this.fill_Z = var2;
         return true;
      } else {
         return false;
      }
   }

   boolean putAt_Z_List(List<ClassCache.UiHelper> var1) {
      if (this.putAt_RingCache_TableCac != AiWrExecutor.Inner_TableCache.fill) {
         return false;
      }

      int var2 = this.pollAt_I;
      var var3 = ClassCache.fill(this.putAt_List.subList(var2, this.putAt_List.size()), var1);
      if (var3 == null) {
         return false;
      }

      this.putAt_List = var3;
      if (this.putAt_RingCache_TaskStat != null) {
         this.putAt_RingCache_TaskStat.flowState = Math.max(0, this.putAt_RingCache_TaskStat.flowState - var2);
         this.putAt_RingCache_TaskStat.flowTemp = Math.min(this.putAt_List.size(), Math.max(0, this.putAt_RingCache_TaskStat.flowTemp - var2));
         boolean var4 = false;
         if (this.putAt_RingCache_TaskStat.putAt_TextPool_JsonCache != null) {
            BlockPos[] var5 = this.putAt_RingCache_TaskStat.putAt_TextPool_JsonCache.cells();

            for (int var6 = 0; var6 < var5.length && this.putAt_RingCache_TaskStat.flowState + var6 < this.putAt_List.size(); var6++) {
               if (!var5[var6].equals(this.putAt_List.get(this.putAt_RingCache_TaskStat.flowState + var6).to())) {
                  var4 = true;
                  break;
               }
            }
         }

         if (var4) {
            this.putAt_RingCache_TaskStat.prevEx_Z = true;
         }
      }

      this.stopBy -= var2;
      this.dataTo -= var2;
      this.initAt -= var2;
      this.pollAt_I = 0;
      this.fill_Z = true;
      return true;
   }

   public AiWrExecutor.Inner_TableCache putAt_RingCache_TableCac() {
      return this.putAt_RingCache_TableCac;
   }

   public BlockPos fill_BlockPos() {
      return this.prev_BlockPos;
   }

   public ClassCache.UiHelper putAt_FileStore_PacketHe() {
      return this.chunkAt_FileStore_PacketHe;
   }

   public boolean readEx_Z() {
      return this.syncAt_Z;
   }

   public String putAt_Str() {
      return this.fill_Str;
   }

   public int readEx_I() {
      return this.writeAt;
   }

   public int value_I() {
      return this.reset;
   }

   public int prevEx_I() {
      return this.readBy;
   }

   public int prev_I() {
      return this.poolEx;
   }

   public double putAt_D() {
      return this.fill_D;
   }

   public void putAt_V_Z(boolean var1) {
      this.closeOf_Z = var1;
      if (var1 && this.putAt_SyncCore == null) {
         this.putAt_SyncCore = new UtilCore(this.putAt_Minecraft);
      }
   }

   public void putAt_V_PacketHelper(StateCache var1) {
      this.putAt_PacketHelper = var1;
   }

   void putAt_V_FileStore_RingUtil(ClassCache.Inner_NetHelper var1) {
      this.putAt_FileStore_RingUtil = var1;
   }

   void fill(boolean var1) {
      this.store_Z = var1;
   }

   void readEx(boolean var1) {
      this.nextBy_Z = var1;
   }

   void putAt_V_D(double var1) {
      this.putAt_D = var1;
   }

   void putAt_V_FileStore_PacketHe(ClassCache.UiHelper var1) {
      this.prev_FileStore_PacketHe = var1;
   }

   public void value(boolean var1) {
      this.flushOf_Z = var1;
   }

   public void prevEx_V_Z(boolean var1) {
      this.stateOf_Z = var1;
   }

   public void prev(boolean var1) {
      this.dataEx_Z = var1;
   }

   public void putAt_V_Predicate(Predicate<BlockPos> var1) {
      this.fill_Predicate = var1 != null ? var1 : putAt_Predicate;
   }

   public void putAt_V_BiPredicate(BiPredicate<BlockState, ItemStack> var1) {
      this.putAt_JsonCache.putAt_JsonCache_BiPredicate(var1);
   }

   public void putAt_V_Function(Function<BlockPos, Vec3> var1) {
      this.putAt_Function = var1;
   }

   public void putAt_V_I(int var1) {
      this.addTo = Math.max(0, var1);
   }

   public void putAt_V_Function_Runnable(Function<Predicate<AABB>, Vec3> var1, Runnable var2) {
      if (!AutismLiteVariant.enabled()) {
         this.fill_Function = var1;
         this.putAt_Runnable = var2;
      }
   }

   public void readEx_V() {
      this.putAt_ValueSink_CacheHel.putAt();
   }

   public void value_V() {
      this.putAt_ValueSink_CacheHel.putAt();
   }

   public void prevEx_V() {
      this.syncAt_Z = false;
   }

   public void chunkAt(boolean var1) {
      this.putAt_V_Z_Predicate(var1, null);
   }

   public void putAt_V_Z_Predicate(boolean var1, Predicate<BlockPos> var2) {
      this.frameOf_Z = var1;
      this.readEx_Predicate = var1 ? var2 : null;
   }

   public void prev_V() {
      this.nextBy();
      if (this.putAt_Minecraft.player != null) {
         this.putAt_V_LocalPlayer_Z(this.putAt_Minecraft.player, false);
      }

      this.chunkAt_V();
      AiBlockDigger.putAt_V_Minecraft(this.putAt_Minecraft);
   }

   void chunkAt_V() {
      if (this.putAt_Minecraft.player != null
         && this.putAt_Minecraft.player.onClimbable()
         && !this.putAt_Minecraft.player.isInWater()
         && AiLadderJump.putAt(this.putAt_NetEntry(), this.putAt_Minecraft.player.blockPosition())) {
         LocalPlayer var1 = this.putAt_Minecraft.player;
         NodeWalker.Inner_DataUtil var2 = AiLadderJump.putAt_TokenCore_CacheHel_TokenCore_TaskStat(
            this.putAt_NetEntry(), putAt_TokenCore_TableCac_LocalPlayer(var1), var1.blockPosition(), false, var1.getYRot()
         );
         this.putAt_V_LocalPlayer_TokenCore_CacheHel(var1, new NodeWalker.Inner_DataUtil(var2.forward(), var2.strafe(), false, false, true, var2.yaw()));
      }
   }

   public boolean value_Z() {
      if (this.putAt_Minecraft.player != null && this.putAt_Minecraft.level != null) {
         LocalPlayer var1 = this.putAt_Minecraft.player;
         if (!var1.isInWater() && (!var1.onGround() || !(var1.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4))) {
            if (AiMotionSafety.putAt_Z_TokenCore_TaskStat_2(this.putAt_NetEntry(), putAt_TokenCore_TableCac_LocalPlayer(var1), NodeWalker.Inner_DataUtil.NONE, 64)
               )
             {
               return true;
            }

            AiMotionSafety.fill_V();
            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public int chunkAt_I() {
      return this.putAt_RingCache_TableCac == AiWrExecutor.Inner_TableCache.fill && this.fill_Z && this.putAt_List != null
         ? Math.max(0, this.putAt_List.size() - this.pollAt_I)
         : -1;
   }

   public int state_I() {
      return this.putAt_RingCache_TableCac == AiWrExecutor.Inner_TableCache.fill && this.putAt_List != null && !this.putAt_List.isEmpty()
         ? Math.max(0, this.putAt_List.size() - this.pollAt_I)
         : -1;
   }

   public BlockPos readEx_BlockPos() {
      return this.putAt_RingCache_TableCac == AiWrExecutor.Inner_TableCache.fill && this.putAt_List != null && !this.putAt_List.isEmpty()
         ? this.putAt_List.get(this.putAt_List.size() - 1).to()
         : null;
   }

   public BlockPos value_BlockPos() {
      return !this.putAt_List.isEmpty() && this.putAt_RingCache_TableCac == AiWrExecutor.Inner_TableCache.fill
         ? this.putAt_List.get(this.putAt_List.size() - 1).to()
         : null;
   }

   public Set<BlockPos> putAt_Set() {
      if (this.putAt_RingCache_TableCac == AiWrExecutor.Inner_TableCache.fill && this.putAt_List != null && !this.putAt_List.isEmpty()) {
         HashSet<BlockPos> var1 = new HashSet<>();

         for (int var2 = Math.max(0, this.pollAt_I); var2 < this.putAt_List.size(); var2++) {
            ClassCache.UiHelper var3 = this.putAt_List.get(var2);
            var1.add(var3.from().below());
            var1.add(var3.to().below());
            int var4 = var3.to().getX() - var3.from().getX();
            int var5 = var3.to().getZ() - var3.from().getZ();
            if (var4 != 0 && var5 != 0) {
               var1.add(var3.from().offset(var4, -1, 0));
               var1.add(var3.from().offset(0, -1, var5));
            }
         }

         return var1;
      } else {
         return Set.of();
      }
   }

   public Set<BlockPos> putAt_Set_I(int var1) {
      if (this.putAt_RingCache_TableCac != AiWrExecutor.Inner_TableCache.fill) {
         return Set.of();
      }

      HashSet<BlockPos> var2 = new HashSet<>();

      for (int var3 = this.pollAt_I; var3 < Math.min(this.putAt_List.size(), this.pollAt_I + var1); var3++) {
         for (BlockPos var5 : this.putAt_List.get(var3).cellsToBreak()) {
            if (!putAt_Z_FileStore_BitCache(this.putAt_FileStore_BitCache_BlockPos(var5))) {
               var2.add(var5);
            }
         }
      }

      return var2;
   }

   int closeOf_I() {
      if (this.chunkAt_I() < 0) {
         return -1;
      }

      double var1 = 0.0;

      for (int var3 = this.pollAt_I; var3 < this.putAt_List.size(); var3++) {
         var1 += this.putAt_List.get(var3).costTicks();
      }

      double var5 = this.fill_D + var1;
      return var5 > 0.0 ? Math.clamp((int)(100.0 * this.fill_D / var5), 0, 99) : 0;
   }

   public boolean prevEx_Z() {
      if (this.putAt_Minecraft.player != null && !fill_Z()) {
         return false;
      } else if (this.putAt_RingCache_TableCac != AiWrExecutor.Inner_TableCache.fill) {
         return true;
      } else if (this.putAt_RingCache_TaskStat == null) {
         return this.putAt_FileStore_PacketHe == null ? true : this.tick_Z();
      } else {
         return this.putAt_Minecraft.player == null
            || this.putAt_Minecraft.player.onGround()
            || this.putAt_Minecraft.player.onClimbable()
            || this.putAt_Minecraft.player.isInWater();
      }
   }

   public boolean prev_Z() {
      return this.putAt_BlockPos != null && (this.putAt_Minecraft.level == null || !this.readEx(this.putAt_BlockPos))
         || this.putAt_SyncCore != null && this.putAt_SyncCore.init_Z()
         || this.putAt_AiOperation != null
         || this.nextBy_I > 0;
   }

   public boolean chunkAt_Z() {
      return this.fill_BlockPos != null;
   }

   public boolean state_Z() {
      return this.putAt_FileStore_PacketHe != null && !this.putAt_FileStore_PacketHe.cellsToPlace().isEmpty() && !this.value_Z;
   }

   boolean closeOf_Z() {
      return false;
   }

   public void state_V() {
      if (!AutismLiteVariant.enabled()) {
         cG.init_V(this);
      }

      if (this.putAt_RingCache_TableCac != AiWrExecutor.Inner_TableCache.prevEx) {
         this.putAt_RingCache_TableCac = AiWrExecutor.Inner_TableCache.prevEx;
         this.state(true);
      }
   }

   public boolean pollAt_Z() {
      if (!this.prevEx_Z()) {
         return false;
      }

      if (this.putAt_RingCache_TableCac == AiWrExecutor.Inner_TableCache.prevEx) {
         return true;
      }

      if (!AutismLiteVariant.enabled()) {
         cG.init_V(this);
      }

      this.state(false);
      this.putAt_CacheHelper.init_V();
      this.putAt_List = List.of();
      this.pollAt_I = 0;
      this.putAt_FileStore_PacketHe = null;
      this.prev_BlockPos = null;
      this.chunkAt_FileStore_PacketHe = null;
      this.putAt_RingCache_TableCac = AiWrExecutor.Inner_TableCache.putAt_RingCache_TableCac;
      return true;
   }

   public void closeOf_V() {
      if (this.putAt_RingCache_TableCac == AiWrExecutor.Inner_TableCache.value) {
         this.putAt_RingCache_TableCac = AiWrExecutor.Inner_TableCache.putAt_RingCache_TableCac;
         this.prev_BlockPos = null;
         this.chunkAt_FileStore_PacketHe = null;
         this.putAt_List = List.of();
         this.putAt_FileStore_PacketHe = null;
         this.tick_V();
      }
   }

   @Override
   public boolean putAt_Z() {
      return this.putAt_RingCache_TableCac == AiWrExecutor.Inner_TableCache.fill;
   }

   @Override
   public BlockPos putAt_BlockPos() {
      return i.init(this.putAt_Minecraft);
   }

   @Override
   public int flowTemp() {
      int var1 = 0;

      for (int var2 = this.pollAt_I; var2 < this.putAt_List.size(); var2++) {
         if (var2 != this.pollAt_I || !this.value_Z) {
            var1 += this.putAt_List.get(var2).cellsToPlace().size();
         }
      }

      return var1;
   }

   @Override
   public List<ClassCache.UiHelper> putAt_List() {
      return this.putAt_RingCache_TableCac == AiWrExecutor.Inner_TableCache.fill
         ? List.copyOf(this.putAt_List.subList(this.pollAt_I, this.putAt_List.size()))
         : List.of();
   }

   @Override
   public int flowState() {
      double var1 = 0.0;

      for (int var3 = this.pollAt_I + (this.putAt_FileStore_PacketHe == null && this.putAt_RingCache_TaskStat == null ? 0 : 1);
         var3 < this.putAt_List.size();
         var3++
      ) {
         var1 += this.putAt_List.get(var3).costTicks();
      }

      return (int)Math.ceil(var1);
   }

   public AiWrExecutor.Inner_TableCache fill_RingCache_TableCac() {
      if (this.putAt_RingCache_TableCac != AiWrExecutor.Inner_TableCache.fill) {
         return this.putAt_RingCache_TableCac;
      }

      if (this.putAt_Minecraft.player != null && this.putAt_Minecraft.level != null) {
         this.fill_ItemStackArr = null;
         if (this.putAt_NetEntry != null) {
            this.putAt_NetEntry.putAt_V();
         }

         this.pollAt_V();
         this.pollOf_V();
         if (this.putAt_RingCache_TableCac == AiWrExecutor.Inner_TableCache.fill && this.putAt_RingCache_TaskStat == null && this.putAt_TokenCodec == null) {
            this.putAt_CacheHelper.load_V();
         }

         return this.putAt_RingCache_TableCac;
      } else {
         this.state_V();
         return this.putAt_RingCache_TableCac;
      }
   }

   private void pollAt_V() {
      if (!AutismLiteVariant.enabled()) {
         if (this.putAt_RingCache_TaskStat != null && this.putAt_RingCache_TaskStat.putAt_TextPool_JsonCache != null) {
            var var1 = List.of(this.putAt_RingCache_TaskStat.putAt_TextPool_JsonCache.checkpoints());
            if (this.putAt_RingCache_TaskStat.putAt_TextPool_IoHelper.flowTemp() > 0 && this.putAt_RingCache_TaskStat.putAt_TableUtil != null) {
               cG.init(this, this.putAt_RingCache_TaskStat.putAt_TextPool_IoHelper, this.putAt_RingCache_TaskStat.putAt_TableUtil.flowState(), var1, 0);
            } else {
               cG.init(this, this.putAt_RingCache_TaskStat.putAt_TextPool_JsonCache, var1, 0);
            }
         } else {
            cG.putAt_V_Obj_3(this, this.putAt_List, this.pollAt_I, this::putAt_BlockState);
         }
      }
   }

   private void pollOf_V() {
      LocalPlayer var1 = this.putAt_Minecraft.player;
      int var2 = 0;

      while (this.putAt_RingCache_TableCac == AiWrExecutor.Inner_TableCache.fill) {
         if (this.pollAt_I >= this.putAt_List.size()) {
            if (this.fill_BlockPos != null) {
               this.flushOf_V();
               return;
            }

            if (!AutismLiteVariant.enabled()) {
               cG.init_V(this);
            }

            this.putAt_RingCache_TableCac = AiWrExecutor.Inner_TableCache.readEx;
            this.state(false);
            return;
         }

         if (this.putAt_RingCache_JsonCach == null) {
            if (this.putAt_RingCache_TaskStat == null) {
               if (this.putAt_FileStore_PacketHe == null
                  && !var1.isInWater()
                  && putAt_Z_FileStore_PacketHe(
                     this.putAt_List.get(this.pollAt_I), this::putAt_FileStore_BitCache_BlockPos, this::putAt_Z_BlockPos, AiRoutePolicy::fill
                  )) {
                  int var3 = putAt_I_List(this.putAt_List, this.pollAt_I, this::putAt_FileStore_BitCache_BlockPos, this::putAt_Z_BlockPos, AiRoutePolicy::fill);
                  if (var3 > this.pollAt_I) {
                     if (this.frameOf_Z && var1.onGround() && !this.value(this.putAt_List.get(this.pollAt_I))) {
                        this.nextBy();
                        return;
                     }

                     int var9 = AiRoutePolicy.putAt_I_List(this.putAt_List, this.pollAt_I, 5, this.putAt_Function(), this.dataEx_Z);
                     if (var9 < 0 && !this.readEx(this.putAt_List.get(this.pollAt_I))) {
                        var9 = this.pollAt_I;
                     }

                     if (var9 < 0) {
                        var9 = AiRoutePolicy.putAt_I_List_2(this.putAt_List, this.pollAt_I, 5, this.prev(var1), false);
                     }

                     if (var9 >= 0) {
                        this.fill_Str = "a move ahead turned impossible";
                        this.putAt_V_BlockPos_FileStore_PacketHe(this.putAt_List.get(var9).to(), this.putAt_List.get(var9));
                        return;
                     }

                     ClassCache.UiHelper var10 = this.putAt_List.get(this.pollAt_I);
                     if (!putAt_Z_FileStore_PacketHe_FileStore_PacketHe(var10, this.fill_FileStore_PacketHe)
                        && this.putAt_ValueSink_CacheHel.putAt(var10, System.nanoTime())) {
                        this.syncAt_Z = true;
                        this.fill_Str = "looping on " + var10.type() + " " + var10.from().toShortString() + " -> " + var10.to().toShortString();
                        this.putAt_V_BlockPos_FileStore_PacketHe(var10.to(), var10);
                        return;
                     }

                     this.fill_FileStore_PacketHe = null;
                     BlockPos var11 = this.putAt_List.get(var3 - 1).to();
                     boolean var7 = this.fill_Z && var3 == this.putAt_List.size();
                     if (var3 - this.pollAt_I == 1
                        && AiPaceScript.putAt_Z_TokenCore_TaskStat_4(this.putAt_NetEntry(), putAt_TokenCore_TableCac_LocalPlayer(var1), var11, false, var7)) {
                        this.fill_D = this.fill_D + this.putAt_List.get(this.pollAt_I).costTicks();
                        this.pollAt_I = var3;
                        if (++var2 >= 16) {
                           return;
                        }
                     } else {
                        this.putAt_V_LocalPlayer(var1, this.pollAt_I, var3, false, 0);
                        if (this.fill(var1)) {
                           return;
                        }

                        if (++var2 >= 16) {
                           return;
                        }
                     }
                     continue;
                  }
               }

               if (this.putAt_FileStore_PacketHe == null) {
                  this.putAt_FileStore_PacketHe = this.putAt_List.get(this.pollAt_I);
                  this.fill_V(this.putAt_FileStore_PacketHe);
                  if (this.putAt_RingCache_TableCac != AiWrExecutor.Inner_TableCache.fill) {
                     return;
                  }
               }

               if (this.putAt_FileStore_PacketHe.type() != ClassCache.Inner_SyncManager.run || this.putAt_TokenCodec == null && !this.pollOf_Z()) {
                  if (!this.fill_Z(this.putAt_FileStore_PacketHe)) {
                     int var8 = AiRoutePolicy.putAt_I_List(this.putAt_List, this.pollAt_I, 5, this.putAt_Function(), this.dataEx_Z);
                     if (var8 < 0 && this.tick_Z() && !this.readEx(this.putAt_FileStore_PacketHe)) {
                        var8 = this.pollAt_I;
                     }

                     if (var8 >= 0) {
                        this.fill_Str = "a move ahead turned impossible";
                        this.putAt_V_BlockPos_FileStore_PacketHe(this.putAt_List.get(var8).to(), this.putAt_List.get(var8));
                        return;
                     }

                     if (this.putAt_AiOperation != null) {
                        this.nextBy();
                        this.stateOf_V();
                        return;
                     }

                     if (this.putAt_SyncCore != null && this.putAt_SyncCore.init_Z()) {
                        this.nextBy();
                        this.store_V();
                        return;
                     }

                     if (this.frameOf_Z && var1.onGround() && !this.value(this.putAt_FileStore_PacketHe)) {
                        this.putAt_CacheHelper.init_V_DataUtil_DataUtil(DataUtil.Inner_DataUtil.write);
                        if (!this.putAt_FileStore_PacketHe.cellsToBreak().isEmpty() && this.putAt_Z_LocalPlayer(var1, this.putAt_FileStore_PacketHe, false)) {
                           return;
                        }

                        this.nextBy();
                        return;
                     }

                     boolean var4 = this.putAt_FileStore_PacketHe.type() == ClassCache.Inner_SyncManager.poll
                        && !var1.onGround()
                        && !var1.isInWater()
                        && !var1.onClimbable();
                     boolean var5 = this.putAt_FileStore_PacketHe.type() == ClassCache.Inner_SyncManager.run
                        && !this.value_Z
                        && (this.prevEx_Z || this.putAt_BlockPos != null)
                        && this.pollOf_I < ClassCache.Inner_CachePool.putAt_I_D(this.putAt_FileStore_PacketHe.costTicks()) + 60;
                     boolean var6 = !var1.isInWater() || ++this.stateOf_I % 3 == 0;
                     if (var5) {
                        this.pollOf_I++;
                     } else if (!var4 && var6 && ++this.pollOf_I > ClassCache.Inner_CachePool.putAt_I_D(this.putAt_FileStore_PacketHe.costTicks())) {
                        this.putAt_V_Str("over its time budget" + this.putAt_Str_FileStore_PacketHe(this.putAt_FileStore_PacketHe));
                        return;
                     }

                     this.putAt_V_LocalPlayer_FileStore_PacketHe(var1, this.putAt_FileStore_PacketHe);
                     PacketUtil.init(
                        this.putAt_FileStore_PacketHe.type(),
                        this.putAt_TokenCodec == null
                           || this.putAt_TokenCodec.init_JsonUtil().putAt_TextStore_BitCore() == JsonUtil.Inner_ConfigStore.init,
                        false
                     );
                     this.flushOf_V();
                     return;
                  }

                  if (this.putAt_FileStore_PacketHe.type() == ClassCache.Inner_SyncManager.set
                     && !this.putAt_Z_FileStore_PacketHe(this.putAt_FileStore_PacketHe)) {
                     if (++this.pollOf_I > ClassCache.Inner_CachePool.putAt_I_D(this.putAt_FileStore_PacketHe.costTicks())) {
                        this.putAt_V_Str("over its time budget" + this.putAt_Str_FileStore_PacketHe(this.putAt_FileStore_PacketHe));
                     }

                     return;
                  }

                  this.fill_D = this.fill_D + this.putAt_FileStore_PacketHe.costTicks();
                  this.pollAt_I++;
                  this.putAt_FileStore_PacketHe = null;
                  this.dataEx();
                  if (++var2 >= 16) {
                     return;
                  }
               } else {
                  if (this.prevEx_Z_LocalPlayer(var1)) {
                     return;
                  }

                  this.dataEx();
                  if (++var2 >= 16) {
                     return;
                  }
               }
            } else {
               if (this.fill(var1)) {
                  return;
               }

               if (++var2 >= 16) {
                  return;
               }
            }
         } else {
            if (this.putAt_Z_LocalPlayer(var1)) {
               return;
            }

            if (++var2 >= 16) {
               return;
            }
         }
      }
   }

   private void putAt_V_LocalPlayer(LocalPlayer var1, int var2, int var3, boolean var4, int var5) {
      this.putAt(var1, var2, var3, var4, var5, 0, 0);
   }

   private void putAt(LocalPlayer var1, int var2, int var3, boolean var4, int var5, int var6, int var7) {
      this.tick_V();
      var var8 = List.copyOf(this.putAt_List.subList(var2, var3));
      boolean var9 = this.flushOf_Z && !putAt_Z_List_Function(var8, this::putAt_FileStore_BitCache_BlockPos);
      boolean var10 = this.stateOf_Z && this.prev(var1) && !var1.hasEffect(MobEffects.BLINDNESS);
      double var11 = AiPacePlayback.putAt_D_LocalPlayer(var1);
      boolean var13 = Double.isFinite(this.putAt_D) && this.putAt_D > 0.0 && Math.abs(var11 - this.putAt_D) > 1.0E-4;
      double var14 = var13 ? this.putAt_D : var11;
      double var16 = var13 ? var11 : 0.0 / 0.0;
      NodeWalker.Inner_StreamUtil var18 = putAt_TokenCore_TableCac_LocalPlayer(var1);
      float var19 = var1.getYRot();
      RingBuffer var20 = this.putAt_PoolStore();
      boolean var21 = this.fill_Z && var3 == this.putAt_List.size();
      AiPaceScript.Inner_JsonCache var22 = AiPaceScript.putAt_TextPool_JsonCache_FileStore_TextStor_2(var20, var18, var8, var14, var9, var10, var21);
      if (var22 == null) {
         this.putAt_V_Str("wr guide construction returned null");
      } else {
         AiPaceScript.Inner_TextState var23 = var4 ? AiPaceScript.Inner_TextState.DEFAULT : AiPaceScript.Inner_TextState.TRAVEL;
         Predicate var24 = this.putAt_Predicate_TokenCore_TableCac_Predicate(var18, this.fill_Predicate == putAt_Predicate ? null : this.fill_Predicate);
         AiPaceScript.Inner_TextState var25 = var23.withControls(var9, var10).withAllowedCells(var24);
         AiWrExecutor.Inner_TaskState var26 = new AiWrExecutor.Inner_TaskState(
            var2, var3, var14, var16, var18, var22, var24 != null ? var24 : this.fill_Predicate, var4, var5, var6, var7
         );
         this.putAt_RingCache_TaskStat = var26;
         AiPaceScript.Inner_IoPool var27 = AiPaceScript.Inner_IoPool.of(var18, var19);
         if (!var4
            && !var13
            && var22.length() <= 35.0
            && AiPaceScript.putAt_Z_TokenCore_TaskStat_2(this.putAt_NetEntry(), var27, var22, var25, var26.putAt_TextPool_IoHelper)) {
            var26.readEx_Z = true;
         } else {
            var var28 = AiPaceScript.putAt_Supplier_FileStore_TextStor(var20, var14, this.putAt_FileStore_RingUtil);
            Thread.ofVirtual().name("autism-wr-travel").start(() -> {
               try {
                  AiPaceScript.putAt(var28, var27, var22, var25, () -> var26.fill_Z, var26.putAt_TextPool_IoHelper);
               } catch (Throwable var9x) {
                  if (putAt_Z) {
                     ListPool.init("wr travel search failed: " + var9x);
                  }

                  var26.putAt_TextPool_IoHelper.putAt(false, null);
               } finally {
                  var26.readEx_Z = true;
               }
            });
            boolean var29 = !var18.onGround() || Math.hypot(var18.vx(), var18.vz()) > 0.005;
            var26.putAt(var29 ? 8000000L : 6000000L);
         }
      }
   }

   private Predicate<BlockPos> putAt_Predicate_TokenCore_TableCac_Predicate(NodeWalker.Inner_StreamUtil var1, Predicate<BlockPos> var2) {
      if (var2 != null && var1.onGround()) {
         BlockPos var3 = BlockPos.containing(var1.x(), var1.y() + 1.0E-4, var1.z());
         BlockPos var4 = i.init(this.putAt_Minecraft);
         if (var3.equals(var4)) {
            return var2;
         }

         int var5 = var3.getX();
         int var6 = var3.getY();
         int var7 = var3.getZ();
         int var8 = var4.getX();
         int var9 = var4.getZ();
         return var7x -> var2.test(var7x)
            || var7x.equals(var3)
            || var7x.getY() == var6 && (var7x.getX() == var5 && var7x.getZ() == var9 || var7x.getX() == var8 && var7x.getZ() == var7);
      } else {
         return var2;
      }
   }

   private void tick_V() {
      this.putAt_RingCache_JsonCach = null;
      if (this.putAt_RingCache_TaskStat != null) {
         this.putAt_RingCache_TaskStat.fill_Z = true;
         this.putAt_RingCache_TaskStat = null;
      }
   }

   private boolean putAt_Z_LocalPlayer(LocalPlayer var1) {
      AiWrExecutor.Inner_JsonCache var2 = this.putAt_RingCache_JsonCach;
      if (var2 == null) {
         return false;
      }

      if (var1.isInWater()) {
         this.putAt_RingCache_JsonCach = null;
         return false;
      }

      if (!var1.onGround() && !var1.onClimbable()) {
         if (var2.putAt_TokenCore_TableCac != null) {
            AiPacePlayback.putAt_V_LocalPlayer_TokenCore_TableCac(var1, var2.putAt_TokenCore_TableCac);
         } else {
            AiPacePlayback.putAt_V_LocalPlayer(var1);
         }

         var2.value = 0;
         return true;
      } else {
         if (var1.onClimbable()) {
            this.chunkAt_V();
         } else {
            if (var1.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
               AiPacePlayback.putAt_V_LocalPlayer(var1);
               var2.value = 0;
               return true;
            }

            if (var2.value < 2) {
               AiPacePlayback.fill_V_LocalPlayer(var1);
               var2.value++;
               return true;
            }
         }

         this.putAt_RingCache_JsonCach = null;
         int var3 = putAt_I_List_2(this.putAt_List, var1.position(), var2.flowState, this.pollAt_I, Math.min(var2.fill, this.putAt_List.size()));
         if (var3 > this.pollAt_I) {
            for (int var4 = this.pollAt_I; var4 < var3; var4++) {
               this.fill_D = this.fill_D + this.putAt_List.get(var4).costTicks();
            }
         }

         this.pollAt_I = var3;
         int var5 = putAt_I_List(this.putAt_List, this.pollAt_I, this::putAt_FileStore_BitCache_BlockPos, this::putAt_Z_BlockPos, AiRoutePolicy::fill);
         if (var5 <= this.pollAt_I) {
            return false;
         }

         this.putAt(var1, this.pollAt_I, var5, var2.putAt_Z, var2.readEx, 0, 0);
         return this.putAt_RingCache_TaskStat != null && this.fill(var1);
      }
   }

   private boolean fill(LocalPlayer var1) {
      AiWrExecutor.Inner_TaskState var2 = this.putAt_RingCache_TaskStat;
      if (var2 == null) {
         return false;
      }

      if (this.frameOf_Z && var1.onGround() && this.pollAt_I < this.putAt_List.size() && !this.value(this.putAt_List.get(this.pollAt_I))) {
         this.nextBy();
         return true;
      }

      if (var2.value_Z) {
         Vec3 var14 = var1.getDeltaMovement();
         if (putAt_Z_Z(this.fill_Z && this.nextBy_Z, var2.flowTemp >= this.putAt_List.size(), var1.onGround(), var1.isInWater(), var14.x, var14.z)
            && ++var2.prev <= 6) {
            NodeWalker.Inner_DataUtil var17 = AiPaceScript.putAt(var1.getYRot(), putAt_TokenCore_TableCac_LocalPlayer(var1));
            this.putAt_V_LocalPlayer_TokenCore_CacheHel(var1, new NodeWalker.Inner_DataUtil(var17.forward(), var17.strafe(), false, false, true, var17.yaw()));
            return true;
         } else {
            this.putAt_V_RingCache_TaskStat(var2);
            return false;
         }
      } else if (!var2.prevEx_Z || !var1.onGround() && !var1.onClimbable()) {
         if (var1.onGround() || var1.onClimbable()) {
            int var10 = AiRoutePolicy.putAt_I_List(this.putAt_List, this.pollAt_I, 5, this.putAt_Function(), this.dataEx_Z);
            if (var10 < 0) {
               var10 = AiRoutePolicy.putAt_I_List_2(this.putAt_List, this.pollAt_I, 5, this.prev(var1), false);
            }

            if (var10 >= 0) {
               this.tick_V();
               this.fill_Str = "a move ahead turned impossible";
               this.putAt_V_BlockPos_FileStore_PacketHe(this.putAt_List.get(var10).to(), this.putAt_List.get(var10));
               return true;
            }
         }

         if (var2.putAt_TextPool_IoHelper.flowTemp() == 0) {
            if (var2.putAt_TextPool_IoHelper.putAt_Z()) {
               return this.putAt_Z_LocalPlayer_2(var1, var2, "wr search found no way on");
            }

            if (++var2.prevEx_I > 60) {
               return this.putAt_Z_LocalPlayer_2(var1, var2, "wr startup timed out");
            }

            if (var1.onClimbable() && !var1.isInWater()) {
               this.chunkAt_V();
            } else if (var1.onGround() && var1.getDeltaMovement().horizontalDistanceSqr() < 1.0E-6) {
               AiPacePlayback.fill_V_LocalPlayer(var1);
            } else {
               AiPacePlayback.putAt_V_LocalPlayer(var1);
            }

            if (var2.value_I < 2 && var1.onGround() && var1.getDeltaMovement().horizontalDistanceSqr() < 1.0E-6) {
               NodeWalker.Inner_StreamUtil var13 = putAt_TokenCore_TableCac_LocalPlayer(var1);
               double var16 = Math.hypot(var13.x() - var2.putAt_TokenCore_TableCac.x(), var13.z() - var2.putAt_TokenCore_TableCac.z());
               if (var13.pose() == 0 && (var16 > 0.025 || var13.pose() != var2.putAt_TokenCore_TableCac.pose())) {
                  this.putAt(var1, var2.flowState, var2.flowTemp, var2.putAt_Z, var2.readEx_I, var2.prevEx_I, var2.value_I + 1);
               }
            }

            return true;
         } else {
            if (var2.putAt_TableUtil.flowState() == 0) {
               NodeWalker.Inner_StreamUtil var11 = putAt_TokenCore_TableCac_LocalPlayer(var1);
               double var4 = Math.hypot(var11.x() - var2.putAt_TokenCore_TableCac.x(), var11.z() - var2.putAt_TokenCore_TableCac.z());
               if (var4 > 0.025 || var11.pose() != var2.putAt_TokenCore_TableCac.pose()) {
                  if (var2.value_I < 2 && var1.onGround() && var1.getDeltaMovement().horizontalDistanceSqr() < 1.0E-6 && var11.pose() == 0) {
                     this.putAt(var1, var2.flowState, var2.flowTemp, var2.putAt_Z, var2.readEx_I, var2.prevEx_I, var2.value_I + 1);
                     return true;
                  } else {
                     return this.putAt_Z_LocalPlayer_2(var1, var2, "wr start drifted before tick 0");
                  }
               }
            }

            BlockPos var12 = this.putAt_BlockPos_I(var2.flowTemp);
            if (var12 != null && var1.position().distanceToSqr(Vec3.atCenterOf(var12)) <= 12.25) {
               this.putAt_V_BlockPos(var12);
            }

            Vec3 var15 = null;
            Runnable var5 = null;
            if (!AutismLiteVariant.enabled() && this.fill_Function != null && var1.onGround()) {
               var15 = this.fill_Function
                  .apply(var2x -> AiRoutePolicy.putAt_Z_AABB(var2x, this.putAt_List, this.pollAt_I, var1.getBoundingBox(), var1.getDeltaMovement()));
               if (var15 != null) {
                  var5 = this.putAt_Runnable;
               }
            }

            Vec3 var6 = null;
            if (this.putAt_Function != null && this.fill_Z && var2.flowTemp >= this.putAt_List.size()) {
               var6 = this.putAt_Function.apply(this.putAt_List.get(this.putAt_List.size() - 1).to());
            }

            AiPacePlayback.Inner_JsonCache var7 = var2.putAt_TableUtil.putAt(var1, var15, var5, var6, var2.putAt_Predicate);
            this.putAt_V_LocalPlayer_RingCache_TaskStat(var1, var2);
            switch (var7) {
               case putAt_TableUtil_JsonCach:
                  var2.prevEx_I = 0;
                  this.flushOf_V();
                  return true;
               case readEx:
                  if (++var2.prevEx_I > 60) {
                     return this.putAt_Z_LocalPlayer_2(var1, var2, "wr search starved mid-stretch");
                  } else {
                     if (!var1.onGround() && !var1.onClimbable() && !var1.isInWater()) {
                        NodeWalker.Inner_StreamUtil var20 = var2.putAt_TableUtil.putAt_TokenCore_TableCac();
                        if (var20 != null) {
                           AiPacePlayback.putAt_V_LocalPlayer_TokenCore_TableCac(var1, var20);
                           return true;
                        }
                     }

                     if (var2.putAt_TableUtil.flowState() <= 0 || var2.prevEx_I < 2 || !var1.onGround() && !var1.onClimbable()) {
                        if (var1.onClimbable() && !var1.isInWater()) {
                           this.chunkAt_V();
                        } else {
                           AiPacePlayback.putAt_V_LocalPlayer(var1);
                        }

                        return true;
                     }

                     return this.putAt_Z_LocalPlayer_2(var1, var2, "wr search starved mid-stretch");
                  }
               case fill:
                  if (!var2.putAt_TextPool_IoHelper.fill_Z()) {
                     return this.putAt_Z_LocalPlayer_2(var1, var2, "wr partial script ended short of goal");
                  } else {
                     Vec3 var19 = var1.getDeltaMovement();
                     if (putAt_Z_Z(this.fill_Z && this.nextBy_Z, var2.flowTemp >= this.putAt_List.size(), var1.onGround(), var1.isInWater(), var19.x, var19.z)) {
                        var2.value_Z = true;
                        var2.prev = 0;
                        NodeWalker.Inner_DataUtil var9 = AiPaceScript.putAt(var1.getYRot(), putAt_TokenCore_TableCac_LocalPlayer(var1));
                        this.putAt_V_LocalPlayer_TokenCore_CacheHel(
                           var1, new NodeWalker.Inner_DataUtil(var9.forward(), var9.strafe(), false, false, true, var9.yaw())
                        );
                        return true;
                     }

                     this.putAt_V_RingCache_TaskStat(var2);
                     return false;
                  }
               case prevEx:
               case value:
                  if (!var1.onGround() && !var1.onClimbable() && !var1.isInWater()) {
                     NodeWalker.Inner_StreamUtil var8 = var2.putAt_TableUtil.putAt_TokenCore_TableCac();
                     if (var8 != null) {
                        AiPacePlayback.putAt_V_LocalPlayer_TokenCore_TableCac(var1, var8);
                        return true;
                     }
                  }

                  String var18 = var2.putAt_TableUtil.putAt_Str() != null ? var2.putAt_TableUtil.putAt_Str() : var7.name();
                  return this.putAt_Z_LocalPlayer_2(var1, var2, "wr " + var7.name().toLowerCase(Locale.ROOT) + ": " + var18);
               default:
                  return true;
            }
         }
      } else {
         int var3 = putAt_I_List(this.putAt_List, this.pollAt_I, this::putAt_FileStore_BitCache_BlockPos, this::putAt_Z_BlockPos, AiRoutePolicy::fill);
         this.tick_V();
         if (var3 > this.pollAt_I) {
            this.putAt_V_LocalPlayer(var1, this.pollAt_I, var3, false, 0);
         }

         return this.putAt_RingCache_TaskStat != null && this.fill(var1);
      }
   }

   private void putAt_V_LocalPlayer_RingCache_TaskStat(LocalPlayer var1, AiWrExecutor.Inner_TaskState var2) {
      if (var2.putAt_TextPool_JsonCache != null) {
         int var3 = Math.max(0, var2.putAt_TableUtil.flowState() - 1);
         int var4 = var2.putAt_TextPool_IoHelper.putAt_I_I(var3);
         if (var4 >= 0) {
            int[] var5 = var2.putAt_TextPool_JsonCache.cellPoint();
            int var6 = var2.chunkAt;

            while (var6 < var5.length && var5[var6] <= var4) {
               var6++;
            }

            int var7 = Math.min(var2.flowTemp, var2.flowState + var6);
            if (var7 > this.pollAt_I) {
               for (int var8 = this.pollAt_I; var8 < var7; var8++) {
                  this.fill_D = this.fill_D + this.putAt_List.get(var8).costTicks();
               }

               this.pollAt_I = var7;
               var2.chunkAt = var6;
            }
         }
      }
   }

   private void putAt_V_RingCache_TaskStat(AiWrExecutor.Inner_TaskState var1) {
      for (int var2 = this.pollAt_I; var2 < var1.flowTemp && var2 < this.putAt_List.size(); var2++) {
         this.fill_D = this.fill_D + this.putAt_List.get(var2).costTicks();
      }

      this.pollAt_I = Math.min(var1.flowTemp, this.putAt_List.size());
      this.tick_V();
      if (this.pollAt_I >= this.putAt_List.size() && this.fill_Z) {
         AiPacePlayback.fill_V_LocalPlayer(this.putAt_Minecraft.player);
      }
   }

   private boolean putAt_Z_LocalPlayer_2(LocalPlayer var1, AiWrExecutor.Inner_TaskState var2, String var3) {
      int var4 = AiRoutePolicy.putAt_I_List(this.putAt_List, this.pollAt_I, 5, this.putAt_Function(), this.dataEx_Z);
      if (var4 < 0 && this.pollAt_I < this.putAt_List.size() && !this.readEx(this.putAt_List.get(this.pollAt_I))) {
         var4 = this.pollAt_I;
      }

      if (var4 >= 0) {
         this.tick_V();
         this.fill_Str = var3 + " (path invalidated)";
         this.putAt_V_BlockPos_FileStore_PacketHe(this.putAt_List.get(var4).to(), this.putAt_List.get(var4));
         return true;
      }

      if (var2.flowTemp > this.pollAt_I && var2.flowTemp <= this.putAt_List.size()) {
         BlockPos var5 = this.putAt_List.get(var2.flowTemp - 1).to();
         if (AiPaceScript.putAt_Z_TokenCore_TaskStat_4(
            this.putAt_NetEntry(), putAt_TokenCore_TableCac_LocalPlayer(var1), var5, false, this.fill_Z && var2.flowTemp == this.putAt_List.size()
         )) {
            this.putAt_V_RingCache_TaskStat(var2);
            return false;
         }
      }

      boolean var14 = !var2.putAt_Z && var2.putAt_TextPool_IoHelper.putAt_Z() && !var2.putAt_TextPool_IoHelper.fill_Z();
      int var6 = var2.readEx_I + 1;
      if ((var14 || var6 <= 3) && this.pollAt_I < var2.flowTemp && var2.flowTemp <= this.putAt_List.size()) {
         if (putAt_Z) {
            ListPool.init("wr recover #" + var6 + " (fine=" + (var14 || var2.putAt_Z) + "): " + var3);
         }

         int var7 = var2.flowState;
         int var8 = var2.flowTemp;
         boolean var9 = var14 || var2.putAt_Z;
         NodeWalker.Inner_StreamUtil var10 = !var1.onGround() && !var1.onClimbable() ? var2.putAt_TableUtil.putAt_TokenCore_TableCac() : null;
         this.tick_V();
         boolean var11 = var1.onGround() && var1.getDeltaMovement().horizontalDistanceSqr() <= 1.0E-6 && putAt_TokenCore_TableCac_LocalPlayer(var1).pose() == 0;
         if (!var11) {
            this.putAt_RingCache_JsonCach = new AiWrExecutor.Inner_JsonCache(var7, var8, var9, var6, var10);
            return this.putAt_Z_LocalPlayer(var1);
         }

         int var12 = putAt_I_List_2(this.putAt_List, var1.position(), var7, this.pollAt_I, Math.min(var8, this.putAt_List.size()));
         if (var12 > this.pollAt_I) {
            for (int var13 = this.pollAt_I; var13 < var12; var13++) {
               this.fill_D = this.fill_D + this.putAt_List.get(var13).costTicks();
            }
         }

         this.pollAt_I = var12;
         int var16 = putAt_I_List(this.putAt_List, this.pollAt_I, this::putAt_FileStore_BitCache_BlockPos, this::putAt_Z_BlockPos, AiRoutePolicy::fill);
         if (var16 > this.pollAt_I) {
            this.putAt(var1, this.pollAt_I, var16, var9, var6, 0, 0);
            return true;
         }
      }

      this.tick_V();
      ClassCache.UiHelper var15 = this.pollAt_I < this.putAt_List.size()
         ? this.putAt_List.get(this.pollAt_I)
         : (this.putAt_List.isEmpty() ? null : this.putAt_List.get(this.putAt_List.size() - 1));
      this.fill_Str = var3;
      this.putAt_V_BlockPos_FileStore_PacketHe(var15 == null ? this.putAt_BlockPos() : var15.to(), var15);
      return true;
   }

   static int putAt_I_List_2(List<ClassCache.UiHelper> var0, Vec3 var1, int var2, int var3, int var4) {
      int var5 = Math.max(Math.max(0, var2), var3 - 6);
      int var6 = Math.min(var0.size(), Math.min(var4, var3 + 8));
      int var7 = Math.min(var3, Math.max(0, var6 - 1));
      double var8 = 1.7976931348623157E308;

      for (int var10 = var5; var10 < var6; var10++) {
         BlockPos var11 = ((ClassCache.UiHelper)var0.get(var10)).from();
         double var12 = var1.distanceToSqr(var11.getX() + 0.5, var11.getY(), var11.getZ() + 0.5);
         if (var12 < var8) {
            var8 = var12;
            var7 = var10;
         }
      }

      return var7;
   }

   private BlockPos putAt_BlockPos_I(int var1) {
      if (var1 < this.putAt_List.size() && this.putAt_List.get(var1).type() == ClassCache.Inner_SyncManager.set) {
         BlockPos var2 = this.putAt_List.get(var1).to();
         if (this.putAt_Z_BlockPos(var2)) {
            return var2;
         }
      }

      return null;
   }

   private void putAt_V_BlockPos(BlockPos var1) {
      LocalPlayer var2 = this.putAt_Minecraft.player;
      if (var2 != null && this.putAt_Z_BlockPos(var1) && var2.tickCount - this.syncAt_I >= 3) {
         BlockHitResult var3 = DataUtil.init_BlockHitResult();
         if (var3 != null && (var3.getBlockPos().equals(var1) || var3.getBlockPos().equals(var1.above()))) {
            this.putAt_Minecraft.gameMode.useItemOn(var2, InteractionHand.MAIN_HAND, var3);
            Compat263.swing(var2, InteractionHand.MAIN_HAND);
            this.syncAt_I = var2.tickCount;
         }
      }
   }

   private void fill_V(ClassCache.UiHelper var1) {
      this.pollOf_I = 0;
      this.tick_I = 0;
      this.flushOf_I = 0;
      this.stateOf_I = 0;
      this.readEx_Z = false;
      this.value_Z = false;
      this.prevEx_Z = false;
      this.prev_Z = false;
      this.putAt_BlockPos = null;
      this.putAt_Block = null;
      this.pollAt_Z = false;
      this.pollOf_Z = false;
      this.value_FileStore_PacketHe = null;
      this.prevEx_FileStore_PacketHe = null;
      this.nextBy_I = 0;
      if (var1.type() != ClassCache.Inner_SyncManager.push) {
         this.state_Z = false;
      }

      this.putAt_CacheHelper.init_V_DataUtil_DataUtil(
            var1.type() != ClassCache.Inner_SyncManager.tick && var1.type() != ClassCache.Inner_SyncManager.run
               ? DataUtil.Inner_DataUtil.write
               : DataUtil.Inner_DataUtil.sync
         );
      if (!putAt_Z_FileStore_PacketHe_FileStore_PacketHe(var1, this.fill_FileStore_PacketHe) && this.putAt_ValueSink_CacheHel.putAt(var1, System.nanoTime())) {
         this.syncAt_Z = true;
         this.fill_Str = "looping on " + var1.type() + " " + var1.from().toShortString() + " -> " + var1.to().toShortString();
         this.putAt_V_BlockPos_FileStore_PacketHe(var1.to(), var1);
      } else {
         this.fill_FileStore_PacketHe = null;
         if (AiRoutePolicy.putAt_Z_FileStore_PacketHe_Function(var1, this.putAt_Function())) {
            this.putAt_V_Str("dig cost jumped since planning");
         }
      }
   }

   private static boolean putAt_Z_FileStore_PacketHe_FileStore_PacketHe(ClassCache.UiHelper var0, ClassCache.UiHelper var1) {
      return var0 != null && var1 != null && var0.type() == var1.type() && var0.from().equals(var1.from()) && var0.to().equals(var1.to());
   }

   private void putAt_V_LocalPlayer_FileStore_PacketHe(LocalPlayer var1, ClassCache.UiHelper var2) {
      switch (var2.type()) {
         case get:
            this.tick(var1, var2);
            break;
         case flush:
         case close:
         case open:
         default:
            if (!var2.cellsToBreak().isEmpty() && this.putAt_Z_LocalPlayer(var1, var2, true)) {
               return;
            }

            boolean var3 = var2.to().getY() > var2.from().getY();
            this.putAt_V_LocalPlayer_2(
               var1, var2, this.stateOf_Z && AiRoutePolicy.putAt_Z_FileStore_SyncPool_Z(var2.type(), this.prev(var1)) && !var1.isInWater(), var3, null
            );
            break;
         case poll:
            if (this.putAt_FileStore_BitCache_BlockPos(var2.to()).water()) {
               this.closeOf(var1, var2);
            } else {
               this.putAt_V_LocalPlayer_2(var1, var2, false, false, null);
            }
            break;
         case set:
            this.state(var1, var2);
            break;
         case push:
            this.fill(var1, var2);
            break;
         case pop:
            this.readEx(var1, var2);
            break;
         case next:
            this.value(var1, var2);
            break;
         case stop:
            this.prevEx(var1, var2);
            break;
         case tick:
            this.prev(var1, var2);
            break;
         case run:
            this.chunkAt(var1, var2);
      }
   }

   private void fill(LocalPlayer var1, ClassCache.UiHelper var2) {
      if (!this.putAt_Z_LocalPlayer_FileStore_PacketHe(var1, var2)) {
         this.nextBy();
         BlockPos var3 = var2.cellsToBreak().get(0);

         for (int var4 = 1; var4 < var2.cellsToBreak().size(); var4++) {
            BlockPos var5 = var2.cellsToBreak().get(var4);
            if (!this.putAt_Minecraft.level.getBlockState(var5).isAir()) {
               this.putAt_V_Vec3(Vec3.atCenterOf(var5));
               switch (this.putAt_JsonCache.putAt_JsonCache_CacheHel_BlockPos(var5)) {
                  case fill:
                     this.writeAt++;
                     this.putAt_JsonCache.putAt_V();
                     break;
                  case readEx:
                     this.putAt_V_Str("dig obstruction failed: " + this.putAt_JsonCache.putAt_Str());
                  case putAt_JsonCache_CacheHel:
               }

               return;
            }
         }

         double var10 = var3.getX() + 0.5;
         double var6 = var3.getZ() + 0.5;
         this.putAt_V_List_List(var2.cellsToBreak(), this.putAt_FileStore_BitCache_BlockPos(var3).passable() ? List.of() : List.of(var3));
         if (this.putAt_FileStore_BitCache_BlockPos(var3).passable()) {
            this.putAt_V_LocalPlayer(var1, var10, var6, 0.1);
            AiBlockDigger.putAt_V_Minecraft(this.putAt_Minecraft);
         } else if (!this.value(var3)) {
            BlockPos var8 = var2.from();
            BlockState var9 = this.putAt_Minecraft.level.getBlockState(var8);
            if (!var9.isAir()
               && var9.getFluidState().isEmpty()
               && this.prevEx_FileStore_PacketHe != var2
               && var9.getCollisionShape(this.putAt_Minecraft.level, var8).isEmpty()
               && !var9.getShape(this.putAt_Minecraft.level, var8).isEmpty()
               && var9.getDestroySpeed(this.putAt_Minecraft.level, var8) >= 0.0F) {
               this.putAt_V_Vec3(Vec3.atCenterOf(var8));
               switch (this.putAt_JsonCache.putAt_JsonCache_CacheHel_BlockPos(var8)) {
                  case fill:
                     this.putAt_JsonCache.putAt_V();
                     break;
                  case readEx:
                     this.putAt_JsonCache.putAt_V();
                     this.prevEx_FileStore_PacketHe = var2;
                  case putAt_JsonCache_CacheHel:
               }
            } else {
               this.putAt_V_Vec3(Vec3.atCenterOf(var3));
               this.putAt_V_LocalPlayer(var1, var10, var6, 0.2);
               switch (this.putAt_JsonCache.putAt_JsonCache_CacheHel_BlockPos(var3)) {
                  case fill:
                     this.writeAt++;
                     this.putAt_JsonCache.putAt_V();
                     break;
                  case readEx:
                     this.putAt_V_Str("dig failed: " + this.putAt_JsonCache.putAt_Str());
                  case putAt_JsonCache_CacheHel:
               }
            }
         }
      }
   }

   private boolean putAt_Z_LocalPlayer_FileStore_PacketHe(LocalPlayer var1, ClassCache.UiHelper var2) {
      if (var2.from().getX() != this.getTo || var2.from().getZ() != this.tableEx || var2.from().getY() > this.levelOf) {
         this.getTo = var2.from().getX();
         this.tableEx = var2.from().getZ();
         this.levelOf = var2.from().getY();
      }

      if (this.value_BlockPos != null) {
         if (!this.putAt_FileStore_BitCache_BlockPos(this.value_BlockPos).passable()) {
            this.prevEx_BlockPos = this.value_BlockPos;
            this.state_Z = true;
            AutismClientAddon.LOG.info("[AI] capped the shaft at {}", this.value_BlockPos.toShortString());
            this.value_BlockPos = null;
            return false;
         }

         if (++this.count > 40) {
            this.value_BlockPos = null;
            return false;
         }

         this.nextBy();
         AiBlockDigger.putAt_V_Minecraft(this.putAt_Minecraft);
         if (TaskTimer.init_Z()) {
            TaskTimer.init_V("cap " + this.value_BlockPos.toShortString());
         }

         if (this.fill_RingCache_CacheHel(this.value_BlockPos) == AiWrExecutor.Inner_CacheHelper.readEx) {
            this.value_BlockPos = null;
            return false;
         } else {
            return true;
         }
      } else if (var1.onGround() && !var1.isInWater() && this.pollAt_I() >= 0) {
         BlockPos var3 = var1.blockPosition();
         BlockPos var4 = var3.above(2);
         if (!this.putAt_Z_BlockPos_BlockPos(var3, var4)) {
            return false;
         }

         int var5 = this.putAt_I_BlockPos(var3);
         boolean var6 = var5 >= 4 && this.readEx(var1);
         if (var5 <= 8 && !var6) {
            return false;
         }

         AutismClientAddon.LOG.info("[AI] capping the shaft at {}: {} deep{}", var4.toShortString(), var5, var6 ? ", a hostile after the player" : "");
         this.value_BlockPos = var4;
         this.count = 0;
         FileCache.read_V(var4);
         return this.putAt_Z_LocalPlayer_FileStore_PacketHe(var1, var2);
      } else {
         return false;
      }
   }

   private int putAt_I_BlockPos(BlockPos var1) {
      int var2 = this.levelOf - var1.getY();
      int var3 = 0;

      for (int var4 = this.pollAt_I; var4 < this.putAt_List.size(); var4++) {
         ClassCache.UiHelper var5 = this.putAt_List.get(var4);
         if (var5.type() != ClassCache.Inner_SyncManager.push || var5.to().getX() != this.getTo || var5.to().getZ() != this.tableEx) {
            break;
         }

         var3++;
      }

      return var2 + Math.max(var3, this.addTo);
   }

   private boolean readEx(LocalPlayer var1) {
      for (Mob var3 : this.putAt_Minecraft.level.getEntitiesOfClass(Mob.class, var1.getBoundingBox().inflate(24.0))) {
         if (var3.isAlive() && var3 instanceof Enemy && var3.isAggressive()) {
            return true;
         }
      }

      return false;
   }

   private boolean putAt_Z_BlockPos_BlockPos(BlockPos var1, BlockPos var2) {
      BlockPos var3 = var1.above();
      if (this.putAt_FileStore_BitCache_BlockPos(var3).passable()
         && this.putAt_FileStore_BitCache_BlockPos(var2).passable()
         && this.putAt_FileStore_BitCache_BlockPos(var2.above()).passable()) {
         for (Direction var5 : Plane.HORIZONTAL) {
            if (this.putAt_FileStore_BitCache_BlockPos(var3.relative(var5)).passable()
               || this.putAt_FileStore_BitCache_BlockPos(var2.relative(var5)).passable()) {
               return false;
            }
         }

         if (this.state_Z
            && this.prevEx_BlockPos != null
            && this.prevEx_BlockPos.getY() > var1.getY()
            && Math.max(Math.abs(this.prevEx_BlockPos.getX() - var1.getX()), Math.abs(this.prevEx_BlockPos.getZ() - var1.getZ())) <= 6) {
            return false;
         }

         for (int var6 = this.pollAt_I; var6 < this.putAt_List.size(); var6++) {
            BlockPos var7 = this.putAt_List.get(var6).to();
            if (var7.getX() == var2.getX() && var7.getZ() == var2.getZ() && var7.getY() >= var2.getY() - 1) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private void readEx(LocalPlayer var1, ClassCache.UiHelper var2) {
      if (!this.putAt_Z_LocalPlayer(var1, var2, true)) {
         Vec3 var3 = this.putAt_Vec3_LocalPlayer(var1);
         if (var3 == null && this.putAt_Function != null && this.pollAt_I == this.putAt_List.size() - 1) {
            var3 = this.putAt_Function.apply(var2.to());
         }

         AiBlockDigger.putAt_V_Minecraft(this.putAt_Minecraft);
         this.putAt_V_LocalPlayer_2(
            var1, var2, false, false, var3 != null ? var3 : new Vec3(var2.to().getX() + 0.5, var1.getEyePosition().y, var2.to().getZ() + 0.5)
         );
      }
   }

   private void value(LocalPlayer var1, ClassCache.UiHelper var2) {
      if (!this.putAt_Z_LocalPlayer(var1, var2, true)) {
         AiBlockDigger.putAt_V_Minecraft(this.putAt_Minecraft);
         Vec3 var3 = this.putAt_Vec3_LocalPlayer(var1);
         if (var3 == null) {
            var3 = new Vec3(var2.to().getX() + 0.5, var1.getEyePosition().y, var2.to().getZ() + 0.5);
         }

         if (!var1.onGround() && !var1.isInWater() && !var1.onClimbable()) {
            this.putAt_V_Vec3(var3);
            this.nextBy();
            this.putAt_V_LocalPlayer_Z(var1, false);
         } else {
            this.putAt_V_LocalPlayer_2(var1, var2, false, false, var3);
         }
      }
   }

   private void prevEx(LocalPlayer var1, ClassCache.UiHelper var2) {
      boolean var3 = false;

      for (BlockPos var5 : var2.cellsToBreak()) {
         if (var5.getX() == var2.from().getX()
            && var5.getZ() == var2.from().getZ()
            && !putAt_Z_FileStore_BitCache(this.putAt_FileStore_BitCache_BlockPos(var5))) {
            var3 = true;
         }
      }

      if (!this.putAt_Z_LocalPlayer(var1, var2, !var3)) {
         AiBlockDigger.putAt_V_Minecraft(this.putAt_Minecraft);
         Vec3 var6 = this.putAt_Vec3_LocalPlayer(var1);
         this.putAt_V_LocalPlayer_2(var1, var2, false, var1.onGround(), var6);
      }
   }

   private boolean putAt_Z_LocalPlayer(LocalPlayer var1, ClassCache.UiHelper var2, boolean var3) {
      var var4 = var2.cellsToBreak();
      ArrayList var5 = new ArrayList(var4.size());

      for (BlockPos var7 : var4) {
         if (!putAt_Z_FileStore_BitCache(this.putAt_FileStore_BitCache_BlockPos(var7))) {
            var5.add(var7);
         }
      }

      this.putAt_V_List_List(var4, var5);
      if (var5.isEmpty()) {
         if (!this.fill(var4)) {
            return false;
         }

         this.nextBy();
         return true;
      } else {
         BlockPos var21 = null;
         Vec3 var22 = null;
         if (!this.putAt_CacheHelper.init_Z()) {
            Vec3 var8 = var1.getEyePosition();
            double var9 = var1.blockInteractionRange();
            EventBus.Inner_CachePool var11 = this.putAt_EventBus_JsonCache();
            Vec3 var12 = var3 && var2.type() == ClassCache.Inner_SyncManager.stop ? AiRoutePolicy.putAt_Vec3_Vec3_FileStore_PacketHe(var8, var2) : var8;
            this.putAt_EventBus_CacheHelp = this.putAt_EventBus_CacheHelp_FileStore_PacketHe(var2, var5, var12, var1, var11, var9);
            BlockPos var13 = this.putAt_EventBus_CacheHelp == null ? null : EventBus.init(this.putAt_EventBus_CacheHelp, var8, var5, var11, var9);
            if (var13 != null) {
               var21 = var13;
               var22 = EventBus.init(this.putAt_EventBus_CacheHelp, var8, var11, var9);
            }
         }

         if (var21 == null) {
            var21 = (BlockPos)var5.get(0);
         }

         boolean var23 = var22 != null;
         if (this.putAt_Minecraft.gameMode instanceof AutismMultiPlayerGameModeAccessor var24 && var24.autism$getDestroyProgress() > 0.0F) {
            BlockPos var26 = var24.autism$getDestroyBlockPos();
            if (var26 != null && !var26.equals(var21) && var5.contains(var26)) {
               BlockHitResult var28 = DataUtil.init_BlockHitResult();
               BlockHitResult var29 = var28 != null && var28.getBlockPos().equals(var26) ? var28 : CachePool.init(this.putAt_Minecraft, var26);
               if (var29 != null) {
                  var21 = var26.immutable();
                  var22 = var29.getLocation();
                  var23 = false;
               }
            }
         }

         BlockPos var25 = this.putAt_BlockPos_LocalPlayer_BlockPos(var1, var21);
         if (var25 != null && this.value_FileStore_PacketHe != var2) {
            this.nextBy();
            if (!this.putAt_JsonCache.putAt_Z()) {
               this.putAt_V_Vec3(Vec3.atCenterOf(var25));
            }

            switch (this.putAt_JsonCache.putAt_JsonCache_CacheHel_BlockPos(var25)) {
               case fill:
                  this.putAt_JsonCache.putAt_V();
                  break;
               case readEx:
                  this.putAt_JsonCache.putAt_V();
                  this.value_FileStore_PacketHe = var2;
               case putAt_JsonCache_CacheHel:
            }

            return true;
         } else {
            if (this.value(var21)) {
               this.nextBy();
               return true;
            }

            this.chunkAt_Z = var23;
            double var27 = var2.to().getX() + 0.5 - var1.getX();
            double var30 = var2.to().getZ() + 0.5 - var1.getZ();
            boolean var14 = var3
               && var27 * var27 + var30 * var30 > 0.6889
               && (var2.to().getY() >= var2.from().getY() || !putAt_Z_FileStore_BitCache(this.putAt_FileStore_BitCache_BlockPos(var2.to().above())));
            if (var14) {
               this.putAt_Z_F(NodeWalker.putAt_F_D(var1.getX(), var1.getZ(), var2.to().getX() + 0.5, var2.to().getZ() + 0.5));
            } else {
               this.putAt_Minecraft.options.keyUp.setDown(false);
               this.putAt_Minecraft.options.keyDown.setDown(false);
               this.putAt_Minecraft.options.keyLeft.setDown(false);
               this.putAt_Minecraft.options.keyRight.setDown(false);
            }

            this.putAt_Minecraft.options.keyJump.setDown(false);
            this.putAt_Minecraft.options.keyShift.setDown(var1.onClimbable() && !var1.onGround());
            this.putAt_V_LocalPlayer_Z(var1, false);

            try {
               switch (this.putAt_JsonCache.putAt_JsonCache_CacheHel_BlockPos_Vec3(var21, var22)) {
                  case fill:
                     this.putAt_JsonCache.putAt_V();
                     boolean var15 = false;

                     for (BlockPos var17 : var4) {
                        if (!putAt_Z_FileStore_BitCache(this.putAt_FileStore_BitCache_BlockPos(var17))) {
                           var15 = true;
                           break;
                        }
                     }

                     if (!var15 && !this.fill(var4)) {
                        return false;
                     }
                     break;
                  case readEx:
                     this.putAt_V_Str("dig failed: " + this.putAt_JsonCache.putAt_Str());
                  case putAt_JsonCache_CacheHel:
               }
            } finally {
               this.chunkAt_Z = false;
            }

            return true;
         }
      }
   }

   private BlockPos putAt_BlockPos_LocalPlayer_BlockPos(LocalPlayer var1, BlockPos var2) {
      if (CachePool.init(this.putAt_Minecraft, var2) != null) {
         return null;
      }

      Vec3 var3 = var1.getEyePosition();
      BlockHitResult var4 = this.putAt_Minecraft
         .level
         .clip(new ClipContext(var3, Vec3.atCenterOf(var2), net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, var1));
      if (var4.getType() != Type.BLOCK) {
         return null;
      }

      BlockPos var5 = var4.getBlockPos().immutable();
      if (!var5.equals(var2) && !(var3.distanceToSqr(var4.getLocation()) > var1.blockInteractionRange() * var1.blockInteractionRange())) {
         BlockState var6 = this.putAt_Minecraft.level.getBlockState(var5);
         if (!var6.isAir() && var6.getFluidState().isEmpty() && !var6.hasBlockEntity() && !(var6.getDestroySpeed(this.putAt_Minecraft.level, var5) < 0.0F)) {
            Block var7 = var6.getBlock();
            if (!AiRoutePolicy.putAt(var5)
               && (var6.canBeReplaced() || var7 instanceof VegetationBlock || var7 instanceof LeafLitterBlock || var7 instanceof SnowLayerBlock)) {
               return var6.getCollisionShape(this.putAt_Minecraft.level, var5).isEmpty() ? var5 : null;
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

   private EventBus.Inner_DataUtil putAt_EventBus_CacheHelp_FileStore_PacketHe(
      ClassCache.UiHelper var1, List<BlockPos> var2, Vec3 var3, LocalPlayer var4, EventBus.Inner_CachePool var5, double var6
   ) {
      var var8 = new ArrayList<>(var2);
      var8.addAll(this.putAt_List_List(var2));
      var var9 = Set.copyOf(var8);
      boolean var10 = !var9.equals(this.fill_Set);
      this.fill_Set = var9;
      if (var10
         && var1.equals(this.readEx_FileStore_PacketHe)
         && this.fill_EventBus_CacheHelp != null
         && var9.equals(this.readEx_Set)
         && EventBus.init(this.fill_EventBus_CacheHelp, var3, var2, var5, var6) != null) {
         this.readEx_FileStore_PacketHe = null;
         return this.fill_EventBus_CacheHelp;
      } else {
         return EventBus.init_EventBus_DataUtil_EventBus_DataUtil(
            this.putAt_EventBus_CacheHelp, var10, var3, var4.getYRot(), var4.getXRot(), var2, var8, var5, var6, this.putAt_F()
         );
      }
   }

   private float putAt_F() {
      if (this.pollAt_I >= this.putAt_List.size()) {
         return 0.0F / 0.0F;
      }

      BlockPos var1 = this.putAt_List.get(this.pollAt_I).from();
      BlockPos var2 = this.putAt_List.get(Math.min(this.putAt_List.size() - 1, this.pollAt_I + 3)).to();
      return Math.hypot(var2.getX() - var1.getX(), var2.getZ() - var1.getZ()) < 1.5
         ? 0.0F / 0.0F
         : NodeWalker.putAt_F_D(var1.getX(), var1.getZ(), var2.getX(), var2.getZ());
   }

   private List<BlockPos> putAt_List_List(List<BlockPos> var1) {
      int var2 = -1;

      for (int var3 = this.pollAt_I; var3 < Math.min(this.putAt_List.size(), this.pollAt_I + 3) && var2 < 0; var3++) {
         if (!Collections.disjoint(this.putAt_List.get(var3).cellsToBreak(), var1)) {
            var2 = var3;
         }
      }

      if (var2 >= 0 && var2 + 1 < this.putAt_List.size()) {
         ClassCache.UiHelper var7 = this.putAt_List.get(var2 + 1);
         if (var7.type() != ClassCache.Inner_SyncManager.pop && var7.type() != ClassCache.Inner_SyncManager.stop && var7.type() != ClassCache.Inner_SyncManager.next) {
            return List.of();
         }

         ArrayList<BlockPos> var4 = new ArrayList<>();

         for (BlockPos var6 : var7.cellsToBreak()) {
            if (!putAt_Z_FileStore_BitCache(this.putAt_FileStore_BitCache_BlockPos(var6)) && !var1.contains(var6)) {
               var4.add(var6);
            }
         }

         return var4;
      } else {
         return List.of();
      }
   }

   private void putAt_V_List_List(List<BlockPos> var1, List<BlockPos> var2) {
      for (BlockPos var4 : var1) {
         if (var2.contains(var4)) {
            this.putAt_Set.add(var4);
         } else if (this.putAt_Set.remove(var4)) {
            this.writeAt++;
         }
      }
   }

   private EventBus.Inner_CachePool putAt_EventBus_JsonCache() {
      return var1 -> this.putAt_Minecraft.level.getBlockState(var1).getShape(this.putAt_Minecraft.level, var1);
   }

   private Vec3 putAt_Vec3_LocalPlayer(LocalPlayer var1) {
      if (!this.putAt_CacheHelper.init_Z() && this.pollAt_I + 1 < this.putAt_List.size()) {
         ClassCache.UiHelper var2 = this.putAt_List.get(this.pollAt_I + 1);
         if (var2.cellsToBreak().isEmpty()) {
            return null;
         }

         ArrayList var3 = new ArrayList();

         for (BlockPos var5 : var2.cellsToBreak()) {
            if (!putAt_Z_FileStore_BitCache(this.putAt_FileStore_BitCache_BlockPos(var5))) {
               var3.add(var5);
            }
         }

         if (var3.isEmpty()) {
            return null;
         }

         Vec3 var15 = new Vec3(var2.from().getX() + 0.5, var2.from().getY() + var1.getEyeHeight(), var2.from().getZ() + 0.5);
         double var16 = var1.blockInteractionRange();
         EventBus.Inner_CachePool var7 = this.putAt_EventBus_JsonCache();
         ArrayList var8 = new ArrayList(var3);
         var8.addAll(this.putAt_List_List(var3));
         var var9 = Set.copyOf(var8);
         if (!var2.equals(this.readEx_FileStore_PacketHe)
            || !var9.equals(this.readEx_Set)
            || this.fill_EventBus_CacheHelp == null
            || EventBus.init(this.fill_EventBus_CacheHelp, var15, var3, var7, var16) == null) {
            this.fill_EventBus_CacheHelp = EventBus.init_EventBus_DataUtil_Vec3(
               var15, var1.getYRot(), var1.getXRot(), var3, var8, var7, var16, this.putAt_F()
            );
            this.readEx_FileStore_PacketHe = var2;
            this.readEx_Set = var9;
         }

         Vec3 var10 = this.fill_EventBus_CacheHelp == null ? null : EventBus.init(this.fill_EventBus_CacheHelp, var15, var7, var16);
         if (var10 == null) {
            return null;
         }

         RotationMath.Inner_DataUtil var11 = RotationMath.init(var10, var15);
         float var12 = Math.abs(var11.load()) > 70.0F ? var1.getYRot() : var11.init();
         Vec3 var13 = var1.getEyePosition().add(Vec3.directionFromRotation(var11.load(), var12).scale(Math.max(2.0, var10.distanceTo(var15))));
         float var14 = EventBus.load(var13.subtract(var1.getEyePosition()));
         if (Math.abs(var14) > 70.0F) {
            var13 = var1.getEyePosition().add(Vec3.directionFromRotation(var14, var1.getYRot()).scale(3.0));
         }

         return var13;
      } else {
         return null;
      }
   }

   private static boolean putAt_Z_FileStore_BitCache(ClassCache.Inner_FileCache var0) {
      return var0.passable() && !var0.web();
   }

   private boolean fill(List<BlockPos> var1) {
      for (Entity var3 : this.putAt_Minecraft.level.entitiesForRendering()) {
         if (var3 instanceof FallingBlockEntity) {
            BlockPos var4 = var3.blockPosition();

            for (BlockPos var6 : var1) {
               if (var4.getX() == var6.getX() && var4.getZ() == var6.getZ() && var4.getY() >= var6.getY() - 1 && var4.getY() <= var6.getY() + 8) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private void prev(LocalPlayer var1, ClassCache.UiHelper var2) {
      BlockPos var3 = var2.cellsToPlace().get(0);
      if (this.prev_Z || var2.cellsToBreak().isEmpty() || !var1.onGround() || !this.putAt_Z_LocalPlayer(var1, var2, false)) {
         if (!this.prev_Z) {
            double var4 = var3.getX() + 0.5 - var1.getX();
            double var6 = var3.getZ() + 0.5 - var1.getZ();
            this.nextBy();
            this.putAt_Minecraft.options.keyShift.setDown(true);
            if (!var1.onGround()) {
               return;
            }

            if (var4 * var4 + var6 * var6 > 0.04) {
               this.putAt_CacheHelper.init_V_ce_DataUtil(putAt_TaskUtil_CacheHelp_LocalPlayer(var1));
               this.putAt_V_LocalPlayer(var1, var3.getX() + 0.5, var3.getZ() + 0.5, 0.08);
               return;
            }

            if (!AiRoutePolicy.putAt_Z_D(var1.getY(), var3.getY(), var2.repairFarmland())
               && (!this.value(var1) || !AiRoutePolicy.putAt_Z_D(var1.getY() + 0.125, var3.getY(), false))) {
               this.putAt_V_Str("pillar takeoff is below the new support");
               return;
            }

            Vec3 var8 = var1.getDeltaMovement();
            if (var8.x * var8.x + var8.z * var8.z > 0.0064) {
               return;
            }

            if (this.flowTemp(var3) < 0 && (this.flushOf_Z() || this.stateOf_Z())) {
               return;
            }

            this.putAt_CacheHelper.init_V_ce_DataUtil(putAt_TaskUtil_CacheHelp_LocalPlayer(var1));
            this.putAt_CacheHelper.load_V();
            if (this.putAt_CacheHelper.init_D() > 6.0) {
               return;
            }

            this.prev_Z = true;
         }

         this.putAt_CacheHelper.init_V_ce_DataUtil(putAt_TaskUtil_CacheHelp_LocalPlayer(var1));
         this.putAt_Minecraft.options.keyJump.setDown(true);
         this.putAt_Minecraft.options.keyUp.setDown(false);
         this.putAt_V_LocalPlayer_Z(var1, false);
         this.putAt_Minecraft.options.keyShift.setDown(false);
         if (this.value_Z) {
            this.putAt_Minecraft.options.keyJump.setDown(false);
         } else if (this.readEx(var3)) {
            this.value_Z = true;
            this.putAt_Minecraft.options.keyJump.setDown(false);
            if (this.putAt_BlockPos != null) {
               this.reset++;
               AiRoutePolicy.putAt_V_BlockPos_Block(var3, this.putAt_Block);
               FileCache.write_V(var3);
               AutismClientAddon.LOG.info(
                     "[AI] placed {} at {} (pillar, {} -> {})",
                     this.putAt_Block,
                     var3.toShortString(),
                     this.putAt_FileStore_PacketHe == null ? "-" : this.putAt_FileStore_PacketHe.from().toShortString(),
                     this.putAt_FileStore_PacketHe == null ? "-" : this.putAt_FileStore_PacketHe.to().toShortString()
                  );
            }
         } else if (this.putAt_BlockPos != null) {
            if (++this.flushOf_I > 6) {
               this.putAt_V_Str("pillar block never confirmed");
            }
         } else {
            if (var1.getY() > var3.getY() + 1.01) {
               if (this.tick_I >= 6) {
                  this.putAt_V_Str("pillar place attempts used up");
                  return;
               }

               AiWrExecutor.Inner_CacheHelper var9 = this.putAt_RingCache_CacheHel_BlockPos(var3);
               if (var9 == AiWrExecutor.Inner_CacheHelper.putAt_RingCache_CacheHel) {
                  this.putAt_BlockPos = var3;
               }

               if (var9 != AiWrExecutor.Inner_CacheHelper.fill) {
                  this.tick_I++;
               }
            }
         }
      }
   }

   private static RotationMath.Inner_DataUtil putAt_TaskUtil_CacheHelp_LocalPlayer(LocalPlayer var0) {
      return new RotationMath.Inner_DataUtil(var0.getYRot(), 90.0F);
   }

   private boolean value(LocalPlayer var1) {
      BlockPos var2 = BlockPos.containing(var1.getX(), var1.getY() - 0.01, var1.getZ());
      return this.putAt_Minecraft.level.getBlockState(var2).is(Blocks.SOUL_SAND);
   }

   private AiWrExecutor.Inner_CacheHelper putAt_RingCache_CacheHel_BlockPos(BlockPos var1) {
      int var2 = this.flowTemp(var1);
      if (var2 < 0) {
         return this.fill_RingCache_CacheHel(var1);
      }

      if (this.putAt_CacheHelper.init_D() > 1.0) {
         return AiWrExecutor.Inner_CacheHelper.fill;
      }

      HitResult var3 = this.putAt_Minecraft.player.pick(this.putAt_Minecraft.player.blockInteractionRange(), 1.0F, false);
      if (var3 instanceof BlockHitResult var4 && var3.getType() == Type.BLOCK && var4.getBlockPos().equals(var1.below()) && var4.getDirection() == Direction.UP
         )
       {
         if (BlockPlacementPlanner.init_Z(this.putAt_Minecraft.level.getBlockState(var4.getBlockPos()), var4.getBlockPos())) {
            this.putAt_Minecraft.options.keyShift.setDown(true);
            if (!this.putAt_Minecraft.player.isShiftKeyDown() || !this.putAt_Minecraft.player.getLastSentInput().shift()) {
               return AiWrExecutor.Inner_CacheHelper.fill;
            }
         }

         this.fill(var2);
         ItemStack var5 = this.putAt_Minecraft.player.getInventory().getItem(var2);
         this.putAt_Str = BuiltInRegistries.ITEM.getKey(var5.getItem()).toString();
         this.readEx_J = ca.init_J_BlockPos(var1);
         this.putAt_Block = ((BlockItem)var5.getItem()).getBlock();
         InteractionResult var6 = this.putAt_Minecraft.gameMode.useItemOn(this.putAt_Minecraft.player, InteractionHand.MAIN_HAND, var4);
         if (var6.consumesAction() && !this.putAt_Minecraft.level.getBlockState(var1).isAir()) {
            AiRoutePolicy.putAt_V_BlockPos_Block(var1, this.putAt_Block);
         }

         return AiWrExecutor.Inner_CacheHelper.putAt_RingCache_CacheHel;
      } else {
         return this.fill_RingCache_CacheHel(var1);
      }
   }

   private boolean pollOf_Z() {
      if (!this.putAt_CacheHelper.init_Z() && this.putAt_FileStore_PacketHe != null) {
         int var1 = this.putAt_FileStore_PacketHe.to().getX() - this.putAt_FileStore_PacketHe.from().getX();
         int var2 = this.putAt_FileStore_PacketHe.to().getZ() - this.putAt_FileStore_PacketHe.from().getZ();
         return this.putAt_FileStore_PacketHe.to().getY() == this.putAt_FileStore_PacketHe.from().getY()
            && Math.abs(var1) + Math.abs(var2) == 1
            && this.putAt_FileStore_PacketHe.cellsToPlace().size() == 1
            && this.putAt_FileStore_PacketHe.cellsToPlace().get(0).equals(this.putAt_FileStore_PacketHe.to().below());
      } else {
         return false;
      }
   }

   private void putAt_V_LocalPlayer(LocalPlayer var1) {
      ClassCache.UiHelper var2 = this.putAt_FileStore_PacketHe;
      int var3 = var2.to().getX() - var2.from().getX();
      int var4 = var2.to().getZ() - var2.from().getZ();

      int var5;
      for (var5 = this.pollAt_I; var5 + 1 < this.putAt_List.size(); var5++) {
         ClassCache.UiHelper var6 = this.putAt_List.get(var5 + 1);
         if (var6.type() != ClassCache.Inner_SyncManager.run
            || !var6.from().equals(this.putAt_List.get(var5).to())
            || var6.to().getX() - var6.from().getX() != var3
            || var6.to().getZ() - var6.from().getZ() != var4
            || var6.to().getY() != var2.from().getY()) {
            break;
         }
      }

      int var15 = var2.from().getY() - 1;
      int var7 = var5 - this.pollAt_I + 1;
      this.initAt = var5;
      int var8 = var5 + 1 < this.putAt_List.size() ? this.putAt_I_FileStore_PacketHe(this.putAt_List.get(var5 + 1), var2.from(), var3, var4, var7) : -1;
      if (var8 > var7) {
         var7 = var8;
         var5++;
      }

      JsonUtil.Inner_CachePool var9 = new JsonUtil.Inner_CachePool(var2.from().getX(), var2.from().getZ(), var15, var3, var4, var7);
      JsonUtil.Inner_ConfigStore var10 = NetHelper.init_JsonUtil_ConfigSto_Z(!this.dataEx_Z, var9, var1, this.pollOf_I());
      boolean var11 = var5 + 1 < this.putAt_List.size() && putAt_Z_FileStore_PacketHe(this.putAt_List.get(var5 + 1), var3, var4);
      if (var9.length() <= 2) {
         var10 = JsonUtil.Inner_ConfigStore.load;
      }

      this.putAt_TokenCodec = new NetHelper(var9, var10, this.putAt_CacheHelper, var11);
      this.stopBy = this.pollAt_I;
      this.dataTo = var5;
      double var12 = 0.0;

      for (int var14 = this.pollAt_I; var14 <= var5; var14++) {
         var12 += ClassCache.Inner_CachePool.putAt_I_D(this.putAt_List.get(var14).costTicks());
      }

      this.setOf = (int)Math.ceil(var12) + 60;
      AutismClientAddon.LOG.info("[AI] bridge {} cells {} from {} toward {},{}", var9.length(), var10.name().toLowerCase(Locale.ROOT), var2.from().toShortString(), var3, var4);
   }

   private static boolean putAt_Z_FileStore_PacketHe(ClassCache.UiHelper var0, int var1, int var2) {
      int var3 = Integer.signum(var0.to().getX() - var0.from().getX());
      int var4 = Integer.signum(var0.to().getZ() - var0.from().getZ());
      return var3 != var1 || var4 != var2;
   }

   private int putAt_I_FileStore_PacketHe(ClassCache.UiHelper var1, BlockPos var2, int var3, int var4, int var5) {
      if (AiRoutePolicy.fill(var1.type()) && var1.to().getY() == var2.getY()) {
         int var6 = var1.to().getX() - var2.getX();
         int var7 = var1.to().getZ() - var2.getZ();
         int var8 = var6 * var3 + var7 * var4;
         int var9 = Math.abs(var6 * var4 - var7 * var3);
         if (var8 > var5 && var9 <= 1) {
            for (int var10 = var5 + 1; var10 <= var8; var10++) {
               BlockPos var11 = new BlockPos(var2.getX() + var3 * var10, var2.getY() - 1, var2.getZ() + var4 * var10);
               ClassCache.Inner_FileCache var12 = this.putAt_FileStore_BitCache_BlockPos(var11);
               if (!var12.loaded()) {
                  return -1;
               }

               if (!var12.passable()) {
                  return var12.top16() >= 16 ? var10 - 1 : -1;
               }

               if (var12.water()
                  || var12.lava()
                  || !this.putAt_FileStore_BitCache_BlockPos(var11.above()).passable()
                  || !this.putAt_FileStore_BitCache_BlockPos(var11.above(2)).passable()) {
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

   private boolean prevEx_Z_LocalPlayer(LocalPlayer var1) {
      if (this.putAt_TokenCodec == null) {
         this.putAt_V_LocalPlayer(var1);
      }

      JsonUtil.Inner_CachePool var2 = this.putAt_TokenCodec.init_JsonUtil_CachePool();
      if (this.putAt_TokenCodec.init_I() > this.setOf) {
         this.putAt_V_Str("bridge over its time budget");
         return true;
      }

      int var3 = this.flowTemp(this.putAt_TokenCodec.init_BlockPos());
      if (var3 < 0 && var1.onGround() && this.putAt_TokenCodec.init_JsonUtil().putAt_I_TokenCore_TaskStat(this.putAt_NetEntry()) <= var2.length()) {
         this.nextBy();
         this.putAt_Minecraft.options.keyShift.setDown(true);
         if (this.putAt_AiOperation != null) {
            this.stateOf_V();
            return true;
         } else if (this.putAt_SyncCore != null && this.putAt_SyncCore.init_Z()) {
            this.store_V();
            return true;
         } else if (!this.flushOf_Z() && !this.stateOf_Z()) {
            this.putAt_V_Str("out of blocks mid-bridge");
            return true;
         } else {
            return true;
         }
      } else {
         NetHelper.Inner_DataUtil var4 = this.putAt_TokenCodec.init(var3, this::fill, var3x -> {
            this.readBy++;
            ItemStack var4x = var1.getInventory().getItem(var3);
            this.putAt_Str = BuiltInRegistries.ITEM.getKey(var4x.getItem()).toString();
            Block var5x = this.putAt_Minecraft.level.getBlockState(var3x).getBlock();
            AiRoutePolicy.putAt_V_BlockPos_Block(var3x, var5x);
            FileCache.write_V(var3x);
         });
         if (var4 == NetHelper.Inner_DataUtil.write) {
            this.putAt_V_Str("bridge footing below the planned support level");
            return true;
         }

         if (var4 == NetHelper.Inner_DataUtil.read) {
            this.putAt_V_Str("fell off the bridge");
            return true;
         }

         double var5 = var2.along(var1.getX(), var1.getZ());

         while (
            this.pollAt_I < this.initAt
               && var5 >= this.pollAt_I - this.stopBy + 1
               && !this.putAt_FileStore_BitCache_BlockPos(this.putAt_List.get(this.pollAt_I).cellsToPlace().get(0)).passable()
         ) {
            this.pollAt_I++;
         }

         this.putAt_FileStore_PacketHe = this.putAt_List.get(this.pollAt_I);
         if (var4 != NetHelper.Inner_DataUtil.load) {
            return true;
         }

         this.fill_D = this.fill_D + this.putAt_List.get(this.dataTo).costTicks();
         this.pollAt_I = this.dataTo + 1;
         this.putAt_FileStore_PacketHe = null;
         this.putAt_TokenCodec = null;

         while (this.pollAt_I < this.putAt_List.size() && putAt_Z_FileStore_PacketHe_LocalPlayer(this.putAt_List.get(this.pollAt_I), var1)) {
            this.fill_D = this.fill_D + this.putAt_List.get(this.pollAt_I++).costTicks();
         }

         return false;
      }
   }

   private static boolean putAt_Z_FileStore_PacketHe_LocalPlayer(ClassCache.UiHelper var0, LocalPlayer var1) {
      if (AiRoutePolicy.putAt_Z(var0) && !(Math.abs(var1.getY() - var0.to().getY()) > 0.6)) {
         double var2 = var0.to().getX() - var0.from().getX();
         double var4 = var0.to().getZ() - var0.from().getZ();
         double var6 = Math.hypot(var2, var4);
         if (var6 < 1.0E-6) {
            return false;
         }

         double var8 = var1.getX() - (var0.to().getX() + 0.5);
         double var10 = var1.getZ() - (var0.to().getZ() + 0.5);
         return (var8 * var2 + var10 * var4) / var6 > -0.3 && Math.abs(var8 * var4 - var10 * var2) / var6 < 1.5;
      } else {
         return false;
      }
   }

   private void chunkAt(LocalPlayer var1, ClassCache.UiHelper var2) {
      BlockPos var3 = var2.cellsToPlace().get(0);
      if (!this.putAt_FileStore_BitCache_BlockPos(var3).passable()) {
         if (!this.value_Z) {
            this.value_Z = true;
            if (this.putAt_BlockPos != null) {
               this.readBy++;
               AiRoutePolicy.putAt_V_BlockPos_Block(var3, this.putAt_Block);
               FileCache.write_V(var3);
               this.putAt_BlockPos = null;
            }
         }
      } else if (this.value_Z) {
         this.value_Z = false;
      }

      if (this.value_Z) {
         this.putAt_Minecraft.options.keyShift.setDown(false);
         this.putAt_V_LocalPlayer_2(var1, var2, false, false, new Vec3(var2.to().getX() + 0.5, var1.getEyePosition().y, var2.to().getZ() + 0.5));
      } else {
         this.putAt_V_Vec3(Vec3.atCenterOf(var3));
         this.putAt_Minecraft.options.keyShift.setDown(true);
         this.putAt_Minecraft.options.keyJump.setDown(false);
         this.putAt_V_LocalPlayer_Z(var1, false);
         if (!var1.onGround() && var1.getY() < var2.from().getY() - 0.5) {
            this.putAt_V_Str("slipped into the gap before the support landed");
         } else if (this.putAt_BlockPos != null) {
            if (++this.flushOf_I > 6) {
               if (!this.readEx_Z) {
                  this.flushOf_I = 0;
                  AiWrExecutor.Inner_CacheHelper var8 = this.fill_RingCache_CacheHel(var3);
                  if (var8 == AiWrExecutor.Inner_CacheHelper.putAt_RingCache_CacheHel) {
                     this.readEx_Z = true;
                  } else if (var8 == AiWrExecutor.Inner_CacheHelper.readEx) {
                     this.putAt_V_Str("bridge place impossible");
                  }
               } else {
                  this.putAt_V_Str("bridge block never confirmed");
               }
            }
         } else {
            double var4 = var1.getEyePosition().distanceTo(Vec3.atCenterOf(var3));
            boolean var6 = var1.onGround() && putAt_D_LocalPlayer_FileStore_PacketHe(var1, var2) <= 0.55;
            this.putAt_Minecraft.options.keyUp.setDown(!var6 && var4 > 1.8);
            if (!(var4 > 4.4)) {
               if (this.tick_I >= 6) {
                  this.putAt_V_Str("no face to place the bridge block on");
               } else {
                  AiWrExecutor.Inner_CacheHelper var7 = this.fill_RingCache_CacheHel(var3);
                  if (var7 == AiWrExecutor.Inner_CacheHelper.putAt_RingCache_CacheHel) {
                     this.putAt_BlockPos = var3;
                  }

                  if (var7 != AiWrExecutor.Inner_CacheHelper.fill) {
                     this.tick_I++;
                  }
               }
            }
         }
      }
   }

   private static double putAt_D_LocalPlayer_FileStore_PacketHe(LocalPlayer var0, ClassCache.UiHelper var1) {
      int var2 = Integer.signum(var1.to().getX() - var1.from().getX());
      int var3 = Integer.signum(var1.to().getZ() - var1.from().getZ());
      if (var2 > 0) {
         return var1.from().getX() + 1.0 - var0.getX();
      } else if (var2 < 0) {
         return var0.getX() - var1.from().getX();
      } else if (var3 > 0) {
         return var1.from().getZ() + 1.0 - var0.getZ();
      } else {
         return var3 < 0 ? var0.getZ() - var1.from().getZ() : 1.0;
      }
   }

   private void state(LocalPlayer var1, ClassCache.UiHelper var2) {
      BlockPos var3 = var2.to();
      boolean var4 = this.putAt_Z_BlockPos(var3);
      int var5 = Integer.signum(var2.to().getX() - var2.from().getX());
      int var6 = Integer.signum(var2.to().getZ() - var2.from().getZ());
      Vec3 var7 = new Vec3(var2.to().getX() + 0.5 + var5 * 1.2, var1.getEyePosition().y, var2.to().getZ() + 0.5 + var6 * 1.2);
      if (var4) {
         this.putAt_V_Vec3(this.putAt_Vec3_BlockPos(var3));
         this.fill_V(var3);
      } else {
         this.putAt_V_Vec3(var7);
      }

      float var8 = NodeWalker.putAt_F_D(var1.getX(), var1.getZ(), var7.x, var7.z);
      boolean var9 = this.putAt_Z_F(var8);
      this.putAt_V_LocalPlayer_Z(var1, this.stateOf_Z && var9 && this.prev(var1));
      this.putAt_Minecraft.options.keyJump.setDown(false);
      this.putAt_Minecraft.options.keyShift.setDown(false);
   }

   private Vec3 putAt_Vec3_BlockPos(BlockPos var1) {
      double var2 = this.putAt_Minecraft.player.getEyeY();
      boolean var4 = this.putAt_Minecraft.level.getBlockState(var1).getBlock() instanceof DoorBlock;
      double var5 = var1.getY() + (var4 ? 1.9 : 0.8);
      double var7 = var1.getY() + 0.1;
      return new Vec3(var1.getX() + 0.5, Math.max(var7, Math.min(var5, var2)), var1.getZ() + 0.5);
   }

   private void fill_V(BlockPos var1) {
      LocalPlayer var2 = this.putAt_Minecraft.player;
      if (var2 != null && this.putAt_Z_BlockPos(var1) && var2.tickCount - this.syncAt_I >= 3) {
         this.putAt_CacheHelper.load_V();
         BlockHitResult var3 = DataUtil.init_BlockHitResult();
         if (var3 != null && (var3.getBlockPos().equals(var1) || var3.getBlockPos().equals(var1.above()))) {
            this.putAt_Minecraft.gameMode.useItemOn(var2, InteractionHand.MAIN_HAND, var3);
            Compat263.swing(var2, InteractionHand.MAIN_HAND);
            this.syncAt_I = var2.tickCount;
         }
      }
   }

   private boolean putAt_Z_FileStore_PacketHe(ClassCache.UiHelper var1) {
      if (this.putAt_Z_BlockPos(var1.to())) {
         this.state(this.putAt_Minecraft.player, var1);
         return false;
      }

      if (AiRoutePolicy.fill(var1.to())) {
         this.fill_BlockPos = var1.to().immutable();
         this.store_I = 0;
         this.dataEx_I = Integer.signum(var1.to().getX() - var1.from().getX());
         this.frameOf_I = Integer.signum(var1.to().getZ() - var1.from().getZ());
      }

      return true;
   }

   private void flushOf_V() {
      if (this.fill_BlockPos != null) {
         BlockPos var1 = this.fill_BlockPos;
         if (this.putAt_Minecraft.player != null
            && this.putAt_Minecraft.level != null
            && this.fill_Z(var1)
            && ++this.store_I <= 24
            && !(this.putAt_Minecraft.player.blockPosition().distSqr(var1) > 36.0)) {
            if (AiRoutePolicy.putAt(
               this.putAt_List_BlockPos(var1), Vec3.atLowerCornerOf(var1), this.putAt_Minecraft.player.getBoundingBox(), this.dataEx_I, this.frameOf_I, 0.05
            )) {
               AABB var2 = new AABB(var1);
               if (this.putAt_Minecraft.level.getBlockState(var1).getBlock() instanceof DoorBlock) {
                  var2 = var2.expandTowards(0.0, 1.0, 0.0).expandTowards(0.0, -1.0, 0.0);
               }

               if (!var2.intersects(this.putAt_Minecraft.player.getBoundingBox())) {
                  this.nextBy();
                  this.putAt_V_Vec3(
                     AiRoutePolicy.putAt_Vec3_List(
                        this.putAt_Minecraft.level.getBlockState(var1).getShape(this.putAt_Minecraft.level, var1).toAabbs(),
                        Vec3.atLowerCornerOf(var1),
                        this.putAt_Minecraft.player.getEyePosition()
                     )
                  );
                  this.putAt_CacheHelper.load_V();
                  if (DataUtil.init_Z_BlockPos(var1)) {
                     BlockHitResult var3 = DataUtil.init_BlockHitResult();
                     if (var3 != null) {
                        this.putAt_Minecraft.gameMode.useItemOn(this.putAt_Minecraft.player, InteractionHand.MAIN_HAND, var3);
                        Compat263.swing(this.putAt_Minecraft.player, InteractionHand.MAIN_HAND);
                     }

                     this.fill_BlockPos = null;
                  }
               }
            }
         } else {
            this.fill_BlockPos = null;
         }
      }
   }

   private List<AABB> putAt_List_BlockPos(BlockPos var1) {
      BlockState var2 = this.putAt_Minecraft.level.getBlockState(var1);
      return var2.hasProperty(BlockStateProperties.OPEN)
         ? var2.setValue(BlockStateProperties.OPEN, false).getShape(this.putAt_Minecraft.level, var1).toAabbs()
         : var2.getShape(this.putAt_Minecraft.level, var1).toAabbs();
   }

   private static boolean putAt_Z_BlockState(BlockState var0) {
      return var0.getBlock() instanceof DoorBlock || var0.getBlock() instanceof FenceGateBlock;
   }

   private boolean putAt_Z_BlockPos(BlockPos var1) {
      if (this.putAt_Minecraft.level == null) {
         return false;
      }

      BlockState var2 = this.putAt_Minecraft.level.getBlockState(var1);
      return putAt_Z_BlockState(var2) && var2.hasProperty(BlockStateProperties.OPEN) && !var2.getValue(BlockStateProperties.OPEN);
   }

   private boolean fill_Z(BlockPos var1) {
      if (this.putAt_Minecraft.level == null) {
         return false;
      }

      BlockState var2 = this.putAt_Minecraft.level.getBlockState(var1);
      return putAt_Z_BlockState(var2) && var2.hasProperty(BlockStateProperties.OPEN) && var2.getValue(BlockStateProperties.OPEN);
   }

   private void closeOf(LocalPlayer var1, ClassCache.UiHelper var2) {
      if (!var1.onGround() && !var1.isInWater() && !var1.onClimbable()) {
         this.pollAt(var1, var2);
      } else if (var1.onGround() && !var1.isInWater() && !this.putAt_Z_TokenCore_TableCac_FileStore_PacketHe(putAt_TokenCore_TableCac_LocalPlayer(var1), var2)) {
         this.pollOf(var1, var2);
      } else {
         this.putAt_V_LocalPlayer_2(var1, var2, false, false, null);
      }
   }

   private void pollAt(LocalPlayer var1, ClassCache.UiHelper var2) {
      Vec3 var3 = new Vec3(var2.to().getX() + 0.5, var1.getEyePosition().y, var2.to().getZ() + 0.5);
      this.putAt_V_Vec3(var3);
      double var4 = var2.to().getX() + 0.5;
      double var6 = var2.to().getZ() + 0.5;
      float var8 = NodeWalker.putAt_F_D(var1.getX(), var1.getZ(), var4, var6);
      NodeWalker.Inner_StreamUtil var9 = putAt_TokenCore_TableCac_LocalPlayer(var1);
      BitSetUtil var10 = this.putAt_NetEntry();
      float var11 = this.putAt_CacheHelper.init_F();
      NodeWalker.Inner_DataUtil[] var12 = new NodeWalker.Inner_DataUtil[]{
         putAt_TokenCore_CacheHel_F_F(var11, var8),
         new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, false, var11),
         putAt_TokenCore_CacheHel_F_F(var11, var8 + 180.0F)
      };
      int var13 = -1;
      int var14 = 1;
      double var15 = 1.7976931348623157E308;
      double var17 = 1.7976931348623157E308;

      for (int var19 = 0; var19 < var12.length; var19++) {
         l.Inner_TableCache var20 = l.putAt(var9, var12[var19], false, var10);
         if (var20.down()) {
            double var21 = Math.hypot(var20.at().x() - var4, var20.at().z() - var6);
            if (var21 < var17) {
               var17 = var21;
               var14 = var19;
            }

            if (!(var20.at().y() > var2.to().getY() + 0.5)
               && this.putAt_FileStore_BitCache_BlockPos(BlockPos.containing(var20.at().x(), var2.to().getY(), var20.at().z())).water()
               && var21 < var15) {
               var15 = var21;
               var13 = var19;
            }
         }
      }

      this.putAt_Minecraft.options.keyJump.setDown(false);
      this.putAt_Minecraft.options.keyShift.setDown(false);
      this.putAt_V_LocalPlayer_Z(var1, false);
      switch (var13 >= 0 ? var13 : var14) {
         case 0:
            this.putAt_Z_F(var8);
            break;
         case 2:
            this.putAt_Z_F(var8 + 180.0F);
            break;
         default:
            this.nextBy();
      }
   }

   private boolean putAt_Z_TokenCore_TableCac_FileStore_PacketHe(NodeWalker.Inner_StreamUtil var1, ClassCache.UiHelper var2) {
      BitSetUtil var3 = this.putAt_NetEntry();
      double var4 = var2.to().getX() + 0.5;
      double var6 = var2.to().getZ() + 0.5;

      for (int var8 = 0; var8 < 3; var8++) {
         NodeWalker.Inner_StreamUtil var9 = var1;
         boolean var10 = false;
         boolean var11 = false;
         boolean var12 = false;
         boolean var13 = false;
         ArrayList<NodeWalker.Inner_StreamUtil> var14 = new ArrayList<>();

         for (int var15 = 0; var15 < 60 && !var11; var15++) {
            float var16 = NodeWalker.putAt_F_D(var9.x(), var9.z(), var4, var6);
            NodeWalker.Inner_DataUtil var17 = var10 && var8 != 0
               ? (
                  var8 == 1
                     ? new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, false, var16)
                     : new NodeWalker.Inner_DataUtil(-1.0, 0.0, false, false, false, var16)
               )
               : new NodeWalker.Inner_DataUtil(1.0, 0.0, false, false, false, var16);
            NodeWalker.Inner_StreamUtil var18 = NodeWalker.putAt(var9, var17, var3);
            if (NodeWalker.putAt_Z(var3, var9, var18)) {
               break;
            }

            var9 = var18;
            if (!var9.onGround()) {
               var10 = true;
            } else if (var10) {
               var11 = true;
            } else if (var15 > 20) {
               break;
            }

            if (var10) {
               var14.add(var9);
            }

            BlockPos var19 = BlockPos.containing(var9.x(), var9.y() + 0.01, var9.z());
            if (var10 && !var13 && this.putAt_FileStore_BitCache_BlockPos(var19).water()) {
               var13 = true;
               var12 = !ClassCache.Inner_RenderState.putAt_Z_TokenCore_TaskStat(var3, var9.x(), var9.z(), var19)
                  || !ClassCache.Inner_RenderState.putAt_Z_TokenCore_TaskStat(var3, var14, var19.getY());
            }
         }

         if (var11
            && !var12
            && !(var9.y() > var2.to().getY() + 0.5)
            && this.putAt_FileStore_BitCache_BlockPos(BlockPos.containing(var9.x(), var2.to().getY(), var9.z())).water()) {
            return true;
         }
      }

      return false;
   }

   private void pollOf(LocalPlayer var1, ClassCache.UiHelper var2) {
      double var3 = var2.from().getX() + 0.5;
      double var5 = var2.from().getZ() + 0.5;
      double var7 = var3 - var1.getX();
      double var9 = var5 - var1.getZ();
      NodeWalker.Inner_StreamUtil var11 = putAt_TokenCore_TableCac_LocalPlayer(var1);
      if (Math.hypot(var7, var9) > 1.5) {
         this.putAt_V_LocalPlayer_2(var1, var2, false, false, null);
      } else if (Math.hypot(var7, var9) <= 0.1 && Math.hypot(var11.vx(), var11.vz()) < 0.03) {
         this.nextBy();
         this.putAt_V_Str("no way into the water from the drop's takeoff");
      } else {
         this.putAt_V_LocalPlayer(var1, var3, var5, 0.06);
         this.putAt_Minecraft.options.keyJump.setDown(false);
         this.putAt_Minecraft.options.keyShift.setDown(true);
         this.putAt_V_LocalPlayer_Z(var1, false);
      }
   }

   private void tick(LocalPlayer var1, ClassCache.UiHelper var2) {
      boolean var3 = var2.to().getY() > var2.from().getY();
      if (var1.isInWater()) {
         this.putAt_CacheHelper.init_V();
         this.putAt_V_LocalPlayer(var1, var2.to().getX() + 0.5, var2.to().getZ() + 0.5, 0.15);
         this.putAt_V_LocalPlayer_Z(var1, false);
         this.putAt_Minecraft.options.keyJump.setDown(var3);
         this.putAt_Minecraft.options.keyShift.setDown(!var3);
      } else {
         this.putAt_V_LocalPlayer_2(var1, var2, false, var3, null);
      }
   }

   private void putAt_V_LocalPlayer_2(LocalPlayer var1, ClassCache.UiHelper var2, boolean var3, boolean var4, Vec3 var5) {
      Vec3 var6 = var5 != null ? var5 : AiRoutePolicy.putAt_Vec3_FileStore_PacketHe_D(var2, var1.getEyePosition().y);
      this.putAt_V_Vec3(var6);
      float var7 = NodeWalker.putAt_F_D(var1.getX(), var1.getZ(), var2.to().getX() + 0.5, var2.to().getZ() + 0.5);
      boolean var8 = this.putAt_Z_F(var7);
      this.putAt_V_LocalPlayer_Z(var1, var8 && var3 && !var1.isInWater());
      double var9 = var2.to().getY();
      if (var1.isInWater() && this.pollAt_I < this.putAt_List.size() && this.putAt_FileStore_BitCache_BlockPos(var1.blockPosition().above(2)).passable()) {
         var9 = AiRoutePolicy.putAt_D_List_I(this.putAt_List, this.pollAt_I);
      }

      boolean var11 = var1.isInWater() && AiRoutePolicy.putAt_Z_D(var1.getY(), var9, var1.onGround(), var1.isUnderWater());
      this.putAt_Minecraft.options.keyJump.setDown(var4 || var11);
      this.putAt_Minecraft.options.keyShift.setDown(false);
   }

   private boolean fill_Z(ClassCache.UiHelper var1) {
      LocalPlayer var2 = this.putAt_Minecraft.player;
      if (!var1.cellsToPlace().isEmpty()
         && !this.value_Z
         && !AiRoutePolicy.putAt(
            var2.blockPosition().getX(),
            var2.getY(),
            var2.blockPosition().getZ(),
            var2.onGround() || var2.isInWater() || var2.onClimbable(),
            var1.from().getX(),
            var1.to().getX(),
            var1.from().getZ(),
            var1.to().getZ(),
            var1.to().getY()
         )) {
         return false;
      } else if (var1.type() == ClassCache.Inner_SyncManager.set) {
         int var6 = Integer.signum(var1.to().getX() - var1.from().getX());
         int var4 = Integer.signum(var1.to().getZ() - var1.from().getZ());
         return !this.putAt_Z_BlockPos(var1.to())
            && AiRoutePolicy.putAt(this.putAt_List_BlockPos(var1.to()), Vec3.atLowerCornerOf(var1.to()), var2.getBoundingBox(), var6, var4, 0.05);
      } else if (var1.type() == ClassCache.Inner_SyncManager.get) {
         boolean var5 = var1.to().getY() > var1.from().getY();
         return (var5 ? var2.getY() >= var1.to().getY() + 0.1 : var2.getY() < var1.to().getY() + 0.9 && var2.getY() > var1.to().getY() - 0.5)
            && Math.abs(var2.getX() - (var1.to().getX() + 0.5)) < 1.4
            && Math.abs(var2.getZ() - (var1.to().getZ() + 0.5)) < 1.4
            && (var2.isInWater() || var2.onClimbable() || var2.onGround());
      } else if (var1.type() == ClassCache.Inner_SyncManager.push) {
         return !this.putAt_FileStore_BitCache_BlockPos(var1.cellsToBreak().get(0)).passable()
            ? false
            : var2.getY() < var1.from().getY() - 0.5 && (var2.onGround() || var2.isInWater() || var2.onClimbable());
      } else if (var1.type() == ClassCache.Inner_SyncManager.next) {
         return AiRoutePolicy.putAt_Z_FileStore_PacketHe(
            var1, var2.blockPosition().getX(), var2.blockPosition().getZ(), var2.getY(), var2.onGround() || var2.isInWater() || var2.onClimbable()
         );
      } else if (var1.type() == ClassCache.Inner_SyncManager.poll) {
         return AiRoutePolicy.putAt_Z_D(var2.getY(), var1.to().getY(), var2.onGround(), var2.isInWater(), var2.onClimbable())
            && Math.abs(var2.getX() - (var1.to().getX() + 0.5)) < 1.4
            && Math.abs(var2.getZ() - (var1.to().getZ() + 0.5)) < 1.4;
      } else if (var2.isInWater()
         && this.putAt_FileStore_BitCache_BlockPos(var1.to()).water()
         && !this.putAt_List.isEmpty()
         && var1 != this.putAt_List.get(this.putAt_List.size() - 1)
         && Math.hypot(var2.getX() - (var1.to().getX() + 0.5), var2.getZ() - (var1.to().getZ() + 0.5)) <= 1.2
         && Math.abs(var2.getY() - var1.to().getY()) <= 1.0) {
         return true;
      } else if (var2.blockPosition().getX() == var1.to().getX() && var2.blockPosition().getZ() == var1.to().getZ()) {
         double var3 = ClassCache.Inner_RenderState.putAt(var1.to().getY(), this.putAt_FileStore_BitCache_BlockPos(var1.to().below()));
         return var1.type() != ClassCache.Inner_SyncManager.tick && var1.type() != ClassCache.Inner_SyncManager.stop
            ? Math.abs(var2.getY() - var3) <= 0.6 && (var2.onGround() || var2.isInWater() || var2.onClimbable())
            : var2.onGround() && Math.abs(var2.getY() - var3) <= 0.55;
      } else {
         return false;
      }
   }

   private boolean tick_Z() {
      return this.putAt_FileStore_PacketHe == null
         || AiRoutePolicy.putAt_Z_FileStore_SyncPool(this.putAt_FileStore_PacketHe.type(), this.prev_Z, this.putAt_BlockPos != null || this.value_Z);
   }

   private boolean readEx(ClassCache.UiHelper var1) {
      return AiRoutePolicy.putAt_Z_FileStore_PacketHe(var1, this.putAt_Function(), this.dataEx_Z);
   }

   private boolean value(ClassCache.UiHelper var1) {
      return this.readEx_Predicate != null
         && var1.cellsToBreak().isEmpty()
         && var1.cellsToPlace().isEmpty()
         && var1.jump() == null
         && this.readEx_Predicate.test(var1.to());
   }

   private boolean readEx(BlockPos var1) {
      return this.putAt_FileStore_BitCache_BlockPos(var1).passable()
         ? false
         : this.putAt_BlockPos == null
            || this.putAt_Minecraft.level.getBlockState(var1).getBlock() == this.putAt_Block && ca.init_J_BlockPos(var1) > this.readEx_J;
   }

   private AiWrExecutor.Inner_CacheHelper fill_RingCache_CacheHel(BlockPos var1) {
      int var2 = this.flowTemp(var1);
      if (var2 < 0) {
         if (this.flushOf_Z()) {
            return AiWrExecutor.Inner_CacheHelper.fill;
         } else {
            return this.stateOf_Z() ? AiWrExecutor.Inner_CacheHelper.fill : AiWrExecutor.Inner_CacheHelper.readEx;
         }
      } else {
         this.fill(var2);
         ItemStack var3 = this.putAt_Minecraft.player.getInventory().getItem(var2);
         this.putAt_Str = BuiltInRegistries.ITEM.getKey(var3.getItem()).toString();
         BlockPlacementPlanner.Inner_StateCache var4 = new BlockPlacementPlanner.Inner_StateCache(
               var1,
               this.putAt_Minecraft.player.getEyePosition(),
               this.putAt_Minecraft.player.blockInteractionRange(),
               BlockPlacementPlanner.init(var3, this.putAt_Minecraft.player, InteractionHand.MAIN_HAND)
            )
            .init_U_StateCache_Z(this.putAt_Minecraft.player.isShiftKeyDown() && this.putAt_Minecraft.player.getLastSentInput().shift())
            .init_U_StateCache_U_StreamUtil(new BlockPlacementPlanner.Inner_StreamUtil(6));
         BlockPlacementPlanner.Inner_JsonUtil[] var5 = new BlockPlacementPlanner.Inner_JsonUtil[1];
         BlockPlacementPlanner.Inner_CachePool var6 = BlockPlacementPlanner.init(var4, var5);
         if (var6 == null) {
            return AiWrExecutor.Inner_CacheHelper.readEx;
         }

         this.putAt_CacheHelper.init_V_DataUtil_DataUtil(DataUtil.Inner_DataUtil.write);
         RotationMath.Inner_DataUtil var7 = var6.init_U_DataUtil().init_ce_DataUtil();
         Vec3 var8 = this.putAt_Minecraft.player.getEyePosition();
         Vec3 var9 = var6.init_U_DataUtil().init_Vec3();
         boolean var10 = this.putAt_FileStore_PacketHe != null && this.putAt_FileStore_PacketHe.type() == ClassCache.Inner_SyncManager.tick;
         if (var10 && var7 != null && var9 != null && Math.hypot(var9.x - var8.x, var9.z - var8.z) < 0.4) {
            this.putAt_CacheHelper.init_V_Vec3(var9);
         } else if (var7 != null) {
            this.putAt_CacheHelper.init_V_ce_DataUtil(var7);
         } else {
            this.putAt_CacheHelper.init_V_Vec3(var9);
         }

         this.putAt_CacheHelper.load_V();
         if (!DataUtil.init_Z_BlockPos(var6.init_BlockHitResult().getBlockPos())) {
            return AiWrExecutor.Inner_CacheHelper.fill;
         }

         this.readEx_J = ca.init_J_BlockPos(var1);
         this.putAt_Block = ((BlockItem)var3.getItem()).getBlock();
         InteractionResult var11 = this.putAt_Minecraft.gameMode.useItemOn(this.putAt_Minecraft.player, InteractionHand.MAIN_HAND, var6.init_BlockHitResult());
         if (var11.consumesAction() && !this.putAt_Minecraft.level.getBlockState(var1).isAir()) {
            AiRoutePolicy.putAt_V_BlockPos_Block(var1, this.putAt_Block);
         }

         return AiWrExecutor.Inner_CacheHelper.putAt_RingCache_CacheHel;
      }
   }

   private int flowTemp(BlockPos var1) {
      AiRoutePolicy.Inner_TableCache var2 = AiRoutePolicy.putAt_ValueSink_TableCac();
      if (var2 != null && var1 != null && this.putAt_Minecraft.player != null) {
         if (!var2.fill(var1)) {
            return -1;
         }

         Item var3 = var2.putAt_Item(var1);
         if (var3 != null) {
            for (int var4 = 0; var4 < 9; var4++) {
               if (this.putAt_Minecraft.player.getInventory().getItem(var4).is(var3)) {
                  return var4;
               }
            }

            return -1;
         }
      }

      return this.pollAt_I();
   }

   private int pollAt_I() {
      int var1 = -1;
      int var2 = 2147483647;
      int var3 = -1;

      for (int var4 = 0; var4 < 9; var4++) {
         ItemStack var5 = this.putAt_Minecraft.player.getInventory().getItem(var4);
         if (var5.getItem() instanceof BlockItem var6
            && !(var6.getBlock() instanceof FallingBlock)
            && var6.getBlock().defaultBlockState().isCollisionShapeFullBlock(this.putAt_Minecraft.level, this.putAt_Minecraft.player.blockPosition())
            && !var6.getBlock().defaultBlockState().hasBlockEntity()) {
            String var9 = BuiltInRegistries.ITEM.getKey(var5.getItem()).toString();
            int var8 = UtilCore.init_I(var9);
            if (var8 >= 0 && UtilCore.init(var5, this.putAt_Minecraft.player.getInventory())) {
               if (var9.equals(this.putAt_Str)) {
                  var8 = -1;
               }

               if (var8 < var2 || var8 == var2 && var5.getCount() > var3) {
                  var1 = var4;
                  var2 = var8;
                  var3 = var5.getCount();
               }
            }
         }
      }

      return var1;
   }

   private int pollOf_I() {
      int var1 = 0;

      for (int var2 = 0; var2 < 9; var2++) {
         ItemStack var3 = this.putAt_Minecraft.player.getInventory().getItem(var2);
         if (var3.getItem() instanceof BlockItem var4
            && !(var4.getBlock() instanceof FallingBlock)
            && var4.getBlock().defaultBlockState().isCollisionShapeFullBlock(this.putAt_Minecraft.level, this.putAt_Minecraft.player.blockPosition())
            && !var4.getBlock().defaultBlockState().hasBlockEntity()) {
            String var6 = BuiltInRegistries.ITEM.getKey(var3.getItem()).toString();
            if (UtilCore.init_I(var6) >= 0 && UtilCore.init(var3, this.putAt_Minecraft.player.getInventory())) {
               var1 += var3.getCount();
            }
         }
      }

      return var1;
   }

   private BlockState putAt_BlockState() {
      int var1 = this.pollAt_I();
      return var1 >= 0 && this.putAt_Minecraft.player.getInventory().getItem(var1).getItem() instanceof BlockItem var2
         ? var2.getBlock().defaultBlockState()
         : null;
   }

   private Item putAt_Item() {
      AiRoutePolicy.Inner_TableCache var1 = AiRoutePolicy.putAt_ValueSink_TableCac();
      return var1 != null && this.putAt_FileStore_PacketHe != null && !this.putAt_FileStore_PacketHe.cellsToPlace().isEmpty()
         ? var1.putAt_Item(this.putAt_FileStore_PacketHe.cellsToPlace().get(0))
         : null;
   }

   private boolean flushOf_Z() {
      Item var1 = this.putAt_Item();
      if (!this.closeOf_Z && !this.tick_Z && var1 == null) {
         return false;
      }

      if (this.putAt_SyncCore == null) {
         this.putAt_SyncCore = new UtilCore(this.putAt_Minecraft);
      }

      if (!this.putAt_SyncCore.init_Z() && !this.pollAt_Z) {
         ArrayList var2 = new ArrayList(27);

         for (int var3 = 9; var3 < 36; var3++) {
            var2.add(this.putAt_SyncCore_JsonCache_ItemStack(this.putAt_Minecraft.player.getInventory().getItem(var3)));
         }

         int var7 = -1;
         if (var1 != null) {
            for (int var4 = 9; var4 < 36 && var7 < 0; var4++) {
               if (this.putAt_Minecraft.player.getInventory().getItem(var4).is(var1)) {
                  var7 = var4 - 9;
               }
            }

            if (var7 < 0) {
               return false;
            }
         } else {
            var7 = UtilCore.init(var2, this.putAt_Str);
         }

         if (var7 < 0) {
            return false;
         }

         ArrayList<UtilCore.Inner_DataUtil> var8 = new ArrayList<>(9);

         for (int var5 = 0; var5 < 9; var5++) {
            ItemStack var6 = this.putAt_Minecraft.player.getInventory().getItem(var5);
            var8.add(
               new UtilCore.Inner_DataUtil(var6.isEmpty(), var6.getItem() instanceof BlockItem, var6.has(DataComponents.FOOD), var6.isDamageableItem())
            );
         }

         int var9 = UtilCore.init(var8);
         if (var9 < 0) {
            return false;
         }

         String var10 = var1 != null ? BuiltInRegistries.ITEM.getKey(var1).toString() : ((UtilCore.Inner_CachePool)var2.get(var7)).itemId();
         this.putAt_SyncCore.init_V_I(9 + var7, var9, var10);
         return true;
      } else {
         return false;
      }
   }

   private boolean stateOf_Z() {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      if (this.putAt_AiOperation != null || this.pollOf_Z || this.putAt_Minecraft.player == null) {
         return false;
      }

      if (UtilCore.init(this.putAt_Minecraft.player.getInventory()) <= 0) {
         return false;
      }

      int var1 = 0;

      for (int var2 = this.pollAt_I; var2 < this.putAt_List.size(); var2++) {
         var1 += this.putAt_List.get(var2).cellsToPlace().size();
      }

      this.putAt_AiOperation = aV.init(this.putAt_Minecraft.player.getInventory(), Math.max(1, var1));
      if (this.putAt_AiOperation == null) {
         this.pollOf_Z = true;
         return false;
      } else {
         return true;
      }
   }

   private boolean value(BlockPos var1) {
      if (this.closeOf_Z && this.putAt_SyncCore != null && !this.putAt_SyncCore.init_Z() && !this.pollAt_Z) {
         BlockState var2 = this.putAt_Minecraft.level.getBlockState(var1);
         int var3 = autismclient.util.BlockBreakSpeedHelper.init(this.putAt_Minecraft, var2, 9, false);
         if (var3 < 0
            || var2.requiresCorrectToolForDrops()
               && ClassCache.Inner_StreamUtil.chunkAt(var2)
               && !this.putAt_Minecraft.player.getInventory().getItem(var3).isCorrectToolForDrops(var2)) {
            if (v.init(this.putAt_List_BlockState(var2)) instanceof v.Inner_DataUtil.Inner_CachePool var4) {
               int var6 = var4.backpackSlot();
               this.putAt_SyncCore.init_V_I(
                     var6,
                     this.putAt_Minecraft.player.getInventory().getSelectedSlot(),
                     BuiltInRegistries.ITEM.getKey(this.putAt_Minecraft.player.getInventory().getItem(var6).getItem()).toString()
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

   private UtilCore.Inner_CachePool putAt_SyncCore_JsonCache_ItemStack(ItemStack var1) {
      return var1.getItem() instanceof BlockItem var2 && UtilCore.init(var1, this.putAt_Minecraft.player.getInventory())
         ? new UtilCore.Inner_CachePool(
            BuiltInRegistries.ITEM.getKey(var1.getItem()).toString(),
            var1.getCount(),
            var2.getBlock() instanceof FallingBlock,
            var2.getBlock().defaultBlockState().isCollisionShapeFullBlock(this.putAt_Minecraft.level, this.putAt_Minecraft.player.blockPosition()),
            var2.getBlock().defaultBlockState().hasBlockEntity()
         )
         : new UtilCore.Inner_CachePool(null, 0, false, false, false);
   }

   private List<v.Inner_StreamUtil> putAt_List_BlockState(BlockState var1) {
      boolean var2 = !var1.requiresCorrectToolForDrops();
      ArrayList<v.Inner_StreamUtil> var3 = new ArrayList<>(36);

      for (int var4 = 0; var4 < 36; var4++) {
         ItemStack var5 = this.putAt_Minecraft.player.getInventory().getItem(var4);
         var3.add(
            new v.Inner_StreamUtil(
               var4,
               var5.isDamageableItem() ? var5.getMaxDamage() - var5.getDamageValue() : -1,
               var2 || var5.isCorrectToolForDrops(var1),
               af.load_Z(var5)
            )
         );
      }

      return var3;
   }

   private void stateOf_V() {
      AiOperation.Result var1 = this.putAt_AiOperation.tick();
      if (var1.terminal()) {
         if (var1.status() == AiOperation.Status.SUCCEEDED) {
            this.tick_Z = true;
         } else {
            this.pollOf_Z = true;
         }

         this.putAt_AiOperation = null;
      }
   }

   private void store_V() {
      switch (this.putAt_SyncCore.load_UtilCore_BufferHel()) {
         case read:
            this.poolEx++;
            this.putAt_SyncCore.init_V();
            break;
         case write:
            this.pollAt_Z = true;
            this.putAt_SyncCore.init_V();
      }
   }

   private void fill(int var1) {
      if (this.task == -1) {
         this.initEx = this.putAt_Minecraft.player.getInventory().getSelectedSlot();
      }

      if (this.putAt_Minecraft.player.getInventory().getSelectedSlot() != var1) {
         this.putAt_Minecraft.player.getInventory().setSelectedSlot(var1);
         this.task = var1;
      }
   }

   private BitSetUtil putAt_NetEntry() {
      if (this.putAt_NetEntry == null || this.putAt_NetEntry.putAt_ClientLevel() != this.putAt_Minecraft.level) {
         this.putAt_NetEntry = new BitSetUtil(this.putAt_Minecraft.level, this.putAt_Minecraft.player);
      }

      this.putAt_NetEntry.putAt_V_D(this.putAt_FileStore_RingUtil.putAt_D);
      return this.putAt_NetEntry;
   }

   private RingBuffer putAt_PoolStore() {
      StateCache var1 = this.putAt_PacketHelper != null ? this.putAt_PacketHelper : StateCache.init_StateCache();
      if (this.putAt_PoolStore == null || this.putAt_PoolStore.putAt_ClientLevel() != this.putAt_Minecraft.level || this.fill_PacketHelper != var1) {
         ItemStack[] var2 = new ItemStack[9];
         Arrays.fill(var2, ItemStack.EMPTY);
         this.putAt_PoolStore = new RingBuffer(this.putAt_Minecraft, var2, var1);
         this.fill_PacketHelper = var1;
      }

      return this.putAt_PoolStore;
   }

   private static NodeWalker.Inner_StreamUtil putAt_TokenCore_TableCac_LocalPlayer(LocalPlayer var0) {
      return AiMotionSafety.putAt_TokenCore_TableCac(var0);
   }

   private ClassCache.Inner_FileCache putAt_FileStore_BitCache_BlockPos(BlockPos var1) {
      ClassCache.Inner_FileCache var2 = ClassCache.Inner_StreamUtil.putAt_FileStore_BitCache_Minecraft(
         this.putAt_Minecraft, this.putAt_Minecraft.level, var1, putAt_ItemStackArr
      );
      if (!var2.loaded() && this.putAt_PacketHelper != null) {
         ClassCache.Inner_FileCache var3 = this.putAt_PacketHelper.init_ClassCache_FileCac_I(var1.getX(), var1.getY(), var1.getZ());
         return var3 != null ? var3 : var2;
      } else {
         return var2;
      }
   }

   private Function<BlockPos, ClassCache.Inner_FileCache> putAt_Function() {
      if (this.fill_ItemStackArr == null && this.putAt_Minecraft.player != null) {
         this.fill_ItemStackArr = new ItemStack[36];

         for (int var1 = 0; var1 < 36; var1++) {
            this.fill_ItemStackArr[var1] = this.putAt_Minecraft.player.getInventory().getItem(var1).copy();
         }
      }

      ItemStack[] var2 = this.fill_ItemStackArr != null ? this.fill_ItemStackArr : putAt_ItemStackArr;
      return var2x -> {
         ClassCache.Inner_FileCache var3 = ClassCache.Inner_StreamUtil.putAt_FileStore_BitCache_Minecraft(
            this.putAt_Minecraft, this.putAt_Minecraft.level, var2x, var2
         );
         if (!var3.loaded() && this.putAt_PacketHelper != null) {
            ClassCache.Inner_FileCache var4 = this.putAt_PacketHelper.init_ClassCache_FileCac_I(var2x.getX(), var2x.getY(), var2x.getZ());
            return var4 != null ? var4 : var3;
         } else {
            return var3;
         }
      };
   }

   private void putAt_V_Vec3(Vec3 var1) {
      LocalPlayer var2 = this.putAt_Minecraft.player;
      if (this.readEx_BlockPos != null && !this.putAt_JsonCache.putAt_Z_BlockPos(this.readEx_BlockPos)) {
         this.readEx_BlockPos = null;
      }

      if (this.putAt_JsonCache.fill_Z() && this.putAt_JsonCache.putAt_BlockPos() != null) {
         this.readEx_BlockPos = this.putAt_JsonCache.putAt_BlockPos();
      }

      if (var2 != null && !this.putAt_JsonCache.fill_Z()) {
         Vec3 var3 = var2.getEyePosition();
         if (Mth.floor(var1.x) == Mth.floor(var3.x)
            && Mth.floor(var1.z) == Mth.floor(var3.z)
            && Math.abs(var1.y - var3.y) > 1.0
            && (
               this.putAt_FileStore_PacketHe == null
                  || this.putAt_FileStore_PacketHe.cellsToBreak().isEmpty()
                  || this.putAt_FileStore_PacketHe.cellsToBreak().contains(BlockPos.containing(var1))
            )) {
            BlockPos var12 = BlockPos.containing(var1);
            VoxelShape var13 = this.putAt_Minecraft.level == null
               ? null
               : this.putAt_Minecraft.level.getBlockState(var12).getShape(this.putAt_Minecraft.level, var12);
            boolean var14 = var1.y < var3.y;
            double var7 = var13 != null && !var13.isEmpty()
               ? var12.getY() + (var14 ? var13.max(Axis.Y) : var13.min(Axis.Y))
               : var12.getY() + (var14 ? 1.0 : 0.0);
            this.putAt_CacheHelper.init_V_ce_DataUtil(
                  new RotationMath.Inner_DataUtil(var2.getYRot(), AiMotionSafety.putAt_F_Vec3(var3, var2.getYRot(), var2.getXRot(), var14, var7))
               );
            return;
         }

         if (this.putAt_FileStore_PacketHe != null
            && !this.putAt_FileStore_PacketHe.cellsToBreak().isEmpty()
            && !this.chunkAt_Z
            && this.readEx_BlockPos == null) {
            int var4 = this.putAt_FileStore_PacketHe.to().getX() - this.putAt_FileStore_PacketHe.from().getX();
            int var5 = this.putAt_FileStore_PacketHe.to().getZ() - this.putAt_FileStore_PacketHe.from().getZ();
            if (var4 == 0 != (var5 == 0)
               && Mth.floor(var1.x) == this.putAt_FileStore_PacketHe.to().getX()
               && Mth.floor(var1.z) == this.putAt_FileStore_PacketHe.to().getZ()) {
               double var6 = var4 != 0
                  ? Math.abs(var2.getZ() - (this.putAt_FileStore_PacketHe.from().getZ() + 0.5))
                  : Math.abs(var2.getX() - (this.putAt_FileStore_PacketHe.from().getX() + 0.5));
               if (var6 < 0.3) {
                  double var8 = Math.hypot(var1.x - var3.x, var1.z - var3.z);
                  float var10 = (float)Math.toDegrees(Math.atan2(var3.y - var1.y, Math.max(0.3, var8)));
                  float var11 = NodeWalker.putAt_F_D(0.0, 0.0, var4, var5);
                  if (this.putAt_Z_Vec3(var3, var11, var10, BlockPos.containing(var1))) {
                     this.putAt_CacheHelper.init_V_ce_DataUtil(new RotationMath.Inner_DataUtil(var11, var10));
                     return;
                  }
               }
            }
         }
      }

      this.putAt_CacheHelper.init_V_Vec3(var1);
   }

   private boolean putAt_Z_Vec3(Vec3 var1, float var2, float var3, BlockPos var4) {
      Vec3 var5 = var1.add(Vec3.directionFromRotation(var3, var2).scale(Math.max(4.5, this.putAt_Minecraft.player.blockInteractionRange())));
      BlockHitResult var6 = this.putAt_Minecraft
         .level
         .clip(new ClipContext(var1, var5, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, this.putAt_Minecraft.player));
      return var6.getType() == Type.BLOCK && var6.getBlockPos().equals(var4);
   }

   private void putAt_V_LocalPlayer(LocalPlayer var1, double var2, double var4, double var6) {
      double var8 = Math.toRadians(var1.getYRot());
      double var10 = var2 - var1.getX();
      double var12 = var4 - var1.getZ();
      double var14 = var10 * -Math.sin(var8) + var12 * Math.cos(var8);
      double var16 = var10 * -Math.cos(var8) + var12 * -Math.sin(var8);
      this.putAt_Minecraft.options.keyUp.setDown(var14 > var6);
      this.putAt_Minecraft.options.keyDown.setDown(var14 < -var6);
      this.putAt_Minecraft.options.keyRight.setDown(var16 > var6);
      this.putAt_Minecraft.options.keyLeft.setDown(var16 < -var6);
   }

   private boolean putAt_Z_F(float var1) {
      int[] var2 = AiMotionSafety.putAt_IArr_F_F(this.putAt_CacheHelper.init_F(), var1);
      this.putAt_Minecraft.options.keyUp.setDown(var2[0] > 0);
      this.putAt_Minecraft.options.keyDown.setDown(var2[0] < 0);
      this.putAt_Minecraft.options.keyRight.setDown(var2[1] > 0);
      this.putAt_Minecraft.options.keyLeft.setDown(var2[1] < 0);
      return var2[0] > 0;
   }

   private static NodeWalker.Inner_DataUtil putAt_TokenCore_CacheHel_F_F(float var0, float var1) {
      int[] var2 = AiMotionSafety.putAt_IArr_F_F(var0, var1);
      return new NodeWalker.Inner_DataUtil(var2[0], -var2[1], false, false, false, var0);
   }

   private void putAt_V_LocalPlayer_TokenCore_CacheHel(LocalPlayer var1, NodeWalker.Inner_DataUtil var2) {
      AiPacePlayback.putAt_V_LocalPlayer(var1, var2, var1.getXRot());
   }

   private boolean prev(LocalPlayer var1) {
      return var1.getFoodData().getFoodLevel() > 6;
   }

   private void putAt_V_LocalPlayer_Z(LocalPlayer var1, boolean var2) {
      AiMotionSafety.putAt_V_LocalPlayer_Z(var1, var2);
      var1.setSprinting(var2);
      if (!this.putAt_Minecraft.options.toggleSprint().get()) {
         this.putAt_Minecraft.options.keySprint.setDown(var2);
      }
   }

   private void nextBy() {
      this.putAt_Minecraft.options.keyUp.setDown(false);
      this.putAt_Minecraft.options.keyDown.setDown(false);
      this.putAt_Minecraft.options.keyLeft.setDown(false);
      this.putAt_Minecraft.options.keyRight.setDown(false);
      this.putAt_Minecraft.options.keySprint.setDown(false);
      this.putAt_Minecraft.options.keyJump.setDown(false);
      this.putAt_Minecraft.options.keyShift.setDown(false);
      if (this.putAt_Minecraft.player != null) {
         this.putAt_Minecraft.player.setSprinting(false);
      }
   }

   private void dataEx() {
      this.nextBy();
   }

   private String putAt_Str_FileStore_PacketHe(ClassCache.UiHelper var1) {
      if (var1 != null && this.putAt_Minecraft.level != null) {
         BlockPos var2 = var1.to();
         Function var3 = var1x -> BuiltInRegistries.BLOCK.getKey(this.putAt_Minecraft.level.getBlockState(((BlockPos)var1x)).getBlock()).getPath();
         return " (target feet "
            + (String)var3.apply(var2)
            + ", head "
            + (String)var3.apply(var2.above())
            + ", floor "
            + (String)var3.apply(var2.below())
            + ")";
      } else {
         return "";
      }
   }

   private void putAt_V_Str(String var1) {
      this.fill_Str = var1;
      this.putAt_V_BlockPos_FileStore_PacketHe(
         this.putAt_FileStore_PacketHe == null ? this.putAt_BlockPos() : this.putAt_FileStore_PacketHe.to(), this.putAt_FileStore_PacketHe
      );
   }

   private void putAt_V_BlockPos_FileStore_PacketHe(BlockPos var1, ClassCache.UiHelper var2) {
      if (putAt_Z && this.putAt_Minecraft.player != null) {
         ListPool.init("wr stall move=" + var2 + " reason=" + this.fill_Str + " pos=" + this.putAt_Minecraft.player.position());
      }

      this.prev_BlockPos = var1;
      this.chunkAt_FileStore_PacketHe = var2;
      this.putAt_RingCache_TableCac = AiWrExecutor.Inner_TableCache.value;
      this.state(true);
   }

   private void state(boolean var1) {
      this.tick_V();
      this.putAt_TokenCodec = null;
      this.putAt_Set.clear();
      this.putAt_JsonCache.putAt_V();
      AiMotionSafety.putAt_V();
      this.fill_BlockPos = null;
      if (this.putAt_SyncCore != null) {
         this.putAt_SyncCore.init_V();
      }

      if (this.putAt_AiOperation != null) {
         this.putAt_AiOperation.cancel();
         this.putAt_AiOperation = null;
      }

      if (var1) {
         this.putAt_PoolStore = null;
         this.fill_PacketHelper = null;
         if (this.putAt_Minecraft.player != null && this.task >= 0 && this.putAt_Minecraft.player.getInventory().getSelectedSlot() == this.task) {
            this.putAt_Minecraft.player.getInventory().setSelectedSlot(this.initEx);
         }

         this.task = -1;
      }
   }

   enum Inner_CacheHelper {
      putAt_RingCache_CacheHel,
      fill,
      readEx;
   }

   private static final class Inner_JsonCache {
      final int flowState;
      final int fill;
      final int readEx;
      final boolean putAt_Z;
      final NodeWalker.Inner_StreamUtil putAt_TokenCore_TableCac;
      int value;

      Inner_JsonCache(int var1, int var2, boolean var3, int var4, NodeWalker.Inner_StreamUtil var5) {
         this.flowState = var1;
         this.fill = var2;
         this.putAt_Z = var3;
         this.readEx = var4;
         this.putAt_TokenCore_TableCac = var5;
      }
   }

   public enum Inner_TableCache {
      putAt_RingCache_TableCac,
      fill,
      readEx,
      value,
      prevEx;
   }

   private static final class Inner_TaskState {
      int flowState;
      int flowTemp;
      final double putAt_D;
      final NodeWalker.Inner_StreamUtil putAt_TokenCore_TableCac;
      final AiPaceScript.Inner_JsonCache putAt_TextPool_JsonCache;
      final Predicate<BlockPos> putAt_Predicate;
      final AiPaceScript.Inner_IoHelper putAt_TextPool_IoHelper = new AiPaceScript.Inner_IoHelper();
      final AiPacePlayback putAt_TableUtil;
      final boolean putAt_Z;
      final int readEx_I;
      final int value_I;
      volatile boolean fill_Z;
      volatile boolean readEx_Z;
      int prevEx_I;
      boolean value_Z;
      int prev;
      boolean prevEx_Z;
      int chunkAt;

      Inner_TaskState(
         int var1,
         int var2,
         double var3,
         double var5,
         NodeWalker.Inner_StreamUtil var7,
         AiPaceScript.Inner_JsonCache var8,
         Predicate<BlockPos> var9,
         boolean var10,
         int var11,
         int var12,
         int var13
      ) {
         this.flowState = var1;
         this.flowTemp = var2;
         this.putAt_D = var3;
         this.putAt_TokenCore_TableCac = var7;
         this.putAt_TextPool_JsonCache = var8;
         this.putAt_Predicate = var9;
         this.putAt_TableUtil = new AiPacePlayback(this.putAt_TextPool_IoHelper, var3, var5);
         this.putAt_Z = var10;
         this.readEx_I = var11;
         this.prevEx_I = var12;
         this.value_I = var13;
         this.chunkAt = 0;
      }

      void putAt(long var1) {
         long var3 = System.nanoTime() + var1;

         while (this.putAt_TextPool_IoHelper.flowTemp() == 0 && !this.putAt_TextPool_IoHelper.putAt_Z() && !this.fill_Z && System.nanoTime() < var3) {
            Thread.onSpinWait();
         }
      }
   }
}
