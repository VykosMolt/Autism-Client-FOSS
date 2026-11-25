package autismclient.mixin;

import autismclient.ai.aF;
import autismclient.modules.ByteReader;
import autismclient.modules.TaskTimer;
import autismclient.modules.UtilCore;
import autismclient.modules.X;
import autismclient.modules.ScaffoldModule;
import autismclient.modules.af;
import autismclient.modules.al;
import autismclient.modules.bg;
import autismclient.modules.j;
import autismclient.util.AutismLiteVariant;
import autismclient.util.W;
import autismclient.util.ci;
import autismclient.util.ck;
import autismclient.util.r;
import autismclient.util.t;
import autismclient.util.g.PoolEntry;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class AutismMultiPlayerGameModeMixin {
   @WrapMethod(method = "attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V")
   private void autism$displaceKnockback(Player player, Entity target, Operation<Void> original) {
      if (!AutismLiteVariant.enabled()) {
         if (TaskTimer.init_Z_Player_Entity(player, target)) {
            return;
         }

         X.init_V_Player(player, target, () -> original.call(player, target));
      } else {
         original.call(player, target);
      }
   }

   @WrapOperation(
      method = "interact",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V")
   )
   private void autism$routeMultiPovEntityInteraction(ClientPacketListener listener, Packet<?> packet, Operation<Void> original) {
      if (!(packet instanceof ServerboundInteractPacket interact && !AutismLiteVariant.enabled() && PoolEntry.init_Z_ServerboundInterac(interact))) {
         original.call(listener, packet);
      }
   }

   @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
   private void autism$skipTickWithoutPlayer(CallbackInfo ci) {
      if (Minecraft.getInstance().player == null) {
         ci.cancel();
      }
   }

   @Inject(method = "attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
   private void autism$recordAttackInputs(Player player, Entity entity, CallbackInfo ci) {
      autismclient.util.ci.init(player);
   }

   @Inject(method = "attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"))
   private void autism$recordCombatEngagement(Player player, Entity entity, CallbackInfo ci) {
      autismclient.util.ci.load(player);
      if (!AutismLiteVariant.enabled()) {
         bg.init(player, entity);
         r.init(player, entity);
         t.init(player, entity);
         ByteReader.init_V(entity);
      }
   }

   @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
   private void autism$onStartDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
      if (!AutismLiteVariant.enabled() && !UtilCore.close()) {
         cir.setReturnValue(false);
      } else {
         if (al.init_Z_BlockPos_Direction(pos, direction)) {
            cir.setReturnValue(true);
         }

         if (!AutismLiteVariant.enabled() && !cir.isCancelled()) {
            UtilCore.init_V_BlockPos_3(pos);
         }
      }
   }

   @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
   private void autism$routeBlockCapture(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
      if (hit != null && ck.init_ck().pool() && ck.init_ck().init_Z_BlockPos_Direction(hit.getBlockPos(), hit.getDirection())) {
         cir.setReturnValue(InteractionResult.SUCCESS);
      }
   }

   @Inject(method = "destroyBlock", at = @At("RETURN"), require = 0)
   private void autism$onDestroyBlockInvalidateEsp(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      if (Boolean.TRUE.equals(cir.getReturnValue())) {
         af.init_V_BlockPos(pos);
      }
   }

   @Inject(method = "useItemOn", at = @At("RETURN"))
   private void autism$observeVanillaUseItemOnResult(
      LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir
   ) {
      ScaffoldModule.init_V_InteractionHand(hand, hit, cir.getReturnValue());
      if (!AutismLiteVariant.enabled()) {
         j.init_V_InteractionHand_2(hand, hit, cir.getReturnValue());
      }

      if (hit != null && cir.getReturnValue() != null && cir.getReturnValue().consumesAction()) {
         BlockPos hitPos = hit.getBlockPos();
         af.init_V_BlockPos(hitPos);
         if (hit.getDirection() != null) {
            af.init_V_BlockPos(hitPos.relative(hit.getDirection()));
         }

         if (!AutismLiteVariant.enabled()) {
            aF.init_V(hitPos);
         }
      }
   }

   @Inject(
      method = "continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getId()I", ordinal = 0)
   )
   private void autism$onBlockBreakingProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
      al.init_V_BlockPos_Direction(pos, direction);
   }

   @ModifyExpressionValue(
      method = "continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"
      )
   )
   private float autism$modifyFastBreakDestroyProgress(float original, BlockPos pos, Direction direction) {
      return !AutismLiteVariant.enabled() && UtilCore.read_Z_10() ? UtilCore.init_F_F_BlockPos(original, pos) : al.init_F_F_BlockPos(original, pos);
   }

   @Inject(method = "setLocalMode(Lnet/minecraft/world/level/GameType;)V", at = @At("RETURN"))
   private void autism$trackServerMode(GameType mode, CallbackInfo ci) {
      W.init_V(mode);
   }

   @Inject(method = "setLocalMode(Lnet/minecraft/world/level/GameType;Lnet/minecraft/world/level/GameType;)V", at = @At("RETURN"))
   private void autism$trackServerMode(GameType mode, @Nullable GameType previousMode, CallbackInfo ci) {
      W.init_V(mode);
   }
}
