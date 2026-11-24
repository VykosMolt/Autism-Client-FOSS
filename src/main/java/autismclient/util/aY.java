package autismclient.util;

import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public final class aY {
   private static String init_Str;
   private static aY.Inner_DataUtil init_aY_DataUtil;

   private aY() {
   }

   public static boolean init(String requester, BlockHitResult ray) {
      if (requester != null && s.load(ray)) {
         init_Str = requester;
         return true;
      } else {
         return false;
      }
   }

   public static boolean init(String requester, BlockHitResult ray, InteractionHand hand) {
      if (requester != null && s.load(ray, hand)) {
         init_Str = requester;
         return true;
      } else {
         return false;
      }
   }

   public static boolean init_Z(String requester) {
      return requester != null && requester.equals(init_Str);
   }

   public static void init_V(String requester) {
      if (init_Z(requester)) {
         s.read_V();
      }
   }

   static void init() {
      init_Str = null;
   }

   public static boolean init(String requester, ce.Inner_DataUtil current, ce.Inner_DataUtil target) {
      return "bed-nuker".equals(requester) && current != null && target != null && ce.init_F(current, target) <= 0.05F;
   }

   public static boolean init(String requester, ce.Inner_DataUtil current, ce.Inner_DataUtil target, ap.Inner_TaskQueue stream) {
      if (!init(requester, current, target)) {
         return false;
      }

      ap.init_V_ap_TaskQueue_3(stream, current, 0.0, 0.0, ce.init());
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
         init_aY_DataUtil = new aY.Inner_DataUtil(mc.player, mc.level, mc.gameMode, block.immutable());
      }
   }

   public static void load() {
      aY.Inner_DataUtil stop = init_aY_DataUtil;
      init_aY_DataUtil = null;
      Minecraft mc = Minecraft.getInstance();
      if (stop != null && mc.player == stop.init_Obj && mc.level == stop.load && mc.gameMode == stop.read) {
         init(stop.init_BlockPos);
      }
   }

   private record Inner_DataUtil(Object init_Obj, Object load, Object read, BlockPos init_BlockPos) {
   }
}
