package autismclient.util.mm;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;

public final class PacketUtil {
   public final long init_J;
   public final int init_I;
   public final int load_I;
   public final long load_J;
   private final String init_Str;
   private final String load_Str;
   private final String read;

   private PacketUtil(long rv, int ke, int cke, long ts, String nB64, String ctB64, String sigB64) {
      this.init_J = rv;
      this.init_I = ke;
      this.load_I = cke;
      this.load_J = ts;
      this.init_Str = nB64;
      this.load_Str = ctB64;
      this.read = sigB64;
   }

   public static PacketUtil init_PacketUtil_BArr(byte[] payload) {
      try {
         JsonObject o = JsonParser.parseString(new String(payload, StandardCharsets.UTF_8)).getAsJsonObject();
         return o.get("v").getAsInt() != 1
            ? null
            : new PacketUtil(
               o.get("rv").getAsLong(),
               o.get("ke").getAsInt(),
               o.get("cke").getAsInt(),
               o.get("ts").getAsLong(),
               o.get("n").getAsString(),
               o.get("ct").getAsString(),
               o.get("sig").getAsString()
            );
      } catch (Throwable t) {
         return null;
      }
   }

   public boolean init_Z_PublicKey_Str(PublicKey serverKey, String lobbyTopic) {
      try {
         byte[] msg = (this.init_Str_Str(lobbyTopic) + "|" + this.load_Str).getBytes(StandardCharsets.UTF_8);
         return autismclient.util.mm.a.StreamUtil.init_Z_PublicKey(serverKey, msg, Base64.getDecoder().decode(this.read));
      } catch (Throwable t) {
         return false;
      }
   }

   public PacketUtil.Inner_DataUtil init_PacketUtil_DataUti_BArr_Str(byte[] key32, String lobbyTopic) {
      try {
         byte[] pt = autismclient.util.mm.a.StreamUtil.load(
            key32,
            Base64.getDecoder().decode(this.init_Str),
            Base64.getDecoder().decode(this.load_Str),
            this.init_Str_Str(lobbyTopic).getBytes(StandardCharsets.UTF_8)
         );
         if (pt == null) {
            return null;
         }

         JsonObject o = JsonParser.parseString(new String(pt, StandardCharsets.UTF_8)).getAsJsonObject();
         return new PacketUtil.Inner_DataUtil(
            init_Str_JsonObject_Str(o, "t"),
            init_Str_JsonObject_Str(o, "did"),
            init_Str_JsonObject_Str(o, "fp"),
            init_Str_JsonObject_Str(o, "name"),
            o.has("term") ? o.get("term").getAsInt() : 0,
            init_Str_JsonObject_Str(o, "hostDid"),
            init_Str_JsonObject_Str(o, "hostFp"),
            init_Z_JsonObject_Str(o, "admin"),
            init_Z_JsonObject_Str(o, "speaker"),
            init_Z_JsonObject_Str(o, "allowed"),
            o.has("speakerVersion") ? o.get("speakerVersion").getAsInt() : 0,
            init_Str_JsonObject_Str(o, "contentTopic"),
            init_Str_JsonObject_Str(o, "controlTopic"),
            init_Str_JsonObject_Str(o, "role")
         );
      } catch (Throwable t) {
         return null;
      }
   }

   private String init_Str_Str(String lobbyTopic) {
      return "mmsys|1|" + lobbyTopic + "|" + this.init_J + "|" + this.init_I + "|" + this.load_I + "|" + this.load_J + "|" + this.init_Str;
   }

   private static String init_Str_JsonObject_Str(JsonObject o, String k) {
      try {
         return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : "";
      } catch (Throwable t) {
         return "";
      }
   }

   private static boolean init_Z_JsonObject_Str(JsonObject o, String k) {
      try {
         return o.has(k) && !o.get(k).isJsonNull() && o.get(k).getAsBoolean();
      } catch (Throwable t) {
         return false;
      }
   }

   public record Inner_DataUtil(
      String init_Str,
      String load_Str,
      String read_Str,
      String write,
      int init_I,
      String update,
      String sync,
      boolean init_Z,
      boolean load_Z,
      boolean read_Z,
      int load_I,
      String flush,
      String close,
      String open
   ) {
   }
}
