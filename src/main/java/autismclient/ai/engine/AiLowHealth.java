package autismclient.ai.engine;

public final class AiLowHealth {
   public static final String init_Str = "recovery ops allowed at low health: game:fish, game:farm (harvest only), game:collect_drops, game:eat, game:sleep, game:flee, game:obtain <food> with allowCombat:false, and game:goto while no hostile is within 24 blocks";
   private static int init_I;

   private AiLowHealth() {
   }

   public static boolean init_Z() {
      return init_I > 0;
   }

   public static void init_V() {
      init_I++;
   }

   public static void load() {
      init_I--;
   }

   public static String init(String activity, float health, float maxHealth) {
      return "Health too low for "
         + activity
         + " ("
         + Math.round(health * 10.0F) / 10.0
         + "/"
         + (int)maxHealth
         + "); recovery ops allowed at low health: game:fish, game:farm (harvest only), game:collect_drops, game:eat, game:sleep, game:flee, game:obtain <food> with allowCombat:false, and game:goto while no hostile is within 24 blocks";
   }
}
