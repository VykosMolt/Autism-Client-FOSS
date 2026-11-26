package autismclient.util.i;

import autismclient.AutismClientAddon;
import autismclient.compat263.Compat263;
import autismclient.modules.AutoTotemModule;
import autismclient.modules.aD;
import autismclient.util.ah;
import autismclient.util.aw;
import autismclient.util.ck;
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
   public static final String init_Str = "villager-dupe";
   private static final VillagerDupeDriver init_DataUtil = new VillagerDupeDriver();
   private static final List<Predicate<Minecraft>> init_List = new CopyOnWriteArrayList<>();
   private static final int init_I = -2147483648;
   private static final int load_I = 200;
   private static final int read_I = 600;
   private static final int write_I = 3;
   private static final int update_I = 100;
   private static final long init_J = 50000000L;
   private static final long load_J = 150000000L;
   private static final int sync_I = 8;
   private final Map<Integer, VillagerDupeDriver.TaskQueue> init_Map = new HashMap<>();
   private final ArrayDeque<int[]> init_ArrayDeque = new ArrayDeque<>();
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private int flush;
   private ClientLevel init_ClientLevel;
   private boolean init_Z;
   private int close = -1;
   private boolean load_Z;
   private boolean read_Z;
   private int open = -2147483648;
   private int poll;
   private boolean write_Z;
   private long read_J;
   private long write_J = 50000000L;
   private boolean update_Z;
   private int push;
   private int pop = -2147483648;
   private int tick = -2147483648;
   private int run = -2147483648;
   private boolean sync_Z;
   private int get;

   public static VillagerDupeDriver init_DataUtil() {
      return init_DataUtil;
   }

   public VillagerDupeDriver.BufferHelper init_DataUtil_BufferHel_Minecraft_DataUtil_CachePool(Minecraft mc, VillagerDupeDriver.CachePool opts) {
      return this.init_DataUtil_BufferHel_Minecraft(mc, opts, VillagerDupeDriver.StreamUtil.SHEAR);
   }

   public VillagerDupeDriver.BufferHelper load(Minecraft mc, VillagerDupeDriver.CachePool opts) {
      return this.init_DataUtil_BufferHel_Minecraft(mc, opts, VillagerDupeDriver.StreamUtil.HOLD);
   }

   public VillagerDupeDriver.BufferHelper read(Minecraft mc, VillagerDupeDriver.CachePool opts) {
      return this.init_DataUtil_BufferHel_Minecraft(mc, opts, VillagerDupeDriver.StreamUtil.WATCH);
   }

   public boolean init_Z() {
      return this.init_Z;
   }

   public int init_I() {
      return this.get;
   }

   public static void init_V_Predicate(Predicate<Minecraft> body) {
      init_List.add(body);
   }

   public static void init_V_Minecraft(Minecraft mc) {
      if (!init_List.isEmpty() && !AutoTotemModule.init_Z_6()) {
         for (Predicate<Minecraft> driver : init_List) {
            boolean keep = false;

            try {
               keep = driver.test(mc);
            } catch (Throwable t) {
               AutismClientAddon.LOG.debug("[Autism] Villager dupe driver failed", t);
            }

            if (!keep) {
               init_List.remove(driver);
            }
         }
      }
   }

   public static void load_V_Minecraft(Minecraft mc) {
      init_DataUtil.init_Z_Minecraft(mc);
   }

   public void read_V_Minecraft(Minecraft mc) {
      if (this.init_Z && mc != null && mc.player != null && this.close >= 0 && this.close < 9 && !aD.read_Z()) {
         mc.player.getInventory().setSelectedSlot(this.close);
      }

      this.init_Z = false;
      this.close = -1;
      this.init_V();
   }

   public VillagerDupeDriver.ConfigStore init_DataUtil_ConfigSto_Minecraft_DataUtil_CachePool(Minecraft mc, VillagerDupeDriver.CachePool opts) {
      if (!load_Z_Minecraft(mc)) {
         return VillagerDupeDriver.ConfigStore.NO_WORLD;
      }

      LocalPlayer player = mc.player;
      VillagerDupeDriver.ConfigStore emerald = this.init_DataUtil_ConfigSto_LocalPlayer_DataUtil_CachePool(player, opts);
      return emerald != null ? emerald : this.init(mc, player, opts, player.hasInfiniteMaterials(), ck.init_ck().state_I());
   }

   public static boolean init_Z_Minecraft(Minecraft mc, VillagerDupeDriver.Inner_DataUtil filter, boolean traders) {
      if (!load_Z_Minecraft(mc)) {
         return false;
      }

      LocalPlayer player = mc.player;
      double reach = Math.max(0.0, player.entityInteractionRange());
      if (reach <= 0.0) {
         return false;
      }

      VillagerDupeDriver.Inner_DataUtil wanted = filter == null ? VillagerDupeDriver.Inner_DataUtil.ALL : filter;

      for (AbstractVillager villager : mc.level.getEntitiesOfClass(AbstractVillager.class, player.getBoundingBox().inflate(reach), Entity::isAlive)) {
         if (init_Z_LocalPlayer(player, villager, traders) && wanted.init_Z_ItemStack(villager.getMainHandItem())) {
            return true;
         }
      }

      return false;
   }

   public static boolean init_Z_ItemStack(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
         return equippable != null && equippable.canBeSheared() && !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
      } else {
         return false;
      }
   }

   private VillagerDupeDriver.BufferHelper init_DataUtil_BufferHel_Minecraft(Minecraft mc, VillagerDupeDriver.CachePool opts, VillagerDupeDriver.StreamUtil mode) {
      if (!load_Z_Minecraft(mc)) {
         return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.NO_WORLD);
      }

      LocalPlayer player = mc.player;
      int now = ck.init_ck().state_I();
      this.init_V_ClientLevel(mc.level);
      this.load(mc, player, opts, now);
      if (mc.gui.screen() != null) {
         if (!this.init_Z_Minecraft(mc) && player.containerMenu instanceof MerchantMenu) {
            this.load_V();
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
            this.sync_Z = false;

            try {
               return this.init(mc, player, opts, now, mode);
            } finally {
               if (this.sync_Z) {
                  ah.read_V("villager-dupe");
               }

               this.sync_Z = false;
            }
         } else {
            return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.BUSY);
         }
      }
   }

   private VillagerDupeDriver.BufferHelper init(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts, int now, VillagerDupeDriver.StreamUtil mode) {
      this.init_V_LocalPlayer(player);
      VillagerDupeDriver.ConfigStore emerald = this.init_DataUtil_ConfigSto(mc, player, opts, now);
      if (emerald != null) {
         return new VillagerDupeDriver.BufferHelper(0, emerald);
      }

      if (mode == VillagerDupeDriver.StreamUtil.HOLD) {
         return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.WAITING);
      }

      boolean creative = player.hasInfiniteMaterials();
      VillagerDupeDriver.ConfigStore shears = this.init(mc, player, opts, creative, now);
      List<AbstractVillager> targets = this.init_List_Minecraft(mc, player, opts);
      if (targets.isEmpty()) {
         if (shears == null) {
            this.init_V(mc, player, opts, now);
         }

         return new VillagerDupeDriver.BufferHelper(0, shears == null ? VillagerDupeDriver.ConfigStore.WAITING : shears);
      } else {
         if (player.isShiftKeyDown()) {
            return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.SNEAKING);
         }

         if (shears != null) {
            return new VillagerDupeDriver.BufferHelper(0, shears);
         }

         if (!this.init_Z_I(now) && init_I_I_I(now, this.tick) >= opts.init_I()) {
            targets.removeIf(villagerx -> this.init_Z_Minecraft_3(mc, villagerx, now));
            if (targets.isEmpty()) {
               return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.WAITING);
            }

            VillagerDupeDriver.ConfigStore ready = this.load(mc, player, opts, creative, now);
            if (ready != null) {
               return new VillagerDupeDriver.BufferHelper(0, ready);
            }

            int budget = creative ? 2147483647 : init_I_ItemStack_I(player.getOffhandItem(), this.init_I_Minecraft_I(mc, now));
            int count = Math.min(targets.size(), opts.update() ? Math.min(1, budget) : budget);
            if (count <= 0) {
               return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.SHEARS_WORN);
            }

            if (!this.load_Z()) {
               return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.BUSY);
            }

            boolean swing = false;

            for (int i = 0; i < count; i++) {
               AbstractVillager villager = targets.get(i);
               swing |= init_Z_Minecraft_2(mc, player, villager);
               VillagerDupeDriver.TaskQueue track = this.init_Map.get(villager.getId());
               if (track != null) {
                  track.init_ItemStack = villager.getMainHandItem();
               }
            }

            if (swing) {
               Compat263.swing(player, InteractionHand.OFF_HAND);
            }

            this.tick = now;
            this.run = now;
            if (!creative) {
               if (this.init_ArrayDeque.isEmpty()) {
                  this.load_V_LocalPlayer(player);
               }

               this.init_ArrayDeque.addLast(new int[]{now, count});
            }

            this.init_V(mc, player, opts, now);
            return new VillagerDupeDriver.BufferHelper(count, VillagerDupeDriver.ConfigStore.SHEARED);
         } else {
            return new VillagerDupeDriver.BufferHelper(0, VillagerDupeDriver.ConfigStore.WAITING);
         }
      }
   }

   private VillagerDupeDriver.ConfigStore init_DataUtil_ConfigSto(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts, int now) {
      Inventory inventory = player.getInventory();
      if (player.getMainHandItem().is(Items.EMERALD)) {
         if (this.init_Z) {
            this.init_V_Minecraft(mc, opts, now);
         }

         return null;
      } else {
         if (this.init_Z && System.nanoTime() - this.read_J < this.write_J) {
            return VillagerDupeDriver.ConfigStore.WAITING;
         }

         if (init_I_I_I(now, this.pop) == 0) {
            return VillagerDupeDriver.ConfigStore.BUSY;
         }

         int slot = this.init_Z && init_Z_Inventory_I(inventory, this.close) ? this.close : init_I_Inventory(inventory);
         if (slot < 0) {
            int from = load_I_Inventory(inventory);
            if (from < 0) {
               if (player.getOffhandItem().is(Items.EMERALD)) {
                  return this.init_DataUtil_ConfigSto_Minecraft(mc, player, opts);
               }

               return VillagerDupeDriver.ConfigStore.NO_EMERALD;
            }

            if (!opts.load()) {
               return VillagerDupeDriver.ConfigStore.EMERALD_NOT_IN_HOTBAR;
            }

            int into = read_I_Inventory(inventory);
            if (into < 0) {
               into = inventory.getSelectedSlot();
            }

            if (!this.load_Z() || !aw.init(mc, from, into)) {
               return VillagerDupeDriver.ConfigStore.BUSY;
            }

            slot = into;
         }

         if (inventory.getSelectedSlot() != slot) {
            if (!this.load_Z()) {
               return VillagerDupeDriver.ConfigStore.BUSY;
            }

            aw.init_V(mc, slot);
            if (inventory.getSelectedSlot() != slot) {
               return VillagerDupeDriver.ConfigStore.BUSY;
            }

            this.pop = now;
         }

         if (!player.getMainHandItem().is(Items.EMERALD)) {
            return VillagerDupeDriver.ConfigStore.BUSY;
         }

         if (this.init_Z) {
            this.init_V_Minecraft(mc, opts, now);
         }

         return null;
      }
   }

   private VillagerDupeDriver.ConfigStore init_DataUtil_ConfigSto_Minecraft(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts) {
      if (opts.read() && !ah.load_Z("villager-dupe")) {
         int slot = init_I_LocalPlayer_Z(player, player.hasInfiniteMaterials());
         if (slot < 0) {
            return VillagerDupeDriver.ConfigStore.EMERALD_IN_OFFHAND;
         } else if (this.load_Z() && aw.init_Z(mc, slot)) {
            this.load_V_LocalPlayer(player);
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
      if (player.getMainHandItem().is(Items.EMERALD) || init_I_Inventory(inventory) >= 0) {
         return null;
      }

      if (load_I_Inventory(inventory) >= 0) {
         return opts.load() ? null : VillagerDupeDriver.ConfigStore.EMERALD_NOT_IN_HOTBAR;
      }

      if (!player.getOffhandItem().is(Items.EMERALD)) {
         return VillagerDupeDriver.ConfigStore.NO_EMERALD;
      }

      boolean freed = opts.read() && init_I_LocalPlayer_Z(player, player.hasInfiniteMaterials()) >= 0;
      return freed ? null : VillagerDupeDriver.ConfigStore.EMERALD_IN_OFFHAND;
   }

   private VillagerDupeDriver.ConfigStore init(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts, boolean creative, int now) {
      ItemStack offhand = player.getOffhandItem();
      boolean offhandShears = offhand.is(Items.SHEARS);
      if (!offhandShears || !creative && init_I_ItemStack_I(offhand, this.init_I_Minecraft_I(mc, now)) <= 0) {
         boolean settling = offhandShears && !creative && init_I_ItemStack_I(offhand, 0) > 0;
         if (!opts.read()) {
            if (settling) {
               return VillagerDupeDriver.ConfigStore.BUSY;
            } else if (offhandShears) {
               return VillagerDupeDriver.ConfigStore.SHEARS_WORN;
            } else {
               return init_Z_LocalPlayer(player) ? VillagerDupeDriver.ConfigStore.SHEARS_NOT_IN_OFFHAND : VillagerDupeDriver.ConfigStore.NO_SHEARS;
            }
         } else if (init_I_LocalPlayer_Z(player, creative) < 0) {
            if (settling) {
               return VillagerDupeDriver.ConfigStore.BUSY;
            } else {
               return !offhandShears && !init_Z_LocalPlayer(player) ? VillagerDupeDriver.ConfigStore.NO_SHEARS : VillagerDupeDriver.ConfigStore.SHEARS_WORN;
            }
         } else if (opts.write() && offhand.is(Items.TOTEM_OF_UNDYING)) {
            return VillagerDupeDriver.ConfigStore.TOTEM;
         } else {
            return ah.load_Z("villager-dupe") ? VillagerDupeDriver.ConfigStore.OFFHAND_TAKEN : null;
         }
      } else {
         return null;
      }
   }

   private VillagerDupeDriver.ConfigStore load(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts, boolean creative, int now) {
      VillagerDupeDriver.ConfigStore status = this.init(mc, player, opts, creative, now);
      if (status != null) {
         return status;
      }

      ItemStack offhand = player.getOffhandItem();
      if (!offhand.is(Items.SHEARS) || !creative && init_I_ItemStack_I(offhand, this.init_I_Minecraft_I(mc, now)) <= 0) {
         int slot = init_I_LocalPlayer_Z(player, creative);
         if (slot < 0) {
            return VillagerDupeDriver.ConfigStore.NO_SHEARS;
         } else if (this.load_Z() && aw.init_Z(mc, slot)) {
            this.load_V_LocalPlayer(player);
            return player.getOffhandItem().is(Items.SHEARS) ? null : VillagerDupeDriver.ConfigStore.BUSY;
         } else {
            return VillagerDupeDriver.ConfigStore.BUSY;
         }
      } else {
         return null;
      }
   }

   private static int init_I_ItemStack_I(ItemStack shears, int inflight) {
      return !shears.isDamageableItem() ? 2147483647 : shears.getMaxDamage() - shears.getDamageValue() - 1 - inflight;
   }

   private void init_V_LocalPlayer(LocalPlayer player) {
      ItemStack offhand = player.getOffhandItem();
      if (offhand != this.init_ItemStack) {
         int done = offhand.getDamageValue() - this.flush;
         if (offhand.is(Items.SHEARS) && this.init_ItemStack.is(Items.SHEARS) && done >= 0) {
            this.init_ItemStack = offhand;
            this.flush = offhand.getDamageValue();

            while (done > 0 && !this.init_ArrayDeque.isEmpty()) {
               int[] entry = this.init_ArrayDeque.peekFirst();
               int take = Math.min(done, entry[1]);
               entry[1] -= take;
               done -= take;
               if (entry[1] == 0) {
                  this.init_ArrayDeque.pollFirst();
               }
            }
         } else {
            this.load_V_LocalPlayer(player);
         }
      }
   }

   private void load_V_LocalPlayer(LocalPlayer player) {
      this.init_ArrayDeque.clear();
      this.init_ItemStack = player.getOffhandItem();
      this.flush = this.init_ItemStack.getDamageValue();
   }

   private int init_I_Minecraft_I(Minecraft mc, int now) {
      int window = load_I_Minecraft(mc) + 3;
      int sum = 0;
      Iterator<int[]> it = this.init_ArrayDeque.iterator();

      while (it.hasNext()) {
         int[] entry = it.next();
         if (init_I_I_I(now, entry[0]) > window) {
            it.remove();
         } else {
            sum += entry[1];
         }
      }

      return sum;
   }

   private List<AbstractVillager> init_List_Minecraft(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts) {
      List<AbstractVillager> out = new ArrayList<>();
      double reach = Math.max(0.0, player.entityInteractionRange());
      if (reach <= 0.0) {
         return out;
      }

      for (AbstractVillager villager : mc.level.getEntitiesOfClass(AbstractVillager.class, player.getBoundingBox().inflate(reach), Entity::isAlive)) {
         if (init_Z_LocalPlayer(player, villager, opts.init_Z()) && opts.init_DataUtil_DataUtil().init_Z_ItemStack(villager.getMainHandItem())) {
            out.add(villager);
         }
      }

      out.sort(Comparator.comparingDouble(villagerx -> villagerx.distanceToSqr(player)));
      return out;
   }

   private static boolean init_Z_LocalPlayer(LocalPlayer player, AbstractVillager villager, boolean traders) {
      return villager.isAlive()
         && !villager.isRemoved()
         && !villager.isBaby()
         && !villager.isVehicle()
         && (traders || villager instanceof Villager)
         && player.isWithinEntityInteractionRange(villager, 0.0);
   }

   private static boolean init_Z_Minecraft_2(Minecraft mc, LocalPlayer player, AbstractVillager villager) {
      AABB box = villager.getBoundingBox();
      Vec3 eye = player.getEyePosition();
      Vec3 hit = new Vec3(Math.clamp(eye.x, box.minX, box.maxX), Math.clamp(eye.y, box.minY, box.maxY), Math.clamp(eye.z, box.minZ, box.maxZ));
      return mc.gameMode.interact(player, villager, new EntityHitResult(villager, hit), InteractionHand.OFF_HAND) instanceof Success success
         && success.swingSource() == SwingSource.PREDICTED;
   }

   private void init_V(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts, int now) {
      if (opts.sync()
         && !this.init_Z
         && !this.read_Z
         && init_I_I_I(now, this.pop) != 0
         && player.getMainHandItem().is(Items.EMERALD)
         && !player.isShiftKeyDown()
         && this.init_List_Minecraft(mc, player, opts).isEmpty()
         && this.init_Z_DataUtil_CachePool_I(opts, now)) {
         Inventory inventory = player.getInventory();
         int away = write(inventory);
         if (away >= 0 && this.load_Z()) {
            int from = inventory.getSelectedSlot();
            boolean empty = inventory.getItem(away).isEmpty();
            aw.init_V(mc, away);
            if (inventory.getSelectedSlot() == away) {
               this.pop = now;
               this.close = from;
               this.load_Z = empty;
               this.init_Z = true;
               this.write_Z = false;
               this.read_J = System.nanoTime();
               this.update_Z = false;

               for (VillagerDupeDriver.TaskQueue track : this.init_Map.values()) {
                  track.write_Z = false;
               }
            }
         }
      }
   }

   private boolean init_Z_DataUtil_CachePool_I(VillagerDupeDriver.CachePool opts, int now) {
      for (VillagerDupeDriver.TaskQueue track : this.init_Map.values()) {
         if (track.init_I == now
            && track.init_Z
            && init_Z_DataUtil_TaskQueue_I(track, now)
            && (track.load_ItemStack == null || opts.init_DataUtil_DataUtil().init_Z_ItemStack(track.load_ItemStack) || init_I_I_I(now, track.load_I) > 600)) {
            return true;
         }
      }

      return false;
   }

   private static boolean init_Z_DataUtil_TaskQueue_I(VillagerDupeDriver.TaskQueue track, int now) {
      return track.write_I < 3 || init_I_I_I(now, track.update) >= 100;
   }

   private void init_V_Minecraft(Minecraft mc, VillagerDupeDriver.CachePool opts, int now) {
      this.init_Z = false;
      this.close = -1;
      this.poll = load_I_Minecraft(mc) + 1;
      this.read_Z = false;

      for (VillagerDupeDriver.TaskQueue track : this.init_Map.values()) {
         boolean asked = track.init_I == now && track.init_Z && init_Z_DataUtil_TaskQueue_I(track, now);
         if (asked) {
            track.read_I = now;
         }

         track.load_Z = asked && (this.load_Z || track.load_ItemStack == null || opts.init_DataUtil_DataUtil().init_Z_ItemStack(track.load_ItemStack));
         this.read_Z = this.read_Z | track.load_Z;
      }

      this.open = now;
   }

   private boolean init_Z_I(int now) {
      return this.init_Z || init_I_I_I(now, this.open) < this.poll;
   }

   private boolean init_Z_Minecraft_3(Minecraft mc, AbstractVillager villager, int now) {
      if (this.init_Z || !this.write_Z && init_I_I_I(now, this.open) <= init_I_Minecraft(mc)) {
         VillagerDupeDriver.TaskQueue track = this.init_Map.get(villager.getId());
         return track != null && !track.read_Z && track.load_ItemStack != null ? !ItemStack.isSameItem(villager.getMainHandItem(), track.load_ItemStack) : true;
      } else {
         return false;
      }
   }

   private void init_V() {
      this.read_Z = false;

      for (VillagerDupeDriver.TaskQueue track : this.init_Map.values()) {
         track.load_Z = false;
      }
   }

   private void load(Minecraft mc, LocalPlayer player, VillagerDupeDriver.CachePool opts, int now) {
      double reach = Math.max(0.0, player.entityInteractionRange());

      for (AbstractVillager villager : mc.level.getEntitiesOfClass(AbstractVillager.class, player.getBoundingBox().inflate(reach + 2.0), Entity::isAlive)) {
         if (!villager.isBaby() && (opts.init_Z() || villager instanceof Villager)) {
            ItemStack hand = villager.getMainHandItem();
            VillagerDupeDriver.TaskQueue track = this.init_Map.get(villager.getId());
            if (track == null) {
               track = new VillagerDupeDriver.TaskQueue(hand);
               this.init_Map.put(villager.getId(), track);
            } else if (hand != track.init_ItemStack) {
               track.init_ItemStack = hand;
               boolean sinceFlick = this.init_Z || init_I_I_I(now, this.open) <= init_I_Minecraft(mc);
               if (hand.isEmpty()) {
                  if (sinceFlick) {
                     track.write_Z = true;
                  }
               } else {
                  if (track.init_Item != null && hand.getItem() != track.init_Item) {
                     track.read_Z = true;
                  }

                  track.init_Item = hand.getItem();
                  if (sinceFlick && track.write_Z) {
                     this.write_Z = true;
                     this.update_Z = true;
                  }

                  track.write_I = 0;
                  if (init_I_I_I(now, track.read_I) <= init_I_Minecraft(mc)) {
                     track.load_ItemStack = hand;
                     track.load_I = now;
                  }

                  if (track.load_Z && (this.load_Z || opts.init_DataUtil_DataUtil().init_Z_ItemStack(hand))) {
                     track.load_Z = false;
                     this.update_Z = true;
                  }
               }
            }

            track.init_I = now;
            track.init_Z = init_Z_LocalPlayer(player, villager, opts.init_Z());
         }
      }

      this.init_Map.values().removeIf(trackx -> init_I_I_I(now, trackx.init_I) > 200);
      if (this.read_Z) {
         boolean waiting = false;

         for (VillagerDupeDriver.TaskQueue track : this.init_Map.values()) {
            waiting |= track.load_Z;
         }

         if (!waiting || init_I_I_I(now, this.open) > init_I_Minecraft(mc)) {
            boolean unanswered = false;

            for (VillagerDupeDriver.TaskQueue track : this.init_Map.values()) {
               if (track.load_Z) {
                  unanswered = true;
                  if (++track.write_I >= 3) {
                     track.update = now;
                  }
               }

               track.load_Z = false;
            }

            this.read_Z = false;
            this.init_V_Z(unanswered && !this.update_Z);
         }
      }
   }

   private void init_V_Z(boolean lost) {
      if (lost) {
         this.write_J = Math.min(150000000L, this.write_J + 50000000L);
         this.push = 0;
      } else if (this.write_J > 50000000L && ++this.push >= 8) {
         this.write_J -= 50000000L;
         this.push = 0;
      }
   }

   private void load_V() {
      for (VillagerDupeDriver.TaskQueue track : this.init_Map.values()) {
         track.load_ItemStack = null;
         track.load_I = -2147483648;
         track.read_I = -2147483648;
      }
   }

   private boolean init_Z_Minecraft(Minecraft mc) {
      if (this.run == -2147483648 || mc == null || mc.player == null || mc.gui.screen() == null) {
         return false;
      }

      if (!(mc.player.containerMenu instanceof MerchantMenu)) {
         return false;
      }

      if (init_I_I_I(ck.init_ck().state_I(), this.run) > init_I_Minecraft(mc)) {
         return false;
      }

      mc.player.closeContainer();
      this.get++;
      return true;
   }

   private void init_V_ClientLevel(ClientLevel current) {
      if (current != this.init_ClientLevel) {
         this.init_ClientLevel = current;
         this.init_Map.clear();
         this.init_ArrayDeque.clear();
         this.read_Z = false;
         this.run = -2147483648;
         this.write_J = 50000000L;
         this.push = 0;
      }
   }

   private boolean load_Z() {
      if (!this.sync_Z) {
         this.sync_Z = ah.write_Z("villager-dupe");
      }

      return this.sync_Z;
   }

   private static int init_I_Minecraft(Minecraft mc) {
      return 2 * load_I_Minecraft(mc) + 10;
   }

   private static int load_I_Minecraft(Minecraft mc) {
      PlayerInfo info = mc.getConnection() != null && mc.player != null ? mc.getConnection().getPlayerInfo(mc.player.getUUID()) : null;
      int ms = info == null ? 0 : Math.max(0, info.getLatency());
      return Math.min(40, (ms + 49) / 50);
   }

   private static int init_I_I_I(int now, int then) {
      if (then == -2147483648) {
         return 2147483647;
      }

      int delta = now - then;
      return delta < 0 ? 2147483647 : delta;
   }

   private static boolean load_Z_Minecraft(Minecraft mc) {
      return mc != null && mc.player != null && mc.level != null && mc.gameMode != null && mc.getConnection() != null;
   }

   private static boolean init_Z_Inventory_I(Inventory inventory, int slot) {
      return slot >= 0 && slot < 9 && inventory.getItem(slot).is(Items.EMERALD);
   }

   private static int init_I_Inventory(Inventory inventory) {
      int selected = inventory.getSelectedSlot();
      if (init_Z_Inventory_I(inventory, selected)) {
         return selected;
      }

      for (int slot = 0; slot < 9; slot++) {
         if (init_Z_Inventory_I(inventory, slot)) {
            return slot;
         }
      }

      return -1;
   }

   private static int load_I_Inventory(Inventory inventory) {
      for (int slot = 9; slot < 36; slot++) {
         if (inventory.getItem(slot).is(Items.EMERALD)) {
            return slot;
         }
      }

      return -1;
   }

   private static int read_I_Inventory(Inventory inventory) {
      for (int slot = 0; slot < 9; slot++) {
         if (inventory.getItem(slot).isEmpty()) {
            return slot;
         }
      }

      return -1;
   }

   private static int write(Inventory inventory) {
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

   private static boolean init_Z_LocalPlayer(LocalPlayer player) {
      for (int slot = 0; slot < 36; slot++) {
         if (player.getInventory().getItem(slot).is(Items.SHEARS)) {
            return true;
         }
      }

      return false;
   }

   private static int init_I_LocalPlayer_Z(LocalPlayer player, boolean creative) {
      Inventory inventory = player.getInventory();
      int best = -1;
      int bestUses = 0;

      for (int i = 0; i < 36; i++) {
         int slot = (i + 9) % 36;
         ItemStack stack = inventory.getItem(slot);
         if (stack.is(Items.SHEARS)) {
            int uses = creative ? 2147483647 : init_I_ItemStack_I(stack, 0);
            if (uses > bestUses) {
               best = slot;
               bestUses = uses;
            }
         }
      }

      return best;
   }

   public record BufferHelper(int init_I, VillagerDupeDriver.ConfigStore init_DataUtil_ConfigSto) {
   }

   public record CachePool(
      VillagerDupeDriver.Inner_DataUtil init_DataUtil_DataUtil, boolean init_Z, boolean load, boolean read, boolean write, boolean update, boolean sync, int init_I
   ) {
      public CachePool {
         if (init_DataUtil_DataUtil == null) {
            init_DataUtil_DataUtil = VillagerDupeDriver.Inner_DataUtil.ALL;
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

      public String init_Str() {
         return this.init_Str;
      }

      public String load_Str() {
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

   public enum Inner_DataUtil {
      ALL("All"),
      SADDLE("Saddles"),
      HORSE_ARMOR("Horse Armor"),
      CARPET("Carpets");

      private static volatile Set<Item> init_Set;
      private final String init_Str;

      Inner_DataUtil(String label) {
         this.init_Str = label;
      }

      public String init() {
         return this.init_Str;
      }

      public boolean init_Z_ItemStack(ItemStack stack) {
         if (!VillagerDupeDriver.init_Z_ItemStack(stack)) {
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
               var10000 = init_Z_Item(stack.getItem());
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }

      public static VillagerDupeDriver.Inner_DataUtil init_DataUtil_DataUtil_Str(String token) {
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

      private static boolean init_Z_Item(Item item) {
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
      ItemStack init_ItemStack;
      int init_I;
      boolean init_Z;
      ItemStack load_ItemStack;
      int load_I = -2147483648;
      int read_I = -2147483648;
      int write_I;
      int update = -2147483648;
      boolean load_Z;
      Item init_Item;
      boolean read_Z;
      boolean write_Z;

      TaskQueue(ItemStack hand) {
         this.init_ItemStack = hand;
         if (!hand.isEmpty()) {
            this.init_Item = hand.getItem();
         }
      }
   }
}
