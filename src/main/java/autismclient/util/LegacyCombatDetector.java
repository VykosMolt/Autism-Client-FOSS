package autismclient.util;

import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import java.lang.reflect.Method;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class LegacyCombatDetector {
   private static Object lastLevel;
   private static boolean checked;
   private static boolean reflectionFailed;
   private static Method targetVersionMethod;
   private static Method olderThanOrEqualMethod;
   private static Object v1_8;

   private LegacyCombatDetector() {
   }

   public static boolean isLegacyCombat(Player player) {
      if (player == null) {
         return false;
      }

      if (player.getAttributeValue(Attributes.ATTACK_SPEED) >= 20.0) {
         return true;
      }

      if (isViaLegacyTarget()) {
         return true;
      }

      if (player.level() != lastLevel) {
         lastLevel = player.level();
         checked = false;
      }

      if (!checked) {
         float delay = player.getCurrentItemAttackStrengthDelay();
         int ticker = ((AutismLivingEntityAccessor)player).autism$getAttackStrengthTicker();
         float vanilla = Mth.clamp((ticker + 0.5F) / delay, 0.0F, 1.0F);
         if (vanilla < 0.9F && player.getAttackStrengthScale(0.5F) >= 0.999F) {
            checked = true;
         }
      }

      return checked;
   }

   public static boolean isLegacy() {
      return isViaLegacyTarget();
   }

   private static boolean isViaLegacyTarget() {
      if (reflectionFailed) {
         return false;
      }

      try {
         if (targetVersionMethod == null) {
            Class<?> translator = Class.forName("com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator");
            Class<?> versions = Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion");
            v1_8 = versions.getField("v1_8").get(null);
            olderThanOrEqualMethod = versions.getMethod("olderThanOrEqualTo", versions);
            targetVersionMethod = translator.getMethod("getTargetVersion");
         }

         Object version = targetVersionMethod.invoke(null);
         return version != null && (Boolean)olderThanOrEqualMethod.invoke(version, v1_8);
      } catch (ReflectiveOperationException | RuntimeException | LinkageError absent) {
         reflectionFailed = true;
         return false;
      }
   }
}
