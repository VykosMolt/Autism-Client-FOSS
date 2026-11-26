package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;

final class AiPrintPreparation implements AiOperation {
   static final int init_I = 4096;
   static final int load_I = 4096;
   static final long init_J = 2000000L;
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final AiPrintPreparation.Inner_TaskQueue init_bg_TaskQueue = new AiPrintPreparation.Inner_TaskQueue(this.init_Minecraft.level, this.init_Minecraft.player);
   private final Provider init_HolderLookup_Provi = this.init_Minecraft.level.registryAccess();
   private final BlockPos init_BlockPos = this.init_Minecraft.player.blockPosition().immutable();
   private final Path init_Path;
   private final BlockPos load_BlockPos;
   private final AiBuilder.Inner_ConfigStore init_TableIndex_ConfigS;
   private final boolean init_Z;
   private final boolean load_Z;
   private final long load_J;
   private final long read_J = System.nanoTime();
   private final AiPrintPreparation.Inner_SyncManager init_bg_SyncManager = new AiPrintPreparation.Inner_SyncManager();
   private Future<?> init_Future;
   private AiPrintPreparation.Inner_BufferHelper init_bg_BufferHelper = AiPrintPreparation.Inner_BufferHelper.init;
   private AiPrintPreparation.Inner_DataUtil init_bg_DataUtil;
   private AiPrintPreparation.Inner_ConfigStore init_bg_ConfigStore;
   private AiPrintPreparation.Inner_StreamUtil init_bg_StreamUtil;
   private AiPrintPreparation.Inner_CachePool init_bg_CachePool;
   private AiPrintPreparation.Inner_NetHelper init_bg_NetHelper;
   private JsonObject init_JsonObject;
   private boolean read_Z;
   private boolean write_Z;
   private AiOperation init_AiOperation;
   private AiOperation.Result init_AiOperation_Result;
   private long write_J;
   private long update = 1L;
   private long sync;
   private long flush;
   private int read_I;
   private int write_I;
   private JsonArray init_JsonArray = new JsonArray();

   AiPrintPreparation(JsonObject args, AiBuilder.Inner_ConfigStore power) {
      if (!args.has("blocks") && !args.has("blueprint")) {
         AiPrintSupply.init_V_TableIndex_ConfigS(power);
         this.init_TableIndex_ConfigS = power;
         this.load_BlockPos = AiBuilder.auditOrigin(args, this.init_BlockPos);
         this.init_Path = AiSchematic.init_Path_Path_Str(AiSchematic.init(), AiJson.init_Str_JsonObject(args, "schematic", ""));
         this.init_Z = AiJson.init_Z_JsonObject(args, "skipUnknown", false);
         this.load_Z = AiJson.init_Z_JsonObject(args, "verify", true);
         int seconds = args.has("timeoutSeconds") ? AiJson.init_I_JsonObject(args, "timeoutSeconds", 0) : 0;
         this.load_J = seconds > 0 ? System.nanoTime() + seconds * 1000000000L : 0L;
         this.init_Future = this.init_bg_SyncManager.init(this::init_bg_DataUtil);
      } else {
         throw new IllegalArgumentException("schematic is mutually exclusive with blocks/blueprint; pass one source");
      }
   }

   private AiPrintPreparation.Inner_DataUtil init_bg_DataUtil() throws IOException {
      AiPrintPreparation.Inner_StateCache stamp = AiPrintPreparation.Inner_StateCache.read(this.init_Path);
      AiSchematic.Inner_BufferHelper loaded = AiSchematic.init_bP_BufferHelper(
         this.init_Path, this.load_BlockPos, this.init_TableIndex_ConfigS.rotation(), this.init_TableIndex_ConfigS.mirror(), this.init_Z, 2097152
      );
      AiBuilder.init();
      AiBuilder.Inner_DataUtil blueprint = this.init_TableIndex_ConfigS.layers()
         ? AiBuilder.init_TableIndex_DataUti_TableIndex_DataUti(
            loaded.blueprint(), this.init_TableIndex_ConfigS.layerStart(), this.init_TableIndex_ConfigS.layerHeight()
         )
         : loaded.blueprint();
      init_V_TableIndex_DataUti(blueprint);
      LinkedHashSet<BlockPos> neighbors = new LinkedHashSet<>(AiBuilder.read(blueprint));
      neighbors.addAll(AiSchematicReadiness.read(blueprint));
      neighbors.addAll(AiSchematicReadiness.load(blueprint));
      if (!stamp.equals(AiPrintPreparation.Inner_StateCache.read(this.init_Path))) {
         throw new IOException("Schematic changed while it was being read; start the build again");
      } else {
         return new AiPrintPreparation.Inner_DataUtil(
            blueprint,
            this.init_TableIndex_ConfigS.withSignText(loaded.signText()).withMachine(AiSchematicMachinePlan.init_bV_bP_BufferHelper_TableIndex_DataUti(loaded, blueprint)),
            List.copyOf(neighbors),
            stamp
         );
      }
   }

   private AiPrintPreparation.Inner_ConfigStore init(AiPrintPreparation.Inner_DataUtil input, Map<Long, BlockState> snapshot) throws IOException {
      AiBuilder.Inner_DataUtil blueprint = AiBuilder.init(input.blueprint(), snapshot);
      init_V_TableIndex_DataUti(blueprint);
      AiBuilder.init();
      AiSchematicReadiness.Inner_DataUtil buildable = AiSchematicReadiness.init_bX_DataUtil(blueprint, 16, snapshot);
      blueprint = buildable.blueprint();
      AiBuilder.init();
      List<autismclient.ai.print.PrintTarget> targets = List.copyOf(AiPrintRules.init(blueprint));
      autismclient.ai.print.PrintCore.ao geometry = autismclient.ai.print.PrintCore.init_UiHelper_ao_List_Runnable(targets, AiBuilder::init);
      AiBuilder.init();
      AiBuilder.Inner_DataUtil construction = AiBuilder.load_TableIndex_DataUti(blueprint);
      AiPrintPreparation.Inner_JsonUtil source = AiPrintPreparation.Inner_JsonUtil.of(blueprint);
      if (!input.stamp().equals(AiPrintPreparation.Inner_StateCache.read(this.init_Path))) {
         throw new IOException("Schematic changed during preparation; start the build again");
      }

      AiBuilder.Inner_ConfigStore power = input.power().withMachine(input.power().machine().init(blueprint));
      return new AiPrintPreparation.Inner_ConfigStore(
         blueprint,
         construction,
         power,
         buildable.report(),
         buildable.skipped(),
         targets,
         source,
         power.machine().init_bV_StreamUtil_BlockPos_HolderLookup_Provi(blueprint.origin(), this.init_HolderLookup_Provi),
         geometry
      );
   }

   static void init_V_TableIndex_DataUti(AiBuilder.Inner_DataUtil blueprint) {
      if (blueprint.cells().isEmpty()) {
         throw new IllegalArgumentException("The selected schematic layers contain no buildable cells");
      }
   }

   @Override
   public AiOperation.Result tick() {
      boolean preparing = this.init_AiOperation == null;
      long started = System.nanoTime();
      AiOperation.Result result = this.init_AiOperation_Result_26();
      if (preparing) {
         this.sync = Math.max(this.sync, System.nanoTime() - started);
      }

      if (!preparing && !result.terminal()) {
         return result;
      }

      JsonObject data = JsonParser.parseString(result.dataJson()).getAsJsonObject();
      data.addProperty("preparationDone", this.write_J);
      data.addProperty("preparationTotal", this.update);
      data.addProperty("filePreparationDone", this.write_J);
      data.addProperty("maxPreparationTickMs", this.sync / 1000000.0);
      data.addProperty("preparationWallMs", ((this.flush == 0L ? System.nanoTime() : this.flush) - this.read_J) / 1000000L);
      String message = result.message();
      if (this.write_I > 0) {
         data.addProperty("preflightSkippedCells", this.write_I);
         data.add("preflightIssues", this.init_JsonArray.deepCopy());
         data.addProperty("sourceComplete", false);
         data.addProperty("machineComplete", false);
         if (result.status() == AiOperation.Status.SUCCEEDED) {
            message = message + ". Skipped " + this.write_I + (this.write_I == 1 ? " cell" : " cells") + " that could not be placed as saved.";
         }
      }

      return new AiOperation.Result(result.status(), message, data.toString(), result.evidence(), result.failure());
   }

   private AiOperation.Result init_AiOperation_Result_26() {
      if (this.init_AiOperation_Result != null) {
         return this.init_AiOperation_Result;
      }

      if (this.write_Z) {
         return this.init(AiOperation.Status.CANCELLED, "Build cancelled", AiFailure.size);
      }

      if (!this.init_bg_TaskQueue.matches(this.init_Minecraft.level, this.init_Minecraft.player)) {
         return this.init(AiOperation.Status.CANCELLED, "World or player changed", AiFailure.size);
      }

      if (!this.init_Minecraft.player.isAlive()) {
         return this.init(AiOperation.Status.FAILED, "The player died", AiFailure.flush);
      }

      if (this.load_J != 0L && System.nanoTime() >= this.load_J) {
         return this.init(AiOperation.Status.TIMED_OUT, "Requested build deadline reached", AiFailure.prev);
      }

      if (this.init_AiOperation != null) {
         AiOperation.Result result = this.init_AiOperation.tick();
         if (result.terminal()) {
            this.init_AiOperation_Result = result;
            this.init_bg_SyncManager.close();
         }

         return result;
      } else {
         try {
            label216: {
               switch (this.init_bg_BufferHelper) {
                  case init:
                     if (!this.init_Future.isDone()) {
                        return this.init_AiOperation_Result_Str_10("Reading schematic");
                     }

                     this.init_bg_DataUtil = (AiPrintPreparation.Inner_DataUtil)this.init_Future.get();
                     this.init_Future = null;
                     this.write_J++;
                     this.read_I = this.init_bg_DataUtil.blueprint().cells().size();
                     this.update = this.write_J + this.init_bg_DataUtil.leafNeighbors().size() + this.read_I + 2L;
                     this.init_bg_StreamUtil = new AiPrintPreparation.Inner_StreamUtil(this.init_bg_DataUtil.leafNeighbors());
                     this.init_bg_BufferHelper = AiPrintPreparation.Inner_BufferHelper.load;
                     break;
                  case load:
                     int before = this.init_bg_StreamUtil.init_I;
                     this.init_bg_StreamUtil
                        .init(
                           4096,
                           System.nanoTime() + 2000000L,
                           pos -> AiTerrain.init(this.init_Minecraft.level, pos) ? this.init_Minecraft.level.getBlockState(pos) : Blocks.AIR.defaultBlockState()
                        );
                     this.write_J = this.write_J + (this.init_bg_StreamUtil.init_I - before);
                     if (this.init_bg_StreamUtil.init_Z()) {
                        Map<Long, BlockState> snapshot = this.init_bg_StreamUtil.init_Map();
                        AiPrintPreparation.Inner_DataUtil input = this.init_bg_DataUtil;
                        this.init_Future = this.init_bg_SyncManager.init(() -> this.init(input, snapshot));
                        this.init_bg_BufferHelper = AiPrintPreparation.Inner_BufferHelper.read;
                     }
                     break;
                  case read:
                     if (!this.init_Future.isDone()) {
                        return this.init_AiOperation_Result_Str_10("Preparing schematic rules and targets");
                     }

                     this.init_bg_ConfigStore = (AiPrintPreparation.Inner_ConfigStore)this.init_Future.get();
                     this.init_Future = null;
                     this.write_J++;
                     this.read_I = this.init_bg_ConfigStore.blueprint().cells().size();
                     this.init_bg_SyncManager.close();
                     this.write_I = this.init_bg_ConfigStore.skipped();

                     for (AiSchematicReadiness.Inner_StreamUtil issue : this.init_bg_ConfigStore.audit().issues()) {
                        AiBuilder.Inner_StreamUtil cell = issue.cell();
                        this.init_JsonArray
                           .add(
                              AiJson.init_JsonObject_ObjArr(
                                 "kind",
                                 issue.kind().name(),
                                 "block",
                                 cell.block(),
                                 "dx",
                                 cell.dx(),
                                 "dy",
                                 cell.dy(),
                                 "dz",
                                 cell.dz(),
                                 "detail",
                                 issue.detail()
                              )
                           );
                     }

                     if (this.read_I == 0) {
                        this.flush = System.nanoTime();
                        return this.init(AiOperation.Status.FAILED, "No cells in the selected schematic can be placed at this site", AiFailure.write);
                     }

                     this.init_bg_CachePool = new AiPrintPreparation.Inner_CachePool(this.init_bg_ConfigStore.stockPlan(), this.init_bg_ConfigStore.source().keys());
                     this.init_bg_BufferHelper = this.init_Z_22() ? AiPrintPreparation.Inner_BufferHelper.sync : AiPrintPreparation.Inner_BufferHelper.write;
                     break;
                  case write:
                     if (this.init_Z_22()) {
                        this.init_bg_BufferHelper = AiPrintPreparation.Inner_BufferHelper.sync;
                     } else {
                        int beforeWrite = this.init_bg_CachePool.init_I;
                        this.init_bg_CachePool.init(4096, System.nanoTime() + 2000000L, this.init_Minecraft.level);
                        this.write_J = this.write_J + (this.init_bg_CachePool.init_I - beforeWrite);
                        if (this.init_bg_CachePool.init_Z()) {
                           this.init_JsonObject = this.init_JsonObject_43();
                           this.read_Z = true;
                           if (this.init_JsonObject.isEmpty()) {
                              this.init_bg_BufferHelper = AiPrintPreparation.Inner_BufferHelper.sync;
                           } else {
                              if (!this.init_bg_ConfigStore.power().shulkerStock()) {
                                 return this.load();
                              }

                              this.init_bg_NetHelper = new AiPrintPreparation.Inner_NetHelper(
                                 this.init_BlockPos, this.init_bg_ConfigStore.blueprint().origin(), this.init_bg_ConfigStore.source()
                              );
                              this.update = Math.max(this.update, this.write_J + this.init_bg_NetHelper.init_J() + 1L);
                              this.init_bg_BufferHelper = AiPrintPreparation.Inner_BufferHelper.update;
                           }
                        }
                     }
                     break;
                  case update:
                     if (this.init_Z_22()) {
                        this.init_bg_BufferHelper = AiPrintPreparation.Inner_BufferHelper.sync;
                     } else {
                        long beforeUpdate = this.init_bg_NetHelper.init_J;
                        this.init_bg_NetHelper
                           .init(4096, System.nanoTime() + 2000000L, pos -> AiTerrain.init(this.init_Minecraft.level, pos), this.init_Minecraft.level::getBlockState);
                        this.write_J = this.write_J + (this.init_bg_NetHelper.init_J - beforeUpdate);
                        if (this.init_bg_NetHelper.init_Z) {
                           this.init_bg_BufferHelper = AiPrintPreparation.Inner_BufferHelper.sync;
                        } else if (this.init_bg_NetHelper.init_Z()) {
                           this.init_JsonObject = this.init_JsonObject_43();
                           if (!this.init_JsonObject.isEmpty()) {
                              return this.load();
                           }

                           this.init_bg_BufferHelper = AiPrintPreparation.Inner_BufferHelper.sync;
                        }
                     }
                     break;
                  case sync:
                     if (!this.init_Z_22() && !this.read_Z) {
                        this.init_bg_BufferHelper = AiPrintPreparation.Inner_BufferHelper.write;
                     } else {
                        if (this.init_Z_22() || this.init_bg_NetHelper != null && this.init_bg_NetHelper.init_Z) {
                           break label216;
                        }

                        this.init_JsonObject = this.init_JsonObject_43();
                        if (this.init_JsonObject.isEmpty()) {
                           break label216;
                        }

                        if (!this.init_bg_ConfigStore.power().shulkerStock()) {
                           return this.load();
                        }

                        this.init_bg_NetHelper = new AiPrintPreparation.Inner_NetHelper(
                           this.init_BlockPos, this.init_bg_ConfigStore.blueprint().origin(), this.init_bg_ConfigStore.source()
                        );
                        this.update = Math.max(this.update, this.write_J + this.init_bg_NetHelper.init_J() + 1L);
                        this.init_bg_BufferHelper = AiPrintPreparation.Inner_BufferHelper.update;
                     }
               }

               String var10001;
               switch (this.init_bg_BufferHelper) {
                  case init:
                     var10001 = "Reading schematic";
                     break;
                  case load:
                     var10001 = "Checking outside schematic context";
                     break;
                  case read:
                     var10001 = "Preparing schematic rules and targets";
                     break;
                  case write:
                     var10001 = "Checking schematic materials";
                     break;
                  case update:
                     var10001 = "Looking for external shulker stock";
                     break;
                  case sync:
                     var10001 = "Schematic prepared";
                     break;
                  default:
                     throw new MatchException(null, null);
               }

               return this.init_AiOperation_Result_Str_10(var10001);
            }

            int seconds = this.load_J == 0L ? 0 : (int)Math.max(1L, (this.load_J - System.nanoTime() + 999999999L) / 1000000000L);
            if (this.init_bg_ConfigStore.power().repeat() != null) {
               this.init_AiOperation = new AiPrintRepeat(
                  this.init_bg_ConfigStore.blueprint(),
                  this.init_bg_ConfigStore.power(),
                  this.load_Z,
                  seconds,
                  this.init_bg_ConfigStore.targets(),
                  this.init_bg_NetHelper == null ? null : this.init_bg_NetHelper.init_BlockPos,
                  this.init_bg_ConfigStore.nativeActions(),
                  this.init_bg_ConfigStore.geometry()
               );
            } else {
               AiPrintOp operation = new AiPrintOp(
                  this.init_bg_ConfigStore.blueprint(),
                  seconds,
                  this.init_bg_ConfigStore.power(),
                  this.init_bg_ConfigStore.targets(),
                  this.init_bg_ConfigStore.nativeActions(),
                  this.init_bg_ConfigStore.geometry()
               );
               if (this.init_bg_NetHelper != null && this.init_bg_NetHelper.init_BlockPos != null) {
                  operation.init_V_BlockPos_2(this.init_bg_NetHelper.init_BlockPos);
               }

               this.init_AiOperation = operation;
            }

            this.write_J = this.update = Math.max(this.write_J, this.update);
            this.flush = System.nanoTime();
            this.init_bg_DataUtil = null;
            this.init_bg_ConfigStore = null;
            this.init_bg_StreamUtil = null;
            this.init_bg_CachePool = null;
            this.init_bg_NetHelper = null;
            this.init_JsonObject = null;
            return this.init_AiOperation_Result_Str_10(
               this.write_I == 0
                  ? "Schematic prepared; starting construction"
                  : "Starting construction. Skipping " + this.write_I + (this.write_I == 1 ? " cell" : " cells") + " that cannot be placed as saved."
            );
         } catch (InterruptedException failure) {
            return this.init(AiOperation.Status.CANCELLED, "Schematic preparation interrupted", AiFailure.size);
         } catch (CancellationException failure) {
            return this.init(AiOperation.Status.CANCELLED, "Schematic preparation cancelled", AiFailure.size);
         } catch (ExecutionException failure) {
            Throwable cause = failure.getCause();
            return this.init(AiOperation.Status.FAILED, "Could not prepare schematic: " + cause.getMessage(), AiFailure.flush);
         } catch (RuntimeException failure) {
            return this.init(AiOperation.Status.FAILED, "Could not prepare schematic: " + failure.getMessage(), AiFailure.flush);
         }
      }
   }

   private boolean init_Z_22() {
      return this.init_Minecraft.gameMode != null
         && this.init_Minecraft.gameMode.getPlayerMode() == GameType.CREATIVE
         && this.init_Minecraft.player.getAbilities().instabuild;
   }

   private JsonObject init_JsonObject_43() {
      return AiBuilder.init_JsonObject_Iterable_2(
         this.init_bg_CachePool.init_List(), this.init_Minecraft.player.getInventory(), this.init_bg_ConfigStore.power().shulkerStock()
      );
   }

   private AiOperation.Result load() {
      this.init_bg_SyncManager.close();
      this.flush = System.nanoTime();
      this.init_AiOperation_Result = AiOperation.Result.failed(
         AiFailure.pop,
         "Missing materials for the schematic; "
            + (
               this.init_bg_ConfigStore.power().shulkerStock()
                  ? "carried stock is insufficient and no nearby external shulker was found"
                  : "shulker stock is disabled"
            )
            + ": "
            + this.init_JsonObject,
         AiJson.init_JsonObject_ObjArr(
               "missing",
               this.init_JsonObject,
               "placed",
               0,
               "skipped",
               0,
               "failed",
               new JsonArray(),
               "shulkerStock",
               false,
               "cells",
               this.init_bg_ConfigStore.blueprint().cells().size()
            )
            .toString()
      );
      return this.init_AiOperation_Result;
   }

   private AiOperation.Result init_AiOperation_Result_Str_10(String message) {
      return new AiOperation.Result(
         AiOperation.Status.RUNNING,
         message,
         AiJson.init_JsonObject_ObjArr(
               "preparing", this.init_AiOperation == null, "stage", "prepare", "phase", this.init_bg_BufferHelper.name(), "cells", this.read_I
            )
            .toString(),
         List.of()
      );
   }

   private AiOperation.Result init(AiOperation.Status status, String message, AiFailure failure) {
      this.init_bg_SyncManager.close();
      if (this.flush == 0L) {
         this.flush = System.nanoTime();
      }

      if (this.init_AiOperation != null) {
         this.init_AiOperation.cancel();
      }

      this.init_AiOperation_Result = new AiOperation.Result(status, message, "{}", List.of(), failure);
      return this.init_AiOperation_Result;
   }

   @Override
   public void cancel() {
      this.write_Z = true;
      this.init_bg_SyncManager.close();
      if (this.init_AiOperation != null) {
         this.init_AiOperation.cancel();
      }
   }

   @Override
   public boolean canInterruptNow() {
      return this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
   }

   private enum Inner_BufferHelper {
      init,
      load,
      read,
      write,
      update,
      sync;
   }

   static final class Inner_CachePool {
      final AiBuilder.Inner_DataUtil init_TableIndex_DataUti;
      final Set<Long> init_Set;
      final Set<BlockPos> load = new HashSet<>();
      private final Map<AiPrintPreparation.Inner_CachePool.Inner_DataUtil, AiCellMaterials.Inner_CachePool> init_Map = new LinkedHashMap<>();
      int init_I;

      Inner_CachePool(AiBuilder.Inner_DataUtil blueprint, Set<Long> source) {
         this.init_TableIndex_DataUti = blueprint;
         this.init_Set = source;
      }

      void init(int limit, LevelReader world) {
         this.init(limit, 9223372036854775807L, world);
      }

      void init(int limit, long deadline, LevelReader world) {
         int first = this.init_I;
         int end = Math.min(this.init_TableIndex_DataUti.cells().size(), this.init_I + Math.max(1, limit));

         while (this.init_I < end && (this.init_I == first || System.nanoTime() < deadline)) {
            AiBuilder.Inner_StreamUtil cell = this.init_TableIndex_DataUti.cells().get(this.init_I++);
            if (!AiBuilder.read_Z(cell)) {
               BlockPos at = this.init_TableIndex_DataUti.origin().offset(cell.dx(), cell.dy(), cell.dz());
               BlockState live = world.getBlockState(at);
               if (!AiBuilder.update(cell, live)) {
                  for (AiCellMaterials.Inner_CachePool requirement : AiCellMaterials.init(cell, live, world.getFluidState(at.above()).is(FluidTags.WATER))) {
                     this.init(requirement);
                  }

                  BlockPos foundation = AiSchematicActions.init_BlockPos_TableIndex_StreamU_2(cell, at, world, this.init_Set);
                  if (foundation != null && this.load.add(foundation)) {
                     this.init(
                        new AiCellMaterials.Inner_CachePool(AiCellMaterials.Inner_DataUtil.init, "minecraft:dirt", Items.DIRT, 1, AiCellMaterials.Inner_BufferHelper.init)
                     );
                  }
               }
            }
         }
      }

      private void init(AiCellMaterials.Inner_CachePool requirement) {
         AiPrintPreparation.Inner_CachePool.Inner_DataUtil key = new AiPrintPreparation.Inner_CachePool.Inner_DataUtil(requirement.itemSpec(), requirement.use());
         AiCellMaterials.Inner_CachePool previous = this.init_Map.get(key);
         if (previous == null) {
            this.init_Map.put(key, requirement);
         } else if (requirement.use() == AiCellMaterials.Inner_BufferHelper.init) {
            this.init_Map
               .put(
                  key,
                  new AiCellMaterials.Inner_CachePool(
                     previous.action(), previous.itemSpec(), previous.item(), Math.addExact(previous.count(), requirement.count()), previous.use()
                  )
               );
         }
      }

      boolean init_Z() {
         return this.init_I == this.init_TableIndex_DataUti.cells().size();
      }

      List<AiCellMaterials.Inner_CachePool> init_List() {
         return List.copyOf(this.init_Map.values());
      }

      private record Inner_DataUtil(String spec, AiCellMaterials.Inner_BufferHelper use) {
      }
   }

   record Inner_ConfigStore(
      AiBuilder.Inner_DataUtil blueprint,
      AiBuilder.Inner_DataUtil stockPlan,
      AiBuilder.Inner_ConfigStore power,
      AiSchematicReadiness.Inner_TaskQueue audit,
      int skipped,
      List<autismclient.ai.print.PrintTarget> targets,
      AiPrintPreparation.Inner_JsonUtil source,
      AiSchematicMachinePlan.Inner_StreamUtil nativeActions,
      autismclient.ai.print.PrintCore.ao geometry
   ) {
   }

   record Inner_DataUtil(AiBuilder.Inner_DataUtil blueprint, AiBuilder.Inner_ConfigStore power, List<BlockPos> leafNeighbors, AiPrintPreparation.Inner_StateCache stamp) {
   }

   record Inner_JsonUtil(Set<Long> keys, int minX, int maxX, int minZ, int maxZ) {
      static AiPrintPreparation.Inner_JsonUtil of(AiBuilder.Inner_DataUtil blueprint) {
         HashSet<Long> keys = new HashSet<>();
         int minX = 2147483647;
         int maxX = -2147483648;
         int minZ = minX;
         int maxZ = maxX;

         for (AiBuilder.Inner_StreamUtil cell : blueprint.cells()) {
            AiBuilder.init();
            BlockPos at = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
            keys.add(at.asLong());
            minX = Math.min(minX, at.getX());
            maxX = Math.max(maxX, at.getX());
            minZ = Math.min(minZ, at.getZ());
            maxZ = Math.max(maxZ, at.getZ());
         }

         return new AiPrintPreparation.Inner_JsonUtil(Collections.unmodifiableSet(keys), minX, maxX, minZ, maxZ);
      }
   }

   static final class Inner_NetHelper {
      private final List<AiPrintPreparation.Inner_NetHelper.Inner_DataUtil> init_List = new ArrayList<>(2);
      private final Set<Long> init_Set;
      int init_I;
      int load;
      int read;
      int write;
      long init_J;
      boolean init_Z;
      BlockPos init_BlockPos;

      Inner_NetHelper(BlockPos player, BlockPos origin, AiPrintPreparation.Inner_JsonUtil index) {
         this.init_Set = index.keys();
         this.init_List.add(new AiPrintPreparation.Inner_NetHelper.Inner_DataUtil(player.offset(-24, -8, -24), player.offset(24, 8, 24)));
         if (index.minX() <= index.maxX()) {
            this.init_List
               .add(
                  new AiPrintPreparation.Inner_NetHelper.Inner_DataUtil(
                     new BlockPos(index.minX() - 24, origin.getY() - 8, index.minZ() - 24),
                     new BlockPos(index.maxX() + 24, origin.getY() + 8, index.maxZ() + 24)
                  )
               );
         }

         this.init_V();
         BlockPos anyHeightStock = AuditBuilderShulkerStock.findExternalStock(
            player, index.minX(), index.maxX(), index.minZ(), index.maxZ(), this.init_Set
         );
         if (anyHeightStock != null) {
            this.init_Z = true;
            this.init_BlockPos = anyHeightStock;
         }
      }

      private void init_V() {
         if (this.init_I < this.init_List.size()) {
            BlockPos min = this.init_List.get(this.init_I).min();
            this.load = min.getX();
            this.read = min.getY();
            this.write = min.getZ();
         }
      }

      void init(int limit, Predicate<BlockPos> loaded, Function<BlockPos, BlockState> state) {
         this.init(limit, 9223372036854775807L, loaded, state);
      }

      void init(int limit, long deadline, Predicate<BlockPos> loaded, Function<BlockPos, BlockState> state) {
         for (int inSlice = 0; inSlice < Math.max(1, limit) && !this.init_Z() && (inSlice == 0 || System.nanoTime() < deadline); inSlice++) {
            this.init_J++;
            BlockPos at = new BlockPos(this.load, this.read, this.write);
            BlockPos max = this.init_List.get(this.init_I).max();
            if (++this.load > max.getX()) {
               this.load = this.init_List.get(this.init_I).min().getX();
               if (++this.read > max.getY()) {
                  this.read = this.init_List.get(this.init_I).min().getY();
                  if (++this.write > max.getZ()) {
                     this.init_I++;
                     this.init_V();
                  }
               }
            }

            if (!this.init_Set.contains(at.asLong()) && loaded.test(at) && state.apply(at).getBlock() instanceof ShulkerBoxBlock) {
               this.init_Z = true;
               this.init_BlockPos = at;
            }
         }
      }

      long init_J() {
         long total = 0L;

         for (AiPrintPreparation.Inner_NetHelper.Inner_DataUtil region : this.init_List) {
            total += ((long)region.max().getX() - region.min().getX() + 1L)
               * ((long)region.max().getY() - region.min().getY() + 1L)
               * ((long)region.max().getZ() - region.min().getZ() + 1L);
         }

         return total;
      }

      boolean init_Z() {
         return this.init_Z || this.init_I >= this.init_List.size();
      }

      private record Inner_DataUtil(BlockPos min, BlockPos max) {
      }
   }

   record Inner_StateCache(long size, FileTime modified, Object fileKey) {
      static AiPrintPreparation.Inner_StateCache read(Path file) throws IOException {
         BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
         return new AiPrintPreparation.Inner_StateCache(attributes.size(), attributes.lastModifiedTime(), attributes.fileKey());
      }
   }

   static final class Inner_StreamUtil {
      final List<BlockPos> init_List;
      private final Map<Long, BlockState> init_Map = new HashMap<>();
      int init_I;

      Inner_StreamUtil(List<BlockPos> positions) {
         this.init_List = positions;
      }

      void init(int limit, Function<BlockPos, BlockState> state) {
         this.init(limit, 9223372036854775807L, state);
      }

      void init(int limit, long deadline, Function<BlockPos, BlockState> state) {
         int first = this.init_I;
         int end = Math.min(this.init_List.size(), this.init_I + Math.max(1, limit));

         while (this.init_I < end && (this.init_I == first || System.nanoTime() < deadline)) {
            BlockPos at = this.init_List.get(this.init_I++);
            this.init_Map.put(at.asLong(), state.apply(at));
         }
      }

      boolean init_Z() {
         return this.init_I == this.init_List.size();
      }

      Map<Long, BlockState> init_Map() {
         if (!this.init_Z()) {
            throw new IllegalStateException("Leaf snapshot is incomplete");
         } else {
            return Collections.unmodifiableMap(this.init_Map);
         }
      }
   }

   static final class Inner_SyncManager implements AutoCloseable {
      private final ExecutorService init_ExecutorService = Executors.newSingleThreadExecutor(task -> {
         Thread thread = new Thread(task, "schematic-preparation");
         thread.setDaemon(true);
         return thread;
      });
      private Future<?> init_Future;
      private boolean init_Z;

      synchronized <T> Future<T> init(Callable<T> action) {
         if (this.init_Z) {
            throw new CancellationException("Schematic preparation cancelled");
         }

         Future<T> next = this.init_ExecutorService.submit(() -> {
            AiBuilder.init();
            T result = action.call();
            AiBuilder.init();
            return result;
         });
         this.init_Future = next;
         return next;
      }

      @Override
      public synchronized void close() {
         if (!this.init_Z) {
            this.init_Z = true;
            if (this.init_Future != null) {
               this.init_Future.cancel(true);
            }

            this.init_Future = null;
            this.init_ExecutorService.shutdownNow();
         }
      }
   }

   record Inner_TaskQueue(Object world, Object player) {
      boolean matches(Object currentWorld, Object currentPlayer) {
         return this.world != null && this.player != null && this.world == currentWorld && this.player == currentPlayer;
      }
   }
}
