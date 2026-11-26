package autismclient.mixin.security;

import autismclient.protector.TranslationSnapshot;
import autismclient.protector.ModNamespaceCatalog;
import java.util.List;
import java.util.Map;
import net.minecraft.locale.DeprecatedTranslationsInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeprecatedTranslationsInfo.class)
public abstract class AutismProtectorDeprecatedTranslationsInfoMixin {
   @Shadow
   public abstract List<String> removed();

   @Shadow
   public abstract Map<String, String> renamed();

   @Inject(method = "applyToMap", at = @At("HEAD"))
   private void autism$trackDeprecatedTranslations(Map<String, String> translations, CallbackInfo ci) {
      ModNamespaceCatalog.init(this.removed(), this.renamed());
      TranslationSnapshot.Inner_DataUtil builder = TranslationSnapshot.Inner_DataUtil.init_ClassCache_DataUti();
      if (builder != null) {
         builder.init(this.removed(), this.renamed());
      }
   }
}
