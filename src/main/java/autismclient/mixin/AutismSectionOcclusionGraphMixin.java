package autismclient.mixin;

import autismclient.util.AuditClientFixes;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SectionOcclusionGraph.class)
public class AutismSectionOcclusionGraphMixin {
   @ModifyVariable(method = "runUpdates", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private boolean autism$disableSmartCull(boolean smartCull) {
      return AuditClientFixes.shouldDisableOcclusion() ? false : smartCull;
   }
}
