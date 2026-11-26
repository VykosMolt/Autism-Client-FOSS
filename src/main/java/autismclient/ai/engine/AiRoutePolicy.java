package autismclient.ai.engine;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.LongPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AiRoutePolicy {
   static final int flowState = 3;
   static final double putAt_D = 6.0;
   static final double fill_D = 30.0;
   static final int flowTemp = 5;
   static final double readEx_D = 10.0;
   static final int readEx_I = 3;
   static final long putAt_J = 45000000000L;
   static final long fill_J = 500000000L;
   static final double value_D = 0.2;
   private static final ItemStack[] putAt_ItemStackArr = new ItemStack[9];
   private static volatile LongPredicate putAt_LongPredicate = var0 -> false;
   private static volatile long readEx_J;
   private static volatile Set<Long> putAt_Set = Set.of();
   private static volatile long value_J;
   private static volatile AiRoutePolicy.Inner_TableCache putAt_ValueSink_TableCac;

   private AiRoutePolicy() {
   }

   public static void putAt(LongPredicate var0) {
      putAt_LongPredicate = var0;
      readEx_J = System.nanoTime() + 3000000000L;
   }

   public static void putAt_V() {
      putAt_LongPredicate = var0 -> false;
      readEx_J = 0L;
   }

   static boolean putAt(BlockPos var0) {
      return System.nanoTime() < readEx_J && putAt_LongPredicate.test(var0.asLong());
   }

   public static void putAt(Set<Long> var0) {
      putAt_Set = Set.copyOf(var0);
      value_J = var0.isEmpty() ? 0L : System.nanoTime() + 3000000000L;
   }

   static boolean fill(BlockPos var0) {
      return System.nanoTime() < value_J && (putAt_Set.contains(var0.asLong()) || putAt_Set.contains(var0.below().asLong()));
   }

   public static void putAt(AiRoutePolicy.Inner_TableCache var0) {
      putAt_ValueSink_TableCac = var0;
   }

   static AiRoutePolicy.Inner_TableCache putAt_ValueSink_TableCac() {
      return putAt_ValueSink_TableCac;
   }

   static void putAt_V_BlockPos_Block(BlockPos var0, Block var1) {
      AiRoutePolicy.Inner_TableCache var2 = putAt_ValueSink_TableCac;
      if (var2 == null || var2.putAt_Z(var0)) {
         AiScaffold.init(var0, var1);
      }
   }

   static long putAt_J(AiPathfinder.UiHelper var0) {
      return (var0.from().asLong() * 31L + var0.to().asLong()) * 31L + var0.type().ordinal();
   }

   static boolean putAt_Z_AABB(AABB var0, List<AiPathfinder.UiHelper> var1, int var2, AABB var3, Vec3 var4) {
      if (var0.intersects(var3.expandTowards(var4.scale(3.0)).inflate(0.15))) {
         return false;
      }

      for (int var5 = var2; var5 < var1.size(); var5++) {
         AiPathfinder.UiHelper var6 = (AiPathfinder.UiHelper)var1.get(var5);
         BlockPos var7 = var6.from();
         BlockPos var8 = var6.to();
         AABB var9 = new AABB(
            Math.min(var7.getX(), var8.getX()) + 0.19,
            Math.min(var7.getY(), var8.getY()) - 1,
            Math.min(var7.getZ(), var8.getZ()) + 0.19,
            Math.max(var7.getX(), var8.getX()) + 0.81,
            Math.max(var7.getY(), var8.getY()) + 1.8 + 1.4,
            Math.max(var7.getZ(), var8.getZ()) + 0.81
         );
         if (var0.intersects(var9)) {
            return false;
         }
      }

      return true;
   }

   static boolean putAt_Z_FileStore_PacketHe_Function(AiPathfinder.UiHelper var0, Function<BlockPos, AiPathfinder.Inner_FileCache> var1) {
      if (var0.plannedBreakTicks() < 0.0) {
         return false;
      }

      double var2 = 0.0;

      for (BlockPos var5 : var0.cellsToBreak()) {
         AiPathfinder.Inner_FileCache var6 = (AiPathfinder.Inner_FileCache)var1.apply(var5);
         if (!var6.passable() && var6.diggable()) {
            var2 += var6.breakTicks();
         }
      }

      return var2 - var0.plannedBreakTicks() > 10.0;
   }

   static boolean fill(AiPathfinder.UiHelper var0, Function<BlockPos, AiPathfinder.Inner_FileCache> var1) {
      return putAt_Z_FileStore_PacketHe(var0, var1, false);
   }

   static boolean putAt_Z_FileStore_PacketHe(AiPathfinder.UiHelper var0, Function<BlockPos, AiPathfinder.Inner_FileCache> var1, boolean var2) {
      if (var2 && var0.type() == AiPathfinder.Inner_SyncManager.BRIDGE_CELL) {
         AiPathfinder.Inner_FileCache var3 = (AiPathfinder.Inner_FileCache)var1.apply(var0.to().below());
         AiPathfinder.Inner_FileCache var4 = (AiPathfinder.Inner_FileCache)var1.apply(var0.to());
         AiPathfinder.Inner_FileCache var5 = (AiPathfinder.Inner_FileCache)var1.apply(var0.to().above());
         return var3.loaded()
            && var3.passable()
            && var4.loaded()
            && var4.passable()
            && !var4.lava()
            && !var4.hazardFeet()
            && !var4.feetTrigger()
            && var5.loaded()
            && var5.passable()
            && !var5.lava()
            && !var5.hazardFeet()
            && prevEx(var0, var1);
      } else {
         return value(var0, var1) && prevEx(var0, var1);
      }
   }

   private static boolean value(AiPathfinder.UiHelper var0, Function<BlockPos, AiPathfinder.Inner_FileCache> var1) {
      boolean var10000;
      switch (var0.type()) {
         case WALK:
         case WALK_DIAGONAL:
         case SPRINT:
         case SPRINT_DIAGONAL:
            var10000 = putAt_Z_BlockPos_Function(var0.to(), var1);
            break;
         case STEP_UP_1:
            var10000 = (
                  !((AiPathfinder.Inner_FileCache)var1.apply(var0.to().below())).passable()
                     || ((AiPathfinder.Inner_FileCache)var1.apply(var0.from())).water() && ((AiPathfinder.Inner_FileCache)var1.apply(var0.to())).water()
               )
               && putAt_Z_BlockPos_Function(var0.to(), var1);
            break;
         case SPRINT_JUMP:
            var10000 = AiPathfinder.Inner_RenderState.prevEx(var1, var0.from(), var0.to());
            break;
         case JUMP_UP_1:
            var10000 = !((AiPathfinder.Inner_FileCache)var1.apply(var0.to().below())).passable()
               && putAt_Z_BlockPos_Function(var0.to(), var1)
               && (!((AiPathfinder.Inner_FileCache)var1.apply(var0.from().below())).farmland() || ((AiPathfinder.Inner_FileCache)var1.apply(var0.from())).water());
            break;
         case DESCEND_1:
         case DESCEND_2:
         case DESCEND_3:
            var10000 = putAt_Z_BlockPos_Function(var0.to(), var1)
               && !((AiPathfinder.Inner_FileCache)var1.apply(var0.to().below())).farmland()
               && (
                  AiPathfinder.Inner_RenderState.value(var1, var0.from(), var0.to())
                     || AiPathfinder.Inner_RenderState.putAt_Z_Function(
                        var1,
                        var0.to(),
                        Integer.signum(var0.to().getX() - var0.from().getX()),
                        Integer.signum(var0.to().getZ() - var0.from().getZ()),
                        var0.from().getY() - var0.to().getY()
                     )
               );
            break;
         case CLIMB_1:
            if (var0.to().getY() > var0.from().getY()) {
               AiPathfinder.Inner_FileCache var13 = (AiPathfinder.Inner_FileCache)var1.apply(var0.to());
               var10000 = (var13.passable() || var13.climbable())
                  && !var13.lava()
                  && !var13.hazardFeet()
                  && !var13.feetTrigger()
                  && (
                     ((AiPathfinder.Inner_FileCache)var1.apply(var0.to().above())).passable()
                        || ((AiPathfinder.Inner_FileCache)var1.apply(var0.to().above())).climbable()
                  );
            } else {
               var10000 = ((AiPathfinder.Inner_FileCache)var1.apply(var0.from())).climbable() && ((AiPathfinder.Inner_FileCache)var1.apply(var0.to())).climbable()
                  || putAt_Z_BlockPos_Function(var0.to(), var1);
            }
            break;
         case DESCEND_SOFT:
            AiPathfinder.Inner_FileCache var12 = (AiPathfinder.Inner_FileCache)var1.apply(var0.to());
            var10000 = var12.passable()
               && !var12.lava()
               && !var12.hazardFeet()
               && !var12.feetTrigger()
               && !((AiPathfinder.Inner_FileCache)var1.apply(var0.to().below())).farmland()
               && Math.min(var12.fallMult10(), ((AiPathfinder.Inner_FileCache)var1.apply(var0.to().below())).fallMult10()) <= 2;
            break;
         case DIG_DOWN_1:
            BlockPos var11 = var0.cellsToBreak().get(0);
            AiPathfinder.Inner_FileCache var17 = (AiPathfinder.Inner_FileCache)var1.apply(var11);
            AiPathfinder.Inner_FileCache var21 = (AiPathfinder.Inner_FileCache)var1.apply(var11.below());
            AiPathfinder.Inner_FileCache var23 = (AiPathfinder.Inner_FileCache)var1.apply(var0.from());
            AiPathfinder.Inner_FileCache var25 = (AiPathfinder.Inner_FileCache)var1.apply(var0.from().above());
            var10000 = (var17.passable() || var17.diggable())
               && !var21.passable()
               && !var21.hazardFloor()
               && !var21.lava()
               && !var21.water()
               && !var21.farmland()
               && var21.top16() <= 16
               && (!var23.water() || !var25.water());
            break;
         case DIG_TUNNEL:
         case DIG_ASCEND:
         case DIG_DESCEND:
            AiPathfinder.Inner_FileCache var10 = (AiPathfinder.Inner_FileCache)var1.apply(var0.to().below());
            boolean var16 = !var10.passable() && !var10.hazardFloor() && !var10.lava();

            for (BlockPos var22 : var0.cellsToBreak()) {
               AiPathfinder.Inner_FileCache var24 = (AiPathfinder.Inner_FileCache)var1.apply(var22);
               if (!var24.passable() && !var24.diggable()) {
                  var16 = false;
               }
            }

            var10000 = var16;
            break;
         case PILLAR:
            AiPathfinder.Inner_FileCache var9 = (AiPathfinder.Inner_FileCache)var1.apply(var0.from().below());
            boolean var15 = (!var9.farmland() && var9.top16() >= 16 || var0.repairFarmland() && var9.farmland() && var9.top16() == 15 || var9.soulSand())
               && !var9.door();

            for (int var19 = 2; var19 <= 3; var19++) {
               BlockPos var5 = var0.from().above(var19);
               AiPathfinder.Inner_FileCache var6 = (AiPathfinder.Inner_FileCache)var1.apply(var5);
               if (var6.lava()) {
                  var15 = false;
               } else if (!var6.passable() && (!var0.cellsToBreak().contains(var5) || !var6.diggable()) && var19 == 2) {
                  var15 = false;
               }
            }

            var10000 = var15;
            break;
         case BRIDGE_CELL:
            AiPathfinder.Inner_FileCache var8 = (AiPathfinder.Inner_FileCache)var1.apply(var0.cellsToPlace().get(0));
            var10000 = var8.passable()
               && !var8.water()
               && !var8.lava()
               && ((AiPathfinder.Inner_FileCache)var1.apply(var0.to())).passable()
               && ((AiPathfinder.Inner_FileCache)var1.apply(var0.to().above())).passable()
               && AiPathfinder.Inner_RenderState.flowState(var1, var0.cellsToPlace().get(0)) >= 0;
            break;
         case DOOR:
            AiPathfinder.Inner_FileCache var7 = (AiPathfinder.Inner_FileCache)var1.apply(var0.to());
            AiPathfinder.Inner_FileCache var14 = (AiPathfinder.Inner_FileCache)var1.apply(var0.to().above());
            AiPathfinder.Inner_FileCache var18 = (AiPathfinder.Inner_FileCache)var1.apply(var0.to().below());
            var10000 = (var7.passable() || var7.door())
               && !var7.lava()
               && !var7.hazardFeet()
               && !var7.feetTrigger()
               && (var14.passable() || var14.door())
               && !var14.lava()
               && !var14.hazardFeet()
               && !var18.passable()
               && !var18.hazardFloor()
               && !var18.lava();
            break;
         case JUMP_GAP_2:
         case JUMP_GAP_3:
         case PARKOUR_ASCEND:
            int var2 = Integer.signum(var0.to().getX() - var0.from().getX());
            int var3 = Integer.signum(var0.to().getZ() - var0.from().getZ());
            int var4 = Math.max(Math.abs(var0.to().getX() - var0.from().getX()), Math.abs(var0.to().getZ() - var0.from().getZ())) - 1;
            var10000 = AiPathfinder.Inner_RenderState.putAt_Z_Function(var1, var0.from(), var2, var3, var4, var0.type() == AiPathfinder.Inner_SyncManager.PARKOUR_ASCEND);
            break;
         case JUMP:
            var10000 = (putAt_Z_BlockPos_Function(var0.to(), var1) || AiPathfinder.Inner_RenderState.putAt_Z_Function_2(var1, var0.to(), var0.jump()))
               && !((AiPathfinder.Inner_FileCache)var1.apply(var0.to().below())).farmland()
               && AiPathfinder.Inner_RenderState.fill((AiPathfinder.Inner_FileCache)var1.apply(var0.from()), (AiPathfinder.Inner_FileCache)var1.apply(var0.from().below()));
            break;
         case SLIME_BOUNCE:
            var10000 = var0.jump() != null
               && (
                  ((AiPathfinder.Inner_FileCache)var1.apply(var0.to())).climbable()
                     || putAt_Z_BlockPos_Function(var0.to(), var1) && !((AiPathfinder.Inner_FileCache)var1.apply(var0.to().below())).farmland()
               )
               && AiPathfinder.Inner_RenderState.fill((AiPathfinder.Inner_FileCache)var1.apply(var0.from()), (AiPathfinder.Inner_FileCache)var1.apply(var0.from().below()));
            break;
         case LADDER_JUMP:
            var10000 = var0.jump() != null
               && (
                  ((AiPathfinder.Inner_FileCache)var1.apply(var0.to())).climbable()
                     ? ((AiPathfinder.Inner_FileCache)var1.apply(var0.from())).climbable()
                        || AiPathfinder.Inner_RenderState.fill(
                           (AiPathfinder.Inner_FileCache)var1.apply(var0.from()), (AiPathfinder.Inner_FileCache)var1.apply(var0.from().below())
                        )
                     : ((AiPathfinder.Inner_FileCache)var1.apply(var0.from())).climbable() && putAt_Z_BlockPos_Function(var0.to(), var1)
               );
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static boolean prevEx(AiPathfinder.UiHelper var0, Function<BlockPos, AiPathfinder.Inner_FileCache> var1) {
      boolean var10000;
      switch (var0.type()) {
         case DESCEND_2:
         case DESCEND_3:
         case DESCEND_SOFT:
            for (int var7 = var0.to().getY() - 1; var7 <= var0.from().getY() + 1; var7++) {
               if (((AiPathfinder.Inner_FileCache)var1.apply(new BlockPos(var0.to().getX(), var7, var0.to().getZ()))).cached()) {
                  return false;
               }
            }

            var10000 = true;
            break;
         case CLIMB_1:
         case DOOR:
         default:
            var10000 = true;
            break;
         case DIG_DOWN_1:
            var10000 = putAt_Z_List_Function(var0.cellsToBreak(), var1) && !((AiPathfinder.Inner_FileCache)var1.apply(var0.cellsToBreak().get(0).below())).cached();
            break;
         case DIG_TUNNEL:
         case DIG_ASCEND:
         case DIG_DESCEND:
            var10000 = putAt_Z_List_Function(var0.cellsToBreak(), var1) && !((AiPathfinder.Inner_FileCache)var1.apply(var0.to().below())).cached();
            break;
         case PILLAR:
            var10000 = putAt_Z_List_Function(var0.cellsToPlace(), var1)
               && !((AiPathfinder.Inner_FileCache)var1.apply(var0.from().above(2))).cached()
               && !((AiPathfinder.Inner_FileCache)var1.apply(var0.from().above(3))).cached();
            break;
         case BRIDGE_CELL:
            var10000 = putAt_Z_List_Function(var0.cellsToPlace(), var1) && !((AiPathfinder.Inner_FileCache)var1.apply(var0.to())).cached();
            break;
         case JUMP_GAP_2:
         case JUMP_GAP_3:
         case PARKOUR_ASCEND:
            if (!((AiPathfinder.Inner_FileCache)var1.apply(var0.from())).cached()
               && !((AiPathfinder.Inner_FileCache)var1.apply(var0.to())).cached()
               && !((AiPathfinder.Inner_FileCache)var1.apply(var0.to().below())).cached()) {
               int var2 = Integer.signum(var0.to().getX() - var0.from().getX());
               int var3 = Integer.signum(var0.to().getZ() - var0.from().getZ());
               int var4 = Math.max(Math.abs(var0.to().getX() - var0.from().getX()), Math.abs(var0.to().getZ() - var0.from().getZ())) - 1;
               boolean var5 = true;

               for (int var6 = 1; var6 <= var4; var6++) {
                  if (((AiPathfinder.Inner_FileCache)var1.apply(var0.from().offset(var2 * var6, 0, var3 * var6))).cached()) {
                     var5 = false;
                  }
               }

               var10000 = var5;
            } else {
               var10000 = false;
            }
            break;
         case JUMP:
            var10000 = !((AiPathfinder.Inner_FileCache)var1.apply(var0.from())).cached()
               && !((AiPathfinder.Inner_FileCache)var1.apply(var0.to())).cached()
               && !((AiPathfinder.Inner_FileCache)var1.apply(var0.to().below())).cached();
            break;
         case SLIME_BOUNCE:
         case LADDER_JUMP:
            var10000 = !((AiPathfinder.Inner_FileCache)var1.apply(var0.from())).cached() && !((AiPathfinder.Inner_FileCache)var1.apply(var0.to())).cached();
      }

      return var10000;
   }

   private static boolean putAt_Z_List_Function(List<BlockPos> var0, Function<BlockPos, AiPathfinder.Inner_FileCache> var1) {
      for (BlockPos var3 : var0) {
         if (((AiPathfinder.Inner_FileCache)var1.apply(var3)).cached()) {
            return false;
         }
      }

      return true;
   }

   private static boolean putAt_Z_BlockPos_Function(BlockPos var0, Function<BlockPos, AiPathfinder.Inner_FileCache> var1) {
      AiPathfinder.Inner_FileCache var2 = (AiPathfinder.Inner_FileCache)var1.apply(var0);
      AiPathfinder.Inner_FileCache var3 = (AiPathfinder.Inner_FileCache)var1.apply(var0.above());
      return AiPathfinder.Inner_RenderState.putAt_Z_FileStore_BitCache(var2, var3, (AiPathfinder.Inner_FileCache)var1.apply(var0.below()))
         ? true
         : var2.loaded() && var2.water() && !var2.lava() && !var2.hazardFeet() && var3.loaded() && (var3.water() || var3.passable()) && !var3.lava();
   }

   static int putAt_I_List(List<AiPathfinder.UiHelper> var0, int var1, int var2, Function<BlockPos, AiPathfinder.Inner_FileCache> var3) {
      return putAt_I_List(var0, var1, var2, var3, false);
   }

   static int putAt_I_List(List<AiPathfinder.UiHelper> var0, int var1, int var2, Function<BlockPos, AiPathfinder.Inner_FileCache> var3, boolean var4) {
      int var5 = Math.min(var0.size(), var1 + 1 + var2);
      HashMap var6 = new HashMap();
      Function var7 = var2x -> {
         AiPathfinder.Inner_FileCache var3x = (AiPathfinder.Inner_FileCache)var6.get(((net.minecraft.core.BlockPos)var2x).asLong());
         return var3x != null ? var3x : (AiPathfinder.Inner_FileCache)var3.apply(((BlockPos)var2x));
      };

      for (int var8 = Math.max(0, var1); var8 < var5; var8++) {
         AiPathfinder.UiHelper var9 = (AiPathfinder.UiHelper)var0.get(var8);
         if (var8 > var1 && !putAt_Z_FileStore_PacketHe(var9, var6.isEmpty() ? var3 : var7, var4)) {
            return var8;
         }

         for (BlockPos var11 : var9.cellsToBreak()) {
            var6.put(var11.asLong(), AiPathfinder.Inner_FileCache.air());
         }

         for (BlockPos var13 : var9.cellsToPlace()) {
            var6.put(var13.asLong(), AiPathfinder.Inner_FileCache.fullBlock(1.0 / 0.0));
         }
      }

      return -1;
   }

   public static int putAt(List<AiPathfinder.UiHelper> var0) {
      Minecraft var1 = Minecraft.getInstance();
      if (var1.level == null) {
         return -1;
      }

      Function var2 = var1x -> AiPathfinder.Inner_StreamUtil.putAt_FileStore_BitCache_Minecraft(var1, var1.level, ((BlockPos)var1x), putAt_ItemStackArr);
      return !var0.isEmpty() && !putAt_Z_FileStore_PacketHe((AiPathfinder.UiHelper)var0.getFirst(), var2, true)
         ? 0
         : putAt_I_List(var0, 0, var0.size(), var2, true);
   }

   static int putAt_I_List_2(List<AiPathfinder.UiHelper> var0, int var1, int var2, boolean var3, boolean var4) {
      if (var3) {
         return -1;
      }

      int var5 = var4 ? var1 + 1 : var1;
      int var6 = Math.min(var0.size(), var1 + 1 + var2);

      for (int var7 = Math.max(var5, 0); var7 < var6; var7++) {
         if (putAt(((AiPathfinder.UiHelper)var0.get(var7)).type())) {
            return var7;
         }
      }

      return -1;
   }

   static boolean putAt(AiPathfinder.Inner_SyncManager var0) {
      return var0 == AiPathfinder.Inner_SyncManager.JUMP_GAP_3 || var0 == AiPathfinder.Inner_SyncManager.PARKOUR_ASCEND;
   }

   static boolean fill(AiPathfinder.Inner_SyncManager var0) {
      return var0 == AiPathfinder.Inner_SyncManager.JUMP_GAP_2
         || var0 == AiPathfinder.Inner_SyncManager.JUMP_GAP_3
         || var0 == AiPathfinder.Inner_SyncManager.PARKOUR_ASCEND
         || var0 == AiPathfinder.Inner_SyncManager.JUMP;
   }

   static boolean readEx(AiPathfinder.Inner_SyncManager var0) {
      return var0 == AiPathfinder.Inner_SyncManager.DIG_DOWN_1
         || var0 == AiPathfinder.Inner_SyncManager.DIG_TUNNEL
         || var0 == AiPathfinder.Inner_SyncManager.DIG_ASCEND
         || var0 == AiPathfinder.Inner_SyncManager.DIG_DESCEND
         || var0 == AiPathfinder.Inner_SyncManager.PILLAR;
   }

   static boolean putAt_Z(AiPathfinder.UiHelper var0) {
      if (var0.to().getY() == var0.from().getY() && var0.cellsToBreak().isEmpty() && var0.cellsToPlace().isEmpty()) {
         boolean var10000;
         switch (var0.type()) {
            case WALK:
            case WALK_DIAGONAL:
            case SPRINT:
            case SPRINT_DIAGONAL:
            case SPRINT_JUMP:
               var10000 = true;
               break;
            case STEP_UP_1:
            default:
               var10000 = false;
         }

         return var10000;
      } else {
         return false;
      }
   }

   static boolean putAt_Z_FileStore_SyncPool_Z(AiPathfinder.Inner_SyncManager var0, boolean var1) {
      boolean var10000;
      switch (var0) {
         case SPRINT:
         case SPRINT_DIAGONAL:
         case SPRINT_JUMP:
         case DESCEND_1:
         case JUMP_GAP_2:
         case JUMP:
            var10000 = var1;
            break;
         case STEP_UP_1:
         case JUMP_UP_1:
         case DESCEND_2:
         case DESCEND_3:
         case CLIMB_1:
         case DESCEND_SOFT:
         case DIG_DOWN_1:
         case DIG_TUNNEL:
         case DIG_ASCEND:
         case DIG_DESCEND:
         case PILLAR:
         case BRIDGE_CELL:
         case DOOR:
         default:
            var10000 = false;
            break;
         case JUMP_GAP_3:
         case PARKOUR_ASCEND:
            var10000 = true;
      }

      return var10000;
   }

   static boolean putAt_Z_FileStore_SyncPool(AiPathfinder.Inner_SyncManager var0, boolean var1, boolean var2) {
      if (fill(var0)) {
         return !var1;
      }

      boolean var10000;
      switch (var0) {
         case PILLAR:
         case BRIDGE_CELL:
            var10000 = !var2;
            break;
         default:
            var10000 = true;
      }

      return var10000;
   }

   static boolean putAt_Z_FileStore_SyncPool(AiPathfinder.Inner_SyncManager var0, boolean var1, boolean var2, double var3) {
      return putAt_Z_FileStore_SyncPool(var0, var1, var2, var3, true);
   }

   static boolean putAt_Z_FileStore_SyncPool(AiPathfinder.Inner_SyncManager var0, boolean var1, boolean var2, double var3, boolean var5) {
      if (!var5 && !readEx(var0)) {
         return false;
      }

      boolean var6 = fill(var0) ? !var1 : var2;
      return var6 && var3 > 6.0;
   }

   static boolean putAt_Z_FileStore_PacketHe_BlockPos(AiPathfinder.UiHelper var0, BlockPos var1) {
      int var2 = Integer.signum(var0.to().getX() - var0.from().getX());
      int var3 = Integer.signum(var0.to().getZ() - var0.from().getZ());
      return var1.equals(var0.to()) || var1.equals(var0.to().offset(var2, 0, var3)) || var1.equals(var0.to().offset(2 * var2, 0, 2 * var3));
   }

   static boolean putAt_Z_Z_I(boolean var0, int var1) {
      return var0 && var1 <= 0;
   }

   static boolean putAt(List<AABB> var0, Vec3 var1, AABB var2, int var3, int var4, double var5) {
      for (AABB var8 : var0) {
         AABB var9 = var8.move(var1.x, var1.y, var1.z);
         boolean var10 = var3 > 0
            ? var2.minX > var9.maxX + var5
            : (var3 < 0 ? var2.maxX < var9.minX - var5 : (var4 > 0 ? var2.minZ > var9.maxZ + var5 : var2.maxZ < var9.minZ - var5));
         if (!var10) {
            return false;
         }
      }

      return true;
   }

   static Vec3 putAt_Vec3_List(List<AABB> var0, Vec3 var1, Vec3 var2) {
      AABB var3 = null;
      double var4 = 1.7976931348623157E308;

      for (AABB var7 : var0) {
         double var8 = var2.distanceToSqr(var7.getCenter().add(var1));
         if (var8 < var4) {
            var4 = var8;
            var3 = var7;
         }
      }

      return var3 != null ? var3.getCenter().add(var1) : var1.add(0.5, 0.5, 0.5);
   }

   static boolean putAt_Z_D(double var0, double var2, boolean var4, boolean var5, boolean var6) {
      double var7 = var0 - var2;
      return var7 >= -1.0 && var7 <= 1.35 && (var5 || var6 || var4 && var7 <= 0.6);
   }

   static boolean putAt(int var0, double var1, int var3, boolean var4, int var5, int var6, int var7, int var8, double var9) {
      if (var4 && !(var1 < var9 - 0.001)) {
         int var11 = Integer.signum(var6 - var5);
         int var12 = Integer.signum(var8 - var7);
         if (var11 != 0) {
            return var11 > 0 ? var0 >= var6 : var0 <= var6;
         } else if (var12 != 0) {
            return var12 > 0 ? var3 >= var8 : var3 <= var8;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   static boolean putAt_Z_FileStore_PacketHe(AiPathfinder.UiHelper var0, int var1, int var2, double var3, boolean var5) {
      if (!var5) {
         return false;
      } else {
         double var6 = var3 - var0.to().getY();
         if (!(var6 < -1.0) && !(var6 > 0.6)) {
            int var8 = Integer.signum(var0.to().getX() - var0.from().getX());
            int var9 = Integer.signum(var0.to().getZ() - var0.from().getZ());
            BlockPos var10 = var0.to();
            return var1 == var10.getX() && var2 == var10.getZ() || var1 == var10.getX() + var8 && var2 == var10.getZ() + var9;
         } else {
            return false;
         }
      }
   }

   static boolean putAt_Z_D(double var0, int var2, boolean var3) {
      return var0 >= var2 - (var3 ? 0.0625 : 0.0) - 0.01;
   }

   static Vec3 putAt_Vec3_Vec3_FileStore_PacketHe(Vec3 var0, AiPathfinder.UiHelper var1) {
      int var2 = Integer.signum(var1.to().getX() - var1.from().getX());
      int var3 = Integer.signum(var1.to().getZ() - var1.from().getZ());
      if (var2 != 0 == (var3 != 0)) {
         return var0;
      }

      double var4 = var1.from().getX() + 0.5;
      double var6 = var1.from().getZ() + 0.5;
      double var8 = (var0.x - var4) * var2 + (var0.z - var6) * var3;
      return var8 >= 0.199 ? var0 : new Vec3(var2 != 0 ? var4 + var2 * 0.2 : var0.x, var0.y, var3 != 0 ? var6 + var3 * 0.2 : var0.z);
   }

   static Vec3 putAt_Vec3_FileStore_PacketHe_D(AiPathfinder.UiHelper var0, double var1) {
      if (!var0.cellsToBreak().isEmpty()) {
         return Vec3.atCenterOf(var0.cellsToBreak().get(0));
      } else {
         return !var0.cellsToPlace().isEmpty() ? Vec3.atCenterOf(var0.cellsToPlace().get(0)) : new Vec3(var0.to().getX() + 0.5, var1, var0.to().getZ() + 0.5);
      }
   }

   static Vec3 putAt_Vec3_Vec3(Vec3 var0, float var1, float var2) {
      double var3 = Math.toRadians(var1);
      double var5 = Math.toRadians(var2);
      double var7 = 4.0 * Math.cos(var5);
      return var0.add(-Math.sin(var3) * var7, -Math.sin(var5) * 4.0, Math.cos(var3) * var7);
   }

   static boolean putAt_Z_D(double var0, double var2, boolean var4, boolean var5) {
      return var5 || !var4 && var0 < var2 + 0.05 || var2 - var0 > 0.6;
   }

   static double putAt_D_List_I(List<AiPathfinder.UiHelper> var0, int var1) {
      double var2 = ((AiPathfinder.UiHelper)var0.get(var1)).to().getY();

      for (int var4 = var1 + 1; var4 < Math.min(var0.size(), var1 + 5); var4++) {
         AiPathfinder.UiHelper var5 = (AiPathfinder.UiHelper)var0.get(var4);
         if (var5.type() == AiPathfinder.Inner_SyncManager.JUMP_UP_1 || var5.type() == AiPathfinder.Inner_SyncManager.STEP_UP_1) {
            return Math.max(var2, var5.to().getY());
         }

         if (!putAt_Z(var5) || !var5.cellsToBreak().isEmpty() || !var5.cellsToPlace().isEmpty() || var5.to().getY() != var2) {
            break;
         }
      }

      return var2;
   }

   static boolean readEx(AiPathfinder.UiHelper var0, Function<BlockPos, AiPathfinder.Inner_FileCache> var1) {
      if (var0.to().getY() < var0.from().getY()
         && (
            var0.type() == AiPathfinder.Inner_SyncManager.DESCEND_1
               || var0.type() == AiPathfinder.Inner_SyncManager.DESCEND_2
               || var0.type() == AiPathfinder.Inner_SyncManager.DESCEND_3
               || var0.type() == AiPathfinder.Inner_SyncManager.DIG_DESCEND
         )) {
         BlockPos var2 = var0.to().offset(Integer.signum(var0.to().getX() - var0.from().getX()), 0, Integer.signum(var0.to().getZ() - var0.from().getZ()));
         AiPathfinder.Inner_FileCache var3 = (AiPathfinder.Inner_FileCache)var1.apply(var2);
         return !var3.passable() && var3.top16() >= 16 && ((AiPathfinder.Inner_FileCache)var1.apply(var2.above())).passable();
      } else {
         return false;
      }
   }

   static {
      Arrays.fill(putAt_ItemStackArr, ItemStack.EMPTY);
   }

   static final class Inner_CacheHelper {
      private final ArrayDeque<long[]> putAt = new ArrayDeque<>();

      boolean putAt(AiPathfinder.UiHelper var1, long var2) {
         while (!this.putAt.isEmpty() && var2 - ((long[])this.putAt.peekFirst())[1] > 45000000000L) {
            this.putAt.pollFirst();
         }

         long var4 = AiRoutePolicy.putAt_J(var1);
         var var6 = this.putAt.descendingIterator();

         while (var6.hasNext()) {
            long[] var7 = (long[])var6.next();
            if (var7[0] == var4) {
               if (var2 - var7[1] < 500000000L) {
                  var7[1] = var2;
                  return false;
               }
               break;
            }
         }

         int var9 = 1;

         for (long[] var8 : this.putAt) {
            if (var8[0] == var4) {
               var9++;
            }
         }

         this.putAt.addLast(new long[]{var4, var2});

         while (this.putAt.size() > 256) {
            this.putAt.pollFirst();
         }

         return var9 >= 3;
      }

      void putAt() {
         this.putAt.clear();
      }
   }

   public interface Inner_TableCache {
      Item putAt_Item(BlockPos var1);

      boolean putAt_Z(BlockPos var1);

      default boolean fill(BlockPos var1) {
         return true;
      }
   }
}
