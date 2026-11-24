package autismclient.ai;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class PkgCls_b {
   private static PkgCls_b.Inner_DataUtil init_b_DataUtil;
   private static boolean init_Z;

   private PkgCls_b() {
   }

   public static int init(int applied) {
      return (applied ^ 16384) & 32767;
   }

   public static PkgCls_b.Inner_DataUtil init(AbstractContainerMenu menu, int source, int destination, int hotbar) {
      ItemStack from = menu.getSlot(source).getItem();
      ItemStack to = menu.getSlot(destination).getItem();
      return menu.getCarried().isEmpty() && !ItemStack.matches(from, to)
         ? init(menu, source, hotbar, ContainerInput.SWAP, Map.of(source, to, destination, from), ItemStack.EMPTY, true)
         : null;
   }

   static PkgCls_b.Inner_DataUtil init(AbstractContainerMenu menu, int slot, int button, Map<Integer, ItemStack> expected, ItemStack cursor) {
      return init(menu, slot, button, ContainerInput.PICKUP, expected, cursor, false);
   }

   private static PkgCls_b.Inner_DataUtil init(
      AbstractContainerMenu menu, int slot, int button, ContainerInput type, Map<Integer, ItemStack> expected, ItemStack cursor, boolean swap
   ) {
      Minecraft mc = Minecraft.getInstance();
      ClientPacketListener connection = mc.getConnection();
      long now = System.nanoTime();
      if (mc.player == null || connection == null || mc.player.containerMenu != menu) {
         return null;
      }

      if (init_b_DataUtil != null && init_b_DataUtil.init_AbstractContainerM == menu && !init_b_DataUtil.load(now)) {
         return null;
      }

      PkgCls_b.Inner_DataUtil receipt = new PkgCls_b.Inner_DataUtil(menu, expected, cursor, swap, now);
      if (swap) {
         receipt.init_I = button;
         receipt.write = init_Z;
         init_Z = false;
      }

      connection.send(
         new ServerboundContainerClickPacket(
            menu.containerId,
            menu.getStateId(),
            (short)slot,
            (byte)button,
            type,
            new Int2ObjectOpenHashMap<>(),
            HashedStack.create(menu.getCarried(), connection.decoratedHashOpsGenenerator())
         )
      );
      init_b_DataUtil = receipt;
      return receipt;
   }

   public static void init(int id, int slot, ItemStack stack) {
      PkgCls_b.Inner_DataUtil receipt = init_b_DataUtil();
      if (receipt != null && !receipt.write) {
         if (receipt.init_AbstractContainerM.containerId == id) {
            receipt.init(slot, stack);
         } else {
            LocalPlayer player = Minecraft.getInstance().player;
            if (id == player.inventoryMenu.containerId && slot >= 0 && slot < player.inventoryMenu.slots.size()) {
               Slot cell = player.inventoryMenu.getSlot(slot);
               if (cell.container == player.getInventory()) {
                  init(cell.getContainerSlot(), stack);
               }
            }
         }
      }
   }

   public static void init(int slot, ItemStack stack) {
      PkgCls_b.Inner_DataUtil receipt = init_b_DataUtil();
      if (receipt != null && !receipt.write) {
         Inventory inventory = Minecraft.getInstance().player.getInventory();

         for (int index : receipt.load_Map.keySet()) {
            Slot cell = receipt.init_AbstractContainerM.getSlot(index);
            if (cell.container == inventory && cell.getContainerSlot() == slot) {
               receipt.init(index, stack);
            }
         }
      }
   }

   public static void init(ItemStack stack) {
      PkgCls_b.Inner_DataUtil receipt = init_b_DataUtil();
      if (receipt != null) {
         receipt.init(stack);
      }
   }

   public static void init(int id, List<ItemStack> stacks, ItemStack carried) {
      aq.init(id, carried.isEmpty());
      PkgCls_b.Inner_DataUtil receipt = init_b_DataUtil();
      if (receipt != null && receipt.init_AbstractContainerM.containerId == id) {
         receipt.init(stacks, carried);
      }
   }

   private static PkgCls_b.Inner_DataUtil init_b_DataUtil() {
      if (init_b_DataUtil == null) {
         return null;
      }

      LocalPlayer player = Minecraft.getInstance().player;
      if (player == null || player.containerMenu != init_b_DataUtil.init_AbstractContainerM) {
         init_b_DataUtil = null;
      }

      return init_b_DataUtil;
   }

   public static void init_V() {
      PkgCls_b.Inner_DataUtil receipt = init_b_DataUtil();
      if (receipt != null && receipt.load_Z) {
         receipt.load(Minecraft.getInstance().player.containerMenu, System.nanoTime());
      }
   }

   static boolean init_Z() {
      PkgCls_b.Inner_DataUtil receipt = init_b_DataUtil();
      return receipt != null && receipt.load_Z;
   }

   static void load() {
      init_Z = true;
   }

   static void read() {
      init_b_DataUtil = null;
      init_Z = false;
      aq.init_V();
   }

   public static void init(int containerId, int slot, int button, ContainerInput type) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      ClientPacketListener connection = mc.getConnection();
      if (player != null && connection != null) {
         AbstractContainerMenu menu = player.containerMenu;
         if (containerId == menu.containerId
            && slot >= 0
            && slot < menu.slots.size()
            && slot <= 32767
            && button >= 0
            && (button <= 8 || type == ContainerInput.SWAP && button == 40)) {
            ArrayList<ItemStack> before = new ArrayList<>(menu.slots.size());

            for (Slot cell : menu.slots) {
               before.add(cell.getItem().copy());
            }

            menu.clicked(slot, button, type, player);
            Int2ObjectOpenHashMap<HashedStack> changed = new Int2ObjectOpenHashMap<>();

            for (int i = 0; i < before.size(); i++) {
               if (!ItemStack.matches(before.get(i), menu.getSlot(i).getItem())) {
                  changed.put(i, HashedStack.create(menu.getSlot(i).getItem(), connection.decoratedHashOpsGenenerator()));
               }
            }

            connection.send(
               new ServerboundContainerClickPacket(
                  containerId,
                  init(menu.getStateId()),
                  (short)slot,
                  (byte)button,
                  type,
                  changed,
                  HashedStack.create(menu.getCarried(), connection.decoratedHashOpsGenenerator())
               )
            );
         } else {
            throw new IllegalArgumentException("Stale or invalid container transaction");
         }
      } else {
         throw new IllegalStateException("Disconnected before container transaction");
      }
   }

   public static final class Inner_DataUtil {
      private static final long init_J = 8000000000L;
      private static final long load_J = 750000000L;
      private final AbstractContainerMenu init_AbstractContainerM;
      private final Map<Integer, ItemStack> init_Map = new HashMap<>();
      private final Map<Integer, ItemStack> load_Map = new HashMap<>();
      private final Map<Integer, ItemStack> read_Map = new HashMap<>();
      private final ItemStack init_ItemStack;
      private final boolean init_Z;
      private final boolean load_Z;
      private final long read_J;
      private ItemStack load_ItemStack;
      private PkgCls_b.Inner_StreamUtil init_b_StreamUtil;
      private int init_I = -1;
      private boolean read_Z;
      private boolean write;

      Inner_DataUtil(AbstractContainerMenu menu, Map<Integer, ItemStack> expected, ItemStack cursor, boolean swap, long started) {
         this.init_AbstractContainerM = menu;
         expected.forEach((slot, stack) -> {
            this.init_Map.put(slot, menu.getSlot(slot).getItem().copy());
            this.load_Map.put(slot, stack.copy());
         });
         this.init_ItemStack = cursor.copy();
         this.init_Z = !ItemStack.matches(cursor, menu.getCarried());
         this.load_Z = swap;
         this.read_J = started;
      }

      void init(int slot, ItemStack stack) {
         if (this.load_Map.containsKey(slot)) {
            this.read_Map.put(slot, stack.copy());
         }
      }

      void init(ItemStack stack) {
         this.load_ItemStack = stack.copy();
      }

      void init(List<ItemStack> stacks, ItemStack carried) {
         if (stacks.size() == this.init_AbstractContainerM.slots.size()) {
            this.load_Map.keySet().forEach(slot -> this.init(slot, stacks.get(slot)));
            this.init(carried);
         }
      }

      public PkgCls_b.Inner_StreamUtil init(AbstractContainerMenu current, long now) {
         if (this.init_b_StreamUtil != null) {
            return this.init_b_StreamUtil;
         }

         if (current != this.init_AbstractContainerM) {
            return this.init(PkgCls_b.Inner_StreamUtil.write);
         }

         if (ItemStack.matches(this.init_ItemStack, this.init_AbstractContainerM.getCarried())
            && (this.load_ItemStack == null ? !this.init_Z : ItemStack.matches(this.init_ItemStack, this.load_ItemStack))) {
            boolean confirmed = true;
            boolean exchanged = this.load_Z;
            boolean preserved = false;

            for (Entry<Integer, ItemStack> entry : this.load_Map.entrySet()) {
               int slot = entry.getKey();
               ItemStack wanted = entry.getValue();
               ItemStack actual = this.init_AbstractContainerM.getSlot(slot).getItem();
               ItemStack update = this.read_Map.get(slot);
               boolean matches = ItemStack.matches(wanted, actual);
               boolean authoritative = update != null && ItemStack.matches(update, actual);
               confirmed &= matches && (authoritative || update == null && ItemStack.matches(wanted, this.init_Map.get(slot)));
               exchanged &= authoritative && !ItemStack.matches(actual, this.init_Map.get(slot));
               preserved |= matches;
            }

            if (confirmed) {
               return this.init(PkgCls_b.Inner_StreamUtil.load);
            }

            if (exchanged && preserved) {
               return this.init(PkgCls_b.Inner_StreamUtil.read);
            }
         }

         return this.load(now) ? this.init(PkgCls_b.Inner_StreamUtil.write) : PkgCls_b.Inner_StreamUtil.init;
      }

      public PkgCls_b.Inner_StreamUtil load(AbstractContainerMenu current, long now) {
         PkgCls_b.Inner_StreamUtil state = this.init(current, now);
         if (state == PkgCls_b.Inner_StreamUtil.init && this.init_I >= 0 && this.init(now)) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            ClientPacketListener connection = mc.getConnection();
            if (player != null
               && connection != null
               && player.inventoryMenu == this.init_AbstractContainerM
               && player.containerMenu == this.init_AbstractContainerM
               && this.init_AbstractContainerM.getCarried().isEmpty()) {
               int slot = 36 + this.init_I;
               Slot cell = this.init_AbstractContainerM.getSlot(slot);
               if (cell.container == player.getInventory() && cell.getContainerSlot() == this.init_I) {
                  this.read_Z = true;
                  connection.send(
                     new ServerboundContainerClickPacket(
                        this.init_AbstractContainerM.containerId,
                        PkgCls_b.init(this.init_AbstractContainerM.getStateId()),
                        (short)slot,
                        (byte)this.init_I,
                        ContainerInput.SWAP,
                        new Int2ObjectOpenHashMap<>(),
                        HashedStack.create(ItemStack.EMPTY, connection.decoratedHashOpsGenenerator())
                     )
                  );
                  return state;
               } else {
                  return state;
               }
            } else {
               return state;
            }
         } else {
            return state;
         }
      }

      boolean init(long now) {
         return this.load_Z && !this.read_Z && this.init_b_StreamUtil == null && now - this.read_J >= 750000000L && !this.load(now);
      }

      private boolean load(long now) {
         return now - this.read_J >= 8000000000L;
      }

      private PkgCls_b.Inner_StreamUtil init(PkgCls_b.Inner_StreamUtil state) {
         if (PkgCls_b.init_b_DataUtil == this) {
            PkgCls_b.init_b_DataUtil = null;
         }

         this.init_b_StreamUtil = state;
         return state;
      }
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read,
      write;
   }
}
