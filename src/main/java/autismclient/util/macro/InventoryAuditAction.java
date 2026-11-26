package autismclient.util.macro;

import autismclient.util.AutismSessionState;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InventoryAuditAction implements MacroAction {
   private static final int init_I = 10000;
   private static final int load = 100;
   private static final int read = 12000;
   private static final int write = 50;
   private static final int update = 3;
   private static final int sync = 25;
   public InventoryAuditAction.Inner_BufferHelper mode = InventoryAuditAction.Inner_BufferHelper.DUPE;
   public List<String> targetItems = new ArrayList<>();
   public List<MacroItemTarget> itemTargets = new ArrayList<>();
   public String openCommand = "/ec";
   public InventoryAuditAction.Inner_ConfigStore openMode = InventoryAuditAction.Inner_ConfigStore.COMMAND;
   public BlockPos containerPos = BlockPos.ZERO;
   public InventoryAuditAction.Inner_DataUtil dupeVector = InventoryAuditAction.Inner_DataUtil.DESYNC_REOPEN;
   public int delayBeforeReopen = 200;
   public int delayAfterReopen = 500;
   public int iterations = 1;
   public int maxTransferAttempts = 5;
   public int transferRetryDelayMs = 50;
   public boolean multipleStacks = false;
   public int spamCount = 3;
   public int spamDelayMs = 50;
   private boolean init_Z = true;

   public boolean init_Z_2() {
      return this.load_Z_2();
   }

   public boolean load_Z_2() {
      for (MacroItemTarget target : this.init_List_3()) {
         if (target != null && (target.init_Z() || target.update())) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void execute(Minecraft mc) {
      if (mc != null && mc.player != null && this.mode != InventoryAuditAction.Inner_BufferHelper.DUPE && this.mode == InventoryAuditAction.Inner_BufferHelper.DUPE_SPAM) {
      }
   }

   public void init_V_Minecraft_2(Minecraft mc) throws InterruptedException {
      if (mc != null && mc.player != null) {
         MacroTemplateResolver.ResolveResult commandResolution = MacroVariableContext.init_j_CachePool_Str_Minecraft(this.init_Str_2(), mc);
         if (commandResolution.init_Z()) {
            String cmd = commandResolution.init_Str().trim();
            if (!cmd.isBlank()) {
               int runIterations = this.load(this.iterations);
               int maxAttempts = Math.max(1, Math.min(this.maxTransferAttempts, 20));
               int retryDelay = Math.max(10, Math.min(this.transferRetryDelayMs, 500));

               for (int iter = 0; iter < runIterations; iter++) {
                  this.init_V_4();
                  if (!this.init_Z_Minecraft(mc, cmd, 12000L)) {
                     return;
                  }

                  InventoryAuditAction.Inner_CachePool before = this.load_ListPool_CachePool(mc);
                  if (before == null || before.init_Map().isEmpty()) {
                     return;
                  }

                  Map<String, Integer> targetContainerItems = this.init_Map_ListPool_CachePool(before);
                  if (targetContainerItems.isEmpty()) {
                     InventoryAuditAction.Inner_CachePool playerSnapshot = this.init_ListPool_CachePool_Minecraft(mc);
                     Map<String, Integer> playerTargetItems = this.load(playerSnapshot);
                     if (!playerTargetItems.isEmpty()) {
                        boolean transferred = this.init_Z_Minecraft_Map(mc, playerTargetItems);
                        if (transferred) {
                           this.init_V_Minecraft_Z(mc, true);
                           if (!this.read_Z_Minecraft_J(mc, 12000L)) {
                              return;
                           }

                           if (!this.init_Z_Minecraft(mc, cmd, 12000L)) {
                              return;
                           }

                           before = this.load_ListPool_CachePool(mc);
                           if (before == null || before.init_Map().isEmpty()) {
                              return;
                           }

                           targetContainerItems = this.init_Map_ListPool_CachePool(before);
                        }
                     }

                     if (targetContainerItems.isEmpty()) {
                        this.init_V_Minecraft_Z(mc, true);
                        continue;
                     }
                  }

                  if (this.mode == InventoryAuditAction.Inner_BufferHelper.DUPE_SPAM) {
                     int spamOpens = Math.max(1, Math.min(this.spamCount, 20));
                     int spamDelay = Math.max(10, Math.min(this.spamDelayMs, 1000));
                     this.init(mc, cmd, spamOpens, spamDelay);
                     this.init_V_J(100L);
                  }

                  this.init_ListPool_NetHelper_Minecraft(mc);
                  int totalTransferAttempts = 0;
                  int emptyVisibleTransferPasses = 0;
                  Set<Integer> targetSlots = this.init_Set_Minecraft(mc);

                  while (totalTransferAttempts < maxAttempts) {
                     this.init_V_4();
                     if (!this.init_Z_Minecraft(mc, cmd, 12000L)) {
                        break;
                     }

                     this.init_V_J(retryDelay);
                     InventoryAuditAction.Inner_NetHelper attempt = this.init_ListPool_NetHelper_Minecraft_Set(mc, targetSlots);
                     totalTransferAttempts++;
                     if (attempt.init_I() <= 0 && targetSlots.isEmpty()) {
                        if (++emptyVisibleTransferPasses >= 2) {
                           break;
                        }
                     } else {
                        emptyVisibleTransferPasses = 0;
                     }
                  }

                  if (!this.write(mc, 12000L)) {
                     return;
                  }

                  if (!this.init_Z_Minecraft(mc, cmd, 12000L)) {
                     return;
                  }

                  this.init_V_Minecraft_Z(mc, true);
                  if (iter < runIterations - 1) {
                     this.init_V_J(500L);
                  }
               }
            }
         }
      }
   }

   private Set<Integer> init_Set_Minecraft(Minecraft mc) {
      Set<Integer> slots = new HashSet<>();
      List<MacroItemTarget> targets = this.load_List_2();
      if (!targets.isEmpty()) {
         for (MacroItemTarget target : targets) {
            if (target != null && target.init_Z()) {
               slots.add(target.init_I);
            }
         }
      }

      return slots;
   }

   private InventoryAuditAction.Inner_NetHelper init_ListPool_NetHelper_Minecraft_Set(Minecraft mc, Set<Integer> targetSlots) throws InterruptedException {
      InventoryAuditAction.Inner_NetHelper[] result = new InventoryAuditAction.Inner_NetHelper[1];
      CountDownLatch latch = new CountDownLatch(1);
      mc.execute(() -> {
         try {
            if (mc.player == null || mc.gameMode == null) {
               result[0] = new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
               return;
            }

            AbstractContainerMenu handler = mc.player.containerMenu;
            if (handler == null || handler == mc.player.inventoryMenu) {
               result[0] = new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
               return;
            }

            Map<String, Integer> items = new LinkedHashMap<>();
            int slotCount = 0;
            Set<Integer> firedSlots = new HashSet<>();

            for (int visibleSlot : targetSlots) {
               int handlerSlot = autismclient.util.InventorySlotHelper.toScreenSlot(mc, visibleSlot);
               if (handlerSlot >= 0 && handlerSlot < handler.slots.size() && firedSlots.add(handlerSlot)) {
                  Slot slot = handler.slots.get(handlerSlot);
                  if (slot != null && !autismclient.util.InventorySlotHelper.isPlayerInventorySlot(mc, slot)) {
                     ItemStack stack = slot.getItem();
                     if (stack != null && !stack.isEmpty()) {
                        slotCount++;
                        items.merge(this.init_Str_ItemStack(stack), stack.getCount(), Integer::sum);
                     }

                     mc.gameMode.handleContainerInput(handler.containerId, handlerSlot, 0, ContainerInput.QUICK_MOVE, mc.player);
                  }
               }
            }

            for (int i = 0; i < handler.slots.size(); i++) {
               if (!firedSlots.contains(i)) {
                  Slot slot = handler.slots.get(i);
                  if (slot != null && !autismclient.util.InventorySlotHelper.isPlayerInventorySlot(mc, slot)) {
                     ItemStack stack = slot.getItem();
                     if (stack != null && !stack.isEmpty()) {
                        int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, slot.index);
                        if (this.init_Z_ItemStack_I(stack, visibleSlot)) {
                           firedSlots.add(i);
                           slotCount++;
                           items.merge(this.init_Str_ItemStack(stack), stack.getCount(), Integer::sum);
                           mc.gameMode.handleContainerInput(handler.containerId, i, 0, ContainerInput.QUICK_MOVE, mc.player);
                        }
                     }
                  }
               }
            }

            result[0] = new InventoryAuditAction.Inner_NetHelper(slotCount, items);
         } finally {
            latch.countDown();
         }
      });
      this.init_Z_CountDownLatch_J(latch, 2000L);
      return result[0] != null ? result[0] : new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
   }

   private void init(InventoryAuditAction.Inner_CachePool before, InventoryAuditAction.Inner_CachePool after, InventoryAuditAction.Inner_NetHelper transfer, String iterLabel, int transferAttempts) {
   }

   private InventoryAuditAction.Inner_NetHelper init_ListPool_NetHelper_Minecraft(Minecraft mc) throws InterruptedException {
      InventoryAuditAction.Inner_NetHelper var10000;
      switch (this.dupeVector) {
         case DESYNC_REOPEN:
            var10000 = this.load_ListPool_NetHelper(mc);
            break;
         case CLOSE_NO_PACKET:
            var10000 = this.read(mc);
            break;
         case SHIFT_CLICK_REOPEN:
            var10000 = this.write(mc);
            break;
         case DELAYED_PACKETS:
            var10000 = this.sync(mc);
            break;
         case SWAP_HOTBAR:
            var10000 = this.close(mc);
            break;
         case DROP_EXPLOIT:
            var10000 = this.open(mc);
            break;
         case DELAYED_DESYNC_REOPEN:
            var10000 = this.flush(mc);
            break;
         case SWAP_DESYNC_REOPEN:
            var10000 = this.update(mc);
            break;
         case DROP_DELAYED_PACKETS:
            var10000 = this.poll(mc);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private InventoryAuditAction.Inner_NetHelper load_ListPool_NetHelper(Minecraft mc) throws InterruptedException {
      InventoryAuditAction.Inner_NetHelper transfer = this.push(mc);
      CountDownLatch latch = new CountDownLatch(1);
      mc.execute(() -> {
         try {
            if (mc.player != null && mc.getConnection() != null && mc.player.containerMenu != null) {
               mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
            }
         } finally {
            latch.countDown();
         }
      });
      this.init_Z_CountDownLatch_J(latch, 2000L);
      this.init_V_J(100L);
      this.init_V_Minecraft_Z(mc, false);
      return transfer;
   }

   private InventoryAuditAction.Inner_NetHelper read(Minecraft mc) throws InterruptedException {
      InventoryAuditAction.Inner_NetHelper transfer = this.push(mc);
      this.init_V_Minecraft_Z(mc, false);
      return transfer;
   }

   private InventoryAuditAction.Inner_NetHelper write(Minecraft mc) throws InterruptedException {
      InventoryAuditAction.Inner_NetHelper[] result = new InventoryAuditAction.Inner_NetHelper[1];
      CountDownLatch latch = new CountDownLatch(1);
      mc.execute(() -> {
         try {
            if (mc.player == null || mc.gameMode == null) {
               result[0] = new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
               return;
            }

            AbstractContainerMenu handler = mc.player.containerMenu;
            if (handler == null || handler == mc.player.inventoryMenu) {
               result[0] = new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
               return;
            }

            Map<String, Integer> items = new LinkedHashMap<>();
            int slotCount = 0;

            for (int i = 0; i < handler.slots.size(); i++) {
               Slot slot = handler.slots.get(i);
               if (slot != null && !autismclient.util.InventorySlotHelper.isPlayerInventorySlot(mc, slot)) {
                  ItemStack stack = slot.getItem();
                  if (stack != null && !stack.isEmpty()) {
                     int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, slot.index);
                     if (this.init_Z_ItemStack_I(stack, visibleSlot) && this.init(mc, handler, i, 0, ContainerInput.QUICK_MOVE)) {
                        slotCount++;
                        items.merge(this.init_Str_ItemStack(stack), stack.getCount(), Integer::sum);
                     }
                  }
               }
            }

            result[0] = new InventoryAuditAction.Inner_NetHelper(slotCount, items);
            mc.player.closeContainer();
            if (mc.gui.screen() != null) {
               mc.gui.setScreen(null);
            }
         } finally {
            latch.countDown();
         }
      });
      this.init_Z_CountDownLatch_J(latch, 3000L);
      return result[0] != null ? result[0] : new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
   }

   private InventoryAuditAction.Inner_NetHelper update(Minecraft mc) throws InterruptedException {
      InventoryAuditAction.Inner_NetHelper transfer = this.pop(mc);
      this.load_V_2(mc);
      this.init_V_J(100L);
      this.init_V_Minecraft_Z(mc, false);
      return transfer;
   }

   private InventoryAuditAction.Inner_NetHelper sync(Minecraft mc) throws InterruptedException {
      AutismSessionState shared = AutismSessionState.init_ck();
      boolean previousDelayPackets = shared.write_Z();
      boolean previousUseCustomPackets = shared.reset_Z();
      Set<Class<? extends Packet<?>>> previousC2SPackets = new HashSet<>(shared.load_Set());
      Set<Class<? extends Packet<?>>> previousS2CPackets = new HashSet<>(shared.read_Set());
      shared.tick(true);
      shared.init_V_Set(Set.of(ServerboundContainerClickPacket.class));
      shared.load_V_Set(Set.of());
      shared.write(true);

      try {
         InventoryAuditAction.Inner_NetHelper transfer = this.push(mc);
         int[] flushed = new int[]{0};
         CountDownLatch flushLatch = new CountDownLatch(1);
         mc.execute(() -> {
            try {
               if (mc.getConnection() != null && mc.player != null) {
                  flushed[0] = shared.init_I_ClientPacketListen(mc.getConnection());
                  if (mc.player.containerMenu != null) {
                     mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
                  }
               }
            } finally {
               flushLatch.countDown();
            }
         });
         this.init_Z_CountDownLatch_J(flushLatch, 3000L);
         this.init_V_Minecraft_Z(mc, false);
         return transfer;
      } finally {
         shared.write(previousDelayPackets);
         shared.tick(previousUseCustomPackets);
         shared.init_V_Set(previousC2SPackets);
         shared.load_V_Set(previousS2CPackets);
      }
   }

   private InventoryAuditAction.Inner_NetHelper flush(Minecraft mc) throws InterruptedException {
      AutismSessionState shared = AutismSessionState.init_ck();
      boolean previousDelayPackets = shared.write_Z();
      boolean previousUseCustomPackets = shared.reset_Z();
      Set<Class<? extends Packet<?>>> previousC2SPackets = new HashSet<>(shared.load_Set());
      Set<Class<? extends Packet<?>>> previousS2CPackets = new HashSet<>(shared.read_Set());
      shared.tick(true);
      shared.init_V_Set(Set.of(ServerboundContainerClickPacket.class));
      shared.load_V_Set(Set.of());
      shared.write(true);

      try {
         InventoryAuditAction.Inner_NetHelper transfer = this.push(mc);
         CountDownLatch flushLatch = new CountDownLatch(1);
         mc.execute(() -> {
            try {
               if (mc.getConnection() != null && mc.player != null) {
                  shared.init_I_ClientPacketListen(mc.getConnection());
                  if (mc.player.containerMenu != null) {
                     mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
                  }
               }
            } finally {
               flushLatch.countDown();
            }
         });
         this.init_Z_CountDownLatch_J(flushLatch, 3000L);
         this.init_V_J(100L);
         this.init_V_Minecraft_Z(mc, false);
         return transfer;
      } finally {
         shared.write(previousDelayPackets);
         shared.tick(previousUseCustomPackets);
         shared.init_V_Set(previousC2SPackets);
         shared.load_V_Set(previousS2CPackets);
      }
   }

   private InventoryAuditAction.Inner_NetHelper close(Minecraft mc) throws InterruptedException {
      InventoryAuditAction.Inner_NetHelper[] result = new InventoryAuditAction.Inner_NetHelper[1];
      CountDownLatch latch = new CountDownLatch(1);
      mc.execute(() -> {
         try {
            if (mc.player == null || mc.gameMode == null) {
               result[0] = new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
               return;
            }

            AbstractContainerMenu handler = mc.player.containerMenu;
            if (handler == null || handler == mc.player.inventoryMenu) {
               result[0] = new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
               return;
            }

            Map<String, Integer> items = new LinkedHashMap<>();
            int slotCount = 0;
            int hotbarSlot = 0;

            for (int i = 0; i < handler.slots.size() && hotbarSlot <= 8; i++) {
               Slot slot = handler.slots.get(i);
               if (slot != null && !autismclient.util.InventorySlotHelper.isPlayerInventorySlot(mc, slot)) {
                  ItemStack stack = slot.getItem();
                  if (stack != null && !stack.isEmpty()) {
                     int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, slot.index);
                     if (this.init_Z_ItemStack_I(stack, visibleSlot) && this.init(mc, handler, i, 0, ContainerInput.QUICK_MOVE)) {
                        slotCount++;
                        items.merge(this.init_Str_ItemStack(stack), stack.getCount(), Integer::sum);
                     }
                  }
               }
            }

            result[0] = new InventoryAuditAction.Inner_NetHelper(slotCount, items);
            mc.player.closeContainer();
            if (mc.gui.screen() != null) {
               mc.gui.setScreen(null);
            }
         } finally {
            latch.countDown();
         }
      });
      this.init_Z_CountDownLatch_J(latch, 3000L);
      return result[0] != null ? result[0] : new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
   }

   private InventoryAuditAction.Inner_NetHelper open(Minecraft mc) throws InterruptedException {
      InventoryAuditAction.Inner_NetHelper transfer = this.tick(mc);
      this.init_V_Minecraft_Z(mc, false);
      return transfer;
   }

   private InventoryAuditAction.Inner_NetHelper poll(Minecraft mc) throws InterruptedException {
      AutismSessionState shared = AutismSessionState.init_ck();
      boolean previousDelayPackets = shared.write_Z();
      boolean previousUseCustomPackets = shared.reset_Z();
      Set<Class<? extends Packet<?>>> previousC2SPackets = new HashSet<>(shared.load_Set());
      Set<Class<? extends Packet<?>>> previousS2CPackets = new HashSet<>(shared.read_Set());
      shared.tick(true);
      shared.init_V_Set(Set.of(ServerboundContainerClickPacket.class));
      shared.load_V_Set(Set.of());
      shared.write(true);

      try {
         InventoryAuditAction.Inner_NetHelper transfer = this.tick(mc);
         CountDownLatch flushLatch = new CountDownLatch(1);
         mc.execute(() -> {
            try {
               if (mc.getConnection() != null && mc.player != null) {
                  shared.init_I_ClientPacketListen(mc.getConnection());
                  if (mc.player.containerMenu != null) {
                     mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
                  }
               }
            } finally {
               flushLatch.countDown();
            }
         });
         this.init_Z_CountDownLatch_J(flushLatch, 3000L);
         this.init_V_Minecraft_Z(mc, false);
         return transfer;
      } finally {
         shared.write(previousDelayPackets);
         shared.tick(previousUseCustomPackets);
         shared.init_V_Set(previousC2SPackets);
         shared.load_V_Set(previousS2CPackets);
      }
   }

   private void init_V_Minecraft_Str(Minecraft mc, String cmd) {
      mc.execute(() -> {
         if (!autismclient.modules.aD.read_Z() && MacroExecutor.init_Z() && mc.getConnection() != null) {
            String clean = cmd.startsWith("/") ? cmd.substring(1) : cmd;
            mc.getConnection().sendCommand(clean);
         }
      });
   }

   private void init_V_Minecraft_BlockPos(Minecraft mc, BlockPos pos) {
      mc.execute(() -> {
         if (!autismclient.modules.aD.read_Z() && MacroExecutor.init_Z() && mc.player != null && mc.getConnection() != null && pos != null) {
            autismclient.util.InteractionTargetRef target = autismclient.util.InteractionTargetRef.init_E_BlockPos(pos);
            if (target != null) {
               target.init_Z_Minecraft(mc);
            }
         }
      });
   }

   private void load_V_2(Minecraft mc) throws InterruptedException {
      CountDownLatch latch = new CountDownLatch(1);
      mc.execute(() -> {
         try {
            if (mc.player != null && mc.getConnection() != null && mc.player.containerMenu != null) {
               mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
            }
         } finally {
            latch.countDown();
         }
      });
      this.init_Z_CountDownLatch_J(latch, 2000L);
   }

   private boolean init_Z_Minecraft_J(Minecraft mc, long timeoutMs) throws InterruptedException {
      CompletableFuture<Void> future = MacroWaitRegistry.init_CompletableFuture("");

      try {
         return this.init_Z_CompletableFuture_J(future, timeoutMs);
      } finally {
         future.cancel(true);
      }
   }

   private boolean load_Z_Minecraft_J(Minecraft mc, long timeoutMs) throws InterruptedException {
      if (!this.init_ListPool_StreamUti_Minecraft(mc).init_Z() && !this.init_Z_Minecraft_J(mc, timeoutMs)) {
         return false;
      }

      long deadline = System.currentTimeMillis() + timeoutMs;
      InventoryAuditAction.Inner_StreamUtil previous = null;
      int stablePolls = 0;

      while (System.currentTimeMillis() <= deadline) {
         this.init_V_4();
         InventoryAuditAction.Inner_StreamUtil current = this.init_ListPool_StreamUti_Minecraft(mc);
         if (current.init_Z()) {
            if (current.equals(previous)) {
               stablePolls++;
            } else {
               previous = current;
               stablePolls = 1;
            }

            if (stablePolls >= 3) {
               return true;
            }
         } else {
            previous = null;
            stablePolls = 0;
         }

         long remaining = deadline - System.currentTimeMillis();
         if (remaining <= 0L) {
            break;
         }

         this.init_V_J(Math.min(50L, remaining));
      }

      return false;
   }

   private boolean read_Z_Minecraft_J(Minecraft mc, long timeoutMs) throws InterruptedException {
      long deadline = System.currentTimeMillis() + timeoutMs;

      while (System.currentTimeMillis() <= deadline) {
         this.init_V_4();
         if (!this.init_ListPool_StreamUti_Minecraft(mc).init_Z()) {
            return true;
         }

         long remaining = deadline - System.currentTimeMillis();
         if (remaining <= 0L) {
            break;
         }

         this.init_V_J(Math.min(50L, remaining));
      }

      return !this.init_ListPool_StreamUti_Minecraft(mc).init_Z();
   }

   private boolean write(Minecraft mc, long timeoutMs) throws InterruptedException {
      if (this.init_ListPool_StreamUti_Minecraft(mc).init_Z()) {
         this.init_V_Minecraft_Z(mc, true);
      }

      return this.read_Z_Minecraft_J(mc, timeoutMs);
   }

   private boolean init_Z_Minecraft(Minecraft mc, String cmd, long timeoutMs) throws InterruptedException {
      long deadline = System.currentTimeMillis() + timeoutMs;
      long retryIntervalMs = this.init_J();
      long nextAttemptAt = 0L;
      int attempts = 0;
      int maxAttempts = this.init_I();
      InventoryAuditAction.Inner_StreamUtil previous = null;
      int stablePolls = 0;

      while (System.currentTimeMillis() <= deadline) {
         this.init_V_4();
         long now = System.currentTimeMillis();
         InventoryAuditAction.Inner_StreamUtil current = this.init_ListPool_StreamUti_Minecraft(mc);
         if (current.init_Z()) {
            if (current.equals(previous)) {
               stablePolls++;
            } else {
               previous = current;
               stablePolls = 1;
            }

            if (stablePolls >= 3) {
               return true;
            }
         } else {
            previous = null;
            stablePolls = 0;
            if (attempts < maxAttempts && now >= nextAttemptAt) {
               if (this.openMode == InventoryAuditAction.Inner_ConfigStore.CONTAINER) {
                  this.init_V_Minecraft_BlockPos(mc, this.containerPos);
               } else {
                  this.init_V_Minecraft_Str(mc, cmd);
               }

               attempts++;
               nextAttemptAt = now + retryIntervalMs;
            }
         }

         long remaining = deadline - System.currentTimeMillis();
         if (remaining <= 0L) {
            break;
         }

         this.init_V_J(Math.min(50L, remaining));
      }

      return false;
   }

   private void init(Minecraft mc, String cmd, int totalOpens, int delayMs) throws InterruptedException {
      for (int spam = 1; spam < totalOpens; spam++) {
         this.init_V_J(delayMs);
         if (this.openMode == InventoryAuditAction.Inner_ConfigStore.CONTAINER) {
            this.init_V_Minecraft_BlockPos(mc, this.containerPos);
         } else {
            this.init_V_Minecraft_Str(mc, cmd);
         }
      }
   }

   private int init_I() {
      if (this.mode == InventoryAuditAction.Inner_BufferHelper.DUPE_SPAM) {
         return Math.max(2, Math.min(this.spamCount, this.openMode == InventoryAuditAction.Inner_ConfigStore.CONTAINER ? 8 : 4));
      } else {
         return this.openMode == InventoryAuditAction.Inner_ConfigStore.CONTAINER ? 4 : 2;
      }
   }

   private long init_J() {
      int configuredDelay = this.mode == InventoryAuditAction.Inner_BufferHelper.DUPE_SPAM
         ? Math.max(10, Math.min(this.spamDelayMs, 1000))
         : Math.max(10, Math.min(this.transferRetryDelayMs, 500));
      long fallback = this.openMode == InventoryAuditAction.Inner_ConfigStore.CONTAINER ? 175L : 250L;
      return Math.max(50L, Math.min(configuredDelay > 0 ? configuredDelay : fallback, 400L));
   }

   private InventoryAuditAction.Inner_StreamUtil init_ListPool_StreamUti_Minecraft(Minecraft mc) throws InterruptedException {
      InventoryAuditAction.Inner_StreamUtil[] state = new InventoryAuditAction.Inner_StreamUtil[]{new InventoryAuditAction.Inner_StreamUtil(false, -1, -1, 0, 0)};
      CountDownLatch latch = new CountDownLatch(1);
      mc.execute(() -> {
         try {
            if (mc.player == null || mc.player.containerMenu == null || mc.player.containerMenu == mc.player.inventoryMenu) {
               return;
            }

            AbstractContainerMenu handler = mc.player.containerMenu;
            int filledSlots = 0;
            int totalItems = 0;

            for (Slot slot : handler.slots) {
               if (slot != null) {
                  ItemStack stack = slot.getItem();
                  if (stack != null && !stack.isEmpty()) {
                     filledSlots++;
                     totalItems += stack.getCount();
                  }
               }
            }

            state[0] = new InventoryAuditAction.Inner_StreamUtil(true, handler.containerId, handler.slots.size(), filledSlots, totalItems);
         } finally {
            latch.countDown();
         }
      });
      this.init_Z_CountDownLatch_J(latch, 2000L);
      return state[0];
   }

   private void init_V_Minecraft_Z(Minecraft mc, boolean sendPacket) throws InterruptedException {
      CountDownLatch latch = new CountDownLatch(1);
      mc.execute(() -> {
         try {
            if (mc.player != null) {
               if (!sendPacket) {
                  AutismSessionState.init_ck().stop(true);
               }

               if (mc.player.containerMenu != mc.player.inventoryMenu) {
                  mc.player.closeContainer();
               }

               if (mc.gui.screen() != null) {
                  mc.gui.setScreen(null);
               }
            }
         } finally {
            latch.countDown();
         }
      });
      this.init_Z_CountDownLatch_J(latch, 2000L);
   }

   private InventoryAuditAction.Inner_NetHelper push(Minecraft mc) throws InterruptedException {
      InventoryAuditAction.Inner_NetHelper[] result = new InventoryAuditAction.Inner_NetHelper[1];
      CountDownLatch latch = new CountDownLatch(1);
      mc.execute(() -> {
         try {
            if (mc.player == null || mc.gameMode == null) {
               result[0] = new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
               return;
            }

            AbstractContainerMenu handler = mc.player.containerMenu;
            if (handler == null || handler == mc.player.inventoryMenu) {
               result[0] = new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
               return;
            }

            Map<String, Integer> items = new LinkedHashMap<>();
            int slotCount = 0;

            for (int i = 0; i < handler.slots.size(); i++) {
               Slot slot = handler.slots.get(i);
               if (slot != null && !autismclient.util.InventorySlotHelper.isPlayerInventorySlot(mc, slot)) {
                  ItemStack stack = slot.getItem();
                  if (stack != null && !stack.isEmpty()) {
                     int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, slot.index);
                     if (this.init_Z_ItemStack_I(stack, visibleSlot) && this.init(mc, handler, i, 0, ContainerInput.QUICK_MOVE)) {
                        slotCount++;
                        items.merge(this.init_Str_ItemStack(stack), stack.getCount(), Integer::sum);
                     }
                  }
               }
            }

            result[0] = new InventoryAuditAction.Inner_NetHelper(slotCount, items);
         } finally {
            latch.countDown();
         }
      });
      this.init_Z_CountDownLatch_J(latch, 3000L);
      return result[0] != null ? result[0] : new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
   }

   private InventoryAuditAction.Inner_NetHelper pop(Minecraft mc) throws InterruptedException {
      InventoryAuditAction.Inner_NetHelper[] result = new InventoryAuditAction.Inner_NetHelper[1];
      CountDownLatch latch = new CountDownLatch(1);
      mc.execute(() -> {
         try {
            if (mc.player == null || mc.gameMode == null) {
               result[0] = new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
               return;
            }

            AbstractContainerMenu handler = mc.player.containerMenu;
            if (handler == null || handler == mc.player.inventoryMenu) {
               result[0] = new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
               return;
            }

            Map<String, Integer> items = new LinkedHashMap<>();
            int slotCount = 0;
            int hotbarSlot = 0;

            for (int i = 0; i < handler.slots.size() && hotbarSlot <= 8; i++) {
               Slot slot = handler.slots.get(i);
               if (slot != null && !autismclient.util.InventorySlotHelper.isPlayerInventorySlot(mc, slot)) {
                  ItemStack stack = slot.getItem();
                  if (stack != null && !stack.isEmpty()) {
                     int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, slot.index);
                     if (this.init_Z_ItemStack_I(stack, visibleSlot) && this.init(mc, handler, i, 0, ContainerInput.QUICK_MOVE)) {
                        slotCount++;
                        items.merge(this.init_Str_ItemStack(stack), stack.getCount(), Integer::sum);
                     }
                  }
               }
            }

            result[0] = new InventoryAuditAction.Inner_NetHelper(slotCount, items);
         } finally {
            latch.countDown();
         }
      });
      this.init_Z_CountDownLatch_J(latch, 3000L);
      return result[0] != null ? result[0] : new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
   }

   private InventoryAuditAction.Inner_NetHelper tick(Minecraft mc) throws InterruptedException {
      InventoryAuditAction.Inner_NetHelper[] result = new InventoryAuditAction.Inner_NetHelper[1];
      CountDownLatch latch = new CountDownLatch(1);
      mc.execute(() -> {
         try {
            if (mc.player == null || mc.gameMode == null) {
               result[0] = new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
               return;
            }

            AbstractContainerMenu handler = mc.player.containerMenu;
            if (handler == null || handler == mc.player.inventoryMenu) {
               result[0] = new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
               return;
            }

            Map<String, Integer> items = new LinkedHashMap<>();
            int slotCount = 0;

            for (int i = 0; i < handler.slots.size(); i++) {
               Slot slot = handler.slots.get(i);
               if (slot != null && !autismclient.util.InventorySlotHelper.isPlayerInventorySlot(mc, slot)) {
                  ItemStack stack = slot.getItem();
                  if (stack != null && !stack.isEmpty()) {
                     int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, slot.index);
                     if (this.init_Z_ItemStack_I(stack, visibleSlot) && this.init(mc, handler, i, 1, ContainerInput.THROW)) {
                        slotCount++;
                        items.merge(this.init_Str_ItemStack(stack), stack.getCount(), Integer::sum);
                     }
                  }
               }
            }

            result[0] = new InventoryAuditAction.Inner_NetHelper(slotCount, items);
         } finally {
            latch.countDown();
         }
      });
      this.init_Z_CountDownLatch_J(latch, 3000L);
      return result[0] != null ? result[0] : new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
   }

   private boolean init(Minecraft mc, AbstractContainerMenu handler, int slotId, int button, ContainerInput input) {
      if (mc == null || mc.player == null || mc.gameMode == null || handler == null) {
         return false;
      }

      if (mc.player.containerMenu != handler) {
         return false;
      }

      if (slotId >= 0 && slotId < handler.slots.size()) {
         Slot slot = handler.slots.get(slotId);
         if (slot != null && !autismclient.util.InventorySlotHelper.isPlayerInventorySlot(mc, slot)) {
            ItemStack beforeSlot = slot.getItem().copy();
            if (beforeSlot.isEmpty()) {
               return false;
            }

            mc.gameMode.handleContainerInput(handler.containerId, slotId, button, input, mc.player);
            if (slotId >= handler.slots.size()) {
               return true;
            }

            Slot afterSlot = handler.slots.get(slotId);
            if (afterSlot == null) {
               return true;
            }

            ItemStack afterStack = afterSlot.getItem();
            return afterStack.isEmpty() ? true : ItemStack.isSameItemSameComponents(beforeSlot, afterStack) && afterStack.getCount() < beforeSlot.getCount();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean load(Minecraft mc, AbstractContainerMenu handler, int slotId, int button, ContainerInput input) {
      if (mc == null || mc.player == null || mc.gameMode == null || handler == null) {
         return false;
      }

      if (mc.player.containerMenu != handler) {
         return false;
      }

      if (slotId >= 0 && slotId < handler.slots.size()) {
         Slot slot = handler.slots.get(slotId);
         if (slot != null && autismclient.util.InventorySlotHelper.isPlayerInventorySlot(mc, slot)) {
            ItemStack beforeSlot = slot.getItem().copy();
            if (beforeSlot.isEmpty()) {
               return false;
            }

            mc.gameMode.handleContainerInput(handler.containerId, slotId, button, input, mc.player);
            if (slotId >= handler.slots.size()) {
               return true;
            }

            Slot afterSlot = handler.slots.get(slotId);
            if (afterSlot == null) {
               return true;
            }

            ItemStack afterStack = afterSlot.getItem();
            return afterStack.isEmpty() ? true : ItemStack.isSameItemSameComponents(beforeSlot, afterStack) && afterStack.getCount() < beforeSlot.getCount();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean init_Z_ItemStack_I(ItemStack stack, int visibleSlot) {
      if (stack != null && !stack.isEmpty()) {
         return this.targetItems.isEmpty() ? false : this.load_Z_ItemStack_I(stack, visibleSlot);
      } else {
         return false;
      }
   }

   private boolean load_Z_ItemStack_I(ItemStack stack, int visibleSlot) {
      List<MacroItemTarget> targets = this.load_List_2();
      if (targets.isEmpty()) {
         return false;
      }

      for (MacroItemTarget itemTarget : targets) {
         if (!itemTarget.init_Z() || itemTarget.init_I == visibleSlot) {
            if (!itemTarget.update() || itemTarget.init_Z_ItemStack_I(stack, visibleSlot)) {
               return true;
            }
         } else if (itemTarget.update()) {
            MacroItemTarget identityTarget = itemTarget.init_ArrayUtil();
            identityTarget.init_I = -1;
            if (identityTarget.init_Z_ItemStack_I(stack, visibleSlot)) {
               return true;
            }
         }
      }

      return false;
   }

   private Map<String, Integer> init_Map_ListPool_CachePool(InventoryAuditAction.Inner_CachePool snapshot) {
      Map<String, Integer> totals = new LinkedHashMap<>();
      if (snapshot == null) {
         return totals;
      }

      for (InventoryAuditAction.Inner_TaskQueue slot : snapshot.init_Map().values()) {
         ItemStack stack = slot.init_ItemStack();
         if (stack != null && !stack.isEmpty() && !this.init_Z_I(slot.init_I()) && this.init_Z_ItemStack_I(stack, slot.init_I())) {
            totals.merge(this.init_Str_ItemStack(stack), stack.getCount(), Integer::sum);
         }
      }

      return totals;
   }

   private int init_I_Map_Set(Map<String, Integer> totals, Set<String> items) {
      int sum = 0;

      for (String item : items) {
         sum += totals.getOrDefault(item, 0);
      }

      return sum;
   }

   private boolean init_Z_I(int visibleSlot) {
      return visibleSlot >= 0 && visibleSlot < 41;
   }

   private InventoryAuditAction.Inner_CachePool init_ListPool_CachePool_Minecraft(Minecraft mc) throws InterruptedException {
      CompletableFuture<InventoryAuditAction.Inner_CachePool> future = new CompletableFuture<>();
      mc.execute(
         () -> {
            try {
               if (mc.player == null || mc.player.containerMenu == null) {
                  future.complete(null);
                  return;
               }

               AbstractContainerMenu handler = mc.player.containerMenu;
               String title = mc.gui.screen() != null && mc.gui.screen().getTitle() != null ? mc.gui.screen().getTitle().getString() : "";
               Map<Integer, InventoryAuditAction.Inner_TaskQueue> slots = new LinkedHashMap<>();

               for (Slot slot : handler.slots) {
                  if (slot != null) {
                     int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, slot.index);
                     if (this.init_Z_I(visibleSlot)) {
                        slots.put(
                           visibleSlot, new InventoryAuditAction.Inner_TaskQueue(visibleSlot, this.init_Str_I(visibleSlot), this.init_ItemStack_ItemStack(slot.getItem()))
                        );
                     }
                  }
               }

               future.complete(new InventoryAuditAction.Inner_CachePool(title, System.currentTimeMillis(), slots));
            } catch (Exception e) {
               future.complete(null);
            }
         }
      );
      return this.init_Obj_CompletableFuture_J(future, 3000L);
   }

   private Map<String, Integer> load(InventoryAuditAction.Inner_CachePool snapshot) {
      Map<String, Integer> totals = new LinkedHashMap<>();
      List<MacroItemTarget> targets = this.load_List_2();
      if (snapshot != null && !targets.isEmpty()) {
         for (InventoryAuditAction.Inner_TaskQueue slot : snapshot.init_Map().values()) {
            ItemStack stack = slot.init_ItemStack();
            if (stack != null && !stack.isEmpty()) {
               String itemId = this.init_Str_ItemStack(stack);

               for (MacroItemTarget itemTarget : targets) {
                  if (this.init_Z_ArrayUtil(itemTarget, stack, slot.init_I())) {
                     totals.merge(itemId, stack.getCount(), Integer::sum);
                     break;
                  }
               }
            }
         }

         return totals;
      } else {
         return totals;
      }
   }

   private boolean init_Z_ArrayUtil(MacroItemTarget itemTarget, ItemStack stack, int visibleSlot) {
      if (itemTarget == null) {
         return false;
      } else if (itemTarget.update()) {
         MacroItemTarget identityTarget = itemTarget.init_ArrayUtil();
         identityTarget.init_I = -1;
         return identityTarget.init_Z_ItemStack_I(stack, visibleSlot);
      } else {
         return itemTarget.init_Z_ItemStack_I(stack, visibleSlot);
      }
   }

   private boolean init_Z_Minecraft_Map(Minecraft mc, Map<String, Integer> targetItemsToTransfer) throws InterruptedException {
      if (targetItemsToTransfer != null && !targetItemsToTransfer.isEmpty()) {
         boolean[] result = new boolean[]{false};
         CountDownLatch latch = new CountDownLatch(1);
         mc.execute(
            () -> {
               try {
                  if (mc.player != null && mc.player.containerMenu != null && mc.gameMode != null) {
                     AbstractContainerMenu handler = mc.player.containerMenu;
                     if (handler != mc.player.inventoryMenu) {
                        for (int i = 0; i < handler.slots.size(); i++) {
                           if (!MacroExecutor.init_Z()) {
                              return;
                           }

                           Slot slot = handler.slots.get(i);
                           if (slot != null && autismclient.util.InventorySlotHelper.isPlayerInventorySlot(mc, slot)) {
                              ItemStack stack = slot.getItem();
                              if (stack != null && !stack.isEmpty()) {
                                 int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, slot.index);
                                 if (this.init_Z_I(visibleSlot)
                                    && targetItemsToTransfer.containsKey(this.init_Str_ItemStack(stack))
                                    && this.load(mc, handler, i, 0, ContainerInput.QUICK_MOVE)) {
                                    result[0] = true;
                                    if (!this.multipleStacks) {
                                       return;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               } finally {
                  latch.countDown();
               }
            }
         );
         this.init_Z_CountDownLatch_J(latch, 3000L);
         return result[0];
      } else {
         return false;
      }
   }

   private boolean read_Z_ItemStack_I(ItemStack stack, int visibleSlot) {
      List<MacroItemTarget> targets = this.load_List_2();
      if (targets.isEmpty()) {
         return false;
      }

      for (MacroItemTarget itemTarget : targets) {
         if (this.init_Z_ArrayUtil(itemTarget, stack, visibleSlot)) {
            return true;
         }
      }

      return false;
   }

   private boolean load_Z_Minecraft_Map(Minecraft mc, Map<String, Integer> targetItemsToExtract) throws InterruptedException {
      if (targetItemsToExtract != null && !targetItemsToExtract.isEmpty()) {
         boolean[] result = new boolean[]{false};
         CountDownLatch latch = new CountDownLatch(1);
         mc.execute(
            () -> {
               try {
                  if (mc.player != null && mc.player.containerMenu != null && mc.gameMode != null) {
                     AbstractContainerMenu handler = mc.player.containerMenu;

                     for (int i = 0; i < handler.slots.size(); i++) {
                        if (!MacroExecutor.init_Z()) {
                           return;
                        }

                        Slot slot = handler.slots.get(i);
                        if (slot != null && !autismclient.util.InventorySlotHelper.isPlayerInventorySlot(mc, slot)) {
                           ItemStack stack = slot.getItem();
                           if (stack != null && !stack.isEmpty()) {
                              int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, slot.index);
                              if (this.init_Z_ItemStack_I(stack, visibleSlot)
                                 && targetItemsToExtract.containsKey(this.init_Str_ItemStack(stack))
                                 && this.init(mc, handler, i, 0, ContainerInput.QUICK_MOVE)) {
                                 result[0] = true;
                                 if (!this.multipleStacks) {
                                    return;
                                 }
                              }
                           }
                        }
                     }
                  }
               } finally {
                  latch.countDown();
               }
            }
         );
         this.init_Z_CountDownLatch_J(latch, 3000L);
         return result[0];
      } else {
         return false;
      }
   }

   private InventoryAuditAction.Inner_CachePool load_ListPool_CachePool(Minecraft mc) throws InterruptedException {
      CompletableFuture<InventoryAuditAction.Inner_CachePool> future = new CompletableFuture<>();
      mc.execute(() -> {
         try {
            if (mc.player == null || mc.player.containerMenu == null) {
               future.complete(null);
               return;
            }

            AbstractContainerMenu handler = mc.player.containerMenu;
            future.complete(this.init_ListPool_CachePool_Minecraft_AbstractContainerM(mc, handler));
         } catch (Exception e) {
            future.complete(null);
         }
      });
      return this.init_Obj_CompletableFuture_J(future, 3000L);
   }

   private InventoryAuditAction.Inner_CachePool init_ListPool_CachePool_Minecraft_AbstractContainerM(Minecraft mc, AbstractContainerMenu handler) {
      String title = mc.gui.screen() != null && mc.gui.screen().getTitle() != null ? mc.gui.screen().getTitle().getString() : "";
      Map<Integer, InventoryAuditAction.Inner_TaskQueue> slots = this.init_Map_Minecraft_AbstractContainerM(mc, handler);
      return new InventoryAuditAction.Inner_CachePool(title, System.currentTimeMillis(), slots);
   }

   private Map<Integer, InventoryAuditAction.Inner_TaskQueue> init_Map_Minecraft_AbstractContainerM(Minecraft mc, AbstractContainerMenu handler) {
      Map<Integer, InventoryAuditAction.Inner_TaskQueue> slots = new LinkedHashMap<>();

      for (Slot slot : handler.slots) {
         if (slot != null) {
            int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, slot.index);
            slots.putIfAbsent(
               visibleSlot, new InventoryAuditAction.Inner_TaskQueue(visibleSlot, this.init_Str_I(visibleSlot), this.init_ItemStack_ItemStack(slot.getItem()))
            );
         }
      }

      return slots;
   }

   private ItemStack init_ItemStack_ItemStack(ItemStack stack) {
      return stack != null && !stack.isEmpty() ? stack.copy() : ItemStack.EMPTY;
   }

   private String init_Str_I(int visibleSlot) {
      return "Slot " + visibleSlot;
   }

   private String init_Str_ItemStack(ItemStack stack) {
      String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
      return itemId.startsWith("minecraft:") ? itemId.substring("minecraft:".length()) : itemId;
   }

   private void init_V_4() throws InterruptedException {
      if (Thread.currentThread().isInterrupted() || !MacroExecutor.init_Z()) {
         throw new InterruptedException("Macro stopped");
      }
   }

   private void init_V_J(long millis) throws InterruptedException {
      long remaining = Math.max(0L, millis);

      while (remaining > 0L) {
         this.init_V_4();
         long slice = Math.min(remaining, 25L);
         Thread.sleep(slice);
         remaining -= slice;
      }
   }

   private boolean init_Z_CountDownLatch_J(CountDownLatch latch, long timeoutMs) throws InterruptedException {
      long deadline = System.currentTimeMillis() + Math.max(0L, timeoutMs);

      while (latch.getCount() > 0L) {
         this.init_V_4();
         long remaining = deadline - System.currentTimeMillis();
         if (remaining <= 0L) {
            return false;
         }

         if (latch.await(Math.min(remaining, 25L), TimeUnit.MILLISECONDS)) {
            return true;
         }
      }

      return true;
   }

   private boolean init_Z_CompletableFuture_J(CompletableFuture<?> future, long timeoutMs) throws InterruptedException {
      long deadline = System.currentTimeMillis() + Math.max(0L, timeoutMs);

      while (true) {
         this.init_V_4();
         long remaining = deadline - System.currentTimeMillis();
         if (remaining <= 0L) {
            future.cancel(true);
            return false;
         }

         try {
            future.get(Math.min(remaining, 25L), TimeUnit.MILLISECONDS);
            return true;
         } catch (TimeoutException var9) {
         } catch (CancellationException | ExecutionException e) {
            return false;
         }
      }
   }

   private <T> T init_Obj_CompletableFuture_J(CompletableFuture<T> future, long timeoutMs) throws InterruptedException {
      long deadline = System.currentTimeMillis() + Math.max(0L, timeoutMs);

      while (true) {
         this.init_V_4();
         long remaining = deadline - System.currentTimeMillis();
         if (remaining <= 0L) {
            future.cancel(true);
            return null;
         }

         try {
            return future.get(Math.min(remaining, 25L), TimeUnit.MILLISECONDS);
         } catch (TimeoutException var9) {
         } catch (CancellationException | ExecutionException e) {
            return null;
         }
      }
   }

   private int init_I_I_2(int value) {
      return Math.max(0, Math.min(value, 10000));
   }

   private int load(int value) {
      return Math.max(1, Math.min(value, 100));
   }

   private String init_Str_2() {
      String raw = this.openCommand == null ? "" : this.openCommand.trim();
      return raw.isEmpty() ? "/ec" : raw;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("mode", this.mode.name());
      tag.put("targetItems", MacroItemTarget.init_ListTag_List(this.init_List_3()));
      tag.putString("openCommand", this.init_Str_2());
      tag.putString("openMode", this.openMode.name());
      tag.putInt("containerX", this.containerPos.getX());
      tag.putInt("containerY", this.containerPos.getY());
      tag.putInt("containerZ", this.containerPos.getZ());
      tag.putString("dupeVector", this.dupeVector.name());
      tag.putInt("delayBeforeReopen", this.init_I_I_2(this.delayBeforeReopen));
      tag.putInt("delayAfterReopen", this.init_I_I_2(this.delayAfterReopen));
      tag.putInt("iterations", this.load(this.iterations));
      tag.putInt("maxTransferAttempts", Math.max(1, Math.min(this.maxTransferAttempts, 20)));
      tag.putInt("transferRetryDelayMs", Math.max(10, Math.min(this.transferRetryDelayMs, 500)));
      tag.putBoolean("multipleStacks", this.multipleStacks);
      tag.putInt("spamCount", Math.max(1, Math.min(this.spamCount, 20)));
      tag.putInt("spamDelayMs", Math.max(10, Math.min(this.spamDelayMs, 1000)));
      tag.putInt("grabPreDelayMs", 0);
      tag.putBoolean("closeAfterGrab", true);
      tag.putBoolean("enabled", this.init_Z);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("mode")) {
         try {
            String modeStr = tag.getStringOr("mode", "DUPE");
            if (!"AUTO_TEST".equals(modeStr) && !"GRAB".equals(modeStr) && !"SAVE".equals(modeStr) && !"COMPARE".equals(modeStr) && !"CLEAR".equals(modeStr)) {
               this.mode = InventoryAuditAction.Inner_BufferHelper.valueOf(modeStr);
            } else {
               this.mode = InventoryAuditAction.Inner_BufferHelper.DUPE;
            }
         } catch (IllegalArgumentException ignored) {
            this.mode = InventoryAuditAction.Inner_BufferHelper.DUPE;
         }
      } else {
         this.mode = InventoryAuditAction.Inner_BufferHelper.DUPE;
      }

      this.itemTargets.clear();
      this.targetItems.clear();
      if (tag.contains("targetItems")) {
         ListTag list = tag.getList("targetItems").orElse(new ListTag());
         this.itemTargets.addAll(MacroItemTarget.init_List_ListTag(list));
      }

      this.load_V_2();
      if (tag.contains("openCommand")) {
         this.openCommand = tag.getStringOr("openCommand", "/ec");
      }

      if (tag.contains("openMode")) {
         try {
            this.openMode = InventoryAuditAction.Inner_ConfigStore.valueOf(tag.getStringOr("openMode", "COMMAND"));
         } catch (IllegalArgumentException ignored) {
            this.openMode = InventoryAuditAction.Inner_ConfigStore.COMMAND;
         }
      }

      if (tag.contains("containerX") || tag.contains("containerY") || tag.contains("containerZ")) {
         this.containerPos = new BlockPos(tag.getIntOr("containerX", 0), tag.getIntOr("containerY", 0), tag.getIntOr("containerZ", 0));
      }

      if (tag.contains("dupeVector")) {
         try {
            this.dupeVector = InventoryAuditAction.Inner_DataUtil.valueOf(tag.getStringOr("dupeVector", "DESYNC_REOPEN"));
         } catch (IllegalArgumentException ignored) {
            this.dupeVector = InventoryAuditAction.Inner_DataUtil.DESYNC_REOPEN;
         }
      }

      if (tag.contains("delayBeforeReopen")) {
         this.delayBeforeReopen = tag.getIntOr("delayBeforeReopen", 200);
      }

      if (tag.contains("delayAfterReopen")) {
         this.delayAfterReopen = tag.getIntOr("delayAfterReopen", 500);
      }

      if (tag.contains("iterations")) {
         this.iterations = tag.getIntOr("iterations", 1);
      }

      if (tag.contains("maxTransferAttempts")) {
         this.maxTransferAttempts = tag.getIntOr("maxTransferAttempts", 5);
      }

      if (tag.contains("transferRetryDelayMs")) {
         this.transferRetryDelayMs = tag.getIntOr("transferRetryDelayMs", 50);
      }

      if (tag.contains("multipleStacks")) {
         this.multipleStacks = tag.getBooleanOr("multipleStacks", false);
      }

      if (tag.contains("spamCount")) {
         this.spamCount = tag.getIntOr("spamCount", 3);
      }

      if (tag.contains("spamDelayMs")) {
         this.spamDelayMs = tag.getIntOr("spamDelayMs", 50);
      }

      this.delayBeforeReopen = this.init_I_I_2(this.delayBeforeReopen);
      this.delayAfterReopen = this.init_I_I_2(this.delayAfterReopen);
      this.iterations = this.load(this.iterations);
      this.openCommand = this.init_Str_2();
      if (tag.contains("enabled")) {
         this.init_Z = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.INVENTORY_AUDIT;
   }

   @Override
   public String getDisplayName() {
      String src = this.openMode == InventoryAuditAction.Inner_ConfigStore.CONTAINER
         ? "(" + this.containerPos.getX() + "," + this.containerPos.getY() + "," + this.containerPos.getZ() + ")"
         : this.init_Str_2();
      String var10000;
      switch (this.mode) {
         case DUPE:
            var10000 = "Dupe " + this.dupeVector.name() + " " + src;
            break;
         case DUPE_SPAM:
            var10000 = "DupeSpam " + this.dupeVector.name() + " " + src + " (x" + this.spamCount + ")";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   @Override
   public String getIcon() {
      return this.mode == InventoryAuditAction.Inner_BufferHelper.DUPE_SPAM ? "DSP" : "DUP";
   }

   @Override
   public boolean isEnabled() {
      return this.init_Z;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.init_Z = enabled;
   }

   private List<MacroItemTarget> init_List_3() {
      if (!this.itemTargets.isEmpty()) {
         return this.itemTargets;
      }

      for (String target : this.targetItems) {
         MacroItemTarget parsed = MacroItemTarget.update(target);
         if (parsed.init_Z() || parsed.update()) {
            this.itemTargets.add(parsed);
         }
      }

      return this.itemTargets;
   }

   private List<MacroItemTarget> load_List_2() {
      List<MacroItemTarget> runtime = new ArrayList<>();
      Minecraft mc = Minecraft.getInstance();

      for (MacroItemTarget target : this.init_List_3()) {
         if (target != null) {
            MacroItemTarget resolved = target.init_ArrayUtil_Minecraft(mc);
            if (resolved != null && (resolved.init_Z() || resolved.update())) {
               runtime.add(resolved);
            }
         }
      }

      return runtime;
   }

   private void load_V_2() {
      this.targetItems.clear();

      for (MacroItemTarget target : this.itemTargets) {
         if (target != null) {
            String entry = target.write_Str();
            if (!entry.isBlank()) {
               this.targetItems.add(entry);
            }
         }
      }
   }

   public enum Inner_BufferHelper {
      DUPE,
      DUPE_SPAM;
   }

   private record Inner_CachePool(String init_Str, long init_J, Map<Integer, InventoryAuditAction.Inner_TaskQueue> init_Map) {
   }

   public enum Inner_ConfigStore {
      COMMAND,
      CONTAINER;
   }

   public enum Inner_DataUtil {
      DESYNC_REOPEN,
      CLOSE_NO_PACKET,
      SHIFT_CLICK_REOPEN,
      DELAYED_PACKETS,
      SWAP_HOTBAR,
      DROP_EXPLOIT,
      DELAYED_DESYNC_REOPEN,
      SWAP_DESYNC_REOPEN,
      DROP_DELAYED_PACKETS;
   }

   private record Inner_NetHelper(int init_I, Map<String, Integer> init_Map) {
   }

   private record Inner_StreamUtil(boolean init_Z, int init_I, int load, int read, int write) {
   }

   private record Inner_TaskQueue(int init_I, String init_Str, ItemStack init_ItemStack) {
   }
}
