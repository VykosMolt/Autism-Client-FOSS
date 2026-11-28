package autismclient.ai;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

final class AiTranscriptLayout {
   static final int init_I = 1;
   static final int load_I = 2;
   static final int read = 9;
   static final int write = 16;
   static final int update = 8;
   static final int sync = 5;
   static final int flush = 10;
   static final int close = 3;
   static final int open = 8;
   static final int poll = 3;
   private final Map<AiService.TranscriptEntry, AiTranscriptLayout.WrapCache> wrapCache = new IdentityHashMap<>();
   private List<AiTranscriptLayout.LayoutLine> lines = List.of();
   private List<AiTranscriptLayout.MessageBlock> blocks = List.of();
   private int contentHeight;
   private int cachedSize = -1;
   private int cachedWidth = -1;
   private int cachedEpoch;
   private int cachedToggleRevision = -1;
   private AiService.TranscriptEntry firstEntry;
   private AiService.TranscriptEntry lastEntry;
   private AiTranscriptLayout.LayoutMetrics lastMetrics;

   List<AiTranscriptLayout.LayoutLine> lines() {
      return this.lines;
   }

   List<AiTranscriptLayout.MessageBlock> load() {
      return this.blocks;
   }

   int height() {
      return this.contentHeight;
   }

   boolean init(
      List<AiService.TranscriptEntry> messages,
      int width,
      int epoch,
      AiTranscriptLayout.LayoutMetrics metrics,
      Map<AiService.TranscriptEntry, Boolean> pinned,
      int toggleRevision,
      AiTranscriptLayout.TextMeasurer measure
   ) {
      int size = messages.size();
      AiService.TranscriptEntry first = size == 0 ? null : messages.get(0);
      AiService.TranscriptEntry last = size == 0 ? null : messages.get(size - 1);
      if (size == this.cachedSize
         && first == this.firstEntry
         && last == this.lastEntry
         && width == this.cachedWidth
         && epoch == this.cachedEpoch
         && toggleRevision == this.cachedToggleRevision
         && metrics.equals(this.lastMetrics)) {
         return false;
      }

      this.cachedSize = size;
      this.firstEntry = first;
      this.lastEntry = last;
      this.cachedWidth = width;
      this.cachedEpoch = epoch;
      this.cachedToggleRevision = toggleRevision;
      this.lastMetrics = metrics;
      this.rebuild(messages, Math.max(20, width), epoch, metrics, pinned, measure);
      return true;
   }

   void invalidate() {
      this.cachedSize = -1;
   }

   int firstLineAt(int y) {
      int lo = 0;
      int hi = this.lines.size();

      while (lo < hi) {
         int mid = lo + hi >>> 1;
         AiTranscriptLayout.LayoutLine line = this.lines.get(mid);
         if (line.y() + line.height() <= y) {
            lo = mid + 1;
         } else {
            hi = mid;
         }
      }

      return lo;
   }

   int firstBlockAt(int y) {
      int lo = 0;
      int hi = this.blocks.size();

      while (lo < hi) {
         int mid = lo + hi >>> 1;
         if (this.blocks.get(mid).bottom() <= y) {
            lo = mid + 1;
         } else {
            hi = mid;
         }
      }

      return lo;
   }

   AiTranscriptLayout.MessageBlock blockAt(int y) {
      int i = this.firstBlockAt(y);
      if (i >= this.blocks.size()) {
         return null;
      }

      AiTranscriptLayout.MessageBlock block = this.blocks.get(i);
      return y >= block.top() ? block : null;
   }

   static AiTranscriptLayout.LineKind init_cC_CachePool_Str(String role) {
      AiTranscriptLayout.LineKind var10000;
      switch (role == null ? "" : role) {
         case "user":
            var10000 = AiTranscriptLayout.LineKind.USER;
            break;
         case "tool":
            var10000 = AiTranscriptLayout.LineKind.TOOL;
            break;
         case "system":
            var10000 = AiTranscriptLayout.LineKind.SYSTEM;
            break;
         case "error":
            var10000 = AiTranscriptLayout.LineKind.ERROR;
            break;
         case "success":
            var10000 = AiTranscriptLayout.LineKind.SUCCESS;
            break;
         case "warning":
            var10000 = AiTranscriptLayout.LineKind.WARNING;
            break;
         default:
            var10000 = AiTranscriptLayout.LineKind.ASSISTANT;
      }

      return var10000;
   }

   static String init_Str_Str(String text) {
      int colon = text.indexOf(": ");
      return truncate(colon > 0 ? text.substring(0, colon).trim() : text.replace('\n', ' ').trim(), 80);
   }

   static String load_Str_Str(String text) {
      int colon = text.indexOf(": ");
      String summary = colon > 0 ? text.substring(colon + 2, Math.min(text.length(), colon + 2 + 400)) : "";
      return truncate(summary.replace('\n', ' ').trim(), 300);
   }

   private static String truncate(String text, int max) {
      return text.length() <= max ? text : text.substring(0, max);
   }

   static String copyText(AiService.TranscriptEntry message) {
      return "tool".equals(message.role()) && !message.detail().isBlank() ? message.text() + "\n" + message.detail() : message.text();
   }

   private void rebuild(
      List<AiService.TranscriptEntry> messages, int width, int epoch, AiTranscriptLayout.LayoutMetrics m, Map<AiService.TranscriptEntry, Boolean> pinned, AiTranscriptLayout.TextMeasurer measure
   ) {
      List<AiTranscriptLayout.LayoutLine> nextLines = new ArrayList<>(Math.max(16, messages.size() * 2));
      List<AiTranscriptLayout.MessageBlock> nextBlocks = new ArrayList<>(messages.size());
      int tools = 0;

      for (AiService.TranscriptEntry message : messages) {
         if ("tool".equals(message.role())) {
            tools++;
         }
      }

      int toolIndex = 0;
      int y = 0;
      AiTranscriptLayout.LineKind previous = null;

      for (AiService.TranscriptEntry message : messages) {
         AiTranscriptLayout.LineKind kind = init_cC_CachePool_Str(message.role());
         if (previous != null) {
            y += sameGroup(previous, kind) ? m.groupGap() : m.blockGap();
         }

         previous = kind;
         int top = y;
         boolean expanded = false;
         AiTranscriptLayout.WrapCache wrap = this.wrapCache.computeIfAbsent(message, ignored -> new AiTranscriptLayout.WrapCache());
         switch (kind) {
            case USER:
               y += m.userPad();

               for (String text : wrapCached(wrap, message.text(), width - m.userIndent() - m.userPad(), epoch, measure)) {
                  nextLines.add(new AiTranscriptLayout.LayoutLine(message, AiTranscriptLayout.LineKind.USER, text, "", m.userIndent(), y, m.line(), false));
                  y += m.line();
               }

               y += m.userPad();
               break;
            case ASSISTANT:
               boolean started = false;

               for (AiTranscriptLayout.WrappedRow row : init_List_cC_StateCache(wrap, message.text(), width, epoch, m.line(), measure)) {
                  boolean first = (row.flags() & 1) != 0;
                  boolean last = (row.flags() & 2) != 0;
                  if (first && started) {
                     y += m.groupGap();
                  }

                  if (first && row.deco() == AiTranscriptLayout.RowDecoration.CODE) {
                     y += 3;
                  }

                  nextLines.add(
                     new AiTranscriptLayout.LayoutLine(
                        message,
                        AiTranscriptLayout.LineKind.ASSISTANT,
                        row.text(),
                        "",
                        row.indent(),
                        y,
                        row.height(),
                        false,
                        row.spans(),
                        row.deco(),
                        row.marker(),
                        row.flags()
                     )
                  );
                  y += row.height();
                  if (last && row.deco() == AiTranscriptLayout.RowDecoration.CODE) {
                     y += 3;
                  }

                  started = true;
               }
               break;
            case TOOL:
               toolIndex++;
               Boolean pin = pinned.get(message);
               expanded = pin != null ? pin : tools - toolIndex < 3;
               nextLines.add(
                  new AiTranscriptLayout.LayoutLine(
                     message, AiTranscriptLayout.LineKind.TOOL, init_Str_Str(message.text()), load_Str_Str(message.text()), 0, y, m.tool(), expanded
                  )
               );
               y += m.tool();
               if (expanded && !message.detail().isBlank()) {
                  List<String> detail = wrapExpandedCached(wrap, message.detail(), width - m.toolIndent(), epoch, measure);
                  int shown = Math.min(8, detail.size());

                  for (int i = 0; i < shown; i++) {
                     nextLines.add(new AiTranscriptLayout.LayoutLine(message, AiTranscriptLayout.LineKind.TOOL_DETAIL, detail.get(i), "", m.toolIndent(), y, m.line(), true));
                     y += m.line();
                  }

                  if (detail.size() > shown) {
                     nextLines.add(
                        new AiTranscriptLayout.LayoutLine(
                           message,
                           AiTranscriptLayout.LineKind.MORE,
                           "+" + (detail.size() - shown) + " more lines, right-click to copy all",
                           "",
                           m.toolIndent(),
                           y,
                           m.line(),
                           true
                        )
                     );
                     y += m.line();
                  }
               }
               break;
            default:
               for (String text : wrapCached(wrap, message.text(), width, epoch, measure)) {
                  nextLines.add(new AiTranscriptLayout.LayoutLine(message, kind, text, "", 0, y, m.line(), false));
                  y += m.line();
               }
         }

         nextBlocks.add(new AiTranscriptLayout.MessageBlock(message, kind, top, y, expanded));
      }

      this.lines = List.copyOf(nextLines);
      this.blocks = List.copyOf(nextBlocks);
      this.contentHeight = y;
      if (this.wrapCache.size() > messages.size() + 32) {
         Map<AiService.TranscriptEntry, Boolean> live = new IdentityHashMap<>();

         for (AiService.TranscriptEntry message : messages) {
            live.put(message, Boolean.TRUE);
         }

         this.wrapCache.keySet().removeIf(messagex -> !live.containsKey(messagex));
      }
   }

   private static boolean sameGroup(AiTranscriptLayout.LineKind previous, AiTranscriptLayout.LineKind next) {
      return previous == next || previous == AiTranscriptLayout.LineKind.TOOL && next == AiTranscriptLayout.LineKind.TOOL;
   }

   private static List<String> wrapCached(AiTranscriptLayout.WrapCache wrap, String text, int width, int epoch, AiTranscriptLayout.TextMeasurer measure) {
      if (wrap.width != width || wrap.epoch != epoch) {
         wrap.init_List = wrapPlain(text, width, measure);
         wrap.width = width;
         wrap.epoch = epoch;
      }

      return wrap.init_List;
   }

   private static List<AiTranscriptLayout.WrappedRow> init_List_cC_StateCache(
      AiTranscriptLayout.WrapCache wrap, String text, int width, int epoch, int line, AiTranscriptLayout.TextMeasurer measure
   ) {
      if (wrap.rowsWidth != width || wrap.rowsEpoch != epoch || wrap.rowsLineHeight != line) {
         wrap.read_List = layoutRows(text, width, line, measure);
         wrap.rowsWidth = width;
         wrap.rowsEpoch = epoch;
         wrap.rowsLineHeight = line;
      }

      return wrap.read_List;
   }

   static List<AiTranscriptLayout.WrappedRow> layoutRows(String text, int width, int line, AiTranscriptLayout.TextMeasurer measure) {
      List<AiTranscriptLayout.WrappedRow> out = new ArrayList<>();

      for (AiMarkdown.MarkdownBlock block : AiMarkdown.parse(text == null ? "" : text)) {
         int var10000;
         switch (block.type()) {
            case BULLET:
               var10000 = 9 + block.indent() * 10;
               break;
            case NUMBERED:
               var10000 = 16 + block.indent() * 10;
               break;
            case QUOTE:
               var10000 = 8;
               break;
            case CODE:
               var10000 = 5;
               break;
            default:
               var10000 = 0;
         }

         int indent = var10000;
         if (block.type() == AiMarkdown.BlockType.RULE) {
            out.add(new AiTranscriptLayout.WrappedRow("", List.of(), AiTranscriptLayout.RowDecoration.RULE, "", 0, 3, Math.max(4, line / 2)));
         } else {
            AiTranscriptLayout.RowDecoration var16;
            switch (block.type()) {
               case BULLET:
                  var16 = AiTranscriptLayout.RowDecoration.BULLET;
                  break;
               case NUMBERED:
                  var16 = AiTranscriptLayout.RowDecoration.NUMBERED;
                  break;
               case QUOTE:
                  var16 = AiTranscriptLayout.RowDecoration.QUOTE;
                  break;
               case CODE:
                  var16 = AiTranscriptLayout.RowDecoration.CODE;
                  break;
               case HEADING:
                  var16 = AiTranscriptLayout.RowDecoration.HEADING;
                  break;
               default:
                  var16 = AiTranscriptLayout.RowDecoration.NONE;
            }

            AiTranscriptLayout.RowDecoration deco = var16;
            int height = block.type() == AiMarkdown.BlockType.HEADING ? line + (block.level() <= 2 ? 3 : 1) : line;
            String safe = block.text().replace('\t', ' ');
            byte[] styles = block.styles();
            int first = out.size();

            for (autismclient.gui.ui.TextWrapLayout.Inner_DataUtil w : autismclient.gui.ui.TextWrapLayout.init(
               safe, Math.max(1, width - indent), (s, e) -> init_I(safe, styles, s, e, measure)
            )) {
               String rowText = safe.substring(w.init(), w.read());
               out.add(
                  new AiTranscriptLayout.WrappedRow(
                     rowText, init_List(safe, styles, w.init(), w.read(), measure), deco, out.size() == first ? block.marker() : "", indent, 0, height
                  )
               );
            }

            while (out.size() > first + 1 && out.get(out.size() - 1).text().isBlank() && deco != AiTranscriptLayout.RowDecoration.CODE) {
               out.remove(out.size() - 1);
            }

            if (out.size() > first) {
               out.set(first, withFlag(out.get(first), 1));
               out.set(out.size() - 1, withFlag(out.get(out.size() - 1), 2));
            }
         }
      }

      if (out.isEmpty()) {
         out.add(new AiTranscriptLayout.WrappedRow("", List.of(), AiTranscriptLayout.RowDecoration.NONE, "", 0, 3, line));
      }

      return out;
   }

   private static AiTranscriptLayout.WrappedRow withFlag(AiTranscriptLayout.WrappedRow row, int flag) {
      return new AiTranscriptLayout.WrappedRow(row.text(), row.spans(), row.deco(), row.marker(), row.indent(), row.flags() | flag, row.height());
   }

   static int init_I(String text, byte[] styles, int start, int end, AiTranscriptLayout.TextMeasurer measure) {
      int total = 0;

      for (int[] run : styleRuns(styles, start, end)) {
         total += measure.init(text, run[0], run[1], run[2]);
      }

      return total;
   }

   static List<AiTranscriptLayout.StyledSpan> init_List(String text, byte[] styles, int start, int end, AiTranscriptLayout.TextMeasurer measure) {
      List<AiTranscriptLayout.StyledSpan> out = new ArrayList<>();

      for (int[] run : styleRuns(styles, start, end)) {
         out.add(new AiTranscriptLayout.StyledSpan(text.substring(run[0], run[1]), run[2], measure.init(text, run[0], run[1], run[2])));
      }

      return List.copyOf(out);
   }

   private static List<int[]> styleRuns(byte[] styles, int start, int end) {
      List<int[]> out = new ArrayList<>();
      int from = start;

      for (int i = start + 1; i <= end; i++) {
         if (i == end || styles[i] != styles[from]) {
            out.add(new int[]{from, i, styles[from]});
            from = i;
         }
      }

      return out;
   }

   private static List<String> wrapExpandedCached(AiTranscriptLayout.WrapCache wrap, String text, int width, int epoch, AiTranscriptLayout.TextMeasurer measure) {
      if (wrap.read_I != width || wrap.plainEpoch != epoch) {
         wrap.load_List = wrapPlain(text, width, measure);
         wrap.read_I = width;
         wrap.plainEpoch = epoch;
      }

      return wrap.load_List;
   }

   static List<String> wrapPlain(String text, int width, AiTranscriptLayout.TextMeasurer measure) {
      String safe = text == null ? "" : text.replace("\r", "").replace('\t', ' ');
      List<String> out = new ArrayList<>();

      for (autismclient.gui.ui.TextWrapLayout.Inner_DataUtil line : autismclient.gui.ui.TextWrapLayout.init(
         safe, Math.max(1, width), (s, e) -> measure.init(safe, s, e)
      )) {
         out.add(safe.substring(line.init(), line.read()));
      }

      while (out.size() > 1 && out.get(out.size() - 1).isBlank()) {
         out.remove(out.size() - 1);
      }

      if (out.isEmpty()) {
         out.add("");
      }

      return List.copyOf(out);
   }

   record LayoutLine(
      AiService.TranscriptEntry message,
      AiTranscriptLayout.LineKind kind,
      String text,
      String secondary,
      int x,
      int y,
      int height,
      boolean expanded,
      List<AiTranscriptLayout.StyledSpan> spans,
      AiTranscriptLayout.RowDecoration deco,
      String marker,
      int flags
   ) {
      LayoutLine(AiService.TranscriptEntry message, AiTranscriptLayout.LineKind kind, String text, String secondary, int x, int y, int height, boolean expanded) {
         this(message, kind, text, secondary, x, y, height, expanded, null, AiTranscriptLayout.RowDecoration.NONE, "", 0);
      }
   }

   enum LineKind {
      USER,
      ASSISTANT,
      SYSTEM,
      ERROR,
      SUCCESS,
      WARNING,
      TOOL,
      TOOL_DETAIL,
      MORE;
   }

   interface TextMeasurer {
      int init(String var1, int var2, int var3);

      default int init(String text, int start, int end, int style) {
         return this.init(text, start, end);
      }
   }

   record MessageBlock(AiService.TranscriptEntry message, AiTranscriptLayout.LineKind kind, int top, int bottom, boolean expanded) {
   }

   record StyledSpan(String text, int style, int width) {
   }

   record WrappedRow(String text, List<AiTranscriptLayout.StyledSpan> spans, AiTranscriptLayout.RowDecoration deco, String marker, int indent, int flags, int height) {
   }

   private static final class WrapCache {
      int width = -1;
      int epoch;
      List<String> init_List = List.of();
      int read_I = -1;
      int plainEpoch;
      List<String> load_List = List.of();
      int rowsWidth = -1;
      int rowsEpoch;
      int rowsLineHeight;
      List<AiTranscriptLayout.WrappedRow> read_List = List.of();
   }

   enum RowDecoration {
      NONE,
      HEADING,
      BULLET,
      NUMBERED,
      CODE,
      QUOTE,
      RULE;
   }

   record LayoutMetrics(int line, int tool, int userIndent, int toolIndent, int userPad, int groupGap, int blockGap) {
   }
}
