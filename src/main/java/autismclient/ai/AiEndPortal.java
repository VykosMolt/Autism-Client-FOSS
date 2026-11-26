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

   static AiEndPortal.Inner_StreamUtil init_t_StreamUtil(BlockPos portal) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && portal != null) {
         for (BlockPos p : BlockPos.betweenClosed(portal.offset(-3, 0, -3), portal.offset(3, 0, 3))) {
            if (AiTerrain.init(mc.level, p) && mc.level.getBlockState(p).is(Blocks.END_PORTAL_FRAME)) {
               AiEndPortal.Inner_StreamUtil ring = load(p);
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

   static AiEndPortal.Inner_StreamUtil load(BlockPos seen) {
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
                  if (!AiTerrain.init(mc.level, cell)) {
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
                     active &= AiTerrain.init(mc.level, center.offset(x, 0, z)) && mc.level.getBlockState(center.offset(x, 0, z)).is(Blocks.END_PORTAL);
                  }
               }

               empty.sort(Comparator.comparingDouble(p -> p.distSqr(mc.player.blockPosition())));
               return new AiEndPortal.Inner_StreamUtil(center, List.copyOf(frames), List.copyOf(empty), active);
            }
         }
      }

      return null;
   }

   static BlockHitResult init(Minecraft mc, Vec3 eye, BlockPos frame, double reach) {
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

   static AiOperation init_AiOperation(final BlockPos frame) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         final autismclient.ai.a.AiAim init_DataUtil = new autismclient.ai.a.AiAim();
         final Object init_Obj = mc.level;
         final AiEndPortal.Inner_DataUtil init_t_DataUtil = new AiEndPortal.Inner_DataUtil();
         int init_I;
         int load;
         BlockHitResult init_BlockHitResult;

         @Override
         public AiOperation.Result tick() {
            if (mc.player != null && mc.level == this.init_Obj) {
               BlockState state = mc.level.getBlockState(frame);
               if (!state.is(Blocks.END_PORTAL_FRAME)) {
                  return this.init(AiOperation.Status.FAILED, "Portal frame changed");
               }

               if (this.init_t_DataUtil
                  .init(state.getValue(EndPortalFrameBlock.HAS_EYE), AiCraft.init_I("minecraft:ender_eye"), AiServerEvidence.init_J_BlockPos(frame), AiServerEvidence.init_J())) {
                  return this.init(
                     AiOperation.Status.SUCCEEDED, this.init_t_DataUtil.init() ? "Frame eye and one consumed Eye confirmed" : "Frame was already filled"
                  );
               }

               if (state.getValue(EndPortalFrameBlock.HAS_EYE)) {
                  return ++this.init_I > 60
                     ? this.init(AiOperation.Status.UNCERTAIN, "Filled frame observed without the matching one-Eye inventory acknowledgement")
                     : AiGameOperations.init_AiOperation_Result(AiOperation.Status.RUNNING, "Waiting for frame inventory acknowledgement", new JsonObject());
               }

               if (++this.init_I > 60) {
                  return this.init(AiOperation.Status.UNCERTAIN, "No frame-eye acknowledgement; reobserve before another insert");
               }

               if (!this.init_t_DataUtil.init()) {
                  if (this.init_BlockHitResult == null) {
                     this.init_BlockHitResult = AiEndPortal.init(mc, mc.player.getEyePosition(), frame, mc.player.blockInteractionRange() - 0.1);
                  }

                  if (this.init_BlockHitResult == null) {
                     Vec3 middle = Vec3.atLowerCornerOf(frame).add(0.5, 0.81, 0.5);
                     if (mc.player.getEyePosition().distanceToSqr(middle) > mc.player.blockInteractionRange() * mc.player.blockInteractionRange()) {
                        return this.init(AiOperation.Status.FAILED, "Frame is outside actual block interaction range");
                     }

                     return this.init(AiOperation.Status.FAILED, "Frame insertion ray is obstructed");
                  }

                  this.init_DataUtil.init_V_Vec3(this.init_BlockHitResult.getLocation());
                  this.init_DataUtil.load_V();
               }

               if (!this.init_t_DataUtil.init() && this.init_DataUtil.load_Z()) {
                  if (!mc.player.getMainHandItem().is(Items.ENDER_EYE)) {
                     return this.init(AiOperation.Status.FAILED, "No Eye held for insertion");
                  }

                  BlockHitResult ray = autismclient.ai.a.AiAim.init_BlockHitResult();
                  if (ray == null || !ray.getBlockPos().equals(frame)) {
                     this.init_BlockHitResult = null;
                     return ++this.load >= 4
                        ? this.init(AiOperation.Status.FAILED, "Frame insertion ray is obstructed")
                        : AiGameOperations.init_AiOperation_Result(AiOperation.Status.RUNNING, "Aiming at another face of the frame", new JsonObject());
                  }

                  this.init_t_DataUtil.init(AiCraft.init_I("minecraft:ender_eye"), AiServerEvidence.init_J_BlockPos(frame), AiServerEvidence.init_J());
                  mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, ray);
               }

               return AiGameOperations.init_AiOperation_Result(
                  AiOperation.Status.RUNNING, this.init_t_DataUtil.init() ? "Waiting for frame eye" : "Aiming at frame", new JsonObject()
               );
            } else {
               return this.init(AiOperation.Status.CANCELLED, "World changed during frame insertion");
            }
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            this.init_DataUtil.init_V();
            return AiGuardLayer.init_AiOperation_Result_Z_AiOperation_Result(
               this.init_t_DataUtil.load(),
               AiGameOperations.init_AiOperation_Result(
                  status,
                  message,
                  AiJson.init_JsonObject_ObjArr("frame", frame.toShortString(), "sent", this.init_t_DataUtil.init(), "acknowledged", !this.init_t_DataUtil.load())
               )
            );
         }

         @Override
         public boolean canInterruptNow() {
            return !this.init_t_DataUtil.load();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_t_DataUtil.load();
         }

         @Override
         public void cancel() {
            this.init_DataUtil.init_V();
         }
      };
   }

   static final class Inner_DataUtil {
      private int init_I;
      private long init_J;
      private long load_J;
      private boolean init_Z;
      private boolean load_Z;

      void init(int eyes, long blockRevision, long inventoryRevision) {
         if (this.init_Z) {
            throw new IllegalStateException("A frame insertion was already sent");
         }

         this.init_I = eyes;
         this.init_J = blockRevision;
         this.load_J = inventoryRevision;
         this.init_Z = true;
      }

      boolean init(boolean filled, int eyes, long blockRevision, long inventoryRevision) {
         if (filled && (!this.init_Z || this.init_I - eyes == 1 && blockRevision > this.init_J && inventoryRevision > this.load_J)) {
            this.load_Z = true;
         }

         return this.load_Z;
      }

      boolean init() {
         return this.init_Z;
      }

      boolean load() {
         return this.init_Z && !this.load_Z;
      }
   }

   record Inner_StreamUtil(BlockPos center, List<BlockPos> frames, List<BlockPos> empty, boolean active) {
   }
}
