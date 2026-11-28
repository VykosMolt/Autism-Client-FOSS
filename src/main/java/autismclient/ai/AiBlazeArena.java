package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class AiBlazeArena implements AiOperation {
   private final Minecraft mc = Minecraft.getInstance();
   private final Object level = this.mc.level;
   private final Object connection = this.mc.getConnection();
   private final Object player = this.mc.player;
   private final BlockPos spawner;
   private final List<BlockPos> plannedCells;
   private final BlockPos home;
   private final List<BlockPos> stairStands = new ArrayList<>();
   private int stairCellCount;
   private int stairIndex;
   private int stairsReached;
   private AiOperation stairWalk;
   private AiOperation faceApproach;
   private final Set<BlockPos> approachedCells = new HashSet<>();
   private final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer().fast();
   private final AiFireControl fireControl = new AiFireControl();
   private final long deadline;
   private int cellIndex;
   private int update;
   private int sync;
   private int flush;
   private final JsonArray placementOutcomes = new JsonArray();
   private final JsonArray constructionWalks = new JsonArray();
   private boolean placing;
   private boolean cancelled;

   AiBlazeArena(BlockPos spawner, int seconds) {
      this.spawner = spawner.immutable();
      this.deadline = System.nanoTime() + Math.max(1, seconds) * 1000000000L;
      this.home = this.mc.player.blockPosition();
      this.plannedCells = new ArrayList<>();
      int rise = Math.max(0, Math.min(2, (int)Math.ceil(spawner.getY() + 2.05 - this.mc.player.getEyeY())));
      if (capCells(spawner)
         .stream()
         .filter(p -> p.getY() == spawner.getY() + 2)
         .noneMatch(p -> this.mc.level.getBlockState(p).canBeReplaced())) {
         rise = 0;
      }

      if (rise > 0) {
         for (Direction direction : Plane.HORIZONTAL) {
            List<List<BlockPos>> columns = new ArrayList<>();
            List<BlockPos> stands = new ArrayList<>();
            boolean safe = true;

            for (int n = 1; n <= rise; n++) {
               BlockPos base = this.home.relative(direction, n);
               if (!AiTerrain.isLoaded(this.mc.level, base)
                  || !this.mc.level.getBlockState(base.below()).isCollisionShapeFullBlock(this.mc.level, base.below())) {
                  safe = false;
                  break;
               }

               for (int y = 0; y <= n + 1; y++) {
                  if (!this.mc.level.getBlockState(base.above(y)).isAir() || !this.mc.level.getFluidState(base.above(y)).isEmpty()) {
                     safe = false;
                  }
               }

               if (!safe) {
                  break;
               }

               List<BlockPos> column = new ArrayList<>();

               for (int y = 0; y < n; y++) {
                  column.add(base.above(y));
               }

               columns.add(column);
               stands.add(base.above(n));
            }

            if (safe) {
               for (int n = columns.size() - 1; n >= 0; n--) {
                  this.plannedCells.addAll(columns.get(n));
               }

               this.stairStands.addAll(stands);
               break;
            }
         }
      }

      this.stairCellCount = this.plannedCells.size();
      this.plannedCells.addAll(capCells(spawner));

      for (Entity e : AiWorldScan.findEntitiesByType("minecraft:blaze", 20.0)) {
         if (!(e.distanceToSqr(this.mc.player) < 25.0) && this.mc.player.hasLineOfSight(e)) {
            Vec3 line = e.getEyePosition().subtract(this.mc.player.getEyePosition()).normalize();
            BlockPos screen = BlockPos.containing(this.mc.player.position().add(line.x * 2.0, 0.0, line.z * 2.0));
            if (!(screen.distSqr(spawner) < 4.0) && !screen.equals(this.mc.player.blockPosition())) {
               this.plannedCells.add(screen);
               this.plannedCells.add(screen.above());
               break;
            }
         }
      }
   }

   static List<BlockPos> capCells(BlockPos spawner) {
      List<BlockPos> cells = new ArrayList<>();
      cells.add(spawner.above());
      cells.add(spawner.above(2));

      for (int r = 1; r <= 2; r++) {
         for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
               if (Math.abs(x) + Math.abs(z) == r) {
                  cells.add(spawner.offset(x, 2, z));
               }
            }
         }
      }

      return cells;
   }

   @Override
   public AiOperation.Result tick() {
      if (this.cancelled
         || this.mc.player != this.player
         || this.mc.level != this.level
         || this.mc.getConnection() != this.connection) {
         return this.finish(AiOperation.Status.CANCELLED, "Spawner setup world/player changed");
      }

      if (!this.mc.player.isAlive()) {
         return this.finish(AiOperation.Status.FAILED, "Player died during spawner setup");
      }

      this.flush++;
      if (System.nanoTime() > this.deadline && this.canInterruptNow()) {
         return this.finish(AiOperation.Status.SUCCEEDED, "Spawner setup budget ended; partial geometry recorded");
      }

      if (this.mc.level.getBlockState(this.spawner).is(Blocks.SPAWNER)
         && this.mc.level.getBlockEntity(this.spawner) instanceof SpawnerBlockEntity be
         && "minecraft:blaze".equals(AiWorldScan.spawnerEntityId(be))) {
         if (!this.placementNeedsReconciliation()
            && (this.stairWalk == null || this.stairWalk.canInterruptNow())
            && (this.faceApproach == null || this.faceApproach.canInterruptNow())) {
            for (Entity e : AiWorldScan.findEntitiesByType("minecraft:blaze", 5.0)) {
               if (e instanceof Blaze && AiDefense.load(this.mc, e) || e.distanceToSqr(this.mc.player) < 9.0) {
                  return this.finish(AiOperation.Status.SUCCEEDED, "A blaze is in melee range; prioritize its kill over more construction");
               }
            }

            if (AiCombat.nextIncoming(this.mc) != null) {
               return this.finish(AiOperation.Status.SUCCEEDED, "Incoming shot; hand control back to combat");
            }

            if (this.fireControl.init(false, null)) {
               return this.running(this.fireControl.status());
            }
         }

         if (this.cellIndex >= this.plannedCells.size()) {
            return this.finish(AiOperation.Status.SUCCEEDED, "Small spawner cap and observed shooter screen settled");
         }

         if (this.faceApproach != null) {
            AiOperation.Result r = this.faceApproach.tick();
            if (r.terminal()) {
               this.constructionWalks
                  .add(
                     AiJson.objectOf(
                        "kind",
                        "placement-face",
                        "cell",
                        this.plannedCells.get(this.cellIndex).toShortString(),
                        "result",
                        r.status(),
                        "message",
                        r.message(),
                        "body",
                        this.mc.player.position().toString()
                     )
                  );
               this.faceApproach.cancel();
               this.faceApproach = null;
               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  this.placementOutcomes
                     .add(AiJson.objectOf("cell", this.plannedCells.get(this.cellIndex).toShortString(), "result", "kept", "reason", r.message()));
                  this.sync++;
                  this.cellIndex++;
               }
            }

            return this.running("Reach the roof's actual support side over existing floor");
         } else if (this.stairIndex < this.stairStands.size() && this.cellIndex >= this.stairCellCount) {
            BlockPos stand = this.stairStands.get(this.stairIndex);
            if (!this.mc.level.getBlockState(stand.below()).isCollisionShapeFullBlock(this.mc.level, stand.below())) {
               this.stairIndex = this.stairStands.size();
               return this.running("Construction step unavailable; keep reachable existing cover");
            }

            if (this.stairWalk == null) {
               this.stairWalk = AiGoto.travelToCompat(
                  stand,
                  0.6,
                  false,
                  false,
                  false,
                  3,
                  autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                  false,
                  0.0,
                  null,
                  new AiGoto.GotoOptions(false, false, 1, true, false, false)
               );
            }

            AiOperation.Result r = this.stairWalk.tick();
            if (r.terminal()) {
               this.constructionWalks
                  .add(
                     AiJson.objectOf(
                        "stand", stand.toShortString(), "result", r.status(), "message", r.message(), "body", this.mc.player.position().toString()
                     )
                  );
               this.stairWalk.cancel();
               this.stairWalk = null;
               if (r.status() == AiOperation.Status.SUCCEEDED) {
                  this.stairIndex++;
                  this.stairsReached++;
               } else {
                  this.stairIndex = this.stairStands.size();
               }
            }

            return this.running("Walk onto the confirmed construction stair to reach roof faces");
         } else {
            BlockPos cell = this.plannedCells.get(this.cellIndex);
            if (!this.placing) {
               String refusal = this.keepReason(cell);
               if (!refusal.isEmpty() && !refusal.equals("outside current placement reach")) {
                  this.placementOutcomes.add(AiJson.objectOf("cell", cell.toShortString(), "result", "kept", "reason", refusal));
                  this.sync++;
                  this.cellIndex++;
                  return this.running("Reusing existing cover or preserving the open exit");
               }

               Item material = this.expendableBlock();
               if (material == null) {
                  return this.finish(AiOperation.Status.SUCCEEDED, "No expendable nonflammable blocks; use existing fortress cover");
               }

               if (!autismclient.ai.engine.AiPlacer.canPlaceFromHere(cell, material)) {
                  if (this.cellIndex >= this.stairCellCount && this.approachedCells.add(cell)) {
                     this.faceApproach = AiGoto.approachToPlaceCompat(cell, material, 2);
                     return this.running("Find a visible support face without more scaffolding");
                  }

                  this.placementOutcomes
                     .add(
                        AiJson.objectOf("cell", cell.toShortString(), "result", "kept", "reason", "no reachable support face after bounded approach")
                     );
                  this.sync++;
                  this.cellIndex++;
                  return this.running("Keep a partial cap where no legal support side is reachable");
               }

               this.placer.start(cell, material);
               this.placing = true;
            }

            autismclient.ai.engine.AiPlacer.PlaceStatus status = this.placer.tick();
            if (status != autismclient.ai.engine.AiPlacer.PlaceStatus.WORKING) {
               if (status == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                  this.update++;
               } else {
                  this.sync++;
               }

               this.placementOutcomes
                  .add(
                     AiJson.objectOf(
                        "cell",
                        cell.toShortString(),
                        "result",
                        status,
                        "reason",
                        status == autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED ? this.placer.failureReason() : "server block change confirmed",
                        "body",
                        this.mc.player.position().toString(),
                        "eyeY",
                        this.mc.player.getEyeY(),
                        "onGround",
                        this.mc.player.onGround(),
                        "velocity",
                        this.mc.player.getDeltaMovement().toString()
                     )
                  );
               this.placer.reset();
               this.placing = false;
               this.cellIndex++;
            }

            return this.running("Set minimal spawner escape cap / fireball screen (" + this.update + " blocks confirmed)");
         }
      } else {
         return this.finish(AiOperation.Status.FAILED, "Observed blaze spawner no longer present");
      }
   }

   private String keepReason(BlockPos cell) {
      if (!AiTerrain.isLoaded(this.mc.level, cell)) {
         return "unobserved cell";
      }

      if (!this.mc.level.getBlockState(cell).canBeReplaced()) {
         return "existing block retained";
      }

      if (!this.mc.level.getFluidState(cell).isEmpty()) {
         return "fluid preserved";
      }

      if (new AABB(cell).inflate(0.15).intersects(this.mc.player.getBoundingBox())) {
         return "body/exit clearance";
      }

      if (!AiCombat.isSafeStep(this.mc, this.mc.player.blockPosition())) {
         return "unsafe current footing";
      }

      if (this.mc.player.getEyePosition().distanceTo(Vec3.atCenterOf(cell)) > this.mc.player.blockInteractionRange() - 0.2) {
         return "outside current placement reach";
      }

      if (!this.mc.level.getEntitiesOfClass(LivingEntity.class, new AABB(cell)).isEmpty()) {
         return "living entity occupies cell";
      }

      if (cell.getY() < this.spawner.getY() + 1
         && new AABB(cell).clip(this.mc.player.getEyePosition(), Vec3.atCenterOf(this.spawner)).isPresent()) {
         return "melee lane preserved";
      }

      for (Direction d : Direction.values()) {
         if (!this.mc.level.getBlockState(cell.relative(d)).getCollisionShape(this.mc.level, cell.relative(d)).isEmpty()) {
            return "";
         }
      }

      return "no existing support face";
   }

   private Item expendableBlock() {
      if (AiRunResources.isCampaignActive() && AiRunResources.spareBuildingBlockCount() <= 32) {
         return null;
      }

      for (int i = 0; i < 36; i++) {
         ItemStack stack = this.mc.player.getInventory().getItem(i);
         if (AiItems.isPlainItem(stack) && stack.getItem() instanceof BlockItem block) {
            Block b = block.getBlock();
            if (b == Blocks.COBBLESTONE || b == Blocks.COBBLED_DEEPSLATE || b == Blocks.NETHERRACK || b == Blocks.NETHER_BRICKS || b == Blocks.STONE) {
               return stack.getItem();
            }
         }
      }

      return null;
   }

   private JsonObject progressData() {
      return AiJson.objectOf(
         "spawner",
         this.spawner.toShortString(),
         "blocksConfirmed",
         this.update,
         "skipped",
         this.sync,
         "planned",
         this.plannedCells.size(),
         "visited",
         this.cellIndex,
         "ticks",
         this.flush,
         "completeEnclosure",
         false,
         "placementOutcomes",
         this.placementOutcomes,
         "constructionStairs",
         this.stairStands.size(),
         "constructionStairsReached",
         this.stairsReached,
         "constructionWalks",
         this.constructionWalks,
         "home",
         this.home.toShortString(),
         "purpose",
         "limit hover/escape locally and screen observed fireball lane"
      );
   }

   private AiOperation.Result running(String m) {
      return AiGameOperations.result(AiOperation.Status.RUNNING, m, this.progressData());
   }

   private AiOperation.Result finish(AiOperation.Status s, String m) {
      JsonObject d = this.progressData();
      this.cancel();
      return AiGameOperations.result(s, m, d);
   }

   private boolean placementNeedsReconciliation() {
      return this.placing && this.placer.needsReconciliation();
   }

   @Override
   public boolean canInterruptNow() {
      return !this.placementNeedsReconciliation()
         && this.fireControl.canInterrupt()
         && (this.stairWalk == null || this.stairWalk.canInterruptNow())
         && (this.faceApproach == null || this.faceApproach.canInterruptNow());
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.placementNeedsReconciliation()
         || this.fireControl.read()
         || this.stairWalk != null && this.stairWalk.requiresReconciliationAfterEmergency()
         || this.faceApproach != null && this.faceApproach.requiresReconciliationAfterEmergency();
   }

   @Override
   public void cancel() {
      this.cancelled = true;
      this.placer.reset();
      this.fireControl.reset();
      if (this.stairWalk != null) {
         this.stairWalk.cancel();
      }

      if (this.faceApproach != null) {
         this.faceApproach.cancel();
      }
   }
}
