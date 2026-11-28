package autismclient.ai.engine;

import autismclient.ai.AiItems;
import java.util.function.BiPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class MiningCost {
   private MiningCost() {
   }

   public static double init(Minecraft mc, ItemStack stack, BlockState state) {
      double base = stack.isEmpty() ? 1.0 : autismclient.util.BlockBreakSpeedHelper.breakSpeed(mc, stack, state);
      LocalPlayer player = mc.player;
      int haste = MobEffectUtil.hasDigSpeed(player) ? MobEffectUtil.getDigSpeedAmplification(player) + 1 : 0;
      int fatigue = player.hasEffect(MobEffects.MINING_FATIGUE) ? player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier() : -1;
      return init(
         base,
         haste,
         fatigue,
         player.getAttributeValue(Attributes.BLOCK_BREAK_SPEED),
         player.isEyeInFluid(FluidTags.WATER) ? player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED) : 1.0,
         player.onGround()
      );
   }

   static double init(double base, int haste, int fatigue, double blockSpeed, double submerged, boolean grounded) {
      double var10000;
      switch (fatigue) {
         case -1:
            var10000 = 1.0;
            break;
         case 0:
            var10000 = 0.3;
            break;
         case 1:
            var10000 = 0.09;
            break;
         case 2:
            var10000 = 0.0027;
            break;
         default:
            var10000 = 8.1E-4;
      }

      double slow = var10000;
      return base * (1.0 + haste * 0.2) * slow * blockSpeed * submerged / (grounded ? 1 : 5);
   }

   public static double breakTicks(Minecraft mc, ItemStack stack, BlockState state, float hardness) {
      if (mc.player.hasInfiniteMaterials()) {
         return hardness < 0.0F ? 1.0 / 0.0 : 1.0;
      } else {
         return AiPathfinder.MoveCosts.breakBlockTicksCompat(
            hardness, init(mc, stack, state), !state.requiresCorrectToolForDrops() || stack.isCorrectToolForDrops(state)
         );
      }
   }

   public static double breakTicks(Minecraft mc, ItemStack stack, BlockState state, float hardness, MiningCost.Inner_DataUtil f) {
      if (f.creative()) {
         return hardness < 0.0F ? 1.0 / 0.0 : 1.0;
      }

      if (stack != null && stack.is(Items.IRON_PICKAXE) && init_Z_I(init(stack), f.ironReserve(), f.stonePick(), init(state))) {
         return 1.0 / 0.0;
      }

      double base = stack != null && !stack.isEmpty() ? autismclient.util.BlockBreakSpeedHelper.breakSpeed(mc, stack, state) : 1.0;
      return AiPathfinder.MoveCosts.breakBlockTicksCompat(
         hardness,
         init(base, f.haste(), f.fatigue(), f.blockSpeed(), 1.0, true),
         !state.requiresCorrectToolForDrops() || stack != null && !stack.isEmpty() && stack.isCorrectToolForDrops(state)
      );
   }

   public static int init(Minecraft mc, BlockState state, boolean requireDrops) {
      return init_I_Minecraft(mc, state, requireDrops, 9);
   }

   public static int bestToolSlot(Minecraft mc, BlockState state, boolean requireDrops) {
      return init_I_Minecraft(mc, state, requireDrops, 36);
   }

   public static int bestToolSlot(Minecraft mc, BlockState state, boolean requireDrops, BiPredicate<BlockState, ItemStack> accepted) {
      return init_I_Minecraft(mc, state, requireDrops, 36, accepted);
   }

   private static int init_I_Minecraft(Minecraft mc, BlockState state, boolean requireDrops, int slots) {
      return init_I_Minecraft(mc, state, requireDrops, slots, (block, tool) -> true);
   }

   private static int init_I_Minecraft(Minecraft mc, BlockState state, boolean requireDrops, int slots, BiPredicate<BlockState, ItemStack> accepted) {
      Inventory inventory = mc.player.getInventory();
      boolean snowCover = state.is(Blocks.SNOW) && state.getValue(SnowLayerBlock.LAYERS) == 1;
      boolean needsTool = !mc.player.hasInfiniteMaterials() && init(state.requiresCorrectToolForDrops(), snowCover, requireDrops);
      int tool = -1;
      double bestSpeed = 1.0;

      for (int slot = 0; slot < slots; slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (AiItems.hasEnoughDurability(stack, 2) && (!needsTool || stack.isCorrectToolForDrops(state)) && accepted.test(state, stack)) {
            double speed = autismclient.util.BlockBreakSpeedHelper.breakSpeed(mc, stack, state);
            if (speed > bestSpeed || speed == bestSpeed && slot == inventory.getSelectedSlot()) {
               bestSpeed = speed;
               tool = slot;
            }
         }
      }

      if (tool >= 0 && inventory.getItem(tool).is(Items.IRON_PICKAXE) && init_Z_I(init(inventory.getItem(tool)), AiRestock.reservedStrikes(), true, init(state))) {
         for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(Items.STONE_PICKAXE) && AiItems.hasEnoughDurability(stack, 2) && accepted.test(state, stack)) {
               return slot;
            }
         }
      }

      if (tool >= 0 && (!needsTool || inventory.getItem(tool).isCorrectToolForDrops(state))) {
         return tool;
      }

      if (needsTool) {
         for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isCorrectToolForDrops(state) && AiItems.hasEnoughDurability(stack, 2) && accepted.test(state, stack)) {
               return slot;
            }
         }

         return -1;
      } else {
         int hand = init(mc, slots);
         return accepted.test(state, inventory.getItem(hand)) ? hand : -1;
      }
   }

   static boolean init(boolean requiresCorrectTool, boolean singleSnowLayer, boolean requireDrops) {
      return requiresCorrectTool && (requireDrops || !singleSnowLayer);
   }

   static boolean init_Z_I(int remaining, int reserve, boolean stoneAvailable, boolean stoneHarvests) {
      return reserve > 0 && remaining <= reserve && stoneAvailable && stoneHarvests;
   }

   private static boolean init(BlockState state) {
      ItemStack stone = new ItemStack(Items.STONE_PICKAXE);
      return stone.isCorrectToolForDrops(state) && stone.getDestroySpeed(state) > 1.0F;
   }

   private static int init(ItemStack stack) {
      return stack.getMaxDamage() - stack.getDamageValue();
   }

   static int init(Minecraft mc, int slots) {
      Inventory inventory = mc.player.getInventory();
      int selected = inventory.getSelectedSlot();
      if (!inventory.getItem(selected).isDamageableItem()) {
         return selected;
      }

      for (int i = 1; i < 9; i++) {
         for (int slot : new int[]{selected + i, selected - i}) {
            if (slot >= 0 && slot < 9 && !inventory.getItem(slot).isDamageableItem()) {
               return slot;
            }
         }
      }

      return selected;
   }

   public record Inner_DataUtil(int haste, int fatigue, double blockSpeed, boolean creative, int ironReserve, boolean stonePick) {
      public static final MiningCost.Inner_DataUtil PLAIN = new MiningCost.Inner_DataUtil(0, -1, 1.0, false);

      public Inner_DataUtil(int haste, int fatigue, double blockSpeed, boolean creative) {
         this(haste, fatigue, blockSpeed, creative, 0, false);
      }

      public static MiningCost.Inner_DataUtil of(Minecraft mc) {
         LocalPlayer player = mc.player;
         if (player == null) {
            return PLAIN;
         }

         int haste = MobEffectUtil.hasDigSpeed(player) ? MobEffectUtil.getDigSpeedAmplification(player) + 1 : 0;
         int fatigue = player.hasEffect(MobEffects.MINING_FATIGUE) ? player.getEffect(MobEffects.MINING_FATIGUE).getAmplifier() : -1;
         boolean stone = false;

         for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.STONE_PICKAXE) && MiningCost.init(stack) > 2) {
               stone = true;
            }
         }

         return new MiningCost.Inner_DataUtil(
            haste, fatigue, player.getAttributeValue(Attributes.BLOCK_BREAK_SPEED), player.hasInfiniteMaterials(), AiRestock.reservedStrikes(), stone
         );
      }
   }
}
