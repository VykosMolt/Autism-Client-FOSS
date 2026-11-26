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
   private static Object init_Obj;
   private static Object load_Obj;
   private static String init_Str = "";
   private static String load_Str = "";

   private AiWorldIdentity() {
   }

   static String init(Path path, String name) {
      String normalized = path.toAbsolutePath().normalize().toString();
      if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
         normalized = normalized.toLowerCase(Locale.ROOT);
      }

      return "local/" + AiJson.sha256Hex(normalized) + "/" + name;
   }

   public static synchronized String init(Minecraft mc) {
      IntegratedServer server = mc.getSingleplayerServer();
      if (server != null) {
         if (init_Obj != server) {
            init_Obj = server;
            Path root = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            String base = init(root, server.getWorldData().getLevelName());

            try {
               BasicFileAttributes attributes = Files.readAttributes(root, BasicFileAttributes.class);
               init_Str = attributes.creationTime().toMillis() > 0L
                  ? base + "/created/" + AiJson.sha256Hex(attributes.creationTime().toString())
                  : base + "/session/" + UUID.randomUUID();
            } catch (IOException | SecurityException unavailable) {
               init_Str = base + "/session/" + UUID.randomUUID();
            }
         }

         return init_Str;
      } else {
         init_Obj = null;
         init_Str = "";
         ServerData remote = mc.getCurrentServer();
         Object connection = mc.getConnection();
         if (connection == null && mc.level == null) {
            load_Obj = null;
            load_Str = "";
            return "offline";
         }

         if (load_Obj != connection || load_Str.isEmpty()) {
            load_Obj = connection;
            load_Str = (remote != null ? "server/" + remote.ip.trim().toLowerCase(Locale.ROOT) : "unidentified") + "/session/" + UUID.randomUUID();
         }

         return load_Str;
      }
   }

   public static String load(Minecraft mc) {
      return mc.level == null ? "offline" : mc.level.dimension().identifier().toString();
   }
}
