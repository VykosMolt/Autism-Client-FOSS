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
   private final AiService init_cc;
   private final AiViewState init_cF;
   private final AiAssistantOverlay init_AiAssistantOverlay;
   final autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil = new autismclient.gui.ui.widgets.TextInputWidget().setMaxLength(16000).setHistoryNavigationEnabled(true);
   final AiActionMenu init_StreamUtil;
   private final AiTranscriptLayout init_cC = new AiTranscriptLayout();
   private final AiUiKit.Inner_StreamUtil init_cE_StreamUtil = new AiUiKit.Inner_StreamUtil(true);
   private final Map<AiService.TranscriptEntry, Boolean> init_Map = new IdentityHashMap<>();
   private AiTranscriptLayout.Inner_ConfigStore init_cC_ConfigStore;
   private int read_I;
   private boolean init_Z;
   private boolean load_Z;
   private String init_Str = "";
   private int write_I = -1;
   private String load_Str = "";
   private String read_Str = "";
   private List<String> init_List = List.of();
   private int update_I = -1;
   private int sync_I;
   private List<String[]> load_List = List.of();
   private autismclient.gui.f.NetHelper init_NetHelper = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
   private final ArrayList<AiAttachments.Inner_DataUtil> init_ArrayList = new ArrayList<>();
   private boolean read_Z;
   private boolean write_Z;
   private final Map<AiService.TranscriptEntry, String> load_Map = new IdentityHashMap<>();
   private String write_Str = "";
   private List<String> read_List = List.of();
   private int flush = -1;
   private int close;
   private boolean update_Z;
   private boolean sync_Z;
   private long init_J;

   void init_V_Str_Str(String action, String query) {
      this.init_StreamUtil.init_V_Str_Str(action, query);
   }

   void init_V_Z(boolean enabled) {
      this.write_Z = enabled;
      if (!enabled) {
         this.init_ArrayList.clear();
      }
   }

   int init_I() {
      if (this.init_cF.settings.builtIn) {
         return this.init_StreamUtil.init_I() + AiUiKit.toolbarHeight() + AiUiKit.rowHeight() * 2 + 6;
      }

      int rows = this.write_Z ? Math.max(2, this.init_ArrayList.size()) : this.init_ArrayList.size();
      int composer = this.init_cF.settings.builtIn ? this.init_StreamUtil.init_I() : AiUiKit.toolbarHeight();
      return AiUiKit.lineHeight() * 3 + 2 + composer + 2 + AiUiKit.rowHeight() + rows * (AiUiKit.toolbarHeight() + 2);
   }

   AiChatView(AiService service, AiViewState view, AiAssistantOverlay host) {
      this.init_cc = service;
      this.init_cF = view;
      this.init_AiAssistantOverlay = host;
      this.init_StreamUtil = new AiActionMenu(service, view, host, this.init_cE_StreamUtil::scrollToBottom);
      this.init_BitSetUtil.setOnSubmit(text -> this.load_V());
   }

   autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil() {
      return this.init_cF.settings.builtIn ? this.init_StreamUtil.init_BitSetUtil : this.init_BitSetUtil;
   }

   boolean init_Z_I_BitSetUtil(int key, autismclient.gui.ui.widgets.TextInputWidget focused) {
      return this.init_cF.settings.builtIn && (focused == null || this.init_StreamUtil.init_List().contains(focused))
         ? this.init_StreamUtil.init_Z_I_Z(key, focused != null)
         : false;
   }

   boolean init_Z() {
      return this.init_cF.settings.builtIn && (this.init_Z || this.init_StreamUtil.init_Z());
   }

   boolean load_Z() {
      if (!this.init_cF.settings.builtIn) {
         return false;
      } else if (this.init_Z) {
         this.init_Z = false;
         return true;
      } else {
         return this.init_StreamUtil.load_Z();
      }
   }

   int init_I_I(int width) {
      if (this.init_cF.settings.builtIn && !this.init_Z && this.init_cF.pendingQuestion == null) {
         int height = this.init_StreamUtil.init_I_I(width);
         return height == 0 ? 0 : height + AiUiKit.toolbarHeight() + AiUiKit.rowHeight() * 2 + 8;
      } else {
         return 0;
      }
   }

   autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil_C(char c) {
      return this.init_cF.settings.builtIn ? this.init_StreamUtil.init_BitSetUtil_C(c) : null;
   }

   autismclient.gui.f.NetHelper init_NetHelper() {
      return this.init_NetHelper;
   }

   void init_V_cE_2(AiUiKit kit, autismclient.gui.f.NetHelper area, long now) {
      if (!this.init_cF.activeSessionId.equals(this.init_Str)) {
         this.init_Str = this.init_cF.activeSessionId;
         this.init_Map.clear();
         this.init_cC.init_V();
         this.init_cE_StreamUtil.scrollToBottom();
         this.init_Z = false;
         this.load_Z = false;
         this.init_ArrayList.clear();
      }

      if (!this.init_cF.awaitingPlanApproval) {
         this.load_Z = false;
      }

      if (this.write_Z && this.init_ArrayList.isEmpty()) {
         this.init_ArrayList.add(new AiAttachments.Inner_DataUtil("31774.litematic", "litematic-v7", 1060, 3201L));
         this.init_ArrayList.add(new AiAttachments.Inner_DataUtil("31669.schem", "sponge-v3", 23031, 12606L));
      }

      if (this.write_I != this.init_cF.pollCount) {
         this.write_I = this.init_cF.pollCount;
         this.load_Str = this.init_cF.awaitingPlanApproval ? this.init_Str() : "";
         this.load_List = List.of();
      }

      int ctrl = AiUiKit.toolbarHeight();
      boolean builtIn = this.init_cF.settings.builtIn;
      if (builtIn && this.init_BitSetUtil.isFocused()) {
         this.init_BitSetUtil.setFocused(false);
      }

      if (!builtIn) {
         this.init_StreamUtil.init_V();
         this.init_StreamUtil.load_V();
      }

      autismclient.gui.f.NetHelper status = autismclient.gui.f.NetHelper.init(area.read(), area.load() - AiUiKit.rowHeight(), area.update(), AiUiKit.rowHeight());
      if (builtIn) {
         this.init_V_cE_2(kit, area, status, now);
      } else {
         int composerH = builtIn ? this.init_StreamUtil.init_I_cE(kit, area.update(), status.write() - 2 - area.write() - AiUiKit.lineHeight() * 3 - 2) : ctrl;
         autismclient.gui.f.NetHelper composer = autismclient.gui.f.NetHelper.init(area.read(), status.write() - 2 - composerH, area.update(), composerH);
         int bottom = composer.write() - 2;

         for (int i = this.init_ArrayList.size() - 1; i >= 0; i--) {
            AiAttachments.Inner_DataUtil file = this.init_ArrayList.get(i);
            autismclient.gui.f.NetHelper row = autismclient.gui.f.NetHelper.init(area.read(), bottom - ctrl, area.update(), ctrl);
            int remove = ctrl;
            kit.drawButton(
               autismclient.gui.f.NetHelper.init(row.read(), row.write(), row.update() - remove - 2, ctrl),
               file.name() + " (" + file.cells() + " blocks)",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
               () -> this.init_BitSetUtil.setValue("build \"" + file.name() + "\""),
               file.format() + " / " + file.bytes() + " bytes"
            );
            kit.drawIconButton(
               autismclient.gui.f.NetHelper.init(row.init() - remove, row.write(), remove, ctrl),
               autismclient.util.GuiIconAtlas.DELETE,
               false,
               false,
               "Remove attachment",
               () -> this.init_ArrayList.remove(file)
            );
            bottom = row.write() - 2;
         }

         this.init_NetHelper = autismclient.gui.f.NetHelper.init(area.read(), area.write(), area.update(), Math.max(0, bottom - area.write()));
         int keep = AiUiKit.lineHeight() * 3;
         if (this.init_cF.pendingQuestion != null) {
            AiService.UserPromptQuestion q = this.init_cF.pendingQuestion;
            int options = q.options().size();
            int base = load(options);
            int wrapped = this.init_List_cE(kit, q, area.update() - 10).size();
            int lines = Math.max(1, Math.min(Math.min(4, wrapped), (bottom - area.write() - keep - base - 2) / AiUiKit.lineHeight()));
            int h = base + lines * AiUiKit.lineHeight();
            autismclient.gui.f.NetHelper card = autismclient.gui.f.NetHelper.init(area.read(), bottom - h, area.update(), h);
            this.init_V_cE(kit, card, q, lines);
            bottom = card.write() - 2;
         }

         if (this.init_cF.awaitingPlanApproval) {
            int base = read_I();
            int lines = Math.max(0, Math.min(3, (bottom - area.write() - keep - base - 2) / AiUiKit.lineHeight()));
            autismclient.gui.f.NetHelper card = autismclient.gui.f.NetHelper.init(
               area.read(), bottom - base - lines * AiUiKit.lineHeight(), area.update(), base + lines * AiUiKit.lineHeight()
            );
            this.init_V_cE(kit, card, lines);
            bottom = card.write() - 2;
         }

         if (this.init_Z && !this.init_cF.objective.isBlank()) {
            if (this.load_List.isEmpty()) {
               this.load_List = this.init_List();
            }

            List<String[]> rows = this.load_List;
            int title = AiUiKit.inputHeight() + 2;
            int fit = Math.max(1, Math.min(rows.size(), (bottom - area.write() - keep - title - 4) / AiUiKit.rowHeight()));
            int h = title + fit * AiUiKit.rowHeight() + 4;
            autismclient.gui.f.NetHelper card = autismclient.gui.f.NetHelper.init(area.read(), bottom - h, area.update(), h);
            this.init_V_cE_3(kit, card, rows.subList(Math.max(0, rows.size() - fit), rows.size()));
            bottom = card.write() - 2;
         }

         this.load_V_cE_2(kit, autismclient.gui.f.NetHelper.init(area.read(), area.write(), area.update(), Math.max(0, bottom - area.write())), now);
         if (builtIn) {
            this.init_StreamUtil.init_V_cE(kit, composer, now);
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
      String progress = this.init_cF.statusLine.isBlank() ? "No task running" : this.init_cF.statusLine;
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
      if (this.init_cF.isActive()
         || (this.init_cF.runState == AiService.RunState.FAILED || this.init_cF.runState == AiService.RunState.BLOCKED)
            && !this.init_cF.objective.isBlank()) {
         int stopW = kit.measureButtonWidth("Stop");
         right -= stopW;
         kit.drawButton(autismclient.gui.f.NetHelper.init(right, y, stopW, h), "Stop", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, () -> {
            this.init_cc.stopTask();
            this.init_cF.invalidate();
         });
         if (this.init_cF.isWorking()) {
            int pauseW = kit.measureButtonWidth("Pause");
            right -= pauseW + 2;
            kit.drawButton(autismclient.gui.f.NetHelper.init(right, y, pauseW, h), "Pause", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, () -> {
               this.init_cc.pauseTask();
               this.init_cF.invalidate();
            });
         } else if (this.init_cF.runState == AiService.RunState.PAUSED
            || this.init_cF.runState == AiService.RunState.FAILED
            || this.init_cF.runState == AiService.RunState.BLOCKED) {
            int resumeW = kit.measureButtonWidth("Resume");
            right -= resumeW + 2;
            kit.drawButton(
               autismclient.gui.f.NetHelper.init(right, y, resumeW, h), "Resume", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, () -> {
                  this.init_cc.resumeTask();
                  this.init_cF.invalidate();
               }
            );
         }
      }

      bottom = activity.write() - 2;
      this.init_NetHelper = autismclient.gui.f.NetHelper.init(area.read(), area.write(), area.update(), Math.max(0, bottom - area.write()));
      if (this.init_cF.pendingQuestion != null) {
         this.init_V_cE(
            kit,
            this.init_NetHelper,
            this.init_cF.pendingQuestion,
            Math.min(3, this.init_List_cE(kit, this.init_cF.pendingQuestion, area.update() - 10).size())
         );
      } else if (this.init_Z) {
         autismclient.gui.f.NetHelper close = autismclient.gui.f.NetHelper.init(area.read(), area.write(), area.update(), h);
         kit.drawButton(close, "Back to actions", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, () -> this.init_Z = false);
         this.load_V_cE_2(kit, autismclient.gui.f.NetHelper.init(area.read(), close.load() + 2, area.update(), Math.max(0, bottom - close.load() - 2)), now);
      } else {
         this.init_StreamUtil.init_V_cE(kit, this.init_NetHelper, now);
      }

      this.read(kit, status);
   }

   private AiTranscriptLayout.Inner_TaskQueue init_cC_TaskQueue_cE(AiUiKit kit) {
      int line = AiUiKit.lineHeight();
      return new AiTranscriptLayout.Inner_TaskQueue(line, AiUiKit.rowHeight(), 4 + kit.measureTextWidth("You") + 6, 13, 3, 2, 5);
   }

   private void load_V_cE_2(AiUiKit kit, autismclient.gui.f.NetHelper v, long now) {
      if (v.sync() >= 6) {
         autismclient.gui.ui.UiColorScheme colors = kit.colors;
         if (this.init_cF.transcript.isEmpty() && !this.init_cF.isWorking()) {
            this.init_V_cE_NetHelper(kit, v);
         } else {
            if (this.init_cC_ConfigStore == null) {
               this.init_cC_ConfigStore = AiUiKit.createTextMeasurer(kit.font);
            }

            int contentW = Math.max(40, v.update() - 6 - 3);
            AiTranscriptLayout.Inner_TaskQueue m = this.init_cC_TaskQueue_cE(kit);
            this.init_cC.init(this.init_cF.transcript, contentW, load_I(), m, this.init_Map, this.read_I, this.init_cC_ConfigStore);
            boolean live = this.init_cF.isWorking();
            int liveH = AiUiKit.lineHeight() + 2;
            int liveY = this.init_cC.init_I() + (this.init_cC.init_I() > 0 ? m.blockGap() : 0);
            this.init_cE_StreamUtil.layout(v, (live ? liveY + liveH : this.init_cC.init_I()) + 2);
            kit.registerScrollPane(this.init_cE_StreamUtil);
            kit.pushClip(v);
            int top = v.write() - this.init_cE_StreamUtil.offset;
            int viewBottom = this.init_cE_StreamUtil.offset + v.sync();
            List<AiTranscriptLayout.Inner_DataUtil> blocks = this.init_cC.load();

            for (int i = this.init_cC.load_I_I(this.init_cE_StreamUtil.offset); i < blocks.size(); i++) {
               AiTranscriptLayout.Inner_DataUtil block = blocks.get(i);
               if (block.top() >= viewBottom) {
                  break;
               }

               autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(v.read(), top + block.top(), contentW, block.bottom() - block.top());
               AiService.TranscriptEntry message = block.message();
               Runnable toggle = null;
               if (block.kind() == AiTranscriptLayout.Inner_CachePool.USER) {
                  PacketUtil.load(kit.graphics, bounds, 3, colors.cardSurface);
               } else if (block.kind() == AiTranscriptLayout.Inner_CachePool.TOOL && !message.detail().isBlank()) {
                  boolean expanded = block.expanded();
                  toggle = () -> this.init_V_cc_JsonUtil_Z(message, expanded);
                  autismclient.gui.f.NetHelper head = autismclient.gui.f.NetHelper.init(bounds.read(), bounds.write(), bounds.update(), m.tool());
                  if (kit.isHovered(head)) {
                     PacketUtil.load(kit.graphics, head, 3, colors.innerHighlight);
                  }
               }

               kit.addInteractiveRegion(bounds, toggle, () -> init_V_cc_JsonUtil(message), null);
            }

            List<AiTranscriptLayout.Inner_BufferHelper> lines = this.init_cC.init_List();

            for (int i = this.init_cC.init_I_I(this.init_cE_StreamUtil.offset); i < lines.size(); i++) {
               AiTranscriptLayout.Inner_BufferHelper line = lines.get(i);
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
                     this.init_V_cE_2(kit, line, v.read(), x, y, contentW);
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
                        kit.drawCenteredRowText(line.secondary(), row, nameX + nameW + 6, room - nameW - 6, this.init_I_cE_cc_JsonUtil(kit, line.message()));
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
               this.init_V_cE(kit, v.read(), top + liveY, contentW, liveH, now);
            }

            kit.popClip();
            kit.drawScrollbar(this.init_cE_StreamUtil);
         }
      }
   }

   static int load_I() {
      return autismclient.gui.sdf.MsdfTextRenderer.cacheGeneration() * 31 + Float.floatToIntBits(autismclient.gui.sdf.MsdfTextRenderer.fontScale());
   }

   private int init_I_cE_cc_JsonUtil(AiUiKit kit, AiService.TranscriptEntry message) {
      if (this.load_Map.size() > 400) {
         this.load_Map.clear();
      }

      int var10000;
      switch ((String)this.load_Map.computeIfAbsent(message, AiService::extractToolStatus)) {
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

   private void init_V_cE_2(AiUiKit kit, AiTranscriptLayout.Inner_BufferHelper line, int left, int x, int y, int contentW) {
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

         int textY = line.deco() == AiTranscriptLayout.Inner_StreamUtil.HEADING ? y + 1 : y;
         int base = line.deco() == AiTranscriptLayout.Inner_StreamUtil.QUOTE ? colors.textSecondary : colors.textPrimary;
         float cx = x;

         for (AiTranscriptLayout.Inner_JsonUtil span : line.spans()) {
            int style = span.style();
            int color = base;
            if ((style & 4) != 0 && line.deco() != AiTranscriptLayout.Inner_StreamUtil.CODE) {
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

   private void init_V_cE(AiUiKit kit, int x, int y, int width, int height, long now) {
      float pulse = 0.55F + 0.45F * (float)Math.sin(now / 220.0);
      kit.drawStatusDot(x + 4, y + height / 2, 2.5F, PacketUtil.init(-10497050, pulse));
      AiService.TranscriptEntry last = this.init_cF.latestTranscriptEntry();
      long since = Math.max(this.init_cF.workStartedMs, last == null ? 0L : last.at());
      String seconds = Math.max(0L, (now - since) / 1000L) + "s";
      String progress = this.init_cF.statusLine;
      int room = width - 12;
      String label = !progress.isEmpty() && !progress.equalsIgnoreCase("thinking")
         ? kit.ellipsizeLeft(progress, room - kit.measureTextWidth(" - " + seconds)) + " - " + seconds
         : "Thinking " + seconds;
      kit.drawEllipsizedText(label, x + 11, y + 1, room, kit.colors.textSecondary);
   }

   private void init_V_cE_NetHelper(AiUiKit kit, autismclient.gui.f.NetHelper v) {
      autismclient.gui.ui.UiColorScheme colors = kit.colors;
      int ctrl = AiUiKit.toolbarHeight();
      if (!this.init_cF.hasProviderKey && !this.init_cF.settings.builtIn) {
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
            this.init_AiAssistantOverlay::load_V
         );
      } else {
         int x = v.read() + 4;
         int y = v.write() + 6;
         int w = v.update() - 8;
         kit.pushClip(v);
         kit.drawEllipsizedText(
            this.init_cF.settings.builtIn ? "Pick an action below." : "Ask a question or give the assistant a task.", x, y, w, colors.textSecondary
         );
         y += AiUiKit.lineHeight() + 2 + 2;
         kit.drawEllipsizedText(
            autismclient.util.ValueSink.init_Str(this.init_cF.settings.emergencyKey) + " stops the assistant at any time.", x, y, w, colors.textMuted
         );
         kit.popClip();
      }
   }

   private void init_V_cc_JsonUtil_Z(AiService.TranscriptEntry message, boolean expanded) {
      this.init_Map.put(message, !expanded);
      this.read_I++;
      if (this.init_cE_StreamUtil.followBottom && this.init_cE_StreamUtil.maxScroll() > 0) {
         this.init_cE_StreamUtil.followBottom = false;
      }
   }

   private static void init_V_cc_JsonUtil(AiService.TranscriptEntry message) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.keyboardHandler != null) {
         mc.keyboardHandler.setClipboard(AiTranscriptLayout.init_Str_cc_JsonUtil(message));
         autismclient.util.bi.init("Message copied");
      }
   }

   private static int read_I() {
      return AiUiKit.inputHeight() + 2 + 2 + AiUiKit.toolbarHeight() + 4;
   }

   private void init_V_cE(AiUiKit kit, autismclient.gui.f.NetHelper card, int lines) {
      String title = "Plan ready for review";
      kit.blockClicks(card);
      autismclient.gui.f.b.BufferHelper.init_V(kit.renderContext, card, title);
      int y = autismclient.gui.f.b.BufferHelper.init_I(kit.renderContext, card, title);
      int x = card.read() + 5;
      int w = card.update() - 10;
      if (lines > 0) {
         int epoch = load_I();
         if (this.update_I != w || this.sync_I != epoch || !this.read_Str.equals(this.load_Str)) {
            this.init_List = kit.renderContext.init_PoolEntry().wrapLines(this.load_Str, w);
            this.update_I = w;
            this.sync_I = epoch;
            this.read_Str = this.load_Str;
         }

         List<String> wrapped = this.init_List;

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

   private List<String> init_List_cE(AiUiKit kit, AiService.UserPromptQuestion q, int w) {
      int epoch = load_I();
      if (this.flush != w || this.close != epoch || !this.write_Str.equals(q.text())) {
         this.read_List = kit.renderContext.init_PoolEntry().wrapLines(AiUiKit.flattenSingleLine(q.text()), Math.max(20, w));
         this.flush = w;
         this.close = epoch;
         this.write_Str = q.text();
      }

      return this.read_List;
   }

   private void init_V_cE(AiUiKit kit, autismclient.gui.f.NetHelper card, AiService.UserPromptQuestion q, int lines) {
      String title = "The assistant asks";
      kit.blockClicks(card);
      autismclient.gui.f.b.BufferHelper.init_V(kit.renderContext, card, title);
      int y = autismclient.gui.f.b.BufferHelper.init_I(kit.renderContext, card, title);
      int x = card.read() + 5;
      int w = card.update() - 10;
      List<String> wrapped = this.init_List_cE(kit, q, w);
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
               () -> this.init_V_Str(option),
               !label.endsWith("...") && label.contains(option) ? null : option
            );
            y += ctrl + 2;
         }
      }
   }

   private void init_V_Str(String text) {
      this.init_cc.answerPendingQuestion(text);
      this.init_cE_StreamUtil.scrollToBottom();
      this.init_cF.invalidate();
   }

   private String init_Str() {
      List<AiService.TranscriptEntry> messages = this.init_cF.transcript;

      for (int i = messages.size() - 1; i >= 0; i--) {
         if ("assistant".equals(messages.get(i).role())) {
            return init_Str_Str_I(messages.get(i).text(), 600);
         }
      }

      return init_Str_Str_I(this.init_cF.planNotes, 600);
   }

   private List<String[]> init_List() {
      List<String[]> rows = new ArrayList<>();
      rows.add(new String[]{"Objective", init_Str_Str_I(this.init_cF.objective, 400)});
      List<String> updates = this.init_cF.steeringNotes;

      for (int i = Math.max(0, updates.size() - 3); i < updates.size(); i++) {
         rows.add(new String[]{"Update", init_Str_Str_I(updates.get(i), 400)});
      }

      if (!this.init_cF.planNotes.isBlank()) {
         rows.add(new String[]{"State", init_Str_Str_I(this.init_cF.planNotes, 400)});
      }

      if (!this.init_cF.queuedObjectives.isEmpty()) {
         rows.add(new String[]{"Queued", this.init_cF.queuedObjectives.size() + ": " + init_Str_Str_I(this.init_cF.queuedObjectives.get(0), 400)});
      }

      return rows;
   }

   private void init_V_cE_3(AiUiKit kit, autismclient.gui.f.NetHelper card, List<String[]> rows) {
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
      int attachW = kit.measureButtonWidth(this.read_Z ? "Loading..." : "Attach");
      kit.drawButton(
         autismclient.gui.f.NetHelper.init(c.read(), c.write(), attachW, h),
         this.read_Z ? "Loading..." : "Attach",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
         this::read_V,
         "Browse for a .schem, .schematic or .litematic file"
      );
      int fieldX = c.read() + attachW + 2;
      boolean canResume = !this.init_cF.awaitingPlanApproval
         && (
            this.init_cF.runState == AiService.RunState.PAUSED
               || (this.init_cF.runState == AiService.RunState.BLOCKED || this.init_cF.runState == AiService.RunState.FAILED)
                  && !this.init_cF.objective.isBlank()
         );
      if (this.init_cF.isActive()) {
         right -= h;
         kit.drawIconButton(
            autismclient.gui.f.NetHelper.init(right, c.write(), h, h),
            autismclient.util.GuiIconAtlas.STOP,
            false,
            false,
            "Stop (" + autismclient.util.ValueSink.init_Str(this.init_cF.settings.emergencyKey) + ")",
            this::open
         );
         right -= 2;
      }

      if (this.init_cF.isWorking()) {
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
         autismclient.gui.f.NetHelper.init(right, c.write(), sendW, h), "Send", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this::load_V
      );
      right -= 2;
      if (this.init_cF.isActive() && !this.init_cF.awaitingPlanApproval) {
         right -= h;
         kit.drawIconButton(autismclient.gui.f.NetHelper.init(right, c.write(), h, h), autismclient.util.GuiIconAtlas.QUEUE, false, false, "Queue as the next objective", this::write);
         right -= 2;
      }

      this.init_BitSetUtil
         .setPlaceholder(
            this.init_cF.pendingQuestion != null
               ? "Answer the question..."
               : (
                  this.init_cF.awaitingPlanApproval
                     ? (this.load_Z ? "Say what to change, then Reject" : "Feedback for the plan...")
                     : (
                        this.init_cF.isWorking()
                           ? "Add instructions to the running task..."
                           : (this.read_Z() ? "Describe a task to plan first (Shift+Tab: act directly)..." : "Ask or give a task...")
                     )
               )
         );
      kit.drawTextInput(this.init_BitSetUtil, autismclient.gui.f.NetHelper.init(fieldX, c.write(), Math.max(20, right - fieldX), h));
   }

   private void read_V() {
      if (!this.read_Z && this.init_ArrayList.size() < 4) {
         this.read_Z = true;
         String draftSession = this.init_Str;
         CompletableFuture.<AiAttachments.Inner_DataUtil>supplyAsync(() -> {
            try {
               String path = AiAttachments.init();
               return path != null && !path.isBlank() ? AiAttachments.init(Path.of(path), AiSchematic.init()) : null;
            } catch (Exception e) {
               throw new CompletionException(e);
            }
         }).whenComplete((file, error) -> Minecraft.getInstance().execute(() -> {
            this.read_Z = false;
            if (error != null) {
               Throwable cause = error.getCause() == null ? error : error.getCause();
               autismclient.util.bi.write("Could not attach schematic: " + cause.getMessage());
            } else if (file != null && draftSession.equals(this.init_Str) && !this.init_ArrayList.contains(file)) {
               this.init_ArrayList.add(file);
               this.init_AiAssistantOverlay.init(this.init_BitSetUtil);
            }
         }));
      }
   }

   boolean read_Z() {
      if (this.init_cF.settings.builtIn) {
         return false;
      }

      long now = System.currentTimeMillis();
      if (this.update_Z && (now > this.init_J || this.init_cF.settings.planMode == this.sync_Z)) {
         this.update_Z = false;
      }

      return this.update_Z ? this.sync_Z : this.init_cF.settings.planMode;
   }

   void init_V() {
      if (!this.init_cF.settings.builtIn) {
         boolean next = !this.read_Z();
         this.update_Z = true;
         this.sync_Z = next;
         this.init_J = System.currentTimeMillis() + 1500L;
         this.init_cc.editSettingsAsync(s -> s.planMode = next);
         autismclient.util.bi.init(
            next ? "Plan mode: research first, then a plan for your approval" : "Act mode: tasks execute directly", next ? -14249 : -10497050
         );
         this.init_cF.invalidate();
      }
   }

   private void load_V_cE(AiUiKit kit, autismclient.gui.f.NetHelper s, int x) {
      boolean on = this.read_Z();
      String label = on ? "Plan" : "Act";
      autismclient.gui.f.NetHelper chip = autismclient.gui.f.NetHelper.init(x, s.write() + (s.sync() - AiUiKit.rowHeight()) / 2, kit.measureTextWidth(label) + 10, AiUiKit.rowHeight());
      autismclient.gui.ui.widgets.PillBadgeRenderer.init(kit.renderContext, chip, label, on ? -14249 : kit.colors.accent, on, kit.isHovered(chip));
      kit.addInteractiveRegion(
         chip,
         this::init_V,
         null,
         on
            ? "Plan mode: the assistant only researches and proposes a plan you approve first. Shift+Tab switches to act mode."
            : "Act mode: tasks execute directly. Shift+Tab switches to plan mode (research and a plan for approval first)."
      );
   }

   private String load_Str() {
      String text = this.init_BitSetUtil.getValue().trim();
      if (text.isEmpty()) {
         return null;
      }

      this.init_BitSetUtil.pushHistoryEntry(text);
      this.init_BitSetUtil.setValue("");
      return text;
   }

   void load_V() {
      if (!this.read_Z && (!this.init_BitSetUtil.getValue().isBlank() || !this.init_ArrayList.isEmpty())) {
         if (this.init_cF.pendingQuestion != null) {
            this.init_V_Str(this.load_Str());
         } else if (this.init_cF.awaitingPlanApproval) {
            this.sync();
         } else {
            String text = AiAttachments.init(this.init_BitSetUtil.getValue().isBlank() ? "" : this.load_Str(), this.init_ArrayList);
            this.init_ArrayList.clear();
            this.init_cc.submitPrompt(text);
            this.init_cE_StreamUtil.scrollToBottom();
            this.init_cF.invalidate();
         }
      }
   }

   private void write() {
      if (!this.read_Z) {
         String text = this.init_BitSetUtil.getValue().isBlank() && !this.init_ArrayList.isEmpty() ? "" : this.load_Str();
         if (text == null) {
            this.init_AiAssistantOverlay.init(this.init_BitSetUtil);
         } else {
            this.init_cc.enqueueObjective(AiAttachments.init(text, this.init_ArrayList));
            this.init_ArrayList.clear();
            autismclient.util.bi.load("Queued as the next objective");
            this.init_cF.invalidate();
         }
      }
   }

   private void update() {
      this.init_cc.approvePlan();
      this.init_cE_StreamUtil.scrollToBottom();
      this.init_cF.invalidate();
   }

   private void sync() {
      if (this.init_BitSetUtil.getValue().isBlank()) {
         this.load_Z = true;
         this.init_AiAssistantOverlay.init(this.init_BitSetUtil);
      } else {
         String text = this.load_Str();
         this.init_cc.rejectPlan(text);
         this.load_Z = false;
         this.init_cE_StreamUtil.scrollToBottom();
         this.init_cF.invalidate();
      }
   }

   private void flush() {
      this.init_cc.pauseTask();
      this.init_cF.invalidate();
   }

   private void close() {
      this.init_cc.resumeTask();
      this.init_cE_StreamUtil.scrollToBottom();
      this.init_cF.invalidate();
   }

   private void open() {
      this.init_cc.stopTask();
      this.init_cF.invalidate();
   }

   private void read(AiUiKit kit, autismclient.gui.f.NetHelper s) {
      autismclient.gui.ui.UiColorScheme colors = kit.colors;
      int limit = s.init() - 10;
      int x = s.read() + 2;
      kit.drawStatusDot(x + 3, s.write() + s.sync() / 2, 2.5F, this.init_I_cE(kit));
      x += 10;
      String label = this.read_Str();
      kit.drawCenteredRowText(label, s, x, limit - x, colors.textPrimary);
      x += kit.measureTextWidth(label) + 8;
      if (limit - x > 60 && !this.init_cF.settings.builtIn) {
         this.load_V_cE(kit, s, x);
         x += kit.measureTextWidth(this.read_Z() ? "Plan" : "Act") + 10 + 8;
      }

      if (this.init_cF.chatModeActive && this.init_cF.runState != AiService.RunState.CHATTING && limit - x > 40) {
         kit.drawCenteredRowText("Chat", s, x, limit - x, -10497050);
         x += kit.measureTextWidth("Chat") + 8;
      }

      AiService.TokenUsage u = this.init_cF.tokenUsage;
      if (this.init_cF.settings.builtIn) {
         if (!this.init_cF.statusLine.isBlank() && x < limit) {
            kit.drawCenteredRowText(this.init_cF.statusLine, s, x, limit - x, colors.textSecondary);
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
      boolean hasTask = !this.init_cF.objective.isBlank();
      kit.addInteractiveRegion(hit, hasTask ? () -> this.init_Z = !this.init_Z : null, null, kit.isHovered(hit) ? this.init_Str_Z(hasTask) : null);
   }

   private String read_Str() {
      if (this.init_cF.pendingQuestion != null) {
         return "Your turn";
      }

      if (this.init_cF.awaitingPlanApproval) {
         return "Awaiting approval";
      }

      String var10000;
      switch (this.init_cF.runState) {
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

   private int init_I_cE(AiUiKit kit) {
      return this.init_cF.pendingQuestion == null && !this.init_cF.awaitingPlanApproval ? init_I_cE_Str(kit, this.init_cF.runState.name()) : -14249;
   }

   static int init_I_cE_Str(AiUiKit kit, String state) {
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

   private String init_Str_Z(boolean hasTask) {
      AiService.TokenUsage u = this.init_cF.tokenUsage;
      StringBuilder tip = new StringBuilder();
      if (this.init_cF.settings.builtIn) {
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

      if (this.init_cF.chatModeActive) {
         tip.append("\nChat: ").append(init_Str_Str_I(this.init_cF.chatModeSummary, 160));
      }

      if (hasTask) {
         tip.append("\nObjective: ").append(init_Str_Str_I(this.init_cF.objective, 160));
         if (!this.init_cF.planNotes.isBlank()) {
            tip.append("\nTask: ").append(init_Str_Str_I(this.init_cF.planNotes, 160));
         }

         tip.append(this.init_Z ? "\nClick to hide task details" : "\nClick for task details");
      }

      return tip.toString();
   }

   private static String init_Str_Str_I(String text, int max) {
      String flat = AiUiKit.flattenSingleLine(text);
      return flat.length() <= max ? flat : flat.substring(0, max) + "...";
   }
}
