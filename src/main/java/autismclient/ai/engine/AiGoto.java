package autismclient.ai.engine;

import autismclient.AutismClientAddon;
import autismclient.ai.AiJson;
import autismclient.ai.AiOps;
import autismclient.ai.AiVisualization;
import autismclient.ai.AiServerEvidence;
import autismclient.ai.AiPlanks;
import autismclient.api.ai.AiOperation;
import autismclient.modules.ScopedToggleOverride;
import autismclient.modules.Module;
import autismclient.modules.al;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismWaypoints;
import autismclient.util.OverlayScreenController;
import autismclient.util.RotationMath;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AiGoto {
   private static final ItemStack[] putAt_ItemStackArr = new ItemStack[9];
   private static volatile long fill = -9223372036854775808L;
   private static volatile int flowState;
   private static volatile long readEx;
   public static final String putAt_Str = "Player is wedged in solid blocks";
   static final boolean putAt_Z = Boolean.getBoolean("autism.ai.snapshotGrid");
   static final long putAt_J = 30000000000L;
   private static volatile long value;

   private AiGoto() {
   }

   static boolean putAt(AiWrExecutor.Inner_TableCache var0, Vec3 var1) {
      return var0 != AiWrExecutor.Inner_TableCache.fill && var1.horizontalDistanceSqr() <= 4.0E-4;
   }

   private static void putAt_V(BlockPos var0) {
      long var1 = System.nanoTime();
      if (var0.asLong() == fill && var1 - readEx < 30000000000L) {
         flowState++;
      } else {
         fill = var0.asLong();
         flowState = 1;
      }

      readEx = var1;
   }

   public static int flowState(BlockPos var0) {
      return var0.asLong() == fill && System.nanoTime() - readEx < 30000000000L ? flowState : 0;
   }

   static boolean putAt_Z_FileStore_TokenCod(AiPathfinder.Inner_JsonUtil var0, boolean var1, boolean var2, BlockPos var3) {
      return (var1 || var2) && var0.readEx_Z_BlockPos(var3);
   }

   static boolean putAt(AiPathfinder.Inner_PacketUtil var0) {
      return var0.exhausted() && var0.movements().isEmpty();
   }

   static boolean putAt_Z_VoxelShape(VoxelShape var0, VoxelShape var1, VoxelShape var2) {
      if (var0.isEmpty()) {
         return true;
      }

      for (VoxelShape var6 : new VoxelShape[]{var0, var1, var2}) {
         if (!var6.isEmpty() && !Block.isShapeFullBlock(var6)) {
            return true;
         }
      }

      return false;
   }

   static boolean putAt(VoxelShape var0, VoxelShape var1) {
      return var0.isEmpty() && !var1.isEmpty() && var1.max(Axis.Y) > 1.0;
   }

   public static BlockPos putAt_BlockPos(BlockPos var0, int var1) {
      if (putAt_Z(var0)) {
         return var0;
      }

      for (int[] var5 : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
         BlockPos var6 = var0.offset(var5[0], 0, var5[1]);
         if (putAt_Z(var6)) {
            return var6;
         }
      }

      for (int var7 = 1; var7 <= var1; var7++) {
         BlockPos var8 = var0.above(var7);
         BlockPos var9 = var0.below(var7);
         if (putAt_Z(var8)) {
            return var8;
         }

         if (putAt_Z(var9)) {
            return var9;
         }
      }

      return var0;
   }

   public static boolean putAt_Z(BlockPos var0) {
      Minecraft var1 = Minecraft.getInstance();
      return var1.level == null
         ? false
         : AiPathfinder.Inner_RenderState.putAt_Z_FileStore_BitCache(
            AiPathfinder.Inner_StreamUtil.putAt_FileStore_BitCache_Minecraft(var1, var1.level, var0, putAt_ItemStackArr),
            AiPathfinder.Inner_StreamUtil.putAt_FileStore_BitCache_Minecraft(var1, var1.level, var0.above(), putAt_ItemStackArr),
            AiPathfinder.Inner_StreamUtil.putAt_FileStore_BitCache_Minecraft(var1, var1.level, var0.below(), putAt_ItemStackArr)
         );
   }

   static BlockPos putAt(BlockPos var0, Predicate<BlockPos> var1) {
      BlockPos var2 = null;
      double var3 = 1.7976931348623157E308;

      for (int var5 = -2; var5 <= 2; var5++) {
         for (int var6 = -1; var6 <= 2; var6++) {
            for (int var7 = -2; var7 <= 2; var7++) {
               if (var5 != 0 || var6 != 0 || var7 != 0) {
                  BlockPos var8 = var0.offset(var5, var6, var7);
                  if (var1.test(var8)) {
                     double var9 = var8.distSqr(var0);
                     if (var9 < var3) {
                        var3 = var9;
                        var2 = var8;
                     }
                  }
               }
            }
         }
      }

      return var2;
   }

   public static AiGoto.Inner_CachePool putAt_HashEntry_TaskStat_BlockPos(
      BlockPos var0, AiPathfinder.Inner_JsonUtil var1, int var2, AiGoto.Inner_BufferHelper var3
   ) {
      return putAt(var0, 0.6, true, true, true, var2, AiPathfinder.Inner_IoWorker.SAFEST, true, 2.0, var1, var3, true);
   }

   public static void putAt_V_HashEntry_TaskStat(AiGoto.Inner_CachePool var0, Function<Predicate<AABB>, Vec3> var1, Runnable var2) {
      if (!AutismLiteVariant.enabled()) {
         AiWalkingWork.putAt(var0, var1, var2);
      }
   }

   public static AiGoto.Inner_CachePool putAt_HashEntry_TaskStat_BlockPos(BlockPos var0, AiPathfinder.Inner_JsonUtil var1, int var2) {
      return putAt(var0, 0.6, true, true, true, var2, AiPathfinder.Inner_IoWorker.SAFEST, true, 2.0, var1, AiGoto.Inner_BufferHelper.DEFAULT, true);
   }

   static boolean putAt(Entity var0) {
      if (var0 instanceof ZombifiedPiglin var3) {
         return !var3.isAggressive();
      } else if (!(var0 instanceof Enderman var1)) {
         return var0 instanceof Piglin var2 ? !var2.isAggressive() : false;
      } else {
         return !var1.isCreepy() && !var1.isAggressive();
      }
   }

   public static AiOperation putAt(JsonObject var0) {
      return putAt_AiOperation_JsonObject(var0, 0, null);
   }

   public static AiOperation putAt_AiOperation_JsonObject(JsonObject var0, int var1, AiGoto.Inner_TableCache var2) {
      Minecraft var3 = Minecraft.getInstance();
      if (var3.player != null && var3.level != null) {
         BlockPos var4 = putAt(var0, var3);
         double var5 = Math.max(0.6, Math.min(8.0, AiJson.init_D_JsonObject(var0, "radius", 1.5)));
         boolean var7 = !AutismLiteVariant.enabled() && AiJson.init_Z_JsonObject(var0, "parkour", false);
         boolean var8 = !var7 && AiJson.init_Z_JsonObject(var0, "allowDig", true);
         boolean var9 = !var7 && AiJson.init_Z_JsonObject(var0, "allowPillar", true);
         boolean var10 = !var7 && AiJson.init_Z_JsonObject(var0, "allowBridge", true);
         boolean var11 = AiJson.init_Z_JsonObject(var0, "allowInventory", false);
         boolean var12 = AiJson.init_Z_JsonObject(var0, "yOnly", false);
         boolean var13 = AiJson.init_Z_JsonObject(var0, "interactOnArrival", false);
         int var14 = var0.has("timeoutSeconds") ? Math.max(1, Math.min(3600, AiJson.init_I_JsonObject(var0, "timeoutSeconds", 300))) : 0;
         int var15 = Math.max(0, Math.min(60, AiJson.init_I_JsonObject(var0, "digReluctance", 2)));
         AiPathfinder.Inner_IoWorker var16 = AiPathfinder.Inner_IoWorker.putAt(AiJson.init_Str_JsonObject(var0, "strategy", "fastest"));
         AiGoto.Inner_BufferHelper var17 = new AiGoto.Inner_BufferHelper(
            AiJson.init_Z_JsonObject(var0, "sprint", true),
            AiJson.init_Z_JsonObject(var0, "bunnyhop", true),
            Math.max(0, Math.min(3, AiJson.init_I_JsonObject(var0, "maxFall", 3))),
            AiJson.init_Z_JsonObject(var0, "avoidWater", false),
            AiJson.init_Z_JsonObject(var0, "allowClimb", true),
            AiJson.init_Z_JsonObject(var0, "allowDoors", true)
         );
         int var18 = var7 ? AiJson.init_I_JsonObject(var0, "parkourSpeed", -1) : -1;
         if (!var7 || !var0.has("parkourSpeed") || var18 >= 0 && var18 <= 2) {
            AiGoto.Inner_CachePool var19 = putAt(
               var4,
               var5,
               var8,
               var9,
               var10,
               var14,
               var16,
               var11,
               var15,
               var12 ? AiPathfinder.Inner_JsonUtil.putAt_FileStore_TokenCod_I_BlockPos(var4.getY(), var4) : null,
               var17,
               false,
               true,
               var7,
               var7 && (AiJson.init_Z_JsonObject(var0, "assumeNoFallDamage", false) || var2 != null && var2.putAt_Z),
               var1,
               var2,
               var18,
               var7 && AiJson.init_Z_JsonObject(var0, "parkourRecordPace", false)
            );
            return !AutismLiteVariant.enabled() && var13 ? putAt(var19, var4) : var19;
         } else {
            throw new IllegalArgumentException("Parkour speed must be 0, 1 or 2");
         }
      } else {
         throw new IllegalArgumentException("Engine travel requires a connected living player");
      }
   }

   static BlockPos putAt(JsonObject var0, Minecraft var1) {
      BlockPos var2 = var1.player.blockPosition();
      if (var0.has("x") && var0.has("y") && var0.has("z")) {
         return new BlockPos(AiJson.init_I_JsonObject(var0, "x", 0), AiJson.init_I_JsonObject(var0, "y", 0), AiJson.init_I_JsonObject(var0, "z", 0));
      }

      if (var0.has("x") && var0.has("z")) {
         BlockPos var13 = new BlockPos(AiJson.init_I_JsonObject(var0, "x", 0), var2.getY(), AiJson.init_I_JsonObject(var0, "z", 0));
         BlockPos var15 = putAt_BlockPos(var13, 24);
         return var15 != null ? var15 : var13;
      }

      if (var0.has("dx") || var0.has("dy") || var0.has("dz")) {
         return var2.offset(
            AiJson.init_I_JsonObject(var0, "dx", 0), AiJson.init_I_JsonObject(var0, "dy", 0), AiJson.init_I_JsonObject(var0, "dz", 0)
         );
      }

      if (var0.has("direction")) {
         double var12 = AiJson.init_D_JsonObject(var0, "distance", 10.0);
         if (var12 > 0.0 && !(var12 > 512.0)) {
            return var2.offset(putAt_BlockPos_Str(AiJson.init_Str_JsonObject(var0, "direction", ""), var1.player.getYRot(), var12));
         } else {
            throw new IllegalArgumentException("distance must be 1-512 blocks");
         }
      } else if (var0.has("waypoint")) {
         String var11 = AiJson.init_Str_JsonObject(var0, "waypoint", "").trim();
         AutismWaypoints var14 = AutismWaypoints.init_AutismWaypoints();
         String var16 = autismclient.util.AuditWaypointScope.current(var1);
         AutismWaypoints.Waypoint var6 = var14.init_AutismWaypoints_Wa(var16, var11);
         if (var6 != null) {
            return new BlockPos(var6.x(), var6.y(), var6.z());
         }

         ArrayList<String> var7 = new ArrayList<>();

         for (AutismWaypoints.Waypoint var9 : var14.init_List_Str(var16)) {
            var7.add(var9.name());
         }

         throw new IllegalArgumentException("No waypoint named '" + var11 + "' in this world; known: " + var7);
      } else if (var0.has("entity")) {
         Entity var10 = var1.level.getEntity(AiJson.init_I_JsonObject(var0, "entity", -1));
         if (var10 == null) {
            throw new IllegalArgumentException("No loaded entity with id " + AiJson.init_I_JsonObject(var0, "entity", -1));
         } else {
            return var10.blockPosition();
         }
      } else {
         if (!var0.has("player")) {
            throw new IllegalArgumentException("A destination is required: x,y,z (or x,z), dx/dy/dz, direction+distance, waypoint, entity or player");
         }

         String var3 = AiJson.init_Str_JsonObject(var0, "player", "").trim();

         for (AbstractClientPlayer var5 : var1.level.players()) {
            if (var5.getName().getString().equalsIgnoreCase(var3) || var5.getGameProfile().name().equalsIgnoreCase(var3)) {
               return var5.blockPosition();
            }
         }

         throw new IllegalArgumentException("No loaded player named '" + var3 + "'");
      }
   }

   static BlockPos putAt_BlockPos_Str(String var0, float var1, double var2) {
      String var4 = var0.toLowerCase(Locale.ROOT).trim();
      double var5 = -Math.sin(Math.toRadians(var1));
      double var7 = Math.cos(Math.toRadians(var1));
      double var13 = 0.0;
      double var9;
      double var11;
      switch (var4) {
         case "north":
         case "n":
            var9 = 0.0;
            var11 = -1.0;
            break;
         case "south":
         case "s":
            var9 = 0.0;
            var11 = 1.0;
            break;
         case "east":
         case "e":
            var9 = 1.0;
            var11 = 0.0;
            break;
         case "west":
         case "w":
            var9 = -1.0;
            var11 = 0.0;
            break;
         case "forward":
         case "forwards":
         case "ahead":
         case "straight":
            var9 = var5;
            var11 = var7;
            break;
         case "back":
         case "backward":
         case "backwards":
            var9 = -var5;
            var11 = -var7;
            break;
         case "left":
            var9 = var7;
            var11 = -var5;
            break;
         case "right":
            var9 = -var7;
            var11 = var5;
            break;
         case "up":
            var9 = 0.0;
            var11 = 0.0;
            var13 = var2;
            break;
         case "down":
            var9 = 0.0;
            var11 = 0.0;
            var13 = -var2;
            break;
         default:
            throw new IllegalArgumentException("Unknown direction '" + var0 + "' (north|south|east|west|forward|back|left|right|up|down)");
      }

      return new BlockPos((int)Math.round(var9 * var2), (int)Math.round(var13), (int)Math.round(var11 * var2));
   }

   static boolean putAt(Block var0) {
      String var1 = BuiltInRegistries.BLOCK.getKey(var0).toString();
      boolean var10000;
      switch (var1) {
         case "minecraft:chest":
         case "minecraft:trapped_chest":
         case "minecraft:ender_chest":
         case "minecraft:barrel":
         case "minecraft:furnace":
         case "minecraft:blast_furnace":
         case "minecraft:smoker":
         case "minecraft:crafting_table":
         case "minecraft:enchanting_table":
         case "minecraft:brewing_stand":
         case "minecraft:stonecutter":
         case "minecraft:grindstone":
         case "minecraft:loom":
         case "minecraft:cartography_table":
         case "minecraft:smithing_table":
         case "minecraft:anvil":
         case "minecraft:chipped_anvil":
         case "minecraft:damaged_anvil":
         case "minecraft:hopper":
         case "minecraft:dispenser":
         case "minecraft:dropper":
         case "minecraft:beacon":
            var10000 = true;
            break;
         default:
            var10000 = var1.equals("minecraft:shulker_box") || var1.startsWith("minecraft:") && var1.endsWith("_shulker_box");
      }

      return var10000;
   }

   static AiOperation putAt(final AiOperation var0, final BlockPos var1) {
      return new AiOperation() {
         private AiOperation fill;

         @Override
         public AiOperation.Result tick() {
            if (this.fill != null) {
               AiOperation.Result var3 = this.fill.tick();
               return !var3.terminal()
                  ? new AiOperation.Result(AiOperation.Status.RUNNING, "Opening the target block", "{}", List.of())
                  : new AiOperation.Result(var3.status(), "Arrived at target; " + var3.message(), var3.dataJson(), var3.evidence(), var3.failure());
            } else {
               AiOperation.Result var1x = var0.tick();
               if (var1x.status() != AiOperation.Status.SUCCEEDED) {
                  return var1x;
               } else {
                  Minecraft var2 = Minecraft.getInstance();
                  if (var2.level != null && AiGoto.putAt(var2.level.getBlockState(var1).getBlock())) {
                     this.fill = AiOps.init_AiOperation(var1);
                     return new AiOperation.Result(AiOperation.Status.RUNNING, "Arrived at target; opening the block", "{}", List.of());
                  } else {
                     return var1x;
                  }
               }
            }
         }

         @Override
         public void cancel() {
            var0.cancel();
            if (this.fill != null) {
               this.fill.cancel();
            }
         }

         @Override
         public boolean canInterruptNow() {
            return (this.fill == null ? var0 : this.fill).canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return (this.fill == null ? var0 : this.fill).requiresReconciliationAfterEmergency();
         }
      };
   }

   public static AiOperation putAt_AiOperation_BlockPos(BlockPos var0, double var1, boolean var3, boolean var4, boolean var5, int var6) {
      Minecraft var7 = Minecraft.getInstance();
      boolean var8 = var7.level == null || var7.level.dimension() == Level.OVERWORLD;
      return putAt(var0, var1, var3, var4, var5, var6, var8 ? AiPathfinder.Inner_IoWorker.FASTEST : AiPathfinder.Inner_IoWorker.SAFEST);
   }

   public static AiOperation putAt_AiOperation(BlockPos var0, int var1) {
      return putAt_AiOperation_BlockPos(var0, var1, Set.of());
   }

   public static AiOperation putAt_AiOperation_BlockPos(BlockPos var0, int var1, Set<Long> var2) {
      return fill(var0, var1, var2, true);
   }

   public static AiOperation fill(BlockPos var0, int var1) {
      return fill(var0, var1, Set.of());
   }

   public static AiOperation fill(BlockPos var0, int var1, Set<Long> var2) {
      return putAt_AiOperation_BlockPos_2(var0, var1, var2, false);
   }

   public static AiOperation putAt_AiOperation_BlockPos_2(BlockPos var0, int var1, Set<Long> var2, boolean var3) {
      return putAt_AiOperation_BlockPos_3(var0, var1, var2, var3, Set.of());
   }

   public static AiOperation putAt_AiOperation_BlockPos_3(BlockPos var0, int var1, Set<Long> var2, boolean var3, Set<Long> var4) {
      Minecraft var5 = Minecraft.getInstance();
      AiGoto.Inner_StreamUtil var6 = new AiGoto.Inner_StreamUtil(
         var0,
         var5.player.blockInteractionRange() - 0.35,
         var5.player.getEyeHeight(),
         null,
         ConcurrentHashMap.newKeySet(),
         var5.level.getFluidState(var0).is(FluidTags.WATER)
      );
      AiPathfinder.Inner_JsonUtil var7 = putAt(var2.isEmpty() ? var6 : readEx(var6, var2), var4);
      return putAt(var0, 0.6, true, var3, var3, var1, AiPathfinder.Inner_IoWorker.SAFEST, true, 2.0, var3 ? putAt(var7) : var7);
   }

   public static AiOperation putAt_AiOperation_BlockPos(BlockPos var0, double var1, int var3, Set<Long> var4) {
      return putAt_AiOperation_BlockPos(var0, var1, var3, var4, Set.of());
   }

   public static AiOperation putAt_AiOperation_BlockPos(BlockPos var0, double var1, int var3, Set<Long> var4, Set<Long> var5) {
      return putAt(
         var0,
         var1,
         true,
         true,
         true,
         var3,
         AiPathfinder.Inner_IoWorker.SAFEST,
         true,
         2.0,
         putAt(putAt(readEx(AiPathfinder.Inner_JsonUtil.putAt_FileStore_TokenCod_BlockPos_D(var0, var1), var4), var5))
      );
   }

   public static AiPathfinder.Inner_JsonUtil putAt(final AiPathfinder.Inner_JsonUtil var0, Set<Long> var1) {
      if (var1.isEmpty()) {
         return var0;
      }

      final var var2 = Set.copyOf(var1);
      return new AiGoto.Inner_DataUtil() {
         @Override
         public AiPathfinder.Inner_JsonUtil putAt_FileStore_TokenCod() {
            return var0;
         }

         @Override
         public boolean isInGoal(int var1, int var2x, int var3) {
            return !var2.contains(BlockPos.asLong(var1, var2x, var3)) && var0.isInGoal(var1, var2x, var3);
         }

         @Override
         public boolean putAt_Z_I(int var1, int var2x, int var3, AiPathfinder.Inner_StateCache var4) {
            return !var2.contains(BlockPos.asLong(var1, var2x, var3)) && var0.putAt_Z_I(var1, var2x, var3, var4);
         }

         @Override
         public double heuristic(int var1, int var2x, int var3) {
            return var0.heuristic(var1, var2x, var3);
         }

         @Override
         public BlockPos putAt_BlockPos() {
            return var0.putAt_BlockPos();
         }

         @Override
         public boolean outsideSearchBox(BlockPos var1, int var2x, int var3) {
            return var0.outsideSearchBox(var1, var2x, var3);
         }

         @Override
         public boolean putAt_Z_BlockPos(BlockPos var1) {
            return !var2.contains(var1.asLong()) && var0.putAt_Z_BlockPos(var1);
         }

         @Override
         public boolean fill_Z_BlockPos(BlockPos var1) {
            return var0.fill_Z_BlockPos(var1);
         }

         @Override
         public boolean readEx_Z_BlockPos(BlockPos var1) {
            return var0.readEx_Z_BlockPos(var1);
         }

         @Override
         public boolean putAt_Z() {
            return true;
         }

         @Override
         public double putAt_D() {
            return var0.putAt_D();
         }

         @Override
         public Set<AiPathfinder.Inner_SyncManager> putAt_Set() {
            return var0.putAt_Set();
         }

         @Override
         public AiPathfinder.Inner_JsonUtil putAt_FileStore_TokenCod_FileStore_TextStor(AiPathfinder.Inner_StateCache var1) {
            return AiGoto.putAt(var0.putAt_FileStore_TokenCod_FileStore_TextStor(var1), var2);
         }
      };
   }

   static AiPathfinder.Inner_JsonUtil putAt(final AiPathfinder.Inner_JsonUtil var0) {
      return new AiGoto.Inner_DataUtil() {
         @Override
         public AiPathfinder.Inner_JsonUtil putAt_FileStore_TokenCod() {
            return var0;
         }

         @Override
         public boolean isInGoal(int var1, int var2, int var3) {
            return var0.isInGoal(var1, var2, var3);
         }

         @Override
         public boolean putAt_Z_I(int var1, int var2, int var3, AiPathfinder.Inner_StateCache var4) {
            return var0.putAt_Z_I(var1, var2, var3, var4);
         }

         @Override
         public double heuristic(int var1, int var2, int var3) {
            return var0.heuristic(var1, var2, var3);
         }

         @Override
         public BlockPos putAt_BlockPos() {
            return var0.putAt_BlockPos();
         }

         @Override
         public boolean outsideSearchBox(BlockPos var1, int var2, int var3) {
            return var0.outsideSearchBox(var1, var2, var3);
         }

         @Override
         public boolean putAt_Z_BlockPos(BlockPos var1) {
            return var0.putAt_Z_BlockPos(var1);
         }

         @Override
         public boolean fill_Z_BlockPos(BlockPos var1) {
            return var0.fill_Z_BlockPos(var1);
         }

         @Override
         public boolean readEx_Z_BlockPos(BlockPos var1) {
            return var0.readEx_Z_BlockPos(var1);
         }

         @Override
         public boolean putAt_Z() {
            return true;
         }

         @Override
         public double putAt_D() {
            return var0.putAt_D();
         }

         @Override
         public AiPathfinder.Inner_JsonUtil putAt_FileStore_TokenCod_FileStore_TextStor(AiPathfinder.Inner_StateCache var1) {
            return AiGoto.putAt(var0.putAt_FileStore_TokenCod_FileStore_TextStor(var1));
         }

         @Override
         public Set<AiPathfinder.Inner_SyncManager> putAt_Set() {
            var var1 = EnumSet.of(
               AiPathfinder.Inner_SyncManager.JUMP, AiPathfinder.Inner_SyncManager.JUMP_GAP_2, AiPathfinder.Inner_SyncManager.JUMP_GAP_3, AiPathfinder.Inner_SyncManager.PARKOUR_ASCEND
            );
            var1.addAll(var0.putAt_Set());
            return Set.copyOf(var1);
         }
      };
   }

   public static AiOperation putAt(List<BlockPos> var0, int var1) {
      if (var0.isEmpty()) {
         throw new IllegalArgumentException("No water source candidates");
      }

      Minecraft var2 = Minecraft.getInstance();
      AiGoto.Inner_StreamUtil var3 = new AiGoto.Inner_StreamUtil(
         var0, var2.player.blockInteractionRange() - 0.35, var2.player.getEyeHeight(), null, ConcurrentHashMap.newKeySet(), true
      );
      return putAt((BlockPos)var0.getFirst(), 0.6, true, true, true, var1, AiPathfinder.Inner_IoWorker.SAFEST, true, 2.0, var3);
   }

   public static AiOperation fill(BlockPos var0, int var1, Set<Long> var2, boolean var3) {
      return putAt_AiOperation_BlockPos_4(var0, var1, var2, var3, null);
   }

   public static AiOperation putAt_AiOperation_BlockPos_4(BlockPos var0, int var1, Set<Long> var2, boolean var3, Direction var4) {
      return putAt_AiOperation_BlockPos_3(var0, var1, var2, var3, var4, 0.25);
   }

   public static AiOperation putAt_AiOperation_BlockPos_3(BlockPos var0, int var1, Set<Long> var2, boolean var3, Direction var4, double var5) {
      Minecraft var7 = Minecraft.getInstance();
      return putAt(
         var0,
         0.6,
         true,
         var3,
         var3,
         var1,
         AiPathfinder.Inner_IoWorker.FASTEST,
         true,
         2.0,
         readEx(new InteractionGoal(var0, Math.max(1.5, var7.player.blockInteractionRange() - var5), AiBlockInteraction.init(var7).y - var7.player.getY(), var4), var2)
      );
   }

   public static AiOperation readEx(BlockPos var0, int var1, Set<Long> var2, boolean var3) {
      return putAt_AiOperation_BlockPos_2(var0, var1, var2, var3, 0.25);
   }

   public static AiOperation putAt_AiOperation_BlockPos_2(BlockPos var0, int var1, Set<Long> var2, boolean var3, double var4) {
      return putAt_AiOperation_BlockPos_2(var0, var1, var2, var3, var4, false);
   }

   public static AiOperation putAt_AiOperation_BlockPos_2(BlockPos var0, int var1, Set<Long> var2, boolean var3, double var4, boolean var6) {
      Minecraft var7 = Minecraft.getInstance();
      AiPathfinder.Inner_JsonUtil var8 = readEx(
         new InteractionGoal(var0, Math.max(1.5, var7.player.blockInteractionRange() - var4), AiBlockInteraction.init(var7).y - var7.player.getY()).init_q(), var2
      );
      return putAt(var0, 0.6, true, var3, var3, var1, AiPathfinder.Inner_IoWorker.FASTEST, true, 2.0, var6 ? fill(var8) : var8);
   }

   static AiPathfinder.Inner_JsonUtil fill(final AiPathfinder.Inner_JsonUtil var0) {
      return new AiGoto.Inner_DataUtil() {
         @Override
         public AiPathfinder.Inner_JsonUtil putAt_FileStore_TokenCod() {
            return var0;
         }

         @Override
         public boolean isInGoal(int var1, int var2, int var3) {
            return var0.isInGoal(var1, var2, var3);
         }

         @Override
         public boolean putAt_Z_I(int var1, int var2, int var3, AiPathfinder.Inner_StateCache var4) {
            return var0.putAt_Z_I(var1, var2, var3, var4) && !var4.probe(var1, var2, var3).water();
         }

         @Override
         public double heuristic(int var1, int var2, int var3) {
            return var0.heuristic(var1, var2, var3);
         }

         @Override
         public BlockPos putAt_BlockPos() {
            return var0.putAt_BlockPos();
         }

         @Override
         public boolean outsideSearchBox(BlockPos var1, int var2, int var3) {
            return var0.outsideSearchBox(var1, var2, var3);
         }

         @Override
         public boolean putAt_Z_BlockPos(BlockPos var1) {
            return var0.putAt_Z_BlockPos(var1);
         }

         @Override
         public boolean fill_Z_BlockPos(BlockPos var1) {
            return var0.fill_Z_BlockPos(var1);
         }

         @Override
         public boolean readEx_Z_BlockPos(BlockPos var1) {
            return var0.readEx_Z_BlockPos(var1);
         }

         @Override
         public boolean putAt_Z() {
            return true;
         }

         @Override
         public double putAt_D() {
            return var0.putAt_D();
         }

         @Override
         public AiPathfinder.Inner_JsonUtil putAt_FileStore_TokenCod_FileStore_TextStor(AiPathfinder.Inner_StateCache var1) {
            return AiGoto.fill(var0.putAt_FileStore_TokenCod_FileStore_TextStor(var1));
         }

         @Override
         public Set<AiPathfinder.Inner_SyncManager> putAt_Set() {
            return var0.putAt_Set();
         }
      };
   }

   public static AiOperation putAt_AiOperation_BlockPos_2(BlockPos var0, Item var1, int var2) {
      return putAt_AiOperation_BlockPos_3(var0, var1, var2, null);
   }

   public static AiOperation putAt_AiOperation_BlockPos_3(BlockPos var0, Item var1, int var2, AABB var3) {
      return putAt_AiOperation_BlockPos_6(var0, var1, var2, var3, true);
   }

   public static AiOperation putAt_AiOperation_BlockPos_6(BlockPos var0, Item var1, int var2, AABB var3, boolean var4) {
      return putAt_AiOperation_BlockPos_5(var0, var1, var2, var3, var4, Set.of());
   }

   public static AiOperation putAt(BlockPos var0, Item var1, int var2, AABB var3, boolean var4, Set<Long> var5, Set<Long> var6) {
      Minecraft var7 = Minecraft.getInstance();
      AiPathfinder.Inner_JsonUtil var8 = readEx(new PlacementGoal(var0, var1, var7.player.blockInteractionRange() - 0.25, var7.player.getEyeHeight(), var3), var5);
      return putAt(var0, 0.6, false, !var4, !var4, var2, AiPathfinder.Inner_IoWorker.FASTEST, true, 2.0, fill(var8, var6));
   }

   static AiPathfinder.Inner_JsonUtil fill(final AiPathfinder.Inner_JsonUtil var0, final Set<Long> var1) {
      return var1.isEmpty() ? var0 : new AiGoto.Inner_DataUtil() {
         @Override
         public AiPathfinder.Inner_JsonUtil putAt_FileStore_TokenCod() {
            return var0;
         }

         boolean putAt_Z_I(int var1x, int var2, int var3) {
            return var1.contains(BlockPos.asLong(var1x, var2, var3)) || var1.contains(BlockPos.asLong(var1x, var2 + 1, var3));
         }

         @Override
         public boolean isInGoal(int var1x, int var2, int var3) {
            return !this.putAt_Z_I(var1x, var2, var3) && var0.isInGoal(var1x, var2, var3);
         }

         @Override
         public boolean putAt_Z_I(int var1x, int var2, int var3, AiPathfinder.Inner_StateCache var4) {
            return !this.putAt_Z_I(var1x, var2, var3) && var0.putAt_Z_I(var1x, var2, var3, var4);
         }

         @Override
         public double heuristic(int var1x, int var2, int var3) {
            return var0.heuristic(var1x, var2, var3);
         }

         @Override
         public BlockPos putAt_BlockPos() {
            return var0.putAt_BlockPos();
         }

         @Override
         public boolean outsideSearchBox(BlockPos var1x, int var2, int var3) {
            return var0.outsideSearchBox(var1x, var2, var3);
         }

         @Override
         public boolean putAt_Z_BlockPos(BlockPos var1x) {
            return var0.putAt_Z_BlockPos(var1x);
         }

         @Override
         public boolean fill_Z_BlockPos(BlockPos var1x) {
            return var0.fill_Z_BlockPos(var1x);
         }

         @Override
         public boolean readEx_Z_BlockPos(BlockPos var1x) {
            return var0.readEx_Z_BlockPos(var1x);
         }

         @Override
         public boolean putAt_Z() {
            return true;
         }

         @Override
         public double putAt_D() {
            return var0.putAt_D();
         }

         @Override
         public AiPathfinder.Inner_JsonUtil putAt_FileStore_TokenCod_FileStore_TextStor(AiPathfinder.Inner_StateCache var1x) {
            return AiGoto.fill(var0.putAt_FileStore_TokenCod_FileStore_TextStor(var1x), var1);
         }

         @Override
         public Set<AiPathfinder.Inner_SyncManager> putAt_Set() {
            return var0.putAt_Set();
         }

         @Override
         public boolean value(BlockPos var1x) {
            return var0.value(var1x);
         }

         @Override
         public boolean write(BlockPos cell) {
            return this.value(cell);
         }
      };
   }

   public static AiOperation putAt_AiOperation_BlockPos_5(BlockPos var0, Item var1, int var2, AABB var3, boolean var4, Set<Long> var5) {
      Minecraft var6 = Minecraft.getInstance();
      return putAt(
         var0,
         0.6,
         false,
         !var4,
         !var4,
         var2,
         AiPathfinder.Inner_IoWorker.FASTEST,
         true,
         2.0,
         readEx(new PlacementGoal(var0, var1, var6.player.blockInteractionRange() - 0.25, var6.player.getEyeHeight(), var3), var5)
      );
   }

   public static AiOperation fill(BlockPos var0, Item var1, int var2, AABB var3) {
      Minecraft var4 = Minecraft.getInstance();
      return putAt(
         var0,
         0.6,
         true,
         true,
         true,
         var2,
         AiPathfinder.Inner_IoWorker.FASTEST,
         true,
         2.0,
         new PlacementGoal(var0, var1, var4.player.blockInteractionRange() - 0.25, var4.player.getEyeHeight(), var3)
      );
   }

   public static AiOperation putAt_AiOperation_BlockPos_5(BlockPos var0, Item var1, int var2, AABB var3, Set<Long> var4) {
      return putAt_AiOperation_BlockPos_4(var0, var1, var2, var3, var4, null);
   }

   public static AiPathfinder.Inner_JsonUtil readEx(AiPathfinder.Inner_JsonUtil var0, Set<Long> var1) {
      return putAt_FileStore_TokenCod_FileStore_TokenCod(var0, var1, -2147483648);
   }

   static AiPathfinder.Inner_JsonUtil readEx(AiPathfinder.Inner_JsonUtil var0) {
      while (var0 instanceof AiGoto.Inner_DataUtil) {
         AiGoto.Inner_DataUtil var1 = (AiGoto.Inner_DataUtil)var0;
         var0 = var1.putAt_FileStore_TokenCod();
      }

      return var0;
   }

   public static AiPathfinder.Inner_JsonUtil putAt_FileStore_TokenCod_FileStore_TokenCod(
      final AiPathfinder.Inner_JsonUtil var0, final Set<Long> var1, final int var2
   ) {
      return var1.isEmpty() && var2 == -2147483648 ? var0 : new AiGoto.Inner_DataUtil() {
         @Override
         public AiPathfinder.Inner_JsonUtil putAt_FileStore_TokenCod() {
            return var0;
         }

         @Override
         public boolean isInGoal(int var1x, int var2x, int var3) {
            return var0.isInGoal(var1x, var2x, var3);
         }

         @Override
         public boolean putAt_Z_I(int var1x, int var2x, int var3, AiPathfinder.Inner_StateCache var4) {
            return var0.putAt_Z_I(var1x, var2x, var3, var4);
         }

         @Override
         public double heuristic(int var1x, int var2x, int var3) {
            return var0.heuristic(var1x, var2x, var3);
         }

         @Override
         public BlockPos putAt_BlockPos() {
            return var0.putAt_BlockPos();
         }

         @Override
         public boolean outsideSearchBox(BlockPos var1x, int var2x, int var3) {
            return var0.outsideSearchBox(var1x, var2x, var3);
         }

         @Override
         public boolean putAt_Z_BlockPos(BlockPos var1x) {
            return var0.putAt_Z_BlockPos(var1x);
         }

         @Override
         public boolean fill_Z_BlockPos(BlockPos var1x) {
            return var1x.getY() >= var2 && !var1.contains(var1x.asLong()) && var0.fill_Z_BlockPos(var1x);
         }

         @Override
         public boolean readEx_Z_BlockPos(BlockPos var1x) {
            return !var1.contains(var1x.asLong()) && var0.readEx_Z_BlockPos(var1x);
         }

         @Override
         public boolean putAt_Z() {
            return true;
         }

         @Override
         public double putAt_D() {
            return Math.min(var0.putAt_D(), var2 == -2147483648 ? 40.0 : 12.0);
         }

         @Override
         public AiPathfinder.Inner_JsonUtil putAt_FileStore_TokenCod_FileStore_TextStor(AiPathfinder.Inner_StateCache var1x) {
            return AiGoto.putAt_FileStore_TokenCod_FileStore_TokenCod(var0.putAt_FileStore_TokenCod_FileStore_TextStor(var1x), var1, var2);
         }

         @Override
         public Set<AiPathfinder.Inner_SyncManager> putAt_Set() {
            return var0.putAt_Set();
         }
      };
   }

   public static AiOperation putAt_AiOperation_BlockPos_4(BlockPos var0, Item var1, int var2, AABB var3, Set<Long> var4, PlacementGoal.Inner_DataUtil var5) {
      return putAt_AiOperation_List(List.of(new PlacementGoal.Inner_CachePool(var0, var1, var5)), var2, var3, var4);
   }

   public static AiOperation putAt_AiOperation_List(List<PlacementGoal.Inner_CachePool> var0, int var1, AABB var2, Set<Long> var3) {
      return putAt_AiOperation_List(var0, var1, var2, var3, Set.of());
   }

   public static AiOperation putAt_AiOperation_List(List<PlacementGoal.Inner_CachePool> var0, int var1, AABB var2, Set<Long> var3, Set<Long> var4) {
      if (AutismLiteVariant.enabled()) {
         throw new IllegalStateException("Schematic construction is unavailable in Autism Lite");
      } else {
         return new AiSchematicApproach(var0, var1, var2, var3, var4);
      }
   }

   public static AiOperation putAt(BlockPos var0, double var1, boolean var3, boolean var4, boolean var5, int var6, AiPathfinder.Inner_IoWorker var7) {
      return putAt(var0, var1, var3, var4, var5, var6, var7, false);
   }

   public static AiOperation putAt(BlockPos var0, double var1, boolean var3, boolean var4, boolean var5, int var6, AiPathfinder.Inner_IoWorker var7, boolean var8) {
      return putAt(var0, var1, var3, var4, var5, var6, var7, var8, 2.0);
   }

   public static AiOperation putAt(
      BlockPos var0, double var1, boolean var3, boolean var4, boolean var5, int var6, AiPathfinder.Inner_IoWorker var7, boolean var8, double var9
   ) {
      return putAt(var0, var1, var3, var4, var5, var6, var7, var8, var9, null);
   }

   public static AiOperation putAt(
      BlockPos var0,
      double var1,
      boolean var3,
      boolean var4,
      boolean var5,
      int var6,
      AiPathfinder.Inner_IoWorker var7,
      boolean var8,
      double var9,
      AiPathfinder.Inner_JsonUtil var11
   ) {
      return putAt(var0, var1, var3, var4, var5, var6, var7, var8, var9, var11, AiGoto.Inner_BufferHelper.DEFAULT);
   }

   public static AiOperation putAt(
      BlockPos var0,
      double var1,
      boolean var3,
      boolean var4,
      boolean var5,
      int var6,
      AiPathfinder.Inner_IoWorker var7,
      boolean var8,
      double var9,
      AiPathfinder.Inner_JsonUtil var11,
      AiGoto.Inner_BufferHelper var12
   ) {
      return putAt(var0, var1, var3, var4, var5, var6, var7, var8, var9, var11, var12, false);
   }

   public static AiGoto.Inner_CachePool putAt(
      BlockPos var0,
      double var1,
      boolean var3,
      boolean var4,
      boolean var5,
      int var6,
      AiPathfinder.Inner_IoWorker var7,
      boolean var8,
      double var9,
      AiPathfinder.Inner_JsonUtil var11,
      AiGoto.Inner_BufferHelper var12,
      boolean var13
   ) {
      return putAt(var0, var1, var3, var4, var5, var6, var7, var8, var9, var11, var12, var13, false);
   }

   private static AiGoto.Inner_CachePool putAt(
      BlockPos var0,
      double var1,
      boolean var3,
      boolean var4,
      boolean var5,
      int var6,
      AiPathfinder.Inner_IoWorker var7,
      boolean var8,
      double var9,
      AiPathfinder.Inner_JsonUtil var11,
      AiGoto.Inner_BufferHelper var12,
      boolean var13,
      boolean var14
   ) {
      return putAt(var0, var1, var3, var4, var5, var6, var7, var8, var9, var11, var12, var13, var14, false, false, 0, null, -1, false);
   }

   private static AiGoto.Inner_CachePool putAt(
      final BlockPos var0,
      double var1,
      final boolean var3,
      final boolean var4,
      final boolean var5,
      int var6,
      final AiPathfinder.Inner_IoWorker var7,
      final boolean var8,
      final double var9,
      final AiPathfinder.Inner_JsonUtil var11,
      final AiGoto.Inner_BufferHelper var12,
      final boolean var13,
      final boolean var14,
      final boolean var15,
      final boolean var16,
      final int var17,
      final AiGoto.Inner_TableCache var18,
      final int var19,
      final boolean var20
   ) {
      Minecraft var21 = Minecraft.getInstance();
      if (var21.player != null && var21.level != null) {
         final long var22 = var14 && var6 == 0 ? 9223372036854775807L : System.nanoTime() + Math.max(1, Math.min(3600, var6)) * 1000000000L;
         final double var24 = Math.max(0.6, Math.min(8.0, var1));
         final double var26 = var19 < 0 ? 0.0 / 0.0 : 0.1 * (1.0 + 0.2 * var19);
         return new AiGoto.Inner_CachePool() {
            final int flowState = var18 == null ? 1 : var18.putAt_List.size();
            final boolean putAt_Z = var19 >= 0 && var18 != null && this.flowState >= 2;
            final Minecraft putAt_Minecraft = Minecraft.getInstance();
            final Object putAt_Obj = this.putAt_Minecraft.level;
            final BlockPos putAt_BlockPos = this.readEx_BlockPos_5();
            final int flowTemp = this.putAt_Minecraft.player.getInventory().getSelectedSlot();
            final AiPathfinder.Inner_StateCache putAt_FileStore_TextStor = AiGoto.putAt_Z
               ? new AiPathfinder.Inner_StreamUtil(this.putAt_Minecraft, this.putAt_BlockPos, var0, this.fill_ItemStackArr(), AiChunkMemory.init_StateCache())
               : new AiLiveGrid(this.putAt_Minecraft, this.fill_ItemStackArr(), AiChunkMemory.init_StateCache());
            AiPathfinder.Inner_JsonUtil putAt_FileStore_TokenCod = var11 != null
               ? var11.putAt_FileStore_TokenCod_FileStore_TextStor(this.putAt_FileStore_TextStor)
               : AiPathfinder.Inner_JsonUtil.putAt_FileStore_TokenCod_BlockPos(var0, var24, var15);
            boolean fill_Z;
            boolean readEx_Z;
            boolean value_Z;
            boolean prevEx_Z;
            final AiPathfinder.Inner_BufferHelper putAt_FileStore_TaskStat = new AiPathfinder.Inner_BufferHelper(
               this.putAt_FileStore_TextStor, var14, this.putAt_Z && var17 == 0 ? 0.0 / 0.0 : var26, var20
            );
            AiPathfinder.Inner_BufferHelper fill_FileStore_TaskStat;
            boolean prev_Z;
            AiPathfinder.Inner_BufferHelper readEx_FileStore_TaskStat;
            BlockPos fill_BlockPos;
            BlockPos readEx_BlockPos;
            double putAt_D;
            boolean chunkAt_Z;
            int readEx_I;
            BlockPos value_BlockPos;
            BlockPos prevEx_BlockPos;
            double fill_D;
            boolean state_Z;
            AiPathfinder.Inner_NetHelper putAt_FileStore_RingUtil;
            boolean closeOf_Z;
            boolean pollAt_Z = (var19 >= 0 || var20) && var17 + 1 < this.flowState;
            double readEx_D = -1.0;
            final Object fill_Obj = new Object();
            final double value_D = this.putAt_Minecraft.player.position().distanceTo(Vec3.atBottomCenterOf(var0));
            long putAt_J = -9223372036854775808L;
            long fill_J;
            final AiWrExecutor putAt_RingCache = new AiWrExecutor();
            AiPathfinder.Inner_NetHelper fill_FileStore_RingUtil;
            boolean pollOf_Z;
            AiPathfinder.Inner_PacketUtil putAt_FileStore_StreamSt;
            AiParkourFallWarning putAt_BufferCore;
            boolean tick_Z;
            ScopedToggleOverride.Inner_StreamUtil putAt_JsonWorker_TableCa;
            static final int value_I = 60;
            final AiAim putAt_CacheHelper;
            boolean flushOf_Z;
            boolean stateOf_Z;
            int prevEx_I;
            int prev_I;
            int chunkAt_I;
            Vec3 putAt_Vec3;
            boolean store_Z;
            boolean nextBy_Z;
            boolean dataEx_Z;
            final Set<Long> putAt_Set;
            int state_I;
            int closeOf_I;
            int pollAt_I;
            double prevEx_D;
            double prev_D;
            Vec3 fill_Vec3;
            final long readEx_J;
            double chunkAt_D;
            final Set<AiPathfinder.Inner_SyncManager> fill_Set;
            AiPathfinder.Inner_SyncManager putAt_FileStore_SyncPool;
            BlockPos prev_BlockPos;
            BlockPos chunkAt_BlockPos;
            int pollOf_I;
            long value_J;
            long prevEx_J;
            boolean frameOf;
            boolean syncAt;
            AiPathfinder.Inner_JsonUtil fill_FileStore_TokenCod;
            final AiPlacer putAt_FilePool;
            BlockPos state_BlockPos;
            int tick_I;
            int flushOf_I;
            int stateOf_I;
            int store_I;
            static final long prev_J = 20000000L;
            long chunkAt_J;
            ItemStack[] putAt_ItemStackArr;

            {
               this.fill_FileStore_RingUtil = var16
                  ? AiPathfinder.Inner_NetHelper.PARKOUR_NO_DAMAGE
                  : (var15 ? AiPathfinder.Inner_NetHelper.PARKOUR_SAFE : AiPathfinder.Inner_NetHelper.STANDARD);
               if (!AutismLiteVariant.enabled()) {
                  AiWalkingWork.putAt(this, this.putAt_RingCache);
               }

               this.putAt_RingCache.prev(var11 != null && var11.putAt_Z());
               this.putAt_RingCache.putAt_V_Predicate(var1x -> this.putAt_FileStore_TokenCod.putAt_Z_BlockPos(var1x));
               this.putAt_RingCache.putAt_V_Z(var8);
               this.putAt_RingCache.putAt_V_PacketHelper(AiChunkMemory.init_StateCache());
               this.putAt_RingCache.value(var12 == null || var12.bunnyhop());
               this.putAt_RingCache.prevEx_V_Z(var12 == null || var12.sprint());
               this.putAt_RingCache.fill(var20);
               if (var15 && var18 != null && var17 + 1 < this.flowState) {
                  this.putAt_RingCache.readEx(false);
               }

               if (var15 && var19 >= 0 && (!this.putAt_Z || var17 > 0)) {
                  this.putAt_RingCache.putAt_V_D(var26);
               }

               this.putAt_CacheHelper = new AiAim();
               this.putAt_Set = new HashSet<>();
               this.prevEx_D = 1.7976931348623157E308;
               this.readEx_J = System.nanoTime();
               this.chunkAt_D = -1.0;
               this.fill_Set = new HashSet<>();
               this.value_J = System.nanoTime();
               this.putAt_FilePool = new AiPlacer();
               this.chunkAt_J = -1L;
            }

            @Override
            public AiOperation.Result tick() {
               AiFallGuard.init_V();
               if (!this.flushOf_Z && this.putAt_Minecraft.player != null && this.putAt_Minecraft.level != null) {
                  if (this.putAt_Minecraft.level != this.putAt_Obj) {
                     return this.putAt(autismclient.ai.AiFailure.WORLD_CHANGED, "World changed during travel");
                  }

                  if (!AutismLiteVariant.enabled()) {
                     if (this.tick_Z) {
                        return this.putAt(AiOperation.Status.CANCELLED, "Parkour cancelled");
                     }

                     if (this.putAt_FileStore_StreamSt != null) {
                        return this.pollOf_AiOperation_Result_3();
                     }
                  }

                  if (System.nanoTime() > var22) {
                     return this.putAt(AiOperation.Status.TIMED_OUT, "Goto deadline reached");
                  }

                  if (this.putAt_Minecraft.player.getHealth() <= 4.0F && !AiLowHealth.init_Z() && !var15) {
                     return this.putAt(
                        autismclient.ai.AiFailure.LOW_HEALTH,
                        AiLowHealth.init("travel", this.putAt_Minecraft.player.getHealth(), this.putAt_Minecraft.player.getMaxHealth())
                     );
                  }

                  if (this.putAt_Vec3 != null) {
                     return this.chunkAt_AiOperation_Result_5();
                  }

                  if (this.state_BlockPos != null) {
                     AiPlacer.Inner_DataUtil var14x = this.flushOf_I < 60
                        ? this.putAt_FilePool.init_DataPipe_DataUtil()
                        : AiPlacer.Inner_DataUtil.FAILED;
                     this.flushOf_I++;
                     if (var14x == AiPlacer.Inner_DataUtil.WORKING) {
                        return this.fill("Blocking the water at " + this.state_BlockPos.toShortString());
                     }

                     BlockPos var15x = this.putAt_Minecraft.player.blockPosition();
                     boolean var16x = !this.putAt_Minecraft.level.getFluidState(var15x).isEmpty()
                        || !this.putAt_Minecraft.level.getFluidState(var15x.above()).isEmpty();
                     if (var14x == AiPlacer.Inner_DataUtil.PLACED && var16x && this.flushOf_I < 80) {
                        return this.fill("Waiting for the water to drain");
                     }

                     AutismClientAddon.LOG.info(
                           "[AI] travel: water at {} {} ({})",
                           this.state_BlockPos.toShortString(),
                           var14x == AiPlacer.Inner_DataUtil.PLACED ? "blocked" : "not blocked",
                           var14x == AiPlacer.Inner_DataUtil.PLACED ? "replanning" : this.putAt_FilePool.init_Str()
                        );
                     this.state_BlockPos = null;
                     this.putAt_FileStore_TaskStat.putAt(this.putAt_RingCache.putAt_BlockPos());
                     this.putAt_RingCache.closeOf_V();
                     return this.fill("Replanning after blocking the water");
                  } else {
                     if (this.putAt_FileStore_TextStor instanceof AiPathfinder.Inner_StreamUtil var1x) {
                        var1x.putAt_V_BlockPos(this.stateOf_Z ? this.putAt_FileStore_TaskStat.putAt_BlockPos() : this.putAt_Minecraft.player.blockPosition());
                        var1x.putAt_V_J(this.putAt_RingCache.putAt_RingCache_TableCac() == AiWrExecutor.Inner_TableCache.fill ? 750000L : 4000000L);
                     } else {
                        AiLiveGrid.putAt(this.putAt_Minecraft, AiChunkMemory.init_StateCache());
                     }

                     long var13x = System.nanoTime();
                     if (this.readEx_Z
                        || !this.stateOf_Z
                        || var14 && this.putAt_FileStore_TaskStat.putAt_Z() && !this.putAt_RingCache.putAt_Z()
                        || this.prevEx_J != 0L && var13x - this.prevEx_J > 1000000000L) {
                        this.value_J = var13x;
                     }

                     this.prevEx_J = var13x;
                     if (!this.readEx_Z && this.stateOf_Z && System.nanoTime() - this.value_J > 30000000000L) {
                        AutismClientAddon.LOG.info(
                              "[AI] NO PROGRESS: {} for {} s at {} (best h {})",
                              this.putAt_FileStore_TokenCod,
                              30L,
                              this.putAt_RingCache.putAt_BlockPos().toShortString(),
                              String.format(Locale.ROOT, "%.1f", this.prevEx_D)
                           );
                        if (this.putAt_Minecraft.level != null) {
                           AutismClientAddon.LOG.info("[AI] {}", AiTerrainDump.init(this.putAt_Minecraft.level, this.putAt_Minecraft.player.blockPosition(), null, 6, 4));
                        }

                        return this.putAt(autismclient.ai.AiFailure.UNREACHABLE, "No progress toward the target in 30 s");
                     } else if (this.readEx_Z) {
                        if (this.putAt_RingCache.putAt_Z() && !this.putAt_RingCache.pollAt_Z()) {
                           this.putAt_RingCache.fill_RingCache_TableCac();
                           return this.fill("Pausing");
                        } else {
                           this.putAt_RingCache.prev_V();
                           return this.fill("Paused");
                        }
                     } else {
                        if (this.fill_Z && this.stateOf_Z && this.putAt_RingCache.pollAt_Z()) {
                           this.fill_Z = false;
                           this.putAt_FileStore_TaskStat
                              .putAt_V_BlockPos_2(this.putAt_RingCache.putAt_BlockPos(), this.putAt_FileStore_TokenCod, this.putAt_FileStore_CacheHel());
                        }

                        Vec3 var3x = this.putAt_Minecraft.player.position();
                        if (this.fill_Vec3 != null) {
                           this.prev_D = this.prev_D + var3x.distanceTo(this.fill_Vec3);
                        }

                        this.fill_Vec3 = var3x;
                        BlockPos var4x = this.putAt_RingCache.putAt_BlockPos();
                        if (!this.chunkAt_Z_6()
                           && !this.putAt_RingCache.putAt_Z()
                           && !this.putAt_RingCache.state_Z()
                           && this.putAt_FileStore_TokenCod.isInGoal(var4x.getX(), var4x.getY(), var4x.getZ())) {
                           this.putAt_RingCache.prev_V();
                           return this.fill("Waiting for endpoint landing");
                        }

                        double var5x = this.putAt_FileStore_TokenCod.heuristic(var4x.getX(), var4x.getY(), var4x.getZ());
                        if (var5x < this.prevEx_D - 0.5) {
                           this.prevEx_D = var5x;
                           this.value_J = System.nanoTime();
                           this.prevEx_I = 0;
                           this.prev_I = 0;
                           this.fill_Set.clear();
                           this.putAt_FileStore_SyncPool = null;
                           this.prev_BlockPos = null;
                           this.frameOf = false;
                        }

                        if (var18 != null && this.stateOf_Z) {
                           this.readEx_V_13();
                        }

                        if ((var18 == null || this.stateOf_Z) && this.prev_Z_8() && !this.putAt_RingCache.chunkAt_Z()) {
                           return this.putAt_AiOperation_Result_Str_26("Arrived at target");
                        }

                        this.value_Z = false;
                        if (this.fill_J != 0L) {
                           if (System.nanoTime() < this.fill_J) {
                              this.putAt_RingCache.prev_V();
                              return this.fill("Waiting for terrain ahead to load");
                           }

                           this.fill_J = 0L;
                           this.putAt_FileStore_TaskStat.putAt(this.readEx_BlockPos_5());
                        }

                        if (!this.stateOf_Z) {
                           if (!this.closeOf_Z_4()) {
                              return this.fill("Landing before starting the route");
                           }

                           if (!var15
                              && this.closeOf_I < 2
                              && this.putAt_Minecraft.player.onGround()
                              && AiGoto.putAt(
                                 this.putAt_Minecraft
                                    .level
                                    .getBlockState(this.putAt_Minecraft.player.blockPosition().below())
                                    .getCollisionShape(this.putAt_Minecraft.level, this.putAt_Minecraft.player.blockPosition().below()),
                                 this.putAt_Minecraft
                                    .level
                                    .getBlockState(this.putAt_Minecraft.player.blockPosition().below(2))
                                    .getCollisionShape(this.putAt_Minecraft.level, this.putAt_Minecraft.player.blockPosition().below(2))
                              )
                              && this.pollAt_Z_4()) {
                              this.closeOf_I++;
                              return this.fill("Centering on supported footing before planning");
                           }

                           if (var3 && !var4 && this.state_Z_4()) {
                              return this.putAt(autismclient.ai.AiFailure.MISSING_TOOL, "No harvesting tool for the blocks enclosing the current stance");
                           }

                           if (var18 != null) {
                              AiOperation.Result var7x = this.closeOf_AiOperation_Result_4();
                              if (var7x != null) {
                                 return var7x;
                              }
                           } else {
                              this.putAt_FileStore_TaskStat
                                 .putAt_V_BlockPos_2(this.readEx_BlockPos_5(), this.putAt_FileStore_TokenCod, this.putAt_FileStore_CacheHel());
                              if (!this.putAt_FileStore_TaskStat.putAt(20000000L)) {
                                 this.stateOf_Z = true;
                                 return this.state_AiOperation_Result_4();
                              }
                           }

                           this.stateOf_Z = true;
                        }

                        this.putAt_FileStore_TaskStat.putAt(this.putAt_FileStore_CacheHel());
                        this.putAt_FileStore_TaskStat.putAt(this.putAt_RingCache);
                        if (var15) {
                           this.chunkAt_V_4();
                        }

                        if (this.putAt_RingCache.putAt_RingCache_TableCac() == AiWrExecutor.Inner_TableCache.fill) {
                           AiPathfinder.Inner_PacketUtil var17x = this.putAt_FileStore_TaskStat.fill_FileStore_StreamSt();
                           if (var17x != null && !var17x.movements().isEmpty() && this.putAt_RingCache.putAt_Z_List_Z(var17x.movements(), var17x.reachesGoal())
                              )
                            {
                              this.putAt_FileStore_TaskStat.readEx();
                              this.putAt_FileStore_TaskStat.putAt(var17x);
                              this.value_BlockPos = var17x.reachesGoal() ? var17x.destination() : null;
                           }
                        }

                        for (int var18x = 0; var18x < 2; var18x++) {
                           double var8x = this.putAt_RingCache.putAt_D();
                           AiWrExecutor.Inner_TableCache var10 = this.putAt_RingCache.fill_RingCache_TableCac();
                           if (var14 && this.putAt_RingCache.putAt_D() > var8x) {
                              this.value_J = var13x;
                           }

                           if (var10 == AiWrExecutor.Inner_TableCache.fill) {
                              return this.fill(this.putAt_Str_Vec3_2(var3x));
                           }

                           if (var10 == AiWrExecutor.Inner_TableCache.value) {
                              return this.putAt(this.putAt_RingCache.fill_BlockPos(), "Movement stalled");
                           }

                           if (var10 == AiWrExecutor.Inner_TableCache.prevEx) {
                              return this.putAt(AiOperation.Status.CANCELLED, "Goto cancelled");
                           }

                           if (this.putAt_FileStore_TaskStat.putAt_Str() != null) {
                              return this.putAt(autismclient.ai.AiFailure.UNKNOWN, this.putAt_FileStore_TaskStat.putAt_Str());
                           }

                           if (var10 == AiWrExecutor.Inner_TableCache.putAt_RingCache_TableCac) {
                              if (!this.closeOf_Z_4()) {
                                 return this.fill("Landing before accepting the route");
                              }

                              AiPathfinder.Inner_PacketUtil var11x = this.putAt_FileStore_TaskStat.putAt_FileStore_StreamSt();
                              if (var11x == null) {
                                 if (!this.putAt_FileStore_TaskStat.putAt_Z() && !this.putAt_FileStore_TaskStat.fill_Z()) {
                                    this.putAt_FileStore_TaskStat.putAt(this.putAt_RingCache.putAt_BlockPos());
                                 }

                                 if (this.putAt_FileStore_TaskStat.putAt(20000000L)) {
                                    this.putAt_FileStore_TaskStat.putAt(this.putAt_RingCache);
                                    var11x = this.putAt_FileStore_TaskStat.putAt_FileStore_StreamSt();
                                 }

                                 if (var11x == null) {
                                    return this.state_AiOperation_Result_4();
                                 }
                              }

                              if (var14 && !AutismLiteVariant.enabled()) {
                                 AiVisualization.init_V(this.fill_Obj);
                              }

                              if (var11x.movements().isEmpty()) {
                                 if (var11x.awaitingTerrain()) {
                                    return this.putAt_AiOperation_Result_44();
                                 }

                                 if (var11x.reachesGoal() && this.prev_Z_8()) {
                                    return this.putAt_AiOperation_Result_Str_26("Already at target");
                                 }

                                 if (var11x.reachesGoal() && this.pollOf_Z_2()) {
                                    return this.fill("Adjusting the interaction position");
                                 }

                                 if (var11x.wedgedStart()) {
                                    return this.fill_AiOperation_Result_23();
                                 }

                                 if (var11x.confinedStart()) {
                                    return this.readEx_AiOperation_Result_16();
                                 }

                                 if (var11x.hitPending()) {
                                    return this.value_AiOperation_Result_10();
                                 }

                                 if (AiGoto.putAt(var11x)) {
                                    return this.putAt(
                                       autismclient.ai.AiFailure.TIMEOUT,
                                       var14
                                          ? "Route search reached its memory limit before it could prove a route"
                                          : "Route search budget exhausted before a route could be proved"
                                    );
                                 }

                                 return this.prev_AiOperation_Result_7();
                              }

                              BlockPos var12x = this.readEx_BlockPos_5();
                              if (!var11x.movements().get(0).from().equals(var12x)) {
                                 this.putAt_FileStore_TaskStat.putAt(var12x);
                                 if (!this.putAt_FileStore_TaskStat.putAt(20000000L)) {
                                    return this.fill("Updating the route from the live stance");
                                 }

                                 this.putAt_FileStore_TaskStat.putAt(this.putAt_RingCache);
                              } else {
                                 if (!AutismLiteVariant.enabled() && this.pollOf_Z) {
                                    if (!var11x.reachesGoal()) {
                                       return this.putAt(autismclient.ai.AiFailure.UNREACHABLE, "No complete route proved through loaded terrain");
                                    }

                                    this.putAt_FileStore_StreamSt = var11x;
                                    this.putAt_RingCache.prev_V();
                                    return this.pollOf_AiOperation_Result_3();
                                 }

                                 this.putAt_V_FileStore_StreamSt(var11x);
                                 this.putAt_FileStore_TaskStat.putAt(this.putAt_RingCache);
                              }
                           } else {
                              BlockPos var19x = this.putAt_RingCache.putAt_BlockPos();
                              if (this.syncAt
                                 && this.fill_FileStore_TokenCod != null
                                 && this.fill_FileStore_TokenCod.isInGoal(var19x.getX(), var19x.getY(), var19x.getZ())) {
                                 this.syncAt = false;
                                 this.fill_FileStore_TokenCod = null;
                                 this.putAt_FileStore_TaskStat.putAt_V_BlockPos_2(var19x, this.putAt_FileStore_TokenCod, this.putAt_FileStore_CacheHel());
                                 return this.fill("Escape complete; resuming the placement route");
                              }

                              if (this.prev_Z_8() && !this.putAt_RingCache.chunkAt_Z()) {
                                 return this.putAt_AiOperation_Result_Str_26("Arrived at target");
                              }

                              if (this.pollOf_Z_2()) {
                                 return this.fill("Adjusting the interaction position");
                              }

                              if (!this.closeOf_Z_4()) {
                                 this.prevEx_Z = true;
                                 return this.fill("Landing before accepting the next route");
                              }

                              if (this.prevEx_Z) {
                                 this.prevEx_Z = false;
                                 this.putAt_FileStore_TaskStat.putAt(var19x);
                                 if (this.putAt_FileStore_TaskStat.putAt(20000000L)) {
                                    this.putAt_FileStore_TaskStat.putAt(this.putAt_RingCache);
                                 }
                              }

                              AiPathfinder.Inner_PacketUtil var20x = this.putAt_FileStore_TaskStat.readEx();
                              if (var20x == null) {
                                 var20x = this.putAt_FileStore_TaskStat.putAt_FileStore_StreamSt();
                              }

                              if (var20x != null
                                 && var20x.movements().isEmpty()
                                 && var20x.reachesGoal()
                                 && this.prev_Z_8()
                                 && !this.putAt_RingCache.chunkAt_Z()) {
                                 return this.putAt_AiOperation_Result_Str_26("Arrived at target");
                              }

                              if (var20x != null && var20x.movements().isEmpty()) {
                                 if (var20x.awaitingTerrain()) {
                                    return this.putAt_AiOperation_Result_44();
                                 }

                                 if (var20x.wedgedStart()) {
                                    return this.fill_AiOperation_Result_23();
                                 }

                                 if (var20x.confinedStart()) {
                                    return this.readEx_AiOperation_Result_16();
                                 }

                                 if (var20x.hitPending()) {
                                    return this.value_AiOperation_Result_10();
                                 }

                                 if (AiGoto.putAt(var20x)) {
                                    return this.putAt(autismclient.ai.AiFailure.TIMEOUT, "Route search budget exhausted before a route could be proved");
                                 }

                                 return this.prev_AiOperation_Result_7();
                              }

                              if (var20x == null) {
                                 if (!this.putAt_FileStore_TaskStat.fill_Z() && !this.putAt_FileStore_TaskStat.putAt_Z()) {
                                    this.putAt_FileStore_TaskStat.putAt(this.putAt_FileStore_CacheHel());
                                    this.putAt_FileStore_TaskStat.putAt(var19x);
                                    return this.fill("Planning next segment");
                                 }

                                 this.putAt_FileStore_TaskStat.fill_V();
                                 return this.fill("Planning next segment");
                              }

                              if (!var20x.movements().getFirst().from().equals(var19x)) {
                                 this.putAt_FileStore_TaskStat.putAt(var19x);
                                 return this.fill("Updating the next segment from the live stance");
                              }

                              this.putAt_V_FileStore_StreamSt(var20x);
                              this.putAt_FileStore_TaskStat.putAt(this.putAt_RingCache);
                           }
                        }

                        return this.fill(this.putAt_Str_Vec3_2(var3x));
                     }
                  }
               } else {
                  return this.putAt(AiOperation.Status.CANCELLED, "Goto cancelled");
               }
            }

            AiOperation.Result putAt_AiOperation_Result_44() {
               this.putAt_RingCache.prev_V();
               this.fill_J = System.nanoTime() + 250000000L;
               return this.fill("Waiting for terrain ahead to load");
            }

            private double putAt_D_I(int var1) {
               return this.putAt_Z && var1 == 0 ? 0.0 / 0.0 : var26;
            }

            private double fill(int var1) {
               double var2 = this.putAt_D_I(var1);
               return Double.isFinite(var2) && var2 > 0.0 ? var2 : this.putAt_FileStore_TextStor.movementSpeed();
            }

            private AiOperation.Result closeOf_AiOperation_Result_4() {
               BlockPos var1x = this.readEx_BlockPos_5();
               double var2 = this.fill(var17);
               boolean var4x = this.tick_Z_2();
               if (this.readEx_FileStore_TaskStat == null && var18.putAt_FileStore_TaskStat != null) {
                  AiPathfinder.Inner_BufferHelper var5x = var18.putAt_FileStore_TaskStat;
                  var18.putAt_FileStore_TaskStat = null;
                  double var6x = this.fill(var18.flowState);
                  if (var18.flowState >= var17
                     && var18.putAt_FileStore_RingUtil == this.fill_FileStore_RingUtil
                     && var18.fill == var4x
                     && Math.abs(var18.putAt_D - var6x) < 1.0E-6) {
                     this.readEx_FileStore_TaskStat = var5x;
                     this.readEx_I = var18.flowState;
                     this.readEx_BlockPos = var18.putAt_BlockPos;
                     this.putAt_D = var18.putAt_D;
                     this.chunkAt_Z = var18.fill;
                     this.fill_BlockPos = this.readEx_I == var17 ? this.readEx_BlockPos : null;
                  } else {
                     var5x.putAt_V();
                  }
               }

               boolean var17x = this.readEx_FileStore_TaskStat != null && this.readEx_I > var17 && !this.closeOf_Z;
               AiGoto.Inner_TableCache.Inner_CacheHelper var18x = this.readEx_FileStore_TaskStat != null && !var17x
                  ? null
                  : var18.putAt(var17, var1x, this.fill_FileStore_RingUtil, var2, var4x);
               if (var18x == null && (this.readEx_FileStore_TaskStat == null || var17x)) {
                  var18x = var18.fill(var17, var1x, this.fill_FileStore_RingUtil, var2, var4x);
               }

               if (var18x != null && !var18x.segment().reachesGoal() && (var19 < 0 || var18x.segment().movements().isEmpty())) {
                  AiPathfinder.Inner_FileCache var7x = this.putAt_FileStore_TextStor.probe(var0.getX(), var0.getY(), var0.getZ());
                  if (var7x != null && var7x.loaded() && !var7x.cached()) {
                     var18x = null;
                  }
               }

               if (var18x != null) {
                  if (this.pollAt_Z && !this.closeOf_Z && var18x.segment().reachesGoal()) {
                     AiOperation.Result var20x = this.putAt(var18x, var4x);
                     if (var20x != null) {
                        return var20x;
                     }
                  }

                  this.putAt_FileStore_TaskStat.putAt(this.putAt_FileStore_TokenCod);
                  this.putAt_FileStore_TaskStat.putAt(this.putAt_FileStore_CacheHel());
                  this.readEx_D = var18x.planningMs();
                  this.stateOf_Z = true;
                  if (var18x.segment().movements().isEmpty()) {
                     if (var18x.segment().reachesGoal() && this.prev_Z_8()) {
                        this.chunkAt_D = (System.nanoTime() - this.readEx_J) / 1000000.0;
                        return this.putAt_AiOperation_Result_Str_26("Already at checkpoint");
                     }

                     if (var18x.segment().awaitingTerrain()) {
                        return this.putAt_AiOperation_Result_44();
                     }

                     this.putAt_FileStore_TaskStat.putAt_V_BlockPos_2(var1x, this.putAt_FileStore_TokenCod, this.putAt_FileStore_CacheHel());
                     return this.state_AiOperation_Result_4();
                  } else {
                     this.putAt_V_FileStore_StreamSt(var18x.segment());
                     return null;
                  }
               } else {
                  if (var17x) {
                     this.value_V_9();
                  }

                  boolean var19x = this.fill_BlockPos != null
                     && var1x.getY() == this.fill_BlockPos.getY()
                     && Math.abs(var1x.getX() - this.fill_BlockPos.getX()) + Math.abs(var1x.getZ() - this.fill_BlockPos.getZ()) == 1;
                  double var8x = this.fill(this.readEx_FileStore_TaskStat != null ? this.readEx_I : var17);
                  if (this.readEx_FileStore_TaskStat == null
                     || !var1x.equals(this.fill_BlockPos) && !var19x
                     || this.chunkAt_Z != var4x
                     || Math.abs(this.putAt_D - var8x) >= 1.0E-6) {
                     this.value_V_9();
                     this.readEx_FileStore_TaskStat = new AiPathfinder.Inner_BufferHelper(this.putAt_FileStore_TextStor, true, this.putAt_D_I(var17), var20);
                     this.fill_BlockPos = var1x;
                     this.putAt_D = var2;
                     this.chunkAt_Z = var4x;
                     this.readEx_I = var17;
                     this.fill_V_4(var1x);
                  }

                  this.readEx_FileStore_TaskStat.putAt(this.putAt_RingCache);
                  if (this.readEx_FileStore_TaskStat.putAt_Str() != null) {
                     return this.putAt(autismclient.ai.AiFailure.UNKNOWN, this.readEx_FileStore_TaskStat.putAt_Str());
                  }

                  AiPathfinder.Inner_PacketUtil var10 = this.readEx_FileStore_TaskStat.putAt_FileStore_StreamSt();
                  if (var10 == null) {
                     return this.pollAt_AiOperation_Result_4();
                  }

                  boolean var11x = var10.awaitingTerrain() || !var10.reachesGoal() && !var10.movements().isEmpty();
                  if (var10.reachesGoal() || var11x) {
                     AiPathfinder.Inner_ByteReader var21x = this.readEx_FileStore_TaskStat.putAt_FileStore_RingBuff();
                     var18.putAt(
                        this.readEx_I,
                        this.readEx_BlockPos,
                        var10,
                        var21x == null ? 0.0 : var21x.elapsedNanos() / 1000000.0,
                        this.fill_FileStore_RingUtil,
                        this.putAt_D,
                        this.chunkAt_Z
                     );
                     if (!var11x && !this.closeOf_Z && this.readEx_I == var17) {
                        this.value_V_9();
                        return this.closeOf_AiOperation_Result_4();
                     }

                     if (!var11x) {
                        BlockPos var13x = var10.destination() == null ? this.readEx_BlockPos : var10.destination();

                        while (++this.readEx_I < this.flowState) {
                           double var14x = this.fill(this.readEx_I);
                           AiGoto.Inner_TableCache.Inner_CacheHelper var16x = var18.putAt(this.readEx_I, var13x, this.fill_FileStore_RingUtil, var14x, var4x);
                           if (var16x == null || !var16x.segment().reachesGoal()) {
                              if (Math.abs(this.putAt_D - var14x) >= 1.0E-6) {
                                 this.readEx_FileStore_TaskStat.putAt_V();
                                 this.readEx_FileStore_TaskStat = new AiPathfinder.Inner_BufferHelper(
                                    this.putAt_FileStore_TextStor, true, this.putAt_D_I(this.readEx_I), var20
                                 );
                                 this.putAt_D = var14x;
                              }

                              this.fill_V_4(var13x);
                              return this.pollAt_AiOperation_Result_4();
                           }

                           if (var16x.segment().destination() != null) {
                              var13x = var16x.segment().destination();
                           }
                        }
                     }

                     this.value_V_9();
                     if (!this.closeOf_Z) {
                        return this.closeOf_AiOperation_Result_4();
                     }

                     if (var11x) {
                        return this.putAt(autismclient.ai.AiFailure.UNREACHABLE, "Load the remaining checkpoints before approving a route with fall damage");
                     }

                     ArrayList var22x = new ArrayList();

                     for (int var23 = var17; var23 < this.flowState; var23++) {
                        var22x.addAll(var18.putAt_HashEntry_TableCacArr[var23].segment().movements());
                     }

                     this.putAt_FileStore_StreamSt = new AiPathfinder.Inner_PacketUtil(var22x, true, 0, false, false);
                     return this.pollOf_AiOperation_Result_3();
                  } else if (this.readEx_I == var17 && !var1x.equals(this.readEx_BlockPos)) {
                     this.value_V_9();
                     return this.closeOf_AiOperation_Result_4();
                  } else {
                     int var12x = this.readEx_I;
                     this.value_V_9();
                     Arrays.fill(var18.putAt_HashEntry_TableCacArr, var17, this.flowState, null);
                     if (this.fill_FileStore_RingUtil == AiPathfinder.Inner_NetHelper.PARKOUR_SAFE && !var10.exhausted()) {
                        this.fill_FileStore_RingUtil = AiPathfinder.Inner_NetHelper.PARKOUR_NO_DAMAGE;
                        this.closeOf_Z = true;
                        return this.fill("Planning route for a possible fall damage warning");
                     } else {
                        return this.putAt(
                           var10.exhausted() ? autismclient.ai.AiFailure.TIMEOUT : autismclient.ai.AiFailure.UNREACHABLE,
                           "No proved route to checkpoint " + (var12x + 1) + "/" + this.flowState + " at " + var18.putAt_List.get(var12x).toShortString()
                        );
                     }
                  }
               }
            }

            private AiOperation.Result putAt(AiGoto.Inner_TableCache.Inner_CacheHelper var1, boolean var2) {
               BlockPos var3x = var1.segment().destination();
               if (var3x == null) {
                  var3x = var1.from();
               }

               for (int var4x = var17 + 1; var4x < this.flowState; var4x++) {
                  double var5x = this.fill(var4x);
                  AiGoto.Inner_TableCache.Inner_CacheHelper var7x = var18.putAt(var4x, var3x, this.fill_FileStore_RingUtil, var5x, var2);
                  if (var7x != null && var7x.segment().reachesGoal()) {
                     if (var7x.segment().destination() != null) {
                        var3x = var7x.segment().destination();
                     }
                  } else {
                     if (this.readEx_FileStore_TaskStat == null
                        || this.readEx_I != var4x
                        || !var3x.equals(this.readEx_BlockPos)
                        || this.chunkAt_Z != var2
                        || Math.abs(this.putAt_D - var5x) >= 1.0E-6) {
                        this.value_V_9();
                        this.readEx_I = var4x;
                        this.fill_BlockPos = null;
                        this.putAt_D = var5x;
                        this.chunkAt_Z = var2;
                        this.readEx_FileStore_TaskStat = new AiPathfinder.Inner_BufferHelper(this.putAt_FileStore_TextStor, true, this.putAt_D_I(var4x), var20);
                        this.fill_V_4(var3x);
                     }

                     this.readEx_FileStore_TaskStat.putAt(this.putAt_RingCache);
                     if (this.readEx_FileStore_TaskStat.putAt_Str() != null) {
                        break;
                     }

                     AiPathfinder.Inner_PacketUtil var8x = this.readEx_FileStore_TaskStat.putAt_FileStore_StreamSt();
                     if (var8x == null) {
                        return this.pollAt_AiOperation_Result_4();
                     }

                     if (!var8x.reachesGoal() && var8x.movements().isEmpty()) {
                        break;
                     }

                     AiPathfinder.Inner_ByteReader var9x = this.readEx_FileStore_TaskStat.putAt_FileStore_RingBuff();
                     var18.putAt(var4x, var3x, var8x, var9x == null ? 0.0 : var9x.elapsedNanos() / 1000000.0, this.fill_FileStore_RingUtil, var5x, var2);
                     if (!var8x.reachesGoal()) {
                        break;
                     }

                     if (var8x.destination() != null) {
                        var3x = var8x.destination();
                     }
                  }
               }

               this.value_V_9();
               this.pollAt_Z = false;
               return null;
            }

            private void putAt(double var1, boolean var3x) {
               if (this.value_BlockPos != null && var17 + 1 < this.flowState) {
                  boolean var4x = this.putAt_FileStore_RingUtil != null && this.putAt_FileStore_RingUtil != this.fill_FileStore_RingUtil;
                  if (!this.value_BlockPos.equals(this.prevEx_BlockPos) || !(Math.abs(this.fill_D - var1) < 1.0E-6) || this.state_Z != var3x || var4x) {
                     if (this.prevEx_BlockPos != null && AiNavDebug.init_Z()) {
                        AiNavDebug.init(
                           "Refresh checkpoint suffix after leg "
                              + (var17 + 1)
                              + "/"
                              + this.flowState
                              + " at "
                              + this.readEx_BlockPos_5().toShortString()
                              + ": speed "
                              + this.fill_D
                              + " -> "
                              + var1
                              + ", sprint "
                              + this.state_Z
                              + " -> "
                              + var3x
                              + ", end "
                              + this.value_BlockPos.toShortString()
                        );
                     }

                     this.prevEx_BlockPos = this.value_BlockPos;
                     this.fill_D = var1;
                     this.state_Z = var3x;
                     this.putAt_FileStore_RingUtil = this.fill_FileStore_RingUtil;
                     BlockPos var5x = this.prevEx_BlockPos;

                     int var6x;
                     for (var6x = var17 + 1; var6x < this.flowState; var6x++) {
                        AiGoto.Inner_TableCache.Inner_CacheHelper var7x = var18.putAt(var6x, var5x, this.fill_FileStore_RingUtil, var1, var3x);
                        if (var7x == null) {
                           break;
                        }

                        if (!var7x.segment().reachesGoal()) {
                           if (var19 >= 0 && !var7x.segment().movements().isEmpty()) {
                              this.value_V_9();
                              return;
                           }
                           break;
                        }

                        if (var7x.segment().destination() != null) {
                           var5x = var7x.segment().destination();
                        }
                     }

                     if (var4x
                        || this.readEx_FileStore_TaskStat == null
                        || this.readEx_I != var6x
                        || !var5x.equals(this.readEx_BlockPos)
                        || !(Math.abs(this.putAt_D - var1) < 1.0E-6)
                        || this.chunkAt_Z != var3x) {
                        this.value_V_9();
                        if (var6x < this.flowState) {
                           this.readEx_I = var6x;
                           this.readEx_FileStore_TaskStat = new AiPathfinder.Inner_BufferHelper(this.putAt_FileStore_TextStor, true, this.putAt_D_I(var6x), var20);
                           this.fill_BlockPos = null;
                           this.putAt_D = var1;
                           this.chunkAt_Z = var3x;
                           this.fill_V_4(var5x);
                        }
                     }
                  }
               }
            }

            private void readEx_V_13() {
               if (!this.closeOf_Z && var17 + 1 < this.flowState) {
                  double var1x = this.fill(var17 + 1);
                  boolean var3x = this.tick_Z_2();
                  this.putAt(var1x, var3x);
                  if (this.readEx_FileStore_TaskStat != null && this.readEx_I > var17) {
                     this.readEx_FileStore_TaskStat.putAt(this.putAt_RingCache);
                     if (this.readEx_FileStore_TaskStat.putAt_Str() != null) {
                        this.value_V_9();
                     } else {
                        AiPathfinder.Inner_PacketUtil var4x = this.readEx_FileStore_TaskStat.putAt_FileStore_StreamSt();
                        if (var4x != null) {
                           if (var4x.reachesGoal() && this.chunkAt_Z == var3x && !(Math.abs(this.putAt_D - var1x) >= 1.0E-6)) {
                              AiPathfinder.Inner_ByteReader var5x = this.readEx_FileStore_TaskStat.putAt_FileStore_RingBuff();
                              var18.putAt(
                                 this.readEx_I,
                                 this.readEx_BlockPos,
                                 var4x,
                                 var5x == null ? 0.0 : var5x.elapsedNanos() / 1000000.0,
                                 this.fill_FileStore_RingUtil,
                                 this.putAt_D,
                                 this.chunkAt_Z
                              );
                              BlockPos var6x = var4x.destination() == null ? this.readEx_BlockPos : var4x.destination();

                              while (++this.readEx_I < this.flowState) {
                                 AiGoto.Inner_TableCache.Inner_CacheHelper var7x = var18.putAt(
                                    this.readEx_I, var6x, this.fill_FileStore_RingUtil, this.putAt_D, this.chunkAt_Z
                                 );
                                 if (var7x == null || !var7x.segment().reachesGoal()) {
                                    this.fill_V_4(var6x);
                                    return;
                                 }

                                 if (var7x.segment().destination() != null) {
                                    var6x = var7x.segment().destination();
                                 }
                              }

                              this.value_V_9();
                           } else {
                              this.value_V_9();
                           }
                        }
                     }
                  }
               }
            }

            private void fill_V_4(BlockPos var1) {
               this.readEx_BlockPos = var1;
               BlockPos var2 = var18.putAt_List.get(this.readEx_I);
               AiPathfinder.Inner_JsonUtil var3x = this.readEx_I == var17
                  ? this.putAt_FileStore_TokenCod
                  : AiPathfinder.Inner_JsonUtil.putAt_FileStore_TokenCod_BlockPos(var2, this.readEx_I == this.flowState - 1 ? var24 : 0.6, true);
               this.readEx_FileStore_TaskStat.putAt_V_BlockPos_2(var1, var3x, this.putAt_FileStore_CacheHel());
            }

            private boolean tick_Z_2() {
               return var12.sprint()
                  && this.putAt_Minecraft.player.getFoodData().getFoodLevel() > 6
                  && (!AiLowHealth.init_Z() || this.putAt_Minecraft.player.getHealth() > 4.0F);
            }

            private void value_V_9() {
               if (this.readEx_FileStore_TaskStat != null) {
                  this.readEx_FileStore_TaskStat.putAt_V();
                  this.readEx_FileStore_TaskStat = null;
               }

               if (!AutismLiteVariant.enabled()) {
                  AiVisualization.init_V(this.fill_Obj);
               }
            }

            private AiOperation.Result pollAt_AiOperation_Result_4() {
               this.putAt_RingCache.chunkAt_V();
               if (!AutismLiteVariant.enabled()) {
                  AiPathfinder.Inner_EventBus var1x = this.readEx_FileStore_TaskStat.putAt_FileStore_EventBus();
                  if (var1x != null) {
                     AiVisualization.putAt_V_Obj_3(this.fill_Obj, var1x.trail(), 0, () -> null);
                  }

                  int var2 = (int)Math.round(this.putAt_Minecraft.player.position().distanceTo(Vec3.atBottomCenterOf(var18.putAt_List.get(this.readEx_I))));
                  AiVisualization.load(this.fill_Obj, "Parkour", "Planning  ·  " + (this.readEx_I + 1) + "/" + this.flowState, var2, 100 * var17 / this.flowState);
               }

               return this.fill("Planning route to checkpoint " + (this.readEx_I + 1) + "/" + this.flowState);
            }

            AiOperation.Result putAt_AiOperation_Result_Str_26(String var1) {
               if (!this.putAt_RingCache.value_Z()) {
                  return this.fill("Braking before arrival beside lava");
               }

               if (!var13) {
                  if (!AutismLiteVariant.enabled() && var14) {
                     this.putAt_V_Obj(new Object(), var17 + 1 == this.flowState ? "Arrived" : "Reached", 0, 100);
                  }

                  return this.putAt(AiOperation.Status.SUCCEEDED, var1);
               } else {
                  this.value_Z = true;
                  this.putAt_RingCache.pollAt_Z();
                  this.putAt_RingCache.prev_V();
                  return this.fill("At the goal");
               }
            }

            @Override
            public void putAt_V_41() {
               this.putAt_RingCache.readEx_V();
            }

            @Override
            public void putAt_V_FileStore_TokenCod(AiPathfinder.Inner_JsonUtil var1) {
               AiPathfinder.Inner_JsonUtil var2 = var1.putAt_FileStore_TokenCod_FileStore_TextStor(this.putAt_FileStore_TextStor);
               this.putAt_FileStore_TokenCod = var2;
               this.putAt_FileStore_TaskStat.putAt(var2);
               this.prevEx_D = 1.7976931348623157E308;
               this.value_J = System.nanoTime();
               this.prevEx_I = 0;
               this.prev_I = 0;
               this.fill_Set.clear();
               this.putAt_FileStore_SyncPool = null;
               this.prev_BlockPos = null;
               BlockPos var3x = this.putAt_RingCache.value_BlockPos();
               if (var3x == null || !var2.isInGoal(var3x.getX(), var3x.getY(), var3x.getZ())) {
                  if (var3x != null && this.putAt_Minecraft.player != null) {
                     BlockPos var4x = this.putAt_Minecraft.player.blockPosition();
                     if (var2.heuristic(var3x.getX(), var3x.getY(), var3x.getZ()) + 1.0 < var2.heuristic(var4x.getX(), var4x.getY(), var4x.getZ())) {
                        return;
                     }
                  }

                  this.value_Z = false;
                  this.fill_Z = true;
               }
            }

            @Override
            public boolean putAt_Z_30() {
               return this.value_Z;
            }

            @Override
            public void putAt_V_Z_2(boolean var1) {
               this.putAt_RingCache.chunkAt(var1);
            }

            @Override
            public void putAt(boolean var1, Predicate<BlockPos> var2) {
               this.putAt_RingCache.putAt_V_Z_Predicate(var1, var2);
            }

            @Override
            public int putAt_I_43() {
               return this.putAt_RingCache.chunkAt_I();
            }

            @Override
            public int fill_I_18() {
               return this.putAt_RingCache.state_I();
            }

            @Override
            public boolean fill_Z_18() {
               return this.putAt_RingCache.prevEx_Z();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.putAt_RingCache.prev_Z() || this.putAt_FilePool.load_Z();
            }

            @Override
            public BlockPos putAt_BlockPos_23() {
               return this.putAt_RingCache.readEx_BlockPos();
            }

            @Override
            public Set<BlockPos> putAt_Set_9() {
               return this.putAt_RingCache.putAt_Set_I(2);
            }

            @Override
            public Set<BlockPos> fill_Set_4() {
               return this.putAt_RingCache.putAt_Set();
            }

            @Override
            public void putAt_V_Function(Function<BlockPos, Vec3> var1) {
               this.putAt_RingCache.putAt_V_Function(var1);
            }

            @Override
            public void putAt_V_I_6(int var1) {
               this.putAt_RingCache.putAt_V_I(var1);
            }

            @Override
            public void putAt_V_BiPredicate(BiPredicate<BlockState, ItemStack> var1) {
               this.putAt_RingCache.putAt_V_BiPredicate(var1);
               if (this.putAt_FileStore_TextStor instanceof AiPathfinder.Inner_StreamUtil var2) {
                  var2.putAt_V_BiPredicate(var1);
               } else if (this.putAt_FileStore_TextStor instanceof AiLiveGrid var3x) {
                  var3x.putAt_V_BiPredicate_2(var1);
               }
            }

            @Override
            public BlockPos fill_BlockPos_9() {
               return this.chunkAt_BlockPos;
            }

            @Override
            public void putAt_V_BlockPos_6(BlockPos var1) {
               this.putAt_FileStore_TaskStat.putAt_V_BlockPos_BlockPos(var1, this.putAt_RingCache.putAt_BlockPos());
            }

            @Override
            public void fill(boolean var1) {
               if (var1 != this.readEx_Z) {
                  this.readEx_Z = var1;
                  if (var1) {
                     this.putAt_RingCache.pollAt_Z();
                     this.putAt_RingCache.value_V();
                     if (var14 && !AutismLiteVariant.enabled()) {
                        AiVisualization.init_V(this.fill_Obj);
                     }
                  } else {
                     this.fill_Z = true;
                  }

                  this.value_Z = false;
               }
            }

            AiOperation.Result fill_AiOperation_Result_23() {
               if (this.closeOf_I < 2 && this.pollAt_Z_4()) {
                  this.closeOf_I++;
                  this.putAt_RingCache.pollAt_Z();
                  return this.fill("Stepping clear of a partial block before replanning");
               } else {
                  return !var3
                     ? this.putAt(
                        autismclient.ai.AiFailure.UNREACHABLE,
                        "Player is wedged in solid blocks at "
                           + this.putAt_RingCache.putAt_BlockPos().toShortString()
                           + " (the feet or head cell is inside solid terrain); retry with allowDig:true so the route can dig out"
                     )
                     : this.putAt(
                        (BlockPos)null,
                        "No route toward the target in loaded terrain (Player is wedged in solid blocks at "
                           + this.putAt_RingCache.putAt_BlockPos().toShortString()
                           + " and even dig-out moves found no way toward it)"
                     );
               }
            }

            AiOperation.Result readEx_AiOperation_Result_16() {
               if (this.closeOf_I < 2 && this.pollAt_Z_4()) {
                  this.closeOf_I++;
                  this.putAt_RingCache.pollAt_Z();
                  return this.fill("Stepping onto a full floor before replanning");
               } else {
                  return var14 && !var3
                     ? this.prev_AiOperation_Result_7()
                     : this.putAt(
                        (BlockPos)null,
                        "No route toward the target in loaded terrain (player is stuck in a non-solid hazard at "
                           + this.putAt_RingCache.putAt_BlockPos().toShortString()
                           + ": no floor, fire, powder snow, lava or a hazardous floor, and no adjacent cell is standable)"
                     );
               }
            }

            AiOperation.Result value_AiOperation_Result_10() {
               return this.putAt(
                  (BlockPos)null,
                  "No route toward the target in loaded terrain (the terrain snapshot around the player at "
                     + this.putAt_RingCache.putAt_BlockPos().toShortString()
                     + " never settled; chunk copies kept failing or invalidating)"
               );
            }

            AiOperation.Result putAt(BlockPos var1, String var2) {
               if (this.putAt_RingCache.putAt_Str().equals("dig failed: no tool that gets its drops")) {
                  return this.putAt(autismclient.ai.AiFailure.MISSING_TOOL, "No usable harvesting tool for a block on the route");
               }

               if (this.value_Z_11()) {
                  return this.fill(var2 + "; stabilizing before replan");
               }

               if ((var11 instanceof AiGoto.Inner_DataUtil || var11 instanceof PlacementGoal var3x && var3x.load()) && this.closeOf_I < 2 && this.pollAt_Z_4()
                  )
                {
                  this.closeOf_I++;
                  this.putAt_RingCache.pollAt_Z();
                  return this.fill("Leaving the partial construction footing before replanning");
               }

               if (var2.startsWith("No route toward the target")) {
                  AiGoto.putAt_V(this.putAt_RingCache.putAt_BlockPos());
                  if (Boolean.getBoolean("autism.ai.schematics") && AiGoto.flowState(this.putAt_RingCache.putAt_BlockPos()) == 1) {
                     this.prevEx_V_7();
                  }
               }

               AiPathfinder.UiHelper var7x = this.putAt_RingCache.putAt_FileStore_PacketHe();
               AutismClientAddon.LOG.info(
                     "[AI] travel {}: {}{} at {} (replans {}, stalls {}){}",
                     var13 ? "job" : "goto",
                     var2,
                     var7x == null ? "" : " - " + var7x.type() + " " + var7x.from().toShortString() + " -> " + var7x.to().toShortString(),
                     this.putAt_RingCache.putAt_BlockPos().toShortString(),
                     this.prev_I,
                     this.prevEx_I,
                     var7x != null && !this.putAt_RingCache.putAt_Str().isEmpty() ? ": " + this.putAt_RingCache.putAt_Str() : ""
                  );
               if (!var15
                  && !this.frameOf
                  && !AiLowHealth.init_Z()
                  && this.putAt_Minecraft.player.mayBuild()
                  && var2.startsWith("No route toward the target")
                  && this.readEx_Z_15()) {
                  this.frameOf = true;
                  if (var11 instanceof PlacementGoal var8x && var8x.init_AABB() != null && !var8x.load()) {
                     AABB var9x = var8x.init_AABB();
                     BlockPos var10 = AiGoto.putAt_BlockPos(
                        new BlockPos((int)Math.floor(var9x.minX) - 2, this.putAt_Minecraft.player.blockPosition().getY(), (int)Math.floor(var9x.getCenter().z)),
                        12
                     );
                     this.fill_FileStore_TokenCod = AiPathfinder.Inner_JsonUtil.putAt_FileStore_TokenCod_BlockPos_D(var10, 2.0);
                     this.syncAt = true;
                     this.putAt_FileStore_TaskStat
                        .putAt_V_BlockPos_2(this.putAt_RingCache.putAt_BlockPos(), this.fill_FileStore_TokenCod, this.putAt_FileStore_CacheHel().withDig());
                  } else {
                     this.putAt_FileStore_TaskStat.putAt(this.putAt_FileStore_CacheHel().withDig());
                     this.putAt_FileStore_TaskStat.putAt(this.putAt_RingCache.putAt_BlockPos());
                  }

                  this.putAt_RingCache.closeOf_V();
                  return this.fill(var2 + "; sealed pocket: dig-enabled escape replan");
               } else {
                  AiPathfinder.UiHelper var4x = this.putAt_RingCache.putAt_FileStore_PacketHe();
                  if (var14 && var4x != null) {
                     return this.putAt(var4x, var2);
                  }

                  if (this.putAt_RingCache.readEx_Z() && var4x != null) {
                     this.putAt_RingCache.prevEx_V();
                     if (++this.pollOf_I >= 3) {
                        return this.putAt(autismclient.ai.AiFailure.UNREACHABLE, var2 + "; went round in circles " + this.pollOf_I + " times");
                     }

                     this.putAt_FileStore_TaskStat.putAt_V_BlockPos_BlockPos(var4x.to(), this.putAt_RingCache.putAt_BlockPos());
                     this.putAt_FileStore_TaskStat.putAt(this.putAt_FileStore_CacheHel());
                     this.putAt_FileStore_TaskStat.putAt(this.putAt_RingCache.putAt_BlockPos());
                     this.putAt_RingCache.closeOf_V();
                     return this.fill("Going round in circles; replanning without " + var4x.to().toShortString());
                  } else {
                     boolean var5x = var2.contains("past its target") || var2.contains("slid below a flat step");
                     if (var4x != null && !var5x) {
                        AiPathfinder.putAt_V_FileStore_PacketHe_J(var4x, 120000000000L);
                     }

                     if (var4x != null && !var5x) {
                        AiPathfinder.putAt_V(var4x);
                     }

                     if (var4x != null && !var5x) {
                        if (var4x.type() == this.putAt_FileStore_SyncPool && var4x.to().equals(this.prev_BlockPos)) {
                           this.chunkAt_BlockPos = var4x.to();
                           AiPathfinder.fill(var4x, 120000000000L);
                           return this.putAt(autismclient.ai.AiFailure.UNREACHABLE, var2 + "; " + var4x.type() + " stalled again with its type excluded");
                        }

                        this.putAt_FileStore_SyncPool = var4x.type();
                        this.prev_BlockPos = var4x.to();
                        this.fill_Set.addAll(AiPathfinder.putAt(var4x.type()));
                     }

                     this.prevEx_I++;
                     this.prev_I++;
                     AiPathfinder.Inner_HashUtil var6x = AiPathfinder.putAt_FileStore_HashEntr_I_I(this.prevEx_I, this.prev_I);
                     if (var13 && var2.startsWith("No route toward the target") && this.prev_I >= 2) {
                        var6x = AiPathfinder.Inner_HashUtil.FAILED;
                     }

                     if (var6x == AiPathfinder.Inner_HashUtil.FAILED) {
                        return this.putAt(autismclient.ai.AiFailure.UNREACHABLE, var2 + "; no reachable route after repeated replans");
                     }

                     if (var6x == AiPathfinder.Inner_HashUtil.BLACKLIST_AND_REPLAN && var1 != null) {
                        this.putAt_FileStore_TaskStat.putAt_V_BlockPos_BlockPos(var1, this.putAt_RingCache.putAt_BlockPos());
                     }

                     this.putAt_FileStore_TaskStat.putAt(this.putAt_FileStore_CacheHel());
                     this.putAt_FileStore_TaskStat.putAt(this.putAt_RingCache.putAt_BlockPos());
                     this.putAt_RingCache.closeOf_V();
                     return this.fill(var2 + "; replanning (attempt " + this.prev_I + ")");
                  }
               }
            }

            private AiOperation.Result putAt(AiPathfinder.UiHelper var1, String var2) {
               BlockPos var3x = this.putAt_RingCache.putAt_BlockPos();
               String var4x = this.putAt_RingCache.putAt_Str();
               boolean var5x = var4x.contains("past its target") || var4x.contains("slid below") || var4x.contains("below the takeoff");
               boolean var6x = this.putAt_RingCache.readEx_Z();
               if (var6x) {
                  this.putAt_RingCache.prevEx_V();
               }

               this.prev_I++;
               if (++this.prevEx_I < 6 && (!var6x || ++this.pollOf_I < 3)) {
                  if (!var5x || var6x || var1.jump() != null && var4x.contains("below the takeoff")) {
                     this.putAt_FileStore_TaskStat.putAt_V_FileStore_PacketHe_BlockPos(var1, var3x);
                  }

                  this.putAt_FileStore_TaskStat.putAt(this.putAt_FileStore_CacheHel());
                  this.putAt_FileStore_TaskStat.putAt(var3x);
                  this.putAt_RingCache.closeOf_V();
                  return this.fill(var2 + "; replanning from " + var3x.toShortString());
               } else {
                  this.chunkAt_BlockPos = var1.to();
                  return this.putAt(autismclient.ai.AiFailure.UNREACHABLE, var2 + "; repeatedly stalled near " + var3x.toShortString());
               }
            }

            private void prevEx_V_7() {
               BlockPos var1x = this.putAt_RingCache.putAt_BlockPos();
               AiPathfinder.Inner_DataUtil var2 = this.putAt_FileStore_CacheHel();
               ArrayList var3x = new ArrayList();
               int var4x = AiPathfinder.Inner_RenderState.putAt_I_FileStore_TextStor(
                  this.putAt_FileStore_TextStor, var2, this.putAt_FileStore_TaskStat.putAt_Set(), var1x, var3x
               );
               AutismClientAddon.LOG.info(
                     "[AI] no-route detail: {} goal {} flags {} disabled {} blacklist {} floor {}",
                     var1x.toShortString(),
                     this.putAt_FileStore_TokenCod,
                     var4x,
                     var2.disabledTypes(),
                     this.putAt_FileStore_TaskStat.flowState(),
                     this.putAt_FileStore_TextStor.probe(var1x.getX(), var1x.getY() - 1, var1x.getZ())
                  );

               for (Direction var6x : Plane.HORIZONTAL) {
                  BlockPos var7x = var1x.relative(var6x);
                  AutismClientAddon.LOG.info(
                        "[AI] no-route neighbor: {} enter {} banned {} feet {} head {} floor {}",
                        var7x.toShortString(),
                        this.putAt_FileStore_TokenCod.putAt_Z_BlockPos(var7x),
                        this.putAt_FileStore_TaskStat.putAt_Set().contains(var7x.asLong()),
                        this.putAt_FileStore_TextStor.probe(var7x.getX(), var7x.getY(), var7x.getZ()),
                        this.putAt_FileStore_TextStor.probe(var7x.getX(), var7x.getY() + 1, var7x.getZ()),
                        this.putAt_FileStore_TextStor.probe(var7x.getX(), var7x.getY() - 1, var7x.getZ())
                     );
               }

               for (int var10 = 0; var10 < Math.min(16, var3x.size()); var10++) {
                  AiPathfinder.UiHelper var12x = (AiPathfinder.UiHelper)var3x.get(var10);
                  boolean var14x = this.putAt_FileStore_TokenCod.putAt_Z_BlockPos(var12x.to()) && !AiPathfinder.putAt_Z(var12x);

                  for (BlockPos var9x : var12x.cellsToBreak()) {
                     var14x &= this.putAt_FileStore_TokenCod.fill_Z_BlockPos(var9x);
                  }

                  for (BlockPos var16x : var12x.cellsToPlace()) {
                     var14x &= this.putAt_FileStore_TokenCod.readEx_Z_BlockPos(var16x);
                  }

                  AutismClientAddon.LOG.info(
                        "[AI] no-route move: {} to {} allowed {} globalBan {}", var12x.type(), var12x.to().toShortString(), var14x, AiPathfinder.putAt_Z(var12x)
                     );
               }

               AiPathfinder.Inner_TextFormat var11x = new AiPathfinder.Inner_TextFormat(
                  this.putAt_FileStore_TextStor, var1x, this.putAt_FileStore_TokenCod, var2, this.putAt_FileStore_TaskStat.putAt_Set()
               );
               if (!var11x.putAt_Z_I(512)) {
                  var11x.putAt_V();
               }

               AiPathfinder.Inner_PacketUtil var13x = var11x.putAt_FileStore_StreamSt();
               AutismClientAddon.LOG.info(
                     "[AI] no-route live check: reaches {} moves {} expanded {} exhausted {} pending {}",
                     var13x.reachesGoal(),
                     var13x.movements().size(),
                     var13x.expanded(),
                     var13x.exhausted(),
                     var13x.hitPending()
                  );
            }

            AiOperation.Result prevEx_AiOperation_Result_7() {
               if ((var4 || var5)
                  && this.tick_I < 4
                  && this.putAt_Minecraft.player.mayBuild()
                  && (this.putAt_Minecraft.player.onGround() || this.putAt_Minecraft.player.isInWater())) {
                  BlockPos var1x = this.putAt_Minecraft.player.blockPosition();
                  BlockPos var2 = null;

                  for (BlockPos var4x : List.of(var1x, var1x.above())) {
                     for (Direction var8x : Direction.values()) {
                        if (var8x != Direction.DOWN) {
                           BlockPos var9x = var4x.relative(var8x);
                           if (!var9x.equals(var1x)
                              && !var9x.equals(var1x.above())
                              && var2 == null
                              && this.putAt_Minecraft.level.getFluidState(var9x).is(FluidTags.WATER)
                              && this.putAt_Minecraft.level.getBlockState(var9x).canBeReplaced()) {
                              if (!AiGoto.putAt_Z_FileStore_TokenCod(this.putAt_FileStore_TokenCod, var4, var5, var9x)) {
                                 this.stateOf_I++;
                              } else {
                                 var2 = var9x.immutable();
                              }
                           }
                        }
                     }
                  }

                  if (var2 == null) {
                     return null;
                  }

                  Inventory var10 = this.putAt_Minecraft.player.getInventory();
                  Item var11x = null;
                  int var12x = 2147483647;

                  for (int var13x = 0; var13x < 36; var13x++) {
                     ItemStack var14x = var10.getItem(var13x);
                     if (var14x.getItem() instanceof BlockItem var15x && !(var15x.getBlock() instanceof FallingBlock) && AiRestock.init(var14x, var10)) {
                        int var17x = AiRestock.init_I(BuiltInRegistries.ITEM.getKey(var14x.getItem()).toString());
                        if (var17x >= 0 && var17x < var12x) {
                           var12x = var17x;
                           var11x = var14x.getItem();
                        }
                     }
                  }

                  if (var11x == null) {
                     return null;
                  }

                  this.tick_I++;
                  this.state_BlockPos = var2;
                  this.flushOf_I = 0;
                  this.putAt_RingCache.pollAt_Z();
                  this.putAt_FilePool.init_V_BlockPos_Item(var2, var11x);
                  AutismClientAddon.LOG.info("[AI] travel: no way out of {} beside water; blocking the water at {}", var1x.toShortString(), var2.toShortString());
                  return this.fill("Blocking the water at " + var2.toShortString());
               } else {
                  return null;
               }
            }

            boolean readEx_Z_15() {
               BlockPos var1x = this.putAt_Minecraft.player.blockPosition();
               AABB var2 = var11 instanceof PlacementGoal var3x ? var3x.init_AABB() : null;
               ArrayDeque<BlockPos> var12x = new ArrayDeque<>();
               HashSet<Long> var4x = new HashSet<>();
               var12x.add(var1x);
               var4x.add(var1x.asLong());

               while (!var12x.isEmpty() && var4x.size() < 2000) {
                  BlockPos var5x = (BlockPos)var12x.poll();
                  if (var2 != null) {
                     if (!var2.contains(var5x.getX() + 0.5, var5x.getY() + 0.5, var5x.getZ() + 0.5)) {
                        return false;
                     }
                  } else if (Math.max(Math.abs(var5x.getX() - var1x.getX()), Math.abs(var5x.getZ() - var1x.getZ())) >= 6) {
                     return false;
                  }

                  for (int[] var9x : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                     for (int var10 = -3; var10 <= 1; var10++) {
                        BlockPos var11x = var5x.offset(var9x[0], var10, var9x[1]);
                        if (var4x.add(var11x.asLong()) && this.putAt_Z_BlockPos_18(var11x)) {
                           var12x.add(var11x);
                        }
                     }
                  }
               }

               return true;
            }

            boolean putAt(BlockPos var1, int var2) {
               for (BlockPos var4x : BlockPos.betweenClosed(var1.offset(-var2, -2, -var2), var1.offset(var2, 0, var2))) {
                  if (this.putAt_Minecraft.level.getFluidState(var4x).is(FluidTags.LAVA)) {
                     return true;
                  }
               }

               return false;
            }

            boolean putAt_Z_BlockPos_18(BlockPos var1) {
               return this.putAt_Minecraft.level.getBlockState(var1).getCollisionShape(this.putAt_Minecraft.level, var1).isEmpty()
                  && this.putAt_Minecraft.level.getBlockState(var1.above()).getCollisionShape(this.putAt_Minecraft.level, var1.above()).isEmpty()
                  && !this.putAt_Minecraft.level.getBlockState(var1.below()).getCollisionShape(this.putAt_Minecraft.level, var1.below()).isEmpty();
            }

            boolean value_Z_11() {
               LocalPlayer var1x = this.putAt_Minecraft.player;
               if (var1x == null) {
                  return false;
               }

               boolean var2 = var1x.isInWater()
                  && var1x.getAirSupply() < var1x.getMaxAirSupply()
                  && this.putAt_Minecraft.level.getFluidState(var1x.blockPosition().above()).is(FluidTags.WATER);
               boolean var3x = var1x.isInLava();
               if ((var2 || var3x) && ++this.chunkAt_I <= 60) {
                  this.putAt_Minecraft.options.keyJump.setDown(true);
                  this.fill_V_19();
                  return true;
               }

               this.chunkAt_I = 0;
               this.putAt_Minecraft.options.keyJump.setDown(false);
               this.putAt_Minecraft.options.keyUp.setDown(false);
               this.putAt_Minecraft.options.keyDown.setDown(false);
               this.putAt_Minecraft.options.keyLeft.setDown(false);
               this.putAt_Minecraft.options.keyRight.setDown(false);
               if (!var3x && !var1x.onGround() && !var1x.isInWater() && !var1x.onClimbable() && var1x.getDeltaMovement().y < -0.08) {
                  return true;
               }

               if (var1x.onGround() && !var1x.isInWater() && var1x.getDeltaMovement().horizontalDistanceSqr() > 9.0E-4 && ++this.store_I <= 12) {
                  this.putAt_Minecraft.options.keyShift.setDown(true);
                  return true;
               }

               if (this.store_I > 0) {
                  this.store_I = 0;
                  this.putAt_Minecraft.options.keyShift.setDown(false);
               }

               return false;
            }

            void fill_V_19() {
               this.putAt_Minecraft.options.keyUp.setDown(false);
               this.putAt_Minecraft.options.keyDown.setDown(false);
               this.putAt_Minecraft.options.keyLeft.setDown(false);
               this.putAt_Minecraft.options.keyRight.setDown(false);
               BlockPos var1x = AiGoto.putAt(
                  this.putAt_Minecraft.player.blockPosition(),
                  var1xx -> this.putAt_Minecraft.level.getBlockState(var1xx).getCollisionShape(this.putAt_Minecraft.level, var1xx).isEmpty()
                     && this.putAt_Minecraft.level.getFluidState(var1xx).isEmpty()
               );
               if (var1x != null) {
                  LocalPlayer var2 = this.putAt_Minecraft.player;
                  double var3x = var1x.getX() + 0.5 - var2.getX();
                  double var5x = var1x.getZ() + 0.5 - var2.getZ();
                  if (var3x * var3x + var5x * var5x >= 1.5625) {
                     this.putAt_CacheHelper.init_V_Vec3(Vec3.atCenterOf(var1x));
                     this.putAt_CacheHelper.load_V();
                     this.putAt_Minecraft.options.keyUp.setDown(true);
                  } else {
                     double var7x = Math.toRadians(var2.getYRot());
                     double var9x = var3x * -Math.sin(var7x) + var5x * Math.cos(var7x);
                     double var11x = var3x * -Math.cos(var7x) + var5x * -Math.sin(var7x);
                     this.putAt_Minecraft.options.keyUp.setDown(var9x > 0.15);
                     this.putAt_Minecraft.options.keyDown.setDown(var9x < -0.15);
                     this.putAt_Minecraft.options.keyRight.setDown(var11x > 0.15);
                     this.putAt_Minecraft.options.keyLeft.setDown(var11x < -0.15);
                  }
               }
            }

            private void putAt_V_FileStore_StreamSt(AiPathfinder.Inner_PacketUtil var1) {
               if (this.chunkAt_D < 0.0) {
                  this.chunkAt_D = (System.nanoTime() - this.readEx_J) / 1000000.0;
               }

               this.state_V_3();
               this.prev_Z = var15 && !var1.reachesGoal();
               this.putAt_RingCache.putAt_V_FileStore_RingUtil(this.fill_FileStore_RingUtil);
               this.putAt_RingCache.putAt_V_List_Z(var1.movements(), var1.reachesGoal());
               this.putAt_FileStore_TaskStat.putAt(var1);
               if (var18 != null) {
                  this.value_BlockPos = var1.reachesGoal() ? var1.destination() : null;
                  if (!this.closeOf_Z && var17 + 1 < this.flowState) {
                     this.putAt(this.fill(var17 + 1), this.tick_Z_2());
                  }

                  this.prev_V_6();
               }
            }

            private void prev_V_6() {
               if (var20 && var18 != null && var17 + 1 < this.flowState && this.value_BlockPos != null) {
                  AiGoto.Inner_TableCache.Inner_CacheHelper var1x = var18.putAt(
                     var17 + 1, this.value_BlockPos, this.fill_FileStore_RingUtil, this.fill(var17 + 1), this.tick_Z_2()
                  );
                  this.putAt_RingCache
                     .putAt_V_FileStore_PacketHe(var1x != null && !var1x.segment().movements().isEmpty() ? var1x.segment().movements().getFirst() : null);
               }
            }

            private void chunkAt_V_4() {
               if (!this.putAt_RingCache.putAt_Z()) {
                  this.state_V_3();
               } else {
                  if (this.prev_Z) {
                     AiPathfinder.Inner_FileCache var1x = this.putAt_FileStore_TextStor.probe(var0.getX(), var0.getY(), var0.getZ());
                     if (var1x == null || !var1x.loaded() || var1x.cached()) {
                        return;
                     }

                     this.prev_Z = false;
                     List var2 = this.putAt_RingCache.putAt_List();
                     if (var2.size() < 2) {
                        return;
                     }

                     BlockPos var3x = ((AiPathfinder.UiHelper)var2.getFirst()).to();
                     this.fill_FileStore_TaskStat = new AiPathfinder.Inner_BufferHelper(this.putAt_FileStore_TextStor, true, this.putAt_D_I(var17), var20);
                     this.fill_FileStore_TaskStat.putAt_V_BlockPos_2(var3x, this.putAt_FileStore_TokenCod, this.putAt_FileStore_CacheHel());
                     AiNavDebug.init("Finish loaded while moving; proving a route from " + var3x.toShortString());
                  }

                  if (this.fill_FileStore_TaskStat != null) {
                     this.fill_FileStore_TaskStat.putAt(this.putAt_RingCache);
                     AiPathfinder.Inner_PacketUtil var4x = this.fill_FileStore_TaskStat.putAt_FileStore_StreamSt();
                     if (var4x != null || this.fill_FileStore_TaskStat.putAt_Str() != null) {
                        this.state_V_3();
                        if (var4x != null && var4x.reachesGoal() && this.putAt_RingCache.putAt_Z_List(var4x.movements())) {
                           this.putAt_FileStore_TaskStat.fill(var4x);
                           this.value_BlockPos = var4x.destination();
                           this.prev_V_6();
                           AiNavDebug.init("Joined loaded route before the terrain frontier");
                        }
                     }
                  }
               }
            }

            private void state_V_3() {
               if (this.fill_FileStore_TaskStat != null) {
                  this.fill_FileStore_TaskStat.putAt_V();
                  this.fill_FileStore_TaskStat = null;
               }
            }

            private AiOperation.Result pollOf_AiOperation_Result_3() {
               if (this.putAt_BufferCore == null && !OverlayScreenController.init_Z()) {
                  AiParkourFallWarning.Inner_DataUtil var1x = AiParkourFallWarning.putAt_BufferCore_CacheHe_Minecraft_List(
                     this.putAt_Minecraft, this.putAt_FileStore_StreamSt.movements()
                  );
                  if (var1x.damage() == 0.0F) {
                     this.readEx(false);
                     return this.fill("Found a route with a harmless landing");
                  }

                  this.putAt_BufferCore = new AiParkourFallWarning(this.putAt_Minecraft, var1x, this::readEx, () -> this.tick_Z = true);
                  if (!this.putAt_BufferCore.putAt_Z()) {
                     this.putAt_BufferCore = null;
                  }
               }

               return this.fill("Waiting for fall damage approval");
            }

            private void readEx(boolean var1) {
               if (this.flushOf_Z
                  || this.putAt_Minecraft.level != this.putAt_Obj
                  || this.putAt_Minecraft.player == null
                  || !this.putAt_Minecraft.player.isAlive()) {
                  this.tick_Z = true;
               } else if (this.closeOf_Z) {
                  if (var1) {
                     Module var4x = al.init_Module_Str("no-fall");
                     if (var4x != null && !var4x.isEnabled()) {
                        var18.putAt_JsonWorker_TableCa = var4x.borrowEnabled(true);
                     }
                  }

                  var18.putAt_Z = true;
                  this.closeOf_Z = false;
                  this.putAt_FileStore_StreamSt = null;
                  this.putAt_BufferCore = null;
               } else {
                  if (var1) {
                     Module var2 = al.init_Module_Str("no-fall");
                     if (var2 != null && !var2.isEnabled()) {
                        this.putAt_JsonWorker_TableCa = var2.borrowEnabled(true);
                     }
                  }

                  AiPathfinder.Inner_PacketUtil var3x = this.putAt_FileStore_StreamSt;
                  this.putAt_FileStore_StreamSt = null;
                  this.putAt_BufferCore = null;
                  this.pollOf_Z = false;
                  if (this.closeOf_Z_4() && this.readEx_BlockPos_5().equals(var3x.movements().getFirst().from())) {
                     this.putAt_V_FileStore_StreamSt(var3x);
                  } else {
                     this.putAt_FileStore_TaskStat.putAt(this.readEx_BlockPos_5());
                  }
               }
            }

            AiOperation.Result prev_AiOperation_Result_7() {
               if (this.fill_FileStore_RingUtil == AiPathfinder.Inner_NetHelper.PARKOUR_SAFE) {
                  this.pollOf_Z = true;
                  this.fill_FileStore_RingUtil = AiPathfinder.Inner_NetHelper.PARKOUR_NO_DAMAGE;
                  this.value_BlockPos = null;
                  if (var18 != null) {
                     this.value_V_9();
                  }

                  if (this.chunkAt_D >= 0.0) {
                     this.putAt_RingCache.pollAt_Z();
                  }

                  this.putAt_FileStore_TaskStat.putAt_V_BlockPos_2(this.readEx_BlockPos_5(), this.putAt_FileStore_TokenCod, this.putAt_FileStore_CacheHel());
                  return this.fill("Checking whether a route requires a damaging fall");
               } else {
                  String var1x = "No route toward the target in loaded terrain";
                  if (Boolean.getBoolean("autism.ai.schematics")) {
                     this.prevEx_V_7();
                  }

                  if (this.putAt_Minecraft.level != null && System.nanoTime() - AiGoto.value > 10000000000L) {
                     AiGoto.value = System.nanoTime();
                     AutismClientAddon.LOG.info(
                           "[AI] no route from {} to {}: {}",
                           this.putAt_RingCache.putAt_BlockPos().toShortString(),
                           var0.toShortString(),
                           AiTerrainDump.init(this.putAt_Minecraft.level, this.putAt_RingCache.putAt_BlockPos(), var0, 4, 2)
                        );
                  }

                  if (this.value_Z_11()) {
                     return this.fill(var1x + "; stabilizing before replan");
                  }

                  if ((var11 instanceof AiGoto.Inner_DataUtil || var11 instanceof PlacementGoal var2 && var2.load())
                     && this.closeOf_I < 2
                     && this.pollAt_Z_4()) {
                     this.closeOf_I++;
                     this.putAt_RingCache.pollAt_Z();
                     return this.fill("Leaving the partial construction footing before replanning");
                  }

                  if (!var15 && !this.frameOf && !AiLowHealth.init_Z() && this.putAt_Minecraft.player.mayBuild() && this.readEx_Z_15()) {
                     this.frameOf = true;
                     AiPathfinder.Inner_DataUtil var5x = this.putAt_FileStore_CacheHel().withDig();
                     if (this.putAt(this.putAt_RingCache.putAt_BlockPos(), 3)) {
                        var var3x = new HashSet<>(var5x.disabledTypes());
                        var3x.add(AiPathfinder.Inner_SyncManager.PILLAR);
                        var5x = var5x.withDisabledTypes(var3x);
                     }

                     this.putAt_FileStore_TaskStat.putAt(var5x);
                     this.putAt_FileStore_TaskStat.putAt(this.putAt_RingCache.putAt_BlockPos());
                     this.putAt_RingCache.closeOf_V();
                     return this.fill(var1x + "; sealed pocket: dig-enabled escape replan");
                  } else {
                     AiOperation.Result var4x = this.prevEx_AiOperation_Result_7();
                     if (var4x != null) {
                        return var4x;
                     }

                     AutismClientAddon.LOG.info("[AI] travel {}: {} at {}", var13 ? "job" : "goto", var1x, this.putAt_RingCache.putAt_BlockPos().toShortString());
                     return this.putAt(autismclient.ai.AiFailure.UNREACHABLE, var1x);
                  }
               }
            }

            String putAt_Str_Vec3_2(Vec3 var1) {
               BlockPos var2 = var13 && this.putAt_RingCache.value_BlockPos() != null ? this.putAt_RingCache.value_BlockPos() : var0;
               int var3x = (int)Math.round(Math.sqrt(var1.distanceToSqr(Vec3.atBottomCenterOf(var2))));
               if (!AutismLiteVariant.enabled() && var14 && !var13 && this.putAt_J != this.putAt_Minecraft.level.getGameTime()) {
                  this.putAt_J = this.putAt_Minecraft.level.getGameTime();
                  int var4x = this.putAt_RingCache.closeOf_I();
                  if (var4x < 0) {
                     var4x = Math.clamp((int)(100.0 * (1.0 - var3x / Math.max(1.0, this.value_D))), 0, 99);
                  }

                  this.putAt_V_Obj(this.putAt_RingCache, "Moving", var3x, var4x);
               }

               String var5x = this.putAt_FileStore_TaskStat.fill_Z() ? ", planning ahead" : "";
               return "Travelling to " + var2.toShortString() + " (" + var3x + "m" + var5x + ")";
            }

            AiPathfinder.Inner_DataUtil putAt_FileStore_CacheHel() {
               boolean var1x = var12.sprint() && this.putAt_Minecraft.player.getFoodData().getFoodLevel() > 6;
               if (this.putAt_FileStore_TextStor instanceof AiPathfinder.Inner_StreamUtil var2) {
                  var2.putAt_V_ItemStackArr(this.putAt_ItemStackArr_2());
               } else if (this.putAt_FileStore_TextStor instanceof AiLiveGrid var3x) {
                  var3x.putAt_V_ItemStackArr_2(this.putAt_ItemStackArr_2());
               }

               AiLiveWorld.putAt_D_LivingEntity(this.putAt_Minecraft.player);
               boolean var9x = this.putAt_Minecraft.player.hasEffect(MobEffects.JUMP_BOOST);
               boolean var10 = this.putAt_Minecraft.player.hasEffect(MobEffects.SLOW_FALLING);
               boolean var12x = this.putAt_Minecraft.player.hasEffect(MobEffects.LEVITATION);
               boolean var5x = AiLowHealth.init_Z() && this.putAt_Minecraft.player.getHealth() <= 4.0F;
               boolean var6x = this.putAt_Minecraft.player.mayBuild();
               var var7x = new HashSet<>(this.fill_Set);
               if (!var12.bunnyhop()) {
                  var7x.add(AiPathfinder.Inner_SyncManager.SPRINT_JUMP);
               }

               if (!var12.allowClimb()) {
                  var7x.add(AiPathfinder.Inner_SyncManager.CLIMB_1);
               }

               if (!var12.allowDoors()) {
                  var7x.add(AiPathfinder.Inner_SyncManager.DOOR);
               }

               if (this.putAt_FileStore_TokenCod != null) {
                  var7x.addAll(this.putAt_FileStore_TokenCod.putAt_Set());
               }

               return new AiPathfinder.Inner_DataUtil(
                     var3 && var6x,
                     var4 && var6x && !var5x,
                     var5 && var6x && !var5x,
                     var1x && !var5x,
                     this.prevEx_Z_9(),
                     !var5x,
                     var5x ? AiPathfinder.Inner_IoWorker.SAFEST : var7,
                     this.putAt_FileStore_TaskStor(),
                     var9x,
                     var10,
                     var12x,
                     var9
                  )
                  .withDisabledTypes(var7x)
                  .withConstructionBlocks(this.readEx_I_10())
                  .withMovementLimits(var12.maxFall(), var12.avoidWater())
                  .withFarmlandRepair(var11 instanceof AiGoto.Inner_DataUtil || var11 instanceof PlacementGoal var8x && var8x.load())
                  .withSupportedBridges(this.putAt_FileStore_TokenCod != null && this.putAt_FileStore_TokenCod.putAt_Z())
                  .withFallPolicy(this.fill_FileStore_RingUtil);
            }

            int readEx_I_10() {
               int var1x = 0;
               int var2 = 0;
               int var3x = 0;

               for (int var4x = 0; var4x < (var8 ? 36 : 9); var4x++) {
                  ItemStack var5x = this.putAt_Minecraft.player.getInventory().getItem(var4x);
                  int var6x = this.putAt_FileStore_TokenCod == null ? 0 : this.putAt_FileStore_TokenCod.putAt_I_ItemStack(var5x);
                  if (var6x > 0) {
                     var1x += var6x;
                  } else if (this.putAt_Z_ItemStack_2(var5x)) {
                     if (var5x.is(ItemTags.PLANKS)) {
                        var2 += var5x.getCount();
                     } else if (AiRestock.init(var5x)) {
                        var3x += var5x.getCount();
                     } else {
                        var1x += var5x.getCount();
                     }
                  }
               }

               var1x += AiRestock.write() > 0 ? Math.min(var3x, AiRestock.read(this.putAt_Minecraft.player.getInventory())) : var3x;
               if (!AutismLiteVariant.enabled()) {
                  var2 += AiPlanks.init(this.putAt_Minecraft.player.getInventory());
               }

               if (AiRestock.init_I() > 0) {
                  var2 = Math.min(var2, AiRestock.init(this.putAt_Minecraft.player.getInventory()));
               }

               return var1x + var2;
            }

            boolean prevEx_Z_9() {
               if (this.prevEx_I_3() < 0 && (!var8 || this.value_I_5() < 0)) {
                  return AutismLiteVariant.enabled()
                     ? false
                     : AiPlanks.init(this.putAt_Minecraft.player.getInventory()) > 0 && AiRestock.init(this.putAt_Minecraft.player.getInventory()) > 0;
               } else {
                  return true;
               }
            }

            int value_I_5() {
               ArrayList<AiRestock.Inner_CachePool> var1x = new ArrayList<>(27);

               for (int var2 = 9; var2 < 36; var2++) {
                  ItemStack var3x = this.putAt_Minecraft.player.getInventory().getItem(var2);
                  if (var3x.getItem() instanceof BlockItem var4x && AiRestock.init(var3x, this.putAt_Minecraft.player.getInventory())) {
                     var1x.add(
                        new AiRestock.Inner_CachePool(
                           BuiltInRegistries.ITEM.getKey(var3x.getItem()).toString(),
                           var3x.getCount(),
                           var4x.getBlock() instanceof FallingBlock,
                           var4x.getBlock()
                              .defaultBlockState()
                              .isCollisionShapeFullBlock(this.putAt_Minecraft.level, this.putAt_Minecraft.player.blockPosition()),
                           var4x.getBlock().defaultBlockState().hasBlockEntity()
                        )
                     );
                  } else {
                     var1x.add(new AiRestock.Inner_CachePool(null, 0, false, false, false));
                  }
               }

               int var6x = AiRestock.init(var1x, null);
               return var6x < 0 ? -1 : 9 + var6x;
            }

            AiPathfinder.Inner_PoolEntry putAt_FileStore_TaskStor() {
               if (this.putAt_Minecraft.level == null) {
                  return AiPathfinder.Inner_PoolEntry.value;
               }

               final AiPathfinder.Inner_PoolEntry $outerCost;
               if (AutismLiteVariant.enabled()) {
                  $outerCost = AiPathfinder.Inner_PoolEntry.value;
               } else {
                  $outerCost = autismclient.ai.AiDragonZones.init_ClassCache_PoolEnt(this.putAt_Minecraft);
               }

               if (var7 != AiPathfinder.Inner_IoWorker.SAFEST) {
                  return $outerCost == AiPathfinder.Inner_PoolEntry.value ? AiPathfinder.Inner_PoolEntry.value : new AiPathfinder.Inner_PoolEntry() {
                     @Override
                     public double surcharge(int var1x, int var2, int var3x) {
                        return 0.0;
                     }

                     @Override
                     public double putAt(int var1x, int var2, int var3x) {
                        return $outerCost.surcharge(var1x, var2, var3x);
                     }
                  };
               }

               final ArrayList<BlockPos> var2 = new ArrayList<>();
               final ArrayList<BlockPos> var3x = new ArrayList<>();
               ArrayList var4x = new ArrayList();
               ArrayList<Double> var5x = new ArrayList<>();

               for (Entity var7x : this.putAt_Minecraft.level.entitiesForRendering()) {
                  if (var7x instanceof Enemy && var7x.isAlive() && var7x.distanceToSqr(this.putAt_Minecraft.player) <= 576.0 && !AiGoto.putAt(var7x)) {
                     var2.add(var7x.blockPosition());
                     if (var7x instanceof PiglinBrute || var7x instanceof Hoglin || var7x instanceof Zoglin || var7x instanceof WitherSkeleton) {
                        var3x.add(var7x.blockPosition());
                     }

                     if (AiSight.init(var7x)) {
                        var4x.add(var7x.getEyePosition());
                        var5x.add(AiSight.load(var7x) ? 40.0 : 10.0);
                     }
                  }
               }

               final AiPathfinder.Inner_PoolEntry var10 = AiPathfinder.Inner_PoolEntry.putAt(var2);
               final ClientLevel var11x = this.putAt_Minecraft.level;
               final AiPathfinder.Inner_PoolEntry var8x = AiSight.init(var11x, var4x, var5x);
               final boolean var9x = var11x.dimension() == Level.NETHER;
               return new AiPathfinder.Inner_PoolEntry() {
                  @Override
                  public double surcharge(int var1x, int var2x, int var3xx) {
                     double var4x = var8x.surcharge(var1x, var2x, var3xx);
                     return var10.surcharge(var1x, var2x, var3xx)
                        + fill(var11x, var1x, var2x, var3xx)
                        + var4x
                        + putAt_D_ClientLevel(var11x, var1x, var2x, var3xx)
                        + putAt_D_List(var3x, var1x, var2x, var3xx)
                        + (
                           (var4x > 0.0 || var9x && putAt_Z_List(var2, var1x, var2x, var3xx, 10))
                                 && putAt_Z_ClientLevel(var11x, var1x, var2x, var3xx)
                              ? 60.0
                              : 0.0
                        );
                  }

                  @Override
                  public double putAt(int var1x, int var2x, int var3xx, int var4xx, int var5xx, int var6x) {
                     return fill(var11x, var4xx, var5xx, var6x) > 0.0 && fill(var11x, var1x, var2x, var3xx) == 0.0
                        ? 40.0
                        : 0.0;
                  }

                  @Override
                  public double putAt(int var1x, int var2x, int var3xx) {
                     return $outerCost.surcharge(var1x, var2x, var3xx);
                  }
               };
            }

            static double putAt_D_ClientLevel(ClientLevel var0x, int var1x, int var2, int var3x) {
               try {
                  for (int var4x = -1; var4x <= 1; var4x++) {
                     for (int var5x = -1; var5x <= 1; var5x++) {
                        if (var4x != 0 || var5x != 0) {
                           for (int var6x = -1; var6x <= 0; var6x++) {
                              if (var0x.getFluidState(new BlockPos(var1x + var4x, var2 + var6x, var3x + var5x)).is(FluidTags.LAVA)) {
                                 return 8.0;
                              }
                           }
                        }
                     }
                  }

                  return 0.0;
               } catch (RuntimeException var7x) {
                  return 0.0;
               }
            }

            static boolean putAt_Z_ClientLevel(ClientLevel var0x, int var1x, int var2, int var3x) {
               try {
                  for (int var4x = -1; var4x <= 1; var4x++) {
                     for (int var5x = -1; var5x <= 1; var5x++) {
                        if (var4x != 0 || var5x != 0) {
                           boolean var6x = true;

                           for (int var7x = 0; var7x >= -5 && var6x; var7x--) {
                              BlockPos var8x = new BlockPos(var1x + var4x, var2 + var7x, var3x + var5x);
                              if (!var0x.getBlockState(var8x).getCollisionShape(var0x, var8x).isEmpty() || var0x.getFluidState(var8x).is(FluidTags.WATER)) {
                                 var6x = false;
                              }
                           }

                           if (var6x) {
                              return true;
                           }
                        }
                     }
                  }

                  return false;
               } catch (RuntimeException var9x) {
                  return false;
               }
            }

            static boolean putAt_Z_List(List<BlockPos> var0x, int var1x, int var2, int var3x, int var4x) {
               for (BlockPos var6x : var0x) {
                  double var7x = var1x - var6x.getX();
                  double var9x = var2 - var6x.getY();
                  double var11x = var3x - var6x.getZ();
                  if (var7x * var7x + var9x * var9x + var11x * var11x <= var4x * var4x) {
                     return true;
                  }
               }

               return false;
            }

            static double putAt_D_List(List<BlockPos> var0x, int var1x, int var2, int var3x) {
               if (var0x.isEmpty()) {
                  return 0.0;
               }

               double var4x = 1.7976931348623157E308;

               for (BlockPos var7x : var0x) {
                  double var8x = var1x - var7x.getX();
                  double var10 = var2 - var7x.getY();
                  double var12x = var3x - var7x.getZ();
                  var4x = Math.min(var4x, var8x * var8x + var10 * var10 + var12x * var12x);
               }

               return var4x >= 400.0 ? 0.0 : 400.0 * (1.0 - Math.sqrt(var4x) / 20.0);
            }

            static double fill(ClientLevel var0x, int var1x, int var2, int var3x) {
               if (var0x.dimension() != Level.OVERWORLD) {
                  return 0.0;
               }

               BlockPos var4x = new BlockPos(var1x, var2, var3x);

               try {
                  if (!var0x.getBlockState(var4x).isAir()
                     || var0x.getBrightness(LightLayer.SKY, var4x) > 0
                     || var0x.getBrightness(LightLayer.BLOCK, var4x) >= 8) {
                     return 0.0;
                  }
               } catch (RuntimeException var6x) {
                  return 0.0;
               }

               return AiEditLedger.write_Z(var4x) ? 0.0 : 12.0;
            }

            int prevEx_I_3() {
               for (int var1x = 0; var1x < 9; var1x++) {
                  if (this.putAt_Z_ItemStack_2(this.putAt_Minecraft.player.getInventory().getItem(var1x))) {
                     return var1x;
                  }
               }

               return -1;
            }

            boolean putAt_Z_ItemStack_2(ItemStack var1) {
               return var1.getItem() instanceof BlockItem var2
                  && AiRestock.init_Z(BuiltInRegistries.ITEM.getKey(var1.getItem()).toString())
                  && AiRestock.init(var1, this.putAt_Minecraft.player.getInventory())
                  && !(var2.getBlock() instanceof FallingBlock)
                  && var2.getBlock().defaultBlockState().isCollisionShapeFullBlock(this.putAt_Minecraft.level, this.putAt_Minecraft.player.blockPosition())
                  && !var2.getBlock().defaultBlockState().hasBlockEntity();
            }

            ItemStack[] putAt_ItemStackArr_2() {
               long var1x = AiServerEvidence.init_J();
               if (this.putAt_ItemStackArr != null && var1x == this.chunkAt_J) {
                  return this.putAt_ItemStackArr;
               }

               this.putAt_ItemStackArr = this.fill_ItemStackArr();
               this.chunkAt_J = var1x;
               return this.putAt_ItemStackArr;
            }

            ItemStack[] fill_ItemStackArr() {
               byte var1x = 36;
               ItemStack[] var2 = new ItemStack[var1x];

               for (int var3x = 0; var3x < var1x; var3x++) {
                  var2[var3x] = this.putAt_Minecraft.player.getInventory().getItem(var3x).copy();
               }

               return var2;
            }

            BlockPos readEx_BlockPos_5() {
               return AiStance.init(this.putAt_Minecraft);
            }

            boolean prev_Z_8() {
               AiPathfinder.Inner_JsonUtil var1x = AiGoto.readEx(this.putAt_FileStore_TokenCod);
               if ((this.chunkAt_Z_6() || var1x instanceof AiGoto.Inner_StreamUtil var2 && var2.putAt_Z && this.putAt_Minecraft.player.isInWater())
                  && !this.putAt_RingCache.state_Z()
                  && !this.putAt_RingCache.closeOf_Z()) {
                  BlockPos var5x = this.putAt_RingCache.putAt_BlockPos();
                  if (!this.putAt_FileStore_TokenCod.putAt_Z_BlockPos(var5x)) {
                     return false;
                  }

                  if (var1x instanceof AiGoto.Inner_StreamUtil var7x) {
                     if (var7x.putAt(this.putAt_Minecraft)) {
                        return true;
                     }

                     if (this.putAt_FileStore_TokenCod.isInGoal(var5x.getX(), var5x.getY(), var5x.getZ()) && var7x.prevEx(var5x)) {
                        this.putAt_FileStore_TaskStat.putAt(var5x);
                     }

                     return false;
                  } else if (var1x instanceof PlacementGoal var6x) {
                     return !AiGoto.putAt(this.putAt_RingCache.putAt_RingCache_TableCac(), this.putAt_Minecraft.player.getDeltaMovement())
                        ? false
                        : var6x.init(this.putAt_Minecraft);
                  } else if (var1x instanceof InteractionGoal var3x) {
                     if (var3x.load_Z() && !AiMotionSafety.putAt(var3x.load_BlockPos())) {
                        var3x.init_V(var5x);
                        return false;
                     } else {
                        BlockHitResult var4x = AiBlockInteraction.load(this.putAt_Minecraft, var3x.load_BlockPos(), var3x.init_Direction());
                        return var4x != null && AiBlockInteraction.init(this.putAt_Minecraft).distanceTo(var4x.getLocation()) <= var3x.load_D() + 0.25;
                     }
                  } else if (!this.putAt_FileStore_TokenCod.putAt_Z_I(var5x.getX(), var5x.getY(), var5x.getZ(), this.putAt_FileStore_TextStor)) {
                     return false;
                  } else {
                     return var24 <= 0.65
                           && var5x.equals(var0)
                           && this.putAt_Minecraft.level != null
                           && AiPathfinder.Inner_StreamUtil.value(this.putAt_Minecraft.level.getBlockState(var0))
                        ? AiPaceScript.putAt(AiPhysics.Inner_StreamUtil.of(this.putAt_Minecraft.player), var0)
                        : true;
                  }
               } else {
                  return false;
               }
            }

            boolean chunkAt_Z_6() {
               return AiMotionSafety.fill_Z();
            }

            boolean state_Z_4() {
               if (this.putAt_Minecraft.level.noCollision(this.putAt_Minecraft.player, this.putAt_Minecraft.player.getBoundingBox().move(0.0, 1.0, 0.0))) {
                  return false;
               }

               BlockPos var1x = this.readEx_BlockPos_5();

               for (Direction var3x : List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
                  for (BlockPos var5x : List.of(var1x.relative(var3x), var1x.relative(var3x).above())) {
                     if (this.putAt_Minecraft.level.isInsideBuildHeight(var5x)
                        && this.putAt_Minecraft.level.getChunkSource().getChunk(var5x.getX() >> 4, var5x.getZ() >> 4, ChunkStatus.FULL, false) != null) {
                        BlockState var6x = this.putAt_Minecraft.level.getBlockState(var5x);
                        if (var6x.isCollisionShapeFullBlock(this.putAt_Minecraft.level, var5x)
                           && var6x.requiresCorrectToolForDrops()
                           && MiningCost.load(this.putAt_Minecraft, var6x, true) < 0) {
                           continue;
                        }

                        return false;
                     }

                     return false;
                  }
               }

               return true;
            }

            boolean closeOf_Z_4() {
               return this.chunkAt_Z_6()
                  || this.putAt_Minecraft.player.isInLava()
                  || this.putAt_Minecraft.player.getAbilities().flying
                  || this.putAt_Minecraft.level.getBlockState(this.putAt_Minecraft.player.blockPosition()).is(Blocks.COBWEB)
                  || this.putAt_Minecraft.level.getBlockState(this.putAt_Minecraft.player.blockPosition().above()).is(Blocks.COBWEB);
            }

            boolean pollAt_Z_4() {
               LocalPlayer var1x = this.putAt_Minecraft.player;
               if (var1x.onGround() && this.putAt_Minecraft.level.noCollision(var1x, var1x.getBoundingBox().deflate(0.001))) {
                  BlockPos var2 = var1x.blockPosition();
                  if (!AiGoto.putAt_Z_VoxelShape(
                     this.putAt_Minecraft.level.getBlockState(var2.below()).getCollisionShape(this.putAt_Minecraft.level, var2.below()),
                     this.putAt_Minecraft.level.getBlockState(var2).getCollisionShape(this.putAt_Minecraft.level, var2),
                     this.putAt_Minecraft.level.getBlockState(var2.above()).getCollisionShape(this.putAt_Minecraft.level, var2.above())
                  )) {
                     return false;
                  }

                  Vec3 var3x = null;
                  double var4x = 1.7976931348623157E308;
                  boolean var6x = false;
                  boolean var7x = false;
                  AiLiveWorld var8x = new AiLiveWorld(this.putAt_Minecraft.level, var1x);
                  BlockPos var9x = var1x.blockPosition();

                  for (int var10 = -2; var10 <= 2; var10++) {
                     for (int var11x = -2; var11x <= 2; var11x++) {
                        for (int var12x = -1; var12x <= 1; var12x++) {
                           BlockPos var13x = var9x.offset(var10, var12x, var11x);
                           AiPathfinder.Inner_FileCache var14x = this.putAt_FileStore_TextStor.probe(var13x.getX(), var13x.getY(), var13x.getZ());
                           AiPathfinder.Inner_FileCache var15x = this.putAt_FileStore_TextStor.probe(var13x.getX(), var13x.getY() + 1, var13x.getZ());
                           boolean var16x = var12.allowClimb()
                              && var14x.loaded()
                              && var14x.climbable()
                              && !var14x.lava()
                              && !var14x.hazardFeet()
                              && var15x.loaded()
                              && (var15x.passable() || var15x.climbable())
                              && !var15x.lava()
                              && !var15x.hazardFeet()
                              && var8x.state(var13x.getX(), var13x.getY(), var13x.getZ());
                           if (var16x
                              || AiPathfinder.Inner_RenderState.putAt_Z_FileStore_BitCache(
                                 var14x,
                                 this.putAt_FileStore_TextStor.probe(var13x.getX(), var13x.getY() + 1, var13x.getZ()),
                                 this.putAt_FileStore_TextStor.probe(var13x.getX(), var13x.getY() - 1, var13x.getZ())
                              )) {
                              BlockState var17x = this.putAt_Minecraft.level.getBlockState(var13x.below());
                              VoxelShape var18x = var17x.getCollisionShape(this.putAt_Minecraft.level, var13x.below());
                              if (var16x
                                 || !var18x.isEmpty()
                                    && var17x.getFluidState().isEmpty()
                                    && !var17x.is(Blocks.FARMLAND)
                                    && !var17x.is(Blocks.MAGMA_BLOCK)
                                    && !var17x.is(Blocks.CACTUS)) {
                                 double var19x = var16x ? 1.0 : var18x.max(Axis.Y);
                                 if (!(var19x < 0.05) && !(var19x > 1.5)) {
                                    Vec3 var21x = new Vec3(var13x.getX() + 0.5, var13x.getY() - 1 + var19x, var13x.getZ() + 0.5);
                                    Vec3 var22x = var21x.subtract(var1x.position());
                                    if (!(var22x.lengthSqr() < 0.03)
                                       && (!var16x || !(Math.abs(var22x.y) > 0.08))
                                       && !(var22x.y < -0.6)
                                       && !(var22x.y > 1.25)
                                       && this.putAt_Minecraft.level.noCollision(var1x, var1x.getBoundingBox().move(var22x).deflate(0.001))) {
                                       boolean var23 = var22x.y > 0.6;
                                       if ((!var23 || !AiPathfinder.Inner_CachePool.putAt() || !this.fill_Z_8(var1x.blockPosition()))
                                          && (var23 || AiMotionSafety.putAt_Z_Vec3_Vec3(var1x.position(), var21x))) {
                                          if (var23) {
                                             float var24x = (float)(Math.toDegrees(Math.atan2(var22x.z, var22x.x)) - 90.0);
                                             AiJumpSolver.UiHelper var25 = AiJumpSolver.putAt_IoUtil_IoPool_TokenCore_TaskStat(
                                                var8x, AiPhysics.Inner_StreamUtil.of(var1x), var24x, false, var13x, var21x.y
                                             );
                                             if (var25.runTicks() != 0 || var25.margin() < 0.08) {
                                                continue;
                                             }
                                          }

                                          boolean var30 = true;
                                          int var31 = Math.max(8, (int)Math.ceil(Math.hypot(var22x.x, var22x.z) * 8.0));

                                          for (int var26x = 1; !var23 && var26x <= var31; var26x++) {
                                             AABB var27 = var1x.getBoundingBox()
                                                .move(var22x.x * var26x / var31, Math.max(0.0, var22x.y), var22x.z * var26x / var31);
                                             BlockPos var28 = BlockPos.containing(var27.getCenter().x, var27.minY, var27.getCenter().z);
                                             boolean var29 = var16x && var8x.state(var28.getX(), var28.getY(), var28.getZ());
                                             if (!this.putAt_Minecraft.level.noCollision(var1x, var27.deflate(0.001))
                                                || !var29 && this.putAt_Minecraft.level.noCollision(var1x, var27.move(0.0, -Math.abs(var22x.y) - 0.08, 0.0))) {
                                                var30 = false;
                                                break;
                                             }
                                          }

                                          double var32 = var22x.length() * 4.5
                                             + this.putAt_FileStore_TokenCod.heuristic(var13x.getX(), var13x.getY(), var13x.getZ());
                                          if (var30 && var32 < var4x) {
                                             var3x = var21x;
                                             var4x = var32;
                                             var6x = var23;
                                             var7x = var16x;
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  this.putAt_Vec3 = var3x;
                  this.store_Z = var6x;
                  this.nextBy_Z = var7x;
                  this.dataEx_Z = false;
                  this.state_I = 0;
                  if (var3x != null && Boolean.getBoolean("autism.navigation.debug")) {
                     AutismClientAddon.LOG.info("[AI] footing exit {} -> {} (jump {})", var1x.position(), var3x, var6x);
                  }

                  return var3x != null;
               } else {
                  return false;
               }
            }

            boolean fill_Z_8(BlockPos var1) {
               for (Direction var3x : Plane.HORIZONTAL) {
                  BlockPos var4x = var1.relative(var3x);
                  boolean var5x = false;

                  for (int var6x = 0; var6x <= 4 && !var5x; var6x++) {
                     BlockPos var7x = var4x.below(var6x);
                     BlockState var8x = this.putAt_Minecraft.level.getBlockState(var7x);
                     var5x = !var8x.getCollisionShape(this.putAt_Minecraft.level, var7x).isEmpty() || !var8x.getFluidState().isEmpty();
                  }

                  if (!var5x) {
                     return true;
                  }
               }

               return false;
            }

            AiOperation.Result chunkAt_AiOperation_Result_5() {
               LocalPlayer var1x = this.putAt_Minecraft.player;
               double var2 = var1x.position().distanceToSqr(this.putAt_Vec3);
               double var4x = Math.pow(var1x.getX() - this.putAt_Vec3.x, 2.0) + Math.pow(var1x.getZ() - this.putAt_Vec3.z, 2.0);
               this.putAt_Minecraft.options.keyJump.setDown(false);
               this.putAt_Minecraft.options.keyShift.setDown(this.dataEx_Z || this.nextBy_Z && var1x.onClimbable());
               if (this.dataEx_Z) {
                  var1x.setSprinting(false);
               }

               this.putAt_Minecraft.options.keyDown.setDown(false);
               this.putAt_Minecraft.options.keyLeft.setDown(false);
               this.putAt_Minecraft.options.keyRight.setDown(false);
               boolean var6x = this.nextBy_Z && var1x.onClimbable() && var4x < 0.03;
               double var7x = this.dataEx_Z ? 0.0025 : 0.03;
               if ((!var1x.onGround() || !(var2 < var7x)) && !var6x && ++this.state_I <= (this.dataEx_Z ? 48 : 24)) {
                  this.putAt_CacheHelper.init_V_DataUtil_DataUtil(AiAim.Inner_DataUtil.NORMAL);
                  if (var4x >= var7x) {
                     this.putAt_CacheHelper.init_V_ce_DataUtil(
                           new RotationMath.Inner_DataUtil(AiPhysics.putAt_F_D(var1x.getX(), var1x.getZ(), this.putAt_Vec3.x, this.putAt_Vec3.z), 0.0F)
                        );
                  }

                  this.putAt_CacheHelper.load_V();
                  this.putAt_Minecraft.options.keyUp.setDown(var4x >= var7x && this.putAt_CacheHelper.load_Z());
                  if (this.store_Z && this.putAt_CacheHelper.load_Z()) {
                     AiLiveWorld var9x = new AiLiveWorld(this.putAt_Minecraft.level, var1x);
                     AiPhysics.Inner_StreamUtil var10 = AiPhysics.Inner_StreamUtil.of(var1x);
                     BlockPos var11x = BlockPos.containing(this.putAt_Vec3.x, Math.ceil(this.putAt_Vec3.y - 0.001), this.putAt_Vec3.z);
                     if (var1x.onGround() && this.putAt_Vec3.y - var1x.getY() > 0.6) {
                        AiJumpSolver.UiHelper var15x = AiJumpSolver.putAt_IoUtil_IoPool_TokenCore_TaskStat(
                           var9x, var10, var1x.getYRot(), false, var11x, this.putAt_Vec3.y
                        );
                        this.putAt_Minecraft.options.keyUp.setDown(var15x.runTicks() == 0 && var15x.margin() >= 0.08);
                        this.putAt_Minecraft.options.keyJump.setDown(var15x.runTicks() == 0 && var15x.margin() >= 0.08);
                     } else if (!var1x.onGround()) {
                        AiJumpSolver.Inner_DataUtil var12x = AiJumpSolver.putAt_IoUtil_CacheHelper_TokenCore_TaskStat(
                           var9x, var10, var1x.getYRot(), false, var11x, this.putAt_Vec3.y
                        );
                        this.putAt_Minecraft.options.keyUp.setDown(var12x.forward() > 0.0);
                        this.putAt_Minecraft.options.keyLeft.setDown(var12x.strafe() > 0.0);
                        this.putAt_Minecraft.options.keyRight.setDown(var12x.strafe() < 0.0);
                     }
                  }

                  double var13x = this.putAt_Minecraft.options.keyUp.isDown() ? 1.0 : 0.0;
                  double var14x = (this.putAt_Minecraft.options.keyLeft.isDown() ? 1 : 0) - (this.putAt_Minecraft.options.keyRight.isDown() ? 1 : 0);
                  if (!AiMotionSafety.putAt_Z_D(var13x, var14x, this.putAt_Minecraft.options.keyJump.isDown(), false)) {
                     AiMotionSafety.fill_V();
                     this.putAt_Vec3 = null;
                     this.putAt_FileStore_TaskStat.putAt(this.putAt_RingCache.putAt_BlockPos());
                     return this.fill("Partial-block exit became unsafe; replanning");
                  } else {
                     return this.fill("Stepping clear of the partial block");
                  }
               } else {
                  this.putAt_Minecraft.options.keyUp.setDown(false);
                  this.putAt_Vec3 = null;
                  if (this.dataEx_Z) {
                     this.putAt_Minecraft.options.keyShift.setDown(false);
                  }

                  this.dataEx_Z = false;
                  if (this.stateOf_Z) {
                     this.putAt_FileStore_TaskStat.putAt(this.putAt_RingCache.putAt_BlockPos());
                  }

                  return this.fill("Replanning from the clear stance");
               }
            }

            boolean readEx(BlockPos var1) {
               LocalPlayer var2 = this.putAt_Minecraft.player;
               if (var2.onGround() && !this.putAt_Set.contains(var1.asLong())) {
                  Vec3 var3x = new Vec3(var1.getX() + 0.5, var2.getY(), var1.getZ() + 0.5);
                  Vec3 var4x = var3x.subtract(var2.position());
                  if (!(var4x.lengthSqr() < 0.0025) && AiMotionSafety.putAt_Z_Vec3_Vec3(var2.position(), var3x)) {
                     for (int var5x = 1; var5x <= 8; var5x++) {
                        AABB var6x = var2.getBoundingBox().move(var4x.scale(var5x / 8.0)).deflate(0.001);
                        if (!this.putAt_Minecraft.level.noCollision(var2, var6x) || this.putAt_Minecraft.level.noCollision(var2, var6x.move(0.0, -0.08, 0.0))) {
                           return false;
                        }
                     }

                     this.putAt_Set.add(var1.asLong());
                     this.putAt_Vec3 = var3x;
                     this.store_Z = this.nextBy_Z = false;
                     this.dataEx_Z = true;
                     this.state_I = 0;
                     return true;
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            }

            boolean pollOf_Z_2() {
               if (AiGoto.readEx(this.putAt_FileStore_TokenCod) instanceof PlacementGoal var3x) {
                  BlockPos var6x = this.putAt_RingCache.putAt_BlockPos();
                  if (!this.chunkAt_Z_6() || !this.putAt_FileStore_TokenCod.isInGoal(var6x.getX(), var6x.getY(), var6x.getZ())) {
                     return false;
                  }

                  if (this.putAt_Vec3 != null) {
                     return true;
                  }

                  if (this.readEx(var6x)) {
                     return true;
                  }

                  if (!AiGoto.putAt(this.putAt_RingCache.putAt_RingCache_TableCac(), this.putAt_Minecraft.player.getDeltaMovement())) {
                     AiMotionSafety.putAt_V();
                     return true;
                  }

                  if (++this.pollAt_I > 16) {
                     return false;
                  }

                  var3x.update(var6x);
                  AutismClientAddon.LOG.info("[AI] stance {} cannot place at {}; picking another", var6x.toShortString(), var0.toShortString());
                  this.putAt_FileStore_TaskStat.putAt(var6x);
                  return true;
               } else if (AiGoto.readEx(this.putAt_FileStore_TokenCod) instanceof InteractionGoal var1x && this.chunkAt_Z_6() && var1x.init_I() < 16) {
                  BlockPos var5x = this.putAt_RingCache.putAt_BlockPos();
                  if (!this.putAt_FileStore_TokenCod.isInGoal(var5x.getX(), var5x.getY(), var5x.getZ())) {
                     return false;
                  }

                  if (AiBlockInteraction.load(this.putAt_Minecraft, var1x.load_BlockPos(), var1x.init_Direction()) != null) {
                     return false;
                  }

                  var1x.init_V(var5x);
                  AutismClientAddon.LOG.info("[AI] stance {} has no clear line to {}; picking another", var5x.toShortString(), var1x.load_BlockPos().toShortString());
                  this.putAt_FileStore_TaskStat.putAt(var5x);
                  return true;
               } else {
                  return false;
               }
            }

            JsonObject putAt_JsonObject_61() {
               JsonObject var1x = AiJson.init_JsonObject_ObjArr(
                  "x",
                  var0.getX(),
                  "y",
                  var0.getY(),
                  "z",
                  var0.getZ(),
                  "firstPathReadyMs",
                  this.chunkAt_D,
                  "pathTicks",
                  Math.round(this.putAt_RingCache.putAt_D()),
                  "blocksDug",
                  this.putAt_RingCache.readEx_I(),
                  "pillarsPlaced",
                  this.putAt_RingCache.value_I(),
                  "bridgesPlaced",
                  this.putAt_RingCache.prevEx_I(),
                  "restocks",
                  this.putAt_RingCache.prev_I(),
                  "distanceTravelled",
                  Math.round(this.prev_D * 10.0) / 10.0,
                  "replans",
                  this.prev_I,
                  "waterPlugs",
                  this.tick_I,
                  "protectedWaterPlugVetoes",
                  this.stateOf_I,
                  "blacklisted",
                  this.putAt_FileStore_TaskStat.flowState()
               );
               if (this.readEx_D >= 0.0) {
                  var1x.addProperty("checkpointSearchMs", this.readEx_D);
               }

               if (var19 >= 0) {
                  var1x.addProperty("parkourSpeed", var19);
                  var1x.addProperty("planningMovementSpeed", var26);
               }

               if (var20) {
                  var1x.addProperty("parkourRecordPace", true);
               }

               AiPathfinder.Inner_ByteReader var2 = this.putAt_FileStore_TaskStat.putAt_FileStore_RingBuff();
               if (AiGoto.readEx(this.putAt_FileStore_TokenCod) instanceof AiGoto.Inner_StreamUtil var3x && var3x.fill_BlockPos != null) {
                  var1x.addProperty("fluidSourceX", var3x.fill_BlockPos.getX());
                  var1x.addProperty("fluidSourceY", var3x.fill_BlockPos.getY());
                  var1x.addProperty("fluidSourceZ", var3x.fill_BlockPos.getZ());
               }

               if (Boolean.getBoolean("autism.navigation.debug")) {
                  AiPathfinder.Inner_DataUtil var8x = this.putAt_FileStore_CacheHel();
                  BlockPos var10 = this.putAt_RingCache.putAt_BlockPos();
                  AiPathfinder.Inner_FileCache var5x = this.putAt_FileStore_TextStor.probe(var10.getX(), var10.getY() - 1, var10.getZ());
                  var1x.add(
                     "construction",
                     AiJson.init_JsonObject_ObjArr(
                        "blocks",
                        var8x.constructionBlocks(),
                        "pillar",
                        var8x.allowPillar(),
                        "bridge",
                        var8x.allowBridge(),
                        "repairFarmland",
                        var8x.repairFarmland(),
                        "disabled",
                        var8x.disabledTypes(),
                        "floorTop16",
                        var5x.top16(),
                        "floorFarmland",
                        var5x.farmland()
                     )
                  );
                  if (var5x.farmland()) {
                     JsonArray var6x = new JsonArray();

                     for (int var7x = 0; var7x <= 3; var7x++) {
                        var6x.add(this.putAt_FileStore_TextStor.probe(var10.getX(), var10.getY() + var7x, var10.getZ()).toString());
                     }

                     var1x.add("pillarColumn", var6x);
                  }
               }

               if (var2 != null) {
                  var1x.add(
                     "search",
                     AiJson.init_JsonObject_ObjArr(
                        "elapsedMs",
                        var2.elapsedNanos() / 1000000.0,
                        "rounds",
                        var2.rounds(),
                        "expanded",
                        var2.expanded(),
                        "generated",
                        var2.generated(),
                        "blockQueries",
                        var2.blockQueries(),
                        "peakOpen",
                        var2.peakOpen()
                     )
                  );
               }

               AiPathfinder.UiHelper var9x = this.putAt_RingCache.putAt_FileStore_PacketHe();
               if (var9x != null) {
                  var1x.addProperty("stalledMove", var9x.type().name());
                  var1x.addProperty("stalledFrom", var9x.from().toShortString());
                  var1x.addProperty("stalledTo", var9x.to().toShortString());
               }

               return var1x;
            }

            AiOperation.Result state_AiOperation_Result_4() {
               this.putAt_RingCache.chunkAt_V();
               if (!var14) {
                  return this.fill("Planning route");
               }

               AiPathfinder.Inner_EventBus var1x = this.putAt_FileStore_TaskStat.putAt_FileStore_EventBus();
               String var2 = var1x == null ? "Checking terrain" : String.format(Locale.ROOT, "%,d positions checked", var1x.expanded());
               if (!AutismLiteVariant.enabled()) {
                  if (var1x != null) {
                     AiVisualization.putAt_V_Obj_3(this.fill_Obj, var1x.trail(), 0, () -> null);
                  }

                  int var3x = (int)Math.round(this.putAt_Minecraft.player.position().distanceTo(Vec3.atBottomCenterOf(var0)));
                  this.putAt_V_Obj(this.fill_Obj, "Planning", var3x, 0);
               }

               return this.fill("Planning route - " + var2);
            }

            void putAt_V_Obj(Object var1, String var2, int var3x, int var4x) {
               if (!AutismLiteVariant.enabled()) {
                  if (this.flowState > 1) {
                     var2 = var2 + "  ·  " + (var17 + 1) + "/" + this.flowState;
                  }

                  AiVisualization.load(var1, var15 ? "Parkour" : "Navigation", var2, var3x, (100 * var17 + var4x) / this.flowState);
               }
            }

            AiOperation.Result fill(String var1) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, var1, this.putAt_JsonObject_61().toString(), List.of());
            }

            AiOperation.Result putAt(AiOperation.Status var1, String var2) {
               if (var1 == AiOperation.Status.SUCCEEDED
                  && var18 != null
                  && this.readEx_FileStore_TaskStat != null
                  && this.readEx_I > var17
                  && !this.closeOf_Z
                  && var18.putAt_FileStore_TaskStat == null) {
                  var18.putAt_FileStore_TaskStat = this.readEx_FileStore_TaskStat;
                  var18.flowState = this.readEx_I;
                  var18.putAt_BlockPos = this.readEx_BlockPos;
                  var18.putAt_D = this.putAt_D;
                  var18.fill = this.chunkAt_Z;
                  var18.putAt_FileStore_RingUtil = this.fill_FileStore_RingUtil;
                  this.readEx_FileStore_TaskStat = null;
               }

               this.cancel();
               return new AiOperation.Result(var1, var2, this.putAt_JsonObject_61().toString(), List.of());
            }

            AiOperation.Result putAt(autismclient.ai.AiFailure var1, String var2) {
               this.cancel();
               return AiOperation.Result.failed(var1, var2, this.putAt_JsonObject_61().toString());
            }

            @Override
            public void cancel() {
               if (!this.flushOf_Z) {
                  this.flushOf_Z = true;
                  if (!AutismLiteVariant.enabled() && this.putAt_BufferCore != null) {
                     this.putAt_BufferCore.putAt_V();
                  }

                  if (this.putAt_JsonWorker_TableCa != null) {
                     this.putAt_JsonWorker_TableCa.close();
                  }

                  if (this.readEx_FileStore_TaskStat != null) {
                     this.readEx_FileStore_TaskStat.putAt_V();
                  }

                  this.state_V_3();
                  this.putAt_FileStore_TaskStat.putAt_V();
                  this.putAt_RingCache.state_V();
                  if (var14 && !AutismLiteVariant.enabled()) {
                     AiVisualization.init_V(this.fill_Obj);
                  }

                  if (this.putAt_Minecraft.player != null && this.putAt_Minecraft.player.getInventory().getSelectedSlot() != this.flowTemp) {
                     this.putAt_Minecraft.player.getInventory().setSelectedSlot(this.flowTemp);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Engine travel requires a connected living player");
      }
   }

   public record Inner_BufferHelper(boolean sprint, boolean bunnyhop, int maxFall, boolean avoidWater, boolean allowClimb, boolean allowDoors) {
      public static final AiGoto.Inner_BufferHelper DEFAULT = new AiGoto.Inner_BufferHelper(true, true, 3, false, true, true);
   }

   private interface Inner_DataUtil extends AiPathfinder.Inner_JsonUtil {
      AiPathfinder.Inner_JsonUtil putAt_FileStore_TokenCod();

      @Override
      default boolean init_Z_I(int x, int y, int z, AiPathfinder.Inner_StateCache planned) {
         return this.putAt_Z_I(x, y, z, planned);
      }

      @Override
      default AiPathfinder.Inner_JsonUtil init(AiPathfinder.Inner_StateCache grid) {
         return this.putAt_FileStore_TokenCod_FileStore_TextStor(grid);
      }

      @Override
      default boolean init_Z(BlockPos cell) {
         return this.putAt_Z_BlockPos(cell);
      }

      @Override
      default boolean read(BlockPos cell) {
         return this.readEx_Z_BlockPos(cell);
      }

      @Override
      default boolean init_Z() {
         return this.putAt_Z();
      }

      @Override
      default boolean load(BlockPos cell) {
         return this.fill_Z_BlockPos(cell);
      }

      @Override
      default double init_D() {
         return this.putAt_D();
      }

      @Override
      default Set<AiPathfinder.Inner_SyncManager> init_Set() {
         return this.putAt_Set();
      }

      @Override
      default BlockPos init_BlockPos() {
         return this.putAt_BlockPos();
      }
   }

   static final class Inner_StreamUtil implements AiPathfinder.Inner_JsonUtil {
      final BlockPos putAt_BlockPos;
      final List<BlockPos> putAt_List;
      final double putAt_D;
      final double fill_D;
      final AiPathfinder.Inner_StateCache putAt_FileStore_TextStor;
      final Set<Long> putAt_Set;
      final boolean putAt_Z;
      BlockPos fill_BlockPos;

      Inner_StreamUtil(BlockPos var1, double var2, double var4, AiPathfinder.Inner_StateCache var6, Set<Long> var7) {
         this(var1, var2, var4, var6, var7, false);
      }

      Inner_StreamUtil(BlockPos var1, double var2, double var4, AiPathfinder.Inner_StateCache var6, Set<Long> var7, boolean var8) {
         this(List.of(var1), var2, var4, var6, var7, var8);
      }

      Inner_StreamUtil(List<BlockPos> var1, double var2, double var4, AiPathfinder.Inner_StateCache var6, Set<Long> var7, boolean var8) {
         this.putAt_List = List.copyOf(var1);
         this.putAt_BlockPos = this.putAt_List.getFirst();
         this.putAt_D = var2;
         this.fill_D = var4;
         this.putAt_FileStore_TextStor = var6;
         this.putAt_Set = var7;
         this.putAt_Z = var8;
      }

      @Override
      public AiPathfinder.Inner_JsonUtil putAt_FileStore_TokenCod_FileStore_TextStor(AiPathfinder.Inner_StateCache var1) {
         return new AiGoto.Inner_StreamUtil(this.putAt_List, this.putAt_D, this.fill_D, var1, this.putAt_Set, this.putAt_Z);
      }

      @Override
      public boolean isInGoal(int var1, int var2, int var3) {
         return this.putAt_Z_I(var1, var2, var3, this.putAt_FileStore_TextStor);
      }

      @Override
      public boolean putAt_Z_I(int var1, int var2, int var3, AiPathfinder.Inner_StateCache var4) {
         if (var4 != null && !this.putAt_Set.contains(BlockPos.asLong(var1, var2, var3))) {
            AiPathfinder.Inner_FileCache var5 = var4.probe(var1, var2, var3);
            if (var5.loaded() && (this.putAt_Z || !var5.water()) && !var5.lava()) {
               for (BlockPos var7 : this.putAt_List) {
                  double var8 = var7.getX() - var1;
                  double var10 = var7.getY() + 0.8 - var2 - this.fill_D;
                  double var12 = var7.getZ() - var3;
                  if (var8 * var8 + var10 * var10 + var12 * var12 <= this.putAt_D * this.putAt_D
                     && InteractionGoal.load(var4, var7, var1, var2, var3, this.fill_D, var7.getX() + 0.5, var7.getY() + 0.8, var7.getZ() + 0.5, this.putAt_D)) {
                     return true;
                  }
               }

               return false;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      @Override
      public double heuristic(int var1, int var2, int var3) {
         double var4 = 1.0 / 0.0;

         for (BlockPos var7 : this.putAt_List) {
            var4 = Math.min(var4, AiPathfinder.Inner_CachePool.fill(var1 - var7.getX(), var7.getY() - var2, var3 - var7.getZ()));
         }

         return Math.max(0.0, var4 - this.putAt_D * AiPathfinder.Inner_CachePool.writeOf);
      }

      @Override
      public boolean putAt_Z_BlockPos(BlockPos var1) {
         return this.putAt_Z || !var1.equals(this.putAt_BlockPos);
      }

      @Override
      public boolean readEx_Z_BlockPos(BlockPos var1) {
         return !this.putAt_List.contains(var1);
      }

      @Override
      public AiPathfinder.Inner_JsonUtil init(AiPathfinder.Inner_StateCache grid) {
         return this.putAt_FileStore_TokenCod_FileStore_TextStor(grid);
      }

      @Override
      public boolean init_Z_I(int x, int y, int z, AiPathfinder.Inner_StateCache planned) {
         return this.putAt_Z_I(x, y, z, planned);
      }

      @Override
      public boolean init_Z(BlockPos cell) {
         return this.putAt_Z_BlockPos(cell);
      }

      @Override
      public boolean read(BlockPos cell) {
         return this.readEx_Z_BlockPos(cell);
      }

      boolean putAt(Minecraft var1) {
         if (var1.player != null
            && var1.level != null
            && !var1.player.isInLava()
            && (this.putAt_Z ? var1.player.onGround() || var1.player.isInWater() : var1.player.onGround() && !var1.player.isInWater())) {
            for (BlockPos var3 : this.putAt_List) {
               if (var1.level.hasChunkAt(var3) && var1.level.getFluidState(var3).isSource()) {
                  Vec3 var4 = new Vec3(var3.getX() + 0.5, var3.getY() + 0.8, var3.getZ() + 0.5);
                  if (!(var1.player.getEyePosition().distanceToSqr(var4) > this.putAt_D * this.putAt_D)) {
                     BlockHitResult var5 = var1.level
                        .clip(
                           new ClipContext(
                              var1.player.getEyePosition(), var4, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.SOURCE_ONLY, var1.player
                           )
                        );
                     if (var5.getType() == Type.BLOCK) {
                        FluidState var6 = var1.level.getFluidState(var5.getBlockPos());
                        if (var6.isSource() && var6.is(this.putAt_Z ? FluidTags.WATER : FluidTags.LAVA)) {
                           this.fill_BlockPos = var5.getBlockPos().immutable();
                           return true;
                        }
                     }
                  }
               }
            }

            return false;
         } else {
            return false;
         }
      }

      boolean prevEx(BlockPos var1) {
         return this.putAt_Set.size() < 16 && this.putAt_Set.add(var1.asLong());
      }
   }

   public static final class Inner_TableCache implements AutoCloseable {
      private final List<BlockPos> putAt_List;
      private final AiGoto.Inner_TableCache.Inner_CacheHelper[] putAt_HashEntry_TableCacArr;
      private boolean putAt_Z;
      private ScopedToggleOverride.Inner_StreamUtil putAt_JsonWorker_TableCa;
      private AiPathfinder.Inner_BufferHelper putAt_FileStore_TaskStat;
      private int flowState;
      private BlockPos putAt_BlockPos;
      private double putAt_D;
      private boolean fill;
      private AiPathfinder.Inner_NetHelper putAt_FileStore_RingUtil;

      public Inner_TableCache(List<BlockPos> var1) {
         this.putAt_List = List.copyOf(var1);
         this.putAt_HashEntry_TableCacArr = new AiGoto.Inner_TableCache.Inner_CacheHelper[var1.size()];
      }

      private AiGoto.Inner_TableCache.Inner_CacheHelper putAt(int var1, BlockPos var2, AiPathfinder.Inner_NetHelper var3, double var4, boolean var6) {
         AiGoto.Inner_TableCache.Inner_CacheHelper var7 = this.putAt_HashEntry_TableCacArr[var1];
         return var7 != null
               && var7.from().equals(var2)
               && var7.fallPolicy() == var3
               && var7.sprint() == var6
               && Math.abs(var7.movementSpeed() - var4) < 1.0E-6
            ? var7
            : null;
      }

      private AiGoto.Inner_TableCache.Inner_CacheHelper fill(int var1, BlockPos var2, AiPathfinder.Inner_NetHelper var3, double var4, boolean var6) {
         AiGoto.Inner_TableCache.Inner_CacheHelper var7 = this.putAt_HashEntry_TableCacArr[var1];
         if (var7 != null && !var7.from().equals(var2) && var7.segment().reachesGoal() && this.putAt(var1, var7.from(), var3, var4, var6) != null) {
            var var8 = var7.segment().movements();

            for (int var9 = 0; var9 < var8.size(); var9++) {
               if (((AiPathfinder.UiHelper)var8.get(var9)).from().equals(var2)) {
                  return new AiGoto.Inner_TableCache.Inner_CacheHelper(
                     var2,
                     new AiPathfinder.Inner_PacketUtil(List.copyOf(var8.subList(var9, var8.size())), true, 0, false, false),
                     var7.planningMs(),
                     var7.fallPolicy(),
                     var7.movementSpeed(),
                     var7.sprint()
                  );
               }
            }

            if (var2.getY() == var7.from().getY() && Math.abs(var2.getX() - var7.from().getX()) + Math.abs(var2.getZ() - var7.from().getZ()) == 1) {
               ArrayList var10 = new ArrayList(var8.size() + 1);
               var10.add(
                  new AiPathfinder.UiHelper(
                     var6 ? AiPathfinder.Inner_SyncManager.SPRINT : AiPathfinder.Inner_SyncManager.WALK,
                     var2,
                     var7.from(),
                     List.of(),
                     List.of(),
                     var6 ? 3.2 : 4.5,
                     -1.0,
                     null
                  )
               );
               var10.addAll(var8);
               return new AiGoto.Inner_TableCache.Inner_CacheHelper(
                  var2,
                  new AiPathfinder.Inner_PacketUtil(var10, true, 0, false, false),
                  var7.planningMs(),
                  var7.fallPolicy(),
                  var7.movementSpeed(),
                  var7.sprint()
               );
            } else {
               return null;
            }
         } else {
            return null;
         }
      }

      private void putAt(int var1, BlockPos var2, AiPathfinder.Inner_PacketUtil var3, double var4, AiPathfinder.Inner_NetHelper var6, double var7, boolean var9) {
         this.putAt_HashEntry_TableCacArr[var1] = new AiGoto.Inner_TableCache.Inner_CacheHelper(var2, var3, var4, var6, var7, var9);
      }

      public void putAt(boolean var1) {
         if (var1 && this.putAt_JsonWorker_TableCa == null) {
            Module var2 = al.init_Module_Str("no-fall");
            if (var2 != null && !var2.isEnabled()) {
               this.putAt_JsonWorker_TableCa = var2.borrowEnabled(true);
            }
         }

         this.putAt_Z = true;
      }

      @Override
      public void close() {
         if (this.putAt_JsonWorker_TableCa != null) {
            this.putAt_JsonWorker_TableCa.close();
            this.putAt_JsonWorker_TableCa = null;
         }

         if (this.putAt_FileStore_TaskStat != null) {
            this.putAt_FileStore_TaskStat.putAt_V();
            this.putAt_FileStore_TaskStat = null;
         }
      }

      private record Inner_CacheHelper(
         BlockPos from, AiPathfinder.Inner_PacketUtil segment, double planningMs, AiPathfinder.Inner_NetHelper fallPolicy, double movementSpeed, boolean sprint
      ) {
      }
   }

   public interface Inner_CachePool extends AiOperation {
      // 5.3 external ABI bridges
      default void init_V_ClassCache_JsonUti(AiPathfinder.Inner_JsonUtil goal) { this.putAt_V_FileStore_TokenCod(goal); }
      default void init_V_15() { this.putAt_V_41(); }
      default boolean init_Z_13() { return this.putAt_Z_30(); }
      default void load(boolean paused) { this.fill(paused); }
      default void init_V_Z(boolean hold) { this.putAt_V_Z_2(hold); }
      default void init(boolean hold, Predicate<BlockPos> keeps) { this.putAt(hold, keeps); }
      default BlockPos load_BlockPos_3() { return this.fill_BlockPos_9(); }
      default void init_V_BlockPos(BlockPos cell) { this.putAt_V_BlockPos_6(cell); }
      default int init_I_14() { return this.putAt_I_43(); }
      default int load_I_8() { return this.fill_I_18(); }
      default boolean load_Z_8() { return this.fill_Z_18(); }
      default BlockPos init_BlockPos_6() { return this.putAt_BlockPos_23(); }
      default Set<BlockPos> init_Set_4() { return this.putAt_Set_9(); }
      default Set<BlockPos> load_Set_2() { return this.fill_Set_4(); }
      default void init_V_Function(Function<BlockPos, Vec3> aim) { this.putAt_V_Function(aim); }
      default void init_V_I_2(int blocks) { this.putAt_V_I_6(blocks); }
      default void init_V_BiPredicate(BiPredicate<BlockState, ItemStack> accepted) { this.putAt_V_BiPredicate(accepted); }
      void putAt_V_FileStore_TokenCod(AiPathfinder.Inner_JsonUtil var1);

      default void putAt_V_41() {
      }

      boolean putAt_Z_30();

      void fill(boolean var1);

      default void putAt_V_Z_2(boolean var1) {
      }

      default void putAt(boolean var1, Predicate<BlockPos> var2) {
         this.putAt_V_Z_2(var1);
      }

      default BlockPos fill_BlockPos_9() {
         return null;
      }

      default void putAt_V_BlockPos_6(BlockPos var1) {
      }

      default int putAt_I_43() {
         return -1;
      }

      default int fill_I_18() {
         return -1;
      }

      default boolean fill_Z_18() {
         return true;
      }

      @Override
      default boolean canInterruptNow() {
         return this.fill_Z_18();
      }

      default BlockPos putAt_BlockPos_23() {
         return null;
      }

      default Set<BlockPos> putAt_Set_9() {
         return Set.of();
      }

      default Set<BlockPos> fill_Set_4() {
         return Set.of();
      }

      default void putAt_V_Function(Function<BlockPos, Vec3> var1) {
      }

      default void putAt_V_I_6(int var1) {
      }

      default void putAt_V_BiPredicate(BiPredicate<BlockState, ItemStack> var1) {
      }
   }

   // 5.3 external ABI bridges for untouched top-level ai/* and MacroExecutor callers
   public static int init_I(BlockPos feet) { return flowState(feet); }
   public static BlockPos init_BlockPos(BlockPos target, int range) { return putAt_BlockPos(target, range); }
   public static boolean init_Z(BlockPos cell) { return putAt_Z(cell); }
   public static AiGoto.Inner_CachePool init_HashUtil_CachePool_BlockPos(BlockPos anchor, AiPathfinder.Inner_JsonUtil goal, int timeoutSeconds, AiGoto.Inner_BufferHelper options) {
      return putAt_HashEntry_TaskStat_BlockPos(anchor, goal, timeoutSeconds, options);
   }
   public static void init_V_HashUtil_CachePool(AiGoto.Inner_CachePool travel, Function<Predicate<AABB>, Vec3> look, Runnable click) {
      putAt_V_HashEntry_TaskStat(travel, look, click);
   }
   public static AiGoto.Inner_CachePool init_HashUtil_CachePool_BlockPos(BlockPos anchor, AiPathfinder.Inner_JsonUtil goal, int timeoutSeconds) {
      return putAt_HashEntry_TaskStat_BlockPos(anchor, goal, timeoutSeconds);
   }
   public static AiOperation init(JsonObject args) { return putAt(args); }
   public static AiOperation init_AiOperation_BlockPos(BlockPos target, double radius, boolean allowDig, boolean allowPillar, boolean allowBridge, int timeoutSeconds) {
      return putAt_AiOperation_BlockPos(target, radius, allowDig, allowPillar, allowBridge, timeoutSeconds);
   }
   public static AiOperation init_AiOperation(BlockPos target, int timeoutSeconds) { return putAt_AiOperation(target, timeoutSeconds); }
   public static AiOperation init_AiOperation_BlockPos(BlockPos target, int timeoutSeconds, Set<Long> protectedCells) {
      return putAt_AiOperation_BlockPos(target, timeoutSeconds, protectedCells);
   }
   public static AiOperation load(BlockPos source, int seconds) { return fill(source, seconds); }
   public static AiOperation load(BlockPos source, int seconds, Set<Long> protectedCells) { return fill(source, seconds, protectedCells); }
   public static AiOperation init_AiOperation_BlockPos_2(BlockPos source, int seconds, Set<Long> protectedCells, boolean supportedAccess) {
      return putAt_AiOperation_BlockPos_2(source, seconds, protectedCells, supportedAccess);
   }
   public static AiOperation init_AiOperation_BlockPos_3(BlockPos source, int seconds, Set<Long> protectedCells, boolean supportedAccess, Set<Long> forbiddenCells) {
      return putAt_AiOperation_BlockPos_3(source, seconds, protectedCells, supportedAccess, forbiddenCells);
   }
   public static AiOperation init_AiOperation_BlockPos(BlockPos target, double radius, int seconds, Set<Long> protectedCells) {
      return putAt_AiOperation_BlockPos(target, radius, seconds, protectedCells);
   }
   public static AiOperation init_AiOperation_BlockPos(BlockPos target, double radius, int seconds, Set<Long> protectedCells, Set<Long> forbiddenCells) {
      return putAt_AiOperation_BlockPos(target, radius, seconds, protectedCells, forbiddenCells);
   }
   public static AiPathfinder.Inner_JsonUtil init(final AiPathfinder.Inner_JsonUtil delegate, Set<Long> cells) { return putAt(delegate, cells); }
   public static AiOperation init(List<BlockPos> sources, int seconds) { return putAt(sources, seconds); }
   public static AiOperation load(BlockPos target, int timeoutSeconds, Set<Long> protectedCells, boolean allowScaffold) {
      return fill(target, timeoutSeconds, protectedCells, allowScaffold);
   }
   public static AiOperation init_AiOperation_BlockPos_4(BlockPos target, int timeoutSeconds, Set<Long> protectedCells, boolean allowScaffold, Direction requiredFace) {
      return putAt_AiOperation_BlockPos_4(target, timeoutSeconds, protectedCells, allowScaffold, requiredFace);
   }
   public static AiOperation init_AiOperation_BlockPos_3(BlockPos target, int timeoutSeconds, Set<Long> protectedCells, boolean allowScaffold, Direction requiredFace, double slack) {
      return putAt_AiOperation_BlockPos_3(target, timeoutSeconds, protectedCells, allowScaffold, requiredFace, slack);
   }
   public static AiOperation read(BlockPos target, int timeoutSeconds, Set<Long> protectedCells, boolean allowScaffold) {
      return readEx(target, timeoutSeconds, protectedCells, allowScaffold);
   }
   public static AiOperation init_AiOperation_BlockPos_2(BlockPos target, int timeoutSeconds, Set<Long> protectedCells, boolean allowScaffold, double slack) {
      return putAt_AiOperation_BlockPos_2(target, timeoutSeconds, protectedCells, allowScaffold, slack);
   }
   public static AiOperation init_AiOperation_BlockPos_2(BlockPos target, int timeoutSeconds, Set<Long> protectedCells, boolean allowScaffold, double slack, boolean dry) {
      return putAt_AiOperation_BlockPos_2(target, timeoutSeconds, protectedCells, allowScaffold, slack, dry);
   }
   public static AiOperation init_AiOperation_BlockPos_2(BlockPos target, Item item, int timeoutSeconds) { return putAt_AiOperation_BlockPos_2(target, item, timeoutSeconds); }
   public static AiOperation init_AiOperation_BlockPos_3(BlockPos target, Item item, int timeoutSeconds, AABB workVolume) { return putAt_AiOperation_BlockPos_3(target, item, timeoutSeconds, workVolume); }
   public static AiOperation init_AiOperation_BlockPos_6(BlockPos target, Item item, int timeoutSeconds, AABB workVolume, boolean walkOnly) { return putAt_AiOperation_BlockPos_6(target, item, timeoutSeconds, workVolume, walkOnly); }
   public static AiOperation init(BlockPos target, Item item, int timeoutSeconds, AABB workVolume, boolean walkOnly, Set<Long> keep, Set<Long> noStand) {
      return putAt(target, item, timeoutSeconds, workVolume, walkOnly, keep, noStand);
   }
   public static AiOperation init_AiOperation_BlockPos_5(BlockPos target, Item item, int timeoutSeconds, AABB workVolume, boolean walkOnly, Set<Long> keep) {
      return putAt_AiOperation_BlockPos_5(target, item, timeoutSeconds, workVolume, walkOnly, keep);
   }
   public static AiOperation load(BlockPos target, Item item, int timeoutSeconds, AABB workVolume) { return fill(target, item, timeoutSeconds, workVolume); }
   public static AiOperation init_AiOperation_BlockPos_5(BlockPos target, Item item, int timeoutSeconds, AABB volume, Set<Long> cells) {
      return putAt_AiOperation_BlockPos_5(target, item, timeoutSeconds, volume, cells);
   }
   public static AiPathfinder.Inner_JsonUtil read(AiPathfinder.Inner_JsonUtil delegate, Set<Long> cells) { return readEx(delegate, cells); }
   public static AiPathfinder.Inner_JsonUtil init_ClassCache_JsonUti_ClassCache_JsonUti(final AiPathfinder.Inner_JsonUtil delegate, final Set<Long> cells, final int minimumDigY) {
      return putAt_FileStore_TokenCod_FileStore_TokenCod(delegate, cells, minimumDigY);
   }
   public static AiOperation init_AiOperation_BlockPos_4(BlockPos target, Item item, int timeoutSeconds, AABB volume, Set<Long> cells, PlacementGoal.Inner_DataUtil constraint) {
      return putAt_AiOperation_BlockPos_4(target, item, timeoutSeconds, volume, cells, constraint);
   }
   public static AiOperation init_AiOperation_List(List<PlacementGoal.Inner_CachePool> batch, int timeoutSeconds, AABB volume, Set<Long> cells) {
      return putAt_AiOperation_List(batch, timeoutSeconds, volume, cells);
   }
   public static AiOperation init_AiOperation_List(List<PlacementGoal.Inner_CachePool> batch, int timeoutSeconds, AABB volume, Set<Long> cells, Set<Long> avoid) {
      return putAt_AiOperation_List(batch, timeoutSeconds, volume, cells, avoid);
   }
   public static AiOperation init(BlockPos target, double radius, boolean allowDig, boolean allowPillar, boolean allowBridge, int timeoutSeconds, AiPathfinder.Inner_IoWorker strategy) {
      return putAt(target, radius, allowDig, allowPillar, allowBridge, timeoutSeconds, strategy);
   }
   public static AiOperation init(BlockPos target, double radius, boolean allowDig, boolean allowPillar, boolean allowBridge, int timeoutSeconds, AiPathfinder.Inner_IoWorker strategy, boolean allowParkour) {
      return putAt(target, radius, allowDig, allowPillar, allowBridge, timeoutSeconds, strategy, allowParkour);
   }
   public static AiOperation init(BlockPos target, double radius, boolean allowDig, boolean allowPillar, boolean allowBridge, int timeoutSeconds, AiPathfinder.Inner_IoWorker strategy, boolean allowParkour, double breakReluctance) {
      return putAt(target, radius, allowDig, allowPillar, allowBridge, timeoutSeconds, strategy, allowParkour, breakReluctance);
   }
   public static AiOperation init(BlockPos target, double radius, boolean allowDig, boolean allowPillar, boolean allowBridge, int timeoutSeconds, AiPathfinder.Inner_IoWorker strategy, boolean allowParkour, double breakReluctance, AiPathfinder.Inner_JsonUtil customGoal) {
      return putAt(target, radius, allowDig, allowPillar, allowBridge, timeoutSeconds, strategy, allowParkour, breakReluctance, customGoal);
   }
   public static AiOperation init(BlockPos target, double radius, boolean allowDig, boolean allowPillar, boolean allowBridge, int timeoutSeconds, AiPathfinder.Inner_IoWorker strategy, boolean allowParkour, double breakReluctance, AiPathfinder.Inner_JsonUtil customGoal, AiGoto.Inner_BufferHelper options) {
      return putAt(target, radius, allowDig, allowPillar, allowBridge, timeoutSeconds, strategy, allowParkour, breakReluctance, customGoal, options);
   }
   public static AiGoto.Inner_CachePool init(BlockPos target, double radius, boolean allowDig, boolean allowPillar, boolean allowBridge, int timeoutSeconds, AiPathfinder.Inner_IoWorker strategy, boolean allowParkour, double breakReluctance, AiPathfinder.Inner_JsonUtil customGoal, AiGoto.Inner_BufferHelper options, boolean persistent) {
      return putAt(target, radius, allowDig, allowPillar, allowBridge, timeoutSeconds, strategy, allowParkour, breakReluctance, customGoal, options, persistent);
   }
}
