package autismclient.ai;

import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.storage.LevelStorageSource;

public class AiWorldDeleteCheck {
   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL AiWorldDeleteCheck: " + what);
         System.exit(1);
      }
   }

   public static void main(String[] args) throws Exception {
      SharedConstants.tryDetectVersion();
      Bootstrap.bootStrap();
      Path root = Files.createTempDirectory("ai-worlds");
      Path saves = Files.createDirectories(root.resolve("game/saves"));
      Files.createDirectories(saves.resolve("World 1"));
      Files.writeString(saves.resolve("World 1/level.dat"), "");
      Files.createDirectories(saves.resolve("not a world"));
      Path victim = Files.createDirectories(root.resolve("victim"));
      Files.writeString(victim.resolve("level.dat"), "");
      LevelStorageSource source = LevelStorageSource.createDefault(saves);

      check(AiWorldCreate.auditIsSaveWorld(source, "World 1"), "listed world accepted");
      check(!AiWorldCreate.auditIsSaveWorld(source, "../../victim"), "relative escape rejected");
      check(!AiWorldCreate.auditIsSaveWorld(source, victim.toString()), "absolute path rejected");
      check(!AiWorldCreate.auditIsSaveWorld(source, ".."), "parent rejected");
      check(!AiWorldCreate.auditIsSaveWorld(source, "not a world"), "folder without level.dat rejected");
      System.out.println("PASS AiWorldDeleteCheck");
   }
}
