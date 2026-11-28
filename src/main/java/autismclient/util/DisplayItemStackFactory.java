package autismclient.util;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public final class DisplayItemStackFactory {
   private DisplayItemStackFactory() {
   }

   public static ItemStack displayStack(Item item) {
      return item != null && item != Items.AIR ? new ItemStack(displayHolder(item)) : ItemStack.EMPTY;
   }

   public static ItemStack forBlock(Block block) {
      return block == null ? ItemStack.EMPTY : displayStack(block.asItem());
   }

   private static Holder<Item> displayHolder(Item item) {
      Identifier model = item.builtInRegistryHolder().key().identifier();
      DataComponentMap components = DataComponentMap.builder()
         .addAll(DataComponents.COMMON_ITEM_COMPONENTS)
         .set(DataComponents.ITEM_MODEL, model)
         .set(DataComponents.ITEM_NAME, Component.translatable(item.getDescriptionId()))
         .build();
      return Holder.direct(item, components);
   }
}
