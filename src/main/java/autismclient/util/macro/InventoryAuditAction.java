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
   private static final int MAX_DELAY = 10000;
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
   private boolean enabled = true;

   public boolean isConfigured() {
      return this.hasConfiguredTarget();
   }

   public boolean hasConfiguredTarget() {
      for (MacroItemTarget target : this.configuredTargets()) {
         if (target != null && (target.hasSlot() || target.hasItemFilter())) {
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

   public void runAudit(Minecraft mc) throws InterruptedException {
      if (mc != null && mc.player != null) {
         MacroTemplateResolver.ResolveResult commandResolution = MacroVariableContext.resolveTemplate(this.openCommandOrDefault(), mc);
         if (commandResolution.success()) {
            String cmd = commandResolution.value().trim();
            if (!cmd.isBlank()) {
               int runIterations = this.load(this.iterations);
               int maxAttempts = Math.max(1, Math.min(this.maxTransferAttempts, 20));
               int retryDelay = Math.max(10, Math.min(this.transferRetryDelayMs, 500));

               for (int iter = 0; iter < runIterations; iter++) {
                  this.checkInterrupted();
                  if (!this.openContainerAndWait(mc, cmd, 12000L)) {
                     return;
                  }

                  InventoryAuditAction.Inner_CachePool before = this.snapshotOpenContainer(mc);
                  if (before == null || before.slots().isEmpty()) {
                     return;
                  }

                  Map<String, Integer> targetContainerItems = this.targetItemTotals(before);
                  if (targetContainerItems.isEmpty()) {
                     InventoryAuditAction.Inner_CachePool playerSnapshot = this.snapshotPlayerInventory(mc);
                     Map<String, Integer> playerTargetItems = this.load(playerSnapshot);
                     if (!playerTargetItems.isEmpty()) {
                        boolean transferred = this.depositPlayerTargets(mc, playerTargetItems);
                        if (transferred) {
                           this.closeOpenContainer(mc, true);
                           if (!this.awaitContainerClosed(mc, 12000L)) {
                              return;
                           }

                           if (!this.openContainerAndWait(mc, cmd, 12000L)) {
                              return;
                           }

                           before = this.snapshotOpenContainer(mc);
                           if (before == null || before.slots().isEmpty()) {
                              return;
                           }

                           targetContainerItems = this.targetItemTotals(before);
                        }
                     }

                     if (targetContainerItems.isEmpty()) {
                        this.closeOpenContainer(mc, true);
                        continue;
                     }
                  }

                  if (this.mode == InventoryAuditAction.Inner_BufferHelper.DUPE_SPAM) {
                     int spamOpens = Math.max(1, Math.min(this.spamCount, 20));
                     int spamDelay = Math.max(10, Math.min(this.spamDelayMs, 1000));
                     this.init(mc, cmd, spamOpens, spamDelay);
                     this.sleep(100L);
                  }

                  this.runDupeVector(mc);
                  int totalTransferAttempts = 0;
                  int emptyVisibleTransferPasses = 0;
                  Set<Integer> targetSlots = this.targetSlots(mc);

                  while (totalTransferAttempts < maxAttempts) {
                     this.checkInterrupted();
                     if (!this.openContainerAndWait(mc, cmd, 12000L)) {
                        break;
                     }

                     this.sleep(retryDelay);
                     InventoryAuditAction.Inner_NetHelper attempt = this.quickMoveTargetSlots(mc, targetSlots);
                     totalTransferAttempts++;
                     if (attempt.slotCount() <= 0 && targetSlots.isEmpty()) {
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

                  if (!this.openContainerAndWait(mc, cmd, 12000L)) {
                     return;
                  }

                  this.closeOpenContainer(mc, true);
                  if (iter < runIterations - 1) {
                     this.sleep(500L);
                  }
               }
            }
         }
      }
   }

   private Set<Integer> targetSlots(Minecraft mc) {
      Set<Integer> slots = new HashSet<>();
      List<MacroItemTarget> targets = this.runtimeTargets();
      if (!targets.isEmpty()) {
         for (MacroItemTarget target : targets) {
            if (target != null && target.hasSlot()) {
               slots.add(target.slot);
            }
         }
      }

      return slots;
   }

   private InventoryAuditAction.Inner_NetHelper quickMoveTargetSlots(Minecraft mc, Set<Integer> targetSlots) throws InterruptedException {
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
                        items.merge(this.itemKey(stack), stack.getCount(), Integer::sum);
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
                        if (this.isTargetStack(stack, visibleSlot)) {
                           firedSlots.add(i);
                           slotCount++;
                           items.merge(this.itemKey(stack), stack.getCount(), Integer::sum);
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
      this.awaitLatch(latch, 2000L);
      return result[0] != null ? result[0] : new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
   }

   private void init(InventoryAuditAction.Inner_CachePool before, InventoryAuditAction.Inner_CachePool after, InventoryAuditAction.Inner_NetHelper transfer, String iterLabel, int transferAttempts) {
   }

   private InventoryAuditAction.Inner_NetHelper runDupeVector(Minecraft mc) throws InterruptedException {
      InventoryAuditAction.Inner_NetHelper var10000;
      switch (this.dupeVector) {
         case DESYNC_REOPEN:
            var10000 = this.desyncReopen(mc);
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

   private InventoryAuditAction.Inner_NetHelper desyncReopen(Minecraft mc) throws InterruptedException {
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
      this.awaitLatch(latch, 2000L);
      this.sleep(100L);
      this.closeOpenContainer(mc, false);
      return transfer;
   }

   private InventoryAuditAction.Inner_NetHelper read(Minecraft mc) throws InterruptedException {
      InventoryAuditAction.Inner_NetHelper transfer = this.push(mc);
      this.closeOpenContainer(mc, false);
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
                     if (this.isTargetStack(stack, visibleSlot) && this.init(mc, handler, i, 0, ContainerInput.QUICK_MOVE)) {
                        slotCount++;
                        items.merge(this.itemKey(stack), stack.getCount(), Integer::sum);
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
      this.awaitLatch(latch, 3000L);
      return result[0] != null ? result[0] : new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
   }

   private InventoryAuditAction.Inner_NetHelper update(Minecraft mc) throws InterruptedException {
      InventoryAuditAction.Inner_NetHelper transfer = this.pop(mc);
      this.sendContainerClose(mc);
      this.sleep(100L);
      this.closeOpenContainer(mc, false);
      return transfer;
   }

   private InventoryAuditAction.Inner_NetHelper sync(Minecraft mc) throws InterruptedException {
      AutismSessionState shared = AutismSessionState.getInstance();
      boolean previousDelayPackets = shared.isDelayPacketsEnabled();
      boolean previousUseCustomPackets = shared.isUseCustomPackets();
      Set<Class<? extends Packet<?>>> previousC2SPackets = new HashSet<>(shared.getCustomC2sPackets());
      Set<Class<? extends Packet<?>>> previousS2CPackets = new HashSet<>(shared.getCustomS2cPackets());
      shared.setUseCustomPackets(true);
      shared.setCustomC2sPackets(Set.of(ServerboundContainerClickPacket.class));
      shared.setCustomS2cPackets(Set.of());
      shared.setDelayPacketsEnabled(true);

      try {
         InventoryAuditAction.Inner_NetHelper transfer = this.push(mc);
         int[] flushed = new int[]{0};
         CountDownLatch flushLatch = new CountDownLatch(1);
         mc.execute(() -> {
            try {
               if (mc.getConnection() != null && mc.player != null) {
                  flushed[0] = shared.flushDelayedPackets(mc.getConnection());
                  if (mc.player.containerMenu != null) {
                     mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
                  }
               }
            } finally {
               flushLatch.countDown();
            }
         });
         this.awaitLatch(flushLatch, 3000L);
         this.closeOpenContainer(mc, false);
         return transfer;
      } finally {
         shared.setDelayPacketsEnabled(previousDelayPackets);
         shared.setUseCustomPackets(previousUseCustomPackets);
         shared.setCustomC2sPackets(previousC2SPackets);
         shared.setCustomS2cPackets(previousS2CPackets);
      }
   }

   private InventoryAuditAction.Inner_NetHelper flush(Minecraft mc) throws InterruptedException {
      AutismSessionState shared = AutismSessionState.getInstance();
      boolean previousDelayPackets = shared.isDelayPacketsEnabled();
      boolean previousUseCustomPackets = shared.isUseCustomPackets();
      Set<Class<? extends Packet<?>>> previousC2SPackets = new HashSet<>(shared.getCustomC2sPackets());
      Set<Class<? extends Packet<?>>> previousS2CPackets = new HashSet<>(shared.getCustomS2cPackets());
      shared.setUseCustomPackets(true);
      shared.setCustomC2sPackets(Set.of(ServerboundContainerClickPacket.class));
      shared.setCustomS2cPackets(Set.of());
      shared.setDelayPacketsEnabled(true);

      try {
         InventoryAuditAction.Inner_NetHelper transfer = this.push(mc);
         CountDownLatch flushLatch = new CountDownLatch(1);
         mc.execute(() -> {
            try {
               if (mc.getConnection() != null && mc.player != null) {
                  shared.flushDelayedPackets(mc.getConnection());
                  if (mc.player.containerMenu != null) {
                     mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
                  }
               }
            } finally {
               flushLatch.countDown();
            }
         });
         this.awaitLatch(flushLatch, 3000L);
         this.sleep(100L);
         this.closeOpenContainer(mc, false);
         return transfer;
      } finally {
         shared.setDelayPacketsEnabled(previousDelayPackets);
         shared.setUseCustomPackets(previousUseCustomPackets);
         shared.setCustomC2sPackets(previousC2SPackets);
         shared.setCustomS2cPackets(previousS2CPackets);
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
                     if (this.isTargetStack(stack, visibleSlot) && this.init(mc, handler, i, 0, ContainerInput.QUICK_MOVE)) {
                        slotCount++;
                        items.merge(this.itemKey(stack), stack.getCount(), Integer::sum);
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
      this.awaitLatch(latch, 3000L);
      return result[0] != null ? result[0] : new InventoryAuditAction.Inner_NetHelper(0, new LinkedHashMap<>());
   }

   private InventoryAuditAction.Inner_NetHelper open(Minecraft mc) throws InterruptedException {
      InventoryAuditAction.Inner_NetHelper transfer = this.tick(mc);
      this.closeOpenContainer(mc, false);
      return transfer;
   }

   private InventoryAuditAction.Inner_NetHelper poll(Minecraft mc) throws InterruptedException {
      AutismSessionState shared = AutismSessionState.getInstance();
      boolean previousDelayPackets = shared.isDelayPacketsEnabled();
      boolean previousUseCustomPackets = shared.isUseCustomPackets();
      Set<Class<? extends Packet<?>>> previousC2SPackets = new HashSet<>(shared.getCustomC2sPackets());
      Set<Class<? extends Packet<?>>> previousS2CPackets = new HashSet<>(shared.getCustomS2cPackets());
      shared.setUseCustomPackets(true);
      shared.setCustomC2sPackets(Set.of(ServerboundContainerClickPacket.class));
      shared.setCustomS2cPackets(Set.of());
      shared.setDelayPacketsEnabled(true);

      try {
         InventoryAuditAction.Inner_NetHelper transfer = this.tick(mc);
         CountDownLatch flushLatch = new CountDownLatch(1);
         mc.execute(() -> {
            try {
               if (mc.getConnection() != null && mc.player != null) {
                  shared.flushDelayedPackets(mc.getConnection());
                  if (mc.player.containerMenu != null) {
                     mc.getConnection().send(new ServerboundContainerClosePacket(mc.player.containerMenu.containerId));
                  }
               }
            } finally {
               flushLatch.countDown();
            }
         });
         this.awaitLatch(flushLatch, 3000L);
         this.closeOpenContainer(mc, false);
         return transfer;
      } finally {
         shared.setDelayPacketsEnabled(previousDelayPackets);
         shared.setUseCustomPackets(previousUseCustomPackets);
         shared.setCustomC2sPackets(previousC2SPackets);
         shared.setCustomS2cPackets(previousS2CPackets);
      }
   }

   private void sendCommand(Minecraft mc, String cmd) {
      mc.execute(() -> {
         if (!autismclient.modules.aD.read_Z() && MacroExecutor.isCurrentRunActive() && mc.getConnection() != null) {
            String clean = cmd.startsWith("/") ? cmd.substring(1) : cmd;
            mc.getConnection().sendCommand(clean);
         }
      });
   }

   private void openContainerAt(Minecraft mc, BlockPos pos) {
      mc.execute(() -> {
         if (!autismclient.modules.aD.read_Z() && MacroExecutor.isCurrentRunActive() && mc.player != null && mc.getConnection() != null && pos != null) {
            autismclient.util.InteractionTargetRef target = autismclient.util.InteractionTargetRef.ofBlock(pos);
            if (target != null) {
               target.interact(mc);
            }
         }
      });
   }

   private void sendContainerClose(Minecraft mc) throws InterruptedException {
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
      this.awaitLatch(latch, 2000L);
   }

   private boolean awaitScreenOpen(Minecraft mc, long timeoutMs) throws InterruptedException {
      CompletableFuture<Void> future = MacroWaitRegistry.awaitGuiTitle("");

      try {
         return this.awaitFuture(future, timeoutMs);
      } finally {
         future.cancel(true);
      }
   }

   private boolean awaitStableContainer(Minecraft mc, long timeoutMs) throws InterruptedException {
      if (!this.sampleContainerState(mc).open() && !this.awaitScreenOpen(mc, timeoutMs)) {
         return false;
      }

      long deadline = System.currentTimeMillis() + timeoutMs;
      InventoryAuditAction.Inner_StreamUtil previous = null;
      int stablePolls = 0;

      while (System.currentTimeMillis() <= deadline) {
         this.checkInterrupted();
         InventoryAuditAction.Inner_StreamUtil current = this.sampleContainerState(mc);
         if (current.open()) {
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

         this.sleep(Math.min(50L, remaining));
      }

      return false;
   }

   private boolean awaitContainerClosed(Minecraft mc, long timeoutMs) throws InterruptedException {
      long deadline = System.currentTimeMillis() + timeoutMs;

      while (System.currentTimeMillis() <= deadline) {
         this.checkInterrupted();
         if (!this.sampleContainerState(mc).open()) {
            return true;
         }

         long remaining = deadline - System.currentTimeMillis();
         if (remaining <= 0L) {
            break;
         }

         this.sleep(Math.min(50L, remaining));
      }

      return !this.sampleContainerState(mc).open();
   }

   private boolean write(Minecraft mc, long timeoutMs) throws InterruptedException {
      if (this.sampleContainerState(mc).open()) {
         this.closeOpenContainer(mc, true);
      }

      return this.awaitContainerClosed(mc, timeoutMs);
   }

   private boolean openContainerAndWait(Minecraft mc, String cmd, long timeoutMs) throws InterruptedException {
      long deadline = System.currentTimeMillis() + timeoutMs;
      long retryIntervalMs = this.retryIntervalMs();
      long nextAttemptAt = 0L;
      int attempts = 0;
      int maxAttempts = this.maxAttempts();
      InventoryAuditAction.Inner_StreamUtil previous = null;
      int stablePolls = 0;

      while (System.currentTimeMillis() <= deadline) {
         this.checkInterrupted();
         long now = System.currentTimeMillis();
         InventoryAuditAction.Inner_StreamUtil current = this.sampleContainerState(mc);
         if (current.open()) {
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
                  this.openContainerAt(mc, this.containerPos);
               } else {
                  this.sendCommand(mc, cmd);
               }

               attempts++;
               nextAttemptAt = now + retryIntervalMs;
            }
         }

         long remaining = deadline - System.currentTimeMillis();
         if (remaining <= 0L) {
            break;
         }

         this.sleep(Math.min(50L, remaining));
      }

      return false;
   }

   private void init(Minecraft mc, String cmd, int totalOpens, int delayMs) throws InterruptedException {
      for (int spam = 1; spam < totalOpens; spam++) {
         this.sleep(delayMs);
         if (this.openMode == InventoryAuditAction.Inner_ConfigStore.CONTAINER) {
            this.openContainerAt(mc, this.containerPos);
         } else {
            this.sendCommand(mc, cmd);
         }
      }
   }

   private int maxAttempts() {
      if (this.mode == InventoryAuditAction.Inner_BufferHelper.DUPE_SPAM) {
         return Math.max(2, Math.min(this.spamCount, this.openMode == InventoryAuditAction.Inner_ConfigStore.CONTAINER ? 8 : 4));
      } else {
         return this.openMode == InventoryAuditAction.Inner_ConfigStore.CONTAINER ? 4 : 2;
      }
   }

   private long retryIntervalMs() {
      int configuredDelay = this.mode == InventoryAuditAction.Inner_BufferHelper.DUPE_SPAM
         ? Math.max(10, Math.min(this.spamDelayMs, 1000))
         : Math.max(10, Math.min(this.transferRetryDelayMs, 500));
      long fallback = this.openMode == InventoryAuditAction.Inner_ConfigStore.CONTAINER ? 175L : 250L;
      return Math.max(50L, Math.min(configuredDelay > 0 ? configuredDelay : fallback, 400L));
   }

   private InventoryAuditAction.Inner_StreamUtil sampleContainerState(Minecraft mc) throws InterruptedException {
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
      this.awaitLatch(latch, 2000L);
      return state[0];
   }

   private void closeOpenContainer(Minecraft mc, boolean sendPacket) throws InterruptedException {
      CountDownLatch latch = new CountDownLatch(1);
      mc.execute(() -> {
         try {
            if (mc.player != null) {
               if (!sendPacket) {
                  AutismSessionState.getInstance().markDropNextContainerClose(true);
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
      this.awaitLatch(latch, 2000L);
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
                     if (this.isTargetStack(stack, visibleSlot) && this.init(mc, handler, i, 0, ContainerInput.QUICK_MOVE)) {
                        slotCount++;
                        items.merge(this.itemKey(stack), stack.getCount(), Integer::sum);
                     }
                  }
               }
            }

            result[0] = new InventoryAuditAction.Inner_NetHelper(slotCount, items);
         } finally {
            latch.countDown();
         }
      });
      this.awaitLatch(latch, 3000L);
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
                     if (this.isTargetStack(stack, visibleSlot) && this.init(mc, handler, i, 0, ContainerInput.QUICK_MOVE)) {
                        slotCount++;
                        items.merge(this.itemKey(stack), stack.getCount(), Integer::sum);
                     }
                  }
               }
            }

            result[0] = new InventoryAuditAction.Inner_NetHelper(slotCount, items);
         } finally {
            latch.countDown();
         }
      });
      this.awaitLatch(latch, 3000L);
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
                     if (this.isTargetStack(stack, visibleSlot) && this.init(mc, handler, i, 1, ContainerInput.THROW)) {
                        slotCount++;
                        items.merge(this.itemKey(stack), stack.getCount(), Integer::sum);
                     }
                  }
               }
            }

            result[0] = new InventoryAuditAction.Inner_NetHelper(slotCount, items);
         } finally {
            latch.countDown();
         }
      });
      this.awaitLatch(latch, 3000L);
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

   private boolean isTargetStack(ItemStack stack, int visibleSlot) {
      if (stack != null && !stack.isEmpty()) {
         return this.targetItems.isEmpty() ? false : this.matchesRuntimeTarget(stack, visibleSlot);
      } else {
         return false;
      }
   }

   private boolean matchesRuntimeTarget(ItemStack stack, int visibleSlot) {
      List<MacroItemTarget> targets = this.runtimeTargets();
      if (targets.isEmpty()) {
         return false;
      }

      for (MacroItemTarget itemTarget : targets) {
         if (!itemTarget.hasSlot() || itemTarget.slot == visibleSlot) {
            if (!itemTarget.hasItemFilter() || itemTarget.matches(stack, visibleSlot)) {
               return true;
            }
         } else if (itemTarget.hasItemFilter()) {
            MacroItemTarget identityTarget = itemTarget.copy();
            identityTarget.slot = -1;
            if (identityTarget.matches(stack, visibleSlot)) {
               return true;
            }
         }
      }

      return false;
   }

   private Map<String, Integer> targetItemTotals(InventoryAuditAction.Inner_CachePool snapshot) {
      Map<String, Integer> totals = new LinkedHashMap<>();
      if (snapshot == null) {
         return totals;
      }

      for (InventoryAuditAction.Inner_TaskQueue slot : snapshot.slots().values()) {
         ItemStack stack = slot.stack();
         if (stack != null && !stack.isEmpty() && !this.isPlayerSlot(slot.visibleSlot()) && this.isTargetStack(stack, slot.visibleSlot())) {
            totals.merge(this.itemKey(stack), stack.getCount(), Integer::sum);
         }
      }

      return totals;
   }

   private int sumOf(Map<String, Integer> totals, Set<String> items) {
      int sum = 0;

      for (String item : items) {
         sum += totals.getOrDefault(item, 0);
      }

      return sum;
   }

   private boolean isPlayerSlot(int visibleSlot) {
      return visibleSlot >= 0 && visibleSlot < 41;
   }

   private InventoryAuditAction.Inner_CachePool snapshotPlayerInventory(Minecraft mc) throws InterruptedException {
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
                     if (this.isPlayerSlot(visibleSlot)) {
                        slots.put(
                           visibleSlot, new InventoryAuditAction.Inner_TaskQueue(visibleSlot, this.slotLabel(visibleSlot), this.copyOrEmpty(slot.getItem()))
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
      return this.join(future, 3000L);
   }

   private Map<String, Integer> load(InventoryAuditAction.Inner_CachePool snapshot) {
      Map<String, Integer> totals = new LinkedHashMap<>();
      List<MacroItemTarget> targets = this.runtimeTargets();
      if (snapshot != null && !targets.isEmpty()) {
         for (InventoryAuditAction.Inner_TaskQueue slot : snapshot.slots().values()) {
            ItemStack stack = slot.stack();
            if (stack != null && !stack.isEmpty()) {
               String itemId = this.itemKey(stack);

               for (MacroItemTarget itemTarget : targets) {
                  if (this.matchesTarget(itemTarget, stack, slot.visibleSlot())) {
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

   private boolean matchesTarget(MacroItemTarget itemTarget, ItemStack stack, int visibleSlot) {
      if (itemTarget == null) {
         return false;
      } else if (itemTarget.hasItemFilter()) {
         MacroItemTarget identityTarget = itemTarget.copy();
         identityTarget.slot = -1;
         return identityTarget.matches(stack, visibleSlot);
      } else {
         return itemTarget.matches(stack, visibleSlot);
      }
   }

   private boolean depositPlayerTargets(Minecraft mc, Map<String, Integer> targetItemsToTransfer) throws InterruptedException {
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
                           if (!MacroExecutor.isCurrentRunActive()) {
                              return;
                           }

                           Slot slot = handler.slots.get(i);
                           if (slot != null && autismclient.util.InventorySlotHelper.isPlayerInventorySlot(mc, slot)) {
                              ItemStack stack = slot.getItem();
                              if (stack != null && !stack.isEmpty()) {
                                 int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, slot.index);
                                 if (this.isPlayerSlot(visibleSlot)
                                    && targetItemsToTransfer.containsKey(this.itemKey(stack))
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
         this.awaitLatch(latch, 3000L);
         return result[0];
      } else {
         return false;
      }
   }

   private boolean matchesAnyTarget(ItemStack stack, int visibleSlot) {
      List<MacroItemTarget> targets = this.runtimeTargets();
      if (targets.isEmpty()) {
         return false;
      }

      for (MacroItemTarget itemTarget : targets) {
         if (this.matchesTarget(itemTarget, stack, visibleSlot)) {
            return true;
         }
      }

      return false;
   }

   private boolean withdrawContainerTargets(Minecraft mc, Map<String, Integer> targetItemsToExtract) throws InterruptedException {
      if (targetItemsToExtract != null && !targetItemsToExtract.isEmpty()) {
         boolean[] result = new boolean[]{false};
         CountDownLatch latch = new CountDownLatch(1);
         mc.execute(
            () -> {
               try {
                  if (mc.player != null && mc.player.containerMenu != null && mc.gameMode != null) {
                     AbstractContainerMenu handler = mc.player.containerMenu;

                     for (int i = 0; i < handler.slots.size(); i++) {
                        if (!MacroExecutor.isCurrentRunActive()) {
                           return;
                        }

                        Slot slot = handler.slots.get(i);
                        if (slot != null && !autismclient.util.InventorySlotHelper.isPlayerInventorySlot(mc, slot)) {
                           ItemStack stack = slot.getItem();
                           if (stack != null && !stack.isEmpty()) {
                              int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, slot.index);
                              if (this.isTargetStack(stack, visibleSlot)
                                 && targetItemsToExtract.containsKey(this.itemKey(stack))
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
         this.awaitLatch(latch, 3000L);
         return result[0];
      } else {
         return false;
      }
   }

   private InventoryAuditAction.Inner_CachePool snapshotOpenContainer(Minecraft mc) throws InterruptedException {
      CompletableFuture<InventoryAuditAction.Inner_CachePool> future = new CompletableFuture<>();
      mc.execute(() -> {
         try {
            if (mc.player == null || mc.player.containerMenu == null) {
               future.complete(null);
               return;
            }

            AbstractContainerMenu handler = mc.player.containerMenu;
            future.complete(this.snapshotMenu(mc, handler));
         } catch (Exception e) {
            future.complete(null);
         }
      });
      return this.join(future, 3000L);
   }

   private InventoryAuditAction.Inner_CachePool snapshotMenu(Minecraft mc, AbstractContainerMenu handler) {
      String title = mc.gui.screen() != null && mc.gui.screen().getTitle() != null ? mc.gui.screen().getTitle().getString() : "";
      Map<Integer, InventoryAuditAction.Inner_TaskQueue> slots = this.menuSlotsByVisibleIndex(mc, handler);
      return new InventoryAuditAction.Inner_CachePool(title, System.currentTimeMillis(), slots);
   }

   private Map<Integer, InventoryAuditAction.Inner_TaskQueue> menuSlotsByVisibleIndex(Minecraft mc, AbstractContainerMenu handler) {
      Map<Integer, InventoryAuditAction.Inner_TaskQueue> slots = new LinkedHashMap<>();

      for (Slot slot : handler.slots) {
         if (slot != null) {
            int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, slot.index);
            slots.putIfAbsent(
               visibleSlot, new InventoryAuditAction.Inner_TaskQueue(visibleSlot, this.slotLabel(visibleSlot), this.copyOrEmpty(slot.getItem()))
            );
         }
      }

      return slots;
   }

   private ItemStack copyOrEmpty(ItemStack stack) {
      return stack != null && !stack.isEmpty() ? stack.copy() : ItemStack.EMPTY;
   }

   private String slotLabel(int visibleSlot) {
      return "Slot " + visibleSlot;
   }

   private String itemKey(ItemStack stack) {
      String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
      return itemId.startsWith("minecraft:") ? itemId.substring("minecraft:".length()) : itemId;
   }

   private void checkInterrupted() throws InterruptedException {
      if (Thread.currentThread().isInterrupted() || !MacroExecutor.isCurrentRunActive()) {
         throw new InterruptedException("Macro stopped");
      }
   }

   private void sleep(long millis) throws InterruptedException {
      long remaining = Math.max(0L, millis);

      while (remaining > 0L) {
         this.checkInterrupted();
         long slice = Math.min(remaining, 25L);
         Thread.sleep(slice);
         remaining -= slice;
      }
   }

   private boolean awaitLatch(CountDownLatch latch, long timeoutMs) throws InterruptedException {
      long deadline = System.currentTimeMillis() + Math.max(0L, timeoutMs);

      while (latch.getCount() > 0L) {
         this.checkInterrupted();
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

   private boolean awaitFuture(CompletableFuture<?> future, long timeoutMs) throws InterruptedException {
      long deadline = System.currentTimeMillis() + Math.max(0L, timeoutMs);

      while (true) {
         this.checkInterrupted();
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

   private <T> T join(CompletableFuture<T> future, long timeoutMs) throws InterruptedException {
      long deadline = System.currentTimeMillis() + Math.max(0L, timeoutMs);

      while (true) {
         this.checkInterrupted();
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

   private int clampDelay(int value) {
      return Math.max(0, Math.min(value, 10000));
   }

   private int load(int value) {
      return Math.max(1, Math.min(value, 100));
   }

   private String openCommandOrDefault() {
      String raw = this.openCommand == null ? "" : this.openCommand.trim();
      return raw.isEmpty() ? "/ec" : raw;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("mode", this.mode.name());
      tag.put("targetItems", MacroItemTarget.toListTag(this.configuredTargets()));
      tag.putString("openCommand", this.openCommandOrDefault());
      tag.putString("openMode", this.openMode.name());
      tag.putInt("containerX", this.containerPos.getX());
      tag.putInt("containerY", this.containerPos.getY());
      tag.putInt("containerZ", this.containerPos.getZ());
      tag.putString("dupeVector", this.dupeVector.name());
      tag.putInt("delayBeforeReopen", this.clampDelay(this.delayBeforeReopen));
      tag.putInt("delayAfterReopen", this.clampDelay(this.delayAfterReopen));
      tag.putInt("iterations", this.load(this.iterations));
      tag.putInt("maxTransferAttempts", Math.max(1, Math.min(this.maxTransferAttempts, 20)));
      tag.putInt("transferRetryDelayMs", Math.max(10, Math.min(this.transferRetryDelayMs, 500)));
      tag.putBoolean("multipleStacks", this.multipleStacks);
      tag.putInt("spamCount", Math.max(1, Math.min(this.spamCount, 20)));
      tag.putInt("spamDelayMs", Math.max(10, Math.min(this.spamDelayMs, 1000)));
      tag.putInt("grabPreDelayMs", 0);
      tag.putBoolean("closeAfterGrab", true);
      tag.putBoolean("enabled", this.enabled);
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
         this.itemTargets.addAll(MacroItemTarget.fromListTag(list));
      }

      this.rebuildTargetItems();
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

      this.delayBeforeReopen = this.clampDelay(this.delayBeforeReopen);
      this.delayAfterReopen = this.clampDelay(this.delayAfterReopen);
      this.iterations = this.load(this.iterations);
      this.openCommand = this.openCommandOrDefault();
      if (tag.contains("enabled")) {
         this.enabled = tag.getBooleanOr("enabled", true);
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
         : this.openCommandOrDefault();
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
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   private List<MacroItemTarget> configuredTargets() {
      if (!this.itemTargets.isEmpty()) {
         return this.itemTargets;
      }

      for (String target : this.targetItems) {
         MacroItemTarget parsed = MacroItemTarget.parse(target);
         if (parsed.hasSlot() || parsed.hasItemFilter()) {
            this.itemTargets.add(parsed);
         }
      }

      return this.itemTargets;
   }

   private List<MacroItemTarget> runtimeTargets() {
      List<MacroItemTarget> runtime = new ArrayList<>();
      Minecraft mc = Minecraft.getInstance();

      for (MacroItemTarget target : this.configuredTargets()) {
         if (target != null) {
            MacroItemTarget resolved = target.resolveTemplates(mc);
            if (resolved != null && (resolved.hasSlot() || resolved.hasItemFilter())) {
               runtime.add(resolved);
            }
         }
      }

      return runtime;
   }

   private void rebuildTargetItems() {
      this.targetItems.clear();

      for (MacroItemTarget target : this.itemTargets) {
         if (target != null) {
            String entry = target.toSpecString();
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

   private record Inner_CachePool(String title, long capturedAtMillis, Map<Integer, InventoryAuditAction.Inner_TaskQueue> slots) {
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

   private record Inner_NetHelper(int slotCount, Map<String, Integer> items) {
   }

   private record Inner_StreamUtil(boolean open, int containerId, int load, int read, int write) {
   }

   private record Inner_TaskQueue(int visibleSlot, String label, ItemStack stack) {
   }
}
