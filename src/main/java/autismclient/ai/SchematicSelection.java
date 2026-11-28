package autismclient.ai;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class SchematicSelection extends AiPlacementSession {
   private final Object level;
   BlockPos cornerA;
   BlockPos cornerB;
   BlockPos aimPos;
   private double distance = 2.0;

   void adjustDistance(double scroll) {
      if (Double.isFinite(scroll)) {
         this.distance = Math.clamp(this.distance + scroll * Math.max(0.5, this.distance * 0.15), 0.5, 250.0);
      }

      this.moveToAim(true);
   }

   String distanceText() {
      return String.format(Locale.ROOT, "%.1f blocks", this.distance);
   }

   SchematicSelection(Minecraft mc) {
      this.level = mc.level;
   }

   void pickCorner(boolean left) {
      this.moveToAim(true);
      if (this.aimPos != null) {
         if (left) {
            this.cornerA = this.aimPos;
         } else {
            this.cornerB = this.aimPos;
         }
      }
   }

   SchematicCapture.CaptureArea getArea() {
      return this.isReadyToConfirm() ? SchematicCapture.CaptureArea.between(this.cornerA, this.cornerB) : null;
   }

   @Override
   boolean isReadyToConfirm() {
      return this.cornerA != null && this.cornerB != null;
   }

   @Override
   boolean isActive() {
      Minecraft mc = Minecraft.getInstance();
      return this.level != null && mc.level == this.level && mc.player != null;
   }

   @Override
   void moveToAim(boolean surface) {
      Minecraft mc = Minecraft.getInstance();
      if (!this.isActive()) {
         this.aimPos = null;
      } else {
         Vec3 eye = eyePosition(mc);
         Vec3 direction = lookDirection(mc);
         BlockHitResult hit = mc.level.clip(new ClipContext(eye, eye.add(direction.scale(this.distance)), Block.OUTLINE, Fluid.NONE, mc.player));
         if (hit.getType() == Type.BLOCK) {
            this.aimPos = hit.getBlockPos().immutable();
         } else {
            this.aimPos = BlockPos.containing(eye.add(direction.scale(this.distance)));
         }
      }
   }

   static Vec3 eyePosition(Minecraft mc) {
      return autismclient.modules.FreecamController.isActive() ? autismclient.modules.FreecamController.getInterpolatedEyePos(1.0F) : mc.player.getEyePosition();
   }

   static Vec3 lookDirection(Minecraft mc) {
      return autismclient.modules.FreecamController.isActive()
         ? Vec3.directionFromRotation(autismclient.modules.FreecamController.getInterpolatedPitch(1.0F), autismclient.modules.FreecamController.getInterpolatedYaw(1.0F))
         : mc.player.getViewVector(1.0F);
   }

   static BlockPos defaultTarget(Vec3 eye, Vec3 direction) {
      return BlockPos.containing(eye.add(direction.scale(2.0)));
   }

   @Override
   BlockPos getOrigin() {
      return this.cornerA == null ? BlockPos.ZERO : this.cornerA;
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
   void init(double scroll, double x, double y, double w, double h) {
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
   void init(BlockPos p, int rotation, String mirror) {
   }

   @Override
   void cancel() {
   }
}
