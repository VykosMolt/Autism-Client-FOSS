package autismclient.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public final class cY {
   public static final int init_I = 64;
   private final Map<String, String> init_Map = new LinkedHashMap<>();
   private final Set<String> init_Set = new HashSet<>();
   private final Map<String, String> load = new HashMap<>();
   private final Map<String, Integer> read = new HashMap<>();

   public static String init_Str_Str(String name) {
      if (name == null) {
         return "";
      }

      StringBuilder out = new StringBuilder();
      name.codePoints().filter(c -> !Character.isISOControl(c) && c != 167).limit(64L).forEach(out::appendCodePoint);
      return out.toString().strip();
   }

   public static String load_Str_Str(String name) {
      return init_Str_Str(name).toLowerCase(Locale.ROOT);
   }

   public String read(String name) {
      String clean = init_Str_Str(name);
      return clean.isEmpty() ? "" : this.init_Map.getOrDefault(load_Str_Str(clean), clean);
   }

   public boolean init_Z_Str(String name) {
      String clean = init_Str_Str(name);
      return !clean.isEmpty() && this.init_Map.putIfAbsent(load_Str_Str(clean), clean) == null;
   }

   public List<String> init_List() {
      return List.copyOf(this.init_Map.values());
   }

   public boolean init_Z_Str_Str(String name, String beforeName) {
      String key = load_Str_Str(name);
      String label = this.init_Map.get(key);
      if (label == null) {
         return false;
      }

      String beforeKey = beforeName == null ? null : load_Str_Str(beforeName);
      if (key.equals(beforeKey)) {
         return false;
      }

      Map<String, String> previous = new LinkedHashMap<>(this.init_Map);
      this.init_Map.clear();
      boolean placed = false;

      for (Entry<String, String> entry : previous.entrySet()) {
         if (!entry.getKey().equals(key)) {
            if (!placed && entry.getKey().equals(beforeKey)) {
               this.init_Map.put(key, label);
               placed = true;
            }

            this.init_Map.put(entry.getKey(), entry.getValue());
         }
      }

      if (!placed) {
         this.init_Map.put(key, label);
      }

      return true;
   }

   public boolean load_Z_Str(String name) {
      return this.init_Set.contains(load_Str_Str(name));
   }

   public String write(String folder) {
      return this.load.get(load_Str_Str(folder));
   }

   public void init_V_Str_Str(String folder, String macroName) {
      String key = load_Str_Str(folder);
      if (this.init_Map.containsKey(key)) {
         if (macroName != null && !macroName.isBlank()) {
            this.load.put(key, cZ.init(macroName));
         } else {
            this.load.remove(key);
            this.read.remove(key);
         }
      }
   }

   public void init_V_List_Set(List<aM> looseInOrder, Set<String> leavingKeys) {
      if (!this.load.isEmpty() && !leavingKeys.isEmpty()) {
         for (String folder : new ArrayList<>(this.load.keySet())) {
            String anchor = this.load.get(folder);
            if (leavingKeys.contains(anchor)) {
               String next = null;
               boolean passed = false;

               for (aM macro : looseInOrder) {
                  String key = cZ.init(macro.init_Str);
                  if (!passed) {
                     passed = key.equals(anchor);
                  } else if (!leavingKeys.contains(key)) {
                     next = key;
                     break;
                  }
               }

               if (next == null) {
                  this.load.remove(folder);
                  this.read.remove(folder);
               } else {
                  this.load.put(folder, next);
               }
            }
         }
      }
   }

   public void init_V_List(List<aM> looseInOrder) {
      if (!this.load.isEmpty()) {
         List<String> keys = new ArrayList<>(looseInOrder.size());

         for (aM macro : looseInOrder) {
            keys.add(cZ.init(macro.init_Str));
         }

         for (String folder : new ArrayList<>(this.load.keySet())) {
            if (!this.init_Map.containsKey(folder)) {
               this.load.remove(folder);
               this.read.remove(folder);
            } else {
               int index = keys.indexOf(this.load.get(folder));
               if (index < 0) {
                  index = this.read.getOrDefault(folder, keys.size());
                  if (index >= keys.size()) {
                     this.load.remove(folder);
                     this.read.remove(folder);
                     continue;
                  }

                  this.load.put(folder, keys.get(index));
               }

               this.read.put(folder, index);
            }
         }
      }
   }

   public void init_V_Str(String name) {
      String key = load_Str_Str(name);
      if (this.init_Map.containsKey(key) && !this.init_Set.remove(key)) {
         this.init_Set.add(key);
      }
   }

   public void init_V_Collection(Collection<aM> macros) {
      TreeSet<String> discovered = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

      for (aM macro : macros) {
         if (macro != null) {
            String clean = init_Str_Str(macro.read);
            if (!clean.isEmpty() && !this.init_Map.containsKey(load_Str_Str(clean))) {
               discovered.add(clean);
            }
         }
      }

      for (String name : discovered) {
         this.init_Z_Str(name);
      }

      for (aM macro : macros) {
         if (macro != null) {
            macro.read = this.read(macro.read);
         }
      }
   }

   public boolean init(String oldName, String newName, Collection<aM> macros) {
      String oldKey = load_Str_Str(oldName);
      String next = init_Str_Str(newName);
      String nextKey = load_Str_Str(next);
      if (this.init_Map.containsKey(oldKey) && !next.isEmpty() && (oldKey.equals(nextKey) || !this.init_Map.containsKey(nextKey))) {
         Map<String, String> previous = new LinkedHashMap<>(this.init_Map);
         this.init_Map.clear();

         for (Entry<String, String> entry : previous.entrySet()) {
            if (entry.getKey().equals(oldKey)) {
               this.init_Map.put(nextKey, next);
            } else {
               this.init_Map.put(entry.getKey(), entry.getValue());
            }
         }

         if (this.init_Set.remove(oldKey)) {
            this.init_Set.add(nextKey);
         }

         String anchor = this.load.remove(oldKey);
         Integer slot = this.read.remove(oldKey);
         if (anchor != null) {
            this.load.put(nextKey, anchor);
         }

         if (slot != null) {
            this.read.put(nextKey, slot);
         }

         for (aM macro : macros) {
            if (macro != null && load_Str_Str(macro.read).equals(oldKey)) {
               macro.read = next;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean init_Z_Str_Collection(String name, Collection<aM> macros) {
      String key = load_Str_Str(name);
      if (this.init_Map.remove(key) == null) {
         return false;
      }

      this.init_Set.remove(key);
      this.load.remove(key);
      this.read.remove(key);

      for (aM macro : macros) {
         if (macro != null && load_Str_Str(macro.read).equals(key)) {
            macro.read = "";
         }
      }

      return true;
   }

   public ListTag init_ListTag() {
      ListTag list = new ListTag();

      for (String name : this.init_List()) {
         CompoundTag folder = new CompoundTag();
         folder.putString("name", name);
         folder.putBoolean("collapsed", this.load_Z_Str(name));
         String anchor = this.load.get(load_Str_Str(name));
         if (anchor != null) {
            folder.putString("before", anchor);
            folder.putInt("beforeSlot", this.read.getOrDefault(load_Str_Str(name), 0));
         }

         list.add(folder);
      }

      return list;
   }

   public static cY init_cY_Tag_Collection(Tag tag, Collection<aM> macros) {
      cY folders = new cY();
      if (tag instanceof ListTag) {
         for (Tag entry : (ListTag)tag) {
            if (entry instanceof CompoundTag folder) {
               String name = init_Str_Str(folder.getStringOr("name", ""));
               folders.init_Z_Str(name);
               if (!name.isEmpty() && folder.getBooleanOr("collapsed", false)) {
                  folders.init_Set.add(load_Str_Str(name));
               }

               String before = folder.getStringOr("before", "");
               if (!name.isEmpty() && !before.isEmpty()) {
                  folders.load.put(load_Str_Str(name), before);
                  folders.read.put(load_Str_Str(name), Math.max(0, folder.getIntOr("beforeSlot", 0)));
               }
            }
         }
      }

      folders.init_V_Collection(macros);
      return folders;
   }

   public List<cY.Inner_DataUtil> init(Collection<aM> macros, String query, boolean includeEmpty) {
      return init(macros, this.init_List(), this::load_Z_Str, query, includeEmpty);
   }

   public List<cY.Inner_DataUtil> load(Collection<aM> macros, String query, boolean includeEmpty) {
      return init(macros, this.init_List(), this::load_Z_Str, query, includeEmpty, true, this.load);
   }

   public static List<cY.Inner_DataUtil> init(
      Collection<aM> macros, Collection<String> folders, Predicate<String> collapsed, String query, boolean includeEmpty
   ) {
      return init(macros, folders, collapsed, query, includeEmpty, false);
   }

   public static List<cY.Inner_DataUtil> init(
      Collection<aM> macros, Collection<String> folders, Predicate<String> collapsed, String query, boolean includeEmpty, boolean preserveOrder
   ) {
      return init(macros, folders, collapsed, query, includeEmpty, preserveOrder, Map.of());
   }

   private static List<cY.Inner_DataUtil> init(
      Collection<aM> macros,
      Collection<String> folders,
      Predicate<String> collapsed,
      String query,
      boolean includeEmpty,
      boolean preserveOrder,
      Map<String, String> anchors
   ) {
      String needle = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
      Map<String, String> labels = new HashMap<>();
      Map<String, List<aM>> groups = new HashMap<>();
      List<String> order = new ArrayList<>();

      for (String folder : folders) {
         String clean = init_Str_Str(folder);
         if (!clean.isEmpty() && !labels.containsKey(load_Str_Str(clean))) {
            labels.put(load_Str_Str(clean), clean);
            groups.put(load_Str_Str(clean), new ArrayList<>());
            order.add(load_Str_Str(clean));
         }
      }

      TreeMap<String, String> extra = new TreeMap<>();

      for (aM macro : macros) {
         if (macro != null && macro.init_Str != null && !macro.init_Str.isBlank()) {
            String folder = init_Str_Str(macro.read);
            String key = load_Str_Str(folder);
            if (!labels.containsKey(key)) {
               labels.put(key, folder);
               groups.put(key, new ArrayList<>());
               if (!key.isEmpty()) {
                  extra.put(key, folder);
               }
            }

            groups.get(key).add(macro);
         }
      }

      List<String> folderKeys = new ArrayList<>(order);
      folderKeys.addAll(extra.keySet());
      List<cY.Inner_DataUtil> rows = new ArrayList<>();
      List<aM> loose = groups.getOrDefault("", List.of());
      if (anchors.isEmpty()) {
         init(rows, "", loose, needle, collapsed, includeEmpty, preserveOrder);

         for (String groupKey : folderKeys) {
            init(rows, labels.get(groupKey), groups.get(groupKey), needle, collapsed, includeEmpty, preserveOrder);
         }

         return List.copyOf(rows);
      } else {
         Map<String, Integer> slots = new HashMap<>();

         for (int i = 0; i < loose.size(); i++) {
            slots.putIfAbsent(cZ.init(loose.get(i).init_Str), i);
         }

         boolean[] emitted = new boolean[folderKeys.size()];

         for (int i = 0; i <= loose.size(); i++) {
            for (int f = 0; f < folderKeys.size(); f++) {
               if (!emitted[f]) {
                  String anchor = anchors.get(folderKeys.get(f));
                  int slot = anchor == null ? loose.size() : slots.getOrDefault(anchor, loose.size());
                  if (slot <= i) {
                     emitted[f] = true;
                     init(rows, labels.get(folderKeys.get(f)), groups.get(folderKeys.get(f)), needle, collapsed, includeEmpty, preserveOrder);
                  }
               }
            }

            if (i < loose.size() && (needle.isEmpty() || loose.get(i).init_Str.toLowerCase(Locale.ROOT).contains(needle))) {
               rows.add(new cY.Inner_DataUtil("", loose.get(i), 0, false));
            }
         }

         return List.copyOf(rows);
      }
   }

   private static void init(
      List<cY.Inner_DataUtil> rows, String folder, List<aM> all, String needle, Predicate<String> collapsed, boolean includeEmpty, boolean preserveOrder
   ) {
      boolean folderMatch = folder.toLowerCase(Locale.ROOT).contains(needle);
      Stream<aM> matching = all.stream().filter(m -> folderMatch || m.init_Str.toLowerCase(Locale.ROOT).contains(needle));
      List<aM> matches = preserveOrder ? matching.toList() : matching.sorted(Comparator.comparing(m -> m.init_Str, String.CASE_INSENSITIVE_ORDER)).toList();
      if (!matches.isEmpty() || includeEmpty && all.isEmpty() && folderMatch) {
         boolean shut = needle.isEmpty() && collapsed.test(folder);
         if (!folder.isEmpty()) {
            rows.add(new cY.Inner_DataUtil(folder, null, matches.size(), shut));
         }

         if (folder.isEmpty() || !shut) {
            for (aM macro : matches) {
               rows.add(new cY.Inner_DataUtil(folder, macro, 0, false));
            }
         }
      }
   }

   public record Inner_DataUtil(String init_Str, aM init_aM, int init_I, boolean init_Z) {
      public boolean init_Z() {
         return this.init_aM == null;
      }

      public String init_Str() {
         return this.init_Z() ? this.init_Str + " (" + this.init_I + ")" : this.init_aM.init_Str;
      }

      public String load_Str() {
         return this.init_Str;
      }

      public boolean load_Z() {
         return this.init_Z;
      }
   }
}
