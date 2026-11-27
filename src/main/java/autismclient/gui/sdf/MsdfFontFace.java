package autismclient.gui.sdf;

import net.minecraft.resources.Identifier;

public enum MsdfFontFace {
   BODY("onest_regular", 8.5F),
   LABEL("onest_medium", 8.5F),
   TITLE("onest_bold", 10.0F);

   private final String init_Str;
   private final float init_F;
   private volatile MsdfFontAtlas init_StreamUtil;
   private volatile boolean init_Z;

   MsdfFontFace(String atlasName, float defaultSize) {
      this.init_Str = atlasName;
      this.init_F = defaultSize;
   }

   public float baseScale() {
      return this.init_F;
   }

   public MsdfFontAtlas atlas() {
      MsdfFontAtlas font = this.init_StreamUtil;
      if (font == null && !this.init_Z) {
         synchronized (this) {
            font = this.init_StreamUtil;
            if (font == null && !this.init_Z) {
               font = MsdfFontAtlas.load(this.init_Str);
               if (font == null) {
                  this.init_Z = true;
               } else {
                  this.init_StreamUtil = font;
               }
            }
         }
      }

      return font == null && this != BODY ? BODY.atlas() : font;
   }

   public static MsdfFontFace init(Identifier fontId) {
      if (autismclient.gui.f.a.DataUtil.read.equals(fontId)) {
         return TITLE;
      } else {
         return autismclient.gui.f.a.DataUtil.load.equals(fontId) ? LABEL : BODY;
      }
   }

   public static void prewarm() {
      for (MsdfFontFace font : values()) {
         font.atlas();
      }
   }

   public static void releaseAll() {
      for (MsdfFontFace font : values()) {
         MsdfFontAtlas old = font.init_StreamUtil;
         if (old != null) {
            old.release();
         }

         font.init_StreamUtil = null;
         font.init_Z = false;
      }
   }
}
