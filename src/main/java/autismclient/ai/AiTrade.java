package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

final class AiTrade {
   private static final int init_I = 80;
   private static final int load = 40;
   private static final int read = 30;
   private static final double init_D = 32.0;
   private static final int write = 3000;
   static final Set<String> JOB_SITE_BLOCKS = Set.of(
      "minecraft:lectern",
      "minecraft:composter",
      "minecraft:barrel",
      "minecraft:blast_furnace",
      "minecraft:smoker",
      "minecraft:cartography_table",
      "minecraft:fletching_table",
      "minecraft:loom",
      "minecraft:smithing_table",
      "minecraft:grindstone",
      "minecraft:stonecutter",
      "minecraft:brewing_stand",
      "minecraft:cauldron",
      "minecraft:water_cauldron",
      "minecraft:lava_cauldron",
      "minecraft:powder_snow_cauldron"
   );

   private AiTrade() {
   }

   static boolean isUntraded(List<Integer> offerUses) {
      for (int uses : offerUses) {
         if (uses > 0) {
            return false;
         }
      }

      return true;
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final AiTrade.Inner_DataUtil action = parseAction(AiJson.getString(args, "action", "inspect"));
         final int entityId = AiJson.getInt(args, "entityId", -1);
         final int tradeIndex = AiJson.getInt(args, "tradeIndex", -1);
         final String wanted = AiJson.getString(args, "item", "");
         final int count = Math.max(1, Math.min(256, AiJson.getInt(args, "count", 1)));
         final int timeoutSeconds = Math.max(5, Math.min(900, AiJson.getInt(args, "timeoutSeconds", 120)));
         final boolean waitRestock = AiJson.getBoolean(args, "waitRestock", false);
         final boolean reroll = AiJson.getBoolean(args, "reroll", false);
         final int rerollAttempts = Math.max(1, Math.min(8, AiJson.getInt(args, "rerollAttempts", 3)));
         if (action == AiTrade.Inner_DataUtil.TRADE && tradeIndex < 0 && wanted.isBlank()) {
            throw new IllegalArgumentException("trade needs tradeIndex from an inspect listing, or item to select the offer by its result");
         }

         final AiItems.ItemSpec wantedTarget = wanted.isBlank() ? null : AiItems.resolveItem(wanted);
         return new AiOperation() {
            final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer();
            final autismclient.ai.engine.AiBlockDigger init_RefQueue_DataUtil = new autismclient.ai.engine.AiBlockDigger(
               mc, this::selectSlot, this.aim::aimAt, true, true, this.aim::tick
            );
            AiOperation leg;
            Entity trader;
            int menuContainerId = -1;
            int openAttempts;
            int offerIndex = -1;
            int resultWaitTicks;
            int serverWaitTicks;
            int completedTrades;
            long pendingRevision = -1L;
            final int originalSlot = mc.player.getInventory().getSelectedSlot();
            ItemStack pendingResult = ItemStack.EMPTY;
            int resultCountBefore;
            boolean cancelled;
            boolean waitingRestock;
            boolean restocked;
            int restockWaitTicks;
            int rerolls;
            int rerollPhase;
            int rerollPhaseTicks;
            BlockPos jobSite;
            Item jobSiteItem;

            @Override
            public AiOperation.Result tick() {
               if (!this.cancelled && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.deadline) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Trade deadline reached after " + this.completedTrades + " trade(s)");
                  }

                  if (!(mc.player.containerMenu instanceof MerchantMenu)) {
                     if (this.menuContainerId >= 0) {
                        return this.finish(
                           this.completedTrades > 0 ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED, "The trade screen closed after " + this.completedTrades + " trade(s)"
                        );
                     }

                     AiOperation.Result open = this.openTradeScreen();
                     if (open != null) {
                        return open;
                     }
                  }

                  MerchantMenu menu = (MerchantMenu)mc.player.containerMenu;
                  this.menuContainerId = menu.containerId;
                  if (menu.getOffers().isEmpty()) {
                     return this.finish(AiOperation.Status.FAILED, "This trader has no offers");
                  }

                  if (action == AiTrade.Inner_DataUtil.INSPECT) {
                     return this.finish(AiOperation.Status.SUCCEEDED, "Read " + menu.getOffers().size() + " offer(s)");
                  }

                  if (this.waitingRestock) {
                     MerchantOffer offer = menu.getOffers().get(this.offerIndex);
                     if (!offer.isOutOfStock()) {
                        this.waitingRestock = false;
                        this.restockWaitTicks = 0;
                        this.restocked = true;
                        return this.running("Offer restocked");
                     } else if (++this.restockWaitTicks <= 3000) {
                        return this.running("Waiting for the trader to restock");
                     } else {
                        return this.completedTrades > 0
                           ? this.finish(AiOperation.Status.SUCCEEDED, "Offer did not restock within the wait cap after " + this.completedTrades + " trade(s)")
                           : this.fail(AiFailure.OUT_OF_STOCK, "Offer did not restock within the wait cap after " + this.completedTrades + " trade(s)");
                     }
                  } else {
                     if (this.offerIndex < 0) {
                        this.offerIndex = this.findOfferIndex(menu);
                        if (this.offerIndex < 0) {
                           if (wantedTarget == null || !reroll) {
                              return this.finish(
                                 AiOperation.Status.FAILED,
                                 wantedTarget == null
                                    ? "Trade index " + tradeIndex + " is not an offer on this trader"
                                    : "No offer on this trader produces " + wanted
                              );
                           }

                           AiOperation.Result rolling = this.rerollStep(menu);
                           if (rolling != null) {
                              return rolling;
                           }

                           this.offerIndex = this.findOfferIndex(menu);
                           if (this.offerIndex < 0) {
                              if (this.rerolls >= rerollAttempts) {
                                 return this.fail(AiFailure.OUT_OF_STOCK, "No offer on this trader produces " + wanted + " after " + this.rerolls + " reroll(s)");
                              }

                              return this.running("Rerolled offers lack " + wanted + "; rolling again");
                           }
                        }

                        MerchantOffer offer = menu.getOffers().get(this.offerIndex);
                        if (offer.isOutOfStock()) {
                           if (waitRestock) {
                              this.waitingRestock = true;
                              this.restockWaitTicks = 0;
                              return this.running("Offer is out of stock; waiting for the trader to restock");
                           }

                           return this.fail(AiFailure.OUT_OF_STOCK, "Offer " + this.offerIndex + " is out of stock until the trader restocks");
                        }

                        if (!this.canAfford(offer)) {
                           return this.finish(AiOperation.Status.FAILED, "Cannot afford offer " + this.offerIndex + ": needs " + this.costText(offer));
                        }
                     }

                     if (this.completedTrades >= count) {
                        return this.finish(AiOperation.Status.SUCCEEDED, "Completed " + this.completedTrades + " trade(s)");
                     }

                     MerchantOffer offer = menu.getOffers().get(this.offerIndex);
                     if (offer.isOutOfStock()) {
                        if (waitRestock) {
                           this.waitingRestock = true;
                           this.restockWaitTicks = 0;
                           return this.running("Offer ran out of stock; waiting for the trader to restock");
                        } else {
                           return this.completedTrades > 0
                              ? this.finish(AiOperation.Status.SUCCEEDED, "Offer ran out of stock after " + this.completedTrades + " of " + count + " trade(s)")
                              : this.fail(AiFailure.OUT_OF_STOCK, "Offer ran out of stock after " + this.completedTrades + " of " + count + " trade(s)");
                        }
                     } else {
                        if (!this.canAfford(offer)) {
                           return this.finish(
                              this.completedTrades > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                              "Ran out of payment after " + this.completedTrades + " of " + count + " trade(s); needs " + this.costText(offer)
                           );
                        }

                        if (this.pendingRevision < 0L) {
                           mc.getConnection().send(new ServerboundSelectTradePacket(this.offerIndex));
                           menu.setSelectionHint(this.offerIndex);
                           menu.tryMoveItems(this.offerIndex);
                           ItemStack result = menu.getSlot(2).getItem();
                           if (!result.isEmpty()) {
                              this.resultWaitTicks = 0;
                              if (!this.hasRoomFor(result)) {
                                 return this.finish(
                                    this.completedTrades > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                                    "No inventory room for the trade result after " + this.completedTrades + " of " + count + " trade(s)"
                                 );
                              }

                              this.pendingRevision = AiServerEvidence.containerRevision(this.menuContainerId);
                              this.pendingResult = result.copy();
                              this.resultCountBefore = this.countHeld(result);
                              AiContainerTransactions.sendPredictedClick(this.menuContainerId, 2, 0, ContainerInput.QUICK_MOVE);
                              return this.running("Taking trade " + (this.completedTrades + 1) + "/" + count);
                           } else {
                              return ++this.resultWaitTicks > 30
                                 ? this.finish(
                                    this.completedTrades > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                                    "The trader did not fill the result slot after " + this.completedTrades + " trade(s)"
                                 )
                                 : this.running("Selecting offer " + this.offerIndex);
                           }
                        } else if (AiServerEvidence.containerRevision(this.menuContainerId) <= this.pendingRevision) {
                           return ++this.serverWaitTicks > 40
                              ? this.finish(AiOperation.Status.UNCERTAIN, "A trade was not confirmed by the server; reobserve before repeating")
                              : this.running("Waiting for the server to apply the trade");
                        } else if (this.countHeld(this.pendingResult) > this.resultCountBefore) {
                           this.completedTrades++;
                           this.pendingRevision = -1L;
                           this.pendingResult = ItemStack.EMPTY;
                           this.serverWaitTicks = 0;
                           this.resultWaitTicks = 0;
                           return this.running("Trade " + this.completedTrades + "/" + count + " confirmed");
                        } else {
                           this.pendingRevision = -1L;
                           this.pendingResult = ItemStack.EMPTY;
                           this.serverWaitTicks = 0;
                           return this.finish(
                              AiOperation.Status.UNCERTAIN, "The server updated but no trade result reached the inventory; reobserve before repeating"
                           );
                        }
                     }
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Trade cancelled");
               }
            }

            AiOperation.Result openTradeScreen() {
               if (this.trader == null || !this.trader.isAlive()) {
                  this.trader = this.findTrader();
                  this.openAttempts = 0;
                  if (this.trader == null) {
                     return this.finish(
                        AiOperation.Status.FAILED,
                        entityId >= 0 ? "Entity " + entityId + " is not a live trader" : "No villager or wandering trader within 32 blocks"
                     );
                  }
               }

               double reach = Math.max(3.0, mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE));
               if (mc.player.distanceTo(this.trader) > reach * 0.9) {
                  if (this.leg == null) {
                     this.leg = AiGoto.travelToCompat(
                        this.trader.blockPosition(), 2.0, false, false, true, Math.min(120, this.remainingSeconds())
                     );
                  }

                  AiOperation.Result legResult = this.leg.tick();
                  if (!legResult.terminal()) {
                     return this.running("Walking to the trader");
                  }

                  this.leg.cancel();
                  this.leg = null;
                  return legResult.status() != AiOperation.Status.SUCCEEDED
                     ? this.finish(AiOperation.Status.FAILED, "No route to the trader: " + legResult.message())
                     : this.running("Arrived at the trader");
               } else {
                  this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(this.trader));
                  this.aim.tick();
                  if (++this.openAttempts > 80) {
                     return this.finish(AiOperation.Status.FAILED, "Could not open the trade screen");
                  }

                  if (!autismclient.ai.engine.AiAim.isLookingAtEntity(this.trader)) {
                     return this.running("Turning toward the trader");
                  }

                  mc.gameMode
                     .interact(
                        mc.player,
                        this.trader,
                        new EntityHitResult(this.trader, autismclient.ai.engine.AiAim.defaultEntityAimPoint(this.trader)),
                        InteractionHand.MAIN_HAND
                     );
                  return mc.player.containerMenu instanceof MerchantMenu ? null : this.running("Opening the trade screen");
               }
            }

            Entity findTrader() {
               if (entityId < 0) {
                  Entity best = null;
                  double bestDistance = 1024.0;

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof Merchant && entity.isAlive()) {
                        double distance = entity.distanceToSqr(mc.player);
                        if (distance < bestDistance) {
                           bestDistance = distance;
                           best = entity;
                        }
                     }
                  }

                  return best;
               } else {
                  Entity explicit = mc.level.getEntity(entityId);
                  return explicit instanceof Merchant && explicit.isAlive() ? explicit : null;
               }
            }

            int findOfferIndex(MerchantMenu menu) {
               if (tradeIndex >= 0) {
                  return tradeIndex < menu.getOffers().size() ? tradeIndex : -1;
               }

               for (int index = 0; index < menu.getOffers().size(); index++) {
                  MerchantOffer offer = menu.getOffers().get(index);
                  if (wantedTarget != null && wantedTarget.matches(offer.getResult()) && !offer.isOutOfStock()) {
                     return index;
                  }
               }

               for (int index = 0; index < menu.getOffers().size(); index++) {
                  if (wantedTarget != null && wantedTarget.matches(menu.getOffers().get(index).getResult())) {
                     return index;
                  }
               }

               return -1;
            }

            boolean canAfford(MerchantOffer offer) {
               return this.hasCost(offer.getCostA()) && this.hasCost(offer.getCostB());
            }

            int countHeld(ItemStack stack) {
               int total = 0;

               for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
                  ItemStack held = mc.player.getInventory().getItem(slot);
                  if (ItemStack.isSameItemSameComponents(held, stack)) {
                     total += held.getCount();
                  }
               }

               return total;
            }

            boolean hasRoomFor(ItemStack stack) {
               if (AiItems.countFreeSlots() > 0) {
                  return true;
               }

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack held = mc.player.getInventory().getItem(slot);
                  if (!held.isEmpty() && ItemStack.isSameItemSameComponents(held, stack) && held.getCount() < held.getMaxStackSize()) {
                     return true;
                  }
               }

               return false;
            }

            boolean hasCost(ItemStack cost) {
               if (cost.isEmpty()) {
                  return true;
               }

               int needed = cost.getCount();

               for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (ItemStack.isSameItemSameComponents(stack, cost)) {
                     needed -= stack.getCount();
                  }

                  if (needed <= 0) {
                     return true;
                  }
               }

               return false;
            }

            String costText(MerchantOffer offer) {
               StringBuilder text = new StringBuilder(AiTrade.stackText(offer.getCostA()));
               if (!offer.getCostB().isEmpty()) {
                  text.append(" + ").append(AiTrade.stackText(offer.getCostB()));
               }

               return text.toString();
            }

            AiOperation.Result rerollStep(MerchantMenu menu) {
               if (this.rerolls >= rerollAttempts) {
                  return null;
               }

               if (this.rerollPhase != 0) {
                  if (this.rerollPhase == 1) {
                     if (this.leg == null && autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, this.jobSite) == null) {
                        this.leg = AiGoto.approachBlockCompat(this.jobSite, Math.min(60, this.remainingSeconds()));
                        return this.running("Approaching the job-site block");
                     }

                     this.aim.aimAt(Vec3.atCenterOf(this.jobSite));
                     this.aim.tick();
                     switch (this.init_RefQueue_DataUtil.digCompat(this.jobSite)) {
                        case init:
                           return this.running("Breaking the job-site block");
                        case read:
                           this.init_RefQueue_DataUtil.stopCompat();
                           return this.fail(AiFailure.UNREACHABLE, "Could not break the job-site block for a reroll");
                        case load:
                           this.init_RefQueue_DataUtil.stopCompat();
                           this.rerollPhase = 2;
                           this.rerollPhaseTicks = 0;
                           return this.running("Job-site broken; collecting it");
                     }
                  }

                  if (this.rerollPhase == 2) {
                     String itemId = BuiltInRegistries.ITEM.getKey(this.jobSiteItem).toString();
                     if (AiGameState.countInventoryItem(itemId) > 0) {
                        this.rerollPhase = 3;
                        return this.running("Replacing the job-site block");
                     }

                     if (++this.rerollPhaseTicks > 60) {
                        return this.fail(AiFailure.TARGET_GONE, "Could not collect the broken job-site block");
                     }

                     ItemEntity drop = this.findJobSiteDrop(itemId);
                     if (drop != null && mc.player.distanceToSqr(drop) > 2.25) {
                        if (this.leg == null) {
                           this.leg = AiGoto.travelToCompat(drop.blockPosition(), 1.6, false, false, false, 25);
                        }

                        AiOperation.Result legResult = this.leg.tick();
                        if (!legResult.terminal()) {
                           return this.running("Collecting the job-site block");
                        }

                        this.leg.cancel();
                        this.leg = null;
                     }

                     return this.running("Collecting the job-site block");
                  } else {
                     if (this.rerollPhase == 3) {
                        this.placer.start(this.jobSite, this.jobSiteItem);
                        this.rerollPhase = 4;
                        return this.running("Replacing the job-site block");
                     }

                     if (this.rerollPhase == 4) {
                        autismclient.ai.engine.AiPlacer.PlaceStatus status = this.placer.tick();
                        if (status == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                           this.rerolls++;
                           this.rerollPhase = 0;
                           this.rerollPhaseTicks = 0;
                           this.jobSite = null;
                           return null;
                        } else {
                           return status != autismclient.ai.engine.AiPlacer.PlaceStatus.FAILED && ++this.rerollPhaseTicks <= 100
                              ? this.running("Replacing the job-site block")
                              : this.fail(AiFailure.UNREACHABLE, "Could not replace the job-site block: " + this.placer.failureReason());
                        }
                     } else {
                        return null;
                     }
                  }
               } else {
                  List<Integer> uses = new ArrayList<>();

                  for (MerchantOffer offer : menu.getOffers()) {
                     uses.add(offer.getUses());
                  }

                  if (!AiTrade.isUntraded(uses)) {
                     return this.fail(AiFailure.IMPOSSIBLE_STATE, "reroll refused: the trader has already been traded with");
                  }

                  this.jobSite = this.findJobSite();
                  if (this.jobSite == null) {
                     return this.fail(AiFailure.UNREACHABLE, "No job-site block near the trader to break for a reroll");
                  }

                  this.jobSiteItem = mc.level.getBlockState(this.jobSite).getBlock().asItem();
                  this.rerollPhase = 1;
                  this.rerollPhaseTicks = 0;
                  mc.player.closeContainer();
                  this.menuContainerId = -1;
                  return this.running("Breaking the job-site block for reroll " + (this.rerolls + 1) + "/" + rerollAttempts);
               }
            }

            BlockPos findJobSite() {
               if (this.trader != null && mc.level != null) {
                  BlockPos origin = this.trader.blockPosition();
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;

                  for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-16, -8, -16), origin.offset(16, 8, 16))) {
                     if (AiTerrain.isLoaded(mc.level, pos) && AiTrade.JOB_SITE_BLOCKS.contains(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(pos).getBlock()).toString())) {
                        double distance = pos.distSqr(origin);
                        if (distance < bestDistance) {
                           bestDistance = distance;
                           best = pos.immutable();
                        }
                     }
                  }

                  return best;
               } else {
                  return null;
               }
            }

            ItemEntity findJobSiteDrop(String itemId) {
               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof ItemEntity item
                     && item.isAlive()
                     && BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString().equals(itemId)
                     && item.position().distanceToSqr(Vec3.atCenterOf(this.jobSite)) <= 64.0) {
                     return item;
                  }
               }

               return null;
            }

            void selectSlot(int slot) {
               if (mc.player.getInventory().getSelectedSlot() != slot) {
                  mc.player.getInventory().setSelectedSlot(slot);
               }
            }

            int remainingSeconds() {
               return (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L);
            }

            JsonObject progressJson() {
               JsonObject out = AiJson.objectOf(
                  "action", action.name().toLowerCase(Locale.ROOT), "completed", this.completedTrades, "requested", count, "restocked", this.restocked, "rerolls", this.rerolls
               );
               if (this.trader != null) {
                  out.add(
                     "trader",
                     AiJson.objectOf(
                        "entityId",
                        this.trader.getId(),
                        "type",
                        BuiltInRegistries.ENTITY_TYPE.getKey(this.trader.getType()).toString(),
                        "x",
                        this.trader.getBlockX(),
                        "y",
                        this.trader.getBlockY(),
                        "z",
                        this.trader.getBlockZ()
                     )
                  );
               }

               if (mc.player != null && mc.player.containerMenu instanceof MerchantMenu menu) {
                  JsonArray offers = new JsonArray();

                  for (int index = 0; index < menu.getOffers().size(); index++) {
                     MerchantOffer offer = menu.getOffers().get(index);
                     offers.add(
                        AiJson.objectOf(
                           "index",
                           index,
                           "costA",
                           AiTrade.stackText(offer.getCostA()),
                           "costB",
                           offer.getCostB().isEmpty() ? null : AiTrade.stackText(offer.getCostB()),
                           "result",
                           AiTrade.stackText(offer.getResult()),
                           "resultItem",
                           BuiltInRegistries.ITEM.getKey(offer.getResult().getItem()).toString(),
                           "uses",
                           offer.getUses(),
                           "maxUses",
                           offer.getMaxUses(),
                           "outOfStock",
                           offer.isOutOfStock(),
                           "affordable",
                           this.canAfford(offer)
                        )
                     );
                  }

                  out.add("offers", offers);
                  out.addProperty("traderLevel", menu.getTraderLevel());
                  out.addProperty("traderXp", menu.getTraderXp());
               }

               if (this.offerIndex >= 0) {
                  out.addProperty("selectedIndex", this.offerIndex);
               }

               return out;
            }

            AiOperation.Result running(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressJson().toString(), List.of());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               JsonObject data = this.progressJson();
               this.cancel();
               return new AiOperation.Result(status, message, data.toString(), List.of());
            }

            AiOperation.Result fail(AiFailure code, String message) {
               JsonObject data = this.progressJson();
               this.cancel();
               return AiOperation.Result.failed(code, message, data.toString());
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  this.aim.clear();
                  this.init_RefQueue_DataUtil.stopCompat();
                  this.placer.reset();
                  if (this.leg != null) {
                     this.leg.cancel();
                     this.leg = null;
                  }

                  if (mc.player != null && mc.player.getInventory().getSelectedSlot() != this.originalSlot) {
                     mc.player.getInventory().setSelectedSlot(this.originalSlot);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Trading requires a connected living player");
      }
   }

   private static String stackText(ItemStack stack) {
      return stack.isEmpty() ? "none" : stack.getCount() + "x " + BuiltInRegistries.ITEM.getKey(stack.getItem());
   }

   private static AiTrade.Inner_DataUtil parseAction(String text) {
      try {
         return AiTrade.Inner_DataUtil.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("action must be inspect or trade");
      }
   }

   enum Inner_DataUtil {
      INSPECT,
      TRADE;
   }
}
