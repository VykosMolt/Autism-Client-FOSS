package autismclient.mixin;

import java.util.Set;

public class SecurityMixinOverlapCheck {
   private static final Set<String> OLD_OVERLAP = Set.of(
      "AutismProtectorTranslatableContentsMixin",
      "AutismProtectorKeybindContentsMixin",
      "AutismProtectorComponentSerializationMixin",
      "AutismProtectorPacketProcessorMixin",
      "AutismProtectorPacketDecoderMixin",
      "AutismProtectorHttpUtilMixin",
      "AutismProtectorDownloadQueueMixin",
      "AutismProtectorClientLanguageMixin",
      "AutismProtectorDeprecatedTranslationsInfoMixin",
      "AutismProtectorOptionsMixin",
      "AutismProtectorKeyMappingRegistryImplMixin",
      "AutismProtectorPayloadTypeRegistryImplMixin",
      "AutismProtectorResourceLoaderImplMixin"
   );
   private static final Set<String> MANDATORY = Set.of(
      "AutismProtectorTranslatableContentsMixin",
      "AutismProtectorKeybindContentsMixin",
      "AutismProtectorComponentSerializationMixin",
      "AutismProtectorPacketProcessorMixin",
      "AutismProtectorPacketDecoderMixin",
      "AutismProtectorClientLanguageMixin",
      "AutismProtectorDeprecatedTranslationsInfoMixin"
   );

   public static void main(String[] args) {
      for (String name : OLD_OVERLAP) {
         for (int flags = 0; flags < 4; flags++) {
            boolean opsec = (flags & 1) != 0;
            boolean ep = (flags & 2) != 0;
            boolean expected = MANDATORY.contains(name) || !opsec && !ep;
            boolean actual = AutismMixinPlugin.shouldApplySecurityMixin("autismclient.mixin.security." + name, opsec, ep);
            if (actual != expected) {
               System.out.println("FAIL SecurityMixinOverlapCheck: " + name + " opsec=" + opsec + " ep=" + ep + " got " + actual);
               System.exit(1);
            }
         }
      }

      // the receiver gate is applied in every combination, like the codec gate that makes it necessary
      for (String name : Set.of("AutismFabricPayloadCodecMixin", "AuditFabricPayloadGateMixin")) {
         for (int flags = 0; flags < 4; flags++) {
            if (!AutismMixinPlugin.shouldApplySecurityMixin("autismclient.mixin.security." + name, (flags & 1) != 0, (flags & 2) != 0)) {
               System.out.println("FAIL SecurityMixinOverlapCheck: " + name + " is not applied with flags " + flags);
               System.exit(1);
            }
         }
      }

      System.out.println("PASS SecurityMixinOverlapCheck");
   }
}
