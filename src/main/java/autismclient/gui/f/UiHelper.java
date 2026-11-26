package autismclient.gui.f;

import autismclient.gui.d.GuiColorPalette;
import autismclient.util.cA;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class UiHelper {
   private static final UiTheme init_TableIndex = new UiTheme();
   private static final Map<Font, UiTextRenderer> init_Map = new IdentityHashMap<>();
   private static volatile UiHelper.Inner_DataUtil init_UiHelper_DataUtil;

   private UiHelper() {
   }

   public static SyncManager init(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
      return new SyncManager(graphics, init_TableIndex, init(font), Math.max(1, cA.init_I()), Math.max(1, cA.load_I()), mouseX, mouseY, 0.0F);
   }

   public static void init() {
      init_TableIndex.init_V();
      GuiColorPalette.init();
   }

   public static UiTextRenderer init(Font font) {
      UiHelper.Inner_DataUtil last = init_UiHelper_DataUtil;
      if (last != null && last.init_Font() == font) {
         return last.init_PoolEntry();
      }

      UiTextRenderer renderer;
      synchronized (init_Map) {
         renderer = init_Map.computeIfAbsent(font, UiTextRenderer::new);
      }

      init_UiHelper_DataUtil = new UiHelper.Inner_DataUtil(font, renderer);
      return renderer;
   }

   private record Inner_DataUtil(Font init_Font, UiTextRenderer init_PoolEntry) {
   }
}
