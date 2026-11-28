package autismclient.gui.sdf;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.InputStream;
import java.io.Reader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public final class MsdfFontAtlas {
   private final String name;
   private final Identifier textureId;
   private final Int2ObjectOpenHashMap<MsdfFontAtlas.Inner_StreamUtil> glyphs;
   private final double lineHeight;
   private final double ascender;
   private final double read;
   private final double write;
   private final int atlasWidth;
   private final int atlasHeight;
   private final MsdfFontAtlas.Inner_StreamUtil spaceGlyph;
   private NativeImage pixels;
   private MsdfFontAtlas.Inner_DataUtil init_StreamUtil_DataUti;
   private boolean init_Z;

   private MsdfFontAtlas(
      String name,
      Identifier textureId,
      Int2ObjectOpenHashMap<MsdfFontAtlas.Inner_StreamUtil> glyphs,
      double lineHeight,
      double ascender,
      double descender,
      double capHeight,
      int atlasWidth,
      int atlasHeight,
      MsdfFontAtlas.Inner_StreamUtil spaceAdvance,
      NativeImage pixels
   ) {
      this.name = name;
      this.textureId = textureId;
      this.glyphs = glyphs;
      this.lineHeight = lineHeight;
      this.ascender = ascender;
      this.read = descender;
      this.write = capHeight;
      this.atlasWidth = atlasWidth;
      this.atlasHeight = atlasHeight;
      this.spaceGlyph = spaceAdvance;
      this.pixels = pixels;
   }

   public static MsdfFontAtlas load(String baseName) {
      Identifier jsonId = Identifier.fromNamespaceAndPath("autismclient", "font/msdf/" + baseName + ".mtsdf");
      Identifier pngId = Identifier.fromNamespaceAndPath("autismclient", "font/msdf/" + baseName + ".png");

      try {
         Resource json = Minecraft.getInstance().getResourceManager().getResource(jsonId).orElse(null);
         Resource png = Minecraft.getInstance().getResourceManager().getResource(pngId).orElse(null);
         if (json != null && png != null) {
            try (
               Reader reader = json.openAsReader();
               InputStream pngStream = png.open();
            ) {
               return init_StreamUtil_Str(baseName, reader, NativeImage.read(pngStream));
            }
         } else {
            return null;
         }
      } catch (Throwable t) {
         return null;
      }
   }

   public static MsdfFontAtlas init_StreamUtil_Str(String baseName, Reader jsonReader, NativeImage pixels) {
      JsonObject root = JsonParser.parseReader(jsonReader).getAsJsonObject();
      JsonObject atlas = root.getAsJsonObject("atlas");
      JsonObject metrics = root.getAsJsonObject("metrics");
      int atlasWidth = atlas.get("width").getAsInt();
      int atlasHeight = atlas.get("height").getAsInt();
      if (atlasWidth > 0 && atlasHeight > 0) {
         double lineHeight = metrics.get("lineHeight").getAsDouble();
         double ascender = metrics.get("ascender").getAsDouble();
         double descender = metrics.has("descender") ? Math.abs(metrics.get("descender").getAsDouble()) : 0.3;
         Int2ObjectOpenHashMap<MsdfFontAtlas.Inner_StreamUtil> glyphs = new Int2ObjectOpenHashMap<>();
         JsonArray glyphArray = root.getAsJsonArray("glyphs");

         for (int i = 0; i < glyphArray.size(); i++) {
            JsonObject g = glyphArray.get(i).getAsJsonObject();
            int unicode = g.get("unicode").getAsInt();
            double advance = g.get("advance").getAsDouble();
            MsdfFontAtlas.Inner_StreamUtil glyph;
            if (g.has("planeBounds") && g.has("atlasBounds")) {
               JsonObject plane = g.getAsJsonObject("planeBounds");
               JsonObject bounds = g.getAsJsonObject("atlasBounds");
               glyph = new MsdfFontAtlas.Inner_StreamUtil(
                  advance,
                  plane.get("left").getAsDouble(),
                  plane.get("top").getAsDouble(),
                  plane.get("right").getAsDouble(),
                  plane.get("bottom").getAsDouble(),
                  bounds.get("left").getAsDouble() / atlasWidth,
                  bounds.get("top").getAsDouble() / atlasHeight,
                  bounds.get("right").getAsDouble() / atlasWidth,
                  bounds.get("bottom").getAsDouble() / atlasHeight,
                  true
               );
            } else {
               glyph = new MsdfFontAtlas.Inner_StreamUtil(advance, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false);
            }

            glyphs.put(unicode, glyph);
         }

         MsdfFontAtlas.Inner_StreamUtil space = glyphs.get(32);
         MsdfFontAtlas.Inner_StreamUtil capital = glyphs.get(72);
         double capHeight = capital != null && capital.init_Z && capital.read < 0.0 ? -capital.read : 0.7;
         Identifier textureId = Identifier.fromNamespaceAndPath("autismclient", "font/msdf/" + baseName);
         return new MsdfFontAtlas(baseName, textureId, glyphs, lineHeight, ascender, descender, capHeight, atlasWidth, atlasHeight, space, pixels);
      } else {
         throw new IllegalArgumentException("msdf atlas has invalid dimensions " + atlasWidth + "x" + atlasHeight);
      }
   }

   public double lineHeight() {
      return this.lineHeight;
   }

   public double load() {
      return -this.ascender;
   }

   public double read() {
      return this.read;
   }

   public double write() {
      return this.write;
   }

   public boolean hasGlyph(int codePoint) {
      return this.glyphs.containsKey(codePoint);
   }

   public boolean canRender(String text) {
      int i = 0;

      while (i < text.length()) {
         int cp = text.codePointAt(i);
         if (!this.glyphs.containsKey(cp)) {
            return false;
         }

         i += Character.charCount(cp);
      }

      return true;
   }

   public float width(String text, float size) {
      float width = 0.0F;
      int i = 0;

      while (i < text.length()) {
         int cp = text.codePointAt(i);
         MsdfFontAtlas.Inner_StreamUtil g = this.glyphs.get(cp);
         if (g == null) {
            g = this.spaceGlyph;
         }

         if (g != null) {
            width += (float)(g.init_D * size);
         }

         i += Character.charCount(cp);
      }

      return width;
   }

   public float init(String text, float top, float height, float size) {
      double min = 1.0 / 0.0;
      double max = -1.0 / 0.0;
      if (text != null) {
         int i = 0;

         while (i < text.length()) {
            int cp = text.codePointAt(i);
            MsdfFontAtlas.Inner_StreamUtil glyph = this.glyphs.get(cp);
            if (glyph == null) {
               glyph = this.spaceGlyph;
            }

            if (glyph != null && glyph.init_Z) {
               min = Math.min(min, glyph.read);
               max = Math.max(max, glyph.update);
            }

            i += Character.charCount(cp);
         }
      }

      double inkCenter = Double.isFinite(min) ? (min + max) * size * 0.5 : 0.0;
      return (float)(top + height * 0.5 - inkCenter - this.load() * size);
   }

   public String truncate(String text, float size, float maxWidth) {
      float width = 0.0F;
      int end = 0;

      while (end < text.length()) {
         int cp = text.codePointAt(end);
         MsdfFontAtlas.Inner_StreamUtil g = this.glyphs.get(cp);
         if (g == null) {
            g = this.spaceGlyph;
         }

         double advance = g != null ? g.init_D * size : 0.0;
         if (width + advance > maxWidth && end > 0) {
            break;
         }

         width += (float)advance;
         end += Character.charCount(cp);
      }

      return text.substring(0, end);
   }

   public String fit(String text, float size, float maxWidth) {
      if (text != null && !text.isEmpty() && !(maxWidth <= 0.0F)) {
         String fitted = this.truncate(text, size, maxWidth);
         return this.width(fitted, size) <= maxWidth ? fitted : "";
      } else {
         return "";
      }
   }

   public int layoutQuads(String text, float x, float yTop, float size, float[] out, int offset) {
      float cursor = x;
      float baseline = yTop + (float)(-this.ascender * size);
      int quads = 0;
      int i = 0;

      while (i < text.length()) {
         int cp = text.codePointAt(i);
         MsdfFontAtlas.Inner_StreamUtil g = this.glyphs.get(cp);
         if (g == null) {
            g = this.spaceGlyph;
         }

         if (g != null) {
            if (g.init_Z) {
               int o = offset + quads * 8;
               out[o] = cursor + (float)(g.load * size);
               out[o + 1] = baseline + (float)(g.read * size);
               out[o + 2] = cursor + (float)(g.write * size);
               out[o + 3] = baseline + (float)(g.update * size);
               out[o + 4] = (float)g.sync;
               out[o + 5] = (float)g.flush;
               out[o + 6] = (float)g.close;
               out[o + 7] = (float)g.open;
               quads++;
            }

            cursor += (float)(g.init_D * size);
         }

         i += Character.charCount(cp);
      }

      return quads;
   }

   public Identifier textureId() {
      return this.textureId;
   }

   public MsdfFontAtlas.Inner_DataUtil texture() {
      if (this.init_StreamUtil_DataUti == null && !this.init_Z) {
         try {
            this.init_StreamUtil_DataUti = new MsdfFontAtlas.Inner_DataUtil(this.pixels != null ? this.pixels : new NativeImage(1, 1, false));
            Minecraft.getInstance().getTextureManager().register(this.textureId, this.init_StreamUtil_DataUti);
         } catch (Throwable t) {
            this.init_Z = true;
         } finally {
            if (this.pixels != null) {
               this.pixels.close();
               this.pixels = null;
            }
         }
      }

      return this.init_StreamUtil_DataUti;
   }

   public void release() {
      if (this.init_StreamUtil_DataUti != null) {
         try {
            Minecraft.getInstance().getTextureManager().release(this.textureId);
            this.init_StreamUtil_DataUti.close();
         } catch (Throwable var2) {
         }

         this.init_StreamUtil_DataUti = null;
      }
   }

   public static final class Inner_DataUtil extends AbstractTexture {
      Inner_DataUtil(NativeImage pixels) {
         int width = pixels.getWidth();
         int height = pixels.getHeight();
         this.texture = RenderSystem.getDevice().createTexture("autismclient msdf font", 15, GpuFormat.RGBA8_UNORM, width, height, 1, 1);
         this.sampler = RenderSystem.getSamplerCache()
            .getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR, false);
         this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
         RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, pixels);
      }

      public int init() {
         return this.getTexture().getWidth(0);
      }

      public int load() {
         return this.getTexture().getHeight(0);
      }
   }

   public record Inner_StreamUtil(
      double init_D, double load, double read, double write, double update, double sync, double flush, double close, double open, boolean init_Z
   ) {
   }
}
