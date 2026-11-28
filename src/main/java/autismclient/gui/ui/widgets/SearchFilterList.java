package autismclient.gui.ui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public final class SearchFilterList<T> {
   private final Function<T, String> searchText;
   private List<T> items = List.of();
   private List<T> filteredCache = List.of();
   private String query = "";
   private boolean dirty = true;

   public SearchFilterList(Function<T, String> searchText) {
      this.searchText = Objects.requireNonNull(searchText);
   }

   public void setItems(List<T> items) {
      List<T> next = items != null && !items.isEmpty() ? List.copyOf(items) : List.of();
      if (!this.items.equals(next)) {
         this.items = next;
         this.dirty = true;
      }
   }

   public void setQuery(String query) {
      String next = normalize(query);
      if (!this.query.equals(next)) {
         this.query = next;
         this.dirty = true;
      }
   }

   public List<T> filtered() {
      this.refilter();
      return this.filteredCache;
   }

   public int filteredCount() {
      return this.filtered().size();
   }

   private void refilter() {
      if (this.dirty) {
         if (this.query.isEmpty()) {
            this.filteredCache = this.items;
         } else {
            ArrayList<T> next = new ArrayList<>();

            for (T item : this.items) {
               String haystack = normalize(this.searchText.apply(item));
               if (haystack.contains(this.query)) {
                  next.add(item);
               }
            }

            this.filteredCache = List.copyOf(next);
         }

         this.dirty = false;
      }
   }

   private static String normalize(String value) {
      return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
   }
}
