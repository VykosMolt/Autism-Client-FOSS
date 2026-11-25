package autismclient.util.mm;

import autismclient.AutismClientAddon;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class TextFormat {
   private static final long init_J = 120000L;
   private static final long load_J = 300000L;
   private static final int init_I = 24;
   private static final double init_D = 6.0;
   private static final int load_I = 8192;
   private static final int read_I = 4096;
   private static final long read_J = 600000L;
   private static final int write_I = 256;
   private static final double load_D = 128.0;
   private final Map<String, Long> init_Map = new ConcurrentHashMap<>();
   private final Map<String, TextFormat.Inner_DataUtil> load_Map = new ConcurrentHashMap<>();
   private final TextFormat.Inner_DataUtil init_TextFormat_DataUti = new TextFormat.Inner_DataUtil(System.currentTimeMillis(), 256.0, 128.0);
   public final AtomicLong init_AtomicLong = new AtomicLong();
   public final AtomicLong load_AtomicLong = new AtomicLong();
   private volatile long write_J;
   public final AtomicLong read_AtomicLong = new AtomicLong();
   public final AtomicLong write_AtomicLong = new AtomicLong();
   public final AtomicLong update = new AtomicLong();

   public static int init_I_UiHelper(UiHelper type) {
      if (type == null) {
         return 0;
      }

      char var10000;
      switch (type) {
         case load:
            var10000 = 2048;
            break;
         case write:
            var10000 = 4096;
            break;
         case read:
         case update:
            var10000 = '耀';
            break;
         case close:
            var10000 = '\uf000';
            break;
         case init_UiHelper:
            var10000 = 2048;
            break;
         case sync:
            var10000 = 256;
            break;
         case flush:
            var10000 = '@';
            break;
         case open:
            var10000 = '@';
            break;
         case poll:
            var10000 = '@';
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public boolean init_Z_UiHelper_I(UiHelper type, int payloadLen) {
      boolean ok = payloadLen <= init_I_UiHelper(type);
      if (!ok) {
         this.write_AtomicLong.incrementAndGet();
      }

      return ok;
   }

   public boolean init() {
      if (!this.init_TextFormat_DataUti.init(System.currentTimeMillis())) {
         this.read_AtomicLong.incrementAndGet();
         return false;
      } else {
         return true;
      }
   }

   public boolean init_Z_SyncManager(SyncManager env) {
      long now = System.currentTimeMillis();
      this.init_V_J(now);
      long serverNow = IoWorker.init();
      if (Math.abs(serverNow - env.init_J) > 120000L) {
         this.load_AtomicLong.incrementAndGet();
         if (now - this.write_J > 10000L) {
            this.write_J = now;
            AutismClientAddon.LOG
               .info(
                  "[mm-safety] dropped stale frame: sender timestamp {}s {} server time (limit ±120s)",
                  Math.abs(env.init_J - serverNow) / 1000L,
                  env.init_J > serverNow ? "ahead of" : "behind"
               );
         }

         return false;
      } else {
         String fp = env.init_Str();
         if (!this.init_Z_UiHelper_I(env.init_UiHelper(), env.update.length)) {
            return false;
         } else {
            String msgKey = fp + ":" + init_Str_BArr(env.read);
            if (this.init_Map.putIfAbsent(msgKey, now) != null) {
               this.init_AtomicLong.incrementAndGet();
               return false;
            } else {
               TextFormat.Inner_DataUtil bucket = this.load_Map.computeIfAbsent(fp, k -> new TextFormat.Inner_DataUtil(now, 24.0, 6.0));
               if (!bucket.init(now)) {
                  this.read_AtomicLong.incrementAndGet();
                  return false;
               } else {
                  return true;
               }
            }
         }
      }
   }

   public void init_V_Str(String fpHex) {
      this.load_Map.remove(fpHex);
      this.init_Map.keySet().removeIf(k -> k.startsWith(fpHex + ":"));
   }

   private void init_V_J(long now) {
      this.init_Map.values().removeIf(t -> now - t > 300000L);
      if (this.init_Map.size() > 8192) {
         init_V_Map_I(this.init_Map, 8192);
      }

      if (this.load_Map.size() > 4096) {
         this.load_Map.entrySet().removeIf(e -> now - e.getValue().init() > 600000L);
         if (this.load_Map.size() > 4096) {
            this.init_V_I(4096);
         }
      }
   }

   private static void init_V_Map_I(Map<String, Long> map, int cap) {
      int over = map.size() - cap;
      if (over > 0) {
         List<Entry<String, Long>> snapshot = new java.util.ArrayList<>(map.size());
         for (Entry<String, Long> e : map.entrySet()) {
            Long val = e.getValue();
            snapshot.add(Map.entry(e.getKey(), val == null ? Long.MIN_VALUE : val));
         }
         snapshot.sort(Entry.comparingByValue());
         int limit = Math.min(over, snapshot.size());
         for (int i = 0; i < limit; i++) {
            map.remove(snapshot.get(i).getKey());
         }
      }
   }

   private void init_V_I(int cap) {
      int over = this.load_Map.size() - cap;
      if (over > 0) {
         List<Entry<String, Long>> snapshot = new java.util.ArrayList<>(this.load_Map.size());
         for (Entry<String, TextFormat.Inner_DataUtil> e : this.load_Map.entrySet()) {
            TextFormat.Inner_DataUtil bucket = e.getValue();
            snapshot.add(Map.entry(e.getKey(), bucket == null ? Long.MIN_VALUE : bucket.init()));
         }
         snapshot.sort(Entry.comparingByValue());
         int limit = Math.min(over, snapshot.size());
         for (int i = 0; i < limit; i++) {
            this.load_Map.remove(snapshot.get(i).getKey());
         }
      }
   }

   private static String init_Str_BArr(byte[] b) {
      StringBuilder sb = new StringBuilder(b.length * 2);

      for (byte x : b) {
         sb.append(Character.forDigit(x >> 4 & 15, 16)).append(Character.forDigit(x & 15, 16));
      }

      return sb.toString();
   }

   private static final class Inner_DataUtil {
      private final double init_D;
      private final double load;
      private double read;
      private volatile long init_J;

      Inner_DataUtil(long now, double capacity, double refillPerSec) {
         this.init_D = capacity;
         this.load = refillPerSec;
         this.read = capacity;
         this.init_J = now;
      }

      long init() {
         return this.init_J;
      }

      synchronized boolean init(long now) {
         double elapsed = (now - this.init_J) / 1000.0;
         if (elapsed > 0.0) {
            this.read = Math.min(this.init_D, this.read + elapsed * this.load);
            this.init_J = now;
         }

         if (this.read >= 1.0) {
            this.read--;
            return true;
         } else {
            return false;
         }
      }
   }
}
