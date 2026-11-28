package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;

final class AiBarterBatch {
   private AiBarterBatch() {
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int target = Math.max(0, Math.min(64, AiJson.getInt(args, "pearls", 0)));
         boolean additional = AiJson.getBoolean(args, "additional", false);
         final int baseline = AiCraft.countItem("minecraft:ender_pearl");
         final int pearlGoal = target + (additional ? baseline : 0);
         final int partners = Math.max(1, Math.min(4, AiJson.getInt(args, "partners", 3)));
         final int budget = AiBarter.init(Math.max(0, AiJson.getInt(args, "gold", 0)), AiCraft.countItem("minecraft:gold_ingot"));
         if (budget == 0 && (target == 0 || baseline < pearlGoal)) {
            return AiGameOperations.immediate(
               AiOperation.Status.FAILED,
               "No gold ingots available for adult bartering",
               AiJson.objectOf("missing", AiJson.objectOf("minecraft:gold_ingot", 1), "goldSpent", 0, "bartered", 0)
            );
         }

         final int seconds = Math.max(10, Math.min(900, AiJson.getInt(args, "timeoutSeconds", 300)));
         return new AiOperation() {
            final Object startLevel = mc.level;
            final int originalSlot = mc.player.getInventory().getSelectedSlot();
            final long deadlineNanos = System.nanoTime() + seconds * 1000000000L;
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            final AiGuardLayer guard = new AiGuardLayer(AiGuard.GuardPolicy.standard().withoutFlee().keeping(List.of("minecraft:gold_ingot", "minecraft:ender_pearl")), "barter");
            final Map<UUID, AiBarterBatch.PendingTrade> pendingTrades = new LinkedHashMap<>();
            final Set<UUID> skippedPiglins = new HashSet<>();
            final Map<String, Integer> inventoryAtStart = AiBarterBatch.inventoryCounts();
            final Map<String, Integer> observedOutputs = new HashMap<>();
            final Map<String, Integer> confirmedOutputs = new HashMap<>();
            final Map<UUID, Integer> seenDropCounts = new HashMap<>(AiBarterBatch.nearbyDropCounts(mc));
            final Map<String, Integer> seenInventoryCounts = new HashMap<>(this.inventoryAtStart);
            final AiBarterBatch.GoldOffer goldOffer = new AiBarterBatch.GoldOffer();
            AiOperation subOperation;
            Piglin offerTarget;
            Piglin approachedPiglin;
            Map<UUID, Integer> dropsBeforeOffer;
            Map<String, Integer> inventoryBeforeOffer;
            int tickCount;
            int goldSpent;
            int barterCount;
            int unconfirmedOrLost;
            int offerTick;
            int failedApproaches;
            boolean cancelled;
            boolean armorEquipAttempted;
            final AiOperation transferBoundary = new AiOperation() {
               @Override
               public AiOperation.Result tick() {
                  throw new IllegalStateException("Transfer boundary is observation-only");
               }

               @Override
               public boolean canInterruptNow() {
                  return !init_Z_4();
               }

               @Override
               public boolean requiresReconciliationAfterEmergency() {
                  return init_Z_4();
               }

               @Override
               public void cancel() {
               }
            };

            @Override
            public AiOperation.Result tick() {
               if (this.cancelled || mc.player == null || mc.level != this.startLevel) {
                  return this.finish(AiOperation.Status.CANCELLED, "Barter world changed");
               }

               if (!mc.player.isAlive()) {
                  return this.finish(AiOperation.Status.FAILED, "Player died while bartering");
               }

               this.tickCount++;
               if (System.nanoTime() > this.deadlineNanos) {
                  return this.finish(
                     AiOperation.Status.TIMED_OUT, "Barter budget expired; observed pearls " + AiCraft.countItem("minecraft:ender_pearl") + "/" + pearlGoal
                  );
               }

               if (target > 0 && AiCraft.countItem("minecraft:ender_pearl") >= pearlGoal && this.offerTarget == null && this.pendingTrades.isEmpty()) {
                  return this.finish(AiOperation.Status.SUCCEEDED, "Pearl inventory target met after " + this.barterCount + " confirmed barters");
               }

               if (AiWorldScan.findEntitiesByType("minecraft:piglin_brute", 12.0).isEmpty() && AiWorldScan.findEntitiesByType("minecraft:hoglin", 8.0).isEmpty()) {
                  if (AiBarterBatch.wearsGoldArmor(mc)) {
                     AiOperation.Result survival = this.guard
                        .tick(this.init_Z_4() ? this.transferBoundary : this.subOperation, () -> {
                           if (this.subOperation != null) {
                              this.subOperation.cancel();
                              this.subOperation = null;
                              this.approachedPiglin = null;
                           }
                        }, () -> Math.max(1, (int)((this.deadlineNanos - System.nanoTime()) / 1000000000L)));
                     if (survival != null) {
                        this.aim.clear();
                        return survival.terminal() ? this.finish(survival.status(), survival.message()) : survival;
                     }
                  }

                  if (this.subOperation != null) {
                     AiOperation.Result result = this.subOperation.tick();
                     if (!result.terminal()) {
                        return this.running(result.message());
                     }

                     boolean pendingLeg = this.subOperation.requiresReconciliationAfterEmergency();
                     this.subOperation.cancel();
                     this.subOperation = null;
                     if (result.status() == AiOperation.Status.UNCERTAIN || pendingLeg) {
                        return this.finish(AiOperation.Status.UNCERTAIN, "Barter child needs reconciliation: " + result.message());
                     }

                     if (result.status() == AiOperation.Status.CANCELLED) {
                        return this.finish(AiOperation.Status.CANCELLED, result.message());
                     }

                     if (this.approachedPiglin != null) {
                        if (result.status() != AiOperation.Status.SUCCEEDED) {
                           AiNetherStructures.recordBarter(mc, this.approachedPiglin);
                        }

                        this.approachedPiglin = null;
                     } else if (result.status() != AiOperation.Status.SUCCEEDED && ++this.failedApproaches > 4) {
                        return this.finish(AiOperation.Status.FAILED, "Barter preparation or collection repeatedly failed: " + result.message());
                     }
                  }

                  if (!AiBarterBatch.wearsGoldArmor(mc)) {
                     if (this.armorEquipAttempted) {
                        return this.finish(AiOperation.Status.FAILED, "Wear a usable gold armor piece before bartering");
                     }

                     this.armorEquipAttempted = true;
                     this.subOperation = AiEquip.init(AiJson.objectOf("what", "armor", "prefer", "golden_boots"));
                     return this.running("Equipping gold armor for piglin neutrality");
                  } else {
                     if (this.offerTarget != null) {
                        boolean admired = this.offerTarget.isAlive() && this.offerTarget.getOffhandItem().is(Items.GOLD_INGOT);
                        AiBarterBatch.OfferOutcome outcome = this.goldOffer
                           .resolve(AiCraft.countItem("minecraft:gold_ingot"), admired, AiServerEvidence.inventoryRevision(), this.tickCount);
                        if (outcome != AiBarterBatch.OfferOutcome.ACCEPTED) {
                           if (outcome == AiBarterBatch.OfferOutcome.AMBIGUOUS) {
                              this.goldSpent = this.goldSpent + this.goldOffer.consumedGold();
                              return this.finish(AiOperation.Status.UNCERTAIN, "Gold changed by more than one ingot during a single adult interaction");
                           }

                           if (outcome == AiBarterBatch.OfferOutcome.UNACKNOWLEDGED) {
                              return this.finish(
                                 AiOperation.Status.UNCERTAIN, "Adult interaction was not acknowledged; reobserve gold and offhand before spending again"
                              );
                           }

                           return this.running("Waiting for one adult's ingot acknowledgement");
                        }

                        this.goldSpent++;
                        AiBarterBatch.PendingTrade trade = new AiBarterBatch.PendingTrade(this.offerTarget, this.dropsBeforeOffer, this.inventoryBeforeOffer, this.offerTick);
                        trade.admired = admired;
                        this.pendingTrades.put(this.offerTarget.getUUID(), trade);
                        this.offerTarget = null;
                     }

                     for (AiBarterBatch.PendingTrade trade : new ArrayList<>(this.pendingTrades.values())) {
                        Piglin piglin = trade.piglin;
                        if (piglin.isAlive()
                           && mc.level.getEntity(piglin.getId()) == piglin
                           && !piglin.isConverting()
                           && !piglin.isAggressive()
                           && trade.piglinId.equals(piglin.getUUID())) {
                           trade.admired = trade.admired | piglin.getOffhandItem().is(Items.GOLD_INGOT);

                           for (Entity entity : mc.level.entitiesForRendering()) {
                              if (entity instanceof ItemEntity drop
                                 && drop.isAlive()
                                 && !drop.getItem().is(Items.GOLD_INGOT)
                                 && !(drop.distanceToSqr(piglin) > 144.0)) {
                                 int quantity = drop.getItem().getCount();
                                 int before = Math.max(trade.dropsBefore.getOrDefault(drop.getUUID(), 0), this.seenDropCounts.getOrDefault(drop.getUUID(), 0));
                                 if (quantity > before) {
                                    String id = BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).toString();
                                    this.seenDropCounts.put(drop.getUUID(), quantity);
                                    this.observedOutputs.merge(id, quantity - before, Integer::sum);
                                    trade.gainObserved = true;
                                 }
                              }
                           }

                           if (trade.admired && this.tickCount - trade.startTick >= 100 && !piglin.getOffhandItem().is(Items.GOLD_INGOT)) {
                              AiBarterBatch.inventoryCounts().forEach((idxx, count) -> {
                                 int gained = count - Math.max(trade.inventoryBefore.getOrDefault(idxx, 0), this.seenInventoryCounts.getOrDefault(idxx, 0));
                                 if (gained > 0 && AiBarterBatch.isWantedOutput(idxx)) {
                                    trade.gainObserved = true;
                                    this.observedOutputs.putIfAbsent(idxx, 0);
                                    this.confirmedOutputs.merge(idxx, Math.max(0, count - this.inventoryAtStart.getOrDefault(idxx, 0)), Math::max);
                                    this.seenInventoryCounts.put(idxx, count);
                                 }
                              });
                           }

                           if (this.tickCount - trade.startTick >= 100 && !piglin.getOffhandItem().is(Items.GOLD_INGOT) && trade.admired && trade.gainObserved) {
                              this.barterCount++;
                              this.pendingTrades.remove(trade.piglinId);
                           } else if (this.tickCount - trade.startTick > 360) {
                              this.pendingTrades.remove(trade.piglinId);
                              this.skippedPiglins.add(trade.piglinId);
                              this.unconfirmedOrLost++;
                           }
                        } else {
                           this.pendingTrades.remove(trade.piglinId);
                           this.skippedPiglins.add(trade.piglinId);
                           this.unconfirmedOrLost++;
                        }
                     }

                     if (this.offerTarget != null || AiItems.countFreeSlots() >= 2 || this.goldSpent >= budget && this.observedOutputs.isEmpty()) {
                        if (this.pendingTrades.size() < partners
                           && this.goldSpent < budget
                           && AiCraft.countItem("minecraft:gold_ingot") > 0
                           && (target == 0 || AiCraft.countItem("minecraft:ender_pearl") < pearlGoal)) {
                           Piglin partner = AiBarterBatch.nearestPiglin(mc, this.pendingTrades.keySet(), this.skippedPiglins);
                           if (partner != null) {
                              if (!(partner.distanceToSqr(mc.player) > 9.0) && mc.player.hasLineOfSight(partner)) {
                                 if (mc.player.getMainHandItem().is(Items.GOLD_INGOT)) {
                                    this.aim.aimAt(partner.getBoundingBox().getCenter());
                                    this.aim.tick();
                                    if (this.aim.isSettled() && this.aim.isAimedAtEntity(partner)) {
                                       this.dropsBeforeOffer = AiBarterBatch.nearbyDropCounts(mc);
                                       this.inventoryBeforeOffer = AiBarterBatch.inventoryCounts();
                                       this.offerTarget = partner;
                                       this.offerTick = this.tickCount;
                                       this.goldOffer.begin(AiCraft.countItem("minecraft:gold_ingot"), AiServerEvidence.inventoryRevision(), this.tickCount);
                                       mc.gameMode
                                          .interact(
                                             mc.player, partner, new EntityHitResult(partner, partner.getBoundingBox().getCenter()), InteractionHand.MAIN_HAND
                                          );
                                       return this.running("Offering exactly one ingot to an adult");
                                    }

                                    return this.running("Aiming at an adult barter partner");
                                 }

                                 this.subOperation = AiEquip.init(AiJson.objectOf("what", "minecraft:gold_ingot"));
                              } else if (this.pendingTrades.isEmpty()) {
                                 this.approachedPiglin = partner;
                                 this.subOperation = AiGoto.travelToCompat(partner.blockPosition(), 2.5, true, false, false, 40);
                              }

                              if (this.subOperation != null) {
                                 return this.running("Preparing the next safe barter partner");
                              }
                           }
                        }

                        String collect = null;

                        for (String id : this.observedOutputs.keySet().stream().sorted(Comparator.comparingInt(AiBarterBatch::outputSortOrder)).toList()) {
                           if (AiBarterBatch.dropNearby(mc, id)) {
                              collect = id;
                              break;
                           }
                        }

                        if (collect == null || this.pendingTrades.size() < partners && this.goldSpent < budget && this.tickCount % 40 != 0) {
                           if (this.pendingTrades.isEmpty() && this.offerTarget == null && this.subOperation == null) {
                              if (this.goldSpent >= budget || AiCraft.countItem("minecraft:gold_ingot") <= 0) {
                                 return this.finish(
                                    target == 0 && this.barterCount > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                                    "Gold batch exhausted; pearls " + AiCraft.countItem("minecraft:ender_pearl") + "/" + pearlGoal + "; loot is random"
                                 );
                              }

                              if (AiBarterBatch.nearestPiglin(mc, this.pendingTrades.keySet(), this.skippedPiglins) == null) {
                                 return this.finish(AiOperation.Status.FAILED, "No safe unprovoked adult piglin available");
                              }
                           }

                           return this.running("Waiting on " + this.pendingTrades.size() + " genuine barters");
                        } else {
                           this.subOperation = AiGameplay.write(
                              AiJson.objectOf("item", collect, "count", 1, "additional", true, "radius", 12, "timeoutSeconds", 12)
                           );
                           return this.running("Collecting observed barter output " + collect);
                        }
                     } else {
                        this.subOperation = AiTidy.init(
                           AiJson.objectOf(
                              "action",
                              "drop",
                              "freeSlots",
                              3,
                              "keep",
                              "gold_ingot,ender_pearl,obsidian,string,spectral_arrow,potion,splash_potion,fire_charge"
                           )
                        );
                        return this.running("Making capacity for useful barter outputs");
                     }
                  }
               } else {
                  return this.finish(AiOperation.Status.FAILED, "Barter station unsafe: brute or hoglin nearby; choose a protected adult partner");
               }
            }

            JsonObject snapshotJson() {
               JsonObject gains = new JsonObject();
               AiBarterBatch.inventoryCounts().forEach((id, count) -> {
                  int gained = count - this.inventoryAtStart.getOrDefault(id, 0);
                  if (gained > 0 && this.observedOutputs.containsKey(id)) {
                     gains.addProperty(id, gained);
                  }
               });
               return AiJson.objectOf(
                  "bartered",
                  this.barterCount,
                  "goldSpent",
                  this.goldSpent,
                  "goldBudget",
                  budget,
                  "goldLeft",
                  AiCraft.countItem("minecraft:gold_ingot"),
                  "partners",
                  partners,
                  "pendingBarters",
                  this.pendingTrades.size(),
                  "unconfirmedOrLost",
                  this.unconfirmedOrLost,
                  "pearlsTarget",
                  pearlGoal,
                  "pearlsGained",
                  AiCraft.countItem("minecraft:ender_pearl") - baseline,
                  "pearlsMet",
                  AiCraft.countItem("minecraft:ender_pearl") >= pearlGoal,
                  "observedOutputs",
                  AiJson.GSON.toJsonTree(this.observedOutputs),
                  "outputAttribution",
                  "aggregate nearby drop and inventory evidence; individual output attribution may be ambiguous",
                  "inventoryConfirmedOutputs",
                  AiJson.GSON.toJsonTree(this.confirmedOutputs),
                  "collectedOutputs",
                  gains,
                  "lootWeightTotal",
                  469,
                  "pearlWeight",
                  10,
                  "pearlRollMin",
                  2,
                  "pearlRollMax",
                  4,
                  "lootTableAssumption",
                  "vanilla_26.2; server outputs remain authoritative"
               );
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.snapshotJson());
            }

            boolean init_Z_4() {
               return this.goldOffer.isPending() || !this.pendingTrades.isEmpty();
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               AiOperation.Result result = AiGuardLayer.wrapPendingTermination(
                  this.requiresReconciliationAfterEmergency(), AiGameOperations.result(status, message, this.snapshotJson())
               );
               this.cancel();
               return result;
            }

            @Override
            public boolean canInterruptNow() {
               return !this.init_Z_4() && (this.subOperation == null || this.subOperation.canInterruptNow()) && this.guard.canInterruptNow();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.init_Z_4() || this.subOperation != null && this.subOperation.requiresReconciliationAfterEmergency() || this.guard.requiresReconciliation();
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  if (this.subOperation != null) {
                     this.subOperation.cancel();
                     this.subOperation = null;
                  }

                  this.guard.cancel();
                  this.aim.clear();
                  if (mc.player != null && mc.level == this.startLevel) {
                     AiItems.selectOrMoveToHotbar(this.originalSlot);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Bartering requires a connected living player");
      }
   }

   private static int outputSortOrder(String id) {
      byte var10000;
      switch (id) {
         case "minecraft:ender_pearl":
            var10000 = 0;
            break;
         case "minecraft:obsidian":
            var10000 = 1;
            break;
         case "minecraft:string":
            var10000 = 2;
            break;
         case "minecraft:potion":
         case "minecraft:splash_potion":
            var10000 = 3;
            break;
         case "minecraft:spectral_arrow":
            var10000 = 4;
            break;
         default:
            var10000 = 5;
      }

      return var10000;
   }

   private static boolean isWantedOutput(String id) {
      return Set.of(
            "minecraft:ender_pearl",
            "minecraft:obsidian",
            "minecraft:crying_obsidian",
            "minecraft:string",
            "minecraft:spectral_arrow",
            "minecraft:fire_charge",
            "minecraft:potion",
            "minecraft:splash_potion",
            "minecraft:iron_nugget",
            "minecraft:quartz",
            "minecraft:leather",
            "minecraft:gravel",
            "minecraft:blackstone",
            "minecraft:soul_sand",
            "minecraft:nether_brick",
            "minecraft:iron_boots",
            "minecraft:enchanted_book",
            "minecraft:dried_ghast"
         )
         .contains(id);
   }

   private static boolean wearsGoldArmor(Minecraft mc) {
      for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
         ItemStack stack = mc.player.getItemBySlot(slot);
         if (AiRunResources.hasMinDurability(stack, 8) && AiDimension.GOLD_ARMOR.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
            return true;
         }
      }

      return false;
   }

   private static Piglin nearestPiglin(Minecraft mc, Set<UUID> busy, Set<UUID> skipped) {
      Piglin best = null;
      double distance = 1024.0;

      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof Piglin piglin
            && piglin.isAdult()
            && piglin.isAlive()
            && !piglin.isConverting()
            && !piglin.isAggressive()
            && piglin.getOffhandItem().isEmpty()
            && !busy.contains(piglin.getUUID())
            && !skipped.contains(piglin.getUUID())
            && AiNetherStructures.canBarter(mc, piglin)
            && AiCombat.isSafeStep(mc, piglin.blockPosition())
            && !lavaNear(mc, piglin.blockPosition())) {
            double d = piglin.distanceToSqr(mc.player);
            if (d < distance) {
               distance = d;
               best = piglin;
            }
         }
      }

      return best;
   }

   private static boolean lavaNear(Minecraft mc, BlockPos at) {
      for (BlockPos p : BlockPos.betweenClosed(at.offset(-2, -2, -2), at.offset(2, 0, 2))) {
         if (mc.level.getFluidState(p).is(FluidTags.LAVA)) {
            return true;
         }
      }

      return false;
   }

   private static Map<UUID, Integer> nearbyDropCounts(Minecraft mc) {
      Map<UUID, Integer> out = new HashMap<>();

      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof ItemEntity drop) {
            out.put(drop.getUUID(), drop.getItem().getCount());
         }
      }

      return out;
   }

   private static boolean dropNearby(Minecraft mc, String id) {
      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof ItemEntity drop
            && drop.isAlive()
            && drop.distanceToSqr(mc.player) < 144.0
            && id.equals(BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).toString())) {
            return true;
         }
      }

      return false;
   }

   private static Map<String, Integer> inventoryCounts() {
      Map<String, Integer> out = new HashMap<>();
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
               out.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), Integer::sum);
            }
         }
      }

      return out;
   }

   private static final class PendingTrade {
      final Piglin piglin;
      final UUID piglinId;
      final Map<UUID, Integer> dropsBefore;
      final Map<String, Integer> inventoryBefore;
      final int startTick;
      boolean admired;
      boolean gainObserved;

      PendingTrade(Piglin piglin, Map<UUID, Integer> beforeDrops, Map<String, Integer> beforeInventory, int began) {
         this.piglin = piglin;
         this.piglinId = piglin.getUUID();
         this.dropsBefore = beforeDrops;
         this.startTick = began;
         this.inventoryBefore = beforeInventory;
      }
   }

   static final class GoldOffer {
      private int goldBefore;
      private int offerTick;
      private int consumed;
      private long revisionAtOffer;
      private boolean pending;

      void begin(int gold, long revision, int tick) {
         if (this.pending) {
            throw new IllegalStateException("A gold offer is already pending");
         }

         this.goldBefore = gold;
         this.revisionAtOffer = revision;
         this.offerTick = tick;
         this.consumed = 0;
         this.pending = true;
      }

      AiBarterBatch.OfferOutcome resolve(int gold, boolean admiring, long revision, int tick) {
         if (!this.pending) {
            throw new IllegalStateException("No gold offer is pending");
         }

         int consumed = this.goldBefore - gold;
         this.consumed = Math.max(0, consumed);
         if (consumed <= 0 || !admiring && revision <= this.revisionAtOffer) {
            return tick - this.offerTick > 60 ? AiBarterBatch.OfferOutcome.UNACKNOWLEDGED : AiBarterBatch.OfferOutcome.WAITING;
         }

         if (consumed != 1) {
            return AiBarterBatch.OfferOutcome.AMBIGUOUS;
         }

         this.pending = false;
         return AiBarterBatch.OfferOutcome.ACCEPTED;
      }

      boolean isPending() {
         return this.pending;
      }

      int consumedGold() {
         return this.consumed;
      }
   }

   enum OfferOutcome {
      WAITING,
      ACCEPTED,
      AMBIGUOUS,
      UNACKNOWLEDGED;
   }
}
