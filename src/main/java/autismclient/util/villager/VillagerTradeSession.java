package autismclient.util.villager;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

public final class VillagerTradeSession {
   private static VillagerTradeSession active;
   private final TradeOfferFilter filter;
   private final int limit;
   private final int delayTicks;
   private final int timeoutTicks;
   private final int emeraldBudget;
   private final int emeraldReserve;
   private VillagerTradeSession.MenuView view;
   private Object boundMenu;
   private Object boundLevel;
   private Object boundConnection;
   private VillagerTradeSession.Phase phase = VillagerTradeSession.Phase.SELECT;
   private VillagerTradeSession.Status status = VillagerTradeSession.Status.RUNNING;
   private int waitTicks;
   private int completedTrades;
   private int spentEmeralds;
   private int offerIndex = -1;
   private int offerCost;
   private int cursorRemainder;
   private int pop;
   private int tick;
   private ItemStack expectedOutput = ItemStack.EMPTY;
   private ItemStack serverCursor = ItemStack.EMPTY;
   private int heldBefore;
   private int get;
   private String message = "Selecting trade";
   private MerchantOffer offer;
   private int offerUsesBefore;

   public VillagerTradeSession(TradeOfferFilter filter, int limit, int delay, int timeout, int budget, int reserve) {
      this.filter = filter;
      this.limit = Math.max(1, Math.min(10000, limit));
      this.delayTicks = Math.max(1, Math.min(200, delay));
      this.timeoutTicks = Math.max(20, Math.min(1200, timeout));
      this.emeraldBudget = Math.max(0, budget);
      this.emeraldReserve = Math.max(0, reserve);
   }

   public static boolean isRunning() {
      return active != null;
   }

   public boolean isFinished() {
      return this.status != VillagerTradeSession.Status.RUNNING;
   }

   public VillagerTradeSession.Status status() {
      return this.status;
   }

   public int traded() {
      return this.completedTrades;
   }

   public int spent() {
      return this.spentEmeralds;
   }

   public String message() {
      return this.message;
   }

   public boolean reserve() {
      if (active != null && active != this) {
         this.finish(VillagerTradeSession.Status.BUSY, "Another trade is running");
         return false;
      } else {
         active = this;
         return true;
      }
   }

   public void cancel() {
      if (this.view != null) {
         this.view.close();
      }
   }

   public static void onContainerContent(ClientboundContainerSetContentPacket packet) {
      VillagerTradeSession session = active;
      if (session != null && session.view != null && packet.containerId() == session.view.containerId()) {
         session.serverCursor = packet.carriedItem().copy();
         int count = 0;

         for (int i = 3; i < Math.min(39, packet.items().size()); i++) {
            ItemStack stack = packet.items().get(i);
            if (ItemStack.isSameItemSameComponents(stack, session.expectedOutput)) {
               count += stack.getCount();
            }
         }

         session.get = count;
         session.pop++;
      }
   }

   public VillagerTradeSession.Status tick(final Minecraft mc) {
      if (this.isFinished()) {
         return this.status;
      } else {
         return mc.player != null
               && mc.gameMode != null
               && mc.getConnection() != null
               && mc.level != null
               && mc.player.containerMenu instanceof MerchantMenu current
            ? this.step(
               new VillagerTradeSession.MenuView() {
                  @Override
                  public Object menu() {
                     return current;
                  }

                  @Override
                  public Object level() {
                     return mc.level;
                  }

                  @Override
                  public Object connectionToken() {
                     return mc.getConnection();
                  }

                  @Override
                  public int containerId() {
                     return current.containerId;
                  }

                  @Override
                  public List<MerchantOffer> offers() {
                     return current.getOffers();
                  }

                  @Override
                  public ItemStack slotItem(int slot) {
                     return current.getSlot(slot).getItem();
                  }

                  @Override
                  public ItemStack carried() {
                     return current.getCarried();
                  }

                  @Override
                  public int slotCount() {
                     return current.slots.size();
                  }

                  @Override
                  public int roomInSlot(int index, ItemStack stack) {
                     Slot slot = current.getSlot(index);
                     ItemStack existing = slot.getItem();
                     return !slot.mayPlace(stack) || !existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)
                        ? 0
                        : Math.max(0, Math.min(stack.getMaxStackSize(), slot.getMaxStackSize(stack)) - existing.getCount());
                  }

                  @Override
                  public void selectTrade(int index) {
                     current.setSelectionHint(index);
                     mc.getConnection().send(new ServerboundSelectTradePacket(index));
                  }

                  @Override
                  public void pickUpSlot(int slot) {
                     mc.getConnection()
                        .send(
                           new ServerboundContainerClickPacket(
                              current.containerId, -1, (short)slot, (byte)0, ContainerInput.PICKUP, new Int2ObjectOpenHashMap<>(), HashedStack.EMPTY
                           )
                        );
                  }

                  @Override
                  public void close() {
                     if (mc.player != null && mc.player.containerMenu == current) {
                        mc.player.closeContainer();
                     }
                  }
               }
            )
            : this.finish(VillagerTradeSession.Status.NO_MENU, "Open a villager trade screen");
      }
   }

   VillagerTradeSession.Status step(VillagerTradeSession.MenuView current) {
      if (this.isFinished()) {
         return this.status;
      }

      if (active != null && active != this) {
         return this.finish(VillagerTradeSession.Status.BUSY, "Another trade is running");
      }

      if (this.boundMenu == null) {
         if (!current.carried().isEmpty()) {
            return this.finish(VillagerTradeSession.Status.CURSOR_BUSY, "Empty the cursor first");
         }

         this.view = current;
         this.boundMenu = current.menu();
         this.boundLevel = current.level();
         this.boundConnection = current.connectionToken();
         active = this;
      }

      if (this.boundMenu == current.menu() && this.boundLevel == current.level() && this.boundConnection == current.connectionToken()) {
         this.view = current;
         if (this.phase != VillagerTradeSession.Phase.TAKE && this.phase != VillagerTradeSession.Phase.STORED) {
            if (this.phase == VillagerTradeSession.Phase.DELAY) {
               if (++this.waitTicks >= this.delayTicks) {
                  this.phase = VillagerTradeSession.Phase.SELECT;
                  this.waitTicks = 0;
               }

               return this.status;
            } else if (this.phase == VillagerTradeSession.Phase.SELECT) {
               if (this.view.offers().isEmpty() && ++this.waitTicks < this.timeoutTicks) {
                  return this.status;
               }

               boolean match = false;
               boolean stock = false;
               boolean budgetOk = false;
               this.offerIndex = -1;

               for (int i = 0; i < this.view.offers().size(); i++) {
                  MerchantOffer offer = this.view.offers().get(i);
                  if (this.filter.matches(offer, i)) {
                     match = true;
                     if (!offer.isOutOfStock()) {
                        stock = true;
                        if (TradeOfferFilter.affordable(TradeOfferFilter.cost(offer), this.spentEmeralds, this.emeraldBudget, this.emeraldCount(), this.emeraldReserve)) {
                           budgetOk = true;
                           if (this.hasPayment(offer)) {
                              this.offerIndex = i;
                              break;
                           }
                        }
                     }
                  }
               }

               if (this.offerIndex < 0) {
                  return this.finish(
                     !match
                        ? VillagerTradeSession.Status.NO_MATCH
                        : (!stock ? VillagerTradeSession.Status.OUT_OF_STOCK : (!budgetOk ? VillagerTradeSession.Status.BUDGET : VillagerTradeSession.Status.MISSING_INPUTS)),
                     !match ? "No matching offer" : (!stock ? "Waiting for restock" : (!budgetOk ? "Emerald limit reached" : "Missing payment items"))
                  );
               }

               if (!this.view.carried().isEmpty()) {
                  return this.finish(VillagerTradeSession.Status.CURSOR_BUSY, "Cursor changed");
               }

               this.view.selectTrade(this.offerIndex);
               this.phase = VillagerTradeSession.Phase.FILL;
               this.waitTicks = 0;
               this.message = "Filling offer " + (this.offerIndex + 1);
               return this.status;
            } else if (this.phase == VillagerTradeSession.Phase.FILL) {
               if (this.offerIndex >= this.view.offers().size()) {
                  return this.finish(VillagerTradeSession.Status.NO_MATCH, "Offers changed");
               }

               MerchantOffer offer = this.view.offers().get(this.offerIndex);
               if (!this.filter.matches(offer, this.offerIndex) || offer.isOutOfStock()) {
                  return this.finish(VillagerTradeSession.Status.NO_MATCH, "Offer changed");
               }

               if (!this.view.carried().isEmpty()) {
                  return this.finish(VillagerTradeSession.Status.CURSOR_BUSY, "Cursor changed");
               }

               ItemStack output = this.view.slotItem(2);
               if (ItemStack.matches(output, offer.getResult())
                  && offer.satisfiedBy(this.view.slotItem(0), this.view.slotItem(1))) {
                  this.expectedOutput = output.copy();
                  this.offerCost = TradeOfferFilter.cost(offer);
                  if (!TradeOfferFilter.affordable(this.offerCost, this.spentEmeralds, this.emeraldBudget, this.emeraldCount(), this.emeraldReserve)) {
                     return this.finish(VillagerTradeSession.Status.BUDGET, "Emerald limit reached");
                  }

                  if (this.inventoryRoom(this.expectedOutput) < this.expectedOutput.getCount()) {
                     return this.finish(VillagerTradeSession.Status.INVENTORY_FULL, "Inventory full");
                  }

                  this.offer = offer;
                  this.offerUsesBefore = offer.getUses();
                  this.pickUp(2);
                  this.phase = VillagerTradeSession.Phase.TAKE;
                  this.message = "Confirming purchase";
                  return this.status;
               } else {
                  return ++this.waitTicks >= this.timeoutTicks ? this.finish(VillagerTradeSession.Status.MISSING_INPUTS, "Payment was not filled") : this.status;
               }
            } else {
               if (this.phase == VillagerTradeSession.Phase.STORE) {
                  ItemStack cursor = this.view.carried();
                  if (cursor.isEmpty() || !ItemStack.isSameItemSameComponents(cursor, this.expectedOutput)) {
                     return this.finish(VillagerTradeSession.Status.UNCERTAIN, "Cursor changed during trade");
                  }

                  this.expectedOutput = cursor.copy();
                  int slot = this.firstSlotWithRoom(this.expectedOutput);
                  if (slot < 0) {
                     return this.finish(VillagerTradeSession.Status.INVENTORY_FULL, "Inventory full; result is on cursor");
                  }

                  int room = this.roomAt(slot, this.expectedOutput);
                  this.cursorRemainder = Math.max(0, this.expectedOutput.getCount() - room);
                  this.heldBefore = this.countInInventory(this.expectedOutput);
                  this.pickUp(slot);
                  this.phase = VillagerTradeSession.Phase.STORED;
                  this.message = "Storing purchase";
               }

               return this.status;
            }
         } else {
            if (this.pop > this.tick) {
               if (this.phase == VillagerTradeSession.Phase.TAKE) {
                  if (!ItemStack.matches(this.serverCursor, this.expectedOutput)) {
                     return this.finish(VillagerTradeSession.Status.REJECTED, "Purchase was not confirmed");
                  }

                  this.completedTrades++;
                  this.spentEmeralds = this.spentEmeralds + this.offerCost;
                  if (this.view.offers().contains(this.offer) && this.offer.getUses() == this.offerUsesBefore) {
                     this.offer.increaseUses();
                  }

                  this.phase = VillagerTradeSession.Phase.STORE;
               } else {
                  boolean cursorMatches = this.cursorRemainder == 0
                     ? this.serverCursor.isEmpty()
                     : ItemStack.isSameItemSameComponents(this.serverCursor, this.expectedOutput) && this.serverCursor.getCount() == this.cursorRemainder;
                  if (!cursorMatches || this.get != this.heldBefore + this.expectedOutput.getCount() - this.cursorRemainder) {
                     return this.finish(VillagerTradeSession.Status.UNCERTAIN, "Inventory transfer was not confirmed");
                  }

                  if (this.cursorRemainder == 0) {
                     if (this.completedTrades >= this.limit) {
                        return this.finish(VillagerTradeSession.Status.COMPLETE, "Completed " + this.completedTrades + " trades");
                     }

                     this.phase = VillagerTradeSession.Phase.DELAY;
                  } else {
                     this.phase = VillagerTradeSession.Phase.STORE;
                  }
               }

               this.waitTicks = 0;
            } else if (++this.waitTicks >= this.timeoutTicks) {
               return this.finish(VillagerTradeSession.Status.UNCERTAIN, "Server confirmation timed out; purchase not repeated");
            }

            return this.status;
         }
      } else {
         return this.finish(VillagerTradeSession.Status.CANCELLED, "Trade screen changed");
      }
   }

   private void pickUp(int slot) {
      this.tick = this.pop;
      this.waitTicks = 0;
      this.view.pickUpSlot(slot);
   }

   private boolean hasPayment(MerchantOffer offer) {
      int a = 0;
      int b = 0;

      for (int i = 0; i < Math.min(39, this.view.slotCount()); i++) {
         if (i != 2) {
            ItemStack stack = this.view.slotItem(i);
            if (offer.getItemCostA().test(stack)) {
               a += stack.getCount();
            }

            if (offer.getItemCostB().isPresent() && offer.getItemCostB().get().test(stack)) {
               b += stack.getCount();
            }
         }
      }

      return offer.getItemCostB().isPresent() && offer.getItemCostA().test(offer.getCostB())
         ? a >= offer.getCostA().getCount() + offer.getCostB().getCount()
         : a >= offer.getCostA().getCount() && b >= offer.getCostB().getCount();
   }

   private int emeraldCount() {
      int count = 0;

      for (int i = 0; i < Math.min(39, this.view.slotCount()); i++) {
         if (i != 2 && this.view.slotItem(i).is(Items.EMERALD)) {
            count += this.view.slotItem(i).getCount();
         }
      }

      return count;
   }

   private int countInInventory(ItemStack stack) {
      int count = 0;

      for (int i = 3; i < Math.min(39, this.view.slotCount()); i++) {
         if (ItemStack.isSameItemSameComponents(this.view.slotItem(i), stack)) {
            count += this.view.slotItem(i).getCount();
         }
      }

      return count;
   }

   private int roomAt(int index, ItemStack stack) {
      return this.view.roomInSlot(index, stack);
   }

   private int inventoryRoom(ItemStack stack) {
      int room = 0;

      for (int i = 3; i < Math.min(39, this.view.slotCount()); i++) {
         room += this.roomAt(i, stack);
      }

      return room;
   }

   private int firstSlotWithRoom(ItemStack stack) {
      for (int i = 3; i < Math.min(39, this.view.slotCount()); i++) {
         if (!this.view.slotItem(i).isEmpty() && this.roomAt(i, stack) > 0) {
            return i;
         }
      }

      for (int i = 3; i < Math.min(39, this.view.slotCount()); i++) {
         if (this.roomAt(i, stack) > 0) {
            return i;
         }
      }

      return -1;
   }

   public void abortSession(Minecraft mc) {
      this.abort();
   }

   void abort() {
      if (this.view != null
         && (
            !this.view.carried().isEmpty()
               || (!this.isFinished() || this.status == VillagerTradeSession.Status.UNCERTAIN)
                  && (this.phase == VillagerTradeSession.Phase.TAKE || this.phase == VillagerTradeSession.Phase.STORED)
         )) {
         this.view.close();
      }

      if (!this.isFinished()) {
         this.finish(VillagerTradeSession.Status.CANCELLED, "Trade cancelled");
      }

      if (active == this) {
         active = null;
      }
   }

   private VillagerTradeSession.Status finish(VillagerTradeSession.Status value, String message) {
      this.status = value;
      this.message = message;
      if (active == this) {
         active = null;
      }

      return this.status;
   }

   public enum Status {
      RUNNING,
      COMPLETE,
      BUSY,
      NO_MENU,
      NO_MATCH,
      OUT_OF_STOCK,
      MISSING_INPUTS,
      INVENTORY_FULL,
      BUDGET,
      CURSOR_BUSY,
      REJECTED,
      UNCERTAIN,
      CANCELLED;
   }

   interface MenuView {
      Object menu();

      Object level();

      Object connectionToken();

      int containerId();

      List<MerchantOffer> offers();

      ItemStack slotItem(int var1);

      ItemStack carried();

      int slotCount();

      int roomInSlot(int var1, ItemStack var2);

      void selectTrade(int var1);

      void pickUpSlot(int var1);

      void close();
   }

   private enum Phase {
      SELECT,
      FILL,
      TAKE,
      STORE,
      STORED,
      DELAY;
   }
}
