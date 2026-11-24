package autismclient.mixin;

import autismclient.util.AuditClientFixes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager", remap = false)
abstract class AuditSodiumOcclusionMixin {
   @Unique
   private boolean audit$lastDisableOcclusion;

   @ModifyVariable(method = "prepareRenderTrees", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
   private boolean audit$disableSodiumOcclusion(boolean var1) {
      boolean var2 = AuditClientFixes.shouldDisableOcclusion();
      if (var2 != this.audit$lastDisableOcclusion) {
         this.audit$lastDisableOcclusion = var2;
         AuditClientFixes.markSodiumGraphDirty(this);
      }

      return var2 ? false : var1;
   }
}
