package autismclient.gui.e;

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

public final class StreamUtil {
   private final String init_Str;
   private final Identifier init_Identifier;
   private final Int2ObjectOpenHashMap<StreamUtil.Inner_StreamUtil> init_Int2ObjectOpenHash;
   private final double init_D;
   private final double load_D;
   private final double read;
   private final double write;
   private final int init_I;
   private final int load_I;
   private final StreamUtil.Inner_StreamUtil init_StreamUtil_StreamU;
   private NativeImage init_NativeImage;
   private StreamUtil.Inner_DataUtil init_StreamUtil_DataUti;
   private boolean init_Z;

   private StreamUtil(
      String name,
      Identifier textureId,
      Int2ObjectOpenHashMap<StreamUtil.Inner_StreamUtil> glyphs,
      double lineHeight,
      double ascender,
      double descender,
      double capHeight,
      int atlasWidth,
      int atlasHeight,
      StreamUtil.Inner_StreamUtil spaceAdvance,
      NativeImage pixels
   ) {
      this.init_Str = name;
      this.init_Identifier = textureId;
      this.init_Int2ObjectOpenHash = glyphs;
      this.init_D = lineHeight;
      this.load_D = ascender;
      this.read = descender;
      this.write = capHeight;
      this.init_I = atlasWidth;
      this.load_I = atlasHeight;
      this.init_StreamUtil_StreamU = spaceAdvance;
      this.init_NativeImage = pixels;
   }

   public static StreamUtil init_StreamUtil_Str(String baseName) {
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

   public static StreamUtil init_StreamUtil_Str(String baseName, Reader jsonReader, NativeImage pixels) {
      JsonObject root = JsonParser.parseReader(jsonReader).getAsJsonObject();
      JsonObject atlas = root.getAsJsonObject("atlas");
      JsonObject metrics = root.getAsJsonObject("metrics");
      int atlasWidth = atlas.get("width").getAsInt();
      int atlasHeight = atlas.get("height").getAsInt();
      if (atlasWidth > 0 && atlasHeight > 0) {
         double lineHeight = metrics.get("lineHeight").getAsDouble();
         double ascender = metrics.get("ascender").getAsDouble();
         double descender = metrics.has("descender") ? Math.abs(metrics.get("descender").getAsDouble()) : 0.3;
         Int2ObjectOpenHashMap<StreamUtil.Inner_StreamUtil> glyphs = new Int2ObjectOpenHashMap<>();
         JsonArray glyphArray = root.getAsJsonArray("glyphs");

         for (int i = 0; i < glyphArray.size(); i++) {
            JsonObject g = glyphArray.get(i).getAsJsonObject();
            int unicode = g.get("unicode").getAsInt();
            double advance = g.get("advance").getAsDouble();
            StreamUtil.Inner_StreamUtil glyph;
            if (g.has("planeBounds") && g.has("atlasBounds")) {
               JsonObject plane = g.getAsJsonObject("planeBounds");
               JsonObject bounds = g.getAsJsonObject("atlasBounds");
               glyph = new StreamUtil.Inner_StreamUtil(
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
               glyph = new StreamUtil.Inner_StreamUtil(advance, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false);
            }

            glyphs.put(unicode, glyph);
         }

         StreamUtil.Inner_StreamUtil space = glyphs.get(32);
         StreamUtil.Inner_StreamUtil capital = glyphs.get(72);
         double capHeight = capital != null && capital.init_Z && capital.read < 0.0 ? -capital.read : 0.7;
         Identifier textureId = Identifier.fromNamespaceAndPath("autismclient", "font/msdf/" + baseName);
         return new StreamUtil(baseName, textureId, glyphs, lineHeight, ascender, descender, capHeight, atlasWidth, atlasHeight, space, pixels);
      } else {
         throw new IllegalArgumentException("msdf atlas has invalid dimensions " + atlasWidth + "x" + atlasHeight);
      }
   }

   public double init_D() {
      return this.init_D;
   }

   public double load() {
      return -this.load_D;
   }

   public double read() {
      return this.read;
   }

   public double write() {
      return this.write;
   }

   public boolean init_Z_I(int codePoint) {
      return this.init_Int2ObjectOpenHash.containsKey(codePoint);
   }

   public boolean init_Z_Str(String text) {
      int i = 0;

      while (i < text.length()) {
         int cp = text.codePointAt(i);
         if (!this.init_Int2ObjectOpenHash.containsKey(cp)) {
            return false;
         }

         i += Character.charCount(cp);
      }

      return true;
   }

   public float init(String text, float size) {
      float width = 0.0F;
      int i = 0;

      while (i < text.length()) {
         int cp = text.codePointAt(i);
         StreamUtil.Inner_StreamUtil g = this.init_Int2ObjectOpenHash.get(cp);
         if (g == null) {
            g = this.init_StreamUtil_StreamU;
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
            StreamUtil.Inner_StreamUtil glyph = this.init_Int2ObjectOpenHash.get(cp);
            if (glyph == null) {
               glyph = this.init_StreamUtil_StreamU;
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

   public String init_Str_Str(String text, float size, float maxWidth) {
      float width = 0.0F;
      int end = 0;

      while (end < text.length()) {
         int cp = text.codePointAt(end);
         StreamUtil.Inner_StreamUtil g = this.init_Int2ObjectOpenHash.get(cp);
         if (g == null) {
            g = this.init_StreamUtil_StreamU;
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

   public String load(String text, float size, float maxWidth) {
      if (text != null && !text.isEmpty() && !(maxWidth <= 0.0F)) {
         String fitted = this.init_Str_Str(text, size, maxWidth);
         return this.init(fitted, size) <= maxWidth ? fitted : "";
      } else {
         return "";
      }
   }

   public int init(String text, float x, float yTop, float size, float[] out, int offset) {
      float cursor = x;
      float baseline = yTop + (float)(-this.load_D * size);
      int quads = 0;
      int i = 0;

      while (i < text.length()) {
         int cp = text.codePointAt(i);
         StreamUtil.Inner_StreamUtil g = this.init_Int2ObjectOpenHash.get(cp);
         if (g == null) {
            g = this.init_StreamUtil_StreamU;
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

   public Identifier init_Identifier() {
      return this.init_Identifier;
   }

   public StreamUtil.Inner_DataUtil init_StreamUtil_DataUti() {
      if (this.init_StreamUtil_DataUti == null && !this.init_Z) {
         try {
            this.init_StreamUtil_DataUti = new StreamUtil.Inner_DataUtil(this.init_NativeImage != null ? this.init_NativeImage : new NativeImage(1, 1, false));
            Minecraft.getInstance().getTextureManager().register(this.init_Identifier, this.init_StreamUtil_DataUti);
         } catch (Throwable t) {
            this.init_Z = true;
         } finally {
            if (this.init_NativeImage != null) {
               this.init_NativeImage.close();
               this.init_NativeImage = null;
            }
         }
      }

      return this.init_StreamUtil_DataUti;
   }

   public void init_V() {
      if (this.init_StreamUtil_DataUti != null) {
         try {
            Minecraft.getInstance().getTextureManager().release(this.init_Identifier);
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
