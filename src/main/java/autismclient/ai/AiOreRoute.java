package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

final class AiOreRoute {
   static final int init_I = 6;
   static final int MAX_BRANCH_TUNNELS = 8;
   static final int BRANCH_LENGTH = 20;
   static final boolean LEGIT_MINING = Boolean.getBoolean("autism.ai.legitMining");

   private AiOreRoute() {
   }

   static boolean aboveBestY(int feetY, int bestY) {
      return feetY > bestY + 6;
   }

   static Direction rotateClockwise(Direction first, int n) {
      Direction d = first;

      for (int i = 0; i < n; i++) {
         d = d.getClockWise();
      }

      return d;
   }

   static AiOperation mineOre(String family, String drop, int targetHeld, int blocks, int seconds) {
      return !LEGIT_MINING ? searchMine(family, drop, targetHeld, 48, -2147483648, seconds) : branchMineOp(family, drop, targetHeld, blocks, -2147483648, seconds, true);
   }

   static AiOperation branchMine(String family, String drop, int targetHeld, int blocks, int bestY, int seconds) {
      return mineOre(family, drop, targetHeld, blocks, bestY, seconds, null);
   }

   static AiOperation mineOre(String family, String drop, int targetHeld, int blocks, int bestY, int seconds, BlockPos focus) {
      if (!LEGIT_MINING) {
         JsonObject args = AiJson.objectOf("block", family, "drop", drop, "held", targetHeld, "radius", 64, "timeoutSeconds", Math.max(60, seconds));
         if (bestY != -2147483648) {
            args.addProperty("exploreY", bestY);
         }

         if (focus != null) {
            args.addProperty("focusX", focus.getX());
            args.addProperty("focusY", focus.getY());
            args.addProperty("focusZ", focus.getZ());
         }

         return autismclient.ai.engine.AiBlockMine.create(args);
      } else {
         return branchMineOp(family, drop, targetHeld, blocks, bestY, seconds, false);
      }
   }

   static AiOperation searchMine(String family, String drop, int targetHeld, int radius, int bestY, int seconds) {
      JsonObject args = AiJson.objectOf("block", family, "drop", drop, "held", targetHeld, "radius", radius, "timeoutSeconds", Math.max(60, seconds));
      if (bestY != -2147483648) {
         args.addProperty("exploreY", bestY);
      }

      return autismclient.ai.engine.AiBlockMine.create(args);
   }

   private static AiOperation branchMineOp(
      final String family, final String drop, final int targetHeld, final int blocks, final int bestY, final int seconds, final boolean nearOnly
   ) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         return new AiOperation() {
            final long deadline = System.nanoTime() + Math.max(30, seconds) * 1000000000L;
            final Direction heading = Direction.fromYRot(mc.player.getYRot());
            AiOreRoute.RouteMode init_aH_DataUtil = !nearOnly && AiOreRoute.aboveBestY(mc.player.getBlockY(), bestY + 12) ? AiOreRoute.RouteMode.DESCEND : AiOreRoute.RouteMode.NEAR;
            AiOperation child;
            int init_I;
            int branchIndex;
            int descentStartY;
            boolean init_Z;

            @Override
            public AiOperation.Result tick() {
               if (!this.init_Z && mc.player != null) {
                  if (AiCraft.countItem(drop) >= targetHeld) {
                     return this.finish(AiOperation.Status.SUCCEEDED, "Have " + AiCraft.countItem(drop) + " " + drop);
                  }

                  if (System.nanoTime() > this.deadline) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Ran out of time looking for " + family);
                  }

                  if (this.child != null) {
                     AiOperation.Result r = this.child.tick();
                     if (!r.terminal()) {
                        return this.running(r.message());
                     }

                     this.child.cancel();
                     this.child = null;
                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.finish(AiOperation.Status.CANCELLED, "Cancelled");
                     }

                     if (r.failure() == AiFailure.MISSING_TOOL || r.failure() == AiFailure.TOOL_WORN) {
                        this.cancel();
                        return r;
                     }

                     if (AiCraft.countItem(drop) >= targetHeld) {
                        return this.finish(AiOperation.Status.SUCCEEDED, "Have " + AiCraft.countItem(drop) + " " + drop);
                     }

                     AutismClientAddon.LOG
                        .info(
                           "[AI] ore route {} {}: {} {} at y {} ({}/{})",
                           family,
                           this.init_aH_DataUtil,
                           r.status(),
                           AiProgression.clip(r.message()),
                           mc.player.getBlockY(),
                           AiCraft.countItem(drop),
                           targetHeld
                        );
                     return this.afterChild(r);
                  } else if (AiProgression.freeSlots(mc) < 3) {
                     this.child = AiTidy.create(AiJson.objectOf("action", "drop", "freeSlots", 6, "keep", this.keepItems()));
                     return this.running("Making room in the bag");
                  } else {
                     return this.init_AiOperation_Result_18();
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Cancelled");
               }
            }

            AiOperation.Result init_AiOperation_Result_18() {
               int missingBlocks = Math.max(1, blocks - Math.max(0, AiCraft.countItem(drop) - (targetHeld - blocks)));
               int legSeconds = (int)Math.max(20L, Math.min(300L, (this.deadline - System.nanoTime()) / 1000000000L));
               switch (this.init_aH_DataUtil) {
                  case NEAR:
                  case DEEP:
                  case MINE_BRANCH:
                     this.child = autismclient.ai.engine.AiMine.init(
                        AiJson.objectOf(
                           "block",
                           family,
                           "count",
                           missingBlocks,
                           "radius",
                           this.init_aH_DataUtil == AiOreRoute.RouteMode.MINE_BRANCH ? 32 : 48,
                           "timeoutSeconds",
                           Math.min(120, legSeconds)
                        )
                     );
                     return this.running("Mining " + family + (this.init_aH_DataUtil == AiOreRoute.RouteMode.NEAR ? " nearby" : " at y " + mc.player.getBlockY()));
                  case DESCEND: {
                     this.descentStartY = mc.player.getBlockY();
                     Direction d = AiOreRoute.rotateClockwise(this.heading, this.branchIndex);
                     this.child = autismclient.ai.engine.AiExcavate.load(
                        AiJson.objectOf(
                           "shape", "staircase", "targetY", bestY, "direction", d.getName(), "timeoutSeconds", Math.max(60, legSeconds * 2)
                        )
                     );
                     return this.running("Digging down " + d.getName() + " to y " + bestY + " for " + family);
                  }
                  case BRANCH: {
                     Direction d = AiOreRoute.rotateClockwise(this.heading, this.init_I);
                     this.child = autismclient.ai.engine.AiExcavate.load(
                        AiJson.objectOf("shape", "tunnel", "direction", d.getName(), "length", 20, "timeoutSeconds", legSeconds)
                     );
                     return this.running("Branch tunnel " + (this.init_I + 1) + " " + d.getName() + " looking for " + family);
                  }
                  default:
                     return this.finish(AiOperation.Status.FAILED, "Unknown phase");
               }
            }

            AiOperation.Result afterChild(AiOperation.Result r) {
               switch (this.init_aH_DataUtil) {
                  case NEAR:
                     if (nearOnly) {
                        return this.finish(AiOperation.Status.FAILED, "No " + family + " in loaded terrain nearby (" + r.message() + ")");
                     }

                     this.init_aH_DataUtil = AiOreRoute.aboveBestY(mc.player.getBlockY(), bestY) ? AiOreRoute.RouteMode.DESCEND : AiOreRoute.RouteMode.DEEP;
                     break;
                  case DESCEND:
                     boolean arrived = !AiOreRoute.aboveBestY(mc.player.getBlockY(), bestY);
                     boolean progressed = this.descentStartY - mc.player.getBlockY() >= 6;
                     if (!arrived && (!progressed || ++this.branchIndex < 4)) {
                        if (!progressed && ++this.branchIndex >= 4) {
                           this.init_aH_DataUtil = AiOreRoute.RouteMode.DEEP;
                        }
                     } else {
                        this.init_aH_DataUtil = AiOreRoute.RouteMode.DEEP;
                     }
                     break;
                  case DEEP:
                     this.init_aH_DataUtil = AiOreRoute.RouteMode.BRANCH;
                     break;
                  case BRANCH:
                     this.init_aH_DataUtil = AiOreRoute.RouteMode.MINE_BRANCH;
                     break;
                  case MINE_BRANCH:
                     if (++this.init_I >= 8) {
                        return this.finish(
                           AiOperation.Status.FAILED,
                           "No " + family + " after 8 branch tunnels at y " + mc.player.getBlockY() + " (" + AiCraft.countItem(drop) + "/" + targetHeld + ")"
                        );
                     }

                     this.init_aH_DataUtil = AiOreRoute.RouteMode.BRANCH;
               }

               return this.running("Next: " + this.init_aH_DataUtil.name().toLowerCase(Locale.ROOT).replace('_', ' '));
            }

            JsonArray keepItems() {
               JsonArray keep = new JsonArray();

               for (String id : List.of(
                  drop, "minecraft:crafting_table", "minecraft:furnace", "planks", "log", "minecraft:stick", "minecraft:coal", "minecraft:torch"
               )) {
                  keep.add(id);
               }

               AiProgression.KEEP_ITEMS.forEach(keep::add);
               AiProgression.PICKAXES.forEach(keep::add);
               return keep;
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.progressJson());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               if (this.child != null) {
                  this.child.cancel();
                  this.child = null;
               }

               return AiGameOperations.result(status, message, this.progressJson());
            }

            JsonObject progressJson() {
               return AiJson.objectOf(
                  "ore",
                  family,
                  "held",
                  AiCraft.countItem(drop),
                  "target",
                  targetHeld,
                  "phase",
                  this.init_aH_DataUtil.name(),
                  "branches",
                  this.init_I,
                  "y",
                  mc.player == null ? 0 : mc.player.getBlockY()
               );
            }

            @Override
            public void cancel() {
               this.init_Z = true;
               if (this.child != null) {
                  this.child.cancel();
               }
            }

            @Override
            public boolean canInterruptNow() {
               return this.child == null || this.child.canInterruptNow();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.child != null && this.child.requiresReconciliationAfterEmergency();
            }
         };
      } else {
         throw new IllegalArgumentException("Mining requires a connected living player");
      }
   }

   enum RouteMode {
      NEAR,
      DESCEND,
      DEEP,
      BRANCH,
      MINE_BRANCH;
   }
}
