package autismclient.gui.sdf;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public final class UiScissorStack {
   private static final Deque<ScreenRectangle> init = new ArrayDeque<>();

   private UiScissorStack() {
   }

   public static void init(ScreenRectangle rectangle) {
      init(rectangle, null);
   }

   static void init(ScreenRectangle rectangle, ScreenRectangle rootWhenStackEmpty) {
      ScreenRectangle parent = init.peekLast();
      if (parent == null) {
         parent = rootWhenStackEmpty != null ? rootWhenStackEmpty : load_ScreenRectangle();
      }

      ScreenRectangle intersection = rectangle.intersection(parent);
      init.addLast(intersection != null ? intersection : ScreenRectangle.empty());
   }

   public static void load(ScreenRectangle rectangle) {
      init.addLast(rectangle != null ? rectangle : ScreenRectangle.empty());
   }

   public static void pop() {
      if (!init.isEmpty()) {
         init.removeLast();
      }
   }

   public static ScreenRectangle peek() {
      return init.peekLast();
   }

   public static void clear() {
      init.clear();
   }

   private static ScreenRectangle load_ScreenRectangle() {
      Minecraft minecraft = Minecraft.getInstance();
      return new ScreenRectangle(0, 0, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
   }
}
