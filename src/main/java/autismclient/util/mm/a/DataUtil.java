package autismclient.util.mm.a;

import autismclient.AutismClientAddon;
import java.io.File;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public final class DataUtil {
   private static final byte init = 0;
   private static final byte load = 1;
   private static final byte read = 2;
   private static volatile boolean auditWarnedPlaintext;

   private DataUtil() {
   }

   private static File init_File() {
      try {
         if (AutismClientAddon.FOLDER != null) {
            return new File(AutismClientAddon.FOLDER, "autism-seal.key");
         }
      } catch (Throwable var1) {
      }

      File fallback = new File(System.getProperty("java.io.tmpdir", "."), "autismclient-seal");
      return new File(fallback, "autism-seal.key");
   }

   private static File load_File() {
      File key = init_File();
      return new File(key.getParentFile(), key.getName() + ".tmp");
   }

   public static byte[] init(byte[] plain) {
      if (plain == null) {
         plain = new byte[0];
      }

      byte[] dpapi = TaskQueue.init(plain);
      if (dpapi != null) {
         return init((byte)1, dpapi);
      }

      byte[] aes = update(plain);
      if (aes != null) {
         return init((byte)2, aes);
      }

      if (!auditWarnedPlaintext) {
         auditWarnedPlaintext = true;
         AutismClientAddon.LOG.warn("Credential sealing is unavailable (no DPAPI and no private key file); storing credentials unencrypted");
      }

      return init((byte)0, plain);
   }

   // Recreates the file empty with owner-only permissions; write the content after this.
   public static void auditPrivateFile(File f) throws java.io.IOException {
      java.nio.file.Path p = f.toPath();
      Files.deleteIfExists(p);
      try {
         Files.createFile(
            p,
            java.nio.file.attribute.PosixFilePermissions.asFileAttribute(
               EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
            )
         );
      } catch (UnsupportedOperationException nonPosix) {
         Files.createFile(p);
      }
      init(f);
   }

   // Copies source to target via an owner-only temp file + atomic move so an existing target (such as .bak) is never truncated on failure.
   public static void auditCopyPrivateAtomic(File source, File target) throws java.io.IOException {
      File dir = target.getParentFile();
      if (dir != null) {
         dir.mkdirs();
      }
      File tmp = new File(dir, target.getName() + ".tmp");
      try {
         auditPrivateFile(tmp);
         Files.write(tmp.toPath(), Files.readAllBytes(source.toPath()));
         try {
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException notAtomic) {
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
         }
      } catch (Throwable t) {
         try {
            Files.deleteIfExists(tmp.toPath());
         } catch (Throwable ignored) {
         }
         if (t instanceof java.io.IOException io) {
            throw io;
         }
         throw new java.io.IOException("Failed atomic private copy to " + target.getName(), t);
      }
   }

   public static byte[] load(byte[] plain) {
      byte[] sealed = init(plain);
      return sealed.length > 0 && sealed[0] != 0 ? sealed : null;
   }

   public static byte[] read(byte[] stored) {
      if (stored != null && stored.length >= 1) {
         byte[] payload = Arrays.copyOfRange(stored, 1, stored.length);
         byte[] var10000;
         switch (stored[0]) {
            case 0:
               var10000 = payload;
               break;
            case 1:
               var10000 = TaskQueue.load(payload);
               break;
            case 2:
               var10000 = sync(payload);
               break;
            default:
               var10000 = null;
         }

         return var10000;
      } else {
         return null;
      }
   }

   public static byte[] write(byte[] stored) {
      return stored != null && stored.length >= 1 && stored[0] != 0 ? read(stored) : null;
   }

   private static byte[] init(byte scheme, byte[] body) {
      byte[] out = new byte[body.length + 1];
      out[0] = scheme;
      System.arraycopy(body, 0, out, 1, body.length);
      return out;
   }

   private static byte[] update(byte[] plain) {
      try {
         byte[] key = init_BArr();
         if (key == null) {
            return null;
         }

         byte[] nonce = StreamUtil.init_BArr_I(12);
         byte[] ct = StreamUtil.init_BArr_BArr_2(key, nonce, plain, null);
         byte[] out = new byte[12 + ct.length];
         System.arraycopy(nonce, 0, out, 0, 12);
         System.arraycopy(ct, 0, out, 12, ct.length);
         return out;
      } catch (Throwable t) {
         return null;
      }
   }

   private static byte[] sync(byte[] stored) {
      try {
         if (stored != null && stored.length >= 28) {
            byte[] key = load_BArr();
            if (key == null) {
               return null;
            }

            byte[] nonce = Arrays.copyOfRange(stored, 0, 12);
            byte[] ct = Arrays.copyOfRange(stored, 12, stored.length);
            return StreamUtil.load(key, nonce, ct, null);
         } else {
            return null;
         }
      } catch (Throwable t) {
         return null;
      }
   }

   private static byte[] init_BArr() {
      byte[] existing = load_BArr();
      if (existing != null) {
         return existing;
      }

      try {
         File keyFile = init_File();
         File keyTmp = load_File();
         byte[] key = StreamUtil.init_BArr_I(32);
         File dir = keyFile.getParentFile();
         if (dir != null) {
            dir.mkdirs();
         }

         auditPrivateFile(keyTmp);
         Files.write(keyTmp.toPath(), key);
         if (!init(keyTmp)) {
            Files.deleteIfExists(keyTmp.toPath());
            return null;
         }

         try {
            Files.move(keyTmp.toPath(), keyFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException notAtomic) {
            Files.move(keyTmp.toPath(), keyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
         }

         return !init(keyFile) ? null : key;
      } catch (Throwable t) {
         return null;
      }
   }

   private static byte[] load_BArr() {
      try {
         File keyFile = init_File();
         if (!keyFile.exists()) {
            return null;
         }

         if (!init(keyFile)) {
            return null;
         }

         byte[] k = Files.readAllBytes(keyFile.toPath());
         return k != null && k.length == 32 ? k : null;
      } catch (Throwable t) {
         return null;
      }
   }

   private static boolean init(File f) {
      try {
         PosixFileAttributeView posix = Files.getFileAttributeView(f.toPath(), PosixFileAttributeView.class);
         if (posix != null) {
            Set<PosixFilePermission> ownerOnly = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(f.toPath(), ownerOnly);
            return Files.getPosixFilePermissions(f.toPath()).equals(ownerOnly);
         } else {
            f.setReadable(false, false);
            f.setWritable(false, false);
            return f.setReadable(true, true) && f.setWritable(true, true);
         }
      } catch (Throwable ignored) {
         return false;
      }
   }
}
