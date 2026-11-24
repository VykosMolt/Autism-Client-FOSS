package autismclient.util.g;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record KeyHolder(List<DataUtil.Inner_DataUtil> init_List, Map<String, String> init_Map) {
   public KeyHolder {
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

   public KeyHolder(MethodUtil profile, Map<String, String> accountNames) {
      this(profile == null ? List.of() : profile.init_List(), accountNames);
   }

   public String init(String accountId) {
      return accountId == null ? "" : this.init_Map.getOrDefault(accountId, accountId);
   }
}
