package autismclient.util.g.a;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import net.minecraft.network.chat.Component;

public final class TaskQueue {
   private static final long init_J = 120000L;
   private static final long load_J = 600L;
   private static final long read_J = 1900L;
   private static final long write = 1500L;
   private static final long update = 3500L;
   private static final double init_D = 0.001;
   private static final int init_I = 3;
   private static final int load_I = 8192;
   private static final Map<Long, String> init_Map = new ConcurrentHashMap<>();
   private final TaskQueue.Inner_DataUtil init_TaskQueue_DataUtil;
   private final Executor init_Executor;
   private final DataUtil init_DataUtil = new DataUtil();
   private final Random init_Random = new Random();
   private volatile long sync;
   private volatile long flush;
   private volatile String init_Str = "";
   private volatile DataUtil.Inner_DataUtil init_DataUtil_DataUtil;
   private volatile long close;
   private volatile long open = -9223372036854775808L;
   private volatile byte[] init_BArr;
   private volatile long poll;
   private volatile String load_Str;
   private volatile long push;
   private volatile String[] init_StrArr;
   private volatile int read_I;
   private volatile long pop;
   private volatile boolean init_Z;
   private volatile boolean load_Z;
   private volatile String read_Str;

   public TaskQueue(TaskQueue.Inner_DataUtil host, Executor worker) {
      this.init_TaskQueue_DataUtil = host;
      this.init_Executor = worker;
   }

   public void init_V_J(long now) {
      if (this.push != 0L && this.read_Str != null && !this.init_Z && !this.load_Z) {
         if (init_Map.size() < 8192) {
            init_Map.putIfAbsent(this.push, this.read_Str);
         }

         this.init_TaskQueue_DataUtil.read("Solved captcha");
      }

      this.sync = now + 120000L;
      this.init_DataUtil_DataUtil = null;
      this.close = 0L;
      this.open = -9223372036854775808L;
      this.init_BArr = null;
      this.poll = 0L;
      this.load_Str = null;
      this.push = 0L;
      this.init_StrArr = null;
      this.read_I = 0;
      this.pop = 0L;
      this.init_Z = false;
      this.load_Z = false;
      this.read_Str = null;
   }

   private boolean load_Z(long now) {
      return now < this.sync;
   }

   public boolean init() {
      return this.push != 0L || this.init_DataUtil_DataUtil != null || this.init_StrArr != null;
   }

   public boolean init_Z_J(long now) {
      return this.load_Z(now) && this.init();
   }

   private void read(long now) {
      this.sync = now + 120000L;
   }

   public void init(Component component, long now) {
      if (component != null) {
         if (this.push != 0L && init_Z_Component(component)) {
            this.init_Z = true;
            init_Map.remove(this.push);
            this.write(now);
         } else if (this.load_Z(now)) {
            DataUtil.Inner_DataUtil answer = this.init_DataUtil.init_DataUtil_DataUtil_Component(component);
            if (answer != null) {
               this.read(now);
               this.init(answer, now);
            }
         }
      }
   }

   private static boolean init_Z_Component(Component c) {
      String s = c.getString().toLowerCase(Locale.ROOT);
      return s.contains("incorrect") || s.contains("wrong") || s.contains("try again") && !s.contains("➤");
   }

   private void write(long now) {
      String[] cands = this.init_StrArr;
      int idx = this.read_I + 1;
      this.read_I = idx;
      this.pop = 0L;
      if (cands != null && idx < cands.length) {
         this.read(now);
         this.init(new DataUtil.Inner_DataUtil(DataUtil.Inner_CachePool.init, cands[idx]), now);
      }
   }

   public void init(byte[] colors, long now) {
      if (this.load_Z(now) && colors != null && colors.length == 16384) {
         long hash = init_J_BArr(colors);
         if (this.open != hash && hash != this.push) {
            this.push = hash;
            this.read_I = 0;
            this.init_StrArr = null;
            this.init_Z = false;
            this.load_Z = false;
            this.read_Str = null;
            this.read(now);
            String cached = init_Map.get(hash);
            if (cached != null) {
               this.init_StrArr = new String[]{cached};
               this.init(new DataUtil.Inner_DataUtil(DataUtil.Inner_CachePool.init, cached), now);
            }

            if (this.open != -9223372036854775808L) {
               this.init_BArr = colors;
               this.poll = hash;
               this.load_Str = cached;
            } else {
               this.open = hash;
               this.init(colors, hash, cached, now);
            }
         }
      }
   }

   private void init(byte[] colors, long hash, String cachedFirst, long now) {
      this.init_Executor.execute(() -> {
         try {
            CachePool img = CachePool.init(colors);
            List<BufferHelper.Inner_CachePool> ranked = BufferHelper.init_List_CachePool_I(img, 3);
            List<String> list = new ArrayList<>();
            if (cachedFirst != null) {
               list.add(cachedFirst);
            }

            for (int i = 0; i < ranked.size(); i++) {
               BufferHelper.Inner_CachePool r = ranked.get(i);
               if (r.init_Z()) {
                  boolean isBest = list.isEmpty();
                  if ((isBest || !(r.init_D() < 0.001)) && !list.contains(r.init_Str())) {
                     list.add(r.init_Str());
                  }
               }
            }

            if (!list.isEmpty() && hash == this.push) {
               this.init_StrArr = list.toArray(new String[0]);
               this.read(now);
               if (cachedFirst == null) {
                  int idx = Math.min(this.read_I, this.init_StrArr.length - 1);
                  this.init(new DataUtil.Inner_DataUtil(DataUtil.Inner_CachePool.init, this.init_StrArr[idx]), now);
               }
            }
         } catch (Throwable var16) {
         } finally {
            this.update(hash);
         }
      });
   }

   private void update(long finishedHash) {
      byte[] next = this.init_BArr;
      long nextHash = this.poll;
      String nextCached = this.load_Str;
      this.init_BArr = null;
      this.poll = 0L;
      this.load_Str = null;
      if (next != null && nextHash == this.push && this.load_Z(System.currentTimeMillis())) {
         this.open = nextHash;
         this.init(next, nextHash, nextCached, System.currentTimeMillis());
      } else if (this.open == finishedHash) {
         this.open = -9223372036854775808L;
      }
   }

   public void load(Component component, long now) {
      if (component != null) {
         String s = component.getString().toLowerCase(Locale.ROOT);
         if (s.contains("captcha") || s.contains("code") || s.contains("verif")) {
            this.read(now);
         }
      }
   }

   private void init(DataUtil.Inner_DataUtil answer, long now) {
      if (!answer.init_Str().equals(this.init_Str) || now - this.flush >= 1500L) {
         this.init_DataUtil_DataUtil = answer;
         this.close = now + 600L + (long)(this.init_Random.nextDouble() * 1300.0);
      }
   }

   public void load_V(long now) {
      String[] cands = this.init_StrArr;
      if (this.pop != 0L && now >= this.pop && this.load_Z(now) && cands != null && this.read_I + 1 < cands.length) {
         this.load_Z = true;
         this.write(now);
      }

      DataUtil.Inner_DataUtil answer = this.init_DataUtil_DataUtil;
      if (answer != null && now >= this.close) {
         this.init_DataUtil_DataUtil = null;
         this.init_Str = answer.init_Str();
         this.flush = now;

         try {
            if (answer.init_DataUtil_CachePool() == DataUtil.Inner_CachePool.load) {
               this.init_TaskQueue_DataUtil.load(answer.init_Str());
            } else {
               this.init_TaskQueue_DataUtil.init(answer.init_Str());
            }

            if (this.push != 0L) {
               this.read_Str = answer.init_Str();
               this.init_Z = false;
               this.pop = now + 3500L;
            }
         } catch (Throwable var6) {
         }
      }
   }

   private static long init_J_BArr(byte[] data) {
      long h = -3750763034362895579L;

      for (byte b : data) {
         h ^= b & 0xFF;
         h *= 1099511628211L;
      }

      return h;
   }

   public interface Inner_DataUtil {
      void init(String var1);

      void load(String var1);

      default void read(String note) {
      }
   }
}
