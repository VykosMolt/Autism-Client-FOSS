package autismclient.modules;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record EspHighlightBox(AABB box, int fillColor, int outlineColor, Vec3 lineFrom, Vec3 lineTo, float lineWidth) {
   public EspHighlightBox(AABB box, int color) {
      this(box, color, color, null, null, 1.5F);
   }

   public EspHighlightBox(AABB box, int fill, int outline) {
      this(box, fill, outline, null, null, 1.5F);
   }

   public static EspHighlightBox line(Vec3 from, Vec3 to, int color, float width) {
      return new EspHighlightBox(null, 0, color, from, to, width);
   }
}
