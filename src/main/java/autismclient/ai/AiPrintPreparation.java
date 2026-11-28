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
   private final Minecraft client = Minecraft.getInstance();
   private final AiPrintPreparation.WorldGuard worldGuard = new AiPrintPreparation.WorldGuard(this.client.level, this.client.player);
   private final Provider registries = this.client.level.registryAccess();
   private final BlockPos startPos = this.client.player.blockPosition().immutable();
   private final Path schematicPath;
   private final BlockPos origin;
   private final AiBuilder.BuildOptions options;
   private final boolean skipUnknown;
   private final boolean verify;
   private final long deadlineNanos;
   private final long startedNanos = System.nanoTime();
   private final AiPrintPreparation.PreparationWorker worker = new AiPrintPreparation.PreparationWorker();
   private Future<?> pendingTask;
   private AiPrintPreparation.PreparationPhase phase = AiPrintPreparation.PreparationPhase.DECODE;
   private AiPrintPreparation.DecodedSchematic decoded;
   private AiPrintPreparation.NormalizedSchematic normalized;
   private AiPrintPreparation.LeafSnapshotScan leafScan;
   private AiPrintPreparation.MaterialPlan materialPlan;
   private AiPrintPreparation.StockScan worldScan;
   private JsonObject missingMaterials;
   private boolean materialsChecked;
   private boolean cancelled;
   private AiOperation printOperation;
   private AiOperation.Result finalResult;
   private long progressSteps;
   private long totalSteps = 1L;
   private long maxTickNanos;
   private long finishedNanos;
   private int cellCount;
   private int skippedCells;
   private JsonArray preflightIssues = new JsonArray();

   AiPrintPreparation(JsonObject args, AiBuilder.BuildOptions power) {
      if (!args.has("blocks") && !args.has("blueprint")) {
         AiPrintSupply.validateOptions(power);
         this.options = power;
         this.origin = AiBuilder.auditOrigin(args, this.startPos);
         this.schematicPath = AiSchematic.resolveFile(AiSchematic.directory(), AiJson.getString(args, "schematic", ""));
         this.skipUnknown = AiJson.getBoolean(args, "skipUnknown", false);
         this.verify = AiJson.getBoolean(args, "verify", true);
         int seconds = args.has("timeoutSeconds") ? AiJson.getInt(args, "timeoutSeconds", 0) : 0;
         this.deadlineNanos = seconds > 0 ? System.nanoTime() + seconds * 1000000000L : 0L;
         this.pendingTask = this.worker.submit(this::decodeSchematic);
      } else {
         throw new IllegalArgumentException("schematic is mutually exclusive with blocks/blueprint; pass one source");
      }
   }

   private AiPrintPreparation.DecodedSchematic decodeSchematic() throws IOException {
      AiPrintPreparation.FileStamp stamp = AiPrintPreparation.FileStamp.of(this.schematicPath);
      AiSchematic.LoadedSchematic loaded = AiSchematic.load(
         this.schematicPath, this.origin, this.options.rotation(), this.options.mirror(), this.skipUnknown, 2097152
      );
      AiBuilder.checkNotCancelled();
      AiBuilder.Blueprint blueprint = this.options.layers()
         ? AiBuilder.sliceLayerBand(
            loaded.blueprint(), this.options.layerStart(), this.options.layerHeight()
         )
         : loaded.blueprint();
      requireBuildableCells(blueprint);
      LinkedHashSet<BlockPos> neighbors = new LinkedHashSet<>(AiBuilder.collectLeafNeighborPositions(blueprint));
      neighbors.addAll(AiSchematicReadiness.read(blueprint));
      neighbors.addAll(AiSchematicReadiness.load(blueprint));
      if (!stamp.equals(AiPrintPreparation.FileStamp.of(this.schematicPath))) {
         throw new IOException("Schematic changed while it was being read; start the build again");
      } else {
         return new AiPrintPreparation.DecodedSchematic(
            blueprint,
            this.options.withSignText(loaded.signText()).withMachine(AiSchematicMachinePlan.fromSchematic(loaded, blueprint)),
            List.copyOf(neighbors),
            stamp
         );
      }
   }

   private AiPrintPreparation.NormalizedSchematic normalize(AiPrintPreparation.DecodedSchematic input, Map<Long, BlockState> snapshot) throws IOException {
      AiBuilder.Blueprint blueprint = AiBuilder.normalizeSchematicBlueprint(input.blueprint(), snapshot);
      requireBuildableCells(blueprint);
      AiBuilder.checkNotCancelled();
      AiSchematicReadiness.BuildableAssessment buildable = AiSchematicReadiness.assessBuildable(blueprint, 16, snapshot);
      blueprint = buildable.blueprint();
      AiBuilder.checkNotCancelled();
      List<autismclient.ai.print.PrintTarget> targets = List.copyOf(AiPrintRules.targets(blueprint));
      autismclient.ai.print.PrintCore.PreparedGeometry geometry = autismclient.ai.print.PrintCore.prepareGeometry(targets, AiBuilder::checkNotCancelled);
      AiBuilder.checkNotCancelled();
      AiBuilder.Blueprint construction = AiBuilder.stripPoweredLevers(blueprint);
      AiPrintPreparation.SourceIndex source = AiPrintPreparation.SourceIndex.of(blueprint);
      if (!input.stamp().equals(AiPrintPreparation.FileStamp.of(this.schematicPath))) {
         throw new IOException("Schematic changed during preparation; start the build again");
      }

      AiBuilder.BuildOptions power = input.power().withMachine(input.power().machine().init(blueprint));
      return new AiPrintPreparation.NormalizedSchematic(
         blueprint,
         construction,
         power,
         buildable.report(),
         buildable.skipped(),
         targets,
         source,
         power.machine().machineSpec(blueprint.origin(), this.registries),
         geometry
      );
   }

   static void requireBuildableCells(AiBuilder.Blueprint blueprint) {
      if (blueprint.cells().isEmpty()) {
         throw new IllegalArgumentException("The selected schematic layers contain no buildable cells");
      }
   }

   @Override
   public AiOperation.Result tick() {
      boolean preparing = this.printOperation == null;
      long started = System.nanoTime();
      AiOperation.Result result = this.tickPreparation();
      if (preparing) {
         this.maxTickNanos = Math.max(this.maxTickNanos, System.nanoTime() - started);
      }

      if (!preparing && !result.terminal()) {
         return result;
      }

      JsonObject data = JsonParser.parseString(result.dataJson()).getAsJsonObject();
      data.addProperty("preparationDone", this.progressSteps);
      data.addProperty("preparationTotal", this.totalSteps);
      data.addProperty("filePreparationDone", this.progressSteps);
      data.addProperty("maxPreparationTickMs", this.maxTickNanos / 1000000.0);
      data.addProperty("preparationWallMs", ((this.finishedNanos == 0L ? System.nanoTime() : this.finishedNanos) - this.startedNanos) / 1000000L);
      String message = result.message();
      if (this.skippedCells > 0) {
         data.addProperty("preflightSkippedCells", this.skippedCells);
         data.add("preflightIssues", this.preflightIssues.deepCopy());
         data.addProperty("sourceComplete", false);
         data.addProperty("machineComplete", false);
         if (result.status() == AiOperation.Status.SUCCEEDED) {
            message = message + ". Skipped " + this.skippedCells + (this.skippedCells == 1 ? " cell" : " cells") + " that could not be placed as saved.";
         }
      }

      return new AiOperation.Result(result.status(), message, data.toString(), result.evidence(), result.failure());
   }

   private AiOperation.Result tickPreparation() {
      if (this.finalResult != null) {
         return this.finalResult;
      }

      if (this.cancelled) {
         return this.fail(AiOperation.Status.CANCELLED, "Build cancelled", AiFailure.CANCELLED);
      }

      if (!this.worldGuard.matches(this.client.level, this.client.player)) {
         return this.fail(AiOperation.Status.CANCELLED, "World or player changed", AiFailure.CANCELLED);
      }

      if (!this.client.player.isAlive()) {
         return this.fail(AiOperation.Status.FAILED, "The player died", AiFailure.EXECUTION_FAILED);
      }

      if (this.deadlineNanos != 0L && System.nanoTime() >= this.deadlineNanos) {
         return this.fail(AiOperation.Status.TIMED_OUT, "Requested build deadline reached", AiFailure.TIMEOUT);
      }

      if (this.printOperation != null) {
         AiOperation.Result result = this.printOperation.tick();
         if (result.terminal()) {
            this.finalResult = result;
            this.worker.close();
         }

         return result;
      } else {
         try {
            label216: {
               switch (this.phase) {
                  case DECODE:
                     if (!this.pendingTask.isDone()) {
                        return this.runningResult("Reading schematic");
                     }

                     this.decoded = (AiPrintPreparation.DecodedSchematic)this.pendingTask.get();
                     this.pendingTask = null;
                     this.progressSteps++;
                     this.cellCount = this.decoded.blueprint().cells().size();
                     this.totalSteps = this.progressSteps + this.decoded.leafNeighbors().size() + this.cellCount + 2L;
                     this.leafScan = new AiPrintPreparation.LeafSnapshotScan(this.decoded.leafNeighbors());
                     this.phase = AiPrintPreparation.PreparationPhase.LEAVES;
                     break;
                  case LEAVES:
                     int before = this.leafScan.nextIndex;
                     this.leafScan
                        .advance(
                           4096,
                           System.nanoTime() + 2000000L,
                           pos -> AiTerrain.isLoaded(this.client.level, pos) ? this.client.level.getBlockState(pos) : Blocks.AIR.defaultBlockState()
                        );
                     this.progressSteps = this.progressSteps + (this.leafScan.nextIndex - before);
                     if (this.leafScan.isComplete()) {
                        Map<Long, BlockState> snapshot = this.leafScan.snapshot();
                        AiPrintPreparation.DecodedSchematic input = this.decoded;
                        this.pendingTask = this.worker.submit(() -> this.normalize(input, snapshot));
                        this.phase = AiPrintPreparation.PreparationPhase.NORMALIZE;
                     }
                     break;
                  case NORMALIZE:
                     if (!this.pendingTask.isDone()) {
                        return this.runningResult("Preparing schematic rules and targets");
                     }

                     this.normalized = (AiPrintPreparation.NormalizedSchematic)this.pendingTask.get();
                     this.pendingTask = null;
                     this.progressSteps++;
                     this.cellCount = this.normalized.blueprint().cells().size();
                     this.worker.close();
                     this.skippedCells = this.normalized.skipped();

                     for (AiSchematicReadiness.ReadinessIssue issue : this.normalized.audit().issues()) {
                        AiBuilder.CellSpec cell = issue.cell();
                        this.preflightIssues
                           .add(
                              AiJson.objectOf(
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

                     if (this.cellCount == 0) {
                        this.finishedNanos = System.nanoTime();
                        return this.fail(AiOperation.Status.FAILED, "No cells in the selected schematic can be placed at this site", AiFailure.PLACEMENT_IMPOSSIBLE);
                     }

                     this.materialPlan = new AiPrintPreparation.MaterialPlan(this.normalized.stockPlan(), this.normalized.source().keys());
                     this.phase = this.isCreativeInstabuild() ? AiPrintPreparation.PreparationPhase.HANDOFF : AiPrintPreparation.PreparationPhase.MATERIALS;
                     break;
                  case MATERIALS:
                     if (this.isCreativeInstabuild()) {
                        this.phase = AiPrintPreparation.PreparationPhase.HANDOFF;
                     } else {
                        int beforeWrite = this.materialPlan.nextCell;
                        this.materialPlan.scanSlice(4096, System.nanoTime() + 2000000L, this.client.level);
                        this.progressSteps = this.progressSteps + (this.materialPlan.nextCell - beforeWrite);
                        if (this.materialPlan.isDone()) {
                           this.missingMaterials = this.computeMissingMaterials();
                           this.materialsChecked = true;
                           if (this.missingMaterials.isEmpty()) {
                              this.phase = AiPrintPreparation.PreparationPhase.HANDOFF;
                           } else {
                              if (!this.normalized.power().shulkerStock()) {
                                 return this.load();
                              }

                              this.worldScan = new AiPrintPreparation.StockScan(
                                 this.startPos, this.normalized.blueprint().origin(), this.normalized.source()
                              );
                              this.totalSteps = Math.max(this.totalSteps, this.progressSteps + this.worldScan.totalVolume() + 1L);
                              this.phase = AiPrintPreparation.PreparationPhase.STOCK;
                           }
                        }
                     }
                     break;
                  case STOCK:
                     if (this.isCreativeInstabuild()) {
                        this.phase = AiPrintPreparation.PreparationPhase.HANDOFF;
                     } else {
                        long beforeUpdate = this.worldScan.scannedBlocks;
                        this.worldScan
                           .scanSlice(4096, System.nanoTime() + 2000000L, pos -> AiTerrain.isLoaded(this.client.level, pos), this.client.level::getBlockState);
                        this.progressSteps = this.progressSteps + (this.worldScan.scannedBlocks - beforeUpdate);
                        if (this.worldScan.done) {
                           this.phase = AiPrintPreparation.PreparationPhase.HANDOFF;
                        } else if (this.worldScan.isDone()) {
                           this.missingMaterials = this.computeMissingMaterials();
                           if (!this.missingMaterials.isEmpty()) {
                              return this.load();
                           }

                           this.phase = AiPrintPreparation.PreparationPhase.HANDOFF;
                        }
                     }
                     break;
                  case HANDOFF:
                     if (!this.isCreativeInstabuild() && !this.materialsChecked) {
                        this.phase = AiPrintPreparation.PreparationPhase.MATERIALS;
                     } else {
                        if (this.isCreativeInstabuild() || this.worldScan != null && this.worldScan.done) {
                           break label216;
                        }

                        this.missingMaterials = this.computeMissingMaterials();
                        if (this.missingMaterials.isEmpty()) {
                           break label216;
                        }

                        if (!this.normalized.power().shulkerStock()) {
                           return this.load();
                        }

                        this.worldScan = new AiPrintPreparation.StockScan(
                           this.startPos, this.normalized.blueprint().origin(), this.normalized.source()
                        );
                        this.totalSteps = Math.max(this.totalSteps, this.progressSteps + this.worldScan.totalVolume() + 1L);
                        this.phase = AiPrintPreparation.PreparationPhase.STOCK;
                     }
               }

               String var10001;
               switch (this.phase) {
                  case DECODE:
                     var10001 = "Reading schematic";
                     break;
                  case LEAVES:
                     var10001 = "Checking outside schematic context";
                     break;
                  case NORMALIZE:
                     var10001 = "Preparing schematic rules and targets";
                     break;
                  case MATERIALS:
                     var10001 = "Checking schematic materials";
                     break;
                  case STOCK:
                     var10001 = "Looking for external shulker stock";
                     break;
                  case HANDOFF:
                     var10001 = "Schematic prepared";
                     break;
                  default:
                     throw new MatchException(null, null);
               }

               return this.runningResult(var10001);
            }

            int seconds = this.deadlineNanos == 0L ? 0 : (int)Math.max(1L, (this.deadlineNanos - System.nanoTime() + 999999999L) / 1000000000L);
            if (this.normalized.power().repeat() != null) {
               this.printOperation = new AiPrintRepeat(
                  this.normalized.blueprint(),
                  this.normalized.power(),
                  this.verify,
                  seconds,
                  this.normalized.targets(),
                  this.worldScan == null ? null : this.worldScan.stockPos,
                  this.normalized.nativeActions(),
                  this.normalized.geometry()
               );
            } else {
               AiPrintOp operation = new AiPrintOp(
                  this.normalized.blueprint(),
                  seconds,
                  this.normalized.power(),
                  this.normalized.targets(),
                  this.normalized.nativeActions(),
                  this.normalized.geometry()
               );
               if (this.worldScan != null && this.worldScan.stockPos != null) {
                  operation.notifyBlockChanged(this.worldScan.stockPos);
               }

               this.printOperation = operation;
            }

            this.progressSteps = this.totalSteps = Math.max(this.progressSteps, this.totalSteps);
            this.finishedNanos = System.nanoTime();
            this.decoded = null;
            this.normalized = null;
            this.leafScan = null;
            this.materialPlan = null;
            this.worldScan = null;
            this.missingMaterials = null;
            return this.runningResult(
               this.skippedCells == 0
                  ? "Schematic prepared; starting construction"
                  : "Starting construction. Skipping " + this.skippedCells + (this.skippedCells == 1 ? " cell" : " cells") + " that cannot be placed as saved."
            );
         } catch (InterruptedException failure) {
            return this.fail(AiOperation.Status.CANCELLED, "Schematic preparation interrupted", AiFailure.CANCELLED);
         } catch (CancellationException failure) {
            return this.fail(AiOperation.Status.CANCELLED, "Schematic preparation cancelled", AiFailure.CANCELLED);
         } catch (ExecutionException failure) {
            Throwable cause = failure.getCause();
            return this.fail(AiOperation.Status.FAILED, "Could not prepare schematic: " + cause.getMessage(), AiFailure.EXECUTION_FAILED);
         } catch (RuntimeException failure) {
            return this.fail(AiOperation.Status.FAILED, "Could not prepare schematic: " + failure.getMessage(), AiFailure.EXECUTION_FAILED);
         }
      }
   }

   private boolean isCreativeInstabuild() {
      return this.client.gameMode != null
         && this.client.gameMode.getPlayerMode() == GameType.CREATIVE
         && this.client.player.getAbilities().instabuild;
   }

   private JsonObject computeMissingMaterials() {
      return AiBuilder.missingMaterialsForContainer(
         this.materialPlan.requirements(), this.client.player.getInventory(), this.normalized.power().shulkerStock()
      );
   }

   private AiOperation.Result load() {
      this.worker.close();
      this.finishedNanos = System.nanoTime();
      this.finalResult = AiOperation.Result.failed(
         AiFailure.MISSING_MATERIALS,
         "Missing materials for the schematic; "
            + (
               this.normalized.power().shulkerStock()
                  ? "carried stock is insufficient and no nearby external shulker was found"
                  : "shulker stock is disabled"
            )
            + ": "
            + this.missingMaterials,
         AiJson.objectOf(
               "missing",
               this.missingMaterials,
               "placed",
               0,
               "skipped",
               0,
               "failed",
               new JsonArray(),
               "shulkerStock",
               false,
               "cells",
               this.normalized.blueprint().cells().size()
            )
            .toString()
      );
      return this.finalResult;
   }

   private AiOperation.Result runningResult(String message) {
      return new AiOperation.Result(
         AiOperation.Status.RUNNING,
         message,
         AiJson.objectOf(
               "preparing", this.printOperation == null, "stage", "prepare", "phase", this.phase.name(), "cells", this.cellCount
            )
            .toString(),
         List.of()
      );
   }

   private AiOperation.Result fail(AiOperation.Status status, String message, AiFailure failure) {
      this.worker.close();
      if (this.finishedNanos == 0L) {
         this.finishedNanos = System.nanoTime();
      }

      if (this.printOperation != null) {
         this.printOperation.cancel();
      }

      this.finalResult = new AiOperation.Result(status, message, "{}", List.of(), failure);
      return this.finalResult;
   }

   @Override
   public void cancel() {
      this.cancelled = true;
      this.worker.close();
      if (this.printOperation != null) {
         this.printOperation.cancel();
      }
   }

   @Override
   public boolean canInterruptNow() {
      return this.printOperation == null || this.printOperation.canInterruptNow();
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.printOperation != null && this.printOperation.requiresReconciliationAfterEmergency();
   }

   private enum PreparationPhase {
      DECODE,
      LEAVES,
      NORMALIZE,
      MATERIALS,
      STOCK,
      HANDOFF;
   }

   static final class MaterialPlan {
      final AiBuilder.Blueprint blueprint;
      final Set<Long> sourceKeys;
      final Set<BlockPos> foundations = new HashSet<>();
      private final Map<AiPrintPreparation.MaterialPlan.RequirementKey, AiCellMaterials.MaterialRequirement> requirementsByKey = new LinkedHashMap<>();
      int nextCell;

      MaterialPlan(AiBuilder.Blueprint blueprint, Set<Long> source) {
         this.blueprint = blueprint;
         this.sourceKeys = source;
      }

      void advance(int limit, LevelReader world) {
         this.scanSlice(limit, 9223372036854775807L, world);
      }

      void scanSlice(int limit, long deadline, LevelReader world) {
         int first = this.nextCell;
         int end = Math.min(this.blueprint.cells().size(), this.nextCell + Math.max(1, limit));

         while (this.nextCell < end && (this.nextCell == first || System.nanoTime() < deadline)) {
            AiBuilder.CellSpec cell = this.blueprint.cells().get(this.nextCell++);
            if (!AiBuilder.isTransientPistonCell(cell)) {
               BlockPos at = this.blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
               BlockState live = world.getBlockState(at);
               if (!AiBuilder.matchesSchematicCell(cell, live)) {
                  for (AiCellMaterials.MaterialRequirement requirement : AiCellMaterials.requirements(cell, live, world.getFluidState(at.above()).is(FluidTags.WATER))) {
                     this.addRequirement(requirement);
                  }

                  BlockPos foundation = AiSchematicActions.foundationFor(cell, at, world, this.sourceKeys);
                  if (foundation != null && this.foundations.add(foundation)) {
                     this.addRequirement(
                        new AiCellMaterials.MaterialRequirement(AiCellMaterials.CellAction.PLACE, "minecraft:dirt", Items.DIRT, 1, AiCellMaterials.MaterialUse.CONSUME)
                     );
                  }
               }
            }
         }
      }

      private void addRequirement(AiCellMaterials.MaterialRequirement requirement) {
         AiPrintPreparation.MaterialPlan.RequirementKey key = new AiPrintPreparation.MaterialPlan.RequirementKey(requirement.itemSpec(), requirement.use());
         AiCellMaterials.MaterialRequirement previous = this.requirementsByKey.get(key);
         if (previous == null) {
            this.requirementsByKey.put(key, requirement);
         } else if (requirement.use() == AiCellMaterials.MaterialUse.CONSUME) {
            this.requirementsByKey
               .put(
                  key,
                  new AiCellMaterials.MaterialRequirement(
                     previous.action(), previous.itemSpec(), previous.item(), Math.addExact(previous.count(), requirement.count()), previous.use()
                  )
               );
         }
      }

      boolean isDone() {
         return this.nextCell == this.blueprint.cells().size();
      }

      List<AiCellMaterials.MaterialRequirement> requirements() {
         return List.copyOf(this.requirementsByKey.values());
      }

      private record RequirementKey(String spec, AiCellMaterials.MaterialUse use) {
      }
   }

   record NormalizedSchematic(
      AiBuilder.Blueprint blueprint,
      AiBuilder.Blueprint stockPlan,
      AiBuilder.BuildOptions power,
      AiSchematicReadiness.ReadinessReport audit,
      int skipped,
      List<autismclient.ai.print.PrintTarget> targets,
      AiPrintPreparation.SourceIndex source,
      AiSchematicMachinePlan.MachineSpec nativeActions,
      autismclient.ai.print.PrintCore.PreparedGeometry geometry
   ) {
   }

   record DecodedSchematic(AiBuilder.Blueprint blueprint, AiBuilder.BuildOptions power, List<BlockPos> leafNeighbors, AiPrintPreparation.FileStamp stamp) {
   }

   record SourceIndex(Set<Long> keys, int minX, int maxX, int minZ, int maxZ) {
      static AiPrintPreparation.SourceIndex of(AiBuilder.Blueprint blueprint) {
         HashSet<Long> keys = new HashSet<>();
         int minX = 2147483647;
         int maxX = -2147483648;
         int minZ = minX;
         int maxZ = maxX;

         for (AiBuilder.CellSpec cell : blueprint.cells()) {
            AiBuilder.checkNotCancelled();
            BlockPos at = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
            keys.add(at.asLong());
            minX = Math.min(minX, at.getX());
            maxX = Math.max(maxX, at.getX());
            minZ = Math.min(minZ, at.getZ());
            maxZ = Math.max(maxZ, at.getZ());
         }

         return new AiPrintPreparation.SourceIndex(Collections.unmodifiableSet(keys), minX, maxX, minZ, maxZ);
      }
   }

   static final class StockScan {
      private final List<AiPrintPreparation.StockScan.ScanRegion> regions = new ArrayList<>(2);
      private final Set<Long> sourceKeys;
      int regionIndex;
      int cursorX;
      int cursorY;
      int cursorZ;
      long scannedBlocks;
      boolean done;
      BlockPos stockPos;

      StockScan(BlockPos player, BlockPos origin, AiPrintPreparation.SourceIndex index) {
         this.sourceKeys = index.keys();
         this.regions.add(new AiPrintPreparation.StockScan.ScanRegion(player.offset(-24, -8, -24), player.offset(24, 8, 24)));
         if (index.minX() <= index.maxX()) {
            this.regions
               .add(
                  new AiPrintPreparation.StockScan.ScanRegion(
                     new BlockPos(index.minX() - 24, origin.getY() - 8, index.minZ() - 24),
                     new BlockPos(index.maxX() + 24, origin.getY() + 8, index.maxZ() + 24)
                  )
               );
         }

         this.resetCursorToRegion();
         BlockPos anyHeightStock = AuditBuilderShulkerStock.findExternalStock(
            player, index.minX(), index.maxX(), index.minZ(), index.maxZ(), this.sourceKeys
         );
         if (anyHeightStock != null) {
            this.done = true;
            this.stockPos = anyHeightStock;
         }
      }

      private void resetCursorToRegion() {
         if (this.regionIndex < this.regions.size()) {
            BlockPos min = this.regions.get(this.regionIndex).min();
            this.cursorX = min.getX();
            this.cursorY = min.getY();
            this.cursorZ = min.getZ();
         }
      }

      void advance(int limit, Predicate<BlockPos> loaded, Function<BlockPos, BlockState> state) {
         this.scanSlice(limit, 9223372036854775807L, loaded, state);
      }

      void scanSlice(int limit, long deadline, Predicate<BlockPos> loaded, Function<BlockPos, BlockState> state) {
         for (int inSlice = 0; inSlice < Math.max(1, limit) && !this.isDone() && (inSlice == 0 || System.nanoTime() < deadline); inSlice++) {
            this.scannedBlocks++;
            BlockPos at = new BlockPos(this.cursorX, this.cursorY, this.cursorZ);
            BlockPos max = this.regions.get(this.regionIndex).max();
            if (++this.cursorX > max.getX()) {
               this.cursorX = this.regions.get(this.regionIndex).min().getX();
               if (++this.cursorY > max.getY()) {
                  this.cursorY = this.regions.get(this.regionIndex).min().getY();
                  if (++this.cursorZ > max.getZ()) {
                     this.regionIndex++;
                     this.resetCursorToRegion();
                  }
               }
            }

            if (!this.sourceKeys.contains(at.asLong()) && loaded.test(at) && state.apply(at).getBlock() instanceof ShulkerBoxBlock) {
               this.done = true;
               this.stockPos = at;
            }
         }
      }

      long totalVolume() {
         long total = 0L;

         for (AiPrintPreparation.StockScan.ScanRegion region : this.regions) {
            total += ((long)region.max().getX() - region.min().getX() + 1L)
               * ((long)region.max().getY() - region.min().getY() + 1L)
               * ((long)region.max().getZ() - region.min().getZ() + 1L);
         }

         return total;
      }

      boolean isDone() {
         return this.done || this.regionIndex >= this.regions.size();
      }

      private record ScanRegion(BlockPos min, BlockPos max) {
      }
   }

   record FileStamp(long size, FileTime modified, Object fileKey) {
      static AiPrintPreparation.FileStamp of(Path file) throws IOException {
         BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
         return new AiPrintPreparation.FileStamp(attributes.size(), attributes.lastModifiedTime(), attributes.fileKey());
      }
   }

   static final class LeafSnapshotScan {
      final List<BlockPos> positions;
      private final Map<Long, BlockState> statesByPos = new HashMap<>();
      int nextIndex;

      LeafSnapshotScan(List<BlockPos> positions) {
         this.positions = positions;
      }

      void advance(int limit, Function<BlockPos, BlockState> state) {
         this.advance(limit, 9223372036854775807L, state);
      }

      void advance(int limit, long deadline, Function<BlockPos, BlockState> state) {
         int first = this.nextIndex;
         int end = Math.min(this.positions.size(), this.nextIndex + Math.max(1, limit));

         while (this.nextIndex < end && (this.nextIndex == first || System.nanoTime() < deadline)) {
            BlockPos at = this.positions.get(this.nextIndex++);
            this.statesByPos.put(at.asLong(), state.apply(at));
         }
      }

      boolean isComplete() {
         return this.nextIndex == this.positions.size();
      }

      Map<Long, BlockState> snapshot() {
         if (!this.isComplete()) {
            throw new IllegalStateException("Leaf snapshot is incomplete");
         } else {
            return Collections.unmodifiableMap(this.statesByPos);
         }
      }
   }

   static final class PreparationWorker implements AutoCloseable {
      private final ExecutorService executor = Executors.newSingleThreadExecutor(task -> {
         Thread thread = new Thread(task, "schematic-preparation");
         thread.setDaemon(true);
         return thread;
      });
      private Future<?> current;
      private boolean closed;

      synchronized <T> Future<T> submit(Callable<T> action) {
         if (this.closed) {
            throw new CancellationException("Schematic preparation cancelled");
         }

         Future<T> next = this.executor.submit(() -> {
            AiBuilder.checkNotCancelled();
            T result = action.call();
            AiBuilder.checkNotCancelled();
            return result;
         });
         this.current = next;
         return next;
      }

      @Override
      public synchronized void close() {
         if (!this.closed) {
            this.closed = true;
            if (this.current != null) {
               this.current.cancel(true);
            }

            this.current = null;
            this.executor.shutdownNow();
         }
      }
   }

   record WorldGuard(Object world, Object player) {
      boolean matches(Object currentWorld, Object currentPlayer) {
         return this.world != null && this.player != null && this.world == currentWorld && this.player == currentPlayer;
      }
   }
}
