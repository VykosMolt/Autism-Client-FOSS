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
   protected final List<T> items = new ArrayList<>();
   private boolean loaded;
   private boolean auditSaveBlocked;
   private long mutationVersion;

   public final synchronized long getMutationVersion() {
      return this.mutationVersion;
   }

   protected abstract File getStoreFile();

   protected abstract String getListTagKey();

   protected abstract T deserializeItem(CompoundTag var1);

   protected abstract CompoundTag serializeItem(T var1);

   protected void readMetadata(CompoundTag root) {
   }

   protected void writeMetadata(CompoundTag root) {
   }

   protected abstract String getStoreLabel();

   protected final synchronized void ensureLoaded() {
      if (!this.loaded) {
         this.loaded = true;
         this.mutationVersion++;
         File file = this.getStoreFile();
         boolean loaded = false;
         if (file.exists()) {
            loaded = this.auditLoadFrom(file);
            if (!loaded) {
               File corrupt = new File(file.getPath() + ".corrupt-" + System.currentTimeMillis());

               try {
                  // Keep the unreadable file out of the way so the next save cannot overwrite it.
                  Files.move(file.toPath(), corrupt.toPath());
                  AutismClientAddon.LOG.warn("Kept unreadable " + this.getStoreLabel() + " as " + corrupt.getName());
               } catch (Exception e) {
                  this.auditSaveBlocked = true;
                  AutismClientAddon.LOG.error("Could not move unreadable " + this.getStoreLabel() + " aside; saving disabled", e);
               }
            }
         }

         if (!loaded) {
            File backup = new File(file.getPath() + ".bak");
            if (backup.exists() && this.auditLoadFrom(backup)) {
               AutismClientAddon.LOG.warn("Loaded " + this.getStoreLabel() + " from backup");
               if (!this.auditSaveBlocked) {
                  this.save();
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

         for (Tag element : tag.getListOrEmpty(this.getListTagKey())) {
            if (element instanceof CompoundTag compoundTag) {
               items.add(this.deserializeItem(compoundTag));
            }
         }

         this.readMetadata(tag);
         this.items.clear();
         this.items.addAll(items);
         return true;
      } catch (Exception e) {
         AutismClientAddon.LOG.error("Failed to load " + this.getStoreLabel() + " from " + file.getName(), e);
         return false;
      }
   }

   public void save() {
      CompoundTag tag = new CompoundTag();
      List<T> snapshot;
      File target;
      synchronized (this) {
         if (this.auditSaveBlocked) {
            AutismClientAddon.LOG.error("Not saving " + this.getStoreLabel() + ": its unreadable file could not be moved aside");
            return;
         }

         this.mutationVersion++;
         this.writeMetadata(tag);
         snapshot = new ArrayList<>(this.items);
         target = this.getStoreFile();
      }

      ListTag list = new ListTag();

      for (T item : snapshot) {
         list.add(this.serializeItem(item));
      }

      tag.put(this.getListTagKey(), list);
      String key = "nbt:" + target.getAbsolutePath();
      File destination = target;
      DebouncedPersistenceScheduler.schedule(key, () -> this.writeAtomically(tag, destination));
   }

   private void writeAtomically(CompoundTag tag, File target) {
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
         AutismClientAddon.LOG.error("Failed to save " + this.getStoreLabel(), e);
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
         AutismClientAddon.LOG.warn("Failed to back up " + this.getStoreLabel(), e);
      }

      try {
         Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (Exception atomicFailed) {
         try {
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
         } catch (Exception e) {
            AutismClientAddon.LOG.error("Failed to swap in " + this.getStoreLabel(), e);
         }
      }
   }

   public synchronized List<T> getItems() {
      return new ArrayList<>(this.items);
   }

   public synchronized int size() {
      return this.items.size();
   }

   public synchronized boolean contains(T item) {
      return this.items.contains(item);
   }
}
