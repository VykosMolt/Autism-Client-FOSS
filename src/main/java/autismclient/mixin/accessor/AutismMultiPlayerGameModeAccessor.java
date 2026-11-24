package autismclient.mixin.accessor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public interface AutismMultiPlayerGameModeAccessor {
   @Accessor("carriedIndex")
   void autism$setCarriedIndex(int var1);

   @Accessor("destroyProgress")
   float autism$getDestroyProgress();

   @Accessor("destroyProgress")
   void autism$setDestroyProgress(float var1);

   @Accessor("destroyDelay")
   void autism$setDestroyDelay(int var1);

   @Accessor("destroyDelay")
   int autism$getDestroyDelay();

   @Accessor("isDestroying")
   boolean autism$isDestroying();

   @Accessor("destroyBlockPos")
   BlockPos autism$getDestroyBlockPos();

   @Invoker("startPrediction")
   void autism$startPrediction(ClientLevel var1, PredictiveAction var2);

   @Invoker("ensureHasSentCarriedItem")
   void autism$ensureHasSentCarriedItem();
}
