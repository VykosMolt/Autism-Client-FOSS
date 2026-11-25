package autismclient.util.mm.guardian.impl;

import autismclient.e.DataUtil;
import autismclient.util.L;
import autismclient.util.mm.a.StreamUtil;
import autismclient.util.mm.guardian.Guardian;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.lang.management.ManagementFactory;
import java.security.CodeSource;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;

final class InjectionScan {
   private static final Class<?>[] CORE = new Class[]{L.class, StreamUtil.class, Guardian.class};

   private InjectionScan() {
   }

   static JsonObject evidence() {
      JsonObject ev = new JsonObject();

      try {
         JsonArray agents = new JsonArray();

         for (String a : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            String low = a.toLowerCase(Locale.ROOT);
            if (low.startsWith(
                  DataUtil.init(
                     new int[]{
                        -971950303, -971950324, -65688214, -2036337821, 1298033975, 1584347778, 1870143971, 2139885219, 1911933350, -859607349, -878025104
                     }
                  )
               )
               || low.startsWith(
                  DataUtil.init(
                     new int[]{
                        -971950303, -971950324, -65688223, -2036337819, 1298033956, 1584347789, 1870143990, 2139885236, 1911933346, -859607343, -878025108
                     }
                  )
               )
               || low.startsWith(
                  DataUtil.init(
                     new int[]{-971950301, -971950322, -65688225, -2036335901, 1298093466, 1586194827, 1927402164, -380074710, 1102586856, -179548987}
                  )
               )
               || low.startsWith(
                  DataUtil.init(
                     new int[]{
                        -971950301,
                        -971950322,
                        -65688250,
                        -2036335898,
                        1298093456,
                        1586194826,
                        1927402164,
                        -380074715,
                        1102586861,
                        -179548986,
                        -1271052239,
                        -747913093,
                        -1710472845,
                        -1485054200,
                        1207963056,
                        -1207850334
                     }
                  )
               )) {
               agents.add(a.length() > 120 ? a.substring(0, 120) : a);
            }
         }

         ev.add(DataUtil.init(new int[]{-971950299, -971950268, -65688293, -2036333981, 1298153171, 1588041875, 1984660365}), agents);
      } catch (Throwable var10) {
      }

      try {
         String self = codeSource(CORE[0]);
         boolean mismatch = false;

         for (Class<?> c : CORE) {
            String cs = codeSource(c);
            if (self != null && cs != null && !self.equals(cs)) {
               mismatch = true;
               break;
            }
         }

         ev.addProperty(
            DataUtil.init(
               new int[]{-971950299, -971950266, -65688305, -2036334005, 1298153172, 1588041876, 1984660371, 1394932649, 293240139, 500509386, -1664079128}
            ),
            mismatch
         );
      } catch (Throwable var9) {
      }

      try {
         ev.addProperty(DataUtil.init(new int[]{-971950297, -971950262, -65688107, -2036332052, 1298212616}), FabricLoader.getInstance().getAllMods().size());
      } catch (Throwable var8) {
      }

      return ev;
   }

   private static String codeSource(Class<?> c) {
      try {
         CodeSource src = c.getProtectionDomain().getCodeSource();
         return src != null && src.getLocation() != null ? src.getLocation().toString() : null;
      } catch (Throwable t) {
         return null;
      }
   }
}
