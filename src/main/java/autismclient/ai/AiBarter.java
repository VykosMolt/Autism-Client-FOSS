package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

final class AiBarter {
   static final String PIGLIN_ID = "minecraft:piglin";
   static final String GOLD_INGOT_ID = "minecraft:gold_ingot";
   static final String ENDER_PEARL_ID = "minecraft:ender_pearl";
   private static final double init_D = 32.0;
   private static final double load_D = 4.0;
   private static final int AIM_SETTLE_TICKS = 30;
   private static final int THROW_CONFIRM_TICKS = 40;
   private static final int PICKUP_WAIT_TICKS = 200;
   private static final int ADMIRE_WAIT_TICKS = 360;
   private static final double read_D = 12.0;
   private static final double write_D = 24.0;
   private static final int update_I = 3;
   private static final int MAX_PICKUP_FAILURES = 2;
   private static final int PICKUP_TIMEOUT_SECONDS = 25;
   private static final double BABY_PIGLIN_RADIUS = 9.0;
   private static final double BABY_NEARBY_PENALTY = 400.0;
   private static final double LAVA_NEARBY_PENALTY = 900.0;
   private static final int close = 60;
   private static final int open = 4;

   private AiBarter() {
   }

   static AiBarter.BarterDecision decide(int pearlsTarget, int pearlsGained, int goldCarried, int goldSpent, int goldBudget, boolean pastDeadline) {
      if (pearlsTarget > 0 && pearlsGained >= pearlsTarget) {
         return AiBarter.BarterDecision.PEARLS_MET;
      } else if (goldCarried > 0 && goldSpent < goldBudget) {
         return pastDeadline ? AiBarter.BarterDecision.TIMED_OUT : AiBarter.BarterDecision.CONTINUE;
      } else {
         return AiBarter.BarterDecision.GOLD_OUT;
      }
   }

   static int init(int goldArg, int carried) {
      return goldArg > 0 ? Math.min(goldArg, carried) : carried;
   }

   static boolean isBarterPiglin(String typeId, boolean baby, boolean alive, boolean converting) {
      return alive && "minecraft:piglin".equals(typeId) && !baby && !converting;
   }

   static AiOperation init(JsonObject args) {
      return AiBarterBatch.init(args);
   }

   static AiOperation createBarterOp(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int pearlsTarget = Math.max(0, Math.min(64, AiJson.getInt(args, "pearls", 0)));
         final int goldArg = Math.max(0, Math.min(2304, AiJson.getInt(args, "gold", 0)));
         final int timeoutSeconds = Math.max(10, Math.min(900, AiJson.getInt(args, "timeoutSeconds", 300)));
         if (AiGameState.countMainInventoryItem("minecraft:gold_ingot") <= 0) {
            JsonObject missing = new JsonObject();
            missing.addProperty("minecraft:gold_ingot", 1);
            return AiGameOperations.immediateFailure(
               AiOperation.Status.FAILED,
               AiFailure.MISSING_MATERIALS,
               "NO_GOLD: no gold ingots in the inventory to barter with; smelt or mine gold first",
               AiJson.objectOf("reason", "no_gold", "missing", missing)
            );
         } else {
            return new AiOperation() {
               final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
               final int goldToTrade = AiBarter.init(goldArg, AiGameState.countMainInventoryItem("minecraft:gold_ingot"));
               final int pearlsAtStart = AiGameState.countInventoryItem("minecraft:ender_pearl");
               final int originalSlot = mc.player.getInventory().getSelectedSlot();
               final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
               final AiGuard.GuardPolicy guardPolicy = AiGuard.GuardPolicy.standard();
               final Set<Integer> ignoringPiglins = new LinkedHashSet<>();
               final Set<Integer> preexistingDrops = new LinkedHashSet<>();
               final Map<String, Integer> dropsGained = new LinkedHashMap<>();
               final Map<String, Integer> dropBaseline = new LinkedHashMap<>();
               final Map<String, Integer> pickupFailures = new LinkedHashMap<>();
               final List<String> uncollected = new ArrayList<>();
               AiOperation child;
               AiOperation guardOp;
               AiOperation armorOp;
               String interruptReason = "";
               Piglin piglin;
               Piglin admirer;
               BlockPos piglinPos;
               AiBarter.BarterStage stage = AiBarter.BarterStage.APPROACH;
               boolean armorChecked;
               boolean armorWarning;
               boolean cancelled;
               int stageTicks;
               int interruptCount;
               int ingotsThrown;
               int ingotsRecovered;
               int ingotsStolen;
               int barters;
               int goldSpent;
               int push;
               int refusedThrows;
               int babyWaitTicks;
               int goldBeforeThrow;
               int babyStealsInRow;
               final Set<Integer> holdersBeforeThrow = new LinkedHashSet<>();
               long throwRevision = -1L;
               String pickupItem;

               @Override
               public AiOperation.Result tick() {
                  if (!this.cancelled && mc.player != null && mc.level != null) {
                     if (mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                        return this.fail(
                           AiFailure.LOW_HEALTH,
                           "low_health",
                           "Low health; bartering stopped after "
                              + this.barters
                              + " barter(s); recovery ops allowed at low health: game:fish, game:farm (harvest only), game:collect_drops, game:eat, game:sleep, game:flee, game:obtain <food> with allowCombat:false, and game:goto while no hostile is within 24 blocks"
                        );
                     }

                     int pearlsGained = this.pearlsGained();
                     AiBarter.BarterDecision decision = AiBarter.decide(
                        pearlsTarget, pearlsGained, AiGameState.countMainInventoryItem("minecraft:gold_ingot"), this.goldSpent, this.goldToTrade, System.nanoTime() > this.deadline
                     );
                     if (decision == AiBarter.BarterDecision.GOLD_OUT
                        && (
                           this.stage == AiBarter.BarterStage.AWAIT
                              || this.stage == AiBarter.BarterStage.RECOVER
                              || this.stage == AiBarter.BarterStage.COLLECT
                              || this.push < 3 && this.nearestItem(16.0) != null
                        )) {
                        decision = AiBarter.BarterDecision.CONTINUE;
                     }

                     switch (decision) {
                        case PEARLS_MET:
                           String detail = pearlsGained
                              + "/"
                              + pearlsTarget
                              + " ender pearls after "
                              + this.barters
                              + " barter(s), gold left "
                              + AiGameState.countMainInventoryItem("minecraft:gold_ingot");
                           AiRunTrace.recordGlobalSplit("pearls_met", detail);
                           return this.finish(AiOperation.Status.SUCCEEDED, "Pearl target met: " + detail);
                        case GOLD_OUT:
                           if (this.barters > 0) {
                              return this.finish(
                                 AiOperation.Status.SUCCEEDED,
                                 "Out of gold after " + this.barters + " barter(s); pearls " + pearlsGained + (pearlsTarget > 0 ? "/" + pearlsTarget : "")
                              );
                           } else {
                              if (this.ingotsThrown == 0) {
                                 return this.fail(AiFailure.MISSING_MATERIALS, "no_gold", "NO_GOLD: the carried gold is gone before the first barter");
                              }

                              return this.fail(
                                 AiFailure.TARGET_GONE, "gold_lost", "Every thrown ingot was stolen or lost without one barter (baby piglins steal gold; brutes never barter)"
                              );
                           }
                        case TIMED_OUT:
                           return this.finish(
                              AiOperation.Status.TIMED_OUT,
                              "Barter deadline reached after "
                                 + this.barters
                                 + " barter(s); pearls "
                                 + pearlsGained
                                 + (pearlsTarget > 0 ? "/" + pearlsTarget : "")
                           );
                        default:
                           AiOperation.Result guarded = this.tickGuard();
                           if (guarded != null) {
                              return guarded;
                           } else if (this.armorOp != null) {
                              AiOperation.Result r = this.armorOp.tick();
                              if (!r.terminal()) {
                                 return this.running("Equipping a gold armor piece for piglin neutrality");
                              } else {
                                 this.armorOp.cancel();
                                 this.armorOp = null;
                                 if (r.status() != AiOperation.Status.SUCCEEDED) {
                                    this.armorWarning = true;
                                 }

                                 return this.running(
                                    r.status() == AiOperation.Status.SUCCEEDED
                                       ? "Gold armor worn"
                                       : "No gold armor available; piglins stay hostile (warning only)"
                                 );
                              }
                           } else {
                              if (!this.armorChecked) {
                                 this.armorChecked = true;
                                 if (!this.wearsGoldArmor()) {
                                    this.armorOp = AiEquip.init(AiJson.objectOf("what", "armor", "prefer", "golden_*"));
                                    return this.running("Equipping a gold armor piece for piglin neutrality");
                                 }
                              }

                              if (this.stage == AiBarter.BarterStage.APPROACH) {
                                 return this.tickApproach();
                              } else if (this.stage == AiBarter.BarterStage.THROW) {
                                 return this.tickThrow();
                              } else if (this.stage == AiBarter.BarterStage.AWAIT) {
                                 return this.tickAwait();
                              } else {
                                 return this.stage == AiBarter.BarterStage.RECOVER ? this.recoverIngot() : this.collectDrops();
                              }
                           }
                     }
                  } else {
                     return this.finish(AiOperation.Status.CANCELLED, "Barter cancelled");
                  }
               }

               AiOperation.Result tickApproach() {
                  if (this.piglin == null
                     || !this.piglin.isAlive()
                     || this.piglin.isBaby()
                     || this.piglin.isConverting()
                     || this.ignoringPiglins.contains(this.piglin.getId())) {
                     this.piglin = this.nearestPiglin();
                     this.push = 0;
                     if (this.piglin == null) {
                        if (this.barters > 0) {
                           return this.finish(
                              AiOperation.Status.SUCCEEDED, "No more adult piglins nearby after " + this.barters + " barter(s)"
                           );
                        }

                        if (!this.ignoringPiglins.isEmpty()) {
                           return this.fail(AiFailure.UNREACHABLE, "unreachable", "No route to any adult piglin nearby");
                        }

                        return this.fail(
                           AiFailure.TARGET_NOT_FOUND, "no_piglins", "NO_PIGLINS: no adult piglin within 32 blocks (babies steal gold without bartering; brutes never barter)"
                        );
                     }
                  }

                  this.piglinPos = this.piglin.blockPosition();
                  if (mc.player.distanceToSqr(this.piglin) > 16.0) {
                     if (this.child == null) {
                        this.child = AiGoto.travelToCompat(
                           this.piglin.blockPosition(), 3.0, true, true, true, Math.min(60, this.secondsLeft())
                        );
                     }

                     AiOperation.Result r = this.child.tick();
                     if (!r.terminal()) {
                        return this.running("Walking to the piglin");
                     } else {
                        this.child.cancel();
                        this.child = null;
                        if (r.status() != AiOperation.Status.SUCCEEDED) {
                           this.ignoringPiglins.add(this.piglin.getId());
                           this.piglin = null;
                           return this.running("Piglin unreachable: " + r.message());
                        } else {
                           return this.running("Arrived at the piglin");
                        }
                     }
                  } else {
                     if (this.child != null) {
                        this.child.cancel();
                        this.child = null;
                     }

                     this.releaseMovementKeys();
                     this.stage = AiBarter.BarterStage.THROW;
                     this.stageTicks = 0;
                     return this.running("In toss range of the piglin");
                  }
               }

               AiOperation.Result tickThrow() {
                  if (this.piglin != null && this.piglin.isAlive()) {
                     if (mc.player.containerMenu != mc.player.inventoryMenu) {
                        return this.finish(AiOperation.Status.FAILED, "Close the open container before bartering");
                     }

                     if (!mc.player.containerMenu.getCarried().isEmpty()) {
                        return this.finish(AiOperation.Status.FAILED, "Clear the cursor stack before bartering");
                     }

                     if (this.throwRevision >= 0L) {
                        int now = AiGameState.countMainInventoryItem("minecraft:gold_ingot");
                        if (now < this.goldBeforeThrow) {
                           int spent = this.goldBeforeThrow - now;
                           this.goldSpent += spent;
                           this.ingotsThrown += spent;
                           this.refusedThrows = 0;
                           this.throwRevision = -1L;
                           this.stage = AiBarter.BarterStage.AWAIT;
                           this.stageTicks = 0;
                           return this.running("Ingot thrown; waiting for the piglin to pick it up");
                        } else if (AiServerEvidence.inventoryRevision() > this.throwRevision) {
                           this.throwRevision = -1L;
                           this.stageTicks = 0;
                           return ++this.refusedThrows >= 3
                              ? this.finish(
                                 AiOperation.Status.UNCERTAIN, "The ingot throw was applied but no ingot left the inventory; reobserve before repeating"
                              )
                              : this.running("The throw was refused; retrying");
                        } else {
                           return ++this.stageTicks > 40
                              ? this.finish(
                                 AiOperation.Status.UNCERTAIN, "The ingot throw was not confirmed by the server; reobserve before repeating"
                              )
                              : this.running("Waiting for the throw to apply");
                        }
                     } else {
                        Piglin admirer = this.findAdmiringPiglin();
                        if (admirer != null) {
                           this.admirer = admirer;
                           this.stage = AiBarter.BarterStage.AWAIT;
                           this.stageTicks = 0;
                           return this.running("A piglin is already admiring an ingot");
                        }

                        this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(this.piglin));
                        this.aim.tick();
                        if (!this.aim.isSettled() && ++this.stageTicks <= 30) {
                           return this.running("Turning toward the piglin");
                        }

                        this.stageTicks = 0;
                        int slot = this.goldSlot();
                        if (slot < 0) {
                           if (this.push < 3 && this.nearestItem(16.0) != null) {
                              this.push++;
                              this.stage = AiBarter.BarterStage.RECOVER;
                              return this.running("Recovering the thrown ingot");
                           } else {
                              return this.running("No ingot in the bag; re-evaluating");
                           }
                        } else {
                           Vec3 toss = mc.player.position().add(this.piglin.position().subtract(mc.player.position()).scale(0.6));
                           if (this.babyPiglinNear(toss, 9.0) && ++this.babyWaitTicks <= 60) {
                              return this.running("A baby piglin is near the toss; waiting");
                           }

                           this.babyWaitTicks = 0;
                           this.holdersBeforeThrow.clear();

                           for (Entity entity : mc.level.entitiesForRendering()) {
                              if (entity instanceof Piglin piglin && piglin.getOffhandItem().is(Items.GOLD_INGOT)) {
                                 this.holdersBeforeThrow.add(piglin.getId());
                              }
                           }

                           this.snapshotDrops();
                           this.goldBeforeThrow = AiGameState.countMainInventoryItem("minecraft:gold_ingot");
                           this.throwRevision = AiServerEvidence.inventoryRevision();
                           AiContainerTransactions.sendPredictedClick(mc.player.containerMenu.containerId, this.screenSlot(slot), 0, ContainerInput.THROW);
                           return this.running("Throwing one gold ingot at the piglin");
                        }
                     }
                  } else {
                     this.piglin = null;
                     this.stage = AiBarter.BarterStage.APPROACH;
                     return this.running("Piglin gone; re-targeting");
                  }
               }

               AiOperation.Result tickAwait() {
                  if (this.admirer == null) {
                     this.admirer = this.findAdmiringPiglin();
                     if (this.admirer != null) {
                        this.stageTicks = 0;
                     }
                  }

                  if (this.admirer == null) {
                     Piglin taker = this.ingotTaker();
                     if (taker != null && taker.isBaby()) {
                        this.ingotsStolen++;
                        this.holdersBeforeThrow.add(taker.getId());
                        AutismClientAddon.LOG.info("[AI] barter: a baby piglin took the ingot ({} stolen, {} bartered)", this.ingotsStolen, this.barters);
                        if (++this.babyStealsInRow >= 4 && this.barters == 0) {
                           return this.fail(AiFailure.TARGET_GONE, "babies", "Baby piglins took " + this.ingotsStolen + " ingots here; barter elsewhere");
                        }

                        this.piglin = null;
                        this.stage = AiBarter.BarterStage.APPROACH;
                        this.stageTicks = 0;
                        return this.running("A baby piglin took the ingot; trying a piglin away from babies");
                     } else if (++this.stageTicks > 200) {
                        this.stageTicks = 0;
                        if (this.nearestItem(16.0) != null) {
                           if (++this.push >= 3) {
                              if (this.piglin != null) {
                                 this.ignoringPiglins.add(this.piglin.getId());
                              }

                              this.piglin = null;
                              this.push = 0;
                              this.stage = AiBarter.BarterStage.APPROACH;
                              return this.running("This piglin keeps ignoring the ingot (it may be admire-disabled); trying another");
                           } else {
                              this.stage = AiBarter.BarterStage.RECOVER;
                              return this.running("Recovering the ignored ingot");
                           }
                        } else {
                           this.ingotsStolen++;
                           this.piglin = null;
                           this.stage = AiBarter.BarterStage.APPROACH;
                           return this.running("The ingot vanished without a barter (a baby piglin steals gold); continuing");
                        }
                     } else {
                        return this.running("Waiting for a piglin to pick up the ingot");
                     }
                  } else {
                     this.piglinPos = this.admirer.blockPosition();
                     this.trackNewDrops();
                     if (this.admirer.isAlive() && !this.admirer.isConverting()) {
                        if (!this.admirer.getOffhandItem().is(Items.GOLD_INGOT)) {
                           this.barters++;
                           this.babyStealsInRow = 0;
                           this.stage = AiBarter.BarterStage.COLLECT;
                           this.stageTicks = 0;
                           return this.running("Barter " + this.barters + " tossed its loot; collecting");
                        } else if (++this.stageTicks > 360) {
                           this.admirer = null;
                           this.piglin = null;
                           this.stageTicks = 0;
                           this.stage = AiBarter.BarterStage.APPROACH;
                           return this.running("The admire never resolved; re-targeting");
                        } else {
                           return this.running("The piglin is examining the ingot");
                        }
                     } else {
                        this.admirer = null;
                        this.stageTicks = 0;
                        return this.running("The bartering piglin is gone; re-targeting");
                     }
                  }
               }

               AiOperation.Result recoverIngot() {
                  if (this.child == null) {
                     this.child = AiGameplay.write(
                        AiJson.objectOf("item", "minecraft:gold_ingot", "count", 1, "additional", true, "timeoutSeconds", 25)
                     );
                     return this.running("Walking back onto the thrown ingot");
                  }

                  AiOperation.Result r = this.child.tick();
                  if (!r.terminal()) {
                     return this.running("Recovering the ignored ingot");
                  }

                  this.child.cancel();
                  this.child = null;
                  if (r.status() == AiOperation.Status.SUCCEEDED) {
                     this.ingotsRecovered++;
                     this.goldSpent = Math.max(0, this.goldSpent - 1);
                  }

                  this.stage = AiBarter.BarterStage.THROW;
                  this.stageTicks = 0;
                  return this.running(
                     r.status() == AiOperation.Status.SUCCEEDED ? "Ingot recovered; re-throwing" : "The ingot is gone; continuing"
                  );
               }

               AiOperation.Result collectDrops() {
                  this.trackNewDrops();
                  if (this.child != null) {
                     AiOperation.Result r = this.child.tick();
                     if (!r.terminal()) {
                        return this.running("Collecting barter drops");
                     }

                     this.child.cancel();
                     this.child = null;
                     if (r.status() != AiOperation.Status.SUCCEEDED && this.pickupItem != null) {
                        this.pickupFailures.merge(this.pickupItem, 1, Integer::sum);
                     }

                     this.pickupItem = null;
                     return this.running("Drop collection leg " + r.status().name().toLowerCase(Locale.ROOT));
                  } else {
                     String next = null;

                     for (String itemId : this.dropBaseline.keySet()) {
                        if (this.pickupFailures.getOrDefault(itemId, 0) < 2 && this.groundCount(itemId) > 0) {
                           next = itemId;
                           break;
                        }
                     }

                     if (next != null) {
                        this.pickupItem = next;
                        int onGround = this.groundCount(next);
                        this.child = AiGameplay.write(AiJson.objectOf("item", next, "count", onGround, "additional", true, "timeoutSeconds", 25));
                        return this.running("Collecting " + onGround + " " + next);
                     }

                     for (Entry<String, Integer> entry : this.dropBaseline.entrySet()) {
                        int gained = AiGameState.countInventoryItem(entry.getKey()) - entry.getValue();
                        if (gained > 0) {
                           this.dropsGained.merge(entry.getKey(), gained, Integer::sum);
                        }

                        int left = this.groundCount(entry.getKey());
                        if (left > 0) {
                           this.uncollected.add(entry.getKey() + " x" + left);
                        }
                     }

                     this.dropBaseline.clear();
                     this.pickupFailures.clear();
                     this.admirer = null;
                     this.piglin = null;
                     this.stage = AiBarter.BarterStage.APPROACH;
                     this.stageTicks = 0;
                     return this.running("Barter drops collected; continuing");
                  }
               }

               AiOperation.Result tickGuard() {
                  if (this.guardOp != null) {
                     AiOperation.Result result = this.guardOp.tick();
                     if (!result.terminal()) {
                        return this.running("Interrupted: " + this.interruptReason + " - " + result.message());
                     }

                     this.guardOp.cancel();
                     this.guardOp = null;
                     return this.running("Resuming after " + this.interruptReason);
                  } else {
                     AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
                     AiGuard.GuardPolicy policy = mc.level.dimension() != Level.OVERWORLD ? this.guardPolicy.withoutFlee() : this.guardPolicy;
                     AiOperation action = AiGuard.createResponseOperation(policy, vitals, this.secondsLeft());
                     if (action == null) {
                        return null;
                     }

                     this.guardOp = action;
                     this.interruptReason = AiGuard.decisionReason(this.guardPolicy, vitals);
                     this.interruptCount++;
                     if (this.child != null) {
                        this.child.cancel();
                        this.child = null;
                     }

                     if (this.armorOp != null) {
                        this.armorOp.cancel();
                        this.armorOp = null;
                        this.armorWarning = true;
                     }

                     return this.running("Interrupted: " + this.interruptReason);
                  }
               }

               Piglin nearestPiglin() {
                  Piglin best = null;
                  double bestDistance = 1024.0;

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof Piglin piglin) {
                        String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(piglin.getType()).toString();
                        if (AiBarter.isBarterPiglin(typeId, piglin.isBaby(), piglin.isAlive(), piglin.isConverting())
                           && !piglin.isAggressive()
                           && !this.ignoringPiglins.contains(piglin.getId())) {
                           double distance = piglin.distanceToSqr(mc.player)
                              + (this.babyPiglinNear(piglin.position(), 9.0) ? 400.0 : 0.0)
                              + (this.lavaNear(piglin.blockPosition(), 3) ? 900.0 : 0.0);
                           boolean hoglin = false;

                           for (Entity other : mc.level.entitiesForRendering()) {
                              if ((other instanceof Hoglin || other instanceof Zoglin) && other.isAlive() && other.distanceToSqr(piglin) < 256.0) {
                                 hoglin = true;
                                 break;
                              }
                           }

                           if (!hoglin && distance < bestDistance) {
                              bestDistance = distance;
                              best = piglin;
                           }
                        }
                     }
                  }

                  return best;
               }

               boolean lavaNear(BlockPos at, int radius) {
                  for (int dx = -radius; dx <= radius; dx++) {
                     for (int dz = -radius; dz <= radius; dz++) {
                        for (int dy = -3; dy <= 1; dy++) {
                           if (mc.level.getFluidState(at.offset(dx, dy, dz)).is(FluidTags.LAVA)) {
                              return true;
                           }
                        }
                     }
                  }

                  return false;
               }

               boolean babyPiglinNear(Vec3 point, double radius) {
                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof Piglin piglin
                        && piglin.isAlive()
                        && piglin.isBaby()
                        && piglin.getOffhandItem().isEmpty()
                        && piglin.position().distanceToSqr(point) <= radius * radius) {
                        return true;
                     }
                  }

                  return false;
               }

               Piglin ingotTaker() {
                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof Piglin piglin
                        && piglin.isAlive()
                        && !this.holdersBeforeThrow.contains(piglin.getId())
                        && piglin.getOffhandItem().is(Items.GOLD_INGOT)
                        && piglin.distanceToSqr(mc.player) < 576.0) {
                        return piglin;
                     }
                  }

                  return null;
               }

               void releaseMovementKeys() {
                  if (mc.options != null) {
                     mc.options.keyUp.setDown(false);
                     mc.options.keyDown.setDown(false);
                     mc.options.keyLeft.setDown(false);
                     mc.options.keyRight.setDown(false);
                     mc.options.keyJump.setDown(false);
                     mc.options.keySprint.setDown(false);
                  }
               }

               Piglin findAdmiringPiglin() {
                  Piglin best = null;
                  double bestDistance = 576.0;

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof Piglin piglin
                        && AiBarter.isBarterPiglin("minecraft:piglin", piglin.isBaby(), piglin.isAlive(), piglin.isConverting())
                        && piglin.getOffhandItem().is(Items.GOLD_INGOT)) {
                        double distance = piglin.distanceToSqr(mc.player);
                        if (distance < bestDistance) {
                           bestDistance = distance;
                           best = piglin;
                        }
                     }
                  }

                  return best;
               }

               ItemEntity nearestItem(double radius) {
                  ItemEntity best = null;
                  double bestDistance = radius * radius;

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof ItemEntity drop && drop.isAlive() && drop.getItem().is(Items.GOLD_INGOT)) {
                        double distance = drop.distanceToSqr(mc.player);
                        if (distance < bestDistance) {
                           bestDistance = distance;
                           best = drop;
                        }
                     }
                  }

                  return best;
               }

               void snapshotDrops() {
                  this.preexistingDrops.clear();
                  Vec3 center = this.piglin.position();

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof ItemEntity drop && drop.isAlive() && drop.position().distanceToSqr(center) <= 576.0) {
                        this.preexistingDrops.add(drop.getId());
                     }
                  }
               }

               void trackNewDrops() {
                  if (this.piglinPos != null) {
                     Vec3 center = Vec3.atCenterOf(this.piglinPos);

                     for (Entity entity : mc.level.entitiesForRendering()) {
                        if (entity instanceof ItemEntity drop && drop.isAlive() && !this.preexistingDrops.contains(drop.getId())) {
                           String itemId = BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).toString();
                           if (!"minecraft:gold_ingot".equals(itemId) && !(drop.position().distanceToSqr(center) > 144.0)) {
                              this.dropBaseline.putIfAbsent(itemId, AiGameState.countInventoryItem(itemId));
                           }
                        }
                     }
                  }
               }

               int groundCount(String itemId) {
                  int total = 0;
                  Vec3 center = this.piglinPos != null ? Vec3.atCenterOf(this.piglinPos) : mc.player.position();

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof ItemEntity drop
                        && drop.isAlive()
                        && BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).toString().equals(itemId)
                        && drop.position().distanceToSqr(center) <= 144.0) {
                        total += drop.getItem().getCount();
                     }
                  }

                  return total;
               }

               boolean wearsGoldArmor() {
                  List<String> armor = new ArrayList<>(4);

                  for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                     ItemStack stack = mc.player.getItemBySlot(slot);
                     armor.add(stack.isEmpty() ? "minecraft:air" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                  }

                  return AiDimension.init(armor);
               }

               int goldSlot() {
                  for (int slot = 0; slot < 36; slot++) {
                     if (mc.player.getInventory().getItem(slot).is(Items.GOLD_INGOT)) {
                        return slot;
                     }
                  }

                  return -1;
               }

               int screenSlot(int inventorySlot) {
                  int handler = autismclient.util.InventorySlotHelper.toScreenSlot(mc, inventorySlot);
                  if (handler < 0) {
                     throw new IllegalStateException("Inventory slot " + inventorySlot + " has no menu slot");
                  } else {
                     return handler;
                  }
               }

               int pearlsGained() {
                  return AiGameState.countInventoryItem("minecraft:ender_pearl") - this.pearlsAtStart;
               }

               int secondsLeft() {
                  return (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L);
               }

               JsonObject dropsJson() {
                  JsonObject drops = new JsonObject();

                  for (Entry<String, Integer> entry : this.dropsGained.entrySet()) {
                     drops.addProperty(entry.getKey(), entry.getValue());
                  }

                  int pearlsGained = this.pearlsGained();
                  JsonObject out = AiJson.objectOf(
                     "bartered",
                     this.barters,
                     "thrown",
                     this.ingotsThrown,
                     "recovered",
                     this.ingotsRecovered,
                     "stolen",
                     this.ingotsStolen,
                     "goldSpent",
                     this.goldSpent,
                     "goldBudget",
                     this.goldToTrade,
                     "goldLeft",
                     AiGameState.countMainInventoryItem("minecraft:gold_ingot"),
                     "pearlsGained",
                     pearlsGained,
                     "pearlsTarget",
                     pearlsTarget,
                     "phase",
                     this.stage.name().toLowerCase(Locale.ROOT)
                  );
                  out.add("dropsByItem", drops);
                  if (pearlsTarget > 0) {
                     out.addProperty("pearlsMet", pearlsGained >= pearlsTarget);
                  }

                  if (this.armorWarning) {
                     out.addProperty("warning", "No gold armor worn; piglins stay hostile and may attack between barters");
                  }

                  if (!this.uncollected.isEmpty()) {
                     out.add("uncollected", AiJson.GSON.toJsonTree(this.uncollected));
                  }

                  Piglin piglin = this.admirer != null ? this.admirer : this.piglin;
                  if (piglin != null) {
                     out.add(
                        "piglin",
                        AiJson.objectOf("entityId", piglin.getId(), "x", piglin.getBlockX(), "y", piglin.getBlockY(), "z", piglin.getBlockZ())
                     );
                  }

                  out.add("survival", AiGuard.statusToJson(AiGuard.sampleStatus(), this.interruptCount, this.interruptReason));
                  return out;
               }

               AiOperation.Result running(String message) {
                  return new AiOperation.Result(
                     AiOperation.Status.RUNNING, message + " (" + this.barters + " bartered)", this.dropsJson().toString(), List.of()
                  );
               }

               AiOperation.Result finish(AiOperation.Status status, String message) {
                  JsonObject data = this.dropsJson();
                  this.cancel();
                  return new AiOperation.Result(status, message, data.toString(), List.of());
               }

               AiOperation.Result fail(AiFailure code, String reason, String message) {
                  JsonObject data = this.dropsJson();
                  data.addProperty("reason", reason);
                  this.cancel();
                  return AiOperation.Result.failed(code, message, data.toString());
               }

               @Override
               public void cancel() {
                  if (!this.cancelled) {
                     this.cancelled = true;
                     this.releaseMovementKeys();
                     this.aim.clear();
                     if (this.child != null) {
                        this.child.cancel();
                        this.child = null;
                     }

                     if (this.guardOp != null) {
                        this.guardOp.cancel();
                        this.guardOp = null;
                     }

                     if (this.armorOp != null) {
                        this.armorOp.cancel();
                        this.armorOp = null;
                     }

                     if (mc.player != null && mc.player.getInventory().getSelectedSlot() != this.originalSlot) {
                        mc.player.getInventory().setSelectedSlot(this.originalSlot);
                     }
                  }
               }
            };
         }
      } else {
         throw new IllegalArgumentException("Bartering requires a connected living player");
      }
   }

   private enum BarterStage {
      APPROACH,
      THROW,
      AWAIT,
      RECOVER,
      COLLECT;
   }

   enum BarterDecision {
      CONTINUE,
      PEARLS_MET,
      GOLD_OUT,
      TIMED_OUT;
   }
}
