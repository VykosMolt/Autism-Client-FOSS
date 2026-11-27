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
   private final Map<AiService.TranscriptEntry, AiTranscriptLayout.Inner_StateCache> wrapCache = new IdentityHashMap<>();
   private List<AiTranscriptLayout.Inner_BufferHelper> lines = List.of();
   private List<AiTranscriptLayout.Inner_DataUtil> blocks = List.of();
   private int push;
   private int pop = -1;
   private int tick = -1;
   private int run;
   private int get = -1;
   private AiService.TranscriptEntry firstEntry;
   private AiService.TranscriptEntry lastEntry;
   private AiTranscriptLayout.Inner_TaskQueue lastMetrics;

   List<AiTranscriptLayout.Inner_BufferHelper> lines() {
      return this.lines;
   }

   List<AiTranscriptLayout.Inner_DataUtil> load() {
      return this.blocks;
   }

   int height() {
      return this.push;
   }

   boolean init(
      List<AiService.TranscriptEntry> messages,
      int width,
      int epoch,
      AiTranscriptLayout.Inner_TaskQueue metrics,
      Map<AiService.TranscriptEntry, Boolean> pinned,
      int toggleRevision,
      AiTranscriptLayout.Inner_ConfigStore measure
   ) {
      int size = messages.size();
      AiService.TranscriptEntry first = size == 0 ? null : messages.get(0);
      AiService.TranscriptEntry last = size == 0 ? null : messages.get(size - 1);
      if (size == this.pop
         && first == this.firstEntry
         && last == this.lastEntry
         && width == this.tick
         && epoch == this.run
         && toggleRevision == this.get
         && metrics.equals(this.lastMetrics)) {
         return false;
      }

      this.pop = size;
      this.firstEntry = first;
      this.lastEntry = last;
      this.tick = width;
      this.run = epoch;
      this.get = toggleRevision;
      this.lastMetrics = metrics;
      this.rebuild(messages, Math.max(20, width), epoch, metrics, pinned, measure);
      return true;
   }

   void invalidate() {
      this.pop = -1;
   }

   int firstLineAt(int y) {
      int lo = 0;
      int hi = this.lines.size();

      while (lo < hi) {
         int mid = lo + hi >>> 1;
         AiTranscriptLayout.Inner_BufferHelper line = this.lines.get(mid);
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

   AiTranscriptLayout.Inner_DataUtil blockAt(int y) {
      int i = this.firstBlockAt(y);
      if (i >= this.blocks.size()) {
         return null;
      }

      AiTranscriptLayout.Inner_DataUtil block = this.blocks.get(i);
      return y >= block.top() ? block : null;
   }

   static AiTranscriptLayout.Inner_CachePool init_cC_CachePool_Str(String role) {
      AiTranscriptLayout.Inner_CachePool var10000;
      switch (role == null ? "" : role) {
         case "user":
            var10000 = AiTranscriptLayout.Inner_CachePool.USER;
            break;
         case "tool":
            var10000 = AiTranscriptLayout.Inner_CachePool.TOOL;
            break;
         case "system":
            var10000 = AiTranscriptLayout.Inner_CachePool.SYSTEM;
            break;
         case "error":
            var10000 = AiTranscriptLayout.Inner_CachePool.ERROR;
            break;
         case "success":
            var10000 = AiTranscriptLayout.Inner_CachePool.SUCCESS;
            break;
         case "warning":
            var10000 = AiTranscriptLayout.Inner_CachePool.WARNING;
            break;
         default:
            var10000 = AiTranscriptLayout.Inner_CachePool.ASSISTANT;
      }

      return var10000;
   }

   static String init_Str_Str(String text) {
      int colon = text.indexOf(": ");
      return init(colon > 0 ? text.substring(0, colon).trim() : text.replace('\n', ' ').trim(), 80);
   }

   static String load_Str_Str(String text) {
      int colon = text.indexOf(": ");
      String summary = colon > 0 ? text.substring(colon + 2, Math.min(text.length(), colon + 2 + 400)) : "";
      return init(summary.replace('\n', ' ').trim(), 300);
   }

   private static String init(String text, int max) {
      return text.length() <= max ? text : text.substring(0, max);
   }

   static String copyText(AiService.TranscriptEntry message) {
      return "tool".equals(message.role()) && !message.detail().isBlank() ? message.text() + "\n" + message.detail() : message.text();
   }

   private void rebuild(
      List<AiService.TranscriptEntry> messages, int width, int epoch, AiTranscriptLayout.Inner_TaskQueue m, Map<AiService.TranscriptEntry, Boolean> pinned, AiTranscriptLayout.Inner_ConfigStore measure
   ) {
      List<AiTranscriptLayout.Inner_BufferHelper> nextLines = new ArrayList<>(Math.max(16, messages.size() * 2));
      List<AiTranscriptLayout.Inner_DataUtil> nextBlocks = new ArrayList<>(messages.size());
      int tools = 0;

      for (AiService.TranscriptEntry message : messages) {
         if ("tool".equals(message.role())) {
            tools++;
         }
      }

      int toolIndex = 0;
      int y = 0;
      AiTranscriptLayout.Inner_CachePool previous = null;

      for (AiService.TranscriptEntry message : messages) {
         AiTranscriptLayout.Inner_CachePool kind = init_cC_CachePool_Str(message.role());
         if (previous != null) {
            y += init(previous, kind) ? m.groupGap() : m.blockGap();
         }

         previous = kind;
         int top = y;
         boolean expanded = false;
         AiTranscriptLayout.Inner_StateCache wrap = this.wrapCache.computeIfAbsent(message, ignored -> new AiTranscriptLayout.Inner_StateCache());
         switch (kind) {
            case USER:
               y += m.userPad();

               for (String text : init(wrap, message.text(), width - m.userIndent() - m.userPad(), epoch, measure)) {
                  nextLines.add(new AiTranscriptLayout.Inner_BufferHelper(message, AiTranscriptLayout.Inner_CachePool.USER, text, "", m.userIndent(), y, m.line(), false));
                  y += m.line();
               }

               y += m.userPad();
               break;
            case ASSISTANT:
               boolean started = false;

               for (AiTranscriptLayout.Inner_NetHelper row : init_List_cC_StateCache(wrap, message.text(), width, epoch, m.line(), measure)) {
                  boolean first = (row.flags() & 1) != 0;
                  boolean last = (row.flags() & 2) != 0;
                  if (first && started) {
                     y += m.groupGap();
                  }

                  if (first && row.deco() == AiTranscriptLayout.Inner_StreamUtil.CODE) {
                     y += 3;
                  }

                  nextLines.add(
                     new AiTranscriptLayout.Inner_BufferHelper(
                        message,
                        AiTranscriptLayout.Inner_CachePool.ASSISTANT,
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
                  if (last && row.deco() == AiTranscriptLayout.Inner_StreamUtil.CODE) {
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
                  new AiTranscriptLayout.Inner_BufferHelper(
                     message, AiTranscriptLayout.Inner_CachePool.TOOL, init_Str_Str(message.text()), load_Str_Str(message.text()), 0, y, m.tool(), expanded
                  )
               );
               y += m.tool();
               if (expanded && !message.detail().isBlank()) {
                  List<String> detail = load(wrap, message.detail(), width - m.toolIndent(), epoch, measure);
                  int shown = Math.min(8, detail.size());

                  for (int i = 0; i < shown; i++) {
                     nextLines.add(new AiTranscriptLayout.Inner_BufferHelper(message, AiTranscriptLayout.Inner_CachePool.TOOL_DETAIL, detail.get(i), "", m.toolIndent(), y, m.line(), true));
                     y += m.line();
                  }

                  if (detail.size() > shown) {
                     nextLines.add(
                        new AiTranscriptLayout.Inner_BufferHelper(
                           message,
                           AiTranscriptLayout.Inner_CachePool.MORE,
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
               for (String text : init(wrap, message.text(), width, epoch, measure)) {
                  nextLines.add(new AiTranscriptLayout.Inner_BufferHelper(message, kind, text, "", 0, y, m.line(), false));
                  y += m.line();
               }
         }

         nextBlocks.add(new AiTranscriptLayout.Inner_DataUtil(message, kind, top, y, expanded));
      }

      this.lines = List.copyOf(nextLines);
      this.blocks = List.copyOf(nextBlocks);
      this.push = y;
      if (this.wrapCache.size() > messages.size() + 32) {
         Map<AiService.TranscriptEntry, Boolean> live = new IdentityHashMap<>();

         for (AiService.TranscriptEntry message : messages) {
            live.put(message, Boolean.TRUE);
         }

         this.wrapCache.keySet().removeIf(messagex -> !live.containsKey(messagex));
      }
   }

   private static boolean init(AiTranscriptLayout.Inner_CachePool previous, AiTranscriptLayout.Inner_CachePool next) {
      return previous == next || previous == AiTranscriptLayout.Inner_CachePool.TOOL && next == AiTranscriptLayout.Inner_CachePool.TOOL;
   }

   private static List<String> init(AiTranscriptLayout.Inner_StateCache wrap, String text, int width, int epoch, AiTranscriptLayout.Inner_ConfigStore measure) {
      if (wrap.width != width || wrap.epoch != epoch) {
         wrap.init_List = init(text, width, measure);
         wrap.width = width;
         wrap.epoch = epoch;
      }

      return wrap.init_List;
   }

   private static List<AiTranscriptLayout.Inner_NetHelper> init_List_cC_StateCache(
      AiTranscriptLayout.Inner_StateCache wrap, String text, int width, int epoch, int line, AiTranscriptLayout.Inner_ConfigStore measure
   ) {
      if (wrap.update != width || wrap.sync != epoch || wrap.flush != line) {
         wrap.read_List = init(text, width, line, measure);
         wrap.update = width;
         wrap.sync = epoch;
         wrap.flush = line;
      }

      return wrap.read_List;
   }

   static List<AiTranscriptLayout.Inner_NetHelper> init(String text, int width, int line, AiTranscriptLayout.Inner_ConfigStore measure) {
      List<AiTranscriptLayout.Inner_NetHelper> out = new ArrayList<>();

      for (AiMarkdown.Inner_DataUtil block : AiMarkdown.parse(text == null ? "" : text)) {
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
         if (block.type() == AiMarkdown.Inner_StreamUtil.RULE) {
            out.add(new AiTranscriptLayout.Inner_NetHelper("", List.of(), AiTranscriptLayout.Inner_StreamUtil.RULE, "", 0, 3, Math.max(4, line / 2)));
         } else {
            AiTranscriptLayout.Inner_StreamUtil var16;
            switch (block.type()) {
               case BULLET:
                  var16 = AiTranscriptLayout.Inner_StreamUtil.BULLET;
                  break;
               case NUMBERED:
                  var16 = AiTranscriptLayout.Inner_StreamUtil.NUMBERED;
                  break;
               case QUOTE:
                  var16 = AiTranscriptLayout.Inner_StreamUtil.QUOTE;
                  break;
               case CODE:
                  var16 = AiTranscriptLayout.Inner_StreamUtil.CODE;
                  break;
               case HEADING:
                  var16 = AiTranscriptLayout.Inner_StreamUtil.HEADING;
                  break;
               default:
                  var16 = AiTranscriptLayout.Inner_StreamUtil.NONE;
            }

            AiTranscriptLayout.Inner_StreamUtil deco = var16;
            int height = block.type() == AiMarkdown.Inner_StreamUtil.HEADING ? line + (block.level() <= 2 ? 3 : 1) : line;
            String safe = block.text().replace('\t', ' ');
            byte[] styles = block.styles();
            int first = out.size();

            for (autismclient.gui.ui.TextWrapLayout.Inner_DataUtil w : autismclient.gui.ui.TextWrapLayout.init(
               safe, Math.max(1, width - indent), (s, e) -> init_I(safe, styles, s, e, measure)
            )) {
               String rowText = safe.substring(w.init(), w.read());
               out.add(
                  new AiTranscriptLayout.Inner_NetHelper(
                     rowText, init_List(safe, styles, w.init(), w.read(), measure), deco, out.size() == first ? block.marker() : "", indent, 0, height
                  )
               );
            }

            while (out.size() > first + 1 && out.get(out.size() - 1).text().isBlank() && deco != AiTranscriptLayout.Inner_StreamUtil.CODE) {
               out.remove(out.size() - 1);
            }

            if (out.size() > first) {
               out.set(first, init(out.get(first), 1));
               out.set(out.size() - 1, init(out.get(out.size() - 1), 2));
            }
         }
      }

      if (out.isEmpty()) {
         out.add(new AiTranscriptLayout.Inner_NetHelper("", List.of(), AiTranscriptLayout.Inner_StreamUtil.NONE, "", 0, 3, line));
      }

      return out;
   }

   private static AiTranscriptLayout.Inner_NetHelper init(AiTranscriptLayout.Inner_NetHelper row, int flag) {
      return new AiTranscriptLayout.Inner_NetHelper(row.text(), row.spans(), row.deco(), row.marker(), row.indent(), row.flags() | flag, row.height());
   }

   static int init_I(String text, byte[] styles, int start, int end, AiTranscriptLayout.Inner_ConfigStore measure) {
      int total = 0;

      for (int[] run : init(styles, start, end)) {
         total += measure.init(text, run[0], run[1], run[2]);
      }

      return total;
   }

   static List<AiTranscriptLayout.Inner_JsonUtil> init_List(String text, byte[] styles, int start, int end, AiTranscriptLayout.Inner_ConfigStore measure) {
      List<AiTranscriptLayout.Inner_JsonUtil> out = new ArrayList<>();

      for (int[] run : init(styles, start, end)) {
         out.add(new AiTranscriptLayout.Inner_JsonUtil(text.substring(run[0], run[1]), run[2], measure.init(text, run[0], run[1], run[2])));
      }

      return List.copyOf(out);
   }

   private static List<int[]> init(byte[] styles, int start, int end) {
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

   private static List<String> load(AiTranscriptLayout.Inner_StateCache wrap, String text, int width, int epoch, AiTranscriptLayout.Inner_ConfigStore measure) {
      if (wrap.read_I != width || wrap.write != epoch) {
         wrap.load_List = init(text, width, measure);
         wrap.read_I = width;
         wrap.write = epoch;
      }

      return wrap.load_List;
   }

   static List<String> init(String text, int width, AiTranscriptLayout.Inner_ConfigStore measure) {
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

   record Inner_BufferHelper(
      AiService.TranscriptEntry message,
      AiTranscriptLayout.Inner_CachePool kind,
      String text,
      String secondary,
      int x,
      int y,
      int height,
      boolean expanded,
      List<AiTranscriptLayout.Inner_JsonUtil> spans,
      AiTranscriptLayout.Inner_StreamUtil deco,
      String marker,
      int flags
   ) {
      Inner_BufferHelper(AiService.TranscriptEntry message, AiTranscriptLayout.Inner_CachePool kind, String text, String secondary, int x, int y, int height, boolean expanded) {
         this(message, kind, text, secondary, x, y, height, expanded, null, AiTranscriptLayout.Inner_StreamUtil.NONE, "", 0);
      }
   }

   enum Inner_CachePool {
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

   interface Inner_ConfigStore {
      int init(String var1, int var2, int var3);

      default int init(String text, int start, int end, int style) {
         return this.init(text, start, end);
      }
   }

   record Inner_DataUtil(AiService.TranscriptEntry message, AiTranscriptLayout.Inner_CachePool kind, int top, int bottom, boolean expanded) {
   }

   record Inner_JsonUtil(String text, int style, int width) {
   }

   record Inner_NetHelper(String text, List<AiTranscriptLayout.Inner_JsonUtil> spans, AiTranscriptLayout.Inner_StreamUtil deco, String marker, int indent, int flags, int height) {
   }

   private static final class Inner_StateCache {
      int width = -1;
      int epoch;
      List<String> init_List = List.of();
      int read_I = -1;
      int write;
      List<String> load_List = List.of();
      int update = -1;
      int sync;
      int flush;
      List<AiTranscriptLayout.Inner_NetHelper> read_List = List.of();
   }

   enum Inner_StreamUtil {
      NONE,
      HEADING,
      BULLET,
      NUMBERED,
      CODE,
      QUOTE,
      RULE;
   }

   record Inner_TaskQueue(int line, int tool, int userIndent, int toolIndent, int userPad, int groupGap, int blockGap) {
   }
}
