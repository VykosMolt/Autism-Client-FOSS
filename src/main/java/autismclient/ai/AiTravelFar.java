package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

final class AiTravelFar {
   private static final double REGION_PLAN_MIN_DIST_SQ = 4096.0;
   private static final double load = 40.0;
   private static final double read = 1936.0;

   private AiTravelFar() {
   }

   static AiOperation travel(BlockPos target, double radius, boolean groundTarget, boolean allowDig, boolean allowPillar, boolean allowBridge, int timeoutSeconds) {
      return travelWithStrategy(target, radius, groundTarget, allowDig, allowPillar, allowBridge, timeoutSeconds, autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST);
   }

   static AiOperation travelWithStrategy(
      final BlockPos target,
      double radius,
      final boolean groundTarget,
      final boolean allowDig,
      final boolean allowPillar,
      final boolean allowBridge,
      int timeoutSeconds,
      final autismclient.ai.engine.AiPathfinder.RouteStrategy strategy
   ) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final long deadline = System.nanoTime() + Math.max(1, Math.min(3600, timeoutSeconds)) * 1000000000L;
         final double arrivalRadius = Math.max(0.6, Math.min(8.0, radius));
         return new AiOperation() {
            final Minecraft client = Minecraft.getInstance();
            final Deque<BlockPos> legQueue = new ArrayDeque<>();
            CompletableFuture<List<AiRegions.Region>> regionSearch;
            AiOperation legOperation;
            boolean legTargetsHead;
            boolean regionPlanStarted;
            boolean targetQueued;
            boolean cancelled;

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && this.client.player != null && this.client.level != null) {
                  if (System.nanoTime() > deadline) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Travel deadline reached");
                  }

                  if (!this.regionPlanStarted) {
                     this.regionPlanStarted = true;
                     if (this.client.player.blockPosition().distSqr(target) > 4096.0) {
                        AiService service = AiService.getExistingInstance();
                        if (service != null) {
                           List<AiRegions.Region> observations = service.getDimensionRegions();
                           int x = this.client.player.blockPosition().getX();
                           int z = this.client.player.blockPosition().getZ();
                           if (observations.size() >= 2) {
                              this.regionSearch = CompletableFuture.supplyAsync(
                                 () -> AiRegions.init(observations, x, z, target.getX(), target.getZ())
                              );
                           }
                        }
                     }
                  }

                  if (this.regionSearch != null) {
                     if (!this.regionSearch.isDone()) {
                        return this.running("Planning with observed region connections");
                     }

                     try {
                        for (AiRegions.Region region : this.regionSearch.join()) {
                           this.legQueue.addLast(new BlockPos(region.x(), region.y(), region.z()));
                        }
                     } catch (Exception var7) {
                     }

                     this.regionSearch = null;
                  }

                  if (!this.targetQueued) {
                     this.targetQueued = true;
                     this.legQueue.addLast(target);
                  }

                  for (int chained = 0; chained < 4; chained++) {
                     if (this.legOperation != null) {
                        AiOperation.Result r = this.legOperation.tick();
                        if (!r.terminal()) {
                           return r;
                        }

                        this.legOperation.cancel();
                        this.legOperation = null;
                        if (r.status() != AiOperation.Status.SUCCEEDED) {
                           this.cancel();
                           return new AiOperation.Result(r.status(), r.message(), r.dataJson(), r.evidence(), r.failure());
                        }

                        if (this.legTargetsHead) {
                           this.legQueue.pollFirst();
                           if (this.legQueue.isEmpty()) {
                              return this.finish(AiOperation.Status.SUCCEEDED, "Arrived at target");
                           }
                        }
                     }

                     BlockPos head = this.legQueue.peekFirst();
                     boolean finalTarget = this.legQueue.size() == 1;
                     BlockPos legTarget = head;
                     double legRadius = arrivalRadius;
                     this.legTargetsHead = true;
                     if (this.horizontalDistSqr(head) > 1936.0) {
                        legTarget = this.intermediateLegTarget(head);
                        legRadius = Math.max(arrivalRadius, 4.0);
                        this.legTargetsHead = false;
                     }

                     if (!finalTarget || groundTarget) {
                        legTarget = AiGoto.findStandableNearCompat(legTarget, 24);
                     }

                     this.legOperation = AiGoto.travelToCompat(legTarget, legRadius, allowDig, allowPillar, allowBridge, this.secondsRemaining(), strategy);
                  }

                  return this.running("Continuing the next route leg");
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Travel cancelled");
               }
            }

            BlockPos intermediateLegTarget(BlockPos head) {
               Vec3 pos = this.client.player.position();
               double dx = head.getX() + 0.5 - pos.x;
               double dz = head.getZ() + 0.5 - pos.z;
               double horizontal = Math.hypot(dx, dz);
               if (horizontal < 1.0E-6) {
                  return head;
               }

               double scale = 40.0 / horizontal;
               return BlockPos.containing(pos.x + dx * scale, this.client.player.getY(), pos.z + dz * scale);
            }

            double horizontalDistSqr(BlockPos p) {
               Vec3 pos = this.client.player.position();
               double dx = p.getX() + 0.5 - pos.x;
               double dz = p.getZ() + 0.5 - pos.z;
               return dx * dx + dz * dz;
            }

            int secondsRemaining() {
               return (int)Math.max(1L, (deadline - System.nanoTime()) / 1000000000L);
            }

            AiOperation.Result running(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, "{}", List.of());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               this.cancel();
               return new AiOperation.Result(status, message, "{}", List.of());
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  if (this.legOperation != null) {
                     this.legOperation.cancel();
                     this.legOperation = null;
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Engine travel requires a connected living player");
      }
   }
}
