package autismclient.mixin.accessor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface AutismGameRendererAccessor {
   @Invoker("bobHurt")
   void autism$bobHurt(CameraRenderState var1, PoseStack var2);

   @Invoker("bobView")
   void autism$bobView(CameraRenderState var1, PoseStack var2);
}
