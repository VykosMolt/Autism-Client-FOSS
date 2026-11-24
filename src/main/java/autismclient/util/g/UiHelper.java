package autismclient.util.g;

import autismclient.util.macro.a;
import autismclient.util.macro.ar;
import java.util.Locale;
import java.util.Map;

final class UiHelper {
   private UiHelper() {
   }

   static boolean init(a c, EventBus h, Map<String, String> vars) {
      if (c == null) {
         return true;
      }

      boolean result = c.init_a_BufferHelper == a.Inner_BufferHelper.init ? load(c, h, vars) : read(c, h, vars);
      return c.init_Z != result;
   }

   private static boolean load(a c, EventBus h, Map<String, String> vars) {
      if (c.init_List.isEmpty()) {
         return true;
      }

      if (c.init_a_StreamUtil == a.Inner_StreamUtil.load) {
         for (a child : c.init_List) {
            if (init(child, h, vars)) {
               return true;
            }
         }

         return false;
      } else {
         for (a child : c.init_List) {
            if (!init(child, h, vars)) {
               return false;
            }
         }

         return true;
      }
   }

   private static boolean read(a c, EventBus h, Map<String, String> vars) {
      String item = c.init_Str == null ? "" : c.init_Str;

      try {
         boolean var10000;
         switch (c.init_a_CachePool) {
            case init:
               var10000 = true;
               break;
            case load:
               var10000 = init(h.sync_Str(), item, c.init_a_ConfigStore);
               break;
            case read:
               var10000 = h.init_I_Str(item) > 0;
               break;
            case write:
               var10000 = init(h.init_I_Str(item), c.init_I, c.init_a_DataUtil);
               break;
            case update:
               var10000 = !h.init_Z_I(c.load_I);
               break;
            case sync:
               var10000 = h.init_Z_I(c.load_I);
               break;
            case flush:
               var10000 = h.read_I() <= 0;
               break;
            case close:
               var10000 = h.init_I_Str("") <= 0;
               break;
            case open:
               var10000 = h.sync_Z();
               break;
            case poll:
               var10000 = !h.sync_Z();
               break;
            case push:
               var10000 = init(h.close_Str(), item, c.init_a_ConfigStore);
               break;
            case pop:
               var10000 = init(h.load_I(), c.load_I, c.init_a_DataUtil);
               break;
            case tick:
               var10000 = init(h.read_I(), c.init_I, c.init_a_DataUtil);
               break;
            case get:
               var10000 = !item.isBlank() && !item.equalsIgnoreCase("ANY") ? init(h.flush_Str(), item, c.init_a_ConfigStore) : h.update_Z();
               break;
            case data:
               var10000 = init(h.init_F(), c.init_I, c.init_a_DataUtil);
               break;
            case next:
               var10000 = h.init_Z();
               break;
            case value:
               var10000 = init(c, vars);
               break;
            case clear:
            case start:
            case set:
            case put:
               var10000 = h.read_Z() && h.read_I_Str(item) >= 0;
               break;
            case run:
               var10000 = init(c, h);
               break;
            case prev:
               var10000 = h.init_I_Str("bundle") > 0;
               break;
            case size:
               var10000 = h.init_I_Str("writable_book") > 0;
               break;
            case reset:
            case add:
            case valid:
            case stop:
            case cache:
               var10000 = false;
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } catch (RuntimeException ignored) {
         return false;
      }
   }

   private static boolean init(a c, EventBus h) {
      ar w = new ar();
      w.targetMode = c.init_ar_CachePool;
      w.itemName = c.init_Str == null ? "" : c.init_Str;
      w.slot = c.load_I;
      w.measurement = c.init_ar_StreamUtil;
      w.comparison = init(c.init_a_DataUtil);
      w.value = c.init_I;
      return TextFormat.init_Z_ar_EventBus(w, h);
   }

   private static ar.Inner_DataUtil init(a.Inner_DataUtil cmp) {
      ar.Inner_DataUtil var10000;
      switch (cmp == null ? a.Inner_DataUtil.write : cmp) {
         case init:
            var10000 = ar.Inner_DataUtil.init;
            break;
         case load:
            var10000 = ar.Inner_DataUtil.load;
            break;
         case read:
            var10000 = ar.Inner_DataUtil.read;
            break;
         case write:
            var10000 = ar.Inner_DataUtil.write;
            break;
         case update:
            var10000 = ar.Inner_DataUtil.update;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static boolean init(double value, double amount, a.Inner_DataUtil cmp) {
      boolean var10000;
      switch (cmp == null ? a.Inner_DataUtil.write : cmp) {
         case init:
            var10000 = value < amount;
            break;
         case load:
            var10000 = value <= amount;
            break;
         case read:
            var10000 = value == amount;
            break;
         case write:
            var10000 = value >= amount;
            break;
         case update:
            var10000 = value > amount;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static boolean init(String actual, String expected, a.Inner_ConfigStore op) {
      String a = load(actual);
      String e = load(init(expected));
      boolean var10000;
      switch (op == null ? autismclient.util.macro.a.Inner_ConfigStore.init : op) {
         case load: {
            boolean var14 = !a.equals(e);
            boolean var15 = var14;
            var10000 = var15;
            break;
         }
         case flush: {
            boolean var13 = a.contains(e);
            boolean var14 = var13;
            var10000 = var14;
            break;
         }
         case close: {
            boolean var12 = a.startsWith(e);
            boolean var13 = var12;
            var10000 = var13;
            break;
         }
         case open: {
            boolean var11 = a.endsWith(e);
            boolean var12 = var11;
            var10000 = var12;
            break;
         }
         case push: {
            boolean var10 = a.isEmpty();
            boolean var11 = var10;
            var10000 = var11;
            break;
         }
         case poll: {
            boolean var9;
            try {
               var9 = a.matches(expected == null ? "" : expected);
            } catch (RuntimeException ex) {
               var9 = false;
               boolean var9x = var9;
               var10000 = var9x;
               break;
            }

            boolean var10 = var9;
            var10000 = var10;
            break;
         }
         default:
            boolean var5 = a.equals(e) || !e.isEmpty() && a.contains(e);
            boolean var5x = var5;
            var10000 = var5x;
      }

      return var10000;
   }

   private static boolean init(a c, Map<String, String> vars) {
      String left = vars == null ? "" : vars.getOrDefault(read(c.init_Str), "");
      String right = c.load_Str == null ? "" : c.load_Str;
      boolean var10000;
      switch (c.init_a_ConfigStore == null ? a.Inner_ConfigStore.init : c.init_a_ConfigStore) {
         case load: {
            boolean var15 = !left.equals(right);
            boolean var16 = var15;
            var10000 = var16;
            break;
         }
         case flush: {
            boolean var14 = left.contains(right);
            boolean var15 = var14;
            var10000 = var15;
            break;
         }
         case close: {
            boolean var13 = left.startsWith(right);
            boolean var14 = var13;
            var10000 = var14;
            break;
         }
         case open: {
            boolean var12 = left.endsWith(right);
            boolean var13 = var12;
            var10000 = var13;
            break;
         }
         case push: {
            boolean var11 = left.isEmpty();
            boolean var12 = var11;
            var10000 = var12;
            break;
         }
         case poll: {
            boolean var10;
            try {
               var10 = left.matches(right);
            } catch (RuntimeException ex) {
               var10 = false;
               boolean var10x = var10;
               var10000 = var10x;
               break;
            }

            boolean var11 = var10;
            var10000 = var11;
            break;
         }
         case pop: {
            boolean var8 = left.equalsIgnoreCase("true") || left.equals("1");
            boolean var9 = var8;
            var10000 = var9;
            break;
         }
         case read:
         case write:
         case update:
         case sync: {
            boolean var7 = load(left, right, c.init_a_ConfigStore);
            boolean var8 = var7;
            var10000 = var8;
            break;
         }
         case init:
            boolean var4 = left.equals(right);
            boolean var4x = var4;
            var10000 = var4x;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static boolean load(String left, String right, a.Inner_ConfigStore op) {
      try {
         double l = Double.parseDouble(left.trim());
         double r = Double.parseDouble(right.trim());
         boolean var10000;
         switch (op) {
            case read:
               var10000 = l < r;
               break;
            case write:
               var10000 = l <= r;
               break;
            case update:
               var10000 = l > r;
               break;
            case sync:
               var10000 = l >= r;
               break;
            default:
               var10000 = false;
         }

         return var10000;
      } catch (NumberFormatException ex) {
         return false;
      }
   }

   private static String init(String s) {
      if (s == null) {
         return "";
      }

      int colon = s.indexOf(58);
      return colon >= 0 ? s.substring(colon + 1) : s;
   }

   private static String load(String s) {
      return s == null ? "" : s.toLowerCase(Locale.ROOT).replace('_', ' ').trim();
   }

   private static String read(String s) {
      if (s == null) {
         return "";
      }

      String t = s.trim();
      return t.startsWith("{") && t.endsWith("}") && t.length() >= 2 ? t.substring(1, t.length() - 1) : t;
   }
}
