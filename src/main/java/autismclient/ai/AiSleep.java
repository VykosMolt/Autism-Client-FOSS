package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class AiSleep {
   private static final int init_I = 24;
   private static final int load_I = 80;
   private static final int read = 120;
   private static final int write = 300;
   private static final int update = 40;
   private static final long init_J = 12542L;
   private static final long load_J = 23460L;

   private AiSleep() {
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int timeoutSeconds = Math.max(15, Math.min(900, AiJson.getInt(args, "timeoutSeconds", 180)));
         final boolean place = AiJson.getBoolean(args, "place", true);
         final boolean requireNight = AiJson.getBoolean(args, "requireNight", true);
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final long load_J = AiSleep.init_J();
            final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
            final autismclient.ai.engine.AiPlacer init_DataPipe = new autismclient.ai.engine.AiPlacer();
            AiOperation init_AiOperation;
            BlockPos init_BlockPos;
            int init_I;
            int load_I;
            int read_I;
            int write;
            boolean init_Z;
            boolean load_Z;
            boolean read_Z;
            long read_J = -1L;

            @Override
            public AiOperation.Result tick() {
               if (!this.read_Z && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.init_J) {
                     return this.init(this.load_Z ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT, "Sleep deadline reached");
                  }

                  if (mc.player.isSleeping()) {
                     this.load_Z = true;
                     this.read_I++;
                     if (!AiSleep.load()) {
                        mc.player.stopSleeping();
                        return this.init(AiOperation.Status.SUCCEEDED, "Slept through; day time is now " + AiSleep.init_J());
                     } else if (this.read_I > 300) {
                        mc.player.stopSleeping();
                        return this.init(
                           AiOperation.Status.UNCERTAIN, "Still dark after 300 ticks asleep (day time " + AiSleep.init_J() + "); the sleep did not complete"
                        );
                     } else {
                        return this.init("Sleeping (" + this.read_I + " ticks)");
                     }
                  } else {
                     if (this.load_Z) {
                        return this.init(
                           AiSleep.load() ? AiOperation.Status.UNCERTAIN : AiOperation.Status.SUCCEEDED,
                           AiSleep.load() ? "Woke early and it is still dark; the sleep did not complete" : "Woke up; day time is now " + AiSleep.init_J()
                        );
                     }

                     String dimension = mc.level.dimension().identifier().toString();
                     if (mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.BED_RULE).destroyOnUse()) {
                        return this.init(
                           AiOperation.Status.FAILED, "Beds explode in " + dimension + "; sleep in the Overworld, or use game:dimension to get there first"
                        );
                     }

                     if (requireNight && !AiSleep.load()) {
                        return this.init(
                           AiOperation.Status.FAILED,
                           "It is not dark enough to sleep (day time "
                              + AiSleep.init_J()
                              + "); beds work at night and during a thunderstorm. Pass requireNight:false to try anyway."
                        );
                     }

                     if (this.init_Z) {
                        AiOperation.Result placed = this.init_AiOperation_Result_31();
                        if (placed != null) {
                           return placed;
                        }
                     }

                     if ((this.init_BlockPos == null || !(mc.level.getBlockState(this.init_BlockPos).getBlock() instanceof BedBlock)) && this.write-- <= 0) {
                        this.init_BlockPos = this.init_BlockPos_12();
                        this.write = 40;
                     }

                     if (this.init_BlockPos == null && this.write < 40) {
                        return this.init("Looking for a bed");
                     }

                     if (this.init_BlockPos != null) {
                        BlockHitResult interactionHit = autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, this.init_BlockPos);
                        if (interactionHit == null) {
                           if (this.init_AiOperation == null) {
                              this.init_AiOperation = AiGoto.init_AiOperation(this.init_BlockPos, Math.min(120, this.init_I_32()));
                           }

                           AiOperation.Result legResult = this.init_AiOperation.tick();
                           if (!legResult.terminal()) {
                              return this.init("Walking to the bed");
                           } else {
                              this.init_AiOperation.cancel();
                              this.init_AiOperation = null;
                              if (legResult.status() != AiOperation.Status.SUCCEEDED) {
                                 this.init_BlockPos = null;
                                 return this.init("No route to that bed; looking for another");
                              } else {
                                 return this.init("Arrived at the bed");
                              }
                           }
                        } else {
                           if (this.init_AiOperation != null) {
                              this.init_AiOperation.cancel();
                              this.init_AiOperation = null;
                           }

                           this.init_DataUtil.aimAt(interactionHit.getLocation());
                           this.init_DataUtil.tick();
                           if (++this.init_I > 80) {
                              String refusal = AiSleep.init(AiChatLedger.init_List(16), this.read_J);
                              return this.init(
                                 AiOperation.Status.FAILED,
                                 refusal.isEmpty()
                                    ? "The bed did not accept the player and no refusal reason was observed; the usual reasons are monsters nearby or an obstructed bed"
                                    : "The bed refused: " + refusal
                              );
                           }

                           if (!autismclient.ai.engine.AiAim.isLookingAtBlock(this.init_BlockPos)) {
                              return this.init("Turning toward the bed");
                           }

                           BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                           if (hit == null) {
                              return this.init("Turning toward the bed");
                           }

                           if (this.read_J < 0L) {
                              this.read_J = AiChatLedger.init_J();
                           }

                           mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                           Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                           return this.init("Getting into bed");
                        }
                     } else {
                        if (!place) {
                           return this.init(AiOperation.Status.FAILED, "No bed within 24 blocks and place is disabled");
                        }

                        Item carried = this.init_Item_4();
                        if (carried == null) {
                           JsonObject missing = new JsonObject();
                           missing.addProperty("minecraft:red_bed", 1);
                           return this.init(AiOperation.Status.FAILED, "No bed nearby and none in the inventory", AiJson.objectOf("missing", missing));
                        }

                        BlockPos spot = AiCraft.findPlacementSpot();
                        if (spot == null) {
                           return this.init(AiOperation.Status.FAILED, "No free supported spot next to the player for a bed");
                        }

                        this.init_DataPipe.init_V_BlockPos_Item(spot, carried);
                        this.init_Z = true;
                        this.load_I = 0;
                        return this.init("Placing a bed");
                     }
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Sleep cancelled");
               }
            }

            AiOperation.Result init_AiOperation_Result_31() {
               autismclient.ai.engine.AiPlacer.PlaceStatus status = this.init_DataPipe.init_DataPipe_DataUtil();
               if (status == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                  this.init_Z = false;
                  this.init_BlockPos = this.init_DataPipe.init_BlockPos();
                  return this.init("Bed placed");
               }

               if (status != autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED && ++this.load_I <= 120) {
                  return this.init("Placing a bed");
               }

               this.init_Z = false;
               return this.init(AiOperation.Status.FAILED, "Could not place a bed: " + this.init_DataPipe.init_Str());
            }

            BlockPos init_BlockPos_12() {
               BlockPos origin = mc.player.blockPosition();
               BlockPos best = null;
               double bestDistance = 1.7976931348623157E308;

               for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-24, -8, -24), origin.offset(24, 8, 24))) {
                  if (AiTerrain.isLoaded(mc.level, pos) && mc.level.getBlockState(pos).getBlock() instanceof BedBlock) {
                     double distance = mc.player.distanceToSqr(Vec3.atCenterOf(pos));
                     if (distance < bestDistance) {
                        bestDistance = distance;
                        best = pos.immutable();
                     }
                  }
               }

               return best;
            }

            Item init_Item_4() {
               AiItems.ItemSpec beds = AiItems.resolve("bed");

               for (Item item : beds.items()) {
                  if (AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(item).toString()) > 0) {
                     return item;
                  }
               }

               return null;
            }

            int init_I_32() {
               return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
            }

            JsonObject init_JsonObject_48() {
               JsonObject out = AiJson.objectOf(
                  "dayTime",
                  AiSleep.init_J(),
                  "night",
                  AiSleep.init_Z(),
                  "canSleep",
                  AiSleep.load(),
                  "startDayTime",
                  this.load_J,
                  "slept",
                  this.load_Z,
                  "sleeping",
                  mc.player != null && mc.player.isSleeping()
               );
               if (this.init_BlockPos != null) {
                  out.addProperty("bedAt", this.init_BlockPos.toShortString());
               }

               return out;
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_48().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, null);
            }

            AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.init_JsonObject_48();
               if (extra != null) {
                  for (Entry<String, JsonElement> entry : extra.entrySet()) {
                     data.add(entry.getKey(), entry.getValue());
                  }
               }

               this.cancel();
               return new AiOperation.Result(status, message, data.toString(), List.of());
            }

            @Override
            public void cancel() {
               if (!this.read_Z) {
                  this.read_Z = true;
                  this.init_DataUtil.clear();
                  this.init_DataPipe.init_V();
                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Sleeping requires a connected living player");
      }
   }

   static long init_J() {
      Minecraft mc = Minecraft.getInstance();
      return mc.level == null ? 0L : mc.level.getOverworldClockTime() % 24000L;
   }

   static boolean init_Z() {
      long time = init_J();
      return time >= 12542L && time <= 23460L;
   }

   static boolean load() {
      Minecraft mc = Minecraft.getInstance();
      return mc.level == null ? false : mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.BED_RULE).canSleep(mc.level);
   }

   static String init(List<AiChatLedger.ChatEntry> entries, long sinceRevision) {
      for (AiChatLedger.ChatEntry entry : entries) {
         if (entry.revision() > sinceRevision) {
            String text = entry.text().toLowerCase(Locale.ROOT);
            if (text.contains("sleep") || text.contains("rest") || text.contains("bed") || text.contains("monsters")) {
               return entry.text();
            }
         }
      }

      return "";
   }
}
