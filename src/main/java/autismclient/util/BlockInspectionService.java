package autismclient.util;

import autismclient.compat263.CompatGLFW;
import autismclient.modules.Module;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

public final class BlockInspectionService {
   static final String FORMAT_ID = "autism:block_data/v1";
   private static final long PENDING_TIMEOUT_NANOS = 1500000000L;
   private static boolean init_Z;
   private static BlockInspectionService.Inner_DataUtil pendingInspection;

   private BlockInspectionService() {
   }

   public static boolean init() {
      return init_Z || pendingInspection != null;
   }

   public static void tick(Minecraft mc) {
      if (mc == null || mc.options == null || !mc.options.keyUse.isDown() || !inspectKeysDown(mc)) {
         init_Z = false;
      }

      BlockInspectionService.Inner_DataUtil pending = pendingInspection;
      if (pending != null && mc != null) {
         if (mc.player != null) {
            AbstractContainerMenu menu = mc.player.containerMenu;
            if (menu != null && menu != mc.player.inventoryMenu && menu.containerId != pending.containerId()) {
               pendingInspection = null;
               List<ItemStack> items = containerItems(menu, mc.player.getInventory());
               String title = mc.gui.screen() == null ? "" : mc.gui.screen().getTitle().getString();
               CompoundTag complete = withItems(mc, pending.init_CompoundTag(), items, title, String.valueOf(BuiltInRegistries.MENU.getKey(menu.getType())));
               mc.player.closeContainer();
               openViewer(mc, complete, items, "Server-synchronized contents.");
               return;
            }
         }

         if (System.nanoTime() - pending.load() >= 1500000000L) {
            pendingInspection = null;
            openViewer(mc, pending.init_CompoundTag(), List.of(), "Contents were not provided.");
         }
      }
   }

   public static boolean tryInspect(Minecraft mc) {
      if (!tooltipsEnabled()) {
         return false;
      }

      if (!inspectKeysDown(mc) || !(mc.hitResult instanceof BlockHitResult hit && hit.getType() == Type.BLOCK)) {
         return false;
      }

      if (init_Z) {
         return true;
      }

      init_Z = true;
      if (mc.level == null) {
         bi.write("Block data unavailable.");
         return true;
      }

      BlockPos pos = hit.getBlockPos().immutable();
      BlockState state = mc.level.getBlockState(pos);
      BlockEntity blockEntity = mc.level.getBlockEntity(pos);
      CompoundTag localEntity = null;

      try {
         if (blockEntity != null) {
            localEntity = blockEntity.saveWithFullMetadata(mc.level.registryAccess());
         }

         BlockInspectionService.Inner_StreamUtil snapshot = new BlockInspectionService.Inner_StreamUtil(
            mc.level.dimension().identifier().toString(), pos, BlockStateParser.serialize(state), encodeState(state)
         );
         CompoundTag localPayload = buildPayload(snapshot, localEntity, "client");
         if (blockEntity != null
            && mc.player != null
            && mc.player.connection != null
            && mc.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            openViewer(mc, localPayload, List.of(), "Requesting server data.");
            CompoundTag localCopy = localEntity == null ? null : localEntity.copy();
            mc.player.connection.getDebugQueryHandler().queryBlockEntityTag(pos, serverEntity -> {
               if (serverEntity != null) {
                  CompoundTag authoritative = mergeServer(localCopy, serverEntity, pos);
                  CompoundTag serverPayload = buildPayload(snapshot, authoritative, "server");
                  openViewer(mc, serverPayload, List.of(), "Authoritative server data.");
               }
            });
            return true;
         }

         if (hasContainer(mc, pos, state, blockEntity)) {
            int containerId = mc.player != null && mc.player.containerMenu != null ? mc.player.containerMenu.containerId : -1;
            pendingInspection = new BlockInspectionService.Inner_DataUtil(localPayload, false, -1L, containerId, System.nanoTime());
            bi.init("Reading block contents...", UiColorPalette.cyan());
            return false;
         }

         openViewer(mc, localPayload, List.of(), blockEntity == null ? "No block entity data." : "Client-synchronized data.");
      } catch (Throwable error) {
         bi.write("Block inspection failed.");
      }

      return true;
   }

   static boolean isInspectChord(boolean control, boolean shift) {
      return control && shift;
   }

   private static boolean tooltipsEnabled() {
      Module module = autismclient.modules.al.init_Module_Str("better-tooltips");
      return module != null && module.isEnabled() && Boolean.parseBoolean(module.value("block-nbt-inspect"));
   }

   static CompoundTag buildPayload(BlockInspectionService.Inner_StreamUtil snapshot, CompoundTag blockEntity, String source) {
      CompoundTag root = new CompoundTag();
      root.putString("format", "autism:block_data/v1");
      root.putString("source", source == null ? "client" : source);
      root.putString("dimension", snapshot.dimension());
      root.putIntArray("position", new int[]{snapshot.pos().getX(), snapshot.pos().getY(), snapshot.pos().getZ()});
      root.putString("block_state", snapshot.load());
      root.put("state", snapshot.state().copy());
      if (blockEntity != null) {
         root.put("block_entity", blockEntity.copy());
      }

      return root;
   }

   static CompoundTag mergeServer(CompoundTag local, CompoundTag server, BlockPos pos) {
      CompoundTag merged = local == null ? new CompoundTag() : local.copy();
      if (server != null) {
         merged.merge(server);
      }

      merged.putInt("x", pos.getX());
      merged.putInt("y", pos.getY());
      merged.putInt("z", pos.getZ());
      return merged;
   }

   private static CompoundTag encodeState(BlockState state) {
      CompoundTag encoded = new CompoundTag();
      String id = state.typeHolder()
         .unwrapKey()
         .map(key -> key.identifier().toString())
         .orElseGet(() -> BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
      encoded.putString("Name", id);
      if (!state.isSingletonState()) {
         CompoundTag properties = new CompoundTag();
         state.getValues().forEach(value -> properties.putString(value.property().getName(), value.valueName()));
         if (!properties.isEmpty()) {
            encoded.put("Properties", properties);
         }
      }

      return encoded;
   }

   private static boolean hasContainer(Minecraft mc, BlockPos pos, BlockState state, BlockEntity blockEntity) {
      if (blockEntity instanceof Container) {
         return true;
      }

      try {
         return state.getMenuProvider(mc.level, pos) != null;
      } catch (Throwable ignored) {
         return false;
      }
   }

   private static List<ItemStack> containerItems(AbstractContainerMenu menu, Container playerInventory) {
      if (menu == null) {
         return List.of();
      }

      int count = Math.max(0, menu.slots.size() - 36);
      List<ItemStack> items = new ArrayList<>(count);

      for (int i = 0; i < count; i++) {
         Slot slot = menu.slots.get(i);
         if (slot != null && slot.container == playerInventory) {
            break;
         }

         ItemStack stack = slot == null ? ItemStack.EMPTY : slot.getItem();
         items.add(stack == null ? ItemStack.EMPTY : stack.copy());
      }

      return List.copyOf(items);
   }

   static CompoundTag withItems(Minecraft mc, CompoundTag source, List<ItemStack> items, String title, String menuType) {
      ListTag encodedItems = new ListTag();
      if (mc != null && mc.level != null && items != null) {
         for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack != null && !stack.isEmpty()) {
               try {
                  Tag encoded = ItemStack.CODEC.encodeStart(mc.level.registryAccess().createSerializationContext(NbtOps.INSTANCE), stack).result().orElse(null);
                  if (encoded instanceof CompoundTag item) {
                     CompoundTag withSlot = item.copy();
                     withSlot.putByte("Slot", (byte)slot);
                     encodedItems.add(withSlot);
                  }
               } catch (Throwable var11) {
               }
            }
         }
      }

      return withEncodedItems(source, encodedItems, items == null ? 0 : items.size(), title, menuType);
   }

   static CompoundTag withEncodedItems(CompoundTag source, ListTag encodedItems, int slotCount, String title, String menuType) {
      CompoundTag root = source == null ? new CompoundTag() : source.copy();
      CompoundTag entity = root.getCompound("block_entity").orElse(new CompoundTag());
      entity.put("Items", encodedItems == null ? new ListTag() : encodedItems.copy());
      root.put("block_entity", entity);
      root.putString("source", "container");
      root.putBoolean("contents_available", true);
      root.putInt("container_slots", Math.max(0, slotCount));
      if (title != null && !title.isBlank()) {
         root.putString("container_title", title);
      }

      if (menuType != null && !menuType.isBlank() && !"null".equals(menuType)) {
         root.putString("container_type", menuType);
      }

      return root;
   }

   private static void openViewer(Minecraft mc, CompoundTag root, List<ItemStack> items, String note) {
      if (mc != null) {
         Runnable open = () -> {
            CompoundTag displayRoot = root == null ? new CompoundTag() : root.copy();
            List<ItemStack> displayItems = items == null ? List.of() : items;
            CompoundTag entity = displayRoot.getCompound("block_entity").orElse(null);
            if (displayItems.isEmpty() && entity != null && entity.contains("Items")) {
               displayItems = decodeItems(mc, entity);
               displayRoot.putBoolean("contents_available", true);
               if (!displayRoot.contains("container_slots")) {
                  displayRoot.putInt("container_slots", displayItems.size());
               }
            }

            BlockNbtFormatter.Inner_DataUtil inspection = BlockNbtFormatter.init(displayRoot, displayItems, note);
            if (!AutismItemNbtInspectOverlay.inspectSource(inspection)) {
               bi.write("NBT overlay unavailable.");
            } else {
               AutismItemNbtInspectOverlay overlay = AutismItemNbtInspectOverlay.getOrCreate(mc.font);
               if (!(mc.gui.screen() instanceof autismclient.gui.screens.AutismOverlaysScreen host && host.init(overlay))) {
                  mc.gui.setScreen(new autismclient.gui.screens.AutismOverlaysScreen(overlay));
               }
            }
         };
         if (mc.isSameThread()) {
            open.run();
         } else {
            mc.execute(open);
         }
      }
   }

   private static List<ItemStack> decodeItems(Minecraft mc, CompoundTag entity) {
      if (mc != null && mc.level != null && entity != null) {
         List<ItemStack> decoded = new ArrayList<>();

         for (Tag value : entity.getListOrEmpty("Items")) {
            if (value instanceof CompoundTag item) {
               int slot = Byte.toUnsignedInt(item.getByteOr("Slot", (byte)0));
               if (slot <= 255) {
                  ItemStack stack = ItemStack.CODEC
                     .parse(mc.level.registryAccess().createSerializationContext(NbtOps.INSTANCE), item)
                     .result()
                     .orElse(ItemStack.EMPTY);

                  while (decoded.size() <= slot) {
                     decoded.add(ItemStack.EMPTY);
                  }

                  decoded.set(slot, stack.copy());
               }
            }
         }

         return List.copyOf(decoded);
      } else {
         return List.of();
      }
   }

   private static boolean inspectKeysDown(Minecraft mc) {
      if (mc != null && mc.getWindow() != null) {
         long window = mc.getWindow().handle();
         boolean control = CompatGLFW.glfwGetKey(window, 341) == 1 || CompatGLFW.glfwGetKey(window, 345) == 1;
         boolean shift = CompatGLFW.glfwGetKey(window, 340) == 1 || CompatGLFW.glfwGetKey(window, 344) == 1;
         return isInspectChord(control, shift);
      } else {
         return false;
      }
   }

   private record Inner_DataUtil(CompoundTag init_CompoundTag, boolean init_Z, long init_J, int containerId, long load) {
      private Inner_DataUtil {
         init_CompoundTag = init_CompoundTag == null ? new CompoundTag() : init_CompoundTag.copy();
      }
   }

   record Inner_StreamUtil(String dimension, BlockPos pos, String load, CompoundTag state) {
      Inner_StreamUtil {
         dimension = dimension == null ? "" : dimension;
         pos = pos == null ? BlockPos.ZERO : pos.immutable();
         load = load == null ? "minecraft:air" : load;
         state = state == null ? new CompoundTag() : state.copy();
      }
   }
}
