package autismclient.mixin.accessor;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LocalPlayer.class)
public interface AutismLocalPlayerAccessor {
   @Invoker("isSprintingPossible")
   boolean autism$isSprintingPossible(boolean var1);

   @Accessor("positionReminder")
   void autism$setPositionReminder(int var1);

   @Accessor("xLast")
   double autism$getLastSentX();

   @Accessor("yLast")
   double autism$getLastSentY();

   @Accessor("zLast")
   double autism$getLastSentZ();

   @Accessor("yRotLast")
   float autism$getLastSentYaw();

   @Accessor("xRotLast")
   float autism$getLastSentPitch();

   @Invoker("isSlowDueToUsingItem")
   boolean autism$isSlowDueToUsingItem();
}
