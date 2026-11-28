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
   private boolean dragging = false;
   private double dragOffsetX = 0.0;
   private double dragOffsetY = 0.0;
   private int scrollOffset = 0;
   private boolean collapsed = false;
   private boolean visible = false;
   private boolean configurationOnly = false;
   private int selectedPacketId = -1;
   private boolean scrollToSelectionPending = false;
   private long queueChangedAtMillis = 0L;
   private String queueSignature = "";
   private boolean queueChangedSinceSort = false;
   private boolean draggingScrollbar = false;
   private int scrollGrabOffset = 0;
   private final Font font;
   private final autismclient.gui.f.b.CharEncoder theme = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.widgets.ScrollOffsetController scroll = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private List<AutismSessionState.DelayedPacket> displayedQueue = new ArrayList<>();
   private final List<AutismQueueEditorOverlay.ButtonRegion> buttonRegions = new ArrayList<>();
   private final List<AutismQueueEditorOverlay.PacketRowRegion> removeRegions = new ArrayList<>();
   private String hoverTooltip = null;
   private int tooltipX = 0;
   private int tooltipY = 0;

   public AutismQueueEditorOverlay(Font textRenderer) {
      this.font = textRenderer;
      this.panelWidth = this.defaultWidth();
      this.panelHeight = this.defaultPanelHeight();
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
      return this.minPanelHeight();
   }

   @Override
   public boolean isResizable() {
      return true;
   }

   @Override
   protected int getResizeMinHeight() {
      return this.listTopOffset() + 3 * this.rowHeight() + this.listBottomPad();
   }

   @Override
   protected void onSizeChanged() {
      if (!this.hasCustomStoredSize()) {
         this.panelWidth = this.defaultWidth();
      }
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.panelHeight, this.visible, this.collapsed);
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
         this.collapsed = clamped.collapsed;
      }
   }

   public boolean isTimedCaptureMode() {
      return AutismSessionState.getInstance().isTimedCaptureMode();
   }

   @Override
   public void setVisible(boolean visible) {
      this.visible = visible;
      if (visible) {
         this.scrollOffset = 0;
         this.scroll.jumpTo(0, 0);
         OverlayManager.getInstance().bringToFront(this);
      }

      this.saveState();
   }

   public void toggleVisible() {
      this.setVisible(!this.visible);
   }

   public void setConfigurationOnly(boolean configurationOnly) {
      this.configurationOnly = configurationOnly;
   }

   @Override
   public boolean isVisible() {
      return this.visible;
   }

   @Override
   public boolean isCollapsed() {
      return this.collapsed;
   }

   @Override
   public void setCollapsed(boolean collapsed) {
      if (this.collapsed != collapsed) {
         this.collapsed = collapsed;
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

      List<AutismSessionState.DelayedPacket> displayQueue = this.displayedQueue.isEmpty() ? this.activeQueue() : this.displayedQueue;
      int panelHeight = this.collapsed
         ? 14
         : this.clampToScreen(this, new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.panelHeightFor(displayQueue), this.visible, this.collapsed)).height;
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

   private int listTopY() {
      return this.panelY + this.listTopOffset();
   }

   private int listTopOffset() {
      int toolbarRows = LanSyncManager.getInstance().isInSession() ? 4 : 3;
      return this.toolbarTop() + toolbarRows * this.toolbarButtonHeight() + (toolbarRows - 1) * this.toolbarGap() + this.toolbarBottomPad();
   }

   private int listX() {
      return this.panelX + this.panelPadding();
   }

   private int listWidth() {
      return autismclient.gui.ui.layout.PanelLayoutMath.innerWidth(this.panelWidth, this.panelPadding());
   }

   private int listContentWidth(boolean hasScrollbar) {
      return Math.max(40, autismclient.gui.ui.layout.PanelLayoutMath.contentWidth(this.listWidth(), hasScrollbar, this.scrollbarWidth()));
   }

   private int rowWidth() {
      return this.listWidth();
   }

   private int buttonWidthFor(String label, int minWidth, int maxWidth) {
      return autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.theme, autismclient.gui.f.b.l.read, label, 5, minWidth, maxWidth);
   }

   private int listHeightFor(int panelHeight) {
      return Math.max(this.rowHeight() + 4, panelHeight - this.listTopOffset() - this.panelPadding());
   }

   private int listVisibleHeight(int panelHeight) {
      return this.alignHeightToStep(Math.max(0, this.listHeightFor(panelHeight) - 2), this.rowHeight());
   }

   private int listClipTop() {
      return this.listTopY() - 1;
   }

   private int listClipBottom(int panelHeight) {
      return this.listClipTop() + this.listVisibleHeight(panelHeight);
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics(int panelHeight) {
      int visibleHeight = this.listVisibleHeight(panelHeight);
      int contentHeight = this.displayedQueue.size() * this.rowHeight();
      int maxScroll = Math.max(0, contentHeight - visibleHeight);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
         contentHeight,
         Math.max(1, visibleHeight),
         this.listX() + this.listWidth() - 5,
         this.listClipTop(),
         4,
         Math.max(1, this.listHeightFor(panelHeight) - 2),
         this.scroll.tickAndClamp(0.0F, maxScroll)
      );
   }

   private int removeButtonX(int rowX, int rowWidth) {
      return rowX + rowWidth - this.removeButtonSize() - this.panelPadding();
   }

   private int delayButtonWidth() {
      return this.delayButtonMinWidth();
   }

   private int delayButtonX(int rowX, int rowWidth) {
      return this.removeButtonX(rowX, rowWidth) - 4 - this.delayButtonWidth();
   }

   private int rowLabelMaxWidth(int rowX, int rowWidth) {
      return Math.max(40, this.delayButtonX(rowX, rowWidth) - 14 - 2 - (rowX + 8) - 6);
   }

   private int panelHeightFor(List<AutismSessionState.DelayedPacket> displayQueue) {
      int size = displayQueue.size();
      int rows = size == 0 ? 1 : Math.min(size, this.maxVisibleRows());
      return this.resolveWindowHeight(Math.max(this.getMinHeight(), this.listTopOffset() + rows * this.rowHeight() + this.listBottomPad()), this.getResizeMinHeight());
   }

   private AutismSessionState.DelayedPacket selectedPacket(List<AutismSessionState.DelayedPacket> queue) {
      if (this.selectedPacketId == -1) {
         return null;
      }

      for (AutismSessionState.DelayedPacket packet : queue) {
         if (packet.getId() == this.selectedPacketId) {
            return packet;
         }
      }

      this.selectedPacketId = -1;
      return null;
   }

   private void clearSelection() {
      this.selectedPacketId = -1;
   }

   private List<AutismSessionState.DelayedPacket> activeQueue() {
      AutismSessionState shared = AutismSessionState.getInstance();
      List<AutismSessionState.DelayedPacket> queue = shared.getSendingPackets();
      if (queue.isEmpty()) {
         queue = shared.getDelayedPackets();
      }

      return queue;
   }

   private int unusedAutoToggleButton(GuiGraphicsExtractor context, int x, int y, int h, String label, boolean active, int mouseX, int mouseY, String tip, Runnable action) {
      int width = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.theme, autismclient.gui.f.b.l.read, label, 5, 24, 96);
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, this.font, x, y, width, h, label, active, "queue:auto:" + label + ":" + x + ":" + y, mouseX, mouseY);
      this.buttonRegions.add(new AutismQueueEditorOverlay.ButtonRegion(x, y, width, h, action));
      this.setTooltipIfHovered(x, y, width, h, mouseX, mouseY, tip);
      return x + width;
   }

   private int ghostToolbarButton(GuiGraphicsExtractor context, int x, int y, int h, String label, int mouseX, int mouseY, String tip, Runnable action) {
      int width = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.theme, autismclient.gui.f.b.l.read, label, 5, 24, 96);
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(
         context, this.font, x, y, width, h, label, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST, true, mouseX, mouseY
      );
      this.buttonRegions.add(new AutismQueueEditorOverlay.ButtonRegion(x, y, width, h, action));
      this.setTooltipIfHovered(x, y, width, h, mouseX, mouseY, tip);
      return x + width;
   }

   private int unusedStatusToggleButton(GuiGraphicsExtractor context, int x, int y, int h, String label, boolean active, int mouseX, int mouseY, String tip, Runnable action) {
      int width = autismclient.gui.ui.layout.PanelLayoutMath.measureLabelWidth(this.font, this.theme, autismclient.gui.f.b.l.read, label, 5, 24, 112);
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, this.font, x, y, width, h, label, active, "queue:status:" + label + ":" + x + ":" + y, mouseX, mouseY);
      this.buttonRegions.add(new AutismQueueEditorOverlay.ButtonRegion(x, y, width, h, action));
      this.setTooltipIfHovered(x, y, width, h, mouseX, mouseY, tip);
      return x + width;
   }

   private void drawButton(
      GuiGraphicsExtractor context, int x, int y, int w, int h, String label, boolean active, int mouseX, int mouseY, String tip, Runnable action
   ) {
      this.drawTonedButton(context, x, y, w, h, label, autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY, active, mouseX, mouseY, tip, action);
   }

   private void drawTonedButton(
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
         this.buttonRegions.add(new AutismQueueEditorOverlay.ButtonRegion(x, y, w, h, action));
      }

      this.setTooltipIfHovered(x, y, w, h, mouseX, mouseY, tip);
   }

   private void unusedDrawPlainButton(GuiGraphicsExtractor context, int x, int y, int w, int h, String label, int mouseX, int mouseY, String tip, Runnable action) {
      this.drawButton(context, x, y, w, h, label, false, mouseX, mouseY, tip, action);
   }

   private void drawEnabledButton(
      GuiGraphicsExtractor context, int x, int y, int w, int h, String label, boolean enabled, int mouseX, int mouseY, String tip, Runnable action
   ) {
      autismclient.gui.ui.widgets.ButtonDrawHelper.init(context, this.font, x, y, w, h, label, enabled, "queue:state:" + label + ":" + x + ":" + y, mouseX, mouseY);
      this.buttonRegions.add(new AutismQueueEditorOverlay.ButtonRegion(x, y, w, h, action));
      this.setTooltipIfHovered(x, y, w, h, mouseX, mouseY, tip);
   }

   private boolean isSortedByDelay(List<AutismSessionState.DelayedPacket> queue) {
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

   private void noteQueueChange(List<AutismSessionState.DelayedPacket> queue) {
      String signature = this.signatureOf(queue);
      if (!signature.equals(this.queueSignature)) {
         this.queueSignature = signature;
         this.queueChangedAtMillis = System.currentTimeMillis();
         this.queueChangedSinceSort = true;
      }
   }

   private void autoSortQueue(AutismSessionState shared) {
      List<AutismSessionState.DelayedPacket> delayedQueue = shared.getDelayedPackets();
      this.noteQueueChange(delayedQueue);
      if (delayedQueue.size() >= 2 && this.queueChangedSinceSort && System.currentTimeMillis() - this.queueChangedAtMillis >= 800L) {
         if (this.isSortedByDelay(delayedQueue)) {
            this.queueChangedSinceSort = false;
         } else {
            shared.sortDelayedPacketsByDelay();
            this.queueSignature = this.signatureOf(shared.getDelayedPackets());
            this.queueChangedSinceSort = false;
            this.scrollToSelectionPending = true;
         }
      }
   }

   private void scrollToSelection(List<AutismSessionState.DelayedPacket> queue, AutismSessionState.DelayedPacket selectedPacket, int panelHeight) {
      if (selectedPacket != null && !queue.isEmpty()) {
         int selectedIndex = -1;

         for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getId() == selectedPacket.getId()) {
               selectedIndex = i;
               break;
            }
         }

         if (selectedIndex >= 0) {
            int visibleHeight = this.listVisibleHeight(panelHeight);
            int maxScroll = Math.max(0, queue.size() * this.rowHeight() - visibleHeight);
            int rowTop = selectedIndex * this.rowHeight();
            int rowBottom = rowTop + this.rowHeight();
            if (rowTop < this.scrollOffset) {
               this.scrollOffset = rowTop;
            } else if (rowBottom > this.scrollOffset + visibleHeight) {
               this.scrollOffset = rowBottom - visibleHeight;
            }

            this.scrollOffset = this.quantizeScrollOffset(this.scrollOffset, this.rowHeight(), maxScroll);
         }
      }
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.visible || this.isClosing()) {
         AutismSessionState shared = AutismSessionState.getInstance();
         List<AutismSessionState.DelayedPacket> staggeredQueue = shared.getSendingPackets();
         if (staggeredQueue.isEmpty()) {
            this.autoSortQueue(shared);
            this.displayedQueue = shared.getDelayedPackets();
         } else {
            this.displayedQueue = new ArrayList<>(staggeredQueue);
         }

         AutismSessionState.DelayedPacket selectedPacket = this.selectedPacket(this.displayedQueue);
         OverlayBounds clamped = this.clampToScreen(
            this, new OverlayBounds(this.panelX, this.panelY, this.resolveWindowWidth(this.panelWidth, this.getMinWidth()), this.panelHeightFor(this.displayedQueue), this.visible, this.collapsed)
         );
         this.panelX = clamped.x;
         this.panelY = clamped.y;
         this.panelWidth = clamped.width;
         int panelHeight = clamped.height;
         this.panelHeight = panelHeight;
         if (!this.collapsed && this.scrollToSelectionPending) {
            this.scrollToSelection(this.displayedQueue, selectedPacket, panelHeight);
            this.scrollToSelectionPending = false;
         }

         this.buttonRegions.clear();
         this.removeRegions.clear();
         this.hoverTooltip = null;
         String title = "Queue (" + this.displayedQueue.size() + ")";
         OverlayBounds bounds = new OverlayBounds(this.panelX, this.panelY, this.panelWidth, panelHeight, this.visible, this.collapsed);
         this.renderWindowFrame(context, mouseX, mouseY, bounds, title, this.collapsed, this.dragging);
         this.renderPinButton(context, mouseX, mouseY, bounds);
         boolean clipBody = this.beginBodyClip(context, bounds, this.collapsed);
         if (!clipBody) {
            this.renderInactiveDim(context, bounds, this.collapsed, this.dragging);
         } else {
            try {
               int gap = this.toolbarGap();
               int row1Y = this.panelY + this.toolbarTop();
               int row2Y = row1Y + this.toolbarButtonHeight() + gap;
               int row3Y = row2Y + this.toolbarButtonHeight() + gap;
               int row4Y = row3Y + this.toolbarButtonHeight() + gap;
               int leftX = this.panelX + this.panelPadding();
               int rowWidth = this.rowWidth();
               int rowRight = leftX + rowWidth;
               int bx = leftX;
               bx = this.ghostToolbarButton(context, bx, row1Y, this.toolbarButtonHeight(), "Copy", mouseX, mouseY, "Copy queue to clipboard (serialized)", () -> {
                  MacroClipboardCodec.copyQueue(this.activeQueue());
                  bi.init("Queue copied.");
               }) + gap;
               bx = this.ghostToolbarButton(context, bx, row1Y, this.toolbarButtonHeight(), "Paste", mouseX, mouseY, "Replace queue from clipboard", () -> {
                  if (AutismSessionState.getInstance().getSendingPackets().isEmpty()) {
                     AutismSessionState.DelayedPacket.resetIdCounter();
                  }

                  List<AutismSessionState.DelayedPacket> newQueue = MacroClipboardCodec.readPackets();
                  if (newQueue != null) {
                     AutismSessionState.getInstance().replaceDelayedPackets(newQueue);
                     this.clearSelection();
                     this.scrollOffset = 0;
                     AutismClientMessaging.sendPrefixed("§aQueue pasted!");
                  }
               }) + gap;
               bx = this.ghostToolbarButton(context, bx, row1Y, this.toolbarButtonHeight(), "Dupe", mouseX, mouseY, "Duplicate all packets in the queue", () -> {
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
               bx = this.ghostToolbarButton(context, bx, row1Y, this.toolbarButtonHeight(), "Ins", mouseX, mouseY, "Insert clipboard packets at the end of the queue", () -> {
                  List<AutismSessionState.DelayedPacket> newPackets = MacroClipboardCodec.readPackets();
                  if (newPackets != null && !newPackets.isEmpty()) {
                     List<AutismSessionState.DelayedPacket> combinedQueue = new ArrayList<>(AutismSessionState.getInstance().getDelayedPackets());
                     combinedQueue.addAll(newPackets);
                     AutismSessionState.getInstance().replaceDelayedPackets(combinedQueue);
                     this.clearSelection();
                     AutismClientMessaging.sendPrefixed("§aInserted " + newPackets.size() + " packet(s).");
                  }
               }) + gap;
               int replayWidth = Math.max(this.buttonWidthFor("Replay", 48, 84), rowRight - bx);
               this.drawTonedButton(
                  context,
                  bx,
                  row1Y,
                  replayWidth,
                  this.toolbarButtonHeight(),
                  "Replay",
                  autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                  true,
                  mouseX,
                  mouseY,
                  "Restore the last flushed queue back into the editor",
                  () -> {
                     if (AutismSessionState.getInstance().restoreLastFlushedQueue()) {
                        this.clearSelection();
                        this.scrollOffset = 0;
                        AutismClientMessaging.sendPrefixed("§aRestored - packets will regenerate on send");
                     } else {
                        AutismClientMessaging.sendPrefixed("§cNo history");
                     }
                  }
               );
               boolean flushOnDelay = autismclient.modules.ModuleRuntimeManager.getInstance().isFlushQueueOnDelayDisable();
               boolean captureAsExact = autismclient.modules.ModuleRuntimeManager.getInstance().isCaptureAsExact();
               int clearWidth = this.buttonWidthFor("Clear", 42, 58);
               int captureModeBtnWidth = this.toolbarButtonHeight();
               int flushWidth = Math.max(this.buttonWidthFor("FLUSH ON DELAY", 110, 150), rowWidth - clearWidth - captureModeBtnWidth - gap - gap);
               this.drawEnabledButton(
                  context,
                  leftX,
                  row2Y,
                  flushWidth,
                  this.toolbarButtonHeight(),
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
               this.drawTonedButton(
                  context,
                  leftX + flushWidth + gap,
                  row2Y,
                  captureModeBtnWidth,
                  this.toolbarButtonHeight(),
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
               this.drawTonedButton(
                  context,
                  leftX + flushWidth + gap + captureModeBtnWidth + gap,
                  row2Y,
                  clearWidth,
                  this.toolbarButtonHeight(),
                  "Clear",
                  autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER,
                  true,
                  mouseX,
                  mouseY,
                  "Remove all packets from the queue",
                  () -> {
                     int cleared = AutismSessionState.getInstance().cancelAndClearPacketQueues();
                     this.clearSelection();
                     this.scrollOffset = 0;
                     autismclient.modules.ModuleRuntimeManager.getInstance().notifyQueueCleared(cleared);
                  }
               );
               boolean captureModeOn = AutismSessionState.getInstance().isTimedCaptureMode();
               String delayModeLabel = AutismSessionState.getInstance().getDelayUnit() == AutismSessionState.DelayUnit.TICKS ? "Ticks" : "Ms";
               int modeWidth = this.buttonWidthFor(delayModeLabel, 34, 44);
               int captureWidth = this.buttonWidthFor("Capture", 56, 72);
               int plus1Width = this.buttonWidthFor("+1", 22, 28);
               int plus10Width = this.buttonWidthFor("+10", 26, 34);
               int plus20Width = this.buttonWidthFor("+20", 26, 34);
               int sendWidth = Math.max(
                  this.buttonWidthFor("Send Q", 54, 82), rowWidth - modeWidth - captureWidth - plus1Width - plus10Width - plus20Width - gap * 5
               );
               this.drawTonedButton(
                  context,
                  leftX,
                  row3Y,
                  modeWidth,
                  this.toolbarButtonHeight(),
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
               this.drawEnabledButton(
                  context,
                  leftX + modeWidth + gap,
                  row3Y,
                  captureWidth,
                  this.toolbarButtonHeight(),
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
               this.drawTonedButton(
                  context,
                  adjustX,
                  row3Y,
                  plus1Width,
                  this.toolbarButtonHeight(),
                  "+1",
                  autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                  true,
                  mouseX,
                  mouseY,
                  "Add +1 to the selected packet, or apply a +1 incremental pattern to the whole queue",
                  () -> this.bumpDelays(1)
               );
               this.drawTonedButton(
                  context,
                  adjustX + plus1Width + gap,
                  row3Y,
                  plus10Width,
                  this.toolbarButtonHeight(),
                  "+10",
                  autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                  true,
                  mouseX,
                  mouseY,
                  "Add +10 to the selected packet, or apply a +10 incremental pattern to the whole queue",
                  () -> this.bumpDelays(10)
               );
               this.drawTonedButton(
                  context,
                  adjustX + plus1Width + gap + plus10Width + gap,
                  row3Y,
                  plus20Width,
                  this.toolbarButtonHeight(),
                  "+20",
                  autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                  true,
                  mouseX,
                  mouseY,
                  "Add +20 to the selected packet, or apply a +20 incremental pattern to the whole queue",
                  () -> this.bumpDelays(20)
               );
               this.drawTonedButton(
                  context,
                  rowRight - sendWidth,
                  row3Y,
                  sendWidth,
                  this.toolbarButtonHeight(),
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
                  int syncWidth = this.buttonWidthFor("Sync", 46, 58);
                  int syncExecWidth = Math.max(this.buttonWidthFor("Sync Exec", 92, 118), rowWidth - syncWidth - gap);
                  this.drawTonedButton(
                     context,
                     leftX,
                     row4Y,
                     syncWidth,
                     this.toolbarButtonHeight(),
                     "Sync",
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST,
                     !this.configurationOnly,
                     mouseX,
                     mouseY,
                     this.configurationOnly ? "Join a world to share packet queues" : "Broadcast this queue to all LAN peers",
                     this::shareQueueToLan
                  );
                  this.drawTonedButton(
                     context,
                     leftX + syncWidth + gap,
                     row4Y,
                     syncExecWidth,
                     this.toolbarButtonHeight(),
                     "Sync Exec",
                     autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS,
                     !this.configurationOnly,
                     mouseX,
                     mouseY,
                     this.configurationOnly ? "Join a world to execute packet queues" : "Tell LAN peers to execute their queues together",
                     () -> LanSyncManager.getInstance().triggerQueueFlushSync()
                  );
               }

               if (this.displayedQueue.isEmpty()) {
                  ThemedTextHelper.drawTone(
                     context,
                     this.font,
                     "Queue empty",
                     ThemedTextHelper.TextTone.MUTED,
                     this.panelX + 10,
                     UiLayoutMath.centerTextY(this.listTopY(), this.rowHeight(), this.theme.init_I_l(autismclient.gui.f.b.l.write), this.theme.flush()),
                     false
                  );
               } else {
                  int visibleHeight = this.listVisibleHeight(panelHeight);
                  int totalContentHeight = this.displayedQueue.size() * this.rowHeight();
                  int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
                  this.scrollOffset = this.quantizeScrollOffset(this.scrollOffset, this.rowHeight(), maxScroll);
                  this.scroll.setOffset(this.scrollOffset, maxScroll);
                  int drawScroll = this.scroll.tickAndClamp(delta, maxScroll);
                  int contentStartY = this.listTopY();
                  int listX = this.listX();
                  int listY = contentStartY - 2;
                  int listWidth = this.listWidth();
                  int listHeight = this.listHeightFor(panelHeight);
                  int clipTop = this.listClipTop();
                  int clipBottom = this.listClipBottom(panelHeight);
                  autismclient.gui.ui.widgets.ListRowRenderer.drawListFrame(context, listX, listY, listWidth, listHeight, selectedPacket != null);
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.scrollbarMetrics(panelHeight);
                  int listContentWidth = this.listContentWidth(scrollbarMetrics.isScrollable());
                  autismclient.gui.ui.UiClipStack.getInstance()
                     .push(context, autismclient.gui.f.NetHelper.init(listX + 1, clipTop, Math.max(0, listWidth - 2), Math.max(0, clipBottom - clipTop)));
                  int baseY = contentStartY - drawScroll;
                  int groupColorIndex = 0;
                  int lastDelay = -2147483648;

                  for (int i = 0; i < this.displayedQueue.size(); i++) {
                     AutismSessionState.DelayedPacket packet = this.displayedQueue.get(i);
                     int itemY = baseY + i * this.rowHeight();
                     int rowY = itemY - 1;
                     if (rowY + this.rowHeight() > clipTop && rowY < clipBottom) {
                        boolean grouped = false;
                        if (i > 0 && this.displayedQueue.get(i - 1).getDelay() == packet.getDelay()) {
                           grouped = true;
                        }

                        if (i < this.displayedQueue.size() - 1 && this.displayedQueue.get(i + 1).getDelay() == packet.getDelay()) {
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
                        int labelMaxWidth = this.rowLabelMaxWidth(rowX, rowW);
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
                        int delayBtnWidth = this.delayButtonWidth();
                        int delayBtnX = this.delayButtonX(rowX, rowW);
                        int buttonY = rowY + Math.max(0, (this.rowHeight() - this.removeButtonSize()) / 2);
                        int modeBtnW = 14;
                        int modeBtnX = delayBtnX - modeBtnW - 2;
                        boolean exact = packet.isExactReplay();
                        autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                           context,
                           this.font,
                           modeBtnX,
                           buttonY,
                           modeBtnW,
                           this.removeButtonSize(),
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
                           this.removeButtonSize(),
                           delayText,
                           autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY,
                           true,
                           mouseX,
                           mouseY
                        );
                        int removeBtnX = this.removeButtonX(rowX, rowW);
                        boolean removeHovered = mouseX >= removeBtnX
                           && mouseX < removeBtnX + this.removeButtonSize()
                           && mouseY >= buttonY
                           && mouseY < buttonY + this.removeButtonSize();
                        autismclient.gui.ui.widgets.ListRowRenderer.drawDeleteButtonSquare(context, removeBtnX, buttonY, this.removeButtonSize(), removeHovered);
                        autismclient.gui.ui.widgets.ListRowRenderer.drawRowSeparator(context, rowX, rowY + this.rowHeight(), rowW);
                        this.removeRegions
                           .add(
                              new AutismQueueEditorOverlay.PacketRowRegion(
                                 packet, rowX, rowY, rowW, this.rowHeight(), delayBtnX, delayBtnWidth, removeBtnX, this.removeButtonSize(), modeBtnX, modeBtnW
                              )
                           );
                     }
                  }

                  autismclient.gui.ui.UiClipStack.getInstance().pop(context);
                  autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(context, scrollbarMetrics, scrollbarMetrics.containsTrack(mouseX, mouseY), this.draggingScrollbar);
               }
            } finally {
               this.endBodyClip(context, clipBody);
               this.renderInactiveDim(context, bounds, this.collapsed, this.dragging);
            }

            if (!this.collapsed && this.hoverTooltip != null) {
               context.nextStratum();
               this.drawTooltip(context, this.hoverTooltip, this.tooltipX, this.tooltipY);
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
      List<AutismSessionState.DelayedPacket> displayQueue = this.displayedQueue.isEmpty() ? this.activeQueue() : this.displayedQueue;
      int panelHeight = this.collapsed
         ? 14
         : this.clampToScreen(this, new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.panelHeightFor(displayQueue), this.visible, this.collapsed)).height;
      if (button == 0 && mouseX >= this.panelX && mouseX <= this.panelX + this.panelWidth && mouseY >= this.panelY && mouseY <= this.panelY + 14) {
         OverlayBounds bounds = new OverlayBounds(this.panelX, this.panelY, this.panelWidth, panelHeight, this.visible, this.collapsed);
         if (this.isOverPinButton(mouseX, mouseY, bounds)) {
            this.togglePinned();
            return true;
         } else if (this.isOverHeaderCloseButton(mouseX, mouseY, bounds)) {
            this.setVisible(false);
            return true;
         } else {
            this.dragging = true;
            this.dragOffsetX = mouseX - this.panelX;
            this.dragOffsetY = mouseY - this.panelY;
            return true;
         }
      } else {
         if (this.collapsed) {
            return false;
         }

         if (button == 0) {
            for (AutismQueueEditorOverlay.ButtonRegion region : this.buttonRegions) {
               if (region.contains(mouseX, mouseY)) {
                  region.action.run();
                  return true;
               }
            }
         }

         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.scrollbarMetrics(panelHeight);
         if (button == 0 && scrollbarMetrics.isScrollable() && scrollbarMetrics.containsTrack(mouseX, mouseY)) {
            this.draggingScrollbar = true;
            this.scrollGrabOffset = scrollbarMetrics.containsThumb(mouseX, mouseY) ? (int)Math.round(mouseY) - scrollbarMetrics.thumbY() : scrollbarMetrics.thumbHeight() / 2;
            this.scrollOffset = this.quantizeScrollOffset(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(scrollbarMetrics, mouseY, this.scrollGrabOffset), this.rowHeight(), scrollbarMetrics.maxScroll());
            this.scroll.jumpTo(this.scrollOffset, scrollbarMetrics.maxScroll());
            return true;
         }

         for (AutismQueueEditorOverlay.PacketRowRegion region : this.removeRegions) {
            if (region.contains(mouseX, mouseY)) {
               if (region.inRemove(mouseX, mouseY) && button == 0) {
                  shared.removeDelayedPacket(region.packet);
                  if (this.selectedPacketId == region.packet.getId()) {
                     this.clearSelection();
                  }

                  return true;
               }

               if (region.inMode(mouseX, mouseY) && button == 0) {
                  AutismSessionState.ReplayMode next = region.packet.isExactReplay() ? AutismSessionState.ReplayMode.REGENERATE : AutismSessionState.ReplayMode.EXACT;
                  region.packet.setReplayMode(next);
                  return true;
               }

               if (region.inDelay(mouseX, mouseY)) {
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
                  this.selectedPacketId = this.selectedPacketId == region.packet.getId() ? -1 : region.packet.getId();
                  return true;
               }
            }
         }

         return this.containsPoint(mouseX, mouseY);
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.dragging && button == 0) {
         OverlayBounds nextBounds = this.clampToScreen(
            this, new OverlayBounds((int)(mouseX - this.dragOffsetX), (int)(mouseY - this.dragOffsetY), this.panelWidth, this.panelHeight, this.visible, this.collapsed)
         );
         this.panelX = nextBounds.x;
         this.panelY = nextBounds.y;
         return true;
      } else if (this.draggingScrollbar && button == 0) {
         List<AutismSessionState.DelayedPacket> displayQueue = this.displayedQueue.isEmpty() ? this.activeQueue() : this.displayedQueue;
         int panelHeight = this.collapsed
            ? 14
            : this.clampToScreen(this, new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.panelHeightFor(displayQueue), this.visible, this.collapsed)).height;
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.scrollbarMetrics(panelHeight);
         this.scrollOffset = this.quantizeScrollOffset(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(scrollbarMetrics, mouseY, this.scrollGrabOffset), this.rowHeight(), scrollbarMetrics.maxScroll());
         this.scroll.jumpTo(this.scrollOffset, scrollbarMetrics.maxScroll());
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0 && this.dragging) {
         this.dragging = false;
         this.saveState();
         return true;
      } else if (button == 0 && this.draggingScrollbar) {
         this.draggingScrollbar = false;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.visible && !this.collapsed && this.containsPoint(mouseX, mouseY)) {
         List<AutismSessionState.DelayedPacket> displayQueue = this.displayedQueue.isEmpty() ? this.activeQueue() : this.displayedQueue;
         int panelHeight = this.clampToScreen(this, new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.panelHeightFor(displayQueue), this.visible, this.collapsed)).height;
         int visibleHeight = this.listVisibleHeight(panelHeight);
         int maxScroll = Math.max(0, displayQueue.size() * this.rowHeight() - visibleHeight);
         if (maxScroll <= 0) {
            return false;
         }

         this.scrollOffset = this.quantizeScrollOffset(this.scrollOffset - (int)(Math.signum(amount) * this.rowHeight()), this.rowHeight(), maxScroll);
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

   private void shareQueueToLan() {
      if (!this.configurationOnly) {
         LanSyncManager sync = LanSyncManager.getInstance();
         if (!sync.isInSession()) {
            AutismClientMessaging.sendPrefixed("§cNot in LAN session");
         } else {
            List<AutismSessionState.DelayedPacket> queue = this.activeQueue();
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

   private void setTooltipIfHovered(int x, int y, int w, int h, int mx, int my, String tip) {
      if (mx >= x && mx < x + w && my >= y && my < y + h) {
         this.hoverTooltip = tip;
         this.tooltipX = mx + 8;
         this.tooltipY = my + 12;
      }
   }

   private void drawTooltip(GuiGraphicsExtractor ctx, String text, int x, int y) {
      TooltipRenderer.render(autismclient.gui.f.UiHelper.init(ctx, this.font, x, y), text, x, y, 200);
   }

   private void bumpDelays(int increment) {
      List<AutismSessionState.DelayedPacket> queue = this.activeQueue();
      if (queue.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§cQueue is empty");
      } else if (this.selectedPacketId != -1) {
         for (AutismSessionState.DelayedPacket packet : queue) {
            if (packet.getId() == this.selectedPacketId) {
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

   private int defaultPanelHeight() {
      int toolbar = this.toolbarTop() + 3 * this.toolbarButtonHeight() + 2 * this.toolbarGap() + this.toolbarBottomPad();
      return toolbar + 3 * this.rowHeight() + this.listBottomPad();
   }

   private int minPanelHeight() {
      return this.listTopOffset() + this.rowHeight() + this.listBottomPad();
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int removeButtonSize() {
      return 12;
   }

   private int toolbarButtonHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int maxVisibleRows() {
      return 12;
   }

   private int delayButtonMinWidth() {
      return 40;
   }

   private int scrollbarWidth() {
      return 12;
   }

   private int panelPadding() {
      return 4;
   }

   private int toolbarGap() {
      return 2;
   }

   private int toolbarTop() {
      return 18;
   }

   private int toolbarBottomPad() {
      return 4;
   }

   private int listBottomPad() {
      return 6;
   }

   private static class ButtonRegion {
      final int x;
      final int y;
      final int width;
      final int height;
      final Runnable action;

      private ButtonRegion(int x, int y, int width, int height, Runnable action) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.action = action;
      }

      private boolean contains(double mouseX, double mouseY) {
         return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
      }
   }

   private static class PacketRowRegion {
      final AutismSessionState.DelayedPacket packet;
      final int x;
      final int y;
      final int width;
      final int height;
      final int delayX;
      final int delayWidth;
      final int removeX;
      final int removeWidth;
      final int modeX;
      final int modeWidth;

      private PacketRowRegion(
         AutismSessionState.DelayedPacket packet, int x, int y, int width, int height, int delayX, int delayWidth, int removeX, int removeWidth, int modeX, int modeWidth
      ) {
         this.packet = packet;
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.delayX = delayX;
         this.delayWidth = delayWidth;
         this.removeX = removeX;
         this.removeWidth = removeWidth;
         this.modeX = modeX;
         this.modeWidth = modeWidth;
      }

      private boolean contains(double mouseX, double mouseY) {
         return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
      }

      private boolean inDelay(double mouseX, double mouseY) {
         return this.contains(mouseX, mouseY) && mouseX >= this.delayX && mouseX < this.delayX + this.delayWidth;
      }

      private boolean inRemove(double mouseX, double mouseY) {
         return this.contains(mouseX, mouseY) && mouseX >= this.removeX && mouseX < this.removeX + this.removeWidth;
      }

      private boolean inMode(double mouseX, double mouseY) {
         return this.contains(mouseX, mouseY) && mouseX >= this.modeX && mouseX < this.modeX + this.modeWidth;
      }
   }
}
