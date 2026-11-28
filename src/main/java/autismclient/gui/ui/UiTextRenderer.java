package autismclient.gui.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import autismclient.gui.f.NetHelper;

public final class UiTextRenderer {
   private static final int CACHE_LIMIT = 2048;
   private static final int VANILLA_LINE_HEIGHT = 9;
   private static final int VANILLA_BASELINE_OFFSET = 1;
   private static final autismclient.gui.sdf.MsdfFontFace BODY_FONT = autismclient.gui.sdf.MsdfFontFace.BODY;
   private final Font font;
   private int cacheGeneration = -1;
   private final Map<String, Integer> widthCache = new LinkedHashMap<String, Integer>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<String, Integer> eldest) {
         return this.size() > 2048;
      }
   };
   private final Map<UiTextRenderer.TrimKey, String> trimCache = new LinkedHashMap<UiTextRenderer.TrimKey, String>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<UiTextRenderer.TrimKey, String> eldest) {
         return this.size() > 2048;
      }
   };
   private final Map<UiTextRenderer.WrapKey, List<String>> wrapCache = new LinkedHashMap<UiTextRenderer.WrapKey, List<String>>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<UiTextRenderer.WrapKey, List<String>> eldest) {
         return this.size() > 2048;
      }
   };

   public UiTextRenderer(Font font) {
      this.font = font;
   }

   public Font font() {
      return this.font;
   }

   public int measureWidth(String text) {
      String safe = text == null ? "" : text;
      int generation = autismclient.gui.sdf.MsdfTextRenderer.cacheGeneration();
      if (generation != this.cacheGeneration) {
         this.cacheGeneration = generation;
         this.widthCache.clear();
         this.trimCache.clear();
         this.wrapCache.clear();
      }

      Integer cached = this.widthCache.get(safe);
      if (cached != null) {
         return cached;
      }

      int width = autismclient.gui.sdf.MsdfTextRenderer.canRender(BODY_FONT, safe)
         ? autismclient.gui.sdf.MsdfTextRenderer.measureWidth(BODY_FONT, safe, autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(BODY_FONT))
         : this.font.width(safe);
      this.widthCache.put(safe, width);
      return width;
   }

   public int measureFragmentWidth(String value, String fragment) {
      String safe = fragment == null ? "" : fragment;
      return autismclient.gui.sdf.MsdfTextRenderer.canRender(BODY_FONT, value == null ? "" : value)
         ? autismclient.gui.sdf.MsdfTextRenderer.measureWidth(BODY_FONT, safe, autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(BODY_FONT))
         : this.font.width(safe);
   }

   public String trimToWidth(String text, int maxWidth) {
      String safe = text == null ? "" : text;
      if (maxWidth <= 0) {
         return "";
      }

      if (this.measureWidth(safe) <= maxWidth) {
         return safe;
      }

      UiTextRenderer.TrimKey key = new UiTextRenderer.TrimKey(safe, maxWidth);
      String cached = this.trimCache.get(key);
      if (cached != null) {
         return cached;
      }

      String trimmed = autismclient.gui.sdf.MsdfTextRenderer.canRender(BODY_FONT, safe)
         ? autismclient.gui.sdf.MsdfTextRenderer.trimToWidth(BODY_FONT, safe, autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(BODY_FONT), maxWidth)
         : this.font.plainSubstrByWidth(safe, maxWidth);
      this.trimCache.put(key, trimmed);
      return trimmed;
   }

   public String ellipsize(String text, int maxWidth) {
      String safe = text == null ? "" : text;
      if (maxWidth > 0 && !safe.isEmpty()) {
         if (this.measureWidth(safe) <= maxWidth) {
            return safe;
         }

         if (autismclient.gui.sdf.MsdfTextRenderer.canRender(BODY_FONT, safe)) {
            return autismclient.gui.sdf.MsdfTextRenderer.ellipsize(BODY_FONT, safe, autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(BODY_FONT), maxWidth);
         }

         String ellipsis = "...";
         int ellipsisWidth = this.measureWidth(ellipsis);
         if (maxWidth <= ellipsisWidth) {
            return this.trimToWidth(safe, maxWidth);
         }

         String base = this.font.plainSubstrByWidth(safe, Math.max(0, maxWidth - ellipsisWidth));

         while (!base.isEmpty() && this.measureWidth(base + ellipsis) > maxWidth) {
            base = base.substring(0, base.length() - 1);
         }

         return base.isEmpty() ? ellipsis : base + ellipsis;
      } else {
         return "";
      }
   }

   public void draw(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
      this.drawFloat(graphics, text, x, y, color);
   }

   public void drawFloat(GuiGraphicsExtractor graphics, String text, int x, float y, int color) {
      String safe = text == null ? "" : text;
      if (autismclient.gui.sdf.MsdfTextRenderer.canRender(BODY_FONT, safe)) {
         float size = autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(BODY_FONT);
         autismclient.gui.sdf.MsdfTextRenderer.draw(
            graphics, BODY_FONT, safe, x, autismclient.gui.sdf.MsdfTextRenderer.alignToVanillaTop(BODY_FONT, y, size), size, color, false
         );
      } else {
         graphics.text(this.font, safe, x, Math.round(y), color, false);
      }
   }

   public void drawTrimmed(GuiGraphicsExtractor graphics, String text, int x, int y, int maxWidth, int color) {
      this.draw(graphics, this.trimToWidth(text, maxWidth), x, y, color);
   }

   public void drawFitted(GuiGraphicsExtractor graphics, String text, int x, int y, int maxWidth, int color) {
      this.drawFitted(graphics, text, x, y, maxWidth, color, 0.75F);
   }

   public void drawFitted(GuiGraphicsExtractor graphics, String text, int x, int y, int maxWidth, int color, float minimumScale) {
      this.drawFittedFloat(graphics, text, x, y, maxWidth, color, minimumScale);
   }

   private void drawFittedFloat(GuiGraphicsExtractor graphics, String text, int x, float y, int maxWidth, int color, float minimumScale) {
      String safe = text == null ? "" : text;
      if (!safe.isEmpty() && maxWidth > 0) {
         int textWidth = this.measureWidth(safe);
         if (textWidth <= maxWidth) {
            this.drawFloat(graphics, safe, x, y, color);
         } else {
            float requiredScale = Math.min(1.0F, (float)maxWidth / Math.max(1, textWidth));
            float scale = Math.max(Math.max(0.5F, Math.min(1.0F, minimumScale)), requiredScale);
            if (requiredScale < scale) {
               safe = this.ellipsize(safe, (int)Math.floor(maxWidth / scale));
            }

            graphics.pose().pushMatrix();

            try {
               graphics.pose().scale(scale, scale);
               float shrunkBoxShift = autismclient.gui.sdf.MsdfTextRenderer.glyphBoxHeight(BODY_FONT, autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(BODY_FONT))
                  * (1.0F - scale)
                  * 0.5F;
               this.drawFloat(graphics, safe, Math.round(x / scale), (y + shrunkBoxShift) / scale, color);
            } finally {
               graphics.pose().popMatrix();
            }
         }
      }
   }

   public void drawEllipsizedInBounds(GuiGraphicsExtractor graphics, String text, NetHelper bounds, int color) {
      String display = this.ellipsize(text, bounds.update());
      if (!display.isEmpty() && bounds.sync() > 0) {
         if (autismclient.gui.sdf.MsdfTextRenderer.canRender(BODY_FONT, display)) {
            autismclient.gui.sdf.MsdfFontAtlas msdf = BODY_FONT.atlas();
            if (msdf != null) {
               float size = autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(BODY_FONT);
               autismclient.gui.sdf.MsdfTextRenderer.draw(
                  graphics, BODY_FONT, display, bounds.read(), msdf.init(display, bounds.write(), bounds.sync(), size), size, color, false
               );
               return;
            }
         }

         this.drawFitted(graphics, display, bounds.read(), this.centerTextY(bounds), bounds.update(), color);
      }
   }

   public void drawEllipsized(GuiGraphicsExtractor graphics, String text, int x, int y, int maxWidth, int color) {
      String display = this.ellipsize(text, maxWidth);
      if (!display.isEmpty()) {
         this.draw(graphics, display, x, y, color);
      }
   }

   public List<String> wrapLines(String text, int maxWidth) {
      String safe = text == null ? "" : text;
      int width = Math.max(1, maxWidth);
      UiTextRenderer.WrapKey key = new UiTextRenderer.WrapKey(safe, width);
      List<String> cached = this.wrapCache.get(key);
      if (cached != null) {
         return cached;
      }

      List<String> lines = new ArrayList<>();

      for (TextWrapLayout.Inner_DataUtil line : TextWrapLayout.wrap(safe, width, (start, end) -> this.measureWidth(safe.substring(start, end)))) {
         lines.add(safe.substring(line.init(), line.read()));
      }

      if (lines.isEmpty()) {
         lines.add("");
      }

      List<String> result = List.copyOf(lines);
      this.wrapCache.put(key, result);
      return result;
   }

   public void drawCentered(GuiGraphicsExtractor graphics, String text, NetHelper bounds, int color) {
      this.drawCentered(graphics, text, bounds, color, 0);
   }

   public void drawCentered(GuiGraphicsExtractor graphics, String text, NetHelper bounds, int color, int horizontalNudge) {
      String display = text == null ? "" : text;
      int maxWidth = Math.max(0, bounds.update() - 4);
      float scale = this.measureWidth(display) <= maxWidth ? 1.0F : Math.max(0.75F, Math.min(1.0F, (float)maxWidth / Math.max(1, this.measureWidth(display))));
      if (this.measureWidth(display) * scale > maxWidth) {
         display = this.ellipsize(display, (int)Math.floor(maxWidth / scale));
      }

      int displayWidth = Math.round(this.measureWidth(display) * scale);
      int centeredX = bounds.read() + Math.max(2, (bounds.update() - displayWidth + 1) / 2);
      int minX = bounds.read() + 1;
      int maxX = Math.max(minX, bounds.init() - displayWidth - 1);
      int x = Math.max(minX, Math.min(maxX, centeredX + horizontalNudge));
      this.drawFittedFloat(graphics, display, x, this.centerTextYFloat(bounds), maxWidth, color, 0.75F);
   }

   public int centerTextY(NetHelper bounds) {
      return Math.round(this.centerTextYFloat(bounds));
   }

   private float centerTextYFloat(NetHelper bounds) {
      if (BODY_FONT.atlas() == null) {
         int spareHeight = Math.max(0, bounds.sync() - 9);
         return bounds.write() + Math.max(1, (spareHeight + 1) / 2 + 1);
      } else {
         return autismclient.gui.sdf.MsdfTextRenderer.centerVertically(
            BODY_FONT, bounds.write(), bounds.sync(), autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(BODY_FONT)
         );
      }
   }

   private record TrimKey(String text, int maxWidth) {
      private TrimKey {
         Objects.requireNonNull(text);
      }
   }

   private record WrapKey(String text, int maxWidth) {
      private WrapKey {
         Objects.requireNonNull(text);
      }
   }
}
