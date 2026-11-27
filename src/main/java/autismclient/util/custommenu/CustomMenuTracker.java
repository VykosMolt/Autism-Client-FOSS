package autismclient.util.custommenu;

import autismclient.api.custommenu.CustomMenuSnapshot;
import net.minecraft.network.protocol.Packet;

public final class CustomMenuTracker {
   private static final CustomMenuStateStore init = new CustomMenuStateStore();

   private CustomMenuTracker() {
   }

   public static void init(Packet<?> packet, String phase) {
      init.init(packet, phase);
   }

   public static void load(Packet<?> packet, String phase) {
      init.load(packet, phase);
   }

   public static CustomMenuSnapshot snapshot() {
      return init.snapshot();
   }

   public static long generation() {
      return init.generation();
   }

   public static void init(CustomMenuSnapshot expected, CustomMenuSnapshot replacement) {
      init.init(expected, replacement);
   }

   public static void init(long seenGeneration, CustomMenuSnapshot replacement, String phase) {
      init.init(seenGeneration, replacement, phase);
   }

   public static void clear() {
      init.clear();
   }
}
