package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AbstractFurnaceBlock;

final class AiFurnaceReturn implements AiOperation {
   private final Minecraft mc = Minecraft.getInstance();
   private final Object level = this.mc.level;
   private final Object connection = this.mc.getConnection();
   private final Object player = this.mc.player;
   private final BlockPos station;
   private final BlockPos home;
   private final long deadline = System.nanoTime() + 45000000000L;
   private AiOperation travel;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private int attempts;
   private String reason;
   private JsonObject evidence = new JsonObject();

   AiFurnaceReturn(BlockPos station, String reason) {
      this.station = station.immutable();
      this.home = this.mc.player.blockPosition();
      this.reason = reason;
   }

   static autismclient.ai.engine.AiPathfinder.PathGoal init(
      final autismclient.ai.engine.AiPathfinder.PathGoal interaction, final BlockPos station, final BlockPos origin, final boolean repairs
   ) {
      return new autismclient.ai.engine.AiPathfinder.PathGoal() {
         @Override
         public autismclient.ai.engine.AiPathfinder.PathGoal init(autismclient.ai.engine.AiPathfinder.GridView grid) {
            return AiFurnaceReturn.init(interaction.init(grid), station, origin, repairs);
         }

         @Override
         public boolean isInGoal(int x, int y, int z) {
            return interaction.isInGoal(x, y, z);
         }

         @Override
         public boolean isInGoalWithGrid(int x, int y, int z, autismclient.ai.engine.AiPathfinder.GridView grid) {
            return interaction.isInGoalWithGrid(x, y, z, grid);
         }

         @Override
         public double heuristic(int x, int y, int z) {
            return interaction.heuristic(x, y, z);
         }

         @Override
         public boolean allowsCell(BlockPos p) {
            return p.getX() >= Math.min(origin.getX(), station.getX()) - 12
               && p.getX() <= Math.max(origin.getX(), station.getX()) + 12
               && p.getZ() >= Math.min(origin.getZ(), station.getZ()) - 12
               && p.getZ() <= Math.max(origin.getZ(), station.getZ()) + 12
               && p.getY() >= Math.min(origin.getY(), station.getY()) - 4
               && p.getY() <= Math.max(origin.getY(), station.getY()) + 4;
         }

         @Override
         public boolean load(BlockPos p) {
            return repairs && this.allowsCell(p) && !p.equals(station) && !p.equals(station.below()) && interaction.load(p);
         }

         @Override
         public boolean read(BlockPos p) {
            return repairs && this.allowsCell(p) && !p.equals(station) && !p.equals(station.above());
         }

         @Override
         public Set<autismclient.ai.engine.AiPathfinder.MoveType> disallowedMoves() {
            return Set.of(
               autismclient.ai.engine.AiPathfinder.MoveType.DIG_DOWN_1,
               autismclient.ai.engine.AiPathfinder.MoveType.JUMP,
               autismclient.ai.engine.AiPathfinder.MoveType.JUMP_GAP_2,
               autismclient.ai.engine.AiPathfinder.MoveType.JUMP_GAP_3,
               autismclient.ai.engine.AiPathfinder.MoveType.PARKOUR_ASCEND,
               autismclient.ai.engine.AiPathfinder.MoveType.SPRINT_JUMP
            );
         }
      };
   }

   @Override
   public AiOperation.Result tick() {
      if (this.load_Z
         || this.mc.level != this.level
         || this.mc.getConnection() != this.connection
         || this.mc.player != this.player
         || this.mc.player == null) {
         return this.init(AiOperation.Status.CANCELLED, "Furnace return world/player changed");
      }

      if (!this.mc.player.isAlive()) {
         return this.init(AiOperation.Status.FAILED, "Player died returning to the furnace");
      }

      if (AiTerrain.isLoaded(this.mc.level, this.station)
         && !(this.mc.level.getBlockState(this.station).getBlock() instanceof AbstractFurnaceBlock)) {
         return this.init(AiOperation.Status.FAILED, "The committed furnace was removed; its contents need reconciliation");
      }

      if ((this.travel == null || this.travel.canInterruptNow())
         && this.mc.player.onGround()
         && autismclient.ai.engine.AiBlockInteraction.findReachableHit(this.mc, this.station) != null) {
         this.read_Z = this.travel != null;
         return this.init(
            AiOperation.Status.SUCCEEDED,
            this.init_Z ? "Recovered a legal furnace stance after the return route changed" : "Returned to a legal furnace stance over existing terrain"
         );
      }

      if (System.nanoTime() >= this.deadline) {
         return this.init(
            this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
            "Furnace return exceeded its recovery budget; committed contents remain at the station"
         );
      }

      if (this.travel == null) {
         autismclient.ai.engine.AiPathfinder.PathGoal interaction = new autismclient.ai.engine.InteractionGoal(
            this.station, Math.max(1.5, this.mc.player.blockInteractionRange() - 0.25), this.mc.player.getEyeHeight()
         );
         this.travel = AiGoto.travelToCompat(
            this.station,
            0.6,
            this.init_Z,
            this.init_Z,
            this.init_Z,
            this.init_Z ? 25 : 12,
            autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
            true,
            2.0,
            init(interaction, this.station, this.home, this.init_Z),
            new AiGoto.GotoOptions(false, false, 1, true, true, true)
         );
         this.attempts++;
      }

      AiOperation.Result r = this.travel.tick();
      this.evidence = AiRunResources.parseEvidenceJson(r.dataJson());
      if (!r.terminal()) {
         return AiGameOperations.result(
            AiOperation.Status.RUNNING,
            (this.init_Z ? "Repairing a changed furnace return route: " : "Replanning a walk back to the furnace: ") + r.message(),
            this.init()
         );
      } else {
         boolean uncertain = r.status() == AiOperation.Status.UNCERTAIN || this.travel.requiresReconciliationAfterEmergency();
         this.travel.cancel();
         this.travel = null;
         if (uncertain) {
            return this.init(AiOperation.Status.UNCERTAIN, "Furnace return has an unsettled action: " + r.message());
         } else if (r.status() == AiOperation.Status.SUCCEEDED && autismclient.ai.engine.AiBlockInteraction.findReachableHit(this.mc, this.station) != null) {
            return this.init(AiOperation.Status.SUCCEEDED, "Returned to the furnace using a newly verified stance");
         } else if (r.status() != AiOperation.Status.CANCELLED && !this.init_Z) {
            this.init_Z = true;
            this.reason = "Existing walking return unavailable: " + r.message();
            return AiGameOperations.result(
               AiOperation.Status.RUNNING, "Walking route changed; allow bounded repairs without digging straight down", this.init()
            );
         } else {
            return this.init(r.status() == AiOperation.Status.SUCCEEDED ? AiOperation.Status.FAILED : r.status(), r.message());
         }
      }
   }

   JsonObject init() {
      return AiJson.objectOf(
         "returnMode",
         this.init_Z ? "repair_changed_route" : "walk_existing_terrain",
         "returnReason",
         this.reason,
         "returnEditsAllowed",
         this.init_Z,
         "returnAttempts",
         this.attempts,
         "station",
         this.station.toShortString(),
         "arrivalBeforeRouteCompletion",
         this.read_Z,
         "arrivalProof",
         "live_station_ray_on_supported_ground",
         "returnNavigation",
         this.evidence
      );
   }

   private AiOperation.Result init(AiOperation.Status status, String message) {
      JsonObject data = this.init();
      this.cancel();
      return AiGameOperations.result(status, message, data);
   }

   @Override
   public boolean canInterruptNow() {
      return this.travel == null || this.travel.canInterruptNow();
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.travel != null && this.travel.requiresReconciliationAfterEmergency();
   }

   @Override
   public void cancel() {
      this.load_Z = true;
      if (this.travel != null) {
         this.travel.cancel();
      }
   }
}
