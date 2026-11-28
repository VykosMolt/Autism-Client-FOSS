package autismclient.util.custommenu;

import autismclient.api.custommenu.CustomMenuSnapshot;
import net.minecraft.network.protocol.Packet;

public final class CustomMenuTracker {
   private static final CustomMenuStateStore store = new CustomMenuStateStore();

   private CustomMenuTracker() {
   }

   public static void init(Packet<?> packet, String phase) {
      store.init(packet, phase);
   }

   public static void load(Packet<?> packet, String phase) {
      store.load(packet, phase);
   }

   public static CustomMenuSnapshot snapshot() {
      return store.snapshot();
   }

   public static long generation() {
      return store.generation();
   }

   public static void init(CustomMenuSnapshot expected, CustomMenuSnapshot replacement) {
      store.init(expected, replacement);
   }

   public static void init(long seenGeneration, CustomMenuSnapshot replacement, String phase) {
      store.init(seenGeneration, replacement, phase);
   }

   public static void clear() {
      store.clear();
   }
}
