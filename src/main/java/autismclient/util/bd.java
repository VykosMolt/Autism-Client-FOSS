package autismclient.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record bd(List<bd.Inner_DataUtil> init_List, Map<String, String> init_Map) {
   public bd {
      init_List = init_List == null ? List.of() : List.copyOf(new ArrayList<>(init_List));
      Map<String, String> names = new LinkedHashMap<>();
      if (init_Map != null) {
         init_Map.forEach((accountId, name) -> {
            String id = accountId == null ? "" : accountId.trim();
            String displayName = name == null ? "" : name.trim();
            if (!id.isEmpty() && !displayName.isEmpty()) {
               names.put(id, displayName);
            }
         });
      }

      init_Map = Map.copyOf(names);
   }

   public String init(String accountId) {
      return accountId == null ? "" : this.init_Map.getOrDefault(accountId, accountId);
   }

   public record Inner_DataUtil(String init, String load, String read) {
      public Inner_DataUtil {
         init = init == null ? "" : init.trim();
         load = load == null ? "" : load.trim();
         read = read == null ? "" : read.trim();
      }
   }
}
