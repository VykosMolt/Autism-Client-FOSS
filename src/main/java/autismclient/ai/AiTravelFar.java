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
   private static final double init = 4096.0;
   private static final double load = 40.0;
   private static final double read = 1936.0;

   private AiTravelFar() {
   }

   static AiOperation init(BlockPos target, double radius, boolean groundTarget, boolean allowDig, boolean allowPillar, boolean allowBridge, int timeoutSeconds) {
      return init(target, radius, groundTarget, allowDig, allowPillar, allowBridge, timeoutSeconds, autismclient.ai.engine.AiPathfinder.Inner_IoWorker.init);
   }

   static AiOperation init(
      final BlockPos target,
      double radius,
      final boolean groundTarget,
      final boolean allowDig,
      final boolean allowPillar,
      final boolean allowBridge,
      int timeoutSeconds,
      final autismclient.ai.engine.AiPathfinder.Inner_IoWorker strategy
   ) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         final long deadline = System.nanoTime() + Math.max(1, Math.min(3600, timeoutSeconds)) * 1000000000L;
         final double arrivalRadius = Math.max(0.6, Math.min(8.0, radius));
         return new AiOperation() {
            final Minecraft init_Minecraft = Minecraft.getInstance();
            final Deque<BlockPos> init_Deque = new ArrayDeque<>();
            CompletableFuture<List<AiRegions.Region>> init_CompletableFuture;
            AiOperation init_AiOperation;
            boolean init_Z;
            boolean load;
            boolean read;
            boolean write;

            @Override
            public AiOperation.Result tick() {
               if (!this.write && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
                  if (System.nanoTime() > deadline) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Travel deadline reached");
                  }

                  if (!this.load) {
                     this.load = true;
                     if (this.init_Minecraft.player.blockPosition().distSqr(target) > 4096.0) {
                        AiService service = AiService.load_cc();
                        if (service != null) {
                           List<AiRegions.Region> observations = service.load_List();
                           int x = this.init_Minecraft.player.blockPosition().getX();
                           int z = this.init_Minecraft.player.blockPosition().getZ();
                           if (observations.size() >= 2) {
                              this.init_CompletableFuture = CompletableFuture.supplyAsync(
                                 () -> AiRegions.init(observations, x, z, target.getX(), target.getZ())
                              );
                           }
                        }
                     }
                  }

                  if (this.init_CompletableFuture != null) {
                     if (!this.init_CompletableFuture.isDone()) {
                        return this.init("Planning with observed region connections");
                     }

                     try {
                        for (AiRegions.Region region : this.init_CompletableFuture.join()) {
                           this.init_Deque.addLast(new BlockPos(region.x(), region.y(), region.z()));
                        }
                     } catch (Exception var7) {
                     }

                     this.init_CompletableFuture = null;
                  }

                  if (!this.read) {
                     this.read = true;
                     this.init_Deque.addLast(target);
                  }

                  for (int chained = 0; chained < 4; chained++) {
                     if (this.init_AiOperation != null) {
                        AiOperation.Result r = this.init_AiOperation.tick();
                        if (!r.terminal()) {
                           return r;
                        }

                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        if (r.status() != AiOperation.Status.SUCCEEDED) {
                           this.cancel();
                           return new AiOperation.Result(r.status(), r.message(), r.dataJson(), r.evidence(), r.failure());
                        }

                        if (this.init_Z) {
                           this.init_Deque.pollFirst();
                           if (this.init_Deque.isEmpty()) {
                              return this.init(AiOperation.Status.SUCCEEDED, "Arrived at target");
                           }
                        }
                     }

                     BlockPos head = this.init_Deque.peekFirst();
                     boolean finalTarget = this.init_Deque.size() == 1;
                     BlockPos legTarget = head;
                     double legRadius = arrivalRadius;
                     this.init_Z = true;
                     if (this.init_D_4(head) > 1936.0) {
                        legTarget = this.init_BlockPos_5(head);
                        legRadius = Math.max(arrivalRadius, 4.0);
                        this.init_Z = false;
                     }

                     if (!finalTarget || groundTarget) {
                        legTarget = AiGoto.init_BlockPos(legTarget, 24);
                     }

                     this.init_AiOperation = AiGoto.init(legTarget, legRadius, allowDig, allowPillar, allowBridge, this.init(), strategy);
                  }

                  return this.init("Continuing the next route leg");
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Travel cancelled");
               }
            }

            BlockPos init_BlockPos_5(BlockPos head) {
               Vec3 pos = this.init_Minecraft.player.position();
               double dx = head.getX() + 0.5 - pos.x;
               double dz = head.getZ() + 0.5 - pos.z;
               double horizontal = Math.hypot(dx, dz);
               if (horizontal < 1.0E-6) {
                  return head;
               }

               double scale = 40.0 / horizontal;
               return BlockPos.containing(pos.x + dx * scale, this.init_Minecraft.player.getY(), pos.z + dz * scale);
            }

            double init_D_4(BlockPos p) {
               Vec3 pos = this.init_Minecraft.player.position();
               double dx = p.getX() + 0.5 - pos.x;
               double dz = p.getZ() + 0.5 - pos.z;
               return dx * dx + dz * dz;
            }

            int init() {
               return (int)Math.max(1L, (deadline - System.nanoTime()) / 1000000000L);
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, "{}", List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               this.cancel();
               return new AiOperation.Result(status, message, "{}", List.of());
            }

            @Override
            public void cancel() {
               if (!this.write) {
                  this.write = true;
                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Engine travel requires a connected living player");
      }
   }
}
