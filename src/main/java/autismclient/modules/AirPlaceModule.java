package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismMinecraftAccessor;
import autismclient.util.PerTickPlacementLock;
import java.util.List;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult.Fail;
import net.minecraft.world.InteractionResult.Success;
import net.minecraft.world.InteractionResult.SwingSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public final class AirPlaceModule extends Module {
   static final String DESCRIPTION = "Places blocks in air.";
   static final String REACH_LABEL = "Set placement reach";
   static final String GUIDE_LABEL = "Show placement guide";
   static final String GUIDE_FILL_LABEL = "Fill placement guide";
   static final String GUIDE_COLOR_LABEL = "Set guide color";
   private volatile BlockPos init_BlockPos;

   AirPlaceModule() {
      super("air-place", "AirPlace", ae.read, "Places blocks in air.");
      this.add(new DoubleSetting("range", "Range", 5.0, 1.0, 6.0, 0.05).description("Set placement reach").build());
      this.add(new BoolSetting("guide", "Guide", true).description("Show placement guide").build());
      this.add(new BoolSetting("fill", "Fill", true).description("Fill placement guide").build());
      this.add(new ColorSetting("guide-color", "Guide Color", -50373).description("Set guide color").build());
   }

   @Override
   public void onDisable() {
      this.init_BlockPos = null;
   }

   @Override
   public void tick() {
      this.init_BlockPos = null;
      if (MC != null && MC.level != null) {
         Player player = MC.player;
         AirPlaceModule.Inner_DataUtil placement = targetWithin(player, MC.hitResult, this.decimal("range"));
         if (placement != null && !al.init_Z_HitResult(placement.hit(), placement.hand(), this.id())) {
            if (this.bool("guide")) {
               this.init_BlockPos = placement.hit().getBlockPos().immutable();
            }

            this.init_V_StreamUtil_DataUti(placement);
         }
      }
   }

   @Override
   public boolean shouldCancelUse(HitResult hitResult, InteractionHand ignoredHand) {
      if (MC != null
         && MC.level != null
         && MC.player != null
         && MC.gameMode != null) {
         AirPlaceModule.Inner_DataUtil placement = targetWithin(MC.player, hitResult, this.decimal("range"));
         if (placement == null) {
            return false;
         } else if (MC.player.isHandsBusy()) {
            return true;
         } else {
            return al.init_Z_HitResult(placement.hit(), placement.hand(), this.id()) ? true : true;
         }
      } else {
         return false;
      }
   }

   private void init_V_StreamUtil_DataUti(AirPlaceModule.Inner_DataUtil placement) {
      if (MC.player != null
         && MC.gameMode != null
         && MC.options.keyUse.isDown()
         && ((AutismMinecraftAccessor)MC).autism$getRightClickDelay() <= 0
         && !BlinkPacketManager.isHoldingActionsOnly()
         && autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
         try {
            if (!PerTickPlacementLock.init(this.id())) {
               return;
            }

            this.init_V_BlockHitResult(placement.hit());
            ((AutismMinecraftAccessor)MC).autism$setRightClickDelay(4);
         } finally {
            autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
         }
      }
   }

   private void init_V_BlockHitResult(BlockHitResult hit) {
      for (InteractionHand hand : InteractionHand.values()) {
         if (hand != InteractionHand.OFF_HAND || !autismclient.util.InventorySlotArbiter.isHotbarClaimedByOther(this.id())) {
            ItemStack stack = MC.player.getItemInHand(hand);
            if (!stack.isItemEnabled(MC.level.enabledFeatures())) {
               return;
            }

            int oldCount = stack.getCount();
            InteractionResult blockResult = MC.gameMode.useItemOn(MC.player, hand, hit);
            if (blockResult instanceof Success success) {
               if (success.swingSource() == SwingSource.PREDICTED) {
                  Compat263.swing(MC.player, hand);
               }

               if (!stack.isEmpty() && (stack.getCount() != oldCount || MC.player.hasInfiniteMaterials())) {
                  Compat263.getItemInHandRenderer(MC.gameRenderer).itemUsed(hand);
               }

               return;
            }

            if (blockResult instanceof Fail) {
               return;
            }

            if (!stack.isEmpty() && MC.gameMode.useItem(MC.player, hand) instanceof Success success) {
               if (success.swingSource() == SwingSource.PREDICTED) {
                  Compat263.swing(MC.player, hand);
               }

               Compat263.getItemInHandRenderer(MC.gameRenderer).itemUsed(hand);
               return;
            }
         }
      }
   }

   public BlockPos target() {
      return this.init_BlockPos;
   }

   public boolean fill() {
      return this.bool("fill");
   }

   public int guideColor() {
      try {
         String value = this.value("guide-color").replace("#", "");
         if (value.length() == 6) {
            value = "FF" + value;
         }

         return (int)Long.parseLong(value, 16);
      } catch (RuntimeException ignored) {
         return -50373;
      }
   }

   public static AirPlaceModule.Inner_DataUtil targetFor(Player player, HitResult currentHit) {
      return al.init_Module_Str("air-place") instanceof AirPlaceModule airPlace && airPlace.isEnabled()
         ? targetWithin(player, currentHit, airPlace.decimal("range"))
         : null;
   }

   static AirPlaceModule.Inner_DataUtil targetWithin(Player player, HitResult currentHit, double range) {
      if (player != null && player.level() != null && currentHit != null && currentHit.getType() == Type.MISS && !isHandsBusy(player)) {
         InteractionHand hand = init_InteractionHand_Player(player);
         if (hand == null) {
            return null;
         } else {
            HitResult picked = player.pick(Math.max(1.0, Math.min(6.0, range)), 0.0F, false);
            if (bothMissed(currentHit, picked) && picked instanceof BlockHitResult blockHit) {
               BlockPos pos = blockHit.getBlockPos();
               return !player.level().isOutsideBuildHeight(pos) && player.level().getBlockState(pos).canBeReplaced()
                  ? new AirPlaceModule.Inner_DataUtil(blockHit, hand)
                  : null;
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   static InteractionHand init_InteractionHand_Player(Player player) {
      if (player == null) {
         return null;
      } else {
         ItemStack main = player.getMainHandItem();
         if (isPlaceable(main, player)) {
            return InteractionHand.MAIN_HAND;
         } else if (!main.isEmpty()) {
            return null;
         } else {
            return isPlaceable(player.getOffhandItem(), player) ? InteractionHand.OFF_HAND : null;
         }
      }
   }

   static boolean bothMissed(HitResult currentHit, HitResult pickedHit) {
      return currentHit != null && pickedHit instanceof BlockHitResult && currentHit.getType() == Type.MISS && pickedHit.getType() == Type.MISS;
   }

   private static boolean isPlaceable(ItemStack stack, Player player) {
      return stack != null && !stack.isEmpty() && stack.getItem() instanceof BlockItem && stack.isItemEnabled(player.level().enabledFeatures());
   }

   private static boolean isHandsBusy(Player player) {
      return player instanceof LocalPlayer local ? local.isHandsBusy() : player.isUsingItem();
   }

   static List<String> init_List_7() {
      return List.of("Set placement reach", "Show placement guide", "Fill placement guide", "Set guide color");
   }

   public record Inner_DataUtil(BlockHitResult hit, InteractionHand hand) {
   }
}
