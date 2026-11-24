package autismclient.util.macro;

public final class h {
   public static final int init = -11184811;
   public static final int load = -3750202;

   private h() {
   }

   public static int init(int index, int current, int lastCompletedStep, int nowColor) {
      if (index == current) {
         return nowColor;
      } else {
         return index < lastCompletedStep ? -11184811 : -3750202;
      }
   }
}
