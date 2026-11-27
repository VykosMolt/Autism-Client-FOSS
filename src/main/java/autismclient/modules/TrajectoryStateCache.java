package autismclient.modules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

final class TrajectoryStateCache {
   private static final FireballBlastPredictor init_x = new FireballBlastPredictor();
   private static final ThrownPearlTracker init_aV = new ThrownPearlTracker();
   private static List<TrajectoriesModule.Inner_CachePool> init_List = List.of();
   private static List<FireballBlastPredictor.Inner_DataUtil> load = List.of();
   private static Level init_Level;
   private static Object init_Obj;
   private static final Map<Vec3, Integer> init_Map = new HashMap<>();
   private static int init_I = -2147483648;
   private static double init_D = 0.0 / 0.0;

   private TrajectoryStateCache() {
   }

   static void init(Minecraft mc, List<TrajectoriesModule.Inner_CachePool> paths) {
      if (init(mc)) {
         init_List = paths;
         Vec3 explosion = null;

         for (TrajectoriesModule.Inner_CachePool path : paths) {
            if (path.init_aS_StreamUtil() == TrajectoriesModule.Inner_StreamUtil.FIRE && path.hit() != null) {
               explosion = path.hit().getLocation();
            }
         }

         load = init_x.predictAt(init_Level, explosion);
         Module module = al.init_Module_Str("trajectories");
         double threshold = module == null ? 50.0 : module.integer("pearl-warning-damage");
         if (init_I != mc.player.tickCount || init_D != threshold) {
            init_Map.clear();
            init_I = mc.player.tickCount;
            init_D = threshold;
         }

         if (init_Map.size() > 4) {
            init_Map.clear();
         }

         ThrownPearlTracker.Inner_DataUtil remembered = init_aV.remembered();
         if (remembered != null) {
            init(mc, remembered.position(), threshold);
         }

         for (TrajectoriesModule.Inner_CachePool path : paths) {
            if (path.load_Vec3() != null) {
               init(mc, path.load_Vec3(), threshold);
            }
         }

         if (paths.isEmpty() && init_aV.remembered() == null) {
            TrajectoryWorldRenderer.resetCounters();
         }
      }
   }

   static List<FireballBlastPredictor.Inner_DataUtil> init(List<TrajectoriesModule.Inner_CachePool> paths) {
      return init_List == paths ? load : List.of();
   }

   static int init(Vec3 destination) {
      return init_Map.getOrDefault(destination, -12971);
   }

   private static void init(Minecraft mc, Vec3 destination, double threshold) {
      init_Map.computeIfAbsent(destination, at -> PearlLandingEvaluator.init(mc, at, threshold));
   }

   static double init_D() {
      return init_aV.init_D();
   }

   static ThrownPearlTracker.Inner_DataUtil rememberedPearl() {
      Minecraft mc = Minecraft.getInstance();
      return !am.init_Z() && mc.level == init_Level && mc.player == init_Obj && mc.player != null && mc.player.isAlive() ? init_aV.remembered() : null;
   }

   static void init(Minecraft mc, boolean enabled) {
      if (init(mc)) {
         init_aV.tick(mc, enabled);
      }
   }

   private static boolean init(Minecraft mc) {
      if (mc != null && mc.level != null && mc.player != null && mc.player.isAlive() && !mc.player.isSpectator()) {
         if (mc.level != init_Level || mc.player != init_Obj) {
            reset();
         }

         init_Level = mc.level;
         init_Obj = mc.player;
         return true;
      } else {
         reset();
         return false;
      }
   }

   static void reset() {
      init_x.reset();
      init_aV.reset();
      init_List = List.of();
      load = List.of();
      init_Level = null;
      init_Obj = null;
      init_Map.clear();
      init_I = -2147483648;
      init_D = 0.0 / 0.0;
      TrajectoryWorldRenderer.resetCounters();
      TrajectoryOffscreenIndicator.reset();
   }
}
