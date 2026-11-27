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
   private final int write;
   private final int update;
   private VillagerTradeSession.Inner_DataUtil view;
   private Object boundMenu;
   private Object boundLevel;
   private Object read_Obj;
   private VillagerTradeSession.Inner_StreamUtil phase = VillagerTradeSession.Inner_StreamUtil.SELECT;
   private VillagerTradeSession.Inner_CachePool status = VillagerTradeSession.Inner_CachePool.RUNNING;
   private int sync;
   private int flush;
   private int close;
   private int open = -1;
   private int poll;
   private int push;
   private int pop;
   private int tick;
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private ItemStack serverCursor = ItemStack.EMPTY;
   private int run;
   private int get;
   private String message = "Selecting trade";
   private MerchantOffer offer;
   private int set;

   public VillagerTradeSession(TradeOfferFilter filter, int limit, int delay, int timeout, int budget, int reserve) {
      this.filter = filter;
      this.limit = Math.max(1, Math.min(10000, limit));
      this.delayTicks = Math.max(1, Math.min(200, delay));
      this.timeoutTicks = Math.max(20, Math.min(1200, timeout));
      this.write = Math.max(0, budget);
      this.update = Math.max(0, reserve);
   }

   public static boolean isRunning() {
      return active != null;
   }

   public boolean isFinished() {
      return this.status != VillagerTradeSession.Inner_CachePool.RUNNING;
   }

   public VillagerTradeSession.Inner_CachePool status() {
      return this.status;
   }

   public int traded() {
      return this.flush;
   }

   public int spent() {
      return this.close;
   }

   public String message() {
      return this.message;
   }

   public boolean reserve() {
      if (active != null && active != this) {
         this.init(VillagerTradeSession.Inner_CachePool.BUSY, "Another trade is running");
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
            if (ItemStack.isSameItemSameComponents(stack, session.init_ItemStack)) {
               count += stack.getCount();
            }
         }

         session.get = count;
         session.pop++;
      }
   }

   public VillagerTradeSession.Inner_CachePool tick(final Minecraft mc) {
      if (this.isFinished()) {
         return this.status;
      } else {
         return mc.player != null
               && mc.gameMode != null
               && mc.getConnection() != null
               && mc.level != null
               && mc.player.containerMenu instanceof MerchantMenu current
            ? this.step(
               new VillagerTradeSession.Inner_DataUtil() {
                  @Override
                  public Object menu() {
                     return current;
                  }

                  @Override
                  public Object level() {
                     return mc.level;
                  }

                  @Override
                  public Object read() {
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
                  public int init(int index, ItemStack stack) {
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
                  public void load(int slot) {
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
            : this.init(VillagerTradeSession.Inner_CachePool.NO_MENU, "Open a villager trade screen");
      }
   }

   VillagerTradeSession.Inner_CachePool step(VillagerTradeSession.Inner_DataUtil current) {
      if (this.isFinished()) {
         return this.status;
      }

      if (active != null && active != this) {
         return this.init(VillagerTradeSession.Inner_CachePool.BUSY, "Another trade is running");
      }

      if (this.boundMenu == null) {
         if (!current.carried().isEmpty()) {
            return this.init(VillagerTradeSession.Inner_CachePool.CURSOR_BUSY, "Empty the cursor first");
         }

         this.view = current;
         this.boundMenu = current.menu();
         this.boundLevel = current.level();
         this.read_Obj = current.read();
         active = this;
      }

      if (this.boundMenu == current.menu() && this.boundLevel == current.level() && this.read_Obj == current.read()) {
         this.view = current;
         if (this.phase != VillagerTradeSession.Inner_StreamUtil.TAKE && this.phase != VillagerTradeSession.Inner_StreamUtil.STORED) {
            if (this.phase == VillagerTradeSession.Inner_StreamUtil.DELAY) {
               if (++this.sync >= this.delayTicks) {
                  this.phase = VillagerTradeSession.Inner_StreamUtil.SELECT;
                  this.sync = 0;
               }

               return this.status;
            } else if (this.phase == VillagerTradeSession.Inner_StreamUtil.SELECT) {
               if (this.view.offers().isEmpty() && ++this.sync < this.timeoutTicks) {
                  return this.status;
               }

               boolean match = false;
               boolean stock = false;
               boolean budgetOk = false;
               this.open = -1;

               for (int i = 0; i < this.view.offers().size(); i++) {
                  MerchantOffer offer = this.view.offers().get(i);
                  if (this.filter.matches(offer, i)) {
                     match = true;
                     if (!offer.isOutOfStock()) {
                        stock = true;
                        if (TradeOfferFilter.init(TradeOfferFilter.init(offer), this.close, this.write, this.emeraldCount(), this.update)) {
                           budgetOk = true;
                           if (this.hasPayment(offer)) {
                              this.open = i;
                              break;
                           }
                        }
                     }
                  }
               }

               if (this.open < 0) {
                  return this.init(
                     !match
                        ? VillagerTradeSession.Inner_CachePool.NO_MATCH
                        : (!stock ? VillagerTradeSession.Inner_CachePool.OUT_OF_STOCK : (!budgetOk ? VillagerTradeSession.Inner_CachePool.BUDGET : VillagerTradeSession.Inner_CachePool.MISSING_INPUTS)),
                     !match ? "No matching offer" : (!stock ? "Waiting for restock" : (!budgetOk ? "Emerald limit reached" : "Missing payment items"))
                  );
               }

               if (!this.view.carried().isEmpty()) {
                  return this.init(VillagerTradeSession.Inner_CachePool.CURSOR_BUSY, "Cursor changed");
               }

               this.view.selectTrade(this.open);
               this.phase = VillagerTradeSession.Inner_StreamUtil.FILL;
               this.sync = 0;
               this.message = "Filling offer " + (this.open + 1);
               return this.status;
            } else if (this.phase == VillagerTradeSession.Inner_StreamUtil.FILL) {
               if (this.open >= this.view.offers().size()) {
                  return this.init(VillagerTradeSession.Inner_CachePool.NO_MATCH, "Offers changed");
               }

               MerchantOffer offer = this.view.offers().get(this.open);
               if (!this.filter.matches(offer, this.open) || offer.isOutOfStock()) {
                  return this.init(VillagerTradeSession.Inner_CachePool.NO_MATCH, "Offer changed");
               }

               if (!this.view.carried().isEmpty()) {
                  return this.init(VillagerTradeSession.Inner_CachePool.CURSOR_BUSY, "Cursor changed");
               }

               ItemStack output = this.view.slotItem(2);
               if (ItemStack.matches(output, offer.getResult())
                  && offer.satisfiedBy(this.view.slotItem(0), this.view.slotItem(1))) {
                  this.init_ItemStack = output.copy();
                  this.poll = TradeOfferFilter.init(offer);
                  if (!TradeOfferFilter.init(this.poll, this.close, this.write, this.emeraldCount(), this.update)) {
                     return this.init(VillagerTradeSession.Inner_CachePool.BUDGET, "Emerald limit reached");
                  }

                  if (this.load(this.init_ItemStack) < this.init_ItemStack.getCount()) {
                     return this.init(VillagerTradeSession.Inner_CachePool.INVENTORY_FULL, "Inventory full");
                  }

                  this.offer = offer;
                  this.set = offer.getUses();
                  this.init_V_I(2);
                  this.phase = VillagerTradeSession.Inner_StreamUtil.TAKE;
                  this.message = "Confirming purchase";
                  return this.status;
               } else {
                  return ++this.sync >= this.timeoutTicks ? this.init(VillagerTradeSession.Inner_CachePool.MISSING_INPUTS, "Payment was not filled") : this.status;
               }
            } else {
               if (this.phase == VillagerTradeSession.Inner_StreamUtil.STORE) {
                  ItemStack cursor = this.view.carried();
                  if (cursor.isEmpty() || !ItemStack.isSameItemSameComponents(cursor, this.init_ItemStack)) {
                     return this.init(VillagerTradeSession.Inner_CachePool.UNCERTAIN, "Cursor changed during trade");
                  }

                  this.init_ItemStack = cursor.copy();
                  int slot = this.read(this.init_ItemStack);
                  if (slot < 0) {
                     return this.init(VillagerTradeSession.Inner_CachePool.INVENTORY_FULL, "Inventory full; result is on cursor");
                  }

                  int room = this.init(slot, this.init_ItemStack);
                  this.push = Math.max(0, this.init_ItemStack.getCount() - room);
                  this.run = this.countInInventory(this.init_ItemStack);
                  this.init_V_I(slot);
                  this.phase = VillagerTradeSession.Inner_StreamUtil.STORED;
                  this.message = "Storing purchase";
               }

               return this.status;
            }
         } else {
            if (this.pop > this.tick) {
               if (this.phase == VillagerTradeSession.Inner_StreamUtil.TAKE) {
                  if (!ItemStack.matches(this.serverCursor, this.init_ItemStack)) {
                     return this.init(VillagerTradeSession.Inner_CachePool.REJECTED, "Purchase was not confirmed");
                  }

                  this.flush++;
                  this.close = this.close + this.poll;
                  if (this.view.offers().contains(this.offer) && this.offer.getUses() == this.set) {
                     this.offer.increaseUses();
                  }

                  this.phase = VillagerTradeSession.Inner_StreamUtil.STORE;
               } else {
                  boolean cursorMatches = this.push == 0
                     ? this.serverCursor.isEmpty()
                     : ItemStack.isSameItemSameComponents(this.serverCursor, this.init_ItemStack) && this.serverCursor.getCount() == this.push;
                  if (!cursorMatches || this.get != this.run + this.init_ItemStack.getCount() - this.push) {
                     return this.init(VillagerTradeSession.Inner_CachePool.UNCERTAIN, "Inventory transfer was not confirmed");
                  }

                  if (this.push == 0) {
                     if (this.flush >= this.limit) {
                        return this.init(VillagerTradeSession.Inner_CachePool.COMPLETE, "Completed " + this.flush + " trades");
                     }

                     this.phase = VillagerTradeSession.Inner_StreamUtil.DELAY;
                  } else {
                     this.phase = VillagerTradeSession.Inner_StreamUtil.STORE;
                  }
               }

               this.sync = 0;
            } else if (++this.sync >= this.timeoutTicks) {
               return this.init(VillagerTradeSession.Inner_CachePool.UNCERTAIN, "Server confirmation timed out; purchase not repeated");
            }

            return this.status;
         }
      } else {
         return this.init(VillagerTradeSession.Inner_CachePool.CANCELLED, "Trade screen changed");
      }
   }

   private void init_V_I(int slot) {
      this.tick = this.pop;
      this.sync = 0;
      this.view.load(slot);
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

   private int init(int index, ItemStack stack) {
      return this.view.init(index, stack);
   }

   private int load(ItemStack stack) {
      int room = 0;

      for (int i = 3; i < Math.min(39, this.view.slotCount()); i++) {
         room += this.init(i, stack);
      }

      return room;
   }

   private int read(ItemStack stack) {
      for (int i = 3; i < Math.min(39, this.view.slotCount()); i++) {
         if (!this.view.slotItem(i).isEmpty() && this.init(i, stack) > 0) {
            return i;
         }
      }

      for (int i = 3; i < Math.min(39, this.view.slotCount()); i++) {
         if (this.init(i, stack) > 0) {
            return i;
         }
      }

      return -1;
   }

   public void init_V_Minecraft(Minecraft mc) {
      this.load_V();
   }

   void load_V() {
      if (this.view != null
         && (
            !this.view.carried().isEmpty()
               || (!this.isFinished() || this.status == VillagerTradeSession.Inner_CachePool.UNCERTAIN)
                  && (this.phase == VillagerTradeSession.Inner_StreamUtil.TAKE || this.phase == VillagerTradeSession.Inner_StreamUtil.STORED)
         )) {
         this.view.close();
      }

      if (!this.isFinished()) {
         this.init(VillagerTradeSession.Inner_CachePool.CANCELLED, "Trade cancelled");
      }

      if (active == this) {
         active = null;
      }
   }

   private VillagerTradeSession.Inner_CachePool init(VillagerTradeSession.Inner_CachePool value, String message) {
      this.status = value;
      this.message = message;
      if (active == this) {
         active = null;
      }

      return this.status;
   }

   public enum Inner_CachePool {
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

   interface Inner_DataUtil {
      Object menu();

      Object level();

      Object read();

      int containerId();

      List<MerchantOffer> offers();

      ItemStack slotItem(int var1);

      ItemStack carried();

      int slotCount();

      int init(int var1, ItemStack var2);

      void selectTrade(int var1);

      void load(int var1);

      void close();
   }

   private enum Inner_StreamUtil {
      SELECT,
      FILL,
      TAKE,
      STORE,
      STORED,
      DELAY;
   }
}
