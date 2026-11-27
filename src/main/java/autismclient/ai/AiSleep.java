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
   private static final int BED_SEARCH_RADIUS = 24;
   private static final int USE_ATTEMPT_TICKS = 80;
   private static final int read = 120;
   private static final int write = 300;
   private static final int update = 40;
   private static final long SLEEP_START_TICK = 12542L;
   private static final long SLEEP_END_TICK = 23460L;

   private AiSleep() {
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int timeoutSeconds = Math.max(15, Math.min(900, AiJson.getInt(args, "timeoutSeconds", 180)));
         final boolean place = AiJson.getBoolean(args, "place", true);
         final boolean requireNight = AiJson.getBoolean(args, "requireNight", true);
         return new AiOperation() {
            final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
            final long startDayTime = AiSleep.dayTime();
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer();
            AiOperation approachOp;
            BlockPos bedPos;
            int useTicks;
            int placeTicks;
            int sleepTicks;
            int write;
            boolean placingBed;
            boolean slept;
            boolean cancelled;
            long chatRevisionBeforeUse = -1L;

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.deadline) {
                     return this.init(this.slept ? AiOperation.Status.UNCERTAIN : AiOperation.Status.TIMED_OUT, "Sleep deadline reached");
                  }

                  if (mc.player.isSleeping()) {
                     this.slept = true;
                     this.sleepTicks++;
                     if (!AiSleep.load()) {
                        mc.player.stopSleeping();
                        return this.init(AiOperation.Status.SUCCEEDED, "Slept through; day time is now " + AiSleep.dayTime());
                     } else if (this.sleepTicks > 300) {
                        mc.player.stopSleeping();
                        return this.init(
                           AiOperation.Status.UNCERTAIN, "Still dark after 300 ticks asleep (day time " + AiSleep.dayTime() + "); the sleep did not complete"
                        );
                     } else {
                        return this.init("Sleeping (" + this.sleepTicks + " ticks)");
                     }
                  } else {
                     if (this.slept) {
                        return this.init(
                           AiSleep.load() ? AiOperation.Status.UNCERTAIN : AiOperation.Status.SUCCEEDED,
                           AiSleep.load() ? "Woke early and it is still dark; the sleep did not complete" : "Woke up; day time is now " + AiSleep.dayTime()
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
                              + AiSleep.dayTime()
                              + "); beds work at night and during a thunderstorm. Pass requireNight:false to try anyway."
                        );
                     }

                     if (this.placingBed) {
                        AiOperation.Result placed = this.tickBedPlacement();
                        if (placed != null) {
                           return placed;
                        }
                     }

                     if ((this.bedPos == null || !(mc.level.getBlockState(this.bedPos).getBlock() instanceof BedBlock)) && this.write-- <= 0) {
                        this.bedPos = this.nearestBed();
                        this.write = 40;
                     }

                     if (this.bedPos == null && this.write < 40) {
                        return this.init("Looking for a bed");
                     }

                     if (this.bedPos != null) {
                        BlockHitResult interactionHit = autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, this.bedPos);
                        if (interactionHit == null) {
                           if (this.approachOp == null) {
                              this.approachOp = AiGoto.approachBlockCompat(this.bedPos, Math.min(120, this.secondsLeft()));
                           }

                           AiOperation.Result legResult = this.approachOp.tick();
                           if (!legResult.terminal()) {
                              return this.init("Walking to the bed");
                           } else {
                              this.approachOp.cancel();
                              this.approachOp = null;
                              if (legResult.status() != AiOperation.Status.SUCCEEDED) {
                                 this.bedPos = null;
                                 return this.init("No route to that bed; looking for another");
                              } else {
                                 return this.init("Arrived at the bed");
                              }
                           }
                        } else {
                           if (this.approachOp != null) {
                              this.approachOp.cancel();
                              this.approachOp = null;
                           }

                           this.aim.aimAt(interactionHit.getLocation());
                           this.aim.tick();
                           if (++this.useTicks > 80) {
                              String refusal = AiSleep.init(AiChatLedger.recent(16), this.chatRevisionBeforeUse);
                              return this.init(
                                 AiOperation.Status.FAILED,
                                 refusal.isEmpty()
                                    ? "The bed did not accept the player and no refusal reason was observed; the usual reasons are monsters nearby or an obstructed bed"
                                    : "The bed refused: " + refusal
                              );
                           }

                           if (!autismclient.ai.engine.AiAim.isLookingAtBlock(this.bedPos)) {
                              return this.init("Turning toward the bed");
                           }

                           BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                           if (hit == null) {
                              return this.init("Turning toward the bed");
                           }

                           if (this.chatRevisionBeforeUse < 0L) {
                              this.chatRevisionBeforeUse = AiChatLedger.revision();
                           }

                           mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                           Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                           return this.init("Getting into bed");
                        }
                     } else {
                        if (!place) {
                           return this.init(AiOperation.Status.FAILED, "No bed within 24 blocks and place is disabled");
                        }

                        Item carried = this.carriedBed();
                        if (carried == null) {
                           JsonObject missing = new JsonObject();
                           missing.addProperty("minecraft:red_bed", 1);
                           return this.init(AiOperation.Status.FAILED, "No bed nearby and none in the inventory", AiJson.objectOf("missing", missing));
                        }

                        BlockPos spot = AiCraft.findPlacementSpot();
                        if (spot == null) {
                           return this.init(AiOperation.Status.FAILED, "No free supported spot next to the player for a bed");
                        }

                        this.placer.start(spot, carried);
                        this.placingBed = true;
                        this.placeTicks = 0;
                        return this.init("Placing a bed");
                     }
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Sleep cancelled");
               }
            }

            AiOperation.Result tickBedPlacement() {
               autismclient.ai.engine.AiPlacer.PlaceStatus status = this.placer.tick();
               if (status == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                  this.placingBed = false;
                  this.bedPos = this.placer.targetPos();
                  return this.init("Bed placed");
               }

               if (status != autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED && ++this.placeTicks <= 120) {
                  return this.init("Placing a bed");
               }

               this.placingBed = false;
               return this.init(AiOperation.Status.FAILED, "Could not place a bed: " + this.placer.failureReason());
            }

            BlockPos nearestBed() {
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

            Item carriedBed() {
               AiItems.ItemSpec beds = AiItems.resolve("bed");

               for (Item item : beds.items()) {
                  if (AiGameState.countInventoryItem(BuiltInRegistries.ITEM.getKey(item).toString()) > 0) {
                     return item;
                  }
               }

               return null;
            }

            int secondsLeft() {
               return (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L);
            }

            JsonObject progressData() {
               JsonObject out = AiJson.objectOf(
                  "dayTime",
                  AiSleep.dayTime(),
                  "night",
                  AiSleep.isNight(),
                  "canSleep",
                  AiSleep.load(),
                  "startDayTime",
                  this.startDayTime,
                  "slept",
                  this.slept,
                  "sleeping",
                  mc.player != null && mc.player.isSleeping()
               );
               if (this.bedPos != null) {
                  out.addProperty("bedAt", this.bedPos.toShortString());
               }

               return out;
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressData().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, null);
            }

            AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.progressData();
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
               if (!this.cancelled) {
                  this.cancelled = true;
                  this.aim.clear();
                  this.placer.reset();
                  if (this.approachOp != null) {
                     this.approachOp.cancel();
                     this.approachOp = null;
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Sleeping requires a connected living player");
      }
   }

   static long dayTime() {
      Minecraft mc = Minecraft.getInstance();
      return mc.level == null ? 0L : mc.level.getOverworldClockTime() % 24000L;
   }

   static boolean isNight() {
      long time = dayTime();
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
