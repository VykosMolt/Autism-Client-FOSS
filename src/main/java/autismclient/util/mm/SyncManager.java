package autismclient.util.mm;

import java.nio.ByteBuffer;
import java.security.PublicKey;

public final class SyncManager {
   public static final byte init_B = 2;
   private static final int load_I = 62;
   public final int init_I;
   public final byte[] init_BArr;
   public final byte[] load_BArr;
   public final byte[] read;
   public final long init_J;
   public final long load_J;
   public final byte[] write;
   public final byte[] update;

   private SyncManager(int typeId, byte[] senderFp, byte[] lobbyTag, byte[] msgId, long timestamp, long seq, byte[] senderSpki, byte[] payload) {
      this.init_I = typeId;
      this.init_BArr = senderFp;
      this.load_BArr = lobbyTag;
      this.read = msgId;
      this.init_J = timestamp;
      this.load_J = seq;
      this.write = senderSpki;
      this.update = payload;
   }

   public UiHelper init_UiHelper() {
      return UiHelper.init(this.init_I);
   }

   public String init_Str() {
      return autismclient.util.mm.a.StreamUtil.init_Str_BArr(this.init_BArr);
   }

   public static String init_Str_BArr(byte[] frame) {
      if (frame != null && frame.length >= 62) {
         byte[] fp = new byte[8];
         System.arraycopy(frame, 2, fp, 0, 8);
         return autismclient.util.mm.a.StreamUtil.init_Str_BArr(fp);
      } else {
         return null;
      }
   }

   public static String load(byte[] frame) {
      if (frame != null && frame.length >= 62) {
         byte[] id = new byte[16];
         System.arraycopy(frame, 18, id, 0, 16);
         return autismclient.util.mm.a.StreamUtil.init_Str_BArr(id);
      } else {
         return null;
      }
   }

   public static int init_I_BArr(byte[] frame) {
      return frame != null && frame.length >= 62 ? frame[1] & 0xFF : -1;
   }

   public static byte[] init_BArr_CachePool(
      autismclient.util.mm.a.CachePool id,
      autismclient.util.mm.a.BufferHelper session,
      autismclient.util.mm.a.ConfigStore room,
      byte[] key,
      UiHelper type,
      byte[] payload
   ) {
      byte[] senderFp = id.read();
      byte[] lobbyTag = room.init_BArr();
      byte[] msgId = autismclient.util.mm.a.StreamUtil.init_BArr_I(16);
      long timestamp = IoWorker.init();
      long seq = session.init_J();
      byte[] nonce = session.init_BArr();
      byte[] header = ByteBuffer.allocate(62)
         .put((byte)2)
         .put((byte)type.init_I)
         .put(senderFp, 0, 8)
         .put(lobbyTag, 0, 8)
         .put(msgId, 0, 16)
         .putLong(timestamp)
         .putLong(seq)
         .put(nonce, 0, 12)
         .array();
      byte[] senderSpki = id.init_BArr();
      byte[] signed = init_BArr_BArr2(header, senderSpki, payload);
      byte[] sig = id.init_BArr_BArr(signed);
      byte[] inner = ByteBuffer.allocate(2 + senderSpki.length + 2 + sig.length + payload.length)
         .putShort((short)senderSpki.length)
         .put(senderSpki)
         .putShort((short)sig.length)
         .put(sig)
         .put(payload)
         .array();
      byte[] ciphertext = autismclient.util.mm.a.StreamUtil.init_BArr_BArr_2(key, nonce, inner, header);
      byte[] frame = new byte[header.length + ciphertext.length];
      System.arraycopy(header, 0, frame, 0, header.length);
      System.arraycopy(ciphertext, 0, frame, header.length, ciphertext.length);
      return frame;
   }

   public static SyncManager init_SyncManager_BArr_BArr(byte[] frame, byte[] key) {
      try {
         if (frame.length <= 62) {
            return null;
         }

         byte[] header = new byte[62];
         System.arraycopy(frame, 0, header, 0, 62);
         ByteBuffer h = ByteBuffer.wrap(header);
         byte version = h.get();
         if (version != 2) {
            return null;
         }

         int typeId = h.get() & 255;
         byte[] senderFp = new byte[8];
         h.get(senderFp);
         byte[] lobbyTag = new byte[8];
         h.get(lobbyTag);
         byte[] msgId = new byte[16];
         h.get(msgId);
         long timestamp = h.getLong();
         long seq = h.getLong();
         byte[] nonce = new byte[12];
         h.get(nonce);
         byte[] ciphertext = new byte[frame.length - 62];
         System.arraycopy(frame, 62, ciphertext, 0, ciphertext.length);
         byte[] inner = autismclient.util.mm.a.StreamUtil.load(key, nonce, ciphertext, header);
         if (inner == null) {
            return null;
         }

         ByteBuffer b = ByteBuffer.wrap(inner);
         int spkiLen = b.getShort() & '\uffff';
         if (spkiLen > 0 && spkiLen <= b.remaining()) {
            byte[] senderSpki = new byte[spkiLen];
            b.get(senderSpki);
            int sigLen = b.getShort() & '\uffff';
            if (sigLen > 0 && sigLen <= b.remaining()) {
               byte[] sig = new byte[sigLen];
               b.get(sig);
               byte[] payload = new byte[b.remaining()];
               b.get(payload);
               byte[] expectedFp = autismclient.util.mm.a.StreamUtil.init_BArr_BArr_I(autismclient.util.mm.a.StreamUtil.init_BArr_BArr2(senderSpki), 8);
               if (!autismclient.util.mm.a.StreamUtil.init_Z_BArr_BArr(expectedFp, senderFp)) {
                  return null;
               }

               PublicKey pub = autismclient.util.mm.a.StreamUtil.init_PublicKey_BArr(senderSpki);
               byte[] signed = init_BArr_BArr2(header, senderSpki, payload);
               return !autismclient.util.mm.a.StreamUtil.init_Z_PublicKey(pub, signed, sig)
                  ? null
                  : new SyncManager(typeId, senderFp, lobbyTag, msgId, timestamp, seq, senderSpki, payload);
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

   private static byte[] init_BArr_BArr2(byte[]... parts) {
      int len = 0;

      for (byte[] p : parts) {
         len += p.length;
      }

      byte[] out = new byte[len];
      int off = 0;

      for (byte[] p : parts) {
         System.arraycopy(p, 0, out, off, p.length);
         off += p.length;
      }

      return out;
   }
}
