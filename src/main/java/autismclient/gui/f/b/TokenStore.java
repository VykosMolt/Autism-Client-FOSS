package autismclient.gui.f.b;

public final class TokenStore {
   private TokenStore() {
   }

   public static autismclient.gui.f.NetHelper init(autismclient.gui.f.NetHelper row, int count, int index, int gap) {
      return JsonUtil.init(row, count, index, gap);
   }
}
