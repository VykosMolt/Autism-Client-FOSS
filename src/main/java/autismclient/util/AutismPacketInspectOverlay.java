package autismclient.util;

import autismclient.gui.ui.widgets.UiLayoutMath;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class AutismPacketInspectOverlay extends DraggableOverlayWindow {
   private static final Minecraft MINECRAFT = Minecraft.getInstance();
   private static final int flush_I = 0;
   private static final int close_I = 8;
   private final Font font;
   private final autismclient.gui.f.b.CharEncoder theme = new autismclient.gui.f.b.CharEncoder();
   private boolean write_Z;
   private boolean update_Z;
   private boolean dragging;
   private double dragOffsetX;
   private double dragOffsetY;
   private int panelX = 120;
   private int panelY = 50;
   private int panelWidth = 220;
   private int panelHeight = 173;
   private PacketInspectorEngine.InspectionReport report;
   private AutismPacketLoggerOverlay.LoggedPacketEntry entry;
   private List<AutismPacketInspectOverlay.InspectLine> lines = Collections.emptyList();
   private boolean linesDirty = true;
   private int linesWidth = -1;
   private autismclient.gui.ui.layout.ScrollableListView listView = null;
   private String scrollKey = "";
   private int scrollOffset = 0;
   private AtomicReference<PacketInspectorEngine.PayloadAnalysis> pendingAnalysis;
   private PacketInspectorEngine.PayloadAnalysis analysis;
   private CompletableFuture<?> analysisTask;
   private String analysisKey = "";
   private boolean analysisRequested;
   private static final int get = 4;
   private static final int set = 128;
   private static final Map<String, Integer> savedScroll = new LinkedHashMap<String, Integer>() {
      @Override
      protected boolean removeEldestEntry(Entry<String, Integer> eldest) {
         return this.size() > 128;
      }
   };

   public AutismPacketInspectOverlay(Font textRenderer) {
      this.font = textRenderer;
      this.panelWidth = this.run_I_16();
      this.panelHeight = this.get_I_14();
   }

   public void show(PacketInspectorEngine.InspectionReport inspection, AutismPacketLoggerOverlay.LoggedPacketEntry sourceEntry, int anchorX, int anchorY) {
      if (inspection != null) {
         OverlayManager manager = OverlayManager.getInstance();
         manager.register(this);
         this.rememberScroll();
         this.clearAnalysis();
         this.report = inspection;
         this.entry = sourceEntry;
         this.analysisRequested = false;
         this.write_Z = true;
         this.update_Z = false;
         this.scrollKey = this.scrollKeyFor(inspection, sourceEntry);
         this.scrollOffset = savedScroll.getOrDefault(this.scrollKey, 0);
         this.listView = null;
         this.linesDirty = true;
         this.requestAnalysis();
         if (sourceEntry != null && this.analysis != null) {
            this.report = this.currentReport();
         }

         this.positionPanel(anchorX, anchorY);
         manager.bringToFront(this);
      }
   }

   public void inspect(AutismPacketLoggerOverlay.LoggedPacketEntry sourceEntry, int anchorX, int anchorY) {
      if (sourceEntry != null) {
         OverlayManager manager = OverlayManager.getInstance();
         manager.register(this);
         this.rememberScroll();
         this.clearAnalysis();
         this.entry = sourceEntry;
         this.analysisRequested = false;
         this.write_Z = true;
         this.update_Z = false;
         this.scrollKey = this.scrollKeyFor(null, sourceEntry);
         this.scrollOffset = savedScroll.getOrDefault(this.scrollKey, 0);
         this.listView = null;
         this.linesDirty = true;
         this.requestAnalysis();
         this.report = this.currentReport();
         this.positionPanel(anchorX, anchorY);
         manager.bringToFront(this);
      }
   }

   public void clearInspection() {
      this.rememberScroll();
      this.write_Z = false;
      this.dragging = false;
      this.entry = null;
      this.clearAnalysis();
   }

   private void clearAnalysis() {
      this.analysisKey = "";
      this.pendingAnalysis = null;
      this.analysis = null;
      this.analysisRequested = false;
      if (this.analysisTask != null) {
         this.analysisTask.cancel(true);
         this.analysisTask = null;
      }
   }

   private void requestAnalysis() {
      if (this.entry != null) {
         AutismPayloadSupport.PayloadEntry snapshot = AutismPayloadSupport.resolveEntryPayload(this.entry);
         if (snapshot != null && (!this.analysisRequested || this.analysisTask == null || this.analysisTask.isDone())) {
            this.analysisRequested = true;
            String baseKey = this.scrollKeyFor(null, this.entry);
            this.analysis = PacketInspectorEngine.analyzePayload(snapshot);
            this.pendingAnalysis = new AtomicReference<>(this.analysis);
            this.analysisKey = baseKey;
            this.startAnalysis(snapshot, baseKey);
         }
      }
   }

   private void startAnalysis(AutismPayloadSupport.PayloadEntry snapshot, String analysisKey) {
      if (snapshot != null && analysisKey != null && !analysisKey.isBlank()) {
         this.analysisTask = CompletableFuture.runAsync(
            () -> {
               try {
                  PacketInspectorEngine.PayloadAnalysis result = PacketInspectorEngine.analyzePayloadWithProgress(
                     snapshot, view -> this.publishAnalysis(analysisKey, view)
                  );
                  this.publishAnalysis(analysisKey, result);
               } catch (Throwable t) {
                  this.publishAnalysis(analysisKey, PacketInspectorEngine.failedAnalysis(AutismPayloadSupport.describeError(t)));
               }
            }
         );
      }
   }

   private void publishAnalysis(String analysisKey, PacketInspectorEngine.PayloadAnalysis view) {
      AtomicReference<PacketInspectorEngine.PayloadAnalysis> ref = this.pendingAnalysis;
      if (ref != null && view != null && analysisKey.equals(this.analysisKey)) {
         ref.set(view);
      }
   }

   private void applyAnalysis() {
      if (this.pendingAnalysis != null && this.entry != null) {
         PacketInspectorEngine.PayloadAnalysis next = this.pendingAnalysis.get();
         if (next != null && next != this.analysis) {
            int oldScroll = this.listView != null ? this.listView.getScrollOffset() : this.scrollOffset;
            this.analysis = next;
            this.report = this.currentReport();
            this.scrollOffset = Math.max(0, oldScroll);
            this.linesDirty = true;
            if (this.listView != null) {
               this.listView.setScrollOffset(this.scrollOffset);
            }
         }
      }
   }

   private PacketInspectorEngine.InspectionReport currentReport() {
      return this.entry == null
         ? this.report
         : PacketInspectorEngine.inspect(this.entry, this.analysis, true);
   }

   private void rebuildList(boolean keepScroll) {
      this.rememberScroll();
      int oldScroll = keepScroll && this.listView != null ? this.listView.getScrollOffset() : 0;
      this.scrollKey = this.scrollKeyFor(null, this.entry);
      this.scrollOffset = keepScroll ? Math.max(0, oldScroll) : savedScroll.getOrDefault(this.scrollKey, 0);
      this.listView = null;
      this.lines = Collections.emptyList();
      this.linesDirty = true;
      this.report = this.currentReport();
   }

   @Override
   public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.write_Z || this.isClosing()) {
         this.applyAnalysis();
         OverlayBounds bounds = this.getBounds();
         String title = this.report == null ? "Packet Inspect" : this.report.title();
         this.renderWindowFrame(context, mouseX, mouseY, bounds, title, this.update_Z, this.dragging);
         boolean clipBody = this.beginBodyClip(context, bounds, this.update_Z);
         if (!clipBody) {
            this.renderInactiveDim(context, bounds, this.update_Z, this.dragging);
         } else {
            try {
               int listX = this.panelX + this.contentPadding();
               int tabsHeight = this.value();
               int tabsY = this.panelY + 14 + this.size();
               if (tabsHeight > 0) {
                  this.init(context, listX, tabsY, this.panelWidth - this.contentPadding() * 2, mouseX, mouseY);
               }

               int progressHeight = this.progressHeight();
               int progressY = tabsY + tabsHeight;
               if (progressHeight > 0) {
                  this.drawAnalysis(context, listX, progressY, this.panelWidth - this.contentPadding() * 2, progressHeight, this.analysis);
               }

               int listY = progressY + progressHeight;
               int listWidth = this.panelWidth - this.contentPadding() * 2;
               int listHeight = Math.max(1, this.panelHeight - 14 - this.set_I_14() - this.state() - tabsHeight - progressHeight);
               int innerX = listX + this.add_I_12();
               int innerY = listY + this.add_I_12();
               int innerWidth = autismclient.gui.ui.widgets.ScrollbarContentMath.maxTextWidth(listWidth, this.add_I_12());
               int innerHeight = listHeight - this.add_I_12() * 2;
               int lineH = this.lineHeight();
               if (!OverlayManager.getInstance().isFocused(this) && !OverlayManager.getInstance().isTopmost(this)) {
                  boolean var32 = false;
               } else {
                  boolean footerY = true;
               }

               this.rebuildLines(innerWidth);
               if (this.lines.isEmpty()) {
                  autismclient.gui.ui.widgets.ListRowRenderer.drawEmptyPlaceholder(context, this.font, "No inspection data.", listX, listY, listWidth);
               } else {
                  int contentHeight = this.lines.size() * lineH;
                  if (this.listView == null
                     || this.listView.getX() != listX
                     || this.listView.getY() != listY
                     || this.listView.getWidth() != listWidth
                     || this.listView.getHeight() != listHeight) {
                     int previousScrollOffset = this.listView != null ? this.listView.getScrollOffset() : this.scrollOffset;
                     this.listView = new autismclient.gui.ui.layout.ScrollableListView(listX, listY, listWidth, listHeight, lineH, 4);
                     this.listView.setContentHeight(contentHeight);
                     this.listView.setScrollOffset(previousScrollOffset);
                     this.scrollOffset = this.listView.getScrollOffset();
                  }

                  this.listView.setContentHeight(contentHeight);
                  if (this.scrollOffset > 0 && this.listView.getScrollOffset() == 0) {
                     this.listView.setScrollOffset(this.scrollOffset);
                  }

                  this.listView.beginFrame(context, this.theme.set(), this.theme.reset());

                  try {
                     this.listView
                        .forEachVisibleRow(
                           context,
                           this.lines.size(),
                           (idx, bnd) -> {
                              AutismPacketInspectOverlay.InspectLine ln = this.lines.get(idx);
                              this.drawLine(
                                 context,
                                 ln,
                                 innerX,
                                 UiLayoutMath.centerTextY(bnd.y, lineH, this.theme.init_I_l(autismclient.gui.f.b.l.read), this.theme.flush())
                              );
                           }
                        );
                  } finally {
                     this.listView.endFrame(context);
                  }

                  this.listView.renderScrollbar(context, mouseX, mouseY);
               }

               int footerY = this.panelY + this.panelHeight - this.set_I_14() + this.queue();
               autismclient.gui.f.b.k.init(
                  context,
                  this.font,
                  this.lines.size() + " lines",
                  this.theme.init_Identifier_l(autismclient.gui.f.b.l.write),
                  this.theme.load(autismclient.gui.f.b.l.write),
                  this.panelX + this.contentPadding() + 5,
                  UiLayoutMath.centerTextY(footerY, this.reset(), this.theme.init_I_l(autismclient.gui.f.b.l.write), this.theme.flush()),
                  false
               );

               for (AutismPacketInspectOverlay.FooterButton button : this.footerButtons(footerY)) {
                  autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone var10000;
                  switch (button.action()) {
                     case WAIT:
                     case QUEUE:
                     case ADD_PAYLOAD:
                        var10000 = autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY;
                        break;
                     case SEND:
                        var10000 = autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS;
                        break;
                     case EDIT_PAYLOAD:
                        var10000 = autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY;
                        break;
                     case COPY_RAW:
                        var10000 = autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY;
                        break;
                     case COPY:
                        var10000 = autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.GHOST;
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone variant = var10000;
                  autismclient.gui.ui.widgets.ButtonDrawHelper.init(
                     context, this.font, button.x(), button.load(), button.read(), this.reset(), button.label(), variant, true, mouseX, mouseY
                  );
               }
            } finally {
               this.endBodyClip(context, clipBody);
               this.renderInactiveDim(context, bounds, this.update_Z, this.dragging);
            }
         }
      }
   }

   private void rebuildLines(int innerWidth) {
      if (this.linesDirty || this.linesWidth != innerWidth) {
         if (this.report == null) {
            this.lines = Collections.emptyList();
            this.linesDirty = false;
            this.linesWidth = innerWidth;
         } else {
            List<AutismPacketInspectOverlay.InspectLine> wrapped = new ArrayList<>();

            for (PacketInspectorEngine.ColoredLine line : this.report.lines()) {
               this.wrapLine(line, innerWidth, wrapped);
            }

            this.lines = wrapped;
            this.linesDirty = false;
            this.linesWidth = innerWidth;
         }
      }
   }

   private void wrapLine(PacketInspectorEngine.ColoredLine line, int maxWidth, List<AutismPacketInspectOverlay.InspectLine> target) {
      String text = line.text();
      if (text == null || text.isEmpty()) {
         target.add(AutismPacketInspectOverlay.InspectLine.of("", 0, line.color()));
      } else if (this.isSectionHeader(text)) {
         this.wrapText(text, 0, line.color(), maxWidth, target);
      } else {
         int leadingSpaces = this.leadingSpaces(text);
         String indentComponent = " ".repeat(leadingSpaces);
         int indentWidth = this.textWidth(indentComponent);
         int colonIndex = text.indexOf(58, leadingSpaces);
         if (colonIndex > leadingSpaces && colonIndex < text.length() - 1) {
            String label = text.substring(leadingSpaces, colonIndex + 1).trim();
            String value = text.substring(colonIndex + 1).stripLeading();
            String prefix = indentComponent + label + " ";
            int prefixWidth = this.textWidth(prefix);
            int continuationOffset = Math.min(prefixWidth, indentWidth + this.start_I_11());
            this.wrapSegment(prefix, value, line.color(), prefixWidth, continuationOffset, maxWidth, target);
         } else {
            this.wrapText(text.substring(leadingSpaces), indentWidth, line.color(), maxWidth, target);
         }
      }
   }

   private void wrapSegment(
      String prefix,
      String value,
      int valueColor,
      int prefixWidth,
      int continuationOffset,
      int maxWidth,
      List<AutismPacketInspectOverlay.InspectLine> target
   ) {
      int safeContinuationOffset = autismclient.gui.ui.widgets.ScrollbarContentMath.clampOffset(maxWidth, continuationOffset);
      if (prefixWidth >= maxWidth) {
         this.wrapText(prefix.stripTrailing(), 0, UiColorPalette.silver(), maxWidth, target);
         this.wrapText(value, safeContinuationOffset, valueColor, maxWidth, target);
      } else if (value != null && !value.isEmpty()) {
         String remaining = value;

         for (boolean firstLine = true; !remaining.isEmpty(); firstLine = false) {
            int offset = firstLine ? prefixWidth : safeContinuationOffset;
            int availableWidth = autismclient.gui.ui.widgets.ScrollbarContentMath.availableWidth(maxWidth, offset);
            String current = remaining;
            int split = autismclient.gui.ui.TextWrapLayout.breakIndex(
               current, 0, current.length(), availableWidth, (start, end) -> this.textWidth(current.substring(start, end))
            );
            String part = remaining.substring(0, split).stripTrailing();
            target.add(new AutismPacketInspectOverlay.InspectLine(firstLine ? prefix : null, UiColorPalette.silver(), part, valueColor, offset));
            remaining = remaining.substring(split).stripLeading();
         }
      } else {
         target.add(new AutismPacketInspectOverlay.InspectLine(prefix, UiColorPalette.silver(), "", valueColor, prefixWidth));
      }
   }

   private void wrapText(String text, int offset, int color, int maxWidth, List<AutismPacketInspectOverlay.InspectLine> target) {
      if (text != null) {
         if (text.isEmpty()) {
            target.add(AutismPacketInspectOverlay.InspectLine.of("", offset, color));
         } else {
            int safeOffset = autismclient.gui.ui.widgets.ScrollbarContentMath.clampOffset(maxWidth, offset);
            String remaining = text;

            while (!remaining.isEmpty()) {
               int availableWidth = autismclient.gui.ui.widgets.ScrollbarContentMath.availableWidth(maxWidth, safeOffset);
               String current = remaining;
               int split = autismclient.gui.ui.TextWrapLayout.breakIndex(
                  current, 0, current.length(), availableWidth, (start, end) -> this.textWidth(current.substring(start, end))
               );
               String part = remaining.substring(0, split).stripTrailing();
               target.add(AutismPacketInspectOverlay.InspectLine.of(part, safeOffset, color));
               remaining = remaining.substring(split).stripLeading();
            }
         }
      }
   }

   private void drawLine(GuiGraphicsExtractor context, AutismPacketInspectOverlay.InspectLine line, int x, int y) {
      if (line != null && line.value() != null) {
         if (line.label() != null && !line.label().isEmpty()) {
            autismclient.gui.f.b.k.init(
               context, this.font, line.label(), this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), line.labelColor(), x, y, false
            );
         }

         int valueX = x + line.read();
         autismclient.gui.f.b.k.init(
            context, this.font, line.value(), this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), line.valueColor(), valueX, y, false
         );
      }
   }

   private void drawAnalysis(GuiGraphicsExtractor context, int x, int y, int width, int height, PacketInspectorEngine.PayloadAnalysis view) {
      if (view != null && width > 0 && height > 0) {
         int fill = -1441722090;
         autismclient.gui.f.PacketUtil.init(context, autismclient.gui.f.NetHelper.init(x, y, width, height - 2), fill, -2007870113);
         String status = view.status() != null && !view.status().isBlank() ? view.status() : "Decoding";
         int percent = Math.max(0, Math.min(100, (int)Math.round(view.progress() * 100.0)));
         String label = "Analyzer: " + status + " - progress " + percent + "%";
         autismclient.gui.f.b.k.init(
            context,
            this.font,
            label,
            this.theme.init_Identifier_l(autismclient.gui.f.b.l.write),
            view.load() ? UiColorPalette.errorText() : UiColorPalette.textBody(),
            x + 4,
            y + 2,
            false
         );
         int barX = x + 4;
         int barY = y + height - 8;
         int barWidth = Math.max(1, width - 8);
         int barHeight = 4;
         autismclient.gui.ui.widgets.ProgressBarRenderer.draw(
            autismclient.gui.f.UiHelper.init(context, this.font, -1, -1),
            autismclient.gui.f.NetHelper.init(barX, barY, barWidth, barHeight),
            view.progress()
         );
      }
   }

   private void init(GuiGraphicsExtractor context, int x, int y, int width, int mouseX, int mouseY) {
   }

   private boolean isSectionHeader(String text) {
      String trimmed = text == null ? "" : text.trim();
      return trimmed.startsWith("[") && trimmed.endsWith("]");
   }

   private int leadingSpaces(String text) {
      int count = 0;

      while (count < text.length() && text.charAt(count) == ' ') {
         count++;
      }

      return count;
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.write_Z) {
         return false;
      }

      OverlayBounds bounds = this.getBounds();
      if (button == 0 && mouseY >= this.panelY && mouseY <= this.panelY + 14 && mouseX >= this.panelX && mouseX <= this.panelX + this.panelWidth) {
         if (this.isOverHeaderCloseButton(mouseX, mouseY, bounds)) {
            this.clearInspection();
            return true;
         } else {
            this.dragging = true;
            this.dragOffsetX = mouseX - this.panelX;
            this.dragOffsetY = mouseY - this.panelY;
            return true;
         }
      } else {
         if (this.update_Z) {
            return true;
         }

         int tabsHeight = this.value();
         int progressHeight = this.progressHeight();
         int listY = this.panelY + 14 + this.size() + tabsHeight + progressHeight;
         int listHeight = Math.max(1, this.panelHeight - 14 - this.set_I_14() - this.state() - tabsHeight - progressHeight);
         int listX = this.panelX + this.contentPadding();
         int listWidth = this.panelWidth - this.contentPadding() * 2;
         if (button == 0
            && this.listView != null
            && this.listView.containsPoint(mouseX, mouseY)
            && this.listView.mouseClicked(mouseX, mouseY, button)) {
            return true;
         }

         int footerY = this.panelY + this.panelHeight - this.set_I_14() + this.queue();
         if (button == 0) {
            for (AutismPacketInspectOverlay.FooterButton footerButton : this.footerButtons(footerY)) {
               if (mouseX >= footerButton.x()
                  && mouseX < footerButton.x() + footerButton.read()
                  && mouseY >= footerButton.load()
                  && mouseY < footerButton.load() + this.reset()) {
                  this.runAction(footerButton.action());
                  return true;
               }
            }
         }

         return this.containsPoint(mouseX, mouseY);
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0 && this.dragging) {
         this.dragging = false;
         this.rememberScroll();
         this.saveBounds();
         return true;
      } else if (button == 0 && this.listView != null) {
         this.listView.mouseReleased();
         this.rememberScroll();
         return true;
      } else {
         this.dragging = false;
         return false;
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.listView != null) {
         this.listView.mouseDragged(mouseX, mouseY);
         this.rememberScroll();
      }

      if (this.write_Z && button == 0 && this.dragging) {
         this.setBounds(new OverlayBounds((int)Math.round(mouseX - this.dragOffsetX), (int)Math.round(mouseY - this.dragOffsetY), this.panelWidth, this.panelHeight, this.write_Z, this.update_Z));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.write_Z && !this.update_Z) {
         int tabsHeight = this.value();
         int progressHeight = this.progressHeight();
         int listY = this.panelY + 14 + this.size() + tabsHeight + progressHeight;
         int listHeight = Math.max(1, this.panelHeight - 14 - this.set_I_14() - this.state() - tabsHeight - progressHeight);
         int listX = this.panelX + this.contentPadding();
         int listWidth = this.panelWidth - this.contentPadding() * 2;
         if (mouseX < listX || mouseX > listX + listWidth || mouseY < listY || mouseY > listY + listHeight) {
            return false;
         } else if (this.listView != null) {
            boolean handled = this.listView.mouseScrolled(mouseX, mouseY, amount);
            this.rememberScroll();
            return handled;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.write_Z) {
         return false;
      } else if (keyCode == 256) {
         this.clearInspection();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      return false;
   }

   @Override
   public boolean isVisible() {
      return this.write_Z;
   }

   @Override
   public void setVisible(boolean visible) {
      this.write_Z = visible;
      if (!visible) {
         this.clearInspection();
      }
   }

   @Override
   public boolean containsPoint(double mouseX, double mouseY) {
      if (!this.write_Z) {
         return false;
      }

      int height = this.update_Z ? 14 : this.panelHeight;
      return mouseX >= this.panelX && mouseX <= this.panelX + this.panelWidth && mouseY >= this.panelY && mouseY <= this.panelY + height;
   }

   @Override
   public boolean isOverHeader(double mouseX, double mouseY) {
      return !this.write_Z
         ? false
         : mouseX >= this.panelX
            && mouseX <= this.panelX + this.panelWidth
            && mouseY >= this.panelY
            && mouseY <= this.panelY + 14
            && !this.isOverHeaderControl(mouseX, mouseY, this.getBounds());
   }

   @Override
   public int getZPriority() {
      return 12;
   }

   @Override
   public boolean isCollapsed() {
      return this.update_Z;
   }

   @Override
   public void setCollapsed(boolean collapsed) {
      if (this.update_Z != collapsed) {
         this.update_Z = collapsed;
         this.dragging = false;
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
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.panelX, this.panelY, this.panelWidth, this.panelHeight, this.write_Z, this.update_Z);
   }

   @Override
   public void setBounds(OverlayBounds bounds) {
      if (bounds != null) {
         OverlayBounds clamped = this.clampToScreen(this, bounds);
         boolean sizeChanged = this.panelWidth != clamped.width || this.panelHeight != clamped.height;
         this.panelX = clamped.x;
         this.panelY = clamped.y;
         this.panelWidth = clamped.width;
         this.panelHeight = clamped.height;
         this.write_Z = clamped.visible;
         this.update_Z = clamped.collapsed;
         if (sizeChanged) {
            this.linesDirty = true;
         }
      }
   }

   @Override
   public int getMinWidth() {
      return this.pop_I_18();
   }

   @Override
   public int getMinHeight() {
      return this.tick_I_16();
   }

   private void runAction(AutismPacketInspectOverlay.PacketAction action) {
      if (action != null) {
         switch (action) {
            case WAIT:
               LoggedPacketActions.addWaitCondition(this.entry);
               break;
            case SEND:
               this.sendPacket(this.entry);
               break;
            case QUEUE:
               LoggedPacketActions.queue(this.entry);
               break;
            case EDIT_PAYLOAD:
               LoggedPacketActions.editPayload(this.entry);
               break;
            case ADD_PAYLOAD:
               LoggedPacketActions.addPayloadAction(this.entry);
               break;
            case COPY_RAW:
               this.copyRawBytes();
               break;
            case COPY:
               if (this.report != null) {
                  MINECRAFT.keyboardHandler.setClipboard(this.report.copyText());
                  bi.init("Copied packet inspection.");
               }
         }
      }
   }

   private void copyRawBytes() {
      String export = this.rawExport(this.entry);
      if (export != null && !export.isBlank()) {
         MINECRAFT.keyboardHandler.setClipboard(export);
         bi.init("Copied raw packet bytes.");
      } else {
         bi.read("No raw packet bytes were captured.");
      }
   }

   private String rawExport(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      if (entry == null) {
         return "";
      }

      AutismPayloadSupport.PayloadEntry payload = AutismPayloadSupport.resolveEntryPayload(entry);
      PacketWireCaptureStore.PacketCapture packet = PacketWireCaptureStore.captureFor(entry.packet);
      byte[] payloadBytes = payload == null ? new byte[0] : payload.rawBytes();
      byte[] packetBytes = packet == null ? new byte[0] : packet.plaintext();
      if (payloadBytes.length == 0 && packetBytes.length == 0) {
         return "";
      }

      StringBuilder out = new StringBuilder(Math.max(256, (payloadBytes.length + packetBytes.length) * 4));
      out.append("# AUTISM Packet Raw Export\n");
      out.append("Name: ").append(entry.name).append('\n');
      out.append("Direction: ").append(entry.direction).append('\n');
      out.append("Class: ").append(entry.packetClass == null ? "unknown" : entry.packetClass.getName()).append('\n');
      out.append("Tick: ").append(entry.tick).append('\n');
      out.append("Time: ").append(Instant.ofEpochMilli(entry.timestampMs)).append('\n');
      if (payload != null) {
         out.append("Payload Channel: ").append(payload.channel()).append('\n');
         out.append("Payload Protocol: ").append(payload.protocolPhase()).append('\n');
         out.append("Payload Class: ").append(payload.payloadClassName()).append('\n');
         out.append("Payload Packet ID: ").append(payload.packetId()).append('\n');
      }

      if (packet != null) {
         out.append("Captured Protocol: ").append(packet.load()).append('\n');
         out.append("Captured Packet ID: ").append(packet.packetId()).append('\n');
         out.append("Captured Packet Type: ").append(packet.write()).append('\n');
      }

      String decoded = PacketDetailFormatter.describe(entry);
      if (decoded != null && !decoded.isBlank()) {
         out.append('\n').append(decoded).append('\n');
      }

      this.appendHexSection(out, "Payload Body", payloadBytes);
      this.appendHexSection(out, "Plaintext Packet", packetBytes);
      return out.toString();
   }

   private void appendHexSection(StringBuilder out, String title, byte[] bytes) {
      if (out != null && bytes != null && bytes.length != 0) {
         out.append('\n').append('[').append(title).append("]\n");
         out.append("Length: ").append(bytes.length).append(" bytes\n");
         out.append("Hex: ").append(this.toHex(bytes, false)).append('\n');
         out.append("Hex Spaced:\n").append(this.hexDump(bytes, 32)).append('\n');
         out.append("Base64: ").append(Base64.getEncoder().encodeToString(bytes)).append('\n');
         String utf8 = AutismPayloadSupport.decodePrintableUtf8(bytes);
         if (utf8 != null && !utf8.isBlank()) {
            out.append("UTF-8 Escaped:\n").append(this.escapeControlChars(utf8)).append('\n');
         }
      }
   }

   private String escapeControlChars(String text) {
      if (text != null && !text.isEmpty()) {
         StringBuilder sb = new StringBuilder(text.length() + 16);

         for (int i = 0; i < text.length(); i++) {
            char chr = text.charAt(i);
            switch (chr) {
               case '\u0000':
                  sb.append("\\0\n");
                  break;
               case '\t':
                  sb.append("\\t");
                  break;
               case '\n':
                  sb.append('\n');
                  break;
               case '\r':
                  if (i + 1 >= text.length() || text.charAt(i + 1) != '\n') {
                     sb.append('\n');
                  }
                  break;
               default:
                  if (Character.isISOControl(chr)) {
                     sb.append(String.format("\\u%04X", Integer.valueOf(chr)));
                  } else {
                     sb.append(chr);
                  }
            }
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   private String toHex(byte[] bytes, boolean spaced) {
      if (bytes != null && bytes.length != 0) {
         StringBuilder sb = new StringBuilder(bytes.length * (spaced ? 3 : 2));

         for (int i = 0; i < bytes.length; i++) {
            if (spaced && i > 0) {
               sb.append(' ');
            }

            this.appendHexByte(sb, bytes[i] & 255);
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   private String hexDump(byte[] bytes, int bytesPerLine) {
      if (bytes != null && bytes.length != 0) {
         int perLine = Math.max(1, bytesPerLine);
         StringBuilder sb = new StringBuilder(bytes.length * 3 + bytes.length / perLine + 8);

         for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
               sb.append((char)(i % perLine == 0 ? '\n' : ' '));
            }

            this.appendHexByte(sb, bytes[i] & 255);
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   private void appendHexByte(StringBuilder sb, int value) {
      char[] digits = "0123456789ABCDEF".toCharArray();
      sb.append(digits[value >>> 4 & 15]);
      sb.append(digits[value & 15]);
   }

   private void sendPacket(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      if (entry != null && entry.packet != null && "C2S".equalsIgnoreCase(entry.direction)) {
         LoggedPacketActions.resend(entry);
      } else {
         AutismClientMessaging.sendPrefixed("§cOnly C2S packets can be sent.");
      }
   }

   private void positionPanel(int anchorX, int anchorY) {
      int screenWidth = MINECRAFT.getWindow() != null ? cA.init_I() : 600;
      int screenHeight = MINECRAFT.getWindow() != null ? cA.load_I() : 400;
      int lineHeight = this.lineHeight();
      int lineCount = this.report == null ? 0 : this.report.lines().size();
      int footerInfoWidth = this.textWidth(lineCount + " lines") + this.contentPadding() + 15;
      int footerButtonsWidth = this.actionsWidth();
      int titleWidth = this.textWidth(this.report == null ? "Packet Inspect" : this.report.title()) + this.entry();
      int minWidth = Math.max(this.pop_I_18(), Math.max(titleWidth, footerInfoWidth + footerButtonsWidth + this.node()));
      int maxWidth = Math.max(minWidth, Math.min(this.stop(), screenWidth - 12));
      int maxHeight = Math.max(this.tick_I_16(), Math.min(this.next(), screenHeight - 12));
      int desiredWidth = this.bestPanelWidth(minWidth, maxWidth);
      AutismPacketInspectOverlay.PanelFit fit = this.fitWidth(desiredWidth);
      int desiredVisibleLines = Math.max(3, Math.min(fit.load(), this.prev()));
      int desiredListHeight = Math.max(this.task(), desiredVisibleLines * lineHeight + this.add_I_12() * 2 + 2);
      int desiredHeight = 14 + this.size() + this.value() + this.progressHeight() + desiredListHeight + this.set_I_14() + this.buffer();
      desiredHeight = Math.min(maxHeight, Math.max(this.tick_I_16(), desiredHeight));
      this.setBounds(new OverlayBounds(anchorX, anchorY, desiredWidth, desiredHeight, true, false));
   }

   private void rememberScroll() {
      if (this.scrollKey != null && !this.scrollKey.isEmpty()) {
         int offset = this.listView != null ? this.listView.getScrollOffset() : this.scrollOffset;
         this.scrollOffset = Math.max(0, offset);
         savedScroll.put(this.scrollKey, this.scrollOffset);
      }
   }

   private String scrollKeyFor(PacketInspectorEngine.InspectionReport inspection, AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      if (entry != null) {
         String className = entry.packetClass == null ? "" : entry.packetClass.getName();
         return entry.direction + "|" + entry.name + "|" + className + "|" + entry.timestampMs + "|" + entry.tick;
      } else {
         return inspection == null ? "" : inspection.title();
      }
   }

   private int bestPanelWidth(int minPanelWidth, int maxPanelWidth) {
      int rawLineCount = this.report == null ? 0 : this.report.lines().size();
      int bestWidth = minPanelWidth;
      long bestScore = 9223372036854775807L;

      for (int width = minPanelWidth; width <= maxPanelWidth; width += 8) {
         long score = this.layoutScore(width, rawLineCount);
         if (score < bestScore || score == bestScore && width < bestWidth) {
            bestScore = score;
            bestWidth = width;
         }
      }

      if (bestWidth != maxPanelWidth) {
         long score = this.layoutScore(maxPanelWidth, rawLineCount);
         if (score < bestScore || score == bestScore && maxPanelWidth < bestWidth) {
            bestWidth = maxPanelWidth;
         }
      }

      return bestWidth;
   }

   private long layoutScore(int panelWidthCandidate, int rawLineCount) {
      AutismPacketInspectOverlay.PanelFit fit = this.fitWidth(panelWidthCandidate);
      int extraLines = Math.max(0, fit.load() - rawLineCount);
      int wastedPixels = Math.max(0, fit.init() - fit.read());
      return panelWidthCandidate / 6L + extraLines * 52L + wastedPixels * 4L;
   }

   private AutismPacketInspectOverlay.PanelFit fitWidth(int panelWidthCandidate) {
      int listWidth = Math.max(32, panelWidthCandidate - this.contentPadding() * 2);
      int contentWidth = autismclient.gui.ui.widgets.ScrollbarContentMath.maxTextWidth(listWidth, this.add_I_12());
      List<AutismPacketInspectOverlay.InspectLine> measured = new ArrayList<>();
      if (this.report != null) {
         for (PacketInspectorEngine.ColoredLine line : this.report.lines()) {
            this.wrapLine(line, contentWidth, measured);
         }
      }

      int longestWrappedLineWidth = 0;

      for (AutismPacketInspectOverlay.InspectLine line : measured) {
         longestWrappedLineWidth = Math.max(longestWrappedLineWidth, line.width(this.font));
      }

      return new AutismPacketInspectOverlay.PanelFit(contentWidth, measured.size(), longestWrappedLineWidth);
   }

   private List<AutismPacketInspectOverlay.FooterButton> footerButtons(int footerY) {
      List<AutismPacketInspectOverlay.FooterButton> buttons = new ArrayList<>();
      int cursorX = this.panelX + this.panelWidth - this.contentPadding();

      for (AutismPacketInspectOverlay.PacketAction action : this.availableActions()) {
         cursorX = this.addActionButton(buttons, cursorX, footerY, action);
      }

      return buttons;
   }

   private List<AutismPacketInspectOverlay.PacketAction> availableActions() {
      List<AutismPacketInspectOverlay.PacketAction> actions = new ArrayList<>();
      actions.add(AutismPacketInspectOverlay.PacketAction.COPY);
      if (this.hasRawPayload(this.entry)) {
         actions.add(AutismPacketInspectOverlay.PacketAction.COPY_RAW);
      }

      if (LoggedPacketActions.isResendable(this.entry)) {
         actions.add(AutismPacketInspectOverlay.PacketAction.QUEUE);
      }

      if (LoggedPacketActions.isPayloadC2S(this.entry)) {
         actions.add(AutismPacketInspectOverlay.PacketAction.EDIT_PAYLOAD);
      }

      if (LoggedPacketActions.canAddPayloadAction(this.entry)) {
         actions.add(AutismPacketInspectOverlay.PacketAction.ADD_PAYLOAD);
      }

      if (LoggedPacketActions.canAddSendAction(this.entry)) {
         actions.add(AutismPacketInspectOverlay.PacketAction.SEND);
      }

      if (LoggedPacketActions.canAddWaitCondition(this.entry)) {
         actions.add(AutismPacketInspectOverlay.PacketAction.WAIT);
      }

      return actions;
   }

   private boolean hasRawPayload(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      if (entry == null) {
         return false;
      }

      AutismPayloadSupport.PayloadEntry payload = AutismPayloadSupport.resolveEntryPayload(entry);
      if (payload != null && payload.rawBytes().length > 0) {
         return true;
      }

      PacketWireCaptureStore.PacketCapture packet = PacketWireCaptureStore.captureFor(entry.packet);
      return packet != null && packet.plaintext().length > 0;
   }

   private int actionsWidth() {
      int width = 0;

      for (AutismPacketInspectOverlay.PacketAction action : this.availableActions()) {
         if (width > 0) {
            width += this.clear();
         }

         width += this.actionButtonWidth(action);
      }

      return width;
   }

   private int addActionButton(
      List<AutismPacketInspectOverlay.FooterButton> buttons, int cursorX, int footerY, AutismPacketInspectOverlay.PacketAction action
   ) {
      int width = this.actionButtonWidth(action);
      int x = cursorX - width;
      buttons.add(new AutismPacketInspectOverlay.FooterButton(action, x, footerY, width));
      return x - this.clear();
   }

   private int actionButtonWidth(AutismPacketInspectOverlay.PacketAction action) {
      return Math.min(
         68,
         autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
            autismclient.gui.f.b.k.init(this.font, action.init_Str, this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), 0)
         )
      );
   }

   private int textWidth(String text) {
      return text != null && !text.isEmpty()
         ? autismclient.gui.f.b.k.init(this.font, text, this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), UiColorPalette.softWhite())
         : 0;
   }

   private int pop_I_18() {
      return 176;
   }

   private int tick_I_16() {
      return 14 + this.size() + this.task() + this.set_I_14() + this.buffer();
   }

   private int run_I_16() {
      return 220;
   }

   private int get_I_14() {
      return 173;
   }

   private int set_I_14() {
      return this.queue() + this.reset() + 4;
   }

   private int contentPadding() {
      return 4;
   }

   private int add_I_12() {
      return 3;
   }

   private int clear() {
      return 2;
   }

   private int reset() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int start_I_11() {
      return 32;
   }

   private int stop() {
      return 392;
   }

   private int next() {
      return 300;
   }

   private int prev() {
      return 13;
   }

   private int lineHeight() {
      return Math.max(autismclient.gui.ui.UiDensityMetrics.scaledHeight(12), this.theme.init_I_l_I(autismclient.gui.f.b.l.read, 2));
   }

   private int size() {
      return 4;
   }

   private int progressHeight() {
      return this.analysis != null && !this.analysis.complete() ? 22 : 0;
   }

   private int value() {
      return 0;
   }

   private int cache() {
      return 16;
   }

   private boolean hasPayload() {
      return this.entry != null && AutismPayloadSupport.resolveEntryPayload(this.entry) != null;
   }

   private int state() {
      return this.size() + this.buffer();
   }

   private int queue() {
      return 2;
   }

   private int buffer() {
      return 0;
   }

   private int entry() {
      return 54;
   }

   private int node() {
      return 16;
   }

   private int task() {
      return 3 * this.lineHeight() + this.add_I_12() * 2 + 2;
   }

   private record InspectLine(String label, int labelColor, String value, int valueColor, int read) {
      static AutismPacketInspectOverlay.InspectLine of(String text, int offset, int color) {
         return new AutismPacketInspectOverlay.InspectLine(null, color, text, color, offset);
      }

      int width(Font textRenderer) {
         int width = this.read
            + autismclient.gui.f.b.k.init(
               textRenderer, this.value, new autismclient.gui.f.b.CharEncoder().init_Identifier_l(autismclient.gui.f.b.l.read), UiColorPalette.softWhite()
            );
         if (this.label != null && !this.label.isEmpty()) {
            width = Math.max(
               width,
               autismclient.gui.f.b.k.init(
                  textRenderer, this.label, new autismclient.gui.f.b.CharEncoder().init_Identifier_l(autismclient.gui.f.b.l.read), UiColorPalette.softWhite()
               )
            );
         }

         return width;
      }
   }

   private record FooterButton(AutismPacketInspectOverlay.PacketAction action, int x, int load, int read) {
      private String label() {
         return this.action.init_Str;
      }
   }

   private record PanelFit(int init, int load, int read) {
   }

   private enum PacketAction {
      WAIT("Wait"),
      SEND("Send"),
      QUEUE("Queue"),
      EDIT_PAYLOAD("Edit Payload"),
      ADD_PAYLOAD("+Payload"),
      COPY_RAW("Raw Hex"),
      COPY("Copy");

      private final String init_Str;

      PacketAction(String label) {
         this.init_Str = label;
      }
   }
}
