package autismclient.util;

import autismclient.gui.ui.widgets.TooltipRenderer;
import autismclient.gui.ui.widgets.UiLayoutMath;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class AutismQueueEditorOverlay extends DraggableOverlayWindow {
   private static final Minecraft MINECRAFT = Minecraft.getInstance();
   private int panelX = 100;
   private int panelY = 100;
   private int panelWidth = 248;
   private int panelHeight = 176;
   private boolean write_Z = false;
   private double dragOffsetX = 0.0;
   private double dragOffsetY = 0.0;
   private int push = 0;
   private boolean update_Z = false;
   private boolean visible = false;
   private boolean configurationOnly = false;
   private int pop = -1;
   private boolean close_Z = false;
   private long init_J = 0L;
   private String queueSignature = "";
   private boolean open_Z = false;
   private boolean poll_Z = false;
   private int tick = 0;
   private final Font font;
   private final autismclient.gui.f.b.CharEncoder theme = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.widgets.ScrollOffsetController scroll = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private List<AutismSessionState.DelayedPacket> init_List = new ArrayList<>();
   private final List<AutismQueueEditorOverlay.Inner_DataUtil> buttonRegions = new ArrayList<>();
   private final List<AutismQueueEditorOverlay.Inner_StreamUtil> removeRegions = new ArrayList<>();
   private String hoverTooltip = null;
   private int run = 0;
   private int get = 0;

   public AutismQueueEditorOverlay(Font textRenderer) {
      this.font = textRenderer;
      this.panelWidth = this.defaultWidth();
      this.panelHeight = this.clear();
   }

   @Override
   protected boolean supportsPinning() {
      return true;
   }

   @Override
   public int getMinWidth() {
      return this.defaultWidth();
   }

   @Override
   public int getMinHeight() {
      return this.reset();
   }

   @Override
   public boolean isResizable() {
      return true;
   }

   @Override
   protected int getResizeMinHeight() {
      return this.pop_I_23() + 3 * this.rowHeight() + this.queue();
   }

   @Override
   protected void onSizeChanged() {
      if (!this.hasCustomStoredSize()) {
         this.panelWidth = this.defaultWidth();
      }
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.panelHeight, this.visible, this.update_Z);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.clampToScreen(this, bounds);
         this.panelX = clamped.x;
         this.panelY = clamped.y;
         this.panelWidth = clamped.width;
         this.panelHeight = clamped.height;
         this.visible = clamped.visible;
         this.update_Z = clamped.collapsed;
      }
   }

   public boolean isTimedCaptureMode() {
      return AutismSessionState.getInstance().isTimedCaptureMode();
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;
      if (visible) {
         this.push = 0;
         this.scroll.jumpTo(0, 0);
         OverlayManager.getInstance().bringToFront(this);
      }

      this.saveState();
   }

   public void toggleVisible() {
      this.setVisible(!this.visible);
   }

   public void load(boolean configurationOnly) {
      this.configurationOnly = configurationOnly;
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public boolean isCollapsed() {
      return this.update_Z;
   }

   @Override
   public void setCollapsed(boolean collapsed) {
      if (this.update_Z != collapsed) {
         this.update_Z = collapsed;
         if (collapsed) {
            this.clearFocusAndDismissMenu();
         }

         this.saveBounds();
      }
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.visible) {
         return false;
      }

      List<AutismSessionState.DelayedPacket> displayQueue = this.init_List.isEmpty() ? this.init_List_12() : this.init_List;
      int panelHeight = this.update_Z
         ? 14
         : this.clampToScreen(this, new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.init_I(displayQueue), this.visible, this.update_Z)).height;
      return mouseX >= this.panelX && mouseX <= this.panelX + this.panelWidth && mouseY >= this.panelY && mouseY <= this.panelY + panelHeight;
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      if (!this.visible) {
         return false;
      }

      OverlayBounds bounds = this.getBounds();
      return mouseX >= this.panelX
         && mouseX <= this.panelX + this.panelWidth
         && mouseY >= this.panelY
         && mouseY <= this.panelY + 14
         && !this.isOverHeaderControl(mouseX, mouseY, bounds);
   }

   public void saveState() {
      AutismSessionState shared = AutismSessionState.getInstance();
      shared.setQueueEditorVisible(this.visible);
      shared.setQueueEditorX(this.panelX);
      shared.setQueueEditorY(this.panelY);
      this.saveBounds();
   }

   public void restoreState() {
      AutismSessionState shared = AutismSessionState.getInstance();
      this.loadBounds();
      this.visible = shared.isQueueEditorVisible();
      this.panelX = shared.getQueueEditorX();
      this.panelY = shared.getQueueEditorY();
   }

   private int push_I_23() {
      return this.panelY + this.pop_I_23();
   }

   private int pop_I_23() {
      int toolbarRows = LanSyncManager.getInstance().isInSession() ? 4 : 3;
      return this.cache() + toolbarRows * this.next() + (toolbarRows - 1) * this.value() + this.state();
   }

   private int tick_I_21() {
      return this.panelX + this.data();
   }

   private int run_I_21() {
      return autismclient.gui.ui.layout.PanelLayoutMath.innerWidth(this.panelWidth, this.data());
   }

   private int init_I(boolean hasScrollbar) {
      return Math.max(40, autismclient.gui.ui.layout.PanelLayoutMath.contentWidth(this.run_I_21(), hasScrollbar, this.size()));
   }

   private int get_I_19() {
      return this.run_I_21();
   }

   private int init_I_Str_5(String label, int minWidth, int maxWidth) {
      return autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.theme, autismclient.gui.f.b.l.read, label, 5, minWidth, maxWidth);
   }

   private int init_I(int panelHeight) {
      return Math.max(this.rowHeight() + 4, panelHeight - this.pop_I_23() - this.data());
   }

   private int load(int panelHeight) {
      return this.alignHeightToStep(Math.max(0, this.init_I(panelHeight) - 2), this.rowHeight());
   }

   private int set_I_19() {
      return this.push_I_23() - 1;
   }

   private int read(int panelHeight) {
      return this.set_I_19() + this.load(panelHeight);
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics(int panelHeight) {
      int visibleHeight = this.load(panelHeight);
      int contentHeight = this.init_List.size() * this.rowHeight();
      int maxScroll = Math.max(0, contentHeight - visibleHeight);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
         contentHeight,
         Math.max(1, visibleHeight),
         this.tick_I_21() + this.run_I_21() - 5,
         this.set_I_19(),
         4,
         Math.max(1, this.init_I(panelHeight) - 2),
         this.scroll.tickAndClamp(0.0F, maxScroll)
      );
   }

   private int flush(int rowX, int rowWidth) {
      return rowX + rowWidth - this.stop() - this.data();
   }

   private int put() {
      return this.valid();
   }

   private int close(int rowX, int rowWidth) {
      return this.flush(rowX, rowWidth) - 4 - this.put();
   }

   private int open(int rowX, int rowWidth) {
      return Math.max(40, this.close(rowX, rowWidth) - 14 - 2 - (rowX + 8) - 6);
   }

   private int init_I(List<AutismSessionState.DelayedPacket> displayQueue) {
      int size = displayQueue.size();
      int rows = size == 0 ? 1 : Math.min(size, this.prev());
      return this.resolveWindowHeight(Math.max(this.getMinHeight(), this.pop_I_23() + rows * this.rowHeight() + this.queue()), this.getResizeMinHeight());
   }

   private AutismSessionState.DelayedPacket selectedPacket(List<AutismSessionState.DelayedPacket> queue) {
      if (this.pop == -1) {
         return null;
      }

      for (AutismSessionState.DelayedPacket packet : queue) {
         if (packet.getId() == this.pop) {
            return packet;
         }
      }

      this.pop = -1;
      return null;
   }

   private void clearSelection() {
      this.pop = -1;
   }

   private List<AutismSessionState.DelayedPacket> init_List_12() {
      AutismSessionState shared = AutismSessionState.getInstance();
      List<AutismSessionState.DelayedPacket> queue = shared.getSendingPackets();
      if (queue.isEmpty()) {
         queue = shared.getDelayedPackets();
      }

      return queue;
   }

   private int init(GuiGraphicsExtractor context, int x, int y, int h, String label, boolean active, int mouseX, int mouseY, String tip, Runnable action) {
      int width = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.theme, autismclient.gui.f.b.l.read, label, 5, 24, 96);
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, this.font, x, y, width, h, label, active, "queue:auto:" + label + ":" + x + ":" + y, mouseX, mouseY);
      this.buttonRegions.add(new AutismQueueEditorOverlay.Inner_DataUtil(x, y, width, h, action));
      this.init(x, y, width, h, mouseX, mouseY, tip);
      return x + width;
   }

   private int init(GuiGraphicsExtractor context, int x, int y, int h, String label, int mouseX, int mouseY, String tip, Runnable action) {
      int width = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.theme, autismclient.gui.f.b.l.read, label, 5, 24, 96);
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(
         context, this.font, x, y, width, h, label, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST, true, mouseX, mouseY
      );
      this.buttonRegions.add(new AutismQueueEditorOverlay.Inner_DataUtil(x, y, width, h, action));
      this.init(x, y, width, h, mouseX, mouseY, tip);
      return x + width;
   }

   private int load(GuiGraphicsExtractor context, int x, int y, int h, String label, boolean active, int mouseX, int mouseY, String tip, Runnable action) {
      int width = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.theme, autismclient.gui.f.b.l.read, label, 5, 24, 112);
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, this.font, x, y, width, h, label, active, "queue:status:" + label + ":" + x + ":" + y, mouseX, mouseY);
      this.buttonRegions.add(new AutismQueueEditorOverlay.Inner_DataUtil(x, y, width, h, action));
      this.init(x, y, width, h, mouseX, mouseY, tip);
      return x + width;
   }

   private void init(
      GuiGraphicsExtractor context, int x, int y, int w, int h, String label, boolean active, int mouseX, int mouseY, String tip, Runnable action
   ) {
      this.init(context, x, y, w, h, label, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, active, mouseX, mouseY, tip, action);
   }

   private void init(
      GuiGraphicsExtractor context,
      int x,
      int y,
      int w,
      int h,
      String label,
      autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant,
      boolean active,
      int mouseX,
      int mouseY,
      String tip,
      Runnable action
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, this.font, x, y, w, h, label, variant, active, mouseX, mouseY);
      if (active) {
         this.buttonRegions.add(new AutismQueueEditorOverlay.Inner_DataUtil(x, y, w, h, action));
      }

      this.init(x, y, w, h, mouseX, mouseY, tip);
   }

   private void init(GuiGraphicsExtractor context, int x, int y, int w, int h, String label, int mouseX, int mouseY, String tip, Runnable action) {
      this.init(context, x, y, w, h, label, false, mouseX, mouseY, tip, action);
   }

   private void load(
      GuiGraphicsExtractor context, int x, int y, int w, int h, String label, boolean enabled, int mouseX, int mouseY, String tip, Runnable action
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, this.font, x, y, w, h, label, enabled, "queue:state:" + label + ":" + x + ":" + y, mouseX, mouseY);
      this.buttonRegions.add(new AutismQueueEditorOverlay.Inner_DataUtil(x, y, w, h, action));
      this.init(x, y, w, h, mouseX, mouseY, tip);
   }

   private boolean init_Z(List<AutismSessionState.DelayedPacket> queue) {
      for (int i = 1; i < queue.size(); i++) {
         AutismSessionState.DelayedPacket previous = queue.get(i - 1);
         AutismSessionState.DelayedPacket current = queue.get(i);
         if (previous.getDelay() > current.getDelay()) {
            return false;
         }

         if (previous.getDelay() == current.getDelay() && previous.getId() > current.getId()) {
            return false;
         }
      }

      return true;
   }

   private String signatureOf(List<AutismSessionState.DelayedPacket> queue) {
      if (queue.isEmpty()) {
         return "";
      }

      StringBuilder signature = new StringBuilder(queue.size() * 16);

      for (AutismSessionState.DelayedPacket packet : queue) {
         signature.append(packet.getId()).append(':').append(packet.getDelay()).append(';');
      }

      return signature.toString();
   }

   private void init_V(List<AutismSessionState.DelayedPacket> queue) {
      String signature = this.signatureOf(queue);
      if (!signature.equals(this.queueSignature)) {
         this.queueSignature = signature;
         this.init_J = System.currentTimeMillis();
         this.open_Z = true;
      }
   }

   private void init(AutismSessionState shared) {
      List<AutismSessionState.DelayedPacket> delayedQueue = shared.getDelayedPackets();
      this.init_V(delayedQueue);
      if (delayedQueue.size() >= 2 && this.open_Z && System.currentTimeMillis() - this.init_J >= 800L) {
         if (this.init_Z(delayedQueue)) {
            this.open_Z = false;
         } else {
            shared.sortDelayedPacketsByDelay();
            this.queueSignature = this.signatureOf(shared.getDelayedPackets());
            this.open_Z = false;
            this.close_Z = true;
         }
      }
   }

   private void init_V_List_3(List<AutismSessionState.DelayedPacket> queue, AutismSessionState.DelayedPacket selectedPacket, int panelHeight) {
      if (selectedPacket != null && !queue.isEmpty()) {
         int selectedIndex = -1;

         for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getId() == selectedPacket.getId()) {
               selectedIndex = i;
               break;
            }
         }

         if (selectedIndex >= 0) {
            int visibleHeight = this.load(panelHeight);
            int maxScroll = Math.max(0, queue.size() * this.rowHeight() - visibleHeight);
            int rowTop = selectedIndex * this.rowHeight();
            int rowBottom = rowTop + this.rowHeight();
            if (rowTop < this.push) {
               this.push = rowTop;
            } else if (rowBottom > this.push + visibleHeight) {
               this.push = rowBottom - visibleHeight;
            }

            this.push = this.quantizeScrollOffset(this.push, this.rowHeight(), maxScroll);
         }
      }
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.visible || this.isClosing()) {
         AutismSessionState shared = AutismSessionState.getInstance();
         List<AutismSessionState.DelayedPacket> staggeredQueue = shared.getSendingPackets();
         if (staggeredQueue.isEmpty()) {
            this.init(shared);
            this.init_List = shared.getDelayedPackets();
         } else {
            this.init_List = new ArrayList<>(staggeredQueue);
         }

         AutismSessionState.DelayedPacket selectedPacket = this.selectedPacket(this.init_List);
         OverlayBounds clamped = this.clampToScreen(
            this, new OverlayBounds(this.panelX, this.panelY, this.resolveWindowWidth(this.panelWidth, this.getMinWidth()), this.init_I(this.init_List), this.visible, this.update_Z)
         );
         this.panelX = clamped.x;
         this.panelY = clamped.y;
         this.panelWidth = clamped.width;
         int panelHeight = clamped.height;
         this.panelHeight = panelHeight;
         if (!this.update_Z && this.close_Z) {
            this.init_V_List_3(this.init_List, selectedPacket, panelHeight);
            this.close_Z = false;
         }

         this.buttonRegions.clear();
         this.removeRegions.clear();
         this.hoverTooltip = null;
         String title = "Queue (" + this.init_List.size() + ")";
         OverlayBounds bounds = new OverlayBounds(this.panelX, this.panelY, this.panelWidth, panelHeight, this.visible, this.update_Z);
         this.renderWindowFrame(context, mouseX, mouseY, bounds, title, this.update_Z, this.write_Z);
         this.renderPinButton(context, mouseX, mouseY, bounds);
         boolean clipBody = this.beginBodyClip(context, bounds, this.update_Z);
         if (!clipBody) {
            this.renderInactiveDim(context, bounds, this.update_Z, this.write_Z);
         } else {
            try {
               int gap = this.value();
               int row1Y = this.panelY + this.cache();
               int row2Y = row1Y + this.next() + gap;
               int row3Y = row2Y + this.next() + gap;
               int row4Y = row3Y + this.next() + gap;
               int leftX = this.panelX + this.data();
               int rowWidth = this.get_I_19();
               int rowRight = leftX + rowWidth;
               int bx = leftX;
               bx = this.init(context, bx, row1Y, this.next(), "Copy", mouseX, mouseY, "Copy queue to clipboard (serialized)", () -> {
                  MacroClipboardCodec.copyQueue(this.init_List_12());
                  bi.init("Queue copied.");
               }) + gap;
               bx = this.init(context, bx, row1Y, this.next(), "Paste", mouseX, mouseY, "Replace queue from clipboard", () -> {
                  if (AutismSessionState.getInstance().getSendingPackets().isEmpty()) {
                     AutismSessionState.DelayedPacket.resetIdCounter();
                  }

                  List<AutismSessionState.DelayedPacket> newQueue = MacroClipboardCodec.read();
                  if (newQueue != null) {
                     AutismSessionState.getInstance().replaceDelayedPackets(newQueue);
                     this.clearSelection();
                     this.push = 0;
                     AutismClientMessaging.sendPrefixed("§aQueue pasted!");
                  }
               }) + gap;
               bx = this.init(context, bx, row1Y, this.next(), "Dupe", mouseX, mouseY, "Duplicate all packets in the queue", () -> {
                  List<AutismSessionState.DelayedPacket> currentQueue = AutismSessionState.getInstance().getDelayedPackets();
                  if (!currentQueue.isEmpty()) {
                     List<AutismSessionState.DelayedPacket> duplicatedQueue = new ArrayList<>(currentQueue);

                     for (AutismSessionState.DelayedPacket packetx : currentQueue) {
                        duplicatedQueue.add(new AutismSessionState.DelayedPacket(packetx.packet, packetx.getDelay(), packetx.getReplayMode()));
                     }

                     AutismSessionState.getInstance().replaceDelayedPackets(duplicatedQueue);
                     this.clearSelection();
                     AutismClientMessaging.sendPrefixed("§aDuplicated " + currentQueue.size() + " packet(s).");
                  }
               }) + gap;
               bx = this.init(context, bx, row1Y, this.next(), "Ins", mouseX, mouseY, "Insert clipboard packets at the end of the queue", () -> {
                  List<AutismSessionState.DelayedPacket> newPackets = MacroClipboardCodec.read();
                  if (newPackets != null && !newPackets.isEmpty()) {
                     List<AutismSessionState.DelayedPacket> combinedQueue = new ArrayList<>(AutismSessionState.getInstance().getDelayedPackets());
                     combinedQueue.addAll(newPackets);
                     AutismSessionState.getInstance().replaceDelayedPackets(combinedQueue);
                     this.clearSelection();
                     AutismClientMessaging.sendPrefixed("§aInserted " + newPackets.size() + " packet(s).");
                  }
               }) + gap;
               int replayWidth = Math.max(this.init_I_Str_5("Replay", 48, 84), rowRight - bx);
               this.init(
                  context,
                  bx,
                  row1Y,
                  replayWidth,
                  this.next(),
                  "Replay",
                  autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                  true,
                  mouseX,
                  mouseY,
                  "Restore the last flushed queue back into the editor",
                  () -> {
                     if (AutismSessionState.getInstance().restoreLastFlushedQueue()) {
                        this.clearSelection();
                        this.push = 0;
                        AutismClientMessaging.sendPrefixed("§aRestored - packets will regenerate on send");
                     } else {
                        AutismClientMessaging.sendPrefixed("§cNo history");
                     }
                  }
               );
               boolean flushOnDelay = autismclient.modules.ModuleRuntimeManager.getInstance().isFlushQueueOnDelayDisable();
               boolean captureAsExact = autismclient.modules.ModuleRuntimeManager.getInstance().isCaptureAsExact();
               int clearWidth = this.init_I_Str_5("Clear", 42, 58);
               int captureModeBtnWidth = this.next();
               int flushWidth = Math.max(this.init_I_Str_5("FLUSH ON DELAY", 110, 150), rowWidth - clearWidth - captureModeBtnWidth - gap - gap);
               this.load(
                  context,
                  leftX,
                  row2Y,
                  flushWidth,
                  this.next(),
                  "FLUSH ON DELAY",
                  flushOnDelay,
                  mouseX,
                  mouseY,
                  flushOnDelay
                     ? "When delay turns off, queued packets auto-flush"
                     : "When delay turns off, keep the queued packets until you flush or clear them",
                  () -> {
                     autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
                     boolean newValue = !module.isFlushQueueOnDelayDisable();
                     module.setFlushQueueOnDelayDisable(newValue);
                     AutismClientMessaging.sendPrefixed(newValue ? "§aFlush on Delay ON" : "§eFlush on Delay OFF");
                  }
               );
               this.init(
                  context,
                  leftX + flushWidth + gap,
                  row2Y,
                  captureModeBtnWidth,
                  this.next(),
                  captureAsExact ? "E" : "R",
                  captureAsExact ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
                  true,
                  mouseX,
                  mouseY,
                  captureAsExact ? "Capture as Exact: new packets stored verbatim" : "Capture as Regenerate: new packets rewritten on send",
                  () -> {
                     autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
                     boolean nowExact = !module.isCaptureAsExact();
                     module.setCaptureAsExact(nowExact);
                     AutismSessionState.ReplayMode bulkMode = nowExact ? AutismSessionState.ReplayMode.EXACT : AutismSessionState.ReplayMode.REGENERATE;
                     int updated = 0;

                     for (AutismSessionState.DelayedPacket qp : AutismSessionState.getInstance().getDelayedPackets()) {
                        if (qp.getReplayMode() != bulkMode) {
                           qp.setReplayMode(bulkMode);
                           updated++;
                        }
                     }

                     AutismClientMessaging.sendPrefixed(
                        nowExact
                           ? "§cCapture mode: EXACT" + (updated > 0 ? " (" + updated + " queued updated)" : "")
                           : "§aCapture mode: REGENERATE" + (updated > 0 ? " (" + updated + " queued updated)" : "")
                     );
                  }
               );
               this.init(
                  context,
                  leftX + flushWidth + gap + captureModeBtnWidth + gap,
                  row2Y,
                  clearWidth,
                  this.next(),
                  "Clear",
                  autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER,
                  true,
                  mouseX,
                  mouseY,
                  "Remove all packets from the queue",
                  () -> {
                     int cleared = AutismSessionState.getInstance().cancelAndClearPacketQueues();
                     this.clearSelection();
                     this.push = 0;
                     autismclient.modules.ModuleRuntimeManager.getInstance().notifyQueueCleared(cleared);
                  }
               );
               boolean captureModeOn = AutismSessionState.getInstance().isTimedCaptureMode();
               String delayModeLabel = AutismSessionState.getInstance().getDelayUnit() == AutismSessionState.DelayUnit.TICKS ? "Ticks" : "Ms";
               int modeWidth = this.init_I_Str_5(delayModeLabel, 34, 44);
               int captureWidth = this.init_I_Str_5("Capture", 56, 72);
               int plus1Width = this.init_I_Str_5("+1", 22, 28);
               int plus10Width = this.init_I_Str_5("+10", 26, 34);
               int plus20Width = this.init_I_Str_5("+20", 26, 34);
               int sendWidth = Math.max(
                  this.init_I_Str_5("Send Q", 54, 82), rowWidth - modeWidth - captureWidth - plus1Width - plus10Width - plus20Width - gap * 5
               );
               this.init(
                  context,
                  leftX,
                  row3Y,
                  modeWidth,
                  this.next(),
                  delayModeLabel,
                  autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                  true,
                  mouseX,
                  mouseY,
                  "Toggle delay unit: Ticks or milliseconds",
                  () -> {
                     AutismSessionState.getInstance().toggleDelayUnit();
                     String newMode = AutismSessionState.getInstance().getDelayUnit() == AutismSessionState.DelayUnit.TICKS ? "Ticks" : "Ms";
                     AutismClientMessaging.sendPrefixed("§aDelay mode: " + newMode);
                  }
               );
               this.load(
                  context,
                  leftX + modeWidth + gap,
                  row3Y,
                  captureWidth,
                  this.next(),
                  "Capture",
                  captureModeOn,
                  mouseX,
                  mouseY,
                  "Record real timing between captured packets",
                  () -> {
                     boolean newCapture = !AutismSessionState.getInstance().isTimedCaptureMode();
                     AutismSessionState.getInstance().setTimedCaptureMode(newCapture);
                     AutismClientMessaging.sendPrefixed(newCapture ? "§aCapture Real Delays ON" : "§eCapture Real Delays OFF");
                  }
               );
               int adjustX = leftX + modeWidth + gap + captureWidth + gap;
               this.init(
                  context,
                  adjustX,
                  row3Y,
                  plus1Width,
                  this.next(),
                  "+1",
                  autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                  true,
                  mouseX,
                  mouseY,
                  "Add +1 to the selected packet, or apply a +1 incremental pattern to the whole queue",
                  () -> this.init_V_2(1)
               );
               this.init(
                  context,
                  adjustX + plus1Width + gap,
                  row3Y,
                  plus10Width,
                  this.next(),
                  "+10",
                  autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                  true,
                  mouseX,
                  mouseY,
                  "Add +10 to the selected packet, or apply a +10 incremental pattern to the whole queue",
                  () -> this.init_V_2(10)
               );
               this.init(
                  context,
                  adjustX + plus1Width + gap + plus10Width + gap,
                  row3Y,
                  plus20Width,
                  this.next(),
                  "+20",
                  autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                  true,
                  mouseX,
                  mouseY,
                  "Add +20 to the selected packet, or apply a +20 incremental pattern to the whole queue",
                  () -> this.init_V_2(20)
               );
               this.init(
                  context,
                  rowRight - sendWidth,
                  row3Y,
                  sendWidth,
                  this.next(),
                  this.configurationOnly ? "In Game" : "Send Q",
                  autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
                  !this.configurationOnly,
                  mouseX,
                  mouseY,
                  this.configurationOnly ? "Join a world to send queued packets" : "Send all queued packets to the server",
                  () -> {
                     if (!this.configurationOnly && MINECRAFT.getConnection() != null) {
                        int count = AutismSessionState.getInstance().flushDelayedPackets(MINECRAFT.getConnection());
                        autismclient.modules.ModuleRuntimeManager.getInstance().notifyQueueFlushed(count);
                     }
                  }
               );
               boolean inLanSession = LanSyncManager.getInstance().isInSession();
               if (inLanSession) {
                  int syncWidth = this.init_I_Str_5("Sync", 46, 58);
                  int syncExecWidth = Math.max(this.init_I_Str_5("Sync Exec", 92, 118), rowWidth - syncWidth - gap);
                  this.init(
                     context,
                     leftX,
                     row4Y,
                     syncWidth,
                     this.next(),
                     "Sync",
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                     !this.configurationOnly,
                     mouseX,
                     mouseY,
                     this.configurationOnly ? "Join a world to share packet queues" : "Broadcast this queue to all LAN peers",
                     this::close_V_14
                  );
                  this.init(
                     context,
                     leftX + syncWidth + gap,
                     row4Y,
                     syncExecWidth,
                     this.next(),
                     "Sync Exec",
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
                     !this.configurationOnly,
                     mouseX,
                     mouseY,
                     this.configurationOnly ? "Join a world to execute packet queues" : "Tell LAN peers to execute their queues together",
                     () -> LanSyncManager.getInstance().triggerQueueFlushSync()
                  );
               }

               if (this.init_List.isEmpty()) {
                  ThemedTextHelper.init(
                     context,
                     this.font,
                     "Queue empty",
                     ThemedTextHelper.TextTone.MUTED,
                     this.panelX + 10,
                     UiLayoutMath.centerTextY(this.push_I_23(), this.rowHeight(), this.theme.init_I_l(autismclient.gui.f.b.l.write), this.theme.flush()),
                     false
                  );
               } else {
                  int visibleHeight = this.load(panelHeight);
                  int totalContentHeight = this.init_List.size() * this.rowHeight();
                  int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
                  this.push = this.quantizeScrollOffset(this.push, this.rowHeight(), maxScroll);
                  this.scroll.setOffset(this.push, maxScroll);
                  int drawScroll = this.scroll.tickAndClamp(delta, maxScroll);
                  int contentStartY = this.push_I_23();
                  int listX = this.tick_I_21();
                  int listY = contentStartY - 2;
                  int listWidth = this.run_I_21();
                  int listHeight = this.init_I(panelHeight);
                  int clipTop = this.set_I_19();
                  int clipBottom = this.read(panelHeight);
                  autismclient.gui.ui.widgets.ListRowRenderer.drawListFrame(context, listX, listY, listWidth, listHeight, selectedPacket != null);
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.scrollbarMetrics(panelHeight);
                  int listContentWidth = this.init_I(scrollbarMetrics.isScrollable());
                  autismclient.gui.ui.UiClipStack.getInstance()
                     .push(context, autismclient.gui.f.NetHelper.init(listX + 1, clipTop, Math.max(0, listWidth - 2), Math.max(0, clipBottom - clipTop)));
                  int baseY = contentStartY - drawScroll;
                  int groupColorIndex = 0;
                  int lastDelay = -2147483648;

                  for (int i = 0; i < this.init_List.size(); i++) {
                     AutismSessionState.DelayedPacket packet = this.init_List.get(i);
                     int itemY = baseY + i * this.rowHeight();
                     int rowY = itemY - 1;
                     if (rowY + this.rowHeight() > clipTop && rowY < clipBottom) {
                        boolean grouped = false;
                        if (i > 0 && this.init_List.get(i - 1).getDelay() == packet.getDelay()) {
                           grouped = true;
                        }

                        if (i < this.init_List.size() - 1 && this.init_List.get(i + 1).getDelay() == packet.getDelay()) {
                           grouped = true;
                        }

                        int textColor = UiColorPalette.textBright();
                        if (grouped) {
                           if (packet.getDelay() != lastDelay) {
                              groupColorIndex++;
                           }

                           textColor = groupColorIndex % 2 == 0 ? -12268289 : -21948;
                        }

                        lastDelay = packet.getDelay();
                        int rowX = listX + 1;
                        int rowW = Math.max(36, listContentWidth - 2);
                        boolean selected = selectedPacket != null && selectedPacket.getId() == packet.getId();
                        boolean hovered = mouseX >= rowX && mouseX < rowX + rowW && mouseY >= rowY && mouseY < rowY + this.rowHeight();
                        autismclient.gui.ui.widgets.ListRowRenderer.drawRow(
                           context,
                           this.font,
                           "",
                           rowX,
                           rowY,
                           rowW,
                           this.rowHeight(),
                           hovered,
                           selected,
                           grouped ? autismclient.gui.ui.widgets.ListRowRenderer.RowTone.WARNING : autismclient.gui.ui.widgets.ListRowRenderer.RowTone.NORMAL
                        );
                        String label = "#" + packet.getId() + " " + PacketNameResolver.nameOf(packet.packet);
                        String delayText = String.valueOf(packet.getDelay());
                        int labelMaxWidth = this.open(rowX, rowW);
                        String trimmed = autismclient.gui.f.b.k.init(
                                 this.font, label, this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), textColor
                              )
                              > labelMaxWidth
                           ? autismclient.gui.f.b.k.init(
                              this.font,
                              label,
                              Math.max(1, labelMaxWidth),
                              this.theme.init_Identifier_l(autismclient.gui.f.b.l.read),
                              textColor
                           )
                           : label;
                        int rowTextY = UiLayoutMath.centerTextY(
                           rowY, this.rowHeight(), this.theme.init_I_l(autismclient.gui.f.b.l.read), this.theme.flush()
                        );
                        autismclient.gui.f.b.k.init(
                           context,
                           this.font,
                           trimmed,
                           this.theme.init_Identifier_l(autismclient.gui.f.b.l.read),
                           selected ? UiColorPalette.selectedText() : textColor,
                           rowX + 7,
                           rowTextY,
                           false
                        );
                        int delayBtnWidth = this.put();
                        int delayBtnX = this.close(rowX, rowW);
                        int buttonY = rowY + Math.max(0, (this.rowHeight() - this.stop()) / 2);
                        int modeBtnW = 14;
                        int modeBtnX = delayBtnX - modeBtnW - 2;
                        boolean exact = packet.isExactReplay();
                        autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                           context,
                           this.font,
                           modeBtnX,
                           buttonY,
                           modeBtnW,
                           this.stop(),
                           exact ? "E" : "R",
                           exact ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
                           true,
                           mouseX,
                           mouseY
                        );
                        autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                           context,
                           this.font,
                           delayBtnX,
                           buttonY,
                           delayBtnWidth,
                           this.stop(),
                           delayText,
                           autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY,
                           true,
                           mouseX,
                           mouseY
                        );
                        int removeBtnX = this.flush(rowX, rowW);
                        boolean removeHovered = mouseX >= removeBtnX
                           && mouseX < removeBtnX + this.stop()
                           && mouseY >= buttonY
                           && mouseY < buttonY + this.stop();
                        autismclient.gui.ui.widgets.ListRowRenderer.drawDeleteButtonSquare(context, removeBtnX, buttonY, this.stop(), removeHovered);
                        autismclient.gui.ui.widgets.ListRowRenderer.drawRowSeparator(context, rowX, rowY + this.rowHeight(), rowW);
                        this.removeRegions
                           .add(
                              new AutismQueueEditorOverlay.Inner_StreamUtil(
                                 packet, rowX, rowY, rowW, this.rowHeight(), delayBtnX, delayBtnWidth, removeBtnX, this.stop(), modeBtnX, modeBtnW
                              )
                           );
                     }
                  }

                  autismclient.gui.ui.UiClipStack.getInstance().pop(context);
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(context, scrollbarMetrics, scrollbarMetrics.containsTrack(mouseX, mouseY), this.poll_Z);
               }
            } finally {
               this.endBodyClip(context, clipBody);
               this.renderInactiveDim(context, bounds, this.update_Z, this.write_Z);
            }

            if (!this.update_Z && this.hoverTooltip != null) {
               context.nextStratum();
               this.drawTooltip(context, this.hoverTooltip, this.run, this.get);
            }
         }
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.visible) {
         return false;
      }

      AutismSessionState shared = AutismSessionState.getInstance();
      List<AutismSessionState.DelayedPacket> displayQueue = this.init_List.isEmpty() ? this.init_List_12() : this.init_List;
      int panelHeight = this.update_Z
         ? 14
         : this.clampToScreen(this, new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.init_I(displayQueue), this.visible, this.update_Z)).height;
      if (button == 0 && mouseX >= this.panelX && mouseX <= this.panelX + this.panelWidth && mouseY >= this.panelY && mouseY <= this.panelY + 14) {
         OverlayBounds bounds = new OverlayBounds(this.panelX, this.panelY, this.panelWidth, panelHeight, this.visible, this.update_Z);
         if (this.isOverPinButton(mouseX, mouseY, bounds)) {
            this.togglePinned();
            return true;
         } else if (this.isOverHeaderCloseButton(mouseX, mouseY, bounds)) {
            this.setVisible(false);
            return true;
         } else {
            this.write_Z = true;
            this.dragOffsetX = mouseX - this.panelX;
            this.dragOffsetY = mouseY - this.panelY;
            return true;
         }
      } else {
         if (this.update_Z) {
            return false;
         }

         if (button == 0) {
            for (AutismQueueEditorOverlay.Inner_DataUtil region : this.buttonRegions) {
               if (region.init(mouseX, mouseY)) {
                  region.action.run();
                  return true;
               }
            }
         }

         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.scrollbarMetrics(panelHeight);
         if (button == 0 && scrollbarMetrics.isScrollable() && scrollbarMetrics.containsTrack(mouseX, mouseY)) {
            this.poll_Z = true;
            this.tick = scrollbarMetrics.containsThumb(mouseX, mouseY) ? (int)Math.round(mouseY) - scrollbarMetrics.thumbY() : scrollbarMetrics.thumbHeight() / 2;
            this.push = this.quantizeScrollOffset(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(scrollbarMetrics, mouseY, this.tick), this.rowHeight(), scrollbarMetrics.maxScroll());
            this.scroll.jumpTo(this.push, scrollbarMetrics.maxScroll());
            return true;
         }

         for (AutismQueueEditorOverlay.Inner_StreamUtil region : this.removeRegions) {
            if (region.init(mouseX, mouseY)) {
               if (region.read(mouseX, mouseY) && button == 0) {
                  shared.removeDelayedPacket(region.packet);
                  if (this.pop == region.packet.getId()) {
                     this.clearSelection();
                  }

                  return true;
               }

               if (region.write(mouseX, mouseY) && button == 0) {
                  AutismSessionState.ReplayMode next = region.packet.isExactReplay() ? AutismSessionState.ReplayMode.REGENERATE : AutismSessionState.ReplayMode.EXACT;
                  region.packet.setReplayMode(next);
                  return true;
               }

               if (region.load(mouseX, mouseY)) {
                  if (button == 0) {
                     shared.updateDelayedPacketDelay(region.packet, region.packet.getDelay() + 1);
                     return true;
                  }

                  if (button == 1) {
                     shared.updateDelayedPacketDelay(region.packet, Math.max(0, region.packet.getDelay() - 1));
                     return true;
                  }
               }

               if (button == 0) {
                  this.pop = this.pop == region.packet.getId() ? -1 : region.packet.getId();
                  return true;
               }
            }
         }

         return this.containsPoint(mouseX, mouseY);
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.write_Z && button == 0) {
         OverlayBounds nextBounds = this.clampToScreen(
            this, new OverlayBounds((int)(mouseX - this.dragOffsetX), (int)(mouseY - this.dragOffsetY), this.panelWidth, this.panelHeight, this.visible, this.update_Z)
         );
         this.panelX = nextBounds.x;
         this.panelY = nextBounds.y;
         return true;
      } else if (this.poll_Z && button == 0) {
         List<AutismSessionState.DelayedPacket> displayQueue = this.init_List.isEmpty() ? this.init_List_12() : this.init_List;
         int panelHeight = this.update_Z
            ? 14
            : this.clampToScreen(this, new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.init_I(displayQueue), this.visible, this.update_Z)).height;
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.scrollbarMetrics(panelHeight);
         this.push = this.quantizeScrollOffset(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(scrollbarMetrics, mouseY, this.tick), this.rowHeight(), scrollbarMetrics.maxScroll());
         this.scroll.jumpTo(this.push, scrollbarMetrics.maxScroll());
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0 && this.write_Z) {
         this.write_Z = false;
         this.saveState();
         return true;
      } else if (button == 0 && this.poll_Z) {
         this.poll_Z = false;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.visible && !this.update_Z && this.containsPoint(mouseX, mouseY)) {
         List<AutismSessionState.DelayedPacket> displayQueue = this.init_List.isEmpty() ? this.init_List_12() : this.init_List;
         int panelHeight = this.clampToScreen(this, new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.init_I(displayQueue), this.visible, this.update_Z)).height;
         int visibleHeight = this.load(panelHeight);
         int maxScroll = Math.max(0, displayQueue.size() * this.rowHeight() - visibleHeight);
         if (maxScroll <= 0) {
            return false;
         }

         this.push = this.quantizeScrollOffset(this.push - (int)(Math.signum(amount) * this.rowHeight()), this.rowHeight(), maxScroll);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return false;
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      return false;
   }

   private void close_V_14() {
      if (!this.configurationOnly) {
         LanSyncManager sync = LanSyncManager.getInstance();
         if (!sync.isInSession()) {
            AutismClientMessaging.sendPrefixed("§cNot in LAN session");
         } else {
            List<AutismSessionState.DelayedPacket> queue = this.init_List_12();
            if (queue.isEmpty()) {
               AutismClientMessaging.sendPrefixed("§cQueue is empty");
            } else {
               String queueData = MacroClipboardCodec.encodeQueue(queue);
               if (queueData == null) {
                  AutismClientMessaging.sendPrefixed("§cFailed to serialize queue");
               } else {
                  sync.broadcastQueueData(queueData);
                  AutismClientMessaging.sendPrefixed("§aQueue sent to " + sync.getSessionPeerCount() + " clients");
               }
            }
         }
      }
   }

   private void init(int x, int y, int w, int h, int mx, int my, String tip) {
      if (mx >= x && mx < x + w && my >= y && my < y + h) {
         this.hoverTooltip = tip;
         this.run = mx + 8;
         this.get = my + 12;
      }
   }

   private void drawTooltip(GuiGraphicsExtractor ctx, String text, int x, int y) {
      TooltipRenderer.init(autismclient.gui.f.UiHelper.init(ctx, this.font, x, y), text, x, y, 200);
   }

   private void init_V_2(int increment) {
      List<AutismSessionState.DelayedPacket> queue = this.init_List_12();
      if (queue.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§cQueue is empty");
      } else if (this.pop != -1) {
         for (AutismSessionState.DelayedPacket packet : queue) {
            if (packet.getId() == this.pop) {
               packet.setDelay(packet.getDelay() + increment);
               String modeStr = AutismSessionState.getInstance().getEffectiveDelayUnit() == AutismSessionState.DelayUnit.TICKS ? "ticks" : "ms";
               AutismClientMessaging.sendPrefixed("§aPacket #" + packet.getId() + " delay: " + packet.getDelay() + " " + modeStr);
               return;
            }
         }

         this.clearSelection();
         AutismClientMessaging.sendPrefixed("§cSelected packet not found");
      } else {
         for (int i = 0; i < queue.size(); i++) {
            queue.get(i).setDelay(i * increment);
         }

         AutismClientMessaging.sendPrefixed("§aApplied +" + increment + " incremental delays");
      }
   }

   private int defaultWidth() {
      return 248;
   }

   private int clear() {
      int toolbar = this.cache() + 3 * this.next() + 2 * this.value() + this.state();
      return toolbar + 3 * this.rowHeight() + this.queue();
   }

   private int reset() {
      return this.pop_I_23() + this.rowHeight() + this.queue();
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int stop() {
      return 12;
   }

   private int next() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int prev() {
      return 12;
   }

   private int valid() {
      return 40;
   }

   private int size() {
      return 12;
   }

   private int data() {
      return 4;
   }

   private int value() {
      return 2;
   }

   private int cache() {
      return 18;
   }

   private int state() {
      return 4;
   }

   private int queue() {
      return 6;
   }

   private static class Inner_DataUtil {
      final int x;
      final int load;
      final int read;
      final int write;
      final Runnable action;

      private Inner_DataUtil(int x, int y, int width, int height, Runnable action) {
         this.x = x;
         this.load = y;
         this.read = width;
         this.write = height;
         this.action = action;
      }

      private boolean init(double mouseX, double mouseY) {
         return mouseX >= this.x && mouseX < this.x + this.read && mouseY >= this.load && mouseY < this.load + this.write;
      }
   }

   private static class Inner_StreamUtil {
      final AutismSessionState.DelayedPacket packet;
      final int x;
      final int load;
      final int read;
      final int write;
      final int update;
      final int sync;
      final int flush;
      final int close;
      final int open;
      final int poll;

      private Inner_StreamUtil(
         AutismSessionState.DelayedPacket packet, int x, int y, int width, int height, int delayX, int delayWidth, int removeX, int removeWidth, int modeX, int modeWidth
      ) {
         this.packet = packet;
         this.x = x;
         this.load = y;
         this.read = width;
         this.write = height;
         this.update = delayX;
         this.sync = delayWidth;
         this.flush = removeX;
         this.close = removeWidth;
         this.open = modeX;
         this.poll = modeWidth;
      }

      private boolean init(double mouseX, double mouseY) {
         return mouseX >= this.x && mouseX < this.x + this.read && mouseY >= this.load && mouseY < this.load + this.write;
      }

      private boolean load(double mouseX, double mouseY) {
         return this.init(mouseX, mouseY) && mouseX >= this.update && mouseX < this.update + this.sync;
      }

      private boolean read(double mouseX, double mouseY) {
         return this.init(mouseX, mouseY) && mouseX >= this.flush && mouseX < this.flush + this.close;
      }

      private boolean write(double mouseX, double mouseY) {
         return this.init(mouseX, mouseY) && mouseX >= this.open && mouseX < this.open + this.poll;
      }
   }
}
