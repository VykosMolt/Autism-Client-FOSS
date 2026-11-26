package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.util.macro.MacroExecutor;
import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

public class MacroLibraryManager {
   private static MacroLibraryManager init_aN;
   private List<AutismMacro> init_List = new ArrayList<>();
   private MacroFolderLayout init_cY = new MacroFolderLayout();
   private volatile File init_File;
   private volatile long init_J;
   private volatile boolean init_Z = false;
   private static final long load = 300L;
   private static volatile long read = -1L;

   private MacroLibraryManager() {
      this.init_File = init_File();
      this.update();
   }

   public static File init_File() {
      return new File(AutismClientAddon.FOLDER, "autism_macros.nbt");
   }

   public static void init_V_File(File file) {
      if (file != null) {
         try {
            CompoundTag tag = new CompoundTag();
            tag.put("macros", new ListTag());
            Files.createDirectories(file.toPath().getParent());
            NbtIo.write(tag, file.toPath());
         } catch (Exception e) {
            AutismClientAddon.LOG.warn("Could not write empty macro library {}", file, e);
         }
      }
   }

   public static synchronized MacroLibraryManager init_aN() {
      if (init_aN == null) {
         init_aN = new MacroLibraryManager();
      }

      return init_aN;
   }

   public synchronized String init_Str_Str(String preferredName) {
      String baseName = preferredName != null && !preferredName.isBlank() ? preferredName.trim() : "New Macro";
      String candidate = baseName;
      int suffix = 1;

      while (this.init_aM_Str(candidate) != null) {
         candidate = baseName + " (" + suffix++ + ")";
      }

      return candidate;
   }

   public synchronized AutismMacro init_aM_aM_Str(AutismMacro source, String preferredName) {
      if (source == null) {
         return null;
      }

      AutismMacro copy = source.init_aM();
      copy.load_aM();
      copy.init_Str = this.init_Str_Str(preferredName != null && !preferredName.isBlank() ? preferredName : source.init_Str);
      this.init_V_aM(copy);
      return copy;
   }

   public synchronized List<AutismMacro> init(List<AutismMacro> copies, boolean imported, AutismMacro anchor) {
      List<AutismMacro> added = new ArrayList<>();
      if (copies != null && !copies.isEmpty()) {
         int anchorIndex = -1;
         if (anchor != null) {
            for (int i = 0; i < this.init_List.size(); i++) {
               if (this.init_List.get(i) == anchor) {
                  anchorIndex = i;
                  break;
               }
            }
         }

         Set<String> folderKeys = new HashSet<>();

         for (String folder : this.load_List()) {
            folderKeys.add(MacroFolderLayout.load_Str_Str(folder));
         }

         for (AutismMacro source : copies) {
            if (source != null) {
               AutismMacro copy = source;
               if (imported) {
                  copy.load_aM();
               }

               copy.init_Str = this.init_Str_Str(copy.init_Str);
               String folder;
               if (anchorIndex >= 0) {
                  folder = anchor.read == null ? "" : anchor.read;
               } else {
                  folder = copy.read != null && folderKeys.contains(MacroFolderLayout.load_Str_Str(copy.read)) ? this.init_cY.read(copy.read) : "";
               }

               copy.read = folder;
               int index;
               if (anchorIndex >= 0) {
                  index = anchorIndex + 1 + added.size();
               } else {
                  index = this.init_List.size();
                  String key = MacroFolderLayout.load_Str_Str(folder);

                  for (int i = this.init_List.size() - 1; i >= 0; i--) {
                     AutismMacro macro = this.init_List.get(i);
                     if (macro != null && MacroFolderLayout.load_Str_Str(macro.read).equals(key)) {
                        index = i + 1;
                        break;
                     }
                  }
               }

               this.init_List.add(Math.min(index, this.init_List.size()), copy);
               added.add(copy);
            }
         }

         if (!added.isEmpty()) {
            this.load_V();
         }

         return added;
      } else {
         return added;
      }
   }

   public synchronized void init_V_aM(AutismMacro macro) {
      if (macro != null) {
         AutismMacro existing = this.init_aM_Str(macro.init_Str);
         if (existing != null && existing != macro) {
            this.init_Z_aM(existing);
         }

         if (existing != macro) {
            this.init_List.add(macro);
         }

         this.load_V();
      }
   }

   private boolean init_Z_aM(AutismMacro macro) {
      Iterator<AutismMacro> it = this.init_List.iterator();

      while (it.hasNext()) {
         if (it.next() == macro) {
            it.remove();
            return true;
         }
      }

      return false;
   }

   public synchronized AutismMacro init_aM_Str(String name) {
      if (name == null) {
         return null;
      }

      for (AutismMacro macro : this.init_List) {
         if (macro != null && MacroNameNormalizer.init(macro.init_Str, name)) {
            return macro;
         }
      }

      return null;
   }

   public synchronized List<AutismMacro> init_List() {
      return new ArrayList<>(this.init_List);
   }

   public synchronized List<String> load_List() {
      return this.init_cY.init_List();
   }

   public synchronized List<MacroFolderLayout.Inner_DataUtil> init_List_Str(String query) {
      return this.init(query, false, macro -> true);
   }

   public synchronized List<MacroFolderLayout.Inner_DataUtil> init(String query, boolean includeEmpty, Predicate<AutismMacro> filter) {
      return this.init_cY.init(this.init_List.stream().filter(filter).toList(), query, includeEmpty);
   }

   public synchronized List<MacroFolderLayout.Inner_DataUtil> load(String query, boolean includeEmpty, Predicate<AutismMacro> filter) {
      return this.init_cY.load(this.init_List.stream().filter(filter).toList(), query, includeEmpty);
   }

   public synchronized boolean init_Z_Str(String name) {
      if (!this.init_cY.init_Z_Str(name)) {
         return false;
      }

      this.load_V();
      return true;
   }

   public synchronized boolean init_Z_Str_Str(String oldName, String newName) {
      if (!this.init_cY.init(oldName, newName, this.init_List)) {
         return false;
      }

      this.load_V();
      return true;
   }

   public synchronized boolean load(String folder, String beforeFolder) {
      if (!this.init_cY.init_Z_Str_Str(folder, beforeFolder)) {
         return false;
      }

      this.load_V();
      return true;
   }

   public synchronized boolean init(String folder, String beforeFolder, String anchorMacro) {
      if (this.load_List().stream().noneMatch(f -> MacroFolderLayout.load_Str_Str(f).equals(MacroFolderLayout.load_Str_Str(folder)))) {
         return false;
      }

      this.init_cY.init_Z_Str_Str(folder, beforeFolder);
      this.init_cY.init_V_Str_Str(folder, anchorMacro);
      this.load_V();
      return true;
   }

   public synchronized String load_Str(String folder) {
      return this.init_cY.write(folder);
   }

   private List<AutismMacro> read_List() {
      List<AutismMacro> out = new ArrayList<>();

      for (AutismMacro macro : this.init_List) {
         if (macro != null && macro.init_Str != null && (macro.read == null || macro.read.isEmpty())) {
            out.add(macro);
         }
      }

      return out;
   }

   private void init_V_Collection(Collection<AutismMacro> leaving) {
      Set<String> keys = new HashSet<>();

      for (AutismMacro macro : leaving) {
         if (macro != null && macro.init_Str != null && (macro.read == null || macro.read.isEmpty())) {
            keys.add(MacroNameNormalizer.init(macro.init_Str));
         }
      }

      if (!keys.isEmpty()) {
         this.init_cY.init_V_List_Set(this.read_List(), keys);
      }
   }

   public synchronized boolean load_Z(String name) {
      if (!this.init_cY.init_Z_Str_Collection(name, this.init_List)) {
         return false;
      }

      this.load_V();
      return true;
   }

   public synchronized boolean init_Z_aM_Str(AutismMacro macro, String folder) {
      if (macro != null && !this.init_List.stream().noneMatch(m -> m == macro)) {
         String target = MacroFolderLayout.init_Str_Str(folder);
         if (!target.isEmpty() && this.load_List().stream().noneMatch(f -> MacroFolderLayout.load_Str_Str(f).equals(MacroFolderLayout.load_Str_Str(target)))) {
            return false;
         }

         macro.read = this.init_cY.read(target);
         this.load_V();
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean init(Collection<AutismMacro> moving, String targetFolder, String insertBeforeName) {
      return this.init(moving, targetFolder, insertBeforeName, List.of());
   }

   public synchronized boolean init(Collection<AutismMacro> moving, String targetFolder, String insertBeforeName, List<String> foldersAbove) {
      if (moving != null && !moving.isEmpty()) {
         Set<AutismMacro> block = new LinkedHashSet<>();

         for (AutismMacro macro : moving) {
            if (macro != null) {
               block.add(macro);
            }
         }

         if (block.isEmpty()) {
            return false;
         }

         for (AutismMacro macro : block) {
            if (this.init_List.stream().noneMatch(m -> m == macro)) {
               return false;
            }
         }

         String target = MacroFolderLayout.init_Str_Str(targetFolder);
         if (!target.isEmpty() && this.load_List().stream().noneMatch(f -> MacroFolderLayout.load_Str_Str(f).equals(MacroFolderLayout.load_Str_Str(target)))) {
            return false;
         }

         String canonical = this.init_cY.read(target);
         this.init_V_Collection(block);

         for (AutismMacro macro : block) {
            this.init_Z_aM(macro);
         }

         int index = -1;
         if (insertBeforeName != null) {
            for (int i = 0; i < this.init_List.size(); i++) {
               AutismMacro macro = this.init_List.get(i);
               if (macro != null && MacroNameNormalizer.init(macro.init_Str, insertBeforeName)) {
                  index = i;
                  break;
               }
            }
         }

         if (index < 0) {
            String targetKey = MacroFolderLayout.load_Str_Str(canonical);

            for (int i = this.init_List.size() - 1; i >= 0; i--) {
               AutismMacro macro = this.init_List.get(i);
               if (macro != null && MacroFolderLayout.load_Str_Str(macro.read).equals(targetKey)) {
                  index = i + 1;
                  break;
               }
            }

            if (index < 0) {
               index = this.init_List.size();
            }
         }

         for (AutismMacro macro : block) {
            macro.read = canonical;
         }

         this.init_List.addAll(index, block);
         if (canonical.isEmpty() && foldersAbove != null && !foldersAbove.isEmpty()) {
            String first = block.iterator().next().init_Str;

            for (String folder : foldersAbove) {
               this.init_cY.init_V_Str_Str(folder, first);
            }
         }

         this.load_V();
         return true;
      } else {
         return false;
      }
   }

   public synchronized void init_V_Str(String folder) {
      this.init_cY.init_V_Str(folder);
      this.load(false);
   }

   public long init_J() {
      return this.init_J;
   }

   public synchronized void load(AutismMacro macro) {
      if (macro != null && this.init_Z_aM_Z(macro, true)) {
         this.load_V();
      }
   }

   private boolean init_Z_aM_Z(AutismMacro macro, boolean announce) {
      if (MacroExecutor.init_Z_Str(macro.init_Str)) {
         MacroExecutor.init_V_Str(macro.init_Str);
         AutismClientMessaging.sendPrefixed("§eStopped running macro before deletion: " + macro.init_Str);
      }

      this.init_V_Collection(List.of(macro));
      if (!this.init_Z_aM(macro)) {
         return false;
      }

      String deletedName = macro.init_Str == null ? "" : macro.init_Str;
      if (announce) {
         AutismClientMessaging.sendPrefixed("§aDeleted macro: " + macro.init_Str);
      }

      AutismMacroEditorOverlay editor = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
      if (editor != null && editor.init_Z_aM(macro)) {
         editor.update_V_10();
      }

      if (!this.init_Z && LanSyncManager.init_aE().write_Z()) {
         LanSyncManager.init_aE().read_V_Str(macro.init_Str);
      }

      return true;
   }

   public void read(AutismMacro macro) {
      this.load(macro);
   }

   public synchronized int init_I_Collection(Collection<AutismMacro> macros) {
      if (macros != null && !macros.isEmpty()) {
         int removed = 0;

         for (AutismMacro macro : macros) {
            if (macro != null && this.init_Z_aM_Z(macro, false)) {
               removed++;
            }
         }

         if (removed > 0) {
            this.load_V();
         }

         return removed;
      } else {
         return 0;
      }
   }

   public synchronized List<AutismMacro> init_List_Collection(Collection<AutismMacro> macros) {
      List<AutismMacro> copies = new ArrayList<>();
      if (macros != null && !macros.isEmpty()) {
         for (AutismMacro macro : macros) {
            if (macro != null) {
               int index = -1;

               for (int i = 0; i < this.init_List.size(); i++) {
                  if (this.init_List.get(i) == macro) {
                     index = i;
                     break;
                  }
               }

               if (index >= 0) {
                  AutismMacro copy = macro.init_aM_Str(this.init_Str_Str(macro.init_Str));
                  copy.load_I = -1;
                  this.init_List.add(index + 1, copy);
                  copies.add(copy);
               }
            }
         }

         if (!copies.isEmpty()) {
            this.load_V();
         }

         return copies;
      } else {
         return copies;
      }
   }

   public void load_V(String name) {
      AutismMacro macro = this.init_aM_Str(name);
      if (macro != null) {
         macro.init_V();
         AutismClientMessaging.sendPrefixed("§aExecuting macro: " + macro.init_Str);
      } else {
         AutismClientMessaging.sendPrefixed("§cMacro not found: " + name);
      }
   }

   public void init_V() {
      if (MacroExecutor.load_Z()) {
         MacroExecutor.read_V();
      } else {
         AutismClientMessaging.sendPrefixed("§eNo macro is currently running.");
      }
   }

   public void load_V() {
      this.load(true);
   }

   public void read_V() {
      read = System.currentTimeMillis() + 300L;
   }

   public static void init_V_Z(boolean force) {
      long due = read;
      if (due >= 0L && (force || System.currentTimeMillis() >= due)) {
         init_aN().load_V();
      }
   }

   private void load(boolean announce) {
      read = -1L;
      boolean broadcast;
      synchronized (this) {
         this.init_J++;
         File target = this.init_File;
         broadcast = announce && !this.init_Z;
         this.init_cY.init_V_Collection(this.init_List);
         this.init_cY.init_V_List(this.read_List());
         CompoundTag tag = new CompoundTag();
         ListTag list = new ListTag();

         for (AutismMacro macro : this.init_List) {
            if (macro != null) {
               list.add(macro.load_CompoundTag());
            }
         }

         tag.put("macros", list);
         tag.put("folders", this.init_cY.init_ListTag());
         DebouncedPersistenceScheduler.init("macro-library:" + target.getAbsolutePath(), AuditMacroRead.track(() -> init_V_File_CompoundTag(target, tag), target, tag));
      }

      if (broadcast && LanSyncManager.init_aE().write_Z()) {
         LanSyncManager.init_aE().push();
      }
   }

   private static void init_V_File_CompoundTag(File targetFile, CompoundTag tag) {
      Path target = targetFile.toPath();
      Path temp = target.resolveSibling(targetFile.getName() + ".tmp");
      Path backup = target.resolveSibling(targetFile.getName() + ".bak");

      try {
         Files.createDirectories(target.getParent());
         NbtIo.write(tag, temp);
         if (Files.exists(target)) {
            try {
               if (AuditMacroRead.hasLibrary(target)) {
                  Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING);
               }
            } catch (Exception backupError) {
               AutismClientAddon.LOG.warn("Could not update macro backup {}; continuing with atomic save", backup, backupError);
            }
         }

         try {
            CopyOption[] var10002 = new CopyOption[]{StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING};
            AuditMacroRead.recoverDirectory(target);
            Files.move(temp, target, var10002);
            AuditMacroRead.committed(targetFile, tag);
         } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            AuditMacroRead.committed(targetFile, tag);
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.error("Failed to save Autism macros", e);
      } finally {
         try {
            Files.deleteIfExists(temp);
         } catch (Exception var15) {
         }
      }
   }

   public synchronized void init(File newFile, boolean seedIfMissing, boolean announce) {
      if (newFile != null) {
         File old = this.init_File;
         if (old == null || !old.equals(newFile)) {
            boolean prevSuppress = this.init_Z;
            this.init_Z = true;

            try {
               this.load_V();
               this.init_File = newFile;
               File backup = new File(newFile.getParentFile(), newFile.getName() + ".bak");
               if (AuditMacroRead.exists_Z_File(newFile) || backup.exists()) {
                  this.init_List = new ArrayList<>();
                  this.init_cY = new MacroFolderLayout();
                  this.init_J++;
                  this.update();
               } else if (seedIfMissing) {
                  this.load_V();
               } else {
                  this.init_List = new ArrayList<>();
                  this.init_cY = new MacroFolderLayout();
                  this.init_J++;
               }
            } finally {
               this.init_Z = prevSuppress;
            }

            if (announce && !this.init_Z && LanSyncManager.init_aE().write_Z()) {
               LanSyncManager.init_aE().push();
            }
         }
      }
   }

   public synchronized void write() {
      this.init(init_File(), false, true);
   }

   public synchronized File load_File() {
      return this.init_File;
   }

   public synchronized void update() {
      Path target = this.init_File.toPath();
      Path backup = target.resolveSibling(this.init_File.getName() + ".bak");
      if (AuditMacroRead.exists_Z_Path_LinkOptionArr(target) || Files.exists(backup)) {
         try {
            if (!AuditMacroRead.exists_Z_Path_LinkOptionArr(target)) {
               throw new IllegalStateException("Main macro file is missing");
            }

            this.init_List = this.init_List_Path(target);
            this.init_J++;
         } catch (Exception e) {
            AutismClientAddon.LOG.error("Failed to load Autism macros; trying backup", e);
            if (!Files.exists(backup)) {
               return;
            }

            try {
               this.init_List = this.init_List_Path(backup);
               this.init_J++;
               AutismClientAddon.LOG.warn("Recovered Autism macros from {}", backup);
            } catch (Exception backupError) {
               AutismClientAddon.LOG.error("Failed to load Autism macro backup", backupError);
            }
         }
      }
   }

   private List<AutismMacro> init_List_Path(Path path) throws IOException {
      CompoundTag tag = AuditMacroRead.read(path);
      if (tag == null) {
         throw new IllegalStateException("Macro file was empty");
      }

      if (tag.get("macros") instanceof ListTag list) {
         ArrayList var12 = new ArrayList();
         HashMap seen = new HashMap();

         for (Tag element : list) {
            if (element instanceof CompoundTag macroTag) {
               try {
                  AutismMacro macro = new AutismMacro().init_aM_CompoundTag(macroTag);
                  Integer previous = (Integer)seen.get(MacroNameNormalizer.init(macro.init_Str));
                  if (previous != null) {
                     AutismClientAddon.LOG.warn("Dropping macro '{}' shadowed by a name that differs only in case", ((AutismMacro)var12.get(previous)).init_Str);
                     var12.set(previous, macro);
                  } else {
                     seen.put(MacroNameNormalizer.init(macro.init_Str), var12.size());
                     var12.add(macro);
                  }
               } catch (Throwable macroError) {
                  AutismClientAddon.LOG.warn("Skipping one damaged macro entry from {}", path, macroError);
               }
            }
         }

         this.init_cY = MacroFolderLayout.init_cY_Tag_Collection(tag.get("folders"), var12);
         return var12;
      } else {
         throw new IllegalStateException("Macro file has no macro list");
      }
   }
}
