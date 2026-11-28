package autismclient.util.villager;

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

public record TradeOfferFilter(int tradeIndex, List<String> resultIds, List<String> costIds, List<TradeOfferFilter.Inner_DataUtil> read, boolean init_Z, int maxPrice) {
   public TradeOfferFilter {
      resultIds = List.copyOf(resultIds);
      costIds = List.copyOf(costIds);
      read = List.copyOf(read);
   }

   public boolean matches(MerchantOffer offer, int index) {
      if (offer != null && (this.tradeIndex <= 0 || index + 1 == this.tradeIndex)) {
         if (!this.resultIds.isEmpty() && !matchesAny(this.resultIds, offer.getResult())) {
            return false;
         }

         if (!this.costIds.isEmpty() && !matchesAny(this.costIds, offer.getCostA()) && !matchesAny(this.costIds, offer.getCostB())) {
            return false;
         }

         int price = cost(offer);
         if (!withinPriceLimit(price, this.maxPrice)) {
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
               if (key.equals(book.enchantment()) && countMatches(enchants.getLevel(enchantment), book.minLevel(), this.init_Z) && withinPriceLimit(price, book.load())) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean countMatches(int actual, int wanted, boolean exact) {
      return exact ? actual == wanted : actual >= wanted;
   }

   public static boolean withinPriceLimit(int price, int limit) {
      return limit == 0 || price <= limit;
   }

   public static boolean affordable(int cost, int spent, int budget, int available, int reserve) {
      return (budget == 0 || (long)spent + cost <= budget) && (cost == 0 || available - cost >= reserve);
   }

   public static int cost(MerchantOffer offer) {
      return emeraldCount(offer.getCostA()) + emeraldCount(offer.getCostB());
   }

   private static int emeraldCount(ItemStack stack) {
      return stack.is(Items.EMERALD) ? stack.getCount() : 0;
   }

   private static boolean matchesAny(List<String> ids, ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      }

      String actual = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
      return ids.stream().map(TradeOfferFilter::normalizeItem).anyMatch(actual::equals);
   }

   public static String normalizeItem(String value) {
      if (value != null && !value.isBlank()) {
         String clean = value.strip().toLowerCase(Locale.ROOT);
         return clean.contains(":") ? clean : "minecraft:" + clean;
      } else {
         return "";
      }
   }

   public record Inner_DataUtil(String enchantment, int minLevel, int load) {
      public Inner_DataUtil(String enchantment, int minLevel, int load) {
         enchantment = TradeOfferFilter.normalizeItem(enchantment);
         if (!enchantment.isBlank() && minLevel >= 1 && minLevel <= 255 && load >= 0 && load <= 128) {
            this.enchantment = enchantment;
            this.minLevel = minLevel;
            this.load = load;
         } else {
            throw new IllegalArgumentException("Book format: enchantment level [price]");
         }
      }

      public static TradeOfferFilter.Inner_DataUtil parse(String value) {
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
