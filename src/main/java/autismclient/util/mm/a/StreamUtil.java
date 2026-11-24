package autismclient.util.mm.a;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class StreamUtil {
   public static final SecureRandom init = new SecureRandom();

   private StreamUtil() {
   }

   public static byte[] init_BArr_I(int n) {
      byte[] out = new byte[n];
      init.nextBytes(out);
      return out;
   }

   public static boolean init_Z_BArr_BArr(byte[] a, byte[] b) {
      return MessageDigest.isEqual(a, b);
   }

   public static byte[] init_BArr_BArr2(byte[]... parts) {
      try {
         MessageDigest md = MessageDigest.getInstance("SHA-256");

         for (byte[] p : parts) {
            md.update(p);
         }

         return md.digest();
      } catch (Exception e) {
         throw new StreamUtil.Inner_DataUtil("sha256", e);
      }
   }

   public static byte[] init_BArr_Str(String s) {
      return s.getBytes(StandardCharsets.UTF_8);
   }

   public static byte[] init_BArr_BArr_BArr(byte[] key, byte[] data) {
      try {
         Mac mac = Mac.getInstance("HmacSHA256");
         mac.init(new SecretKeySpec(key.length == 0 ? new byte[1] : key, "HmacSHA256"));
         return mac.doFinal(data);
      } catch (Exception e) {
         throw new StreamUtil.Inner_DataUtil("hmac", e);
      }
   }

   public static byte[] load(byte[] salt, byte[] ikm) {
      return init_BArr_BArr_BArr(salt, ikm);
   }

   public static byte[] init_BArr_BArr(byte[] prk, String info, int length) {
      byte[] infoBytes = init_BArr_Str(info);
      byte[] out = new byte[length];
      byte[] t = new byte[0];
      int pos = 0;

      for (byte counter = 1; pos < length; counter++) {
         byte[] input = new byte[t.length + infoBytes.length + 1];
         System.arraycopy(t, 0, input, 0, t.length);
         System.arraycopy(infoBytes, 0, input, t.length, infoBytes.length);
         input[input.length - 1] = counter;
         t = init_BArr_BArr_BArr(prk, input);
         int n = Math.min(t.length, length - pos);
         System.arraycopy(t, 0, out, pos, n);
         pos += n;
      }

      return out;
   }

   public static byte[] init_BArr_BArr(byte[] salt, byte[] ikm, String info, int length) {
      return init_BArr_BArr(load(salt, ikm), info, length);
   }

   public static byte[] init_BArr_CArr(char[] password, byte[] salt, int iterations, int dkLenBytes) {
      try {
         SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
         PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, dkLenBytes * 8);
         return f.generateSecret(spec).getEncoded();
      } catch (Exception e) {
         throw new StreamUtil.Inner_DataUtil("pbkdf2", e);
      }
   }

   public static byte[] init_BArr_BArr_2(byte[] key32, byte[] nonce12, byte[] plaintext, byte[] aad) {
      try {
         Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
         c.init(1, new SecretKeySpec(key32, "AES"), new GCMParameterSpec(128, nonce12));
         if (aad != null) {
            c.updateAAD(aad);
         }

         return c.doFinal(plaintext);
      } catch (Exception e) {
         throw new StreamUtil.Inner_DataUtil("aesGcmSeal", e);
      }
   }

   public static byte[] load(byte[] key32, byte[] nonce12, byte[] ciphertext, byte[] aad) {
      try {
         Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
         c.init(2, new SecretKeySpec(key32, "AES"), new GCMParameterSpec(128, nonce12));
         if (aad != null) {
            c.updateAAD(aad);
         }

         return c.doFinal(ciphertext);
      } catch (AEADBadTagException badTag) {
         return null;
      } catch (Exception e) {
         return null;
      }
   }

   public static KeyPair init_KeyPair() {
      try {
         return KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
      } catch (Exception e) {
         throw new StreamUtil.Inner_DataUtil("genEd25519", e);
      }
   }

   public static byte[] init_BArr_PrivateKey_BArr(PrivateKey priv, byte[] message) {
      try {
         Signature s = Signature.getInstance("Ed25519");
         s.initSign(priv);
         s.update(message);
         return s.sign();
      } catch (Exception e) {
         throw new StreamUtil.Inner_DataUtil("ed25519Sign", e);
      }
   }

   public static boolean init_Z_PublicKey(PublicKey pub, byte[] message, byte[] signature) {
      try {
         Signature s = Signature.getInstance("Ed25519");
         s.initVerify(pub);
         s.update(message);
         return s.verify(signature);
      } catch (Exception e) {
         return false;
      }
   }

   public static PublicKey init_PublicKey_BArr(byte[] spki) {
      try {
         return KeyFactory.getInstance("Ed25519").generatePublic(new X509EncodedKeySpec(spki));
      } catch (Exception e) {
         throw new StreamUtil.Inner_DataUtil("ed25519Pub", e);
      }
   }

   public static PrivateKey init_PrivateKey_BArr(byte[] pkcs8) {
      try {
         return KeyFactory.getInstance("Ed25519").generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
      } catch (Exception e) {
         throw new StreamUtil.Inner_DataUtil("ed25519Priv", e);
      }
   }

   public static KeyPair load() {
      try {
         return KeyPairGenerator.getInstance("X25519").generateKeyPair();
      } catch (Exception e) {
         throw new StreamUtil.Inner_DataUtil("genX25519", e);
      }
   }

   public static byte[] init_BArr_PrivateKey_PublicKey(PrivateKey priv, PublicKey peerPub) {
      try {
         KeyAgreement ka = KeyAgreement.getInstance("X25519");
         ka.init(priv);
         ka.doPhase(peerPub, true);
         return ka.generateSecret();
      } catch (Exception e) {
         throw new StreamUtil.Inner_DataUtil("x25519Agree", e);
      }
   }

   public static PublicKey load_PublicKey(byte[] spki) {
      try {
         return KeyFactory.getInstance("X25519").generatePublic(new X509EncodedKeySpec(spki));
      } catch (Exception e) {
         throw new StreamUtil.Inner_DataUtil("x25519Pub", e);
      }
   }

   public static PrivateKey load_PrivateKey(byte[] pkcs8) {
      try {
         return KeyFactory.getInstance("X25519").generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
      } catch (Exception e) {
         throw new StreamUtil.Inner_DataUtil("x25519Priv", e);
      }
   }

   public static byte[] init_BArr_PublicKey(PublicKey key) {
      return key.getEncoded();
   }

   public static byte[] init_BArr_BArr_I(byte[] data, int n) {
      return Arrays.copyOf(data, n);
   }

   public static String init_Str_BArr(byte[] data) {
      StringBuilder sb = new StringBuilder(data.length * 2);

      for (byte b : data) {
         sb.append(Character.forDigit(b >> 4 & 15, 16)).append(Character.forDigit(b & 15, 16));
      }

      return sb.toString();
   }

   public static final class Inner_DataUtil extends RuntimeException {
      public Inner_DataUtil(String message, Throwable cause) {
         super(message, cause);
      }
   }
}
