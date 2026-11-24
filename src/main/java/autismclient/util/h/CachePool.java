package autismclient.util.h;

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

public final class CachePool {
   private static CachePool init_CachePool;
   private final StreamUtil init_StreamUtil;
   private final int init_I;
   private final int load_I;
   private final int read_I;
   private final int write;
   private final int update;
   private CachePool.Inner_DataUtil init_CachePool_DataUtil;
   private Object init_Obj;
   private Object load_Obj;
   private Object read_Obj;
   private CachePool.Inner_StreamUtil init_CachePool_StreamUt = CachePool.Inner_StreamUtil.init;
   private CachePool.Inner_CachePool init_CachePool_CachePoo = CachePool.Inner_CachePool.init;
   private int sync;
   private int flush;
   private int close;
   private int open = -1;
   private int poll;
   private int push;
   private int pop;
   private int tick;
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private ItemStack load_ItemStack = ItemStack.EMPTY;
   private int run;
   private int get;
   private String init_Str = "Selecting trade";
   private MerchantOffer init_MerchantOffer;
   private int set;

   public CachePool(StreamUtil filter, int limit, int delay, int timeout, int budget, int reserve) {
      this.init_StreamUtil = filter;
      this.init_I = Math.max(1, Math.min(10000, limit));
      this.load_I = Math.max(1, Math.min(200, delay));
      this.read_I = Math.max(20, Math.min(1200, timeout));
      this.write = Math.max(0, budget);
      this.update = Math.max(0, reserve);
   }

   public static boolean init_Z() {
      return init_CachePool != null;
   }

   public boolean load_Z() {
      return this.init_CachePool_CachePoo != CachePool.Inner_CachePool.init;
   }

   public CachePool.Inner_CachePool init_CachePool_CachePoo() {
      return this.init_CachePool_CachePoo;
   }

   public int init_I() {
      return this.flush;
   }

   public int load_I() {
      return this.close;
   }

   public String init_Str() {
      return this.init_Str;
   }

   public boolean read_Z() {
      if (init_CachePool != null && init_CachePool != this) {
         this.init(CachePool.Inner_CachePool.read, "Another trade is running");
         return false;
      } else {
         init_CachePool = this;
         return true;
      }
   }

   public void init_V() {
      if (this.init_CachePool_DataUtil != null) {
         this.init_CachePool_DataUtil.init_V();
      }
   }

   public static void init_V_ClientboundContain(ClientboundContainerSetContentPacket packet) {
      CachePool session = init_CachePool;
      if (session != null && session.init_CachePool_DataUtil != null && packet.containerId() == session.init_CachePool_DataUtil.init_I()) {
         session.load_ItemStack = packet.carriedItem().copy();
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

   public CachePool.Inner_CachePool init_CachePool_CachePoo_Minecraft(final Minecraft mc) {
      if (this.load_Z()) {
         return this.init_CachePool_CachePoo;
      } else {
         return mc.player != null
               && mc.gameMode != null
               && mc.getConnection() != null
               && mc.level != null
               && mc.player.containerMenu instanceof MerchantMenu current
            ? this.init_CachePool_CachePoo_CachePool_DataUtil(
               new CachePool.Inner_DataUtil() {
                  @Override
                  public Object init_Obj() {
                     return current;
                  }

                  @Override
                  public Object load_Obj() {
                     return mc.level;
                  }

                  @Override
                  public Object read() {
                     return mc.getConnection();
                  }

                  @Override
                  public int init_I() {
                     return current.containerId;
                  }

                  @Override
                  public List<MerchantOffer> init_List() {
                     return current.getOffers();
                  }

                  @Override
                  public ItemStack init_ItemStack(int slot) {
                     return current.getSlot(slot).getItem();
                  }

                  @Override
                  public ItemStack init_ItemStack() {
                     return current.getCarried();
                  }

                  @Override
                  public int load_I() {
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
                  public void init_V(int index) {
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
                  public void init_V() {
                     if (mc.player != null && mc.player.containerMenu == current) {
                        mc.player.closeContainer();
                     }
                  }
               }
            )
            : this.init(CachePool.Inner_CachePool.write, "Open a villager trade screen");
      }
   }

   CachePool.Inner_CachePool init_CachePool_CachePoo_CachePool_DataUtil(CachePool.Inner_DataUtil current) {
      if (this.load_Z()) {
         return this.init_CachePool_CachePoo;
      }

      if (init_CachePool != null && init_CachePool != this) {
         return this.init(CachePool.Inner_CachePool.read, "Another trade is running");
      }

      if (this.init_Obj == null) {
         if (!current.init_ItemStack().isEmpty()) {
            return this.init(CachePool.Inner_CachePool.poll, "Empty the cursor first");
         }

         this.init_CachePool_DataUtil = current;
         this.init_Obj = current.init_Obj();
         this.load_Obj = current.load_Obj();
         this.read_Obj = current.read();
         init_CachePool = this;
      }

      if (this.init_Obj == current.init_Obj() && this.load_Obj == current.load_Obj() && this.read_Obj == current.read()) {
         this.init_CachePool_DataUtil = current;
         if (this.init_CachePool_StreamUt != CachePool.Inner_StreamUtil.read && this.init_CachePool_StreamUt != CachePool.Inner_StreamUtil.update) {
            if (this.init_CachePool_StreamUt == CachePool.Inner_StreamUtil.sync) {
               if (++this.sync >= this.load_I) {
                  this.init_CachePool_StreamUt = CachePool.Inner_StreamUtil.init;
                  this.sync = 0;
               }

               return this.init_CachePool_CachePoo;
            } else if (this.init_CachePool_StreamUt == CachePool.Inner_StreamUtil.init) {
               if (this.init_CachePool_DataUtil.init_List().isEmpty() && ++this.sync < this.read_I) {
                  return this.init_CachePool_CachePoo;
               }

               boolean match = false;
               boolean stock = false;
               boolean budgetOk = false;
               this.open = -1;

               for (int i = 0; i < this.init_CachePool_DataUtil.init_List().size(); i++) {
                  MerchantOffer offer = this.init_CachePool_DataUtil.init_List().get(i);
                  if (this.init_StreamUtil.init_Z_MerchantOffer_I(offer, i)) {
                     match = true;
                     if (!offer.isOutOfStock()) {
                        stock = true;
                        if (StreamUtil.init(StreamUtil.init(offer), this.close, this.write, this.read_I(), this.update)) {
                           budgetOk = true;
                           if (this.init_Z_MerchantOffer(offer)) {
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
                        ? CachePool.Inner_CachePool.update
                        : (!stock ? CachePool.Inner_CachePool.sync : (!budgetOk ? CachePool.Inner_CachePool.open : CachePool.Inner_CachePool.flush)),
                     !match ? "No matching offer" : (!stock ? "Waiting for restock" : (!budgetOk ? "Emerald limit reached" : "Missing payment items"))
                  );
               }

               if (!this.init_CachePool_DataUtil.init_ItemStack().isEmpty()) {
                  return this.init(CachePool.Inner_CachePool.poll, "Cursor changed");
               }

               this.init_CachePool_DataUtil.init_V(this.open);
               this.init_CachePool_StreamUt = CachePool.Inner_StreamUtil.load;
               this.sync = 0;
               this.init_Str = "Filling offer " + (this.open + 1);
               return this.init_CachePool_CachePoo;
            } else if (this.init_CachePool_StreamUt == CachePool.Inner_StreamUtil.load) {
               if (this.open >= this.init_CachePool_DataUtil.init_List().size()) {
                  return this.init(CachePool.Inner_CachePool.update, "Offers changed");
               }

               MerchantOffer offer = this.init_CachePool_DataUtil.init_List().get(this.open);
               if (!this.init_StreamUtil.init_Z_MerchantOffer_I(offer, this.open) || offer.isOutOfStock()) {
                  return this.init(CachePool.Inner_CachePool.update, "Offer changed");
               }

               if (!this.init_CachePool_DataUtil.init_ItemStack().isEmpty()) {
                  return this.init(CachePool.Inner_CachePool.poll, "Cursor changed");
               }

               ItemStack output = this.init_CachePool_DataUtil.init_ItemStack(2);
               if (ItemStack.matches(output, offer.getResult())
                  && offer.satisfiedBy(this.init_CachePool_DataUtil.init_ItemStack(0), this.init_CachePool_DataUtil.init_ItemStack(1))) {
                  this.init_ItemStack = output.copy();
                  this.poll = StreamUtil.init(offer);
                  if (!StreamUtil.init(this.poll, this.close, this.write, this.read_I(), this.update)) {
                     return this.init(CachePool.Inner_CachePool.open, "Emerald limit reached");
                  }

                  if (this.load(this.init_ItemStack) < this.init_ItemStack.getCount()) {
                     return this.init(CachePool.Inner_CachePool.close, "Inventory full");
                  }

                  this.init_MerchantOffer = offer;
                  this.set = offer.getUses();
                  this.init_V_I(2);
                  this.init_CachePool_StreamUt = CachePool.Inner_StreamUtil.read;
                  this.init_Str = "Confirming purchase";
                  return this.init_CachePool_CachePoo;
               } else {
                  return ++this.sync >= this.read_I ? this.init(CachePool.Inner_CachePool.flush, "Payment was not filled") : this.init_CachePool_CachePoo;
               }
            } else {
               if (this.init_CachePool_StreamUt == CachePool.Inner_StreamUtil.write) {
                  ItemStack cursor = this.init_CachePool_DataUtil.init_ItemStack();
                  if (cursor.isEmpty() || !ItemStack.isSameItemSameComponents(cursor, this.init_ItemStack)) {
                     return this.init(CachePool.Inner_CachePool.pop, "Cursor changed during trade");
                  }

                  this.init_ItemStack = cursor.copy();
                  int slot = this.read(this.init_ItemStack);
                  if (slot < 0) {
                     return this.init(CachePool.Inner_CachePool.close, "Inventory full; result is on cursor");
                  }

                  int room = this.init(slot, this.init_ItemStack);
                  this.push = Math.max(0, this.init_ItemStack.getCount() - room);
                  this.run = this.init_I_ItemStack(this.init_ItemStack);
                  this.init_V_I(slot);
                  this.init_CachePool_StreamUt = CachePool.Inner_StreamUtil.update;
                  this.init_Str = "Storing purchase";
               }

               return this.init_CachePool_CachePoo;
            }
         } else {
            if (this.pop > this.tick) {
               if (this.init_CachePool_StreamUt == CachePool.Inner_StreamUtil.read) {
                  if (!ItemStack.matches(this.load_ItemStack, this.init_ItemStack)) {
                     return this.init(CachePool.Inner_CachePool.push, "Purchase was not confirmed");
                  }

                  this.flush++;
                  this.close = this.close + this.poll;
                  if (this.init_CachePool_DataUtil.init_List().contains(this.init_MerchantOffer) && this.init_MerchantOffer.getUses() == this.set) {
                     this.init_MerchantOffer.increaseUses();
                  }

                  this.init_CachePool_StreamUt = CachePool.Inner_StreamUtil.write;
               } else {
                  boolean cursorMatches = this.push == 0
                     ? this.load_ItemStack.isEmpty()
                     : ItemStack.isSameItemSameComponents(this.load_ItemStack, this.init_ItemStack) && this.load_ItemStack.getCount() == this.push;
                  if (!cursorMatches || this.get != this.run + this.init_ItemStack.getCount() - this.push) {
                     return this.init(CachePool.Inner_CachePool.pop, "Inventory transfer was not confirmed");
                  }

                  if (this.push == 0) {
                     if (this.flush >= this.init_I) {
                        return this.init(CachePool.Inner_CachePool.load, "Completed " + this.flush + " trades");
                     }

                     this.init_CachePool_StreamUt = CachePool.Inner_StreamUtil.sync;
                  } else {
                     this.init_CachePool_StreamUt = CachePool.Inner_StreamUtil.write;
                  }
               }

               this.sync = 0;
            } else if (++this.sync >= this.read_I) {
               return this.init(CachePool.Inner_CachePool.pop, "Server confirmation timed out; purchase not repeated");
            }

            return this.init_CachePool_CachePoo;
         }
      } else {
         return this.init(CachePool.Inner_CachePool.tick, "Trade screen changed");
      }
   }

   private void init_V_I(int slot) {
      this.tick = this.pop;
      this.sync = 0;
      this.init_CachePool_DataUtil.load(slot);
   }

   private boolean init_Z_MerchantOffer(MerchantOffer offer) {
      int a = 0;
      int b = 0;

      for (int i = 0; i < Math.min(39, this.init_CachePool_DataUtil.load_I()); i++) {
         if (i != 2) {
            ItemStack stack = this.init_CachePool_DataUtil.init_ItemStack(i);
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

   private int read_I() {
      int count = 0;

      for (int i = 0; i < Math.min(39, this.init_CachePool_DataUtil.load_I()); i++) {
         if (i != 2 && this.init_CachePool_DataUtil.init_ItemStack(i).is(Items.EMERALD)) {
            count += this.init_CachePool_DataUtil.init_ItemStack(i).getCount();
         }
      }

      return count;
   }

   private int init_I_ItemStack(ItemStack stack) {
      int count = 0;

      for (int i = 3; i < Math.min(39, this.init_CachePool_DataUtil.load_I()); i++) {
         if (ItemStack.isSameItemSameComponents(this.init_CachePool_DataUtil.init_ItemStack(i), stack)) {
            count += this.init_CachePool_DataUtil.init_ItemStack(i).getCount();
         }
      }

      return count;
   }

   private int init(int index, ItemStack stack) {
      return this.init_CachePool_DataUtil.init(index, stack);
   }

   private int load(ItemStack stack) {
      int room = 0;

      for (int i = 3; i < Math.min(39, this.init_CachePool_DataUtil.load_I()); i++) {
         room += this.init(i, stack);
      }

      return room;
   }

   private int read(ItemStack stack) {
      for (int i = 3; i < Math.min(39, this.init_CachePool_DataUtil.load_I()); i++) {
         if (!this.init_CachePool_DataUtil.init_ItemStack(i).isEmpty() && this.init(i, stack) > 0) {
            return i;
         }
      }

      for (int i = 3; i < Math.min(39, this.init_CachePool_DataUtil.load_I()); i++) {
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
      if (this.init_CachePool_DataUtil != null
         && (
            !this.init_CachePool_DataUtil.init_ItemStack().isEmpty()
               || (!this.load_Z() || this.init_CachePool_CachePoo == CachePool.Inner_CachePool.pop)
                  && (this.init_CachePool_StreamUt == CachePool.Inner_StreamUtil.read || this.init_CachePool_StreamUt == CachePool.Inner_StreamUtil.update)
         )) {
         this.init_CachePool_DataUtil.init_V();
      }

      if (!this.load_Z()) {
         this.init(CachePool.Inner_CachePool.tick, "Trade cancelled");
      }

      if (init_CachePool == this) {
         init_CachePool = null;
      }
   }

   private CachePool.Inner_CachePool init(CachePool.Inner_CachePool value, String message) {
      this.init_CachePool_CachePoo = value;
      this.init_Str = message;
      if (init_CachePool == this) {
         init_CachePool = null;
      }

      return this.init_CachePool_CachePoo;
   }

   public enum Inner_CachePool {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close,
      open,
      poll,
      push,
      pop,
      tick;
   }

   interface Inner_DataUtil {
      Object init_Obj();

      Object load_Obj();

      Object read();

      int init_I();

      List<MerchantOffer> init_List();

      ItemStack init_ItemStack(int var1);

      ItemStack init_ItemStack();

      int load_I();

      int init(int var1, ItemStack var2);

      void init_V(int var1);

      void load(int var1);

      void init_V();
   }

   private enum Inner_StreamUtil {
      init,
      load,
      read,
      write,
      update,
      sync;
   }
}
