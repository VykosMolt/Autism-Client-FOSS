package autismclient.util.macro;

import autismclient.api.macro.AddonCondition;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class MacroWaitRegistry {
   private static final List<MacroWaitRegistry.WaitEntry> PENDING_WAITS = Collections.synchronizedList(new ArrayList<>());
   private static final AtomicLong INVENTORY_REVISION = new AtomicLong();
   private static volatile long lastWorldChangeNanos = 0L;
   private static volatile long unusedLastWriteNanos = 0L;

   public static long getInventoryRevision() {
      return INVENTORY_REVISION.get();
   }

   public static int pendingCount() {
      return PENDING_WAITS.size();
   }

   public static void bumpInventoryRevision() {
      INVENTORY_REVISION.incrementAndGet();
   }

   public static CompletableFuture<Void> awaitGuiTitle(String guiTitle) {
      Minecraft mc = Minecraft.getInstance();
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroWaitRegistry.WaitGuiTitleEntry condition = new MacroWaitRegistry.WaitGuiTitleEntry(guiTitle, mc.gui.screen(), future);
      PENDING_WAITS.add(condition);
      return future;
   }

   public static CompletableFuture<Void> waitForGuiClose(String guiTitle) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      PENDING_WAITS.add(new MacroWaitRegistry.WaitGuiCloseEntry(guiTitle, future));
      return future;
   }

   public static CompletableFuture<Void> awaitScreenChange(Screen prevScreen) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      PENDING_WAITS.add(new MacroWaitRegistry.WaitScreenChangeEntry(prevScreen, future));
      return future;
   }

   public static CompletableFuture<Void> waitForItemName(String itemName) {
      return waitForItem(MacroItemTarget.parse(itemName));
   }

   public static CompletableFuture<Void> waitForItem(MacroItemTarget target) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroWaitRegistry.WaitInventoryItemEntry condition = new MacroWaitRegistry.WaitInventoryItemEntry(target, future);
      PENDING_WAITS.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (evaluateOnClientThread(mc, condition)) {
         condition.completeWait();
         PENDING_WAITS.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> awaitHealth(float threshold, boolean below) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroWaitRegistry.WaitHealthEntry condition = new MacroWaitRegistry.WaitHealthEntry(threshold, below, future);
      PENDING_WAITS.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (evaluateOnClientThread(mc, condition)) {
         condition.completeWait();
         PENDING_WAITS.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> waitForBlock(WaitForBlockAction action) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroWaitRegistry.WaitBlockEntry condition = new MacroWaitRegistry.WaitBlockEntry(action, future);
      PENDING_WAITS.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (evaluateOnClientThread(mc, condition)) {
         condition.completeWait();
         PENDING_WAITS.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> awaitCooldownByItemName(String itemName, boolean checkMainHand) {
      return awaitCooldown(MacroItemTarget.parse(itemName), checkMainHand);
   }

   public static CompletableFuture<Void> awaitCooldown(MacroItemTarget target, boolean checkMainHand) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroWaitRegistry.WaitCooldownEntry condition = new MacroWaitRegistry.WaitCooldownEntry(target, checkMainHand, future);
      PENDING_WAITS.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (evaluateOnClientThread(mc, condition)) {
         condition.completeWait();
         PENDING_WAITS.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> waitForPosition(double x, double y, double z, double leeway, boolean checkRotation, float yaw, float pitch, float rotLeeway) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroWaitRegistry.WaitPosEntry condition = new MacroWaitRegistry.WaitPosEntry(x, y, z, leeway, checkRotation, yaw, pitch, rotLeeway, future);
      PENDING_WAITS.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (evaluateOnClientThread(mc, condition)) {
         condition.completeWait();
         PENDING_WAITS.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> awaitAnyWorldChange() {
      return waitForWorldChange(new WaitForWorldChangeAction());
   }

   public static CompletableFuture<Void> waitForWorldChange(WaitForWorldChangeAction action) {
      Minecraft mc = Minecraft.getInstance();
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroWaitRegistry.WaitWorldChangeEntry condition = new MacroWaitRegistry.WaitWorldChangeEntry(currentDimensionId(mc), action == null ? "" : action.targetDimension, future);
      PENDING_WAITS.add(condition);
      return future;
   }

   public static CompletableFuture<Void> waitForGamemode(WaitGamemodeChangeAction action) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroWaitRegistry.WaitGamemodeEntry condition = new MacroWaitRegistry.WaitGamemodeEntry(action, autismclient.util.FakeGamemodeManager.snapshot(), future);
      PENDING_WAITS.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (evaluateOnClientThread(mc, condition)) {
         condition.completeWait();
         PENDING_WAITS.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> awaitPositionDelta(double distance, boolean horizontalOnly) {
      Minecraft mc = Minecraft.getInstance();
      Vec3 origin = mc.player == null ? Vec3.ZERO : mc.player.position();
      return awaitPositionDeltaFrom(origin, distance, horizontalOnly);
   }

   public static CompletableFuture<Void> awaitPositionDeltaFrom(Vec3 origin, double distance, boolean horizontalOnly) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroWaitRegistry.WaitPositionDeltaEntry condition = new MacroWaitRegistry.WaitPositionDeltaEntry(origin, distance, horizontalOnly, future);
      PENDING_WAITS.add(condition);
      return future;
   }

   public static CompletableFuture<Void> awaitTickSync() {
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroWaitRegistry.WaitTickSyncEntry condition = new MacroWaitRegistry.WaitTickSyncEntry(future);
      PENDING_WAITS.add(condition);
      return future;
   }

   public static void onInventoryUpdate(Minecraft mc) {
      if (!PENDING_WAITS.isEmpty()) {
         Screen current = mc.gui.screen();
         List<MacroWaitRegistry.WaitEntry> toRemove = new ArrayList<>();
         synchronized (PENDING_WAITS) {
            for (MacroWaitRegistry.WaitEntry cond : PENDING_WAITS) {
               if (cond.isWaitCancelled()) {
                  toRemove.add(cond);
               } else if (cond instanceof MacroWaitRegistry.WaitGuiTitleEntry gc) {
                  if (current != null && gc.matchesScreen(current)) {
                     gc.completeWait();
                     toRemove.add(gc);
                  }
               } else if (cond instanceof MacroWaitRegistry.WaitContainerItemEntry hic) {
                  if (hic.isWaitSatisfied(mc)) {
                     hic.completeWait();
                     toRemove.add(hic);
                  }
               } else if (cond instanceof MacroWaitRegistry.WaitInventoryItemEntry ic) {
                  if (ic.isWaitSatisfied(mc)) {
                     ic.completeWait();
                     toRemove.add(ic);
                  }
               } else if (cond instanceof MacroWaitRegistry.WaitSlotChangeEntry scc && scc.isWaitSatisfied(mc)) {
                  scc.completeWait();
                  toRemove.add(scc);
               }
            }

            PENDING_WAITS.removeAll(toRemove);
         }
      }
   }

   public static void onSlotUpdate(int slot) {
      if (!PENDING_WAITS.isEmpty()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player != null) {
            List<MacroWaitRegistry.WaitEntry> toRemove = new ArrayList<>();
            synchronized (PENDING_WAITS) {
               for (MacroWaitRegistry.WaitEntry cond : PENDING_WAITS) {
                  if (cond.isWaitCancelled()) {
                     toRemove.add(cond);
                  } else if (cond instanceof MacroWaitRegistry.WaitSlotChangeEntry scc) {
                     if (scc.isWaitSatisfied(mc)) {
                        scc.completeWait();
                        toRemove.add(scc);
                     }
                  } else if (cond instanceof MacroWaitRegistry.WaitContainerItemEntry hic) {
                     if (hic.isWaitSatisfied(mc)) {
                        hic.completeWait();
                        toRemove.add(hic);
                     }
                  } else if (cond instanceof MacroWaitRegistry.WaitInventoryItemEntry ic && ic.isWaitSatisfied(mc)) {
                     ic.completeWait();
                     toRemove.add(ic);
                  }
               }

               PENDING_WAITS.removeAll(toRemove);
            }
         }
      }
   }

   public static void tick(Minecraft mc) {
      if (!PENDING_WAITS.isEmpty()) {
         List<MacroWaitRegistry.WaitEntry> toRemove = new ArrayList<>();
         synchronized (PENDING_WAITS) {
            for (MacroWaitRegistry.WaitEntry cond : PENDING_WAITS) {
               if (cond.isWaitCancelled()) {
                  toRemove.add(cond);
               } else {
                  try {
                     if (cond.isWaitSatisfied(mc)) {
                        cond.completeWait();
                        toRemove.add(cond);
                     }
                  } catch (Throwable t) {
                     cond.cancelWait();
                     toRemove.add(cond);
                  }
               }
            }

            PENDING_WAITS.removeAll(toRemove);
         }
      }
   }

   public static void checkScreenWaits(Screen screen) {
      if (!PENDING_WAITS.isEmpty()) {
         List<MacroWaitRegistry.WaitEntry> toRemove = new ArrayList<>();
         synchronized (PENDING_WAITS) {
            for (MacroWaitRegistry.WaitEntry cond : PENDING_WAITS) {
               if (cond instanceof MacroWaitRegistry.WaitGuiTitleEntry gc) {
                  if (gc.matchesScreen(screen)) {
                     gc.completeWait();
                     toRemove.add(gc);
                  }
               } else if (cond instanceof MacroWaitRegistry.WaitContainerItemEntry hic) {
                  Minecraft mc = Minecraft.getInstance();
                  if (hic.isWaitSatisfied(mc)) {
                     hic.completeWait();
                     toRemove.add(hic);
                  }
               } else if (cond instanceof MacroWaitRegistry.WaitSlotChangeEntry scc) {
                  Minecraft mc = Minecraft.getInstance();
                  if (scc.isWaitSatisfied(mc)) {
                     scc.completeWait();
                     toRemove.add(scc);
                  }
               }
            }

            PENDING_WAITS.removeAll(toRemove);
         }
      }
   }

   private static boolean evaluateOnClientThread(Minecraft mc, MacroWaitRegistry.WaitEntry condition) {
      if (mc != null && condition != null) {
         try {
            if (mc.isSameThread()) {
               return condition.isWaitSatisfied(mc);
            }

            CompletableFuture<Boolean> result = new CompletableFuture<>();
            mc.execute(() -> {
               try {
                  result.complete(condition.isWaitSatisfied(mc));
               } catch (Throwable t) {
                  result.complete(false);
               }
            });
            return Boolean.TRUE.equals(result.get(250L, TimeUnit.MILLISECONDS));
         } catch (Exception ignored) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static void runOnClientThreadBlocking(Minecraft mc, Runnable task) {
      if (mc != null && task != null) {
         try {
            if (mc.isSameThread()) {
               task.run();
               return;
            }

            CompletableFuture<Void> result = new CompletableFuture<>();
            mc.execute(() -> {
               try {
                  task.run();
               } finally {
                  result.complete(null);
               }
            });
            result.get(250L, TimeUnit.MILLISECONDS);
         } catch (Exception var3) {
         }
      }
   }

   public static void cancelAll() {
      synchronized (PENDING_WAITS) {
         for (MacroWaitRegistry.WaitEntry cond : PENDING_WAITS) {
            cond.cancelWait();
         }

         PENDING_WAITS.clear();
      }
   }

   public static boolean hasPendingWaits() {
      return !PENDING_WAITS.isEmpty();
   }

   public static boolean hasInventoryWaits() {
      if (PENDING_WAITS.isEmpty()) {
         return false;
      }

      synchronized (PENDING_WAITS) {
         for (MacroWaitRegistry.WaitEntry cond : PENDING_WAITS) {
            if (cond instanceof MacroWaitRegistry.WaitGuiTitleEntry
               || cond instanceof MacroWaitRegistry.WaitContainerItemEntry
               || cond instanceof MacroWaitRegistry.WaitInventoryItemEntry
               || cond instanceof MacroWaitRegistry.WaitSlotChangeEntry) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean hasSoundWaits() {
      if (PENDING_WAITS.isEmpty()) {
         return false;
      }

      synchronized (PENDING_WAITS) {
         for (MacroWaitRegistry.WaitEntry cond : PENDING_WAITS) {
            if (cond instanceof MacroWaitRegistry.WaitSoundEntry) {
               return true;
            }
         }

         return false;
      }
   }

   public static CompletableFuture<Void> waitForAddonCondition(AddonCondition condition) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      if (condition == null) {
         future.complete(null);
         return future;
      }

      MacroWaitRegistry.WaitAddonConditionEntry adapter = new MacroWaitRegistry.WaitAddonConditionEntry(condition, future);
      PENDING_WAITS.add(adapter);
      Minecraft mc = Minecraft.getInstance();
      if (evaluateOnClientThread(mc, adapter)) {
         adapter.completeWait();
         PENDING_WAITS.remove(adapter);
      }

      return future;
   }

   public static void markWorldChange() {
      lastWorldChangeNanos = System.nanoTime();
   }

   public static void markWrite() {
      unusedLastWriteNanos = System.nanoTime();
   }

   private static String currentDimensionId(Minecraft mc) {
      try {
         return mc != null && mc.level != null && mc.level.dimension() != null ? mc.level.dimension().identifier().toString() : "";
      } catch (Throwable ignored) {
         return "";
      }
   }

   private static boolean dimensionMatches(String current, String target) {
      return target != null && !target.isBlank() ? normalizeDimensionName(current).equals(normalizeDimensionName(target)) : true;
   }

   private static String normalizeDimensionName(String raw) {
      if (raw == null) {
         return "";
      } else {
         String value = raw.trim().toLowerCase(Locale.ROOT);
         if (value.isEmpty()) {
            return "";
         } else if (value.equals("nether") || value.equals("the_nether")) {
            return "minecraft:the_nether";
         } else if (value.equals("overworld") || value.equals("world")) {
            return "minecraft:overworld";
         } else if (!value.equals("end") && !value.equals("the_end")) {
            return !value.contains(":") ? "minecraft:" + value : value;
         } else {
            return "minecraft:the_end";
         }
      }
   }

   public static CompletableFuture<Void> awaitServerTickSync(int bufferMs, int maxWaitMs, boolean ignorePing) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroWaitRegistry.WaitServerTickSyncEntry condition = new MacroWaitRegistry.WaitServerTickSyncEntry(future, bufferMs, maxWaitMs, ignorePing);
      PENDING_WAITS.add(condition);
      return future;
   }

   public static CompletableFuture<Void> waitForEntity(WaitForEntityAction action) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroWaitRegistry.WaitEntityEntry condition = new MacroWaitRegistry.WaitEntityEntry(action, future);
      PENDING_WAITS.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (condition.isWaitSatisfied(mc)) {
         condition.completeWait();
         PENDING_WAITS.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> waitForContainerItem(String itemName) {
      return itemWait(MacroItemTarget.parse(itemName));
   }

   public static CompletableFuture<Void> itemWait(MacroItemTarget target) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroWaitRegistry.WaitContainerItemEntry condition = new MacroWaitRegistry.WaitContainerItemEntry(target, future);
      PENDING_WAITS.add(condition);
      Minecraft mc = Minecraft.getInstance();
      if (evaluateOnClientThread(mc, condition)) {
         condition.completeWait();
         PENDING_WAITS.remove(condition);
      }

      return future;
   }

   public static CompletableFuture<Void> awaitSlotNumberChange(int slotNumber) {
      return waitForSlotChange(new WaitForSlotChangeAction(slotNumber));
   }

   public static CompletableFuture<Void> waitForSound(WaitForSoundAction action) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      PENDING_WAITS.add(new MacroWaitRegistry.WaitSoundEntry(action, future));
      return future;
   }

   public static void onSound(String soundId, double x, double y, double z) {
      if (!PENDING_WAITS.isEmpty()) {
         Minecraft mc = Minecraft.getInstance();
         List<MacroWaitRegistry.WaitEntry> toRemove = new ArrayList<>();
         synchronized (PENDING_WAITS) {
            for (MacroWaitRegistry.WaitEntry cond : PENDING_WAITS) {
               if (cond.isWaitCancelled()) {
                  toRemove.add(cond);
               } else if (cond instanceof MacroWaitRegistry.WaitSoundEntry sc && sc.matchesSound(mc, soundId, x, y, z)) {
                  sc.action.recordMatch(soundId, x, y, z);
                  sc.completeWait();
                  toRemove.add(sc);
               }
            }

            PENDING_WAITS.removeAll(toRemove);
         }
      }
   }

   public static CompletableFuture<Void> waitForSlotChange(WaitForSlotChangeAction action) {
      Minecraft mc = Minecraft.getInstance();
      Map<Integer, String> initialNames = new HashMap<>();
      Map<Integer, Integer> initialCounts = new HashMap<>();
      runOnClientThreadBlocking(mc, () -> {
         if (mc.player != null) {
            AbstractContainerMenu handler = mc.player.containerMenu;

            for (int i = 0; i < action.entries.size(); i++) {
               WaitForSlotChangeAction.Inner_DataUtil e = action.entries.get(i);
               MacroItemTarget entryTarget = e.resolveItemTarget();
               if (e.mode == WaitForSlotChangeAction.Inner_StreamUtil.ANY_CHANGE && entryTarget != null && entryTarget.hasSlot()) {
                  int slotNum = entryTarget.slot;
                  int handlerSlot = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, slotNum);
                  if (handler != null && handlerSlot >= 0 && handlerSlot < handler.slots.size()) {
                     ItemStack s = handler.slots.get(handlerSlot).getItem();
                     initialNames.put(i, describeStack(s));
                     initialCounts.put(i, s.isEmpty() ? 0 : s.getCount());
                  }
               }
            }
         }
      });
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroWaitRegistry.WaitSlotChangeEntry condition = new MacroWaitRegistry.WaitSlotChangeEntry(action, initialNames, initialCounts, future);
      PENDING_WAITS.add(condition);
      boolean hasAnyChange = action.entries.stream().anyMatch(e -> e.mode == WaitForSlotChangeAction.Inner_StreamUtil.ANY_CHANGE);
      if (!hasAnyChange && evaluateOnClientThread(mc, condition)) {
         condition.completeWait();
         PENDING_WAITS.remove(condition);
      }

      return future;
   }

   private static String describeStack(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
         String rich = MacroExecutor.serializeComponentJson(stack.getHoverName());
         String components = String.valueOf(stack.getComponents());
         return id + "|" + stack.getCount() + "|" + (rich == null ? "" : rich) + "|" + components.hashCode();
      } else {
         return "";
      }
   }

   public static CompletableFuture<Void> waitForLanStep(WaitForLanStepAction action) {
      CompletableFuture<Void> future = new CompletableFuture<>();
      MacroWaitRegistry.WaitLanStepEntry cond = new MacroWaitRegistry.WaitLanStepEntry(action, future);
      if (cond.isLanStepReached()) {
         future.complete(null);
      } else {
         PENDING_WAITS.add(cond);
      }

      return future;
   }

   public static void update() {
      if (!PENDING_WAITS.isEmpty()) {
         List<MacroWaitRegistry.WaitEntry> toRemove = new ArrayList<>();
         synchronized (PENDING_WAITS) {
            for (MacroWaitRegistry.WaitEntry cond : PENDING_WAITS) {
               if (cond.isWaitCancelled()) {
                  toRemove.add(cond);
               } else if (cond instanceof MacroWaitRegistry.WaitLanStepEntry lsc && lsc.isLanStepReached()) {
                  lsc.completeWait();
                  toRemove.add(lsc);
               }
            }

            PENDING_WAITS.removeAll(toRemove);
         }
      }
   }

   private static boolean titleMatchesSearch(String searchLower, String[] searchWords, String target) {
      String normalizedTarget = MacroExecutor.cleanVisibleChatText(target);
      if (!searchLower.isEmpty() && !normalizedTarget.isEmpty()) {
         String tl = normalizedTarget.toLowerCase();
         if (searchLower.equals(tl)) {
            return true;
         }

         if (tl.contains(searchLower)) {
            return true;
         }

         if (searchWords.length == 0) {
            return false;
         }

         String[] targetWords = tl.split("\\s+");

         for (String word : searchWords) {
            boolean found = false;

            for (String tWord : targetWords) {
               if (tWord.contains(word) || word.contains(tWord)) {
                  found = true;
                  break;
               }
            }

            if (!found) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean titleMatches(String search, String target) {
      if (!search.isEmpty() && !target.isEmpty()) {
         if (search.equals(target)) {
            return true;
         }

         if (target.toLowerCase().contains(search.toLowerCase())) {
            return true;
         }

         String[] searchWords = search.toLowerCase().split("\\s+");
         String[] targetWords = target.toLowerCase().split("\\s+");

         for (String word : searchWords) {
            boolean found = false;

            for (String tWord : targetWords) {
               if (tWord.contains(word) || word.contains(tWord)) {
                  found = true;
                  break;
               }
            }

            if (!found) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   static class WaitEntityEntry implements MacroWaitRegistry.WaitEntry {
      final WaitForEntityAction action;
      final Set<String> entityIds;
      final Set<String> load;
      final double radiusSq;
      final CompletableFuture<Void> future;

      WaitEntityEntry(WaitForEntityAction action, CompletableFuture<Void> future) {
         this.action = action;
         this.entityIds = new HashSet<>();
         this.load = new HashSet<>();

         for (String entry : action.entityIds) {
            if (entry.startsWith("~")) {
               String[] p = entry.split("~", 4);
               if (p.length >= 2 && !p[1].isEmpty()) {
                  this.load.add(p[1]);
               }
            } else {
               this.entityIds.add(entry);
            }
         }

         this.radiusSq = action.radius * action.radius;
         this.future = future;
      }

      private boolean matchesEntity(Entity entity) {
         if (this.action.containerEntitiesOnly) {
            String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            if (!typeId.contains("boat") && !typeId.contains("minecart") && !typeId.contains("llama") && !typeId.contains("chest")) {
               return false;
            }
         }

         if (this.entityIds.isEmpty() && this.load.isEmpty()) {
            return true;
         }

         if (!this.entityIds.isEmpty()) {
            String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            if (this.entityIds.contains(typeId)) {
               return true;
            }
         }

         return !this.load.isEmpty() && this.load.contains(entity.getStringUUID());
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         if (mc.level != null && mc.player != null) {
            switch (this.action.checkMode) {
               case LOOKING_AT:
                  Entity targeted = mc.crosshairPickEntity;
                  if (targeted != null && targeted != mc.player) {
                     return this.matchesEntity(targeted);
                  }

                  return false;
               case MOUNTED_IN:
                  Entity vehicle = mc.player.getVehicle();
                  return vehicle != null && this.matchesEntity(vehicle);
               case WITHIN_REACH:
                  double reachSq = mc.player.blockInteractionRange() * mc.player.blockInteractionRange();

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity != mc.player && !(entity.distanceToSqr(mc.player) > reachSq) && this.matchesEntity(entity)) {
                        return true;
                     }
                  }

                  return false;
               case NEARBY:
               default:
                  Vec3 center = this.action.centerOnPlayer
                     ? new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ())
                     : new Vec3(this.action.f_x_D, this.action.f_y_D, this.action.f_z_D);

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity != mc.player && !(entity.distanceToSqr(center) > this.radiusSq) && this.matchesEntity(entity)) {
                        return true;
                     }
                  }

                  return false;
            }
         } else {
            return false;
         }
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.future.isCancelled();
      }
   }

   static class WaitSlotChangeEntry implements MacroWaitRegistry.WaitEntry {
      final WaitForSlotChangeAction action;
      final Map<Integer, String> initialNames;
      final Map<Integer, Integer> baselineCounts;
      final CompletableFuture<Void> future;

      WaitSlotChangeEntry(WaitForSlotChangeAction action, Map<Integer, String> initialNames, Map<Integer, Integer> initialCounts, CompletableFuture<Void> future) {
         this.action = action;
         this.initialNames = initialNames;
         this.baselineCounts = initialCounts;
         this.future = future;
      }

      WaitSlotChangeEntry(int slotNumber, String initialItemName, int initialCount, CompletableFuture<Void> future) {
         WaitForSlotChangeAction a = new WaitForSlotChangeAction(slotNumber);
         this.action = a;
         this.initialNames = new HashMap<>();
         this.baselineCounts = new HashMap<>();
         if (slotNumber >= 0) {
            this.initialNames.put(0, initialItemName);
            this.baselineCounts.put(0, initialCount);
         }

         this.future = future;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         if (mc.player == null) {
            return false;
         }

         if (this.action.entries.isEmpty()) {
            return true;
         }

         AbstractContainerMenu handler = mc.player.containerMenu;

         for (int i = 0; i < this.action.entries.size(); i++) {
            if (!this.entrySatisfied(mc, handler, this.action.entries.get(i), i)) {
               return false;
            }
         }

         return true;
      }

      private boolean entrySatisfied(Minecraft mc, AbstractContainerMenu handler, WaitForSlotChangeAction.Inner_DataUtil e, int entryIdx) {
         MacroItemTarget entryTarget = e.resolveItemTarget();
         if (entryTarget == null) {
            entryTarget = new MacroItemTarget();
         }

         if (entryTarget.hasSlot()) {
            int slotNum = entryTarget.slot;
            if (handler == null) {
               return e.mode == WaitForSlotChangeAction.Inner_StreamUtil.IS_EMPTY;
            } else {
               int handlerSlot = autismclient.util.InventorySlotHelper.resolveConfiguredSlot(mc, slotNum);
               if (handlerSlot >= 0 && handlerSlot < handler.slots.size()) {
                  ItemStack stack = handler.slots.get(handlerSlot).getItem();
                  return this.stackSatisfies(stack, entryTarget, slotNum, e, entryIdx);
               } else {
                  return e.mode == WaitForSlotChangeAction.Inner_StreamUtil.IS_EMPTY;
               }
            }
         } else if (e.mode == WaitForSlotChangeAction.Inner_StreamUtil.IS_EMPTY) {
            if (handler != null) {
               for (Slot s : handler.slots) {
                  if (s != null && !s.getItem().isEmpty()) {
                     int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, s.index);
                     if (!entryTarget.hasItemFilter() || entryTarget.matches(s.getItem(), visibleSlot)) {
                        return false;
                     }
                  }
               }
            } else {
               for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                  ItemStack stack = mc.player.getInventory().getItem(i);
                  if (!stack.isEmpty() && (!entryTarget.hasItemFilter() || entryTarget.matches(stack, i))) {
                     return false;
                  }
               }
            }

            return true;
         } else {
            if (handler != null) {
               for (Slot s : handler.slots) {
                  if (s != null && !s.getItem().isEmpty()) {
                     ItemStack stack = s.getItem();
                     int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, s.index);
                     if ((!entryTarget.hasItemFilter() || entryTarget.matches(stack, visibleSlot))
                        && this.stackSatisfies(stack, entryTarget, visibleSlot, e, entryIdx)) {
                        return true;
                     }
                  }
               }
            } else {
               for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                  ItemStack stack = mc.player.getInventory().getItem(i);
                  if (!stack.isEmpty() && (!entryTarget.hasItemFilter() || entryTarget.matches(stack, i)) && this.stackSatisfies(stack, entryTarget, i, e, entryIdx)) {
                     return true;
                  }
               }
            }

            return false;
         }
      }

      private boolean stackSatisfies(ItemStack stack, MacroItemTarget target, int visibleSlot, WaitForSlotChangeAction.Inner_DataUtil e, int entryIdx) {
         boolean nameMatches = target == null || !target.hasItemFilter() || !stack.isEmpty() && target.matches(stack, visibleSlot);
         boolean var10000;
         switch (e.mode) {
            case NOT_EMPTY:
               var10000 = !stack.isEmpty() && nameMatches;
               break;
            case IS_EMPTY:
               var10000 = stack.isEmpty();
               break;
            case COUNT_AT_LEAST:
               var10000 = !stack.isEmpty() && nameMatches && stack.getCount() >= e.count;
               break;
            case COUNT_BELOW:
               var10000 = stack.isEmpty() || nameMatches && stack.getCount() < e.count;
               break;
            case ANY_CHANGE:
               String initName = this.initialNames.getOrDefault(entryIdx, "");
               int initCount = this.baselineCounts.getOrDefault(entryIdx, 0);
               String curName = MacroWaitRegistry.describeStack(stack);
               int curCount = stack.isEmpty() ? 0 : stack.getCount();
               var10000 = !curName.equals(initName) || curCount != initCount;
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.future.isCancelled();
      }
   }

   static class WaitCooldownEntry implements MacroWaitRegistry.WaitEntry {
      final MacroItemTarget target;
      final boolean checkMainHand;
      final CompletableFuture<Void> future;

      WaitCooldownEntry(MacroItemTarget target, boolean checkMainHand, CompletableFuture<Void> future) {
         this.target = target == null ? new MacroItemTarget() : target.copy();
         this.checkMainHand = checkMainHand;
         this.future = future;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         if (mc.player == null) {
            return false;
         }

         if (this.target.hasItemFilter()) {
            boolean foundAny = false;

            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
               ItemStack stack = mc.player.getInventory().getItem(i);
               if (!stack.isEmpty() && this.target.matches(stack, i)) {
                  foundAny = true;
                  if (mc.player.getCooldowns().isOnCooldown(stack)) {
                     return false;
                  }
               }
            }

            return foundAny;
         } else {
            ItemStack stack = this.checkMainHand ? mc.player.getMainHandItem() : mc.player.getOffhandItem();
            return stack.isEmpty() ? true : !mc.player.getCooldowns().isOnCooldown(stack);
         }
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.future.isCancelled();
      }
   }

   static class WaitGamemodeEntry implements MacroWaitRegistry.WaitEntry {
      final WaitGamemodeChangeAction action;
      final autismclient.util.FakeGamemodeManager.GamemodeSnapshot baseline;
      final CompletableFuture<Void> future;
      private volatile boolean cancelled = false;

      WaitGamemodeEntry(WaitGamemodeChangeAction action, autismclient.util.FakeGamemodeManager.GamemodeSnapshot baseline, CompletableFuture<Void> future) {
         this.action = action == null ? new WaitGamemodeChangeAction() : action;
         this.baseline = baseline == null ? autismclient.util.FakeGamemodeManager.snapshot() : baseline;
         this.future = future;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         autismclient.util.FakeGamemodeManager.GamemodeSnapshot now = autismclient.util.FakeGamemodeManager.snapshot();
         return now.realRevision() > this.baseline.realRevision() && this.action.matches(this.normalizeMode(now.realMode()))
            ? true
            : this.action.detectFake && now.fakeRevision() > this.baseline.fakeRevision() && this.action.matches(this.normalizeMode(now.displayedMode()));
      }

      private GameType normalizeMode(GameType mode) {
         return mode == null ? GameType.DEFAULT_MODE : mode;
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.cancelled = true;
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.cancelled || this.future.isCancelled();
      }
   }

   static final class WaitAddonConditionEntry implements MacroWaitRegistry.WaitEntry {
      private final AddonCondition condition;
      private final CompletableFuture<Void> future;

      WaitAddonConditionEntry(AddonCondition condition, CompletableFuture<Void> future) {
         this.condition = condition;
         this.future = future;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         return this.condition.check(mc);
      }

      @Override
      public void completeWait() {
         try {
            this.condition.onComplete();
         } catch (Throwable var2) {
         }

         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.future.isCancelled();
      }
   }

   interface WaitEntry {
      boolean isWaitSatisfied(Minecraft var1);

      void completeWait();

      void cancelWait();

      boolean isWaitCancelled();
   }

   static class WaitPositionDeltaEntry implements MacroWaitRegistry.WaitEntry {
      final Vec3 origin;
      final double distanceSq;
      final boolean horizontalOnly;
      final boolean anyMovement;
      final CompletableFuture<Void> future;
      private volatile boolean cancelled = false;

      WaitPositionDeltaEntry(Vec3 origin, double distance, boolean horizontalOnly, CompletableFuture<Void> future) {
         this.origin = origin == null ? Vec3.ZERO : origin;
         double d = Math.max(0.0, distance);
         this.anyMovement = d <= 0.0;
         this.distanceSq = d * d;
         this.horizontalOnly = horizontalOnly;
         this.future = future;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         if (mc.player == null) {
            return false;
         }

         Vec3 now = mc.player.position();
         double dx = now.x - this.origin.x;
         double dy = this.horizontalOnly ? 0.0 : now.y - this.origin.y;
         double dz = now.z - this.origin.z;
         double d2 = dx * dx + dy * dy + dz * dz;
         return this.anyMovement ? d2 > 1.0E-4 : d2 >= this.distanceSq;
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.cancelled = true;
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.cancelled || this.future.isCancelled();
      }
   }

   static class WaitTickSyncEntry implements MacroWaitRegistry.WaitEntry {
      final CompletableFuture<Void> completion;

      WaitTickSyncEntry(CompletableFuture<Void> future) {
         this.completion = future;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         return true;
      }

      @Override
      public void completeWait() {
         this.completion.complete(null);
      }

      @Override
      public void cancelWait() {
         this.completion.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.completion.isCancelled();
      }
   }

   static class WaitPosEntry implements MacroWaitRegistry.WaitEntry {
      final double x;
      final double y;
      final double z;
      final double leewaySq;
      final boolean checkRotation;
      final float yaw;
      final float pitch;
      final float rotLeeway;
      final CompletableFuture<Void> future;

      WaitPosEntry(
         double x, double y, double z, double leeway, boolean checkRotation, float yaw, float pitch, float rotLeeway, CompletableFuture<Void> future
      ) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.leewaySq = leeway * leeway;
         this.checkRotation = checkRotation;
         this.yaw = yaw;
         this.pitch = pitch;
         this.rotLeeway = rotLeeway;
         this.future = future;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         if (mc.player == null) {
            return false;
         }

         double dx = mc.player.getX() - this.x;
         double dy = mc.player.getY() - this.y;
         double dz = mc.player.getZ() - this.z;
         if (dx * dx + dy * dy + dz * dz > this.leewaySq) {
            return false;
         }

         if (this.checkRotation) {
            float currentYaw = Mth.wrapDegrees(mc.player.getYRot());
            float currentPitch = Mth.wrapDegrees(mc.player.getXRot());
            float targetYaw = Mth.wrapDegrees(this.yaw);
            float targetPitch = Mth.wrapDegrees(this.pitch);
            if (Math.abs(currentYaw - targetYaw) > this.rotLeeway) {
               return false;
            }

            if (Math.abs(currentPitch - targetPitch) > this.rotLeeway) {
               return false;
            }
         }

         return true;
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.future.isCancelled();
      }
   }

   static class WaitGuiTitleEntry implements MacroWaitRegistry.WaitEntry {
      final String title;
      final String titleLower;
      final String[] titleWords;
      final WeakReference<Screen> previousScreen;
      final CompletableFuture<Void> future;

      WaitGuiTitleEntry(String title, Screen prevScreen, CompletableFuture<Void> future) {
         this.title = MacroExecutor.cleanVisibleChatText(title);
         this.titleLower = this.title.toLowerCase();
         this.titleWords = this.titleLower.isEmpty() ? new String[0] : this.titleLower.split("\\s+");
         this.previousScreen = new WeakReference<>(prevScreen);
         this.future = future;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         return this.matchesScreen(mc.gui.screen());
      }

      public boolean matchesScreen(Screen screen) {
         if (screen == null) {
            return false;
         }

         if (GuiScreenMatcher.isClientScreen(screen)) {
            return false;
         }

         if (screen == this.previousScreen.get()) {
            return false;
         }

         if (!this.titleLower.isEmpty() && !GuiScreenMatcher.matchesTitle(screen, this.title)) {
            return false;
         }

         if (!this.titleLower.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null
               && mc.player.containerMenu != null
               && mc.player.containerMenu != mc.player.inventoryMenu
               && screen instanceof AbstractContainerScreen) {
               return mc.player.containerMenu.getStateId() > 0;
            }
         }

         return true;
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.future.isCancelled();
      }
   }

   static class WaitGuiCloseEntry implements MacroWaitRegistry.WaitEntry {
      final String titleLower;
      final String[] titleWords;
      final CompletableFuture<Void> future;
      private volatile boolean cancelled = false;
      private boolean satisfied = false;

      WaitGuiCloseEntry(String title, CompletableFuture<Void> future) {
         this.titleLower = MacroExecutor.cleanVisibleChatText(title).toLowerCase();
         this.titleWords = this.titleLower.isEmpty() ? new String[0] : this.titleLower.split("\\s+");
         this.future = future;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         Screen screen = mc.gui.screen();
         if (screen != null && !this.titleLower.isEmpty() && GuiScreenMatcher.matchesTitle(screen, this.titleLower)) {
            this.satisfied = true;
            return false;
         }

         if (this.titleLower.isEmpty()) {
            if (screen != null) {
               this.satisfied = true;
               return false;
            } else {
               return this.satisfied;
            }
         } else {
            return this.satisfied || screen == null;
         }
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.cancelled = true;
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.cancelled || this.future.isCancelled();
      }
   }

   static class WaitSoundEntry implements MacroWaitRegistry.WaitEntry {
      final WaitForSoundAction action;
      final CompletableFuture<Void> future;
      private volatile boolean cancelled = false;

      WaitSoundEntry(WaitForSoundAction action, CompletableFuture<Void> future) {
         this.action = action;
         this.future = future;
      }

      public boolean matchesSound(Minecraft mc, String soundId, double x, double y, double z) {
         if (!this.action.soundIds.isEmpty()) {
            boolean found = false;
            String played = normalizeSoundId(soundId);
            String playedPath = soundPath(played);

            for (String id : this.action.soundIds) {
               String expected = normalizeSoundId(id);
               if (expected.equals(played) || expected.equals(playedPath) || soundPath(expected).equals(played) || soundPath(expected).equals(playedPath)) {
                  found = true;
                  break;
               }
            }

            if (!found) {
               return false;
            }
         }

         if (this.action.checkDistance && mc != null && mc.player != null) {
            double dx = mc.player.getX() - x;
            double dy = mc.player.getY() - y;
            double dz = mc.player.getZ() - z;
            if (dx * dx + dy * dy + dz * dz > this.action.maxDistance * this.action.maxDistance) {
               return false;
            }
         }

         return true;
      }

      private static String normalizeSoundId(String value) {
         return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      }

      private static String soundPath(String value) {
         String normalized = normalizeSoundId(value);
         int split = normalized.indexOf(58);
         return split >= 0 && split + 1 < normalized.length() ? normalized.substring(split + 1) : normalized;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         return false;
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.cancelled = true;
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.cancelled || this.future.isCancelled();
      }
   }

   static class WaitWorldChangeEntry implements MacroWaitRegistry.WaitEntry {
      final String initialDimension;
      final String dimension;
      final long createdNanos;
      final CompletableFuture<Void> future;
      private volatile boolean cancelled = false;

      WaitWorldChangeEntry(String initialDimension, String targetDimension, CompletableFuture<Void> future) {
         this.initialDimension = initialDimension == null ? "" : initialDimension;
         this.dimension = MacroWaitRegistry.normalizeDimensionName(targetDimension);
         this.future = future;
         this.createdNanos = System.nanoTime();
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         if (MacroWaitRegistry.lastWorldChangeNanos > this.createdNanos && this.dimension.isEmpty()) {
            return true;
         }

         String current = MacroWaitRegistry.currentDimensionId(mc);
         return !current.isEmpty() && !current.equals(this.initialDimension)
            ? this.dimension.isEmpty() || MacroWaitRegistry.dimensionMatches(current, this.dimension)
            : MacroWaitRegistry.lastWorldChangeNanos > this.createdNanos && !this.dimension.isEmpty() && !current.isEmpty() && MacroWaitRegistry.dimensionMatches(current, this.dimension);
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.cancelled = true;
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.cancelled || this.future.isCancelled();
      }
   }

   static class WaitLanStepEntry implements MacroWaitRegistry.WaitEntry {
      final WaitForLanStepAction action;
      final CompletableFuture<Void> future;
      private volatile boolean cancelled = false;

      WaitLanStepEntry(WaitForLanStepAction action, CompletableFuture<Void> future) {
         this.action = action;
         this.future = future;
      }

      boolean isLanStepReached() {
         autismclient.util.LanSyncManager sync = autismclient.util.LanSyncManager.getInstance();
         if (!sync.isInSession()) {
            return true;
         }

         String myUsername = sync.getLocalUsername();
         Map<String, Integer> steps = sync.getPeerStepProgressMap();
         if (!this.action.filterByUser) {
            return this.anyPeerReached(steps, myUsername, this.action.defaultStep);
         }

         if (this.action.entries.isEmpty()) {
            return this.anyPeerReached(steps, myUsername, this.action.defaultStep);
         }

         int peerCount = sync.getConnectedClientCount();

         for (WaitForLanStepAction.StepEntry req : this.action.entries) {
            String targetUser = req.username;
            if (peerCount <= 2 && !targetUser.isEmpty()) {
               boolean nameExists = false;

               for (String name : steps.keySet()) {
                  if (name.equals(targetUser) && !name.equals(myUsername)) {
                     nameExists = true;
                     break;
                  }
               }

               if (!nameExists) {
                  targetUser = "";
               }
            }

            if (targetUser.isEmpty()) {
               boolean anyReached = false;

               for (Entry<String, Integer> entry : steps.entrySet()) {
                  if (!entry.getKey().equals(myUsername) && entry.getValue() >= req.step) {
                     anyReached = true;
                     break;
                  }
               }

               if (!anyReached) {
                  return false;
               }
            } else {
               Map<String, ?> clients = sync.getConnectedClients();
               if (clients.containsKey(targetUser)) {
                  int peerStep = steps.getOrDefault(targetUser, 0);
                  if (peerStep < req.step) {
                     return false;
                  }
               }
            }
         }

         return true;
      }

      private boolean anyPeerReached(Map<String, Integer> steps, String myUsername, int targetStep) {
         for (Entry<String, Integer> entry : steps.entrySet()) {
            if (!entry.getKey().equals(myUsername) && entry.getValue() >= targetStep) {
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         return this.isLanStepReached();
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.cancelled = true;
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.cancelled || this.future.isCancelled();
      }
   }

   static class WaitContainerItemEntry implements MacroWaitRegistry.WaitEntry {
      final MacroItemTarget target;
      final CompletableFuture<Void> future;
      private volatile boolean cancelled = false;

      WaitContainerItemEntry(MacroItemTarget target, CompletableFuture<Void> future) {
         this.target = target == null ? new MacroItemTarget() : target.copy();
         this.future = future;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         if (mc.player != null && this.target.hasItemFilter()) {
            AbstractContainerMenu handler = mc.player.containerMenu;
            if (handler == null) {
               return false;
            }

            for (Slot slot : handler.slots) {
               if (slot != null && !slot.getItem().isEmpty()) {
                  int visibleSlot = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, slot.index);
                  if (this.target.matches(slot.getItem(), visibleSlot)) {
                     return true;
                  }
               }
            }

            return false;
         } else {
            return false;
         }
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.cancelled = true;
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.cancelled || this.future.isCancelled();
      }
   }

   static class WaitBlockEntry implements MacroWaitRegistry.WaitEntry {
      final WaitForBlockAction action;
      final Set<String> blockIds;
      final CompletableFuture<Void> future;

      WaitBlockEntry(WaitForBlockAction action, CompletableFuture<Void> future) {
         this.action = action;
         this.blockIds = new HashSet<>(action.blockIds);
         this.future = future;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         if (mc.level != null && mc.player != null) {
            boolean destroyed = this.action.waitBehavior == WaitForBlockAction.Inner_StreamUtil.DESTROYED;
            boolean anyBlock = this.action.anyBlock;
            switch (this.action.checkMode) {
               case AT_POSITION:
                  Block b = mc.level.getBlockState(this.action.blockPos).getBlock();
                  boolean isAir = b == Blocks.AIR;
                  if (destroyed) {
                     if (anyBlock) {
                        return isAir;
                     } else {
                        if (isAir) {
                           return true;
                        }

                        return !this.blockIds.contains(BuiltInRegistries.BLOCK.getKey(b).toString());
                     }
                  } else if (isAir) {
                     return false;
                  } else if (!anyBlock && !this.blockIds.isEmpty() && !this.blockIds.contains(BuiltInRegistries.BLOCK.getKey(b).toString())) {
                     return false;
                  } else {
                     if (this.action.mustBeInReach) {
                        return this.hasLineOfSight(mc);
                     }

                     return true;
                  }
               case IN_REACH:
                  int r = (int)Math.ceil(this.action.searchRadius);
                  int cx = (int)mc.player.getX();
                  int cy = (int)mc.player.getY();
                  int cz = (int)mc.player.getZ();
                  MutableBlockPos mut = new MutableBlockPos();
                  if (destroyed) {
                     if (anyBlock) {
                        return false;
                     } else {
                        for (int dx = -r; dx <= r; dx++) {
                           for (int dy = -r; dy <= r; dy++) {
                              for (int dz = -r; dz <= r; dz++) {
                                 mut.set(cx + dx, cy + dy, cz + dz);
                                 Block bx = mc.level.getBlockState(mut).getBlock();
                                 if (bx != Blocks.AIR && this.blockIds.contains(BuiltInRegistries.BLOCK.getKey(bx).toString())) {
                                    return false;
                                 }
                              }
                           }
                        }

                        return true;
                     }
                  } else {
                     for (int dx = -r; dx <= r; dx++) {
                        for (int dy = -r; dy <= r; dy++) {
                           for (int dz = -r; dz <= r; dz++) {
                              mut.set(cx + dx, cy + dy, cz + dz);
                              Block bx = mc.level.getBlockState(mut).getBlock();
                              if (bx != Blocks.AIR) {
                                 if (anyBlock || this.blockIds.isEmpty()) {
                                    return true;
                                 }

                                 if (this.blockIds.contains(BuiltInRegistries.BLOCK.getKey(bx).toString())) {
                                    return true;
                                 }
                              }
                           }
                        }
                     }

                     return false;
                  }
               case LOOKING_AT:
                  if (destroyed) {
                     if (mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK) {
                        if (!anyBlock && !this.blockIds.isEmpty()) {
                           BlockHitResult bhr = (BlockHitResult)mc.hitResult;
                           Block blk1 = mc.level.getBlockState(bhr.getBlockPos()).getBlock();
                           return !this.blockIds.contains(BuiltInRegistries.BLOCK.getKey(blk1).toString());
                        }

                        return false;
                     }

                     return anyBlock;
                  } else {
                     if (mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK) {
                        BlockHitResult bhr = (BlockHitResult)mc.hitResult;
                        if (!anyBlock && !this.blockIds.isEmpty()) {
                           Block blk2 = mc.level.getBlockState(bhr.getBlockPos()).getBlock();
                           return this.blockIds.contains(BuiltInRegistries.BLOCK.getKey(blk2).toString());
                        }

                        return true;
                     }

                     return false;
                  }
               default:
                  return false;
            }
         } else {
            return false;
         }
      }

      private boolean hasLineOfSight(Minecraft mc) {
         Vec3 center = Vec3.atCenterOf(this.action.blockPos);
         if (mc.player.distanceToSqr(center) > 36.0) {
            return false;
         }

         ClipContext ctx = new ClipContext(mc.player.getEyePosition(), center, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, mc.player);
         BlockHitResult result = mc.level.clip(ctx);
         return result.getType() == Type.BLOCK && result.getBlockPos().equals(this.action.blockPos);
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.future.isCancelled();
      }
   }

   static class WaitHealthEntry implements MacroWaitRegistry.WaitEntry {
      final float threshold;
      final boolean below;
      final CompletableFuture<Void> future;

      WaitHealthEntry(float threshold, boolean below, CompletableFuture<Void> future) {
         this.threshold = threshold;
         this.below = below;
         this.future = future;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         if (mc.player == null) {
            return false;
         }

         float health = mc.player.getHealth();
         return this.below ? health < this.threshold : health > this.threshold;
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.future.isCancelled();
      }
   }

   static class WaitScreenChangeEntry implements MacroWaitRegistry.WaitEntry {
      final WeakReference<Screen> previousScreen;
      final CompletableFuture<Void> future;
      private volatile boolean cancelled = false;

      WaitScreenChangeEntry(Screen prevScreen, CompletableFuture<Void> future) {
         this.previousScreen = new WeakReference<>(prevScreen);
         this.future = future;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         return mc.gui.screen() != this.previousScreen.get();
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.cancelled = true;
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.cancelled || this.future.isCancelled();
      }
   }

   static class WaitServerTickSyncEntry implements MacroWaitRegistry.WaitEntry {
      final CompletableFuture<Void> future;
      final int bufferMs;
      final int timeoutMs;
      final boolean ignorePing;
      final long startNanos;

      WaitServerTickSyncEntry(CompletableFuture<Void> future, int bufferMs, int maxWaitMs, boolean ignorePing) {
         this.future = future;
         this.bufferMs = bufferMs;
         this.timeoutMs = maxWaitMs;
         this.ignorePing = ignorePing;
         this.startNanos = System.nanoTime();
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         long now = System.nanoTime();
         long elapsedMs = (now - this.startNanos) / 1000000L;
         if (elapsedMs >= this.timeoutMs) {
            return true;
         }

         if (!ServerTickPhaseTracker.isWarmedUp()) {
            return false;
         }

         long optimalTime = ServerTickPhaseTracker.optimalSendTime(this.bufferMs, this.ignorePing);
         return now >= optimalTime;
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.future.isCancelled();
      }
   }

   static class WaitInventoryItemEntry implements MacroWaitRegistry.WaitEntry {
      final MacroItemTarget target;
      final CompletableFuture<Void> future;

      WaitInventoryItemEntry(MacroItemTarget target, CompletableFuture<Void> future) {
         this.target = target == null ? new MacroItemTarget() : target.copy();
         this.future = future;
      }

      @Override
      public boolean isWaitSatisfied(Minecraft mc) {
         if (mc.player != null && this.target.hasItemFilter()) {
            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
               ItemStack stack = mc.player.getInventory().getItem(i);
               if (!stack.isEmpty() && this.target.matches(stack, i)) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      }

      @Override
      public void completeWait() {
         this.future.complete(null);
      }

      @Override
      public void cancelWait() {
         this.future.cancel(true);
      }

      @Override
      public boolean isWaitCancelled() {
         return this.future.isCancelled();
      }
   }
}
