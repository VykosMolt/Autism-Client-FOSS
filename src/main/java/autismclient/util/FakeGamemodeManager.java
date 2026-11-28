package autismclient.util;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameType;

public final class FakeGamemodeManager {
   private static boolean active;
   private static boolean load_Z;
   private static GameType realMode;
   private static GameType fakeMode;
   private static long init_J;
   private static long load_J;

   private FakeGamemodeManager() {
   }

   public static synchronized FakeGamemodeManager.ApplyResult applyFake(GameType mode) {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || mc.player == null || mc.gameMode == null) {
         return FakeGamemodeManager.ApplyResult.fail("Not connected.");
      }

      if (mode == null) {
         return FakeGamemodeManager.ApplyResult.fail("Unknown game mode.");
      }

      if (!active) {
         realMode = mc.gameMode.getPlayerMode();
      }

      GameType previousDisplayed = effectiveMode();
      active = true;
      fakeMode = mode;
      if (previousDisplayed != mode) {
         load_J++;
      }

      init(mc, mode);
      return FakeGamemodeManager.ApplyResult.ok("Fake gamemode: " + mode.getName());
   }

   public static synchronized FakeGamemodeManager.ApplyResult clearFake() {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || mc.player == null || mc.gameMode == null) {
         reset();
         return FakeGamemodeManager.ApplyResult.fail("Not connected.");
      }

      if (!active) {
         realMode = mc.gameMode.getPlayerMode();
         return FakeGamemodeManager.ApplyResult.ok("Fake gamemode already reset.");
      }

      GameType restore = realMode != null ? realMode : GameType.DEFAULT_MODE;
      GameType previousDisplayed = effectiveMode();
      active = false;
      fakeMode = null;
      if (previousDisplayed != restore) {
         load_J++;
      }

      init(mc, restore);
      return FakeGamemodeManager.ApplyResult.ok("Fake gamemode reset: " + restore.getName());
   }

   public static synchronized void onRealGamemodeChanged(GameType mode) {
      if (!load_Z && mode != null) {
         if (realMode != mode) {
            realMode = mode;
            init_J++;
         }

         if (active && fakeMode != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player != null && mc.gameMode != null && mc.gameMode.getPlayerMode() != fakeMode) {
               init(mc, fakeMode);
            }
         }
      }
   }

   public static synchronized void reset() {
      active = false;
      load_Z = false;
      realMode = null;
      fakeMode = null;
      init_J++;
      load_J++;
   }

   public static synchronized FakeGamemodeManager.GamemodeSnapshot snapshot() {
      Minecraft mc = Minecraft.getInstance();
      GameType current = mc != null && mc.gameMode != null ? mc.gameMode.getPlayerMode() : null;
      GameType real = realMode != null ? realMode : current;
      GameType displayed = active && fakeMode != null ? fakeMode : real;
      return new FakeGamemodeManager.GamemodeSnapshot(real, displayed, active, init_J, load_J);
   }

   public static GameType parseMode(String input) {
      if (input == null) {
         return null;
      }

      GameType var10000;
      switch (input.trim().toLowerCase(Locale.ROOT)) {
         case "s":
         case "0":
         case "survival":
            var10000 = GameType.SURVIVAL;
            break;
         case "c":
         case "1":
         case "creative":
            var10000 = GameType.CREATIVE;
            break;
         case "a":
         case "2":
         case "adventure":
            var10000 = GameType.ADVENTURE;
            break;
         case "sp":
         case "spec":
         case "3":
         case "spectator":
            var10000 = GameType.SPECTATOR;
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   public static String displayName(GameType mode) {
      if (mode == null) {
         return "Reset";
      }

      String name = mode.getName();
      return name.isEmpty() ? mode.name() : Character.toUpperCase(name.charAt(0)) + name.substring(1);
   }

   private static void init(Minecraft mc, GameType mode) {
      load_Z = true;

      try {
         mc.gameMode.setLocalMode(mode);
      } finally {
         load_Z = false;
      }
   }

   private static GameType effectiveMode() {
      if (active && fakeMode != null) {
         return fakeMode;
      }

      if (realMode != null) {
         return realMode;
      }

      Minecraft mc = Minecraft.getInstance();
      return mc != null && mc.gameMode != null ? mc.gameMode.getPlayerMode() : null;
   }

   public record ApplyResult(boolean success, String message) {
      public static FakeGamemodeManager.ApplyResult ok(String message) {
         return new FakeGamemodeManager.ApplyResult(true, message == null ? "" : message);
      }

      public static FakeGamemodeManager.ApplyResult fail(String message) {
         return new FakeGamemodeManager.ApplyResult(false, message == null ? "" : message);
      }
   }

   public record GamemodeSnapshot(GameType realMode, GameType displayedMode, boolean fakeActive, long realRevision, long fakeRevision) {
   }
}
