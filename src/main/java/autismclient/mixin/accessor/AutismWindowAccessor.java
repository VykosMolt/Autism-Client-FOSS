package autismclient.mixin.accessor;

import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Window.class)
public interface AutismWindowAccessor {
   @Accessor("width")
   void autism$setScreenWidth(int var1);

   @Accessor("height")
   void autism$setScreenHeight(int var1);

   @Accessor("iconified")
   void autism$setIconified(boolean var1);

   @Accessor("focused")
   void autism$setFocused(boolean var1);
}
