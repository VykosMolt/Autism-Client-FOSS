package autismclient.mixin;

import autismclient.ai.AiOps;
import autismclient.modules.AntiBotTracker;
import autismclient.modules.BacktrackModule;
import autismclient.modules.BedNukerModule;
import autismclient.modules.KBDisplacementModule;
import autismclient.modules.ScaffoldModule;
import autismclient.modules.EspChunkWorker;
import autismclient.modules.al;
import autismclient.modules.WTappingModule;
import autismclient.modules.ClutchModule;
import autismclient.util.AutismLiteVariant;
import autismclient.util.FakeGamemodeManager;
import autismclient.util.SprintStateTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.CombatAttackDispatcher;
import autismclient.util.RecentCombatTracker;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
         if (BacktrackModule.init_Z_Player_Entity(player, target)) {
            return;
         }

         KBDisplacementModule.wrapAttack(player, target, () -> original.call(player, target));
      } else {
         original.call(player, target);
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
      autismclient.util.SprintStateTracker.init(player);
   }

   @Inject(method = "attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"))
   private void autism$recordCombatEngagement(Player player, Entity entity, CallbackInfo ci) {
      autismclient.util.SprintStateTracker.load(player);
      if (!AutismLiteVariant.enabled()) {
         WTappingModule.init(player, entity);
         CombatAttackDispatcher.init(player, entity);
         RecentCombatTracker.init(player, entity);
         AntiBotTracker.onLocalPlayerAttackEntity(entity);
      }
   }

   @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
   private void autism$onStartDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
      if (!AutismLiteVariant.enabled() && !BedNukerModule.close()) {
         cir.setReturnValue(false);
      } else {
         if (al.init_Z_BlockPos_Direction(pos, direction)) {
            cir.setReturnValue(true);
         }

         if (!AutismLiteVariant.enabled() && !cir.isCancelled()) {
            BedNukerModule.init_V_BlockPos_3(pos);
         }
      }
   }

   @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
   private void autism$routeBlockCapture(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
      if (hit != null && AutismSessionState.getInstance().hasBlockCaptureCallback() && AutismSessionState.getInstance().dispatchBlockCapture(hit.getBlockPos(), hit.getDirection())) {
         cir.setReturnValue(InteractionResult.SUCCESS);
      }
   }

   @Inject(method = "destroyBlock", at = @At("RETURN"), require = 0)
   private void autism$onDestroyBlockInvalidateEsp(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      if (Boolean.TRUE.equals(cir.getReturnValue())) {
         EspChunkWorker.init_V_BlockPos(pos);
      }
   }

   @Inject(method = "useItemOn", at = @At("RETURN"))
   private void autism$observeVanillaUseItemOnResult(
      LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir
   ) {
      ScaffoldModule.onUseItemOnResult(hand, hit, cir.getReturnValue());
      if (!AutismLiteVariant.enabled()) {
         ClutchModule.onUseItemOnResult(hand, hit, cir.getReturnValue());
      }

      if (hit != null && cir.getReturnValue() != null && cir.getReturnValue().consumesAction()) {
         BlockPos hitPos = hit.getBlockPos();
         EspChunkWorker.init_V_BlockPos(hitPos);
         if (hit.getDirection() != null) {
            EspChunkWorker.init_V_BlockPos(hitPos.relative(hit.getDirection()));
         }

         if (!AutismLiteVariant.enabled()) {
            AiOps.init_V(hitPos);
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
      return !AutismLiteVariant.enabled() && BedNukerModule.read_Z_10() ? BedNukerModule.init_F_F_BlockPos(original, pos) : al.init_F_F_BlockPos(original, pos);
   }

   @Inject(method = "setLocalMode(Lnet/minecraft/world/level/GameType;)V", at = @At("RETURN"))
   private void autism$trackServerMode(GameType mode, CallbackInfo ci) {
      FakeGamemodeManager.onRealGamemodeChanged(mode);
   }

   @Inject(method = "setLocalMode(Lnet/minecraft/world/level/GameType;Lnet/minecraft/world/level/GameType;)V", at = @At("RETURN"))
   private void autism$trackServerMode(GameType mode, @Nullable GameType previousMode, CallbackInfo ci) {
      FakeGamemodeManager.onRealGamemodeChanged(mode);
   }
}
