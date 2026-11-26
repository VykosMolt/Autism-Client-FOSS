package autismclient.gui.e;

import net.minecraft.resources.Identifier;

public enum ConfigStore {
   BODY("onest_regular", 8.5F),
   LABEL("onest_medium", 8.5F),
   TITLE("onest_bold", 10.0F);

   private final String init_Str;
   private final float init_F;
   private volatile StreamUtil init_StreamUtil;
   private volatile boolean init_Z;

   ConfigStore(String atlasName, float defaultSize) {
      this.init_Str = atlasName;
      this.init_F = defaultSize;
   }

   public float init_F() {
      return this.init_F;
   }

   public StreamUtil init_StreamUtil() {
      StreamUtil font = this.init_StreamUtil;
      if (font == null && !this.init_Z) {
         synchronized (this) {
            font = this.init_StreamUtil;
            if (font == null && !this.init_Z) {
               font = StreamUtil.init_StreamUtil_Str(this.init_Str);
               if (font == null) {
                  this.init_Z = true;
               } else {
                  this.init_StreamUtil = font;
               }
            }
         }
      }

      return font == null && this != BODY ? BODY.init_StreamUtil() : font;
   }

   public static ConfigStore init(Identifier fontId) {
      if (autismclient.gui.f.a.DataUtil.read.equals(fontId)) {
         return TITLE;
      } else {
         return autismclient.gui.f.a.DataUtil.load.equals(fontId) ? LABEL : BODY;
      }
   }

   public static void prewarm() {
      for (ConfigStore font : values()) {
         font.init_StreamUtil();
      }
   }

   public static void init_V() {
      for (ConfigStore font : values()) {
         StreamUtil old = font.init_StreamUtil;
         if (old != null) {
            old.init_V();
         }

         font.init_StreamUtil = null;
         font.init_Z = false;
      }
   }
}
