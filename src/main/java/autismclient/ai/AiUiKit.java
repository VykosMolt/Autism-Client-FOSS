package autismclient.ai;

import autismclient.gui.ui.UiClipStack;
import autismclient.gui.f.PacketUtil;
import autismclient.gui.ui.widgets.ScrollbarMetricsHelper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

final class AiUiKit {
   static final int init_I = -10497050;
   static final int load_I = -14249;
   static final int read_I = 24;
   final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final List<AiUiKit.Inner_DataUtil> init_List = new ArrayList<>();
   private final List<AiUiKit.Inner_CachePool> load_List = new ArrayList<>();
   private final List<AiUiKit.Inner_StreamUtil> read_List = new ArrayList<>();
   private final ArrayDeque<autismclient.gui.f.NetHelper> init_ArrayDeque = new ArrayDeque<>();
   private AiUiKit.Inner_StreamUtil init_cE_StreamUtil;
   GuiGraphicsExtractor init_GuiGraphicsExtract;
   autismclient.gui.f.SyncManager init_SyncManager;
   autismclient.gui.ui.UiColorScheme init_JsonUtil;
   autismclient.gui.ui.layout.LayoutContext init_SyncManager_2;
   Font init_Font;
   int write;
   int update;
   int sync;
   int flush;

   void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean pointerBlocked, float delta) {
      this.init_GuiGraphicsExtract = graphics;
      this.init_Font = Minecraft.getInstance().font;
      this.sync = mouseX;
      this.flush = mouseY;
      this.write = pointerBlocked ? -10000 : mouseX;
      this.update = pointerBlocked ? -10000 : mouseY;
      this.init_SyncManager = autismclient.gui.f.UiHelper.init(graphics, this.init_Font, this.write, this.update);
      this.init_JsonUtil = this.init_SyncManager.init_TableIndex().init_JsonUtil();
      this.init_SyncManager_2 = new autismclient.gui.ui.layout.LayoutContext(
         graphics, this.init_Font, autismclient.gui.ui.layout.LayoutViewport.init_RingBuffer_F(2.0F), this.init_CharEncoder, this.write, this.update, delta
      );
      this.init_List.clear();
      this.load_List.clear();
      this.read_List.clear();
      this.init_ArrayDeque.clear();
   }

   boolean init_Z() {
      return this.init_SyncManager_2 != null;
   }

   static int init_I() {
      return autismclient.gui.ui.UiDensityMetrics.init(13);
   }

   static int load_I() {
      return autismclient.gui.ui.UiDensityMetrics.init(14);
   }

   static int read() {
      return autismclient.gui.ui.UiDensityMetrics.init(12);
   }

   static int write() {
      return autismclient.gui.ui.UiDensityMetrics.init(10);
   }

   int init_I_Str(String text) {
      return this.init_SyncManager.init_PoolEntry().init(text);
   }

   int load(String label) {
      return autismclient.gui.ui.UiDensityMetrics.load(this.init_I_Str(label));
   }

   static autismclient.gui.f.NetHelper init_NetHelper_NetHelper_NetHelper(autismclient.gui.f.NetHelper a, autismclient.gui.f.NetHelper b) {
      int x = Math.max(a.read(), b.read());
      int y = Math.max(a.write(), b.write());
      int r = Math.min(a.init(), b.init());
      int btm = Math.min(a.load(), b.load());
      return autismclient.gui.f.NetHelper.init(x, y, Math.max(0, r - x), Math.max(0, btm - y));
   }

   void init_V_NetHelper(autismclient.gui.f.NetHelper bounds) {
      autismclient.gui.f.NetHelper clip = this.init_ArrayDeque.isEmpty() ? bounds : init_NetHelper_NetHelper_NetHelper(this.init_ArrayDeque.peek(), bounds);
      this.init_ArrayDeque.push(clip);
      UiClipStack.init_HashUtil().init(this.init_GuiGraphicsExtract, clip);
   }

   void init_V() {
      this.init_ArrayDeque.pop();
      UiClipStack.init_HashUtil().init(this.init_GuiGraphicsExtract);
   }

   private autismclient.gui.f.NetHelper init_NetHelper_NetHelper(autismclient.gui.f.NetHelper bounds) {
      return this.init_ArrayDeque.isEmpty() ? bounds : init_NetHelper_NetHelper_NetHelper(this.init_ArrayDeque.peek(), bounds);
   }

   boolean init_Z_NetHelper(autismclient.gui.f.NetHelper bounds) {
      autismclient.gui.f.NetHelper c = this.init_NetHelper_NetHelper(bounds);
      return c.update() > 0 && c.sync() > 0 && c.init_Z(this.write, this.update);
   }

   boolean load_Z(autismclient.gui.f.NetHelper bounds) {
      autismclient.gui.f.NetHelper c = this.init_NetHelper_NetHelper(bounds);
      return c.update() > 0 && c.sync() > 0;
   }

   void init_V_NetHelper_Runnable(autismclient.gui.f.NetHelper bounds, Runnable left) {
      this.init_V_NetHelper(bounds, left, null, null);
   }

   void init_V_NetHelper(autismclient.gui.f.NetHelper bounds, Runnable left, Runnable right, String tip) {
      autismclient.gui.f.NetHelper c = this.init_NetHelper_NetHelper(bounds);
      if (c.update() > 0 && c.sync() > 0) {
         this.init_List.add(new AiUiKit.Inner_DataUtil(c, left, right, tip, null));
      }
   }

   void load_V(autismclient.gui.f.NetHelper bounds) {
      this.init_V_NetHelper(bounds, () -> {}, () -> {}, null);
   }

   void init_V_NetHelper_Str(autismclient.gui.f.NetHelper bounds, String tip) {
      this.init_V_NetHelper(bounds, null, null, tip);
   }

   void init_V_cE_StreamUtil(AiUiKit.Inner_StreamUtil scroll) {
      this.load_List.add(new AiUiKit.Inner_CachePool(scroll.init_NetHelper(), scroll));
      if (scroll.init_PoolEntry_DataUtil != null && scroll.init_PoolEntry_DataUtil.init_Z()) {
         this.read_List.add(scroll);
      }
   }

   void read(autismclient.gui.f.NetHelper bounds) {
      this.load_List.add(new AiUiKit.Inner_CachePool(bounds, null));
   }

   void load(AiUiKit.Inner_StreamUtil scroll) {
      if (scroll.init_PoolEntry_DataUtil != null) {
         ScrollbarMetricsHelper.init(
            this.init_GuiGraphicsExtract, scroll.init_PoolEntry_DataUtil, scroll.init_PoolEntry_DataUtil.init(this.write, this.update), scroll.init_Z()
         );
      }
   }

   void init_V_BitSetUtil_NetHelper(autismclient.gui.ui.widgets.TextInputWidget field, autismclient.gui.f.NetHelper bounds) {
      field.init_TableIndex_F(bounds.read(), bounds.write(), bounds.update(), bounds.sync());
      field.init_V_SyncManager(this.init_SyncManager_2);
      autismclient.gui.f.NetHelper c = this.init_NetHelper_NetHelper(bounds);
      if (c.update() > 0 && c.sync() > 0) {
         this.init_List.add(new AiUiKit.Inner_DataUtil(c, null, null, null, field));
      }
   }

   void init_V_NetHelper_2(autismclient.gui.f.NetHelper bounds, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, Runnable action) {
      this.init_V_NetHelper(bounds, label, tone, action, null);
   }

   void init_V_NetHelper(autismclient.gui.f.NetHelper bounds, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, Runnable action, String tip) {
      boolean colored = tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.read
         || tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.write
         || tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.update;
      autismclient.gui.f.b.CachePool.init(this.init_SyncManager, bounds, label, tone, this.init_Z_NetHelper(bounds), colored);
      this.init_V_NetHelper(bounds, action, null, tip);
   }

   void init_V_NetHelper_3(
      autismclient.gui.f.NetHelper bounds, Identifier icon, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, Runnable action
   ) {
      boolean colored = tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.read
         || tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.write
         || tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.update;
      autismclient.gui.f.b.CachePool.init(this.init_SyncManager, bounds, label, icon, tone, this.init_Z_NetHelper(bounds), colored, colored ? 1.0F : 0.0F);
      this.init_V_NetHelper_Runnable(bounds, action);
   }

   void init(autismclient.gui.f.NetHelper bounds, Identifier icon, boolean selected, boolean danger, String tip, Runnable action) {
      autismclient.gui.ui.widgets.IconButtonRenderer.init(this.init_GuiGraphicsExtract, this.init_Font, bounds, icon, this.init_Z_NetHelper(bounds), true, danger);
      if (selected) {
         int radius = Math.min(3, Math.max(2, Math.min(bounds.update(), bounds.sync()) / 3));
         PacketUtil.load(this.init_GuiGraphicsExtract, bounds, radius, PacketUtil.init(this.init_JsonUtil.clear, 0.14F), this.init_JsonUtil.clear);
      }

      this.init_V_NetHelper(bounds, action, null, tip);
   }

   void init_V_NetHelper_2(autismclient.gui.f.NetHelper bounds, String label, boolean value, String key, Runnable action) {
      autismclient.gui.ui.widgets.ToggleSwitchRenderer.init(this.init_SyncManager, bounds, label, value, this.init_Z_NetHelper(bounds), "ai:" + key);
      this.init_V_NetHelper_Runnable(bounds, action);
   }

   void init_V_Str(String text, int x, int y, int color) {
      this.init_SyncManager.init_PoolEntry().init_V_GuiGraphicsExtract_2(this.init_GuiGraphicsExtract, text, x, y, color);
   }

   void init_V_Str_2(String text, int x, int y, int maxWidth, int color) {
      this.init_SyncManager.init_PoolEntry().read(this.init_GuiGraphicsExtract, text, x, y, maxWidth, color);
   }

   void init_V_Str_3(String text, autismclient.gui.f.NetHelper row, int x, int maxWidth, int color) {
      this.init_SyncManager.init_PoolEntry().read(this.init_GuiGraphicsExtract, text, x, this.init_SyncManager.init_PoolEntry().init_I(row), maxWidth, color);
   }

   String init_Str_Str_I(String text, int maxWidth) {
      String flat = text.replace('\n', ' ').trim();
      if (this.init_I_Str(flat) <= maxWidth) {
         return flat;
      }

      int lo = 0;
      int hi = flat.length();

      while (lo < hi) {
         int mid = lo + hi >>> 1;
         if (this.init_I_Str("..." + flat.substring(mid)) <= maxWidth) {
            hi = mid;
         } else {
            lo = mid + 1;
         }
      }

      return "..." + flat.substring(Math.min(lo, flat.length()));
   }

   void init_V_I(int cx, int cy, float r, int color) {
      PacketUtil.init_V_GuiGraphicsExtract(this.init_GuiGraphicsExtract, cx, cy, r, color);
   }

   boolean init_Z_D(double x, double y, int button, List<autismclient.gui.ui.widgets.TextInputWidget> fields) {
      int ix = (int)x;
      int iy = (int)y;

      for (int i = this.read_List.size() - 1; i >= 0 && button == 0; i--) {
         AiUiKit.Inner_StreamUtil s = this.read_List.get(i);
         if (s.init_PoolEntry_DataUtil.init(x, y)) {
            s.write = s.init_PoolEntry_DataUtil.load(x, y) ? iy - s.init_PoolEntry_DataUtil.update() : s.init_PoolEntry_DataUtil.sync() / 2;
            this.init_cE_StreamUtil = s;
            this.init_Z_D(y);
            return true;
         }
      }

      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         AiUiKit.Inner_DataUtil hit = this.init_List.get(i);
         if (hit.bounds().init_Z(ix, iy)) {
            if (hit.field() != null) {
               for (autismclient.gui.ui.widgets.TextInputWidget other : fields) {
                  if (other != hit.field()) {
                     other.init_V_Z(false);
                  }
               }

               if (button == 0) {
                  hit.field().init_Z_SyncManager_2(this.init_SyncManager_2, (float)x, (float)y, button);
               }

               return true;
            }

            Runnable action = button == 1 ? hit.right() : (button == 0 ? hit.left() : null);
            if (action != null || hit.left() != null || hit.right() != null) {
               for (autismclient.gui.ui.widgets.TextInputWidget field : fields) {
                  field.init_V_Z(false);
               }

               if (action != null) {
                  action.run();
               }

               return true;
            }
         }
      }

      for (autismclient.gui.ui.widgets.TextInputWidget field : fields) {
         field.init_V_Z(false);
      }

      return false;
   }

   boolean init_Z_D(double y) {
      if (this.init_cE_StreamUtil == null) {
         return false;
      }

      this.init_cE_StreamUtil.init_I = ScrollbarMetricsHelper.init(this.init_cE_StreamUtil.init_PoolEntry_DataUtil, y, this.init_cE_StreamUtil.write);
      this.init_cE_StreamUtil.init_Z = this.init_cE_StreamUtil.init_I >= this.init_cE_StreamUtil.init_I();
      return true;
   }

   boolean load_Z() {
      if (this.init_cE_StreamUtil == null) {
         return false;
      }

      this.init_cE_StreamUtil.write = -1;
      this.init_cE_StreamUtil = null;
      return true;
   }

   boolean init(double x, double y, double amount) {
      for (int i = this.load_List.size() - 1; i >= 0; i--) {
         AiUiKit.Inner_CachePool s = this.load_List.get(i);
         if (s.bounds().init_Z((int)x, (int)y)) {
            if (s.scroll() != null) {
               s.scroll().init(amount);
            }

            return true;
         }
      }

      return false;
   }

   void load_V() {
      for (int i = this.init_List.size() - 1; i >= 0; i--) {
         AiUiKit.Inner_DataUtil hit = this.init_List.get(i);
         if (hit.bounds().init_Z(this.write, this.update)) {
            if (hit.tip() != null && !hit.tip().isBlank()) {
               autismclient.gui.ui.widgets.TooltipRenderer.init(this.init_SyncManager, hit.tip(), this.write, this.update);
            }

            if (hit.tip() != null || hit.left() != null || hit.field() != null) {
               return;
            }
         }
      }
   }

   static autismclient.gui.sdf.MsdfFontFace init_ConfigStore_I(int style) {
      if ((style & 1) != 0) {
         return autismclient.gui.sdf.MsdfFontFace.TITLE;
      } else {
         return (style & 2) != 0 ? autismclient.gui.sdf.MsdfFontFace.LABEL : autismclient.gui.sdf.MsdfFontFace.BODY;
      }
   }

   static float init_F_I(int style) {
      float body = autismclient.gui.sdf.MsdfTextRenderer.init_F_ConfigStore(autismclient.gui.sdf.MsdfFontFace.BODY);
      return (style & 16) != 0 ? body * 1.15F : body;
   }

   void init_V_Str(String text, float x, int y, int style, int color) {
      if (!text.isEmpty()) {
         autismclient.gui.sdf.MsdfFontFace face = init_ConfigStore_I(style);
         if (autismclient.gui.sdf.MsdfTextRenderer.init(face, text)) {
            float size = init_F_I(style);
            autismclient.gui.sdf.MsdfTextRenderer.init(
               this.init_GuiGraphicsExtract, face, text, x, autismclient.gui.sdf.MsdfTextRenderer.init(face, y, size), size, color, false
            );
         } else {
            this.init_GuiGraphicsExtract.text(this.init_Font, text, Math.round(x), y, color, false);
         }
      }
   }

   static AiTranscriptLayout.Inner_ConfigStore init_cC_ConfigStore_Font(final Font font) {
      return new AiTranscriptLayout.Inner_ConfigStore() {
         @Override
         public int init(String text, int start, int end) {
            return this.init(text, start, end, 0);
         }

         @Override
         public int init(String text, int start, int end, int style) {
            String part = text.substring(start, end);
            autismclient.gui.sdf.MsdfFontFace face = AiUiKit.init_ConfigStore_I(style);
            if (autismclient.gui.sdf.MsdfTextRenderer.init(face, part)) {
               autismclient.gui.sdf.MsdfFontAtlas msdf = face.init_StreamUtil();
               if (msdf != null) {
                  return (int)Math.ceil(msdf.init(part, AiUiKit.init_F_I(style)) - 1.0E-4);
               }
            }

            return font.width(part);
         }
      };
   }

   static String init_Str_J_J(long at, long now) {
      long s = Math.max(0L, (now - at) / 1000L);
      if (s < 60L) {
         return "now";
      }

      long m = s / 60L;
      if (m < 60L) {
         return m + "m";
      }

      long h = m / 60L;
      return h < 48L ? h + "h" : h / 24L + "d";
   }

   static String init_Str_J(long n) {
      if (n >= 1000000L) {
         return String.format(Locale.ROOT, "%.1fM", n / 1000000.0);
      } else if (n >= 10000L) {
         return n / 1000L + "k";
      } else {
         return n >= 1000L ? String.format(Locale.ROOT, "%.1fk", n / 1000.0) : Long.toString(n);
      }
   }

   static String init_Str_Str(String text) {
      return text == null ? "" : text.replace('\n', ' ').trim();
   }

   private record Inner_CachePool(autismclient.gui.f.NetHelper bounds, AiUiKit.Inner_StreamUtil scroll) {
   }

   private record Inner_DataUtil(autismclient.gui.f.NetHelper bounds, Runnable left, Runnable right, String tip, autismclient.gui.ui.widgets.TextInputWidget field) {
   }

   static final class Inner_StreamUtil {
      int init_I;
      boolean init_Z;
      private int load;
      private int read;
      private int write = -1;
      private autismclient.gui.f.NetHelper init_NetHelper = autismclient.gui.f.NetHelper.init(0, 0, 0, 0);
      private ScrollbarMetricsHelper.Inner_DataUtil init_PoolEntry_DataUtil;

      Inner_StreamUtil(boolean follow) {
         this.init_Z = follow;
      }

      int init_I() {
         return Math.max(0, this.load - this.read);
      }

      void init(autismclient.gui.f.NetHelper viewport, int content) {
         this.init_NetHelper = viewport;
         this.load = content;
         this.read = viewport.sync();
         this.init_I = this.init_Z ? this.init_I() : Math.max(0, Math.min(this.init_I, this.init_I()));
         this.init_PoolEntry_DataUtil = ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
            content, this.read, viewport.init() - 6, viewport.write(), 6, viewport.sync(), this.init_I
         );
      }

      void init(double amount) {
         this.init_I = Math.max(0, Math.min(this.init_I(), this.init_I - (int)Math.round(amount * 24.0)));
         this.init_Z = this.init_I >= this.init_I();
      }

      void init_V() {
         this.init_Z = true;
         this.init_I = 2147483647;
      }

      void load() {
         this.init_Z = false;
         this.init_I = 0;
      }

      boolean init_Z() {
         return this.write >= 0;
      }

      autismclient.gui.f.NetHelper init_NetHelper() {
         return this.init_NetHelper;
      }
   }
}
