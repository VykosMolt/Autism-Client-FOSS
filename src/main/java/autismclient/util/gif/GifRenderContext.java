package autismclient.util.gif;

public final class GifRenderContext {
   private static int entityDepth;
   private static String entityName;
   private static int shieldDepth;

   private GifRenderContext() {
   }

   public static void enter(String name) {
      entityDepth++;
      entityName = name;
   }

   public static void popEntity() {
      if (entityDepth > 0) {
         entityDepth--;
      }

      if (entityDepth == 0) {
         entityName = null;
      }
   }

   public static String currentName() {
      return entityDepth > 0 ? entityName : null;
   }

   public static void pushShield() {
      shieldDepth++;
   }

   public static void popShield() {
      if (shieldDepth > 0) {
         shieldDepth--;
      }
   }

   public static boolean isShieldRendering() {
      return shieldDepth > 0;
   }
}
