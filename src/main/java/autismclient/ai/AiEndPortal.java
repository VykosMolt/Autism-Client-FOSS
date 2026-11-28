package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class AiEndPortal {
   private AiEndPortal() {
   }

   static AiEndPortal.Inner_StreamUtil inspect(BlockPos portal) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && portal != null) {
         for (BlockPos p : BlockPos.betweenClosed(portal.offset(-3, 0, -3), portal.offset(3, 0, 3))) {
            if (AiTerrain.isLoaded(mc.level, p) && mc.level.getBlockState(p).is(Blocks.END_PORTAL_FRAME)) {
               AiEndPortal.Inner_StreamUtil ring = ringAt(p);
               if (ring != null
                  && ring.active
                  && ring.empty.isEmpty()
                  && Math.abs(ring.center.getX() - portal.getX()) <= 1
                  && Math.abs(ring.center.getZ() - portal.getZ()) <= 1
                  && ring.center.getY() == portal.getY()) {
                  return ring;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   static AiEndPortal.Inner_StreamUtil ringAt(BlockPos seen) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return null;
      }

      for (int dx = -2; dx <= 2; dx++) {
         for (int dz = -2; dz <= 2; dz++) {
            BlockPos center = seen.offset(dx, 0, dz);
            List<BlockPos> frames = new ArrayList<>();
            List<BlockPos> empty = new ArrayList<>();
            boolean valid = true;

            for (Direction side : Plane.HORIZONTAL) {
               for (int offset = -1; offset <= 1; offset++) {
                  BlockPos cell = center.relative(side, 2).relative(side.getClockWise(), offset);
                  if (!AiTerrain.isLoaded(mc.level, cell)) {
                     valid = false;
                     break;
                  }

                  BlockState state = mc.level.getBlockState(cell);
                  if (!state.is(Blocks.END_PORTAL_FRAME) || state.getValue(EndPortalFrameBlock.FACING) != side.getOpposite()) {
                     valid = false;
                     break;
                  }

                  frames.add(cell);
                  if (!state.getValue(EndPortalFrameBlock.HAS_EYE)) {
                     empty.add(cell);
                  }
               }

               if (!valid) {
                  break;
               }
            }

            if (valid && frames.size() == 12 && frames.contains(seen)) {
               boolean active = true;

               for (int x = -1; x <= 1; x++) {
                  for (int z = -1; z <= 1; z++) {
                     active &= AiTerrain.isLoaded(mc.level, center.offset(x, 0, z)) && mc.level.getBlockState(center.offset(x, 0, z)).is(Blocks.END_PORTAL);
                  }
               }

               empty.sort(Comparator.comparingDouble(p -> p.distSqr(mc.player.blockPosition())));
               return new AiEndPortal.Inner_StreamUtil(center, List.copyOf(frames), List.copyOf(empty), active);
            }
         }
      }

      return null;
   }

   static BlockHitResult frameHit(Minecraft mc, Vec3 eye, BlockPos frame, double reach) {
      if (mc.level == null) {
         return null;
      }

      double[] grid = new double[]{0.5, 0.25, 0.75};
      List<Vec3> points = new ArrayList<>();

      for (double a : grid) {
         for (double b : grid) {
            points.add(Vec3.atLowerCornerOf(frame).add(a, 0.8, b));
         }
      }

      for (Direction side : Plane.HORIZONTAL) {
         for (double h : new double[]{0.6, 0.35}) {
            for (double lateral : grid) {
               Vec3 centre = Vec3.atLowerCornerOf(frame).add(0.5 + side.getStepX() * 0.499, h, 0.5 + side.getStepZ() * 0.499);
               points.add(centre.add(side.getStepZ() * (lateral - 0.5), 0.0, side.getStepX() * (lateral - 0.5)));
            }
         }
      }

      for (Vec3 point : points) {
         if (!(eye.distanceToSqr(point) > reach * reach)) {
            Vec3 through = point.add(point.subtract(eye).normalize().scale(0.05));
            BlockHitResult clip = mc.level.clip(new ClipContext(eye, through, Block.OUTLINE, Fluid.NONE, mc.player));
            if (clip.getType() == Type.BLOCK && clip.getBlockPos().equals(frame)) {
               return new BlockHitResult(clip.getLocation(), clip.getDirection(), frame, false);
            }
         }
      }

      return null;
   }

   static AiOperation placeEye(final BlockPos frame) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
         final Object startLevel = mc.level;
         final AiEndPortal.Inner_DataUtil eyePlacement = new AiEndPortal.Inner_DataUtil();
         int init_I;
         int load;
         BlockHitResult init_BlockHitResult;

         @Override
         public AiOperation.Result tick() {
            if (mc.player != null && mc.level == this.startLevel) {
               BlockState state = mc.level.getBlockState(frame);
               if (!state.is(Blocks.END_PORTAL_FRAME)) {
                  return this.init(AiOperation.Status.FAILED, "Portal frame changed");
               }

               if (this.eyePlacement
                  .init(state.getValue(EndPortalFrameBlock.HAS_EYE), AiCraft.countItem("minecraft:ender_eye"), AiServerEvidence.blockRevision(frame), AiServerEvidence.inventoryRevision())) {
                  return this.init(
                     AiOperation.Status.SUCCEEDED, this.eyePlacement.init() ? "Frame eye and one consumed Eye confirmed" : "Frame was already filled"
                  );
               }

               if (state.getValue(EndPortalFrameBlock.HAS_EYE)) {
                  return ++this.init_I > 60
                     ? this.init(AiOperation.Status.UNCERTAIN, "Filled frame observed without the matching one-Eye inventory acknowledgement")
                     : AiGameOperations.result(AiOperation.Status.RUNNING, "Waiting for frame inventory acknowledgement", new JsonObject());
               }

               if (++this.init_I > 60) {
                  return this.init(AiOperation.Status.UNCERTAIN, "No frame-eye acknowledgement; reobserve before another insert");
               }

               if (!this.eyePlacement.init()) {
                  if (this.init_BlockHitResult == null) {
                     this.init_BlockHitResult = AiEndPortal.frameHit(mc, mc.player.getEyePosition(), frame, mc.player.blockInteractionRange() - 0.1);
                  }

                  if (this.init_BlockHitResult == null) {
                     Vec3 middle = Vec3.atLowerCornerOf(frame).add(0.5, 0.81, 0.5);
                     if (mc.player.getEyePosition().distanceToSqr(middle) > mc.player.blockInteractionRange() * mc.player.blockInteractionRange()) {
                        return this.init(AiOperation.Status.FAILED, "Frame is outside actual block interaction range");
                     }

                     return this.init(AiOperation.Status.FAILED, "Frame insertion ray is obstructed");
                  }

                  this.aim.aimAt(this.init_BlockHitResult.getLocation());
                  this.aim.tick();
               }

               if (!this.eyePlacement.init() && this.aim.isSettled()) {
                  if (!mc.player.getMainHandItem().is(Items.ENDER_EYE)) {
                     return this.init(AiOperation.Status.FAILED, "No Eye held for insertion");
                  }

                  BlockHitResult ray = autismclient.ai.engine.AiAim.raycastLookBlock();
                  if (ray == null || !ray.getBlockPos().equals(frame)) {
                     this.init_BlockHitResult = null;
                     return ++this.load >= 4
                        ? this.init(AiOperation.Status.FAILED, "Frame insertion ray is obstructed")
                        : AiGameOperations.result(AiOperation.Status.RUNNING, "Aiming at another face of the frame", new JsonObject());
                  }

                  this.eyePlacement.init(AiCraft.countItem("minecraft:ender_eye"), AiServerEvidence.blockRevision(frame), AiServerEvidence.inventoryRevision());
                  mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, ray);
               }

               return AiGameOperations.result(
                  AiOperation.Status.RUNNING, this.eyePlacement.init() ? "Waiting for frame eye" : "Aiming at frame", new JsonObject()
               );
            } else {
               return this.init(AiOperation.Status.CANCELLED, "World changed during frame insertion");
            }
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            this.aim.clear();
            return AiGuardLayer.wrapPendingTermination(
               this.eyePlacement.load(),
               AiGameOperations.result(
                  status,
                  message,
                  AiJson.objectOf("frame", frame.toShortString(), "sent", this.eyePlacement.init(), "acknowledged", !this.eyePlacement.load())
               )
            );
         }

         @Override
         public boolean canInterruptNow() {
            return !this.eyePlacement.load();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.eyePlacement.load();
         }

         @Override
         public void cancel() {
            this.aim.clear();
         }
      };
   }

   static final class Inner_DataUtil {
      private int eyesBefore;
      private long blockRevision;
      private long inventoryRevision;
      private boolean sent;
      private boolean confirmed;

      void init(int eyes, long blockRevision, long inventoryRevision) {
         if (this.sent) {
            throw new IllegalStateException("A frame insertion was already sent");
         }

         this.eyesBefore = eyes;
         this.blockRevision = blockRevision;
         this.inventoryRevision = inventoryRevision;
         this.sent = true;
      }

      boolean init(boolean filled, int eyes, long blockRevision, long inventoryRevision) {
         if (filled && (!this.sent || this.eyesBefore - eyes == 1 && blockRevision > this.blockRevision && inventoryRevision > this.inventoryRevision)) {
            this.confirmed = true;
         }

         return this.confirmed;
      }

      boolean init() {
         return this.sent;
      }

      boolean load() {
         return this.sent && !this.confirmed;
      }
   }

   record Inner_StreamUtil(BlockPos center, List<BlockPos> frames, List<BlockPos> empty, boolean active) {
   }
}
