package autismclient.util.mm;

public final class JsonUtil {
   public final JsonUtil.Inner_DataUtil init_JsonUtil_DataUtil;
   public final String init_Str;
   public final String load_Str;
   public final String read;
   public final long init_J;
   public final boolean init_Z;
   public final boolean load_Z;
   public final autismclient.util.mm.b.DataUtil.NetHelper init_DataUtil_NetHelper;
   public final autismclient.util.mm.b.DataUtil.CachePool init_DataUtil_CachePool;
   public final autismclient.util.mm.b.DataUtil.StateCache init_DataUtil_StateCach;
   public final autismclient.util.mm.b.DataUtil.Inner_DataUtil init_DataUtil_DataUtil;

   public JsonUtil(String senderFpHex, String senderName, String text, boolean self, boolean system) {
      this(system ? JsonUtil.Inner_DataUtil.load : JsonUtil.Inner_DataUtil.init, senderFpHex, senderName, text, self, system, null, null, null, null);
   }

   private JsonUtil(
      JsonUtil.Inner_DataUtil kind,
      String senderFpHex,
      String senderName,
      String text,
      boolean self,
      boolean system,
      autismclient.util.mm.b.DataUtil.NetHelper macro,
      autismclient.util.mm.b.DataUtil.CachePool command,
      autismclient.util.mm.b.DataUtil.StateCache packet,
      autismclient.util.mm.b.DataUtil.Inner_DataUtil blob
   ) {
      this.init_JsonUtil_DataUtil = kind;
      this.init_Str = senderFpHex == null ? "" : senderFpHex;
      this.load_Str = senderName == null ? "" : senderName;
      this.read = text == null ? "" : text;
      this.init_Z = self;
      this.load_Z = system;
      this.init_DataUtil_NetHelper = macro;
      this.init_DataUtil_CachePool = command;
      this.init_DataUtil_StateCach = packet;
      this.init_DataUtil_DataUtil = blob;
      this.init_J = System.currentTimeMillis();
   }

   public static JsonUtil init(String text) {
      return new JsonUtil("", "", text, false, true);
   }

   public static JsonUtil init(String fpHex, String name, boolean self, autismclient.util.mm.b.DataUtil.NetHelper m) {
      return new JsonUtil(JsonUtil.Inner_DataUtil.read, fpHex, name, "", self, false, m, null, null, null);
   }

   public static JsonUtil init(String fpHex, String name, boolean self, autismclient.util.mm.b.DataUtil.StateCache p) {
      return new JsonUtil(JsonUtil.Inner_DataUtil.write, fpHex, name, "", self, false, null, null, p, null);
   }

   public static JsonUtil init(String fpHex, String name, boolean self, autismclient.util.mm.b.DataUtil.CachePool c) {
      return new JsonUtil(JsonUtil.Inner_DataUtil.update, fpHex, name, "", self, false, null, c, null, null);
   }

   public static JsonUtil init(String fpHex, String name, boolean self, autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      return new JsonUtil(JsonUtil.Inner_DataUtil.sync, fpHex, name, "", self, false, null, null, null, b);
   }

   public boolean init_Z() {
      return this.init_JsonUtil_DataUtil == JsonUtil.Inner_DataUtil.read
         || this.init_JsonUtil_DataUtil == JsonUtil.Inner_DataUtil.write
         || this.init_JsonUtil_DataUtil == JsonUtil.Inner_DataUtil.update
         || this.init_JsonUtil_DataUtil == JsonUtil.Inner_DataUtil.sync;
   }

   public String init_Str() {
      switch (this.init_JsonUtil_DataUtil) {
         case read:
            if (this.init_DataUtil_NetHelper.init_I == 1 && !this.init_DataUtil_NetHelper.load.isBlank()) {
               return this.init_DataUtil_NetHelper.load;
            }

            return this.init_DataUtil_NetHelper.init_I + (this.init_DataUtil_NetHelper.init_I == 1 ? " action" : " actions");
         case write:
            return this.init_DataUtil_StateCach.init;
         case update:
            return this.init_DataUtil_CachePool.init_Str;
         case sync:
            return this.init_DataUtil_DataUtil.load;
         default:
            return this.read;
      }
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync;
   }
}
