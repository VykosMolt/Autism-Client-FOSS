package autismclient.ai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;

public final class AiWorldIdentity {
   private static Object cachedIntegratedServer;
   private static Object cachedConnection;
   private static String cachedLocalWorldKey = "";
   private static String cachedRemoteWorldKey = "";

   private AiWorldIdentity() {
   }

   static String localWorldPrefix(Path path, String name) {
      String normalized = path.toAbsolutePath().normalize().toString();
      if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
         normalized = normalized.toLowerCase(Locale.ROOT);
      }

      return "local/" + AiJson.sha256Hex(normalized) + "/" + name;
   }

   public static synchronized String currentWorldKey(Minecraft mc) {
      IntegratedServer server = mc.getSingleplayerServer();
      if (server != null) {
         if (cachedIntegratedServer != server) {
            cachedIntegratedServer = server;
            Path root = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            String base = localWorldPrefix(root, server.getWorldData().getLevelName());

            try {
               BasicFileAttributes attributes = Files.readAttributes(root, BasicFileAttributes.class);
               cachedLocalWorldKey = attributes.creationTime().toMillis() > 0L
                  ? base + "/created/" + AiJson.sha256Hex(attributes.creationTime().toString())
                  : base + "/session/" + UUID.randomUUID();
            } catch (IOException | SecurityException unavailable) {
               cachedLocalWorldKey = base + "/session/" + UUID.randomUUID();
            }
         }

         return cachedLocalWorldKey;
      } else {
         cachedIntegratedServer = null;
         cachedLocalWorldKey = "";
         ServerData remote = mc.getCurrentServer();
         Object connection = mc.getConnection();
         if (connection == null && mc.level == null) {
            cachedConnection = null;
            cachedRemoteWorldKey = "";
            return "offline";
         }

         if (cachedConnection != connection || cachedRemoteWorldKey.isEmpty()) {
            cachedConnection = connection;
            cachedRemoteWorldKey = (remote != null ? "server/" + remote.ip.trim().toLowerCase(Locale.ROOT) : "unidentified") + "/session/" + UUID.randomUUID();
         }

         return cachedRemoteWorldKey;
      }
   }

   public static String currentDimensionId(Minecraft mc) {
      return mc.level == null ? "offline" : mc.level.dimension().identifier().toString();
   }
}
