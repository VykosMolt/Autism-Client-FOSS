package autismclient.gui.ui;

import autismclient.util.cA;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import autismclient.gui.f.NetHelper;

public final class UiClipStack {
   private static final UiClipStack init_HashUtil = new UiClipStack();
   private final Deque<NetHelper> init_Deque = new ArrayDeque<>();

   public static UiClipStack init_HashUtil() {
      return init_HashUtil;
   }

   public void init(GuiGraphicsExtractor graphics, NetHelper bounds) {
      NetHelper clipped = bounds;
      if (!this.init_Deque.isEmpty()) {
         NetHelper parent = this.init_Deque.peek();
         int x0 = Math.max(parent.read(), bounds.read());
         int y0 = Math.max(parent.write(), bounds.write());
         int x1 = Math.min(parent.init(), bounds.init());
         int y1 = Math.min(parent.load(), bounds.load());
         clipped = NetHelper.init(x0, y0, Math.max(0, x1 - x0), Math.max(0, y1 - y0));
      }

      this.init_Deque.push(clipped);
      cA.init(graphics, clipped.read(), clipped.write(), clipped.init(), clipped.load());
   }

   public void init(GuiGraphicsExtractor graphics) {
      if (!this.init_Deque.isEmpty()) {
         this.init_Deque.pop();
         this.read(graphics);
      }
   }

   public int init_I() {
      return this.init_Deque.size();
   }

   public void init(GuiGraphicsExtractor graphics, int depth) {
      while (this.init_Deque.size() > Math.max(0, depth)) {
         this.init_Deque.pop();
         this.read(graphics);
      }
   }

   public void load(GuiGraphicsExtractor graphics) {
      while (!this.init_Deque.isEmpty()) {
         this.init_Deque.pop();
         this.read(graphics);
      }
   }

   private void read(GuiGraphicsExtractor graphics) {
      if (graphics != null) {
         try {
            graphics.disableScissor();
         } catch (IllegalStateException ignored) {
            this.init_Deque.clear();
         }
      }
   }
}
