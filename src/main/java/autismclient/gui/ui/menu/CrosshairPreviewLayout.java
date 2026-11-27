package autismclient.gui.ui.menu;

import autismclient.gui.f.NetHelper;

record CrosshairPreviewLayout(NetHelper listArea, NetHelper load) {
   static final int init_I = 140;

   static CrosshairPreviewLayout init(NetHelper body) {
      if (body.update() >= 8 && body.sync() >= 30) {
         int gap = 6;
         if (body.update() >= 380) {
            int width = 132;
            return new CrosshairPreviewLayout(
               NetHelper.init(body.read(), body.write(), body.update() - width - gap, body.sync()),
               NetHelper.init(body.init() - width - 3, body.write() + 3, width, Math.min(160, Math.max(0, body.sync() - 6)))
            );
         } else {
            int height = Math.min(92, Math.max(0, body.sync() / 3));
            return new CrosshairPreviewLayout(
               NetHelper.init(body.read(), body.write() + height + gap, body.update(), body.sync() - height - gap),
               NetHelper.init(body.read() + 3, body.write() + 3, body.update() - 6, height - 3)
            );
         }
      } else {
         return new CrosshairPreviewLayout(body, NetHelper.init(body.read(), body.write(), 0, 0));
      }
   }
}
