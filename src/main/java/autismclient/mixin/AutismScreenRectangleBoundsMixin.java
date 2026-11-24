package autismclient.mixin;

import autismclient.util.cA;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenRectangle.class)
public abstract class AutismScreenRectangleBoundsMixin {
   @Inject(method = "transformMaxBounds", at = @At("HEAD"), cancellable = true)
   private void autism$coveringBounds(Matrix3x2fc matrix, CallbackInfoReturnable<ScreenRectangle> cir) {
      if (cA.init_Z()) {
         ScreenRectangle self = (ScreenRectangle)(Object)this;
         Vector2f a = matrix.transformPosition(self.left(), self.top(), new Vector2f());
         Vector2f b = matrix.transformPosition(self.right(), self.top(), new Vector2f());
         Vector2f c = matrix.transformPosition(self.left(), self.bottom(), new Vector2f());
         Vector2f d = matrix.transformPosition(self.right(), self.bottom(), new Vector2f());
         int minX = (int)Math.floor(Math.min(Math.min(a.x, b.x), Math.min(c.x, d.x)));
         int minY = (int)Math.floor(Math.min(Math.min(a.y, b.y), Math.min(c.y, d.y)));
         int maxX = (int)Math.ceil(Math.max(Math.max(a.x, b.x), Math.max(c.x, d.x)));
         int maxY = (int)Math.ceil(Math.max(Math.max(a.y, b.y), Math.max(c.y, d.y)));
         cir.setReturnValue(new ScreenRectangle(minX, minY, maxX - minX, maxY - minY));
      }
   }
}
