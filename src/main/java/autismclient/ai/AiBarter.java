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
   static final String init_Str = "minecraft:piglin";
   static final String load_Str = "minecraft:gold_ingot";
   static final String read_Str = "minecraft:ender_pearl";
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
         return AiBarter.Inner_StreamUtil.load;
      } else if (goldCarried > 0 && goldSpent < goldBudget) {
         return pastDeadline ? AiBarter.Inner_StreamUtil.write : AiBarter.Inner_StreamUtil.init;
      } else {
         return AiBarter.Inner_StreamUtil.read;
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
         final int pearlsTarget = Math.max(0, Math.min(64, AiJson.init_I_JsonObject(args, "pearls", 0)));
         final int goldArg = Math.max(0, Math.min(2304, AiJson.init_I_JsonObject(args, "gold", 0)));
         final int timeoutSeconds = Math.max(10, Math.min(900, AiJson.init_I_JsonObject(args, "timeoutSeconds", 300)));
         if (AiGameState.load("minecraft:gold_ingot") <= 0) {
            JsonObject missing = new JsonObject();
            missing.addProperty("minecraft:gold_ingot", 1);
            return AiGameOperations.init(
               AiOperation.Status.FAILED,
               AiFailure.pop,
               "NO_GOLD: no gold ingots in the inventory to barter with; smelt or mine gold first",
               AiJson.init_JsonObject_ObjArr("reason", "no_gold", "missing", missing)
            );
         } else {
            return new AiOperation() {
               final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
               final int init_I = AiBarter.init(goldArg, AiGameState.load("minecraft:gold_ingot"));
               final int load_I = AiGameState.init_I_Str("minecraft:ender_pearl");
               final int read_I = mc.player.getInventory().getSelectedSlot();
               final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
               final AiGuard.Inner_CachePool init_X_CachePool = AiGuard.Inner_CachePool.standard();
               final Set<Integer> init_Set = new LinkedHashSet<>();
               final Set<Integer> load_Set = new LinkedHashSet<>();
               final Map<String, Integer> init_Map = new LinkedHashMap<>();
               final Map<String, Integer> load_Map = new LinkedHashMap<>();
               final Map<String, Integer> read_Map = new LinkedHashMap<>();
               final List<String> init_List = new ArrayList<>();
               AiOperation init_AiOperation;
               AiOperation load_AiOperation;
               AiOperation read_AiOperation;
               String init_Str = "";
               Piglin init_Piglin;
               Piglin load_Piglin;
               BlockPos init_BlockPos;
               AiBarter.Inner_DataUtil init_NetHelper_DataUtil = AiBarter.Inner_DataUtil.init;
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
                     if (mc.player.getHealth() <= 4.0F && !AiGuard.close()) {
                        return this.init(
                           AiFailure.start,
                           "low_health",
                           "Low health; bartering stopped after "
                              + this.open
                              + " barter(s); recovery ops allowed at low health: game:fish, game:farm (harvest only), game:collect_drops, game:eat, game:sleep, game:flee, game:obtain <food> with allowCombat:false, and game:goto while no hostile is within 24 blocks"
                        );
                     }

                     int pearlsGained = this.load_I_4();
                     AiBarter.Inner_StreamUtil decision = AiBarter.init(
                        pearlsTarget, pearlsGained, AiGameState.load("minecraft:gold_ingot"), this.poll, this.init_I, System.nanoTime() > this.init_J
                     );
                     if (decision == AiBarter.Inner_StreamUtil.read
                        && (
                           this.init_NetHelper_DataUtil == AiBarter.Inner_DataUtil.read
                              || this.init_NetHelper_DataUtil == AiBarter.Inner_DataUtil.write
                              || this.init_NetHelper_DataUtil == AiBarter.Inner_DataUtil.update
                              || this.push < 3 && this.init_ItemEntity_D(16.0) != null
                        )) {
                        decision = AiBarter.Inner_StreamUtil.init;
                     }

                     switch (decision) {
                        case load:
                           String detail = pearlsGained
                              + "/"
                              + pearlsTarget
                              + " ender pearls after "
                              + this.open
                              + " barter(s), gold left "
                              + AiGameState.load("minecraft:gold_ingot");
                           AiRunTrace.init_V_Str_Str("pearls_met", detail);
                           return this.init_AiOperation_Result_AiOperation_Status_Str_2(AiOperation.Status.SUCCEEDED, "Pearl target met: " + detail);
                        case read:
                           if (this.open > 0) {
                              return this.init_AiOperation_Result_AiOperation_Status_Str_2(
                                 AiOperation.Status.SUCCEEDED,
                                 "Out of gold after " + this.open + " barter(s); pearls " + pearlsGained + (pearlsTarget > 0 ? "/" + pearlsTarget : "")
                              );
                           } else {
                              if (this.sync == 0) {
                                 return this.init(AiFailure.pop, "no_gold", "NO_GOLD: the carried gold is gone before the first barter");
                              }

                              return this.init(
                                 AiFailure.add, "gold_lost", "Every thrown ingot was stolen or lost without one barter (baby piglins steal gold; brutes never barter)"
                              );
                           }
                        case write:
                           return this.init_AiOperation_Result_AiOperation_Status_Str_2(
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
                                 return this.init_AiOperation_Result_Str_2("Equipping a gold armor piece for piglin neutrality");
                              } else {
                                 this.read_AiOperation.cancel();
                                 this.read_AiOperation = null;
                                 if (r.status() != AiOperation.Status.SUCCEEDED) {
                                    this.load_Z = true;
                                 }

                                 return this.init_AiOperation_Result_Str_2(
                                    r.status() == AiOperation.Status.SUCCEEDED
                                       ? "Gold armor worn"
                                       : "No gold armor available; piglins stay hostile (warning only)"
                                 );
                              }
                           } else {
                              if (!this.init_Z) {
                                 this.init_Z = true;
                                 if (!this.init_Z_5()) {
                                    this.read_AiOperation = AiEquip.init(AiJson.init_JsonObject_ObjArr("what", "armor", "prefer", "golden_*"));
                                    return this.init_AiOperation_Result_Str_2("Equipping a gold armor piece for piglin neutrality");
                                 }
                              }

                              if (this.init_NetHelper_DataUtil == AiBarter.Inner_DataUtil.init) {
                                 return this.init_AiOperation_Result_6();
                              } else if (this.init_NetHelper_DataUtil == AiBarter.Inner_DataUtil.load) {
                                 return this.load_AiOperation_Result_5();
                              } else if (this.init_NetHelper_DataUtil == AiBarter.Inner_DataUtil.read) {
                                 return this.read_AiOperation_Result_3();
                              } else {
                                 return this.init_NetHelper_DataUtil == AiBarter.Inner_DataUtil.write ? this.write() : this.update();
                              }
                           }
                     }
                  } else {
                     return this.init_AiOperation_Result_AiOperation_Status_Str_2(AiOperation.Status.CANCELLED, "Barter cancelled");
                  }
               }

               AiOperation.Result init_AiOperation_Result_6() {
                  if (this.init_Piglin == null
                     || !this.init_Piglin.isAlive()
                     || this.init_Piglin.isBaby()
                     || this.init_Piglin.isConverting()
                     || this.init_Set.contains(this.init_Piglin.getId())) {
                     this.init_Piglin = this.init_Piglin();
                     this.push = 0;
                     if (this.init_Piglin == null) {
                        if (this.open > 0) {
                           return this.init_AiOperation_Result_AiOperation_Status_Str_2(
                              AiOperation.Status.SUCCEEDED, "No more adult piglins nearby after " + this.open + " barter(s)"
                           );
                        }

                        if (!this.init_Set.isEmpty()) {
                           return this.init(AiFailure.close, "unreachable", "No route to any adult piglin nearby");
                        }

                        return this.init(
                           AiFailure.init_A, "no_piglins", "NO_PIGLINS: no adult piglin within 32 blocks (babies steal gold without bartering; brutes never barter)"
                        );
                     }
                  }

                  this.init_BlockPos = this.init_Piglin.blockPosition();
                  if (mc.player.distanceToSqr(this.init_Piglin) > 16.0) {
                     if (this.init_AiOperation == null) {
                        this.init_AiOperation = AiGoto.init_AiOperation_BlockPos(
                           this.init_Piglin.blockPosition(), 3.0, true, true, true, Math.min(60, this.read_I_2())
                        );
                     }

                     AiOperation.Result r = this.init_AiOperation.tick();
                     if (!r.terminal()) {
                        return this.init_AiOperation_Result_Str_2("Walking to the piglin");
                     } else {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        if (r.status() != AiOperation.Status.SUCCEEDED) {
                           this.init_Set.add(this.init_Piglin.getId());
                           this.init_Piglin = null;
                           return this.init_AiOperation_Result_Str_2("Piglin unreachable: " + r.message());
                        } else {
                           return this.init_AiOperation_Result_Str_2("Arrived at the piglin");
                        }
                     }
                  } else {
                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                     }

                     this.init_V_7();
                     this.init_NetHelper_DataUtil = AiBarter.Inner_DataUtil.load;
                     this.write = 0;
                     return this.init_AiOperation_Result_Str_2("In toss range of the piglin");
                  }
               }

               AiOperation.Result load_AiOperation_Result_5() {
                  if (this.init_Piglin != null && this.init_Piglin.isAlive()) {
                     if (mc.player.containerMenu != mc.player.inventoryMenu) {
                        return this.init_AiOperation_Result_AiOperation_Status_Str_2(AiOperation.Status.FAILED, "Close the open container before bartering");
                     }

                     if (!mc.player.containerMenu.getCarried().isEmpty()) {
                        return this.init_AiOperation_Result_AiOperation_Status_Str_2(AiOperation.Status.FAILED, "Clear the cursor stack before bartering");
                     }

                     if (this.load_J >= 0L) {
                        int now = AiGameState.load("minecraft:gold_ingot");
                        if (now < this.run) {
                           int spent = this.run - now;
                           this.poll += spent;
                           this.sync += spent;
                           this.pop = 0;
                           this.load_J = -1L;
                           this.init_NetHelper_DataUtil = AiBarter.Inner_DataUtil.read;
                           this.write = 0;
                           return this.init_AiOperation_Result_Str_2("Ingot thrown; waiting for the piglin to pick it up");
                        } else if (AiServerEvidence.init_J() > this.load_J) {
                           this.load_J = -1L;
                           this.write = 0;
                           return ++this.pop >= 3
                              ? this.init_AiOperation_Result_AiOperation_Status_Str_2(
                                 AiOperation.Status.UNCERTAIN, "The ingot throw was applied but no ingot left the inventory; reobserve before repeating"
                              )
                              : this.init_AiOperation_Result_Str_2("The throw was refused; retrying");
                        } else {
                           return ++this.write > 40
                              ? this.init_AiOperation_Result_AiOperation_Status_Str_2(
                                 AiOperation.Status.UNCERTAIN, "The ingot throw was not confirmed by the server; reobserve before repeating"
                              )
                              : this.init_AiOperation_Result_Str_2("Waiting for the throw to apply");
                        }
                     } else {
                        Piglin admirer = this.read_Piglin();
                        if (admirer != null) {
                           this.load_Piglin = admirer;
                           this.init_NetHelper_DataUtil = AiBarter.Inner_DataUtil.read;
                           this.write = 0;
                           return this.init_AiOperation_Result_Str_2("A piglin is already admiring an ingot");
                        }

                        this.init_DataUtil.init_V_Vec3(autismclient.ai.engine.AiAim.load_Vec3_Entity(this.init_Piglin));
                        this.init_DataUtil.load_V();
                        if (!this.init_DataUtil.load_Z() && ++this.write <= 30) {
                           return this.init_AiOperation_Result_Str_2("Turning toward the piglin");
                        }

                        this.write = 0;
                        int slot = this.init_I_8();
                        if (slot < 0) {
                           if (this.push < 3 && this.init_ItemEntity_D(16.0) != null) {
                              this.push++;
                              this.init_NetHelper_DataUtil = AiBarter.Inner_DataUtil.write;
                              return this.init_AiOperation_Result_Str_2("Recovering the thrown ingot");
                           } else {
                              return this.init_AiOperation_Result_Str_2("No ingot in the bag; re-evaluating");
                           }
                        } else {
                           Vec3 toss = mc.player.position().add(this.init_Piglin.position().subtract(mc.player.position()).scale(0.6));
                           if (this.init_Z_Vec3_D(toss, 9.0) && ++this.tick <= 60) {
                              return this.init_AiOperation_Result_Str_2("A baby piglin is near the toss; waiting");
                           }

                           this.tick = 0;
                           this.read_Set.clear();

                           for (Entity entity : mc.level.entitiesForRendering()) {
                              if (entity instanceof Piglin piglin && piglin.getOffhandItem().is(Items.GOLD_INGOT)) {
                                 this.read_Set.add(piglin.getId());
                              }
                           }

                           this.load_V_3();
                           this.run = AiGameState.load("minecraft:gold_ingot");
                           this.load_J = AiServerEvidence.init_J();
                           AiContainerTransactions.init(mc.player.containerMenu.containerId, this.init_I_I(slot), 0, ContainerInput.THROW);
                           return this.init_AiOperation_Result_Str_2("Throwing one gold ingot at the piglin");
                        }
                     }
                  } else {
                     this.init_Piglin = null;
                     this.init_NetHelper_DataUtil = AiBarter.Inner_DataUtil.init;
                     return this.init_AiOperation_Result_Str_2("Piglin gone; re-targeting");
                  }
               }

               AiOperation.Result read_AiOperation_Result_3() {
                  if (this.load_Piglin == null) {
                     this.load_Piglin = this.read_Piglin();
                     if (this.load_Piglin != null) {
                        this.write = 0;
                     }
                  }

                  if (this.load_Piglin == null) {
                     Piglin taker = this.load_Piglin();
                     if (taker != null && taker.isBaby()) {
                        this.close++;
                        this.read_Set.add(taker.getId());
                        AutismClientAddon.LOG.info("[AI] barter: a baby piglin took the ingot ({} stolen, {} bartered)", this.close, this.open);
                        if (++this.get >= 4 && this.open == 0) {
                           return this.init(AiFailure.add, "babies", "Baby piglins took " + this.close + " ingots here; barter elsewhere");
                        }

                        this.init_Piglin = null;
                        this.init_NetHelper_DataUtil = AiBarter.Inner_DataUtil.init;
                        this.write = 0;
                        return this.init_AiOperation_Result_Str_2("A baby piglin took the ingot; trying a piglin away from babies");
                     } else if (++this.write > 200) {
                        this.write = 0;
                        if (this.init_ItemEntity_D(16.0) != null) {
                           if (++this.push >= 3) {
                              if (this.init_Piglin != null) {
                                 this.init_Set.add(this.init_Piglin.getId());
                              }

                              this.init_Piglin = null;
                              this.push = 0;
                              this.init_NetHelper_DataUtil = AiBarter.Inner_DataUtil.init;
                              return this.init_AiOperation_Result_Str_2("This piglin keeps ignoring the ingot (it may be admire-disabled); trying another");
                           } else {
                              this.init_NetHelper_DataUtil = AiBarter.Inner_DataUtil.write;
                              return this.init_AiOperation_Result_Str_2("Recovering the ignored ingot");
                           }
                        } else {
                           this.close++;
                           this.init_Piglin = null;
                           this.init_NetHelper_DataUtil = AiBarter.Inner_DataUtil.init;
                           return this.init_AiOperation_Result_Str_2("The ingot vanished without a barter (a baby piglin steals gold); continuing");
                        }
                     } else {
                        return this.init_AiOperation_Result_Str_2("Waiting for a piglin to pick up the ingot");
                     }
                  } else {
                     this.init_BlockPos = this.load_Piglin.blockPosition();
                     this.read_V();
                     if (this.load_Piglin.isAlive() && !this.load_Piglin.isConverting()) {
                        if (!this.load_Piglin.getOffhandItem().is(Items.GOLD_INGOT)) {
                           this.open++;
                           this.get = 0;
                           this.init_NetHelper_DataUtil = AiBarter.Inner_DataUtil.update;
                           this.write = 0;
                           return this.init_AiOperation_Result_Str_2("Barter " + this.open + " tossed its loot; collecting");
                        } else if (++this.write > 360) {
                           this.load_Piglin = null;
                           this.init_Piglin = null;
                           this.write = 0;
                           this.init_NetHelper_DataUtil = AiBarter.Inner_DataUtil.init;
                           return this.init_AiOperation_Result_Str_2("The admire never resolved; re-targeting");
                        } else {
                           return this.init_AiOperation_Result_Str_2("The piglin is examining the ingot");
                        }
                     } else {
                        this.load_Piglin = null;
                        this.write = 0;
                        return this.init_AiOperation_Result_Str_2("The bartering piglin is gone; re-targeting");
                     }
                  }
               }

               AiOperation.Result write() {
                  if (this.init_AiOperation == null) {
                     this.init_AiOperation = AiGameplay.write(
                        AiJson.init_JsonObject_ObjArr("item", "minecraft:gold_ingot", "count", 1, "additional", true, "timeoutSeconds", 25)
                     );
                     return this.init_AiOperation_Result_Str_2("Walking back onto the thrown ingot");
                  }

                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.init_AiOperation_Result_Str_2("Recovering the ignored ingot");
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  if (r.status() == AiOperation.Status.SUCCEEDED) {
                     this.flush++;
                     this.poll = Math.max(0, this.poll - 1);
                  }

                  this.init_NetHelper_DataUtil = AiBarter.Inner_DataUtil.load;
                  this.write = 0;
                  return this.init_AiOperation_Result_Str_2(
                     r.status() == AiOperation.Status.SUCCEEDED ? "Ingot recovered; re-throwing" : "The ingot is gone; continuing"
                  );
               }

               AiOperation.Result update() {
                  this.read_V();
                  if (this.init_AiOperation != null) {
                     AiOperation.Result r = this.init_AiOperation.tick();
                     if (!r.terminal()) {
                        return this.init_AiOperation_Result_Str_2("Collecting barter drops");
                     }

                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                     if (r.status() != AiOperation.Status.SUCCEEDED && this.load_Str != null) {
                        this.read_Map.merge(this.load_Str, 1, Integer::sum);
                     }

                     this.load_Str = null;
                     return this.init_AiOperation_Result_Str_2("Drop collection leg " + r.status().name().toLowerCase(Locale.ROOT));
                  } else {
                     String next = null;

                     for (String itemId : this.load_Map.keySet()) {
                        if (this.read_Map.getOrDefault(itemId, 0) < 2 && this.init_I_Str(itemId) > 0) {
                           next = itemId;
                           break;
                        }
                     }

                     if (next != null) {
                        this.load_Str = next;
                        int onGround = this.init_I_Str(next);
                        this.init_AiOperation = AiGameplay.write(AiJson.init_JsonObject_ObjArr("item", next, "count", onGround, "additional", true, "timeoutSeconds", 25));
                        return this.init_AiOperation_Result_Str_2("Collecting " + onGround + " " + next);
                     }

                     for (Entry<String, Integer> entry : this.load_Map.entrySet()) {
                        int gained = AiGameState.init_I_Str(entry.getKey()) - entry.getValue();
                        if (gained > 0) {
                           this.init_Map.merge(entry.getKey(), gained, Integer::sum);
                        }

                        int left = this.init_I_Str(entry.getKey());
                        if (left > 0) {
                           this.init_List.add(entry.getKey() + " x" + left);
                        }
                     }

                     this.load_Map.clear();
                     this.read_Map.clear();
                     this.load_Piglin = null;
                     this.init_Piglin = null;
                     this.init_NetHelper_DataUtil = AiBarter.Inner_DataUtil.init;
                     this.write = 0;
                     return this.init_AiOperation_Result_Str_2("Barter drops collected; continuing");
                  }
               }

               AiOperation.Result sync() {
                  if (this.load_AiOperation != null) {
                     AiOperation.Result result = this.load_AiOperation.tick();
                     if (!result.terminal()) {
                        return this.init_AiOperation_Result_Str_2("Interrupted: " + this.init_Str + " - " + result.message());
                     }

                     this.load_AiOperation.cancel();
                     this.load_AiOperation = null;
                     return this.init_AiOperation_Result_Str_2("Resuming after " + this.init_Str);
                  } else {
                     AiGuard.Inner_BufferHelper vitals = AiGuard.init_X_BufferHelper();
                     AiGuard.Inner_CachePool policy = mc.level.dimension() != Level.OVERWORLD ? this.init_X_CachePool.withoutFlee() : this.init_X_CachePool;
                     AiOperation action = AiGuard.init_AiOperation_X_CachePool(policy, vitals, this.read_I_2());
                     if (action == null) {
                        return null;
                     }

                     this.load_AiOperation = action;
                     this.init_Str = AiGuard.init_Str(this.init_X_CachePool, vitals);
                     this.update++;
                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                     }

                     if (this.read_AiOperation != null) {
                        this.read_AiOperation.cancel();
                        this.read_AiOperation = null;
                        this.load_Z = true;
                     }

                     return this.init_AiOperation_Result_Str_2("Interrupted: " + this.init_Str);
                  }
               }

               Piglin init_Piglin() {
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

               void init_V_7() {
                  if (mc.options != null) {
                     mc.options.keyUp.setDown(false);
                     mc.options.keyDown.setDown(false);
                     mc.options.keyLeft.setDown(false);
                     mc.options.keyRight.setDown(false);
                     mc.options.keyJump.setDown(false);
                     mc.options.keySprint.setDown(false);
                  }
               }

               Piglin read_Piglin() {
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

               ItemEntity init_ItemEntity_D(double radius) {
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
                  Vec3 center = this.init_Piglin.position();

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof ItemEntity drop && drop.isAlive() && drop.position().distanceToSqr(center) <= 576.0) {
                        this.load_Set.add(drop.getId());
                     }
                  }
               }

               void read_V() {
                  if (this.init_BlockPos != null) {
                     Vec3 center = Vec3.atCenterOf(this.init_BlockPos);

                     for (Entity entity : mc.level.entitiesForRendering()) {
                        if (entity instanceof ItemEntity drop && drop.isAlive() && !this.load_Set.contains(drop.getId())) {
                           String itemId = BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).toString();
                           if (!"minecraft:gold_ingot".equals(itemId) && !(drop.position().distanceToSqr(center) > 144.0)) {
                              this.load_Map.putIfAbsent(itemId, AiGameState.init_I_Str(itemId));
                           }
                        }
                     }
                  }
               }

               int init_I_Str(String itemId) {
                  int total = 0;
                  Vec3 center = this.init_BlockPos != null ? Vec3.atCenterOf(this.init_BlockPos) : mc.player.position();

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

               boolean init_Z_5() {
                  List<String> armor = new ArrayList<>(4);

                  for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                     ItemStack stack = mc.player.getItemBySlot(slot);
                     armor.add(stack.isEmpty() ? "minecraft:air" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                  }

                  return AiDimension.init(armor);
               }

               int init_I_8() {
                  for (int slot = 0; slot < 36; slot++) {
                     if (mc.player.getInventory().getItem(slot).is(Items.GOLD_INGOT)) {
                        return slot;
                     }
                  }

                  return -1;
               }

               int init_I_I(int inventorySlot) {
                  int handler = autismclient.util.InventorySlotHelper.load_I(mc, inventorySlot);
                  if (handler < 0) {
                     throw new IllegalStateException("Inventory slot " + inventorySlot + " has no menu slot");
                  } else {
                     return handler;
                  }
               }

               int load_I_4() {
                  return AiGameState.init_I_Str("minecraft:ender_pearl") - this.load_I;
               }

               int read_I_2() {
                  return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
               }

               JsonObject init_JsonObject_14() {
                  JsonObject drops = new JsonObject();

                  for (Entry<String, Integer> entry : this.init_Map.entrySet()) {
                     drops.addProperty(entry.getKey(), entry.getValue());
                  }

                  int pearlsGained = this.load_I_4();
                  JsonObject out = AiJson.init_JsonObject_ObjArr(
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
                     this.init_I,
                     "goldLeft",
                     AiGameState.load("minecraft:gold_ingot"),
                     "pearlsGained",
                     pearlsGained,
                     "pearlsTarget",
                     pearlsTarget,
                     "phase",
                     this.init_NetHelper_DataUtil.name().toLowerCase(Locale.ROOT)
                  );
                  out.add("dropsByItem", drops);
                  if (pearlsTarget > 0) {
                     out.addProperty("pearlsMet", pearlsGained >= pearlsTarget);
                  }

                  if (this.load_Z) {
                     out.addProperty("warning", "No gold armor worn; piglins stay hostile and may attack between barters");
                  }

                  if (!this.init_List.isEmpty()) {
                     out.add("uncollected", AiJson.init.toJsonTree(this.init_List));
                  }

                  Piglin piglin = this.load_Piglin != null ? this.load_Piglin : this.init_Piglin;
                  if (piglin != null) {
                     out.add(
                        "piglin",
                        AiJson.init_JsonObject_ObjArr("entityId", piglin.getId(), "x", piglin.getBlockX(), "y", piglin.getBlockY(), "z", piglin.getBlockZ())
                     );
                  }

                  out.add("survival", AiGuard.init_JsonObject_X_BufferHelper(AiGuard.init_X_BufferHelper(), this.update, this.init_Str));
                  return out;
               }

               AiOperation.Result init_AiOperation_Result_Str_2(String message) {
                  return new AiOperation.Result(
                     AiOperation.Status.RUNNING, message + " (" + this.open + " bartered)", this.init_JsonObject_14().toString(), List.of()
                  );
               }

               AiOperation.Result init_AiOperation_Result_AiOperation_Status_Str_2(AiOperation.Status status, String message) {
                  JsonObject data = this.init_JsonObject_14();
                  this.cancel();
                  return new AiOperation.Result(status, message, data.toString(), List.of());
               }

               AiOperation.Result init(AiFailure code, String reason, String message) {
                  JsonObject data = this.init_JsonObject_14();
                  data.addProperty("reason", reason);
                  this.cancel();
                  return AiOperation.Result.failed(code, message, data.toString());
               }

               @Override
               public void cancel() {
                  if (!this.read_Z) {
                     this.read_Z = true;
                     this.init_V_7();
                     this.init_DataUtil.init_V();
                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                     }

                     if (this.load_AiOperation != null) {
                        this.load_AiOperation.cancel();
                        this.load_AiOperation = null;
                     }

                     if (this.read_AiOperation != null) {
                        this.read_AiOperation.cancel();
                        this.read_AiOperation = null;
                     }

                     if (mc.player != null && mc.player.getInventory().getSelectedSlot() != this.read_I) {
                        mc.player.getInventory().setSelectedSlot(this.read_I);
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
      init,
      load,
      read,
      write,
      update;
   }

   enum Inner_StreamUtil {
      init,
      load,
      read,
      write;
   }
}
