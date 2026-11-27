package autismclient.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class PacketQueueHudRenderer {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final long init_J = 50000000L;
   private static final int ROW_COLOR = -1581859;
   private static final int load_I = -601459;
   private static final int read_I = -6366544;
   private static final int write = -9184882;
   private static final Identifier init_Identifier = init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write);
   private static final Identifier load_Identifier = init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read);
   private static final Identifier read_Identifier = init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load);
   private static final int update = init_CharEncoder.load(autismclient.gui.f.b.l.write);
   private static final int sync = init_CharEncoder.load(autismclient.gui.f.b.l.read);
   private static boolean init_Z;
   private static String init_Str = null;
   private static int flush = -2147483648;
   private static int close = -2147483648;
   private static int open = -2147483648;
   private static int poll = -2147483648;
   private static String load_Str = "";
   private static int push;
   private static List<autismclient.gui.ui.layout.InfoCardRenderer.Inner_DataUtil> init_List = List.of();
   private static long load_J;

   private PacketQueueHudRenderer() {
   }

   public static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor context, Font textRenderer, int x, int y, int width, int maxLines) {
      init(context, textRenderer, x - 6, y - 8, width + 12, maxLines, true, true, true);
   }

   public static int layoutHeight(Font textRenderer, int panelWidth, int maxLines) {
      if (textRenderer == null) {
         return 0;
      }

      init_V(textRenderer, panelWidth, maxLines);
      return autismclient.gui.ui.layout.InfoCardRenderer.init(init_List.size());
   }

   public static int init(
      GuiGraphicsExtractor context, Font textRenderer, int x, int y, int panelWidth, int maxLines, boolean topBorder, boolean bottomBorder, boolean rightBorder
   ) {
      if (textRenderer == null) {
         return 0;
      }

      init_V(textRenderer, panelWidth, maxLines);
      return autismclient.gui.ui.layout.InfoCardRenderer.init(
         context, textRenderer, x, y, panelWidth, load_Str, init_List, push, 0, true, rightBorder, topBorder, bottomBorder
      );
   }

   private static void init_V(Font textRenderer, int panelWidth, int maxLines) {
      AutismSessionState shared = AutismSessionState.getInstance();
      boolean isSending = shared.hasSendingPackets();
      String modeStr = shared.getEffectiveDelayUnit() == AutismSessionState.DelayUnit.MS ? "ms" : "t";
      int queueRevision = shared.getQueueRevision();
      long now = System.nanoTime();
      if (init_Z_Z(isSending, modeStr, maxLines, panelWidth, queueRevision, now)) {
         init(textRenderer, shared.snapshotDelayQueue(isSending, maxLines), isSending, modeStr, maxLines, panelWidth, queueRevision, now);
      }
   }

   private static boolean init_Z_Z(boolean isSending, String modeStr, int maxLines, int panelWidth, int queueRevision, long now) {
      boolean layoutChanged = init_Z != isSending || !Objects.equals(init_Str, modeStr) || flush != maxLines || close != panelWidth;
      if (layoutChanged) {
         return true;
      } else if (init_List.isEmpty()) {
         return true;
      } else {
         return poll == queueRevision ? false : now - load_J >= 50000000L;
      }
   }

   private static void init(
      Font textRenderer, AutismSessionState.DelayQueueSnapshot snapshot, boolean isSending, String modeStr, int maxLines, int panelWidth, int queueRevision, long now
   ) {
      List<AutismSessionState.DelayedPacket> packets = snapshot.visiblePackets();
      int totalCount = snapshot.totalCount();
      int visibleCount = Math.min(packets.size(), maxLines);
      int contentWidth = panelWidth - 12;
      ArrayList<autismclient.gui.ui.layout.InfoCardRenderer.Inner_DataUtil> rows = new ArrayList<>(visibleCount + 2);
      rows.add(
         new autismclient.gui.ui.layout.InfoCardRenderer.Inner_DataUtil(
            autismclient.gui.f.b.k.init(
               textRenderer,
               isSending ? "Sending " + totalCount + " left [" + modeStr + "]" : totalCount + " queued [" + modeStr + "]",
               contentWidth,
               load_Identifier,
               -6366544
            ),
            load_Identifier,
            -6366544
         )
      );
      if (packets.isEmpty()) {
         rows.add(
            new autismclient.gui.ui.layout.InfoCardRenderer.Inner_DataUtil(
               autismclient.gui.f.b.k.init(textRenderer, "Queue empty", contentWidth, init_Identifier, update), init_Identifier, update
            )
         );
      } else {
         for (int i = 0; i < visibleCount; i++) {
            AutismSessionState.DelayedPacket qp = packets.get(i);
            String simpleName = PacketNameResolver.nameOf(qp.packet);
            String delayStr = qp.getDelay() > 0 ? " +" + qp.getDelay() + modeStr : "";
            String label = "#" + qp.getId() + " " + (qp.isExactReplay() ? "[Exact] " : "") + simpleName + delayStr;
            int color = isSending && i == 0 ? -601459 : -1581859;
            rows.add(
               new autismclient.gui.ui.layout.InfoCardRenderer.Inner_DataUtil(
                  autismclient.gui.f.b.k.init(textRenderer, label, contentWidth, load_Identifier, color), load_Identifier, color
               )
            );
         }
      }

      init_Z = isSending;
      init_Str = modeStr;
      flush = maxLines;
      close = panelWidth;
      open = totalCount;
      poll = queueRevision;
      load_Str = autismclient.gui.f.b.k.init(textRenderer, "PACKET QUEUE", contentWidth, read_Identifier, sync);
      push = isSending ? -9184882 : init_CharEncoder.run();
      init_List = rows;
      load_J = now;
   }
}
