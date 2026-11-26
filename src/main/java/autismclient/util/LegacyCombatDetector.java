package autismclient.util;

import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import java.lang.reflect.Method;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class LegacyCombatDetector {
   private static Object init_Obj;
   private static boolean init_Z;
   private static boolean load_Z;
   private static Method init_Method;
   private static Method load_Method;
   private static Object load_Obj;

   private LegacyCombatDetector() {
   }

   public static boolean init(Player player) {
      if (player == null) {
         return false;
      }

      if (player.getAttributeValue(Attributes.ATTACK_SPEED) >= 20.0) {
         return true;
      }

      if (load()) {
         return true;
      }

      if (player.level() != init_Obj) {
         init_Obj = player.level();
         init_Z = false;
      }

      if (!init_Z) {
         float delay = player.getCurrentItemAttackStrengthDelay();
         int ticker = ((AutismLivingEntityAccessor)player).autism$getAttackStrengthTicker();
         float vanilla = Mth.clamp((ticker + 0.5F) / delay, 0.0F, 1.0F);
         if (vanilla < 0.9F && player.getAttackStrengthScale(0.5F) >= 0.999F) {
            init_Z = true;
         }
      }

      return init_Z;
   }

   public static boolean init() {
      return load();
   }

   private static boolean load() {
      if (load_Z) {
         return false;
      }

      try {
         if (init_Method == null) {
            Class<?> translator = Class.forName("com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator");
            Class<?> versions = Class.forName("com.viaversion.viaversion.api.protocol.version.ProtocolVersion");
            load_Obj = versions.getField("v1_8").get(null);
            load_Method = versions.getMethod("olderThanOrEqualTo", versions);
            init_Method = translator.getMethod("getTargetVersion");
         }

         Object version = init_Method.invoke(null);
         return version != null && (Boolean)load_Method.invoke(version, load_Obj);
      } catch (ReflectiveOperationException | RuntimeException | LinkageError absent) {
         load_Z = true;
         return false;
      }
   }
}
