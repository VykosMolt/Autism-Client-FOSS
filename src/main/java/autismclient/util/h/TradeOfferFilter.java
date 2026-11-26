package autismclient.util.h;

import java.util.List;
import java.util.Locale;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffer;

public record TradeOfferFilter(int init_I, List<String> init_List, List<String> load_List, List<TradeOfferFilter.Inner_DataUtil> read, boolean init_Z, int load_I) {
   public TradeOfferFilter {
      init_List = List.copyOf(init_List);
      load_List = List.copyOf(load_List);
      read = List.copyOf(read);
   }

   public boolean init_Z_MerchantOffer_I(MerchantOffer offer, int index) {
      if (offer != null && (this.init_I <= 0 || index + 1 == this.init_I)) {
         if (!this.init_List.isEmpty() && !init_Z_List_ItemStack(this.init_List, offer.getResult())) {
            return false;
         }

         if (!this.load_List.isEmpty() && !init_Z_List_ItemStack(this.load_List, offer.getCostA()) && !init_Z_List_ItemStack(this.load_List, offer.getCostB())) {
            return false;
         }

         int price = init(offer);
         if (!init_Z_I_I(price, this.load_I)) {
            return false;
         }

         if (this.read.isEmpty()) {
            return true;
         }

         if (!offer.getResult().is(Items.ENCHANTED_BOOK)) {
            return false;
         }

         ItemEnchantments enchants = offer.getResult().getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

         for (Holder<Enchantment> enchantment : enchants.keySet()) {
            String key = enchantment.unwrapKey().map(k -> k.identifier().toString()).orElse("");

            for (TradeOfferFilter.Inner_DataUtil book : this.read) {
               if (key.equals(book.init_Str()) && init(enchants.getLevel(enchantment), book.init_I(), this.init_Z) && init_Z_I_I(price, book.load())) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean init(int actual, int wanted, boolean exact) {
      return exact ? actual == wanted : actual >= wanted;
   }

   public static boolean init_Z_I_I(int price, int limit) {
      return limit == 0 || price <= limit;
   }

   public static boolean init(int cost, int spent, int budget, int available, int reserve) {
      return (budget == 0 || (long)spent + cost <= budget) && (cost == 0 || available - cost >= reserve);
   }

   public static int init(MerchantOffer offer) {
      return init(offer.getCostA()) + init(offer.getCostB());
   }

   private static int init(ItemStack stack) {
      return stack.is(Items.EMERALD) ? stack.getCount() : 0;
   }

   private static boolean init_Z_List_ItemStack(List<String> ids, ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }

      String actual = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
      return ids.stream().map(TradeOfferFilter::init).anyMatch(actual::equals);
   }

   public static String init(String value) {
      if (value != null && !value.isBlank()) {
         String clean = value.strip().toLowerCase(Locale.ROOT);
         return clean.contains(":") ? clean : "minecraft:" + clean;
      } else {
         return "";
      }
   }

   public record Inner_DataUtil(String init_Str, int init_I, int load) {
      public Inner_DataUtil(String init_Str, int init_I, int load) {
         init_Str = TradeOfferFilter.init(init_Str);
         if (!init_Str.isBlank() && init_I >= 1 && init_I <= 255 && load >= 0 && load <= 128) {
            this.init_Str = init_Str;
            this.init_I = init_I;
            this.load = load;
         } else {
            throw new IllegalArgumentException("Book format: enchantment level [price]");
         }
      }

      public static TradeOfferFilter.Inner_DataUtil init(String value) {
         String[] parts = value.strip().split("\\s+");
         if (parts.length >= 1 && parts.length <= 3) {
            try {
               return new TradeOfferFilter.Inner_DataUtil(
                  parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : 1, parts.length > 2 ? Integer.parseInt(parts[2]) : 0
               );
            } catch (NumberFormatException e) {
               throw new IllegalArgumentException("Book format: enchantment level [price]");
            }
         } else {
            throw new IllegalArgumentException("Book format: enchantment level [price]");
         }
      }
   }
}
