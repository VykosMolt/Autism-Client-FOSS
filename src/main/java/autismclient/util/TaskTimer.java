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

public final class TaskTimer {
   public static final EquipmentSlot[] init_EquipmentSlotArr = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private static final float init_F = 6.0F;
   private static final ResourceKey<Enchantment>[] init_ResourceKeyArr = init_ResourceKeyArr_ResourceKeyArr(
      Enchantments.PROTECTION, Enchantments.PROJECTILE_PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.BLAST_PROTECTION
   );
   private static final float[] init_FArr = new float[]{1.2F, 0.4F, 0.39F, 0.38F};
   private static final float[] load_FArr = new float[]{0.04F, 0.08F, 0.15F, 0.08F};
   private static final ResourceKey<Enchantment>[] load_ResourceKeyArr = init_ResourceKeyArr_ResourceKeyArr(
      Enchantments.FEATHER_FALLING, Enchantments.THORNS, Enchantments.RESPIRATION, Enchantments.AQUA_AFFINITY, Enchantments.UNBREAKING
   );
   private static final float[] read = new float[]{3.0F, 1.0F, 0.1F, 0.05F, 0.01F};
   private static final Map<ResourceKey<Enchantment>, Holder<Enchantment>> init_Map = new HashMap<>();
   private static Level init_Level;

   private TaskTimer() {
   }

   public static Map<EquipmentSlot, List<TaskTimer.Inner_DataUtil>> init_Map_TaskTimer_BufferHe(
      TaskTimer.Inner_BufferHelper stacks, boolean allowCursed, boolean offhandAvailable
   ) {
      Map<EquipmentSlot, List<TaskTimer.Inner_DataUtil>> byType = new EnumMap<>(EquipmentSlot.class);

      for (int inv = 0; inv < 9; inv++) {
         init_V_Map(byType, stacks.init(inv), inv, TaskTimer.Inner_StreamUtil.init, allowCursed);
      }

      for (int inv = 9; inv < 36; inv++) {
         init_V_Map(byType, stacks.init(inv), inv, TaskTimer.Inner_StreamUtil.load, allowCursed);
      }

      if (offhandAvailable) {
         init_V_Map(byType, stacks.init(EquipmentSlot.OFFHAND), 40, TaskTimer.Inner_StreamUtil.load, allowCursed);
      }

      for (EquipmentSlot slot : init_EquipmentSlotArr) {
         init_V_Map(byType, stacks.init(slot), -1, TaskTimer.Inner_StreamUtil.read, allowCursed);
      }

      return byType;
   }

   private static void init_V_Map(
      Map<EquipmentSlot, List<TaskTimer.Inner_DataUtil>> byType, ItemStack stack, int invIndex, TaskTimer.Inner_StreamUtil kind, boolean allowCursed
   ) {
      if (stack != null && !stack.isEmpty() && init_Z_ItemStack(stack) && (kind == TaskTimer.Inner_StreamUtil.read || allowCursed || !load_Z(stack))) {
         EquipmentSlot slot = init_EquipmentSlot_ItemStack(stack);
         if (slot != null && init_Z_EquipmentSlot(slot)) {
            byType.computeIfAbsent(slot, ignored -> new ArrayList<>()).add(new TaskTimer.Inner_DataUtil(stack, slot, kind, invIndex));
         }
      }
   }

   private static boolean init_Z_EquipmentSlot(EquipmentSlot slot) {
      for (EquipmentSlot managed : init_EquipmentSlotArr) {
         if (managed == slot) {
            return true;
         }
      }

      return false;
   }

   public static Map<EquipmentSlot, TaskTimer.Inner_DataUtil> init_Map_Map_I(Map<EquipmentSlot, List<TaskTimer.Inner_DataUtil>> byType, int threshold) {
      Map<EquipmentSlot, TaskTimer.Inner_DataUtil> current = new EnumMap<>(EquipmentSlot.class);

      for (Entry<EquipmentSlot, List<TaskTimer.Inner_DataUtil>> entry : byType.entrySet()) {
         current.put(
            entry.getKey(),
            init_TaskTimer_DataUtil_List_Comparator(entry.getValue(), Comparator.comparingDouble(c -> load(c.init_ItemStack(), entry.getKey())))
         );
      }

      for (int pass = 0; pass < 2; pass++) {
         Map<EquipmentSlot, TaskTimer.Inner_CachePool> kit = init_Map_Map(current);
         Comparator<TaskTimer.Inner_DataUtil> comparator = init_Comparator_Map_I(kit, threshold);
         Map<EquipmentSlot, TaskTimer.Inner_DataUtil> next = new EnumMap<>(EquipmentSlot.class);

         for (Entry<EquipmentSlot, List<TaskTimer.Inner_DataUtil>> entry : byType.entrySet()) {
            next.put(entry.getKey(), init_TaskTimer_DataUtil_List_Comparator(entry.getValue(), comparator));
         }

         current = next;
      }

      return current;
   }

   private static Map<EquipmentSlot, TaskTimer.Inner_CachePool> init_Map_Map(Map<EquipmentSlot, TaskTimer.Inner_DataUtil> current) {
      double totalDefense = 0.0;
      double totalToughness = 0.0;

      for (Entry<EquipmentSlot, TaskTimer.Inner_DataUtil> entry : current.entrySet()) {
         TaskTimer.Inner_DataUtil candidate = entry.getValue();
         if (candidate != null) {
            totalDefense += init_D_ItemStack_EquipmentSlot(candidate.init_ItemStack(), entry.getKey());
            totalToughness += load(candidate.init_ItemStack(), entry.getKey());
         }
      }

      Map<EquipmentSlot, TaskTimer.Inner_CachePool> kit = new EnumMap<>(EquipmentSlot.class);

      for (Entry<EquipmentSlot, TaskTimer.Inner_DataUtil> entry : current.entrySet()) {
         TaskTimer.Inner_DataUtil candidate = entry.getValue();
         double defense = candidate == null ? 0.0 : init_D_ItemStack_EquipmentSlot(candidate.init_ItemStack(), entry.getKey());
         double toughness = candidate == null ? 0.0 : load(candidate.init_ItemStack(), entry.getKey());
         kit.put(entry.getKey(), new TaskTimer.Inner_CachePool(totalDefense - defense, totalToughness - toughness));
      }

      return kit;
   }

   private static Comparator<TaskTimer.Inner_DataUtil> init_Comparator_Map_I(Map<EquipmentSlot, TaskTimer.Inner_CachePool> kit, int threshold) {
      Comparator<TaskTimer.Inner_DataUtil> byReduction = Comparator.comparingDouble(c -> init_D_D(init_D_TaskTimer_DataUtil_Map(c, kit)));
      return Comparator.<TaskTimer.Inner_DataUtil, Boolean>comparing(c -> init_I_ItemStack(c.init_ItemStack()) > threshold)
         .thenComparing(byReduction.reversed())
         .thenComparingDouble(c -> init_D_D(load_D(c.init_ItemStack())))
         .thenComparingInt(c -> load_I(c.init_ItemStack()))
         .thenComparingInt(c -> read(c.init_ItemStack()))
         .thenComparing(c -> c.init_TaskTimer_StreamUt() == TaskTimer.Inner_StreamUtil.read)
         .thenComparing(c -> c.init_TaskTimer_StreamUt() == TaskTimer.Inner_StreamUtil.init);
   }

   private static double init_D_TaskTimer_DataUtil_Map(TaskTimer.Inner_DataUtil candidate, Map<EquipmentSlot, TaskTimer.Inner_CachePool> kit) {
      TaskTimer.Inner_CachePool param = kit.getOrDefault(candidate.init_EquipmentSlot(), TaskTimer.Inner_CachePool.init_TaskTimer_CachePoo);
      double defense = param.init_D + init_D_ItemStack_EquipmentSlot(candidate.init_ItemStack(), candidate.init_EquipmentSlot());
      double toughness = param.load + load(candidate.init_ItemStack(), candidate.init_EquipmentSlot());
      return init_D_D(6.0, defense, toughness) * (1.0 - init_D_ItemStack(candidate.init_ItemStack()));
   }

   static double init_D_D(double damage, double defense, double toughness) {
      double f = 2.0 + toughness / 4.0;
      double g = load(defense - damage / f, defense * 0.2, 20.0);
      return 1.0 - g / 25.0;
   }

   private static double init_D_ItemStack(ItemStack stack) {
      double sum = 0.0;

      for (int i = 0; i < init_ResourceKeyArr.length; i++) {
         sum += init_I_ItemStack_ResourceKey(stack, init_ResourceKeyArr[i]) * init_FArr[i] * load_FArr[i];
      }

      return sum;
   }

   private static double load_D(ItemStack stack) {
      double sum = 0.0;

      for (int i = 0; i < load_ResourceKeyArr.length; i++) {
         sum += init_I_ItemStack_ResourceKey(stack, load_ResourceKeyArr[i]) * read[i];
      }

      return sum;
   }

   public static boolean init_Z_ItemStack(ItemStack stack) {
      Holder<Item> holder = stack.typeHolder();
      return holder.is(ItemTags.HEAD_ARMOR) || holder.is(ItemTags.CHEST_ARMOR) || holder.is(ItemTags.LEG_ARMOR) || holder.is(ItemTags.FOOT_ARMOR);
   }

   public static EquipmentSlot init_EquipmentSlot_ItemStack(ItemStack stack) {
      Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
      return equippable == null ? null : equippable.slot();
   }

   static double init_D_ItemStack_EquipmentSlot(ItemStack stack, EquipmentSlot slot) {
      return init_D_ItemStack(stack, Attributes.ARMOR, slot);
   }

   static double load(ItemStack stack, EquipmentSlot slot) {
      return init_D_ItemStack(stack, Attributes.ARMOR_TOUGHNESS, slot);
   }

   private static double init_D_ItemStack(ItemStack stack, Holder<Attribute> attribute, EquipmentSlot slot) {
      Attribute value = attribute.value();
      double base = value.getDefaultValue();
      ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
      return modifiers == null ? value.sanitizeValue(base) : value.sanitizeValue(modifiers.compute(attribute, base, slot));
   }

   public static int init_I_ItemStack(ItemStack stack) {
      return stack.getMaxDamage() - stack.getDamageValue();
   }

   private static int load_I(ItemStack stack) {
      ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
      return enchantments == null ? 0 : enchantments.size();
   }

   private static int read(ItemStack stack) {
      Enchantable enchantable = stack.get(DataComponents.ENCHANTABLE);
      return enchantable == null ? 0 : enchantable.value();
   }

   public static boolean load_Z(ItemStack stack) {
      return init_I_ItemStack_ResourceKey(stack, Enchantments.BINDING_CURSE) > 0;
   }

   private static int init_I_ItemStack_ResourceKey(ItemStack stack, ResourceKey<Enchantment> key) {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (init_Level != mc.level) {
            init_Level = mc.level;
            init_Map.clear();
         }

         Holder<Enchantment> holder = init_Map.computeIfAbsent(
            key, k -> mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow((ResourceKey<Enchantment>)k)
         );
         return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
      } catch (Throwable ignored) {
         return 0;
      }
   }

   private static TaskTimer.Inner_DataUtil init_TaskTimer_DataUtil_List_Comparator(
      List<TaskTimer.Inner_DataUtil> candidates, Comparator<TaskTimer.Inner_DataUtil> comparator
   ) {
      TaskTimer.Inner_DataUtil best = null;

      for (TaskTimer.Inner_DataUtil candidate : candidates) {
         if (best == null || comparator.compare(candidate, best) > 0) {
            best = candidate;
         }
      }

      return best;
   }

   private static double init_D_D(double value) {
      return Math.round(value * 1000.0) / 1000.0;
   }

   private static double load(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   @SafeVarargs
   private static ResourceKey<Enchantment>[] init_ResourceKeyArr_ResourceKeyArr(ResourceKey<Enchantment>... keys) {
      return keys;
   }

   public interface Inner_BufferHelper {
      ItemStack init(int var1);

      ItemStack init(EquipmentSlot var1);
   }

   private record Inner_CachePool(double init_D, double load) {
      private static final TaskTimer.Inner_CachePool init_TaskTimer_CachePoo = new TaskTimer.Inner_CachePool(0.0, 0.0);
   }

   public record Inner_DataUtil(ItemStack init_ItemStack, EquipmentSlot init_EquipmentSlot, TaskTimer.Inner_StreamUtil init_TaskTimer_StreamUt, int init_I) {
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read;
   }
}
