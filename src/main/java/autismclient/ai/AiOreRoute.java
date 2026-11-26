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
   static final int load = 8;
   static final int read = 20;
   static final boolean init_Z = Boolean.getBoolean("autism.ai.legitMining");

   private AiOreRoute() {
   }

   static boolean init(int feetY, int bestY) {
      return feetY > bestY + 6;
   }

   static Direction init(Direction first, int n) {
      Direction d = first;

      for (int i = 0; i < n; i++) {
         d = d.getClockWise();
      }

      return d;
   }

   static AiOperation init(String family, String drop, int targetHeld, int blocks, int seconds) {
      return !init_Z ? load(family, drop, targetHeld, 48, -2147483648, seconds) : init(family, drop, targetHeld, blocks, -2147483648, seconds, true);
   }

   static AiOperation init(String family, String drop, int targetHeld, int blocks, int bestY, int seconds) {
      return init(family, drop, targetHeld, blocks, bestY, seconds, null);
   }

   static AiOperation init(String family, String drop, int targetHeld, int blocks, int bestY, int seconds, BlockPos focus) {
      if (!init_Z) {
         JsonObject args = AiJson.init_JsonObject_ObjArr("block", family, "drop", drop, "held", targetHeld, "radius", 64, "timeoutSeconds", Math.max(60, seconds));
         if (bestY != -2147483648) {
            args.addProperty("exploreY", bestY);
         }

         if (focus != null) {
            args.addProperty("focusX", focus.getX());
            args.addProperty("focusY", focus.getY());
            args.addProperty("focusZ", focus.getZ());
         }

         return autismclient.ai.a.AiBlockMine.init_AiOperation(args);
      } else {
         return init(family, drop, targetHeld, blocks, bestY, seconds, false);
      }
   }

   static AiOperation load(String family, String drop, int targetHeld, int radius, int bestY, int seconds) {
      JsonObject args = AiJson.init_JsonObject_ObjArr("block", family, "drop", drop, "held", targetHeld, "radius", radius, "timeoutSeconds", Math.max(60, seconds));
      if (bestY != -2147483648) {
         args.addProperty("exploreY", bestY);
      }

      return autismclient.ai.a.AiBlockMine.init_AiOperation(args);
   }

   private static AiOperation init(
      final String family, final String drop, final int targetHeld, final int blocks, final int bestY, final int seconds, final boolean nearOnly
   ) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         return new AiOperation() {
            final long init_J = System.nanoTime() + Math.max(30, seconds) * 1000000000L;
            final Direction init_Direction = Direction.fromYRot(mc.player.getYRot());
            AiOreRoute.Inner_DataUtil init_aH_DataUtil = !nearOnly && AiOreRoute.init(mc.player.getBlockY(), bestY + 12) ? AiOreRoute.Inner_DataUtil.load : AiOreRoute.Inner_DataUtil.init;
            AiOperation init_AiOperation;
            int init_I;
            int load;
            int read;
            boolean init_Z;

            @Override
            public AiOperation.Result tick() {
               if (!this.init_Z && mc.player != null) {
                  if (AiCraft.init_I(drop) >= targetHeld) {
                     return this.init(AiOperation.Status.SUCCEEDED, "Have " + AiCraft.init_I(drop) + " " + drop);
                  }

                  if (System.nanoTime() > this.init_J) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Ran out of time looking for " + family);
                  }

                  if (this.init_AiOperation != null) {
                     AiOperation.Result r = this.init_AiOperation.tick();
                     if (!r.terminal()) {
                        return this.init(r.message());
                     }

                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.init(AiOperation.Status.CANCELLED, "Cancelled");
                     }

                     if (r.failure() == AiFailure.tick || r.failure() == AiFailure.get) {
                        this.cancel();
                        return r;
                     }

                     if (AiCraft.init_I(drop) >= targetHeld) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Have " + AiCraft.init_I(drop) + " " + drop);
                     }

                     AutismClientAddon.LOG
                        .info(
                           "[AI] ore route {} {}: {} {} at y {} ({}/{})",
                           family,
                           this.init_aH_DataUtil,
                           r.status(),
                           AiProgression.load_Str(r.message()),
                           mc.player.getBlockY(),
                           AiCraft.init_I(drop),
                           targetHeld
                        );
                     return this.init(r);
                  } else if (AiProgression.init_I(mc) < 3) {
                     this.init_AiOperation = AiTidy.init(AiJson.init_JsonObject_ObjArr("action", "drop", "freeSlots", 6, "keep", this.init_JsonArray()));
                     return this.init("Making room in the bag");
                  } else {
                     return this.init_AiOperation_Result_18();
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Cancelled");
               }
            }

            AiOperation.Result init_AiOperation_Result_18() {
               int missingBlocks = Math.max(1, blocks - Math.max(0, AiCraft.init_I(drop) - (targetHeld - blocks)));
               int legSeconds = (int)Math.max(20L, Math.min(300L, (this.init_J - System.nanoTime()) / 1000000000L));
               switch (this.init_aH_DataUtil) {
                  case init:
                  case read:
                  case update:
                     this.init_AiOperation = autismclient.ai.a.AiMine.init(
                        AiJson.init_JsonObject_ObjArr(
                           "block",
                           family,
                           "count",
                           missingBlocks,
                           "radius",
                           this.init_aH_DataUtil == AiOreRoute.Inner_DataUtil.update ? 32 : 48,
                           "timeoutSeconds",
                           Math.min(120, legSeconds)
                        )
                     );
                     return this.init("Mining " + family + (this.init_aH_DataUtil == AiOreRoute.Inner_DataUtil.init ? " nearby" : " at y " + mc.player.getBlockY()));
                  case load: {
                     this.read = mc.player.getBlockY();
                     Direction d = AiOreRoute.init(this.init_Direction, this.load);
                     this.init_AiOperation = autismclient.ai.a.AiExcavate.load(
                        AiJson.init_JsonObject_ObjArr(
                           "shape", "staircase", "targetY", bestY, "direction", d.getName(), "timeoutSeconds", Math.max(60, legSeconds * 2)
                        )
                     );
                     return this.init("Digging down " + d.getName() + " to y " + bestY + " for " + family);
                  }
                  case write: {
                     Direction d = AiOreRoute.init(this.init_Direction, this.init_I);
                     this.init_AiOperation = autismclient.ai.a.AiExcavate.load(
                        AiJson.init_JsonObject_ObjArr("shape", "tunnel", "direction", d.getName(), "length", 20, "timeoutSeconds", legSeconds)
                     );
                     return this.init("Branch tunnel " + (this.init_I + 1) + " " + d.getName() + " looking for " + family);
                  }
                  default:
                     return this.init(AiOperation.Status.FAILED, "Unknown phase");
               }
            }

            AiOperation.Result init(AiOperation.Result r) {
               switch (this.init_aH_DataUtil) {
                  case init:
                     if (nearOnly) {
                        return this.init(AiOperation.Status.FAILED, "No " + family + " in loaded terrain nearby (" + r.message() + ")");
                     }

                     this.init_aH_DataUtil = AiOreRoute.init(mc.player.getBlockY(), bestY) ? AiOreRoute.Inner_DataUtil.load : AiOreRoute.Inner_DataUtil.read;
                     break;
                  case load:
                     boolean arrived = !AiOreRoute.init(mc.player.getBlockY(), bestY);
                     boolean progressed = this.read - mc.player.getBlockY() >= 6;
                     if (!arrived && (!progressed || ++this.load < 4)) {
                        if (!progressed && ++this.load >= 4) {
                           this.init_aH_DataUtil = AiOreRoute.Inner_DataUtil.read;
                        }
                     } else {
                        this.init_aH_DataUtil = AiOreRoute.Inner_DataUtil.read;
                     }
                     break;
                  case read:
                     this.init_aH_DataUtil = AiOreRoute.Inner_DataUtil.write;
                     break;
                  case write:
                     this.init_aH_DataUtil = AiOreRoute.Inner_DataUtil.update;
                     break;
                  case update:
                     if (++this.init_I >= 8) {
                        return this.init(
                           AiOperation.Status.FAILED,
                           "No " + family + " after 8 branch tunnels at y " + mc.player.getBlockY() + " (" + AiCraft.init_I(drop) + "/" + targetHeld + ")"
                        );
                     }

                     this.init_aH_DataUtil = AiOreRoute.Inner_DataUtil.write;
               }

               return this.init("Next: " + this.init_aH_DataUtil.name().toLowerCase(Locale.ROOT).replace('_', ' '));
            }

            JsonArray init_JsonArray() {
               JsonArray keep = new JsonArray();

               for (String id : List.of(
                  drop, "minecraft:crafting_table", "minecraft:furnace", "planks", "log", "minecraft:stick", "minecraft:coal", "minecraft:torch"
               )) {
                  keep.add(id);
               }

               AiProgression.init_Set.forEach(keep::add);
               AiProgression.init_List.forEach(keep::add);
               return keep;
            }

            AiOperation.Result init(String message) {
               return AiGameOperations.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_33());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }

               return AiGameOperations.init_AiOperation_Result(status, message, this.init_JsonObject_33());
            }

            JsonObject init_JsonObject_33() {
               return AiJson.init_JsonObject_ObjArr(
                  "ore",
                  family,
                  "held",
                  AiCraft.init_I(drop),
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
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
               }
            }

            @Override
            public boolean canInterruptNow() {
               return this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
            }
         };
      } else {
         throw new IllegalArgumentException("Mining requires a connected living player");
      }
   }

   enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update;
   }
}
