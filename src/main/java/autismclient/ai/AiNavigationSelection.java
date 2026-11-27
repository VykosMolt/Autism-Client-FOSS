package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

final class AiNavigationSelection extends AiPlacementSession {
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final Object init_Obj = this.init_Minecraft.level;
   private final boolean init_Z;
   private final AiToolCallParser.Inner_CachePool options;
   private final autismclient.ai.engine.AiStandingTarget.Inner_DataUtil init_j_DataUtil = new autismclient.ai.engine.AiStandingTarget.Inner_DataUtil();
   private final List<autismclient.ai.engine.AiStandingTarget.Inner_StreamUtil> checkpoints = new ArrayList<>();
   private autismclient.ai.engine.AiStandingTarget.Inner_StreamUtil init_j_StreamUtil;

   AiNavigationSelection(boolean parkour, boolean assumeNoFallDamage) {
      this(parkour, new AiToolCallParser.Inner_CachePool(assumeNoFallDamage, -1));
   }

   AiNavigationSelection(boolean parkour, AiToolCallParser.Inner_CachePool options) {
      this.init_Z = parkour;
      this.options = options == null ? new AiToolCallParser.Inner_CachePool(false, -1) : options;
   }

   autismclient.ai.engine.AiStandingTarget.Inner_StreamUtil getTarget() {
      return this.init_j_StreamUtil;
   }

   boolean isParkour() {
      return this.init_Z;
   }

   List<autismclient.ai.engine.AiStandingTarget.Inner_StreamUtil> checkpoints() {
      return this.checkpoints;
   }

   void addCheckpoint() {
      if (this.updateAndCheck()) {
         if (this.checkpoints.size() >= 31) {
            autismclient.util.bi.read("Checkpoint limit reached. Right-click to set the finish.");
         } else if (this.checkpoints.isEmpty() || !this.checkpoints.getLast().stance().equals(this.init_j_StreamUtil.stance())) {
            this.checkpoints.add(this.init_j_StreamUtil);
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
      if (this.init_Z) {
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

   void write() {
      try {
         AiDirectOps.run("game:goto", this.buildArgs(this.init_j_StreamUtil.stance()));
      } catch (IllegalArgumentException | IllegalStateException e) {
         autismclient.util.bi.write(e.getMessage());
      }
   }

   @Override
   boolean isReadyToConfirm() {
      return this.init_j_StreamUtil != null && this.init_j_StreamUtil.valid();
   }

   @Override
   boolean isActive() {
      return this.init_Minecraft.level != null && this.init_Minecraft.level == this.init_Obj && this.init_Minecraft.player != null;
   }

   @Override
   BlockPos getOrigin() {
      return this.isReadyToConfirm() ? this.init_j_StreamUtil.stance() : BlockPos.ZERO;
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
      this.load(false);
   }

   void update() {
      this.updateAndCheck();
   }

   boolean updateAndCheck() {
      this.load(true);
      if (!this.isReadyToConfirm()) {
         autismclient.util.bi.write("Aim at a safe standing surface");
      }

      return this.isReadyToConfirm();
   }

   private void load(boolean fresh) {
      if (this.isActive()) {
         this.init_j_StreamUtil = this.init_j_DataUtil
            .init(this.init_Minecraft, SchematicSelection.eyePosition(this.init_Minecraft), SchematicSelection.lookDirection(this.init_Minecraft), 250.0, false, fresh);
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
