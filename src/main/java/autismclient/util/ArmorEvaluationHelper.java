package autismclient.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;

public final class ArmorEvaluationHelper {
   public static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private static final float init_F = 6.0F;
   private static final ResourceKey<Enchantment>[] PROTECTION_ENCHANTS = keys(
      Enchantments.PROTECTION, Enchantments.PROJECTILE_PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.BLAST_PROTECTION
   );
   private static final float[] PROTECTION_WEIGHTS = new float[]{1.2F, 0.4F, 0.39F, 0.38F};
   private static final float[] PROTECTION_FACTORS = new float[]{0.04F, 0.08F, 0.15F, 0.08F};
   private static final ResourceKey<Enchantment>[] load_ResourceKeyArr = keys(
      Enchantments.FEATHER_FALLING, Enchantments.THORNS, Enchantments.RESPIRATION, Enchantments.AQUA_AFFINITY, Enchantments.UNBREAKING
   );
   private static final float[] ENCHANT_WEIGHTS = new float[]{3.0F, 1.0F, 0.1F, 0.05F, 0.01F};
   private static final Map<ResourceKey<Enchantment>, Holder<Enchantment>> enchantHolders = new HashMap<>();
   private static Level enchantHolderLevel;

   private ArmorEvaluationHelper() {
   }

   public static Map<EquipmentSlot, List<ArmorEvaluationHelper.ArmorCandidate>> candidatesBySlot(
      ArmorEvaluationHelper.StackSource stacks, boolean allowCursed, boolean offhandAvailable
   ) {
      Map<EquipmentSlot, List<ArmorEvaluationHelper.ArmorCandidate>> byType = new EnumMap<>(EquipmentSlot.class);

      for (int inv = 0; inv < 9; inv++) {
         addCandidate(byType, stacks.inventoryStack(inv), inv, ArmorEvaluationHelper.SourceKind.HOTBAR, allowCursed);
      }

      for (int inv = 9; inv < 36; inv++) {
         addCandidate(byType, stacks.inventoryStack(inv), inv, ArmorEvaluationHelper.SourceKind.INVENTORY, allowCursed);
      }

      if (offhandAvailable) {
         addCandidate(byType, stacks.equipped(EquipmentSlot.OFFHAND), 40, ArmorEvaluationHelper.SourceKind.INVENTORY, allowCursed);
      }

      for (EquipmentSlot slot : ARMOR_SLOTS) {
         addCandidate(byType, stacks.equipped(slot), -1, ArmorEvaluationHelper.SourceKind.ARMOR, allowCursed);
      }

      return byType;
   }

   private static void addCandidate(
      Map<EquipmentSlot, List<ArmorEvaluationHelper.ArmorCandidate>> byType, ItemStack stack, int invIndex, ArmorEvaluationHelper.SourceKind kind, boolean allowCursed
   ) {
      if (stack != null && !stack.isEmpty() && isArmor(stack) && (kind == ArmorEvaluationHelper.SourceKind.ARMOR || allowCursed || !hasBindingCurse(stack))) {
         EquipmentSlot slot = slotOf(stack);
         if (slot != null && isManagedSlot(slot)) {
            byType.computeIfAbsent(slot, ignored -> new ArrayList<>()).add(new ArmorEvaluationHelper.ArmorCandidate(stack, slot, kind, invIndex));
         }
      }
   }

   private static boolean isManagedSlot(EquipmentSlot slot) {
      for (EquipmentSlot managed : ARMOR_SLOTS) {
         if (managed == slot) {
            return true;
         }
      }

      return false;
   }

   public static Map<EquipmentSlot, ArmorEvaluationHelper.ArmorCandidate> bestBySlot(Map<EquipmentSlot, List<ArmorEvaluationHelper.ArmorCandidate>> byType, int threshold) {
      Map<EquipmentSlot, ArmorEvaluationHelper.ArmorCandidate> current = new EnumMap<>(EquipmentSlot.class);

      for (Entry<EquipmentSlot, List<ArmorEvaluationHelper.ArmorCandidate>> entry : byType.entrySet()) {
         current.put(
            entry.getKey(),
            best(entry.getValue(), Comparator.comparingDouble(c -> load(c.stack(), entry.getKey())))
         );
      }

      for (int pass = 0; pass < 2; pass++) {
         Map<EquipmentSlot, ArmorEvaluationHelper.ArmorScore> kit = init_Map_Map(current);
         Comparator<ArmorEvaluationHelper.ArmorCandidate> comparator = candidateOrder(kit, threshold);
         Map<EquipmentSlot, ArmorEvaluationHelper.ArmorCandidate> next = new EnumMap<>(EquipmentSlot.class);

         for (Entry<EquipmentSlot, List<ArmorEvaluationHelper.ArmorCandidate>> entry : byType.entrySet()) {
            next.put(entry.getKey(), best(entry.getValue(), comparator));
         }

         current = next;
      }

      return current;
   }

   private static Map<EquipmentSlot, ArmorEvaluationHelper.ArmorScore> init_Map_Map(Map<EquipmentSlot, ArmorEvaluationHelper.ArmorCandidate> current) {
      double totalDefense = 0.0;
      double totalToughness = 0.0;

      for (Entry<EquipmentSlot, ArmorEvaluationHelper.ArmorCandidate> entry : current.entrySet()) {
         ArmorEvaluationHelper.ArmorCandidate candidate = entry.getValue();
         if (candidate != null) {
            totalDefense += armorValue(candidate.stack(), entry.getKey());
            totalToughness += load(candidate.stack(), entry.getKey());
         }
      }

      Map<EquipmentSlot, ArmorEvaluationHelper.ArmorScore> kit = new EnumMap<>(EquipmentSlot.class);

      for (Entry<EquipmentSlot, ArmorEvaluationHelper.ArmorCandidate> entry : current.entrySet()) {
         ArmorEvaluationHelper.ArmorCandidate candidate = entry.getValue();
         double defense = candidate == null ? 0.0 : armorValue(candidate.stack(), entry.getKey());
         double toughness = candidate == null ? 0.0 : load(candidate.stack(), entry.getKey());
         kit.put(entry.getKey(), new ArmorEvaluationHelper.ArmorScore(totalDefense - defense, totalToughness - toughness));
      }

      return kit;
   }

   private static Comparator<ArmorEvaluationHelper.ArmorCandidate> candidateOrder(Map<EquipmentSlot, ArmorEvaluationHelper.ArmorScore> kit, int threshold) {
      Comparator<ArmorEvaluationHelper.ArmorCandidate> byReduction = Comparator.comparingDouble(c -> round3(init_D_TaskTimer_DataUtil_Map(c, kit)));
      return Comparator.<ArmorEvaluationHelper.ArmorCandidate, Boolean>comparing(c -> remainingDurability(c.stack()) > threshold)
         .thenComparing(byReduction.reversed())
         .thenComparingDouble(c -> round3(load_D(c.stack())))
         .thenComparingInt(c -> enchantmentCount(c.stack()))
         .thenComparingInt(c -> enchantability(c.stack()))
         .thenComparing(c -> c.kind() == ArmorEvaluationHelper.SourceKind.ARMOR)
         .thenComparing(c -> c.kind() == ArmorEvaluationHelper.SourceKind.HOTBAR);
   }

   private static double init_D_TaskTimer_DataUtil_Map(ArmorEvaluationHelper.ArmorCandidate candidate, Map<EquipmentSlot, ArmorEvaluationHelper.ArmorScore> kit) {
      ArmorEvaluationHelper.ArmorScore param = kit.getOrDefault(candidate.slot(), ArmorEvaluationHelper.ArmorScore.EMPTY);
      double defense = param.defense + armorValue(candidate.stack(), candidate.slot());
      double toughness = param.load + load(candidate.stack(), candidate.slot());
      return damageReduction(6.0, defense, toughness) * (1.0 - protectionScore(candidate.stack()));
   }

   static double damageReduction(double damage, double defense, double toughness) {
      double f = 2.0 + toughness / 4.0;
      double g = clamp(defense - damage / f, defense * 0.2, 20.0);
      return 1.0 - g / 25.0;
   }

   private static double protectionScore(ItemStack stack) {
      double sum = 0.0;

      for (int i = 0; i < PROTECTION_ENCHANTS.length; i++) {
         sum += enchantLevel(stack, PROTECTION_ENCHANTS[i]) * PROTECTION_WEIGHTS[i] * PROTECTION_FACTORS[i];
      }

      return sum;
   }

   private static double load_D(ItemStack stack) {
      double sum = 0.0;

      for (int i = 0; i < load_ResourceKeyArr.length; i++) {
         sum += enchantLevel(stack, load_ResourceKeyArr[i]) * ENCHANT_WEIGHTS[i];
      }

      return sum;
   }

   public static boolean isArmor(ItemStack stack) {
      Holder<Item> holder = stack.typeHolder();
      return holder.is(ItemTags.HEAD_ARMOR) || holder.is(ItemTags.CHEST_ARMOR) || holder.is(ItemTags.LEG_ARMOR) || holder.is(ItemTags.FOOT_ARMOR);
   }

   public static EquipmentSlot slotOf(ItemStack stack) {
      Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
      return equippable == null ? null : equippable.slot();
   }

   static double armorValue(ItemStack stack, EquipmentSlot slot) {
      return attributeValue(stack, Attributes.ARMOR, slot);
   }

   static double load(ItemStack stack, EquipmentSlot slot) {
      return attributeValue(stack, Attributes.ARMOR_TOUGHNESS, slot);
   }

   private static double attributeValue(ItemStack stack, Holder<Attribute> attribute, EquipmentSlot slot) {
      Attribute value = attribute.value();
      double base = value.getDefaultValue();
      ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
      return modifiers == null ? value.sanitizeValue(base) : value.sanitizeValue(modifiers.compute(attribute, base, slot));
   }

   public static int remainingDurability(ItemStack stack) {
      return stack.getMaxDamage() - stack.getDamageValue();
   }

   private static int enchantmentCount(ItemStack stack) {
      ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
      return enchantments == null ? 0 : enchantments.size();
   }

   private static int enchantability(ItemStack stack) {
      Enchantable enchantable = stack.get(DataComponents.ENCHANTABLE);
      return enchantable == null ? 0 : enchantable.value();
   }

   public static boolean hasBindingCurse(ItemStack stack) {
      return enchantLevel(stack, Enchantments.BINDING_CURSE) > 0;
   }

   private static int enchantLevel(ItemStack stack, ResourceKey<Enchantment> key) {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (enchantHolderLevel != mc.level) {
            enchantHolderLevel = mc.level;
            enchantHolders.clear();
         }

         Holder<Enchantment> holder = enchantHolders.computeIfAbsent(
            key, k -> mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow((ResourceKey<Enchantment>)k)
         );
         return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
      } catch (Throwable ignored) {
         return 0;
      }
   }

   private static ArmorEvaluationHelper.ArmorCandidate best(
      List<ArmorEvaluationHelper.ArmorCandidate> candidates, Comparator<ArmorEvaluationHelper.ArmorCandidate> comparator
   ) {
      ArmorEvaluationHelper.ArmorCandidate best = null;

      for (ArmorEvaluationHelper.ArmorCandidate candidate : candidates) {
         if (best == null || comparator.compare(candidate, best) > 0) {
            best = candidate;
         }
      }

      return best;
   }

   private static double round3(double value) {
      return Math.round(value * 1000.0) / 1000.0;
   }

   private static double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   @SafeVarargs
   private static ResourceKey<Enchantment>[] keys(ResourceKey<Enchantment>... keys) {
      return keys;
   }

   public interface StackSource {
      ItemStack inventoryStack(int var1);

      ItemStack equipped(EquipmentSlot var1);
   }

   private record ArmorScore(double defense, double load) {
      private static final ArmorEvaluationHelper.ArmorScore EMPTY = new ArmorEvaluationHelper.ArmorScore(0.0, 0.0);
   }

   public record ArmorCandidate(ItemStack stack, EquipmentSlot slot, ArmorEvaluationHelper.SourceKind kind, int inventorySlot) {
   }

   public enum SourceKind {
      HOTBAR,
      INVENTORY,
      ARMOR;
   }
}
