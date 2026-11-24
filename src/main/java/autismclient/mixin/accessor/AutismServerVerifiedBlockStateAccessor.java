package autismclient.mixin.accessor;

import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler$ServerVerifiedState")
public interface AutismServerVerifiedBlockStateAccessor {
   @Accessor("blockState")
   BlockState autism$getBlockState();
}
