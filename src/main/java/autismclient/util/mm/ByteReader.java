package autismclient.util.mm;

import autismclient.util.AutismPacketLoggerOverlay;
import autismclient.util.aM;
import autismclient.util.aN;
import autismclient.util.bi;
import autismclient.util.ck;
import autismclient.util.o;
import autismclient.util.macro.MacroAction;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.protocol.Packet;

public final class ByteReader {
   private ByteReader() {
   }

   public static autismclient.util.mm.b.DataUtil.NetHelper init(aM macro) {
      if (macro == null) {
         return null;
      }

      aM stripped = macro.init_aM();
      stripped.init_Str = "";
      stripped.load_I = -1;
      String hash = o.init_Str(stripped);
      if (hash == null) {
         return null;
      }

      autismclient.util.mm.b.DataUtil.NetHelper offer = new autismclient.util.mm.b.DataUtil.NetHelper();
      offer.init_Str = "";
      List<MacroAction> actions = macro.init_List;
      offer.init_I = actions == null ? 0 : actions.size();
      if (offer.init_I == 1) {
         MacroAction only = actions.get(0);
         offer.load = only == null ? "" : init(only.getDisplayName());
      }

      offer.read = hash;
      return offer;
   }

   public static autismclient.util.mm.b.DataUtil.StateCache init(List<ck.Inner_CachePool> queue, String friendlyName) {
      if (queue != null && !queue.isEmpty()) {
         String data = o.init_Str(queue);
         if (data == null) {
            return null;
         }

         autismclient.util.mm.b.DataUtil.StateCache offer = new autismclient.util.mm.b.DataUtil.StateCache();
         int n = queue.size();
         offer.init = friendlyName != null && !friendlyName.isBlank() ? friendlyName : n + (n == 1 ? " packet" : " packets");
         offer.load = "C2S";
         offer.read = data;
         return offer;
      } else {
         return null;
      }
   }

   public static List<ck.Inner_CachePool> init_List(autismclient.util.mm.b.DataUtil.StateCache offer) {
      return offer != null && offer.read != null ? o.init_List(offer.read) : null;
   }

   public static int init_I(autismclient.util.mm.b.DataUtil.StateCache offer) {
      List<ck.Inner_CachePool> add = init_List(offer);
      if (add != null && !add.isEmpty()) {
         List<ck.Inner_CachePool> cur = new ArrayList<>(ck.init_ck().load_List());
         cur.addAll(add);
         ck.init_ck().load_V_List(cur);
         return add.size();
      } else {
         return -1;
      }
   }

   public static aM init(autismclient.util.mm.b.DataUtil.NetHelper offer) {
      if (offer == null) {
         return null;
      } else {
         aM macro = o.init_aM(offer.read);
         if (macro == null) {
            bi.init("Could not import macro (corrupt or unsupported).", -2075846);
            return null;
         } else {
            macro.load_I = -1;
            String name = offer.init_Str != null && !offer.init_Str.isBlank() ? offer.init_Str : macro.init_Str;
            aM imported = aN.init_aN().init_aM_aM_Str(macro, name);
            bi.init("Imported macro: " + (imported != null ? imported.init_Str : name), -13248397);
            return imported;
         }
      }
   }

   public static Packet<?> init_Packet(autismclient.util.mm.b.DataUtil.StateCache offer) {
      return offer != null && offer.read != null ? o.init_Packet(offer.read) : null;
   }

   public static AutismPacketLoggerOverlay.Inner_JsonUtil init_AutismPacketLogger(autismclient.util.mm.b.DataUtil.StateCache offer) {
      Packet<?> packet = init_Packet(offer);
      if (packet == null) {
         return null;
      }

      String dir = offer.load != null && !offer.load.isBlank() ? offer.load : "C2S";
      return new AutismPacketLoggerOverlay.Inner_JsonUtil(
         System.currentTimeMillis(),
         0,
         dir,
         offer.init == null ? packet.getClass().getSimpleName() : offer.init,
         packet.getClass(),
         packet,
         false,
         false,
         false,
         null,
         null,
         null,
         null
      );
   }

   private static String init(String s) {
      return s == null ? "" : s;
   }
}
