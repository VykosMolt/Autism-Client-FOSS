package autismclient.modules;

public record CrosshairVectorSpec(CrosshairPresetGeometry.Inner_NetHelper style, double init_D, double load) {
   public CrosshairVectorSpec(CrosshairPresetGeometry.Inner_NetHelper style, double init_D, double load) {
      if (style != null && style.isPreset() && Double.isFinite(init_D) && !(init_D <= 0.0) && Double.isFinite(load) && !(load < 0.0)) {
         this.style = style;
         this.init_D = init_D;
         this.load = load;
      } else {
         throw new IllegalArgumentException("Invalid crosshair geometry");
      }
   }

   public double init_D() {
      return Math.max(0.5, this.init_D * 0.75);
   }

   public double load() {
      return Math.max(this.init_D() + this.init_D / 2.0 + 0.5, 7.0 - this.init_D / 2.0);
   }

   public double extent() {
      double var10000;
      switch (this.style) {
         case CROSS:
            var10000 = 7.0 + this.init_D / 2.0;
            break;
         case TACTICAL:
            var10000 = 5.0 + this.load + this.init_D / 2.0;
            break;
         case CIRCLE:
            var10000 = this.load() + this.init_D / 2.0;
            break;
         case CHEVRON:
            var10000 = 7.0 / Math.sqrt(2.0) + this.init_D / 2.0;
            break;
         default:
            throw new IllegalArgumentException("Custom art is not a vector preset");
      }

      return var10000;
   }

   public double init(double x, double y) {
      double half = this.init_D / 2.0;
      double var10000;
      switch (this.style) {
         case CROSS:
            var10000 = Math.min(init(x, y, 7.0 + half, half), init(x, y, half, 7.0 + half));
            break;
         case TACTICAL:
            double arm = half + this.load + 2.5;
            var10000 = Math.min(init(x, y, half, half), Math.min(init(Math.abs(x) - arm, y, 2.5, half), init(x, Math.abs(y) - arm, half, 2.5)));
            break;
         case CIRCLE:
            var10000 = Math.min(Math.abs(Math.hypot(x, y) - this.load()) - half, Math.hypot(x, y) - this.init_D());
            break;
         case CHEVRON:
            double end = 7.0 / Math.sqrt(2.0);
            double px = Math.abs(x);
            double t = Math.clamp((px + y) / (2.0 * end), 0.0, 1.0);
            var10000 = Math.hypot(px - end * t, y - end * t) - half;
            break;
         default:
            throw new IllegalArgumentException("Custom art is not a vector preset");
      }

      return var10000;
   }

   public double span(double pixelsPerGuiUnit, double scale, double outlineWidth) {
      return 2.0 * pixelsPerGuiUnit * (this.extent() * scale + outlineWidth) + 4.0;
   }

   private static double init(double x, double y, double bx, double by) {
      double dx = Math.abs(x) - bx;
      double dy = Math.abs(y) - by;
      return Math.hypot(Math.max(dx, 0.0), Math.max(dy, 0.0)) + Math.min(Math.max(dx, dy), 0.0);
   }

   public double write() {
      return this.init_D;
   }

   public double update() {
      return this.load;
   }
}
