package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.RegistryListSetting;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

final class O {
   static final String init_Str = "held-items-only";
   static final String load = "held-items";
   static final String read = "minecraft:wooden_sword|minecraft:stone_sword|minecraft:copper_sword|minecraft:iron_sword|minecraft:golden_sword|minecraft:diamond_sword|minecraft:netherite_sword";
   private final BoolSetting init_BoolSetting;
   private final RegistryListSetting init_RegistryListSettin;
   private List<String> init_List;
   private Set<String> init_Set = Set.of();

   O(Module module) {
      this.init_BoolSetting = module.add(new BoolSetting("held-items-only", "Held Items Only", false).description("Only with listed items"));
      this.init_RegistryListSettin = module.add(
         RegistryListSetting.items(
               "held-items",
               "Held Items",
               "minecraft:wooden_sword|minecraft:stone_sword|minecraft:copper_sword|minecraft:iron_sword|minecraft:golden_sword|minecraft:diamond_sword|minecraft:netherite_sword"
            )
            .description("Items that allow it")
            .visibleWhen(this::init_Z)
      );
   }

   boolean init_Z() {
      return Boolean.TRUE.equals(this.init_BoolSetting.get());
   }

   boolean init_Z_ItemStack(ItemStack mainHand) {
      if (!this.init_Z()) {
         return true;
      } else {
         return mainHand != null && !mainHand.isEmpty() ? init(this.init_Set(), BuiltInRegistries.ITEM.getKey(mainHand.getItem()).toString()) : false;
      }
   }

   private Set<String> init_Set() {
      List<String> source = this.init_RegistryListSettin.get();
      if (source != this.init_List) {
         this.init_Set = init_Set_List(source);
         this.init_List = source;
      }

      return this.init_Set;
   }

   static Set<String> init_Set_List(List<String> entries) {
      if (entries != null && !entries.isEmpty()) {
         Set<String> out = new LinkedHashSet<>();

         for (String entry : entries) {
            String id = init_Str_Str(entry);
            if (id != null) {
               out.add(id);
            }
         }

         return Set.copyOf(out);
      } else {
         return Set.of();
      }
   }

   static boolean init(Set<String> ids, String itemId) {
      String id = init_Str_Str(itemId);
      return id != null && ids.contains(id);
   }

   private static String init_Str_Str(String raw) {
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
