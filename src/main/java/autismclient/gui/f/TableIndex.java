package autismclient.gui.f;

public final class TableIndex {
   private final JsonUtil init_JsonUtil = new JsonUtil();
   private final IoWorker init_IoWorker = new IoWorker();
   private final BitSetUtil init_BitSetUtil = new BitSetUtil();

   public JsonUtil init_JsonUtil() {
      return this.init_JsonUtil;
   }

   public void init_V() {
      this.init_JsonUtil.init();
   }

   public IoWorker init_IoWorker() {
      return this.init_IoWorker;
   }

   public BitSetUtil init_BitSetUtil() {
      return this.init_BitSetUtil;
   }
}
