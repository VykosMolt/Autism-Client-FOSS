package autismclient.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public final class OnlinePlayerCollector {
   private static final int init = 5000;

   private OnlinePlayerCollector() {
   }

   public static List<String> init(Minecraft mc, boolean probeHidden) {
      return init(mc, probeHidden, () -> false);
   }

   public static List<String> init(Minecraft mc, boolean probeHidden, final BooleanSupplier cancelled) {
      String self = init(mc);
      LinkedHashMap<String, String> names = CommandTabNameProber.init(mc, self);
      final ClientPacketListener connection = mc == null ? null : mc.getConnection();
      if (probeHidden && connection != null) {
         List<CommandTabNameProber.Inner_StreamUtil> vectors = CommandTabNameProber.discoverVectors(connection);
         CommandTabNameProber.init(connection, names, self, vectors, new CommandTabNameProber.Inner_DataUtil() {
            @Override
            public boolean isCancelled() {
               Minecraft m = Minecraft.getInstance();
               return cancelled != null && cancelled.getAsBoolean() || m == null || m.getConnection() != connection;
            }

            @Override
            public boolean isPaused() {
               return false;
            }

            @Override
            public int maxNames() {
               return 2147483647;
            }

            @Override
            public int maxQueries() {
               return 5000;
            }
         });
      }

      return new ArrayList<>(names.values());
   }

   private static String init(Minecraft mc) {
      return mc != null && mc.player != null ? mc.player.getName().getString() : "";
   }
}
