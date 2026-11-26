package autismclient.util.mm.a;

import autismclient.AutismClientAddon;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Locale;

public final class CachePool {
   private static final File init_File = new File(AutismClientAddon.FOLDER, "mm_identity.dat");
   private static final File load_File = new File(AutismClientAddon.FOLDER, "mm_identity.dat.bak");
   private static final File read_File = new File(AutismClientAddon.FOLDER, "mm_identity.dat.tmp");
   private static final File write_File = new File(AutismClientAddon.FOLDER, "mm_identity.key");
   private static final File update = new File(AutismClientAddon.FOLDER, "mm_identity.key.tmp");
   private static final int init_I = 3;
   private static final byte init_B = 0;
   private static final byte load_B = 1;
   private static final byte read_B = 2;
   private static final int load_I = 16384;
   private static volatile CachePool init_CachePool;
   private final KeyPair init_KeyPair;
   private final byte[] init_BArr;
   private final byte[] load_BArr;
   private final byte[] read_BArr;
   private final String init_Str;
   private final KeyPair load_KeyPair;
   private final byte[] write_BArr;

   private CachePool(KeyPair keyPair, KeyPair x25519KeyPair) {
      this.init_KeyPair = keyPair;
      this.init_BArr = StreamUtil.init_BArr_PublicKey(keyPair.getPublic());
      this.load_BArr = StreamUtil.init_BArr_BArr2(this.init_BArr);
      this.read_BArr = StreamUtil.init_BArr_BArr_I(this.load_BArr, 8);
      this.init_Str = init_Str_BArr(this.read_BArr);
      this.load_KeyPair = x25519KeyPair;
      this.write_BArr = StreamUtil.init_BArr_PublicKey(x25519KeyPair.getPublic());
   }

   public static CachePool init_CachePool() {
      CachePool local = init_CachePool;
      if (local == null) {
         synchronized (CachePool.class) {
            if (init_CachePool == null) {
               init_CachePool = load_CachePool();
            }

            local = init_CachePool;
         }
      }

      return local;
   }

   public PublicKey init_PublicKey() {
      return this.init_KeyPair.getPublic();
   }

   public byte[] init_BArr() {
      return (byte[])this.init_BArr.clone();
   }

   public byte[] load_BArr() {
      return (byte[])this.load_BArr.clone();
   }

   public byte[] read() {
      return (byte[])this.read_BArr.clone();
   }

   public String init_Str() {
      return this.init_Str;
   }

   public byte[] init_BArr_BArr(byte[] message) {
      return StreamUtil.init_BArr_PrivateKey_BArr(this.init_KeyPair.getPrivate(), message);
   }

   public byte[] write() {
      return (byte[])this.write_BArr.clone();
   }

   public byte[] load(byte[] peerX25519Spki) {
      try {
         return StreamUtil.init_BArr_PrivateKey_PublicKey(this.load_KeyPair.getPrivate(), StreamUtil.load_PublicKey(peerX25519Spki));
      } catch (Throwable t) {
         return null;
      }
   }

   public static synchronized void init_V() {
      CachePool created = new CachePool(StreamUtil.init_KeyPair(), StreamUtil.load());
      init_V_CachePool(created);
      init_CachePool = created;
   }

   public static String init_Str_BArr(byte[] fp8) {
      String hex = StreamUtil.init_Str_BArr(fp8).toUpperCase(Locale.ROOT);
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < hex.length(); i += 4) {
         if (i > 0) {
            sb.append('-');
         }

         sb.append(hex, i, Math.min(hex.length(), i + 4));
      }

      return sb.toString();
   }

   private static CachePool load_CachePool() {
      if (init_File.exists()) {
         CachePool.Inner_DataUtil primary = init_CachePool_DataUtil_File(init_File);
         if (primary != null) {
            if (primary.init_Z) {
               init_V_CachePool(primary.init_CachePool);
            }

            return primary.init_CachePool;
         }

         CachePool.Inner_DataUtil backup = load_File.exists() ? init_CachePool_DataUtil_File(load_File) : null;
         if (backup != null) {
            AutismClientAddon.LOG.warn("Matchmaking identity was unreadable; restored it from the backup copy");
            init_V_CachePool(backup.init_CachePool);
            return backup.init_CachePool;
         }

         load_V();
         AutismClientAddon.LOG
            .error(
               "Matchmaking identity is corrupt or could not be decrypted, and no usable backup exists. The old file was preserved as mm_identity.corrupt and a NEW identity was generated — your fingerprint has changed. (A DPAPI-encrypted identity only decrypts under the SAME Windows user account on the SAME machine; a copied/synced file cannot be used elsewhere.)"
            );
      }

      CachePool created = new CachePool(StreamUtil.init_KeyPair(), StreamUtil.load());
      init_V_CachePool(created);
      return created;
   }

   private static CachePool.Inner_DataUtil init_CachePool_DataUtil_File(File f) {
      try (DataInputStream in = new DataInputStream(new FileInputStream(f))) {
         int version = in.readInt();
         if (version == 1) {
            CachePool id = init(init_BArr_DataInputStream(in), init_BArr_DataInputStream(in), null, null);
            return id == null ? null : new CachePool.Inner_DataUtil(id, true);
         }

         if (version == 2) {
            byte[] spki = init_BArr_DataInputStream(in);
            int scheme = in.readUnsignedByte();
            byte[] stored = init_BArr_DataInputStream(in);
            if (spki != null && stored != null) {
               CachePool id = init(spki, init(scheme, stored), null, null);
               return id == null ? null : new CachePool.Inner_DataUtil(id, true);
            } else {
               return null;
            }
         } else if (version == 3) {
            byte[] edSpki = init_BArr_DataInputStream(in);
            int scheme = in.readUnsignedByte();
            byte[] edStored = init_BArr_DataInputStream(in);
            byte[] xSpki = init_BArr_DataInputStream(in);
            byte[] xStored = init_BArr_DataInputStream(in);
            if (edSpki != null && edStored != null && xSpki != null && xStored != null) {
               byte[] edPkcs8 = init(scheme, edStored);
               byte[] xPkcs8 = init(scheme, xStored);
               CachePool id = init(edSpki, edPkcs8, xPkcs8 != null ? xSpki : null, xPkcs8);
               if (id == null) {
                  return null;
               }

               boolean upgrade = scheme == 0 || xPkcs8 == null;
               return new CachePool.Inner_DataUtil(id, upgrade);
            } else {
               return null;
            }
         } else {
            return null;
         }
      } catch (Throwable t) {
         return null;
      }
   }

   private static byte[] init(int scheme, byte[] stored) {
      byte[] var10000;
      switch (scheme) {
         case 1:
            var10000 = WindowsDpapiBridge.load(stored);
            break;
         case 2:
            var10000 = write(stored);
            break;
         default:
            var10000 = stored;
      }

      return var10000;
   }

   private static CachePool init(byte[] edSpki, byte[] edPkcs8, byte[] xSpki, byte[] xPkcs8) {
      if (edSpki != null && edPkcs8 != null) {
         try {
            PublicKey edPub = StreamUtil.init_PublicKey_BArr(edSpki);
            PrivateKey edPriv = StreamUtil.init_PrivateKey_BArr(edPkcs8);
            KeyPair ed = new KeyPair(edPub, edPriv);
            KeyPair x;
            if (xSpki != null && xPkcs8 != null) {
               x = new KeyPair(StreamUtil.load_PublicKey(xSpki), StreamUtil.load_PrivateKey(xPkcs8));
            } else {
               x = StreamUtil.load();
            }

            return new CachePool(ed, x);
         } catch (Throwable t) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static byte[] init_BArr_DataInputStream(DataInputStream in) throws IOException {
      int len = in.readInt();
      if (len > 0 && len <= 16384) {
         byte[] out = new byte[len];
         in.readFully(out);
         return out;
      } else {
         return null;
      }
   }

   private static void load_V() {
      try {
         File q = new File(init_File.getParentFile(), "mm_identity.corrupt");
         Files.move(init_File.toPath(), q.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (Throwable var1) {
      }
   }

   private static void init_V_CachePool(CachePool identity) {
      try {
         File dir = init_File.getParentFile();
         if (dir != null) {
            dir.mkdirs();
         }

         if (init_File.exists()) {
            try {
               Files.copy(init_File.toPath(), load_File.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Throwable var12) {
            }
         }

         byte[] edSpki = identity.init_BArr;
         byte[] edPkcs8 = identity.init_KeyPair.getPrivate().getEncoded();
         byte[] xSpki = identity.write_BArr;
         byte[] xPkcs8 = identity.load_KeyPair.getPrivate().getEncoded();
         CachePool.Inner_StreamUtil sealed = init(edPkcs8, xPkcs8);

         auditEnsurePrivateFile(read_File);
         try (DataOutputStream out = new DataOutputStream(new FileOutputStream(read_File))) {
            out.writeInt(3);
            out.writeInt(edSpki.length);
            out.write(edSpki);
            out.writeByte(sealed.init_B);
            out.writeInt(sealed.init_BArr.length);
            out.write(sealed.init_BArr);
            out.writeInt(xSpki.length);
            out.write(xSpki);
            out.writeInt(sealed.load.length);
            out.write(sealed.load);
            out.flush();
         } finally {
            Arrays.fill(edPkcs8, (byte)0);
            Arrays.fill(xPkcs8, (byte)0);
         }

         init_V_File(read_File);
         init(read_File, init_File);
         init_V_File(init_File);
      } catch (Throwable t) {
         AutismClientAddon.LOG.error("Failed to persist Matchmaking identity", t);
      }
   }

   private static CachePool.Inner_StreamUtil init(byte[] edPkcs8, byte[] xPkcs8) {
      byte[] e = WindowsDpapiBridge.init(edPkcs8);
      byte[] x = WindowsDpapiBridge.init(xPkcs8);
      if (e != null && x != null) {
         return new CachePool.Inner_StreamUtil((byte)1, e, x);
      }

      e = read(edPkcs8);
      x = read(xPkcs8);
      if (e != null && x != null) {
         return new CachePool.Inner_StreamUtil((byte)2, e, x);
      }

      AutismClientAddon.LOG
         .warn(
            "Matchmaking identity stored WITHOUT encryption (no OS encryption and the key file could not be written); the private keys are protected only by file permissions."
         );
      return new CachePool.Inner_StreamUtil((byte)0, edPkcs8, xPkcs8);
   }

   private static void init(File tmp, File target) throws IOException {
      try {
         Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException notAtomic) {
         Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
   }

   private static byte[] read(byte[] pkcs8) {
      byte[] key = null;
      try {
         key = update();
         if (key == null) {
            return null;
         }

         byte[] nonce = StreamUtil.init_BArr_I(12);
         byte[] ct = StreamUtil.init_BArr_BArr_2(key, nonce, pkcs8, null);
         byte[] out = new byte[12 + ct.length];
         System.arraycopy(nonce, 0, out, 0, 12);
         System.arraycopy(ct, 0, out, 12, ct.length);
         return out;
      } catch (Throwable t) {
         return null;
      } finally {
         if (key != null) {
            Arrays.fill(key, (byte)0);
         }
      }
   }

   private static byte[] write(byte[] stored) {
      byte[] key = null;
      try {
         if (stored != null && stored.length >= 28) {
            key = sync();
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
      } finally {
         if (key != null) {
            Arrays.fill(key, (byte)0);
         }
      }
   }

   private static byte[] update() {
      byte[] existing = sync();
      if (existing != null) {
         return existing;
      }

      try {
         byte[] key = StreamUtil.init_BArr_I(32);
         File dir = write_File.getParentFile();
         if (dir != null) {
            dir.mkdirs();
         }

         auditEnsurePrivateFile(update);
         Files.write(update.toPath(), key);
         init_V_File(update);
         init(update, write_File);
         init_V_File(write_File);
         return key;
      } catch (Throwable t) {
         return null;
      }
   }

   private static void auditEnsurePrivateFile(File f) {
      try {
         Files.deleteIfExists(f.toPath());
         try {
            Files.createFile(
               f.toPath(),
               java.nio.file.attribute.PosixFilePermissions.asFileAttribute(
                  java.nio.file.attribute.PosixFilePermissions.fromString("rw-------")
               )
            );
         } catch (UnsupportedOperationException ignored) {
            Files.createFile(f.toPath());
            init_V_File(f);
         }
      } catch (Throwable ignored) {
      }
   }

   private static byte[] sync() {
      try {
         if (!write_File.exists()) {
            return null;
         }

         byte[] k = Files.readAllBytes(write_File.toPath());
         return k != null && k.length == 32 ? k : null;
      } catch (Throwable t) {
         return null;
      }
   }

   private static void init_V_File(File f) {
      try {
         f.setReadable(false, false);
         f.setReadable(true, true);
         f.setWritable(false, false);
         f.setWritable(true, true);
      } catch (Throwable var2) {
      }
   }

   private record Inner_DataUtil(CachePool init_CachePool, boolean init_Z) {
   }

   private record Inner_StreamUtil(byte init_B, byte[] init_BArr, byte[] load) {
   }
}
