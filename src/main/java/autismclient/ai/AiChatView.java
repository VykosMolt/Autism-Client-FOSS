package autismclient.ai;

import autismclient.gui.f.PacketUtil;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.client.Minecraft;

final class AiChatView {
   private static final int init_I = 4;
   private static final int load_I = 2;
   private final AiService service;
   private final AiViewState view;
   private final AiAssistantOverlay host;
   final autismclient.gui.ui.widgets.TextInputWidget input = new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(16000).setHistoryNavigationEnabled(true);
   final AiActionMenu actionMenu;
   private final AiTranscriptLayout transcriptLayout = new AiTranscriptLayout();
   private final AiUiKit.ScrollPane transcriptScroll = new AiUiKit.ScrollPane(true);
   private final Map<AiService.TranscriptEntry, Boolean> expandedOverrides = new IdentityHashMap<>();
   private AiTranscriptLayout.TextMeasurer textMeasurer;
   private int layoutRevision;
   private boolean init_Z;
   private boolean load_Z;
   private String lastSessionId = "";
   private int lastPollCount = -1;
   private String pendingPlanText = "";
   private String wrappedPlanSource = "";
   private List<String> wrappedPlanLines = List.of();
   private int wrappedPlanWidth = -1;
   private int wrappedPlanEpoch;
   private List<String[]> taskRows = List.of();
   private autismclient.gui.f.NetHelper transcriptArea = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private final ArrayList<AiAttachments.Inner_DataUtil> attachments = new ArrayList<>();
   private boolean attachmentPending;
   private boolean attachmentsEnabled;
   private final Map<AiService.TranscriptEntry, String> toolStatusCache = new IdentityHashMap<>();
   private String wrappedQuestionText = "";
   private List<String> wrappedQuestionLines = List.of();
   private int flush = -1;
   private int close;
   private boolean planModeTogglePending;
   private boolean pendingPlanMode;
   private long planModeToggleDeadline;

   void openAction(String action, String query) {
      this.actionMenu.openAction(action, query);
   }

   void setAttachmentsEnabled(boolean enabled) {
      this.attachmentsEnabled = enabled;
      if (!enabled) {
         this.attachments.clear();
      }
   }

   int preferredHeight() {
      if (this.view.settings.builtIn) {
         return this.actionMenu.preferredHeight() + AiUiKit.toolbarHeight() + AiUiKit.rowHeight() * 2 + 6;
      }

      int rows = this.attachmentsEnabled ? Math.max(2, this.attachments.size()) : this.attachments.size();
      int composer = this.view.settings.builtIn ? this.actionMenu.preferredHeight() : AiUiKit.toolbarHeight();
      return AiUiKit.lineHeight() * 3 + 2 + composer + 2 + AiUiKit.rowHeight() + rows * (AiUiKit.toolbarHeight() + 2);
   }

   AiChatView(AiService service, AiViewState view, AiAssistantOverlay host) {
      this.service = service;
      this.view = view;
      this.host = host;
      this.actionMenu = new AiActionMenu(service, view, host, this.transcriptScroll::scrollToBottom);
      this.input.setOnSubmit(text -> this.submitComposerInput());
   }

   autismclient.gui.ui.widgets.TextInputWidget activeInput() {
      return this.view.settings.builtIn ? this.actionMenu.searchInput : this.input;
   }

   boolean handleKey(int key, autismclient.gui.ui.widgets.TextInputWidget focused) {
      return this.view.settings.builtIn && (focused == null || this.actionMenu.inputs().contains(focused))
         ? this.actionMenu.handleKey(key, focused != null)
         : false;
   }

   boolean init_Z() {
      return this.view.settings.builtIn && (this.init_Z || this.actionMenu.isOpen());
   }

   boolean back() {
      if (!this.view.settings.builtIn) {
         return false;
      } else if (this.init_Z) {
         this.init_Z = false;
         return true;
      } else {
         return this.actionMenu.back();
      }
   }

   int bodyHeight(int width) {
      if (this.view.settings.builtIn && !this.init_Z && this.view.pendingQuestion == null) {
         int height = this.actionMenu.heightFor(width);
         return height == 0 ? 0 : height + AiUiKit.toolbarHeight() + AiUiKit.rowHeight() * 2 + 8;
      } else {
         return 0;
      }
   }

   autismclient.gui.ui.widgets.TextInputWidget inputForChar(char c) {
      return this.view.settings.builtIn ? this.actionMenu.inputForChar(c) : null;
   }

   autismclient.gui.f.NetHelper transcriptRect() {
      return this.transcriptArea;
   }

   void render(AiUiKit kit, autismclient.gui.f.NetHelper area, long now) {
      if (!this.view.activeSessionId.equals(this.lastSessionId)) {
         this.lastSessionId = this.view.activeSessionId;
         this.expandedOverrides.clear();
         this.transcriptLayout.invalidate();
         this.transcriptScroll.scrollToBottom();
         this.init_Z = false;
         this.load_Z = false;
         this.attachments.clear();
      }

      if (!this.view.awaitingPlanApproval) {
         this.load_Z = false;
      }

      if (this.attachmentsEnabled && this.attachments.isEmpty()) {
         this.attachments.add(new AiAttachments.Inner_DataUtil("31774.litematic", "litematic-v7", 1060, 3201L));
         this.attachments.add(new AiAttachments.Inner_DataUtil("31669.schem", "sponge-v3", 23031, 12606L));
      }

      if (this.lastPollCount != this.view.pollCount) {
         this.lastPollCount = this.view.pollCount;
         this.pendingPlanText = this.view.awaitingPlanApproval ? this.lastAssistantText() : "";
         this.taskRows = List.of();
      }

      int ctrl = AiUiKit.toolbarHeight();
      boolean builtIn = this.view.settings.builtIn;
      if (builtIn && this.input.isFocused()) {
         this.input.setFocused(false);
      }

      if (!builtIn) {
         this.actionMenu.blur();
         this.actionMenu.cancel();
      }

      autismclient.gui.f.NetHelper status = autismclient.gui.f.NetHelper.init(area.read(), area.load() - AiUiKit.rowHeight(), area.update(), AiUiKit.rowHeight());
      if (builtIn) {
         this.init_V_cE_2(kit, area, status, now);
      } else {
         int composerH = builtIn ? this.actionMenu.composerHeight(kit, area.update(), status.write() - 2 - area.write() - AiUiKit.lineHeight() * 3 - 2) : ctrl;
         autismclient.gui.f.NetHelper composer = autismclient.gui.f.NetHelper.init(area.read(), status.write() - 2 - composerH, area.update(), composerH);
         int bottom = composer.write() - 2;

         for (int i = this.attachments.size() - 1; i >= 0; i--) {
            AiAttachments.Inner_DataUtil file = this.attachments.get(i);
            autismclient.gui.f.NetHelper row = autismclient.gui.f.NetHelper.init(area.read(), bottom - ctrl, area.update(), ctrl);
            int remove = ctrl;
            kit.drawButton(
               autismclient.gui.f.NetHelper.init(row.read(), row.write(), row.update() - remove - 2, ctrl),
               file.name() + " (" + file.cells() + " blocks)",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
               () -> this.input.setValue("build \"" + file.name() + "\""),
               file.format() + " / " + file.bytes() + " bytes"
            );
            kit.drawIconButton(
               autismclient.gui.f.NetHelper.init(row.init() - remove, row.write(), remove, ctrl),
               autismclient.util.GuiIconAtlas.DELETE,
               false,
               false,
               "Remove attachment",
               () -> this.attachments.remove(file)
            );
            bottom = row.write() - 2;
         }

         this.transcriptArea = autismclient.gui.f.NetHelper.init(area.read(), area.write(), area.update(), Math.max(0, bottom - area.write()));
         int keep = AiUiKit.lineHeight() * 3;
         if (this.view.pendingQuestion != null) {
            AiService.UserPromptQuestion q = this.view.pendingQuestion;
            int options = q.options().size();
            int base = load(options);
            int wrapped = this.wrappedQuestionFor(kit, q, area.update() - 10).size();
            int lines = Math.max(1, Math.min(Math.min(4, wrapped), (bottom - area.write() - keep - base - 2) / AiUiKit.lineHeight()));
            int h = base + lines * AiUiKit.lineHeight();
            autismclient.gui.f.NetHelper card = autismclient.gui.f.NetHelper.init(area.read(), bottom - h, area.update(), h);
            this.drawQuestionCard(kit, card, q, lines);
            bottom = card.write() - 2;
         }

         if (this.view.awaitingPlanApproval) {
            int base = composerHeight();
            int lines = Math.max(0, Math.min(3, (bottom - area.write() - keep - base - 2) / AiUiKit.lineHeight()));
            autismclient.gui.f.NetHelper card = autismclient.gui.f.NetHelper.init(
               area.read(), bottom - base - lines * AiUiKit.lineHeight(), area.update(), base + lines * AiUiKit.lineHeight()
            );
            this.drawPlanReviewCard(kit, card, lines);
            bottom = card.write() - 2;
         }

         if (this.init_Z && !this.view.objective.isBlank()) {
            if (this.taskRows.isEmpty()) {
               this.taskRows = this.buildTaskRows();
            }

            List<String[]> rows = this.taskRows;
            int title = AiUiKit.inputHeight() + 2;
            int fit = Math.max(1, Math.min(rows.size(), (bottom - area.write() - keep - title - 4) / AiUiKit.rowHeight()));
            int h = title + fit * AiUiKit.rowHeight() + 4;
            autismclient.gui.f.NetHelper card = autismclient.gui.f.NetHelper.init(area.read(), bottom - h, area.update(), h);
            this.drawTaskCard(kit, card, rows.subList(Math.max(0, rows.size() - fit), rows.size()));
            bottom = card.write() - 2;
         }

         this.renderTranscript(kit, autismclient.gui.f.NetHelper.init(area.read(), area.write(), area.update(), Math.max(0, bottom - area.write())), now);
         if (builtIn) {
            this.actionMenu.render(kit, composer, now);
         } else {
            this.load(kit, composer);
         }

         this.read(kit, status);
      }
   }

   private void init_V_cE_2(AiUiKit kit, autismclient.gui.f.NetHelper area, autismclient.gui.f.NetHelper status, long now) {
      int h = AiUiKit.toolbarHeight();
      int line = AiUiKit.rowHeight();
      int bottom = status.write() - 2;
      autismclient.gui.f.NetHelper activity = autismclient.gui.f.NetHelper.init(area.read(), bottom - h - line - 2, area.update(), h + line + 2);
      String progress = this.view.statusLine.isBlank() ? "No task running" : this.view.statusLine;
      kit.drawCenteredRowText(
         progress,
         autismclient.gui.f.NetHelper.init(activity.read(), activity.write(), activity.update(), line),
         activity.read(),
         activity.update(),
         kit.colors.textSecondary
      );
      kit.addTooltipRegion(autismclient.gui.f.NetHelper.init(activity.read(), activity.write(), activity.update(), line), progress);
      int y = activity.write() + line;
      int w = kit.measureButtonWidth(this.init_Z ? "Hide activity" : "Activity");
      kit.drawButton(
         autismclient.gui.f.NetHelper.init(activity.read(), y, w, h),
         this.init_Z ? "Hide activity" : "Activity",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
         () -> this.init_Z = !this.init_Z
      );
      int right = activity.init();
      if (this.view.isActive()
         || (this.view.runState == AiService.RunState.FAILED || this.view.runState == AiService.RunState.BLOCKED)
            && !this.view.objective.isBlank()) {
         int stopW = kit.measureButtonWidth("Stop");
         right -= stopW;
         kit.drawButton(autismclient.gui.f.NetHelper.init(right, y, stopW, h), "Stop", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, () -> {
            this.service.stopTask();
            this.view.invalidate();
         });
         if (this.view.isWorking()) {
            int pauseW = kit.measureButtonWidth("Pause");
            right -= pauseW + 2;
            kit.drawButton(autismclient.gui.f.NetHelper.init(right, y, pauseW, h), "Pause", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, () -> {
               this.service.pauseTask();
               this.view.invalidate();
            });
         } else if (this.view.runState == AiService.RunState.PAUSED
            || this.view.runState == AiService.RunState.FAILED
            || this.view.runState == AiService.RunState.BLOCKED) {
            int resumeW = kit.measureButtonWidth("Resume");
            right -= resumeW + 2;
            kit.drawButton(
               autismclient.gui.f.NetHelper.init(right, y, resumeW, h), "Resume", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, () -> {
                  this.service.resumeTask();
                  this.view.invalidate();
               }
            );
         }
      }

      bottom = activity.write() - 2;
      this.transcriptArea = autismclient.gui.f.NetHelper.init(area.read(), area.write(), area.update(), Math.max(0, bottom - area.write()));
      if (this.view.pendingQuestion != null) {
         this.drawQuestionCard(
            kit,
            this.transcriptArea,
            this.view.pendingQuestion,
            Math.min(3, this.wrappedQuestionFor(kit, this.view.pendingQuestion, area.update() - 10).size())
         );
      } else if (this.init_Z) {
         autismclient.gui.f.NetHelper close = autismclient.gui.f.NetHelper.init(area.read(), area.write(), area.update(), h);
         kit.drawButton(close, "Back to actions", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, () -> this.init_Z = false);
         this.renderTranscript(kit, autismclient.gui.f.NetHelper.init(area.read(), close.load() + 2, area.update(), Math.max(0, bottom - close.load() - 2)), now);
      } else {
         this.actionMenu.render(kit, this.transcriptArea, now);
      }

      this.read(kit, status);
   }

   private AiTranscriptLayout.LayoutMetrics transcriptMetrics(AiUiKit kit) {
      int line = AiUiKit.lineHeight();
      return new AiTranscriptLayout.LayoutMetrics(line, AiUiKit.rowHeight(), 4 + kit.measureTextWidth("You") + 6, 13, 3, 2, 5);
   }

   private void renderTranscript(AiUiKit kit, autismclient.gui.f.NetHelper v, long now) {
      if (v.sync() >= 6) {
         autismclient.gui.ui.UiColorScheme colors = kit.colors;
         if (this.view.transcript.isEmpty() && !this.view.isWorking()) {
            this.init_V_cE_NetHelper(kit, v);
         } else {
            if (this.textMeasurer == null) {
               this.textMeasurer = AiUiKit.createTextMeasurer(kit.font);
            }

            int contentW = Math.max(40, v.update() - 6 - 3);
            AiTranscriptLayout.LayoutMetrics m = this.transcriptMetrics(kit);
            this.transcriptLayout.layout(this.view.transcript, contentW, textEpoch(), m, this.expandedOverrides, this.layoutRevision, this.textMeasurer);
            boolean live = this.view.isWorking();
            int liveH = AiUiKit.lineHeight() + 2;
            int liveY = this.transcriptLayout.height() + (this.transcriptLayout.height() > 0 ? m.blockGap() : 0);
            this.transcriptScroll.layout(v, (live ? liveY + liveH : this.transcriptLayout.height()) + 2);
            kit.registerScrollPane(this.transcriptScroll);
            kit.pushClip(v);
            int top = v.write() - this.transcriptScroll.offset;
            int viewBottom = this.transcriptScroll.offset + v.sync();
            List<AiTranscriptLayout.MessageBlock> blocks = this.transcriptLayout.blocks();

            for (int i = this.transcriptLayout.firstBlockAt(this.transcriptScroll.offset); i < blocks.size(); i++) {
               AiTranscriptLayout.MessageBlock block = blocks.get(i);
               if (block.top() >= viewBottom) {
                  break;
               }

               autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(v.read(), top + block.top(), contentW, block.bottom() - block.top());
               AiService.TranscriptEntry message = block.message();
               Runnable toggle = null;
               if (block.kind() == AiTranscriptLayout.LineKind.USER) {
                  PacketUtil.load(kit.graphics, bounds, 3, colors.cardSurface);
               } else if (block.kind() == AiTranscriptLayout.LineKind.TOOL && !message.detail().isBlank()) {
                  boolean expanded = block.expanded();
                  toggle = () -> this.toggleMessageExpanded(message, expanded);
                  autismclient.gui.f.NetHelper head = autismclient.gui.f.NetHelper.init(bounds.read(), bounds.write(), bounds.update(), m.tool());
                  if (kit.isHovered(head)) {
                     PacketUtil.load(kit.graphics, head, 3, colors.innerHighlight);
                  }
               }

               kit.addInteractiveRegion(bounds, toggle, () -> copyMessageToClipboard(message), null);
            }

            List<AiTranscriptLayout.LayoutLine> lines = this.transcriptLayout.lines();

            for (int i = this.transcriptLayout.firstLineAt(this.transcriptScroll.offset); i < lines.size(); i++) {
               AiTranscriptLayout.LayoutLine line = lines.get(i);
               if (line.y() >= viewBottom) {
                  break;
               }

               int x = v.read() + line.x();
               int y = top + line.y();
               switch (line.kind()) {
                  case USER:
                     if (i == 0 || lines.get(i - 1).message() != line.message()) {
                        kit.drawText("You", v.read() + 4, y, -10497050);
                     }

                     kit.drawText(line.text(), x, y, colors.textPrimary);
                     break;
                  case ASSISTANT:
                     this.drawTranscriptLine(kit, line, v.read(), x, y, contentW);
                     break;
                  case SYSTEM:
                     kit.drawText(line.text(), x, y, colors.textSecondary);
                     break;
                  case ERROR:
                     kit.drawText(line.text(), x, y, colors.danger);
                     break;
                  case SUCCESS:
                     kit.drawText(line.text(), x, y, colors.success);
                     break;
                  case WARNING:
                     kit.drawText(line.text(), x, y, -14249);
                     break;
                  case TOOL:
                     autismclient.gui.f.NetHelper row = autismclient.gui.f.NetHelper.init(v.read(), y, contentW, line.height());
                     if (!line.message().detail().isBlank()) {
                        PacketUtil.init(
                           kit.graphics,
                           autismclient.gui.f.NetHelper.init(v.read() + 2, y + (line.height() - 8) / 2, 8, 8),
                           line.expanded(),
                           colors.textSecondary
                        );
                     }

                     int nameX = v.read() + 13;
                     int room = contentW - 13;
                     int nameW = Math.min(kit.measureTextWidth(line.text()), room * 3 / 5);
                     kit.drawCenteredRowText(line.text(), row, nameX, nameW, colors.textPrimary);
                     if (!line.secondary().isEmpty()) {
                        kit.drawCenteredRowText(line.secondary(), row, nameX + nameW + 6, room - nameW - 6, this.toolStatusColor(kit, line.message()));
                     }
                     break;
                  case TOOL_DETAIL:
                     PacketUtil.load(kit.graphics, x - 5, y, line.height(), colors.divider);
                     kit.drawText(line.text(), x, y, colors.textSecondary);
                     break;
                  case MORE:
                     kit.drawText(line.text(), x, y, colors.textMuted);
               }
            }

            if (live) {
               this.drawWorkingIndicator(kit, v.read(), top + liveY, contentW, liveH, now);
            }

            kit.popClip();
            kit.drawScrollbar(this.transcriptScroll);
         }
      }
   }

   static int textEpoch() {
      return autismclient.gui.sdf.MsdfTextRenderer.cacheGeneration() * 31 + Float.floatToIntBits(autismclient.gui.sdf.MsdfTextRenderer.fontScale());
   }

   private int toolStatusColor(AiUiKit kit, AiService.TranscriptEntry message) {
      if (this.toolStatusCache.size() > 400) {
         this.toolStatusCache.clear();
      }

      int var10000;
      switch ((String)this.toolStatusCache.computeIfAbsent(message, AiService::extractToolStatus)) {
         case "FAILED":
         case "ERROR":
         case "TIMED_OUT":
            var10000 = kit.colors.danger;
            break;
         case "UNCERTAIN":
         case "INTERRUPTED":
         case "CANCELLED":
         case "BLOCKED":
            var10000 = -14249;
            break;
         default:
            var10000 = kit.colors.textSecondary;
      }

      return var10000;
   }

   private void drawTranscriptLine(AiUiKit kit, AiTranscriptLayout.LayoutLine line, int left, int x, int y, int contentW) {
      autismclient.gui.ui.UiColorScheme colors = kit.colors;
      if (line.spans() == null) {
         kit.drawText(line.text(), x, y, colors.textPrimary);
      } else {
         int h = line.height();
         boolean first = (line.flags() & 1) != 0;
         boolean last = (line.flags() & 2) != 0;
         switch (line.deco()) {
            case RULE:
               PacketUtil.init_V_GuiGraphicsExtract_2(kit.graphics, left, y + h / 2, contentW, colors.divider);
               return;
            case CODE:
               int top = y - (first ? 3 : 0);
               int bottom = y + h + (last ? 3 : 0);
               PacketUtil.init(kit.graphics, autismclient.gui.f.NetHelper.init(left, top, contentW, bottom - top), colors.cardSurface);
               break;
            case QUOTE:
               PacketUtil.load(kit.graphics, left + 2, y, h, colors.borderSubtle);
               break;
            case BULLET:
               if (first) {
                  kit.drawStatusDot(x - 5, y + h / 2, 1.5F, colors.textSecondary);
               }
               break;
            case NUMBERED:
               if (!line.marker().isEmpty()) {
                  kit.drawText(line.marker(), x - kit.measureTextWidth(line.marker()) - 4, y, colors.textSecondary);
               }
         }

         int textY = line.deco() == AiTranscriptLayout.RowDecoration.HEADING ? y + 1 : y;
         int base = line.deco() == AiTranscriptLayout.RowDecoration.QUOTE ? colors.textSecondary : colors.textPrimary;
         float cx = x;

         for (AiTranscriptLayout.StyledSpan span : line.spans()) {
            int style = span.style();
            int color = base;
            if ((style & 4) != 0 && line.deco() != AiTranscriptLayout.RowDecoration.CODE) {
               PacketUtil.load(kit.graphics, autismclient.gui.f.NetHelper.init(Math.round(cx) - 1, y, span.width() + 2, h), 2, colors.innerHighlight);
               color = -10497050;
            } else if ((style & 8) != 0) {
               color = colors.accent;
            }

            kit.drawStyledSpan(span.text(), cx, textY, style, color);
            cx += span.width();
         }
      }
   }

   private void drawWorkingIndicator(AiUiKit kit, int x, int y, int width, int height, long now) {
      float pulse = 0.55F + 0.45F * (float)Math.sin(now / 220.0);
      kit.drawStatusDot(x + 4, y + height / 2, 2.5F, PacketUtil.init(-10497050, pulse));
      AiService.TranscriptEntry last = this.view.latestTranscriptEntry();
      long since = Math.max(this.view.workStartedMs, last == null ? 0L : last.at());
      String seconds = Math.max(0L, (now - since) / 1000L) + "s";
      String progress = this.view.statusLine;
      int room = width - 12;
      String label = !progress.isEmpty() && !progress.equalsIgnoreCase("thinking")
         ? kit.ellipsizeLeft(progress, room - kit.measureTextWidth(" - " + seconds)) + " - " + seconds
         : "Thinking " + seconds;
      kit.drawEllipsizedText(label, x + 11, y + 1, room, kit.colors.textSecondary);
   }

   private void init_V_cE_NetHelper(AiUiKit kit, autismclient.gui.f.NetHelper v) {
      autismclient.gui.ui.UiColorScheme colors = kit.colors;
      int ctrl = AiUiKit.toolbarHeight();
      if (!this.view.hasProviderKey && !this.view.settings.builtIn) {
         String title = "Connect a model provider";
         int w = Math.min(v.update() - 16, 230);
         int h = AiUiKit.inputHeight() + 2 + AiUiKit.lineHeight() + 2 + ctrl + 4 + 2;
         autismclient.gui.f.NetHelper card = autismclient.gui.f.NetHelper.init(
            v.read() + (v.update() - w) / 2, v.write() + Math.max(4, (v.sync() - h) / 3), w, h
         );
         autismclient.gui.f.b.BufferHelper.init_V(kit.renderContext, card, title);
         int y = autismclient.gui.f.b.BufferHelper.init_I(kit.renderContext, card, title) + 1;
         kit.drawEllipsizedText("Add an API key to get started.", card.read() + 5, y, card.update() - 10, colors.textSecondary);
         y += AiUiKit.lineHeight() + 2;
         kit.drawButton(
            autismclient.gui.f.NetHelper.init(card.read() + 5, y, kit.measureButtonWidth("Open settings"), ctrl),
            "Open settings",
            autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
            this.host::showSettings
         );
      } else {
         int x = v.read() + 4;
         int y = v.write() + 6;
         int w = v.update() - 8;
         kit.pushClip(v);
         kit.drawEllipsizedText(
            this.view.settings.builtIn ? "Pick an action below." : "Ask a question or give the assistant a task.", x, y, w, colors.textSecondary
         );
         y += AiUiKit.lineHeight() + 2 + 2;
         kit.drawEllipsizedText(
            autismclient.util.ValueSink.init_Str(this.view.settings.emergencyKey) + " stops the assistant at any time.", x, y, w, colors.textMuted
         );
         kit.popClip();
      }
   }

   private void toggleMessageExpanded(AiService.TranscriptEntry message, boolean expanded) {
      this.expandedOverrides.put(message, !expanded);
      this.layoutRevision++;
      if (this.transcriptScroll.followBottom && this.transcriptScroll.maxScroll() > 0) {
         this.transcriptScroll.followBottom = false;
      }
   }

   private static void copyMessageToClipboard(AiService.TranscriptEntry message) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.keyboardHandler != null) {
         mc.keyboardHandler.setClipboard(AiTranscriptLayout.copyText(message));
         autismclient.util.bi.init("Message copied");
      }
   }

   private static int composerHeight() {
      return AiUiKit.inputHeight() + 2 + 2 + AiUiKit.toolbarHeight() + 4;
   }

   private void drawPlanReviewCard(AiUiKit kit, autismclient.gui.f.NetHelper card, int lines) {
      String title = "Plan ready for review";
      kit.blockClicks(card);
      autismclient.gui.f.b.BufferHelper.init_V(kit.renderContext, card, title);
      int y = autismclient.gui.f.b.BufferHelper.init_I(kit.renderContext, card, title);
      int x = card.read() + 5;
      int w = card.update() - 10;
      if (lines > 0) {
         int epoch = textEpoch();
         if (this.wrappedPlanWidth != w || this.wrappedPlanEpoch != epoch || !this.wrappedPlanSource.equals(this.pendingPlanText)) {
            this.wrappedPlanLines = kit.renderContext.init_PoolEntry().wrapLines(this.pendingPlanText, w);
            this.wrappedPlanWidth = w;
            this.wrappedPlanEpoch = epoch;
            this.wrappedPlanSource = this.pendingPlanText;
         }

         List<String> wrapped = this.wrappedPlanLines;

         for (int i = 0; i < Math.min(lines, wrapped.size()); i++) {
            kit.drawEllipsizedText(wrapped.get(i), x, y + i * AiUiKit.lineHeight(), w, kit.colors.textSecondary);
         }

         y += lines * AiUiKit.lineHeight();
      }

      y += 2;
      int ctrl = AiUiKit.toolbarHeight();
      int approveW = kit.measureButtonWidth("Approve") + 12;
      kit.drawIconLabelButton(
         autismclient.gui.f.NetHelper.init(x, y, approveW, ctrl),
         autismclient.util.GuiIconAtlas.CHECK,
         "Approve",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.write,
         this::update
      );
      kit.drawButton(
         autismclient.gui.f.NetHelper.init(x + approveW + 2, y, kit.measureButtonWidth("Reject"), ctrl),
         "Reject",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         this::sync,
         "Reject with the composer text as feedback"
      );
   }

   private static int load(int options) {
      int ctrl = AiUiKit.toolbarHeight();
      return AiUiKit.inputHeight() + 2 + 2 + (options > 0 ? options * (ctrl + 2) : AiUiKit.lineHeight() + 2) + 4;
   }

   private List<String> wrappedQuestionFor(AiUiKit kit, AiService.UserPromptQuestion q, int w) {
      int epoch = textEpoch();
      if (this.flush != w || this.close != epoch || !this.wrappedQuestionText.equals(q.text())) {
         this.wrappedQuestionLines = kit.renderContext.init_PoolEntry().wrapLines(AiUiKit.flattenSingleLine(q.text()), Math.max(20, w));
         this.flush = w;
         this.close = epoch;
         this.wrappedQuestionText = q.text();
      }

      return this.wrappedQuestionLines;
   }

   private void drawQuestionCard(AiUiKit kit, autismclient.gui.f.NetHelper card, AiService.UserPromptQuestion q, int lines) {
      String title = "The assistant asks";
      kit.blockClicks(card);
      autismclient.gui.f.b.BufferHelper.init_V(kit.renderContext, card, title);
      int y = autismclient.gui.f.b.BufferHelper.init_I(kit.renderContext, card, title);
      int x = card.read() + 5;
      int w = card.update() - 10;
      List<String> wrapped = this.wrappedQuestionFor(kit, q, w);
      int shown = Math.min(lines, wrapped.size());

      for (int i = 0; i < shown; i++) {
         String text = wrapped.get(i);
         if (i == shown - 1 && wrapped.size() > shown) {
            text = kit.renderContext.init_PoolEntry().ellipsize(text + " ...", w);
         }

         kit.drawEllipsizedText(text, x, y + i * AiUiKit.lineHeight(), w, kit.colors.textPrimary);
      }

      y += shown * AiUiKit.lineHeight() + 2;
      int ctrl = AiUiKit.toolbarHeight();
      if (q.options().isEmpty()) {
         kit.drawEllipsizedText("Type your answer below and press Send.", x, y + 1, w, kit.colors.textSecondary);
      } else {
         for (int i = 0; i < q.options().size(); i++) {
            String option = q.options().get(i);
            String label = kit.renderContext.init_PoolEntry().ellipsize(i + 1 + ". " + option, w - 12);
            kit.drawButton(
               autismclient.gui.f.NetHelper.init(x, y, w, ctrl),
               label,
               i == 0 ? autismclient.gui.f.b.CachePool.Inner_DataUtil.read : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
               () -> this.answerQuestion(option),
               !label.endsWith("...") && label.contains(option) ? null : option
            );
            y += ctrl + 2;
         }
      }
   }

   private void answerQuestion(String text) {
      this.service.answerPendingQuestion(text);
      this.transcriptScroll.scrollToBottom();
      this.view.invalidate();
   }

   private String lastAssistantText() {
      List<AiService.TranscriptEntry> messages = this.view.transcript;

      for (int i = messages.size() - 1; i >= 0; i--) {
         if ("assistant".equals(messages.get(i).role())) {
            return truncateFlat(messages.get(i).text(), 600);
         }
      }

      return truncateFlat(this.view.planNotes, 600);
   }

   private List<String[]> buildTaskRows() {
      List<String[]> rows = new ArrayList<>();
      rows.add(new String[]{"Objective", truncateFlat(this.view.objective, 400)});
      List<String> updates = this.view.steeringNotes;

      for (int i = Math.max(0, updates.size() - 3); i < updates.size(); i++) {
         rows.add(new String[]{"Update", truncateFlat(updates.get(i), 400)});
      }

      if (!this.view.planNotes.isBlank()) {
         rows.add(new String[]{"State", truncateFlat(this.view.planNotes, 400)});
      }

      if (!this.view.queuedObjectives.isEmpty()) {
         rows.add(new String[]{"Queued", this.view.queuedObjectives.size() + ": " + truncateFlat(this.view.queuedObjectives.get(0), 400)});
      }

      return rows;
   }

   private void drawTaskCard(AiUiKit kit, autismclient.gui.f.NetHelper card, List<String[]> rows) {
      String title = "Task";
      kit.blockClicks(card);
      autismclient.gui.f.b.BufferHelper.init_V(kit.renderContext, card, title);
      int y = autismclient.gui.f.b.BufferHelper.init_I(kit.renderContext, card, title);
      int x = card.read() + 5;
      int w = card.update() - 10;
      int labelW = kit.measureTextWidth("Objective") + 8;

      for (String[] row : rows) {
         autismclient.gui.f.NetHelper r = autismclient.gui.f.NetHelper.init(x, y, w, AiUiKit.rowHeight());
         kit.drawCenteredRowText(row[0], r, x, labelW - 4, kit.colors.textSecondary);
         kit.drawCenteredRowText(row[1], r, x + labelW, w - labelW, kit.colors.textPrimary);
         if (kit.isHovered(r) && kit.measureTextWidth(row[1]) > w - labelW) {
            kit.addTooltipRegion(r, row[1]);
         }

         y += AiUiKit.rowHeight();
      }
   }

   private void load(AiUiKit kit, autismclient.gui.f.NetHelper c) {
      int h = c.sync();
      int right = c.init();
      int attachW = kit.measureButtonWidth(this.attachmentPending ? "Loading..." : "Attach");
      kit.drawButton(
         autismclient.gui.f.NetHelper.init(c.read(), c.write(), attachW, h),
         this.attachmentPending ? "Loading..." : "Attach",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
         this::read_V,
         "Browse for a .schem, .schematic or .litematic file"
      );
      int fieldX = c.read() + attachW + 2;
      boolean canResume = !this.view.awaitingPlanApproval
         && (
            this.view.runState == AiService.RunState.PAUSED
               || (this.view.runState == AiService.RunState.BLOCKED || this.view.runState == AiService.RunState.FAILED)
                  && !this.view.objective.isBlank()
         );
      if (this.view.isActive()) {
         right -= h;
         kit.drawIconButton(
            autismclient.gui.f.NetHelper.init(right, c.write(), h, h),
            autismclient.util.GuiIconAtlas.STOP,
            false,
            false,
            "Stop (" + autismclient.util.ValueSink.init_Str(this.view.settings.emergencyKey) + ")",
            this::open
         );
         right -= 2;
      }

      if (this.view.isWorking()) {
         right -= h;
         kit.drawIconButton(autismclient.gui.f.NetHelper.init(right, c.write(), h, h), autismclient.util.GuiIconAtlas.PAUSE, false, false, "Pause", this::flush);
         right -= 2;
      } else if (canResume) {
         right -= h;
         kit.drawIconButton(autismclient.gui.f.NetHelper.init(right, c.write(), h, h), autismclient.util.GuiIconAtlas.PLAY, false, false, "Resume", this::close);
         right -= 2;
      }

      int sendW = kit.measureButtonWidth("Send");
      right -= sendW;
      kit.drawButton(
         autismclient.gui.f.NetHelper.init(right, c.write(), sendW, h), "Send", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this::submitComposerInput
      );
      right -= 2;
      if (this.view.isActive() && !this.view.awaitingPlanApproval) {
         right -= h;
         kit.drawIconButton(autismclient.gui.f.NetHelper.init(right, c.write(), h, h), autismclient.util.GuiIconAtlas.QUEUE, false, false, "Queue as the next objective", this::write);
         right -= 2;
      }

      this.input
         .setPlaceholder(
            this.view.pendingQuestion != null
               ? "Answer the question..."
               : (
                  this.view.awaitingPlanApproval
                     ? (this.load_Z ? "Say what to change, then Reject" : "Feedback for the plan...")
                     : (
                        this.view.isWorking()
                           ? "Add instructions to the running task..."
                           : (this.isPlanModeShown() ? "Describe a task to plan first (Shift+Tab: act directly)..." : "Ask or give a task...")
                     )
               )
         );
      kit.drawTextInput(this.input, autismclient.gui.f.NetHelper.init(fieldX, c.write(), Math.max(20, right - fieldX), h));
   }

   private void read_V() {
      if (!this.attachmentPending && this.attachments.size() < 4) {
         this.attachmentPending = true;
         String draftSession = this.lastSessionId;
         CompletableFuture.<AiAttachments.Inner_DataUtil>supplyAsync(() -> {
            try {
               String path = AiAttachments.pickFile();
               return path != null && !path.isBlank() ? AiAttachments.attach(Path.of(path), AiSchematic.directory()) : null;
            } catch (Exception e) {
               throw new CompletionException(e);
            }
         }).whenComplete((file, error) -> Minecraft.getInstance().execute(() -> {
            this.attachmentPending = false;
            if (error != null) {
               Throwable cause = error.getCause() == null ? error : error.getCause();
               autismclient.util.bi.write("Could not attach schematic: " + cause.getMessage());
            } else if (file != null && draftSession.equals(this.lastSessionId) && !this.attachments.contains(file)) {
               this.attachments.add(file);
               this.host.focusInput(this.input);
            }
         }));
      }
   }

   boolean isPlanModeShown() {
      if (this.view.settings.builtIn) {
         return false;
      }

      long now = System.currentTimeMillis();
      if (this.planModeTogglePending && (now > this.planModeToggleDeadline || this.view.settings.planMode == this.pendingPlanMode)) {
         this.planModeTogglePending = false;
      }

      return this.planModeTogglePending ? this.pendingPlanMode : this.view.settings.planMode;
   }

   void togglePlanMode() {
      if (!this.view.settings.builtIn) {
         boolean next = !this.isPlanModeShown();
         this.planModeTogglePending = true;
         this.pendingPlanMode = next;
         this.planModeToggleDeadline = System.currentTimeMillis() + 1500L;
         this.service.editSettingsAsync(s -> s.planMode = next);
         autismclient.util.bi.init(
            next ? "Plan mode: research first, then a plan for your approval" : "Act mode: tasks execute directly", next ? -14249 : -10497050
         );
         this.view.invalidate();
      }
   }

   private void drawPlanModeChip(AiUiKit kit, autismclient.gui.f.NetHelper s, int x) {
      boolean on = this.isPlanModeShown();
      String label = on ? "Plan" : "Act";
      autismclient.gui.f.NetHelper chip = autismclient.gui.f.NetHelper.init(x, s.write() + (s.sync() - AiUiKit.rowHeight()) / 2, kit.measureTextWidth(label) + 10, AiUiKit.rowHeight());
      autismclient.gui.ui.widgets.PillBadgeRenderer.render(kit.renderContext, chip, label, on ? -14249 : kit.colors.accent, on, kit.isHovered(chip));
      kit.addInteractiveRegion(
         chip,
         this::togglePlanMode,
         null,
         on
            ? "Plan mode: the assistant only researches and proposes a plan you approve first. Shift+Tab switches to act mode."
            : "Act mode: tasks execute directly. Shift+Tab switches to plan mode (research and a plan for approval first)."
      );
   }

   private String takeComposerInput() {
      String text = this.input.getValue().trim();
      if (text.isEmpty()) {
         return null;
      }

      this.input.pushHistoryEntry(text);
      this.input.setValue("");
      return text;
   }

   void submitComposerInput() {
      if (!this.attachmentPending && (!this.input.getValue().isBlank() || !this.attachments.isEmpty())) {
         if (this.view.pendingQuestion != null) {
            this.answerQuestion(this.takeComposerInput());
         } else if (this.view.awaitingPlanApproval) {
            this.sync();
         } else {
            String text = AiAttachments.composeMessage(this.input.getValue().isBlank() ? "" : this.takeComposerInput(), this.attachments);
            this.attachments.clear();
            this.service.submitPrompt(text);
            this.transcriptScroll.scrollToBottom();
            this.view.invalidate();
         }
      }
   }

   private void write() {
      if (!this.attachmentPending) {
         String text = this.input.getValue().isBlank() && !this.attachments.isEmpty() ? "" : this.takeComposerInput();
         if (text == null) {
            this.host.focusInput(this.input);
         } else {
            this.service.enqueueObjective(AiAttachments.composeMessage(text, this.attachments));
            this.attachments.clear();
            autismclient.util.bi.load("Queued as the next objective");
            this.view.invalidate();
         }
      }
   }

   private void update() {
      this.service.approvePlan();
      this.transcriptScroll.scrollToBottom();
      this.view.invalidate();
   }

   private void sync() {
      if (this.input.getValue().isBlank()) {
         this.load_Z = true;
         this.host.focusInput(this.input);
      } else {
         String text = this.takeComposerInput();
         this.service.rejectPlan(text);
         this.load_Z = false;
         this.transcriptScroll.scrollToBottom();
         this.view.invalidate();
      }
   }

   private void flush() {
      this.service.pauseTask();
      this.view.invalidate();
   }

   private void close() {
      this.service.resumeTask();
      this.transcriptScroll.scrollToBottom();
      this.view.invalidate();
   }

   private void open() {
      this.service.stopTask();
      this.view.invalidate();
   }

   private void read(AiUiKit kit, autismclient.gui.f.NetHelper s) {
      autismclient.gui.ui.UiColorScheme colors = kit.colors;
      int limit = s.init() - 10;
      int x = s.read() + 2;
      kit.drawStatusDot(x + 3, s.write() + s.sync() / 2, 2.5F, this.statusColor(kit));
      x += 10;
      String label = this.statusLabel();
      kit.drawCenteredRowText(label, s, x, limit - x, colors.textPrimary);
      x += kit.measureTextWidth(label) + 8;
      if (limit - x > 60 && !this.view.settings.builtIn) {
         this.drawPlanModeChip(kit, s, x);
         x += kit.measureTextWidth(this.isPlanModeShown() ? "Plan" : "Act") + 10 + 8;
      }

      if (this.view.chatModeActive && this.view.runState != AiService.RunState.CHATTING && limit - x > 40) {
         kit.drawCenteredRowText("Chat", s, x, limit - x, -10497050);
         x += kit.measureTextWidth("Chat") + 8;
      }

      AiService.TokenUsage u = this.view.tokenUsage;
      if (this.view.settings.builtIn) {
         if (!this.view.statusLine.isBlank() && x < limit) {
            kit.drawCenteredRowText(this.view.statusLine, s, x, limit - x, colors.textSecondary);
         }

         x = limit;
      }

      if (u.lastPromptTokens() > 0L && u.contextWindow() > 0 && x < limit) {
         String ctx = "ctx " + AiUiKit.formatCompactCount(u.lastPromptTokens()) + " / " + AiUiKit.formatCompactCount(u.contextWindow());
         boolean tight = u.lastPromptTokens() > u.contextWindow() * 0.7;
         kit.drawCenteredRowText(ctx, s, x, limit - x, tight ? -14249 : colors.textSecondary);
         x += kit.measureTextWidth(ctx) + 8;
      }

      long prompt = u.inputTokens() + u.cachedInputTokens() + u.cacheWriteTokens();
      if (u.cachedInputTokens() > 0L && prompt > 0L && x < limit) {
         String cache = "cache " + Math.round(100.0 * u.cachedInputTokens() / prompt) + "%";
         kit.drawCenteredRowText(cache, s, x, limit - x, colors.textSecondary);
         x += kit.measureTextWidth(cache) + 8;
      }

      if (u.requests() > 0L && x < limit) {
         String req = u.requests() + " req";
         kit.drawCenteredRowText(req, s, x, limit - x, colors.textSecondary);
      }

      autismclient.gui.f.NetHelper hit = autismclient.gui.f.NetHelper.init(s.read(), s.write(), Math.max(1, limit - s.read()), s.sync());
      boolean hasTask = !this.view.objective.isBlank();
      kit.addInteractiveRegion(hit, hasTask ? () -> this.init_Z = !this.init_Z : null, null, kit.isHovered(hit) ? this.usageTooltip(hasTask) : null);
   }

   private String statusLabel() {
      if (this.view.pendingQuestion != null) {
         return "Your turn";
      }

      if (this.view.awaitingPlanApproval) {
         return "Awaiting approval";
      }

      String var10000;
      switch (this.view.runState) {
         case IDLE:
            var10000 = "Ready";
            break;
         case RUNNING:
            var10000 = "Working";
            break;
         case PLANNING:
            var10000 = "Planning";
            break;
         case PAUSED:
            var10000 = "Paused";
            break;
         case COMPLETED:
            var10000 = "Done";
            break;
         case ANSWERED:
            var10000 = "Answered";
            break;
         case BLOCKED:
            var10000 = "Blocked";
            break;
         case FAILED:
            var10000 = "Failed";
            break;
         case CHATTING:
            var10000 = "Chatting";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private int statusColor(AiUiKit kit) {
      return this.view.pendingQuestion == null && !this.view.awaitingPlanApproval ? stateColor(kit, this.view.runState.name()) : -14249;
   }

   static int stateColor(AiUiKit kit, String state) {
      int var10000;
      switch (state) {
         case "RUNNING":
         case "PLANNING":
         case "CHATTING":
            var10000 = -10497050;
            break;
         case "PAUSED":
            var10000 = -14249;
            break;
         case "COMPLETED":
         case "ANSWERED":
            var10000 = kit.colors.success;
            break;
         case "BLOCKED":
         case "FAILED":
            var10000 = kit.colors.danger;
            break;
         default:
            var10000 = kit.colors.textMuted;
      }

      return var10000;
   }

   private String usageTooltip(boolean hasTask) {
      AiService.TokenUsage u = this.view.tokenUsage;
      StringBuilder tip = new StringBuilder();
      if (this.view.settings.builtIn) {
         tip.append("Built-In: nothing is sent to an API");
      } else {
         tip.append("Input ")
            .append(AiUiKit.formatCompactCount(u.inputTokens()))
            .append(", cached ")
            .append(AiUiKit.formatCompactCount(u.cachedInputTokens()))
            .append(", cache writes ")
            .append(AiUiKit.formatCompactCount(u.cacheWriteTokens()))
            .append("\nOutput ")
            .append(AiUiKit.formatCompactCount(u.outputTokens()))
            .append(", requests ")
            .append(u.requests());
         if (u.lastPromptTokens() > 0L) {
            tip.append("\nLast prompt ").append(AiUiKit.formatCompactCount(u.lastPromptTokens())).append(" of ").append(AiUiKit.formatCompactCount(u.contextWindow()));
         }
      }

      if (this.view.chatModeActive) {
         tip.append("\nChat: ").append(truncateFlat(this.view.chatModeSummary, 160));
      }

      if (hasTask) {
         tip.append("\nObjective: ").append(truncateFlat(this.view.objective, 160));
         if (!this.view.planNotes.isBlank()) {
            tip.append("\nTask: ").append(truncateFlat(this.view.planNotes, 160));
         }

         tip.append(this.init_Z ? "\nClick to hide task details" : "\nClick for task details");
      }

      return tip.toString();
   }

   private static String truncateFlat(String text, int max) {
      String flat = AiUiKit.flattenSingleLine(text);
      return flat.length() <= max ? flat : flat.substring(0, max) + "...";
   }
}
