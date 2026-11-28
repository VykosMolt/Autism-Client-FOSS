package autismclient.modules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

final class TrajectoryStateCache {
   private static final FireballBlastPredictor BLAST_PREDICTOR = new FireballBlastPredictor();
   private static final ThrownPearlTracker PEARL_TRACKER = new ThrownPearlTracker();
   private static List<TrajectoriesModule.Inner_CachePool> cachedPaths = List.of();
   private static List<FireballBlastPredictor.DestroyedBlock> blastPredictions = List.of();
   private static Level cachedLevel;
   private static Object cachedPlayer;
   private static final Map<Vec3, Integer> damageByPoint = new HashMap<>();
   private static int cachedTick = -2147483648;
   private static double cachedThreshold = 0.0 / 0.0;

   private TrajectoryStateCache() {
   }

   static void init(Minecraft mc, List<TrajectoriesModule.Inner_CachePool> paths) {
      if (ensureContext(mc)) {
         cachedPaths = paths;
         Vec3 explosion = null;

         for (TrajectoriesModule.Inner_CachePool path : paths) {
            if (path.init_aS_StreamUtil() == TrajectoriesModule.Inner_StreamUtil.FIRE && path.hit() != null) {
               explosion = path.hit().getLocation();
            }
         }

         blastPredictions = BLAST_PREDICTOR.predictAt(cachedLevel, explosion);
         Module module = al.init_Module_Str("trajectories");
         double threshold = module == null ? 50.0 : module.integer("pearl-warning-damage");
         if (cachedTick != mc.player.tickCount || cachedThreshold != threshold) {
            damageByPoint.clear();
            cachedTick = mc.player.tickCount;
            cachedThreshold = threshold;
         }

         if (damageByPoint.size() > 4) {
            damageByPoint.clear();
         }

         ThrownPearlTracker.Inner_DataUtil remembered = PEARL_TRACKER.remembered();
         if (remembered != null) {
            evaluateLanding(mc, remembered.position(), threshold);
         }

         for (TrajectoriesModule.Inner_CachePool path : paths) {
            if (path.load_Vec3() != null) {
               evaluateLanding(mc, path.load_Vec3(), threshold);
            }
         }

         if (paths.isEmpty() && PEARL_TRACKER.remembered() == null) {
            TrajectoryWorldRenderer.resetCounters();
         }
      }
   }

   static List<FireballBlastPredictor.DestroyedBlock> blastPredictions(List<TrajectoriesModule.Inner_CachePool> paths) {
      return cachedPaths == paths ? blastPredictions : List.of();
   }

   static int tintFor(Vec3 destination) {
      return damageByPoint.getOrDefault(destination, -12971);
   }

   private static void evaluateLanding(Minecraft mc, Vec3 destination, double threshold) {
      damageByPoint.computeIfAbsent(destination, at -> PearlLandingEvaluator.init(mc, at, threshold));
   }

   static double init_D() {
      return PEARL_TRACKER.init_D();
   }

   static ThrownPearlTracker.Inner_DataUtil rememberedPearl() {
      Minecraft mc = Minecraft.getInstance();
      return !am.init_Z() && mc.level == cachedLevel && mc.player == cachedPlayer && mc.player != null && mc.player.isAlive() ? PEARL_TRACKER.remembered() : null;
   }

   static void tickPearls(Minecraft mc, boolean enabled) {
      if (ensureContext(mc)) {
         PEARL_TRACKER.tick(mc, enabled);
      }
   }

   private static boolean ensureContext(Minecraft mc) {
      if (mc != null && mc.level != null && mc.player != null && mc.player.isAlive() && !mc.player.isSpectator()) {
         if (mc.level != cachedLevel || mc.player != cachedPlayer) {
            reset();
         }

         cachedLevel = mc.level;
         cachedPlayer = mc.player;
         return true;
      } else {
         reset();
         return false;
      }
   }

   static void reset() {
      BLAST_PREDICTOR.reset();
      PEARL_TRACKER.reset();
      cachedPaths = List.of();
      blastPredictions = List.of();
      cachedLevel = null;
      cachedPlayer = null;
      damageByPoint.clear();
      cachedTick = -2147483648;
      cachedThreshold = 0.0 / 0.0;
      TrajectoryWorldRenderer.resetCounters();
      TrajectoryOffscreenIndicator.reset();
   }
}
