package autismclient.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ContainerScreenToolbar {
   private ContainerScreenToolbar() {
   }

   public static List<autismclient.gui.ui.widgets.SimpleClickButton> init(Minecraft client, AutismQueueEditorOverlay queueEditorOverlay) {
      List<autismclient.gui.ui.widgets.SimpleClickButton> buttons = new ArrayList<>();
      if (!AutismLiteVariant.enabled() && !autismclient.e.FileCache.init()) {
         return buttons;
      }

      AutismSessionState shared = AutismSessionState.getInstance();
      int baseX = 5;
      int baseY = 5;
      int defaultWidth = 140;
      int defaultHeight = 20;
      buttons.add(
         new autismclient.gui.ui.widgets.SimpleClickButton(
            baseX,
            baseY,
            defaultWidth,
            defaultHeight,
            Component.literal("Close without packet"),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            () -> GuiDesyncHelper.load(client, false)
         )
      );
      buttons.add(
         new autismclient.gui.ui.widgets.SimpleClickButton(
            baseX, baseY + 24, defaultWidth, defaultHeight, Component.literal("De-sync"), autismclient.gui.f.b.CachePool.Inner_DataUtil.update, () -> {
               if (!GuiDesyncHelper.load(client)) {
                  AutismClientMessaging.sendPrefixed("Failed to desync: no open networked GUI.");
               }
            }
         )
      );
      buttons.add(
         new autismclient.gui.ui.widgets.SimpleClickButton(
            baseX,
            baseY + 48,
            defaultWidth,
            defaultHeight,
            Component.literal("Send packets: " + init(shared.isSendPacketsEnabled())),
            shared.isSendPacketsEnabled() ? autismclient.gui.f.b.CachePool.Inner_DataUtil.write : autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
            () -> {
               boolean newValue = !shared.isSendPacketsEnabled();
               autismclient.modules.ModuleRuntimeManager.getInstance().setAndSaveSendGuiPackets(newValue);
               bi.init("Send Packets " + (newValue ? "on" : "off"), newValue ? -13248397 : -50373);
            }
         )
      );
      buttons.add(
         new autismclient.gui.ui.widgets.SimpleClickButton(
            baseX,
            baseY + 72,
            defaultWidth,
            defaultHeight,
            Component.literal("Delay packets: " + init(shared.isDelayPacketsEnabled())),
            shared.isDelayPacketsEnabled() ? autismclient.gui.f.b.CachePool.Inner_DataUtil.write : autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
            () -> {
               boolean newValue = !shared.isDelayPacketsEnabled();
               autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
               int sent = module.toggleDelayPackets(newValue);
               module.notifyDelayToggle(newValue, sent);
            }
         )
      );
      int thirdWidth = (defaultWidth - 10) / 3;
      buttons.add(
         new autismclient.gui.ui.widgets.SimpleClickButton(
            baseX, baseY + 96, thirdWidth, defaultHeight, Component.literal("Clear Q"), autismclient.gui.f.b.CachePool.Inner_DataUtil.update, () -> {
               autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
               int count = module.clearPacketQueue();
               module.notifyQueueCleared(count);
            }
         )
      );
      buttons.add(
         new autismclient.gui.ui.widgets.SimpleClickButton(
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
         new autismclient.gui.ui.widgets.SimpleClickButton(
            baseX + thirdWidth * 2 + 10,
            baseY + 96,
            thirdWidth,
            defaultHeight,
            Component.literal("Pkt Log"),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            () -> autismclient.modules.ModuleRuntimeManager.getInstance().togglePacketLoggerOverlay()
         )
      );
      int halfWidth = (defaultWidth - 5) / 2;
      buttons.add(
         new autismclient.gui.ui.widgets.SimpleClickButton(
            baseX,
            baseY + 120,
            halfWidth,
            defaultHeight,
            Component.literal("Save GUI"),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            () -> GuiDesyncHelper.init(client)
         )
      );
      buttons.add(
         new autismclient.gui.ui.widgets.SimpleClickButton(
            baseX + halfWidth + 5,
            baseY + 120,
            halfWidth,
            defaultHeight,
            Component.literal("Load GUI"),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            () -> {
               if (shared.getStoredGuiScreen() != null && shared.getStoredContainerMenu() != null) {
                  client.execute(() -> {
                     client.gui.setScreen(shared.getStoredGuiScreen());
                     if (client.player != null) {
                        client.player.containerMenu = shared.getStoredContainerMenu();
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
         new autismclient.gui.ui.widgets.SimpleClickButton(
            baseX,
            baseY + 144,
            defaultWidth + 45,
            defaultHeight,
            Component.literal("Disconnect and send packets"),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.update,
            () -> {
               autismclient.modules.ModuleRuntimeManager.getInstance().setDelayGuiPackets(false);
               if (client.getConnection() != null) {
                  int sent = shared.flushDelayedPackets(client.getConnection());
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
