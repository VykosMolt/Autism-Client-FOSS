package autismclient.mixin.security;

import autismclient.protector.PackTranslationPolicyResolver;
import autismclient.protector.CallerModInspector;
import autismclient.protector.TranslationSnapshot;
import autismclient.protector.TranslationSnapshotDuck;
import autismclient.protector.IsolatedResourcePack;
import autismclient.protector.ModNamespaceCatalog;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.io.InputStream;
import java.util.List;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.impl.resource.pack.ModNioPackResources;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.OverlayedPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLanguage.class)
public class AutismProtectorClientLanguageMixin implements TranslationSnapshotDuck {
   @Unique
   private TranslationSnapshot autism$translationSnapshot;

   @Override
   public void autism$setTranslationSnapshot(TranslationSnapshot snapshot) {
      this.autism$translationSnapshot = snapshot;
   }

   @Override
   public TranslationSnapshot autism$getTranslationSnapshot() {
      return this.autism$translationSnapshot;
   }

   @WrapMethod(method = "loadFrom")
   private static ClientLanguage autism$captureTranslations(
      ResourceManager resourceManager, List<String> languageStack, boolean defaultRightToLeft, Operation<ClientLanguage> original
   ) {
      PackTranslationPolicyResolver.Inner_StreamUtil token = PackTranslationPolicyResolver.init(resourceManager);
      TranslationSnapshot.Inner_DataUtil builder = TranslationSnapshot.Inner_DataUtil.init_ClassCache_DataUti_ResourceManager(resourceManager);
      boolean success = false;

      try {
         ModNamespaceCatalog.load();
         ClientLanguage language = original.call(resourceManager, languageStack, defaultRightToLeft);
         ((TranslationSnapshotDuck)language).autism$setTranslationSnapshot(builder.init_ClassCache());
         success = true;
         return language;
      } finally {
         if (!success) {
            PackTranslationPolicyResolver.init(token);
         }

         builder.close();
      }
   }

   @WrapOperation(
      method = "appendFrom",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/locale/Language;loadFromJson(Ljava/io/InputStream;Ljava/util/function/BiConsumer;)V")
   )
   private static void autism$trackTranslations(InputStream stream, BiConsumer<String, String> output, Operation<Void> original, @Local Resource resource) {
      BiConsumer<String, String> snapshotOutput = snapshotOutput(output, resource);
      PackResources source = resource.source();
      if (source instanceof VanillaPackResources) {
         original.call(stream, trackingOutput(snapshotOutput, (key, value) -> ModNamespaceCatalog.init_V(key)));
      } else if (source instanceof FilePackResources || source instanceof OverlayedPackResources || source instanceof IsolatedResourcePack) {
         original.call(stream, trackingOutput(snapshotOutput, ModNamespaceCatalog::init_V));
      } else if (source instanceof PathPackResources) {
         original.call(stream, snapshotOutput);
      } else {
         String modId = source instanceof ModNioPackResources modPack ? modPack.getFabricModMetadata().getId() : CallerModInspector.init(source.getClass());
         if (modId == null) {
            original.call(stream, snapshotOutput);
         } else {
            original.call(stream, trackingOutput(snapshotOutput, (key, value) -> ModNamespaceCatalog.load(key, modId)));
         }
      }
   }

   private static BiConsumer<String, String> snapshotOutput(BiConsumer<String, String> output, Resource resource) {
      TranslationSnapshot.Inner_DataUtil builder = TranslationSnapshot.Inner_DataUtil.init_ClassCache_DataUti();
      return builder == null ? output : (key, value) -> {
         try {
            builder.init(resource.source(), resource.sourcePackId(), key, value);
         } catch (Throwable var6) {
         }

         output.accept(key, value);
      };
   }

   private static BiConsumer<String, String> trackingOutput(BiConsumer<String, String> output, BiConsumer<String, String> tracker) {
      return (key, value) -> {
         tracker.accept(key, value);
         output.accept(key, value);
      };
   }
}
