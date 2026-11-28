package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class AiNetherStructures {
   private static AiNetherStructures.Inner_DataUtil bastionAt;
   private static WeakReference<Object> trackedLevel = new WeakReference<>(null);
   private static long lastScanTick;
   static final long init_J = 120000000000L;
   private static final AiNetherStructures.Inner_StreamUtil skippedTargets = new AiNetherStructures.Inner_StreamUtil();
   private static String worldKey = "";
   private static Object connection;

   private AiNetherStructures() {
   }

   private static void resetIfWorldChanged(Minecraft mc) {
      String world = AiWorldIdentity.currentWorldKey(mc);
      if (!world.equals(worldKey) || mc.getConnection() != connection) {
         skippedTargets.clear();
         worldKey = world;
         connection = mc.getConnection();
      }
   }

   static void recordBarter(Minecraft mc, Piglin partner) {
      resetIfWorldChanged(mc);
      skippedTargets.skip(partner.getUUID(), System.nanoTime());
   }

   static boolean canBarter(Minecraft mc, Piglin partner) {
      if (mc.player != null && mc.level != null && partner.isAlive() && partner.isAdult() && !partner.isConverting() && !partner.isAggressive()) {
         resetIfWorldChanged(mc);
         double reach = Math.min(3.0, mc.player.entityInteractionRange());
         boolean reachable = mc.player.getEyePosition().distanceToSqr(partner.getBoundingBox().getCenter()) <= reach * reach
            && mc.player.hasLineOfSight(partner);
         if (!skippedTargets.isAllowed(partner.getUUID(), reachable, System.nanoTime())) {
            return false;
         }

         double bruteDistance = 1.0 / 0.0;
         double heavyBodyDistance = 1.0 / 0.0;
         double heavyPartnerDistance = 1.0 / 0.0;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity.isAlive()) {
               if (entity instanceof PiglinBrute) {
                  bruteDistance = Math.min(bruteDistance, entity.distanceToSqr(mc.player));
               }

               if (entity instanceof Hoglin || entity instanceof Zoglin) {
                  heavyBodyDistance = Math.min(heavyBodyDistance, entity.distanceToSqr(mc.player));
                  heavyPartnerDistance = Math.min(heavyPartnerDistance, entity.distanceToSqr(partner));
               }
            }
         }

         return !farFromThreats(bruteDistance, heavyBodyDistance, heavyPartnerDistance) ? false : !init_Z(partner.blockPosition());
      } else {
         return false;
      }
   }

   static boolean farFromThreats(double bruteSquared, double heavyBodySquared, double heavyPartnerSquared) {
      return bruteSquared > 1600.0 && heavyBodySquared > 576.0 && heavyPartnerSquared >= 256.0;
   }

   static boolean init_Z(BlockPos at) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return false;
      }

      for (BlockPos p : BlockPos.betweenClosed(at.offset(-12, -8, -12), at.offset(12, 8, 12))) {
         if (AiTerrain.isLoaded(mc.level, p)) {
            BlockState state = mc.level.getBlockState(p);
            if (state.is(Blocks.GILDED_BLACKSTONE)
               || state.is(Blocks.POLISHED_BLACKSTONE_BRICKS)
               || state.is(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)
               || state.is(Blocks.CHISELED_POLISHED_BLACKSTONE)) {
               return bastionAt(at).confidence() >= 0.5;
            }
         }
      }

      return false;
   }

   static AiNetherStructures.Inner_DataUtil bastionAt(BlockPos at) {
      Minecraft mc = Minecraft.getInstance();
      if (bastionAt != null
         && trackedLevel.get() == mc.level
         && bastionAt.at.distSqr(at) < 64.0
         && AiThreats.currentClientTick() - lastScanTick >= 0L
         && AiThreats.currentClientTick() - lastScanTick < 100L
         && bastionAt.revision == autismclient.ai.engine.AiGridRevisions.globalRevision()) {
         return bastionAt;
      }

      int bricks = 0;
      int gold = 0;
      int chests = 0;
      int lava = 0;
      int corridors = 0;
      boolean partial = false;
      Set<Integer> floors = new HashSet<>();

      for (BlockPos p : BlockPos.betweenClosed(at.offset(-12, -8, -12), at.offset(12, 8, 12))) {
         if (!AiTerrain.isLoaded(mc.level, p)) {
            partial = true;
         } else {
            BlockState state = mc.level.getBlockState(p);
            if (AiNetherRun.BASTION_BLOCK.test(state)) {
               bricks++;
               if (mc.level.getBlockState(p.above()).isAir()) {
                  floors.add(p.getY());
               }
            }

            if (state.is(Blocks.GOLD_BLOCK)) {
               gold++;
            }

            if (state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST)) {
               chests++;
            }

            if (state.getFluidState().is(FluidTags.LAVA)) {
               lava++;
            }
         }
      }

      for (Direction axis : Plane.HORIZONTAL) {
         int floor = 0;

         for (int distance = 1; distance <= 10; distance++) {
            BlockPos p = at.relative(axis, distance);
            if (AiTerrain.isLoaded(mc.level, p)
               && AiNetherRun.BASTION_BLOCK.test(mc.level.getBlockState(p.below()))
               && mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()) {
               floor++;
            }
         }

         if (floor >= 8) {
            corridors++;
         }
      }

      int brutes = (int)AiWorldScan.findEntitiesByType("minecraft:piglin_brute", 64.0).stream().filter(e -> e.blockPosition().distSqr(at) < 784.0).count();
      int hoglins = (int)AiWorldScan.findEntitiesByType("minecraft:hoglin", 64.0).stream().filter(e -> e.blockPosition().distSqr(at) < 784.0).count();
      double confidence = bricks < 32 || gold <= 0 && chests <= 0 && brutes <= 0 ? (bricks >= 32 ? 0.35 : 0.0) : 0.8;
      JsonArray variants = new JsonArray();
      if (confidence >= 0.5) {
         if (gold >= 3 && lava >= 12) {
            variants.add(AiJson.objectOf("variant", "treasure_room", "confidence", 0.65));
         }

         if (hoglins > 0 && floors.size() >= 3) {
            variants.add(AiJson.objectOf("variant", "hoglin_stables", "confidence", 0.55));
         }

         if (corridors > 0) {
            variants.add(AiJson.objectOf("variant", "bridge", "confidence", 0.55));
         }

         if (floors.size() >= 3 && chests > 0) {
            variants.add(AiJson.objectOf("variant", "housing_units", "confidence", 0.5));
         }
      }

      lastScanTick = AiThreats.currentClientTick();
      bastionAt = new AiNetherStructures.Inner_DataUtil(
         at.immutable(),
         bricks,
         gold,
         chests,
         lava,
         brutes,
         hoglins,
         floors.size(),
         corridors,
         partial,
         confidence,
         variants,
         AiWorldIdentity.currentWorldKey(mc),
         AiWorldIdentity.currentDimensionId(mc),
         lastScanTick,
         autismclient.ai.engine.AiGridRevisions.globalRevision()
      );
      trackedLevel = new WeakReference<>(mc.level);
      return bastionAt;
   }

   static double structureCost(AiNetherStructures.Inner_DataUtil e, double distance) {
      return distance / 0.22 + e.brutes * 1200 + e.hoglins * 400 + Math.min(800, e.lava * 3) + (e.partial ? 300 : 0) + (1.0 - e.confidence) * 200.0;
   }

   record Inner_DataUtil(
      BlockPos at,
      int bricks,
      int gold,
      int chests,
      int lava,
      int brutes,
      int hoglins,
      int levels,
      int corridors,
      boolean partial,
      double confidence,
      JsonArray variants,
      String world,
      String dimension,
      long observedTick,
      long revision
   ) {
      JsonObject data() {
         return AiJson.objectOf(
            "at",
            this.at.toShortString(),
            "bricks",
            this.bricks,
            "goldBlocks",
            this.gold,
            "containers",
            this.chests,
            "lavaCells",
            this.lava,
            "brutes",
            this.brutes,
            "hoglins",
            this.hoglins,
            "platformLevels",
            this.levels,
            "corridorAxes",
            this.corridors,
            "partialCoverage",
            this.partial,
            "structureConfidence",
            this.confidence,
            "possibleVariants",
            this.variants,
            "world",
            this.world,
            "dimension",
            this.dimension,
            "observedTick",
            this.observedTick,
            "revision",
            this.revision,
            "source",
            "client_loaded_geometry_and_entities",
            "knowledge",
            "SUSPECTED",
            "verifyBeforeAction",
            true,
            "estimatedAccessTicks",
            Minecraft.getInstance().player == null ? -1.0 : AiNetherStructures.structureCost(this, Math.sqrt(this.at.distSqr(Minecraft.getInstance().player.blockPosition()))),
            "costIsEstimate",
            true
         );
      }
   }

   static final class Inner_StreamUtil {
      private final Map<UUID, Long> skipUntil = new LinkedHashMap<>();

      void skip(UUID identity, long now) {
         this.skipUntil.remove(identity);
         this.skipUntil.put(identity, now + 120000000000L);
         if (this.skipUntil.size() > 128) {
            this.skipUntil.remove(this.skipUntil.keySet().iterator().next());
         }
      }

      boolean isAllowed(UUID identity, boolean actuallyReachable, long now) {
         Long until = this.skipUntil.get(identity);
         if (until == null) {
            return true;
         }

         if (!actuallyReachable && now < until) {
            return false;
         }

         this.skipUntil.remove(identity);
         return true;
      }

      void clear() {
         this.skipUntil.clear();
      }
   }
}
