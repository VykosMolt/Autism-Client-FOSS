package autismclient.ai;

import autismclient.AutismClientAddon;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.joml.Vector3f;

final class AiSchematicPlacement extends AiPlacementSession {
   private static AiSchematicPlacement active;
   private static boolean listenersRegistered;
   private final ClientLevel level;
   private final String name;
   private List<AiBuilder.CellSpec> decodedCells = List.of();
   private List<AiBuilder.CellSpec> survivalCells = List.of();
   private BlockPos origin;
   private int rotation;
   private int loadToken;
   private String mirror = "none";
   private String loadError = "";
   private boolean loading = true;
   private boolean closed;
   private boolean failed;
   private boolean update_Z;
   private boolean sync = true;
   private boolean flush;
   private long fileSize;
   private long fileModified;
   private long searchStartedAt;
   private AiPlacementSearch search;
   private AiPlacementSearch.CandidateScore init_aQ_CachePool;
   private AABB bounds;
   private Path path;
   private List<String> read_List;
   private boolean close;
   private double init_D = 6.0;
   private Vec3 init_Vec3;
   private List<AiBuilder.CellSpec> sizedCells;
   private int sizeX;
   private int sizeY;
   private int sizeZ;
   private final AiLatestTransform<AiSchematicPlacement.TransformRequest, List<AiBuilder.CellSpec>> transformWorker = new AiLatestTransform<>(
      ForkJoinPool.commonPool(),
      action -> Minecraft.getInstance().execute(action),
      request -> AiBuilder.normalizeSchematicBlueprint(
            new AiBuilder.Blueprint(
               BlockPos.ZERO, AiBuilder.normalizeCells(AiBuilder.transformCells(request.from(), request.turn(), request.flip()), 2097152, 4096)
            )
         )
         .cells(),
      this::onTransformed
   );
   private String impossibleReason = "";
   private Map<String, Integer> entities = Map.of();

   static void ensureTickHandler() {
      if (!listenersRegistered) {
         listenersRegistered = true;
         ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            AiSchematicPlacement preview = active;
            if (preview != null) {
               if (mc.level == preview.level && mc.player != null && AiAssistant.getInstance().isShown()) {
                  preview.poll();
               } else {
                  AiAssistant.getInstance().push();
                  preview.cancel();
               }
            }
         });
      }
   }

   AiSchematicPlacement(String name) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         if (active != null) {
            active.cancel();
         }

         this.level = mc.level;
         this.name = AiSchematic.validateName(name);
         this.origin = mc.player.blockPosition().relative(mc.player.getDirection(), 4);
         active = this;
         int token = ++this.loadToken;
         CompletableFuture.<AiSchematicPlacement.LoadedSchematic>supplyAsync(
               () -> {
                  try {
                     Path path = AiSchematic.resolveFile(AiSchematic.init(), name);
                     long size = Files.size(path);
                     long modified = Files.getLastModifiedTime(path).toMillis();
                     AiSchematic.LoadedSchematic schematic = AiSchematic.load(path, BlockPos.ZERO, 0, "none", false, 2097152);
                     if (Files.size(path) == size && Files.getLastModifiedTime(path).toMillis() == modified) {
                        AiBuilder.Blueprint survival = AiBuilder.normalizeSchematicBlueprint(
                           new AiBuilder.Blueprint(BlockPos.ZERO, AiBuilder.normalizeCells(schematic.blueprint().cells(), 2097152, 4096))
                        );
                        String impossible = AiSchematicReadiness.init(AiSchematicReadiness.assess(survival, 1, null));
                        return new AiSchematicPlacement.LoadedSchematic(path, size, modified, schematic.blueprint().cells(), schematic.entities(), impossible);
                     } else {
                        throw new IllegalArgumentException("Schematic changed while loading; select it again");
                     }
                  } catch (Exception e) {
                     throw new CompletionException(e);
                  }
               }
            )
            .whenComplete((loaded, failure) -> mc.execute(() -> {
               if (!this.closed && token == this.loadToken && mc.level == this.level) {
                  this.loading = false;
                  if (failure != null) {
                     this.failed = true;
                     this.loadError = describeFailure(failure);
                  } else {
                     this.path = loaded.path;
                     this.fileSize = loaded.size;
                     this.fileModified = loaded.modified;
                     this.decodedCells = loaded.cells;
                     this.entities = loaded.entities;
                     this.impossibleReason = loaded.impossible;
                     if (!this.impossibleReason.isEmpty()) {
                        AutismClientAddon.LOG.info("[AI] schematic preview \"{}\": {}", name, this.impossibleReason);
                     }

                     this.close();
                  }
               }
            }));
      } else {
         throw new IllegalArgumentException("Join a world first");
      }
   }

   private static String describeFailure(Throwable error) {
      while ((error instanceof CompletionException || error instanceof ExecutionException) && error.getCause() != null) {
         error = error.getCause();
      }

      return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
   }

   @Override
   void cancel() {
      this.write(false);
   }

   void handOff() {
      this.write(true);
   }

   private void write(boolean handoff) {
      this.closed = true;
      this.loadToken++;
      this.search = null;
      this.update_Z = false;
      this.transformWorker.close();
      if (!handoff) {
         AiVisualization.clearOwner(this);
      }

      this.bounds = null;
      if (active == this) {
         active = null;
      }
   }

   @Override
   boolean isActive() {
      return !this.closed && !this.loading && !this.failed && !this.survivalCells.isEmpty() && Minecraft.getInstance().level == this.level;
   }

   @Override
   boolean isReadyToConfirm() {
      return this.isActive() && !this.transformWorker.isUpdating();
   }

   boolean isSearching() {
      return this.update_Z || this.search != null;
   }

   String getError() {
      return this.loadError;
   }

   @Override
   BlockPos getOrigin() {
      return this.origin;
   }

   @Override
   int getRotation() {
      return this.rotation;
   }

   @Override
   String getMirror() {
      return this.mirror;
   }

   boolean isClearTerrain() {
      return this.sync;
   }

   boolean isAssumeFly() {
      return this.flush;
   }

   String getFlyHint() {
      Minecraft mc = Minecraft.getInstance();
      return init_Str_Z(
         this.flush,
         mc.gameMode == null ? null : mc.gameMode.getPlayerMode(),
         mc.player != null && mc.player.getAbilities().mayfly,
         mc.player != null && mc.player.getAbilities().instabuild
      );
   }

   static String init_Str_Z(boolean assumeFly, GameType mode, boolean mayfly, boolean instabuild) {
      String choice = "Assume Fly " + (assumeFly ? "on" : "off");
      if (mode == null) {
         return choice + ": flight state unavailable.";
      } else {
         boolean permitted = autismclient.ai.print.PrintFlightPermission.init(assumeFly, mode, mayfly, instabuild);
         if (mode == GameType.CREATIVE) {
            return choice + (permitted ? " / native Creative flight available." : " / no Creative flight; use ground movement.");
         } else if (mode != GameType.SURVIVAL) {
            return choice + ": flight building unavailable in this mode.";
         } else {
            return !assumeFly
               ? choice + ": use ground movement in Survival."
               : choice + (permitted ? ": use granted Survival flight." : ": no flight grant; use ground movement.");
         }
      }
   }

   int getCellCount() {
      return this.survivalCells.size();
   }

   String getStatus() {
      if (this.closed) {
         return "World changed; select the schematic again";
      } else if (this.loading) {
         return "Loading schematic...";
      } else if (!this.loadError.isEmpty()) {
         return this.loadError;
      } else if (this.transformWorker.isUpdating()) {
         return "Updating schematic orientation...";
      } else if (this.update_Z) {
         return "Preparing nearby placement comparison...";
      } else if (this.search != null) {
         return "Comparing nearby sites: " + this.search.comparedCount() + "/" + this.search.load();
      } else if (!this.impossibleReason.isEmpty()) {
         return "Some saved cells may be skipped during construction.";
      } else {
         return this.init_aQ_CachePool == null
            ? this.survivalCells.size() + " blocks; choose a placement"
            : this.init_aQ_CachePool.place()
               + " place / "
               + this.init_aQ_CachePool.remove()
               + " clear / "
               + this.init_aQ_CachePool.support()
               + " support estimate";
      }
   }

   JsonObject statusJson() {
      return AiJson.objectOf(
         "ready",
         this.isReadyToConfirm(),
         "searching",
         this.isSearching(),
         "safe",
         this.getStartIssue().isEmpty(),
         "status",
         this.getStatus(),
         "origin",
         this.origin,
         "rotation",
         this.rotation,
         "assumeFly",
         this.flush,
         "cells",
         this.survivalCells.size(),
         "placedEstimate",
         this.init_aQ_CachePool == null ? -1 : this.init_aQ_CachePool.place(),
         "clearedEstimate",
         this.init_aQ_CachePool == null ? -1 : this.init_aQ_CachePool.remove(),
         "supportEstimate",
         this.init_aQ_CachePool == null ? -1 : this.init_aQ_CachePool.support()
      );
   }

   String sizeText() {
      return this.bounds == null ? "" : (int)this.bounds.getXsize() + " x " + (int)this.bounds.getYsize() + " x " + (int)this.bounds.getZsize();
   }

   @Override
   void init(int x, int y, int z) {
      if (this.isActive()) {
         this.origin = this.origin.offset(x, y, z);
         this.init_Vec3 = null;
         this.open();
      }
   }

   void rotateOnce() {
      this.rotate(1);
   }

   @Override
   void rotate(int steps) {
      if (this.isActive()) {
         this.rotation = AiPlacementControls.rotateBy(this.rotation, steps);
         this.close();
      }
   }

   @Override
   void cycleMirror() {
      if (this.isActive()) {
         this.mirror = this.mirror.equals("none") ? "x" : (this.mirror.equals("x") ? "z" : "none");
         this.close();
      }
   }

   @Override
   void init(BlockPos position, int turn, String mirroring) {
      if (this.isActive()) {
         this.origin = position;
         this.init_Vec3 = null;
         this.rotation = turn;
         this.mirror = mirroring;
         this.close();
      }
   }

   void load(boolean value) {
      this.sync = value;
      this.open();
   }

   void read(boolean value) {
      this.flush = value;
   }

   @Override
   void snapToGround() {
      if (this.isActive() && this.level.hasChunkAt(this.origin)) {
         this.origin = new BlockPos(
            this.origin.getX(),
            this.level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, this.origin.getX(), this.origin.getZ()),
            this.origin.getZ()
         );
         this.init_Vec3 = null;
         this.open();
      }
   }

   private Vec3 init_Vec3_D(double screenX, double screenY, double screenWidth, double screenHeight) {
      if (!(screenWidth <= 0.0) && !(screenHeight <= 0.0)) {
         Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
         float f = (float)Math.tan(Math.toRadians(camera.getFov()) / 2.0);
         return new Vec3(
            new Vector3f((float)((2.0 * screenX / screenWidth - 1.0) * screenWidth / screenHeight) * f, (float)(1.0 - 2.0 * screenY / screenHeight) * f, -1.0F)
               .rotate(camera.rotation())
               .normalize()
         );
      } else {
         return Vec3.ZERO;
      }
   }

   void init_V_D(double screenX, double screenY, double screenWidth, double screenHeight) {
      if (this.isActive()) {
         Minecraft mc = Minecraft.getInstance();
         Camera camera = mc.gameRenderer.mainCamera();
         Vec3 direction = this.init_Vec3_D(screenX, screenY, screenWidth, screenHeight);
         if (!(direction.lengthSqr() < 0.5)) {
            double loadedReach = (mc.options.getEffectiveRenderDistance() + 2) * 16.0;
            Vec3 eye = camera.position();
            Vec3 end = eye.add(direction.scale(loadedReach));
            BlockHitResult hit = this.level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, mc.player));
            BlockPos before = this.origin;
            if (hit.getType() == Type.BLOCK && AiTerrain.isLoaded(this.level, hit.getBlockPos())) {
               this.origin = hit.getBlockPos().relative(hit.getDirection());
               this.init_D = Vec3.atCenterOf(this.origin).distanceTo(eye);
            } else {
               this.init_D = AiPlacementControls.init(eye, direction, this.origin.getY() + 0.5, this.init_D);
               this.init_V_Vec3(AiPlacementControls.init(eye, direction, this.init_D));
            }

            this.init_Vec3 = direction;
            if (!this.origin.equals(before)) {
               this.open();
            }
         }
      }
   }

   @Override
   void moveToAim(boolean surface) {
      if (this.isActive()) {
         if (surface) {
            this.init_V_D(0.5, 0.5, 1.0, 1.0);
         } else {
            BlockPos before = this.origin;
            Vec3 eye = Minecraft.getInstance().gameRenderer.mainCamera().position();
            this.init_Vec3 = this.init_Vec3_D(0.5, 0.5, 1.0, 1.0);
            this.init_V_Vec3(AiPlacementControls.init(eye, this.init_Vec3, this.init_D));
            if (!this.origin.equals(before)) {
               this.open();
            }
         }
      }
   }

   @Override
   void init(double scroll, double screenX, double screenY, double width, double height) {
      if (this.isActive()) {
         Vec3 eye = Minecraft.getInstance().gameRenderer.mainCamera().position();
         if (this.init_Vec3 == null) {
            Vec3 fromEye = Vec3.atCenterOf(this.origin).subtract(eye);
            this.init_Vec3 = fromEye.lengthSqr() < 0.01 ? this.init_Vec3_D(screenX, screenY, width, height) : fromEye.normalize();
            this.init_D = Math.max(0.5, fromEye.length());
         }

         Vec3 direction = this.init_Vec3;
         if (!(direction.lengthSqr() < 0.5)) {
            this.init_D = AiPlacementControls.adjustDistance(this.init_D, scroll);
            this.init_V_Vec3(AiPlacementControls.init(eye, direction, this.init_D));
            this.open();
         }
      }
   }

   private void init_V_Vec3(Vec3 point) {
      if (Double.isFinite(point.x) && Double.isFinite(point.y) && Double.isFinite(point.z)) {
         WorldBorder border = this.level.getWorldBorder();
         this.origin = BlockPos.containing(
            Mth.clamp(point.x, border.getMinX(), border.getMaxX() - 1.0),
            Mth.clamp(point.y, this.level.getMinY(), this.level.getMaxY() - 1),
            Mth.clamp(point.z, border.getMinZ(), border.getMaxZ() - 1.0)
         );
      }
   }

   private void close() {
      this.loadToken++;
      this.update_Z = false;
      this.search = null;
      this.init_aQ_CachePool = null;
      this.transformWorker.init(new AiSchematicPlacement.TransformRequest(this.decodedCells, this.rotation, this.mirror));
   }

   private void onTransformed(List<AiBuilder.CellSpec> result, Throwable failure) {
      if (!this.closed && Minecraft.getInstance().level == this.level) {
         if (failure != null) {
            this.failed = true;
            this.loadError = describeFailure(failure);
         } else {
            AutismClientAddon.LOG
               .info("[AI] schematic preview \"{}\": {} decoded cells, {} survival cells", this.name, this.decodedCells.size(), result.size());
            if (result.isEmpty()) {
               this.failed = true;
               this.loadError = "Schematic has no blocks that can be built in survival";
            } else {
               boolean first = this.survivalCells.isEmpty();
               this.survivalCells = result;
               this.open();
               if (first) {
                  this.snapToGround();
               }
            }
         }
      }
   }

   private void open() {
      this.loadToken++;
      this.update_Z = false;
      this.read_List = null;
      this.search = null;
      this.init_aQ_CachePool = null;
      if (!this.failed) {
         this.loadError = "";
      }

      if (this.sizedCells != this.survivalCells) {
         this.sizeX = this.sizeY = this.sizeZ = 0;

         for (AiBuilder.CellSpec c : this.survivalCells) {
            this.sizeX = Math.max(this.sizeX, c.dx() + 1);
            this.sizeY = Math.max(this.sizeY, c.dy() + 1);
            this.sizeZ = Math.max(this.sizeZ, c.dz() + 1);
         }

         this.sizedCells = this.survivalCells;
      }

      AiVisualization.load(this);
      AiVisualization.init(this, this.origin, this.survivalCells, 0, true);
      this.bounds = this.survivalCells.isEmpty()
         ? null
         : new AABB(
            this.origin.getX(),
            this.origin.getY(),
            this.origin.getZ(),
            this.origin.getX() + this.sizeX,
            this.origin.getY() + this.sizeY,
            this.origin.getZ() + this.sizeZ
         );
   }

   void flush() {
      if (this.isReadyToConfirm()) {
         Minecraft mc = Minecraft.getInstance();
         if (this.bounds != null && this.bounds.getXsize() * this.bounds.getYsize() * this.bounds.getZsize() > 1000000.0) {
            this.loadError = "Large sparse schematic: choose placement in world. Nearby comparison is limited to one million cells.";
         } else {
            BlockPos player = mc.player.blockPosition();
            String selectedMirror = this.mirror;
            int token = ++this.loadToken;
            this.loadError = "";
            this.init_aQ_CachePool = null;
            this.search = null;
            this.update_Z = true;
            CompletableFuture.<List<List<AiBuilder.CellSpec>>>supplyAsync(
                  () -> {
                     List<List<AiBuilder.CellSpec>> variants = new ArrayList<>();

                     for (int turn = 0; turn < 360; turn += 90) {
                        variants.add(
                           AiBuilder.normalizeSchematicBlueprint(
                                 new AiBuilder.Blueprint(
                                    BlockPos.ZERO, AiBuilder.normalizeCells(AiBuilder.transformCells(this.decodedCells, turn, selectedMirror), 2097152)
                                 )
                              )
                              .cells()
                        );
                     }

                     return variants;
                  }
               )
               .whenComplete((variants, failure) -> mc.execute(() -> {
                  if (!this.closed && token == this.loadToken && mc.level == this.level) {
                     this.update_Z = false;
                     if (failure != null) {
                        this.loadError = describeFailure(failure);
                     } else {
                        this.init_V_BlockPos_List(player, variants);
                     }
                  }
               }));
         }
      }
   }

   private void init_V_BlockPos_List(BlockPos player, List<List<AiBuilder.CellSpec>> variants) {
      List<AiPlacementSearch.PlacementCandidate> sites = new ArrayList<>();

      for (int turn = 0; turn < 360; turn += 90) {
         List<AiBuilder.CellSpec> transformed = variants.get(turn / 90);

         for (int dx = -24; dx <= 24; dx += 4) {
            for (int dz = -24; dz <= 24; dz += 4) {
               if (dx * dx + dz * dz >= 16 && dx * dx + dz * dz <= 576) {
                  BlockPos anchor = player.offset(dx, 0, dz);
                  if (AiTerrain.isLoaded(this.level, anchor)) {
                     int height = this.level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, anchor.getX(), anchor.getZ());
                     if (height > this.level.getMinY() + 1) {
                        for (int dy = 0; dy >= -1; dy--) {
                           double vertical = height + dy - player.getY();
                           double distance = dx * dx + dz * dz + vertical * vertical;
                           if (!(distance > 576.0)) {
                              sites.add(new AiPlacementSearch.PlacementCandidate(new BlockPos(anchor.getX(), height + dy, anchor.getZ()), turn, transformed, distance));
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      sites.sort(Comparator.comparingDouble(AiPlacementSearch.PlacementCandidate::distance));
      this.loadError = "";
      this.init_aQ_CachePool = null;
      this.searchStartedAt = System.nanoTime();
      this.search = new AiPlacementSearch(sites, this.init_aQ_ConfigStore(), this.sync);
   }

   private void poll() {
      if (this.isActive()) {
         AiVisualization.init(this, this.origin, this.survivalCells, 0, true);
      }

      if (this.search != null) {
         this.search.init(4096, System.nanoTime() + 2000000L);
         if (this.search.isDone() || this.search.init_J() > 1500000L || System.nanoTime() - this.searchStartedAt > 8000000000L) {
            AiPlacementSearch.CandidateScore found = this.search.getResult();
            this.search = null;
            if (found == null) {
               this.loadError = "No safe site found. Move the preview or try another area.";
               return;
            }

            this.origin = found.candidate().origin();
            this.init_Vec3 = null;
            this.rotation = found.candidate().rotation();
            this.survivalCells = found.candidate().cells();
            this.open();
            this.init_aQ_CachePool = found;
         }
      }
   }

   private AiPlacementSearch.WorldProbe init_aQ_ConfigStore() {
      return new AiPlacementSearch.WorldProbe() {
         @Override
         public AiPlacementSearch.CellEdit classify(BlockPos p, AiBuilder.CellSpec cell) {
            if (AiTerrain.isLoaded(AiSchematicPlacement.this.level, p)
               && !AiSchematicPlacement.this.level.isOutsideBuildHeight(p)
               && AiSchematicPlacement.this.level.getWorldBorder().isWithinBounds(p)) {
               BlockState live = AiSchematicPlacement.this.level.getBlockState(p);
               if (cell != null && AiBuilder.matchesSchematicCell(cell, live)) {
                  return AiPlacementSearch.CellEdit.MATCH;
               } else if (cell == null && !AiSchematicPlacement.this.sync) {
                  return AiPlacementSearch.CellEdit.MATCH;
               } else if (live.isAir()) {
                  return cell == null ? AiPlacementSearch.CellEdit.MATCH : AiPlacementSearch.CellEdit.PLACE;
               } else {
                  return !live.hasBlockEntity()
                        && !(live.getDestroySpeed(AiSchematicPlacement.this.level, p) < 0.0F)
                        && live.getFluidState().isEmpty()
                        && AiSchematicActions.init(live)
                        && AiSchematicActions.init(AiSchematicPlacement.this.level, p)
                     ? AiPlacementSearch.CellEdit.BREAK
                     : AiPlacementSearch.CellEdit.UNSAFE;
               }
            } else {
               return AiPlacementSearch.CellEdit.UNSAFE;
            }
         }

         @Override
         public boolean init_Z(BlockPos p, AiBuilder.CellSpec cell) {
            BlockState below = AiSchematicPlacement.this.level.getBlockState(p.below());
            return cell.dy() == 0
               && below.getCollisionShape(AiSchematicPlacement.this.level, p.below()).isEmpty()
               && !AiBuilder.toBlockState(cell).getCollisionShape(AiSchematicPlacement.this.level, p).isEmpty();
         }
      };
   }

   String getStartIssue() {
      if (!this.isReadyToConfirm()) {
         return this.getStatus();
      }

      if (this.isSearching()) {
         return "Wait for the placement search to finish";
      }

      if (this.survivalCells.size() > 2097152) {
         return "Preview only: " + this.survivalCells.size() + " cells exceeds the builder limit of 2097152; split the schematic or build it in layers";
      }

      try {
         AiSchematic.init(this.survivalCells);
         if (Files.size(this.path) == this.fileSize && Files.getLastModifiedTime(this.path).toMillis() == this.fileModified) {
            AiPlacementSearch.WorldProbe view = this.init_aQ_ConfigStore();

            for (AiBuilder.CellSpec cell : this.survivalCells) {
               if (view.classify(this.origin.offset(cell.dx(), cell.dy(), cell.dz()), cell) == AiPlacementSearch.CellEdit.UNSAFE) {
                  return "Placement overlaps protected blocks, fluids or unloaded terrain. Move the preview.";
               }
            }

            return "";
         } else {
            return "Schematic changed; select it again";
         }
      } catch (Exception e) {
         return describeFailure(e);
      }
   }

   JsonObject buildArguments() {
      return init(this.name, this.origin, this.rotation, this.mirror, this.sync, this.flush, this.survivalCells.size());
   }

   static JsonObject init(String name, BlockPos origin, int rotation, String mirror, boolean clearTerrain, boolean assumeFly, int cellCount) {
      return AiJson.objectOf(
         "schematic",
         name,
         "origin",
         AiJson.objectOf("x", origin.getX(), "y", origin.getY(), "z", origin.getZ()),
         "rotation",
         rotation,
         "mirror",
         mirror,
         "clearTerrain",
         clearTerrain,
         "assumeFly",
         assumeFly,
         "craftMissing",
         false,
         "verify",
         true,
         "timeoutSeconds",
         (int)Math.min(2592000L, Math.max(14400L, cellCount * 4L))
      );
   }

   List<String> missingMaterialLines() {
      Minecraft mc = Minecraft.getInstance();
      boolean creative = mc.player != null && mc.gameMode != null && mc.player.getAbilities().instabuild && mc.gameMode.getPlayerMode() == GameType.CREATIVE;
      if (this.read_List != null && this.close == creative) {
         return this.read_List;
      }

      if (!this.isReadyToConfirm()) {
         return List.of();
      }

      List<AiCellMaterials.MaterialRequirement> needed = AiCellMaterials.combine(AiCellMaterials.init(this.survivalCells, this.origin, this.level));
      Inventory inventory = Minecraft.getInstance().player.getInventory();
      JsonObject missingMaterials = creative ? new JsonObject() : AiBuilder.missingMaterialsForContainer(needed, inventory, true);
      List<String> out = new ArrayList<>();

      for (AiCellMaterials.MaterialRequirement requirement : creative ? List.<AiCellMaterials.MaterialRequirement>of() : needed) {
         JsonObject shortage = missingMaterials.getAsJsonObject(requirement.itemSpec());
         int missing = shortage == null ? 0 : shortage.get("missing").getAsInt();
         if (missing > 0) {
            out.add(requirementLabel(requirement) + ": " + missing + " more needed");
         }
      }

      out.addFirst(
         creative
            ? "Blocks supplied from creative inventory as needed"
            : (out.isEmpty() ? "Placement materials ready in inventory and carried shulkers" : "Material shortfall after counting carried shulkers:")
      );
      out.add("Temporary support blocks may also be needed.");
      TreeMap<String, Integer> gaps = new TreeMap<>();
      HashMap<AiBuilder.CellSpec, List<String>> checked = new HashMap<>();
      int contents = 0;

      for (AiBuilder.CellSpec cell : this.survivalCells) {
         AiBuilder.CellSpec key = new AiBuilder.CellSpec(0, 0, 0, cell.block(), cell.properties());

         for (String gap : checked.computeIfAbsent(key, AiBuilder::unsupportedProperties)) {
            gaps.merge(cell.block().replace("minecraft:", "") + " " + gap, 1, Integer::sum);
         }

         BlockState state = AiBuilder.toBlockState(key);
         if (state.hasBlockEntity() && !(state.getBlock() instanceof SignBlock)) {
            contents++;
         }
      }

      if (!gaps.isEmpty()) {
         out.add("Not reproducible in survival:");
         gaps.entrySet().stream().limit(8L).forEach(e -> out.add("  " + e.getKey() + " x" + e.getValue()));
      }

      if (contents > 0) {
         out.add("Saved container contents use available inventory items.");
         out.add("Unavailable contents are skipped and reported.");
         out.add("Other block-entity data (spawners, command blocks, banners, ...) is not restored.");
      }

      Map<String, Integer> notPlaced = new TreeMap<>(this.entities);
      boolean carts = notPlaced.keySet().removeIf(id -> id.equals("minecart") || id.endsWith("_minecart"));
      notPlaced.keySet().removeIf(id -> id.equals("item_frame") || id.equals("glow_item_frame"));
      if (!notPlaced.isEmpty()) {
         StringBuilder named = new StringBuilder();
         notPlaced.entrySet()
            .stream()
            .sorted(Entry.<String, Integer>comparingByValue().reversed())
            .limit(4L)
            .forEach(e -> named.append(named.isEmpty() ? "" : ", ").append(e.getValue()).append(' ').append(e.getKey().replace('_', ' ')));
         if (notPlaced.size() > 4) {
            named.append(", ...");
         }

         out.add("Saved entities not placed: " + named + ".");
      }

      if (carts) {
         out.add("Rail-backed minecarts use supplied minecart items; minecart contents are not restored.");
      }

      this.close = creative;
      this.read_List = List.copyOf(out);
      return this.read_List;
   }

   static int countItem(Inventory inventory, String itemSpec) {
      AiItems.ItemSpec target = AiItems.resolveItem(itemSpec);
      int count = 0;

      for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (target.matches(stack)) {
            count += stack.getCount();
         }
      }

      return count;
   }

   private static String requirementLabel(AiCellMaterials.MaterialRequirement requirement) {
      String var10000;
      switch (requirement.itemSpec()) {
         case "hoe":
            var10000 = "Hoe";
            break;
         case "shovel":
            var10000 = "Shovel";
            break;
         default:
            var10000 = requirement.item().getDefaultInstance().getHoverName().getString();
      }

      return var10000;
   }

   static Map<Item, Integer> materialsFor(List<AiBuilder.CellSpec> cells) {
      ArrayList<AiCellMaterials.MaterialRequirement> plans = new ArrayList<>();

      for (AiBuilder.CellSpec cell : cells) {
         plans.addAll(AiSchematicActions.materialPlans(cell));
      }

      return AiCellMaterials.itemCounts(plans);
   }

   private record LoadedSchematic(Path path, long size, long modified, List<AiBuilder.CellSpec> cells, Map<String, Integer> entities, String impossible) {
   }

   private record TransformRequest(List<AiBuilder.CellSpec> from, int turn, String flip) {
   }
}
