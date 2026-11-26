package autismclient.mixin;

import autismclient.modules.AntiWebModule;
import autismclient.modules.am;
import autismclient.util.AutismLiteVariant;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class AutismBlockBehaviourMixin {
   @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
   private void autism$solidCobweb(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
      if (!AutismLiteVariant.enabled()
         && state.is(Blocks.COBWEB)
         && context instanceof EntityCollisionContext entity
         && entity.getEntity() instanceof LocalPlayer
         && AntiWebModule.load(entity.getEntity())) {
         cir.setReturnValue(Shapes.block());
      }
   }

   @Inject(method = "getShadeBrightness", at = @At("HEAD"), cancellable = true)
   private void autism$xrayAmbientOcclusion(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
      if (am.read_Z()) {
         cir.setReturnValue(1.0F);
      }
   }
}
