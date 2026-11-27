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
   private static AiSchematicPlacement init_bW;
   private static boolean init_Z;
   private final ClientLevel init_ClientLevel;
   private final String init_Str;
   private List<AiBuilder.CellSpec> init_List = List.of();
   private List<AiBuilder.CellSpec> load_List = List.of();
   private BlockPos init_BlockPos;
   private int init_I;
   private int load_I;
   private String load_Str = "none";
   private String read_Str = "";
   private boolean load_Z = true;
   private boolean read_Z;
   private boolean write_Z;
   private boolean update_Z;
   private boolean sync = true;
   private boolean flush;
   private long init_J;
   private long load_J;
   private long read_J;
   private AiPlacementSearch init_aQ;
   private AiPlacementSearch.Inner_CachePool init_aQ_CachePool;
   private AABB init_AABB;
   private Path init_Path;
   private List<String> read_List;
   private boolean close;
   private double init_D = 6.0;
   private Vec3 init_Vec3;
   private List<AiBuilder.CellSpec> write_List;
   private int read_I;
   private int write_I;
   private int update_I;
   private final AiLatestTransform<AiSchematicPlacement.Inner_StreamUtil, List<AiBuilder.CellSpec>> init_aj = new AiLatestTransform<>(
      ForkJoinPool.commonPool(),
      action -> Minecraft.getInstance().execute(action),
      request -> AiBuilder.normalizeSchematicBlueprint(
            new AiBuilder.Blueprint(
               BlockPos.ZERO, AiBuilder.normalizeCells(AiBuilder.transformCells(request.from(), request.turn(), request.flip()), 2097152, 4096)
            )
         )
         .cells(),
      this::init_V_List_Throwable
   );
   private String write_Str = "";
   private Map<String, Integer> init_Map = Map.of();

   static void ensureTickHandler() {
      if (!init_Z) {
         init_Z = true;
         ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            AiSchematicPlacement preview = init_bW;
            if (preview != null) {
               if (mc.level == preview.init_ClientLevel && mc.player != null && AiAssistant.getInstance().isShown()) {
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
         if (init_bW != null) {
            init_bW.cancel();
         }

         this.init_ClientLevel = mc.level;
         this.init_Str = AiSchematic.validateName(name);
         this.init_BlockPos = mc.player.blockPosition().relative(mc.player.getDirection(), 4);
         init_bW = this;
         int token = ++this.load_I;
         CompletableFuture.<AiSchematicPlacement.Inner_DataUtil>supplyAsync(
               () -> {
                  try {
                     Path path = AiSchematic.resolveFile(AiSchematic.init(), name);
                     long size = Files.size(path);
                     long modified = Files.getLastModifiedTime(path).toMillis();
                     AiSchematic.Inner_BufferHelper schematic = AiSchematic.load(path, BlockPos.ZERO, 0, "none", false, 2097152);
                     if (Files.size(path) == size && Files.getLastModifiedTime(path).toMillis() == modified) {
                        AiBuilder.Blueprint survival = AiBuilder.normalizeSchematicBlueprint(
                           new AiBuilder.Blueprint(BlockPos.ZERO, AiBuilder.normalizeCells(schematic.blueprint().cells(), 2097152, 4096))
                        );
                        String impossible = AiSchematicReadiness.init(AiSchematicReadiness.assess(survival, 1, null));
                        return new AiSchematicPlacement.Inner_DataUtil(path, size, modified, schematic.blueprint().cells(), schematic.entities(), impossible);
                     } else {
                        throw new IllegalArgumentException("Schematic changed while loading; select it again");
                     }
                  } catch (Exception e) {
                     throw new CompletionException(e);
                  }
               }
            )
            .whenComplete((loaded, failure) -> mc.execute(() -> {
               if (!this.read_Z && token == this.load_I && mc.level == this.init_ClientLevel) {
                  this.load_Z = false;
                  if (failure != null) {
                     this.write_Z = true;
                     this.read_Str = init_Str_Throwable(failure);
                  } else {
                     this.init_Path = loaded.path;
                     this.init_J = loaded.size;
                     this.load_J = loaded.modified;
                     this.init_List = loaded.cells;
                     this.init_Map = loaded.entities;
                     this.write_Str = loaded.impossible;
                     if (!this.write_Str.isEmpty()) {
                        AutismClientAddon.LOG.info("[AI] schematic preview \"{}\": {}", name, this.write_Str);
                     }

                     this.close();
                  }
               }
            }));
      } else {
         throw new IllegalArgumentException("Join a world first");
      }
   }

   private static String init_Str_Throwable(Throwable error) {
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
      this.read_Z = true;
      this.load_I++;
      this.init_aQ = null;
      this.update_Z = false;
      this.init_aj.close();
      if (!handoff) {
         AiVisualization.clearOwner(this);
      }

      this.init_AABB = null;
      if (init_bW == this) {
         init_bW = null;
      }
   }

   @Override
   boolean isActive() {
      return !this.read_Z && !this.load_Z && !this.write_Z && !this.load_List.isEmpty() && Minecraft.getInstance().level == this.init_ClientLevel;
   }

   @Override
   boolean isReadyToConfirm() {
      return this.isActive() && !this.init_aj.isUpdating();
   }

   boolean isSearching() {
      return this.update_Z || this.init_aQ != null;
   }

   String getError() {
      return this.read_Str;
   }

   @Override
   BlockPos getOrigin() {
      return this.init_BlockPos;
   }

   @Override
   int getRotation() {
      return this.init_I;
   }

   @Override
   String getMirror() {
      return this.load_Str;
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
      return this.load_List.size();
   }

   String getStatus() {
      if (this.read_Z) {
         return "World changed; select the schematic again";
      } else if (this.load_Z) {
         return "Loading schematic...";
      } else if (!this.read_Str.isEmpty()) {
         return this.read_Str;
      } else if (this.init_aj.isUpdating()) {
         return "Updating schematic orientation...";
      } else if (this.update_Z) {
         return "Preparing nearby placement comparison...";
      } else if (this.init_aQ != null) {
         return "Comparing nearby sites: " + this.init_aQ.comparedCount() + "/" + this.init_aQ.load();
      } else if (!this.write_Str.isEmpty()) {
         return "Some saved cells may be skipped during construction.";
      } else {
         return this.init_aQ_CachePool == null
            ? this.load_List.size() + " blocks; choose a placement"
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
         this.init_BlockPos,
         "rotation",
         this.init_I,
         "assumeFly",
         this.flush,
         "cells",
         this.load_List.size(),
         "placedEstimate",
         this.init_aQ_CachePool == null ? -1 : this.init_aQ_CachePool.place(),
         "clearedEstimate",
         this.init_aQ_CachePool == null ? -1 : this.init_aQ_CachePool.remove(),
         "supportEstimate",
         this.init_aQ_CachePool == null ? -1 : this.init_aQ_CachePool.support()
      );
   }

   String sizeText() {
      return this.init_AABB == null ? "" : (int)this.init_AABB.getXsize() + " x " + (int)this.init_AABB.getYsize() + " x " + (int)this.init_AABB.getZsize();
   }

   @Override
   void init(int x, int y, int z) {
      if (this.isActive()) {
         this.init_BlockPos = this.init_BlockPos.offset(x, y, z);
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
         this.init_I = AiPlacementControls.rotateBy(this.init_I, steps);
         this.close();
      }
   }

   @Override
   void cycleMirror() {
      if (this.isActive()) {
         this.load_Str = this.load_Str.equals("none") ? "x" : (this.load_Str.equals("x") ? "z" : "none");
         this.close();
      }
   }

   @Override
   void init(BlockPos position, int turn, String mirroring) {
      if (this.isActive()) {
         this.init_BlockPos = position;
         this.init_Vec3 = null;
         this.init_I = turn;
         this.load_Str = mirroring;
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
      if (this.isActive() && this.init_ClientLevel.hasChunkAt(this.init_BlockPos)) {
         this.init_BlockPos = new BlockPos(
            this.init_BlockPos.getX(),
            this.init_ClientLevel.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, this.init_BlockPos.getX(), this.init_BlockPos.getZ()),
            this.init_BlockPos.getZ()
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
            BlockHitResult hit = this.init_ClientLevel.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, mc.player));
            BlockPos before = this.init_BlockPos;
            if (hit.getType() == Type.BLOCK && AiTerrain.isLoaded(this.init_ClientLevel, hit.getBlockPos())) {
               this.init_BlockPos = hit.getBlockPos().relative(hit.getDirection());
               this.init_D = Vec3.atCenterOf(this.init_BlockPos).distanceTo(eye);
            } else {
               this.init_D = AiPlacementControls.init(eye, direction, this.init_BlockPos.getY() + 0.5, this.init_D);
               this.init_V_Vec3(AiPlacementControls.init(eye, direction, this.init_D));
            }

            this.init_Vec3 = direction;
            if (!this.init_BlockPos.equals(before)) {
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
            BlockPos before = this.init_BlockPos;
            Vec3 eye = Minecraft.getInstance().gameRenderer.mainCamera().position();
            this.init_Vec3 = this.init_Vec3_D(0.5, 0.5, 1.0, 1.0);
            this.init_V_Vec3(AiPlacementControls.init(eye, this.init_Vec3, this.init_D));
            if (!this.init_BlockPos.equals(before)) {
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
            Vec3 fromEye = Vec3.atCenterOf(this.init_BlockPos).subtract(eye);
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
         WorldBorder border = this.init_ClientLevel.getWorldBorder();
         this.init_BlockPos = BlockPos.containing(
            Mth.clamp(point.x, border.getMinX(), border.getMaxX() - 1.0),
            Mth.clamp(point.y, this.init_ClientLevel.getMinY(), this.init_ClientLevel.getMaxY() - 1),
            Mth.clamp(point.z, border.getMinZ(), border.getMaxZ() - 1.0)
         );
      }
   }

   private void close() {
      this.load_I++;
      this.update_Z = false;
      this.init_aQ = null;
      this.init_aQ_CachePool = null;
      this.init_aj.init(new AiSchematicPlacement.Inner_StreamUtil(this.init_List, this.init_I, this.load_Str));
   }

   private void init_V_List_Throwable(List<AiBuilder.CellSpec> result, Throwable failure) {
      if (!this.read_Z && Minecraft.getInstance().level == this.init_ClientLevel) {
         if (failure != null) {
            this.write_Z = true;
            this.read_Str = init_Str_Throwable(failure);
         } else {
            AutismClientAddon.LOG
               .info("[AI] schematic preview \"{}\": {} decoded cells, {} survival cells", this.init_Str, this.init_List.size(), result.size());
            if (result.isEmpty()) {
               this.write_Z = true;
               this.read_Str = "Schematic has no blocks that can be built in survival";
            } else {
               boolean first = this.load_List.isEmpty();
               this.load_List = result;
               this.open();
               if (first) {
                  this.snapToGround();
               }
            }
         }
      }
   }

   private void open() {
      this.load_I++;
      this.update_Z = false;
      this.read_List = null;
      this.init_aQ = null;
      this.init_aQ_CachePool = null;
      if (!this.write_Z) {
         this.read_Str = "";
      }

      if (this.write_List != this.load_List) {
         this.read_I = this.write_I = this.update_I = 0;

         for (AiBuilder.CellSpec c : this.load_List) {
            this.read_I = Math.max(this.read_I, c.dx() + 1);
            this.write_I = Math.max(this.write_I, c.dy() + 1);
            this.update_I = Math.max(this.update_I, c.dz() + 1);
         }

         this.write_List = this.load_List;
      }

      AiVisualization.load(this);
      AiVisualization.init(this, this.init_BlockPos, this.load_List, 0, true);
      this.init_AABB = this.load_List.isEmpty()
         ? null
         : new AABB(
            this.init_BlockPos.getX(),
            this.init_BlockPos.getY(),
            this.init_BlockPos.getZ(),
            this.init_BlockPos.getX() + this.read_I,
            this.init_BlockPos.getY() + this.write_I,
            this.init_BlockPos.getZ() + this.update_I
         );
   }

   void flush() {
      if (this.isReadyToConfirm()) {
         Minecraft mc = Minecraft.getInstance();
         if (this.init_AABB != null && this.init_AABB.getXsize() * this.init_AABB.getYsize() * this.init_AABB.getZsize() > 1000000.0) {
            this.read_Str = "Large sparse schematic: choose placement in world. Nearby comparison is limited to one million cells.";
         } else {
            BlockPos player = mc.player.blockPosition();
            String selectedMirror = this.load_Str;
            int token = ++this.load_I;
            this.read_Str = "";
            this.init_aQ_CachePool = null;
            this.init_aQ = null;
            this.update_Z = true;
            CompletableFuture.<List<List<AiBuilder.CellSpec>>>supplyAsync(
                  () -> {
                     List<List<AiBuilder.CellSpec>> variants = new ArrayList<>();

                     for (int turn = 0; turn < 360; turn += 90) {
                        variants.add(
                           AiBuilder.normalizeSchematicBlueprint(
                                 new AiBuilder.Blueprint(
                                    BlockPos.ZERO, AiBuilder.normalizeCells(AiBuilder.transformCells(this.init_List, turn, selectedMirror), 2097152)
                                 )
                              )
                              .cells()
                        );
                     }

                     return variants;
                  }
               )
               .whenComplete((variants, failure) -> mc.execute(() -> {
                  if (!this.read_Z && token == this.load_I && mc.level == this.init_ClientLevel) {
                     this.update_Z = false;
                     if (failure != null) {
                        this.read_Str = init_Str_Throwable(failure);
                     } else {
                        this.init_V_BlockPos_List(player, variants);
                     }
                  }
               }));
         }
      }
   }

   private void init_V_BlockPos_List(BlockPos player, List<List<AiBuilder.CellSpec>> variants) {
      List<AiPlacementSearch.Inner_DataUtil> sites = new ArrayList<>();

      for (int turn = 0; turn < 360; turn += 90) {
         List<AiBuilder.CellSpec> transformed = variants.get(turn / 90);

         for (int dx = -24; dx <= 24; dx += 4) {
            for (int dz = -24; dz <= 24; dz += 4) {
               if (dx * dx + dz * dz >= 16 && dx * dx + dz * dz <= 576) {
                  BlockPos anchor = player.offset(dx, 0, dz);
                  if (AiTerrain.isLoaded(this.init_ClientLevel, anchor)) {
                     int height = this.init_ClientLevel.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, anchor.getX(), anchor.getZ());
                     if (height > this.init_ClientLevel.getMinY() + 1) {
                        for (int dy = 0; dy >= -1; dy--) {
                           double vertical = height + dy - player.getY();
                           double distance = dx * dx + dz * dz + vertical * vertical;
                           if (!(distance > 576.0)) {
                              sites.add(new AiPlacementSearch.Inner_DataUtil(new BlockPos(anchor.getX(), height + dy, anchor.getZ()), turn, transformed, distance));
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      sites.sort(Comparator.comparingDouble(AiPlacementSearch.Inner_DataUtil::distance));
      this.read_Str = "";
      this.init_aQ_CachePool = null;
      this.read_J = System.nanoTime();
      this.init_aQ = new AiPlacementSearch(sites, this.init_aQ_ConfigStore(), this.sync);
   }

   private void poll() {
      if (this.isActive()) {
         AiVisualization.init(this, this.init_BlockPos, this.load_List, 0, true);
      }

      if (this.init_aQ != null) {
         this.init_aQ.init(4096, System.nanoTime() + 2000000L);
         if (this.init_aQ.isDone() || this.init_aQ.init_J() > 1500000L || System.nanoTime() - this.read_J > 8000000000L) {
            AiPlacementSearch.Inner_CachePool found = this.init_aQ.getResult();
            this.init_aQ = null;
            if (found == null) {
               this.read_Str = "No safe site found. Move the preview or try another area.";
               return;
            }

            this.init_BlockPos = found.candidate().origin();
            this.init_Vec3 = null;
            this.init_I = found.candidate().rotation();
            this.load_List = found.candidate().cells();
            this.open();
            this.init_aQ_CachePool = found;
         }
      }
   }

   private AiPlacementSearch.Inner_ConfigStore init_aQ_ConfigStore() {
      return new AiPlacementSearch.Inner_ConfigStore() {
         @Override
         public AiPlacementSearch.Inner_StreamUtil classify(BlockPos p, AiBuilder.CellSpec cell) {
            if (AiTerrain.isLoaded(AiSchematicPlacement.this.init_ClientLevel, p)
               && !AiSchematicPlacement.this.init_ClientLevel.isOutsideBuildHeight(p)
               && AiSchematicPlacement.this.init_ClientLevel.getWorldBorder().isWithinBounds(p)) {
               BlockState live = AiSchematicPlacement.this.init_ClientLevel.getBlockState(p);
               if (cell != null && AiBuilder.matchesSchematicCell(cell, live)) {
                  return AiPlacementSearch.Inner_StreamUtil.MATCH;
               } else if (cell == null && !AiSchematicPlacement.this.sync) {
                  return AiPlacementSearch.Inner_StreamUtil.MATCH;
               } else if (live.isAir()) {
                  return cell == null ? AiPlacementSearch.Inner_StreamUtil.MATCH : AiPlacementSearch.Inner_StreamUtil.PLACE;
               } else {
                  return !live.hasBlockEntity()
                        && !(live.getDestroySpeed(AiSchematicPlacement.this.init_ClientLevel, p) < 0.0F)
                        && live.getFluidState().isEmpty()
                        && AiSchematicActions.init(live)
                        && AiSchematicActions.init(AiSchematicPlacement.this.init_ClientLevel, p)
                     ? AiPlacementSearch.Inner_StreamUtil.BREAK
                     : AiPlacementSearch.Inner_StreamUtil.UNSAFE;
               }
            } else {
               return AiPlacementSearch.Inner_StreamUtil.UNSAFE;
            }
         }

         @Override
         public boolean init_Z(BlockPos p, AiBuilder.CellSpec cell) {
            BlockState below = AiSchematicPlacement.this.init_ClientLevel.getBlockState(p.below());
            return cell.dy() == 0
               && below.getCollisionShape(AiSchematicPlacement.this.init_ClientLevel, p.below()).isEmpty()
               && !AiBuilder.toBlockState(cell).getCollisionShape(AiSchematicPlacement.this.init_ClientLevel, p).isEmpty();
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

      if (this.load_List.size() > 2097152) {
         return "Preview only: " + this.load_List.size() + " cells exceeds the builder limit of 2097152; split the schematic or build it in layers";
      }

      try {
         AiSchematic.init(this.load_List);
         if (Files.size(this.init_Path) == this.init_J && Files.getLastModifiedTime(this.init_Path).toMillis() == this.load_J) {
            AiPlacementSearch.Inner_ConfigStore view = this.init_aQ_ConfigStore();

            for (AiBuilder.CellSpec cell : this.load_List) {
               if (view.classify(this.init_BlockPos.offset(cell.dx(), cell.dy(), cell.dz()), cell) == AiPlacementSearch.Inner_StreamUtil.UNSAFE) {
                  return "Placement overlaps protected blocks, fluids or unloaded terrain. Move the preview.";
               }
            }

            return "";
         } else {
            return "Schematic changed; select it again";
         }
      } catch (Exception e) {
         return init_Str_Throwable(e);
      }
   }

   JsonObject buildArguments() {
      return init(this.init_Str, this.init_BlockPos, this.init_I, this.load_Str, this.sync, this.flush, this.load_List.size());
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

      List<AiCellMaterials.Inner_CachePool> needed = AiCellMaterials.combine(AiCellMaterials.init(this.load_List, this.init_BlockPos, this.init_ClientLevel));
      Inventory inventory = Minecraft.getInstance().player.getInventory();
      JsonObject missingMaterials = creative ? new JsonObject() : AiBuilder.missingMaterialsForContainer(needed, inventory, true);
      List<String> out = new ArrayList<>();

      for (AiCellMaterials.Inner_CachePool requirement : creative ? List.<AiCellMaterials.Inner_CachePool>of() : needed) {
         JsonObject shortage = missingMaterials.getAsJsonObject(requirement.itemSpec());
         int missing = shortage == null ? 0 : shortage.get("missing").getAsInt();
         if (missing > 0) {
            out.add(init_Str_CharEncoder_CacheP(requirement) + ": " + missing + " more needed");
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

      for (AiBuilder.CellSpec cell : this.load_List) {
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

      Map<String, Integer> notPlaced = new TreeMap<>(this.init_Map);
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

   private static String init_Str_CharEncoder_CacheP(AiCellMaterials.Inner_CachePool requirement) {
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
      ArrayList<AiCellMaterials.Inner_CachePool> plans = new ArrayList<>();

      for (AiBuilder.CellSpec cell : cells) {
         plans.addAll(AiSchematicActions.materialPlans(cell));
      }

      return AiCellMaterials.itemCounts(plans);
   }

   private record Inner_DataUtil(Path path, long size, long modified, List<AiBuilder.CellSpec> cells, Map<String, Integer> entities, String impossible) {
   }

   private record Inner_StreamUtil(List<AiBuilder.CellSpec> from, int turn, String flip) {
   }
}
