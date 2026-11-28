package autismclient.ai;

import autismclient.AutismClientAddon;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

final class AiClipboard {
   private static final int init_I = 32;
   private static final Map<String, List<AiBuilder.CellSpec>> cache = new LinkedHashMap<>();

   private AiClipboard() {
   }

   static Path defaultDirectory() {
      return AutismClientAddon.FOLDER.toPath().resolve("ai").resolve("clipboard");
   }

   static String validateName(String name) {
      String clean = name == null ? "" : name.trim();
      if (clean.isEmpty() || clean.length() > 128) {
         throw new IllegalArgumentException("clipboard name must be 1..128 characters");
      } else if (!clean.contains("..") && !clean.contains("/") && !clean.contains("\\") && !clean.contains(":")) {
         return clean;
      } else {
         throw new IllegalArgumentException("clipboard name must not contain path separators or '..': " + name);
      }
   }

   static synchronized void saveClip(String name, List<AiBuilder.CellSpec> cells) {
      saveClip(name, cells, defaultDirectory());
   }

   static synchronized void saveClip(String name, List<AiBuilder.CellSpec> cells, Path directory) {
      cache.remove(name);
      cache.put(name, cells);

      while (cache.size() > 32) {
         cache.remove(cache.keySet().iterator().next());
      }

      try {
         AiJson.writeAtomicJson(directory.resolve(name + ".json"), toJson(cells));
      } catch (IOException e) {
         throw new IllegalStateException("Could not persist clipboard \"" + name + "\": " + e.getMessage(), e);
      }
   }

   static synchronized List<AiBuilder.CellSpec> load(String name) {
      return loadClip(name, defaultDirectory());
   }

   static synchronized List<AiBuilder.CellSpec> loadClip(String name, Path directory) {
      List<AiBuilder.CellSpec> cells = cache.get(name);
      if (cells != null) {
         return cells;
      }

      Path file = directory.resolve(name + ".json");
      if (!Files.isRegularFile(file)) {
         throw new IllegalArgumentException("No clipboard named \"" + name + "\"; game:copy first (stored: " + listClips(directory) + ")");
      }

      try {
         if (Files.size(file) > 8000000L) {
            throw new IllegalArgumentException("Clipboard \"" + name + "\" exceeds the supported size");
         }

         cells = fromJson(AiJson.parseObject(Files.readString(file)));
      } catch (IllegalArgumentException e) {
         throw e;
      } catch (Exception e) {
         throw new IllegalArgumentException("Clipboard \"" + name + "\" could not be read: " + e.getMessage());
      }

      cache.put(name, cells);
      return cells;
   }

   static synchronized String listClips(Path directory) {
      List<String> names = new ArrayList<>(cache.keySet());
      if (Files.isDirectory(directory)) {
         try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".json"))
               .map(p -> p.getFileName().toString().replaceAll("\\.json$", ""))
               .filter(n -> !names.contains(n))
               .sorted()
               .forEach(names::add);
         } catch (IOException var7) {
         }
      }

      return names.isEmpty() ? "(none)" : String.join(", ", names);
   }

   static JsonObject toJson(List<AiBuilder.CellSpec> cells) {
      JsonArray array = new JsonArray();

      for (AiBuilder.CellSpec cell : cells) {
         JsonObject entry = AiJson.objectOf("dx", cell.dx(), "dy", cell.dy(), "dz", cell.dz(), "block", cell.block());
         if (!cell.properties().isEmpty()) {
            JsonObject properties = new JsonObject();
            cell.properties().forEach(properties::addProperty);
            entry.add("properties", properties);
         }

         array.add(entry);
      }

      return AiJson.objectOf("version", 1, "cells", array);
   }

   static List<AiBuilder.CellSpec> fromJson(JsonObject root) {
      if (AiJson.getInt(root, "version", 0) != 1) {
         throw new IllegalArgumentException("Unsupported clipboard version");
      }

      if (root.has("cells") && root.get("cells").isJsonArray()) {
         JsonArray array = root.getAsJsonArray("cells");
         if (array.size() > 4096) {
            throw new IllegalArgumentException("Clipboard has " + array.size() + " cells; the limit is 4096");
         }

         List<AiBuilder.CellSpec> cells = new ArrayList<>(array.size());

         for (int i = 0; i < array.size(); i++) {
            JsonObject entry = array.get(i).getAsJsonObject();
            String context = "cells[" + i + "]";
            int dx = AiJson.getInt(entry, "dx", 0);
            int dy = AiJson.getInt(entry, "dy", 0);
            int dz = AiJson.getInt(entry, "dz", 0);
            if (Math.abs(dx) > 256 || Math.abs(dy) > 256 || Math.abs(dz) > 256) {
               throw new IllegalArgumentException(context + " offsets must be within +/-256");
            }

            String block = AiJson.getString(entry, "block", "");
            Identifier id = Identifier.tryParse(block);
            Block blockObj = id == null ? null : BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
            if (blockObj == null) {
               throw new IllegalArgumentException(context + " names an unknown block id: " + block);
            }

            cells.add(new AiBuilder.CellSpec(dx, dy, dz, block, AiBuilder.parseBlockProperties(entry, context, blockObj)));
         }

         return List.copyOf(cells);
      } else {
         throw new IllegalArgumentException("Clipboard has no cells array");
      }
   }
}
