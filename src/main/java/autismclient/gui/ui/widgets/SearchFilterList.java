package autismclient.gui.ui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public final class SearchFilterList<T> {
   private final Function<T, String> init_Function;
   private List<T> init_List = List.of();
   private List<T> load = List.of();
   private String init_Str = "";
   private boolean init_Z = true;

   public SearchFilterList(Function<T, String> searchText) {
      this.init_Function = Objects.requireNonNull(searchText);
   }

   public void init_V_List(List<T> items) {
      List<T> next = items != null && !items.isEmpty() ? List.copyOf(items) : List.of();
      if (!this.init_List.equals(next)) {
         this.init_List = next;
         this.init_Z = true;
      }
   }

   public void init_V_Str(String query) {
      String next = init_Str_Str(query);
      if (!this.init_Str.equals(next)) {
         this.init_Str = next;
         this.init_Z = true;
      }
   }

   public List<T> init_List() {
      this.init_V();
      return this.load;
   }

   public int init_I() {
      return this.init_List().size();
   }

   private void init_V() {
      if (this.init_Z) {
         if (this.init_Str.isEmpty()) {
            this.load = this.init_List;
         } else {
            ArrayList<T> next = new ArrayList<>();

            for (T item : this.init_List) {
               String haystack = init_Str_Str(this.init_Function.apply(item));
               if (haystack.contains(this.init_Str)) {
                  next.add(item);
               }
            }

            this.load = List.copyOf(next);
         }

         this.init_Z = false;
      }
   }

   private static String init_Str_Str(String value) {
      return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
   }
}
