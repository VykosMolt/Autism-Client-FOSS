package autismclient.util;

import autismclient.modules.Module;
import java.util.concurrent.atomic.AtomicLong;

public final class cf {
   public static final long init_J = 1L;
   public static final long load = 2L;
   public static final long read = 4L;
   public static final long write = 8L;
   public static final long update = 16L;
   public static final long sync = 32L;
   public static final long flush = 64L;
   public static final long close = 128L;
   public static final long open = 256L;
   public static final long poll = 512L;
   public static final long push = 1024L;
   public static final long pop = 2048L;
   public static final long tick = 4096L;
   public static final long run = 8192L;
   public static final long get = 16384L;
   private static final AtomicLong init_AtomicLong = new AtomicLong();
   private static volatile cf.Inner_DataUtil init_cf_DataUtil = new cf.Inner_DataUtil(-2147483648, 0L);

   private cf() {
   }

   public static cf.Inner_DataUtil init_cf_DataUtil() {
      return init_cf_DataUtil;
   }

   public static boolean init_Z_J(long mask) {
      return (init_cf_DataUtil.init_J & mask) != 0L;
   }

   public static void init_V_J_Z(long bit, boolean active) {
      if ((bit & init_J()) == 0L) {
         throw new IllegalArgumentException("Not an external activity bit: " + bit);
      }

      long current;
      long next;
      do {
         current = init_AtomicLong.get();
         next = active ? current | bit : current & ~bit;
         if (next == current) {
            return;
         }
      } while (!init_AtomicLong.compareAndSet(current, next));

      init_cf_DataUtil_I_J(autismclient.modules.al.init_I(), next);
   }

   public static void init_V_I(int revision) {
      init_cf_DataUtil_I_J(revision, init_AtomicLong.get());
   }

   private static synchronized cf.Inner_DataUtil init_cf_DataUtil_I_J(int revision, long external) {
      cf.Inner_DataUtil current = init_cf_DataUtil;
      if (current.init_I == revision && (current.init_J & init_J()) == external) {
         return current;
      }

      long bits = external;
      if (autismclient.modules.al.read_Z()) {
         bits |= 1L;
      }

      if (autismclient.modules.al.flush_Z()) {
         bits |= 2L;
      }

      if (autismclient.modules.al.write_Z()) {
         bits |= 4L;
      }

      if (autismclient.modules.al.update_Z()) {
         bits |= 8L;
      }

      if (autismclient.modules.al.sync_Z()) {
         bits |= 16L;
      }

      if (autismclient.modules.al.poll_Z()) {
         bits |= 32L;
      }

      Module hud = autismclient.modules.al.init_Module_Str("hud");
      if (hud != null && hud.isEnabled()) {
         bits |= 64L;
      }

      Module nametags = autismclient.modules.al.init_Module_Str("nametags");
      if (nametags != null && nametags.isEnabled()) {
         bits |= 128L;
      }

      Module danger = autismclient.modules.al.init_Module_Str("danger");
      if (danger != null && danger.isEnabled()) {
         bits |= 4096L;
      }

      Module trajectories = autismclient.modules.al.init_Module_Str("trajectories");
      if (trajectories != null && trajectories.isEnabled()) {
         bits |= 8192L;
      }

      Module beds = autismclient.modules.al.init_Module_Str("bed-esp");
      if (beds != null && beds.isEnabled()) {
         bits |= 16384L;
      }

      cf.Inner_DataUtil next = new cf.Inner_DataUtil(revision, bits);
      init_cf_DataUtil = next;
      return next;
   }

   private static long init_J() {
      return 3840L;
   }

   public record Inner_DataUtil(int init_I, long init_J) {
      public boolean init(long mask) {
         return (this.init_J & mask) != 0L;
      }
   }
}
