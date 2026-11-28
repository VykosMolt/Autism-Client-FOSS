package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.RegistryListSetting;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

final class HeldItemFilter {
   static final String init_Str = "held-items-only";
   static final String load = "held-items";
   static final String read = "minecraft:wooden_sword|minecraft:stone_sword|minecraft:copper_sword|minecraft:iron_sword|minecraft:golden_sword|minecraft:diamond_sword|minecraft:netherite_sword";
   private final BoolSetting heldOnly;
   private final RegistryListSetting itemList;
   private List<String> cachedSource;
   private Set<String> cachedIds = Set.of();

   HeldItemFilter(Module module) {
      this.heldOnly = module.add(new BoolSetting("held-items-only", "Held Items Only", false).description("Only with listed items"));
      this.itemList = module.add(
         RegistryListSetting.items(
               "held-items",
               "Held Items",
               "minecraft:wooden_sword|minecraft:stone_sword|minecraft:copper_sword|minecraft:iron_sword|minecraft:golden_sword|minecraft:diamond_sword|minecraft:netherite_sword"
            )
            .description("Items that allow it")
            .visibleWhen(this::isEnabled)
      );
   }

   boolean isEnabled() {
      return Boolean.TRUE.equals(this.heldOnly.get());
   }

   boolean accepts(ItemStack mainHand) {
      if (!this.isEnabled()) {
         return true;
      } else {
         return mainHand != null && !mainHand.isEmpty() ? containsId(this.allowedIds(), BuiltInRegistries.ITEM.getKey(mainHand.getItem()).toString()) : false;
      }
   }

   private Set<String> allowedIds() {
      List<String> source = this.itemList.get();
      if (source != this.cachedSource) {
         this.cachedIds = normalizeIds(source);
         this.cachedSource = source;
      }

      return this.cachedIds;
   }

   static Set<String> normalizeIds(List<String> entries) {
      if (entries != null && !entries.isEmpty()) {
         Set<String> out = new LinkedHashSet<>();

         for (String entry : entries) {
            String id = normalizeId(entry);
            if (id != null) {
               out.add(id);
            }
         }

         return Set.copyOf(out);
      } else {
         return Set.of();
      }
   }

   static boolean containsId(Set<String> ids, String itemId) {
      String id = normalizeId(itemId);
      return id != null && ids.contains(id);
   }

   private static String normalizeId(String raw) {
      if (raw == null) {
         return null;
      } else {
         String id = raw.trim().toLowerCase(Locale.ROOT);
         if (id.isEmpty()) {
            return null;
         } else {
            return id.indexOf(58) >= 0 ? id : "minecraft:" + id;
         }
      }
   }
}
