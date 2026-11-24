package autismclient.gui.f;

import autismclient.gui.d.ValueSink;
import autismclient.util.cA;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class UiHelper {
   private static final TableIndex init_TableIndex = new TableIndex();
   private static final Map<Font, PoolEntry> init_Map = new IdentityHashMap<>();
   private static volatile UiHelper.Inner_DataUtil init_UiHelper_DataUtil;

   private UiHelper() {
   }

   public static SyncManager init(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
      return new SyncManager(graphics, init_TableIndex, init(font), Math.max(1, cA.init_I()), Math.max(1, cA.load_I()), mouseX, mouseY, 0.0F);
   }

   public static void init() {
      init_TableIndex.init_V();
      ValueSink.init();
   }

   public static PoolEntry init(Font font) {
      UiHelper.Inner_DataUtil last = init_UiHelper_DataUtil;
      if (last != null && last.init_Font() == font) {
         return last.init_PoolEntry();
      }

      PoolEntry renderer;
      synchronized (init_Map) {
         renderer = init_Map.computeIfAbsent(font, PoolEntry::new);
      }

      init_UiHelper_DataUtil = new UiHelper.Inner_DataUtil(font, renderer);
      return renderer;
   }

   private record Inner_DataUtil(Font init_Font, PoolEntry init_PoolEntry) {
   }
}
