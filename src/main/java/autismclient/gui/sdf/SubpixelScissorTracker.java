package autismclient.gui.sdf;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor.ScissorStack;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;

public final class SubpixelScissorTracker {
   private static final Map<ScreenRectangle, int[]> init_Map = new IdentityHashMap<>();
   public static int init_I;

   private SubpixelScissorTracker() {
   }

   public static int[] init(ScreenRectangle rectangle) {
      return rectangle != null && !init_Map.isEmpty() ? init_Map.get(rectangle) : null;
   }

   public static void init() {
      init_Map.clear();
   }

   public static boolean init_Z_GuiGraphicsExtract(ScissorStack stack, Matrix3x2fc pose, int x0, int y0, int x1, int y1) {
      Minecraft minecraft = Minecraft.getInstance();
      int guiScale = minecraft.getWindow().getGuiScale();
      if (guiScale <= 0) {
         return false;
      }

      Vector2f topLeft = pose.transformPosition(x0, y0, new Vector2f());
      Vector2f bottomRight = pose.transformPosition(x1, y1, new Vector2f());
      int[] exact = init(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y, guiScale);
      if (init_Z_F(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y, guiScale, exact)) {
         return false;
      }

      ScreenRectangle parent = stack.peek();
      int[] parentExact = parent == null ? new int[]{0, 0, minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight()} : init(parent, guiScale);
      int[] outward = init(exact, guiScale);
      stack.push(new ScreenRectangle(outward[0], outward[1], outward[2] - outward[0], outward[3] - outward[1]));
      ScreenRectangle top = stack.peek();
      if (top != null && top != ScreenRectangle.empty()) {
         init_Map.put(top, init(exact, parentExact));
      }

      return true;
   }

   private static int[] init(ScreenRectangle rectangle, int guiScale) {
      int[] known = init_Map.get(rectangle);
      return known != null
         ? known
         : new int[]{rectangle.left() * guiScale, rectangle.top() * guiScale, rectangle.right() * guiScale, rectangle.bottom() * guiScale};
   }

   static int[] init(float x0, float y0, float x1, float y1, int guiScale) {
      return new int[]{Math.round(x0 * guiScale), Math.round(y0 * guiScale), Math.round(x1 * guiScale), Math.round(y1 * guiScale)};
   }

   static boolean init_Z_F(float x0, float y0, float x1, float y1, int guiScale, int[] exact) {
      int left = (int)Math.floor(x0);
      int top = (int)Math.floor(y0);
      int right = left + (int)Math.floor(x1 - x0);
      int bottom = top + (int)Math.floor(y1 - y0);
      return left * guiScale == exact[0] && top * guiScale == exact[1] && right * guiScale == exact[2] && bottom * guiScale == exact[3];
   }

   static int[] init(int[] exact, int guiScale) {
      int x0 = Math.floorDiv(exact[0], guiScale);
      int y0 = Math.floorDiv(exact[1], guiScale);
      int x1 = Math.max(x0, -Math.floorDiv(-exact[2], guiScale));
      int y1 = Math.max(y0, -Math.floorDiv(-exact[3], guiScale));
      return new int[]{x0, y0, x1, y1};
   }

   static int[] init(int[] a, int[] b) {
      int x0 = Math.max(a[0], b[0]);
      int y0 = Math.max(a[1], b[1]);
      int x1 = Math.max(x0, Math.min(a[2], b[2]));
      int y1 = Math.max(y0, Math.min(a[3], b[3]));
      return new int[]{x0, y0, x1, y1};
   }
}
