package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.KeybindSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.api.module.StringListSetting;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismConfig;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class R extends Module {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private R.Inner_StreamUtil init_R_StreamUtil;
   private R.Inner_ConfigStore init_R_ConfigStore;
   private boolean init_Z;
   private final Map<String, R.Inner_DataUtil> init_Map = new HashMap<>();
   private static int init_I = -2147483648;
   private static WeakReference<AbstractContainerMenu> init_WeakReference = new WeakReference<>(null);
   private static boolean load;
   private static final Comparator<ItemStack> init_Comparator = Comparator.<ItemStack, String>comparing(
         stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()
      )
      .thenComparing(Comparator.comparingInt(ItemStack::getCount).reversed())
      .thenComparing(Comparator.comparingInt(ItemStack::getDamageValue).reversed());

   R() {
      super("inventory-tweaks", "InventoryTweaks", ae.read, "Inventory sorting, protection, auto-drop, and container steal/dump tools.");
      this.add(new BoolSetting("mouse-drag-item-move", "Shift Drag Move", true).description("Shift-drag transfers hovered stacks.").build());
      this.add(new BoolSetting("uncap-bundle-scrolling", "Uncap Bundle Scroll", true).description("Allow unrestricted bundle scrolling.").build());
      this.add(new BoolSetting("shulker-preview", "Shulker Preview", true).description("Preview container on hover").build());
      this.add(new BoolSetting("map-preview", "Map Preview", true).description("Show map art in tooltips").build());
      this.add(new BoolSetting("sorting-enabled", "Sorting", true).group("Sorting").description("Enable inventory sorting.").build());
      this.add(
         new KeybindSetting("sorting-key", "Sort Key", autismclient.util.ValueSink.init_I(2))
            .group("Sorting")
            .visibleWhen(() -> this.bool("sorting-enabled"))
            .description("Sort key")
            .build()
      );
      this.add(
         new IntSetting("sorting-delay", "Sort Delay", 1, 0, 20, 1)
            .group("Sorting")
            .visibleWhen(() -> this.bool("sorting-enabled"))
            .description("Ticks between sorting moves.")
            .build()
      );
      this.add(
         new BoolSetting("disable-in-creative", "Disable In Creative", true)
            .group("Sorting")
            .visibleWhen(() -> this.bool("sorting-enabled"))
            .description("Skip creative screens")
            .build()
      );
      this.add(RegistryListSetting.items("anti-drop-items", "Anti Drop Items", "").group("Anti Drop").description("Undroppable items").build());
      this.add(new BoolSetting("item-frames", "Frames / Pots", true).group("Anti Drop").description("Block frame/pot placing").build());
      this.add(new KeybindSetting("override-bind", "Override Key", -1).group("Anti Drop").description("Hold to bypass anti-drop.").build());
      this.add(RegistryListSetting.items("auto-drop-items", "Auto Drop Items", "").group("Auto Drop").description("Items dropped automatically.").build());
      this.add(new BoolSetting("exclude-hotbar", "Exclude Hotbar", false).group("Auto Drop").description("Keep hotbar stacks").build());
      this.add(new BoolSetting("only-full-stacks", "Only Full Stacks", false).group("Auto Drop").description("Only drop complete stacks.").build());
      this.add(
         new StringListSetting("steal-screens", "Steal Screens", "minecraft:generic_9x3|minecraft:generic_9x6")
            .group("Steal / Dump")
            .description("Menu ids")
            .build()
      );
      this.add(new BoolSetting("inventory-buttons", "Inventory Buttons", true).group("Steal / Dump").description("Show steal/dump buttons").build());
      this.add(new BoolSetting("steal-drop", "Steal Drop", false).group("Steal / Dump").description("Drop instead of move").build());
      this.add(
         new BoolSetting("drop-backwards", "Drop Backwards", false)
            .group("Steal / Dump")
            .visibleWhen(() -> this.bool("steal-drop"))
            .description("Turn while dropping")
            .build()
      );
      this.add(
         new ChoiceSetting("dump-filter", "Dump Filter", "None", "None", "Whitelist", "Blacklist")
            .group("Steal / Dump")
            .description("Filter dumped player items.")
            .build()
      );
      this.add(
         RegistryListSetting.items("dump-items", "Dump Items", "")
            .group("Steal / Dump")
            .visibleWhen(() -> !"None".equals(this.value("dump-filter")))
            .description("Dump filter items")
            .build()
      );
      this.add(
         new ChoiceSetting("steal-filter", "Steal Filter", "None", "None", "Whitelist", "Blacklist")
            .group("Steal / Dump")
            .description("Filter stolen container items.")
            .build()
      );
      this.add(
         RegistryListSetting.items("steal-items", "Steal Items", "")
            .group("Steal / Dump")
            .visibleWhen(() -> !"None".equals(this.value("steal-filter")))
            .description("Steal filter items")
            .build()
      );
      this.add(new BoolSetting("auto-steal", "Auto Steal", false).group("Auto Steal").description("Automatically steal supported containers.").build());
      this.add(new BoolSetting("auto-dump", "Auto Dump", false).group("Auto Steal").description("Auto-dump supported containers.").build());
      this.add(new IntSetting("delay-ms", "Delay", 0, 0, 1000, 5).group("Auto Steal").description("Move delay (ms)").build());
      this.add(new IntSetting("initial-delay-ms", "Initial Delay", 0, 0, 1000, 5).group("Auto Steal").description("First-move delay (ms)").build());
      this.add(new IntSetting("random-ms", "Random", 0, 0, 1000, 5).group("Auto Steal").description("Random extra delay").build());
      boolean firstInstall = !AutismConfig.getGlobal().modules.containsKey("inventory-tweaks");
      if (firstInstall) {
         this.setEnabledSilently(true);
      }
   }

   public static R init_R() {
      return al.init_Module_Str("inventory-tweaks") instanceof R tweaks ? tweaks : null;
   }

   public static boolean init_Z_AbstractContainerM(AbstractContainerMenu handler) {
      int revision = al.init_I();
      if (revision == init_I && init_WeakReference.get() == handler) {
         return load;
      }

      R module = init_R();
      boolean result = module != null && module.isEnabled() && module.bool("inventory-buttons") && module.load(handler);
      init_I = revision;
      init_WeakReference = new WeakReference<>(handler);
      load = result;
      return result;
   }

   public static void init_V_14() {
      R module = init_R();
      if (module != null && module.isEnabled()) {
         module.init_V_Z_Z(true, false);
      }
   }

   public static void load_V_11() {
      R module = init_R();
      if (module != null && module.isEnabled()) {
         module.init_V_Z_Z(false, false);
      }
   }

   public static boolean init_Z_I_Slot(int button, Slot hoveredSlot) {
      R module = init_R();
      if (module != null && module.isEnabled() && module.bool("sorting-enabled")) {
         int bind = module.integer("sorting-key");
         return autismclient.util.ValueSink.init_Z(bind) && autismclient.util.ValueSink.load_I(bind) == button ? module.init_Z_Slot(hoveredSlot) : false;
      } else {
         return false;
      }
   }

   public static boolean load(int keyCode, Slot hoveredSlot) {
      R module = init_R();
      if (module != null && module.isEnabled() && module.bool("sorting-enabled")) {
         int bind = module.integer("sorting-key");
         return !autismclient.util.ValueSink.init_Z(bind) && bind == keyCode ? module.init_Z_Slot(hoveredSlot) : false;
      } else {
         return false;
      }
   }

   public static boolean init_Z_15() {
      R module = init_R();
      return module != null && module.isEnabled() && module.bool("mouse-drag-item-move");
   }

   public static boolean load_Z_7() {
      R module = init_R();
      return module != null && module.isEnabled() && module.bool("shulker-preview");
   }

   public static boolean read_Z_7() {
      R module = init_R();
      return module != null && module.isEnabled() && module.bool("map-preview");
   }

   public static boolean write_Z_3() {
      R module = init_R();
      return module != null && module.isEnabled() && (module.bool("auto-steal") || module.bool("auto-dump"));
   }

   public static int init_I_ItemStack_I(ItemStack stack, int vanillaLimit) {
      R module = init_R();
      if (module != null && module.isEnabled() && module.bool("uncap-bundle-scrolling")) {
         BundleContents contents = stack == null ? null : stack.get(DataComponents.BUNDLE_CONTENTS);
         return contents == null ? vanillaLimit : Math.max(vanillaLimit, contents.size());
      } else {
         return vanillaLimit;
      }
   }

   public static void init_V_I_5(int containerId) {
      R module = init_R();
      if (module != null && module.isEnabled()) {
         module.load(containerId);
      }
   }

   @Override
   public void onDisable() {
      this.init_R_StreamUtil = null;
      this.init_R_ConfigStore = null;
      this.init_Z = false;
      this.init_Map.clear();
   }

   @Override
   public void tick() {
      this.read_V_7();
      this.write_V_7();
      this.update_V_6();
   }

   @Override
   public boolean onPacketSend(Packet<?> packet) {
      return this.init_Z_Packet(packet);
   }

   @Override
   public boolean onPacketReceive(Packet<?> packet) {
      if (packet instanceof ClientboundContainerSetContentPacket content) {
         this.load(content.containerId());
      }

      if (packet instanceof ClientboundContainerSetSlotPacket slot) {
         this.load(slot.getContainerId());
      }

      return false;
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
      if (hitResult == null || hand == null || init_Minecraft.player == null) {
         return false;
      }

      if (this.bool("item-frames") && !this.update_Z_3()) {
         ItemStack held = init_Minecraft.player.getItemInHand(hand);
         if (!this.init_Z_ItemStack_Str(held, "anti-drop-items")) {
            return false;
         } else if (hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof ItemFrame) {
            return true;
         } else {
            return hitResult instanceof BlockHitResult blockHit && init_Minecraft.level != null
               ? init_Minecraft.level.getBlockState(blockHit.getBlockPos()).is(Blocks.DECORATED_POT)
               : false;
         }
      } else {
         return false;
      }
   }

   private boolean init_Z_Slot(Slot focusedSlot) {
      if (focusedSlot == null || init_Minecraft.player == null || init_Minecraft.gameMode == null) {
         return false;
      }

      if (this.bool("disable-in-creative") && init_Minecraft.player.hasInfiniteMaterials()) {
         return false;
      }

      AbstractContainerMenu handler = init_Minecraft.player.containerMenu;
      if (handler != null && handler.getCarried().isEmpty()) {
         List<Slot> slots = this.init_List_Slot_AbstractContainerM(focusedSlot, handler);
         if (slots.size() < 2) {
            return true;
         } else {
            List<R.Inner_CachePool> actions = this.init_List_List(slots);
            if (actions.isEmpty()) {
               AutismClientMessaging.sendPrefixed("Inventory Tweaks: already sorted.");
               return true;
            } else {
               this.init_R_StreamUtil = new R.Inner_StreamUtil(handler.containerId, actions, Math.max(0, this.integer("sorting-delay")));
               AutismClientMessaging.sendPrefixed("Inventory Tweaks: sorting " + slots.size() + " slots.");
               return true;
            }
         }
      } else {
         AutismClientMessaging.sendPrefixed("Inventory Tweaks: clear your cursor before sorting.");
         return true;
      }
   }

   private void read_V_7() {
      if (this.init_R_StreamUtil != null && !this.init_R_StreamUtil.init()) {
         this.init_R_StreamUtil = null;
      }
   }

   private List<Slot> init_List_Slot_AbstractContainerM(Slot focusedSlot, AbstractContainerMenu handler) {
      boolean focusedPlayer = autismclient.util.aw.init_Z(init_Minecraft, focusedSlot);
      List<Slot> slots = new ArrayList<>();

      for (Slot slot : handler.slots) {
         if (slot != null && slot.mayPickup(init_Minecraft.player)) {
            int invSlot = init_I_Slot(slot);
            if (focusedPlayer) {
               if (invSlot >= 9 && invSlot < 36) {
                  slots.add(slot);
               }
            } else if (invSlot < 0 && slot.container == focusedSlot.container) {
               slots.add(slot);
            }
         }
      }

      return slots;
   }

   private List<R.Inner_CachePool> init_List_List(List<Slot> slots) {
      List<ItemStack> simulated = new ArrayList<>();

      for (Slot slot : slots) {
         simulated.add(slot.getItem().copy());
      }

      List<ItemStack> desired = new ArrayList<>();

      for (ItemStack stack : simulated) {
         if (!stack.isEmpty()) {
            desired.add(stack.copy());
         }
      }

      desired.sort(init_Comparator);

      while (desired.size() < simulated.size()) {
         desired.add(ItemStack.EMPTY);
      }

      List<R.Inner_CachePool> actions = new ArrayList<>();

      for (int i = 0; i < simulated.size(); i++) {
         if (!init_Z_ItemStack_ItemStack(simulated.get(i), desired.get(i))) {
            int match = -1;

            for (int j = i + 1; j < simulated.size(); j++) {
               if (init_Z_ItemStack_ItemStack(simulated.get(j), desired.get(i))) {
                  match = j;
                  break;
               }
            }

            if (match >= 0) {
               actions.add(new R.Inner_CachePool(slots.get(i).index, slots.get(match).index));
               ItemStack tmp = simulated.get(i);
               simulated.set(i, simulated.get(match));
               simulated.set(match, tmp);
            }
         }
      }

      return actions;
   }

   private void load(int containerId) {
      if (init_Minecraft.player != null && init_Minecraft.player.containerMenu != null) {
         AbstractContainerMenu handler = init_Minecraft.player.containerMenu;
         if (handler.containerId == containerId && this.load(handler) && this.init_R_ConfigStore == null) {
            if (this.bool("auto-steal") && this.bool("auto-dump") && !this.init_Z) {
               this.init_Z = true;
               this.setValue("auto-dump", "false");
               AutismClientMessaging.sendPrefixed("Inventory Tweaks: Auto Dump disabled because Auto Steal is enabled.");
            }

            if (this.bool("auto-steal")) {
               this.init_V_Z_Z(true, true);
            } else if (this.bool("auto-dump")) {
               this.init_V_Z_Z(false, true);
            }
         }
      }
   }

   private void init_V_Z_Z(boolean steal, boolean automatic) {
      if (init_Minecraft.player != null && init_Minecraft.gameMode != null) {
         AbstractContainerMenu handler = init_Minecraft.player.containerMenu;
         if (!this.load(handler)) {
            if (!automatic) {
               AutismClientMessaging.sendPrefixed("Inventory Tweaks: this menu is not enabled for Steal/Dump.");
            }
         } else {
            List<R.Inner_BufferHelper> clicks = this.init_List_AbstractContainerM_Z(handler, steal);
            if (clicks.isEmpty()) {
               if (!automatic) {
                  AutismClientMessaging.sendPrefixed("Inventory Tweaks: nothing to " + (steal ? "steal." : "dump."));
               }
            } else {
               this.init_R_ConfigStore = new R.Inner_ConfigStore(
                  handler.containerId,
                  clicks,
                  steal,
                  this.bool("drop-backwards"),
                  Math.max(0, this.integer("delay-ms")),
                  Math.max(0, this.integer("initial-delay-ms")),
                  Math.max(0, this.integer("random-ms"))
               );
               if (!automatic) {
                  AutismClientMessaging.sendPrefixed("Inventory Tweaks: " + (steal ? "stealing " : "dumping ") + clicks.size() + " stacks.");
               }
            }
         }
      }
   }

   private List<R.Inner_BufferHelper> init_List_AbstractContainerM_Z(AbstractContainerMenu handler, boolean steal) {
      List<R.Inner_BufferHelper> clicks = new ArrayList<>();

      for (Slot slot : handler.slots) {
         if (slot != null && !slot.getItem().isEmpty()) {
            int invSlot = init_I_Slot(slot);
            if (steal) {
               if (invSlot < 0 && this.init_Z_ItemStack(slot.getItem(), "steal-filter", "steal-items")) {
                  clicks.add(new R.Inner_BufferHelper(slot.index, this.bool("steal-drop")));
               }
            } else if (invSlot >= 0 && invSlot < 36 && this.init_Z_ItemStack(slot.getItem(), "dump-filter", "dump-items")) {
               clicks.add(new R.Inner_BufferHelper(slot.index, false));
            }
         }
      }

      return clicks;
   }

   private void write_V_7() {
      if (this.init_R_ConfigStore != null && !this.init_R_ConfigStore.init()) {
         this.init_R_ConfigStore = null;
      }
   }

   private void update_V_6() {
      if (init_Minecraft.player != null && init_Minecraft.gameMode != null && init_Minecraft.gui.screen() == null && !this.list("auto-drop-items").isEmpty()) {
         int start = this.bool("exclude-hotbar") ? 9 : 0;

         for (int slot = start; slot < 36; slot++) {
            ItemStack stack = init_Minecraft.player.getInventory().getItem(slot);
            if (!stack.isEmpty()
               && this.init_Z_ItemStack_Str(stack, "auto-drop-items")
               && (!this.bool("only-full-stacks") || stack.getCount() >= stack.getMaxStackSize())) {
               autismclient.util.O.load(init_Minecraft, slot, 0);
               return;
            }
         }
      }
   }

   private boolean load(AbstractContainerMenu handler) {
      if (handler != null && init_Minecraft.player != null && handler != init_Minecraft.player.inventoryMenu) {
         Identifier id = this.init_Identifier_AbstractContainerM(handler);
         if (id == null) {
            return false;
         }

         Set<String> allowed = this.init_Set_Str("steal-screens");
         return allowed.contains(id.toString()) || allowed.contains(id.getPath());
      } else {
         return false;
      }
   }

   private Identifier init_Identifier_AbstractContainerM(AbstractContainerMenu handler) {
      try {
         MenuType<?> type = handler.getType();
         return type == null ? null : BuiltInRegistries.MENU.getKey(type);
      } catch (Throwable ignored) {
         return null;
      }
   }

   private boolean init_Z_ItemStack(ItemStack stack, String modeOption, String listOption) {
      String mode = this.choice(modeOption);
      if ("None".equals(mode)) {
         return true;
      }

      boolean listed = this.init_Z_ItemStack_Str(stack, listOption);
      return "Whitelist".equals(mode) ? listed : !listed;
   }

   private boolean init_Z_ItemStack_Str(ItemStack stack, String optionId) {
      if (stack != null && !stack.isEmpty()) {
         String full = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase(Locale.ROOT);
         String path = full.startsWith("minecraft:") ? full.substring("minecraft:".length()) : full;
         Set<String> ids = this.init_Set_Str(optionId);
         return ids.contains(full) || ids.contains(path);
      } else {
         return false;
      }
   }

   private Set<String> init_Set_Str(String optionId) {
      List<String> raw = this.list(optionId);
      String source = String.join("|", raw);
      R.Inner_DataUtil cached = this.init_Map.get(optionId);
      if (cached != null && cached.init_Str().equals(source)) {
         return cached.init_Set();
      }

      Set<String> values = new HashSet<>();

      for (String entry : raw) {
         String id = init_Str_Str(entry);
         if (!id.isBlank()) {
            values.add(id);
            if (id.startsWith("minecraft:")) {
               values.add(id.substring("minecraft:".length()));
            } else {
               values.add("minecraft:" + id);
            }
         }
      }

      Set<String> immutable = Set.copyOf(values);
      this.init_Map.put(optionId, new R.Inner_DataUtil(source, immutable));
      return immutable;
   }

   private boolean update_Z_3() {
      int bind = this.integer("override-bind");
      return bind != -1 && autismclient.util.ValueSink.init(init_Minecraft, bind);
   }

   private boolean init_Z_Packet(Packet<?> packet) {
      if (init_Minecraft.player != null && !this.update_Z_3() && !this.list("anti-drop-items").isEmpty()) {
         if (packet instanceof ServerboundPlayerActionPacket action && (action.getAction() == Action.DROP_ITEM || action.getAction() == Action.DROP_ALL_ITEMS)) {
            return this.init_Z_ItemStack_Str(init_Minecraft.player.getMainHandItem(), "anti-drop-items");
         } else if (packet instanceof ServerboundContainerClickPacket click) {
            ContainerInput input = init_Enum_Obj_Class(click, ContainerInput.class);
            if (input != ContainerInput.THROW) {
               return false;
            }

            int slotId = init_I_Obj_StrArr(click, "getSlot", "slot", "slotNum");
            AbstractContainerMenu handler = init_Minecraft.player.containerMenu;
            return handler != null && slotId >= 0 && slotId < handler.slots.size()
               ? this.init_Z_ItemStack_Str(handler.slots.get(slotId).getItem(), "anti-drop-items")
               : false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static int init_I_Slot(Slot slot) {
      if (slot != null && init_Minecraft.player != null && slot.container == init_Minecraft.player.getInventory()) {
         int index = slot.getContainerSlot();
         return index >= 0 && index < init_Minecraft.player.getInventory().getContainerSize() ? index : -1;
      } else {
         return -1;
      }
   }

   private static boolean init_Z_ItemStack_ItemStack(ItemStack a, ItemStack b) {
      if (a != null && !a.isEmpty() || b != null && !b.isEmpty()) {
         return a != null && b != null && !a.isEmpty() && !b.isEmpty()
            ? a.getCount() == b.getCount() && a.getDamageValue() == b.getDamageValue() && ItemStack.isSameItemSameComponents(a, b)
            : false;
      } else {
         return true;
      }
   }

   private static String init_Str_Str(String raw) {
      return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
   }

   private static <T extends Enum<T>> T init_Enum_Obj_Class(Object packet, Class<T> enumClass) {
      for (Method method : packet.getClass().getDeclaredMethods()) {
         if (method.getParameterCount() == 0 && enumClass.isAssignableFrom(method.getReturnType())) {
            try {
               method.setAccessible(true);
               return enumClass.cast(method.invoke(packet));
            } catch (Throwable var8) {
            }
         }
      }

      for (Field field : packet.getClass().getDeclaredFields()) {
         if (enumClass.isAssignableFrom(field.getType())) {
            try {
               field.setAccessible(true);
               return enumClass.cast(field.get(packet));
            } catch (Throwable var7) {
            }
         }
      }

      return null;
   }

   private static int init_I_Obj_StrArr(Object packet, String... names) {
      for (String name : names) {
         try {
            Method method = packet.getClass().getDeclaredMethod(name);
            method.setAccessible(true);
            if (method.invoke(packet) instanceof Number number) {
               return number.intValue();
            }
         } catch (Throwable var10) {
         }

         try {
            Field field = packet.getClass().getDeclaredField(name);
            field.setAccessible(true);
            if (field.get(packet) instanceof Number number) {
               return number.intValue();
            }
         } catch (Throwable var9) {
         }
      }

      return -1;
   }

   private record Inner_BufferHelper(int init_I, boolean init_Z) {
   }

   private record Inner_CachePool(int init, int load) {
   }

   private final class Inner_ConfigStore {
      private final int init_I;
      private final List<R.Inner_BufferHelper> init_List;
      private final boolean init_Z;
      private final boolean load_Z;
      private final int load_I;
      private final int read;
      private int write;
      private long init_J;

      private Inner_ConfigStore(
         int containerId, List<R.Inner_BufferHelper> clicks, boolean steal, boolean dropBackwards, int delayMs, int initialDelayMs, int randomMs
      ) {
         this.init_I = containerId;
         this.init_List = clicks;
         this.init_Z = steal;
         this.load_Z = dropBackwards;
         this.load_I = delayMs;
         this.read = randomMs;
         this.init_J = System.currentTimeMillis() + initialDelayMs;
      }

      private boolean init() {
         if (R.init_Minecraft.player == null || R.init_Minecraft.player.containerMenu == null || R.init_Minecraft.gameMode == null) {
            return false;
         }

         if (R.init_Minecraft.player.containerMenu.containerId != this.init_I) {
            return false;
         }

         if (System.currentTimeMillis() < this.init_J) {
            return true;
         }

         while (this.write < this.init_List.size()) {
            R.Inner_BufferHelper click = this.init_List.get(this.write++);
            if (click.init_I() >= 0 && click.init_I() < R.init_Minecraft.player.containerMenu.slots.size()) {
               Slot slot = R.init_Minecraft.player.containerMenu.slots.get(click.init_I());
               if (slot != null
                  && !slot.getItem().isEmpty()
                  && (!this.init_Z || R.this.init_Z_ItemStack(slot.getItem(), "steal-filter", "steal-items"))
                  && (this.init_Z || R.this.init_Z_ItemStack(slot.getItem(), "dump-filter", "dump-items"))) {
                  if (click.init_Z()) {
                     float oldYaw = R.init_Minecraft.player.getYRot();
                     if (this.load_Z) {
                        R.init_Minecraft.player.setYRot(oldYaw + 180.0F);
                     }

                     try {
                        autismclient.util.av.init(R.init_Minecraft, click.init_I(), 1, ContainerInput.THROW);
                     } finally {
                        if (this.load_Z) {
                           R.init_Minecraft.player.setYRot(oldYaw);
                        }
                     }
                  } else {
                     autismclient.util.av.init(R.init_Minecraft, click.init_I(), 0, ContainerInput.QUICK_MOVE);
                  }

                  if (this.load_I > 0 || this.read > 0) {
                     this.init_J = System.currentTimeMillis() + this.load_I + ThreadLocalRandom.current().nextInt(this.read + 1);
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   private record Inner_DataUtil(String init_Str, Set<String> init_Set) {
   }

   private final class Inner_StreamUtil {
      private final int init_I;
      private final List<R.Inner_CachePool> init_List;
      private final int load;
      private int read;
      private int write;

      private Inner_StreamUtil(int containerId, List<R.Inner_CachePool> actions, int delayTicks) {
         this.init_I = containerId;
         this.init_List = actions;
         this.load = delayTicks;
      }

      private boolean init() {
         if (R.init_Minecraft.player == null
            || R.init_Minecraft.player.containerMenu == null
            || R.init_Minecraft.player.containerMenu.containerId != this.init_I) {
            return false;
         }

         if (this.write > 0) {
            this.write--;
            return true;
         }

         if (this.read >= this.init_List.size()) {
            return false;
         }

         R.Inner_CachePool action = this.init_List.get(this.read++);
         autismclient.util.aw.write(R.init_Minecraft, action.init(), action.load());
         this.write = this.load;
         return this.read < this.init_List.size() || this.write > 0;
      }
   }
}
