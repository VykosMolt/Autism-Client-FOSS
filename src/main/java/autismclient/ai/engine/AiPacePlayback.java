package autismclient.ai.engine;

import autismclient.util.RotationMath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;

public final class AiPacePlayback {
   static final double putAt_D = 0.05;
   static final int flowState = 10;
   static final float putAt_F = 0.3F;
   static final float fill_F = 5.0F;
   static final float readEx_F = -40.0F;
   static final float value_F = 70.0F;
   static final int flowTemp = 24;
   static final int readEx_I = 4;
   static final int value_I = 10;
   static final int prevEx_I = 30;
   private static final double fill_D = 0.02;
   private static final int prev_I = 60;
   private static final double readEx_D = 0.35;
   private static final double value_D = 0.25;
   private static final int chunkAt_I = 24;
   private static final int state_I = 4;
   private static final float prevEx_F = 24.0F;
   private final AiPaceScript.Inner_IoHelper putAt_TextPool_IoHelper;
   private final double prevEx_D;
   private final int closeOf_I;
   private final double prev_D;
   private final double chunkAt_D;
   private double state_D;
   private boolean putAt_Z;
   private boolean fill_Z;
   private boolean readEx_Z;
   private int pollAt_I;
   private int pollOf;
   private int tick;
   private int flushOf;
   private int stateOf = -1;
   private float prev_F = 0.0F / 0.0F;
   private boolean value_Z;
   private int store;
   private int nextBy;
   private int dataEx = -1;
   private String putAt_Str;
   private boolean prevEx_Z;
   private boolean prev_Z;
   private double closeOf_D;
   private double pollAt_D = 0.0 / 0.0;
   private String fill_Str;
   private final Map<Integer, AiPhysics.MotionState> putAt_Map = new HashMap<>();
   private final Map<Integer, AiPhysics.MoveInput> fill_Map = new HashMap<>();

   public AiPacePlayback(AiPaceScript.Inner_IoHelper var1) {
      this(var1, 0.1, 0.0 / 0.0);
   }

   public AiPacePlayback(AiPaceScript.Inner_IoHelper var1, double var2) {
      this(var1, var2, 0.0 / 0.0);
   }

   public AiPacePlayback(AiPaceScript.Inner_IoHelper var1, double var2, double var4) {
      this.putAt_TextPool_IoHelper = var1;
      double var6 = Double.isFinite(var2) && var2 > 0.0 ? var2 : 0.1;
      double var8 = var1.putAt_D_I(0);
      double var10 = Double.isFinite(var8) && var8 > 0.0 ? var8 : var6;
      this.prevEx_D = var10;
      int var12 = var1.flowState();
      double var13 = var12 >= 0 ? var1.putAt_D_I(var12) : 0.0 / 0.0;
      if (var12 >= 0 && Double.isFinite(var13) && var13 > 0.0 && Math.abs(var10 - var13) > 1.0E-6) {
         this.closeOf_I = var12;
         this.prev_D = var10;
         this.chunkAt_D = var13;
         this.state_D = var10;
      } else if (Double.isFinite(var4) && var4 > 0.0 && Math.abs(var4 - var10) > 1.0E-4) {
         this.closeOf_I = 0;
         this.prev_D = var4;
         this.chunkAt_D = var10;
         this.state_D = var10;
      } else {
         this.closeOf_I = -1;
         this.prev_D = var10;
         this.chunkAt_D = var10;
         this.state_D = var10;
      }
   }

   public int flowState() {
      return this.store;
   }

   public double putAt_D() {
      return this.closeOf_D;
   }

   public double fill() {
      return this.prevEx_D;
   }

   public double readEx() {
      return this.state_D;
   }

   public boolean putAt_Z() {
      return this.value_Z || this.closeOf_I < 0 && Math.abs(this.state_D - this.prevEx_D) > 1.0E-4;
   }

   public String putAt_Str() {
      return this.fill_Str;
   }

   private AiPhysics.MotionState putAt_TokenCore_TableCac_I(int var1) {
      AiPhysics.MotionState var2 = this.putAt_Map.get(var1);
      return var2 != null ? var2 : this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(var1);
   }

   public AiPacePlayback.Inner_JsonCache putAt_TableUtil_JsonCach_LocalPlayer(LocalPlayer var1) {
      return this.putAt_TableUtil_JsonCach_LocalPlayer(var1, null, null, null);
   }

   public AiPacePlayback.Inner_JsonCache putAt_TableUtil_JsonCach_LocalPlayer(LocalPlayer var1, Vec3 var2, Runnable var3, Vec3 var4) {
      return this.putAt(var1, var2, var3, var4, null);
   }

   public AiPacePlayback.Inner_JsonCache putAt(LocalPlayer var1, Vec3 var2, Runnable var3, Vec3 var4, Predicate<BlockPos> var5) {
      Minecraft var6 = Minecraft.getInstance();
      AiLiveWorld var7 = var6.level != null ? new AiLiveWorld(var6.level, var1) : null;
      Predicate var8 = var6.level != null
         ? var1x -> var6.level.getChunkSource().getChunk(((net.minecraft.core.BlockPos)var1x).getX() >> 4, ((net.minecraft.core.BlockPos)var1x).getZ() >> 4, ChunkStatus.FULL, false) != null
         : null;
      AiPhysics.MotionState var9 = AiPhysics.MotionState.of(var1);
      AiPacePlayback.Inner_TableCache var10 = this.putAt_TableUtil_TableCac_TokenCore_TaskStat(var7, var9, var8, var5);
      if (var10.status() != AiPacePlayback.Inner_JsonCache.putAt_TableUtil_JsonCach) {
         return var10.status();
      }

      AiPhysics.MoveInput var11 = var10.applied();
      if (var2 != null && var1.onGround() && !var11.jump() && !var11.sneak()) {
         RotationMath.Rotation var12 = RotationMath.lookAt(var2, var1.getEyePosition());
         AiPhysics.MoveInput var13 = putAt_TokenCore_CacheHel_TokenCore_CacheHel_F(var11, var12.yaw());
         AiPhysics.MotionState var14 = var7 != null ? AiPhysics.fill(var9, var13, var7) : null;
         boolean var15 = var14 == null
            || var14.onGround()
               && var14.vy() <= 0.1
               && !AiPhysics.putAt_Z(var7, var9, var14)
               && AiPaceScript.putAt_Z_TokenCore_TaskStat(var7, var14, 0.002)
               && (var5 == null || var5.test(BlockPos.containing(var14.x(), var14.y() + 1.0E-4, var14.z())))
               && this.putAt_Z_TokenCore_TaskStat(var7, var14, this.store - 1, var5);
         if (var15) {
            putAt_V_LocalPlayer(var1, var13, var12.pitch());
            if (var3 != null) {
               var3.run();
            }

            return AiPacePlayback.Inner_JsonCache.putAt_TableUtil_JsonCach;
         }
      }

      float var16 = var4 != null && this.putAt_TextPool_IoHelper.putAt_Z() && this.putAt_TextPool_IoHelper.flowTemp() - this.store <= 4
         ? RotationMath.lookAt(var4, var1.getEyePosition()).pitch()
         : this.putAt_F_LocalPlayer(var1);
      putAt_V_LocalPlayer(var1, var11, var16);
      return AiPacePlayback.Inner_JsonCache.putAt_TableUtil_JsonCach;
   }

   private boolean putAt_Z_TokenCore_TaskStat(AiPhysics.PhysicsWorld var1, AiPhysics.MotionState var2, int var3, Predicate<BlockPos> var4) {
      int var5 = this.putAt_TextPool_IoHelper.flowTemp();
      int var6 = Math.min(var5, var3 + 1 + 24 + 24);
      ArrayList var7 = new ArrayList(var6 - var3);
      var7.add(var2);
      AiPhysics.MotionState var8 = var2;

      for (int var9 = var3 + 1; var9 < var6; var9++) {
         AiPhysics.MoveInput var10 = this.putAt_TextPool_IoHelper.fill_TokenCore_CacheHel(var9);
         boolean var11 = var10.sprint() && var10.forward() > 0.8 && !var10.sneak() && var8.pose() == 0 && !var8.sprintCut();
         AiPhysics.MoveInput var12 = new AiPhysics.MoveInput(var10.forward(), var10.strafe(), var10.jump(), var11, var10.sneak(), var10.yaw());
         AiPhysics.MotionState var13 = AiPhysics.fill(var8, var12, var1);
         if (AiPhysics.putAt_Z(var1, var8, var13)) {
            return false;
         }

         if (var4 != null && (var13.onGround() || AiPaceScript.fill(var1, var13)) && !var4.test(BlockPos.containing(var13.x(), var13.y() + 1.0E-4, var13.z()))) {
            return false;
         }

         AiPhysics.MotionState var14 = this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(var9);
         if (var14 != null) {
            if (var13.y() < var14.y() - 1.2) {
               return false;
            }

            if (var14.onGround() && var14.vy() <= 0.1 && (!var13.onGround() || Math.abs(var13.y() - var14.y()) > 0.15)) {
               return false;
            }

            if (this.putAt_TextPool_IoHelper.putAt_Z() && var9 == var5 - 1 && Math.hypot(var13.x() - var14.x(), var13.z() - var14.z()) > 0.35) {
               return false;
            }
         }

         var7.add(var13);
         var8 = var13;
      }

      for (int var15 = 0; var15 < var7.size(); var15++) {
         this.putAt_Map.put(var3 + var15, (AiPhysics.MotionState)var7.get(var15));
      }

      return true;
   }

   static AiPhysics.MoveInput putAt_TokenCore_CacheHel_TokenCore_CacheHel_F(AiPhysics.MoveInput var0, float var1) {
      if (var0.forward() == 0.0 && var0.strafe() == 0.0) {
         return var0.withYaw(var1);
      }

      double var2 = Math.toRadians(var0.yaw());
      double var4 = -Math.sin(var2) * var0.forward() + Math.cos(var2) * var0.strafe();
      double var6 = Math.cos(var2) * var0.forward() + Math.sin(var2) * var0.strafe();
      float var8 = AiPhysics.putAt_F_D(0.0, 0.0, var4, var6);
      int[] var9 = AiMotionSafety.putAt_IArr_F_F(var1, var8);
      return new AiPhysics.MoveInput(var9[0], -var9[1], var0.jump(), var0.sprint() && var9[0] > 0, var0.sneak(), var1);
   }

   public AiPacePlayback.Inner_TableCache putAt_TableUtil_TableCac_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var1, AiPhysics.MotionState var2, Predicate<BlockPos> var3
   ) {
      return this.putAt_TableUtil_TableCac_TokenCore_TaskStat(var1, var2, var3, null);
   }

   private static int putAt_I_BlockPosArr(BlockPos[] var0, int var1, AiPhysics.MotionState var2) {
      if (var0 != null && var2 != null) {
         while (var1 < var0.length && AiPaceScript.putAt(var2, var0[var1])) {
            var1++;
         }

         return var1;
      } else {
         return var1;
      }
   }

   private AiPacePlayback.Inner_TableCache putAt_TableUtil_TableCac_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var1, AiPhysics.MotionState var2, Predicate<BlockPos> var3, Predicate<BlockPos> var4
   ) {
      this.nextBy = putAt_I_BlockPosArr(this.putAt_TextPool_IoHelper.putAt_BlockPosArr(), this.nextBy, var2);
      if (this.dataEx >= 0 && this.store >= this.dataEx && !this.prevEx_Z && !this.prev_Z) {
         this.fill_Str = this.putAt_Str != null ? this.putAt_Str : "reached safe landing for replan";
         return new AiPacePlayback.Inner_TableCache(AiPacePlayback.Inner_JsonCache.prevEx, null, this.fill_Str);
      }

      if (this.store >= this.putAt_TextPool_IoHelper.flowTemp()) {
         return new AiPacePlayback.Inner_TableCache(
            this.putAt_TextPool_IoHelper.putAt_Z() && this.store >= this.putAt_TextPool_IoHelper.flowTemp()
               ? AiPacePlayback.Inner_JsonCache.fill
               : AiPacePlayback.Inner_JsonCache.readEx,
            null,
            null
         );
      }

      boolean var5 = this.closeOf_I >= 0 && this.store >= this.closeOf_I - 10 && (!this.putAt_Z || this.store <= this.closeOf_I + 30 + 12);
      boolean var6 = false;
      boolean var7 = false;
      String var8 = null;
      if (this.store > 0) {
         AiPhysics.MotionState var9 = this.putAt_TokenCore_TableCac_I(this.store - 1);
         if (var9 != null) {
            double var10 = Math.sqrt(putAt_D_D(var2.x() - var9.x()) + putAt_D_D(var2.y() - var9.y()) + putAt_D_D(var2.z() - var9.z()));
            this.closeOf_D = Math.max(this.closeOf_D, var10);
            if (AiNavDebug.init_Z() && var10 > 0.001) {
               AiNavDebug.init(String.format(Locale.ROOT, "pace tick %d error %.4f live %s want %s", this.store, var10, var2, var9));
            }

            if (var10 > 0.05 || var2.onGround() != var9.onGround() && var10 > 0.01) {
               var8 = String.format(
                  Locale.ROOT,
                  "tick %d: live %.3f %.3f %.3f v %.3f %.3f %.3f ground %b, predicted %.3f %.3f %.3f v %.3f %.3f %.3f ground %b",
                  this.store,
                  var2.x(),
                  var2.y(),
                  var2.z(),
                  var2.vx(),
                  var2.vy(),
                  var2.vz(),
                  var2.onGround(),
                  var9.x(),
                  var9.y(),
                  var9.z(),
                  var9.vx(),
                  var9.vy(),
                  var9.vz(),
                  var9.onGround()
               );
               if (var5 && var1 != null && var10 <= 0.45) {
                  var6 = true;
                  this.fill_Map.keySet().removeIf(var1x -> var1x >= this.store);
               } else {
                  if (var1 == null
                     || !(var10 <= 0.25)
                     || !(Math.abs(var2.y() - var9.y()) <= 0.25)
                     || !(Math.abs(var1.movementSpeed() - this.state_D) <= 1.0E-4)) {
                     this.fill_Str = var8;
                     return new AiPacePlayback.Inner_TableCache(AiPacePlayback.Inner_JsonCache.value, null, this.fill_Str);
                  }

                  var7 = true;
                  this.fill_Map.keySet().removeIf(var1x -> var1x >= this.store);
               }
            }
         }
      }

      if (this.dataEx >= 0 && this.store >= this.dataEx) {
         String var27 = this.putAt_Str;
         boolean var30 = this.prevEx_Z && var1 != null && !this.value_Z && Math.abs(var1.movementSpeed() - this.state_D) <= 1.0E-4;
         boolean var31 = !var30 && this.prev_Z && var1 != null && !this.value_Z && Math.abs(var1.movementSpeed() - this.state_D) <= 1.0E-4;
         this.dataEx = -1;
         this.putAt_Str = null;
         this.prevEx_Z = false;
         this.prev_Z = false;
         if (var30) {
            AiPacePlayback.Inner_CacheHelper var34 = this.putAt_TableUtil_CacheHel_TokenCore_TaskStat(
               var1, var2, this.store, this.state_D, this.state_D, var3, var4, true, true
            );
            if (var34 != null) {
               return this.putAt_TableUtil_TableCac_TableUtil_CacheHel(var34);
            }
         } else if (var31) {
            AiPacePlayback.Inner_CacheHelper var35 = this.putAt(var1, var2, this.store, this.state_D, var3, var4);
            if (var35 != null) {
               this.pollAt_I = 0;
               return this.putAt_TableUtil_TableCac_TableUtil_CacheHel_Z(var35, false);
            }
         }

         this.fill_Str = var27 != null ? var27 : "reached safe landing for replan";
         return new AiPacePlayback.Inner_TableCache(AiPacePlayback.Inner_JsonCache.prevEx, null, this.fill_Str);
      } else {
         AiPhysics.MoveInput var26 = null;
         if (var1 != null) {
            double var28 = var1.movementSpeed();
            if (this.closeOf_I >= 0 && !this.putAt_Z) {
               boolean var32 = Math.abs(var28 - this.prev_D) <= 1.0E-4;
               boolean var37 = Math.abs(var28 - this.chunkAt_D) <= 1.0E-4;
               if (this.store < this.closeOf_I) {
                  if (var37 && this.store >= this.closeOf_I - 10) {
                     this.putAt_Z = true;
                     this.state_D = this.chunkAt_D;
                     this.fill_Map.keySet().removeIf(var1x -> var1x >= this.store);
                     AiPacePlayback.Inner_TaskState var43 = !var6
                        ? putAt(
                           var1,
                           var2,
                           this.putAt_TextPool_IoHelper,
                           this.store,
                           this.chunkAt_D,
                           24,
                           var3,
                           var4,
                           this::putAt_TokenCore_TableCac_I,
                           var1x -> this.chunkAt_D,
                           true,
                           false,
                           this.putAt_Map,
                           null,
                           1.0 / 0.0,
                           this.nextBy
                        )
                        : null;
                     if (var43 != null && var43.valid()) {
                        this.putAt_V_TokenCore_TableCacArr_Z(var43.rebased(), true);
                        this.fill_Z = true;
                     } else {
                        AiPacePlayback.Inner_CacheHelper var50 = this.putAt_TableUtil_CacheHel_TokenCore_TaskStat(
                           var1, var2, this.store, this.chunkAt_D, this.chunkAt_D, var3, var4, true, false
                        );
                        if (var50 != null) {
                           if (!var2.onGround() || !var50.afterTick().onGround()) {
                              this.pollOf = Math.max(this.pollOf, 6);
                           }

                           return this.putAt_TableUtil_TableCac_TableUtil_CacheHel(var50);
                        }

                        AiPacePlayback.Inner_TableCache var56 = this.putAt(
                           var1,
                           var2,
                           var28,
                           var43 != null
                              ? var43
                              : putAt(
                                 var1,
                                 var2,
                                 this.putAt_TextPool_IoHelper,
                                 this.store,
                                 this.chunkAt_D,
                                 24,
                                 var3,
                                 var4,
                                 this::putAt_TokenCore_TableCac_I,
                                 var1x -> this.chunkAt_D,
                                 true,
                                 false,
                                 this.putAt_Map,
                                 null,
                                 1.0 / 0.0,
                                 this.nextBy
                              ),
                           true
                        );
                        if (var56 != null) {
                           return var56;
                        }
                     }
                  } else if (var32 && !var6) {
                     if (var7) {
                        AiPacePlayback.Inner_TaskState var41 = putAt(
                           var1,
                           var2,
                           this.putAt_TextPool_IoHelper,
                           this.store,
                           this.prev_D,
                           24,
                           var3,
                           var4,
                           this::putAt_TokenCore_TableCac_I,
                           var1x -> var1x < this.closeOf_I ? this.prev_D : this.chunkAt_D,
                           true,
                           false,
                           this.putAt_Map,
                           null,
                           0.35,
                           this.nextBy
                        );
                        if (!var41.valid()) {
                           this.fill_Str = var8;
                           return new AiPacePlayback.Inner_TableCache(AiPacePlayback.Inner_JsonCache.value, null, this.fill_Str);
                        }

                        this.putAt_V_TokenCore_TableCacArr_Z(var41.rebased(), true);
                     } else if (this.store % 4 == 0 && this.store < this.putAt_TextPool_IoHelper.flowTemp()) {
                        AiPacePlayback.Inner_TaskState var42 = putAt(
                           var1,
                           var2,
                           this.putAt_TextPool_IoHelper,
                           this.store,
                           this.prev_D,
                           24,
                           var3,
                           var4,
                           this::putAt_TokenCore_TableCac_I,
                           var1x -> var1x < this.closeOf_I ? this.prev_D : this.chunkAt_D,
                           false,
                           false,
                           this.putAt_Map,
                           this.fill_Map,
                           1.0 / 0.0,
                           this.nextBy
                        );
                        if (!var42.valid()) {
                           if (var42.reason() != null && var42.reason().startsWith("terrain changed ahead")) {
                              AiPacePlayback.Inner_TaskState var49 = putAt(
                                 var1,
                                 var2,
                                 this.putAt_TextPool_IoHelper,
                                 this.store,
                                 this.prev_D,
                                 24,
                                 var3,
                                 var4,
                                 this::putAt_TokenCore_TableCac_I,
                                 var1x -> var1x < this.closeOf_I ? this.prev_D : this.chunkAt_D,
                                 true,
                                 false,
                                 this.putAt_Map,
                                 this.fill_Map,
                                 0.35,
                                 this.nextBy
                              );
                              if (var49.valid()) {
                                 this.putAt_V_TokenCore_TableCacArr_Z(var49.rebased(), false);
                              } else {
                                 AiPacePlayback.Inner_TableCache var55 = this.putAt(var1, var2, var28, var42, false);
                                 if (var55 != null) {
                                    return var55;
                                 }
                              }
                           } else {
                              AiPacePlayback.Inner_TableCache var48 = this.putAt(var1, var2, var28, var42, false);
                              if (var48 != null) {
                                 return var48;
                              }
                           }
                        }
                     }
                  } else if (this.store >= this.closeOf_I - 10) {
                     AiPacePlayback.Inner_CacheHelper var39 = this.putAt_TableUtil_CacheHel_TokenCore_TaskStat(
                        var1, var2, this.store, var28, this.chunkAt_D, var3, var4, true, false
                     );
                     if (var39 != null) {
                        return this.putAt_TableUtil_TableCac_TableUtil_CacheHel(var39);
                     }

                     if (var6) {
                        this.fill_Str = var8;
                        return new AiPacePlayback.Inner_TableCache(AiPacePlayback.Inner_JsonCache.value, null, this.fill_Str);
                     }

                     this.value_Z = true;
                     AiPacePlayback.Inner_TaskState var15 = putAt(
                        var1,
                        var2,
                        this.putAt_TextPool_IoHelper,
                        this.store,
                        this.state_D,
                        24,
                        var3,
                        var4,
                        this::putAt_TokenCore_TableCac_I,
                        null,
                        false,
                        false,
                        this.putAt_Map,
                        null,
                        1.0 / 0.0,
                        this.nextBy
                     );
                     if (!var15.valid()) {
                        AiPacePlayback.Inner_TableCache var54 = this.putAt(var1, var2, var28, var15, true);
                        if (var54 != null) {
                           return var54;
                        }
                     } else {
                        this.state_D = var28;
                        this.putAt_V_TokenCore_TableCacArr_Z(var15.rebased(), true);
                     }
                  } else {
                     this.value_Z = true;
                     AiPacePlayback.Inner_TaskState var40 = putAt(
                        var1,
                        var2,
                        this.putAt_TextPool_IoHelper,
                        this.store,
                        this.state_D,
                        24,
                        var3,
                        var4,
                        this::putAt_TokenCore_TableCac_I,
                        null,
                        false,
                        false,
                        this.putAt_Map,
                        null,
                        1.0 / 0.0,
                        this.nextBy
                     );
                     if (!var40.valid()) {
                        AiPacePlayback.Inner_TableCache var47 = this.putAt(var1, var2, var28, var40, true);
                        if (var47 != null) {
                           return var47;
                        }
                     } else {
                        this.state_D = var28;
                        this.putAt_V_TokenCore_TableCacArr_Z(var40.rebased(), true);
                        if (var40.safeUntilTick() < this.putAt_TextPool_IoHelper.flowTemp() && var40.safeUntilTick() > this.store) {
                           this.dataEx = var40.safeUntilTick();
                           this.putAt_Str = "effect changed; landed safely for replan at live speed";
                        }
                     }
                  }
               } else if (var37) {
                  this.putAt_Z = true;
                  this.state_D = this.chunkAt_D;
                  AiPhysics.MoveInput var44 = !var6 && !var2.onGround() ? this.fill_Map.remove(this.store) : null;
                  if (var44 == null || var44.sprint() && !(var44.forward() <= 0.0)) {
                     boolean var51 = this.fill_Z || var6 || this.store > this.closeOf_I || !this.fill_Map.isEmpty();
                     if (var51) {
                        this.fill_Map.keySet().removeIf(var1x -> var1x >= this.store);
                        AiPacePlayback.Inner_CacheHelper var57 = this.putAt_TableUtil_CacheHel_TokenCore_TaskStat(
                           var1, var2, this.store, this.chunkAt_D, this.chunkAt_D, var3, var4, true, true
                        );
                        if (var57 != null) {
                           if (!var2.onGround() || !var57.afterTick().onGround()) {
                              this.pollOf = Math.max(this.pollOf, 6);
                           }

                           return this.putAt_TableUtil_TableCac_TableUtil_CacheHel(var57);
                        }

                        AiPacePlayback.Inner_TaskState var62 = putAt(
                           var1,
                           var2,
                           this.putAt_TextPool_IoHelper,
                           this.store,
                           this.chunkAt_D,
                           24,
                           var3,
                           var4,
                           this::putAt_TokenCore_TableCac_I,
                           var1x -> this.chunkAt_D,
                           true,
                           false,
                           this.putAt_Map,
                           null,
                           1.0 / 0.0,
                           this.nextBy
                        );
                        if (!var62.valid()) {
                           AiPacePlayback.Inner_TableCache var65 = this.putAt(var1, var2, var28, var62, false);
                           if (var65 != null) {
                              return var65;
                           }
                        } else {
                           this.putAt_V_TokenCore_TableCacArr_Z(var62.rebased(), true);
                           this.fill_Z = true;
                        }
                     } else if (this.store % 4 == 0 && this.store < this.putAt_TextPool_IoHelper.flowTemp()) {
                        AiPacePlayback.Inner_TaskState var58 = putAt(
                           var1,
                           var2,
                           this.putAt_TextPool_IoHelper,
                           this.store,
                           this.chunkAt_D,
                           24,
                           var3,
                           var4,
                           this::putAt_TokenCore_TableCac_I,
                           var1x -> this.chunkAt_D,
                           false,
                           false,
                           this.putAt_Map,
                           this.fill_Map,
                           1.0 / 0.0,
                           this.nextBy
                        );
                        if (!var58.valid()) {
                           AiPacePlayback.Inner_TableCache var63 = this.putAt(var1, var2, var28, var58, false);
                           if (var63 != null) {
                              return var63;
                           }
                        }
                     }
                  } else {
                     var26 = var44;
                     this.pollOf = Math.max(this.pollOf, 4);
                  }
               } else if (this.store < this.closeOf_I + 30) {
                  AiPhysics.MoveInput var45 = !var6 && !var2.onGround() ? this.fill_Map.remove(this.store) : null;
                  if (var45 == null || var45.sprint() && !(var45.forward() <= 0.0)) {
                     this.fill_Map.keySet().removeIf(var1x -> var1x >= this.store);
                     AiPacePlayback.Inner_CacheHelper var52 = this.putAt_TableUtil_CacheHel_TokenCore_TaskStat(
                        var1, var2, this.store, var28, this.chunkAt_D, var3, var4, true, false
                     );
                     if (var52 != null) {
                        return this.putAt_TableUtil_TableCac_TableUtil_CacheHel(var52);
                     }

                     int var59 = this.store;
                     double var64 = var28;
                     AiPacePlayback.Inner_TaskState var66 = putAt(
                        var1,
                        var2,
                        this.putAt_TextPool_IoHelper,
                        this.store,
                        this.chunkAt_D,
                        24,
                        var3,
                        var4,
                        this::putAt_TokenCore_TableCac_I,
                        var4x -> var4x == var59 ? var64 : this.chunkAt_D,
                        true,
                        false,
                        this.putAt_Map,
                        null,
                        1.0 / 0.0,
                        this.nextBy
                     );
                     if (!var66.valid()) {
                        AiPacePlayback.Inner_TableCache var67 = this.putAt(var1, var2, var28, var66, false);
                        if (var67 != null) {
                           return var67;
                        }
                     } else {
                        this.putAt_V_TokenCore_TableCacArr_Z(var66.rebased(), true);
                        this.fill_Z = true;
                     }
                  } else {
                     var26 = var45;
                  }
               } else {
                  this.putAt_Z = true;
                  this.state_D = this.chunkAt_D;
                  this.value_Z = true;
                  this.fill_Map.clear();
                  AiPacePlayback.Inner_TaskState var46 = putAt(
                     var1,
                     var2,
                     this.putAt_TextPool_IoHelper,
                     this.store,
                     this.state_D,
                     24,
                     var3,
                     var4,
                     this::putAt_TokenCore_TableCac_I,
                     null,
                     false,
                     false,
                     this.putAt_Map,
                     null,
                     1.0 / 0.0,
                     this.nextBy
                  );
                  if (!var46.valid()) {
                     AiPacePlayback.Inner_TableCache var53 = this.putAt(var1, var2, var28, var46, true);
                     if (var53 != null) {
                        return var53;
                     }
                  } else {
                     this.state_D = var28;
                     this.putAt_V_TokenCore_TableCacArr_Z(var46.rebased(), true);
                     if (var46.safeUntilTick() < this.putAt_TextPool_IoHelper.flowTemp() && var46.safeUntilTick() > this.store) {
                        this.dataEx = var46.safeUntilTick();
                        this.putAt_Str = "effect changed; landed safely for replan at live speed";
                     }
                  }
               }
            } else {
               boolean var12 = Math.abs(var28 - this.state_D) > 1.0E-4;
               if (!var12 && !var6 && !var7) {
                  var26 = this.fill_Map.remove(this.store);
               } else {
                  this.fill_Map.keySet().removeIf(var1x -> var1x >= this.store);
               }

               if (var7) {
                  if (!var12) {
                     AiPacePlayback.Inner_CacheHelper var36 = this.putAt(var1, var2, this.store, this.state_D, var3, var4);
                     if (var36 != null) {
                        this.pollAt_I = 0;
                        return this.putAt_TableUtil_TableCac_TableUtil_CacheHel_Z(var36, false);
                     }
                  }

                  this.fill_Str = var8;
                  return new AiPacePlayback.Inner_TableCache(AiPacePlayback.Inner_JsonCache.value, null, this.fill_Str);
               }

               AiPhysics.MotionState var13 = this.store > 0 ? this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(this.store - 1) : null;
               double var14 = (this.fill_Z || this.readEx_Z) && !this.value_Z && var13 != null
                  ? fill_D_TokenCore_TableCac_TokenCore_TableCac(var2, var13)
                  : 0.0;
               if (this.readEx_Z && !this.fill_Z && !var12 && !this.value_Z && var14 <= 0.02 && this.putAt_Z_I(this.store)) {
                  this.readEx_Z = false;
                  this.pollAt_I = 0;
                  this.putAt_Map.clear();
               }

               boolean var16 = this.fill_Z && !this.value_Z && var14 > 0.02 && (var14 > 0.06 || this.tick < 60);
               if (var26 == null && !var12 && (this.pollOf > 0 || var6 || var16)) {
                  if (this.pollOf > 0) {
                     if (var2.onGround()) {
                        this.pollOf = 0;
                     } else {
                        this.pollOf--;
                     }
                  }

                  if (var16) {
                     this.tick++;
                  }

                  AiPacePlayback.Inner_CacheHelper var60 = this.putAt_TableUtil_CacheHel_TokenCore_TaskStat(
                     var1, var2, this.store, this.state_D, this.state_D, var3, var4, true, true
                  );
                  if (var60 != null) {
                     return this.putAt_TableUtil_TableCac_TableUtil_CacheHel(var60);
                  }

                  if (var6) {
                     this.fill_Str = var8;
                     return new AiPacePlayback.Inner_TableCache(AiPacePlayback.Inner_JsonCache.value, null, this.fill_Str);
                  }
               } else if (var26 == null && !var12 && this.readEx_Z && !this.fill_Z && !this.value_Z && var14 > 0.02 && this.pollAt_I < 24) {
                  this.pollAt_I++;
                  AiPacePlayback.Inner_CacheHelper var17 = this.putAt(var1, var2, this.store, this.state_D, var3, var4);
                  if (var17 != null) {
                     return this.putAt_TableUtil_TableCac_TableUtil_CacheHel_Z(var17, false);
                  }
               }

               if (var26 == null && (var12 || this.store % 4 == 0 && this.store < this.putAt_TextPool_IoHelper.flowTemp())) {
                  boolean var61 = !var12 && this.fill_Z && !this.value_Z;
                  boolean var18 = !var12 && !this.fill_Z && this.readEx_Z && !this.value_Z;
                  boolean var19 = var61 || var18;
                  double var20 = var18 ? 0.35 : 1.0 / 0.0;
                  AiPacePlayback.Inner_TaskState var22 = putAt(
                     var1,
                     var2,
                     this.putAt_TextPool_IoHelper,
                     this.store,
                     this.state_D,
                     24,
                     var3,
                     var4,
                     this::putAt_TokenCore_TableCac_I,
                     null,
                     false,
                     var19,
                     this.putAt_Map,
                     this.fill_Map,
                     var20,
                     this.nextBy
                  );
                  if (!var22.valid()) {
                     if (var61) {
                        AiPacePlayback.Inner_CacheHelper var68 = this.putAt_TableUtil_CacheHel_TokenCore_TaskStat(
                           var1, var2, this.store, this.state_D, this.state_D, var3, var4, true, true
                        );
                        if (var68 != null) {
                           return this.putAt_TableUtil_TableCac_TableUtil_CacheHel(var68);
                        }
                     } else if (!var12
                        && var22.reason() != null
                        && (
                           var22.reason().startsWith("terrain changed ahead")
                              || var22.reason().startsWith("drift exceeded")
                              || var22.reason().startsWith("transition speed shift")
                              || var22.reason().startsWith("rebase missed")
                        )) {
                        AiPacePlayback.Inner_CacheHelper var23 = this.putAt(var1, var2, this.store, this.state_D, var3, var4);
                        if (var23 != null) {
                           this.pollAt_I = 0;
                           return this.putAt_TableUtil_TableCac_TableUtil_CacheHel_Z(var23, false);
                        }
                     }

                     if (var12) {
                        this.value_Z = true;
                     }

                     boolean var69 = var61
                        && !var12
                        && var22.reason() != null
                        && (var22.reason().startsWith("transition speed shift") || var22.reason().startsWith("rebase missed"));
                     boolean var24 = !var69
                        && !var12
                        && var22.reason() != null
                        && (
                           var22.reason().startsWith("terrain changed ahead")
                              || var22.reason().startsWith("drift exceeded")
                              || var22.reason().startsWith("transition speed shift")
                              || var22.reason().startsWith("rebase missed")
                        );
                     AiPacePlayback.Inner_TableCache var25 = this.putAt_TableUtil_TableCac_TokenCore_TaskStat(var1, var2, var28, var22, var12, var69, var24);
                     if (var25 != null) {
                        return var25;
                     }
                  } else if (var12) {
                     this.value_Z = true;
                     this.state_D = var28;
                     this.putAt_V_TokenCore_TableCacArr_Z(var22.rebased(), true);
                     if (var22.safeUntilTick() < this.putAt_TextPool_IoHelper.flowTemp() && var22.safeUntilTick() > this.store) {
                        this.dataEx = var22.safeUntilTick();
                        this.putAt_Str = "effect changed; landed safely for replan at live speed";
                     }
                  } else if (var19) {
                     this.putAt_V_TokenCore_TableCacArr_Z(var22.rebased(), false);
                  }
               }
            }
         }

         AiPhysics.MoveInput var29 = this.putAt_TextPool_IoHelper.putAt_TokenCore_CacheHel_I(this.store);
         if (var29 == null) {
            return new AiPacePlayback.Inner_TableCache(
               this.putAt_TextPool_IoHelper.putAt_Z() ? AiPacePlayback.Inner_JsonCache.fill : AiPacePlayback.Inner_JsonCache.readEx, null, null
            );
         } else if (var26 != null) {
            this.store++;
            this.prev_F = var26.yaw();
            return new AiPacePlayback.Inner_TableCache(AiPacePlayback.Inner_JsonCache.putAt_TableUtil_JsonCach, var26, null);
         } else {
            AiPhysics.MotionState var11 = this.store > 0 ? this.putAt_TokenCore_TableCac_I(this.store - 1) : var2;
            boolean var33 = var29.sprint() && var29.forward() > 0.8 && !var29.sneak() && var11 != null && var11.pose() == 0 && !var11.sprintCut();
            this.store++;
            AiPhysics.MoveInput var38 = new AiPhysics.MoveInput(
               var29.forward(), var29.strafe(), var29.jump(), var33, var29.sneak(), var29.yaw()
            );
            this.prev_F = var38.yaw();
            return new AiPacePlayback.Inner_TableCache(AiPacePlayback.Inner_JsonCache.putAt_TableUtil_JsonCach, var38, null);
         }
      }
   }

   private void putAt_V_TokenCore_TableCacArr_Z(AiPhysics.MotionState[] var1, boolean var2) {
      this.putAt_V_I(this.store, var1, var2);
   }

   private void putAt_V_I(int var1, AiPhysics.MotionState[] var2, boolean var3) {
      if (var2 != null) {
         if (var3) {
            this.putAt_Map.keySet().removeIf(var1x -> var1x >= var1);
         }

         for (int var4 = 0; var4 < var2.length; var4++) {
            this.putAt_Map.put(var1 + var4, var2[var4]);
         }
      }
   }

   private void putAt_V_I_TableUtil_CacheHel(int var1, AiPacePlayback.Inner_CacheHelper var2) {
      int var3 = var1 + var2.tickDelta();
      int var4 = Math.min(var1, var3);
      this.putAt_Map.keySet().removeIf(var1x -> var1x >= var4);
      this.fill_Map.keySet().removeIf(var1x -> var1x >= var1);
      this.putAt_Map.put(var3, var2.afterTick());
      this.putAt_V_I(var3 + 1, var2.tailRebased(), false);
      AiPhysics.MoveInput[] var5 = var2.futureOverrides();
      if (var5 != null) {
         for (int var6 = 0; var6 < var5.length; var6++) {
            if (var5[var6] != null) {
               this.fill_Map.put(var3 + 1 + var6, var5[var6]);
            }
         }
      }
   }

   private AiPacePlayback.Inner_TableCache putAt_TableUtil_TableCac_TableUtil_CacheHel(AiPacePlayback.Inner_CacheHelper var1) {
      return this.putAt_TableUtil_TableCac_TableUtil_CacheHel_Z(var1, true);
   }

   private AiPacePlayback.Inner_TableCache putAt_TableUtil_TableCac_TableUtil_CacheHel_Z(AiPacePlayback.Inner_CacheHelper var1, boolean var2) {
      int var3 = this.store;
      this.putAt_V_I_TableUtil_CacheHel(var3, var1);
      if (var2) {
         this.fill_Z = true;
      } else {
         this.readEx_Z = true;
      }

      this.dataEx = -1;
      this.putAt_Str = null;
      this.prevEx_Z = false;
      this.prev_Z = false;
      if (var1.tickDelta() != 0) {
         this.flushOf++;
         this.stateOf = var3;
      }

      if (var1.tickDelta() >= 0) {
         this.putAt_TextPool_IoHelper.putAt_TokenCore_CacheHel_I(var3);
      }

      if (var1.tickDelta() > 0) {
         this.putAt_TextPool_IoHelper.putAt_TokenCore_CacheHel_I(var3 + 1);
      }

      this.store = var3 + 1 + var1.tickDelta();
      this.prev_F = var1.input().yaw();
      return new AiPacePlayback.Inner_TableCache(AiPacePlayback.Inner_JsonCache.putAt_TableUtil_JsonCach, var1.input(), null);
   }

   private boolean putAt_Z_I(int var1) {
      for (Entry var3 : this.putAt_Map.entrySet()) {
         int var4 = (Integer)var3.getKey();
         if (var4 >= var1) {
            AiPhysics.MotionState var5 = this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(var4);
            if (var5 != null && putAt_D_TokenCore_TableCac_TokenCore_TableCac((AiPhysics.MotionState)var3.getValue(), var5) > 0.04000000000000001) {
               return false;
            }
         }
      }

      return true;
   }

   private AiPacePlayback.Inner_CacheHelper putAt(
      AiPhysics.PhysicsWorld var1, AiPhysics.MotionState var2, int var3, double var4, Predicate<BlockPos> var6, Predicate<BlockPos> var7
   ) {
      int var8 = this.putAt_TextPool_IoHelper.flowTemp();
      if (var3 >= var8) {
         return null;
      }

      AiPacePlayback.Inner_CacheHelper var9 = this.putAt(var1, var2, var3, 0, var4, var4, var6, var7, true, 0.0, 0.35);
      return var9 != null && !var9.partial() ? var9 : null;
   }

   private static double putAt_D_TokenCore_TableCac_TokenCore_TableCac(AiPhysics.MotionState var0, AiPhysics.MotionState var1) {
      return Math.sqrt(putAt_D_D(var0.x() - var1.x()) + putAt_D_D(var0.y() - var1.y()) + putAt_D_D(var0.z() - var1.z()));
   }

   private static double fill_D_TokenCore_TableCac_TokenCore_TableCac(AiPhysics.MotionState var0, AiPhysics.MotionState var1) {
      if (var0 != null && var1 != null) {
         double var2 = putAt_D_TokenCore_TableCac_TokenCore_TableCac(var0, var1);
         double var4 = Math.hypot(var0.vx() - var1.vx(), var0.vz() - var1.vz());
         return var2 + 1.5 * var4;
      } else {
         return 0.0;
      }
   }

   private float putAt_F_F(float var1, float var2, float var3) {
      float var4 = Math.clamp(Mth.wrapDegrees(var2 - var1), -var3, var3);
      float var5 = var1 + var4;
      if (!Float.isNaN(this.prev_F)) {
         float var6 = Math.abs(Mth.wrapDegrees(var1 - this.prev_F));
         float var7 = Math.min(40.0F, Math.max(24.0F, var6 + 10.0F));
         float var8 = Mth.wrapDegrees(var5 - this.prev_F);
         if (Math.abs(var8) > var7) {
            var5 = this.prev_F + Math.copySign(var7, var8);
         }
      }

      return var5;
   }

   private AiPacePlayback.Inner_CacheHelper putAt_TableUtil_CacheHel_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var1,
      AiPhysics.MotionState var2,
      int var3,
      double var4,
      double var6,
      Predicate<BlockPos> var8,
      Predicate<BlockPos> var9,
      boolean var10,
      boolean var11
   ) {
      int var12 = this.putAt_TextPool_IoHelper.flowTemp();
      if (var3 >= var12) {
         return null;
      }

      AiPacePlayback.Inner_CacheHelper var13 = this.putAt(var1, var2, var3, 0, var4, var6, var8, var9, var10, 0.0);
      AiPacePlayback.Inner_CacheHelper var14 = var13 != null && !var13.partial() ? var13 : null;
      AiPacePlayback.Inner_CacheHelper var15 = var13 != null && var13.partial() ? var13 : null;
      double var16 = var14 != null ? this.putAt_D_TableUtil_CacheHel(var14, var3, var12, 0.0) : 1.0 / 0.0;
      if (this.flushOf < 4) {
         AiPhysics.MotionState var18 = var3 > 0 ? this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(var3 - 1) : null;
         double var19 = var18 != null ? putAt_D_TokenCore_TableCac_TokenCore_TableCac(var2, var18) : 0.0;
         if (var11 && this.putAt_Z && var3 >= 2 && var3 != this.stateOf && var2.onGround()) {
            AiPhysics.MotionState var21 = this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(var3 - 2);
            double var22 = putAt_D_TokenCore_TableCac(var2, var18, var21, var18);
            boolean var24 = var22 < -0.01;
            boolean var25 = var24
               && (var14 == null || var21 != null && var19 > 0.14 && putAt_D_TokenCore_TableCac_TokenCore_TableCac(var2, var21) < var19 - 0.08);
            if (var25) {
               AiPacePlayback.Inner_CacheHelper var26 = this.putAt(var1, var2, var3, -1, var4, var6, var8, var9, var10, 0.035);
               if (var26 != null && !var26.partial()) {
                  double var27 = this.putAt_D_TableUtil_CacheHel(var26, var3, var12, 0.035);
                  if (var14 == null || var27 < var16) {
                     var14 = var26;
                     var16 = var27;
                  }
               }
            }
         }

         boolean var30 = var3 >= this.closeOf_I && var3 + 1 < var12 && (!this.putAt_TextPool_IoHelper.putAt_Z() || var3 + 2 < var12);
         if (var30) {
            AiPhysics.MotionState var31 = this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(var3);
            double var23 = putAt_D_TokenCore_TableCac(var2, var18, var18, var31);
            boolean var32 = var23 > 0.01;
            boolean var33 = var14 == null && (var11 || var3 >= this.closeOf_I + 1) && var23 > -0.02
               || var11
                  && this.putAt_Z
                  && var2.onGround()
                  && var32
                  && var31 != null
                  && var19 > 0.14
                  && putAt_D_TokenCore_TableCac_TokenCore_TableCac(var2, var31) < var19 - 0.08;
            if (var33) {
               AiPacePlayback.Inner_CacheHelper var34 = this.putAt(var1, var2, var3, 1, var4, var6, var8, var9, var10, 0.035);
               if (var34 != null && !var34.partial()) {
                  double var28 = this.putAt_D_TableUtil_CacheHel(var34, var3, var12, 0.035);
                  if (var14 == null || var28 < var16) {
                     var14 = var34;
                  }
               }
            }
         }
      }

      return var14 != null ? var14 : var15;
   }

   private static double putAt_D_TokenCore_TableCac(
      AiPhysics.MotionState var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2, AiPhysics.MotionState var3
   ) {
      if (var1 != null && var2 != null && var3 != null) {
         double var4 = var3.x() - var2.x();
         double var6 = var3.z() - var2.z();
         double var8 = Math.hypot(var4, var6);
         return var8 < 1.0E-4 ? 0.0 : ((var0.x() - var1.x()) * var4 + (var0.z() - var1.z()) * var6) / var8;
      } else {
         return 0.0;
      }
   }

   private double putAt_D_TableUtil_CacheHel(AiPacePlayback.Inner_CacheHelper var1, int var2, int var3, double var4) {
      int var6 = var2 + var1.tickDelta();
      AiPhysics.MotionState var7 = this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(var6);
      double var8 = var1.futureOverrides() != null ? 0.015 * var1.futureOverrides().length : 0.0;
      return this.putAt(var1.afterTick(), var7, var1.tailRebased(), var6 + 1, var3) + var8 + var4;
   }

   private double putAt(AiPhysics.MotionState var1, AiPhysics.MotionState var2, AiPhysics.MotionState[] var3, int var4, int var5) {
      double var6 = var2 != null ? fill_D_TokenCore_TableCac_TokenCore_TableCac(var1, var2) : 0.0;
      if (var3 != null && var3.length != 0) {
         int var8 = var3.length - 1;
         int var9 = Math.min(5, var8);
         AiPhysics.MotionState var10 = var4 + var9 < var5 ? this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(var4 + var9) : null;
         double var11 = var10 != null ? fill_D_TokenCore_TableCac_TokenCore_TableCac(var3[var9], var10) : var6;
         int var13 = Math.min(12, var8);
         AiPhysics.MotionState var14 = var4 + var13 < var5 ? this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(var4 + var13) : null;
         double var15 = var14 != null ? fill_D_TokenCore_TableCac_TokenCore_TableCac(var3[var13], var14) : var11;
         return 0.15 * var6 + 0.55 * var11 + 0.3 * var15;
      } else {
         return var6;
      }
   }

   private AiPacePlayback.Inner_CacheHelper putAt(
      AiPhysics.PhysicsWorld var1,
      AiPhysics.MotionState var2,
      int var3,
      int var4,
      double var5,
      double var7,
      Predicate<BlockPos> var9,
      Predicate<BlockPos> var10,
      boolean var11,
      double var12
   ) {
      return this.putAt(var1, var2, var3, var4, var5, var7, var9, var10, var11, var12, 1.0 / 0.0);
   }

   private AiPacePlayback.Inner_CacheHelper putAt(
      AiPhysics.PhysicsWorld var1,
      AiPhysics.MotionState var2,
      int var3,
      int var4,
      double var5,
      double var7,
      Predicate<BlockPos> var9,
      Predicate<BlockPos> var10,
      boolean var11,
      double var12,
      double var14
   ) {
      int var16 = this.putAt_TextPool_IoHelper.flowTemp();
      int var17 = var3 + var4;
      int var18 = var17 + 1;
      if (var17 >= 0 && var17 < var16) {
         AiPhysics.MoveInput var19 = this.putAt_TextPool_IoHelper.fill_TokenCore_CacheHel(var17);
         AiPhysics.MotionState var20 = this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(var17);
         boolean var21 = var4 < 0;
         boolean var22 = !var21 && var19.jump();
         double var23 = var21 && var19.forward() <= 0.0 && var19.strafe() == 0.0 ? 1.0 : var19.forward();
         double var25 = var21 && var19.forward() <= 0.0 && var19.strafe() == 0.0 ? 0.0 : var19.strafe();
         float var27 = var19.yaw();
         if (var21 && var20 != null && Math.hypot(var20.x() - var2.x(), var20.z() - var2.z()) > 0.08) {
            float var28 = AiPhysics.putAt_F_D(var2.x(), var2.z(), var20.x(), var20.z());
            if (Math.abs(Mth.wrapDegrees(var28 - var19.yaw())) <= 45.0F) {
               var27 = this.putAt_F_F(var19.yaw(), var28, 20.0F);
            }
         }

         boolean var72 = var23 > 0.8 && !var19.sneak() && var2.pose() == 0 && !var2.sprintCut();
         AiPhysics.MoveInput var29 = new AiPhysics.MoveInput(var23, var25, var22, (var19.sprint() || var21) && var72, var19.sneak(), var27);
         int var30 = this.closeOf_I > 0 && var17 < this.closeOf_I ? this.closeOf_I - 1 : var16 - 1;
         AiPhysics.MotionState var31 = this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(Math.min(var17 + 3, var30));
         float var32 = var29.yaw();
         if (!var29.sneak() && var29.forward() > 0.0 && var31 != null && Math.hypot(var31.x() - var2.x(), var31.z() - var2.z()) > 0.2) {
            float var33 = AiPhysics.putAt_F_D(var2.x(), var2.z(), var31.x(), var31.z());
            var32 = this.putAt_F_F(var29.yaw(), var33, var2.onGround() ? 18.0F : 14.0F);
         }

         boolean var73 = Math.abs(Mth.wrapDegrees(var32 - var29.yaw())) >= 1.5F;
         double var34 = var20 != null ? Math.hypot(var2.x() - var20.x(), var2.z() - var20.z()) : 0.0;
         ArrayList<AiPhysics.MoveInput> var36 = new ArrayList<>(16);
         var36.add(var29);
         if (var2.onGround()) {
            if (var29.sprint() && !var29.sneak()) {
               var36.add(new AiPhysics.MoveInput(var29.forward(), var29.strafe(), var29.jump(), false, false, var29.yaw()));
            }

            if (!var29.sprint() && var72) {
               var36.add(new AiPhysics.MoveInput(var29.forward(), var29.strafe(), var29.jump(), true, false, var29.yaw()));
            }

            if (!var29.jump() && !var29.sneak() && (var29.forward() != 0.0 || var29.strafe() != 0.0)) {
               var36.add(new AiPhysics.MoveInput(0.0, 0.0, false, false, false, var29.yaw()));
               if (var2.speed() > 0.03) {
                  AiPhysics.MoveInput var37 = AiPaceScript.putAt(var29.yaw(), var2);
                  if (var37.forward() != 0.0 || var37.strafe() != 0.0) {
                     var36.add(var37);
                  }
               }
            }

            if (!var29.sneak() && var29.forward() > 0.0) {
               boolean var74 = this.closeOf_I < 0 || var17 >= this.closeOf_I;
               if (var73) {
                  var36.add(new AiPhysics.MoveInput(var29.forward(), var29.strafe(), var29.jump(), var29.sprint(), false, var32));
                  if (var29.sprint()) {
                     var36.add(new AiPhysics.MoveInput(var29.forward(), var29.strafe(), var29.jump(), false, false, var32));
                  } else if (var72) {
                     var36.add(new AiPhysics.MoveInput(var29.forward(), var29.strafe(), var29.jump(), true, false, var32));
                  }
               }

               if (var74 && (var29.jump() || var34 > 0.025)) {
                  float var38 = this.putAt_F_F(var29.yaw(), var29.yaw() - 6.0F, 18.0F);
                  float var39 = this.putAt_F_F(var29.yaw(), var29.yaw() + 6.0F, 18.0F);
                  var36.add(new AiPhysics.MoveInput(var29.forward(), var29.strafe(), var29.jump(), var29.sprint(), false, var38));
                  var36.add(new AiPhysics.MoveInput(var29.forward(), var29.strafe(), var29.jump(), var29.sprint(), false, var39));
               }

               if (var74 && (var29.jump() || var34 > 0.06)) {
                  float var77 = this.putAt_F_F(var29.yaw(), var29.yaw() - 12.0F, 18.0F);
                  float var80 = this.putAt_F_F(var29.yaw(), var29.yaw() + 12.0F, 18.0F);
                  var36.add(new AiPhysics.MoveInput(var29.forward(), var29.strafe(), var29.jump(), var29.sprint(), false, var77));
                  var36.add(new AiPhysics.MoveInput(var29.forward(), var29.strafe(), var29.jump(), var29.sprint(), false, var80));
               }

               if (var74 && !var29.jump() && var34 > 0.04) {
                  var36.add(new AiPhysics.MoveInput(var29.forward(), 1.0, false, var29.sprint(), false, var29.yaw()));
                  var36.add(new AiPhysics.MoveInput(var29.forward(), -1.0, false, var29.sprint(), false, var29.yaw()));
               }
            }
         } else {
            if (var29.sprint()) {
               var36.add(new AiPhysics.MoveInput(var29.forward(), var29.strafe(), false, false, var29.sneak(), var29.yaw()));
            } else if (var19.forward() > 0.8 && !var29.sneak()) {
               var36.add(new AiPhysics.MoveInput(var29.forward(), var29.strafe(), false, true, false, var29.yaw()));
            }

            if (var29.forward() != 0.0 || var29.strafe() != 0.0) {
               var36.add(new AiPhysics.MoveInput(0.0, 0.0, false, false, var29.sneak(), var29.yaw()));
            }

            if (!var29.sneak()) {
               var36.add(new AiPhysics.MoveInput(-1.0, 0.0, false, false, false, var29.yaw()));
            }

            if (!var29.sneak() && var29.forward() > 0.0 && var73) {
               var36.add(new AiPhysics.MoveInput(var29.forward(), var29.strafe(), false, var29.sprint(), false, var32));
               if (var29.sprint()) {
                  var36.add(new AiPhysics.MoveInput(var29.forward(), var29.strafe(), false, false, false, var32));
               }
            }

            if (var34 > 0.03) {
               var36.add(new AiPhysics.MoveInput(var29.forward(), 1.0, false, var29.sprint(), var29.sneak(), var29.yaw()));
               var36.add(new AiPhysics.MoveInput(var29.forward(), -1.0, false, var29.sprint(), var29.sneak(), var29.yaw()));
               if (!var29.sneak() && var29.forward() > 0.0) {
                  float var75 = this.putAt_F_F(var29.yaw(), var29.yaw() - 6.0F, 14.0F);
                  float var78 = this.putAt_F_F(var29.yaw(), var29.yaw() + 6.0F, 14.0F);
                  var36.add(new AiPhysics.MoveInput(var29.forward(), var29.strafe(), false, var29.sprint(), false, var75));
                  var36.add(new AiPhysics.MoveInput(var29.forward(), var29.strafe(), false, var29.sprint(), false, var78));
               }
            }
         }

         int var76 = 24 + (var4 < 0 ? 1 : 0);
         AiPhysics.PhysicsWorld var79 = AiPaceScript.fill(var1, var5);
         BlockPos[] var81 = this.putAt_TextPool_IoHelper.putAt_BlockPosArr();
         int var40 = putAt_I_BlockPosArr(var81, this.nextBy, var2);
         AiPacePlayback.Inner_CacheHelper var41 = null;
         double var42 = 1.0 / 0.0;
         AiPacePlayback.Inner_CacheHelper var44 = null;
         double var45 = 1.0 / 0.0;

         for (int var47 = 0; var47 < var36.size(); var47++) {
            AiPhysics.MoveInput var48 = (AiPhysics.MoveInput)var36.get(var47);
            AiPhysics.MotionState var49 = AiPhysics.fill(var2, var48, var79);
            if (this.putAt_Z_TokenCore_TaskStat(var1, var2, var49, var20, var17, var16, var9, var10, var14)) {
               int var50 = putAt_I_BlockPosArr(var81, var40, var49);
               AiPhysics.MotionState[] var51 = null;
               AiPhysics.MoveInput[] var52 = null;
               double var53 = 0.0;
               AiPacePlayback.Inner_TaskState var55 = putAt(
                  var1,
                  var49,
                  this.putAt_TextPool_IoHelper,
                  var18,
                  var7,
                  var76,
                  var9,
                  var10,
                  this::putAt_TokenCore_TableCac_I,
                  var2x -> var7,
                  true,
                  false,
                  this.putAt_Map,
                  null,
                  var14,
                  var50
               );
               if (var55.valid()) {
                  var51 = var55.rebased();
               } else {
                  if (var4 == 0
                     && var55.safeLanding() != null
                     && var55.safeUntilTick() > var18
                     && var55.rebased() != null
                     && var55.reason() != null
                     && (var55.reason().startsWith("transition speed shift") || var55.reason().startsWith("rebase missed"))) {
                     int var56 = Math.min(var55.rebased().length, var55.safeUntilTick() - var18);
                     if (var56 > 0) {
                        AiPhysics.MotionState[] var57 = Arrays.copyOf(var55.rebased(), var56);
                        double var58 = 3.0E-4 * Math.abs(Mth.wrapDegrees(var48.yaw() - var29.yaw()));
                        double var60 = this.putAt(var49, var20, var57, var18, var16) + (var47 == 0 ? 0.0 : 0.003) + var58;
                        if (var60 < var45) {
                           var45 = var60;
                           var44 = new AiPacePlayback.Inner_CacheHelper(var48, var49, var57, null, 0, true);
                        }
                     }
                  }

                  if (var11 && !var49.onGround() && var18 < var16) {
                     int[][] var90 = new int[][]{
                        {0},
                        {0, 0},
                        {1},
                        {1, 0},
                        {1, 1},
                        {1, 1, 0},
                        {1, 1, 1},
                        {2},
                        {2, 0},
                        {2, 1},
                        {2, 2},
                        {2, 1, 0},
                        {3},
                        {3, 3},
                        {4},
                        {5},
                        {6},
                        {7},
                        {4, 4},
                        {5, 5},
                        {6, 6},
                        {7, 7}
                     };
                     AiPhysics.PhysicsWorld var93 = AiPaceScript.fill(var1, var7);

                     for (int[] var61 : var90) {
                        if (var18 + var61.length <= var16) {
                           AiPhysics.MotionState var62 = var49;
                           int var63 = var50;
                           ArrayList<AiPhysics.MotionState> var64 = new ArrayList<>(var61.length);
                           AiPhysics.MoveInput[] var65 = new AiPhysics.MoveInput[var61.length];
                           boolean var66 = true;

                           for (int var67 = 0; var67 < var61.length; var67++) {
                              int var68 = var18 + var67;
                              AiPhysics.MoveInput var69 = this.putAt_TextPool_IoHelper.fill_TokenCore_CacheHel(var68);
                              AiPhysics.MoveInput var10000;
                              switch (var61[var67]) {
                                 case 1:
                                    var10000 = new AiPhysics.MoveInput(0.0, 0.0, false, false, var69.sneak(), var69.yaw());
                                    break;
                                 case 2:
                                    var10000 = new AiPhysics.MoveInput(-1.0, 0.0, false, false, var69.sneak(), var69.yaw());
                                    break;
                                 case 3:
                                    var10000 = new AiPhysics.MoveInput(1.0, var69.strafe(), false, true, false, var69.yaw());
                                    break;
                                 case 4:
                                    var10000 = new AiPhysics.MoveInput(var69.forward(), 1.0, false, var69.sprint(), var69.sneak(), var69.yaw());
                                    break;
                                 case 5:
                                    var10000 = new AiPhysics.MoveInput(var69.forward(), -1.0, false, var69.sprint(), var69.sneak(), var69.yaw());
                                    break;
                                 case 6:
                                    var10000 = new AiPhysics.MoveInput(var69.forward(), 1.0, false, false, var69.sneak(), var69.yaw());
                                    break;
                                 case 7:
                                    var10000 = new AiPhysics.MoveInput(var69.forward(), -1.0, false, false, var69.sneak(), var69.yaw());
                                    break;
                                 default:
                                    var10000 = new AiPhysics.MoveInput(var69.forward(), var69.strafe(), false, false, var69.sneak(), var69.yaw());
                              }

                              AiPhysics.MoveInput var70 = var10000;
                              AiPhysics.MotionState var71 = AiPhysics.fill(var62, var70, var93);
                              if (!this.putAt_Z_TokenCore_TaskStat(
                                 var1, var62, var71, this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(var68), var68, var16, var9, var10, var14
                              )) {
                                 var66 = false;
                                 break;
                              }

                              var64.add(var71);
                              var65[var67] = var70;
                              var62 = var71;
                              var63 = putAt_I_BlockPosArr(var81, var63, var62);
                              if (var62.onGround() && var67 + 1 < var61.length) {
                                 var66 = false;
                                 break;
                              }
                           }

                           if (var66) {
                              AiPacePlayback.Inner_TaskState var105 = putAt(
                                 var1,
                                 var62,
                                 this.putAt_TextPool_IoHelper,
                                 var18 + var61.length,
                                 var7,
                                 var76,
                                 var9,
                                 var10,
                                 this::putAt_TokenCore_TableCac_I,
                                 var2x -> var7,
                                 true,
                                 false,
                                 this.putAt_Map,
                                 null,
                                 var14,
                                 var63
                              );
                              if (var105.valid()) {
                                 var51 = new AiPhysics.MotionState[var64.size() + var105.rebased().length];

                                 for (int var106 = 0; var106 < var64.size(); var106++) {
                                    var51[var106] = (AiPhysics.MotionState)var64.get(var106);
                                 }

                                 System.arraycopy(var105.rebased(), 0, var51, var64.size(), var105.rebased().length);
                                 var52 = var65;
                                 var53 = 0.015 * var61.length;
                                 break;
                              }
                           }
                        }
                     }
                  }
               }

               if (var51 != null) {
                  double var91 = 3.0E-4 * Math.abs(Mth.wrapDegrees(var48.yaw() - var29.yaw()));
                  double var97 = this.putAt(var49, var20, var51, var18, var16) + var53 + var12 + (var47 == 0 ? 0.0 : 0.003) + var91;
                  if (var97 < var42) {
                     var42 = var97;
                     var41 = new AiPacePlayback.Inner_CacheHelper(var48, var49, var51, var52, var4, false);
                  }
               }
            }
         }

         if (var41 == null && var11 && var2.onGround() && var18 + 1 < var16) {
            AiPhysics.PhysicsWorld var82 = AiPaceScript.fill(var1, var7);
            AiPhysics.MoveInput var83 = this.putAt_TextPool_IoHelper.fill_TokenCore_CacheHel(var18);
            AiPhysics.MotionState var84 = this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(var18);
            AiPhysics.MotionState var85 = this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(Math.min(var18 + 3, var16 - 1));

            for (int var86 = 0; var86 < var36.size(); var86++) {
               AiPhysics.MoveInput var87 = (AiPhysics.MoveInput)var36.get(var86);
               AiPhysics.MotionState var88 = AiPhysics.fill(var2, var87, var79);
               if (var88.onGround() && this.putAt_Z_TokenCore_TaskStat(var1, var2, var88, var20, var17, var16, var9, var10, var14)) {
                  boolean var54 = var83.forward() > 0.8 && !var83.sneak() && var88.pose() == 0 && !var88.sprintCut();
                  AiPhysics.MoveInput var89 = new AiPhysics.MoveInput(
                     var83.forward(), var83.strafe(), var83.jump(), var83.sprint() && var54, var83.sneak(), var83.yaw()
                  );
                  ArrayList<AiPhysics.MoveInput> var92 = new ArrayList<>(7);
                  if (var89.sprint() && !var89.sneak()) {
                     var92.add(new AiPhysics.MoveInput(var89.forward(), var89.strafe(), var89.jump(), false, false, var89.yaw()));
                  }

                  if (!var89.jump() && !var89.sneak() && (var89.forward() != 0.0 || var89.strafe() != 0.0)) {
                     var92.add(new AiPhysics.MoveInput(0.0, 0.0, false, false, false, var89.yaw()));
                  }

                  if (!var89.sneak() && var89.forward() > 0.0 && var85 != null && Math.hypot(var85.x() - var88.x(), var85.z() - var88.z()) > 0.2) {
                     float var94 = var89.yaw()
                        + Math.clamp(Mth.wrapDegrees(AiPhysics.putAt_F_D(var88.x(), var88.z(), var85.x(), var85.z()) - var89.yaw()), -18.0F, 18.0F);
                     var92.add(new AiPhysics.MoveInput(var89.forward(), var89.strafe(), var89.jump(), var89.sprint(), false, var94));
                     if (var89.sprint()) {
                        var92.add(new AiPhysics.MoveInput(var89.forward(), var89.strafe(), var89.jump(), false, false, var94));
                     }
                  }

                  if ((this.closeOf_I < 0 || var18 >= this.closeOf_I) && !var89.jump() && !var89.sneak() && var89.forward() > 0.0) {
                     var92.add(new AiPhysics.MoveInput(var89.forward(), 1.0, false, var89.sprint(), false, var89.yaw()));
                     var92.add(new AiPhysics.MoveInput(var89.forward(), -1.0, false, var89.sprint(), false, var89.yaw()));
                  }

                  for (AiPhysics.MoveInput var98 : var92) {
                     AiPhysics.MotionState var99 = AiPhysics.fill(var88, var98, var82);
                     if (this.putAt_Z_TokenCore_TaskStat(var1, var88, var99, var84, var18, var16, var9, var10, var14)) {
                        int var101 = putAt_I_BlockPosArr(var81, putAt_I_BlockPosArr(var81, var40, var88), var99);
                        AiPacePlayback.Inner_TaskState var102 = putAt(
                           var1,
                           var99,
                           this.putAt_TextPool_IoHelper,
                           var18 + 1,
                           var7,
                           var76,
                           var9,
                           var10,
                           this::putAt_TokenCore_TableCac_I,
                           var2x -> var7,
                           true,
                           false,
                           this.putAt_Map,
                           null,
                           var14,
                           var101
                        );
                        if (var102.valid()) {
                           AiPhysics.MotionState[] var103 = new AiPhysics.MotionState[1 + var102.rebased().length];
                           var103[0] = var99;
                           System.arraycopy(var102.rebased(), 0, var103, 1, var102.rebased().length);
                           double var104 = this.putAt(var88, var20, var103, var18, var16) + 0.02 + var12;
                           if (var104 < var42) {
                              var42 = var104;
                              var41 = new AiPacePlayback.Inner_CacheHelper(var87, var88, var103, new AiPhysics.MoveInput[]{var98}, var4, false);
                           }
                        }
                     }
                  }
               }
            }
         }

         return var41 != null ? var41 : var44;
      } else {
         return null;
      }
   }

   private static boolean putAt_Z_TextPool_IoHelper(AiPaceScript.Inner_IoHelper var0, int var1, AiPhysics.MotionState var2, double var3) {
      if (Math.abs(var3 - var2.y()) <= 0.15) {
         return true;
      }

      if (var1 > 0) {
         AiPhysics.MotionState var5 = var0.putAt_TokenCore_TableCac_I(var1 - 1);
         if (var5 != null && var5.onGround() && var5.vy() <= 0.1 && Math.abs(var5.y() - var2.y()) <= 0.65 && Math.abs(var3 - var5.y()) <= 0.15) {
            return true;
         }
      }

      AiPhysics.MotionState var6 = var0.putAt_TokenCore_TableCac_I(var1 + 1);
      return var6 != null && var6.onGround() && var6.vy() <= 0.1 && Math.abs(var6.y() - var2.y()) <= 0.65 && Math.abs(var3 - var6.y()) <= 0.15;
   }

   private boolean putAt_Z_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var1,
      AiPhysics.MotionState var2,
      AiPhysics.MotionState var3,
      AiPhysics.MotionState var4,
      int var5,
      int var6,
      Predicate<BlockPos> var7,
      Predicate<BlockPos> var8,
      double var9
   ) {
      if (var7 != null || var8 != null) {
         BlockPos var11 = BlockPos.containing(var3.x(), var3.y() + 1.0E-4, var3.z());
         if (var7 != null && (!var7.test(var11) || !var7.test(var11.below()))) {
            return false;
         }

         if (var8 != null && (var3.onGround() || AiPaceScript.fill(var1, var3)) && !var8.test(var11)) {
            return false;
         }
      }

      if (AiPhysics.putAt_Z(var1, var2, var3)) {
         return false;
      }

      if (var4 != null) {
         if (var3.y() < var4.y() - 1.2) {
            return false;
         }

         double var16 = Math.hypot(var3.x() - var4.x(), var3.z() - var4.z());
         if (Double.isFinite(var9) && var16 > var9) {
            return false;
         }

         boolean var13 = AiPaceScript.fill(var1, var3);
         double var14 = Double.isFinite(var9) ? var9 : 0.75;
         if (var4.onGround()
            && var4.vy() <= 0.1
            && !var13
            && (!var3.onGround() || !(var3.vy() <= 0.1) || !putAt_Z_TextPool_IoHelper(this.putAt_TextPool_IoHelper, var5, var4, var3.y()) || !(var16 <= var14))
            )
          {
            return false;
         }

         if (this.putAt_TextPool_IoHelper.putAt_Z() && var5 == var6 - 1 && (var16 > 0.35 || Math.abs(var3.y() - var4.y()) > 0.15)) {
            return false;
         }
      }

      return true;
   }

   private AiPacePlayback.Inner_TableCache putAt(
      AiPhysics.PhysicsWorld var1, AiPhysics.MotionState var2, double var3, AiPacePlayback.Inner_TaskState var5, boolean var6
   ) {
      return this.putAt_TableUtil_TableCac_TokenCore_TaskStat(var1, var2, var3, var5, var6, false, false);
   }

   private AiPacePlayback.Inner_TableCache putAt_TableUtil_TableCac_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var1, AiPhysics.MotionState var2, double var3, AiPacePlayback.Inner_TaskState var5, boolean var6, boolean var7, boolean var8
   ) {
      boolean var9 = var2.onGround() || AiPaceScript.fill(var1, var2);
      if (!var9 && var5.safeUntilTick() > this.store && var5.safeLanding() != null && var5.rebased() != null) {
         if (var6) {
            this.state_D = var3;
         }

         this.dataEx = var5.safeUntilTick();
         this.putAt_Str = var5.reason();
         this.prevEx_Z = var7;
         this.prev_Z = var8;
         this.putAt_Map.keySet().removeIf(var1x -> var1x >= this.store);

         for (int var10 = 0; var10 < var5.rebased().length && this.store + var10 < this.dataEx; var10++) {
            this.putAt_Map.put(this.store + var10, var5.rebased()[var10]);
         }

         return null;
      } else {
         this.fill_Str = var5.reason();
         return new AiPacePlayback.Inner_TableCache(AiPacePlayback.Inner_JsonCache.prevEx, null, this.fill_Str);
      }
   }

   public static AiPacePlayback.Inner_TaskState putAt_TableUtil_TaskStat_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPaceScript.Inner_IoHelper var2, int var3, double var4, int var6, Predicate<BlockPos> var7
   ) {
      return putAt(var0, var1, var2, var3, var4, var6, var7, null);
   }

   public static AiPacePlayback.Inner_TaskState putAt(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      AiPaceScript.Inner_IoHelper var2,
      int var3,
      double var4,
      int var6,
      Predicate<BlockPos> var7,
      Predicate<BlockPos> var8
   ) {
      return putAt(var0, var1, var2, var3, var4, var6, var7, var8, var2::putAt_TokenCore_TableCac_I, null, false, false, null, null);
   }

   private static AiPacePlayback.Inner_TaskState putAt(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      AiPaceScript.Inner_IoHelper var2,
      int var3,
      double var4,
      int var6,
      Predicate<BlockPos> var7,
      Predicate<BlockPos> var8,
      IntFunction<AiPhysics.MotionState> var9,
      IntToDoubleFunction var10,
      boolean var11,
      boolean var12,
      Map<Integer, AiPhysics.MotionState> var13,
      Map<Integer, AiPhysics.MoveInput> var14
   ) {
      return putAt(var0, var1, var2, var3, var4, var6, var7, var8, var9, var10, var11, var12, var13, var14, 1.0 / 0.0, -1);
   }

   private static AiPacePlayback.Inner_TaskState putAt(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      AiPaceScript.Inner_IoHelper var2,
      int var3,
      double var4,
      int var6,
      Predicate<BlockPos> var7,
      Predicate<BlockPos> var8,
      IntFunction<AiPhysics.MotionState> var9,
      IntToDoubleFunction var10,
      boolean var11,
      boolean var12,
      Map<Integer, AiPhysics.MotionState> var13,
      Map<Integer, AiPhysics.MoveInput> var14,
      double var15,
      int var17
   ) {
      int var18 = var2.flowTemp();
      double var19 = var0.movementSpeed();
      boolean var21 = var10 == null && Math.abs(var19 - var4) > 1.0E-4;
      if (var3 >= var18) {
         BlockPos[] var61 = var2.putAt_BlockPosArr();
         if (var17 >= 0 && var61 != null && var17 < var61.length && (var11 || var12 || var21) && var2.putAt_Z() && var18 > 0) {
            int var62 = putAt_I_BlockPosArr(var61, var17, var1);
            if (var62 < var61.length) {
               for (int var63 = Math.max(0, var18 - 2); var63 < var18; var63++) {
                  AiPhysics.MotionState var64 = var2.putAt_TokenCore_TableCac_I(var63);
                  if (var64 != null && AiPaceScript.putAt(var64, var61[var62])) {
                     return new AiPacePlayback.Inner_TaskState(
                        false,
                        var21,
                        var18,
                        new AiPhysics.MotionState[0],
                        null,
                        String.format(Locale.ROOT, "rebase missed checkpoint %s", var61[var62].toShortString())
                     );
                  }
               }
            }
         }

         return new AiPacePlayback.Inner_TaskState(true, var21, var18, new AiPhysics.MotionState[0], null, null);
      } else {
         int var22 = Math.min(var18, var3 + Math.max(1, var6));
         int var23 = Math.min(var18, var22 + 24);
         ArrayList var24 = new ArrayList(var22 - var3);
         AiPhysics.MotionState var25 = var1;
         int var26 = var3;
         AiPhysics.MotionState var27 = null;
         boolean var28 = false;
         AiPhysics.PhysicsWorld var29 = var0;
         double var30 = var19;
         BlockPos[] var32 = var2.putAt_BlockPosArr();
         boolean var33 = var17 >= 0 && var32 != null && var17 < var32.length && (var11 || var12 || var21);
         int var34 = var33 ? putAt_I_BlockPosArr(var32, var17, var1) : -1;
         int var35 = -1;
         if (var33 && var34 < var32.length) {
            for (int var36 = Math.max(0, var3 - 2); var36 < var3; var36++) {
               AiPhysics.MotionState var37 = var2.putAt_TokenCore_TableCac_I(var36);
               if (var37 != null && AiPaceScript.putAt(var37, var32[var34])) {
                  var35 = var36;
               }
            }
         }

         for (int var65 = var3;
            var65 < var23
               && (
                  var65 < var22
                     || (!var25.onGround() || !(var25.vy() <= 0.1) || !AiPaceScript.putAt_Z_TokenCore_TaskStat(var0, var25, 0.002)) && !AiPaceScript.fill(var0, var25)
               );
            var65++
         ) {
            AiPhysics.MoveInput var67 = var14 != null ? (AiPhysics.MoveInput)var14.get(var65) : null;
            AiPhysics.MoveInput var38 = var67 != null ? var67 : var2.fill_TokenCore_CacheHel(var65);
            boolean var39 = var38.sprint() && var38.forward() > 0.8 && !var38.sneak() && var25.pose() == 0 && !var25.sprintCut();
            AiPhysics.MoveInput var40 = new AiPhysics.MoveInput(
               var38.forward(), var38.strafe(), var38.jump(), var39, var38.sneak(), var38.yaw()
            );
            AiPhysics.PhysicsWorld var41 = var0;
            if (var10 != null) {
               double var42 = var10.applyAsDouble(var65);
               if (Math.abs(var42 - var30) > 1.0E-9) {
                  var29 = AiPaceScript.fill(var0, var42);
                  var30 = var42;
               }

               var41 = var29;
            }

            AiPhysics.MotionState var68 = AiPhysics.fill(var25, var40, var41);
            if (var7 != null || var8 != null) {
               BlockPos var43 = BlockPos.containing(var68.x(), var68.y() + 1.0E-4, var68.z());
               if (var7 != null && (!var7.test(var43) || !var7.test(var43.below()))) {
                  return new AiPacePlayback.Inner_TaskState(
                     false, var21, var26, ((java.util.Collection<AiPhysics.MotionState>)(java.util.Collection<?>)var24).toArray(AiPhysics.MotionState[]::new), var27, "unloaded chunk ahead at tick " + (var65 + 1)
                  );
               }

               if (var8 != null && (var68.onGround() || AiPaceScript.fill(var0, var68)) && !var8.test(var43)) {
                  return new AiPacePlayback.Inner_TaskState(
                     false, var21, var26, ((java.util.Collection<AiPhysics.MotionState>)(java.util.Collection<?>)var24).toArray(AiPhysics.MotionState[]::new), var27, "forbidden cell ahead at tick " + (var65 + 1)
                  );
               }
            }

            if (AiPhysics.putAt_Z(var0, var25, var68)) {
               return new AiPacePlayback.Inner_TaskState(
                  false, var21, var26, ((java.util.Collection<AiPhysics.MotionState>)(java.util.Collection<?>)var24).toArray(AiPhysics.MotionState[]::new), var27, "hazard appeared ahead at tick " + (var65 + 1)
               );
            }

            AiPhysics.MotionState var69 = var2.putAt_TokenCore_TableCac_I(var65);
            AiPhysics.MotionState var44 = var9 != null ? (AiPhysics.MotionState)var9.apply(var65) : var69;
            boolean var45 = var13 != null && var13.containsKey(var65);
            boolean var46 = var11 || var12 && !var45;
            AiPhysics.MotionState var47 = var46 && var69 != null ? var69 : var44;
            if (var47 != null) {
               double var48 = Math.sqrt(putAt_D_D(var68.x() - var47.x()) + putAt_D_D(var68.y() - var47.y()) + putAt_D_D(var68.z() - var47.z()));
               if (var48 > 0.05) {
                  var28 = true;
               }

               boolean var50 = AiPaceScript.fill(var0, var68);
               boolean var51 = var50 || var68.onGround() && var68.vy() <= 0.1;
               boolean var52 = var50 || var68.onGround() && var68.vy() <= 0.1 && AiPaceScript.putAt_Z_TokenCore_TaskStat(var0, var68, 0.002);
               double var53 = var69 != null ? Math.max(var47.y(), var69.y()) : var47.y();
               if (var46) {
                  if (var68.y() < var53 - 1.2) {
                     return new AiPacePlayback.Inner_TaskState(
                        false,
                        false,
                        var26,
                        ((java.util.Collection<AiPhysics.MotionState>)(java.util.Collection<?>)var24).toArray(AiPhysics.MotionState[]::new),
                        var27,
                        String.format(Locale.ROOT, "transition speed shift drops below route at tick %d", var65 + 1)
                     );
                  }

                  double var55 = Math.hypot(var68.x() - var47.x(), var68.z() - var47.z());
                  if (Double.isFinite(var15) && var55 > var15) {
                     return new AiPacePlayback.Inner_TaskState(
                        false,
                        false,
                        var26,
                        ((java.util.Collection<AiPhysics.MotionState>)(java.util.Collection<?>)var24).toArray(AiPhysics.MotionState[]::new),
                        var27,
                        String.format(Locale.ROOT, "drift exceeded rail margin at tick %d (error %.3f)", var65 + 1, var55)
                     );
                  }

                  double var57 = Double.isFinite(var15) ? var15 : 0.85;
                  if (var47.onGround() && var47.vy() <= 0.1) {
                     if (var51 && putAt_Z_TextPool_IoHelper(var2, var65, var47, var68.y()) && var55 <= var57) {
                        if (var52 && var27 == null) {
                           var26 = var65 + 1;
                           var27 = var68;
                        }
                     } else if (!var50) {
                        boolean var59 = var65 > 0
                           && var2.putAt_TokenCore_TableCac_I(var65 - 1) != null
                           && !var2.putAt_TokenCore_TableCac_I(var65 - 1).onGround();
                        boolean var60 = var59 && var68.y() >= var47.y() - 0.02 && var68.y() <= var47.y() + 0.35 && var55 <= var57 && var65 + 1 < var23;
                        if (!var60) {
                           return new AiPacePlayback.Inner_TaskState(
                              false,
                              false,
                              var26,
                              ((java.util.Collection<AiPhysics.MotionState>)(java.util.Collection<?>)var24).toArray(AiPhysics.MotionState[]::new),
                              var27,
                              String.format(Locale.ROOT, "transition speed shift invalidated landing at tick %d", var65 + 1)
                           );
                        }
                     }
                  } else if (var52 && Math.abs(var68.y() - var47.y()) <= 0.15 && var27 == null) {
                     var26 = var65 + 1;
                     var27 = var68;
                  }

                  if (var2.putAt_Z() && var65 == var18 - 1 && (var55 > 0.35 || Math.abs(var68.y() - var47.y()) > 0.15)) {
                     return new AiPacePlayback.Inner_TaskState(
                        false,
                        false,
                        var26,
                        ((java.util.Collection<AiPhysics.MotionState>)(java.util.Collection<?>)var24).toArray(AiPhysics.MotionState[]::new),
                        var27,
                        String.format(Locale.ROOT, "transition speed shift misses finish at tick %d", var65 + 1)
                     );
                  }
               } else if (!var21) {
                  if (var48 > 0.05 || var68.onGround() != var47.onGround() && var48 > 0.01 || var68.y() < var53 - 1.2) {
                     return new AiPacePlayback.Inner_TaskState(
                        false,
                        false,
                        var26,
                        ((java.util.Collection<AiPhysics.MotionState>)(java.util.Collection<?>)var24).toArray(AiPhysics.MotionState[]::new),
                        var27,
                        String.format(Locale.ROOT, "terrain changed ahead at tick %d (error %.3f)", var65 + 1, var48)
                     );
                  }

                  if (var52 && var27 == null) {
                     var26 = var65 + 1;
                     var27 = var68;
                  }
               } else {
                  if (var68.y() < var47.y() - 1.2) {
                     return new AiPacePlayback.Inner_TaskState(
                        false,
                        true,
                        var26,
                        ((java.util.Collection<AiPhysics.MotionState>)(java.util.Collection<?>)var24).toArray(AiPhysics.MotionState[]::new),
                        var27,
                        String.format(Locale.ROOT, "speed change (%.3f -> %.3f) drops below route at tick %d", var4, var19, var65 + 1)
                     );
                  }

                  if (var47.onGround() && var47.vy() <= 0.1) {
                     if (var51 && putAt_Z_TextPool_IoHelper(var2, var65, var47, var68.y()) && Math.hypot(var68.x() - var47.x(), var68.z() - var47.z()) <= 0.85) {
                        if (var52 && var27 == null) {
                           var26 = var65 + 1;
                           var27 = var68;
                        }
                     } else if (!var50) {
                        return new AiPacePlayback.Inner_TaskState(
                           false,
                           true,
                           var26,
                           ((java.util.Collection<AiPhysics.MotionState>)(java.util.Collection<?>)var24).toArray(AiPhysics.MotionState[]::new),
                           var27,
                           String.format(Locale.ROOT, "speed change (%.3f -> %.3f) invalidated landing at tick %d", var4, var19, var65 + 1)
                        );
                     }
                  } else if (var52 && Math.abs(var68.y() - var47.y()) <= 0.15 && var27 == null) {
                     var26 = var65 + 1;
                     var27 = var68;
                  }
               }
            }

            var24.add(var68);
            var25 = var68;
            if (var33 && var34 < var32.length) {
               int var70 = var34;
               var34 = putAt_I_BlockPosArr(var32, var34, var68);
               if (var34 != var70) {
                  var35 = -1;
               }

               if (var34 < var32.length && var69 != null && AiPaceScript.putAt(var69, var32[var34])) {
                  var35 = var65;
               }

               if (var34 < var32.length && var35 >= 0 && var65 >= var35 + 4) {
                  return new AiPacePlayback.Inner_TaskState(
                     false,
                     var21,
                     var26,
                     ((java.util.Collection<AiPhysics.MotionState>)(java.util.Collection<?>)var24).toArray(AiPhysics.MotionState[]::new),
                     var27,
                     String.format(Locale.ROOT, "rebase missed checkpoint %s at tick %d", var32[var34].toShortString(), var65 + 1)
                  );
               }
            }
         }

         int var66 = var3 + var24.size();
         if (var33 && var34 < var32.length && var35 >= 0 && var2.putAt_Z() && var66 >= var18) {
            return new AiPacePlayback.Inner_TaskState(
               false,
               var21,
               var26,
               ((java.util.Collection<AiPhysics.MotionState>)(java.util.Collection<?>)var24).toArray(AiPhysics.MotionState[]::new),
               var27,
               String.format(Locale.ROOT, "rebase missed checkpoint %s", var32[var34].toShortString())
            );
         } else if (!var21 || !var28 || var66 >= var18) {
            return new AiPacePlayback.Inner_TaskState(true, var21, var66, ((java.util.Collection<AiPhysics.MotionState>)(java.util.Collection<?>)var24).toArray(AiPhysics.MotionState[]::new), var27, null);
         } else {
            return var27 != null && var26 > var3
               ? new AiPacePlayback.Inner_TaskState(true, true, var26, ((java.util.Collection<AiPhysics.MotionState>)(java.util.Collection<?>)var24).toArray(AiPhysics.MotionState[]::new), var27, null)
               : new AiPacePlayback.Inner_TaskState(
                  false,
                  true,
                  var26,
                  ((java.util.Collection<AiPhysics.MotionState>)(java.util.Collection<?>)var24).toArray(AiPhysics.MotionState[]::new),
                  var27,
                  String.format(Locale.ROOT, "speed change (%.3f -> %.3f) drifted off script before safe landing", var4, var19)
               );
         }
      }
   }

   private float putAt_F_LocalPlayer(LocalPlayer var1) {
      if (var1.onGround() || var1.onClimbable() || Double.isNaN(this.pollAt_D) || var1.getY() < this.pollAt_D) {
         this.pollAt_D = var1.getY();
      }

      int var2 = this.putAt_TextPool_IoHelper.flowTemp();
      int var3 = Math.min(this.store + 10, var2 - 1);
      AiPhysics.MotionState var4 = this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(var3);
      float var5 = AiMotionSafety.putAt_F();
      if (var4 == null) {
         return Float.isNaN(var5) ? var1.getXRot() : Math.max(var1.getXRot(), var5);
      }

      AiPhysics.MotionState var6 = var4;
      if (!var4.onGround() && var4.vy() > -0.45) {
         for (int var7 = 1; var7 <= 12; var7++) {
            AiPhysics.MotionState var8 = var3 + var7 < var2 ? this.putAt_TextPool_IoHelper.putAt_TokenCore_TableCac_I(var3 + var7) : null;
            if (var8 != null && var8.onGround() && var8.vy() <= 0.1) {
               var6 = var8;
               break;
            }
         }
      }

      double var20 = var6.x() - var1.getX();
      double var9 = var6.z() - var1.getZ();
      double var11 = this.pollAt_D + var1.getEyeHeight();
      double var13 = var6.y() + 0.6 - var11;
      float var15 = var1.getXRot();
      double var16 = Math.sqrt(var20 * var20 + var9 * var9);
      if (var16 < 0.5 && Math.abs(var13) < 2.2) {
         return Float.isNaN(var5) ? var15 : Math.max(var15, var5);
      }

      float var18 = (float)Math.clamp(-Math.toDegrees(Math.atan2(var13, Math.max(var16, 1.5))), -40.0, 70.0);
      if (!Float.isNaN(var5)) {
         var18 = Math.max(var18, var5);
      }

      if (Math.abs(var18 - var15) < 1.5F) {
         return var15;
      }

      float var19 = var15 + Math.clamp((var18 - var15) * 0.22F, -4.0F, 4.0F);
      return Float.isNaN(var5) ? var19 : Math.max(var19, var5);
   }

   static void putAt_V_LocalPlayer(LocalPlayer var0, AiPhysics.MoveInput var1, float var2) {
      Minecraft var3 = Minecraft.getInstance();
      AiFallGuard.load();
      float var4 = var0.getYRot();
      float var5 = var0.getXRot();
      RotationMath.applyToPlayer(var0, new RotationMath.Rotation(var1.yaw(), var2), false);
      AiCameraTurn.putAt(var0, var4, var5);
      var3.options.keyUp.setDown(var1.forward() > 0.0);
      var3.options.keyDown.setDown(var1.forward() < 0.0);
      var3.options.keyLeft.setDown(var1.strafe() > 0.0);
      var3.options.keyRight.setDown(var1.strafe() < 0.0);
      var3.options.keyJump.setDown(var1.jump());
      var3.options.keyShift.setDown(var1.sneak());
      boolean var6 = var1.sprint() && var1.forward() > 0.0;
      AiMotionSafety.putAt_V_LocalPlayer_Z(var0, var6);
      var0.setSprinting(var6);
      if (!var3.options.toggleSprint().get()) {
         var3.options.keySprint.setDown(var6);
      }
   }

   public AiPhysics.MotionState putAt_TokenCore_TableCac() {
      for (int var1 = Math.max(0, this.store - 1); var1 < this.putAt_TextPool_IoHelper.flowTemp(); var1++) {
         AiPhysics.MotionState var2 = this.putAt_TokenCore_TableCac_I(var1);
         if (var2 != null && var2.onGround() && var2.vy() <= 0.1) {
            return var2;
         }
      }

      return null;
   }

   public static void putAt_V_LocalPlayer_TokenCore_TableCac(LocalPlayer var0, AiPhysics.MotionState var1) {
      Minecraft var2 = Minecraft.getInstance();
      BlockPos var3 = BlockPos.containing(var1.x(), var1.y() + 1.0E-4, var1.z());
      AiJumpSolver.ApproachOffset var4 = AiJumpSolver.putAt_IoUtil_CacheHelper_TokenCore_TaskStat(
         new AiLiveWorld(var2.level, var0), AiPhysics.MotionState.of(var0), var0.getYRot(), true, var3, var1.y()
      );
      putAt_V_LocalPlayer(
         var0, new AiPhysics.MoveInput(var4.forward(), var4.strafe(), false, var4.forward() > 0.0, false, var0.getYRot()), var0.getXRot()
      );
   }

   public static double putAt_D_LocalPlayer(LocalPlayer var0) {
      return AiLiveWorld.putAt_D_LivingEntity(var0);
   }

   public static void putAt_V_LocalPlayer(LocalPlayer var0) {
      fill_V_LocalPlayer(var0);
      if (var0 != null && var0.onGround()) {
         Minecraft.getInstance().options.keyShift.setDown(true);
      }
   }

   public static void fill_V_LocalPlayer(LocalPlayer var0) {
      AiMotionSafety.putAt_V_LocalPlayer_Z(null, true);
      Minecraft var1 = Minecraft.getInstance();
      var1.options.keyUp.setDown(false);
      var1.options.keyDown.setDown(false);
      var1.options.keyLeft.setDown(false);
      var1.options.keyRight.setDown(false);
      var1.options.keyJump.setDown(false);
      var1.options.keyShift.setDown(false);
      if (var0 != null) {
         var0.setSprinting(false);
      }

      if (!var1.options.toggleSprint().get()) {
         var1.options.keySprint.setDown(false);
      }
   }

   private static double putAt_D_D(double var0) {
      return var0 * var0;
   }

   private record Inner_CacheHelper(
      AiPhysics.MoveInput input,
      AiPhysics.MotionState afterTick,
      AiPhysics.MotionState[] tailRebased,
      AiPhysics.MoveInput[] futureOverrides,
      int tickDelta,
      boolean partial
   ) {
   }

   public enum Inner_JsonCache {
      putAt_TableUtil_JsonCach,
      fill,
      readEx,
      value,
      prevEx;
   }

   public record Inner_TableCache(AiPacePlayback.Inner_JsonCache status, AiPhysics.MoveInput applied, String reason) {
   }

   public record Inner_TaskState(
      boolean valid, boolean speedChanged, int safeUntilTick, AiPhysics.MotionState[] rebased, AiPhysics.MotionState safeLanding, String reason
   ) {
   }
}
