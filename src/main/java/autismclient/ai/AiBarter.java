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
   private static final int init_I = 30;
   private static final int load_I = 40;
   private static final int read_I = 200;
   private static final int write_I = 360;
   private static final double read_D = 12.0;
   private static final double write_D = 24.0;
   private static final int update_I = 3;
   private static final int sync_I = 2;
   private static final int flush_I = 25;
   private static final double update_D = 9.0;
   private static final double sync_D = 400.0;
   private static final double flush_D = 900.0;
   private static final int close = 60;
   private static final int open = 4;

   private AiBarter() {
   }

   static AiBarter.Inner_StreamUtil init(int pearlsTarget, int pearlsGained, int goldCarried, int goldSpent, int goldBudget, boolean pastDeadline) {
      if (pearlsTarget > 0 && pearlsGained >= pearlsTarget) {
         return AiBarter.Inner_StreamUtil.PEARLS_MET;
      } else if (goldCarried > 0 && goldSpent < goldBudget) {
         return pastDeadline ? AiBarter.Inner_StreamUtil.TIMED_OUT : AiBarter.Inner_StreamUtil.CONTINUE;
      } else {
         return AiBarter.Inner_StreamUtil.GOLD_OUT;
      }
   }

   static int init(int goldArg, int carried) {
      return goldArg > 0 ? Math.min(goldArg, carried) : carried;
   }

   static boolean init(String typeId, boolean baby, boolean alive, boolean converting) {
      return alive && "minecraft:piglin".equals(typeId) && !baby && !converting;
   }

   static AiOperation init(JsonObject args) {
      return AiBarterBatch.init(args);
   }

   static AiOperation load(JsonObject args) {
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
               final Set<Integer> init_Set = new LinkedHashSet<>();
               final Set<Integer> load_Set = new LinkedHashSet<>();
               final Map<String, Integer> dropsGained = new LinkedHashMap<>();
               final Map<String, Integer> load_Map = new LinkedHashMap<>();
               final Map<String, Integer> read_Map = new LinkedHashMap<>();
               final List<String> uncollected = new ArrayList<>();
               AiOperation child;
               AiOperation load_AiOperation;
               AiOperation read_AiOperation;
               String interruptReason = "";
               Piglin piglin;
               Piglin admirer;
               BlockPos piglinPos;
               AiBarter.Inner_DataUtil stage = AiBarter.Inner_DataUtil.APPROACH;
               boolean init_Z;
               boolean load_Z;
               boolean read_Z;
               int write;
               int update;
               int sync;
               int flush;
               int close;
               int open;
               int poll;
               int push;
               int pop;
               int tick;
               int run;
               int get;
               final Set<Integer> read_Set = new LinkedHashSet<>();
               long load_J = -1L;
               String load_Str;

               @Override
               public AiOperation.Result tick() {
                  if (!this.read_Z && mc.player != null && mc.level != null) {
                     if (mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                        return this.init(
                           AiFailure.LOW_HEALTH,
                           "low_health",
                           "Low health; bartering stopped after "
                              + this.open
                              + " barter(s); recovery ops allowed at low health: game:fish, game:farm (harvest only), game:collect_drops, game:eat, game:sleep, game:flee, game:obtain <food> with allowCombat:false, and game:goto while no hostile is within 24 blocks"
                        );
                     }

                     int pearlsGained = this.pearlsGained();
                     AiBarter.Inner_StreamUtil decision = AiBarter.init(
                        pearlsTarget, pearlsGained, AiGameState.countMainInventoryItem("minecraft:gold_ingot"), this.poll, this.goldToTrade, System.nanoTime() > this.deadline
                     );
                     if (decision == AiBarter.Inner_StreamUtil.GOLD_OUT
                        && (
                           this.stage == AiBarter.Inner_DataUtil.AWAIT
                              || this.stage == AiBarter.Inner_DataUtil.RECOVER
                              || this.stage == AiBarter.Inner_DataUtil.COLLECT
                              || this.push < 3 && this.nearestItem(16.0) != null
                        )) {
                        decision = AiBarter.Inner_StreamUtil.CONTINUE;
                     }

                     switch (decision) {
                        case PEARLS_MET:
                           String detail = pearlsGained
                              + "/"
                              + pearlsTarget
                              + " ender pearls after "
                              + this.open
                              + " barter(s), gold left "
                              + AiGameState.countMainInventoryItem("minecraft:gold_ingot");
                           AiRunTrace.recordGlobalSplit("pearls_met", detail);
                           return this.finish(AiOperation.Status.SUCCEEDED, "Pearl target met: " + detail);
                        case GOLD_OUT:
                           if (this.open > 0) {
                              return this.finish(
                                 AiOperation.Status.SUCCEEDED,
                                 "Out of gold after " + this.open + " barter(s); pearls " + pearlsGained + (pearlsTarget > 0 ? "/" + pearlsTarget : "")
                              );
                           } else {
                              if (this.sync == 0) {
                                 return this.init(AiFailure.MISSING_MATERIALS, "no_gold", "NO_GOLD: the carried gold is gone before the first barter");
                              }

                              return this.init(
                                 AiFailure.TARGET_GONE, "gold_lost", "Every thrown ingot was stolen or lost without one barter (baby piglins steal gold; brutes never barter)"
                              );
                           }
                        case TIMED_OUT:
                           return this.finish(
                              AiOperation.Status.TIMED_OUT,
                              "Barter deadline reached after "
                                 + this.open
                                 + " barter(s); pearls "
                                 + pearlsGained
                                 + (pearlsTarget > 0 ? "/" + pearlsTarget : "")
                           );
                        default:
                           AiOperation.Result guarded = this.sync();
                           if (guarded != null) {
                              return guarded;
                           } else if (this.read_AiOperation != null) {
                              AiOperation.Result r = this.read_AiOperation.tick();
                              if (!r.terminal()) {
                                 return this.running("Equipping a gold armor piece for piglin neutrality");
                              } else {
                                 this.read_AiOperation.cancel();
                                 this.read_AiOperation = null;
                                 if (r.status() != AiOperation.Status.SUCCEEDED) {
                                    this.load_Z = true;
                                 }

                                 return this.running(
                                    r.status() == AiOperation.Status.SUCCEEDED
                                       ? "Gold armor worn"
                                       : "No gold armor available; piglins stay hostile (warning only)"
                                 );
                              }
                           } else {
                              if (!this.init_Z) {
                                 this.init_Z = true;
                                 if (!this.wearsGoldArmor()) {
                                    this.read_AiOperation = AiEquip.init(AiJson.objectOf("what", "armor", "prefer", "golden_*"));
                                    return this.running("Equipping a gold armor piece for piglin neutrality");
                                 }
                              }

                              if (this.stage == AiBarter.Inner_DataUtil.APPROACH) {
                                 return this.init_AiOperation_Result_6();
                              } else if (this.stage == AiBarter.Inner_DataUtil.THROW) {
                                 return this.load_AiOperation_Result_5();
                              } else if (this.stage == AiBarter.Inner_DataUtil.AWAIT) {
                                 return this.read_AiOperation_Result_3();
                              } else {
                                 return this.stage == AiBarter.Inner_DataUtil.RECOVER ? this.write() : this.update();
                              }
                           }
                     }
                  } else {
                     return this.finish(AiOperation.Status.CANCELLED, "Barter cancelled");
                  }
               }

               AiOperation.Result init_AiOperation_Result_6() {
                  if (this.piglin == null
                     || !this.piglin.isAlive()
                     || this.piglin.isBaby()
                     || this.piglin.isConverting()
                     || this.init_Set.contains(this.piglin.getId())) {
                     this.piglin = this.nearestPiglin();
                     this.push = 0;
                     if (this.piglin == null) {
                        if (this.open > 0) {
                           return this.finish(
                              AiOperation.Status.SUCCEEDED, "No more adult piglins nearby after " + this.open + " barter(s)"
                           );
                        }

                        if (!this.init_Set.isEmpty()) {
                           return this.init(AiFailure.UNREACHABLE, "unreachable", "No route to any adult piglin nearby");
                        }

                        return this.init(
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
                           this.init_Set.add(this.piglin.getId());
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
                     this.stage = AiBarter.Inner_DataUtil.THROW;
                     this.write = 0;
                     return this.running("In toss range of the piglin");
                  }
               }

               AiOperation.Result load_AiOperation_Result_5() {
                  if (this.piglin != null && this.piglin.isAlive()) {
                     if (mc.player.containerMenu != mc.player.inventoryMenu) {
                        return this.finish(AiOperation.Status.FAILED, "Close the open container before bartering");
                     }

                     if (!mc.player.containerMenu.getCarried().isEmpty()) {
                        return this.finish(AiOperation.Status.FAILED, "Clear the cursor stack before bartering");
                     }

                     if (this.load_J >= 0L) {
                        int now = AiGameState.countMainInventoryItem("minecraft:gold_ingot");
                        if (now < this.run) {
                           int spent = this.run - now;
                           this.poll += spent;
                           this.sync += spent;
                           this.pop = 0;
                           this.load_J = -1L;
                           this.stage = AiBarter.Inner_DataUtil.AWAIT;
                           this.write = 0;
                           return this.running("Ingot thrown; waiting for the piglin to pick it up");
                        } else if (AiServerEvidence.inventoryRevision() > this.load_J) {
                           this.load_J = -1L;
                           this.write = 0;
                           return ++this.pop >= 3
                              ? this.finish(
                                 AiOperation.Status.UNCERTAIN, "The ingot throw was applied but no ingot left the inventory; reobserve before repeating"
                              )
                              : this.running("The throw was refused; retrying");
                        } else {
                           return ++this.write > 40
                              ? this.finish(
                                 AiOperation.Status.UNCERTAIN, "The ingot throw was not confirmed by the server; reobserve before repeating"
                              )
                              : this.running("Waiting for the throw to apply");
                        }
                     } else {
                        Piglin admirer = this.findAdmiringPiglin();
                        if (admirer != null) {
                           this.admirer = admirer;
                           this.stage = AiBarter.Inner_DataUtil.AWAIT;
                           this.write = 0;
                           return this.running("A piglin is already admiring an ingot");
                        }

                        this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(this.piglin));
                        this.aim.tick();
                        if (!this.aim.isSettled() && ++this.write <= 30) {
                           return this.running("Turning toward the piglin");
                        }

                        this.write = 0;
                        int slot = this.goldSlot();
                        if (slot < 0) {
                           if (this.push < 3 && this.nearestItem(16.0) != null) {
                              this.push++;
                              this.stage = AiBarter.Inner_DataUtil.RECOVER;
                              return this.running("Recovering the thrown ingot");
                           } else {
                              return this.running("No ingot in the bag; re-evaluating");
                           }
                        } else {
                           Vec3 toss = mc.player.position().add(this.piglin.position().subtract(mc.player.position()).scale(0.6));
                           if (this.init_Z_Vec3_D(toss, 9.0) && ++this.tick <= 60) {
                              return this.running("A baby piglin is near the toss; waiting");
                           }

                           this.tick = 0;
                           this.read_Set.clear();

                           for (Entity entity : mc.level.entitiesForRendering()) {
                              if (entity instanceof Piglin piglin && piglin.getOffhandItem().is(Items.GOLD_INGOT)) {
                                 this.read_Set.add(piglin.getId());
                              }
                           }

                           this.load_V_3();
                           this.run = AiGameState.countMainInventoryItem("minecraft:gold_ingot");
                           this.load_J = AiServerEvidence.inventoryRevision();
                           AiContainerTransactions.sendPredictedClick(mc.player.containerMenu.containerId, this.screenSlot(slot), 0, ContainerInput.THROW);
                           return this.running("Throwing one gold ingot at the piglin");
                        }
                     }
                  } else {
                     this.piglin = null;
                     this.stage = AiBarter.Inner_DataUtil.APPROACH;
                     return this.running("Piglin gone; re-targeting");
                  }
               }

               AiOperation.Result read_AiOperation_Result_3() {
                  if (this.admirer == null) {
                     this.admirer = this.findAdmiringPiglin();
                     if (this.admirer != null) {
                        this.write = 0;
                     }
                  }

                  if (this.admirer == null) {
                     Piglin taker = this.load_Piglin();
                     if (taker != null && taker.isBaby()) {
                        this.close++;
                        this.read_Set.add(taker.getId());
                        AutismClientAddon.LOG.info("[AI] barter: a baby piglin took the ingot ({} stolen, {} bartered)", this.close, this.open);
                        if (++this.get >= 4 && this.open == 0) {
                           return this.init(AiFailure.TARGET_GONE, "babies", "Baby piglins took " + this.close + " ingots here; barter elsewhere");
                        }

                        this.piglin = null;
                        this.stage = AiBarter.Inner_DataUtil.APPROACH;
                        this.write = 0;
                        return this.running("A baby piglin took the ingot; trying a piglin away from babies");
                     } else if (++this.write > 200) {
                        this.write = 0;
                        if (this.nearestItem(16.0) != null) {
                           if (++this.push >= 3) {
                              if (this.piglin != null) {
                                 this.init_Set.add(this.piglin.getId());
                              }

                              this.piglin = null;
                              this.push = 0;
                              this.stage = AiBarter.Inner_DataUtil.APPROACH;
                              return this.running("This piglin keeps ignoring the ingot (it may be admire-disabled); trying another");
                           } else {
                              this.stage = AiBarter.Inner_DataUtil.RECOVER;
                              return this.running("Recovering the ignored ingot");
                           }
                        } else {
                           this.close++;
                           this.piglin = null;
                           this.stage = AiBarter.Inner_DataUtil.APPROACH;
                           return this.running("The ingot vanished without a barter (a baby piglin steals gold); continuing");
                        }
                     } else {
                        return this.running("Waiting for a piglin to pick up the ingot");
                     }
                  } else {
                     this.piglinPos = this.admirer.blockPosition();
                     this.read_V();
                     if (this.admirer.isAlive() && !this.admirer.isConverting()) {
                        if (!this.admirer.getOffhandItem().is(Items.GOLD_INGOT)) {
                           this.open++;
                           this.get = 0;
                           this.stage = AiBarter.Inner_DataUtil.COLLECT;
                           this.write = 0;
                           return this.running("Barter " + this.open + " tossed its loot; collecting");
                        } else if (++this.write > 360) {
                           this.admirer = null;
                           this.piglin = null;
                           this.write = 0;
                           this.stage = AiBarter.Inner_DataUtil.APPROACH;
                           return this.running("The admire never resolved; re-targeting");
                        } else {
                           return this.running("The piglin is examining the ingot");
                        }
                     } else {
                        this.admirer = null;
                        this.write = 0;
                        return this.running("The bartering piglin is gone; re-targeting");
                     }
                  }
               }

               AiOperation.Result write() {
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
                     this.flush++;
                     this.poll = Math.max(0, this.poll - 1);
                  }

                  this.stage = AiBarter.Inner_DataUtil.THROW;
                  this.write = 0;
                  return this.running(
                     r.status() == AiOperation.Status.SUCCEEDED ? "Ingot recovered; re-throwing" : "The ingot is gone; continuing"
                  );
               }

               AiOperation.Result update() {
                  this.read_V();
                  if (this.child != null) {
                     AiOperation.Result r = this.child.tick();
                     if (!r.terminal()) {
                        return this.running("Collecting barter drops");
                     }

                     this.child.cancel();
                     this.child = null;
                     if (r.status() != AiOperation.Status.SUCCEEDED && this.load_Str != null) {
                        this.read_Map.merge(this.load_Str, 1, Integer::sum);
                     }

                     this.load_Str = null;
                     return this.running("Drop collection leg " + r.status().name().toLowerCase(Locale.ROOT));
                  } else {
                     String next = null;

                     for (String itemId : this.load_Map.keySet()) {
                        if (this.read_Map.getOrDefault(itemId, 0) < 2 && this.groundCount(itemId) > 0) {
                           next = itemId;
                           break;
                        }
                     }

                     if (next != null) {
                        this.load_Str = next;
                        int onGround = this.groundCount(next);
                        this.child = AiGameplay.write(AiJson.objectOf("item", next, "count", onGround, "additional", true, "timeoutSeconds", 25));
                        return this.running("Collecting " + onGround + " " + next);
                     }

                     for (Entry<String, Integer> entry : this.load_Map.entrySet()) {
                        int gained = AiGameState.countInventoryItem(entry.getKey()) - entry.getValue();
                        if (gained > 0) {
                           this.dropsGained.merge(entry.getKey(), gained, Integer::sum);
                        }

                        int left = this.groundCount(entry.getKey());
                        if (left > 0) {
                           this.uncollected.add(entry.getKey() + " x" + left);
                        }
                     }

                     this.load_Map.clear();
                     this.read_Map.clear();
                     this.admirer = null;
                     this.piglin = null;
                     this.stage = AiBarter.Inner_DataUtil.APPROACH;
                     this.write = 0;
                     return this.running("Barter drops collected; continuing");
                  }
               }

               AiOperation.Result sync() {
                  if (this.load_AiOperation != null) {
                     AiOperation.Result result = this.load_AiOperation.tick();
                     if (!result.terminal()) {
                        return this.running("Interrupted: " + this.interruptReason + " - " + result.message());
                     }

                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                     return this.running("Resuming after " + this.interruptReason);
                  } else {
                     AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
                     AiGuard.GuardPolicy policy = mc.level.dimension() != Level.OVERWORLD ? this.guardPolicy.withoutFlee() : this.guardPolicy;
                     AiOperation action = AiGuard.createResponseOperation(policy, vitals, this.secondsLeft());
                     if (action == null) {
                        return null;
                     }

                     this.load_AiOperation = action;
                     this.interruptReason = AiGuard.decisionReason(this.guardPolicy, vitals);
                     this.update++;
                     if (this.child != null) {
                        this.child.cancel();
                        this.child = null;
                     }

                     if (this.read_AiOperation != null) {
                        this.read_AiOperation.cancel();
                        this.read_AiOperation = null;
                        this.load_Z = true;
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
                        if (AiBarter.init(typeId, piglin.isBaby(), piglin.isAlive(), piglin.isConverting())
                           && !piglin.isAggressive()
                           && !this.init_Set.contains(piglin.getId())) {
                           double distance = piglin.distanceToSqr(mc.player)
                              + (this.init_Z_Vec3_D(piglin.position(), 9.0) ? 400.0 : 0.0)
                              + (this.init_Z_BlockPos_I(piglin.blockPosition(), 3) ? 900.0 : 0.0);
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

               boolean init_Z_BlockPos_I(BlockPos at, int radius) {
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

               boolean init_Z_Vec3_D(Vec3 point, double radius) {
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

               Piglin load_Piglin() {
                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof Piglin piglin
                        && piglin.isAlive()
                        && !this.read_Set.contains(piglin.getId())
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
                        && AiBarter.init("minecraft:piglin", piglin.isBaby(), piglin.isAlive(), piglin.isConverting())
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

               void load_V_3() {
                  this.load_Set.clear();
                  Vec3 center = this.piglin.position();

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof ItemEntity drop && drop.isAlive() && drop.position().distanceToSqr(center) <= 576.0) {
                        this.load_Set.add(drop.getId());
                     }
                  }
               }

               void read_V() {
                  if (this.piglinPos != null) {
                     Vec3 center = Vec3.atCenterOf(this.piglinPos);

                     for (Entity entity : mc.level.entitiesForRendering()) {
                        if (entity instanceof ItemEntity drop && drop.isAlive() && !this.load_Set.contains(drop.getId())) {
                           String itemId = BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).toString();
                           if (!"minecraft:gold_ingot".equals(itemId) && !(drop.position().distanceToSqr(center) > 144.0)) {
                              this.load_Map.putIfAbsent(itemId, AiGameState.countInventoryItem(itemId));
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
                     this.open,
                     "thrown",
                     this.sync,
                     "recovered",
                     this.flush,
                     "stolen",
                     this.close,
                     "goldSpent",
                     this.poll,
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

                  if (this.load_Z) {
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

                  out.add("survival", AiGuard.statusToJson(AiGuard.sampleStatus(), this.update, this.interruptReason));
                  return out;
               }

               AiOperation.Result running(String message) {
                  return new AiOperation.Result(
                     AiOperation.Status.RUNNING, message + " (" + this.open + " bartered)", this.dropsJson().toString(), List.of()
                  );
               }

               AiOperation.Result finish(AiOperation.Status status, String message) {
                  JsonObject data = this.dropsJson();
                  this.cancel();
                  return new AiOperation.Result(status, message, data.toString(), List.of());
               }

               AiOperation.Result init(AiFailure code, String reason, String message) {
                  JsonObject data = this.dropsJson();
                  data.addProperty("reason", reason);
                  this.cancel();
                  return AiOperation.Result.failed(code, message, data.toString());
               }

               @Override
               public void cancel() {
                  if (!this.read_Z) {
                     this.read_Z = true;
                     this.releaseMovementKeys();
                     this.aim.clear();
                     if (this.child != null) {
                        this.child.cancel();
                        this.child = null;
                     }

                     if (this.load_AiOperation != null) {
                        this.load_AiOperation.cancel();
                        this.load_AiOperation = null;
                     }

                     if (this.read_AiOperation != null) {
                        this.read_AiOperation.cancel();
                        this.read_AiOperation = null;
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

   private enum Inner_DataUtil {
      APPROACH,
      THROW,
      AWAIT,
      RECOVER,
      COLLECT;
   }

   enum Inner_StreamUtil {
      CONTINUE,
      PEARLS_MET,
      GOLD_OUT,
      TIMED_OUT;
   }
}
