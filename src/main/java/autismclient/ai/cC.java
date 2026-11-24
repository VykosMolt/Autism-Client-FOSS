package autismclient.ai;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

final class cC {
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
   private final Map<cc.Inner_JsonUtil, cC.Inner_StateCache> init_Map = new IdentityHashMap<>();
   private List<cC.Inner_BufferHelper> init_List = List.of();
   private List<cC.Inner_DataUtil> load_List = List.of();
   private int push;
   private int pop = -1;
   private int tick = -1;
   private int run;
   private int get = -1;
   private cc.Inner_JsonUtil init_cc_JsonUtil;
   private cc.Inner_JsonUtil load_cc_JsonUtil;
   private cC.Inner_TaskQueue init_cC_TaskQueue;

   List<cC.Inner_BufferHelper> init_List() {
      return this.init_List;
   }

   List<cC.Inner_DataUtil> load() {
      return this.load_List;
   }

   int init_I() {
      return this.push;
   }

   boolean init(
      List<cc.Inner_JsonUtil> messages,
      int width,
      int epoch,
      cC.Inner_TaskQueue metrics,
      Map<cc.Inner_JsonUtil, Boolean> pinned,
      int toggleRevision,
      cC.Inner_ConfigStore measure
   ) {
      int size = messages.size();
      cc.Inner_JsonUtil first = size == 0 ? null : messages.get(0);
      cc.Inner_JsonUtil last = size == 0 ? null : messages.get(size - 1);
      if (size == this.pop
         && first == this.init_cc_JsonUtil
         && last == this.load_cc_JsonUtil
         && width == this.tick
         && epoch == this.run
         && toggleRevision == this.get
         && metrics.equals(this.init_cC_TaskQueue)) {
         return false;
      }

      this.pop = size;
      this.init_cc_JsonUtil = first;
      this.load_cc_JsonUtil = last;
      this.tick = width;
      this.run = epoch;
      this.get = toggleRevision;
      this.init_cC_TaskQueue = metrics;
      this.init_V_List(messages, Math.max(20, width), epoch, metrics, pinned, measure);
      return true;
   }

   void init_V() {
      this.pop = -1;
   }

   int init_I_I(int y) {
      int lo = 0;
      int hi = this.init_List.size();

      while (lo < hi) {
         int mid = lo + hi >>> 1;
         cC.Inner_BufferHelper line = this.init_List.get(mid);
         if (line.y() + line.height() <= y) {
            lo = mid + 1;
         } else {
            hi = mid;
         }
      }

      return lo;
   }

   int load_I_I(int y) {
      int lo = 0;
      int hi = this.load_List.size();

      while (lo < hi) {
         int mid = lo + hi >>> 1;
         if (this.load_List.get(mid).bottom() <= y) {
            lo = mid + 1;
         } else {
            hi = mid;
         }
      }

      return lo;
   }

   cC.Inner_DataUtil init_cC_DataUtil_I(int y) {
      int i = this.load_I_I(y);
      if (i >= this.load_List.size()) {
         return null;
      }

      cC.Inner_DataUtil block = this.load_List.get(i);
      return y >= block.top() ? block : null;
   }

   static cC.Inner_CachePool init_cC_CachePool_Str(String role) {
      cC.Inner_CachePool var10000;
      switch (role == null ? "" : role) {
         case "user":
            var10000 = cC.Inner_CachePool.init;
            break;
         case "tool":
            var10000 = cC.Inner_CachePool.flush;
            break;
         case "system":
            var10000 = cC.Inner_CachePool.read;
            break;
         case "error":
            var10000 = cC.Inner_CachePool.write;
            break;
         case "success":
            var10000 = cC.Inner_CachePool.update;
            break;
         case "warning":
            var10000 = cC.Inner_CachePool.sync;
            break;
         default:
            var10000 = cC.Inner_CachePool.load;
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

   static String init_Str_cc_JsonUtil(cc.Inner_JsonUtil message) {
      return "tool".equals(message.role()) && !message.detail().isBlank() ? message.text() + "\n" + message.detail() : message.text();
   }

   private void init_V_List(
      List<cc.Inner_JsonUtil> messages, int width, int epoch, cC.Inner_TaskQueue m, Map<cc.Inner_JsonUtil, Boolean> pinned, cC.Inner_ConfigStore measure
   ) {
      List<cC.Inner_BufferHelper> nextLines = new ArrayList<>(Math.max(16, messages.size() * 2));
      List<cC.Inner_DataUtil> nextBlocks = new ArrayList<>(messages.size());
      int tools = 0;

      for (cc.Inner_JsonUtil message : messages) {
         if ("tool".equals(message.role())) {
            tools++;
         }
      }

      int toolIndex = 0;
      int y = 0;
      cC.Inner_CachePool previous = null;

      for (cc.Inner_JsonUtil message : messages) {
         cC.Inner_CachePool kind = init_cC_CachePool_Str(message.role());
         if (previous != null) {
            y += init(previous, kind) ? m.groupGap() : m.blockGap();
         }

         previous = kind;
         int top = y;
         boolean expanded = false;
         cC.Inner_StateCache wrap = this.init_Map.computeIfAbsent(message, ignored -> new cC.Inner_StateCache());
         switch (kind) {
            case init:
               y += m.userPad();

               for (String text : init(wrap, message.text(), width - m.userIndent() - m.userPad(), epoch, measure)) {
                  nextLines.add(new cC.Inner_BufferHelper(message, cC.Inner_CachePool.init, text, "", m.userIndent(), y, m.line(), false));
                  y += m.line();
               }

               y += m.userPad();
               break;
            case load:
               boolean started = false;

               for (cC.Inner_NetHelper row : init_List_cC_StateCache(wrap, message.text(), width, epoch, m.line(), measure)) {
                  boolean first = (row.flags() & 1) != 0;
                  boolean last = (row.flags() & 2) != 0;
                  if (first && started) {
                     y += m.groupGap();
                  }

                  if (first && row.deco() == cC.Inner_StreamUtil.update) {
                     y += 3;
                  }

                  nextLines.add(
                     new cC.Inner_BufferHelper(
                        message,
                        cC.Inner_CachePool.load,
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
                  if (last && row.deco() == cC.Inner_StreamUtil.update) {
                     y += 3;
                  }

                  started = true;
               }
               break;
            case flush:
               toolIndex++;
               Boolean pin = pinned.get(message);
               expanded = pin != null ? pin : tools - toolIndex < 3;
               nextLines.add(
                  new cC.Inner_BufferHelper(
                     message, cC.Inner_CachePool.flush, init_Str_Str(message.text()), load_Str_Str(message.text()), 0, y, m.tool(), expanded
                  )
               );
               y += m.tool();
               if (expanded && !message.detail().isBlank()) {
                  List<String> detail = load(wrap, message.detail(), width - m.toolIndent(), epoch, measure);
                  int shown = Math.min(8, detail.size());

                  for (int i = 0; i < shown; i++) {
                     nextLines.add(new cC.Inner_BufferHelper(message, cC.Inner_CachePool.close, detail.get(i), "", m.toolIndent(), y, m.line(), true));
                     y += m.line();
                  }

                  if (detail.size() > shown) {
                     nextLines.add(
                        new cC.Inner_BufferHelper(
                           message,
                           cC.Inner_CachePool.open,
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
                  nextLines.add(new cC.Inner_BufferHelper(message, kind, text, "", 0, y, m.line(), false));
                  y += m.line();
               }
         }

         nextBlocks.add(new cC.Inner_DataUtil(message, kind, top, y, expanded));
      }

      this.init_List = List.copyOf(nextLines);
      this.load_List = List.copyOf(nextBlocks);
      this.push = y;
      if (this.init_Map.size() > messages.size() + 32) {
         Map<cc.Inner_JsonUtil, Boolean> live = new IdentityHashMap<>();

         for (cc.Inner_JsonUtil message : messages) {
            live.put(message, Boolean.TRUE);
         }

         this.init_Map.keySet().removeIf(messagex -> !live.containsKey(messagex));
      }
   }

   private static boolean init(cC.Inner_CachePool previous, cC.Inner_CachePool next) {
      return previous == next || previous == cC.Inner_CachePool.flush && next == cC.Inner_CachePool.flush;
   }

   private static List<String> init(cC.Inner_StateCache wrap, String text, int width, int epoch, cC.Inner_ConfigStore measure) {
      if (wrap.init_I != width || wrap.load_I != epoch) {
         wrap.init_List = init(text, width, measure);
         wrap.init_I = width;
         wrap.load_I = epoch;
      }

      return wrap.init_List;
   }

   private static List<cC.Inner_NetHelper> init_List_cC_StateCache(
      cC.Inner_StateCache wrap, String text, int width, int epoch, int line, cC.Inner_ConfigStore measure
   ) {
      if (wrap.update != width || wrap.sync != epoch || wrap.flush != line) {
         wrap.read_List = init(text, width, line, measure);
         wrap.update = width;
         wrap.sync = epoch;
         wrap.flush = line;
      }

      return wrap.read_List;
   }

   static List<cC.Inner_NetHelper> init(String text, int width, int line, cC.Inner_ConfigStore measure) {
      List<cC.Inner_NetHelper> out = new ArrayList<>();

      for (ao.Inner_DataUtil block : ao.init_List(text == null ? "" : text)) {
         int var10000;
         switch (block.type()) {
            case read:
               var10000 = 9 + block.indent() * 10;
               break;
            case write:
               var10000 = 16 + block.indent() * 10;
               break;
            case sync:
               var10000 = 8;
               break;
            case update:
               var10000 = 5;
               break;
            default:
               var10000 = 0;
         }

         int indent = var10000;
         if (block.type() == ao.Inner_StreamUtil.flush) {
            out.add(new cC.Inner_NetHelper("", List.of(), cC.Inner_StreamUtil.flush, "", 0, 3, Math.max(4, line / 2)));
         } else {
            cC.Inner_StreamUtil var16;
            switch (block.type()) {
               case read:
                  var16 = cC.Inner_StreamUtil.read;
                  break;
               case write:
                  var16 = cC.Inner_StreamUtil.write;
                  break;
               case sync:
                  var16 = cC.Inner_StreamUtil.sync;
                  break;
               case update:
                  var16 = cC.Inner_StreamUtil.update;
                  break;
               case load:
                  var16 = cC.Inner_StreamUtil.load;
                  break;
               default:
                  var16 = cC.Inner_StreamUtil.init;
            }

            cC.Inner_StreamUtil deco = var16;
            int height = block.type() == ao.Inner_StreamUtil.load ? line + (block.level() <= 2 ? 3 : 1) : line;
            String safe = block.text().replace('\t', ' ');
            byte[] styles = block.styles();
            int first = out.size();

            for (autismclient.gui.f.ConfigStore.Inner_DataUtil w : autismclient.gui.f.ConfigStore.init(
               safe, Math.max(1, width - indent), (s, e) -> init_I(safe, styles, s, e, measure)
            )) {
               String rowText = safe.substring(w.init(), w.read());
               out.add(
                  new cC.Inner_NetHelper(
                     rowText, init_List(safe, styles, w.init(), w.read(), measure), deco, out.size() == first ? block.marker() : "", indent, 0, height
                  )
               );
            }

            while (out.size() > first + 1 && out.get(out.size() - 1).text().isBlank() && deco != cC.Inner_StreamUtil.update) {
               out.remove(out.size() - 1);
            }

            if (out.size() > first) {
               out.set(first, init(out.get(first), 1));
               out.set(out.size() - 1, init(out.get(out.size() - 1), 2));
            }
         }
      }

      if (out.isEmpty()) {
         out.add(new cC.Inner_NetHelper("", List.of(), cC.Inner_StreamUtil.init, "", 0, 3, line));
      }

      return out;
   }

   private static cC.Inner_NetHelper init(cC.Inner_NetHelper row, int flag) {
      return new cC.Inner_NetHelper(row.text(), row.spans(), row.deco(), row.marker(), row.indent(), row.flags() | flag, row.height());
   }

   static int init_I(String text, byte[] styles, int start, int end, cC.Inner_ConfigStore measure) {
      int total = 0;

      for (int[] run : init(styles, start, end)) {
         total += measure.init(text, run[0], run[1], run[2]);
      }

      return total;
   }

   static List<cC.Inner_JsonUtil> init_List(String text, byte[] styles, int start, int end, cC.Inner_ConfigStore measure) {
      List<cC.Inner_JsonUtil> out = new ArrayList<>();

      for (int[] run : init(styles, start, end)) {
         out.add(new cC.Inner_JsonUtil(text.substring(run[0], run[1]), run[2], measure.init(text, run[0], run[1], run[2])));
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

   private static List<String> load(cC.Inner_StateCache wrap, String text, int width, int epoch, cC.Inner_ConfigStore measure) {
      if (wrap.read_I != width || wrap.write != epoch) {
         wrap.load_List = init(text, width, measure);
         wrap.read_I = width;
         wrap.write = epoch;
      }

      return wrap.load_List;
   }

   static List<String> init(String text, int width, cC.Inner_ConfigStore measure) {
      String safe = text == null ? "" : text.replace("\r", "").replace('\t', ' ');
      List<String> out = new ArrayList<>();

      for (autismclient.gui.f.ConfigStore.Inner_DataUtil line : autismclient.gui.f.ConfigStore.init(
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
      cc.Inner_JsonUtil message,
      cC.Inner_CachePool kind,
      String text,
      String secondary,
      int x,
      int y,
      int height,
      boolean expanded,
      List<cC.Inner_JsonUtil> spans,
      cC.Inner_StreamUtil deco,
      String marker,
      int flags
   ) {
      Inner_BufferHelper(cc.Inner_JsonUtil message, cC.Inner_CachePool kind, String text, String secondary, int x, int y, int height, boolean expanded) {
         this(message, kind, text, secondary, x, y, height, expanded, null, cC.Inner_StreamUtil.init, "", 0);
      }
   }

   enum Inner_CachePool {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close,
      open;
   }

   interface Inner_ConfigStore {
      int init(String var1, int var2, int var3);

      default int init(String text, int start, int end, int style) {
         return this.init(text, start, end);
      }
   }

   record Inner_DataUtil(cc.Inner_JsonUtil message, cC.Inner_CachePool kind, int top, int bottom, boolean expanded) {
   }

   record Inner_JsonUtil(String text, int style, int width) {
   }

   record Inner_NetHelper(String text, List<cC.Inner_JsonUtil> spans, cC.Inner_StreamUtil deco, String marker, int indent, int flags, int height) {
   }

   private static final class Inner_StateCache {
      int init_I = -1;
      int load_I;
      List<String> init_List = List.of();
      int read_I = -1;
      int write;
      List<String> load_List = List.of();
      int update = -1;
      int sync;
      int flush;
      List<cC.Inner_NetHelper> read_List = List.of();
   }

   enum Inner_StreamUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush;
   }

   record Inner_TaskQueue(int line, int tool, int userIndent, int toolIndent, int userPad, int groupGap, int blockGap) {
   }
}
