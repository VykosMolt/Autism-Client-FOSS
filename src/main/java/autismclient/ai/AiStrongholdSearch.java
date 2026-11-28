package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

final class AiStrongholdSearch {
   private AiStrongholdSearch() {
   }

   private static boolean isStrongholdBlock(BlockState state) {
      return state.is(Blocks.STONE_BRICKS)
         || state.is(Blocks.MOSSY_STONE_BRICKS)
         || state.is(Blocks.CRACKED_STONE_BRICKS)
         || state.is(Blocks.INFESTED_STONE_BRICKS)
         || state.is(Blocks.INFESTED_MOSSY_STONE_BRICKS)
         || state.is(Blocks.INFESTED_CRACKED_STONE_BRICKS)
         || state.is(Blocks.STONE_BRICK_STAIRS);
   }

   private static long regionKey(BlockPos at) {
      return new BlockPos(Math.floorDiv(at.getX(), 6), Math.floorDiv(at.getY(), 4), Math.floorDiv(at.getZ(), 6)).asLong();
   }

   static AiOperation searchFresh(Vec3 lead, int seconds) {
      return search(lead, seconds, new AiStrongholdSearch.SearchMemory());
   }

   static AiOperation search(final Vec3 lead, final int seconds, final AiStrongholdSearch.SearchMemory memory) {
      final Minecraft mc = Minecraft.getInstance();
      memory.resetIfChanged(mc.level, mc.getConnection(), lead);
      return new AiOperation() {
         final Object startLevel = mc.level;
         final Object startConnection = mc.getConnection();
         final long deadline = System.nanoTime() + Math.max(30, Math.min(360, seconds)) * 1000000000L;
         final Set<Long> visitedRegions;
         final Set<Long> deadEnds;
         final JsonArray routeGraph;
         AiOperation travelOp;
         BlockPos frontierTarget;
         BlockPos legStart;
         int steps;
         boolean cancelled;

         {
            this.visitedRegions = memory.visitedRegions;
            this.deadEnds = memory.deadEnds;
            this.routeGraph = memory.routeGraph;
         }

         @Override
         public AiOperation.Result tick() {
            if (this.cancelled || mc.player == null || mc.level != this.startLevel || this.startConnection != mc.getConnection()) {
               return this.finish(AiOperation.Status.CANCELLED, "Local stronghold world changed");
            }

            if (!mc.player.isAlive()) {
               return this.finish(AiOperation.Status.FAILED, "Player died in the stronghold search");
            }

            BlockPos frame = AiWorldScan.findNearestBlock(s -> s.is(Blocks.END_PORTAL_FRAME), 12);
            if (frame != null) {
               return this.finish(AiOperation.Status.SUCCEEDED, "Portal-frame evidence loaded at " + frame.toShortString());
            }

            if (System.nanoTime() <= this.deadline && this.steps < 24) {
               if (this.travelOp != null) {
                  AiOperation.Result result = this.travelOp.tick();
                  if (!result.terminal()) {
                     return AiGameOperations.result(
                        AiOperation.Status.RUNNING, "Exploring supported structure terrain: " + result.message(), this.progressData()
                     );
                  }

                  boolean pending = this.travelOp.requiresReconciliationAfterEmergency();
                  this.travelOp.cancel();
                  this.travelOp = null;
                  this.routeGraph
                     .add(
                        AiJson.objectOf(
                           "from",
                           this.legStart.toShortString(),
                           "toward",
                           this.frontierTarget.toShortString(),
                           "actualArrival",
                           mc.player.blockPosition().toShortString(),
                           "status",
                           result.status().name(),
                           "evidence",
                           AiRunResources.compactEvidenceJson(result.dataJson())
                        )
                     );
                  if (result.status() == AiOperation.Status.UNCERTAIN || pending) {
                     return this.finish(AiOperation.Status.UNCERTAIN, "Structure route needs reconciliation: " + result.message());
                  }

                  if (result.status() == AiOperation.Status.CANCELLED) {
                     return this.finish(AiOperation.Status.CANCELLED, "Structure approach cancelled");
                  }

                  if (result.status() != AiOperation.Status.SUCCEEDED) {
                     this.deadEnds.add(AiStrongholdSearch.regionKey(this.frontierTarget));
                  } else if (!memory.reachedStronghold) {
                     memory.reachedStronghold = this.nearStrongholdBlock(mc.player.blockPosition());
                  }

                  this.visitedRegions.add(AiStrongholdSearch.regionKey(this.frontierTarget));
                  this.visitedRegions.add(AiStrongholdSearch.regionKey(mc.player.blockPosition()));
               }

               BlockPos feet = mc.player.blockPosition();
               if (!memory.reachedStronghold) {
                  this.frontierTarget = this.observedFrontier();
                  if (this.frontierTarget == null) {
                     return this.running("No untried loaded masonry supports a standing approach at this Eye estimate");
                  }
               } else {
                  this.frontierTarget = this.exploreTarget(feet);
                  if (this.frontierTarget == null) {
                     return this.running("Observed local corridors exhausted; unloaded rooms remain unknown");
                  }
               }

               this.legStart = feet;
               this.steps++;
               this.travelOp = AiGoto.travelToCompat(
                  this.frontierTarget,
                  1.2,
                  true,
                  true,
                  false,
                  Math.min(90, Math.max(15, (int)((this.deadline - System.nanoTime()) / 1000000000L))),
                  autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                  true,
                  2.0,
                  null,
                  AiGoto.GotoOptions.DEFAULT
               );
               return AiGameOperations.result(
                  AiOperation.Status.RUNNING, "Following observed stronghold frontier " + this.frontierTarget.toShortString(), this.progressData()
               );
            } else {
               return this.running("Local search budget ended; portal room remains unobserved");
            }
         }

         BlockPos observedFrontier() {
            for (BlockPos evidence : AiWorldScan.findBlocks(AiStrongholdSearch::isStrongholdBlock, 8, 16, 256)) {
               if (!(Math.hypot(evidence.getX() - lead.x, evidence.getZ() - lead.z) > 240.0)) {
                  BlockPos candidate = this.nearestStandable(evidence.above());
                  if (candidate != null && !this.visitedRegions.contains(AiStrongholdSearch.regionKey(candidate)) && !this.deadEnds.contains(AiStrongholdSearch.regionKey(candidate))) {
                     return candidate;
                  }
               }
            }

            return null;
         }

         boolean nearStrongholdBlock(BlockPos feet) {
            for (BlockPos p : BlockPos.betweenClosed(feet.offset(-2, -2, -2), feet.offset(2, 1, 2))) {
               if (AiTerrain.isLoaded(mc.level, p) && AiStrongholdSearch.isStrongholdBlock(mc.level.getBlockState(p))) {
                  return true;
               }
            }

            return false;
         }

         BlockPos nearestStandable(BlockPos center) {
            BlockPos best = null;
            double distance = 1.7976931348623157E308;

            for (BlockPos p : BlockPos.betweenClosed(center.offset(-4, -4, -4), center.offset(4, 4, 4))) {
               if (!this.visitedRegions.contains(AiStrongholdSearch.regionKey(p)) && !this.deadEnds.contains(AiStrongholdSearch.regionKey(p)) && this.isStandable(p) && AiCombat.isSafeStep(mc, p)
                  )
                {
                  double d = p.distSqr(center);
                  if (d < distance) {
                     best = p.immutable();
                     distance = d;
                  }
               }
            }

            return best;
         }

         BlockPos exploreTarget(BlockPos feet) {
            BlockPos best = null;
            double score = 1.7976931348623157E308;

            for (BlockPos p : BlockPos.betweenClosed(feet.offset(-24, -12, -24), feet.offset(24, 12, 24))) {
               long key = AiStrongholdSearch.regionKey(p);
               if (!this.visitedRegions.contains(key)
                  && !this.deadEnds.contains(key)
                  && !(p.distSqr(feet) < 16.0)
                  && this.isStandable(p)
                  && AiStrongholdSearch.isStrongholdBlock(mc.level.getBlockState(p.below()))
                  && AiCombat.isSafeStep(mc, p)) {
                  double value = Math.sqrt(p.distSqr(feet)) + Math.abs(p.getY() - feet.getY()) * 2;
                  if (value < score) {
                     best = p.immutable();
                     score = value;
                  }
               }
            }

            return best;
         }

         boolean isStandable(BlockPos at) {
            if (AiTerrain.isLoaded(mc.level, at) && AiTerrain.isLoaded(mc.level, at.above()) && AiTerrain.isLoaded(mc.level, at.below())) {
               for (BlockPos cell : List.of(at, at.above())) {
                  BlockState state = mc.level.getBlockState(cell);
                  if (!state.getCollisionShape(mc.level, cell).isEmpty() || !state.getFluidState().isEmpty() || state.getBlock() instanceof BaseFireBlock) {
                     return false;
                  }
               }

               return !mc.level.getBlockState(at.below()).getCollisionShape(mc.level, at.below()).isEmpty();
            } else {
               return false;
            }
         }

         JsonObject progressData() {
            return AiJson.objectOf(
               "leadIsEstimate",
               true,
               "leadX",
               lead.x,
               "leadZ",
               lead.z,
               "visitedRegions",
               this.visitedRegions.size(),
               "knownDeadEnds",
               this.deadEnds.size(),
               "routeGraph",
               this.routeGraph,
               "steps",
               this.steps
            );
         }

         AiOperation.Result running(String message) {
            AiOperation.Result result = AiStrongholdSearch.runningResult(message, this.progressData(), this.requiresReconciliationAfterEmergency());
            this.cancel();
            return result;
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            AiOperation.Result result = AiGuardLayer.wrapPendingTermination(
               this.requiresReconciliationAfterEmergency(), AiGameOperations.result(status, message, this.progressData())
            );
            this.cancel();
            return result;
         }

         @Override
         public boolean canInterruptNow() {
            return this.travelOp == null || this.travelOp.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.travelOp != null && this.travelOp.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.cancelled = true;
            if (this.travelOp != null) {
               this.travelOp.cancel();
               this.travelOp = null;
            }
         }
      };
   }

   static AiOperation.Result runningResult(String message, JsonObject data, boolean pending) {
      return AiGuardLayer.wrapPendingTermination(pending, AiOperation.Result.failed(AiFailure.TARGET_NOT_FOUND, message, data.toString()));
   }

   static final class SearchMemory {
      private Object world;
      private Object session;
      private Vec3 lead;
      final Set<Long> visitedRegions = new HashSet<>();
      final Set<Long> deadEnds = new HashSet<>();
      final JsonArray routeGraph = new JsonArray();
      boolean reachedStronghold;

      void resetIfChanged(Object world, Object session, Vec3 nextLead) {
         if (this.world != world
            || this.session != session
            || this.lead != null && Math.hypot(this.lead.x - nextLead.x, this.lead.z - nextLead.z) > 240.0) {
            this.visitedRegions.clear();
            this.deadEnds.clear();

            while (!this.routeGraph.isEmpty()) {
               this.routeGraph.remove(this.routeGraph.size() - 1);
            }

            this.reachedStronghold = false;
         }

         this.world = world;
         this.session = session;
         this.lead = nextLead;
      }
   }
}
