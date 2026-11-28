package autismclient.gui.sdf;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public final class UiScissorStack {
   private static final Deque<ScreenRectangle> STACK = new ArrayDeque<>();

   private UiScissorStack() {
   }

   public static void push(ScreenRectangle rectangle) {
      push(rectangle, null);
   }

   static void push(ScreenRectangle rectangle, ScreenRectangle rootWhenStackEmpty) {
      ScreenRectangle parent = STACK.peekLast();
      if (parent == null) {
         parent = rootWhenStackEmpty != null ? rootWhenStackEmpty : fullScreen();
      }

      ScreenRectangle intersection = rectangle.intersection(parent);
      STACK.addLast(intersection != null ? intersection : ScreenRectangle.empty());
   }

   public static void mirrorPush(ScreenRectangle rectangle) {
      STACK.addLast(rectangle != null ? rectangle : ScreenRectangle.empty());
   }

   public static void pop() {
      if (!STACK.isEmpty()) {
         STACK.removeLast();
      }
   }

   public static ScreenRectangle peek() {
      return STACK.peekLast();
   }

   public static void clear() {
      STACK.clear();
   }

   private static ScreenRectangle fullScreen() {
      Minecraft minecraft = Minecraft.getInstance();
      return new ScreenRectangle(0, 0, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
   }
}
