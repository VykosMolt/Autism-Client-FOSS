package autismclient.ai.a;

import java.util.function.Function;
import java.util.function.LongPredicate;

final class l {
   static final int init = 30;
   static final int load = -2;
   static final int read = 8;
   static final int write = 8;

   private l() {
   }

   static long init_J_D_D(double x, double z) {
      return init_J_I_I((int)Math.floor(x), (int)Math.floor(z));
   }

   static long init_J_I_I(int x, int z) {
      return (long)x << 32 ^ z & 4294967295L;
   }

   static boolean init(NodeWalker.Inner_StreamUtil down, LongPredicate allowed, NodeWalker.Inner_CachePool w) {
      NodeWalker.Inner_StreamUtil cur = down;
      NodeWalker.Inner_DataUtil none = new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, false, 0.0F);

      for (int t = 0; t < 8; t++) {
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(cur, none, w);
         if (NodeWalker.init_Z(w, cur, next)) {
            return false;
         }

         cur = next;
         if (!cur.onGround() || Math.abs(cur.y() - down.y()) > 0.01 || !allowed.test(init_J_D_D(cur.x(), cur.z()))) {
            return false;
         }
      }

      return true;
   }

   static boolean init(NodeWalker.Inner_StreamUtil down, NodeWalker.Inner_DataUtil run, double surface, LongPredicate allowed, NodeWalker.Inner_CachePool w) {
      NodeWalker.Inner_StreamUtil cur = down;
      NodeWalker.Inner_DataUtil keys = run.withJump(false);

      for (int t = 0; t < 8; t++) {
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(cur, keys, w);
         if (NodeWalker.init_Z(w, cur, next)) {
            return false;
         }

         cur = next;
         if (!cur.onGround() || Math.abs(cur.y() - surface) > 0.01) {
            return false;
         }

         if (allowed.test(init_J_D_D(cur.x(), cur.z()))) {
            return init(cur, allowed, w);
         }
      }

      return false;
   }

   static boolean init(
      NodeWalker.Inner_StreamUtil state,
      double surface,
      LongPredicate allowed,
      NodeWalker.Inner_CachePool world,
      Function<NodeWalker.Inner_StreamUtil, NodeWalker.Inner_DataUtil> controls
   ) {
      if (!state.onGround()) {
         return false;
      }

      double floor = state.y();
      int tick = 0;

      while (tick < 8) {
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(state, NodeWalker.Inner_DataUtil.NONE, world);
         if (next.onGround() && !(Math.abs(next.y() - floor) > 0.01) && !NodeWalker.init_Z(world, state, next)) {
            NodeWalker.Inner_DataUtil input = controls.apply(next);
            if (init(next, input, 0, surface, allowed, world) <= 0 && init(next, input, 1, surface, allowed, world) <= 0) {
               state = next;
               tick++;
               continue;
            }

            return true;
         }

         return false;
      }

      return false;
   }

   static l.Inner_StreamUtil init(NodeWalker.Inner_StreamUtil s, NodeWalker.Inner_DataUtil keys, boolean jump, NodeWalker.Inner_CachePool w) {
      NodeWalker.Inner_StreamUtil cur = s;
      boolean struck = false;
      boolean airborne = !s.onGround();

      for (int t = 1; t <= 30; t++) {
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(cur, keys.withJump(jump && t == 1), w);
         if (NodeWalker.init_Z(w, cur, next)) {
            return new l.Inner_StreamUtil(null, t, struck);
         }

         cur = next;
         struck |= cur.horizontalCollision();
         if (!cur.onGround()) {
            airborne = true;
         } else if (airborne) {
            return new l.Inner_StreamUtil(cur, t, struck);
         }
      }

      return new l.Inner_StreamUtil(null, 30, struck);
   }

   static int init(NodeWalker.Inner_StreamUtil s, NodeWalker.Inner_DataUtil run, int delay, double surface, LongPredicate allowed, NodeWalker.Inner_CachePool w) {
      NodeWalker.Inner_StreamUtil cur = s;
      boolean jumped = false;

      for (int t = 1; t <= 30; t++) {
         boolean press = !jumped && t > delay && cur.onGround();
         NodeWalker.Inner_StreamUtil next = NodeWalker.init(cur, run.withJump(press), w);
         if (NodeWalker.init_Z(w, cur, next)) {
            return -2;
         }

         cur = next;
         jumped |= press;
         if (cur.onGround()) {
            if (cur.y() >= surface - 0.001) {
               return !init(cur, allowed, w) && !init(cur, run, surface, allowed, w) ? -1 : t;
            }

            if (jumped && t > delay + 1) {
               return -1;
            }
         }
      }

      return -1;
   }

   static boolean init_Z_I_I(int now, int later) {
      return now >= 0 && (later < 0 || now <= later);
   }

   static l.Inner_DataUtil init(
      NodeWalker.Inner_StreamUtil s,
      NodeWalker.Inner_DataUtil go,
      NodeWalker.Inner_DataUtil none,
      NodeWalker.Inner_DataUtil brake,
      LongPredicate allowed,
      boolean far,
      double tx,
      double tz,
      NodeWalker.Inner_CachePool w
   ) {
      l.Inner_DataUtil best = null;
      double bestScore = 1.7976931348623157E308;
      l.Inner_DataUtil nearest = l.Inner_DataUtil.load;
      double nearestDistance = 1.7976931348623157E308;

      for (l.Inner_DataUtil choice : l.Inner_DataUtil.values()) {
         NodeWalker.Inner_DataUtil keys = choice == l.Inner_DataUtil.init ? go : (choice == l.Inner_DataUtil.load ? none : brake);
         l.Inner_StreamUtil landing = init(s, keys, false, w);
         if (landing.down()) {
            double distance = Math.hypot(landing.at().x() - tx, landing.at().z() - tz);
            if (distance < nearestDistance) {
               nearestDistance = distance;
               nearest = choice;
            }

            if (allowed.test(init_J_D_D(landing.at().x(), landing.at().z())) && init(landing.at(), allowed, w)) {
               if (far) {
                  return choice;
               }

               if (distance < bestScore) {
                  bestScore = distance;
                  best = choice;
               }
            }
         }
      }

      return best != null ? best : nearest;
   }

   enum Inner_DataUtil {
      init,
      load,
      read;
   }

   record Inner_StreamUtil(NodeWalker.Inner_StreamUtil at, int ticks, boolean struck) {
      boolean down() {
         return this.at != null;
      }
   }
}
