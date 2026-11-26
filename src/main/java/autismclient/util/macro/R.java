package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class R implements MacroAction {
   private static final int init_I = 60;
   private boolean init_Z = true;
   public R.Inner_DataUtil scope = R.Inner_DataUtil.ALL_CONTAINER;
   public ArrayList<String> slots = new ArrayList<>();
   public int startSlot = 0;
   public int endSlot = 26;

   @Override
   public void execute(Minecraft mc) {
      if (mc != null && !mc.isSameThread()) {
         try {
            this.init_V_Minecraft_4(mc);
         } catch (InterruptedException e) {
            AutismClientMessaging.sendPrefixed("§eRollback canceled safely.");
         } catch (Throwable t) {
            AutismClientMessaging.sendPrefixed("§cRollback failed: " + init_Str_Throwable(t));
         }
      } else {
         AutismClientMessaging.sendPrefixed("§cRollback must run from the macro executor.");
      }
   }

   public void init_V_Minecraft_4(Minecraft mc) throws InterruptedException, ExecutionException, TimeoutException {
      if (mc != null && !mc.isSameThread()) {
         if (!autismclient.modules.aD.read_Z()) {
            IntegratedServer server = mc.getSingleplayerServer();
            if (server == null) {
               AutismClientMessaging.sendPrefixed("§cRollback requires a singleplayer world or the LAN host.");
            } else {
               R.Inner_CachePool plan = init(mc, () -> this.init_R_CachePool_Minecraft(mc));
               if (plan != null) {
                  init();
                  init(server, () -> init(server, plan));
               }
            }
         }
      } else {
         throw new IllegalStateException("rollback cannot execute on the render thread");
      }
   }

   private R.Inner_CachePool init_R_CachePool_Minecraft(Minecraft mc) {
      if (mc.player != null && mc.gameMode != null && mc.player.containerMenu != null) {
         AbstractContainerMenu menu = mc.player.containerMenu;
         if (menu != mc.player.inventoryMenu && menu.containerId != 0 && menu.getCarried().isEmpty()) {
            List<Integer> requested = this.init_List_I_2(menu.slots.size());
            List<R.Inner_StreamUtil> selected = new ArrayList<>();
            IdentityHashMap<Container, Set<Integer>> capturedContainerSlots = new IdentityHashMap<>();

            for (int slotId : requested) {
               if (slotId >= 0 && slotId < menu.slots.size()) {
                  Slot slot = menu.slots.get(slotId);
                  if (slot.container != mc.player.getInventory() && slot.hasItem() && slot.mayPickup(mc.player) && slot.mayPlace(slot.getItem())) {
                     Set<Integer> indexes = capturedContainerSlots.computeIfAbsent(slot.container, ignored -> new LinkedHashSet<>());
                     if (indexes.add(slot.getContainerSlot())) {
                        selected.add(new R.Inner_StreamUtil(slotId, slot.getItem().copy()));
                     }
                  }
               }
            }

            if (selected.isEmpty()) {
               AutismClientMessaging.sendPrefixed("§cRollback found no transferable container items.");
               return null;
            } else {
               return new R.Inner_CachePool(menu.containerId, selected, mc.player.getUUID());
            }
         } else {
            AutismClientMessaging.sendPrefixed("§cOpen a container and keep the cursor empty first.");
            return null;
         }
      } else {
         AutismClientMessaging.sendPrefixed("§cRollback requires an open container.");
         return null;
      }
   }

   private List<Integer> init_List_I_2(int menuSize) {
      LinkedHashSet<Integer> result = new LinkedHashSet<>();
      switch (this.scope == null ? R.Inner_DataUtil.ALL_CONTAINER : this.scope) {
         case ALL_CONTAINER:
            for (int i = 0; i < menuSize; i++) {
               result.add(i);
            }
            break;
         case CAPTURED_SLOTS:
            for (String raw : this.slots) {
               try {
                  result.add(Integer.parseInt(raw.trim()));
               } catch (RuntimeException var6) {
               }
            }
            break;
         case SLOT_RANGE:
            int first = Math.max(0, Math.min(this.startSlot, this.endSlot));
            int last = Math.min(menuSize - 1, Math.max(this.startSlot, this.endSlot));

            for (int i = first; i <= last; i++) {
               result.add(i);
            }
      }

      return List.copyOf(result);
   }

   private static boolean init(IntegratedServer server, R.Inner_CachePool plan) {
      ServerPlayer player = server.getPlayerList().getPlayer(plan.init_UUID);
      if (player != null && player.containerMenu != null && player.containerMenu.containerId == plan.init_I && player.containerMenu.getCarried().isEmpty()) {
         AbstractContainerMenu menu = player.containerMenu;

         for (R.Inner_StreamUtil expected : plan.init_List) {
            if (expected.init_I >= menu.slots.size()) {
               throw new IllegalStateException("container layout changed");
            }

            Slot source = menu.slots.get(expected.init_I);
            if (source.container == player.getInventory() || !ItemStack.matches(source.getItem(), expected.init_ItemStack)) {
               throw new IllegalStateException("container contents changed before duplication");
            }
         }

         Inventory inventory = player.getInventory();
         List<ItemStack> simulated = new ArrayList<>(36);

         for (int slot = 0; slot < 36; slot++) {
            simulated.add(inventory.getItem(slot).copy());
         }

         for (R.Inner_StreamUtil source : plan.init_List) {
            if (!init(simulated, source.init_ItemStack.copy(), inventory.getMaxStackSize())) {
               throw new IllegalStateException("not enough inventory space for all selected items");
            }
         }

         for (int slot = 0; slot < simulated.size(); slot++) {
            ItemStack target = simulated.get(slot);
            if (!ItemStack.matches(inventory.getItem(slot), target)) {
               target.setPopTime(5);
               inventory.setItem(slot, target);
               player.connection.send(inventory.createInventoryUpdatePacket(slot));
            }
         }

         inventory.setChanged();
         menu.broadcastFullState();
         if (init(inventory, simulated) && init(menu, plan)) {
            return true;
         } else {
            throw new IllegalStateException("authoritative duplicate verification failed");
         }
      } else {
         throw new IllegalStateException("the container is no longer open");
      }
   }

   private static boolean init(AbstractContainerMenu menu, R.Inner_CachePool plan) {
      for (R.Inner_StreamUtil expected : plan.init_List) {
         if (expected.init_I >= menu.slots.size() || !ItemStack.matches(menu.slots.get(expected.init_I).getItem(), expected.init_ItemStack)) {
            return false;
         }
      }

      return true;
   }

   private static boolean init(Inventory inventory, List<ItemStack> expected) {
      if (expected.size() != 36) {
         return false;
      }

      for (int slot = 0; slot < expected.size(); slot++) {
         if (!ItemStack.matches(inventory.getItem(slot), expected.get(slot))) {
            return false;
         }
      }

      return true;
   }

   private static boolean init(List<ItemStack> inventory, ItemStack incoming, int inventoryMaxStackSize) {
      if (incoming.isEmpty()) {
         return true;
      }

      for (ItemStack target : inventory) {
         if (incoming.isEmpty()) {
            return true;
         }

         if (!target.isEmpty() && ItemStack.isSameItemSameComponents(target, incoming)) {
            int capacity = Math.min(inventoryMaxStackSize, target.getMaxStackSize()) - target.getCount();
            if (capacity > 0) {
               int moved = Math.min(capacity, incoming.getCount());
               target.grow(moved);
               incoming.shrink(moved);
            }
         }
      }

      for (int slot = 0; slot < inventory.size() && !incoming.isEmpty(); slot++) {
         if (inventory.get(slot).isEmpty()) {
            int moved = Math.min(incoming.getCount(), Math.min(inventoryMaxStackSize, incoming.getMaxStackSize()));
            inventory.set(slot, incoming.copyWithCount(moved));
            incoming.shrink(moved);
         }
      }

      return incoming.isEmpty();
   }

   private static void init() throws InterruptedException {
      if (autismclient.modules.aD.read_Z() || !MacroExecutor.init_Z() || Thread.currentThread().isInterrupted()) {
         throw new InterruptedException("rollback canceled");
      }
   }

   private static <T> T init(Minecraft mc, Supplier<T> task) throws InterruptedException, ExecutionException, TimeoutException {
      if (mc.isSameThread()) {
         return task.get();
      }

      CompletableFuture<T> future = new CompletableFuture<>();
      mc.execute(() -> {
         try {
            future.complete(task.get());
         } catch (Throwable t) {
            future.completeExceptionally(t);
         }
      });
      return future.get(60L, TimeUnit.SECONDS);
   }

   private static <T> T init(IntegratedServer server, Supplier<T> task) throws InterruptedException, ExecutionException, TimeoutException {
      CompletableFuture<T> future = new CompletableFuture<>();
      server.execute(() -> {
         try {
            future.complete(task.get());
         } catch (Throwable t) {
            future.completeExceptionally(t);
         }
      });
      return future.get(60L, TimeUnit.SECONDS);
   }

   private static String init_Str_Throwable(Throwable throwable) {
      Throwable current = throwable;

      while (current.getCause() != null) {
         current = current.getCause();
      }

      String message = current.getMessage();
      return message != null && !message.isBlank() ? message : current.getClass().getSimpleName();
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putBoolean("enabled", this.init_Z);
      tag.putString("scope", this.scope.name());
      tag.put("slots", i.init(this.slots));
      tag.putInt("startSlot", this.startSlot);
      tag.putInt("endSlot", this.endSlot);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.init_Z = tag.getBooleanOr("enabled", true);
      this.scope = i.init(R.Inner_DataUtil.class, tag.getStringOr("scope", "ALL_CONTAINER"), R.Inner_DataUtil.ALL_CONTAINER);
      this.slots = i.init(tag.getList("slots").orElse(new ListTag()));
      this.startSlot = tag.getIntOr("startSlot", 0);
      this.endSlot = tag.getIntOr("endSlot", 26);
   }

   @Override
   public StreamGate getType() {
      return StreamGate.ROLLBACK;
   }

   @Override
   public String getDisplayName() {
      return "Rollback " + this.scope.name().replace('_', ' ');
   }

   @Override
   public String getIcon() {
      return "RB";
   }

   @Override
   public boolean isEnabled() {
      return this.init_Z;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.init_Z = enabled;
   }

   private record Inner_CachePool(int init_I, List<R.Inner_StreamUtil> init_List, UUID init_UUID) {
   }

   public enum Inner_DataUtil {
      ALL_CONTAINER,
      CAPTURED_SLOTS,
      SLOT_RANGE;
   }

   private record Inner_StreamUtil(int init_I, ItemStack init_ItemStack) {
   }
}
