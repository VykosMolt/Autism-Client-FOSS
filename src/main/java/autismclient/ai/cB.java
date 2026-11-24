package autismclient.ai;

import autismclient.ai.a.HashUtil;
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

final class cB {
   private static final int init_I = 80;
   private static final int load = 40;
   private static final int read = 30;
   private static final double init_D = 32.0;
   private static final int write = 3000;
   static final Set<String> init_Set = Set.of(
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

   private cB() {
   }

   static boolean init(List<Integer> offerUses) {
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
         final cB.Inner_DataUtil action = init(ag.init_Str_JsonObject(args, "action", "inspect"));
         final int entityId = ag.init_I_JsonObject(args, "entityId", -1);
         final int tradeIndex = ag.init_I_JsonObject(args, "tradeIndex", -1);
         final String wanted = ag.init_Str_JsonObject(args, "item", "");
         final int count = Math.max(1, Math.min(256, ag.init_I_JsonObject(args, "count", 1)));
         final int timeoutSeconds = Math.max(5, Math.min(900, ag.init_I_JsonObject(args, "timeoutSeconds", 120)));
         final boolean waitRestock = ag.init_Z_JsonObject(args, "waitRestock", false);
         final boolean reroll = ag.init_Z_JsonObject(args, "reroll", false);
         final int rerollAttempts = Math.max(1, Math.min(8, ag.init_I_JsonObject(args, "rerollAttempts", 3)));
         if (action == cB.Inner_DataUtil.load && tradeIndex < 0 && wanted.isBlank()) {
            throw new IllegalArgumentException("trade needs tradeIndex from an inspect listing, or item to select the offer by its result");
         }

         final af.Inner_DataUtil wantedTarget = wanted.isBlank() ? null : af.load_af_DataUtil(wanted);
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
            final autismclient.ai.a.DataPipe init_DataPipe = new autismclient.ai.a.DataPipe();
            final autismclient.ai.a.AiBlockDigger init_RefQueue_DataUtil = new autismclient.ai.a.AiBlockDigger(
               mc, this::init, this.init_DataUtil::init_V_Vec3, true, true, this.init_DataUtil::load_V
            );
            AiOperation init_AiOperation;
            Entity init_Entity;
            int init_I = -1;
            int load_I;
            int read_I = -1;
            int write;
            int update;
            int sync;
            long load_J = -1L;
            final int flush = mc.player.getInventory().getSelectedSlot();
            ItemStack init_ItemStack = ItemStack.EMPTY;
            int close;
            boolean init_Z;
            boolean load_Z;
            boolean read_Z;
            int open;
            int poll;
            int push;
            int pop;
            BlockPos init_BlockPos;
            Item init_Item;

            @Override
            public AiOperation.Result tick() {
               if (!this.init_Z && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.init_J) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Trade deadline reached after " + this.sync + " trade(s)");
                  }

                  if (!(mc.player.containerMenu instanceof MerchantMenu)) {
                     if (this.init_I >= 0) {
                        return this.init(
                           this.sync > 0 ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED, "The trade screen closed after " + this.sync + " trade(s)"
                        );
                     }

                     AiOperation.Result open = this.init_AiOperation_Result_30();
                     if (open != null) {
                        return open;
                     }
                  }

                  MerchantMenu menu = (MerchantMenu)mc.player.containerMenu;
                  this.init_I = menu.containerId;
                  if (menu.getOffers().isEmpty()) {
                     return this.init(AiOperation.Status.FAILED, "This trader has no offers");
                  }

                  if (action == cB.Inner_DataUtil.init) {
                     return this.init(AiOperation.Status.SUCCEEDED, "Read " + menu.getOffers().size() + " offer(s)");
                  }

                  if (this.load_Z) {
                     MerchantOffer offer = menu.getOffers().get(this.read_I);
                     if (!offer.isOutOfStock()) {
                        this.load_Z = false;
                        this.open = 0;
                        this.read_Z = true;
                        return this.init_AiOperation_Result_12("Offer restocked");
                     } else if (++this.open <= 3000) {
                        return this.init_AiOperation_Result_12("Waiting for the trader to restock");
                     } else {
                        return this.sync > 0
                           ? this.init(AiOperation.Status.SUCCEEDED, "Offer did not restock within the wait cap after " + this.sync + " trade(s)")
                           : this.init(A.clear, "Offer did not restock within the wait cap after " + this.sync + " trade(s)");
                     }
                  } else {
                     if (this.read_I < 0) {
                        this.read_I = this.init_I(menu);
                        if (this.read_I < 0) {
                           if (wantedTarget == null || !reroll) {
                              return this.init(
                                 AiOperation.Status.FAILED,
                                 wantedTarget == null
                                    ? "Trade index " + tradeIndex + " is not an offer on this trader"
                                    : "No offer on this trader produces " + wanted
                              );
                           }

                           AiOperation.Result rolling = this.init_AiOperation_Result(menu);
                           if (rolling != null) {
                              return rolling;
                           }

                           this.read_I = this.init_I(menu);
                           if (this.read_I < 0) {
                              if (this.poll >= rerollAttempts) {
                                 return this.init(A.clear, "No offer on this trader produces " + wanted + " after " + this.poll + " reroll(s)");
                              }

                              return this.init_AiOperation_Result_12("Rerolled offers lack " + wanted + "; rolling again");
                           }
                        }

                        MerchantOffer offer = menu.getOffers().get(this.read_I);
                        if (offer.isOutOfStock()) {
                           if (waitRestock) {
                              this.load_Z = true;
                              this.open = 0;
                              return this.init_AiOperation_Result_12("Offer is out of stock; waiting for the trader to restock");
                           }

                           return this.init(A.clear, "Offer " + this.read_I + " is out of stock until the trader restocks");
                        }

                        if (!this.init_Z(offer)) {
                           return this.init(AiOperation.Status.FAILED, "Cannot afford offer " + this.read_I + ": needs " + this.init_Str(offer));
                        }
                     }

                     if (this.sync >= count) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Completed " + this.sync + " trade(s)");
                     }

                     MerchantOffer offer = menu.getOffers().get(this.read_I);
                     if (offer.isOutOfStock()) {
                        if (waitRestock) {
                           this.load_Z = true;
                           this.open = 0;
                           return this.init_AiOperation_Result_12("Offer ran out of stock; waiting for the trader to restock");
                        } else {
                           return this.sync > 0
                              ? this.init(AiOperation.Status.SUCCEEDED, "Offer ran out of stock after " + this.sync + " of " + count + " trade(s)")
                              : this.init(A.clear, "Offer ran out of stock after " + this.sync + " of " + count + " trade(s)");
                        }
                     } else {
                        if (!this.init_Z(offer)) {
                           return this.init(
                              this.sync > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                              "Ran out of payment after " + this.sync + " of " + count + " trade(s); needs " + this.init_Str(offer)
                           );
                        }

                        if (this.load_J < 0L) {
                           mc.getConnection().send(new ServerboundSelectTradePacket(this.read_I));
                           menu.setSelectionHint(this.read_I);
                           menu.tryMoveItems(this.read_I);
                           ItemStack result = menu.getSlot(2).getItem();
                           if (!result.isEmpty()) {
                              this.write = 0;
                              if (!this.init_Z_2(result)) {
                                 return this.init(
                                    this.sync > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                                    "No inventory room for the trade result after " + this.sync + " of " + count + " trade(s)"
                                 );
                              }

                              this.load_J = ca.init_J_I(this.init_I);
                              this.init_ItemStack = result.copy();
                              this.close = this.init_I(result);
                              PkgCls_b.init(this.init_I, 2, 0, ContainerInput.QUICK_MOVE);
                              return this.init_AiOperation_Result_12("Taking trade " + (this.sync + 1) + "/" + count);
                           } else {
                              return ++this.write > 30
                                 ? this.init(
                                    this.sync > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                                    "The trader did not fill the result slot after " + this.sync + " trade(s)"
                                 )
                                 : this.init_AiOperation_Result_12("Selecting offer " + this.read_I);
                           }
                        } else if (ca.init_J_I(this.init_I) <= this.load_J) {
                           return ++this.update > 40
                              ? this.init(AiOperation.Status.UNCERTAIN, "A trade was not confirmed by the server; reobserve before repeating")
                              : this.init_AiOperation_Result_12("Waiting for the server to apply the trade");
                        } else if (this.init_I(this.init_ItemStack) > this.close) {
                           this.sync++;
                           this.load_J = -1L;
                           this.init_ItemStack = ItemStack.EMPTY;
                           this.update = 0;
                           this.write = 0;
                           return this.init_AiOperation_Result_12("Trade " + this.sync + "/" + count + " confirmed");
                        } else {
                           this.load_J = -1L;
                           this.init_ItemStack = ItemStack.EMPTY;
                           this.update = 0;
                           return this.init(
                              AiOperation.Status.UNCERTAIN, "The server updated but no trade result reached the inventory; reobserve before repeating"
                           );
                        }
                     }
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Trade cancelled");
               }
            }

            AiOperation.Result init_AiOperation_Result_30() {
               if (this.init_Entity == null || !this.init_Entity.isAlive()) {
                  this.init_Entity = this.init_Entity_6();
                  this.load_I = 0;
                  if (this.init_Entity == null) {
                     return this.init(
                        AiOperation.Status.FAILED,
                        entityId >= 0 ? "Entity " + entityId + " is not a live trader" : "No villager or wandering trader within 32 blocks"
                     );
                  }
               }

               double reach = Math.max(3.0, mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE));
               if (mc.player.distanceTo(this.init_Entity) > reach * 0.9) {
                  if (this.init_AiOperation == null) {
                     this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(
                        this.init_Entity.blockPosition(), 2.0, false, false, true, Math.min(120, this.init_I_31())
                     );
                  }

                  AiOperation.Result legResult = this.init_AiOperation.tick();
                  if (!legResult.terminal()) {
                     return this.init_AiOperation_Result_12("Walking to the trader");
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  return legResult.status() != AiOperation.Status.SUCCEEDED
                     ? this.init(AiOperation.Status.FAILED, "No route to the trader: " + legResult.message())
                     : this.init_AiOperation_Result_12("Arrived at the trader");
               } else {
                  this.init_DataUtil.init_V_Vec3(autismclient.ai.a.DataUtil.load_Vec3_Entity(this.init_Entity));
                  this.init_DataUtil.load_V();
                  if (++this.load_I > 80) {
                     return this.init(AiOperation.Status.FAILED, "Could not open the trade screen");
                  }

                  if (!autismclient.ai.a.DataUtil.init_Z_Entity(this.init_Entity)) {
                     return this.init_AiOperation_Result_12("Turning toward the trader");
                  }

                  mc.gameMode
                     .interact(
                        mc.player,
                        this.init_Entity,
                        new EntityHitResult(this.init_Entity, autismclient.ai.a.DataUtil.init_Vec3_Entity(this.init_Entity)),
                        InteractionHand.MAIN_HAND
                     );
                  return mc.player.containerMenu instanceof MerchantMenu ? null : this.init_AiOperation_Result_12("Opening the trade screen");
               }
            }

            Entity init_Entity_6() {
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

            int init_I(MerchantMenu menu) {
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

            boolean init_Z(MerchantOffer offer) {
               return this.load(offer.getCostA()) && this.load(offer.getCostB());
            }

            int init_I(ItemStack stack) {
               int total = 0;

               for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
                  ItemStack held = mc.player.getInventory().getItem(slot);
                  if (ItemStack.isSameItemSameComponents(held, stack)) {
                     total += held.getCount();
                  }
               }

               return total;
            }

            boolean init_Z_2(ItemStack stack) {
               if (af.init_I() > 0) {
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

            boolean load(ItemStack cost) {
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

            String init_Str(MerchantOffer offer) {
               StringBuilder text = new StringBuilder(cB.init(offer.getCostA()));
               if (!offer.getCostB().isEmpty()) {
                  text.append(" + ").append(cB.init(offer.getCostB()));
               }

               return text.toString();
            }

            AiOperation.Result init_AiOperation_Result(MerchantMenu menu) {
               if (this.poll >= rerollAttempts) {
                  return null;
               }

               if (this.push != 0) {
                  if (this.push == 1) {
                     if (this.init_AiOperation == null && autismclient.ai.a.CachePool.init(mc, this.init_BlockPos) == null) {
                        this.init_AiOperation = HashUtil.init_AiOperation(this.init_BlockPos, Math.min(60, this.init_I_31()));
                        return this.init_AiOperation_Result_12("Approaching the job-site block");
                     }

                     this.init_DataUtil.init_V_Vec3(Vec3.atCenterOf(this.init_BlockPos));
                     this.init_DataUtil.load_V();
                     switch (this.init_RefQueue_DataUtil.init_RefQueue_DataUtil__BlockPos(this.init_BlockPos)) {
                        case init:
                           return this.init_AiOperation_Result_12("Breaking the job-site block");
                        case read:
                           this.init_RefQueue_DataUtil.init_V();
                           return this.init(A.close, "Could not break the job-site block for a reroll");
                        case load:
                           this.init_RefQueue_DataUtil.init_V();
                           this.push = 2;
                           this.pop = 0;
                           return this.init_AiOperation_Result_12("Job-site broken; collecting it");
                     }
                  }

                  if (this.push == 2) {
                     String itemId = BuiltInRegistries.ITEM.getKey(this.init_Item).toString();
                     if (S.init_I_Str(itemId) > 0) {
                        this.push = 3;
                        return this.init_AiOperation_Result_12("Replacing the job-site block");
                     }

                     if (++this.pop > 60) {
                        return this.init(A.add, "Could not collect the broken job-site block");
                     }

                     ItemEntity drop = this.init_ItemEntity(itemId);
                     if (drop != null && mc.player.distanceToSqr(drop) > 2.25) {
                        if (this.init_AiOperation == null) {
                           this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(drop.blockPosition(), 1.6, false, false, false, 25);
                        }

                        AiOperation.Result legResult = this.init_AiOperation.tick();
                        if (!legResult.terminal()) {
                           return this.init_AiOperation_Result_12("Collecting the job-site block");
                        }

                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                     }

                     return this.init_AiOperation_Result_12("Collecting the job-site block");
                  } else {
                     if (this.push == 3) {
                        this.init_DataPipe.init_V_BlockPos_Item(this.init_BlockPos, this.init_Item);
                        this.push = 4;
                        return this.init_AiOperation_Result_12("Replacing the job-site block");
                     }

                     if (this.push == 4) {
                        autismclient.ai.a.DataPipe.Inner_DataUtil status = this.init_DataPipe.init_DataPipe_DataUtil();
                        if (status == autismclient.ai.a.DataPipe.Inner_DataUtil.load) {
                           this.poll++;
                           this.push = 0;
                           this.pop = 0;
                           this.init_BlockPos = null;
                           return null;
                        } else {
                           return status != autismclient.ai.a.DataPipe.Inner_DataUtil.read && ++this.pop <= 100
                              ? this.init_AiOperation_Result_12("Replacing the job-site block")
                              : this.init(A.close, "Could not replace the job-site block: " + this.init_DataPipe.init_Str());
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

                  if (!cB.init(uses)) {
                     return this.init(A.data, "reroll refused: the trader has already been traded with");
                  }

                  this.init_BlockPos = this.init_BlockPos_11();
                  if (this.init_BlockPos == null) {
                     return this.init(A.close, "No job-site block near the trader to break for a reroll");
                  }

                  this.init_Item = mc.level.getBlockState(this.init_BlockPos).getBlock().asItem();
                  this.push = 1;
                  this.pop = 0;
                  mc.player.closeContainer();
                  this.init_I = -1;
                  return this.init_AiOperation_Result_12("Breaking the job-site block for reroll " + (this.poll + 1) + "/" + rerollAttempts);
               }
            }

            BlockPos init_BlockPos_11() {
               if (this.init_Entity != null && mc.level != null) {
                  BlockPos origin = this.init_Entity.blockPosition();
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;

                  for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-16, -8, -16), origin.offset(16, 8, 16))) {
                     if (cu.init(mc.level, pos) && cB.init_Set.contains(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(pos).getBlock()).toString())) {
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

            ItemEntity init_ItemEntity(String itemId) {
               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof ItemEntity item
                     && item.isAlive()
                     && BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString().equals(itemId)
                     && item.position().distanceToSqr(Vec3.atCenterOf(this.init_BlockPos)) <= 64.0) {
                     return item;
                  }
               }

               return null;
            }

            void init(int slot) {
               if (mc.player.getInventory().getSelectedSlot() != slot) {
                  mc.player.getInventory().setSelectedSlot(slot);
               }
            }

            int init_I_31() {
               return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
            }

            JsonObject init_JsonObject_47() {
               JsonObject out = ag.init_JsonObject_ObjArr(
                  "action", action.name().toLowerCase(Locale.ROOT), "completed", this.sync, "requested", count, "restocked", this.read_Z, "rerolls", this.poll
               );
               if (this.init_Entity != null) {
                  out.add(
                     "trader",
                     ag.init_JsonObject_ObjArr(
                        "entityId",
                        this.init_Entity.getId(),
                        "type",
                        BuiltInRegistries.ENTITY_TYPE.getKey(this.init_Entity.getType()).toString(),
                        "x",
                        this.init_Entity.getBlockX(),
                        "y",
                        this.init_Entity.getBlockY(),
                        "z",
                        this.init_Entity.getBlockZ()
                     )
                  );
               }

               if (mc.player != null && mc.player.containerMenu instanceof MerchantMenu menu) {
                  JsonArray offers = new JsonArray();

                  for (int index = 0; index < menu.getOffers().size(); index++) {
                     MerchantOffer offer = menu.getOffers().get(index);
                     offers.add(
                        ag.init_JsonObject_ObjArr(
                           "index",
                           index,
                           "costA",
                           cB.init(offer.getCostA()),
                           "costB",
                           offer.getCostB().isEmpty() ? null : cB.init(offer.getCostB()),
                           "result",
                           cB.init(offer.getResult()),
                           "resultItem",
                           BuiltInRegistries.ITEM.getKey(offer.getResult().getItem()).toString(),
                           "uses",
                           offer.getUses(),
                           "maxUses",
                           offer.getMaxUses(),
                           "outOfStock",
                           offer.isOutOfStock(),
                           "affordable",
                           this.init_Z(offer)
                        )
                     );
                  }

                  out.add("offers", offers);
                  out.addProperty("traderLevel", menu.getTraderLevel());
                  out.addProperty("traderXp", menu.getTraderXp());
               }

               if (this.read_I >= 0) {
                  out.addProperty("selectedIndex", this.read_I);
               }

               return out;
            }

            AiOperation.Result init_AiOperation_Result_12(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_47().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               JsonObject data = this.init_JsonObject_47();
               this.cancel();
               return new AiOperation.Result(status, message, data.toString(), List.of());
            }

            AiOperation.Result init(A code, String message) {
               JsonObject data = this.init_JsonObject_47();
               this.cancel();
               return AiOperation.Result.failed(code, message, data.toString());
            }

            @Override
            public void cancel() {
               if (!this.init_Z) {
                  this.init_Z = true;
                  this.init_DataUtil.init_V();
                  this.init_RefQueue_DataUtil.init_V();
                  this.init_DataPipe.init_V();
                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }

                  if (mc.player != null && mc.player.getInventory().getSelectedSlot() != this.flush) {
                     mc.player.getInventory().setSelectedSlot(this.flush);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Trading requires a connected living player");
      }
   }

   private static String init(ItemStack stack) {
      return stack.isEmpty() ? "none" : stack.getCount() + "x " + BuiltInRegistries.ITEM.getKey(stack.getItem());
   }

   private static cB.Inner_DataUtil init(String text) {
      try {
         return cB.Inner_DataUtil.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("action must be inspect or trade");
      }
   }

   enum Inner_DataUtil {
      init,
      load;
   }
}
