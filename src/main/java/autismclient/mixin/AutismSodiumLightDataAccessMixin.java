package autismclient.mixin;

import autismclient.modules.am;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess", remap = false)
public abstract class AutismSodiumLightDataAccessMixin {
   @Shadow(remap = false)
   protected BlockAndTintGetter level;
   @Shadow(remap = false)
   @Final
   private MutableBlockPos pos;

   @ModifyVariable(method = "compute", at = @At("STORE"), name = "bl", remap = false)
   private int autism$xraySodiumBlockLight(int bl) {
      if (!am.read_Z()) {
         return bl;
      }

      BlockState state = this.level.getBlockState(this.pos);
      return !am.init_Z_BlockState_BlockPos(state, this.pos) ? am.load_I() : bl;
   }

   @ModifyVariable(method = "compute", at = @At("STORE"), name = "sl", remap = false)
   private int autism$fullbrightSodiumSkyLight(int sl) {
      if (!am.close()) {
         return sl;
      }

      int boosted = am.init(LightLayer.SKY);
      return boosted > sl ? boosted : sl;
   }

   @ModifyVariable(method = "compute", at = @At("STORE"), name = "bl", remap = false)
   private int autism$fullbrightSodiumBlockLight(int bl) {
      if (!am.close()) {
         return bl;
      }

      int boosted = am.init(LightLayer.BLOCK);
      return boosted > bl ? boosted : bl;
   }
}
