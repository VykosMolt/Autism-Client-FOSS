package autismclient.util;

import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public final class SilentMiningCoordinator {
   private static String owner;
   private static SilentMiningCoordinator.PendingStop pendingStop;

   private SilentMiningCoordinator() {
   }

   public static boolean init(String requester, BlockHitResult ray) {
      if (requester != null && ClientInteractionOverride.queueHoldAttack(ray)) {
         owner = requester;
         return true;
      } else {
         return false;
      }
   }

   public static boolean init(String requester, BlockHitResult ray, InteractionHand hand) {
      if (requester != null && ClientInteractionOverride.queueHoldUse(ray, hand)) {
         owner = requester;
         return true;
      } else {
         return false;
      }
   }

   public static boolean isOwner(String requester) {
      return requester != null && requester.equals(owner);
   }

   public static void release(String requester) {
      if (isOwner(requester)) {
         ClientInteractionOverride.releaseHoldOverrides();
      }
   }

   static void init() {
      owner = null;
   }

   public static boolean init(String requester, RotationMath.Rotation current, RotationMath.Rotation target) {
      return "bed-nuker".equals(requester) && current != null && target != null && RotationMath.angularDistance(current, target) <= 0.05F;
   }

   public static boolean init(String requester, RotationMath.Rotation current, RotationMath.Rotation target, RotationStreamPlanner.StreamState stream) {
      if (!init(requester, current, target)) {
         return false;
      }

      RotationStreamPlanner.resume(stream, current, 0.0, 0.0, RotationMath.gcdStep());
      return true;
   }

   public static void init(BlockPos block) {
      Minecraft mc = Minecraft.getInstance();
      if (block != null
         && mc.player != null
         && mc.gameMode instanceof AutismMultiPlayerGameModeAccessor mining
         && mining.autism$isDestroying()
         && block.equals(mining.autism$getDestroyBlockPos())) {
         mc.gameMode.stopDestroyBlock();
      }
   }

   public static void load(BlockPos block) {
      Minecraft mc = Minecraft.getInstance();
      if (block != null && mc.player != null && mc.gameMode != null) {
         pendingStop = new SilentMiningCoordinator.PendingStop(mc.player, mc.level, mc.gameMode, block.immutable());
      }
   }

   public static void load() {
      SilentMiningCoordinator.PendingStop stop = pendingStop;
      pendingStop = null;
      Minecraft mc = Minecraft.getInstance();
      if (stop != null && mc.player == stop.player && mc.level == stop.load && mc.gameMode == stop.read) {
         init(stop.pos);
      }
   }

   private record PendingStop(Object player, Object load, Object read, BlockPos pos) {
   }
}
