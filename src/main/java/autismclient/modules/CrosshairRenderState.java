package autismclient.modules;

import autismclient.util.AutismLiteVariant;

public final class CrosshairRenderState {
   private static volatile CrosshairRenderState.Inner_DataUtil init;

   private CrosshairRenderState() {
   }

   static void setState(CrosshairRenderState.Inner_DataUtil value) {
      init = value;
   }

   static void clear() {
      init = null;
   }

   public static CrosshairRenderState.Inner_DataUtil current() {
      CrosshairRenderState.Inner_DataUtil current = init;
      return current != null && init_Z() ? current : null;
   }

   private static boolean init_Z() {
      return AutismLiteVariant.enabled() || autismclient.e.PathHelper.init();
   }

   public record Inner_DataUtil(
      CrosshairPresetGeometry.Inner_NetHelper style,
      double init_D,
      double load_D,
      double read_D,
      boolean init_Z,
      int init_I,
      float init_F,
      boolean load_Z,
      int load_I,
      double write_D,
      boolean read_Z,
      int read_I,
      int write_I,
      boolean write_Z,
      double update,
      CrosshairPixelArt art
   ) {
   }
}
