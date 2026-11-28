package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.api.ai.AiOperation;
import autismclient.util.AutismLiteVariant;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

final class AiParkourRoute implements AiOperation {
   private final JsonObject putAt_JsonObject;
   private final List<BlockPos> waypoints;
   private final autismclient.ai.engine.AiGoto.WaypointPlan putAt_HashEntry_TableCac;
   private final JsonArray putAt_JsonArray = new JsonArray();
   private AiOperation gotoOp;
   private int flowState;
   private long legStartNanos;
   private Vec3 legStartPos;
   private double firstPathReadyMs = -1.0;
   private int replans;
   private final boolean putAt_Z;
   private volatile autismclient.ai.engine.AiPaceScript.Inner_JsonCache putAt_TextPool_JsonCache;
   private volatile autismclient.ai.engine.AiPaceScript.Inner_TokenCodec putAt_TextPool_TokenCode;
   private volatile boolean fill_Z;
   private volatile boolean readEx_Z;
   private volatile boolean value_Z;
   private autismclient.ai.engine.AiParkourFallWarning putAt_BufferCore;
   private boolean prevEx_Z;
   private volatile float putAt_F = 0.0F / 0.0F;
   private boolean prev_Z;
   private boolean chunkAt_Z;
   private boolean state_Z;
   private boolean closeOf_Z;
   private int readEx_I;
   private int value_I;
   private int paceRestarts;
   private int prev_I;
   private int chunkAt_I;
   private int state_I;
   private int closeOf_I;
   private double fill_D = -1.0;
   private autismclient.ai.engine.AiPhysics.MotionState putAt_TokenCore_TableCac;
   private long startNanos;
   private double readEx_D;
   private double value_D;
   private double prevEx_D = 0.0 / 0.0;
   private boolean pollAt_Z;
   private boolean pollOf_Z;
   private boolean assumeNoFallDamage;
   private autismclient.ai.engine.AiLiveGrid putAt_PoolStore;
   private volatile AiParkourRoute.PlaybackState putAt_RenderCodec_CacheH;
   private int pollAt_I;
   private int pollOf_I;
   private int paceHandoffs;
   private int paceTicks;
   private double paceWorstError;
   private autismclient.ai.engine.AiPaceScript.Inner_CacheHelper putAt_TextPool_CacheHelp;
   private AiOperation fill_AiOperation;
   private long playbackStartedAt;
   private Vec3 playbackStartPos;
   private static final int stateOf = 2;
   private static final double chunkAt_D = 5.0;
   private static final float fill_F = 20.0F;

   AiParkourRoute(JsonObject var1) {
      if (AiJson.getBoolean(var1, "parkour", false)
         && !AiJson.getBoolean(var1, "yOnly", false)
         && var1.has("x")
         && var1.has("y")
         && var1.has("z")) {
         this.putAt_JsonObject = var1.deepCopy();
         ArrayList<BlockPos> var2 = new ArrayList<>();
         JsonElement var3 = this.putAt_JsonObject.remove("checkpoints");
         if (var3 != null) {
            for (JsonElement var5 : var3.getAsJsonArray()) {
               var2.add(putAt_BlockPos_JsonObject_2(var5.getAsJsonObject()));
            }
         }

         var2.add(putAt_BlockPos_JsonObject_2(var1));
         this.waypoints = List.copyOf(var2);
         this.putAt_HashEntry_TableCac = new autismclient.ai.engine.AiGoto.WaypointPlan(this.waypoints);
         this.putAt_Z = true;
         this.putAt_JsonObject.remove("parkourRecordPace");
      } else {
         throw new IllegalArgumentException("Checkpoints require parkour and a final x y z destination");
      }
   }

   static boolean putAt_Z_JsonObject(JsonObject var0) {
      return AiJson.getBoolean(var0, "parkour", false)
         && !AiJson.getBoolean(var0, "yOnly", false)
         && var0.has("x")
         && var0.has("y")
         && var0.has("z");
   }

   private static BlockPos putAt_BlockPos_JsonObject_2(JsonObject var0) {
      return new BlockPos(var0.get("x").getAsInt(), var0.get("y").getAsInt(), var0.get("z").getAsInt());
   }

   @Override
   public AiOperation.Result tick() {
      LocalPlayer var1 = Minecraft.getInstance().player;
      if (this.putAt_Z && !this.prev_Z) {
         AiOperation.Result var2 = this.putAt_AiOperation_Result_LocalPlayer(var1);
         if (var2 != null) {
            return var2;
         }
      }

      String var4;
      JsonObject var5;
      AiOperation.Result var10;
      do {
         BlockPos var9 = this.waypoints.get(this.flowState);
         if (this.gotoOp == null) {
            this.legStartPos = var1.position();
            this.legStartNanos = System.nanoTime();
            JsonObject var3 = this.putAt_JsonObject.deepCopy();
            var3.addProperty("x", var9.getX());
            var3.addProperty("y", var9.getY());
            var3.addProperty("z", var9.getZ());
            if (this.flowState < this.waypoints.size() - 1) {
               var3.addProperty("radius", 0.6);
               var3.addProperty("interactOnArrival", false);
            }

            this.gotoOp = autismclient.ai.engine.AiGoto.fromJsonWithPlan(var3, this.flowState, this.putAt_HashEntry_TableCac);
         }

         var10 = this.gotoOp.tick();
         var4 = "Checkpoint " + (this.flowState + 1) + "/" + this.waypoints.size();
         if (!var10.terminal()) {
            return new AiOperation.Result(AiOperation.Status.RUNNING, var4 + " · " + var10.message(), var10.dataJson(), var10.evidence());
         }

         var5 = AiJson.parseObject(var10.dataJson());
         double var6 = AiJson.getDouble(var5, "firstPathReadyMs", -1.0);
         if (this.flowState == 0) {
            this.firstPathReadyMs = var6;
         }

         this.replans = this.replans + AiJson.getInt(var5, "replans", 0);
         this.putAt_JsonArray
            .add(
               AiJson.objectOf(
                  "target",
                  List.of(var9.getX(), var9.getY(), var9.getZ()),
                  "startedAt",
                  putAt_List_Vec3(this.legStartPos),
                  "finishedAt",
                  putAt_List_Vec3(var1.position()),
                  "status",
                  var10.status().name(),
                  "wallMs",
                  (System.nanoTime() - this.legStartNanos) / 1000000L,
                  "firstPathReadyMs",
                  var6,
                  "searchMs",
                  AiJson.getDouble(var5, "checkpointSearchMs", -1.0),
                  "replans",
                  AiJson.getInt(var5, "replans", 0)
               )
            );
         this.gotoOp = null;
      } while (var10.status() == AiOperation.Status.SUCCEEDED && ++this.flowState < this.waypoints.size());

      this.putAt_HashEntry_TableCac.close();
      var5.add("checkpoints", this.putAt_JsonArray);
      var5.addProperty("firstPathReadyMs", this.firstPathReadyMs);
      var5.addProperty("replans", this.replans);
      String var8 = var10.status() == AiOperation.Status.SUCCEEDED
         ? "Reached all " + this.waypoints.size() + " checkpoints"
         : (var10.message().startsWith("No proved route to checkpoint") ? var10.message() : var4 + " · " + var10.message());
      return new AiOperation.Result(var10.status(), var8, var5.toString(), var10.evidence(), var10.failure());
   }

   private AiOperation.Result putAt_AiOperation_Result_LocalPlayer(LocalPlayer var1) {
      if (this.startNanos == 0L) {
         if (!var1.onGround() || var1.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5 && this.closeOf_I < 10) {
            this.closeOf_I++;
            autismclient.ai.engine.AiPacePlayback.fill_V_LocalPlayer(var1);
            return new AiOperation.Result(AiOperation.Status.RUNNING, "Settling before the record route", "{}", List.of());
         }

         this.startNanos = System.nanoTime();
         Minecraft var2 = Minecraft.getInstance();
         ItemStack[] var3 = new ItemStack[9];
         Arrays.fill(var3, ItemStack.EMPTY);
         this.putAt_PoolStore = new autismclient.ai.engine.AiLiveGrid(var2, var3, autismclient.ai.engine.AiChunkMemory.getInstance());
         autismclient.ai.engine.AiPhysics.MotionState var4 = autismclient.ai.engine.AiPhysics.MotionState.of(var1);
         float var5 = var1.getYRot();
         int var6 = AiJson.getInt(this.putAt_JsonObject, "parkourSpeed", -1);
         double var7 = autismclient.ai.engine.AiPacePlayback.putAt_D_LocalPlayer(var1);
         double var9 = var6 >= 0 ? 0.1 * (1.0 + 0.2 * var6) : var7;
         boolean var11 = !this.waypoints.isEmpty() && autismclient.ai.engine.AiPaceScript.putAt(var4, this.waypoints.get(0));
         this.pollOf_Z = var6 >= 0;
         this.pollAt_Z = var6 >= 0 && this.waypoints.size() >= 2 && !var11 && Math.abs(var7 - var9) > 1.0E-4;
         this.value_D = this.pollAt_Z ? var7 : var9;
         this.readEx_D = var9;
         this.prevEx_D = !this.pollAt_Z && this.pollOf_Z && Math.abs(var7 - var9) > 1.0E-4 ? var7 : 0.0 / 0.0;
         this.assumeNoFallDamage = AiJson.getBoolean(this.putAt_JsonObject, "assumeNoFallDamage", false);
         autismclient.ai.engine.AiLiveGrid var12 = this.putAt_PoolStore;
         double var13 = this.value_D;
         double var15 = this.readEx_D;
         boolean var17 = this.assumeNoFallDamage;
         Thread.ofVirtual()
            .name("autism-parkourwr-plan")
            .start(
               () -> {
                  try {
                     autismclient.ai.engine.AiPaceScript.Inner_TokenCodec var9x = autismclient.ai.engine.AiPaceScript.putAt_TextPool_TokenCode_FileStore_TextStor(
                        var12, var4, this.waypoints, var13, var15, var17, () -> this.readEx_Z
                     );
                     if (var9x == null && !var17 && !this.readEx_Z && !AutismLiteVariant.enabled()) {
                        autismclient.ai.engine.AiPaceScript.Inner_TokenCodec var17x = autismclient.ai.engine.AiPaceScript.putAt_TextPool_TokenCode_FileStore_TextStor(
                           var12, var4, this.waypoints, var13, var15, true, () -> this.readEx_Z
                        );
                        if (var17x != null) {
                           this.putAt_TextPool_TokenCode = var17x;
                           this.value_Z = true;
                        }
                     } else if (var9x != null) {
                        autismclient.ai.engine.AiPaceScript.Inner_JsonCache var10x = var9x.guide();
                        float var11x = autismclient.ai.engine.AiPaceScript.putAt_F_TokenCore_TableCac(var4, var10x, var5);
                        this.putAt_F = var11x;
                        this.putAt(autismclient.ai.engine.AiPaceScript.Inner_IoPool.of(var4, var11x), var10x);
                        this.putAt_TextPool_JsonCache = var10x;
                     }
                  } catch (Throwable var15x) {
                     autismclient.ai.engine.AiNavDebug.log("parkourwr planning failed: " + var15x);
                     AutismClientAddon.LOG.warn("[AI] parkourwr planning failed", var15x);
                  } finally {
                     this.fill_Z = true;
                  }
               }
            );
      }

      if (!this.fill_Z) {
         return this.fill(var1);
      }

      if (!this.value_Z) {
         autismclient.ai.engine.AiPaceScript.Inner_JsonCache var19 = this.putAt_TextPool_JsonCache;
         if (var19 == null) {
            return this.putAt(var1, "no guide");
         }

         autismclient.ai.engine.AiPhysics.MotionState var21 = autismclient.ai.engine.AiPhysics.MotionState.of(var1);

         while (this.pollAt_I < this.waypoints.size() && autismclient.ai.engine.AiPaceScript.putAt(var21, this.waypoints.get(this.pollAt_I))) {
            this.putAt_V_LocalPlayer(var1);
         }

         if (this.pollAt_I == this.waypoints.size()) {
            return this.readEx(var1);
         }

         if (this.fill_AiOperation != null) {
            AiOperation.Result var23 = this.fill_AiOperation.tick();
            String var28 = "Checkpoint " + (this.pollAt_I + 1) + "/" + this.waypoints.size();
            if (!var23.terminal()) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, var28 + " · " + var23.message(), var23.dataJson(), var23.evidence());
            }

            this.fill_AiOperation = null;
            if (var23.status() != AiOperation.Status.SUCCEEDED) {
               return this.putAt(var1, "ordinary leg " + var23.status() + ": " + var23.message());
            }

            if (this.putAt_TextPool_CacheHelp.checkpoint()
               && this.pollAt_I < this.waypoints.size()
               && this.waypoints.get(this.pollAt_I).equals(this.putAt_TextPool_CacheHelp.target())) {
               this.putAt_V_LocalPlayer(var1);
            }

            if (this.pollAt_I == this.waypoints.size()) {
               return this.readEx(var1);
            }

            this.chunkAt_Z = true;
         }

         if (this.chunkAt_Z) {
            if (!var1.onGround() && !var1.onClimbable() || var1.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5) {
               autismclient.ai.engine.AiPacePlayback.fill_V_LocalPlayer(var1);
               return new AiOperation.Result(
                  AiOperation.Status.RUNNING, "Checkpoint " + (this.pollAt_I + 1) + "/" + this.waypoints.size() + " · record route", "{}", List.of()
               );
            }

            this.chunkAt_Z = false;
            int var24 = Math.max(0, this.pollAt_I - this.state_I);
            this.putAt(
               autismclient.ai.engine.AiPaceScript.putAt_TextPool_IoPool_TokenCore_TableCac(var21, var1.getYRot(), var19, var24, this.putAt_TextPool_CacheHelp.point()),
               var19
            );
         }

         if (this.state_Z) {
            if (!var1.onGround() && !var1.onClimbable() && this.putAt_TokenCore_TableCac != null) {
               autismclient.ai.engine.AiPacePlayback.putAt_V_LocalPlayer_TokenCore_TableCac(var1, this.putAt_TokenCore_TableCac);
               return new AiOperation.Result(
                  AiOperation.Status.RUNNING, "Checkpoint " + (this.pollAt_I + 1) + "/" + this.waypoints.size() + " · record route", "{}", List.of()
               );
            }

            double var25 = var1.getDeltaMovement().horizontalDistanceSqr();
            if (!var1.onGround() && !var1.onClimbable() || var25 > 1.0E-5 && this.chunkAt_I < 12) {
               this.chunkAt_I++;
               if (var25 > 9.0E-4) {
                  autismclient.ai.engine.AiPacePlayback.putAt_V_LocalPlayer(var1);
               } else {
                  autismclient.ai.engine.AiPacePlayback.fill_V_LocalPlayer(var1);
               }

               return new AiOperation.Result(
                  AiOperation.Status.RUNNING, "Checkpoint " + (this.pollAt_I + 1) + "/" + this.waypoints.size() + " · record route", "{}", List.of()
               );
            }

            if (this.prev_I < 2) {
               autismclient.ai.engine.AiPacePlayback.fill_V_LocalPlayer(var1);
               this.prev_I++;
               return new AiOperation.Result(
                  AiOperation.Status.RUNNING, "Checkpoint " + (this.pollAt_I + 1) + "/" + this.waypoints.size() + " · record route", "{}", List.of()
               );
            }

            this.state_Z = false;
            this.chunkAt_I = 0;
            if (this.closeOf_Z) {
               this.closeOf_Z = false;
               this.fill_Z = false;
               this.putAt_TextPool_JsonCache = null;
               this.state_I = this.pollAt_I;
               this.putAt_TextPool_CacheHelp = null;
               var var34 = List.copyOf(this.waypoints.subList(this.pollAt_I, this.waypoints.size()));
               autismclient.ai.engine.AiLiveGrid var39 = this.putAt_PoolStore;
               boolean var40 = this.pollAt_Z
                  && this.pollAt_I == 0
                  && var34.size() >= 2
                  && !autismclient.ai.engine.AiPaceScript.putAt(var21, (BlockPos)var34.get(0))
                  && Math.abs(this.value_D - this.readEx_D) > 1.0E-4;
               if (!var40) {
                  this.pollAt_Z = false;
               }

               double var42 = var40 ? this.value_D : this.readEx_D;
               double var44 = this.readEx_D;
               boolean var46 = this.assumeNoFallDamage;
               float var47 = var1.getYRot();
               Thread.ofVirtual()
                  .name("autism-parkourwr-replan")
                  .start(
                     () -> {
                        try {
                           autismclient.ai.engine.AiPaceScript.Inner_JsonCache var10x = autismclient.ai.engine.AiPaceScript.putAt_TextPool_JsonCache_FileStore_TextStor(
                              var39, var21, var34, var42, var44, var46, () -> this.readEx_Z
                           );
                           if (var10x != null) {
                              float var11x = autismclient.ai.engine.AiPaceScript.putAt_F_TokenCore_TableCac(var21, var10x, var47);
                              this.putAt_F = var11x;
                              this.putAt(autismclient.ai.engine.AiPaceScript.Inner_IoPool.of(var21, var11x), var10x);
                           }

                           this.putAt_TextPool_JsonCache = var10x;
                        } catch (Throwable var15x) {
                           autismclient.ai.engine.AiNavDebug.log("parkourwr speed replan failed: " + var15x);
                        } finally {
                           this.fill_Z = true;
                        }
                     }
                  );
               return this.fill(var1);
            }

            int var31 = Math.max(0, this.pollAt_I - this.state_I);
            autismclient.ai.engine.AiPaceScript.Inner_IoPool var35 = autismclient.ai.engine.AiPaceScript.fill(var21, var1.getYRot(), var19, var31, this.value_I);
            if (var35 == null) {
               return this.putAt(var1, null, this.fill_D, "off the route after leaving the script");
            }

            this.putAt(var35, var19);
         }

         AiParkourRoute.PlaybackState var26 = this.putAt_RenderCodec_CacheH;
         if (var26 == null) {
            return this.fill(var1);
         }

         if (var26.putAt_TableUtil == null) {
            autismclient.ai.engine.AiPaceScript.Inner_IoHelper var29 = var26.putAt_TextPool_IoHelper;
            boolean var32 = this.pollOf_I >= 1 && (!this.pollAt_Z || this.pollAt_I > 0 || var29.flowState() >= 0);
            if (!var29.isFinished() && (!var32 || var29.size() < 30)) {
               return this.fill(var1);
            }

            if (var29.size() == 0) {
               autismclient.ai.engine.AiPaceScript.Inner_StreamState var38 = var29.putAt_TextPool_StreamSta();
               return this.putAt(var1, var26, var38 != null ? var38.reach() : var26.putAt_TextPool_IoPool.progress(), "no script");
            }

            double var36 = this.pollAt_Z && this.pollAt_I == 0 ? this.value_D : this.readEx_D;
            var26.putAt_TableUtil = new autismclient.ai.engine.AiPacePlayback(var29, var36, this.prevEx_D);
            this.prevEx_D = 0.0 / 0.0;
            this.pollOf_I++;
            if (this.pollOf_I == 1) {
               this.firstPathReadyMs = (System.nanoTime() - this.startNanos) / 1000000.0;
               this.playbackStartedAt = System.nanoTime();
               this.playbackStartPos = var1.position();
            }

            autismclient.ai.engine.AiNavDebug.log(
               String.format(
                  Locale.ROOT,
                  "parkourwr script %d: %d ticks ready, searched from %.1f blocks",
                  this.pollOf_I,
                  var29.size(),
                  var26.putAt_TextPool_IoPool.progress()
               )
            );
         }

         autismclient.ai.engine.AiPacePlayback var30 = var26.putAt_TableUtil;
         autismclient.ai.engine.AiPacePlayback.Inner_JsonCache var33 = var30.putAt_TableUtil_JsonCach_LocalPlayer(var1);
         if (var33 == autismclient.ai.engine.AiPacePlayback.Inner_JsonCache.putAt_TableUtil_JsonCach) {
            this.paceTicks++;
            if (!AutismLiteVariant.enabled()) {
               AiVisualization.showPaceScript(this, var26.putAt_TextPool_IoHelper, var30.stepIndex(), this.waypoints, this.pollAt_I);
            }

            return new AiOperation.Result(
               AiOperation.Status.RUNNING, "Checkpoint " + (this.pollAt_I + 1) + "/" + this.waypoints.size() + " · record route", "{}", List.of()
            );
         } else {
            this.paceWorstError = Math.max(this.paceWorstError, var30.worstError());
            var26.putAt_Z = true;
            if (var1.getDeltaMovement().horizontalDistanceSqr() > 9.0E-4) {
               autismclient.ai.engine.AiPacePlayback.putAt_V_LocalPlayer(var1);
            } else {
               autismclient.ai.engine.AiPacePlayback.fill_V_LocalPlayer(var1);
            }

            autismclient.ai.engine.AiPaceScript.Inner_StreamState var37 = var26.putAt_TextPool_IoHelper.putAt_TextPool_StreamSta();
            if (var33 == autismclient.ai.engine.AiPacePlayback.Inner_JsonCache.fill && var37 != null) {
               return this.putAt(var1, var26, var37.reach(), "the search found no way on");
            }

            int var8 = Math.max(0, var26.putAt_TextPool_IoHelper.putAt_I_I(var30.stepIndex() - 1) - 3);
            int var41 = Math.max(0, this.pollAt_I - this.state_I);
            autismclient.ai.engine.AiPaceScript.Inner_IoPool var10 = autismclient.ai.engine.AiPaceScript.fill(var21, var1.getYRot(), var19, var41, var8);
            double var43 = var10 != null ? var10.progress() : var26.putAt_TextPool_IoPool.progress();
            boolean var45 = var33 == autismclient.ai.engine.AiPacePlayback.Inner_JsonCache.prevEx && var30.putAt_Z();
            boolean var14 = (var33 == autismclient.ai.engine.AiPacePlayback.Inner_JsonCache.value || var33 == autismclient.ai.engine.AiPacePlayback.Inner_JsonCache.prevEx)
               && (var10 != null || var45);
            if (var14) {
               double var48 = autismclient.ai.engine.AiPacePlayback.putAt_D_LocalPlayer(var1);
               if (var45) {
                  if (this.pollAt_Z && this.pollAt_I == 0) {
                     this.value_D = var48;
                     boolean var49 = Math.abs(this.value_D - this.readEx_D) <= 1.0E-4;
                     if (var49) {
                        this.pollAt_Z = false;
                     }

                     boolean var18 = !this.waypoints.isEmpty() && var1.position().distanceToSqr(Vec3.atCenterOf(this.waypoints.get(0))) <= 25.0;
                     if (!var49 || !var18) {
                        this.closeOf_Z = true;
                     }
                  } else if (!this.pollOf_Z) {
                     this.readEx_D = var48;
                     this.value_D = var48;
                     this.pollAt_Z = false;
                     this.closeOf_Z = true;
                  }
               }

               if (this.pollOf_Z && this.pollAt_I > 0 && Math.abs(var48 - this.readEx_D) > 1.0E-4) {
                  this.prevEx_D = var48;
               }

               if (var43 > this.fill_D + 5.0) {
                  this.readEx_I = 0;
               }

               if (this.readEx_I < 2) {
                  this.readEx_I++;
                  this.paceRestarts++;
                  this.fill_D = Math.max(this.fill_D, var43);
                  this.value_I = var8;
                  this.putAt_TokenCore_TableCac = !var1.onGround() && !var1.onClimbable() ? var30.expectedState() : null;
                  this.state_Z = true;
                  this.prev_I = 0;
                  this.chunkAt_I = 0;
                  this.putAt_RenderCodec_CacheH = null;
                  String var50 = String.format(
                     Locale.ROOT,
                     "parkourwr restart %d at %.1f blocks (cp %d, speed %.3f->%.3f, %s): resuming",
                     this.readEx_I,
                     var43,
                     this.pollAt_I,
                     var48,
                     this.readEx_D,
                     var30.putAt_Str()
                  );
                  autismclient.ai.engine.AiNavDebug.log(var50);
                  AutismClientAddon.LOG.info("[AI] " + var50);
                  return new AiOperation.Result(
                     AiOperation.Status.RUNNING, "Checkpoint " + (this.pollAt_I + 1) + "/" + this.waypoints.size() + " · record route", "{}", List.of()
                  );
               }
            }

            return this.putAt(
               var1,
               var26,
               var43,
               var33 != autismclient.ai.engine.AiPacePlayback.Inner_JsonCache.value && var33 != autismclient.ai.engine.AiPacePlayback.Inner_JsonCache.prevEx
                  ? (var33 == autismclient.ai.engine.AiPacePlayback.Inner_JsonCache.readEx ? "the search fell behind" : "the script ended")
                  : var30.putAt_Str()
            );
         }
      } else {
         if (this.prevEx_Z) {
            this.prev_Z = true;
            this.putAt_HashEntry_TableCac.close();
            return new AiOperation.Result(AiOperation.Status.CANCELLED, "Parkour route cancelled at fall warning", "{}", List.of());
         }

         autismclient.ai.engine.AiPaceScript.Inner_TokenCodec var20 = this.putAt_TextPool_TokenCode;
         if (var20 == null) {
            return this.putAt(var1, "no guide");
         }

         Minecraft var22 = Minecraft.getInstance();
         if (this.putAt_BufferCore == null && !autismclient.util.OverlayScreenController.hasModalScreen()) {
            autismclient.ai.engine.AiParkourFallWarning.FallRisk var27 = autismclient.ai.engine.AiParkourFallWarning.putAt_BufferCore_CacheHe_Minecraft_List(
               var22, var20.movements()
            );
            if (var27.damage() == 0.0F) {
               this.putAt(var1, false);
               return this.fill(var1);
            }

            this.putAt_BufferCore = new autismclient.ai.engine.AiParkourFallWarning(
               var22, var27, var2x -> this.putAt(var1, var2x.booleanValue()), () -> this.prevEx_Z = true
            );
            if (!this.putAt_BufferCore.openWarning()) {
               this.putAt_BufferCore = null;
            }
         }

         autismclient.ai.engine.AiPacePlayback.fill_V_LocalPlayer(var1);
         return new AiOperation.Result(AiOperation.Status.RUNNING, "Waiting for fall damage approval", "{}", List.of());
      }
   }

   private void putAt(autismclient.ai.engine.AiPaceScript.Inner_IoPool var1, autismclient.ai.engine.AiPaceScript.Inner_JsonCache var2) {
      AiParkourRoute.PlaybackState var3 = new AiParkourRoute.PlaybackState(var1);
      this.putAt_RenderCodec_CacheH = var3;
      boolean var4 = this.pollAt_Z && this.state_I == 0 && var1.touched() == 0 && var2.checkpoints().length >= 2;
      double var5 = var4 ? this.value_D : this.readEx_D;
      autismclient.ai.engine.AiPaceScript.Inner_BitCache var7 = var4
         ? autismclient.ai.engine.AiPaceScript.Inner_BitCache.switchAtFirstCheckpoint(this.value_D, this.readEx_D)
         : autismclient.ai.engine.AiPaceScript.Inner_BitCache.uniform(this.readEx_D);
      var var8 = autismclient.ai.engine.AiPaceScript.putAt_Supplier_FileStore_TextStor_2(this.putAt_PoolStore, var5, this.assumeNoFallDamage);
      Thread.ofVirtual()
         .name("autism-parkourwr-search")
         .start(
            () -> {
               try {
                  autismclient.ai.engine.AiPaceScript.putAt_TextPool_PacketHel_Supplier(
                     var8,
                     var1,
                     var2,
                     autismclient.ai.engine.AiPaceScript.Inner_TextState.DEFAULT,
                     var7,
                     () -> this.readEx_Z || var3.putAt_Z,
                     var3.putAt_TextPool_IoHelper
                  );
               } catch (Throwable var7x) {
                  autismclient.ai.engine.AiNavDebug.log("parkourwr search failed: " + var7x);
                  var3.putAt_TextPool_IoHelper.finish(false, null);
               }
            }
         );
   }

   private AiOperation.Result fill(LocalPlayer var1) {
      autismclient.ai.engine.AiPacePlayback.fill_V_LocalPlayer(var1);
      float var2 = this.putAt_F;
      if (!Float.isNaN(var2) && this.pollOf_I == 0) {
         float var3 = var1.getYRot();
         float var4 = var1.getXRot();
         float var5 = Mth.wrapDegrees(var2 - var3);
         float var6 = 20.0F - var4;
         autismclient.util.RotationMath.applyToPlayer(
            var1,
            new autismclient.util.RotationMath.Rotation(var3 + Math.clamp(var5 * 0.3F, -12.0F, 12.0F), var4 + Math.clamp(var6 * 0.3F, -6.0F, 6.0F)),
            false
         );
         autismclient.ai.engine.AiCameraTurn.recordTurn(var1, var3, var4);
      }

      AiParkourRoute.PlaybackState var9 = this.putAt_RenderCodec_CacheH;
      autismclient.ai.engine.AiPaceScript.Inner_JsonCache var10 = this.putAt_TextPool_JsonCache;
      if (var10 != null && !AutismLiteVariant.enabled()) {
         AiVisualization.showPreview(this, var10, this.waypoints, this.pollAt_I);
      }

      if (var9 != null && var10 != null && !(var10.length() <= 0.0)) {
         double var11 = var9.putAt_TextPool_IoPool.progress();
         double var7 = (var9.putAt_TextPool_IoHelper.putAt_D() - var11) / Math.max(1.0E-6, var10.length() - var11);
         return new AiOperation.Result(
            AiOperation.Status.RUNNING,
            String.format(Locale.ROOT, "Planning record script · %d%%", (int)Math.round(100.0 * Math.clamp(var7, 0.0, 1.0))),
            "{}",
            List.of()
         );
      } else {
         return new AiOperation.Result(AiOperation.Status.RUNNING, "Planning route · record script", "{}", List.of());
      }
   }

   private AiOperation.Result putAt(LocalPlayer var1, AiParkourRoute.PlaybackState var2, double var3, String var5) {
      autismclient.ai.engine.AiPaceScript.Inner_JsonCache var6 = this.putAt_TextPool_JsonCache;
      if (this.putAt_TextPool_CacheHelp != null) {
         var3 = Math.max(var3, var6.cum()[this.putAt_TextPool_CacheHelp.point()]);
      }

      int var7 = Math.max(0, this.pollAt_I - this.state_I);
      this.putAt_TextPool_CacheHelp = autismclient.ai.engine.AiPaceScript.putAt_TextPool_CacheHelp_TextPool_JsonCache(var6, var3, var7);
      this.putAt_RenderCodec_CacheH = null;
      if (this.putAt_TextPool_CacheHelp == null) {
         return this.putAt(var1, "no handoff cell after " + var5);
      }

      this.paceHandoffs++;
      String var8 = String.format(
         Locale.ROOT,
         "parkourwr handoff %d at %.1f blocks (%s): ordinary leg to %s%s",
         this.paceHandoffs,
         var3,
         var5,
         this.putAt_TextPool_CacheHelp.target().toShortString(),
         this.putAt_TextPool_CacheHelp.checkpoint() ? " (checkpoint)" : ""
      );
      autismclient.ai.engine.AiNavDebug.log(var8);
      AutismClientAddon.LOG.info("[AI] " + var8);
      JsonObject var9 = this.putAt_JsonObject.deepCopy();
      if (this.pollAt_Z && this.pollAt_I == 0) {
         var9.remove("parkourSpeed");
      }

      BlockPos var10 = this.putAt_TextPool_CacheHelp.target();
      var9.addProperty("x", var10.getX());
      var9.addProperty("y", var10.getY());
      var9.addProperty("z", var10.getZ());
      if (!this.putAt_TextPool_CacheHelp.checkpoint() || this.pollAt_I != this.waypoints.size() - 1) {
         var9.addProperty("radius", 0.6);
         var9.addProperty("interactOnArrival", false);
      }

      this.fill_AiOperation = autismclient.ai.engine.AiGoto.fromJson(var9);
      return new AiOperation.Result(
         AiOperation.Status.RUNNING, "Checkpoint " + (this.pollAt_I + 1) + "/" + this.waypoints.size() + " · ordinary leg", "{}", List.of()
      );
   }

   private void putAt_V_LocalPlayer(LocalPlayer var1) {
      BlockPos var2 = this.waypoints.get(this.pollAt_I);
      this.putAt_JsonArray
         .add(
            AiJson.objectOf(
               "target",
               List.of(var2.getX(), var2.getY(), var2.getZ()),
               "startedAt",
               putAt_List_Vec3(this.playbackStartPos != null ? this.playbackStartPos : var1.position()),
               "finishedAt",
               putAt_List_Vec3(var1.position()),
               "status",
               AiOperation.Status.SUCCEEDED.name(),
               "wallMs",
               (System.nanoTime() - this.playbackStartedAt) / 1000000L,
               "firstPathReadyMs",
               this.pollAt_I == 0 ? this.firstPathReadyMs : 0.0,
               "searchMs",
               -1,
               "replans",
               0,
               "paceTick",
               this.paceTicks
            )
         );
      this.pollAt_I++;
      this.playbackStartedAt = System.nanoTime();
      this.playbackStartPos = var1.position();
   }

   private AiOperation.Result readEx(LocalPlayer var1) {
      if (!AutismLiteVariant.enabled()) {
         AiVisualization.clearOwner(this);
      }

      autismclient.ai.engine.AiPacePlayback.fill_V_LocalPlayer(var1);
      AiParkourRoute.PlaybackState var2 = this.putAt_RenderCodec_CacheH;
      if (var2 != null) {
         var2.putAt_Z = true;
      }

      this.prev_Z = true;
      this.flowState = this.waypoints.size();
      JsonObject var3 = new JsonObject();
      var3.add("checkpoints", this.putAt_JsonArray);
      var3.addProperty("firstPathReadyMs", this.firstPathReadyMs);
      var3.addProperty("replans", 0);
      var3.addProperty("paceTicks", this.paceTicks);
      var3.addProperty("paceScripts", this.pollOf_I);
      var3.addProperty("paceHandoffs", this.paceHandoffs);
      var3.addProperty("paceRestarts", this.paceRestarts);
      var3.addProperty("paceWorstError", this.paceWorstError);
      var3.addProperty("parkourRecordPace", true);
      var3.addProperty("parkourSpeed", AiJson.getInt(this.putAt_JsonObject, "parkourSpeed", -1));
      var3.addProperty("planningMovementSpeed", this.readEx_D);
      this.putAt_HashEntry_TableCac.close();
      return new AiOperation.Result(AiOperation.Status.SUCCEEDED, "Reached all " + this.waypoints.size() + " checkpoints", var3.toString(), List.of());
   }

   private AiOperation.Result putAt(LocalPlayer var1, String var2) {
      if (!AutismLiteVariant.enabled()) {
         AiVisualization.clearOwner(this);
      }

      String var3 = "parkourwr hands the course back at checkpoint " + this.pollAt_I + ": " + var2;
      autismclient.ai.engine.AiNavDebug.log(var3);
      AutismClientAddon.LOG.info("[AI] " + var3);
      autismclient.ai.engine.AiPacePlayback.fill_V_LocalPlayer(var1);
      AiParkourRoute.PlaybackState var4 = this.putAt_RenderCodec_CacheH;
      if (var4 != null) {
         var4.putAt_Z = true;
      }

      this.prev_Z = true;
      this.flowState = this.pollAt_I;
      return null;
   }

   private void putAt(LocalPlayer var1, boolean var2) {
      if (!this.readEx_Z && var1 != null && var1.isAlive()) {
         autismclient.ai.engine.AiPaceScript.Inner_TokenCodec var3 = this.putAt_TextPool_TokenCode;
         this.putAt_TextPool_TokenCode = null;
         this.putAt_BufferCore = null;
         this.value_Z = false;
         if (var3 != null) {
            this.putAt_HashEntry_TableCac.putAt(var2);
            this.putAt_JsonObject.addProperty("assumeNoFallDamage", true);
            this.assumeNoFallDamage = true;
            autismclient.ai.engine.AiPaceScript.Inner_JsonCache var4 = var3.guide();
            autismclient.ai.engine.AiPhysics.MotionState var5 = autismclient.ai.engine.AiPhysics.MotionState.of(var1);
            float var6 = autismclient.ai.engine.AiPaceScript.putAt_F_TokenCore_TableCac(var5, var4, var1.getYRot());
            this.putAt_F = var6;
            this.putAt_TextPool_JsonCache = var4;
            if ((var1.onGround() || var1.onClimbable()) && var1.getDeltaMovement().horizontalDistanceSqr() == 0.0) {
               autismclient.ai.engine.AiPaceScript.Inner_IoPool var7 = autismclient.ai.engine.AiPaceScript.fill(var5, var6, var4, 0, 0);
               this.putAt(var7 != null ? var7 : autismclient.ai.engine.AiPaceScript.Inner_IoPool.of(var5, var6), var4);
            } else {
               this.state_Z = true;
               this.value_I = 0;
               this.putAt_TokenCore_TableCac = null;
               this.prev_I = 0;
            }
         }
      } else {
         this.prevEx_Z = true;
      }
   }

   private static List<Double> putAt_List_Vec3(Vec3 var0) {
      return List.of(var0.x, var0.y, var0.z);
   }

   @Override
   public boolean canInterruptNow() {
      return (this.gotoOp == null || this.gotoOp.canInterruptNow())
         && (this.fill_AiOperation == null || this.fill_AiOperation.canInterruptNow());
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.gotoOp != null && this.gotoOp.requiresReconciliationAfterEmergency();
   }

   @Override
   public void cancel() {
      if (!AutismLiteVariant.enabled()) {
         AiVisualization.clearOwner(this);
      }

      this.readEx_Z = true;
      if (this.putAt_BufferCore != null) {
         this.putAt_BufferCore.closeWarning();
         this.putAt_BufferCore = null;
      }

      AiParkourRoute.PlaybackState var1 = this.putAt_RenderCodec_CacheH;
      if (var1 != null) {
         var1.putAt_Z = true;
      }

      if (this.fill_AiOperation != null) {
         this.fill_AiOperation.cancel();
      }

      if (this.putAt_Z && !this.prev_Z) {
         autismclient.ai.engine.AiPacePlayback.fill_V_LocalPlayer(Minecraft.getInstance().player);
      }

      if (this.gotoOp != null) {
         this.gotoOp.cancel();
      }

      this.putAt_HashEntry_TableCac.close();
   }

   private static final class PlaybackState {
      final autismclient.ai.engine.AiPaceScript.Inner_IoHelper putAt_TextPool_IoHelper = new autismclient.ai.engine.AiPaceScript.Inner_IoHelper();
      final autismclient.ai.engine.AiPaceScript.Inner_IoPool putAt_TextPool_IoPool;
      volatile boolean putAt_Z;
      autismclient.ai.engine.AiPacePlayback putAt_TableUtil;

      PlaybackState(autismclient.ai.engine.AiPaceScript.Inner_IoPool var1) {
         this.putAt_TextPool_IoPool = var1;
      }
   }
}
