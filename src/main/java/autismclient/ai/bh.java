package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.concurrent.Future;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.level.GameType;

final class bh implements AiOperation {
   private static final int push = 32;
   final Minecraft init_Minecraft = Minecraft.getInstance();
   final TableIndex.Inner_DataUtil init_TableIndex_DataUti;
   final TableIndex.Inner_ConfigStore init_TableIndex_ConfigS;
   final boolean init_Z;
   final long init_J;
   int init_I;
   int load_I;
   int read_I;
   int write;
   final JsonArray init_JsonArray = new JsonArray();
   AiOperation init_AiOperation;
   boolean load_Z;
   final bb init_bb = new bb();
   AiOperation.Result init_AiOperation_Result;
   boolean read_Z;
   final Object init_Obj;
   final Object load_Obj;
   List<autismclient.ai.b.ListPool> init_List;
   bV.Inner_StreamUtil init_bV_StreamUtil;
   autismclient.ai.b.UiHelper.ao init_UiHelper_ao;
   final Provider init_HolderLookup_Provi;
   BlockPos init_BlockPos;
   bg.Inner_SyncManager init_bg_SyncManager;
   Future<bh.Inner_DataUtil> init_Future;
   bh.Inner_DataUtil init_bh_DataUtil;
   bg.Inner_CachePool init_bg_CachePool;
   bg.Inner_NetHelper init_bg_NetHelper;
   int update;
   int sync;
   int flush;
   int close;
   int open;
   int poll;
   final JsonArray load_JsonArray = new JsonArray();

   bh(
      TableIndex.Inner_DataUtil base,
      TableIndex.Inner_ConfigStore power,
      boolean verify,
      int timeoutSeconds,
      List<autismclient.ai.b.ListPool> firstTargets,
      BlockPos knownStock,
      bV.Inner_StreamUtil firstActions,
      autismclient.ai.b.UiHelper.ao firstGeometry
   ) {
      this.init_TableIndex_DataUti = base;
      this.init_TableIndex_ConfigS = power;
      this.init_Z = verify;
      this.init_J = timeoutSeconds > 0 ? System.nanoTime() + timeoutSeconds * 1000000000L : 9223372036854775807L;
      this.init_List = firstTargets;
      this.init_bV_StreamUtil = firstActions;
      this.init_UiHelper_ao = firstGeometry;
      this.init_HolderLookup_Provi = this.init_Minecraft.level.registryAccess();
      this.init_BlockPos = knownStock;
      this.init_Obj = this.init_Minecraft.level;
      this.load_Obj = this.init_Minecraft.player;
   }

   @Override
   public AiOperation.Result tick() {
      if (this.init_AiOperation_Result != null) {
         return this.init_AiOperation_Result;
      }

      if (!this.read_Z
         && this.init_Minecraft.player != null
         && this.init_Minecraft.level != null
         && this.init_Minecraft.level == this.init_Obj
         && this.init_Minecraft.player == this.load_Obj) {
         if (this.init_Minecraft.player.getHealth() <= 4.0F && !X.close()) {
            return this.init_AiOperation_Result_AiOperation_Status_Str_9(
               AiOperation.Status.FAILED, X.init_Str_Str("building", this.init_Minecraft.player.getHealth(), this.init_Minecraft.player.getMaxHealth())
            );
         }

         if (System.nanoTime() > this.init_J) {
            return this.init_AiOperation_Result_AiOperation_Status_Str_9(AiOperation.Status.TIMED_OUT, "Build deadline reached");
         }

         if (this.init_AiOperation != null) {
            AiOperation.Result r = this.init_bb.init_AiOperation_Result(this.init_AiOperation);
            this.load_Z = true;
            if (!r.terminal()) {
               return this.init("Building copy " + (this.init_I + 1) + " of " + this.init_Str_16());
            }

            this.init_V_24();
            if (r.status() != AiOperation.Status.SUCCEEDED) {
               return this.init_AiOperation_Result_AiOperation_Status_Str_9(r.status(), r.message());
            }

            this.init_I++;
         }

         TableIndex.Inner_NetHelper repeat = this.init_TableIndex_ConfigS.repeat();
         return repeat.count() >= 0 && this.init_I > repeat.count()
            ? this.init_AiOperation_Result_AiOperation_Status_Str_9(AiOperation.Status.SUCCEEDED, "Built " + this.init_I + " copies")
            : this.init_AiOperation_Result_27();
      } else {
         return this.init_AiOperation_Result_AiOperation_Status_Str_9(AiOperation.Status.CANCELLED, "Build cancelled or world changed");
      }
   }

   private AiOperation.Result init_AiOperation_Result_27() {
      TableIndex.Inner_DataUtil shifted = new TableIndex.Inner_DataUtil(
         this.init_TableIndex_DataUti
            .origin()
            .offset(
               this.init_TableIndex_ConfigS.repeat().dx() * this.init_I,
               this.init_TableIndex_ConfigS.repeat().dy() * this.init_I,
               this.init_TableIndex_ConfigS.repeat().dz() * this.init_I
            ),
         this.init_TableIndex_DataUti.cells()
      );
      int remaining = this.init_J == 9223372036854775807L ? 0 : (int)Math.max(1L, Math.min(2147483647L, (this.init_J - System.nanoTime()) / 1000000000L));
      if (this.init_I == 0 && this.init_List != null) {
         bf operation = new bf(shifted, remaining, this.init_TableIndex_ConfigS, this.init_List, this.init_bV_StreamUtil, this.init_UiHelper_ao);
         if (this.init_BlockPos != null) {
            operation.init_V_BlockPos_2(this.init_BlockPos);
         }

         this.init_List = null;
         this.init_bV_StreamUtil = null;
         this.init_UiHelper_ao = null;
         this.init_AiOperation = operation;
         return this.init("Building copy 1 of " + this.init_Str_16());
      } else {
         if (this.init_bh_DataUtil == null) {
            if (this.init_Future == null) {
               this.init_bg_SyncManager = new bg.Inner_SyncManager();
               this.init_Future = this.init_bg_SyncManager
                  .init(
                     () -> {
                        List<autismclient.ai.b.ListPool> targets = List.copyOf(bi.init(shifted));
                        return new bh.Inner_DataUtil(
                           shifted,
                           TableIndex.load_TableIndex_DataUti(shifted),
                           targets,
                           bg.Inner_JsonUtil.of(shifted),
                           this.init_TableIndex_ConfigS
                              .machine()
                              .init_bV_StreamUtil_BlockPos_HolderLookup_Provi(shifted.origin(), this.init_HolderLookup_Provi),
                           autismclient.ai.b.UiHelper.init_UiHelper_ao_List_Runnable(targets, TableIndex::init)
                        );
                     }
                  );
               return this.init("Preparing copy " + (this.init_I + 1));
            }

            if (!this.init_Future.isDone()) {
               return this.init("Preparing copy " + (this.init_I + 1));
            }

            try {
               this.init_bh_DataUtil = this.init_Future.get();
            } catch (Exception failure) {
               return this.init_AiOperation_Result_AiOperation_Status_Str_9(
                  AiOperation.Status.FAILED, "Could not prepare schematic copy: " + failure.getMessage()
               );
            } finally {
               if (this.init_bg_SyncManager != null) {
                  this.init_bg_SyncManager.close();
               }

               this.init_bg_SyncManager = null;
               this.init_Future = null;
            }

            this.init_bg_CachePool = new bg.Inner_CachePool(this.init_bh_DataUtil.stockPlan(), this.init_bh_DataUtil.source().keys());
         }

         boolean creative = this.init_Minecraft.gameMode != null
            && this.init_Minecraft.gameMode.getPlayerMode() == GameType.CREATIVE
            && this.init_Minecraft.player.getAbilities().instabuild;
         if (!creative) {
            if (!this.init_bg_CachePool.init_Z()) {
               this.init_bg_CachePool.init(4096, System.nanoTime() + 2000000L, this.init_Minecraft.level);
               return this.init("Checking materials for copy " + (this.init_I + 1));
            }

            JsonObject missing = TableIndex.init_JsonObject_Iterable_2(
               this.init_bg_CachePool.init_List(), this.init_Minecraft.player.getInventory(), this.init_TableIndex_ConfigS.shulkerStock()
            );
            if (!missing.isEmpty()) {
               if (this.init_TableIndex_ConfigS.shulkerStock()) {
                  if (this.init_bg_NetHelper == null) {
                     this.init_bg_NetHelper = new bg.Inner_NetHelper(
                        this.init_Minecraft.player.blockPosition(), shifted.origin(), this.init_bh_DataUtil.source()
                     );
                  }

                  this.init_bg_NetHelper
                     .init(4096, System.nanoTime() + 2000000L, pos -> cu.init(this.init_Minecraft.level, pos), this.init_Minecraft.level::getBlockState);
                  if (!this.init_bg_NetHelper.init_Z && !this.init_bg_NetHelper.init_Z()) {
                     return this.init("Checking stock for copy " + (this.init_I + 1));
                  }

                  if (this.init_bg_NetHelper.init_Z) {
                     this.init_BlockPos = this.init_bg_NetHelper.init_BlockPos;
                  }
               }

               if (this.init_bg_NetHelper == null || !this.init_bg_NetHelper.init_Z) {
                  JsonObject data = this.init_JsonObject_44();
                  data.add("missing", missing);
                  return this.init_I > 0 && this.init_TableIndex_ConfigS.repeat().count() < 0
                     ? this.init(AiOperation.Status.SUCCEEDED, "Materials exhausted after " + this.init_I + " copies", data)
                     : this.init(AiOperation.Status.FAILED, "Missing placement materials for copy " + (this.init_I + 1), data);
               }
            }
         }

         bf operation = new bf(
            this.init_bh_DataUtil.blueprint(),
            remaining,
            this.init_TableIndex_ConfigS,
            this.init_bh_DataUtil.targets(),
            this.init_bh_DataUtil.actions(),
            this.init_bh_DataUtil.geometry()
         );
         if (this.init_BlockPos != null) {
            operation.init_V_BlockPos_2(this.init_BlockPos);
         }

         this.init_AiOperation = operation;
         this.init_bh_DataUtil = null;
         this.init_bg_CachePool = null;
         this.init_bg_NetHelper = null;
         return this.init("Building copy " + (this.init_I + 1) + " of " + this.init_Str_16());
      }
   }

   String init_Str_16() {
      return this.init_TableIndex_ConfigS.repeat().count() < 0 ? "many" : String.valueOf(this.init_TableIndex_ConfigS.repeat().count() + 1);
   }

   JsonObject init_JsonObject_44() {
      TableIndex.Inner_NetHelper repeat = this.init_TableIndex_ConfigS.repeat();
      return ag.init_JsonObject_ObjArr(
         "copies",
         this.init_I,
         "placed",
         this.load_I,
         "skipped",
         this.read_I,
         "failed",
         this.init_JsonArray,
         "failedTotal",
         this.write,
         "cells",
         this.init_TableIndex_DataUti.cells().size(),
         "containersFilled",
         this.update,
         "containersSkipped",
         this.sync,
         "savedSlotsVerified",
         this.flush,
         "savedSlotsSkipped",
         this.close,
         "machineComplete",
         false,
         "skippedContents",
         this.load_JsonArray,
         "repeat",
         ag.init_JsonObject_ObjArr("dx", repeat.dx(), "dy", repeat.dy(), "dz", repeat.dz(), "count", repeat.count())
      );
   }

   AiOperation.Result init(String message) {
      return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_44());
   }

   AiOperation.Result init_AiOperation_Result_AiOperation_Status_Str_9(AiOperation.Status status, String message) {
      if (this.init_AiOperation_Result != null) {
         return this.init_AiOperation_Result;
      }

      this.cancel();
      return this.init(status, message, this.init_JsonObject_44());
   }

   AiOperation.Result init(AiOperation.Status status, String message, JsonObject data) {
      if (this.init_AiOperation_Result != null) {
         return this.init_AiOperation_Result;
      }

      this.cancel();
      boolean pending = this.init_bb.init_Z(null);
      data.addProperty("clocksVerified", this.open);
      data.addProperty("clockItemsVerified", this.poll);
      data.addProperty("machineComplete", status == AiOperation.Status.SUCCEEDED && this.sync == 0 && !pending);
      this.init_AiOperation_Result = init_AiOperation_Result_AiOperation_Result_bb(R.init_AiOperation_Result(status, message, data), this.init_bb);
      return this.init_AiOperation_Result;
   }

   static AiOperation.Result init_AiOperation_Result_AiOperation_Result_bb(AiOperation.Result result, bb receipt) {
      boolean pending = receipt.init_Z(null);
      JsonObject data = bK.init_JsonObject(result.dataJson());
      if (receipt.init() != null) {
         JsonObject childData = bK.init_JsonObject(receipt.init().dataJson());
         if (childData.has("machineRecovery")) {
            data.add("machineRecovery", childData.get("machineRecovery").deepCopy());
         }

         if (childData.has("externalWork")) {
            JsonObject external = childData.getAsJsonObject("externalWork").deepCopy();
            if (pending) {
               external.addProperty("recoveryRequired", true);
            }

            data.add("externalWork", external);
         }
      }

      return receipt.init(new AiOperation.Result(result.status(), result.message(), data.toString(), result.evidence(), result.failure()), pending);
   }

   static void init_V_bb_AiOperation(bb receipt, AiOperation child) {
      receipt.init_V(child);
      if (child != null) {
         receipt.init(child.requiresReconciliationAfterEmergency());
      }
   }

   private void init_V_24() {
      if (this.init_AiOperation != null) {
         if (this.load_Z && this.init_bb.init() != null) {
            JsonObject legData = bK.init_JsonObject(this.init_bb.init().dataJson());
            this.load_I = this.load_I + ag.init_I_JsonObject(legData, "placed", 0);
            this.read_I = this.read_I + ag.init_I_JsonObject(legData, "skipped", 0);
            this.write = this.write + ag.init_I_JsonObject(legData, "failedTotal", 0);
            this.update = this.update + ag.init_I_JsonObject(legData, "containersFilled", 0);
            this.sync = this.sync + ag.init_I_JsonObject(legData, "containersSkipped", 0);
            this.flush = this.flush + ag.init_I_JsonObject(legData, "savedSlotsVerified", 0);
            this.close = this.close + ag.init_I_JsonObject(legData, "savedSlotsSkipped", 0);
            this.open = this.open + ag.init_I_JsonObject(legData, "clocksVerified", 0);
            this.poll = this.poll + ag.init_I_JsonObject(legData, "clockItemsVerified", 0);
            if (legData.has("skippedContents")) {
               for (JsonElement omission : legData.getAsJsonArray("skippedContents")) {
                  if (this.load_JsonArray.size() >= 32) {
                     break;
                  }

                  this.load_JsonArray.add(omission);
               }
            }

            if (legData.has("failed")) {
               for (JsonElement failure : legData.getAsJsonArray("failed")) {
                  if (this.init_JsonArray.size() >= 32) {
                     break;
                  }

                  this.init_JsonArray.add(failure);
               }
            }
         }

         this.load_Z = false;
         init_V_bb_AiOperation(this.init_bb, this.init_AiOperation);
         this.init_AiOperation = null;
      }
   }

   @Override
   public boolean canInterruptNow() {
      return !this.init_bb.init_Z(this.init_AiOperation) && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow());
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.init_bb.init_Z(this.init_AiOperation);
   }

   @Override
   public void cancel() {
      if (!this.read_Z) {
         this.read_Z = true;
         if (this.init_bg_SyncManager != null) {
            this.init_bg_SyncManager.close();
            this.init_bg_SyncManager = null;
         }

         this.init_Future = null;
         this.init_bh_DataUtil = null;
         this.init_bg_CachePool = null;
         this.init_bg_NetHelper = null;
         this.init_List = null;
         this.init_bV_StreamUtil = null;
         this.init_UiHelper_ao = null;
         this.init_V_24();
      }
   }

   private record Inner_DataUtil(
      TableIndex.Inner_DataUtil blueprint,
      TableIndex.Inner_DataUtil stockPlan,
      List<autismclient.ai.b.ListPool> targets,
      bg.Inner_JsonUtil source,
      bV.Inner_StreamUtil actions,
      autismclient.ai.b.UiHelper.ao geometry
   ) {
   }
}
