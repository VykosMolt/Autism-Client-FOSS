package autismclient.ai;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

public class AiAddonPathCheck {
   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL AiAddonPathCheck: " + what);
         System.exit(1);
      }
   }

   static boolean accepted(Path addon, String relative) {
      try {
         CachePool.init(addon, relative);
         return true;
      } catch (IllegalArgumentException e) {
         return false;
      }
   }

   public static void main(String[] args) throws Exception {
      Path root = Files.createTempDirectory("ai-addon");
      Path outside = Files.createDirectories(root.resolve("outside"));
      Files.writeString(outside.resolve("victim.txt"), "keep");
      Path addon = Files.createDirectories(root.resolve("addon"));
      Files.createDirectories(addon.resolve("src"));
      Files.createSymbolicLink(addon.resolve("link"), outside);
      Files.createSymbolicLink(addon.resolve("dangling.java"), outside.resolve("new.java"));

      // M9: writes must not go through a symlink
      check(accepted(addon, "src/Main.java"), "plain path accepted");
      check(accepted(addon, "src/new/Deep.java"), "new subdirectory accepted");
      check(!accepted(addon, "link/Evil.java"), "path through a dir symlink rejected");
      check(!accepted(addon, "link/victim.txt"), "existing file behind a symlink rejected");
      check(!accepted(addon, "dangling.java"), "dangling symlink rejected");
      check(!accepted(addon, "../outside/x.java"), "lexical escape still rejected");
      check(accepted(root.resolve("missing"), "src/A.java"), "addon dir not created yet accepted");

      // A8: cleanup deletes the link, not what it points to
      Method cleanup = CachePool.class.getDeclaredMethod("load", Path.class);
      cleanup.setAccessible(true);
      cleanup.invoke(null, addon);
      check(!Files.exists(addon), "addon dir removed");
      check(Files.isRegularFile(outside.resolve("victim.txt")), "file behind the symlink survives");
      System.out.println("PASS AiAddonPathCheck");
   }
}
