package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

final class AiNavigationSelection extends AiPlacementSession {
   private final Minecraft client = Minecraft.getInstance();
   private final Object boundLevel = this.client.level;
   private final boolean parkour;
   private final AiToolCallParser.ParkourFlags options;
   private final autismclient.ai.engine.AiStandingTarget.TargetCache standingResolver = new autismclient.ai.engine.AiStandingTarget.TargetCache();
   private final List<autismclient.ai.engine.AiStandingTarget.StandingSpot> checkpoints = new ArrayList<>();
   private autismclient.ai.engine.AiStandingTarget.StandingSpot standingTarget;

   AiNavigationSelection(boolean parkour, boolean assumeNoFallDamage) {
      this(parkour, new AiToolCallParser.ParkourFlags(assumeNoFallDamage, -1));
   }

   AiNavigationSelection(boolean parkour, AiToolCallParser.ParkourFlags options) {
      this.parkour = parkour;
      this.options = options == null ? new AiToolCallParser.ParkourFlags(false, -1) : options;
   }

   autismclient.ai.engine.AiStandingTarget.StandingSpot getTarget() {
      return this.standingTarget;
   }

   boolean isParkour() {
      return this.parkour;
   }

   List<autismclient.ai.engine.AiStandingTarget.StandingSpot> checkpoints() {
      return this.checkpoints;
   }

   void addCheckpoint() {
      if (this.updateAndCheck()) {
         if (this.checkpoints.size() >= 31) {
            autismclient.util.bi.read("Checkpoint limit reached. Right-click to set the finish.");
         } else if (this.checkpoints.isEmpty() || !this.checkpoints.getLast().stance().equals(this.standingTarget.stance())) {
            this.checkpoints.add(this.standingTarget);
         }
      }
   }

   void undoCheckpoint() {
      if (!this.checkpoints.isEmpty()) {
         this.checkpoints.removeLast();
      }
   }

   JsonObject buildArgs(BlockPos feet) {
      JsonObject args = AiJson.objectOf("x", feet.getX(), "y", feet.getY(), "z", feet.getZ(), "radius", 0.6);
      if (this.parkour) {
         args = AiToolCallParser.init_JsonObject_JsonObject_ParkourOptions(args, this.options);
         args.addProperty("allowInventory", false);
         JsonArray arr = new JsonArray();
         int count = this.checkpoints.size();
         if (count > 0 && this.checkpoints.getLast().stance().equals(feet)) {
            count--;
         }

         for (int i = 0; i < count; i++) {
            BlockPos cp = this.checkpoints.get(i).stance();
            arr.add(AiJson.objectOf("x", cp.getX(), "y", cp.getY(), "z", cp.getZ()));
         }

         if (!arr.isEmpty()) {
            args.add("checkpoints", arr);
         }
      }

      return args;
   }

   void startGoto() {
      try {
         AiDirectOps.run("game:goto", this.buildArgs(this.standingTarget.stance()));
      } catch (IllegalArgumentException | IllegalStateException e) {
         autismclient.util.bi.write(e.getMessage());
      }
   }

   @Override
   boolean isReadyToConfirm() {
      return this.standingTarget != null && this.standingTarget.valid();
   }

   @Override
   boolean isActive() {
      return this.client.level != null && this.client.level == this.boundLevel && this.client.player != null;
   }

   @Override
   BlockPos getOrigin() {
      return this.isReadyToConfirm() ? this.standingTarget.stance() : BlockPos.ZERO;
   }

   @Override
   int getRotation() {
      return 0;
   }

   @Override
   String getMirror() {
      return "none";
   }

   @Override
   void moveToAim(boolean surface) {
      this.resolveTarget(false);
   }

   void refresh() {
      this.updateAndCheck();
   }

   boolean updateAndCheck() {
      this.resolveTarget(true);
      if (!this.isReadyToConfirm()) {
         autismclient.util.bi.write("Aim at a safe standing surface");
      }

      return this.isReadyToConfirm();
   }

   private void resolveTarget(boolean fresh) {
      if (this.isActive()) {
         this.standingTarget = this.standingResolver
            .init(this.client, SchematicSelection.eyePosition(this.client), SchematicSelection.lookDirection(this.client), 250.0, false, fresh);
      }
   }

   @Override
   void init(double scroll, double x, double y, double width, double height) {
   }

   @Override
   void snapToGround() {
   }

   @Override
   void init(int x, int y, int z) {
   }

   @Override
   void rotate(int steps) {
   }

   @Override
   void cycleMirror() {
   }

   @Override
   void init(BlockPos position, int rotation, String mirror) {
   }

   @Override
   void cancel() {
   }
}
