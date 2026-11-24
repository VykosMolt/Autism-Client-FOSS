package autismclient.compat263;

public final class CompatOutputTarget {
   public static final CompatOutputTarget MAIN_TARGET = new CompatOutputTarget("main");
   public static final CompatOutputTarget OUTLINE_TARGET = new CompatOutputTarget("outline");
   public static final CompatOutputTarget WEATHER_TARGET = new CompatOutputTarget("weather");
   public static final CompatOutputTarget ITEM_ENTITY_TARGET = new CompatOutputTarget("item_entity");
   private final String name;

   public CompatOutputTarget(String var1) {
      this.name = var1;
   }

   @Override
   public String toString() {
      return this.name;
   }
}
