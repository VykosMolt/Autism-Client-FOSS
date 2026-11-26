package autismclient.gui.f;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class UiTextRenderer {
   private static final int init_I = 2048;
   private static final int load_I = 9;
   private static final int read_I = 1;
   private static final autismclient.gui.e.MsdfFontFace init_ConfigStore = autismclient.gui.e.MsdfFontFace.BODY;
   private final Font init_Font;
   private int write = -1;
   private final Map<String, Integer> init_Map = new LinkedHashMap<String, Integer>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, Integer> eldest) {
         return this.size() > 2048;
      }
   };
   private final Map<UiTextRenderer.Inner_DataUtil, String> load_Map = new LinkedHashMap<UiTextRenderer.Inner_DataUtil, String>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<UiTextRenderer.Inner_DataUtil, String> eldest) {
         return this.size() > 2048;
      }
   };
   private final Map<UiTextRenderer.Inner_StreamUtil, List<String>> read_Map = new LinkedHashMap<UiTextRenderer.Inner_StreamUtil, List<String>>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<UiTextRenderer.Inner_StreamUtil, List<String>> eldest) {
         return this.size() > 2048;
      }
   };

   public UiTextRenderer(Font font) {
      this.init_Font = font;
   }

   public Font init() {
      return this.init_Font;
   }

   public int init(String text) {
      String safe = text == null ? "" : text;
      int generation = autismclient.gui.e.MsdfTextRenderer.init_I();
      if (generation != this.write) {
         this.write = generation;
         this.init_Map.clear();
         this.load_Map.clear();
         this.read_Map.clear();
      }

      Integer cached = this.init_Map.get(safe);
      if (cached != null) {
         return cached;
      }

      int width = autismclient.gui.e.MsdfTextRenderer.init(init_ConfigStore, safe)
         ? autismclient.gui.e.MsdfTextRenderer.init_I(init_ConfigStore, safe, autismclient.gui.e.MsdfTextRenderer.init_F_ConfigStore(init_ConfigStore))
         : this.init_Font.width(safe);
      this.init_Map.put(safe, width);
      return width;
   }

   public int init(String value, String fragment) {
      String safe = fragment == null ? "" : fragment;
      return autismclient.gui.e.MsdfTextRenderer.init(init_ConfigStore, value == null ? "" : value)
         ? autismclient.gui.e.MsdfTextRenderer.init_I(init_ConfigStore, safe, autismclient.gui.e.MsdfTextRenderer.init_F_ConfigStore(init_ConfigStore))
         : this.init_Font.width(safe);
   }

   public String init_Str(String text, int maxWidth) {
      String safe = text == null ? "" : text;
      if (maxWidth <= 0) {
         return "";
      }

      if (this.init(safe) <= maxWidth) {
         return safe;
      }

      UiTextRenderer.Inner_DataUtil key = new UiTextRenderer.Inner_DataUtil(safe, maxWidth);
      String cached = this.load_Map.get(key);
      if (cached != null) {
         return cached;
      }

      String trimmed = autismclient.gui.e.MsdfTextRenderer.init(init_ConfigStore, safe)
         ? autismclient.gui.e.MsdfTextRenderer.init(init_ConfigStore, safe, autismclient.gui.e.MsdfTextRenderer.init_F_ConfigStore(init_ConfigStore), maxWidth)
         : this.init_Font.plainSubstrByWidth(safe, maxWidth);
      this.load_Map.put(key, trimmed);
      return trimmed;
   }

   public String load(String text, int maxWidth) {
      String safe = text == null ? "" : text;
      if (maxWidth > 0 && !safe.isEmpty()) {
         if (this.init(safe) <= maxWidth) {
            return safe;
         }

         if (autismclient.gui.e.MsdfTextRenderer.init(init_ConfigStore, safe)) {
            return autismclient.gui.e.MsdfTextRenderer.load(init_ConfigStore, safe, autismclient.gui.e.MsdfTextRenderer.init_F_ConfigStore(init_ConfigStore), maxWidth);
         }

         String ellipsis = "...";
         int ellipsisWidth = this.init(ellipsis);
         if (maxWidth <= ellipsisWidth) {
            return this.init_Str(safe, maxWidth);
         }

         String base = this.init_Font.plainSubstrByWidth(safe, Math.max(0, maxWidth - ellipsisWidth));

         while (!base.isEmpty() && this.init(base + ellipsis) > maxWidth) {
            base = base.substring(0, base.length() - 1);
         }

         return base.isEmpty() ? ellipsis : base + ellipsis;
      } else {
         return "";
      }
   }

   public void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
      this.init_V_GuiGraphicsExtract(graphics, text, x, y, color);
   }

   public void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, String text, int x, float y, int color) {
      String safe = text == null ? "" : text;
      if (autismclient.gui.e.MsdfTextRenderer.init(init_ConfigStore, safe)) {
         float size = autismclient.gui.e.MsdfTextRenderer.init_F_ConfigStore(init_ConfigStore);
         autismclient.gui.e.MsdfTextRenderer.init(
            graphics, init_ConfigStore, safe, x, autismclient.gui.e.MsdfTextRenderer.init(init_ConfigStore, y, size), size, color, false
         );
      } else {
         graphics.text(this.init_Font, safe, x, Math.round(y), color, false);
      }
   }

   public void init(GuiGraphicsExtractor graphics, String text, int x, int y, int maxWidth, int color) {
      this.init_V_GuiGraphicsExtract_2(graphics, this.init_Str(text, maxWidth), x, y, color);
   }

   public void load(GuiGraphicsExtractor graphics, String text, int x, int y, int maxWidth, int color) {
      this.init_V_GuiGraphicsExtract_2(graphics, text, x, y, maxWidth, color, 0.75F);
   }

   public void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor graphics, String text, int x, int y, int maxWidth, int color, float minimumScale) {
      this.init_V_GuiGraphicsExtract(graphics, text, x, y, maxWidth, color, minimumScale);
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, String text, int x, float y, int maxWidth, int color, float minimumScale) {
      String safe = text == null ? "" : text;
      if (!safe.isEmpty() && maxWidth > 0) {
         int textWidth = this.init(safe);
         if (textWidth <= maxWidth) {
            this.init_V_GuiGraphicsExtract(graphics, safe, x, y, color);
         } else {
            float requiredScale = Math.min(1.0F, (float)maxWidth / Math.max(1, textWidth));
            float scale = Math.max(Math.max(0.5F, Math.min(1.0F, minimumScale)), requiredScale);
            if (requiredScale < scale) {
               safe = this.load(safe, (int)Math.floor(maxWidth / scale));
            }

            graphics.pose().pushMatrix();

            try {
               graphics.pose().scale(scale, scale);
               float shrunkBoxShift = autismclient.gui.e.MsdfTextRenderer.load(init_ConfigStore, autismclient.gui.e.MsdfTextRenderer.init_F_ConfigStore(init_ConfigStore))
                  * (1.0F - scale)
                  * 0.5F;
               this.init_V_GuiGraphicsExtract(graphics, safe, Math.round(x / scale), (y + shrunkBoxShift) / scale, color);
            } finally {
               graphics.pose().popMatrix();
            }
         }
      }
   }

   public void init(GuiGraphicsExtractor graphics, String text, NetHelper bounds, int color) {
      String display = this.load(text, bounds.update());
      if (!display.isEmpty() && bounds.sync() > 0) {
         if (autismclient.gui.e.MsdfTextRenderer.init(init_ConfigStore, display)) {
            autismclient.gui.e.MsdfFontAtlas msdf = init_ConfigStore.init_StreamUtil();
            if (msdf != null) {
               float size = autismclient.gui.e.MsdfTextRenderer.init_F_ConfigStore(init_ConfigStore);
               autismclient.gui.e.MsdfTextRenderer.init(
                  graphics, init_ConfigStore, display, bounds.read(), msdf.init(display, bounds.write(), bounds.sync(), size), size, color, false
               );
               return;
            }
         }

         this.load(graphics, display, bounds.read(), this.init_I(bounds), bounds.update(), color);
      }
   }

   public void read(GuiGraphicsExtractor graphics, String text, int x, int y, int maxWidth, int color) {
      String display = this.load(text, maxWidth);
      if (!display.isEmpty()) {
         this.init_V_GuiGraphicsExtract_2(graphics, display, x, y, color);
      }
   }

   public List<String> init_List(String text, int maxWidth) {
      String safe = text == null ? "" : text;
      int width = Math.max(1, maxWidth);
      UiTextRenderer.Inner_StreamUtil key = new UiTextRenderer.Inner_StreamUtil(safe, width);
      List<String> cached = this.read_Map.get(key);
      if (cached != null) {
         return cached;
      }

      List<String> lines = new ArrayList<>();

      for (TextWrapLayout.Inner_DataUtil line : TextWrapLayout.init(safe, width, (start, end) -> this.init(safe.substring(start, end)))) {
         lines.add(safe.substring(line.init(), line.read()));
      }

      if (lines.isEmpty()) {
         lines.add("");
      }

      List<String> result = List.copyOf(lines);
      this.read_Map.put(key, result);
      return result;
   }

   public void load(GuiGraphicsExtractor graphics, String text, NetHelper bounds, int color) {
      this.init_V_GuiGraphicsExtract_3(graphics, text, bounds, color, 0);
   }

   public void init_V_GuiGraphicsExtract_3(GuiGraphicsExtractor graphics, String text, NetHelper bounds, int color, int horizontalNudge) {
      String display = text == null ? "" : text;
      int maxWidth = Math.max(0, bounds.update() - 4);
      float scale = this.init(display) <= maxWidth ? 1.0F : Math.max(0.75F, Math.min(1.0F, (float)maxWidth / Math.max(1, this.init(display))));
      if (this.init(display) * scale > maxWidth) {
         display = this.load(display, (int)Math.floor(maxWidth / scale));
      }

      int displayWidth = Math.round(this.init(display) * scale);
      int centeredX = bounds.read() + Math.max(2, (bounds.update() - displayWidth + 1) / 2);
      int minX = bounds.read() + 1;
      int maxX = Math.max(minX, bounds.init() - displayWidth - 1);
      int x = Math.max(minX, Math.min(maxX, centeredX + horizontalNudge));
      this.init_V_GuiGraphicsExtract(graphics, display, x, this.init_F(bounds), maxWidth, color, 0.75F);
   }

   public int init_I(NetHelper bounds) {
      return Math.round(this.init_F(bounds));
   }

   private float init_F(NetHelper bounds) {
      if (init_ConfigStore.init_StreamUtil() == null) {
         int spareHeight = Math.max(0, bounds.sync() - 9);
         return bounds.write() + Math.max(1, (spareHeight + 1) / 2 + 1);
      } else {
         return autismclient.gui.e.MsdfTextRenderer.init(
            init_ConfigStore, bounds.write(), bounds.sync(), autismclient.gui.e.MsdfTextRenderer.init_F_ConfigStore(init_ConfigStore)
         );
      }
   }

   private record Inner_DataUtil(String init_Str, int init_I) {
      private Inner_DataUtil {
         Objects.requireNonNull(init_Str);
      }
   }

   private record Inner_StreamUtil(String init_Str, int init_I) {
      private Inner_StreamUtil {
         Objects.requireNonNull(init_Str);
      }
   }
}
