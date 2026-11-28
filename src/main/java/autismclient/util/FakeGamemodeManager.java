package autismclient.util;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameType;

public final class FakeGamemodeManager {
   private static boolean init_Z;
   private static boolean load_Z;
   private static GameType init_GameType;
   private static GameType load_GameType;
   private static long init_J;
   private static long load_J;

   private FakeGamemodeManager() {
   }

   public static synchronized FakeGamemodeManager.ApplyResult applyFake(GameType mode) {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || mc.player == null || mc.gameMode == null) {
         return FakeGamemodeManager.ApplyResult.load("Not connected.");
      }

      if (mode == null) {
         return FakeGamemodeManager.ApplyResult.load("Unknown game mode.");
      }

      if (!init_Z) {
         init_GameType = mc.gameMode.getPlayerMode();
      }

      GameType previousDisplayed = init_GameType();
      init_Z = true;
      load_GameType = mode;
      if (previousDisplayed != mode) {
         load_J++;
      }

      init(mc, mode);
      return FakeGamemodeManager.ApplyResult.init("Fake gamemode: " + mode.getName());
   }

   public static synchronized FakeGamemodeManager.ApplyResult clearFake() {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || mc.player == null || mc.gameMode == null) {
         reset();
         return FakeGamemodeManager.ApplyResult.load("Not connected.");
      }

      if (!init_Z) {
         init_GameType = mc.gameMode.getPlayerMode();
         return FakeGamemodeManager.ApplyResult.init("Fake gamemode already reset.");
      }

      GameType restore = init_GameType != null ? init_GameType : GameType.DEFAULT_MODE;
      GameType previousDisplayed = init_GameType();
      init_Z = false;
      load_GameType = null;
      if (previousDisplayed != restore) {
         load_J++;
      }

      init(mc, restore);
      return FakeGamemodeManager.ApplyResult.init("Fake gamemode reset: " + restore.getName());
   }

   public static synchronized void onRealGamemodeChanged(GameType mode) {
      if (!load_Z && mode != null) {
         if (init_GameType != mode) {
            init_GameType = mode;
            init_J++;
         }

         if (init_Z && load_GameType != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player != null && mc.gameMode != null && mc.gameMode.getPlayerMode() != load_GameType) {
               init(mc, load_GameType);
            }
         }
      }
   }

   public static synchronized void reset() {
      init_Z = false;
      load_Z = false;
      init_GameType = null;
      load_GameType = null;
      init_J++;
      load_J++;
   }

   public static synchronized FakeGamemodeManager.GamemodeSnapshot snapshot() {
      Minecraft mc = Minecraft.getInstance();
      GameType current = mc != null && mc.gameMode != null ? mc.gameMode.getPlayerMode() : null;
      GameType real = init_GameType != null ? init_GameType : current;
      GameType displayed = init_Z && load_GameType != null ? load_GameType : real;
      return new FakeGamemodeManager.GamemodeSnapshot(real, displayed, init_Z, init_J, load_J);
   }

   public static GameType init(String input) {
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

   public static String init_Str(GameType mode) {
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

   private static GameType init_GameType() {
      if (init_Z && load_GameType != null) {
         return load_GameType;
      }

      if (init_GameType != null) {
         return init_GameType;
      }

      Minecraft mc = Minecraft.getInstance();
      return mc != null && mc.gameMode != null ? mc.gameMode.getPlayerMode() : null;
   }

   public record ApplyResult(boolean success, String message) {
      public static FakeGamemodeManager.ApplyResult init(String message) {
         return new FakeGamemodeManager.ApplyResult(true, message == null ? "" : message);
      }

      public static FakeGamemodeManager.ApplyResult load(String message) {
         return new FakeGamemodeManager.ApplyResult(false, message == null ? "" : message);
      }
   }

   public record GamemodeSnapshot(GameType realMode, GameType displayedMode, boolean fakeActive, long realRevision, long fakeRevision) {
   }
}
