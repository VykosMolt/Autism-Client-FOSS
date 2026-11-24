package autismclient.util.g;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PathHelper {
   private PathHelper() {
   }

   public static DataUtil init(FieldCache profile, Set<String> accountIds) {
      if (profile == null) {
         return DataUtil.init_DataUtil_Collection_Set(List.of(), accountIds);
      }

      List<DataUtil.Inner_DataUtil> accounts = new ArrayList<>(profile.init_List.size());

      for (FieldCache.Inner_BufferHelper spec : profile.init_List) {
         accounts.add(new DataUtil.Inner_DataUtil(spec.init_Str(), spec.load_Str(), spec.read()));
      }

      List<DataUtil.Inner_DataUtil> selectedAccounts = DataUtil.init_DataUtil_Collection_Set(accounts, accountIds).init_List();
      Map<String, Map<String, String>> values = new LinkedHashMap<>();

      for (DataUtil.Inner_DataUtil account : selectedAccounts) {
         values.put(account.init(), profile.init_Map_Str(account.init()));
      }

      DataUtil.Inner_StreamUtil loginMode = profile.init_FieldCache_DataUti == null
         ? DataUtil.Inner_StreamUtil.load
         : DataUtil.Inner_StreamUtil.valueOf(profile.init_FieldCache_DataUti.name());
      return new DataUtil(selectedAccounts, values, loginMode, profile.sync);
   }

   public static FieldCache init_FieldCache(DataUtil draft, FieldCache base) {
      FieldCache result = new FieldCache(base);
      init_V(draft, result);
      return result;
   }

   public static MethodUtil init(FieldCache profile) {
      if (profile == null) {
         return new MethodUtil(List.of(), DataUtil.Inner_StreamUtil.load, "");
      }

      List<DataUtil.Inner_DataUtil> accounts = new ArrayList<>(profile.init_List.size());

      for (FieldCache.Inner_BufferHelper spec : profile.init_List) {
         accounts.add(new DataUtil.Inner_DataUtil(spec.init_Str(), spec.load_Str(), spec.read()));
      }

      DataUtil.Inner_StreamUtil loginMode = profile.init_FieldCache_DataUti == null
         ? DataUtil.Inner_StreamUtil.load
         : DataUtil.Inner_StreamUtil.valueOf(profile.init_FieldCache_DataUti.name());
      return new MethodUtil(accounts, loginMode, profile.sync);
   }

   public static KeyHolder init(FieldCache profile, Map<String, String> accountNames) {
      return new KeyHolder(init(profile), accountNames);
   }

   private static void init_V(DataUtil draft, FieldCache target) {
      if (draft != null && target != null) {
         target.init_FieldCache_DataUti = FieldCache.Inner_DataUtil.valueOf(draft.init_DataUtil_StreamUti().name());
         target.sync = draft.init_Str();

         for (DataUtil.Inner_DataUtil account : draft.init_List()) {
            String accountId = account.init();
            Map<String, String> desired = draft.init_Map_Str(accountId);

            for (String name : target.init_Set_Str(accountId)) {
               if (!desired.containsKey(name)) {
                  target.init_V(accountId, name);
               }
            }

            desired.forEach((namex, value) -> target.init(accountId, namex, value));
         }

         target.init_V();
      }
   }
}
