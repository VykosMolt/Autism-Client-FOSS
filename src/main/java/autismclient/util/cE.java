package autismclient.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public final class cE {
   public static final boolean init = false;

   private cE() {
   }

   public static boolean init_Z(AutismConfig config) {
      if (config == null) {
         return false;
      }

      String current = init();
      String shown = config.welcomeInstallIdentity == null ? "" : config.welcomeInstallIdentity;
      return !current.equals(shown);
   }

   public static void init_V(AutismConfig config) {
      if (config != null) {
         config.welcomeShown = true;
         config.welcomeInstallIdentity = init();
         config.save();
      }
   }

   private static String init() {
      try {
         Optional<ModContainer> optional = FabricLoader.getInstance().getModContainer("autism");
         if (optional.isEmpty()) {
            return "autism:unknown";
         }

         ModContainer container = optional.get();
         StringBuilder id = new StringBuilder();
         id.append(container.getMetadata().getId()).append(':').append(container.getMetadata().getVersion().getFriendlyString());

         for (Path path : container.getOrigin().getPaths()) {
            init(id, path);
         }

         return id.toString();
      } catch (Throwable ignored) {
         return "autism:unknown";
      }
   }

   private static void init(StringBuilder id, Path path) {
      if (path != null) {
         try {
            Path normalized = path.toAbsolutePath().normalize();
            id.append('|').append(normalized.getFileName());
            if (Files.isRegularFile(normalized)) {
               id.append(':').append(Files.size(normalized)).append(':').append(Files.getLastModifiedTime(normalized).toMillis());
            } else if (Files.isDirectory(normalized)) {
               id.append(":dir:").append(Files.getLastModifiedTime(normalized).toMillis());
            }
         } catch (Throwable ignored) {
            id.append('|').append(path);
         }
      }
   }
}
