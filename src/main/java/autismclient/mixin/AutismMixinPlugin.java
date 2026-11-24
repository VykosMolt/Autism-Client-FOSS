package autismclient.mixin;

import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class AutismMixinPlugin implements IMixinConfigPlugin {
   private static final Set<String> SODIUM_MIXINS = Set.of(
      "AutismSodiumBlockRendererMixin",
      "AutismSodiumBlockContextMixin",
      "AutismSodiumLightDataAccessMixin",
      "AutismSodiumDefaultFluidRendererMixin",
      "AutismSodiumFluidRendererImplMixin",
      "AuditSodiumOcclusionMixin",
      "AutismSodiumDefaultChunkRendererMixin",
      "AutismSodiumTranslucentSortMixin"
   );
   private boolean sodiumLoaded;
   private boolean indigoLoaded;
   private boolean opsecLoaded;
   private boolean exploitPreventerLoaded;
   private boolean replayModLoaded;
   private boolean flashbackLoaded;
   private boolean essentialLoaded;
   private boolean lithiumLoaded;
   private boolean viaLoaded;
   private static final Set<String> MANDATORY_SECURITY_MIXINS = Set.of(
      "AutismFabricPayloadCodecMixin",
      // goes wherever the codec gate above goes: that one makes the payloads this one keeps from mod receivers
      "AuditFabricPayloadGateMixin",
      "AutismProtectorTranslatableContentsMixin",
      "AutismProtectorKeybindContentsMixin",
      "AutismProtectorComponentSerializationMixin",
      "AutismProtectorPacketProcessorMixin",
      "AutismProtectorPacketDecoderMixin",
      "AutismProtectorClientLanguageMixin",
      "AutismProtectorDeprecatedTranslationsInfoMixin",
      "AutismProtectorConnectionTrackingMixin",
      "AutismPacketContentsMixin",
      "AutismEmptyPlainTextContentsMixin",
      "AutismResolvedContentsMixin",
      "AutismComponentUtilsMixin",
      "AutismProtectorLevelParticlesPacketMixin",
      "AutismProtectorParticleEngineMixin",
      "AutismProtectorSignEditScreenMixin"
   );
   private static final Set<String> EXPLOIT_PREVENTER_OVERLAP_SECURITY_MIXINS = Set.of(
      "AutismProtectorHttpUtilMixin",
      "AutismProtectorDownloadQueueMixin",
      "AutismProtectorOptionsMixin",
      "AutismProtectorKeyMappingRegistryImplMixin",
      "AutismProtectorPayloadTypeRegistryImplMixin",
      "AutismProtectorResourceLoaderImplMixin"
   );

   @Override
   public void onLoad(String mixinPackage) {
      FabricLoader loader = FabricLoader.getInstance();
      this.sodiumLoaded = loader.isModLoaded("sodium");
      this.indigoLoaded = loader.isModLoaded("fabric-renderer-indigo");
      this.opsecLoaded = loader.isModLoaded("opsec");
      this.exploitPreventerLoaded = loader.isModLoaded("exploitpreventer");
      this.replayModLoaded = loader.isModLoaded("replaymod");
      this.flashbackLoaded = loader.isModLoaded("flashback");
      this.essentialLoaded = loader.isModLoaded("essential") || loader.isModLoaded("essential-container") || loader.isModLoaded("essential-loader");
      this.lithiumLoaded = loader.isModLoaded("lithium");
      this.viaLoaded = loader.isModLoaded("viafabricplus");
   }

   @Override
   public String getRefMapperConfig() {
      return null;
   }

   @Override
   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      if (!shouldApplyViaMixin(mixinClassName, this.viaLoaded)) {
         return false;
      } else {
         String simpleName = mixinClassName.substring(mixinClassName.lastIndexOf(46) + 1);
         if (SODIUM_MIXINS.contains(simpleName)) {
            return this.sodiumLoaded;
         } else if ("AutismFluidRendererMixin".equals(simpleName) && this.sodiumLoaded) {
            return false;
         } else if ("AutismReplayModGuiHandlerMixin".equals(simpleName) || "AutismReplayStudioTeamMixin".equals(simpleName)) {
            return this.replayModLoaded;
         } else if ("AutismFlashbackConnectionMixin".equals(simpleName)) {
            return this.flashbackLoaded;
         } else if (simpleName.startsWith("AutismEssential")) {
            return this.essentialLoaded;
         } else if (mixinClassName.startsWith("autismclient.mixin.indigo.")) {
            return this.indigoLoaded && !this.sodiumLoaded;
         } else if (mixinClassName.startsWith("autismclient.mixin.lithium.")) {
            return this.lithiumLoaded;
         } else {
            return mixinClassName.startsWith("autismclient.mixin.security.")
               ? shouldApplySecurityMixin(mixinClassName, this.opsecLoaded, this.exploitPreventerLoaded)
               : true;
         }
      }
   }

   static boolean shouldApplyViaMixin(String mixinClassName, boolean viaLoaded) {
      return !mixinClassName.startsWith("autismclient.mixin.via.") || viaLoaded;
   }

   static boolean shouldApplySecurityMixin(String mixinClassName, boolean opsecLoaded, boolean exploitPreventerLoaded) {
      String simpleName = mixinClassName.substring(mixinClassName.lastIndexOf(46) + 1);
      if (MANDATORY_SECURITY_MIXINS.contains(simpleName)) {
         return true;
      } else {
         return opsecLoaded ? false : !exploitPreventerLoaded || !EXPLOIT_PREVENTER_OVERLAP_SECURITY_MIXINS.contains(simpleName);
      }
   }

   @Override
   public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
   }

   @Override
   public List<String> getMixins() {
      return null;
   }

   @Override
   public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }

   @Override
   public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }
}
