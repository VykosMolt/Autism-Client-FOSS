package autismclient.mixin;

import autismclient.ai.cc;
import autismclient.modules.RenderState;
import autismclient.modules.aA;
import autismclient.modules.aC;
import autismclient.modules.ai;
import autismclient.modules.ax;
import autismclient.util.AutismLiteVariant;
import autismclient.util.cd;
import autismclient.util.g.PoolEntry;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class AutismEntityMovementMixin {
   @WrapOperation(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"))
   private void autism$noEntityPush(Entity receiving, double x, double y, double z, Operation<Void> original) {
      if (AutismLiteVariant.enabled() || !(receiving instanceof LocalPlayer) || !ax.init(receiving, "entities")) {
         original.call(receiving, x, y, z);
      }
   }

   @WrapOperation(method = "getBlockSpeedFactor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getSpeedFactor()F"))
   private float autism$noBlockSlowdown(Block block, Operation<Float> original) {
      return !AutismLiteVariant.enabled() && (Object)this instanceof LocalPlayer && aA.init((Entity)(Object)this, block) ? 1.0F : original.call(block);
   }

   @Inject(method = "makeStuckInBlock", at = @At("HEAD"), cancellable = true)
   private void autism$ignoreWebAndBerrySlowdown(BlockState state, Vec3 multiplier, CallbackInfo ci) {
      if (!AutismLiteVariant.enabled()) {
         Entity entity = (Entity)(Object)this;
         if (entity instanceof LocalPlayer
            && (state.is(Blocks.COBWEB) && RenderState.init_Z(entity) || state.is(Blocks.SWEET_BERRY_BUSH) && aA.init(entity, state.getBlock()))) {
            entity.resetFallDistance();
            ci.cancel();
         }
      }
   }

   @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
   private Vec3 autism$modifyLocalPlayerMovement(Vec3 movement, MoverType type) {
      return ai.init((Entity)(Object)this, type, movement);
   }

   @Redirect(method = "getBlockBounciness", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getBounceRestitution()F"))
   private float autism$antiBounceRestitution(Block block) {
      Entity entity = (Entity)(Object)this;
      return block instanceof SlimeBlock && ai.init_Z(entity) ? 0.0F : block.getBounceRestitution();
   }

   @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
   private void autism$freecamTurn(double deltaYaw, double deltaPitch, CallbackInfo ci) {
      if ((Object)this == Minecraft.getInstance().player) {
         if (aC.init_Z()) {
            aC.load(deltaYaw, deltaPitch);
            ci.cancel();
         } else if (!AutismLiteVariant.enabled() && PoolEntry.init_Z()) {
            PoolEntry.init_Z_D_D(deltaYaw, deltaPitch);
            ci.cancel();
         } else if (cd.init_Z()) {
            ci.cancel();
         } else if (!AutismLiteVariant.enabled()) {
            cc ai = cc.load_cc();
            if (ai != null && ai.close_Z()) {
               ci.cancel();
            }
         }
      }
   }
}
