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

public final class MacroFolderLayout {
   public static final int init_I = 64;
   private final Map<String, String> init_Map = new LinkedHashMap<>();
   private final Set<String> init_Set = new HashSet<>();
   private final Map<String, String> load = new HashMap<>();
   private final Map<String, Integer> read = new HashMap<>();

   public static String sanitize(String name) {
      if (name == null) {
         return "";
      }

      StringBuilder out = new StringBuilder();
      name.codePoints().filter(c -> !Character.isISOControl(c) && c != 167).limit(64L).forEach(out::appendCodePoint);
      return out.toString().strip();
   }

   public static String key(String name) {
      return sanitize(name).toLowerCase(Locale.ROOT);
   }

   public String read(String name) {
      String clean = sanitize(name);
      return clean.isEmpty() ? "" : this.init_Map.getOrDefault(key(clean), clean);
   }

   public boolean add(String name) {
      String clean = sanitize(name);
      return !clean.isEmpty() && this.init_Map.putIfAbsent(key(clean), clean) == null;
   }

   public List<String> folders() {
      return List.copyOf(this.init_Map.values());
   }

   public boolean moveBefore(String name, String beforeName) {
      String key = key(name);
      String label = this.init_Map.get(key);
      if (label == null) {
         return false;
      }

      String beforeKey = beforeName == null ? null : key(beforeName);
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
      return this.init_Set.contains(key(name));
   }

   public String write(String folder) {
      return this.load.get(key(folder));
   }

   public void setAnchor(String folder, String macroName) {
      String key = key(folder);
      if (this.init_Map.containsKey(key)) {
         if (macroName != null && !macroName.isBlank()) {
            this.load.put(key, MacroNameNormalizer.normalize(macroName));
         } else {
            this.load.remove(key);
            this.read.remove(key);
         }
      }
   }

   public void init_V_List_Set(List<AutismMacro> looseInOrder, Set<String> leavingKeys) {
      if (!this.load.isEmpty() && !leavingKeys.isEmpty()) {
         for (String folder : new ArrayList<>(this.load.keySet())) {
            String anchor = this.load.get(folder);
            if (leavingKeys.contains(anchor)) {
               String next = null;
               boolean passed = false;

               for (AutismMacro macro : looseInOrder) {
                  String key = MacroNameNormalizer.normalize(macro.name);
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

   public void init_V_List(List<AutismMacro> looseInOrder) {
      if (!this.load.isEmpty()) {
         List<String> keys = new ArrayList<>(looseInOrder.size());

         for (AutismMacro macro : looseInOrder) {
            keys.add(MacroNameNormalizer.normalize(macro.name));
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

   public void toggleCollapsed(String name) {
      String key = key(name);
      if (this.init_Map.containsKey(key) && !this.init_Set.remove(key)) {
         this.init_Set.add(key);
      }
   }

   public void discoverFolders(Collection<AutismMacro> macros) {
      TreeSet<String> discovered = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

      for (AutismMacro macro : macros) {
         if (macro != null) {
            String clean = sanitize(macro.folder);
            if (!clean.isEmpty() && !this.init_Map.containsKey(key(clean))) {
               discovered.add(clean);
            }
         }
      }

      for (String name : discovered) {
         this.add(name);
      }

      for (AutismMacro macro : macros) {
         if (macro != null) {
            macro.folder = this.read(macro.folder);
         }
      }
   }

   public boolean init(String oldName, String newName, Collection<AutismMacro> macros) {
      String oldKey = key(oldName);
      String next = sanitize(newName);
      String nextKey = key(next);
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

         for (AutismMacro macro : macros) {
            if (macro != null && key(macro.folder).equals(oldKey)) {
               macro.folder = next;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean removeFolder(String name, Collection<AutismMacro> macros) {
      String key = key(name);
      if (this.init_Map.remove(key) == null) {
         return false;
      }

      this.init_Set.remove(key);
      this.load.remove(key);
      this.read.remove(key);

      for (AutismMacro macro : macros) {
         if (macro != null && key(macro.folder).equals(key)) {
            macro.folder = "";
         }
      }

      return true;
   }

   public ListTag toTag() {
      ListTag list = new ListTag();

      for (String name : this.folders()) {
         CompoundTag folder = new CompoundTag();
         folder.putString("name", name);
         folder.putBoolean("collapsed", this.load_Z_Str(name));
         String anchor = this.load.get(key(name));
         if (anchor != null) {
            folder.putString("before", anchor);
            folder.putInt("beforeSlot", this.read.getOrDefault(key(name), 0));
         }

         list.add(folder);
      }

      return list;
   }

   public static MacroFolderLayout fromTag(Tag tag, Collection<AutismMacro> macros) {
      MacroFolderLayout folders = new MacroFolderLayout();
      if (tag instanceof ListTag) {
         for (Tag entry : (ListTag)tag) {
            if (entry instanceof CompoundTag folder) {
               String name = sanitize(folder.getStringOr("name", ""));
               folders.add(name);
               if (!name.isEmpty() && folder.getBooleanOr("collapsed", false)) {
                  folders.init_Set.add(key(name));
               }

               String before = folder.getStringOr("before", "");
               if (!name.isEmpty() && !before.isEmpty()) {
                  folders.load.put(key(name), before);
                  folders.read.put(key(name), Math.max(0, folder.getIntOr("beforeSlot", 0)));
               }
            }
         }
      }

      folders.discoverFolders(macros);
      return folders;
   }

   public List<MacroFolderLayout.Inner_DataUtil> init(Collection<AutismMacro> macros, String query, boolean includeEmpty) {
      return init(macros, this.folders(), this::load_Z_Str, query, includeEmpty);
   }

   public List<MacroFolderLayout.Inner_DataUtil> load(Collection<AutismMacro> macros, String query, boolean includeEmpty) {
      return init(macros, this.folders(), this::load_Z_Str, query, includeEmpty, true, this.load);
   }

   public static List<MacroFolderLayout.Inner_DataUtil> init(
      Collection<AutismMacro> macros, Collection<String> folders, Predicate<String> collapsed, String query, boolean includeEmpty
   ) {
      return init(macros, folders, collapsed, query, includeEmpty, false);
   }

   public static List<MacroFolderLayout.Inner_DataUtil> init(
      Collection<AutismMacro> macros, Collection<String> folders, Predicate<String> collapsed, String query, boolean includeEmpty, boolean preserveOrder
   ) {
      return init(macros, folders, collapsed, query, includeEmpty, preserveOrder, Map.of());
   }

   private static List<MacroFolderLayout.Inner_DataUtil> init(
      Collection<AutismMacro> macros,
      Collection<String> folders,
      Predicate<String> collapsed,
      String query,
      boolean includeEmpty,
      boolean preserveOrder,
      Map<String, String> anchors
   ) {
      String needle = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
      Map<String, String> labels = new HashMap<>();
      Map<String, List<AutismMacro>> groups = new HashMap<>();
      List<String> order = new ArrayList<>();

      for (String folder : folders) {
         String clean = sanitize(folder);
         if (!clean.isEmpty() && !labels.containsKey(key(clean))) {
            labels.put(key(clean), clean);
            groups.put(key(clean), new ArrayList<>());
            order.add(key(clean));
         }
      }

      TreeMap<String, String> extra = new TreeMap<>();

      for (AutismMacro macro : macros) {
         if (macro != null && macro.name != null && !macro.name.isBlank()) {
            String folder = sanitize(macro.folder);
            String key = key(folder);
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
      List<MacroFolderLayout.Inner_DataUtil> rows = new ArrayList<>();
      List<AutismMacro> loose = groups.getOrDefault("", List.of());
      if (anchors.isEmpty()) {
         init(rows, "", loose, needle, collapsed, includeEmpty, preserveOrder);

         for (String groupKey : folderKeys) {
            init(rows, labels.get(groupKey), groups.get(groupKey), needle, collapsed, includeEmpty, preserveOrder);
         }

         return List.copyOf(rows);
      } else {
         Map<String, Integer> slots = new HashMap<>();

         for (int i = 0; i < loose.size(); i++) {
            slots.putIfAbsent(MacroNameNormalizer.normalize(loose.get(i).name), i);
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

            if (i < loose.size() && (needle.isEmpty() || loose.get(i).name.toLowerCase(Locale.ROOT).contains(needle))) {
               rows.add(new MacroFolderLayout.Inner_DataUtil("", loose.get(i), 0, false));
            }
         }

         return List.copyOf(rows);
      }
   }

   private static void init(
      List<MacroFolderLayout.Inner_DataUtil> rows, String folder, List<AutismMacro> all, String needle, Predicate<String> collapsed, boolean includeEmpty, boolean preserveOrder
   ) {
      boolean folderMatch = folder.toLowerCase(Locale.ROOT).contains(needle);
      Stream<AutismMacro> matching = all.stream().filter(m -> folderMatch || m.name.toLowerCase(Locale.ROOT).contains(needle));
      List<AutismMacro> matches = preserveOrder ? matching.toList() : matching.sorted(Comparator.comparing(m -> m.name, String.CASE_INSENSITIVE_ORDER)).toList();
      if (!matches.isEmpty() || includeEmpty && all.isEmpty() && folderMatch) {
         boolean shut = needle.isEmpty() && collapsed.test(folder);
         if (!folder.isEmpty()) {
            rows.add(new MacroFolderLayout.Inner_DataUtil(folder, null, matches.size(), shut));
         }

         if (folder.isEmpty() || !shut) {
            for (AutismMacro macro : matches) {
               rows.add(new MacroFolderLayout.Inner_DataUtil(folder, macro, 0, false));
            }
         }
      }
   }

   public record Inner_DataUtil(String init_Str, AutismMacro macro, int init_I, boolean init_Z) {
      public boolean init_Z() {
         return this.macro == null;
      }

      public String init_Str() {
         return this.init_Z() ? this.init_Str + " (" + this.init_I + ")" : this.macro.name;
      }

      public String folderName() {
         return this.init_Str;
      }

      public boolean isFolder() {
         return this.init_Z;
      }
   }
}
