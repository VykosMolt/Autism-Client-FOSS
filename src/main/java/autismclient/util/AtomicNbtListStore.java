package autismclient.util;

import autismclient.AutismClientAddon;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

public abstract class AtomicNbtListStore<T> {
   protected final List<T> init_List = new ArrayList<>();
   private boolean init_Z;
   private boolean auditSaveBlocked;
   private long init_J;

   public final synchronized long load_J() {
      return this.init_J;
   }

   protected abstract File init_File();

   protected abstract String init_Str();

   protected abstract T init_Obj_CompoundTag(CompoundTag var1);

   protected abstract CompoundTag init_CompoundTag_Obj(T var1);

   protected void init_V_CompoundTag(CompoundTag root) {
   }

   protected void load_V_CompoundTag(CompoundTag root) {
   }

   protected abstract String load_Str();

   protected final synchronized void load_V() {
      if (!this.init_Z) {
         this.init_Z = true;
         this.init_J++;
         File file = this.init_File();
         boolean loaded = false;
         if (file.exists()) {
            loaded = this.auditLoadFrom(file);
            if (!loaded) {
               File corrupt = new File(file.getPath() + ".corrupt-" + System.currentTimeMillis());

               try {
                  // Keep the unreadable file out of the way so the next save cannot overwrite it.
                  Files.move(file.toPath(), corrupt.toPath());
                  AutismClientAddon.LOG.warn("Kept unreadable " + this.load_Str() + " as " + corrupt.getName());
               } catch (Exception e) {
                  this.auditSaveBlocked = true;
                  AutismClientAddon.LOG.error("Could not move unreadable " + this.load_Str() + " aside; saving disabled", e);
               }
            }
         }

         if (!loaded) {
            File backup = new File(file.getPath() + ".bak");
            if (backup.exists() && this.auditLoadFrom(backup)) {
               AutismClientAddon.LOG.warn("Loaded " + this.load_Str() + " from backup");
               if (!this.auditSaveBlocked) {
                  this.read_V();
               }
            }
         }
      }
   }

   private boolean auditLoadFrom(File file) {
      try {
         CompoundTag tag = NbtIo.read(file.toPath());
         if (tag == null) {
            return false;
         }

         List<T> items = new ArrayList<>();

         for (Tag element : tag.getListOrEmpty(this.init_Str())) {
            if (element instanceof CompoundTag compoundTag) {
               items.add(this.init_Obj_CompoundTag(compoundTag));
            }
         }

         this.init_V_CompoundTag(tag);
         this.init_List.clear();
         this.init_List.addAll(items);
         return true;
      } catch (Exception e) {
         AutismClientAddon.LOG.error("Failed to load " + this.load_Str() + " from " + file.getName(), e);
         return false;
      }
   }

   public void read_V() {
      CompoundTag tag = new CompoundTag();
      List<T> snapshot;
      File target;
      synchronized (this) {
         if (this.auditSaveBlocked) {
            AutismClientAddon.LOG.error("Not saving " + this.load_Str() + ": its unreadable file could not be moved aside");
            return;
         }

         this.init_J++;
         this.load_V_CompoundTag(tag);
         snapshot = new ArrayList<>(this.init_List);
         target = this.init_File();
      }

      ListTag list = new ListTag();

      for (T item : snapshot) {
         list.add(this.init_CompoundTag_Obj(item));
      }

      tag.put(this.init_Str(), list);
      String key = "nbt:" + target.getAbsolutePath();
      File destination = target;
      DebouncedPersistenceScheduler.init(key, () -> this.init(tag, destination));
   }

   private void init(CompoundTag tag, File target) {
      File parent = target.getParentFile();
      if (parent == null) {
         parent = new File(".");
      }

      if (parent != null) {
         parent.mkdirs();
      }

      File tmp = new File(parent, target.getName() + ".tmp");
      File backup = new File(parent, target.getName() + ".bak");

      try {
         NbtIo.write(tag, tmp.toPath());
      } catch (Exception e) {
         AutismClientAddon.LOG.error("Failed to save " + this.load_Str(), e);
         tmp.delete();
         return;
      }

      try {
         if (target.exists() && NbtIo.read(target.toPath()) != null) {
            File backupTmp = new File(parent, backup.getName() + ".tmp");
            try {
               Files.copy(target.toPath(), backupTmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
               try {
                  Files.move(backupTmp.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
               } catch (Exception atomicFailed) {
                  Files.move(backupTmp.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
               }
            } catch (Exception copyErr) {
               try {
                  Files.deleteIfExists(backupTmp.toPath());
               } catch (Exception ignored) {
               }
               throw copyErr;
            }
         }
      } catch (Exception e) {
         AutismClientAddon.LOG.warn("Failed to back up " + this.load_Str(), e);
      }

      try {
         Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (Exception atomicFailed) {
         try {
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
         } catch (Exception e) {
            AutismClientAddon.LOG.error("Failed to swap in " + this.load_Str(), e);
         }
      }
   }

   public synchronized List<T> init_List() {
      return new ArrayList<>(this.init_List);
   }

   public synchronized int close_I() {
      return this.init_List.size();
   }

   public synchronized boolean init_Z_Obj(T item) {
      return this.init_List.contains(item);
   }
}
