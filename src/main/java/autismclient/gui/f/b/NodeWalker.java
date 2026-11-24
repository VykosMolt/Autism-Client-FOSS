package autismclient.gui.f.b;

public final class NodeWalker {
   private NodeWalker() {
   }

   public static void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, double progress) {
      autismclient.gui.f.JsonUtil colors = context.init_TableIndex().init_JsonUtil();
      progress = Math.max(0.0, Math.min(1.0, progress));
      int radius = bounds.sync() / 2;
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, radius, colors.pop, colors.get);
      int innerW = Math.max(0, bounds.update() - 2);
      int fillW = Math.max(0, Math.min(innerW, (int)Math.round(innerW * progress)));
      if (fillW > 0) {
         autismclient.gui.f.PacketUtil.load(
            context.init_GuiGraphicsExtract(),
            autismclient.gui.f.NetHelper.init(bounds.read() + 1, bounds.write() + 1, fillW, Math.max(0, bounds.sync() - 2)),
            Math.max(1, radius - 1),
            init(colors.clear, colors.stop, progress)
         );
      }
   }

   private static int init(int a, int b, double t) {
      t = Math.max(0.0, Math.min(1.0, t));
      int aa = a >>> 24 & 0xFF;
      int ar = a >>> 16 & 0xFF;
      int ag = a >>> 8 & 0xFF;
      int ab = a & 0xFF;
      int ba = b >>> 24 & 0xFF;
      int br = b >>> 16 & 0xFF;
      int bg = b >>> 8 & 0xFF;
      int bb = b & 0xFF;
      int ca = (int)Math.round(aa + (ba - aa) * t);
      int cr = (int)Math.round(ar + (br - ar) * t);
      int cg = (int)Math.round(ag + (bg - ag) * t);
      int cb = (int)Math.round(ab + (bb - ab) * t);
      return ca << 24 | cr << 16 | cg << 8 | cb;
   }
}
