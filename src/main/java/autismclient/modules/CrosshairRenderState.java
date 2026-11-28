package autismclient.modules;

import autismclient.util.AutismLiteVariant;

public final class CrosshairRenderState {
   private static volatile CrosshairRenderState.Snapshot activeState;

   private CrosshairRenderState() {
   }

   static void setState(CrosshairRenderState.Snapshot value) {
      activeState = value;
   }

   static void clear() {
      activeState = null;
   }

   public static CrosshairRenderState.Snapshot current() {
      CrosshairRenderState.Snapshot current = activeState;
      return current != null && init_Z() ? current : null;
   }

   private static boolean init_Z() {
      return AutismLiteVariant.enabled() || autismclient.e.PathHelper.init();
   }

   public record Snapshot(
      CrosshairPresetGeometry.Style style,
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
