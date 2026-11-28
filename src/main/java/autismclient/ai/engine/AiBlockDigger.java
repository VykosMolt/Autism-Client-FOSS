package autismclient.ai.engine;

import autismclient.compat263.Compat263;

import autismclient.AutismClientAddon;
import autismclient.ai.AiVisualization;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.util.AutismLiteVariant;
import autismclient.util.BlockAttackGraceWindow;
import autismclient.util.RotationMath;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AiBlockDigger {
   private static final boolean DEBUG_NAV = Boolean.getBoolean("autism.navigation.debug");
   private static final boolean TRACE_NAV = Boolean.getBoolean("autism.navigation.trace");
   private static final double FALLING_BLOCK_WAIT_DIST_SQ = 9.0;
   static final int MAX_AIM_STALL_TICKS = 10;
   static final double MIN_PRE_TURN_STEP_DEG = 0.3;
   static final double MAX_PRE_TURN_STEP_DEG = 6.0;
   public static final String LOW_AIR_FAILURE_REASON = "too little air to break it under water";
   public static final int AIR_TICKS_PER_WATER_BLOCK = 6;
   public static final int AIR_SAFETY_BUFFER_TICKS = 40;
   public static final int MAX_WATER_SURFACE_SCAN = 24;
   private static int lastAttackTick = -2147483648;
   private final Minecraft minecraft;
   private final AiRestock restock;
   private final boolean allowBlockEntities;
   private final boolean requireDrops;
   private final IntConsumer slotSelector;
   private final Consumer<Vec3> aimPointConsumer;
   private final Runnable aimTickCallback;
   private final BooleanSupplier smoothAimEnabled;
   private BlockPos activeTarget;
   private BlockPos aimingTarget;
   private BlockState activeTargetState;
   private int breakElapsedTicks;
   private int breakTimeoutTicks;
   private int aimStallTicks;
   private float lastAimYaw;
   private float lastAimPitch;
   private boolean activelyDigging;
   private String lastFailureReason = "";
   private BiPredicate<BlockState, ItemStack> acceptedTool = (var0, var1x) -> true;
   private Set<BlockPos> sealedWaterCells = Set.of();
   private Consumer<RotationMath.Rotation> rotationConsumer;
   private Vec3 nextLookDirection;
   private float expectedBreakTicks;
   private Vec3 cachedAimRayDir;
   private BlockPos cachedAimTarget;
   private Vec3 cachedAimPoint;
   private Vec3 preferredAimPoint;
   private BlockPos lastStoppedTarget;
   private BlockState lastStoppedState;

   public AiBlockDigger(Minecraft var1, IntConsumer var2) {
      this(var1, var2, var0 -> {});
   }

   public AiBlockDigger(Minecraft var1, IntConsumer var2, Consumer<Vec3> var3) {
      this(var1, var2, var3, false);
   }

   public AiBlockDigger(Minecraft var1, IntConsumer var2, Consumer<Vec3> var3, boolean var4) {
      this(var1, var2, var3, var4, var4);
   }

   public AiBlockDigger(Minecraft var1, IntConsumer var2, Consumer<Vec3> var3, boolean var4, boolean var5) {
      this(var1, var2, var3, var4, var5, () -> {});
   }

   public AiBlockDigger(Minecraft var1, IntConsumer var2, Consumer<Vec3> var3, boolean var4, boolean var5, Runnable var6) {
      this(var1, var2, var3, var4, var5, var6, () -> false);
   }

   public AiBlockDigger(Minecraft var1, IntConsumer var2, Consumer<Vec3> var3, boolean var4, boolean var5, Runnable var6, BooleanSupplier var7) {
      this.smoothAimEnabled = var7;
      this.aimTickCallback = var6;
      this.requireDrops = var5;
      this.minecraft = var1;
      this.restock = new AiRestock(var1);
      this.allowBlockEntities = var4;
      this.slotSelector = var2;
      this.aimPointConsumer = var3;
   }

   public String lastFailureReason() {
      return this.lastFailureReason;
   }

   private AiBlockDigger.DigState failWithReason(String var1) {
      this.lastFailureReason = var1;
      return AiBlockDigger.DigState.readEx;
   }

   public AiBlockDigger withRotationConsumer(Consumer<RotationMath.Rotation> var1) {
      this.rotationConsumer = var1;
      return this;
   }

   public AiBlockDigger withAcceptedTool(BiPredicate<BlockState, ItemStack> var1) {
      this.acceptedTool = var1;
      return this;
   }

   public AiBlockDigger withSealedWater(Set<BlockPos> var1) {
      this.sealedWaterCells = Set.copyOf(var1);
      return this;
   }

   public AiBlockDigger.DigState dig(BlockPos var1) {
      return this.dig(var1, null);
   }

   public AiBlockDigger.DigState dig(BlockPos var1, Vec3 var2) {
      return this.dig(var1, var2, null);
   }

   public AiBlockDigger.DigState dig(BlockPos var1, Vec3 var2, Vec3 var3) {
      if (this.activelyDigging && !var1.equals(this.activeTarget)) {
         this.stop();
      }

      if (!AutismLiteVariant.enabled()) {
         AiVisualization.init(this, var1);
      }

      this.nextLookDirection = var3;
      this.preferredAimPoint = var2;
      if (this.minecraft.player != null
         && this.minecraft.level != null
         && this.minecraft.gameMode != null
         && this.minecraft.level.getChunkSource().getChunk(var1.getX() >> 4, var1.getZ() >> 4, ChunkStatus.FULL, false) != null) {
         if (this.restock.isRunning()) {
            AiRestock.RestockState var4 = this.restock.tick();
            if (var4 == AiRestock.RestockState.FAILED) {
               this.stop();
               return this.failWithReason("tool swap failed");
            }

            if (var4 != AiRestock.RestockState.SUCCEEDED) {
               return AiBlockDigger.DigState.putAt_JsonCache_CacheHel;
            }

            this.restock.reset();
         }

         if (this.activelyDigging && var1.equals(this.activeTarget)) {
            if (++this.breakElapsedTicks > this.breakTimeoutTicks) {
               this.stop();
               return this.failWithReason("took longer than " + this.breakTimeoutTicks + " ticks");
            }

            BlockState var14 = this.minecraft.level.getBlockState(this.activeTarget);
            if (var14.getBlock() != this.activeTargetState.getBlock() && isDirtVariant(var14) && isDirtVariant(this.activeTargetState)) {
               this.activeTargetState = var14;
            }

            if (var14.getBlock() != this.activeTargetState.getBlock()) {
               if (!var14.isAir() && var14.getFluidState().isEmpty()) {
                  this.stop();
                  return this.failWithReason("turned into " + var14.getBlock().getName().getString());
               } else {
                  AiEditLedger.init(this.activeTarget, this.activeTargetState);
                  this.stop();
                  return AiBlockDigger.DigState.fill;
               }
            } else {
               if (var14.getDestroySpeed(this.minecraft.level, this.activeTarget) < 0.0F
                  || var14.hasBlockEntity() && !this.allowBlockEntities
                  || !var14.getFluidState().isEmpty()) {
                  this.stop();
                  return this.failWithReason("became unbreakable or fluid");
               }

               if (!AiPathfinder.LiveGridView.isSafeToDig(this.minecraft.level, this.activeTarget, this.sealedWaterCells)) {
                  this.stop();
                  return this.failWithReason("fluid appeared next to it");
               }

               if (!this.isSafeToBreakSupport(this.activeTarget)) {
                  this.stop();
                  return this.failWithReason("removing this floor would cause an unsafe fall");
               }

               if (this.hasNearbyFallingEntity(this.activeTarget)) {
                  return AiBlockDigger.DigState.putAt_JsonCache_CacheHel;
               }

               BlockHitResult var15 = AiBlockInteraction.findReachableHit(this.minecraft, this.activeTarget);
               if (var15 == null) {
                  if (DEBUG_NAV) {
                     AiNavDebug.log(
                        "digger lost "
                           + this.activeTarget
                           + " eye="
                           + this.minecraft.player.getEyePosition()
                           + " body="
                           + this.minecraft.player.position()
                           + " motion="
                           + this.minecraft.player.getDeltaMovement()
                           + " pose="
                           + this.minecraft.player.getPose()
                           + " climb="
                           + this.minecraft.player.onClimbable()
                           + " shift="
                           + this.minecraft.options.keyShift.isDown()
                     );
                  }

                  this.stop();
                  return this.failWithReason("lost sight of it while breaking");
               } else {
                  int var17 = MiningCost.bestToolSlot(this.minecraft, var14, this.requireDrops, this.acceptedTool);
                  if (var17 < 0) {
                     this.stop();
                     return this.failWithReason("no tool left that gets its drops");
                  }

                  if (var17 >= 9) {
                     String var19 = BuiltInRegistries.ITEM.getKey(this.minecraft.player.getInventory().getItem(var17).getItem()).toString();
                     this.stop();
                     this.restock.startSwap(var17, this.minecraft.player.getInventory().getSelectedSlot(), var19);
                     return AiBlockDigger.DigState.putAt_JsonCache_CacheHel;
                  }

                  this.slotSelector.accept(var17);
                  Vec3 var18 = this.resolveAimPoint(this.activeTarget, var15);
                  RotationMath.Rotation var20 = this.computePreTurnRotation();
                  if (var20 != null) {
                     this.rotationConsumer.accept(var20);
                  } else {
                     this.aimPointConsumer.accept(var18);
                  }

                  this.aimTickCallback.run();
                  AiMotionAudit.init(this.minecraft);
                  if (!AiAim.isLookingAtBlock(this.activeTarget)) {
                     tickDestroyDelay(this.minecraft);
                     float var21 = this.minecraft.player.getYRot();
                     float var10 = this.minecraft.player.getXRot();
                     boolean var11 = Math.abs(Mth.wrapDegrees(var21 - this.lastAimYaw)) + Math.abs(var10 - this.lastAimPitch) > 0.5F;
                     this.lastAimYaw = var21;
                     this.lastAimPitch = var10;
                     if (TRACE_NAV && !var11) {
                        BlockHitResult var12 = AiAim.raycastLookBlock();
                        AiNavDebug.log(
                           "digger off "
                              + this.activeTarget
                              + " ray "
                              + (var12 == null ? "-" : var12.getBlockPos() + " " + var12.getDirection())
                              + " yaw="
                              + var21
                              + " pitch="
                              + var10
                              + " eye="
                              + this.minecraft.player.getEyePosition()
                        );
                     }

                     if (!var11 && ++this.aimStallTicks > 10) {
                        this.stop();
                        return this.failWithReason("crosshair left it mid-break");
                     } else {
                        return AiBlockDigger.DigState.putAt_JsonCache_CacheHel;
                     }
                  } else {
                     this.aimStallTicks = 0;
                     this.continueBreakingBlock(this.activeTarget, var15.getDirection());
                     return this.checkBreakComplete();
                  }
               }
            }
         } else {
            if (this.activelyDigging) {
               this.stop();
            }

            if (!var1.equals(this.aimingTarget)) {
               this.aimingTarget = var1.immutable();
               this.aimStallTicks = 0;
            }

            BlockState var13 = this.minecraft.level.getBlockState(var1);
            if (var13.isAir()) {
               if (var1.equals(this.lastStoppedTarget) && this.lastStoppedState != null) {
                  AiEditLedger.init(var1, this.lastStoppedState);
               }

               this.lastStoppedTarget = null;
               return AiBlockDigger.DigState.fill;
            } else if (!(var13.getDestroySpeed(this.minecraft.level, var1) < 0.0F)
               && (!var13.hasBlockEntity() || this.allowBlockEntities)
               && var13.getFluidState().isEmpty()) {
               if (!AiPathfinder.LiveGridView.isSafeToDig(this.minecraft.level, var1, this.sealedWaterCells)) {
                  if (DEBUG_NAV) {
                     AiNavDebug.log("digger fail digSafe " + var1);
                  }

                  return this.failWithReason("unsafe to dig (fluid next to it)");
               } else {
                  if (AiPathfinder.LiveGridView.isLitPortalFrame(this.minecraft.level, var1)) {
                     return this.failWithReason("a lit portal's frame");
                  }

                  if (!this.isSafeToBreakSupport(var1)) {
                     return this.failWithReason("removing this floor would cause an unsafe fall");
                  }

                  if (var1.equals(this.minecraft.player.blockPosition().above(2))
                     && this.minecraft.level.getBlockState(var1.above()).getBlock() instanceof FallingBlock) {
                     if (DEBUG_NAV) {
                        AiNavDebug.log("digger fail falling over head " + var1);
                     }

                     return this.failWithReason("sand or gravel over it");
                  } else {
                     int var5 = MiningCost.bestToolSlot(this.minecraft, var13, this.requireDrops, this.acceptedTool);
                     if (var5 < 0) {
                        if (DEBUG_NAV) {
                           AiNavDebug.log("digger fail no-tool " + var1);
                        }

                        AutismClientAddon.LOG.info(
                              "[AI] digger: no tool that gets the drops of {} at {}",
                              BuiltInRegistries.BLOCK.getKey(var13.getBlock()).getPath(),
                              var1.toShortString()
                           );
                        return this.failWithReason("no tool that gets its drops");
                     } else {
                        if (var5 >= 9) {
                           String var16 = BuiltInRegistries.ITEM.getKey(this.minecraft.player.getInventory().getItem(var5).getItem()).toString();
                           this.restock.startSwap(var5, this.minecraft.player.getInventory().getSelectedSlot(), var16);
                           return AiBlockDigger.DigState.putAt_JsonCache_CacheHel;
                        }

                        if (hasInsufficientUnderwaterAir(
                           this.minecraft,
                           MiningCost.breakTicks(
                              this.minecraft,
                              this.minecraft.player.getInventory().getItem(var5),
                              var13,
                              var13.getDestroySpeed(this.minecraft.level, var1)
                           )
                        )) {
                           return this.failWithReason("too little air to break it under water");
                        }

                        BlockHitResult var6 = AiBlockInteraction.findReachableHit(this.minecraft, var1);
                        if (var6 == null) {
                           if (DEBUG_NAV) {
                              AiNavDebug.log("digger fail no-hit " + var1 + " eye=" + this.minecraft.player.getEyePosition());
                           }

                           return this.failWithReason("no clickable face in reach");
                        } else {
                           if (this.isWaitingDestroyDelay()) {
                              this.preAimDuringDelay(var1, var6);
                              tickDestroyDelay(this.minecraft);
                              return AiBlockDigger.DigState.putAt_JsonCache_CacheHel;
                           }

                           this.aimPointConsumer.accept(this.resolveAimPoint(var1, var6));
                           this.aimTickCallback.run();
                           AiMotionAudit.init(this.minecraft);
                           if (!AiAim.isLookingAtBlock(var1)) {
                              tickDestroyDelay(this.minecraft);
                              float var7 = this.minecraft.player.getYRot();
                              float var8 = this.minecraft.player.getXRot();
                              boolean var9 = Math.abs(Mth.wrapDegrees(var7 - this.lastAimYaw)) + Math.abs(var8 - this.lastAimPitch) > 0.5F;
                              this.lastAimYaw = var7;
                              this.lastAimPitch = var8;
                              if (var9) {
                                 return AiBlockDigger.DigState.putAt_JsonCache_CacheHel;
                              }

                              if (this.minecraft.player.getDeltaMovement().lengthSqr() > 0.008
                                 && !this.minecraft.player.isInWater()
                                 && !this.minecraft.player.onClimbable()) {
                                 return AiBlockDigger.DigState.putAt_JsonCache_CacheHel;
                              }

                              if (++this.aimStallTicks > 10) {
                                 this.aimStallTicks = 0;
                                 if (DEBUG_NAV) {
                                    AiNavDebug.log("digger fail aim " + var1);
                                 }

                                 return this.failWithReason("crosshair never landed on it");
                              } else {
                                 if (this.aimStallTicks > 2) {
                                    this.aimPointConsumer.accept(var6.getLocation());
                                 }

                                 return AiBlockDigger.DigState.putAt_JsonCache_CacheHel;
                              }
                           } else {
                              this.aimStallTicks = 0;
                              this.beginBreaking(var1, var6, var5);
                              return this.checkBreakComplete();
                           }
                        }
                     }
                  }
               }
            } else {
               if (DEBUG_NAV) {
                  AiNavDebug.log("digger fail unbreakable/fluid " + var1);
               }

               return this.failWithReason("unbreakable or fluid");
            }
         }
      } else {
         this.stop();
         return this.failWithReason("chunk not loaded");
      }
   }

   static boolean isDirtVariant(BlockState var0) {
      return var0.is(Blocks.DIRT)
         || var0.is(Blocks.GRASS_BLOCK)
         || var0.is(Blocks.MYCELIUM)
         || var0.is(Blocks.PODZOL)
         || var0.is(Blocks.DIRT_PATH)
         || var0.is(Blocks.COARSE_DIRT)
         || var0.is(Blocks.ROOTED_DIRT);
   }

   private boolean isSafeToBreakSupport(BlockPos var1) {
      return AiMotionSafety.isSafeToBreakSupport(var1);
   }

   private AiBlockDigger.DigState checkBreakComplete() {
      BlockState var1 = this.minecraft.level.getBlockState(this.activeTarget);
      if (var1.getBlock() == this.activeTargetState.getBlock() || !var1.isAir() && var1.getFluidState().isEmpty()) {
         return AiBlockDigger.DigState.putAt_JsonCache_CacheHel;
      }

      AiEditLedger.init(this.activeTarget, this.activeTargetState);
      this.stop();
      return AiBlockDigger.DigState.fill;
   }

   public static boolean hasInsufficientUnderwaterAir(Minecraft var0, double var1) {
      LocalPlayer var3 = var0.player;
      return var3 != null
            && var0.level != null
            && var3.isEyeInFluid(FluidTags.WATER)
            && !var3.hasEffect(MobEffects.WATER_BREATHING)
            && !var3.hasEffect(MobEffects.CONDUIT_POWER)
         ? var3.getAirSupply() < var1 + airTicksToSurface(var0) + 40.0
         : false;
   }

   public static int airTicksToSurface(Minecraft var0) {
      BlockPos var1 = BlockPos.containing(var0.player.getEyePosition());

      for (int var2 = 1; var2 <= 24; var2++) {
         var1 = var1.above();
         BlockState var3 = var0.level.getBlockState(var1);
         if (!var3.getFluidState().is(FluidTags.WATER)) {
            return var3.getCollisionShape(var0.level, var1).isEmpty() ? var2 * 6 : 144;
         }
      }

      return 144;
   }

   static boolean shouldPreAimDuringDelay(int var0, boolean var1, boolean var2) {
      return var0 > 0 && var1 && var2;
   }

   boolean isWaitingDestroyDelay() {
      if (!this.activelyDigging
         && this.minecraft.player != null
         && this.minecraft.level != null
         && this.smoothAimEnabled.getAsBoolean()
         && this.minecraft.gameMode instanceof AutismMultiPlayerGameModeAccessor var1) {
         BlockHitResult var3 = AiAim.raycastLookBlock();
         return shouldPreAimDuringDelay(var1.autism$getDestroyDelay(), true, var3 != null && !this.minecraft.level.getBlockState(var3.getBlockPos()).isAir());
      } else {
         return false;
      }
   }

   private static boolean wasAttackTickAlreadyConsumed(Minecraft var0) {
      if (lastAttackTick == var0.player.tickCount) {
         return true;
      }

      lastAttackTick = var0.player.tickCount;
      BlockAttackGraceWindow.init(var0.player);
      return false;
   }

   private void continueBreakingBlock(BlockPos var1, Direction var2) {
      if (!wasAttackTickAlreadyConsumed(this.minecraft)) {
         BlockHitResult var3 = AiAim.raycastLookBlock();
         Direction var4 = var3 != null && var3.getBlockPos().equals(var1) ? var3.getDirection() : var2;
         if (this.minecraft.gameMode.continueDestroyBlock(var1, var4)) {
            Compat263.addBreakingBlockEffect(this.minecraft.level, var1, var4);
            Compat263.swing(this.minecraft.player, InteractionHand.MAIN_HAND);
         }
      }
   }

   public static boolean continueBreakingBlock(Minecraft var0, BlockPos var1, Direction var2) {
      if (var0.player != null && var0.level != null && var0.gameMode != null && !wasAttackTickAlreadyConsumed(var0)) {
         BlockHitResult var3 = AiAim.raycastLookBlock();
         Direction var4 = var3 != null && var3.getBlockPos().equals(var1) ? var3.getDirection() : var2;
         if (!var0.gameMode.continueDestroyBlock(var1, var4)) {
            return false;
         }

         Compat263.addBreakingBlockEffect(var0.level, var1, var4);
         Compat263.swing(var0.player, InteractionHand.MAIN_HAND);
         return true;
      } else {
         return false;
      }
   }

   public static void tickDestroyDelay(Minecraft var0) {
      if (var0.player != null && var0.level != null && var0.gameMode instanceof AutismMultiPlayerGameModeAccessor var1 && var1.autism$getDestroyDelay() > 0) {
         BlockHitResult var3 = AiAim.raycastLookBlock();
         if (var3 != null && !var0.level.getBlockState(var3.getBlockPos()).isAir() && !wasAttackTickAlreadyConsumed(var0)) {
            if (var0.gameMode.continueDestroyBlock(var3.getBlockPos(), var3.getDirection())) {
               Compat263.swing(var0.player, InteractionHand.MAIN_HAND);
            }
         }
      }
   }

   static boolean hasFallingBlockNear(BlockPos var0, List<Vec3> var1) {
      Vec3 var2 = Vec3.atCenterOf(var0);

      for (Vec3 var4 : var1) {
         if (var4.distanceToSqr(var2) <= 9.0) {
            return true;
         }
      }

      return false;
   }

   private boolean hasNearbyFallingEntity(BlockPos var1) {
      ArrayList var2 = null;

      for (Entity var4 : this.minecraft.level.entitiesForRendering()) {
         if (var4 instanceof FallingBlockEntity) {
            if (var2 == null) {
               var2 = new ArrayList();
            }

            var2.add(var4.position());
         }
      }

      return var2 != null && hasFallingBlockNear(var1, var2);
   }

   private void beginBreaking(BlockPos var1, BlockHitResult var2, int var3) {
      this.activeTarget = var1.immutable();
      this.activeTargetState = this.minecraft.level.getBlockState(var1);
      if (var3 >= 0) {
         this.slotSelector.accept(var3);
      }

      this.aimPointConsumer.accept(this.resolveAimPoint(var1, var2));
      this.continueBreakingBlock(this.activeTarget, var2.getDirection());
      this.breakElapsedTicks = 0;
      this.expectedBreakTicks = (float)MiningCost.breakTicks(
         this.minecraft,
         this.minecraft.player.getMainHandItem(),
         this.activeTargetState,
         this.activeTargetState.getDestroySpeed(this.minecraft.level, this.activeTarget)
      );
      this.breakTimeoutTicks = (int)Math.min(72000.0, Math.ceil(this.expectedBreakTicks * 4.0F) + 40.0);
      this.activelyDigging = true;
   }

   private Vec3 resolveAimPoint(BlockPos var1, BlockHitResult var2) {
      Vec3 var3 = this.minecraft.player.getEyePosition();
      if (this.activelyDigging && var1.equals(this.activeTarget)) {
         BlockHitResult var4 = AiAim.raycastLookBlock();
         if (var4 != null && var4.getBlockPos().equals(var1)) {
            this.cachedAimTarget = var1.immutable();
            this.cachedAimRayDir = this.minecraft.player.getLookAngle();
            this.cachedAimPoint = null;
            return var4.getLocation().add(this.cachedAimRayDir.scale(0.02));
         }
      }

      if (!this.activelyDigging && this.rotationConsumer != null && this.preferredAimPoint != null && this.canRaycastAimPoint(var1, this.preferredAimPoint)) {
         this.cachedAimTarget = var1.immutable();
         this.cachedAimRayDir = this.preferredAimPoint.subtract(var3).normalize();
         this.cachedAimPoint = null;
         return this.preferredAimPoint;
      }

      if (var1.equals(this.cachedAimTarget) && this.cachedAimRayDir != null) {
         Vec3 var6 = this.reprojectCachedAimRay(var1, var3);
         if (var6 != null) {
            return var6;
         }
      }

      if (var1.equals(this.cachedAimTarget) && this.cachedAimPoint != null && this.canRaycastAimPoint(var1, this.cachedAimPoint)) {
         return this.cachedAimPoint;
      } else if (this.preferredAimPoint != null && this.canRaycastAimPoint(var1, this.preferredAimPoint)) {
         this.cachedAimTarget = var1.immutable();
         this.cachedAimRayDir = this.preferredAimPoint.subtract(var3).normalize();
         this.cachedAimPoint = null;
         return this.preferredAimPoint;
      } else {
         Vec3 var7 = faceCenterPoint(var1, var2);
         Vec3 var5 = AiBlockInteraction.findVisibleEntryPoint(var3, var2.getLocation(), var7, var2x -> this.canRaycastAimPoint(var1, var2x));
         this.cachedAimTarget = var1.immutable();
         this.cachedAimRayDir = null;
         this.cachedAimPoint = var5;
         return var5;
      }
   }

   private Vec3 reprojectCachedAimRay(BlockPos var1, Vec3 var2) {
      double var3 = this.minecraft.player.blockInteractionRange();
      BlockHitResult var5 = this.minecraft
         .level
         .clip(new ClipContext(var2, var2.add(this.cachedAimRayDir.scale(var3)), Block.OUTLINE, Fluid.NONE, this.minecraft.player));
      return var5.getType() == Type.BLOCK && var5.getBlockPos().equals(var1) ? var5.getLocation().add(this.cachedAimRayDir.scale(0.02)) : null;
   }

   private void preAimDuringDelay(BlockPos var1, BlockHitResult var2) {
      if (this.rotationConsumer != null
         && this.minecraft.player != null
         && this.smoothAimEnabled.getAsBoolean()
         && this.minecraft.gameMode instanceof AutismMultiPlayerGameModeAccessor var3) {
         int var15 = var3.autism$getDestroyDelay();
         if (var15 > 0) {
            Vec3 var5 = this.minecraft.player.getEyePosition();
            Vec3 var6 = this.resolveAimPoint(var1, var2);
            RotationMath.Rotation var7 = RotationMath.ofPlayer(this.minecraft.player);
            RotationMath.Rotation var8 = RotationMath.lookAt(var6, var5);
            float var9 = Mth.wrapDegrees(var8.yaw() - var7.yaw());
            float var10 = var8.pitch() - var7.pitch();
            if (!(Math.abs(var9) + Math.abs(var10) < 0.5F)) {
               float var11 = 1.0F / (var15 + 1);
               RotationMath.Rotation var12 = new RotationMath.Rotation(var7.yaw() + var9 * var11, var7.pitch() + var10 * var11);
               Vec3 var13 = Vec3.directionFromRotation(var12.pitch(), var12.yaw());
               BlockHitResult var14 = this.minecraft
                  .level
                  .clip(
                     new ClipContext(
                        var5,
                        var5.add(var13.scale(this.minecraft.player.blockInteractionRange())),
                        Block.OUTLINE,
                        Fluid.NONE,
                        this.minecraft.player
                     )
                  );
               if (var14.getType() == Type.BLOCK && !this.minecraft.level.getBlockState(var14.getBlockPos()).isAir()) {
                  this.rotationConsumer.accept(var12);
                  this.aimTickCallback.run();
               }
            }
         }
      }
   }

   private RotationMath.Rotation computePreTurnRotation() {
      if (this.rotationConsumer != null
         && this.nextLookDirection != null
         && this.activelyDigging
         && this.activeTarget != null
         && this.minecraft.player != null
         && this.smoothAimEnabled.getAsBoolean()) {
         BlockHitResult var1 = AiAim.raycastLookBlock();
         if (var1 != null && var1.getBlockPos().equals(this.activeTarget)) {
            Vec3 var2 = this.minecraft.player.getEyePosition();
            Vec3 var3 = this.minecraft.player.getLookAngle().normalize();
            Vec3 var4 = this.nextLookDirection.normalize();
            double var5 = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, var3.dot(var4)))));
            if (var5 < 0.5) {
               return null;
            }

            double var7 = Math.max(1.0F, this.expectedBreakTicks - this.breakElapsedTicks - 1.0F);
            double var9 = Math.min(6.0, Math.max(0.3, var5 / var7));
            VoxelShape var11 = this.activeTargetState == null ? null : this.activeTargetState.getShape(this.minecraft.level, this.activeTarget);
            if (var11 != null && !var11.isEmpty()) {
               AABB var12 = var11.bounds().move(this.activeTarget);

               for (double var13 = Math.min(var9, var5); var13 >= 0.15; var13 /= 2.0) {
                  Vec3 var15 = slerpDirection(var3, var4, var13 / var5);
                  BlockHitResult var16 = this.minecraft
                     .level
                     .clip(
                        new ClipContext(
                           var2,
                           var2.add(var15.scale(this.minecraft.player.blockInteractionRange())),
                           Block.OUTLINE,
                           Fluid.NONE,
                           this.minecraft.player
                        )
                     );
                  if (var16.getType() == Type.BLOCK && var16.getBlockPos().equals(this.activeTarget) && AiDigLine.init(var16, var12) >= 0.06) {
                     return new RotationMath.Rotation(AiDigLine.init(var15), AiDigLine.load(var15));
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

   static Vec3 slerpDirection(Vec3 var0, Vec3 var1, double var2) {
      if (var2 <= 0.0) {
         return var0;
      }

      if (var2 >= 1.0) {
         return var1;
      }

      double var4 = Math.max(-1.0, Math.min(1.0, var0.dot(var1)));
      double var6 = Math.acos(var4);
      if (var6 < 1.0E-6) {
         return var1;
      }

      Vec3 var8 = var1.subtract(var0.scale(var4));
      if (var8.lengthSqr() < 1.0E-12) {
         Vec3 var9 = Math.abs(var0.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
         var8 = var9.subtract(var0.scale(var0.dot(var9)));
      }

      return var0.scale(Math.cos(var6 * var2)).add(var8.normalize().scale(Math.sin(var6 * var2))).normalize();
   }

   private boolean canRaycastAimPoint(BlockPos var1, Vec3 var2) {
      Vec3 var3 = this.minecraft.player.getEyePosition();
      BlockHitResult var4 = this.minecraft.level.clip(new ClipContext(var3, var2, Block.OUTLINE, Fluid.NONE, this.minecraft.player));
      return AiBlockInteraction.isValidTargetHit(var3, var1, var4, this.minecraft.player.blockInteractionRange());
   }

   private static Vec3 faceCenterPoint(BlockPos var0, BlockHitResult var1) {
      Direction var2 = var1.getDirection();
      return Vec3.atCenterOf(var0).add(var2.getStepX() * 0.45, var2.getStepY() * 0.45, var2.getStepZ() * 0.45);
   }

   public void stop() {
      if (!AutismLiteVariant.enabled()) {
         AiVisualization.clearOwner(this);
      }

      this.aimStallTicks = 0;
      this.aimingTarget = null;
      this.restock.reset();
      if (this.activelyDigging && this.activeTarget != null && this.activeTargetState != null && this.minecraft.level != null) {
         if (this.minecraft.level.getBlockState(this.activeTarget).isAir()) {
            AiEditLedger.init(this.activeTarget, this.activeTargetState);
         } else {
            this.lastStoppedTarget = this.activeTarget;
            this.lastStoppedState = this.activeTargetState;
         }
      }

      if (this.activelyDigging && this.minecraft.gameMode != null && this.isGameModeDestroying(this.activeTarget)) {
         this.minecraft.gameMode.stopDestroyBlock();
         BlockAttackGraceWindow.init();
      }

      this.activelyDigging = false;
      this.activeTarget = null;
      this.activeTargetState = null;
   }

   private boolean isGameModeDestroying(BlockPos var1) {
      return !(this.minecraft.gameMode instanceof AutismMultiPlayerGameModeAccessor var2)
         ? true
         : var1 != null && var1.equals(var2.autism$getDestroyBlockPos());
   }

   public boolean isAimStalled() {
      return this.aimStallTicks > 0;
   }

   public boolean isDiggingTarget(BlockPos var1) {
      return this.activelyDigging && var1.equals(this.activeTarget);
   }

   BlockPos activeTarget() {
      return this.activeTarget;
   }

   public enum DigState {
      putAt_JsonCache_CacheHel,
      fill,
      readEx;
   }

   // 5.3 external ABI bridges (replacing retired RefQueue.Inner_DataUtil)
   public enum DigOutcome {
      init,
      load,
      read;

      static DigOutcome from(AiBlockDigger.DigState s) {
         return switch (s) {
            case putAt_JsonCache_CacheHel -> init;
            case fill -> load;
            case readEx -> read;
         };
      }
   }

   public String lastFailureReasonCompat() { return this.lastFailureReason(); }
   public AiBlockDigger withRotationConsumerCompat(Consumer<RotationMath.Rotation> aim) { return this.withRotationConsumer(aim); }
   public AiBlockDigger withAcceptedToolCompat(BiPredicate<BlockState, ItemStack> accept) { return this.withAcceptedTool(accept); }
   public AiBlockDigger withSealedWaterCompat(Set<BlockPos> sealedWater) { return this.withSealedWater(sealedWater); }
   public DigOutcome digCompat(BlockPos target) { return DigOutcome.from(this.dig(target)); }
   public DigOutcome digCompat(BlockPos target, Vec3 stand) { return DigOutcome.from(this.dig(target, stand)); }
   public DigOutcome digCompat(BlockPos target, Vec3 stand, Vec3 look) { return DigOutcome.from(this.dig(target, stand, look)); }
   public static boolean hasInsufficientUnderwaterAirCompat(Minecraft mc, double ticks) { return hasInsufficientUnderwaterAir(mc, ticks); }
   public static int airTicksToSurfaceCompat(Minecraft mc) { return airTicksToSurface(mc); }
   public static boolean continueBreakingBlockCompat(Minecraft mc, BlockPos pos, Direction face) { return continueBreakingBlock(mc, pos, face); }
   public static void tickDestroyDelayCompat(Minecraft mc) { tickDestroyDelay(mc); }
   public void stopCompat() { this.stop(); }
   public boolean isAimStalledCompat() { return this.isAimStalled(); }
   public boolean isDiggingTargetCompat(BlockPos target) { return this.isDiggingTarget(target); }
}
