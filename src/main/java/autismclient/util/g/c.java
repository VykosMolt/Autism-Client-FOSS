package autismclient.util.g;

import autismclient.util.macro.A;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.phys.Vec3;

public final class c {
   private static final Object init_Obj = new Object();
   private static long init_J;
   private static long load;
   private static ClassCache init_ClassCache;
   private static b.Inner_DataUtil init_b_DataUtil;

   private c() {
   }

   static String init(String arguments) {
      return PkgCls_a.init_Str_b(PoolEntry.init_ClassCache(), PoolEntry.init_RemotePlayer(), arguments);
   }

   static boolean init() {
      return PoolEntry.init_Z();
   }

   static b.Inner_StreamUtil init_b_StreamUtil(EventBus host) {
      ClassCache session = (ClassCache)host;
      return session.init_PositionMoveRotati() != null && session.init_PositionMoveRotati().position() != null
         ? new b.Inner_StreamUtil(session.init_PositionMoveRotati().position(), session.start_Str())
         : null;
   }

   static b.Inner_DataUtil init(EventBus host, RemotePlayer bot) {
      ClassCache session = (ClassCache)host;
      synchronized (init_Obj) {
         if (session == PoolEntry.init_ClassCache() && bot != null && session.next_Z() && !session.put_Z() && session.stop_Z()) {
            b.Inner_DataUtil handle = new b.Inner_DataUtil(session.open_Str(), bot.getId(), ++init_J, ++load, 0L);
            init_ClassCache = session;
            init_b_DataUtil = handle;
            PoolEntry.init_V_ClassCache_RemotePlayer(session, bot);
            return handle;
         } else {
            return null;
         }
      }
   }

   static void init(EventBus host, b.Inner_DataUtil handle, Vec3 position, boolean onGround) {
      ClassCache session = (ClassCache)host;
      synchronized (init_Obj) {
         if (init(session, handle) && position != null) {
            PoolEntry.init_V_ClassCache(session, position, onGround);
         }
      }
   }

   static boolean init(EventBus host, b.Inner_DataUtil handle, boolean onGround) {
      ClassCache session = (ClassCache)host;
      synchronized (init_Obj) {
         return init(session, handle) && session.next_Z() && session.init_Z_Z(onGround);
      }
   }

   static boolean init(EventBus host, b.Inner_DataUtil handle, List<A.Inner_DataUtil> steps) {
      ClassCache session = (ClassCache)host;
      synchronized (init_Obj) {
         return init(session, handle) && session.next_Z() && session.init_Z_List(steps);
      }
   }

   static String init(EventBus host, b.Inner_DataUtil handle, String dimension) {
      ClassCache session = (ClassCache)host;
      synchronized (init_Obj) {
         if (!init(session, handle) || PoolEntry.init_ClassCache() != session || !session.stop_Z()) {
            return "POV ownership changed";
         } else if (!session.next_Z()) {
            return "POV connection changed";
         } else if (session.put_Z()) {
            return "macro took POV ownership";
         } else {
            RemotePlayer bot = PoolEntry.init_RemotePlayer();
            if (bot != null && bot.getId() == handle.init_I()) {
               return !Objects.equals(dimension == null ? "" : dimension, session.start_Str()) ? "POV dimension changed" : null;
            } else {
               return "POV entity changed";
            }
         }
      }
   }

   static b.Inner_DataUtil init_b_DataUtil(EventBus host) {
      ClassCache session = (ClassCache)host;
      synchronized (init_Obj) {
         return init_ClassCache == session ? init_b_DataUtil : null;
      }
   }

   static b.Inner_DataUtil init_b_DataUtil(EventBus host, b.Inner_DataUtil handle) {
      ClassCache session = (ClassCache)host;
      synchronized (init_Obj) {
         if (!init(session, handle)) {
            return null;
         }

         b.Inner_DataUtil corrected = new b.Inner_DataUtil(handle.init_Str(), handle.init_I(), handle.init_J(), handle.load(), handle.read() + 1L);
         init_b_DataUtil = corrected;
         return corrected;
      }
   }

   static boolean init_Z(EventBus host) {
      ClassCache session = (ClassCache)host;
      synchronized (init_Obj) {
         return init_ClassCache == session && init_b_DataUtil != null;
      }
   }

   static void init_V(EventBus host, b.Inner_DataUtil handle) {
      ClassCache session = (ClassCache)host;
      synchronized (init_Obj) {
         if (init(session, handle)) {
            init_ClassCache = null;
            init_b_DataUtil = null;
         }
      }
   }

   private static boolean init(ClassCache session, b.Inner_DataUtil handle) {
      return session != null && init_ClassCache == session && init_b_DataUtil != null && init_b_DataUtil.equals(handle);
   }
}
