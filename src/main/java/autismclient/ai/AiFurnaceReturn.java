package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AbstractFurnaceBlock;

final class AiFurnaceReturn implements AiOperation {
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final Object init_Obj = this.init_Minecraft.level;
   private final Object load_Obj = this.init_Minecraft.getConnection();
   private final Object read_Obj = this.init_Minecraft.player;
   private final BlockPos init_BlockPos;
   private final BlockPos load_BlockPos;
   private final long init_J = System.nanoTime() + 45000000000L;
   private AiOperation init_AiOperation;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private int init_I;
   private String init_Str;
   private JsonObject init_JsonObject = new JsonObject();

   AiFurnaceReturn(BlockPos station, String reason) {
      this.init_BlockPos = station.immutable();
      this.load_BlockPos = this.init_Minecraft.player.blockPosition();
      this.init_Str = reason;
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
         public boolean init_Z_I(int x, int y, int z, autismclient.ai.engine.AiPathfinder.GridView grid) {
            return interaction.init_Z_I(x, y, z, grid);
         }

         @Override
         public double heuristic(int x, int y, int z) {
            return interaction.heuristic(x, y, z);
         }

         @Override
         public boolean init_Z(BlockPos p) {
            return p.getX() >= Math.min(origin.getX(), station.getX()) - 12
               && p.getX() <= Math.max(origin.getX(), station.getX()) + 12
               && p.getZ() >= Math.min(origin.getZ(), station.getZ()) - 12
               && p.getZ() <= Math.max(origin.getZ(), station.getZ()) + 12
               && p.getY() >= Math.min(origin.getY(), station.getY()) - 4
               && p.getY() <= Math.max(origin.getY(), station.getY()) + 4;
         }

         @Override
         public boolean load(BlockPos p) {
            return repairs && this.init_Z(p) && !p.equals(station) && !p.equals(station.below()) && interaction.load(p);
         }

         @Override
         public boolean read(BlockPos p) {
            return repairs && this.init_Z(p) && !p.equals(station) && !p.equals(station.above());
         }

         @Override
         public Set<autismclient.ai.engine.AiPathfinder.MoveType> init_Set() {
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
         || this.init_Minecraft.level != this.init_Obj
         || this.init_Minecraft.getConnection() != this.load_Obj
         || this.init_Minecraft.player != this.read_Obj
         || this.init_Minecraft.player == null) {
         return this.init(AiOperation.Status.CANCELLED, "Furnace return world/player changed");
      }

      if (!this.init_Minecraft.player.isAlive()) {
         return this.init(AiOperation.Status.FAILED, "Player died returning to the furnace");
      }

      if (AiTerrain.isLoaded(this.init_Minecraft.level, this.init_BlockPos)
         && !(this.init_Minecraft.level.getBlockState(this.init_BlockPos).getBlock() instanceof AbstractFurnaceBlock)) {
         return this.init(AiOperation.Status.FAILED, "The committed furnace was removed; its contents need reconciliation");
      }

      if ((this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
         && this.init_Minecraft.player.onGround()
         && autismclient.ai.engine.AiBlockInteraction.findReachableHit(this.init_Minecraft, this.init_BlockPos) != null) {
         this.read_Z = this.init_AiOperation != null;
         return this.init(
            AiOperation.Status.SUCCEEDED,
            this.init_Z ? "Recovered a legal furnace stance after the return route changed" : "Returned to a legal furnace stance over existing terrain"
         );
      }

      if (System.nanoTime() >= this.init_J) {
         return this.init(
            this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
            "Furnace return exceeded its recovery budget; committed contents remain at the station"
         );
      }

      if (this.init_AiOperation == null) {
         autismclient.ai.engine.AiPathfinder.PathGoal interaction = new autismclient.ai.engine.InteractionGoal(
            this.init_BlockPos, Math.max(1.5, this.init_Minecraft.player.blockInteractionRange() - 0.25), this.init_Minecraft.player.getEyeHeight()
         );
         this.init_AiOperation = AiGoto.travelToCompat(
            this.init_BlockPos,
            0.6,
            this.init_Z,
            this.init_Z,
            this.init_Z,
            this.init_Z ? 25 : 12,
            autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
            true,
            2.0,
            init(interaction, this.init_BlockPos, this.load_BlockPos, this.init_Z),
            new AiGoto.GotoOptions(false, false, 1, true, true, true)
         );
         this.init_I++;
      }

      AiOperation.Result r = this.init_AiOperation.tick();
      this.init_JsonObject = AiRunResources.parseEvidenceJson(r.dataJson());
      if (!r.terminal()) {
         return AiGameOperations.result(
            AiOperation.Status.RUNNING,
            (this.init_Z ? "Repairing a changed furnace return route: " : "Replanning a walk back to the furnace: ") + r.message(),
            this.init()
         );
      } else {
         boolean uncertain = r.status() == AiOperation.Status.UNCERTAIN || this.init_AiOperation.requiresReconciliationAfterEmergency();
         this.init_AiOperation.cancel();
         this.init_AiOperation = null;
         if (uncertain) {
            return this.init(AiOperation.Status.UNCERTAIN, "Furnace return has an unsettled action: " + r.message());
         } else if (r.status() == AiOperation.Status.SUCCEEDED && autismclient.ai.engine.AiBlockInteraction.findReachableHit(this.init_Minecraft, this.init_BlockPos) != null) {
            return this.init(AiOperation.Status.SUCCEEDED, "Returned to the furnace using a newly verified stance");
         } else if (r.status() != AiOperation.Status.CANCELLED && !this.init_Z) {
            this.init_Z = true;
            this.init_Str = "Existing walking return unavailable: " + r.message();
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
         this.init_Str,
         "returnEditsAllowed",
         this.init_Z,
         "returnAttempts",
         this.init_I,
         "station",
         this.init_BlockPos.toShortString(),
         "arrivalBeforeRouteCompletion",
         this.read_Z,
         "arrivalProof",
         "live_station_ray_on_supported_ground",
         "returnNavigation",
         this.init_JsonObject
      );
   }

   private AiOperation.Result init(AiOperation.Status status, String message) {
      JsonObject data = this.init();
      this.cancel();
      return AiGameOperations.result(status, message, data);
   }

   @Override
   public boolean canInterruptNow() {
      return this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
   }

   @Override
   public void cancel() {
      this.load_Z = true;
      if (this.init_AiOperation != null) {
         this.init_AiOperation.cancel();
      }
   }
}
