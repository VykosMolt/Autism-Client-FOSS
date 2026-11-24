package autismclient.modules;

public final class c {
   private c() {
   }

   public static float init(double distance) {
      double t = Math.clamp((distance - 2.0) / 4.0, 0.0, 1.0);
      return (float)(t * t * (3.0 - 2.0 * t));
   }
}
