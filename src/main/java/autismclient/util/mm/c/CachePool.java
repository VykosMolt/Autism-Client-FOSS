package autismclient.util.mm.c;

import autismclient.AutismClientAddon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class CachePool {
   private static final byte init_B = -95;
   private static final byte load_B = 109;
   private static final byte read_B = 1;
   private static final int load_I = 23;
   private static final int read_I = 2400;
   public static final int init_I = 65536;
   private static final int write = 29;
   private static final int update = 256;
   private static final long init_J = 60000L;
   private static final long load_J = 180000L;
   private final List<StreamUtil> init_List = new ArrayList<>();
   private final Map<String, Consumer<byte[]>> init_Map = new ConcurrentHashMap<>();
   private final Map<String, CachePool.Inner_DataUtil> load_Map = new ConcurrentHashMap<>();
   private final Map<String, Long> read_Map = new ConcurrentHashMap<>();
   private volatile long read_J;
   private volatile long auditLastSweepMs;
   private static final int AUDIT_MAX_DEDUP = 8192;

   public CachePool(List<StreamUtil> relays) {
      this.init_List.addAll(relays);
   }

   public List<BufferHelper> init_List() {
      List<BufferHelper> out = new ArrayList<>();

      for (StreamUtil r : this.init_List) {
         out.add(r.init_BufferHelper());
      }

      return out;
   }

   public void init_V_Str_Consumer(String topic, Consumer<byte[]> onEnvelope) {
      this.init_Map.put(topic, onEnvelope);

      for (StreamUtil r : this.init_List) {
         r.init(topic, frame -> this.init_V_Str_BArr(topic, frame));
      }
   }

   public void init(String topic) {
      this.init_Map.remove(topic);

      for (StreamUtil r : this.init_List) {
         r.init_V_Str(topic);
      }

      this.load_Map.values().removeIf(g -> g.init_Str.equals(topic));
   }

   public void load(String topic, Consumer<byte[]> onMessage) {
      for (StreamUtil r : this.init_List) {
         r.init(topic, payload -> {
            try {
               onMessage.accept(payload);
            } catch (Throwable t) {
               this.init_V_Str_Throwable("Matchmaking: sys-event handler threw on a message", t);
            }
         });
      }
   }

   public void load(String topic) {
      for (StreamUtil r : this.init_List) {
         r.init_V_Str(topic);
      }
   }

   public boolean init_Z_Str_BArr(String topic, byte[] envelope) {
      return this.init_Z_Str(topic, envelope, false);
   }

   public boolean init_Z_Str(String topic, byte[] envelope, boolean durable) {
      if (envelope.length > 65536) {
         return false;
      }

      byte[] groupId = autismclient.util.mm.a.StreamUtil.init_BArr_I(16);
      int total = Math.max(1, (envelope.length + 2400 - 1) / 2400);

      for (int index = 0; index < total; index++) {
         int off = index * 2400;
         int len = Math.min(2400, envelope.length - off);
         byte[] frame = new byte[23 + len];
         frame[0] = -95;
         frame[1] = 109;
         frame[2] = 1;
         System.arraycopy(groupId, 0, frame, 3, 16);
         init_V_BArr(frame, 19, total);
         init_V_BArr(frame, 21, index);
         System.arraycopy(envelope, off, frame, 23, len);

         for (StreamUtil r : this.init_List) {
            r.init(topic, frame, durable);
         }
      }

      return true;
   }

   public void init_V() {
      for (StreamUtil r : this.init_List) {
         try {
            r.init_V();
         } catch (Throwable var4) {
         }
      }
   }

   public void load() {
      for (StreamUtil r : this.init_List) {
         try {
            r.load();
         } catch (Throwable var4) {
         }
      }

      this.init_Map.clear();
      this.load_Map.clear();
      this.read_Map.clear();
   }

   private void init_V_Str_BArr(String topic, byte[] frame) {
      try {
         if (frame.length < 23) {
            return;
         }

         if (frame[0] != -95 || frame[1] != 109 || frame[2] != 1) {
            return;
         }

         int total = init_I_BArr_I(frame, 19);
         int index = init_I_BArr_I(frame, 21);
         if (total < 1 || total > 29 || index < 0 || index >= total) {
            return;
         }

         int payloadLen = frame.length - 23;
         if (payloadLen > (total == 1 ? 65536 : 2400)) {
            return;
         }

         String gid = autismclient.util.mm.a.StreamUtil.init_Str_BArr(Arrays.copyOfRange(frame, 3, 19));
         String scopedKey = topic + "\u0000" + gid;
         long now = System.currentTimeMillis();
         this.init(now);
         if (this.read_Map.containsKey(scopedKey)) {
            return;
         }

         byte[] payload = Arrays.copyOfRange(frame, 23, frame.length);
         if (total == 1) {
            if (this.read_Map.size() < AUDIT_MAX_DEDUP) {
               this.read_Map.put(scopedKey, now);
            }
            this.load(topic, payload);
            return;
         }

         if (this.load_Map.size() >= 256 && !this.load_Map.containsKey(scopedKey)) {
            return;
         }

         CachePool.Inner_DataUtil group = this.load_Map.computeIfAbsent(scopedKey, k -> new CachePool.Inner_DataUtil(topic, total, now));
         if (group.init_I != total || !group.init_Str.equals(topic)) {
            return;
         }

         byte[] complete = group.init(index, payload);
         if (complete != null) {
            this.load_Map.remove(scopedKey);
            if (this.read_Map.size() < AUDIT_MAX_DEDUP) {
               this.read_Map.put(scopedKey, now);
            }
            if (complete.length <= 65536) {
               this.load(topic, complete);
            }
         }
      } catch (Throwable t) {
         this.init_V_Str_Throwable("Matchmaking: dropped a bad inbound frame", t);
      }
   }

   private void load(String topic, byte[] envelope) {
      Consumer<byte[]> consumer = this.init_Map.get(topic);
      if (consumer != null) {
         try {
            consumer.accept(envelope);
         } catch (Throwable t) {
            this.init_V_Str_Throwable("Matchmaking: inbound handler threw on a frame", t);
         }
      }
   }

   private void init_V_Str_Throwable(String msg, Throwable t) {
      long now = System.currentTimeMillis();
      if (now - this.read_J >= 1000L) {
         this.read_J = now;
         AutismClientAddon.LOG.warn(msg, t);
      }
   }

   private void init(long now) {
      if (now - this.auditLastSweepMs < 1000L && this.read_Map.size() < AUDIT_MAX_DEDUP) {
         return;
      }
      this.auditLastSweepMs = now;
      this.load_Map.values().removeIf(g -> now - g.init_J > 60000L);
      this.read_Map.values().removeIf(t -> now - t > 180000L);
   }

   private static void init_V_BArr(byte[] b, int off, int v) {
      b[off] = (byte)(v >>> 8 & 0xFF);
      b[off + 1] = (byte)(v & 0xFF);
   }

   private static int init_I_BArr_I(byte[] b, int off) {
      return (b[off] & 0xFF) << 8 | b[off + 1] & 0xFF;
   }

   private static final class Inner_DataUtil {
      final String init_Str;
      final int init_I;
      final long init_J;
      final byte[][] init_BArr2;
      int load;

      Inner_DataUtil(String topic, int total, long createdMs) {
         this.init_Str = topic;
         this.init_I = total;
         this.init_J = createdMs;
         this.init_BArr2 = new byte[total][];
      }

      synchronized byte[] init(int index, byte[] payload) {
         if (this.init_BArr2[index] != null) {
            return null;
         }

         this.init_BArr2[index] = payload;
         if (++this.load < this.init_I) {
            return null;
         }

         int size = 0;

         for (byte[] c : this.init_BArr2) {
            size += c.length;
         }

         byte[] out = new byte[size];
         int off = 0;

         for (byte[] c : this.init_BArr2) {
            System.arraycopy(c, 0, out, off, c.length);
            off += c.length;
         }

         return out;
      }
   }
}
