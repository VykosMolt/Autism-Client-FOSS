package autismclient.util;

import autismclient.AutismClientAddon;
import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;

public final class AuditMacroRead {
   private static final ConcurrentHashMap<Path, CompoundTag> pending = new ConcurrentHashMap<>();
   private static final ThreadLocal<Path> activeCommitKey = new ThreadLocal<>();
   private static final Object[] writeLocks = new Object[32];

   private AuditMacroRead() {
   }

   public static boolean hasLibrary(Path var0) {
      if (!Files.isRegularFile(var0)) {
         return false;
      }

      try {
         CompoundTag var1 = NbtIo.read(var0);
         return var1 != null && var1.get("macros") instanceof ListTag;
      } catch (IOException | RuntimeException var2) {
         return false;
      }
   }

   static boolean fileExists(File var0) {
      return pathExists(var0.toPath());
   }

   static boolean duplicateExists(File var0) {
      Path var1 = var0.toPath();
      return pending.containsKey(key(var1)) || Files.exists(var1) || Files.exists(backupOf(var1));
   }

   static boolean sameFile(File var0, File var1) {
      return var0 != null && var1 != null && key(var0.toPath()).equals(key(var1.toPath()));
   }

   static boolean pathExists(Path var0, LinkOption... var1) {
      return pending.containsKey(key(var0)) || Files.exists(var0, var1);
   }

   static CompoundTag read(Path var0) throws IOException {
      CompoundTag var1 = pending.get(key(var0));
      return var1 != null ? var1.copy() : NbtIo.read(var0);
   }

   static Path copy(Path var0, Path var1, CopyOption... var2) throws IOException {
      Path var3 = key(var0);
      synchronized (lock(var3)) {
         CompoundTag var5 = pending.get(var3);
         Path var6 = var1.toAbsolutePath().getParent();
         if (var6 != null) {
            Files.createDirectories(var6);
         }

         if (var5 == null) {
            Path var18 = var0;
            if (!hasLibrary(var0)) {
               Path var19 = backupOf(var0);
               if (hasLibrary(var19)) {
                  var18 = var19;
               }
            }

            return Files.copy(var18, var1, var2);
         } else {
            Path var7 = Files.createTempFile(var6, ".macro-copy-", ".tmp");

            Path var9;
            try {
               NbtIo.write(var5, var7);

               try {
                  return Files.move(var7, var1, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
               } catch (AtomicMoveNotSupportedException var15) {
                  var9 = Files.move(var7, var1, StandardCopyOption.REPLACE_EXISTING);
               }
            } finally {
               Files.deleteIfExists(var7);
            }

            return var9;
         }
      }
   }

   static void recoverDirectory(Path var0) throws IOException {
      if (Files.isDirectory(var0)) {
         Path var1 = backupOf(var0);
         if (Files.isSymbolicLink(var0) || !hasLibrary(var1)) {
            throw new IOException("Macro library path is a directory: " + var0);
         }

         Path var2 = Files.createTempDirectory(var0.toAbsolutePath().getParent(), ".macro-recovery-");
         Path var3 = var2.resolve(var0.getFileName());

         try {
            Files.move(var0, var3);
         } catch (IOException var7) {
            try {
               Files.delete(var2);
            } catch (IOException var6) {
               var7.addSuppressed(var6);
            }

            throw var7;
         }

         AutismClientAddon.LOG.warn("Moved blocking macro library directory {} to {}", var0, var3);
      }
   }

   static Runnable track(Runnable var0, File var1, CompoundTag var2) {
      Path var3 = key(var1.toPath());
      synchronized (lock(var3)) {
         pending.put(var3, var2);
      }

      return () -> {
         synchronized (lock(var3)) {
            if (pending.get(var3) == var2) {
               Path var4 = activeCommitKey.get();
               activeCommitKey.set(var3);

               try {
                  var0.run();
               } finally {
                  if (var4 == null) {
                     activeCommitKey.remove();
                  } else {
                     activeCommitKey.set(var4);
                  }
               }
            }
         }
      };
   }

   static void committed(File var0, CompoundTag var1) {
      Path var2 = activeCommitKey.get();
      if (var2 != null) {
         pending.computeIfPresent(var2, (var1xx, var2x) -> (CompoundTag)(var2x == var1 ? null : var2x));
      }

      Path var3 = key(var0.toPath());
      if (!var3.equals(var2)) {
         pending.computeIfPresent(var3, (var1xx, var2x) -> (CompoundTag)(var2x == var1 ? null : var2x));
      }
   }

   private static Path backupOf(Path var0) {
      return var0.resolveSibling(var0.getFileName() + ".bak");
   }

   static void delete(File var0, File var1) {
      Path var2 = key(var0.toPath());
      synchronized (lock(var2)) {
         pending.remove(var2);
         deletePath(var0.toPath());
         deletePath(var1.toPath());
      }
   }

   private static void deletePath(Path var0) {
      try {
         Files.deleteIfExists(var0);
      } catch (IOException | SecurityException var2) {
         AutismClientAddon.LOG.warn("Could not delete macro library {}", var0, var2);
      }
   }

   private static Path key(Path var0) {
      Path var1 = var0.toAbsolutePath();

      for (Path var2 = var1.getParent(); var2 != null; var2 = var2.getParent()) {
         try {
            return var2.toRealPath().resolve(var2.relativize(var1)).normalize();
         } catch (NoSuchFileException var4) {
         } catch (IOException var5) {
            break;
         }
      }

      return var1.normalize();
   }

   private static Object lock(Path var0) {
      return writeLocks[var0.hashCode() & writeLocks.length - 1];
   }

   static {
      Arrays.setAll(writeLocks, var0 -> new Object());
   }
}
