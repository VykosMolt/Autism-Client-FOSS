package autismclient.util.g;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record MethodUtil(List<DataUtil.Inner_DataUtil> init_List, DataUtil.Inner_StreamUtil init_DataUtil_StreamUti, String init_Str) {
   public MethodUtil {
      init_List = init_List == null ? List.of() : List.copyOf(new ArrayList<>(init_List));
      init_DataUtil_StreamUti = init_DataUtil_StreamUti == null ? DataUtil.Inner_StreamUtil.load : init_DataUtil_StreamUti;
      init_Str = init_Str == null ? "" : init_Str.trim();
   }

   public DataUtil init(Set<String> accountIds) {
      return DataUtil.init_DataUtil_MethodUtil_Set(this, accountIds);
   }
}
