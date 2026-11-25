package autismclient.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class aH {
   private aH() {
   }

   public static List<autismclient.gui.f.b.DataPipe> init(Minecraft client, AutismQueueEditorOverlay queueEditorOverlay) {
      List<autismclient.gui.f.b.DataPipe> buttons = new ArrayList<>();

      ck shared = ck.init_ck();
      int baseX = 5;
      int baseY = 5;
      int defaultWidth = 140;
      int defaultHeight = 20;
      buttons.add(
         new autismclient.gui.f.b.DataPipe(
            baseX,
            baseY,
            defaultWidth,
            defaultHeight,
            Component.literal("Close without packet"),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            () -> af.load(client, false)
         )
      );
      buttons.add(
         new autismclient.gui.f.b.DataPipe(
            baseX, baseY + 24, defaultWidth, defaultHeight, Component.literal("De-sync"), autismclient.gui.f.b.CachePool.Inner_DataUtil.update, () -> {
               if (!af.load(client)) {
                  AutismClientMessaging.sendPrefixed("Failed to desync: no open networked GUI.");
               }
            }
         )
      );
      buttons.add(
         new autismclient.gui.f.b.DataPipe(
            baseX,
            baseY + 48,
            defaultWidth,
            defaultHeight,
            Component.literal("Send packets: " + init(shared.read_Z())),
            shared.read_Z() ? autismclient.gui.f.b.CachePool.Inner_DataUtil.write : autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
            () -> {
               boolean newValue = !shared.read_Z();
               autismclient.modules.PoolEntry.init_PoolEntry().init_Z_Z(newValue);
               bi.init("Send Packets " + (newValue ? "on" : "off"), newValue ? -13248397 : -50373);
            }
         )
      );
      buttons.add(
         new autismclient.gui.f.b.DataPipe(
            baseX,
            baseY + 72,
            defaultWidth,
            defaultHeight,
            Component.literal("Delay packets: " + init(shared.write_Z())),
            shared.write_Z() ? autismclient.gui.f.b.CachePool.Inner_DataUtil.write : autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
            () -> {
               boolean newValue = !shared.write_Z();
               autismclient.modules.PoolEntry module = autismclient.modules.PoolEntry.init_PoolEntry();
               int sent = module.init_I_Z(newValue);
               module.init(newValue, sent);
            }
         )
      );
      int thirdWidth = (defaultWidth - 10) / 3;
      buttons.add(
         new autismclient.gui.f.b.DataPipe(
            baseX, baseY + 96, thirdWidth, defaultHeight, Component.literal("Clear Q"), autismclient.gui.f.b.CachePool.Inner_DataUtil.update, () -> {
               autismclient.modules.PoolEntry module = autismclient.modules.PoolEntry.init_PoolEntry();
               int count = module.write_I();
               module.read(count);
            }
         )
      );
      buttons.add(
         new autismclient.gui.f.b.DataPipe(
            baseX + thirdWidth + 5,
            baseY + 96,
            thirdWidth,
            defaultHeight,
            Component.literal("Q Editor"),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            () -> {
               if (queueEditorOverlay != null) {
                  queueEditorOverlay.init_V_Z(!queueEditorOverlay.sync_Z());
               }
            }
         )
      );
      buttons.add(
         new autismclient.gui.f.b.DataPipe(
            baseX + thirdWidth * 2 + 10,
            baseY + 96,
            thirdWidth,
            defaultHeight,
            Component.literal("Pkt Log"),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            () -> autismclient.modules.PoolEntry.init_PoolEntry().state()
         )
      );
      int halfWidth = (defaultWidth - 5) / 2;
      buttons.add(
         new autismclient.gui.f.b.DataPipe(
            baseX,
            baseY + 120,
            halfWidth,
            defaultHeight,
            Component.literal("Save GUI"),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            () -> af.init(client)
         )
      );
      buttons.add(
         new autismclient.gui.f.b.DataPipe(
            baseX + halfWidth + 5,
            baseY + 120,
            halfWidth,
            defaultHeight,
            Component.literal("Load GUI"),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            () -> {
               if (shared.init_Screen() != null && shared.init_AbstractContainerM() != null) {
                  client.execute(() -> {
                     client.gui.setScreen(shared.init_Screen());
                     if (client.player != null) {
                        client.player.containerMenu = shared.init_AbstractContainerM();
                     }
                  });
                  bi.init("GUI restored.", -13248397);
               } else {
                  bi.write("No stored GUI.");
               }
            }
         )
      );
      buttons.add(
         new autismclient.gui.f.b.DataPipe(
            baseX,
            baseY + 144,
            defaultWidth + 45,
            defaultHeight,
            Component.literal("Disconnect and send packets"),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
            () -> {
               autismclient.modules.PoolEntry.init_PoolEntry().push(false);
               if (client.getConnection() != null) {
                  int sent = shared.init_I_ClientPacketListen(client.getConnection());
                  client.getConnection().getConnection().disconnect(Component.literal("Disconnecting (Autism)"));
                  bi.init(
                     sent > 0 ? "Sent " + sent + " packet" + (sent == 1 ? "" : "s") + " and disconnected" : "Disconnected - queue empty",
                     sent > 0 ? -13248397 : -14249
                  );
               } else {
                  AutismClientMessaging.sendPrefixed("§cFailed to disconnect: No network.");
               }
            }
         )
      );
      return buttons;
   }

   private static String init(boolean value) {
      return value ? "ON" : "OFF";
   }
}
