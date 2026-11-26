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
   private static final boolean putAt_Z = Boolean.getBoolean("autism.navigation.debug");
   private static final boolean fill_Z = Boolean.getBoolean("autism.navigation.trace");
   private static final double readEx_D = 9.0;
   static final int flowState = 10;
   static final double putAt_D = 0.3;
   static final double fill_D = 6.0;
   public static final String putAt_Str = "too little air to break it under water";
   public static final int flowTemp = 6;
   public static final int readEx_I = 40;
   public static final int value_I = 24;
   private static int prevEx_I = -2147483648;
   private final Minecraft putAt_Minecraft;
   private final AiRestock putAt_SyncCore;
   private final boolean readEx_Z;
   private final boolean value_Z;
   private final IntConsumer putAt_IntConsumer;
   private final Consumer<Vec3> putAt_Consumer;
   private final Runnable putAt_Runnable;
   private final BooleanSupplier putAt_BooleanSupplier;
   private BlockPos putAt_BlockPos;
   private BlockPos fill_BlockPos;
   private BlockState putAt_BlockState;
   private int prev;
   private int chunkAt;
   private int state;
   private float putAt_F;
   private float fill_F;
   private boolean prevEx_Z;
   private String fill_Str = "";
   private BiPredicate<BlockState, ItemStack> putAt_BiPredicate = (var0, var1x) -> true;
   private Set<BlockPos> putAt_Set = Set.of();
   private Consumer<RotationMath.Inner_DataUtil> fill_Consumer;
   private Vec3 putAt_Vec3;
   private float readEx_F;
   private Vec3 fill_Vec3;
   private BlockPos readEx_BlockPos;
   private Vec3 readEx_Vec3;
   private Vec3 value_Vec3;
   private BlockPos value_BlockPos;
   private BlockState fill_BlockState;

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
      this.putAt_BooleanSupplier = var7;
      this.putAt_Runnable = var6;
      this.value_Z = var5;
      this.putAt_Minecraft = var1;
      this.putAt_SyncCore = new AiRestock(var1);
      this.readEx_Z = var4;
      this.putAt_IntConsumer = var2;
      this.putAt_Consumer = var3;
   }

   public String putAt_Str() {
      return this.fill_Str;
   }

   private AiBlockDigger.Inner_CacheHelper putAt_JsonCache_CacheHel_Str(String var1) {
      this.fill_Str = var1;
      return AiBlockDigger.Inner_CacheHelper.readEx;
   }

   public AiBlockDigger putAt_JsonCache_Consumer(Consumer<RotationMath.Inner_DataUtil> var1) {
      this.fill_Consumer = var1;
      return this;
   }

   public AiBlockDigger putAt_JsonCache_BiPredicate(BiPredicate<BlockState, ItemStack> var1) {
      this.putAt_BiPredicate = var1;
      return this;
   }

   public AiBlockDigger putAt_JsonCache_Set(Set<BlockPos> var1) {
      this.putAt_Set = Set.copyOf(var1);
      return this;
   }

   public AiBlockDigger.Inner_CacheHelper putAt_JsonCache_CacheHel_BlockPos(BlockPos var1) {
      return this.putAt_JsonCache_CacheHel_BlockPos_Vec3(var1, null);
   }

   public AiBlockDigger.Inner_CacheHelper putAt_JsonCache_CacheHel_BlockPos_Vec3(BlockPos var1, Vec3 var2) {
      return this.putAt_JsonCache_CacheHel_BlockPos(var1, var2, null);
   }

   public AiBlockDigger.Inner_CacheHelper putAt_JsonCache_CacheHel_BlockPos(BlockPos var1, Vec3 var2, Vec3 var3) {
      if (this.prevEx_Z && !var1.equals(this.putAt_BlockPos)) {
         this.putAt_V();
      }

      if (!AutismLiteVariant.enabled()) {
         AiVisualization.init(this, var1);
      }

      this.putAt_Vec3 = var3;
      this.value_Vec3 = var2;
      if (this.putAt_Minecraft.player != null
         && this.putAt_Minecraft.level != null
         && this.putAt_Minecraft.gameMode != null
         && this.putAt_Minecraft.level.getChunkSource().getChunk(var1.getX() >> 4, var1.getZ() >> 4, ChunkStatus.FULL, false) != null) {
         if (this.putAt_SyncCore.init_Z()) {
            AiRestock.Inner_BufferHelper var4 = this.putAt_SyncCore.load_UtilCore_BufferHel();
            if (var4 == AiRestock.Inner_BufferHelper.FAILED) {
               this.putAt_V();
               return this.putAt_JsonCache_CacheHel_Str("tool swap failed");
            }

            if (var4 != AiRestock.Inner_BufferHelper.SUCCEEDED) {
               return AiBlockDigger.Inner_CacheHelper.putAt_JsonCache_CacheHel;
            }

            this.putAt_SyncCore.init_V();
         }

         if (this.prevEx_Z && var1.equals(this.putAt_BlockPos)) {
            if (++this.prev > this.chunkAt) {
               this.putAt_V();
               return this.putAt_JsonCache_CacheHel_Str("took longer than " + this.chunkAt + " ticks");
            }

            BlockState var14 = this.putAt_Minecraft.level.getBlockState(this.putAt_BlockPos);
            if (var14.getBlock() != this.putAt_BlockState.getBlock() && putAt_Z_BlockState(var14) && putAt_Z_BlockState(this.putAt_BlockState)) {
               this.putAt_BlockState = var14;
            }

            if (var14.getBlock() != this.putAt_BlockState.getBlock()) {
               if (!var14.isAir() && var14.getFluidState().isEmpty()) {
                  this.putAt_V();
                  return this.putAt_JsonCache_CacheHel_Str("turned into " + var14.getBlock().getName().getString());
               } else {
                  AiEditLedger.init(this.putAt_BlockPos, this.putAt_BlockState);
                  this.putAt_V();
                  return AiBlockDigger.Inner_CacheHelper.fill;
               }
            } else {
               if (var14.getDestroySpeed(this.putAt_Minecraft.level, this.putAt_BlockPos) < 0.0F
                  || var14.hasBlockEntity() && !this.readEx_Z
                  || !var14.getFluidState().isEmpty()) {
                  this.putAt_V();
                  return this.putAt_JsonCache_CacheHel_Str("became unbreakable or fluid");
               }

               if (!AiPathfinder.Inner_StreamUtil.putAt_Z_ClientLevel(this.putAt_Minecraft.level, this.putAt_BlockPos, this.putAt_Set)) {
                  this.putAt_V();
                  return this.putAt_JsonCache_CacheHel_Str("fluid appeared next to it");
               }

               if (!this.fill(this.putAt_BlockPos)) {
                  this.putAt_V();
                  return this.putAt_JsonCache_CacheHel_Str("removing this floor would cause an unsafe fall");
               }

               if (this.readEx(this.putAt_BlockPos)) {
                  return AiBlockDigger.Inner_CacheHelper.putAt_JsonCache_CacheHel;
               }

               BlockHitResult var15 = AiBlockInteraction.init(this.putAt_Minecraft, this.putAt_BlockPos);
               if (var15 == null) {
                  if (putAt_Z) {
                     AiNavDebug.init(
                        "digger lost "
                           + this.putAt_BlockPos
                           + " eye="
                           + this.putAt_Minecraft.player.getEyePosition()
                           + " body="
                           + this.putAt_Minecraft.player.position()
                           + " motion="
                           + this.putAt_Minecraft.player.getDeltaMovement()
                           + " pose="
                           + this.putAt_Minecraft.player.getPose()
                           + " climb="
                           + this.putAt_Minecraft.player.onClimbable()
                           + " shift="
                           + this.putAt_Minecraft.options.keyShift.isDown()
                     );
                  }

                  this.putAt_V();
                  return this.putAt_JsonCache_CacheHel_Str("lost sight of it while breaking");
               } else {
                  int var17 = MiningCost.init_I_Minecraft_2(this.putAt_Minecraft, var14, this.value_Z, this.putAt_BiPredicate);
                  if (var17 < 0) {
                     this.putAt_V();
                     return this.putAt_JsonCache_CacheHel_Str("no tool left that gets its drops");
                  }

                  if (var17 >= 9) {
                     String var19 = BuiltInRegistries.ITEM.getKey(this.putAt_Minecraft.player.getInventory().getItem(var17).getItem()).toString();
                     this.putAt_V();
                     this.putAt_SyncCore.init_V_I(var17, this.putAt_Minecraft.player.getInventory().getSelectedSlot(), var19);
                     return AiBlockDigger.Inner_CacheHelper.putAt_JsonCache_CacheHel;
                  }

                  this.putAt_IntConsumer.accept(var17);
                  Vec3 var18 = this.putAt_Vec3_BlockPos_BlockHitResult(this.putAt_BlockPos, var15);
                  RotationMath.Inner_DataUtil var20 = this.putAt_TaskUtil_CacheHelp();
                  if (var20 != null) {
                     this.fill_Consumer.accept(var20);
                  } else {
                     this.putAt_Consumer.accept(var18);
                  }

                  this.putAt_Runnable.run();
                  AiMotionAudit.init(this.putAt_Minecraft);
                  if (!AiAim.init_Z_BlockPos(this.putAt_BlockPos)) {
                     putAt_V_Minecraft(this.putAt_Minecraft);
                     float var21 = this.putAt_Minecraft.player.getYRot();
                     float var10 = this.putAt_Minecraft.player.getXRot();
                     boolean var11 = Math.abs(Mth.wrapDegrees(var21 - this.putAt_F)) + Math.abs(var10 - this.fill_F) > 0.5F;
                     this.putAt_F = var21;
                     this.fill_F = var10;
                     if (fill_Z && !var11) {
                        BlockHitResult var12 = AiAim.init_BlockHitResult();
                        AiNavDebug.init(
                           "digger off "
                              + this.putAt_BlockPos
                              + " ray "
                              + (var12 == null ? "-" : var12.getBlockPos() + " " + var12.getDirection())
                              + " yaw="
                              + var21
                              + " pitch="
                              + var10
                              + " eye="
                              + this.putAt_Minecraft.player.getEyePosition()
                        );
                     }

                     if (!var11 && ++this.state > 10) {
                        this.putAt_V();
                        return this.putAt_JsonCache_CacheHel_Str("crosshair left it mid-break");
                     } else {
                        return AiBlockDigger.Inner_CacheHelper.putAt_JsonCache_CacheHel;
                     }
                  } else {
                     this.state = 0;
                     this.putAt_V_BlockPos_Direction(this.putAt_BlockPos, var15.getDirection());
                     return this.putAt_JsonCache_CacheHel();
                  }
               }
            }
         } else {
            if (this.prevEx_Z) {
               this.putAt_V();
            }

            if (!var1.equals(this.fill_BlockPos)) {
               this.fill_BlockPos = var1.immutable();
               this.state = 0;
            }

            BlockState var13 = this.putAt_Minecraft.level.getBlockState(var1);
            if (var13.isAir()) {
               if (var1.equals(this.value_BlockPos) && this.fill_BlockState != null) {
                  AiEditLedger.init(var1, this.fill_BlockState);
               }

               this.value_BlockPos = null;
               return AiBlockDigger.Inner_CacheHelper.fill;
            } else if (!(var13.getDestroySpeed(this.putAt_Minecraft.level, var1) < 0.0F)
               && (!var13.hasBlockEntity() || this.readEx_Z)
               && var13.getFluidState().isEmpty()) {
               if (!AiPathfinder.Inner_StreamUtil.putAt_Z_ClientLevel(this.putAt_Minecraft.level, var1, this.putAt_Set)) {
                  if (putAt_Z) {
                     AiNavDebug.init("digger fail digSafe " + var1);
                  }

                  return this.putAt_JsonCache_CacheHel_Str("unsafe to dig (fluid next to it)");
               } else {
                  if (AiPathfinder.Inner_StreamUtil.putAt_Z_BlockGetter_BlockPos(this.putAt_Minecraft.level, var1)) {
                     return this.putAt_JsonCache_CacheHel_Str("a lit portal's frame");
                  }

                  if (!this.fill(var1)) {
                     return this.putAt_JsonCache_CacheHel_Str("removing this floor would cause an unsafe fall");
                  }

                  if (var1.equals(this.putAt_Minecraft.player.blockPosition().above(2))
                     && this.putAt_Minecraft.level.getBlockState(var1.above()).getBlock() instanceof FallingBlock) {
                     if (putAt_Z) {
                        AiNavDebug.init("digger fail falling over head " + var1);
                     }

                     return this.putAt_JsonCache_CacheHel_Str("sand or gravel over it");
                  } else {
                     int var5 = MiningCost.init_I_Minecraft_2(this.putAt_Minecraft, var13, this.value_Z, this.putAt_BiPredicate);
                     if (var5 < 0) {
                        if (putAt_Z) {
                           AiNavDebug.init("digger fail no-tool " + var1);
                        }

                        AutismClientAddon.LOG.info(
                              "[AI] digger: no tool that gets the drops of {} at {}",
                              BuiltInRegistries.BLOCK.getKey(var13.getBlock()).getPath(),
                              var1.toShortString()
                           );
                        return this.putAt_JsonCache_CacheHel_Str("no tool that gets its drops");
                     } else {
                        if (var5 >= 9) {
                           String var16 = BuiltInRegistries.ITEM.getKey(this.putAt_Minecraft.player.getInventory().getItem(var5).getItem()).toString();
                           this.putAt_SyncCore.init_V_I(var5, this.putAt_Minecraft.player.getInventory().getSelectedSlot(), var16);
                           return AiBlockDigger.Inner_CacheHelper.putAt_JsonCache_CacheHel;
                        }

                        if (putAt_Z_Minecraft_D(
                           this.putAt_Minecraft,
                           MiningCost.init_D_Minecraft(
                              this.putAt_Minecraft,
                              this.putAt_Minecraft.player.getInventory().getItem(var5),
                              var13,
                              var13.getDestroySpeed(this.putAt_Minecraft.level, var1)
                           )
                        )) {
                           return this.putAt_JsonCache_CacheHel_Str("too little air to break it under water");
                        }

                        BlockHitResult var6 = AiBlockInteraction.init(this.putAt_Minecraft, var1);
                        if (var6 == null) {
                           if (putAt_Z) {
                              AiNavDebug.init("digger fail no-hit " + var1 + " eye=" + this.putAt_Minecraft.player.getEyePosition());
                           }

                           return this.putAt_JsonCache_CacheHel_Str("no clickable face in reach");
                        } else {
                           if (this.putAt_Z()) {
                              this.putAt_V_BlockPos_BlockHitResult(var1, var6);
                              putAt_V_Minecraft(this.putAt_Minecraft);
                              return AiBlockDigger.Inner_CacheHelper.putAt_JsonCache_CacheHel;
                           }

                           this.putAt_Consumer.accept(this.putAt_Vec3_BlockPos_BlockHitResult(var1, var6));
                           this.putAt_Runnable.run();
                           AiMotionAudit.init(this.putAt_Minecraft);
                           if (!AiAim.init_Z_BlockPos(var1)) {
                              putAt_V_Minecraft(this.putAt_Minecraft);
                              float var7 = this.putAt_Minecraft.player.getYRot();
                              float var8 = this.putAt_Minecraft.player.getXRot();
                              boolean var9 = Math.abs(Mth.wrapDegrees(var7 - this.putAt_F)) + Math.abs(var8 - this.fill_F) > 0.5F;
                              this.putAt_F = var7;
                              this.fill_F = var8;
                              if (var9) {
                                 return AiBlockDigger.Inner_CacheHelper.putAt_JsonCache_CacheHel;
                              }

                              if (this.putAt_Minecraft.player.getDeltaMovement().lengthSqr() > 0.008
                                 && !this.putAt_Minecraft.player.isInWater()
                                 && !this.putAt_Minecraft.player.onClimbable()) {
                                 return AiBlockDigger.Inner_CacheHelper.putAt_JsonCache_CacheHel;
                              }

                              if (++this.state > 10) {
                                 this.state = 0;
                                 if (putAt_Z) {
                                    AiNavDebug.init("digger fail aim " + var1);
                                 }

                                 return this.putAt_JsonCache_CacheHel_Str("crosshair never landed on it");
                              } else {
                                 if (this.state > 2) {
                                    this.putAt_Consumer.accept(var6.getLocation());
                                 }

                                 return AiBlockDigger.Inner_CacheHelper.putAt_JsonCache_CacheHel;
                              }
                           } else {
                              this.state = 0;
                              this.putAt_V_BlockPos(var1, var6, var5);
                              return this.putAt_JsonCache_CacheHel();
                           }
                        }
                     }
                  }
               }
            } else {
               if (putAt_Z) {
                  AiNavDebug.init("digger fail unbreakable/fluid " + var1);
               }

               return this.putAt_JsonCache_CacheHel_Str("unbreakable or fluid");
            }
         }
      } else {
         this.putAt_V();
         return this.putAt_JsonCache_CacheHel_Str("chunk not loaded");
      }
   }

   static boolean putAt_Z_BlockState(BlockState var0) {
      return var0.is(Blocks.DIRT)
         || var0.is(Blocks.GRASS_BLOCK)
         || var0.is(Blocks.MYCELIUM)
         || var0.is(Blocks.PODZOL)
         || var0.is(Blocks.DIRT_PATH)
         || var0.is(Blocks.COARSE_DIRT)
         || var0.is(Blocks.ROOTED_DIRT);
   }

   private boolean fill(BlockPos var1) {
      return AiMotionSafety.putAt(var1);
   }

   private AiBlockDigger.Inner_CacheHelper putAt_JsonCache_CacheHel() {
      BlockState var1 = this.putAt_Minecraft.level.getBlockState(this.putAt_BlockPos);
      if (var1.getBlock() == this.putAt_BlockState.getBlock() || !var1.isAir() && var1.getFluidState().isEmpty()) {
         return AiBlockDigger.Inner_CacheHelper.putAt_JsonCache_CacheHel;
      }

      AiEditLedger.init(this.putAt_BlockPos, this.putAt_BlockState);
      this.putAt_V();
      return AiBlockDigger.Inner_CacheHelper.fill;
   }

   public static boolean putAt_Z_Minecraft_D(Minecraft var0, double var1) {
      LocalPlayer var3 = var0.player;
      return var3 != null
            && var0.level != null
            && var3.isEyeInFluid(FluidTags.WATER)
            && !var3.hasEffect(MobEffects.WATER_BREATHING)
            && !var3.hasEffect(MobEffects.CONDUIT_POWER)
         ? var3.getAirSupply() < var1 + putAt_I_Minecraft(var0) + 40.0
         : false;
   }

   public static int putAt_I_Minecraft(Minecraft var0) {
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

   static boolean putAt_Z_I(int var0, boolean var1, boolean var2) {
      return var0 > 0 && var1 && var2;
   }

   boolean putAt_Z() {
      if (!this.prevEx_Z
         && this.putAt_Minecraft.player != null
         && this.putAt_Minecraft.level != null
         && this.putAt_BooleanSupplier.getAsBoolean()
         && this.putAt_Minecraft.gameMode instanceof AutismMultiPlayerGameModeAccessor var1) {
         BlockHitResult var3 = AiAim.init_BlockHitResult();
         return putAt_Z_I(var1.autism$getDestroyDelay(), true, var3 != null && !this.putAt_Minecraft.level.getBlockState(var3.getBlockPos()).isAir());
      } else {
         return false;
      }
   }

   private static boolean putAt_Z_Minecraft(Minecraft var0) {
      if (prevEx_I == var0.player.tickCount) {
         return true;
      }

      prevEx_I = var0.player.tickCount;
      BlockAttackGraceWindow.init(var0.player);
      return false;
   }

   private void putAt_V_BlockPos_Direction(BlockPos var1, Direction var2) {
      if (!putAt_Z_Minecraft(this.putAt_Minecraft)) {
         BlockHitResult var3 = AiAim.init_BlockHitResult();
         Direction var4 = var3 != null && var3.getBlockPos().equals(var1) ? var3.getDirection() : var2;
         if (this.putAt_Minecraft.gameMode.continueDestroyBlock(var1, var4)) {
            Compat263.addBreakingBlockEffect(this.putAt_Minecraft.level, var1, var4);
            Compat263.swing(this.putAt_Minecraft.player, InteractionHand.MAIN_HAND);
         }
      }
   }

   public static boolean putAt_Z_Minecraft(Minecraft var0, BlockPos var1, Direction var2) {
      if (var0.player != null && var0.level != null && var0.gameMode != null && !putAt_Z_Minecraft(var0)) {
         BlockHitResult var3 = AiAim.init_BlockHitResult();
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

   public static void putAt_V_Minecraft(Minecraft var0) {
      if (var0.player != null && var0.level != null && var0.gameMode instanceof AutismMultiPlayerGameModeAccessor var1 && var1.autism$getDestroyDelay() > 0) {
         BlockHitResult var3 = AiAim.init_BlockHitResult();
         if (var3 != null && !var0.level.getBlockState(var3.getBlockPos()).isAir() && !putAt_Z_Minecraft(var0)) {
            if (var0.gameMode.continueDestroyBlock(var3.getBlockPos(), var3.getDirection())) {
               Compat263.swing(var0.player, InteractionHand.MAIN_HAND);
            }
         }
      }
   }

   static boolean putAt_Z_BlockPos_List(BlockPos var0, List<Vec3> var1) {
      Vec3 var2 = Vec3.atCenterOf(var0);

      for (Vec3 var4 : var1) {
         if (var4.distanceToSqr(var2) <= 9.0) {
            return true;
         }
      }

      return false;
   }

   private boolean readEx(BlockPos var1) {
      ArrayList var2 = null;

      for (Entity var4 : this.putAt_Minecraft.level.entitiesForRendering()) {
         if (var4 instanceof FallingBlockEntity) {
            if (var2 == null) {
               var2 = new ArrayList();
            }

            var2.add(var4.position());
         }
      }

      return var2 != null && putAt_Z_BlockPos_List(var1, var2);
   }

   private void putAt_V_BlockPos(BlockPos var1, BlockHitResult var2, int var3) {
      this.putAt_BlockPos = var1.immutable();
      this.putAt_BlockState = this.putAt_Minecraft.level.getBlockState(var1);
      if (var3 >= 0) {
         this.putAt_IntConsumer.accept(var3);
      }

      this.putAt_Consumer.accept(this.putAt_Vec3_BlockPos_BlockHitResult(var1, var2));
      this.putAt_V_BlockPos_Direction(this.putAt_BlockPos, var2.getDirection());
      this.prev = 0;
      this.readEx_F = (float)MiningCost.init_D_Minecraft(
         this.putAt_Minecraft,
         this.putAt_Minecraft.player.getMainHandItem(),
         this.putAt_BlockState,
         this.putAt_BlockState.getDestroySpeed(this.putAt_Minecraft.level, this.putAt_BlockPos)
      );
      this.chunkAt = (int)Math.min(72000.0, Math.ceil(this.readEx_F * 4.0F) + 40.0);
      this.prevEx_Z = true;
   }

   private Vec3 putAt_Vec3_BlockPos_BlockHitResult(BlockPos var1, BlockHitResult var2) {
      Vec3 var3 = this.putAt_Minecraft.player.getEyePosition();
      if (this.prevEx_Z && var1.equals(this.putAt_BlockPos)) {
         BlockHitResult var4 = AiAim.init_BlockHitResult();
         if (var4 != null && var4.getBlockPos().equals(var1)) {
            this.readEx_BlockPos = var1.immutable();
            this.fill_Vec3 = this.putAt_Minecraft.player.getLookAngle();
            this.readEx_Vec3 = null;
            return var4.getLocation().add(this.fill_Vec3.scale(0.02));
         }
      }

      if (!this.prevEx_Z && this.fill_Consumer != null && this.value_Vec3 != null && this.putAt_Z_BlockPos_Vec3(var1, this.value_Vec3)) {
         this.readEx_BlockPos = var1.immutable();
         this.fill_Vec3 = this.value_Vec3.subtract(var3).normalize();
         this.readEx_Vec3 = null;
         return this.value_Vec3;
      }

      if (var1.equals(this.readEx_BlockPos) && this.fill_Vec3 != null) {
         Vec3 var6 = this.putAt_Vec3_BlockPos_Vec3(var1, var3);
         if (var6 != null) {
            return var6;
         }
      }

      if (var1.equals(this.readEx_BlockPos) && this.readEx_Vec3 != null && this.putAt_Z_BlockPos_Vec3(var1, this.readEx_Vec3)) {
         return this.readEx_Vec3;
      } else if (this.value_Vec3 != null && this.putAt_Z_BlockPos_Vec3(var1, this.value_Vec3)) {
         this.readEx_BlockPos = var1.immutable();
         this.fill_Vec3 = this.value_Vec3.subtract(var3).normalize();
         this.readEx_Vec3 = null;
         return this.value_Vec3;
      } else {
         Vec3 var7 = fill_Vec3_BlockPos_BlockHitResult(var1, var2);
         Vec3 var5 = AiBlockInteraction.init(var3, var2.getLocation(), var7, var2x -> this.putAt_Z_BlockPos_Vec3(var1, var2x));
         this.readEx_BlockPos = var1.immutable();
         this.fill_Vec3 = null;
         this.readEx_Vec3 = var5;
         return var5;
      }
   }

   private Vec3 putAt_Vec3_BlockPos_Vec3(BlockPos var1, Vec3 var2) {
      double var3 = this.putAt_Minecraft.player.blockInteractionRange();
      BlockHitResult var5 = this.putAt_Minecraft
         .level
         .clip(new ClipContext(var2, var2.add(this.fill_Vec3.scale(var3)), Block.OUTLINE, Fluid.NONE, this.putAt_Minecraft.player));
      return var5.getType() == Type.BLOCK && var5.getBlockPos().equals(var1) ? var5.getLocation().add(this.fill_Vec3.scale(0.02)) : null;
   }

   private void putAt_V_BlockPos_BlockHitResult(BlockPos var1, BlockHitResult var2) {
      if (this.fill_Consumer != null
         && this.putAt_Minecraft.player != null
         && this.putAt_BooleanSupplier.getAsBoolean()
         && this.putAt_Minecraft.gameMode instanceof AutismMultiPlayerGameModeAccessor var3) {
         int var15 = var3.autism$getDestroyDelay();
         if (var15 > 0) {
            Vec3 var5 = this.putAt_Minecraft.player.getEyePosition();
            Vec3 var6 = this.putAt_Vec3_BlockPos_BlockHitResult(var1, var2);
            RotationMath.Inner_DataUtil var7 = RotationMath.init(this.putAt_Minecraft.player);
            RotationMath.Inner_DataUtil var8 = RotationMath.init(var6, var5);
            float var9 = Mth.wrapDegrees(var8.init() - var7.init());
            float var10 = var8.load() - var7.load();
            if (!(Math.abs(var9) + Math.abs(var10) < 0.5F)) {
               float var11 = 1.0F / (var15 + 1);
               RotationMath.Inner_DataUtil var12 = new RotationMath.Inner_DataUtil(var7.init() + var9 * var11, var7.load() + var10 * var11);
               Vec3 var13 = Vec3.directionFromRotation(var12.load(), var12.init());
               BlockHitResult var14 = this.putAt_Minecraft
                  .level
                  .clip(
                     new ClipContext(
                        var5,
                        var5.add(var13.scale(this.putAt_Minecraft.player.blockInteractionRange())),
                        Block.OUTLINE,
                        Fluid.NONE,
                        this.putAt_Minecraft.player
                     )
                  );
               if (var14.getType() == Type.BLOCK && !this.putAt_Minecraft.level.getBlockState(var14.getBlockPos()).isAir()) {
                  this.fill_Consumer.accept(var12);
                  this.putAt_Runnable.run();
               }
            }
         }
      }
   }

   private RotationMath.Inner_DataUtil putAt_TaskUtil_CacheHelp() {
      if (this.fill_Consumer != null
         && this.putAt_Vec3 != null
         && this.prevEx_Z
         && this.putAt_BlockPos != null
         && this.putAt_Minecraft.player != null
         && this.putAt_BooleanSupplier.getAsBoolean()) {
         BlockHitResult var1 = AiAim.init_BlockHitResult();
         if (var1 != null && var1.getBlockPos().equals(this.putAt_BlockPos)) {
            Vec3 var2 = this.putAt_Minecraft.player.getEyePosition();
            Vec3 var3 = this.putAt_Minecraft.player.getLookAngle().normalize();
            Vec3 var4 = this.putAt_Vec3.normalize();
            double var5 = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, var3.dot(var4)))));
            if (var5 < 0.5) {
               return null;
            }

            double var7 = Math.max(1.0F, this.readEx_F - this.prev - 1.0F);
            double var9 = Math.min(6.0, Math.max(0.3, var5 / var7));
            VoxelShape var11 = this.putAt_BlockState == null ? null : this.putAt_BlockState.getShape(this.putAt_Minecraft.level, this.putAt_BlockPos);
            if (var11 != null && !var11.isEmpty()) {
               AABB var12 = var11.bounds().move(this.putAt_BlockPos);

               for (double var13 = Math.min(var9, var5); var13 >= 0.15; var13 /= 2.0) {
                  Vec3 var15 = putAt_Vec3_Vec3(var3, var4, var13 / var5);
                  BlockHitResult var16 = this.putAt_Minecraft
                     .level
                     .clip(
                        new ClipContext(
                           var2,
                           var2.add(var15.scale(this.putAt_Minecraft.player.blockInteractionRange())),
                           Block.OUTLINE,
                           Fluid.NONE,
                           this.putAt_Minecraft.player
                        )
                     );
                  if (var16.getType() == Type.BLOCK && var16.getBlockPos().equals(this.putAt_BlockPos) && AiDigLine.init(var16, var12) >= 0.06) {
                     return new RotationMath.Inner_DataUtil(AiDigLine.init(var15), AiDigLine.load(var15));
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

   static Vec3 putAt_Vec3_Vec3(Vec3 var0, Vec3 var1, double var2) {
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

   private boolean putAt_Z_BlockPos_Vec3(BlockPos var1, Vec3 var2) {
      Vec3 var3 = this.putAt_Minecraft.player.getEyePosition();
      BlockHitResult var4 = this.putAt_Minecraft.level.clip(new ClipContext(var3, var2, Block.OUTLINE, Fluid.NONE, this.putAt_Minecraft.player));
      return AiBlockInteraction.init(var3, var1, var4, this.putAt_Minecraft.player.blockInteractionRange());
   }

   private static Vec3 fill_Vec3_BlockPos_BlockHitResult(BlockPos var0, BlockHitResult var1) {
      Direction var2 = var1.getDirection();
      return Vec3.atCenterOf(var0).add(var2.getStepX() * 0.45, var2.getStepY() * 0.45, var2.getStepZ() * 0.45);
   }

   public void putAt_V() {
      if (!AutismLiteVariant.enabled()) {
         AiVisualization.init_V(this);
      }

      this.state = 0;
      this.fill_BlockPos = null;
      this.putAt_SyncCore.init_V();
      if (this.prevEx_Z && this.putAt_BlockPos != null && this.putAt_BlockState != null && this.putAt_Minecraft.level != null) {
         if (this.putAt_Minecraft.level.getBlockState(this.putAt_BlockPos).isAir()) {
            AiEditLedger.init(this.putAt_BlockPos, this.putAt_BlockState);
         } else {
            this.value_BlockPos = this.putAt_BlockPos;
            this.fill_BlockState = this.putAt_BlockState;
         }
      }

      if (this.prevEx_Z && this.putAt_Minecraft.gameMode != null && this.value(this.putAt_BlockPos)) {
         this.putAt_Minecraft.gameMode.stopDestroyBlock();
         BlockAttackGraceWindow.init();
      }

      this.prevEx_Z = false;
      this.putAt_BlockPos = null;
      this.putAt_BlockState = null;
   }

   private boolean value(BlockPos var1) {
      return !(this.putAt_Minecraft.gameMode instanceof AutismMultiPlayerGameModeAccessor var2)
         ? true
         : var1 != null && var1.equals(var2.autism$getDestroyBlockPos());
   }

   public boolean fill_Z() {
      return this.state > 0;
   }

   public boolean putAt_Z_BlockPos(BlockPos var1) {
      return this.prevEx_Z && var1.equals(this.putAt_BlockPos);
   }

   BlockPos putAt_BlockPos() {
      return this.putAt_BlockPos;
   }

   public enum Inner_CacheHelper {
      putAt_JsonCache_CacheHel,
      fill,
      readEx;
   }

   // 5.3 external ABI bridges (replacing retired RefQueue.Inner_DataUtil)
   public enum Inner_DataUtil_2 {
      init,
      load,
      read;

      static Inner_DataUtil_2 from(AiBlockDigger.Inner_CacheHelper s) {
         return switch (s) {
            case putAt_JsonCache_CacheHel -> init;
            case fill -> load;
            case readEx -> read;
         };
      }
   }

   public String init_Str() { return this.putAt_Str(); }
   public AiBlockDigger init_RefQueue_DataUtil_Consumer(Consumer<RotationMath.Inner_DataUtil> aim) { return this.putAt_JsonCache_Consumer(aim); }
   public AiBlockDigger init_RefQueue_DataUtil_BiPredicate(BiPredicate<BlockState, ItemStack> accept) { return this.putAt_JsonCache_BiPredicate(accept); }
   public AiBlockDigger init_RefQueue_DataUtil_Set(Set<BlockPos> sealedWater) { return this.putAt_JsonCache_Set(sealedWater); }
   public Inner_DataUtil_2 init_RefQueue_DataUtil__BlockPos(BlockPos target) { return Inner_DataUtil_2.from(this.putAt_JsonCache_CacheHel_BlockPos(target)); }
   public Inner_DataUtil_2 init_RefQueue_DataUtil__BlockPos_Vec3(BlockPos target, Vec3 stand) { return Inner_DataUtil_2.from(this.putAt_JsonCache_CacheHel_BlockPos_Vec3(target, stand)); }
   public Inner_DataUtil_2 init_RefQueue_DataUtil__BlockPos(BlockPos target, Vec3 stand, Vec3 look) { return Inner_DataUtil_2.from(this.putAt_JsonCache_CacheHel_BlockPos(target, stand, look)); }
   public static boolean init_Z_Minecraft_D(Minecraft mc, double ticks) { return putAt_Z_Minecraft_D(mc, ticks); }
   public static int init_I_Minecraft(Minecraft mc) { return putAt_I_Minecraft(mc); }
   public static boolean init_Z_Minecraft(Minecraft mc, BlockPos pos, Direction face) { return putAt_Z_Minecraft(mc, pos, face); }
   public static void init_V_Minecraft(Minecraft mc) { putAt_V_Minecraft(mc); }
   public void init_V() { this.putAt_V(); }
   public boolean load_Z() { return this.fill_Z(); }
   public boolean init_Z_BlockPos(BlockPos target) { return this.putAt_Z_BlockPos(target); }
}
