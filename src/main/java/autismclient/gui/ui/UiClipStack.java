package autismclient.gui.ui;

import autismclient.util.cA;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import autismclient.gui.f.NetHelper;

public final class UiClipStack {
   private static final UiClipStack INSTANCE = new UiClipStack();
   private final Deque<NetHelper> stack = new ArrayDeque<>();

   public static UiClipStack getInstance() {
      return INSTANCE;
   }

   public void push(GuiGraphicsExtractor graphics, NetHelper bounds) {
      NetHelper clipped = bounds;
      if (!this.stack.isEmpty()) {
         NetHelper parent = this.stack.peek();
         int x0 = Math.max(parent.read(), bounds.read());
         int y0 = Math.max(parent.write(), bounds.write());
         int x1 = Math.min(parent.init(), bounds.init());
         int y1 = Math.min(parent.load(), bounds.load());
         clipped = NetHelper.init(x0, y0, Math.max(0, x1 - x0), Math.max(0, y1 - y0));
      }

      this.stack.push(clipped);
      cA.init(graphics, clipped.read(), clipped.write(), clipped.init(), clipped.load());
   }

   public void pop(GuiGraphicsExtractor graphics) {
      if (!this.stack.isEmpty()) {
         this.stack.pop();
         this.disableScissorSafe(graphics);
      }
   }

   public int depth() {
      return this.stack.size();
   }

   public void popToDepth(GuiGraphicsExtractor graphics, int depth) {
      while (this.stack.size() > Math.max(0, depth)) {
         this.stack.pop();
         this.disableScissorSafe(graphics);
      }
   }

   public void clear(GuiGraphicsExtractor graphics) {
      while (!this.stack.isEmpty()) {
         this.stack.pop();
         this.disableScissorSafe(graphics);
      }
   }

   private void disableScissorSafe(GuiGraphicsExtractor graphics) {
      if (graphics != null) {
         try {
            graphics.disableScissor();
         } catch (IllegalStateException ignored) {
            this.stack.clear();
         }
      }
   }
}
