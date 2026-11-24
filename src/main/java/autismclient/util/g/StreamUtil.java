package autismclient.util.g;

import autismclient.util.bd;
import java.util.ArrayList;
import java.util.List;

public final class StreamUtil {
   private StreamUtil() {
   }

   public static bd init() {
      PacketUtil manager = PacketUtil.load_PacketUtil();
      if (manager == null) {
         return new bd(List.of(), null);
      }

      KeyHolder snapshot = manager.init_KeyHolder();
      List<bd.Inner_DataUtil> accounts = new ArrayList<>();

      for (DataUtil.Inner_DataUtil account : snapshot.init_List()) {
         accounts.add(new bd.Inner_DataUtil(account.init(), account.load(), account.read()));
      }

      return new bd(accounts, snapshot.init_Map());
   }
}
