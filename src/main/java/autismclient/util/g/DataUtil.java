package autismclient.util.g;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class DataUtil {
   private final List<DataUtil.Inner_DataUtil> init_List;
   private final Map<String, Map<String, String>> init_Map;
   private DataUtil.Inner_StreamUtil init_DataUtil_StreamUti;
   private String init_Str;

   public DataUtil(
      Collection<DataUtil.Inner_DataUtil> accounts,
      Map<String, ? extends Map<String, String>> formValues,
      DataUtil.Inner_StreamUtil loginMode,
      String allMacroName
   ) {
      this.init_List = init_List_Collection_Set(accounts, null);
      this.init_Map = init_Map_Map(formValues);
      this.init_DataUtil_StreamUti = loginMode == null ? DataUtil.Inner_StreamUtil.load : loginMode;
      this.init_Str = allMacroName == null ? "" : allMacroName.trim();
   }

   public DataUtil(DataUtil source) {
      this(
         source == null ? List.of() : source.init_List,
         source == null ? Map.of() : source.init_Map,
         source == null ? DataUtil.Inner_StreamUtil.load : source.init_DataUtil_StreamUti,
         source == null ? "" : source.init_Str
      );
   }

   public static DataUtil init_DataUtil_Collection_Set(Collection<DataUtil.Inner_DataUtil> accounts, Set<String> accountIds) {
      return new DataUtil(init_List_Collection_Set(accounts, accountIds), Map.of(), DataUtil.Inner_StreamUtil.load, "");
   }

   public static DataUtil init(
      Collection<DataUtil.Inner_DataUtil> accounts,
      Set<String> accountIds,
      Map<String, ? extends Map<String, String>> formValues,
      DataUtil.Inner_StreamUtil loginMode,
      String allMacroName
   ) {
      return new DataUtil(init_List_Collection_Set(accounts, accountIds), formValues, loginMode, allMacroName);
   }

   public static DataUtil init_DataUtil_MethodUtil_Set(MethodUtil profile, Set<String> accountIds) {
      return profile == null
         ? init_DataUtil_Collection_Set(List.of(), accountIds)
         : new DataUtil(init_List_Collection_Set(profile.init_List(), accountIds), Map.of(), profile.init_DataUtil_StreamUti(), profile.init_Str());
   }

   public List<DataUtil.Inner_DataUtil> init_List() {
      return List.copyOf(this.init_List);
   }

   public DataUtil.Inner_StreamUtil init_DataUtil_StreamUti() {
      return this.init_DataUtil_StreamUti;
   }

   public void init_V_DataUtil_StreamUti(DataUtil.Inner_StreamUtil loginMode) {
      this.init_DataUtil_StreamUti = loginMode == null ? DataUtil.Inner_StreamUtil.load : loginMode;
   }

   public String init_Str() {
      return this.init_Str;
   }

   public void init_V_Str(String allMacroName) {
      this.init_Str = allMacroName == null ? "" : allMacroName.trim();
   }

   public Set<String> init_Set_Str(String accountId) {
      Map<String, String> values = this.init_Map.get(load_Str_Str(accountId));
      return values == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(values.keySet()));
   }

   public Map<String, String> init_Map_Str(String accountId) {
      Map<String, String> values = this.init_Map.get(load_Str_Str(accountId));
      return values == null ? Map.of() : Map.copyOf(values);
   }

   public boolean init(String accountId, String name, String value) {
      String account = load_Str_Str(accountId);
      String key = init_Str_Str(name);
      if (!account.isEmpty() && !key.isEmpty() && value != null) {
         this.init_Map.computeIfAbsent(account, ignored -> new LinkedHashMap<>()).put(key, value);
         return true;
      } else {
         return false;
      }
   }

   public void init_V_Str_Str(String accountId, String name) {
      String account = load_Str_Str(accountId);
      Map<String, String> values = this.init_Map.get(account);
      if (values != null) {
         values.remove(init_Str_Str(name));
         if (values.isEmpty()) {
            this.init_Map.remove(account);
         }
      }
   }

   public void load_V_Str(String accountId) {
      this.init_Map.remove(load_Str_Str(accountId));
   }

   public MethodUtil init_MethodUtil() {
      return new MethodUtil(this.init_List, this.init_DataUtil_StreamUti, this.init_Str);
   }

   public DataUtil init_DataUtil() {
      return new DataUtil(this);
   }

   public static String init_Str_Str(String name) {
      if (name == null) {
         return "";
      }

      String value = name.trim().toLowerCase(Locale.ROOT);
      if (value.startsWith("secret.")) {
         value = value.substring("secret.".length());
      }

      return value.matches("[a-z0-9_.-]{1,64}") ? value : "";
   }

   private static List<DataUtil.Inner_DataUtil> init_List_Collection_Set(Collection<DataUtil.Inner_DataUtil> source, Set<String> accountIds) {
      Set<String> selected = init_Set_Set(accountIds);
      Map<String, DataUtil.Inner_DataUtil> unique = new LinkedHashMap<>();
      if (source != null) {
         for (DataUtil.Inner_DataUtil row : source) {
            if (row != null) {
               DataUtil.Inner_DataUtil account = new DataUtil.Inner_DataUtil(row.init(), row.load(), row.read());
               if (!account.init().isEmpty() && (selected == null || selected.contains(account.init()))) {
                  unique.putIfAbsent(account.init(), account);
               }
            }
         }
      }

      return List.copyOf(unique.values());
   }

   private static Set<String> init_Set_Set(Set<String> accountIds) {
      if (accountIds == null) {
         return null;
      }

      Set<String> selected = new LinkedHashSet<>();

      for (String accountId : accountIds) {
         String id = load_Str_Str(accountId);
         if (!id.isEmpty()) {
            selected.add(id);
         }
      }

      return selected;
   }

   private static Map<String, Map<String, String>> init_Map_Map(Map<String, ? extends Map<String, String>> source) {
      Map<String, Map<String, String>> copy = new LinkedHashMap<>();
      if (source != null) {
         source.forEach((accountId, values) -> {
            String account = load_Str_Str(accountId);
            if (!account.isEmpty() && values != null) {
               Map<String, String> row = new LinkedHashMap<>();
               values.forEach((name, value) -> {
                  String key = init_Str_Str(name);
                  if (!key.isEmpty() && value != null) {
                     row.put(key, value);
                  }
               });
               if (!row.isEmpty()) {
                  copy.put(account, row);
               }
            }
         });
      }

      return copy;
   }

   private static String load_Str_Str(String value) {
      return value == null ? "" : value.trim();
   }

   public record Inner_DataUtil(String init, String load, String read) {
      public Inner_DataUtil {
         init = init == null ? "" : init.trim();
         load = load == null ? "" : load.trim();
         read = read == null ? "" : read.trim();
      }
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read;
   }
}
