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
   private static MacroLibraryManager instance;
   private List<AutismMacro> macros = new ArrayList<>();
   private MacroFolderLayout folderLayout = new MacroFolderLayout();
   private volatile File libraryFile;
   private volatile long revision;
   private volatile boolean suppressLanBroadcast = false;
   private static final long load = 300L;
   private static volatile long debouncedSaveDeadlineMs = -1L;

   private MacroLibraryManager() {
      this.libraryFile = defaultLibraryFile();
      this.load();
   }

   public static File defaultLibraryFile() {
      return new File(AutismClientAddon.FOLDER, "autism_macros.nbt");
   }

   public static void writeEmptyLibrary(File file) {
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

   public static synchronized MacroLibraryManager getInstance() {
      if (instance == null) {
         instance = new MacroLibraryManager();
      }

      return instance;
   }

   public synchronized String generateUniqueName(String preferredName) {
      String baseName = preferredName != null && !preferredName.isBlank() ? preferredName.trim() : "New Macro";
      String candidate = baseName;
      int suffix = 1;

      while (this.findByName(candidate) != null) {
         candidate = baseName + " (" + suffix++ + ")";
      }

      return candidate;
   }

   public synchronized AutismMacro importMacro(AutismMacro source, String preferredName) {
      if (source == null) {
         return null;
      }

      AutismMacro copy = source.copy();
      copy.sanitizeForSharing();
      copy.name = this.generateUniqueName(preferredName != null && !preferredName.isBlank() ? preferredName : source.name);
      this.saveMacro(copy);
      return copy;
   }

   public synchronized List<AutismMacro> insertMacros(List<AutismMacro> copies, boolean imported, AutismMacro anchor) {
      List<AutismMacro> added = new ArrayList<>();
      if (copies != null && !copies.isEmpty()) {
         int anchorIndex = -1;
         if (anchor != null) {
            for (int i = 0; i < this.macros.size(); i++) {
               if (this.macros.get(i) == anchor) {
                  anchorIndex = i;
                  break;
               }
            }
         }

         Set<String> folderKeys = new HashSet<>();

         for (String folder : this.getFolders()) {
            folderKeys.add(MacroFolderLayout.key(folder));
         }

         for (AutismMacro source : copies) {
            if (source != null) {
               AutismMacro copy = source;
               if (imported) {
                  copy.sanitizeForSharing();
               }

               copy.name = this.generateUniqueName(copy.name);
               String folder;
               if (anchorIndex >= 0) {
                  folder = anchor.folder == null ? "" : anchor.folder;
               } else {
                  folder = copy.folder != null && folderKeys.contains(MacroFolderLayout.key(copy.folder)) ? this.folderLayout.canonicalName(copy.folder) : "";
               }

               copy.folder = folder;
               int index;
               if (anchorIndex >= 0) {
                  index = anchorIndex + 1 + added.size();
               } else {
                  index = this.macros.size();
                  String key = MacroFolderLayout.key(folder);

                  for (int i = this.macros.size() - 1; i >= 0; i--) {
                     AutismMacro macro = this.macros.get(i);
                     if (macro != null && MacroFolderLayout.key(macro.folder).equals(key)) {
                        index = i + 1;
                        break;
                     }
                  }
               }

               this.macros.add(Math.min(index, this.macros.size()), copy);
               added.add(copy);
            }
         }

         if (!added.isEmpty()) {
            this.save();
         }

         return added;
      } else {
         return added;
      }
   }

   public synchronized void saveMacro(AutismMacro macro) {
      if (macro != null) {
         AutismMacro existing = this.findByName(macro.name);
         if (existing != null && existing != macro) {
            this.removeMacroByIdentity(existing);
         }

         if (existing != macro) {
            this.macros.add(macro);
         }

         this.save();
      }
   }

   private boolean removeMacroByIdentity(AutismMacro macro) {
      Iterator<AutismMacro> it = this.macros.iterator();

      while (it.hasNext()) {
         if (it.next() == macro) {
            it.remove();
            return true;
         }
      }

      return false;
   }

   public synchronized AutismMacro findByName(String name) {
      if (name == null) {
         return null;
      }

      for (AutismMacro macro : this.macros) {
         if (macro != null && MacroNameNormalizer.sameName(macro.name, name)) {
            return macro;
         }
      }

      return null;
   }

   public synchronized List<AutismMacro> getMacros() {
      return new ArrayList<>(this.macros);
   }

   public synchronized List<String> getFolders() {
      return this.folderLayout.folders();
   }

   public synchronized List<MacroFolderLayout.Inner_DataUtil> getFolderEntries(String query) {
      return this.getFilteredFolderEntries(query, false, macro -> true);
   }

   public synchronized List<MacroFolderLayout.Inner_DataUtil> getFilteredFolderEntries(String query, boolean includeEmpty, Predicate<AutismMacro> filter) {
      return this.folderLayout.rows(this.macros.stream().filter(filter).toList(), query, includeEmpty);
   }

   public synchronized List<MacroFolderLayout.Inner_DataUtil> getOrderedFolderEntries(String query, boolean includeEmpty, Predicate<AutismMacro> filter) {
      return this.folderLayout.load(this.macros.stream().filter(filter).toList(), query, includeEmpty);
   }

   public synchronized boolean createFolder(String name) {
      if (!this.folderLayout.add(name)) {
         return false;
      }

      this.save();
      return true;
   }

   public synchronized boolean renameFolder(String oldName, String newName) {
      if (!this.folderLayout.renameFolder(oldName, newName, this.macros)) {
         return false;
      }

      this.save();
      return true;
   }

   public synchronized boolean reorderFolder(String folder, String beforeFolder) {
      if (!this.folderLayout.moveBefore(folder, beforeFolder)) {
         return false;
      }

      this.save();
      return true;
   }

   public synchronized boolean reorderFolderWithAnchor(String folder, String beforeFolder, String anchorMacro) {
      if (this.getFolders().stream().noneMatch(f -> MacroFolderLayout.key(f).equals(MacroFolderLayout.key(folder)))) {
         return false;
      }

      this.folderLayout.moveBefore(folder, beforeFolder);
      this.folderLayout.setAnchor(folder, anchorMacro);
      this.save();
      return true;
   }

   public synchronized String getFolderAnchor(String folder) {
      return this.folderLayout.write(folder);
   }

   private List<AutismMacro> getRootMacros() {
      List<AutismMacro> out = new ArrayList<>();

      for (AutismMacro macro : this.macros) {
         if (macro != null && macro.name != null && (macro.folder == null || macro.folder.isEmpty())) {
            out.add(macro);
         }
      }

      return out;
   }

   private void detachRootFolderAnchors(Collection<AutismMacro> leaving) {
      Set<String> keys = new HashSet<>();

      for (AutismMacro macro : leaving) {
         if (macro != null && macro.name != null && (macro.folder == null || macro.folder.isEmpty())) {
            keys.add(MacroNameNormalizer.normalize(macro.name));
         }
      }

      if (!keys.isEmpty()) {
         this.folderLayout.init_V_List_Set(this.getRootMacros(), keys);
      }
   }

   public synchronized boolean deleteFolder(String name) {
      if (!this.folderLayout.removeFolder(name, this.macros)) {
         return false;
      }

      this.save();
      return true;
   }

   public synchronized boolean moveMacroToFolder(AutismMacro macro, String folder) {
      if (macro != null && !this.macros.stream().noneMatch(m -> m == macro)) {
         String target = MacroFolderLayout.sanitize(folder);
         if (!target.isEmpty() && this.getFolders().stream().noneMatch(f -> MacroFolderLayout.key(f).equals(MacroFolderLayout.key(target)))) {
            return false;
         }

         macro.folder = this.folderLayout.canonicalName(target);
         this.save();
         return true;
      } else {
         return false;
      }
   }

   public synchronized boolean moveMacros(Collection<AutismMacro> moving, String targetFolder, String insertBeforeName) {
      return this.moveMacros(moving, targetFolder, insertBeforeName, List.of());
   }

   public synchronized boolean moveMacros(Collection<AutismMacro> moving, String targetFolder, String insertBeforeName, List<String> foldersAbove) {
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
            if (this.macros.stream().noneMatch(m -> m == macro)) {
               return false;
            }
         }

         String target = MacroFolderLayout.sanitize(targetFolder);
         if (!target.isEmpty() && this.getFolders().stream().noneMatch(f -> MacroFolderLayout.key(f).equals(MacroFolderLayout.key(target)))) {
            return false;
         }

         String canonical = this.folderLayout.canonicalName(target);
         this.detachRootFolderAnchors(block);

         for (AutismMacro macro : block) {
            this.removeMacroByIdentity(macro);
         }

         int index = -1;
         if (insertBeforeName != null) {
            for (int i = 0; i < this.macros.size(); i++) {
               AutismMacro macro = this.macros.get(i);
               if (macro != null && MacroNameNormalizer.sameName(macro.name, insertBeforeName)) {
                  index = i;
                  break;
               }
            }
         }

         if (index < 0) {
            String targetKey = MacroFolderLayout.key(canonical);

            for (int i = this.macros.size() - 1; i >= 0; i--) {
               AutismMacro macro = this.macros.get(i);
               if (macro != null && MacroFolderLayout.key(macro.folder).equals(targetKey)) {
                  index = i + 1;
                  break;
               }
            }

            if (index < 0) {
               index = this.macros.size();
            }
         }

         for (AutismMacro macro : block) {
            macro.folder = canonical;
         }

         this.macros.addAll(index, block);
         if (canonical.isEmpty() && foldersAbove != null && !foldersAbove.isEmpty()) {
            String first = block.iterator().next().name;

            for (String folder : foldersAbove) {
               this.folderLayout.setAnchor(folder, first);
            }
         }

         this.save();
         return true;
      } else {
         return false;
      }
   }

   public synchronized void toggleFolderCollapsed(String folder) {
      this.folderLayout.toggleCollapsed(folder);
      this.saveInternal(false);
   }

   public long getRevision() {
      return this.revision;
   }

   public synchronized void deleteMacro(AutismMacro macro) {
      if (macro != null && this.deleteMacroInternal(macro, true)) {
         this.save();
      }
   }

   private boolean deleteMacroInternal(AutismMacro macro, boolean announce) {
      if (MacroExecutor.isMacroNameRunning(macro.name)) {
         MacroExecutor.stopMacroByName(macro.name);
         AutismClientMessaging.sendPrefixed("§eStopped running macro before deletion: " + macro.name);
      }

      this.detachRootFolderAnchors(List.of(macro));
      if (!this.removeMacroByIdentity(macro)) {
         return false;
      }

      String deletedName = macro.name == null ? "" : macro.name;
      if (announce) {
         AutismClientMessaging.sendPrefixed("§aDeleted macro: " + macro.name);
      }

      AutismMacroEditorOverlay editor = AutismMacroEditorOverlay.getInstance();
      if (editor != null && editor.isEditing(macro)) {
         editor.update_V_10();
      }

      if (!this.suppressLanBroadcast && LanSyncManager.getInstance().isInSession()) {
         LanSyncManager.getInstance().notifyMacroDeleted(macro.name);
      }

      return true;
   }

   public void removeMacro(AutismMacro macro) {
      this.deleteMacro(macro);
   }

   public synchronized int deleteMacros(Collection<AutismMacro> macros) {
      if (macros != null && !macros.isEmpty()) {
         int removed = 0;

         for (AutismMacro macro : macros) {
            if (macro != null && this.deleteMacroInternal(macro, false)) {
               removed++;
            }
         }

         if (removed > 0) {
            this.save();
         }

         return removed;
      } else {
         return 0;
      }
   }

   public synchronized List<AutismMacro> duplicateMacros(Collection<AutismMacro> macros) {
      List<AutismMacro> copies = new ArrayList<>();
      if (macros != null && !macros.isEmpty()) {
         for (AutismMacro macro : macros) {
            if (macro != null) {
               int index = -1;

               for (int i = 0; i < this.macros.size(); i++) {
                  if (this.macros.get(i) == macro) {
                     index = i;
                     break;
                  }
               }

               if (index >= 0) {
                  AutismMacro copy = macro.copyWithName(this.generateUniqueName(macro.name));
                  copy.keyCode = -1;
                  this.macros.add(index + 1, copy);
                  copies.add(copy);
               }
            }
         }

         if (!copies.isEmpty()) {
            this.save();
         }

         return copies;
      } else {
         return copies;
      }
   }

   public void executeMacroByName(String name) {
      AutismMacro macro = this.findByName(name);
      if (macro != null) {
         macro.execute();
         AutismClientMessaging.sendPrefixed("§aExecuting macro: " + macro.name);
      } else {
         AutismClientMessaging.sendPrefixed("§cMacro not found: " + name);
      }
   }

   public void stopRunningMacro() {
      if (MacroExecutor.hasVisibleActiveRun()) {
         MacroExecutor.stopAll();
      } else {
         AutismClientMessaging.sendPrefixed("§eNo macro is currently running.");
      }
   }

   public void save() {
      this.saveInternal(true);
   }

   public void scheduleSave() {
      debouncedSaveDeadlineMs = System.currentTimeMillis() + 300L;
   }

   public static void flushScheduledSave(boolean force) {
      long due = debouncedSaveDeadlineMs;
      if (due >= 0L && (force || System.currentTimeMillis() >= due)) {
         getInstance().save();
      }
   }

   private void saveInternal(boolean announce) {
      debouncedSaveDeadlineMs = -1L;
      boolean broadcast;
      synchronized (this) {
         this.revision++;
         File target = this.libraryFile;
         broadcast = announce && !this.suppressLanBroadcast;
         this.folderLayout.discoverFolders(this.macros);
         this.folderLayout.init_V_List(this.getRootMacros());
         CompoundTag tag = new CompoundTag();
         ListTag list = new ListTag();

         for (AutismMacro macro : this.macros) {
            if (macro != null) {
               list.add(macro.toTag());
            }
         }

         tag.put("macros", list);
         tag.put("folders", this.folderLayout.toTag());
         DebouncedPersistenceScheduler.schedule("macro-library:" + target.getAbsolutePath(), AuditMacroRead.track(() -> writeAtomicNbt(target, tag), target, tag));
      }

      if (broadcast && LanSyncManager.getInstance().isInSession()) {
         LanSyncManager.getInstance().broadcastLocalMacroRoster();
      }
   }

   private static void writeAtomicNbt(File targetFile, CompoundTag tag) {
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

   public synchronized void switchLibraryFile(File newFile, boolean seedIfMissing, boolean announce) {
      if (newFile != null) {
         File old = this.libraryFile;
         if (old == null || !old.equals(newFile)) {
            boolean prevSuppress = this.suppressLanBroadcast;
            this.suppressLanBroadcast = true;

            try {
               this.save();
               this.libraryFile = newFile;
               File backup = new File(newFile.getParentFile(), newFile.getName() + ".bak");
               if (AuditMacroRead.fileExists(newFile) || backup.exists()) {
                  this.macros = new ArrayList<>();
                  this.folderLayout = new MacroFolderLayout();
                  this.revision++;
                  this.load();
               } else if (seedIfMissing) {
                  this.save();
               } else {
                  this.macros = new ArrayList<>();
                  this.folderLayout = new MacroFolderLayout();
                  this.revision++;
               }
            } finally {
               this.suppressLanBroadcast = prevSuppress;
            }

            if (announce && !this.suppressLanBroadcast && LanSyncManager.getInstance().isInSession()) {
               LanSyncManager.getInstance().broadcastLocalMacroRoster();
            }
         }
      }
   }

   public synchronized void resetToDefaultLibraryFile() {
      this.switchLibraryFile(defaultLibraryFile(), false, true);
   }

   public synchronized File getLibraryFile() {
      return this.libraryFile;
   }

   public synchronized void load() {
      Path target = this.libraryFile.toPath();
      Path backup = target.resolveSibling(this.libraryFile.getName() + ".bak");
      if (AuditMacroRead.pathExists(target) || Files.exists(backup)) {
         try {
            if (!AuditMacroRead.pathExists(target)) {
               throw new IllegalStateException("Main macro file is missing");
            }

            this.macros = this.readMacrosFromPath(target);
            this.revision++;
         } catch (Exception e) {
            AutismClientAddon.LOG.error("Failed to load Autism macros; trying backup", e);
            if (!Files.exists(backup)) {
               return;
            }

            try {
               this.macros = this.readMacrosFromPath(backup);
               this.revision++;
               AutismClientAddon.LOG.warn("Recovered Autism macros from {}", backup);
            } catch (Exception backupError) {
               AutismClientAddon.LOG.error("Failed to load Autism macro backup", backupError);
            }
         }
      }
   }

   private List<AutismMacro> readMacrosFromPath(Path path) throws IOException {
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
                  AutismMacro macro = new AutismMacro().fromTag(macroTag);
                  Integer previous = (Integer)seen.get(MacroNameNormalizer.normalize(macro.name));
                  if (previous != null) {
                     AutismClientAddon.LOG.warn("Dropping macro '{}' shadowed by a name that differs only in case", ((AutismMacro)var12.get(previous)).name);
                     var12.set(previous, macro);
                  } else {
                     seen.put(MacroNameNormalizer.normalize(macro.name), var12.size());
                     var12.add(macro);
                  }
               } catch (Throwable macroError) {
                  AutismClientAddon.LOG.warn("Skipping one damaged macro entry from {}", path, macroError);
               }
            }
         }

         this.folderLayout = MacroFolderLayout.fromTag(tag.get("folders"), var12);
         return var12;
      } else {
         throw new IllegalStateException("Macro file has no macro list");
      }
   }
}
