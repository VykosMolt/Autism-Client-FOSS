package autismclient.util.b;

import autismclient.api.custommenu.CustomMenuSnapshot;
import net.minecraft.network.protocol.Packet;

public final class CachePool {
   private static final StreamUtil init = new StreamUtil();

   private CachePool() {
   }

   public static void init(Packet<?> packet, String phase) {
      init.init(packet, phase);
   }

   public static void load(Packet<?> packet, String phase) {
      init.load(packet, phase);
   }

   public static CustomMenuSnapshot init_CustomMenuSnapshot() {
      return init.init_CustomMenuSnapshot();
   }

   public static long init_J() {
      return init.init_J();
   }

   public static void init(CustomMenuSnapshot expected, CustomMenuSnapshot replacement) {
      init.init(expected, replacement);
   }

   public static void init(long seenGeneration, CustomMenuSnapshot replacement, String phase) {
      init.init(seenGeneration, replacement, phase);
   }

   public static void init_V() {
      init.init_V();
   }
}
