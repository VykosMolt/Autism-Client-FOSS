package autismclient.mixin;

import autismclient.util.oresim.worldgen.SyntheticWorldGenLevel;
import autismclient.util.oresim.worldgen.PaleMossDecoratorShim;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.level.levelgen.feature.treedecorators.PaleMossDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator.Context;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PaleMossDecorator.class)
public abstract class AutismOreSimPaleMossDecoratorMixin {
   @Shadow
   @Final
   private float leavesProbability;
   @Shadow
   @Final
   private float trunkProbability;
   @Shadow
   @Final
   private float groundProbability;

   @WrapMethod(method = "place")
   private void autism$placeInSyntheticWorld(Context context, Operation<Void> original) {
      if (context.level() instanceof SyntheticWorldGenLevel synthetic) {
         PaleMossDecoratorShim.init(context, synthetic, this.leavesProbability, this.trunkProbability, this.groundProbability);
      } else {
         original.call(context);
      }
   }
}
