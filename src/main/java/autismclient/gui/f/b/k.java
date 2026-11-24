package autismclient.gui.f.b;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class k {
   private static final int init_I = 2048;
   private static final Map<k.Inner_DataUtil, String> init_Map = Collections.synchronizedMap(new LinkedHashMap<k.Inner_DataUtil, String>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<k.Inner_DataUtil, String> eldest) {
         return this.size() > 2048;
      }
   });
   private static volatile int load;

   public static int init_I() {
      return load;
   }

   private k() {
   }

   public static MutableComponent init(String value, Identifier fontId, int color) {
      return Component.literal(value == null ? "" : value).setStyle(Style.EMPTY.withColor(color));
   }

   public static int init(Font renderer, String value, Identifier fontId, int color) {
      String safe = value == null ? "" : value;
      autismclient.gui.e.ConfigStore face = autismclient.gui.e.ConfigStore.init(fontId);
      return autismclient.gui.e.SyncManager.init(face, safe)
         ? autismclient.gui.e.SyncManager.init_I(face, safe, autismclient.gui.e.SyncManager.init_F_ConfigStore(face))
         : renderer.width(safe);
   }

   public static int init(Font renderer, String value, String fragment, Identifier fontId, int color) {
      String safe = fragment == null ? "" : fragment;
      autismclient.gui.e.ConfigStore face = autismclient.gui.e.ConfigStore.init(fontId);
      return autismclient.gui.e.SyncManager.init(face, value == null ? "" : value)
         ? autismclient.gui.e.SyncManager.init_I(face, safe, autismclient.gui.e.SyncManager.init_F_ConfigStore(face))
         : renderer.width(safe);
   }

   public static int init(Identifier fontId) {
      return 9;
   }

   public static String init(Font renderer, String value, int maxWidth, Identifier fontId, int color) {
      String safeValue = value == null ? "" : value;
      k.Inner_DataUtil key = new k.Inner_DataUtil(safeValue, maxWidth, fontId, autismclient.gui.e.SyncManager.init_I());
      String cached = init_Map.get(key);
      if (cached != null) {
         return cached;
      }

      if (safeValue.isEmpty()) {
         return "";
      }

      int allowedWidth = maxWidth + 4;
      if (init(renderer, safeValue, fontId, color) <= allowedWidth) {
         init_Map.put(key, safeValue);
         return safeValue;
      }

      autismclient.gui.e.ConfigStore face = autismclient.gui.e.ConfigStore.init(fontId);
      String trimmed;
      if (autismclient.gui.e.SyncManager.init(face, safeValue)) {
         trimmed = autismclient.gui.e.SyncManager.init(face, safeValue, autismclient.gui.e.SyncManager.init_F_ConfigStore(face), allowedWidth);
      } else {
         trimmed = renderer.plainSubstrByWidth(safeValue, Math.max(0, allowedWidth));
      }

      init_Map.put(key, trimmed);
      return trimmed;
   }

   public static String load(Font renderer, String value, int maxWidth, Identifier fontId, int color) {
      String safeValue = value == null ? "" : value;
      if (!safeValue.isEmpty() && maxWidth > 0) {
         if (init(renderer, safeValue, fontId, color) <= maxWidth) {
            return safeValue;
         }

         autismclient.gui.e.ConfigStore face = autismclient.gui.e.ConfigStore.init(fontId);
         if (autismclient.gui.e.SyncManager.init(face, safeValue)) {
            return autismclient.gui.e.SyncManager.load(face, safeValue, autismclient.gui.e.SyncManager.init_F_ConfigStore(face), maxWidth);
         }

         String ellipsis = "...";
         int ellipsisWidth = init(renderer, ellipsis, fontId, color);
         if (maxWidth <= ellipsisWidth) {
            return init(renderer, safeValue, maxWidth, fontId, color);
         }

         String base = renderer.plainSubstrByWidth(safeValue, Math.max(0, maxWidth - ellipsisWidth));

         while (!base.isEmpty() && init(renderer, base + ellipsis, fontId, color) > maxWidth) {
            base = base.substring(0, base.length() - 1);
         }

         return base.isEmpty() ? ellipsis : base + ellipsis;
      } else {
         return "";
      }
   }

   public static void init(GuiGraphicsExtractor context, Font renderer, String value, Identifier fontId, int color, int x, int y, boolean shadow) {
      String safe = value == null ? "" : value;
      autismclient.gui.e.ConfigStore face = autismclient.gui.e.ConfigStore.init(fontId);
      if (autismclient.gui.e.SyncManager.init(face, safe)) {
         float size = autismclient.gui.e.SyncManager.init_F_ConfigStore(face);
         autismclient.gui.e.SyncManager.init(context, face, safe, x, autismclient.gui.e.SyncManager.init(face, y, size), size, color, shadow);
      } else {
         context.text(renderer, safe, x, y, color, shadow);
      }
   }

   public static void init(GuiGraphicsExtractor context, Font renderer, String value, Identifier fontId, int color, int x, int y, int maxWidth, boolean shadow) {
      String safe = value == null ? "" : value;
      if (!safe.isEmpty() && maxWidth > 0) {
         int measured = init(renderer, safe, fontId, color);
         if (measured <= maxWidth) {
            init(context, renderer, safe, fontId, color, x, y, shadow);
         } else {
            float scale = Math.min(1.0F, (float)maxWidth / Math.max(1, measured));
            context.pose().pushMatrix();

            try {
               context.pose().scale(scale, scale);
               init(context, renderer, safe, fontId, color, Math.round(x / scale), Math.round(y / scale), shadow);
            } finally {
               context.pose().popMatrix();
            }
         }
      }
   }

   public static void load(GuiGraphicsExtractor context, Font renderer, String value, Identifier fontId, int color, int x, int y, int maxWidth, boolean shadow) {
      String safe = load(renderer, value, maxWidth, fontId, color);
      if (!safe.isEmpty()) {
         init(context, renderer, safe, fontId, color, x, y, shadow);
      }
   }

   public static void init(GuiGraphicsExtractor context, int x0, int y0, int x1, int y1, int color) {
      context.fill(x0, y0, x1, y1, color);
   }

   public static void init_V() {
      init_Map.clear();
      load++;
      autismclient.gui.e.SyncManager.init_V();
   }

   private record Inner_DataUtil(String init_Str, int init_I, Identifier init_Identifier, int load) {
   }
}
