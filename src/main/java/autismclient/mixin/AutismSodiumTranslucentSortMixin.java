package autismclient.mixin;

import autismclient.modules.am;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector", remap = false)
public abstract class AutismSodiumTranslucentSortMixin {
   @Inject(method = "filterSortType", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
   private static void autism$xrayDisableTranslucentSort(CallbackInfoReturnable<Object> cir) {
      if (am.read_Z()) {
         Object current = cir.getReturnValue();
         if (current instanceof Enum<?> sort) {
            String name = sort.name();
            if ("STATIC_TOPO".equals(name) || "DYNAMIC".equals(name)) {
               Object none = am.load(current);
               if (none != current) {
                  cir.setReturnValue(none);
               }
            }
         }
      }
   }
}
