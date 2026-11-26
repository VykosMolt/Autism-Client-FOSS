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
   final autismclient.gui.f.b.TextInputWidget init_BitSetUtil = new autismclient.gui.f.b.TextInputWidget().init_BitSetUtil_I(16000).flush(true);
   final AiActionMenu init_StreamUtil;
   private final AiTranscriptLayout init_cC = new AiTranscriptLayout();
   private final AiUiKit.Inner_StreamUtil init_cE_StreamUtil = new AiUiKit.Inner_StreamUtil(true);
   private final Map<AiService.Inner_JsonUtil, Boolean> init_Map = new IdentityHashMap<>();
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
   private final Map<AiService.Inner_JsonUtil, String> load_Map = new IdentityHashMap<>();
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
      if (this.init_cF.init_AiSettings.builtIn) {
         return this.init_StreamUtil.init_I() + AiUiKit.init_I() + AiUiKit.read() * 2 + 6;
      }

      int rows = this.write_Z ? Math.max(2, this.init_ArrayList.size()) : this.init_ArrayList.size();
      int composer = this.init_cF.init_AiSettings.builtIn ? this.init_StreamUtil.init_I() : AiUiKit.init_I();
      return AiUiKit.write() * 3 + 2 + composer + 2 + AiUiKit.read() + rows * (AiUiKit.init_I() + 2);
   }

   AiChatView(AiService service, AiViewState view, AiAssistantOverlay host) {
      this.init_cc = service;
      this.init_cF = view;
      this.init_AiAssistantOverlay = host;
      this.init_StreamUtil = new AiActionMenu(service, view, host, this.init_cE_StreamUtil::init_V);
      this.init_BitSetUtil.load_BitSetUtil_Consumer(text -> this.load_V());
   }

   autismclient.gui.f.b.TextInputWidget init_BitSetUtil() {
      return this.init_cF.init_AiSettings.builtIn ? this.init_StreamUtil.init_BitSetUtil : this.init_BitSetUtil;
   }

   boolean init_Z_I_BitSetUtil(int key, autismclient.gui.f.b.TextInputWidget focused) {
      return this.init_cF.init_AiSettings.builtIn && (focused == null || this.init_StreamUtil.init_List().contains(focused))
         ? this.init_StreamUtil.init_Z_I_Z(key, focused != null)
         : false;
   }

   boolean init_Z() {
      return this.init_cF.init_AiSettings.builtIn && (this.init_Z || this.init_StreamUtil.init_Z());
   }

   boolean load_Z() {
      if (!this.init_cF.init_AiSettings.builtIn) {
         return false;
      } else if (this.init_Z) {
         this.init_Z = false;
         return true;
      } else {
         return this.init_StreamUtil.load_Z();
      }
   }

   int init_I_I(int width) {
      if (this.init_cF.init_AiSettings.builtIn && !this.init_Z && this.init_cF.init_cc_UiHelper == null) {
         int height = this.init_StreamUtil.init_I_I(width);
         return height == 0 ? 0 : height + AiUiKit.init_I() + AiUiKit.read() * 2 + 8;
      } else {
         return 0;
      }
   }

   autismclient.gui.f.b.TextInputWidget init_BitSetUtil_C(char c) {
      return this.init_cF.init_AiSettings.builtIn ? this.init_StreamUtil.init_BitSetUtil_C(c) : null;
   }

   autismclient.gui.f.NetHelper init_NetHelper() {
      return this.init_NetHelper;
   }

   void init_V_cE_2(AiUiKit kit, autismclient.gui.f.NetHelper area, long now) {
      if (!this.init_cF.read_Str.equals(this.init_Str)) {
         this.init_Str = this.init_cF.read_Str;
         this.init_Map.clear();
         this.init_cC.init_V();
         this.init_cE_StreamUtil.init_V();
         this.init_Z = false;
         this.load_Z = false;
         this.init_ArrayList.clear();
      }

      if (!this.init_cF.init_Z) {
         this.load_Z = false;
      }

      if (this.write_Z && this.init_ArrayList.isEmpty()) {
         this.init_ArrayList.add(new AiAttachments.Inner_DataUtil("31774.litematic", "litematic-v7", 1060, 3201L));
         this.init_ArrayList.add(new AiAttachments.Inner_DataUtil("31669.schem", "sponge-v3", 23031, 12606L));
      }

      if (this.write_I != this.init_cF.init_I) {
         this.write_I = this.init_cF.init_I;
         this.load_Str = this.init_cF.init_Z ? this.init_Str() : "";
         this.load_List = List.of();
      }

      int ctrl = AiUiKit.init_I();
      boolean builtIn = this.init_cF.init_AiSettings.builtIn;
      if (builtIn && this.init_BitSetUtil.load_Z()) {
         this.init_BitSetUtil.init_V_Z(false);
      }

      if (!builtIn) {
         this.init_StreamUtil.init_V();
         this.init_StreamUtil.load_V();
      }

      autismclient.gui.f.NetHelper status = autismclient.gui.f.NetHelper.init(area.read(), area.load() - AiUiKit.read(), area.update(), AiUiKit.read());
      if (builtIn) {
         this.init_V_cE_2(kit, area, status, now);
      } else {
         int composerH = builtIn ? this.init_StreamUtil.init_I_cE(kit, area.update(), status.write() - 2 - area.write() - AiUiKit.write() * 3 - 2) : ctrl;
         autismclient.gui.f.NetHelper composer = autismclient.gui.f.NetHelper.init(area.read(), status.write() - 2 - composerH, area.update(), composerH);
         int bottom = composer.write() - 2;

         for (int i = this.init_ArrayList.size() - 1; i >= 0; i--) {
            AiAttachments.Inner_DataUtil file = this.init_ArrayList.get(i);
            autismclient.gui.f.NetHelper row = autismclient.gui.f.NetHelper.init(area.read(), bottom - ctrl, area.update(), ctrl);
            int remove = ctrl;
            kit.init_V_NetHelper(
               autismclient.gui.f.NetHelper.init(row.read(), row.write(), row.update() - remove - 2, ctrl),
               file.name() + " (" + file.cells() + " blocks)",
               autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
               () -> this.init_BitSetUtil.init_BitSetUtil_Str("build \"" + file.name() + "\""),
               file.format() + " / " + file.bytes() + " bytes"
            );
            kit.init(
               autismclient.gui.f.NetHelper.init(row.init() - remove, row.write(), remove, ctrl),
               autismclient.util.GuiIconAtlas.open,
               false,
               false,
               "Remove attachment",
               () -> this.init_ArrayList.remove(file)
            );
            bottom = row.write() - 2;
         }

         this.init_NetHelper = autismclient.gui.f.NetHelper.init(area.read(), area.write(), area.update(), Math.max(0, bottom - area.write()));
         int keep = AiUiKit.write() * 3;
         if (this.init_cF.init_cc_UiHelper != null) {
            AiService.Inner_UiHelper q = this.init_cF.init_cc_UiHelper;
            int options = q.options().size();
            int base = load(options);
            int wrapped = this.init_List_cE(kit, q, area.update() - 10).size();
            int lines = Math.max(1, Math.min(Math.min(4, wrapped), (bottom - area.write() - keep - base - 2) / AiUiKit.write()));
            int h = base + lines * AiUiKit.write();
            autismclient.gui.f.NetHelper card = autismclient.gui.f.NetHelper.init(area.read(), bottom - h, area.update(), h);
            this.init_V_cE(kit, card, q, lines);
            bottom = card.write() - 2;
         }

         if (this.init_cF.init_Z) {
            int base = read_I();
            int lines = Math.max(0, Math.min(3, (bottom - area.write() - keep - base - 2) / AiUiKit.write()));
            autismclient.gui.f.NetHelper card = autismclient.gui.f.NetHelper.init(
               area.read(), bottom - base - lines * AiUiKit.write(), area.update(), base + lines * AiUiKit.write()
            );
            this.init_V_cE(kit, card, lines);
            bottom = card.write() - 2;
         }

         if (this.init_Z && !this.init_cF.write_Str.isBlank()) {
            if (this.load_List.isEmpty()) {
               this.load_List = this.init_List();
            }

            List<String[]> rows = this.load_List;
            int title = AiUiKit.load_I() + 2;
            int fit = Math.max(1, Math.min(rows.size(), (bottom - area.write() - keep - title - 4) / AiUiKit.read()));
            int h = title + fit * AiUiKit.read() + 4;
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
      int h = AiUiKit.init_I();
      int line = AiUiKit.read();
      int bottom = status.write() - 2;
      autismclient.gui.f.NetHelper activity = autismclient.gui.f.NetHelper.init(area.read(), bottom - h - line - 2, area.update(), h + line + 2);
      String progress = this.init_cF.init_Str.isBlank() ? "No task running" : this.init_cF.init_Str;
      kit.init_V_Str_3(
         progress,
         autismclient.gui.f.NetHelper.init(activity.read(), activity.write(), activity.update(), line),
         activity.read(),
         activity.update(),
         kit.init_JsonUtil.valid
      );
      kit.init_V_NetHelper_Str(autismclient.gui.f.NetHelper.init(activity.read(), activity.write(), activity.update(), line), progress);
      int y = activity.write() + line;
      int w = kit.load(this.init_Z ? "Hide activity" : "Activity");
      kit.init_V_NetHelper_2(
         autismclient.gui.f.NetHelper.init(activity.read(), y, w, h),
         this.init_Z ? "Hide activity" : "Activity",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
         () -> this.init_Z = !this.init_Z
      );
      int right = activity.init();
      if (this.init_cF.load()
         || (this.init_cF.init_cc_FileCache == AiService.Inner_FileCache.close || this.init_cF.init_cc_FileCache == AiService.Inner_FileCache.flush)
            && !this.init_cF.write_Str.isBlank()) {
         int stopW = kit.load("Stop");
         right -= stopW;
         kit.init_V_NetHelper_2(autismclient.gui.f.NetHelper.init(right, y, stopW, h), "Stop", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, () -> {
            this.init_cc.update_V();
            this.init_cF.init_V();
         });
         if (this.init_cF.init_Z()) {
            int pauseW = kit.load("Pause");
            right -= pauseW + 2;
            kit.init_V_NetHelper_2(autismclient.gui.f.NetHelper.init(right, y, pauseW, h), "Pause", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, () -> {
               this.init_cc.write_V();
               this.init_cF.init_V();
            });
         } else if (this.init_cF.init_cc_FileCache == AiService.Inner_FileCache.write
            || this.init_cF.init_cc_FileCache == AiService.Inner_FileCache.close
            || this.init_cF.init_cc_FileCache == AiService.Inner_FileCache.flush) {
            int resumeW = kit.load("Resume");
            right -= resumeW + 2;
            kit.init_V_NetHelper_2(
               autismclient.gui.f.NetHelper.init(right, y, resumeW, h), "Resume", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, () -> {
                  this.init_cc.read_V();
                  this.init_cF.init_V();
               }
            );
         }
      }

      bottom = activity.write() - 2;
      this.init_NetHelper = autismclient.gui.f.NetHelper.init(area.read(), area.write(), area.update(), Math.max(0, bottom - area.write()));
      if (this.init_cF.init_cc_UiHelper != null) {
         this.init_V_cE(
            kit,
            this.init_NetHelper,
            this.init_cF.init_cc_UiHelper,
            Math.min(3, this.init_List_cE(kit, this.init_cF.init_cc_UiHelper, area.update() - 10).size())
         );
      } else if (this.init_Z) {
         autismclient.gui.f.NetHelper close = autismclient.gui.f.NetHelper.init(area.read(), area.write(), area.update(), h);
         kit.init_V_NetHelper_2(close, "Back to actions", autismclient.gui.f.b.CachePool.Inner_DataUtil.load, () -> this.init_Z = false);
         this.load_V_cE_2(kit, autismclient.gui.f.NetHelper.init(area.read(), close.load() + 2, area.update(), Math.max(0, bottom - close.load() - 2)), now);
      } else {
         this.init_StreamUtil.init_V_cE(kit, this.init_NetHelper, now);
      }

      this.read(kit, status);
   }

   private AiTranscriptLayout.Inner_TaskQueue init_cC_TaskQueue_cE(AiUiKit kit) {
      int line = AiUiKit.write();
      return new AiTranscriptLayout.Inner_TaskQueue(line, AiUiKit.read(), 4 + kit.init_I_Str("You") + 6, 13, 3, 2, 5);
   }

   private void load_V_cE_2(AiUiKit kit, autismclient.gui.f.NetHelper v, long now) {
      if (v.sync() >= 6) {
         autismclient.gui.f.UiColorScheme colors = kit.init_JsonUtil;
         if (this.init_cF.init_List.isEmpty() && !this.init_cF.init_Z()) {
            this.init_V_cE_NetHelper(kit, v);
         } else {
            if (this.init_cC_ConfigStore == null) {
               this.init_cC_ConfigStore = AiUiKit.init_cC_ConfigStore_Font(kit.init_Font);
            }

            int contentW = Math.max(40, v.update() - 6 - 3);
            AiTranscriptLayout.Inner_TaskQueue m = this.init_cC_TaskQueue_cE(kit);
            this.init_cC.init(this.init_cF.init_List, contentW, load_I(), m, this.init_Map, this.read_I, this.init_cC_ConfigStore);
            boolean live = this.init_cF.init_Z();
            int liveH = AiUiKit.write() + 2;
            int liveY = this.init_cC.init_I() + (this.init_cC.init_I() > 0 ? m.blockGap() : 0);
            this.init_cE_StreamUtil.init(v, (live ? liveY + liveH : this.init_cC.init_I()) + 2);
            kit.init_V_cE_StreamUtil(this.init_cE_StreamUtil);
            kit.init_V_NetHelper(v);
            int top = v.write() - this.init_cE_StreamUtil.init_I;
            int viewBottom = this.init_cE_StreamUtil.init_I + v.sync();
            List<AiTranscriptLayout.Inner_DataUtil> blocks = this.init_cC.load();

            for (int i = this.init_cC.load_I_I(this.init_cE_StreamUtil.init_I); i < blocks.size(); i++) {
               AiTranscriptLayout.Inner_DataUtil block = blocks.get(i);
               if (block.top() >= viewBottom) {
                  break;
               }

               autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(v.read(), top + block.top(), contentW, block.bottom() - block.top());
               AiService.Inner_JsonUtil message = block.message();
               Runnable toggle = null;
               if (block.kind() == AiTranscriptLayout.Inner_CachePool.init) {
                  PacketUtil.load(kit.init_GuiGraphicsExtract, bounds, 3, colors.open);
               } else if (block.kind() == AiTranscriptLayout.Inner_CachePool.flush && !message.detail().isBlank()) {
                  boolean expanded = block.expanded();
                  toggle = () -> this.init_V_cc_JsonUtil_Z(message, expanded);
                  autismclient.gui.f.NetHelper head = autismclient.gui.f.NetHelper.init(bounds.read(), bounds.write(), bounds.update(), m.tool());
                  if (kit.init_Z_NetHelper(head)) {
                     PacketUtil.load(kit.init_GuiGraphicsExtract, head, 3, colors.add);
                  }
               }

               kit.init_V_NetHelper(bounds, toggle, () -> init_V_cc_JsonUtil(message), null);
            }

            List<AiTranscriptLayout.Inner_BufferHelper> lines = this.init_cC.init_List();

            for (int i = this.init_cC.init_I_I(this.init_cE_StreamUtil.init_I); i < lines.size(); i++) {
               AiTranscriptLayout.Inner_BufferHelper line = lines.get(i);
               if (line.y() >= viewBottom) {
                  break;
               }

               int x = v.read() + line.x();
               int y = top + line.y();
               switch (line.kind()) {
                  case init:
                     if (i == 0 || lines.get(i - 1).message() != line.message()) {
                        kit.init_V_Str("You", v.read() + 4, y, -10497050);
                     }

                     kit.init_V_Str(line.text(), x, y, colors.prev);
                     break;
                  case load:
                     this.init_V_cE_2(kit, line, v.read(), x, y, contentW);
                     break;
                  case read:
                     kit.init_V_Str(line.text(), x, y, colors.valid);
                     break;
                  case write:
                     kit.init_V_Str(line.text(), x, y, colors.data);
                     break;
                  case update:
                     kit.init_V_Str(line.text(), x, y, colors.stop);
                     break;
                  case sync:
                     kit.init_V_Str(line.text(), x, y, -14249);
                     break;
                  case flush:
                     autismclient.gui.f.NetHelper row = autismclient.gui.f.NetHelper.init(v.read(), y, contentW, line.height());
                     if (!line.message().detail().isBlank()) {
                        PacketUtil.init(
                           kit.init_GuiGraphicsExtract,
                           autismclient.gui.f.NetHelper.init(v.read() + 2, y + (line.height() - 8) / 2, 8, 8),
                           line.expanded(),
                           colors.valid
                        );
                     }

                     int nameX = v.read() + 13;
                     int room = contentW - 13;
                     int nameW = Math.min(kit.init_I_Str(line.text()), room * 3 / 5);
                     kit.init_V_Str_3(line.text(), row, nameX, nameW, colors.prev);
                     if (!line.secondary().isEmpty()) {
                        kit.init_V_Str_3(line.secondary(), row, nameX + nameW + 6, room - nameW - 6, this.init_I_cE_cc_JsonUtil(kit, line.message()));
                     }
                     break;
                  case close:
                     PacketUtil.load(kit.init_GuiGraphicsExtract, x - 5, y, line.height(), colors.put);
                     kit.init_V_Str(line.text(), x, y, colors.valid);
                     break;
                  case open:
                     kit.init_V_Str(line.text(), x, y, colors.size);
               }
            }

            if (live) {
               this.init_V_cE(kit, v.read(), top + liveY, contentW, liveH, now);
            }

            kit.init_V();
            kit.load(this.init_cE_StreamUtil);
         }
      }
   }

   static int load_I() {
      return autismclient.gui.e.MsdfTextRenderer.init_I() * 31 + Float.floatToIntBits(autismclient.gui.e.MsdfTextRenderer.init_F());
   }

   private int init_I_cE_cc_JsonUtil(AiUiKit kit, AiService.Inner_JsonUtil message) {
      if (this.load_Map.size() > 400) {
         this.load_Map.clear();
      }

      int var10000;
      switch ((String)this.load_Map.computeIfAbsent(message, AiService::init_Str_cc_JsonUtil)) {
         case "FAILED":
         case "ERROR":
         case "TIMED_OUT":
            var10000 = kit.init_JsonUtil.data;
            break;
         case "UNCERTAIN":
         case "INTERRUPTED":
         case "CANCELLED":
         case "BLOCKED":
            var10000 = -14249;
            break;
         default:
            var10000 = kit.init_JsonUtil.valid;
      }

      return var10000;
   }

   private void init_V_cE_2(AiUiKit kit, AiTranscriptLayout.Inner_BufferHelper line, int left, int x, int y, int contentW) {
      autismclient.gui.f.UiColorScheme colors = kit.init_JsonUtil;
      if (line.spans() == null) {
         kit.init_V_Str(line.text(), x, y, colors.prev);
      } else {
         int h = line.height();
         boolean first = (line.flags() & 1) != 0;
         boolean last = (line.flags() & 2) != 0;
         switch (line.deco()) {
            case flush:
               PacketUtil.init_V_GuiGraphicsExtract_2(kit.init_GuiGraphicsExtract, left, y + h / 2, contentW, colors.put);
               return;
            case update:
               int top = y - (first ? 3 : 0);
               int bottom = y + h + (last ? 3 : 0);
               PacketUtil.init(kit.init_GuiGraphicsExtract, autismclient.gui.f.NetHelper.init(left, top, contentW, bottom - top), colors.open);
               break;
            case sync:
               PacketUtil.load(kit.init_GuiGraphicsExtract, left + 2, y, h, colors.get);
               break;
            case read:
               if (first) {
                  kit.init_V_I(x - 5, y + h / 2, 1.5F, colors.valid);
               }
               break;
            case write:
               if (!line.marker().isEmpty()) {
                  kit.init_V_Str(line.marker(), x - kit.init_I_Str(line.marker()) - 4, y, colors.valid);
               }
         }

         int textY = line.deco() == AiTranscriptLayout.Inner_StreamUtil.load ? y + 1 : y;
         int base = line.deco() == AiTranscriptLayout.Inner_StreamUtil.sync ? colors.valid : colors.prev;
         float cx = x;

         for (AiTranscriptLayout.Inner_JsonUtil span : line.spans()) {
            int style = span.style();
            int color = base;
            if ((style & 4) != 0 && line.deco() != AiTranscriptLayout.Inner_StreamUtil.update) {
               PacketUtil.load(kit.init_GuiGraphicsExtract, autismclient.gui.f.NetHelper.init(Math.round(cx) - 1, y, span.width() + 2, h), 2, colors.add);
               color = -10497050;
            } else if ((style & 8) != 0) {
               color = colors.clear;
            }

            kit.init_V_Str(span.text(), cx, textY, style, color);
            cx += span.width();
         }
      }
   }

   private void init_V_cE(AiUiKit kit, int x, int y, int width, int height, long now) {
      float pulse = 0.55F + 0.45F * (float)Math.sin(now / 220.0);
      kit.init_V_I(x + 4, y + height / 2, 2.5F, PacketUtil.init(-10497050, pulse));
      AiService.Inner_JsonUtil last = this.init_cF.init_cc_JsonUtil();
      long since = Math.max(this.init_cF.init_J, last == null ? 0L : last.at());
      String seconds = Math.max(0L, (now - since) / 1000L) + "s";
      String progress = this.init_cF.init_Str;
      int room = width - 12;
      String label = !progress.isEmpty() && !progress.equalsIgnoreCase("thinking")
         ? kit.init_Str_Str_I(progress, room - kit.init_I_Str(" - " + seconds)) + " - " + seconds
         : "Thinking " + seconds;
      kit.init_V_Str_2(label, x + 11, y + 1, room, kit.init_JsonUtil.valid);
   }

   private void init_V_cE_NetHelper(AiUiKit kit, autismclient.gui.f.NetHelper v) {
      autismclient.gui.f.UiColorScheme colors = kit.init_JsonUtil;
      int ctrl = AiUiKit.init_I();
      if (!this.init_cF.read_Z && !this.init_cF.init_AiSettings.builtIn) {
         String title = "Connect a model provider";
         int w = Math.min(v.update() - 16, 230);
         int h = AiUiKit.load_I() + 2 + AiUiKit.write() + 2 + ctrl + 4 + 2;
         autismclient.gui.f.NetHelper card = autismclient.gui.f.NetHelper.init(
            v.read() + (v.update() - w) / 2, v.write() + Math.max(4, (v.sync() - h) / 3), w, h
         );
         autismclient.gui.f.b.BufferHelper.init_V(kit.init_SyncManager, card, title);
         int y = autismclient.gui.f.b.BufferHelper.init_I(kit.init_SyncManager, card, title) + 1;
         kit.init_V_Str_2("Add an API key to get started.", card.read() + 5, y, card.update() - 10, colors.valid);
         y += AiUiKit.write() + 2;
         kit.init_V_NetHelper_2(
            autismclient.gui.f.NetHelper.init(card.read() + 5, y, kit.load("Open settings"), ctrl),
            "Open settings",
            autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
            this.init_AiAssistantOverlay::load_V
         );
      } else {
         int x = v.read() + 4;
         int y = v.write() + 6;
         int w = v.update() - 8;
         kit.init_V_NetHelper(v);
         kit.init_V_Str_2(
            this.init_cF.init_AiSettings.builtIn ? "Pick an action below." : "Ask a question or give the assistant a task.", x, y, w, colors.valid
         );
         y += AiUiKit.write() + 2 + 2;
         kit.init_V_Str_2(
            autismclient.util.ValueSink.init_Str(this.init_cF.init_AiSettings.emergencyKey) + " stops the assistant at any time.", x, y, w, colors.size
         );
         kit.init_V();
      }
   }

   private void init_V_cc_JsonUtil_Z(AiService.Inner_JsonUtil message, boolean expanded) {
      this.init_Map.put(message, !expanded);
      this.read_I++;
      if (this.init_cE_StreamUtil.init_Z && this.init_cE_StreamUtil.init_I() > 0) {
         this.init_cE_StreamUtil.init_Z = false;
      }
   }

   private static void init_V_cc_JsonUtil(AiService.Inner_JsonUtil message) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.keyboardHandler != null) {
         mc.keyboardHandler.setClipboard(AiTranscriptLayout.init_Str_cc_JsonUtil(message));
         autismclient.util.bi.init("Message copied");
      }
   }

   private static int read_I() {
      return AiUiKit.load_I() + 2 + 2 + AiUiKit.init_I() + 4;
   }

   private void init_V_cE(AiUiKit kit, autismclient.gui.f.NetHelper card, int lines) {
      String title = "Plan ready for review";
      kit.load_V(card);
      autismclient.gui.f.b.BufferHelper.init_V(kit.init_SyncManager, card, title);
      int y = autismclient.gui.f.b.BufferHelper.init_I(kit.init_SyncManager, card, title);
      int x = card.read() + 5;
      int w = card.update() - 10;
      if (lines > 0) {
         int epoch = load_I();
         if (this.update_I != w || this.sync_I != epoch || !this.read_Str.equals(this.load_Str)) {
            this.init_List = kit.init_SyncManager.init_PoolEntry().init_List(this.load_Str, w);
            this.update_I = w;
            this.sync_I = epoch;
            this.read_Str = this.load_Str;
         }

         List<String> wrapped = this.init_List;

         for (int i = 0; i < Math.min(lines, wrapped.size()); i++) {
            kit.init_V_Str_2(wrapped.get(i), x, y + i * AiUiKit.write(), w, kit.init_JsonUtil.valid);
         }

         y += lines * AiUiKit.write();
      }

      y += 2;
      int ctrl = AiUiKit.init_I();
      int approveW = kit.load("Approve") + 12;
      kit.init_V_NetHelper_3(
         autismclient.gui.f.NetHelper.init(x, y, approveW, ctrl),
         autismclient.util.GuiIconAtlas.read,
         "Approve",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.write,
         this::update
      );
      kit.init_V_NetHelper(
         autismclient.gui.f.NetHelper.init(x + approveW + 2, y, kit.load("Reject"), ctrl),
         "Reject",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         this::sync,
         "Reject with the composer text as feedback"
      );
   }

   private static int load(int options) {
      int ctrl = AiUiKit.init_I();
      return AiUiKit.load_I() + 2 + 2 + (options > 0 ? options * (ctrl + 2) : AiUiKit.write() + 2) + 4;
   }

   private List<String> init_List_cE(AiUiKit kit, AiService.Inner_UiHelper q, int w) {
      int epoch = load_I();
      if (this.flush != w || this.close != epoch || !this.write_Str.equals(q.text())) {
         this.read_List = kit.init_SyncManager.init_PoolEntry().init_List(AiUiKit.init_Str_Str(q.text()), Math.max(20, w));
         this.flush = w;
         this.close = epoch;
         this.write_Str = q.text();
      }

      return this.read_List;
   }

   private void init_V_cE(AiUiKit kit, autismclient.gui.f.NetHelper card, AiService.Inner_UiHelper q, int lines) {
      String title = "The assistant asks";
      kit.load_V(card);
      autismclient.gui.f.b.BufferHelper.init_V(kit.init_SyncManager, card, title);
      int y = autismclient.gui.f.b.BufferHelper.init_I(kit.init_SyncManager, card, title);
      int x = card.read() + 5;
      int w = card.update() - 10;
      List<String> wrapped = this.init_List_cE(kit, q, w);
      int shown = Math.min(lines, wrapped.size());

      for (int i = 0; i < shown; i++) {
         String text = wrapped.get(i);
         if (i == shown - 1 && wrapped.size() > shown) {
            text = kit.init_SyncManager.init_PoolEntry().load(text + " ...", w);
         }

         kit.init_V_Str_2(text, x, y + i * AiUiKit.write(), w, kit.init_JsonUtil.prev);
      }

      y += shown * AiUiKit.write() + 2;
      int ctrl = AiUiKit.init_I();
      if (q.options().isEmpty()) {
         kit.init_V_Str_2("Type your answer below and press Send.", x, y + 1, w, kit.init_JsonUtil.valid);
      } else {
         for (int i = 0; i < q.options().size(); i++) {
            String option = q.options().get(i);
            String label = kit.init_SyncManager.init_PoolEntry().load(i + 1 + ". " + option, w - 12);
            kit.init_V_NetHelper(
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
      this.init_cc.open(text);
      this.init_cE_StreamUtil.init_V();
      this.init_cF.init_V();
   }

   private String init_Str() {
      List<AiService.Inner_JsonUtil> messages = this.init_cF.init_List;

      for (int i = messages.size() - 1; i >= 0; i--) {
         if ("assistant".equals(messages.get(i).role())) {
            return init_Str_Str_I(messages.get(i).text(), 600);
         }
      }

      return init_Str_Str_I(this.init_cF.update_Str, 600);
   }

   private List<String[]> init_List() {
      List<String[]> rows = new ArrayList<>();
      rows.add(new String[]{"Objective", init_Str_Str_I(this.init_cF.write_Str, 400)});
      List<String> updates = this.init_cF.read_List;

      for (int i = Math.max(0, updates.size() - 3); i < updates.size(); i++) {
         rows.add(new String[]{"Update", init_Str_Str_I(updates.get(i), 400)});
      }

      if (!this.init_cF.update_Str.isBlank()) {
         rows.add(new String[]{"State", init_Str_Str_I(this.init_cF.update_Str, 400)});
      }

      if (!this.init_cF.load_List.isEmpty()) {
         rows.add(new String[]{"Queued", this.init_cF.load_List.size() + ": " + init_Str_Str_I(this.init_cF.load_List.get(0), 400)});
      }

      return rows;
   }

   private void init_V_cE_3(AiUiKit kit, autismclient.gui.f.NetHelper card, List<String[]> rows) {
      String title = "Task";
      kit.load_V(card);
      autismclient.gui.f.b.BufferHelper.init_V(kit.init_SyncManager, card, title);
      int y = autismclient.gui.f.b.BufferHelper.init_I(kit.init_SyncManager, card, title);
      int x = card.read() + 5;
      int w = card.update() - 10;
      int labelW = kit.init_I_Str("Objective") + 8;

      for (String[] row : rows) {
         autismclient.gui.f.NetHelper r = autismclient.gui.f.NetHelper.init(x, y, w, AiUiKit.read());
         kit.init_V_Str_3(row[0], r, x, labelW - 4, kit.init_JsonUtil.valid);
         kit.init_V_Str_3(row[1], r, x + labelW, w - labelW, kit.init_JsonUtil.prev);
         if (kit.init_Z_NetHelper(r) && kit.init_I_Str(row[1]) > w - labelW) {
            kit.init_V_NetHelper_Str(r, row[1]);
         }

         y += AiUiKit.read();
      }
   }

   private void load(AiUiKit kit, autismclient.gui.f.NetHelper c) {
      int h = c.sync();
      int right = c.init();
      int attachW = kit.load(this.read_Z ? "Loading..." : "Attach");
      kit.init_V_NetHelper(
         autismclient.gui.f.NetHelper.init(c.read(), c.write(), attachW, h),
         this.read_Z ? "Loading..." : "Attach",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.load,
         this::read_V,
         "Browse for a .schem, .schematic or .litematic file"
      );
      int fieldX = c.read() + attachW + 2;
      boolean canResume = !this.init_cF.init_Z
         && (
            this.init_cF.init_cc_FileCache == AiService.Inner_FileCache.write
               || (this.init_cF.init_cc_FileCache == AiService.Inner_FileCache.flush || this.init_cF.init_cc_FileCache == AiService.Inner_FileCache.close)
                  && !this.init_cF.write_Str.isBlank()
         );
      if (this.init_cF.load()) {
         right -= h;
         kit.init(
            autismclient.gui.f.NetHelper.init(right, c.write(), h, h),
            autismclient.util.GuiIconAtlas.close,
            false,
            false,
            "Stop (" + autismclient.util.ValueSink.init_Str(this.init_cF.init_AiSettings.emergencyKey) + ")",
            this::open
         );
         right -= 2;
      }

      if (this.init_cF.init_Z()) {
         right -= h;
         kit.init(autismclient.gui.f.NetHelper.init(right, c.write(), h, h), autismclient.util.GuiIconAtlas.run, false, false, "Pause", this::flush);
         right -= 2;
      } else if (canResume) {
         right -= h;
         kit.init(autismclient.gui.f.NetHelper.init(right, c.write(), h, h), autismclient.util.GuiIconAtlas.flush, false, false, "Resume", this::close);
         right -= 2;
      }

      int sendW = kit.load("Send");
      right -= sendW;
      kit.init_V_NetHelper_2(
         autismclient.gui.f.NetHelper.init(right, c.write(), sendW, h), "Send", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, this::load_V
      );
      right -= 2;
      if (this.init_cF.load() && !this.init_cF.init_Z) {
         right -= h;
         kit.init(autismclient.gui.f.NetHelper.init(right, c.write(), h, h), autismclient.util.GuiIconAtlas.put, false, false, "Queue as the next objective", this::write);
         right -= 2;
      }

      this.init_BitSetUtil
         .load_BitSetUtil_Str(
            this.init_cF.init_cc_UiHelper != null
               ? "Answer the question..."
               : (
                  this.init_cF.init_Z
                     ? (this.load_Z ? "Say what to change, then Reject" : "Feedback for the plan...")
                     : (
                        this.init_cF.init_Z()
                           ? "Add instructions to the running task..."
                           : (this.read_Z() ? "Describe a task to plan first (Shift+Tab: act directly)..." : "Ask or give a task...")
                     )
               )
         );
      kit.init_V_BitSetUtil_NetHelper(this.init_BitSetUtil, autismclient.gui.f.NetHelper.init(fieldX, c.write(), Math.max(20, right - fieldX), h));
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
      if (this.init_cF.init_AiSettings.builtIn) {
         return false;
      }

      long now = System.currentTimeMillis();
      if (this.update_Z && (now > this.init_J || this.init_cF.init_AiSettings.planMode == this.sync_Z)) {
         this.update_Z = false;
      }

      return this.update_Z ? this.sync_Z : this.init_cF.init_AiSettings.planMode;
   }

   void init_V() {
      if (!this.init_cF.init_AiSettings.builtIn) {
         boolean next = !this.read_Z();
         this.update_Z = true;
         this.sync_Z = next;
         this.init_J = System.currentTimeMillis() + 1500L;
         this.init_cc.init_V_Consumer(s -> s.planMode = next);
         autismclient.util.bi.init(
            next ? "Plan mode: research first, then a plan for your approval" : "Act mode: tasks execute directly", next ? -14249 : -10497050
         );
         this.init_cF.init_V();
      }
   }

   private void load_V_cE(AiUiKit kit, autismclient.gui.f.NetHelper s, int x) {
      boolean on = this.read_Z();
      String label = on ? "Plan" : "Act";
      autismclient.gui.f.NetHelper chip = autismclient.gui.f.NetHelper.init(x, s.write() + (s.sync() - AiUiKit.read()) / 2, kit.init_I_Str(label) + 10, AiUiKit.read());
      autismclient.gui.f.b.PillBadgeRenderer.init(kit.init_SyncManager, chip, label, on ? -14249 : kit.init_JsonUtil.clear, on, kit.init_Z_NetHelper(chip));
      kit.init_V_NetHelper(
         chip,
         this::init_V,
         null,
         on
            ? "Plan mode: the assistant only researches and proposes a plan you approve first. Shift+Tab switches to act mode."
            : "Act mode: tasks execute directly. Shift+Tab switches to plan mode (research and a plan for approval first)."
      );
   }

   private String load_Str() {
      String text = this.init_BitSetUtil.init_Str().trim();
      if (text.isEmpty()) {
         return null;
      }

      this.init_BitSetUtil.read_BitSetUtil_Str(text);
      this.init_BitSetUtil.init_BitSetUtil_Str("");
      return text;
   }

   void load_V() {
      if (!this.read_Z && (!this.init_BitSetUtil.init_Str().isBlank() || !this.init_ArrayList.isEmpty())) {
         if (this.init_cF.init_cc_UiHelper != null) {
            this.init_V_Str(this.load_Str());
         } else if (this.init_cF.init_Z) {
            this.sync();
         } else {
            String text = AiAttachments.init(this.init_BitSetUtil.init_Str().isBlank() ? "" : this.load_Str(), this.init_ArrayList);
            this.init_ArrayList.clear();
            this.init_cc.write_V_Str(text);
            this.init_cE_StreamUtil.init_V();
            this.init_cF.init_V();
         }
      }
   }

   private void write() {
      if (!this.read_Z) {
         String text = this.init_BitSetUtil.init_Str().isBlank() && !this.init_ArrayList.isEmpty() ? "" : this.load_Str();
         if (text == null) {
            this.init_AiAssistantOverlay.init(this.init_BitSetUtil);
         } else {
            this.init_cc.init_V_Str(AiAttachments.init(text, this.init_ArrayList));
            this.init_ArrayList.clear();
            autismclient.util.bi.load("Queued as the next objective");
            this.init_cF.init_V();
         }
      }
   }

   private void update() {
      this.init_cc.flush_Z();
      this.init_cE_StreamUtil.init_V();
      this.init_cF.init_V();
   }

   private void sync() {
      if (this.init_BitSetUtil.init_Str().isBlank()) {
         this.load_Z = true;
         this.init_AiAssistantOverlay.init(this.init_BitSetUtil);
      } else {
         String text = this.load_Str();
         this.init_cc.update_Z_Str(text);
         this.load_Z = false;
         this.init_cE_StreamUtil.init_V();
         this.init_cF.init_V();
      }
   }

   private void flush() {
      this.init_cc.write_V();
      this.init_cF.init_V();
   }

   private void close() {
      this.init_cc.read_V();
      this.init_cE_StreamUtil.init_V();
      this.init_cF.init_V();
   }

   private void open() {
      this.init_cc.update_V();
      this.init_cF.init_V();
   }

   private void read(AiUiKit kit, autismclient.gui.f.NetHelper s) {
      autismclient.gui.f.UiColorScheme colors = kit.init_JsonUtil;
      int limit = s.init() - 10;
      int x = s.read() + 2;
      kit.init_V_I(x + 3, s.write() + s.sync() / 2, 2.5F, this.init_I_cE(kit));
      x += 10;
      String label = this.read_Str();
      kit.init_V_Str_3(label, s, x, limit - x, colors.prev);
      x += kit.init_I_Str(label) + 8;
      if (limit - x > 60 && !this.init_cF.init_AiSettings.builtIn) {
         this.load_V_cE(kit, s, x);
         x += kit.init_I_Str(this.read_Z() ? "Plan" : "Act") + 10 + 8;
      }

      if (this.init_cF.load_Z && this.init_cF.init_cc_FileCache != AiService.Inner_FileCache.open && limit - x > 40) {
         kit.init_V_Str_3("Chat", s, x, limit - x, -10497050);
         x += kit.init_I_Str("Chat") + 8;
      }

      AiService.Inner_ByteReader u = this.init_cF.init_cc_ByteReader;
      if (this.init_cF.init_AiSettings.builtIn) {
         if (!this.init_cF.init_Str.isBlank() && x < limit) {
            kit.init_V_Str_3(this.init_cF.init_Str, s, x, limit - x, colors.valid);
         }

         x = limit;
      }

      if (u.lastPromptTokens() > 0L && u.contextWindow() > 0 && x < limit) {
         String ctx = "ctx " + AiUiKit.init_Str_J(u.lastPromptTokens()) + " / " + AiUiKit.init_Str_J(u.contextWindow());
         boolean tight = u.lastPromptTokens() > u.contextWindow() * 0.7;
         kit.init_V_Str_3(ctx, s, x, limit - x, tight ? -14249 : colors.valid);
         x += kit.init_I_Str(ctx) + 8;
      }

      long prompt = u.inputTokens() + u.cachedInputTokens() + u.cacheWriteTokens();
      if (u.cachedInputTokens() > 0L && prompt > 0L && x < limit) {
         String cache = "cache " + Math.round(100.0 * u.cachedInputTokens() / prompt) + "%";
         kit.init_V_Str_3(cache, s, x, limit - x, colors.valid);
         x += kit.init_I_Str(cache) + 8;
      }

      if (u.requests() > 0L && x < limit) {
         String req = u.requests() + " req";
         kit.init_V_Str_3(req, s, x, limit - x, colors.valid);
      }

      autismclient.gui.f.NetHelper hit = autismclient.gui.f.NetHelper.init(s.read(), s.write(), Math.max(1, limit - s.read()), s.sync());
      boolean hasTask = !this.init_cF.write_Str.isBlank();
      kit.init_V_NetHelper(hit, hasTask ? () -> this.init_Z = !this.init_Z : null, null, kit.init_Z_NetHelper(hit) ? this.init_Str_Z(hasTask) : null);
   }

   private String read_Str() {
      if (this.init_cF.init_cc_UiHelper != null) {
         return "Your turn";
      }

      if (this.init_cF.init_Z) {
         return "Awaiting approval";
      }

      String var10000;
      switch (this.init_cF.init_cc_FileCache) {
         case init:
            var10000 = "Ready";
            break;
         case load:
            var10000 = "Working";
            break;
         case read:
            var10000 = "Planning";
            break;
         case write:
            var10000 = "Paused";
            break;
         case update:
            var10000 = "Done";
            break;
         case sync:
            var10000 = "Answered";
            break;
         case flush:
            var10000 = "Blocked";
            break;
         case close:
            var10000 = "Failed";
            break;
         case open:
            var10000 = "Chatting";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private int init_I_cE(AiUiKit kit) {
      return this.init_cF.init_cc_UiHelper == null && !this.init_cF.init_Z ? init_I_cE_Str(kit, this.init_cF.init_cc_FileCache.name()) : -14249;
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
            var10000 = kit.init_JsonUtil.stop;
            break;
         case "BLOCKED":
         case "FAILED":
            var10000 = kit.init_JsonUtil.data;
            break;
         default:
            var10000 = kit.init_JsonUtil.size;
      }

      return var10000;
   }

   private String init_Str_Z(boolean hasTask) {
      AiService.Inner_ByteReader u = this.init_cF.init_cc_ByteReader;
      StringBuilder tip = new StringBuilder();
      if (this.init_cF.init_AiSettings.builtIn) {
         tip.append("Built-In: nothing is sent to an API");
      } else {
         tip.append("Input ")
            .append(AiUiKit.init_Str_J(u.inputTokens()))
            .append(", cached ")
            .append(AiUiKit.init_Str_J(u.cachedInputTokens()))
            .append(", cache writes ")
            .append(AiUiKit.init_Str_J(u.cacheWriteTokens()))
            .append("\nOutput ")
            .append(AiUiKit.init_Str_J(u.outputTokens()))
            .append(", requests ")
            .append(u.requests());
         if (u.lastPromptTokens() > 0L) {
            tip.append("\nLast prompt ").append(AiUiKit.init_Str_J(u.lastPromptTokens())).append(" of ").append(AiUiKit.init_Str_J(u.contextWindow()));
         }
      }

      if (this.init_cF.load_Z) {
         tip.append("\nChat: ").append(init_Str_Str_I(this.init_cF.close, 160));
      }

      if (hasTask) {
         tip.append("\nObjective: ").append(init_Str_Str_I(this.init_cF.write_Str, 160));
         if (!this.init_cF.update_Str.isBlank()) {
            tip.append("\nTask: ").append(init_Str_Str_I(this.init_cF.update_Str, 160));
         }

         tip.append(this.init_Z ? "\nClick to hide task details" : "\nClick for task details");
      }

      return tip.toString();
   }

   private static String init_Str_Str_I(String text, int max) {
      String flat = AiUiKit.init_Str_Str(text);
      return flat.length() <= max ? flat : flat.substring(0, max) + "...";
   }
}
