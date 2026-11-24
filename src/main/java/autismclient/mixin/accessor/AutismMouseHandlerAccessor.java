package autismclient.mixin.accessor;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface AutismMouseHandlerAccessor {
   @Invoker("onButton")
   void autism$invokeOnButton(long var1, MouseButtonInfo var3, int var4);

   @Accessor("xpos")
   void autism$setXpos(double var1);

   @Accessor("ypos")
   void autism$setYpos(double var1);
}
