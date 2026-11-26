package autismclient.ai.engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AiStandingTarget {
   private static final double init_D = 1.0E-6;
   private static final List<BlockPos> init_List = init();

   private AiStandingTarget() {
   }

   public static BlockHitResult init(Minecraft mc, Vec3 eye, Vec3 direction, double reach, boolean water) {
      return mc.level.clip(new ClipContext(eye, eye.add(direction.scale(reach)), Block.OUTLINE, water ? Fluid.ANY : Fluid.NONE, mc.player));
   }

   public static AiStandingTarget.Inner_StreamUtil init(Minecraft mc, BlockHitResult hit, boolean water) {
      if (hit.getType() == Type.MISS) {
         return null;
      }

      BlockPos clicked = hit.getBlockPos();

      for (int dy : new int[]{0, -1, 1, 2, 3}) {
         AiStandingTarget.Inner_StreamUtil target = init(mc, clicked.offset(0, dy, 0), hit.getLocation(), water, dy != 0);
         if (target != null) {
            return target;
         }
      }

      for (BlockPos offset : init_List) {
         AiStandingTarget.Inner_StreamUtil target = init(mc, clicked.offset(offset), hit.getLocation(), water, true);
         if (target != null) {
            return target;
         }
      }

      return new AiStandingTarget.Inner_StreamUtil(null, null, new AABB(clicked).inflate(0.002), false);
   }

   private static AiStandingTarget.Inner_StreamUtil init(Minecraft mc, BlockPos pos, Vec3 hit, boolean water, boolean adjusted) {
      ClientLevel level = mc.level;
      if (!level.isOutsideBuildHeight(pos) && level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
         BlockState state = level.getBlockState(pos);
         if (AiPathfinder.LiveGridView.read(state)) {
            return null;
         }

         VoxelShape shape = state.getCollisionShape(level, pos);
         if (shape.isEmpty()) {
            FluidState fluid = state.getFluidState();
            if (water && fluid.is(FluidTags.WATER)) {
               Vec3 feet = Vec3.atBottomCenterOf(pos).add(0.0, fluid.getHeight(level, pos), 0.0);
               return init(mc, feet) ? new AiStandingTarget.Inner_StreamUtil(feet, pos.above(), new AABB(pos).inflate(0.002), adjusted) : null;
            } else {
               return null;
            }
         } else {
            AiStandingTarget.Inner_StreamUtil best = null;
            double distance = 1.0 / 0.0;

            for (AABB part : shape.toAabbs()) {
               AABB box = part.move(pos);

               for (Vec3 feet : List.of(
                  new Vec3(
                     Math.clamp(pos.getX() + 0.5, box.minX + 1.0E-6, box.maxX - 1.0E-6),
                     box.maxY,
                     Math.clamp(pos.getZ() + 0.5, box.minZ + 1.0E-6, box.maxZ - 1.0E-6)
                  ),
                  new Vec3(box.getCenter().x, box.maxY, box.getCenter().z),
                  new Vec3(Math.clamp(hit.x, box.minX + 1.0E-6, box.maxX - 1.0E-6), box.maxY, Math.clamp(hit.z, box.minZ + 1.0E-6, box.maxZ - 1.0E-6))
               )) {
                  double score = feet.distanceToSqr(hit);
                  if (!(score >= distance) && init(mc, feet)) {
                     best = new AiStandingTarget.Inner_StreamUtil(feet, pos.above(), box.inflate(0.002), adjusted);
                     distance = score;
                  }
               }
            }

            return best;
         }
      } else {
         return null;
      }
   }

   private static boolean init(Minecraft mc, Vec3 feet) {
      ClientLevel level = mc.level;
      AABB body = mc.player.getDimensions(Pose.STANDING).makeBoundingBox(feet).deflate(1.0E-6);
      if (!level.getWorldBorder().isWithinBounds(body)
         || !level.hasChunksAt(BlockPos.containing(body.minX, body.minY, body.minZ), BlockPos.containing(body.maxX, body.maxY, body.maxZ))) {
         return false;
      }

      if (!level.noCollision(mc.player, body)) {
         return false;
      }

      for (BlockPos cell : BlockPos.betweenClosed(BlockPos.containing(body.minX, body.minY, body.minZ), BlockPos.containing(body.maxX, body.maxY, body.maxZ))) {
         BlockState state = level.getBlockState(cell);
         if (AiPathfinder.LiveGridView.load(state) || state.is(Blocks.POWDER_SNOW)) {
            return false;
         }
      }

      return true;
   }

   private static List<BlockPos> init() {
      List<BlockPos> offsets = new ArrayList<>();

      for (int dy = -2; dy <= 6; dy++) {
         for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
               if (dx != 0 || dz != 0 || dy < -1 || dy > 3) {
                  offsets.add(new BlockPos(dx, dy, dz));
               }
            }
         }
      }

      offsets.sort(Comparator.comparingInt(p -> p.getX() * p.getX() + p.getZ() * p.getZ() + 4 * Math.abs(p.getY())));
      return List.copyOf(offsets);
   }

   public static final class Inner_DataUtil {
      private Object init_Obj;
      private BlockHitResult init_BlockHitResult;
      private AiStandingTarget.Inner_StreamUtil init_j_StreamUtil;
      private long init_J;

      public AiStandingTarget.Inner_StreamUtil init(Minecraft mc, Vec3 eye, Vec3 direction, double reach, boolean water, boolean fresh) {
         BlockHitResult hit = AiStandingTarget.init(mc, eye, direction, reach, water);
         long now = System.nanoTime();
         if (fresh
            || this.init_Obj != mc.level
            || this.init_BlockHitResult == null
            || hit.getType() != this.init_BlockHitResult.getType()
            || !hit.getBlockPos().equals(this.init_BlockHitResult.getBlockPos())
            || hit.getLocation().distanceToSqr(this.init_BlockHitResult.getLocation()) > 0.0225
            || now >= this.init_J) {
            this.init_j_StreamUtil = AiStandingTarget.init(mc, hit, water);
            this.init_Obj = mc.level;
            this.init_BlockHitResult = hit;
            this.init_J = now + 170000000L;
         }

         return this.init_j_StreamUtil;
      }

      public void init() {
         this.init_Obj = null;
         this.init_BlockHitResult = null;
         this.init_j_StreamUtil = null;
      }
   }

   public record Inner_StreamUtil(Vec3 feet, BlockPos stance, AABB box, boolean adjusted) {
      public boolean valid() {
         return this.feet != null;
      }

      public int color() {
         return this.valid() ? (this.adjusted ? -20421 : -12136096) : -50373;
      }
   }
}
