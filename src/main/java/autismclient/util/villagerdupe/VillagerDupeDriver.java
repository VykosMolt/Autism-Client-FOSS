package autismclient.util.villagerdupe;

import autismclient.AutismClientAddon;
import autismclient.compat263.Compat263;
import autismclient.modules.AutoTotemModule;
import autismclient.modules.aD;
import autismclient.util.InventorySlotArbiter;
import autismclient.util.InventorySlotHelper;
import autismclient.util.AutismSessionState;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult.Success;
import net.minecraft.world.InteractionResult.SwingSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class VillagerDupeDriver {
   public static final String ACTION_GROUP = "villager-dupe";
   private static final VillagerDupeDriver INSTANCE = new VillagerDupeDriver();
   private static final List<Predicate<Minecraft>> TICK_HANDLERS = new CopyOnWriteArrayList<>();
   private static final int init_I = -2147483648;
   private static final int load_I = 200;
   private static final int read_I = 600;
   private static final int write_I = 3;
   private static final int update_I = 100;
   private static final long init_J = 50000000L;
   private static final long load_J = 150000000L;
   private static final int sync_I = 8;
   private final Map<Integer, VillagerDupeDriver.TaskQueue> villagerTracks = new HashMap<>();
   private final ArrayDeque<int[]> pendingShearUses = new ArrayDeque<>();
   private ItemStack trackedShears = ItemStack.EMPTY;
   private int shearsDamageBefore;
   private ClientLevel lastLevel;
   private boolean flickActive;
   private int restoreSlot = -1;
   private boolean flickSlotEmpty;
   private boolean awaitingAnswers;
   private int flickStartTick = -2147483648;
   private int flickWindowTicks;
   private boolean sawHandCleared;
   private long flickStartNanos;
   private long flickHoldNanos = 50000000L;
   private boolean gotHandUpdate;
   private int slowHoldStreak;
   private int lastSwapTick = -2147483648;
   private int lastUseTick = -2147483648;
   private int menuGuardTick = -2147483648;
   private boolean actionGroupOpen;
   private int closedMenuCount;

   public static VillagerDupeDriver getInstance() {
      return INSTANCE;
   }

   public VillagerDupeDriver.BufferHelper step(Minecraft mc, VillagerDupeDriver.CachePool opts) {
      return this.init_DataUtil_BufferHel_Minecraft(mc, opts, VillagerDupeDriver.StreamUtil.SHEAR);
   }

   public VillagerDupeDriver.BufferHelper start(Minecraft mc, VillagerDupeDriver.CachePool opts) {
      return this.init_DataUtil_BufferHel_Minecraft(mc, opts, VillagerDupeDriver.StreamUtil.HOLD);
   }

   public VillagerDupeDriver.BufferHelper resume(Minecraft mc, VillagerDupeDriver.CachePool opts) {
      return this.init_DataUtil_BufferHel_Minecraft(mc, opts, VillagerDupeDriver.StreamUtil.WATCH);
   }

   public boolean isActive() {
      return this.flickActive;
   }

   public int init_I() {
      return this.closedMenuCount;
   }

   public static void addWatcher(Predicate<Minecraft> body) {
      TICK_HANDLERS.add(body);
   }

   public static void tickWatchers(Minecraft mc) {
      if (!TICK_HANDLERS.isEmpty() && !AutoTotemModule.init_Z_6()) {
         for (Predicate<Minecraft> driver : TICK_HANDLERS) {
            boolean keep = false;

            try {
               keep = driver.test(mc);
            } catch (Throwable t) {
               AutismClientAddon.LOG.debug("[Autism] Villager dupe driver failed", t);
            }

            if (!keep) {
               TICK_HANDLERS.remove(driver);
            }
         }
      }
   }

   public static void tickInstance(Minecraft mc) {
      INSTANCE.init_Z_Minecraft(mc);
   }

   public void restoreSelectedSlot(Minecraft mc) {
      if (this.flickActive && mc != null && mc.player != null && this.restoreSlot >= 0 && this.restoreSlot < 9 && !aD.read_Z()) {
         mc.player.getInventory().setSelectedSlot(this.restoreSlot);
      }

      this.flickActive = false;
      this.restoreSlot = -1;
      this.clearPendingAnswers();
   }

   public VillagerDupeDriver.ConfigStore checkReady(Minecraft mc, VillagerDupeDriver.CachePool opts) {
      if (!isClientReady(mc)) {
         return VillagerDupeDriver.ConfigStore.NO_WORLD;
      }

      LocalPlayer player = mc.player;
      VillagerDupeDriver.ConfigStore emerald = this.init_DataUtil_ConfigSto_LocalPlayer_DataUtil_CachePool(player, opts);
      return emerald != null ? emerald : this.shearsStatus(mc, player, opts, player.hasInfiniteMaterials(), AutismSessionState.getInstance().getClientTick());
   }

   public static boolean hasTrader(Minecraft mc, VillagerDupeDriver.ItemFilter filter, boolean traders) {
      if (!isClientReady(mc)) {
         return false;
      }

      LocalPlayer player = mc.player;
      double reach = Math.max(0.0, player.entityInteractionRange());
      if (reach <= 0.0) {
         return false;
      }

      VillagerDupeDriver.ItemFilter wanted = filter == null ? VillagerDupeDriver.ItemFilter.ALL : filter;

      for (AbstractVillager villager : mc.level.getEntitiesOfClass(AbstractVillager.class, player.getBoundingBox().inflate(reach), Entity::isAlive)) {
         if (isEligibleVillager(player, villager, traders) && wanted.matchesFilter(villager.getMainHandItem())) {
            return true;
         }
      }

      return false;
   }

   public static boolean isShearableEquipment(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
         return equippable != null && equippable.canBeSheared() && !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
      } else {
         return false;
      }
   }

   private VillagerDupeDriver.BufferHelper init_DataUtil_BufferHel_Minecraft(Minecraft mc, VillagerDupeDriver.CachePool opts, VillagerDupeDriver.StreamUtil mode) {
      if (!isClientReady(mc)) {
         return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.NO_WORLD);
      }

      LocalPlayer player = mc.player;
      int now = AutismSessionState.getInstance().getClientTick();
      this.onLevelChanged(mc.level);
      this.scanVillagers(mc, player, opts, now);
      if (mc.gui.screen() != null) {
         if (!this.init_Z_Minecraft(mc) && player.containerMenu instanceof MerchantMenu) {
            this.clearObservedHands();
         }

         return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.SCREEN);
      } else {
         if (mode == VillagerDupeDriver.StreamUtil.WATCH) {
            return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.WAITING);
         }

         if (player.isSpectator()) {
            return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.SPECTATOR);
         }

         if (player.isAlive()
            && player.containerMenu == player.inventoryMenu
            && player.inventoryMenu.getCarried().isEmpty()
            && !player.isUsingItem()
            && !mc.gameMode.isDestroying()
            && !aD.read_Z()) {
            this.actionGroupOpen = false;

            try {
               return this.planStep(mc, player, opts, now, mode);
            } finally {
               if (this.actionGroupOpen) {
                  InventorySlotArbiter.endActionGroup("villager-dupe");
               }

               this.actionGroupOpen = false;
            }
         } else {
            return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.BUSY);
         }
      }
   }

   private VillagerDupeDriver.BufferHelper planStep(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts, int now, VillagerDupeDriver.StreamUtil mode) {
      this.reconcileShearDurability(player);
      VillagerDupeDriver.ConfigStore emerald = this.init_DataUtil_ConfigSto(mc, player, opts, now);
      if (emerald != null) {
         return new VillagerDupeDriver.BufferHelper(0, emerald);
      }

      if (mode == VillagerDupeDriver.StreamUtil.HOLD) {
         return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.WAITING);
      }

      boolean creative = player.hasInfiniteMaterials();
      VillagerDupeDriver.ConfigStore shears = this.shearsStatus(mc, player, opts, creative, now);
      List<AbstractVillager> targets = this.reachableVillagers(mc, player, opts);
      if (targets.isEmpty()) {
         if (shears == null) {
            this.startHotbarFlick(mc, player, opts, now);
         }

         return new VillagerDupeDriver.BufferHelper(0, shears == null ? VillagerDupeDriver.ConfigStore.WAITING : shears);
      } else {
         if (player.isShiftKeyDown()) {
            return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.SNEAKING);
         }

         if (shears != null) {
            return new VillagerDupeDriver.BufferHelper(0, shears);
         }

         if (!this.isFlickWindowOpen(now) && ticksSince(now, this.lastUseTick) >= opts.init_I()) {
            targets.removeIf(villagerx -> this.init_Z_Minecraft_3(mc, villagerx, now));
            if (targets.isEmpty()) {
               return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.WAITING);
            }

            VillagerDupeDriver.ConfigStore ready = this.villagerStatus(mc, player, opts, creative, now);
            if (ready != null) {
               return new VillagerDupeDriver.BufferHelper(0, ready);
            }

            int budget = creative ? 2147483647 : remainingShearUses(player.getOffhandItem(), this.inflightShearCount(mc, now));
            int count = Math.min(targets.size(), opts.update() ? Math.min(1, budget) : budget);
            if (count <= 0) {
               return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.SHEARS_WORN);
            }

            if (!this.ensureActionGroup()) {
               return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.BUSY);
            }

            boolean swing = false;

            for (int i = 0; i < count; i++) {
               AbstractVillager villager = targets.get(i);
               swing |= interactOffHandPredicted(mc, player, villager);
               VillagerDupeDriver.TaskQueue track = this.villagerTracks.get(villager.getId());
               if (track != null) {
                  track.lastHand = villager.getMainHandItem();
               }
            }

            if (swing) {
               Compat263.swing(player, InteractionHand.OFF_HAND);
            }

            this.lastUseTick = now;
            this.menuGuardTick = now;
            if (!creative) {
               if (this.pendingShearUses.isEmpty()) {
                  this.resetShearTracking(player);
               }

               this.pendingShearUses.addLast(new int[]{now, count});
            }

            this.startHotbarFlick(mc, player, opts, now);
            return new VillagerDupeDriver.BufferHelper(count, VillagerDupeDriver.ConfigStore.SHEARED);
         } else {
            return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.WAITING);
         }
      }
   }

   private VillagerDupeDriver.ConfigStore init_DataUtil_ConfigSto(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts, int now) {
      Inventory inventory = player.getInventory();
      if (player.getMainHandItem().is(Items.EMERALD)) {
         if (this.flickActive) {
            this.endHotbarFlick(mc, opts, now);
         }

         return null;
      } else {
         if (this.flickActive && System.nanoTime() - this.flickStartNanos < this.flickHoldNanos) {
            return VillagerDupeDriver.ConfigStore.WAITING;
         }

         if (ticksSince(now, this.lastSwapTick) == 0) {
            return VillagerDupeDriver.ConfigStore.BUSY;
         }

         int slot = this.flickActive && isEmeraldHotbarSlot(inventory, this.restoreSlot) ? this.restoreSlot : findEmeraldHotbarSlot(inventory);
         if (slot < 0) {
            int from = findEmeraldInMainInventory(inventory);
            if (from < 0) {
               if (player.getOffhandItem().is(Items.EMERALD)) {
                  return this.init_DataUtil_ConfigSto_Minecraft(mc, player, opts);
               }

               return VillagerDupeDriver.ConfigStore.NO_EMERALD;
            }

            if (!opts.load()) {
               return VillagerDupeDriver.ConfigStore.EMERALD_NOT_IN_HOTBAR;
            }

            int into = findEmptyHotbarSlot(inventory);
            if (into < 0) {
               into = inventory.getSelectedSlot();
            }

            if (!this.ensureActionGroup() || !InventorySlotHelper.swapToHotbar(mc, from, into)) {
               return VillagerDupeDriver.ConfigStore.BUSY;
            }

            slot = into;
         }

         if (inventory.getSelectedSlot() != slot) {
            if (!this.ensureActionGroup()) {
               return VillagerDupeDriver.ConfigStore.BUSY;
            }

            InventorySlotHelper.selectHotbarSlot(mc, slot);
            if (inventory.getSelectedSlot() != slot) {
               return VillagerDupeDriver.ConfigStore.BUSY;
            }

            this.lastSwapTick = now;
         }

         if (!player.getMainHandItem().is(Items.EMERALD)) {
            return VillagerDupeDriver.ConfigStore.BUSY;
         }

         if (this.flickActive) {
            this.endHotbarFlick(mc, opts, now);
         }

         return null;
      }
   }

   private VillagerDupeDriver.ConfigStore init_DataUtil_ConfigSto_Minecraft(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts) {
      if (opts.read() && !InventorySlotArbiter.isHotbarClaimedByOther("villager-dupe")) {
         int slot = bestShearsSlot(player, player.hasInfiniteMaterials());
         if (slot < 0) {
            return VillagerDupeDriver.ConfigStore.EMERALD_IN_OFFHAND;
         } else if (this.ensureActionGroup() && InventorySlotHelper.swapToOffhand(mc, slot)) {
            this.resetShearTracking(player);
            return VillagerDupeDriver.ConfigStore.BUSY;
         } else {
            return VillagerDupeDriver.ConfigStore.BUSY;
         }
      } else {
         return VillagerDupeDriver.ConfigStore.EMERALD_IN_OFFHAND;
      }
   }

   private VillagerDupeDriver.ConfigStore init_DataUtil_ConfigSto_LocalPlayer_DataUtil_CachePool(LocalPlayer player, VillagerDupeDriver.CachePool opts) {
      Inventory inventory = player.getInventory();
      if (player.getMainHandItem().is(Items.EMERALD) || findEmeraldHotbarSlot(inventory) >= 0) {
         return null;
      }

      if (findEmeraldInMainInventory(inventory) >= 0) {
         return opts.load() ? null : VillagerDupeDriver.ConfigStore.EMERALD_NOT_IN_HOTBAR;
      }

      if (!player.getOffhandItem().is(Items.EMERALD)) {
         return VillagerDupeDriver.ConfigStore.NO_EMERALD;
      }

      boolean freed = opts.read() && bestShearsSlot(player, player.hasInfiniteMaterials()) >= 0;
      return freed ? null : VillagerDupeDriver.ConfigStore.EMERALD_IN_OFFHAND;
   }

   private VillagerDupeDriver.ConfigStore shearsStatus(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts, boolean creative, int now) {
      ItemStack offhand = player.getOffhandItem();
      boolean offhandShears = offhand.is(Items.SHEARS);
      if (!offhandShears || !creative && remainingShearUses(offhand, this.inflightShearCount(mc, now)) <= 0) {
         boolean settling = offhandShears && !creative && remainingShearUses(offhand, 0) > 0;
         if (!opts.read()) {
            if (settling) {
               return VillagerDupeDriver.ConfigStore.BUSY;
            } else if (offhandShears) {
               return VillagerDupeDriver.ConfigStore.SHEARS_WORN;
            } else {
               return hasShears(player) ? VillagerDupeDriver.ConfigStore.SHEARS_NOT_IN_OFFHAND : VillagerDupeDriver.ConfigStore.NO_SHEARS;
            }
         } else if (bestShearsSlot(player, creative) < 0) {
            if (settling) {
               return VillagerDupeDriver.ConfigStore.BUSY;
            } else {
               return !offhandShears && !hasShears(player) ? VillagerDupeDriver.ConfigStore.NO_SHEARS : VillagerDupeDriver.ConfigStore.SHEARS_WORN;
            }
         } else if (opts.write() && offhand.is(Items.TOTEM_OF_UNDYING)) {
            return VillagerDupeDriver.ConfigStore.TOTEM;
         } else {
            return InventorySlotArbiter.isHotbarClaimedByOther("villager-dupe") ? VillagerDupeDriver.ConfigStore.OFFHAND_TAKEN : null;
         }
      } else {
         return null;
      }
   }

   private VillagerDupeDriver.ConfigStore villagerStatus(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts, boolean creative, int now) {
      VillagerDupeDriver.ConfigStore status = this.shearsStatus(mc, player, opts, creative, now);
      if (status != null) {
         return status;
      }

      ItemStack offhand = player.getOffhandItem();
      if (!offhand.is(Items.SHEARS) || !creative && remainingShearUses(offhand, this.inflightShearCount(mc, now)) <= 0) {
         int slot = bestShearsSlot(player, creative);
         if (slot < 0) {
            return VillagerDupeDriver.ConfigStore.NO_SHEARS;
         } else if (this.ensureActionGroup() && InventorySlotHelper.swapToOffhand(mc, slot)) {
            this.resetShearTracking(player);
            return player.getOffhandItem().is(Items.SHEARS) ? null : VillagerDupeDriver.ConfigStore.BUSY;
         } else {
            return VillagerDupeDriver.ConfigStore.BUSY;
         }
      } else {
         return null;
      }
   }

   private static int remainingShearUses(ItemStack shears, int inflight) {
      return !shears.isDamageableItem() ? 2147483647 : shears.getMaxDamage() - shears.getDamageValue() - 1 - inflight;
   }

   private void reconcileShearDurability(LocalPlayer player) {
      ItemStack offhand = player.getOffhandItem();
      if (offhand != this.trackedShears) {
         int done = offhand.getDamageValue() - this.shearsDamageBefore;
         if (offhand.is(Items.SHEARS) && this.trackedShears.is(Items.SHEARS) && done >= 0) {
            this.trackedShears = offhand;
            this.shearsDamageBefore = offhand.getDamageValue();

            while (done > 0 && !this.pendingShearUses.isEmpty()) {
               int[] entry = this.pendingShearUses.peekFirst();
               int take = Math.min(done, entry[1]);
               entry[1] -= take;
               done -= take;
               if (entry[1] == 0) {
                  this.pendingShearUses.pollFirst();
               }
            }
         } else {
            this.resetShearTracking(player);
         }
      }
   }

   private void resetShearTracking(LocalPlayer player) {
      this.pendingShearUses.clear();
      this.trackedShears = player.getOffhandItem();
      this.shearsDamageBefore = this.trackedShears.getDamageValue();
   }

   private int inflightShearCount(Minecraft mc, int now) {
      int window = pingTicks(mc) + 3;
      int sum = 0;
      Iterator<int[]> it = this.pendingShearUses.iterator();

      while (it.hasNext()) {
         int[] entry = it.next();
         if (ticksSince(now, entry[0]) > window) {
            it.remove();
         } else {
            sum += entry[1];
         }
      }

      return sum;
   }

   private List<AbstractVillager> reachableVillagers(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts) {
      List<AbstractVillager> out = new ArrayList<>();
      double reach = Math.max(0.0, player.entityInteractionRange());
      if (reach <= 0.0) {
         return out;
      }

      for (AbstractVillager villager : mc.level.getEntitiesOfClass(AbstractVillager.class, player.getBoundingBox().inflate(reach), Entity::isAlive)) {
         if (isEligibleVillager(player, villager, opts.includeTraders()) && opts.itemFilter().matchesFilter(villager.getMainHandItem())) {
            out.add(villager);
         }
      }

      out.sort(Comparator.comparingDouble(villagerx -> villagerx.distanceToSqr(player)));
      return out;
   }

   private static boolean isEligibleVillager(LocalPlayer player, AbstractVillager villager, boolean traders) {
      return villager.isAlive()
         && !villager.isRemoved()
         && !villager.isBaby()
         && !villager.isVehicle()
         && (traders || villager instanceof Villager)
         && player.isWithinEntityInteractionRange(villager, 0.0);
   }

   private static boolean interactOffHandPredicted(Minecraft mc, LocalPlayer player, AbstractVillager villager) {
      AABB box = villager.getBoundingBox();
      Vec3 eye = player.getEyePosition();
      Vec3 hit = new Vec3(Math.clamp(eye.x, box.minX, box.maxX), Math.clamp(eye.y, box.minY, box.maxY), Math.clamp(eye.z, box.minZ, box.maxZ));
      return mc.gameMode.interact(player, villager, new EntityHitResult(villager, hit), InteractionHand.OFF_HAND) instanceof Success success
         && success.swingSource() == SwingSource.PREDICTED;
   }

   private void startHotbarFlick(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts, int now) {
      if (opts.sync()
         && !this.flickActive
         && !this.awaitingAnswers
         && ticksSince(now, this.lastSwapTick) != 0
         && player.getMainHandItem().is(Items.EMERALD)
         && !player.isShiftKeyDown()
         && this.reachableVillagers(mc, player, opts).isEmpty()
         && this.anyTrackReadyToAsk(opts, now)) {
         Inventory inventory = player.getInventory();
         int away = nextEmptyHotbarSlot(inventory);
         if (away >= 0 && this.ensureActionGroup()) {
            int from = inventory.getSelectedSlot();
            boolean empty = inventory.getItem(away).isEmpty();
            InventorySlotHelper.selectHotbarSlot(mc, away);
            if (inventory.getSelectedSlot() == away) {
               this.lastSwapTick = now;
               this.restoreSlot = from;
               this.flickSlotEmpty = empty;
               this.flickActive = true;
               this.sawHandCleared = false;
               this.flickStartNanos = System.nanoTime();
               this.gotHandUpdate = false;

               for (VillagerDupeDriver.TaskQueue track : this.villagerTracks.values()) {
                  track.handCleared = false;
               }
            }
         }
      }
   }

   private boolean anyTrackReadyToAsk(VillagerDupeDriver.CachePool opts, int now) {
      for (VillagerDupeDriver.TaskQueue track : this.villagerTracks.values()) {
         if (track.lastSeenTick == now
            && track.eligible
            && canAskTrack(track, now)
            && (track.observedHand == null || opts.itemFilter().matchesFilter(track.observedHand) || ticksSince(now, track.observedAt) > 600)) {
            return true;
         }
      }

      return false;
   }

   private static boolean canAskTrack(VillagerDupeDriver.TaskQueue track, int now) {
      return track.misses < 3 || ticksSince(now, track.lastAttemptTick) >= 100;
   }

   private void endHotbarFlick(Minecraft mc, VillagerDupeDriver.CachePool opts, int now) {
      this.flickActive = false;
      this.restoreSlot = -1;
      this.flickWindowTicks = pingTicks(mc) + 1;
      this.awaitingAnswers = false;

      for (VillagerDupeDriver.TaskQueue track : this.villagerTracks.values()) {
         boolean asked = track.lastSeenTick == now && track.eligible && canAskTrack(track, now);
         if (asked) {
            track.askedAt = now;
         }

         track.awaitingAnswer = asked && (this.flickSlotEmpty || track.observedHand == null || opts.itemFilter().matchesFilter(track.observedHand));
         this.awaitingAnswers = this.awaitingAnswers | track.awaitingAnswer;
      }

      this.flickStartTick = now;
   }

   private boolean isFlickWindowOpen(int now) {
      return this.flickActive || ticksSince(now, this.flickStartTick) < this.flickWindowTicks;
   }

   private boolean init_Z_Minecraft_3(Minecraft mc, AbstractVillager villager, int now) {
      if (this.flickActive || !this.sawHandCleared && ticksSince(now, this.flickStartTick) <= answerWindowTicks(mc)) {
         VillagerDupeDriver.TaskQueue track = this.villagerTracks.get(villager.getId());
         return track != null && !track.itemChanged && track.observedHand != null ? !ItemStack.isSameItem(villager.getMainHandItem(), track.observedHand) : true;
      } else {
         return false;
      }
   }

   private void clearPendingAnswers() {
      this.awaitingAnswers = false;

      for (VillagerDupeDriver.TaskQueue track : this.villagerTracks.values()) {
         track.awaitingAnswer = false;
      }
   }

   private void scanVillagers(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts, int now) {
      double reach = Math.max(0.0, player.entityInteractionRange());

      for (AbstractVillager villager : mc.level.getEntitiesOfClass(AbstractVillager.class, player.getBoundingBox().inflate(reach + 2.0), Entity::isAlive)) {
         if (!villager.isBaby() && (opts.includeTraders() || villager instanceof Villager)) {
            ItemStack hand = villager.getMainHandItem();
            VillagerDupeDriver.TaskQueue track = this.villagerTracks.get(villager.getId());
            if (track == null) {
               track = new VillagerDupeDriver.TaskQueue(hand);
               this.villagerTracks.put(villager.getId(), track);
            } else if (hand != track.lastHand) {
               track.lastHand = hand;
               boolean sinceFlick = this.flickActive || ticksSince(now, this.flickStartTick) <= answerWindowTicks(mc);
               if (hand.isEmpty()) {
                  if (sinceFlick) {
                     track.handCleared = true;
                  }
               } else {
                  if (track.lastItem != null && hand.getItem() != track.lastItem) {
                     track.itemChanged = true;
                  }

                  track.lastItem = hand.getItem();
                  if (sinceFlick && track.handCleared) {
                     this.sawHandCleared = true;
                     this.gotHandUpdate = true;
                  }

                  track.misses = 0;
                  if (ticksSince(now, track.askedAt) <= answerWindowTicks(mc)) {
                     track.observedHand = hand;
                     track.observedAt = now;
                  }

                  if (track.awaitingAnswer && (this.flickSlotEmpty || opts.itemFilter().matchesFilter(hand))) {
                     track.awaitingAnswer = false;
                     this.gotHandUpdate = true;
                  }
               }
            }

            track.lastSeenTick = now;
            track.eligible = isEligibleVillager(player, villager, opts.includeTraders());
         }
      }

      this.villagerTracks.values().removeIf(trackx -> ticksSince(now, trackx.lastSeenTick) > 200);
      if (this.awaitingAnswers) {
         boolean waiting = false;

         for (VillagerDupeDriver.TaskQueue track : this.villagerTracks.values()) {
            waiting |= track.awaitingAnswer;
         }

         if (!waiting || ticksSince(now, this.flickStartTick) > answerWindowTicks(mc)) {
            boolean unanswered = false;

            for (VillagerDupeDriver.TaskQueue track : this.villagerTracks.values()) {
               if (track.awaitingAnswer) {
                  unanswered = true;
                  if (++track.misses >= 3) {
                     track.lastAttemptTick = now;
                  }
               }

               track.awaitingAnswer = false;
            }

            this.awaitingAnswers = false;
            this.adaptFlickHold(unanswered && !this.gotHandUpdate);
         }
      }
   }

   private void adaptFlickHold(boolean lost) {
      if (lost) {
         this.flickHoldNanos = Math.min(150000000L, this.flickHoldNanos + 50000000L);
         this.slowHoldStreak = 0;
      } else if (this.flickHoldNanos > 50000000L && ++this.slowHoldStreak >= 8) {
         this.flickHoldNanos -= 50000000L;
         this.slowHoldStreak = 0;
      }
   }

   private void clearObservedHands() {
      for (VillagerDupeDriver.TaskQueue track : this.villagerTracks.values()) {
         track.observedHand = null;
         track.observedAt = -2147483648;
         track.askedAt = -2147483648;
      }
   }

   private boolean init_Z_Minecraft(Minecraft mc) {
      if (this.menuGuardTick == -2147483648 || mc == null || mc.player == null || mc.gui.screen() == null) {
         return false;
      }

      if (!(mc.player.containerMenu instanceof MerchantMenu)) {
         return false;
      }

      if (ticksSince(AutismSessionState.getInstance().getClientTick(), this.menuGuardTick) > answerWindowTicks(mc)) {
         return false;
      }

      mc.player.closeContainer();
      this.closedMenuCount++;
      return true;
   }

   private void onLevelChanged(ClientLevel current) {
      if (current != this.lastLevel) {
         this.lastLevel = current;
         this.villagerTracks.clear();
         this.pendingShearUses.clear();
         this.awaitingAnswers = false;
         this.menuGuardTick = -2147483648;
         this.flickHoldNanos = 50000000L;
         this.slowHoldStreak = 0;
      }
   }

   private boolean ensureActionGroup() {
      if (!this.actionGroupOpen) {
         this.actionGroupOpen = InventorySlotArbiter.beginActionGroup("villager-dupe");
      }

      return this.actionGroupOpen;
   }

   private static int answerWindowTicks(Minecraft mc) {
      return 2 * pingTicks(mc) + 10;
   }

   private static int pingTicks(Minecraft mc) {
      PlayerInfo info = mc.getConnection() != null && mc.player != null ? mc.getConnection().getPlayerInfo(mc.player.getUUID()) : null;
      int ms = info == null ? 0 : Math.max(0, info.getLatency());
      return Math.min(40, (ms + 49) / 50);
   }

   private static int ticksSince(int now, int then) {
      if (then == -2147483648) {
         return 2147483647;
      }

      int delta = now - then;
      return delta < 0 ? 2147483647 : delta;
   }

   private static boolean isClientReady(Minecraft mc) {
      return mc != null && mc.player != null && mc.level != null && mc.gameMode != null && mc.getConnection() != null;
   }

   private static boolean isEmeraldHotbarSlot(Inventory inventory, int slot) {
      return slot >= 0 && slot < 9 && inventory.getItem(slot).is(Items.EMERALD);
   }

   private static int findEmeraldHotbarSlot(Inventory inventory) {
      int selected = inventory.getSelectedSlot();
      if (isEmeraldHotbarSlot(inventory, selected)) {
         return selected;
      }

      for (int slot = 0; slot < 9; slot++) {
         if (isEmeraldHotbarSlot(inventory, slot)) {
            return slot;
         }
      }

      return -1;
   }

   private static int findEmeraldInMainInventory(Inventory inventory) {
      for (int slot = 9; slot < 36; slot++) {
         if (inventory.getItem(slot).is(Items.EMERALD)) {
            return slot;
         }
      }

      return -1;
   }

   private static int findEmptyHotbarSlot(Inventory inventory) {
      for (int slot = 0; slot < 9; slot++) {
         if (inventory.getItem(slot).isEmpty()) {
            return slot;
         }
      }

      return -1;
   }

   private static int nextEmptyHotbarSlot(Inventory inventory) {
      int selected = inventory.getSelectedSlot();

      for (int step = 1; step < 9; step++) {
         int slot = (selected + step) % 9;
         if (inventory.getItem(slot).isEmpty()) {
            return slot;
         }
      }

      for (int step = 1; step < 9; step++) {
         int slot = (selected + step) % 9;
         if (!inventory.getItem(slot).is(Items.EMERALD)) {
            return slot;
         }
      }

      return -1;
   }

   private static boolean hasShears(LocalPlayer player) {
      for (int slot = 0; slot < 36; slot++) {
         if (player.getInventory().getItem(slot).is(Items.SHEARS)) {
            return true;
         }
      }

      return false;
   }

   private static int bestShearsSlot(LocalPlayer player, boolean creative) {
      Inventory inventory = player.getInventory();
      int best = -1;
      int bestUses = 0;

      for (int i = 0; i < 36; i++) {
         int slot = (i + 9) % 36;
         ItemStack stack = inventory.getItem(slot);
         if (stack.is(Items.SHEARS)) {
            int uses = creative ? 2147483647 : remainingShearUses(stack, 0);
            if (uses > bestUses) {
               best = slot;
               bestUses = uses;
            }
         }
      }

      return best;
   }

   public record BufferHelper(int count, VillagerDupeDriver.ConfigStore status) {
   }

   public record CachePool(
      VillagerDupeDriver.ItemFilter itemFilter, boolean includeTraders, boolean load, boolean read, boolean write, boolean update, boolean sync, int init_I
   ) {
      public CachePool {
         if (itemFilter == null) {
            itemFilter = VillagerDupeDriver.ItemFilter.ALL;
         }

         init_I = Math.max(1, init_I);
      }
   }

   public enum ConfigStore {
      SHEARED("", ""),
      WAITING("", ""),
      NO_WORLD("No world", "Not in a world"),
      SCREEN("Screen open", "Screen open"),
      BUSY("Busy", "Hands busy"),
      SPECTATOR("Spectator", "In spectator"),
      SNEAKING("Sneaking", "Stop sneaking to shear"),
      OFFHAND_TAKEN("Offhand busy", "Offhand in use"),
      NO_EMERALD("No emerald", "Need an emerald"),
      EMERALD_NOT_IN_HOTBAR("No emerald", "Emerald not in hotbar"),
      EMERALD_IN_OFFHAND("No emerald", "Emerald in offhand"),
      NO_SHEARS("No shears", "Need shears"),
      SHEARS_NOT_IN_OFFHAND("No shears", "Shears not in offhand"),
      SHEARS_WORN("Shears worn", "Shears worn out"),
      TOTEM("Totem", "Totem in offhand");

      private final String init_Str;
      private final String load_Str;

      ConfigStore(String info, String message) {
         this.init_Str = info;
         this.load_Str = message;
      }

      public String label() {
         return this.init_Str;
      }

      public String detail() {
         return this.load_Str;
      }

      public boolean init_Z() {
         boolean var10000;
         switch (this) {
            case NO_EMERALD:
            case EMERALD_NOT_IN_HOTBAR:
            case EMERALD_IN_OFFHAND:
            case NO_SHEARS:
            case SHEARS_NOT_IN_OFFHAND:
            case SHEARS_WORN:
            case TOTEM:
               var10000 = true;
               break;
            default:
               var10000 = false;
         }

         return var10000;
      }

      public boolean load_Z() {
         return this == SHEARED || this == WAITING;
      }
   }

   public enum ItemFilter {
      ALL("All"),
      SADDLE("Saddles"),
      HORSE_ARMOR("Horse Armor"),
      CARPET("Carpets");

      private static volatile Set<Item> init_Set;
      private final String init_Str;

      ItemFilter(String label) {
         this.init_Str = label;
      }

      public String label() {
         return this.init_Str;
      }

      public boolean matchesFilter(ItemStack stack) {
         if (!VillagerDupeDriver.isShearableEquipment(stack)) {
            return false;
         }

         boolean var10000;
         switch (this) {
            case ALL:
               var10000 = true;
               break;
            case SADDLE:
               var10000 = stack.is(Items.SADDLE);
               break;
            case HORSE_ARMOR:
               var10000 = stack.is(Items.LEATHER_HORSE_ARMOR)
                  || stack.is(Items.COPPER_HORSE_ARMOR)
                  || stack.is(Items.IRON_HORSE_ARMOR)
                  || stack.is(Items.GOLDEN_HORSE_ARMOR)
                  || stack.is(Items.DIAMOND_HORSE_ARMOR)
                  || stack.is(Items.NETHERITE_HORSE_ARMOR);
               break;
            case CARPET:
               var10000 = isCarpetItem(stack.getItem());
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }

      public static VillagerDupeDriver.ItemFilter parse(String token) {
         if (token == null) {
            return ALL;
         }

         String key = token.trim().toLowerCase(Locale.ROOT);
         int colon = key.indexOf(58);
         if (colon >= 0) {
            key = key.substring(colon + 1);
         }

         key = key.replace(' ', '_').replace('-', '_');
         if (key.endsWith("s")) {
            key = key.substring(0, key.length() - 1);
         }

         if (key.equals("saddle")) {
            return SADDLE;
         } else if (!key.equals("horse_armor") && !key.equals("horse_armour") && !key.endsWith("_horse_armor")) {
            return !key.equals("carpet") && !key.endsWith("_carpet") ? ALL : CARPET;
         } else {
            return HORSE_ARMOR;
         }
      }

      private static boolean isCarpetItem(Item item) {
         Set<Item> set = init_Set;
         if (set == null) {
            init_Set = set = Set.copyOf(Items.CARPET.asList());
         }

         return set.contains(item);
      }
   }

   private enum StreamUtil {
      SHEAR,
      HOLD,
      WATCH;
   }

   private static final class TaskQueue {
      ItemStack lastHand;
      int lastSeenTick;
      boolean eligible;
      ItemStack observedHand;
      int observedAt = -2147483648;
      int askedAt = -2147483648;
      int misses;
      int lastAttemptTick = -2147483648;
      boolean awaitingAnswer;
      Item lastItem;
      boolean itemChanged;
      boolean handCleared;

      TaskQueue(ItemStack hand) {
         this.lastHand = hand;
         if (!hand.isEmpty()) {
            this.lastItem = hand.getItem();
         }
      }
   }
}
