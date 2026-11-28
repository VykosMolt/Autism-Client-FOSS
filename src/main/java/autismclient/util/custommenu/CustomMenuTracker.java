package autismclient.util.custommenu;

import autismclient.api.custommenu.CustomMenuSnapshot;
import net.minecraft.network.protocol.Packet;

public final class CustomMenuTracker {
   private static final CustomMenuStateStore store = new CustomMenuStateStore();

   private CustomMenuTracker() {
   }

   public static void onInbound(Packet<?> packet, String phase) {
      store.acceptInbound(packet, phase);
   }

   public static void onPacket(Packet<?> packet, String phase) {
      store.handle(packet, phase);
   }

   public static CustomMenuSnapshot snapshot() {
      return store.snapshot();
   }

   public static long generation() {
      return store.generation();
   }

   public static void replace(CustomMenuSnapshot expected, CustomMenuSnapshot replacement) {
      store.replace(expected, replacement);
   }

   public static void replace(long seenGeneration, CustomMenuSnapshot replacement, String phase) {
      store.replace(seenGeneration, replacement, phase);
   }

   public static void clear() {
      store.clear();
   }
}
