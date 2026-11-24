package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.LevelSettings.DifficultySettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelDirectory;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;

final class cK {
   private cK() {
   }

   static AiOperation init_AiOperation(final JsonObject args) {
      final String name = ag.init_Str_JsonObject(args, "name", "").trim();
      if (name.isBlank() || name.length() > 64) {
         throw new IllegalArgumentException("name required (1..64 characters)");
      }

      if (!name.matches("[\\p{L}\\p{N} _.'\\-]+") || name.equals(".") || name.equals("..") || name.contains("..")) {
         throw new IllegalArgumentException("name contains characters that are unsafe for a world folder");
      }

      GameType var10000;
      switch (ag.init_Str_JsonObject(args, "gamemode", "survival").toLowerCase(Locale.ROOT)) {
         case "survival":
            var10000 = GameType.SURVIVAL;
            break;
         case "creative":
            var10000 = GameType.CREATIVE;
            break;
         case "adventure":
            var10000 = GameType.ADVENTURE;
            break;
         case "spectator":
            var10000 = GameType.SPECTATOR;
            break;
         default:
            throw new IllegalArgumentException("gamemode must be survival, creative, adventure or spectator");
      }

      final GameType gameType = var10000;
      Difficulty var14;
      switch (ag.init_Str_JsonObject(args, "difficulty", "normal").toLowerCase(Locale.ROOT)) {
         case "peaceful":
            var14 = Difficulty.PEACEFUL;
            break;
         case "easy":
            var14 = Difficulty.EASY;
            break;
         case "normal":
            var14 = Difficulty.NORMAL;
            break;
         case "hard":
            var14 = Difficulty.HARD;
            break;
         default:
            throw new IllegalArgumentException("difficulty must be peaceful, easy, normal or hard");
      }

      final Difficulty difficulty = var14;
      final boolean cheats = ag.init_Z_JsonObject(args, "cheats", false);
      final boolean hardcore = ag.init_Z_JsonObject(args, "hardcore", false);
      final boolean bonusChest = ag.init_Z_JsonObject(args, "bonusChest", false);
      final boolean structures = ag.init_Z_JsonObject(args, "structures", true);
      final Long seed = args.has("seed") && args.get("seed").isJsonPrimitive() ? args.get("seed").getAsLong() : null;
      final Minecraft mc = Minecraft.getInstance();
      final Object initialLevel = mc.level;
      return new AiOperation() {
         final long init_J = System.nanoTime() + Math.max(10, Math.min(300, ag.init_I_JsonObject(args, "timeoutSeconds", 120))) * 1000000000L;
         boolean init_Z;
         boolean load;
         String init_Str = "";

         void init() {
            this.init_Z = true;

            try {
               LevelStorageSource source = mc.getLevelSource();
               String levelId = name;
               if (source.levelExists(levelId)) {
                  throw new IllegalArgumentException("World '" + levelId + "' already exists; delete it or choose another name");
               }

               LevelSettings settings = new LevelSettings(
                  name, gameType, new DifficultySettings(difficulty, hardcore, false), cheats, WorldDataConfiguration.DEFAULT
               );
               WorldOptions options = seed == null
                  ? WorldOptions.defaultWithRandomSeed().withStructures(structures).withBonusChest(bonusChest)
                  : new WorldOptions(seed, structures, bonusChest);
               new WorldOpenFlows(mc, source)
                  .createFreshLevel(levelId, settings, options, registry -> WorldPresets.createNormalWorldDimensions(registry), null);
            } catch (IllegalArgumentException e) {
               this.load = true;
               this.init_Str = e.getMessage();
            } catch (Throwable e) {
               this.load = true;
               this.init_Str = "World creation failed: " + e;
            }
         }

         @Override
         public AiOperation.Result tick() {
            if (this.load) {
               return R.init_AiOperation_Result(AiOperation.Status.FAILED, this.init_Str, ag.init_JsonObject_ObjArr("name", name));
            } else if (!this.init_Z) {
               this.init();
               return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Creating world " + name, new JsonObject());
            } else if (mc.player != null && mc.level != null && mc.level != initialLevel) {
               return R.init_AiOperation_Result(
                  AiOperation.Status.SUCCEEDED,
                  "World " + name + " created and joined" + (seed == null ? " (random seed)" : " with seed " + seed),
                  ag.init_JsonObject_ObjArr(
                     "name", name, "seed", seed == null ? "random" : String.valueOf(seed), "gamemode", gameType.name(), "difficulty", difficulty.name()
                  )
               );
            } else {
               return System.nanoTime() - this.init_J >= 0L
                  ? R.init_AiOperation_Result(
                     AiOperation.Status.TIMED_OUT,
                     "World did not load within the deadline; inspect the current screen",
                     ag.init_JsonObject_ObjArr("name", name)
                  )
                  : R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Loading world " + name, new JsonObject());
            }
         }

         @Override
         public void cancel() {
         }
      };
   }

   static JsonObject init_JsonObject(JsonObject args) {
      JsonArray worlds = new JsonArray();

      try {
         for (LevelDirectory directory : Minecraft.getInstance().getLevelSource().findLevelCandidates().levels()) {
            worlds.add(directory.directoryName());
         }
      } catch (Throwable e) {
         throw new IllegalStateException("Could not list worlds: " + e.getMessage());
      }

      return ag.init_JsonObject_ObjArr("worlds", worlds);
   }

   static JsonObject load(JsonObject args) {
      String name = ag.init_Str_JsonObject(args, "name", "").trim();
      if (name.isBlank()) {
         throw new IllegalArgumentException("name required");
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         throw new IllegalArgumentException("Leave the current world before deleting worlds");
      }

      if (!auditIsSaveWorld(mc.getLevelSource(), name)) {
         throw new IllegalArgumentException("Unknown world '" + name + "'");
      }

      try (LevelStorageAccess access = mc.getLevelSource().createAccess(name)) {
         access.deleteLevel();
      } catch (Throwable e) {
         if (!(e instanceof NoSuchFileException) && !(e instanceof FileNotFoundException)) {
            throw new IllegalStateException("Delete failed for '" + name + "': " + e);
         }

         throw new IllegalArgumentException("Unknown world '" + name + "'");
      }

      return ag.init_JsonObject_ObjArr("deleted", name);
   }

   // Only a world the save list shows, so a path can't reach outside the saves folder.
   static boolean auditIsSaveWorld(LevelStorageSource source, String name) {
      try {
         for (LevelDirectory directory : source.findLevelCandidates().levels()) {
            if (directory.directoryName().equals(name)) {
               return true;
            }
         }
      } catch (Exception e) {
      }

      return false;
   }
}
